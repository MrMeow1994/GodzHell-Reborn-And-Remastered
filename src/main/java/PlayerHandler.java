import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

import com.Ghreborn.jagcached.util.DeepTracer;
import com.Ghreborn.jagcached.util.HyperTraceEngine;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mystycdh.GuardianServer;

import java.io.FileWriter;
import java.io.IOException;

public class PlayerHandler {

    // Remark: the player structures are just a temporary solution for now
    // Later we will avoid looping through all the players for each player
    // by making use of a hash table maybe based on map regions (8x8 granularity should be ok)
    public static final int maxPlayers = 1000;
    public static Player[] players = new Player[maxPlayers];
    public static String kickNick = "";
    private final boolean[] slotFree = new boolean[maxPlayers];
    public static boolean kickAllPlayers = false;
    public static String messageToAll = "";
    public static int playerCount = 0;
    public static boolean updateAnnounced;
    public static boolean updateRunning;
    public static int updateSeconds;
    public static long updateStartTime;
    // holds indices of all currently-free player slots
    private final ArrayDeque<Integer> freeSlots = new ArrayDeque<>(maxPlayers);

    public static String[] playersCurrentlyOn = new String[maxPlayers];
    private static final Map<String, Player> playerByUsername = new HashMap<>();
    // where we start searching at when adding a new player
    public int lastchatid = 1; //PM System

    PlayerHandler() {
        Arrays.fill(players, null);
        initFreeSlots();
    }

    public static int[] toIntArray(ArrayList<Integer> integerList) {
        int[] intArray = new int[integerList.size()];

        for (int i = 0; i < integerList.size(); i++) {
            intArray[i] = integerList.get(i);
        }

        return intArray;
    }
    public void fullyWipePlayerPresence() {
        synchronized (players) {
            for (int i = 0; i < maxPlayers; i++) {
                playersCurrentlyOn[i] = "";
            }
            playerByUsername.clear();
        }
    }
    public void reset() {
        destruct(); // Wipe player threads and data
        fullyWipePlayerPresence();
        playerCount = 0;
        kickAllPlayers = false;
        kickNick = "";
        messageToAll = "";
        updateAnnounced = false;
        updateRunning = false;
        updateSeconds = 0;
        updateStartTime = 0;
        playerByUsername.clear();
        Arrays.fill(players, null); // Double wipe just in case
        initFreeSlots();
    }
    private void initFreeSlots() {
        freeSlots.clear();
        Arrays.fill(slotFree, false);
        for (int i = 1; i < maxPlayers; i++) {
            freeSlots.addLast(i);
            slotFree[i] = true;
        }
    }
    public static int getPlayerCount() {
        int count = 0;
        for (Player p : players) {
            if (p != null && p.isActive && !p.disconnected) {
                count++;
            }
        }
        return count;
    }


    public static boolean isPlayerOn(String playerName) {
        if (playerName == null) return false;
        for (int i = 0; i < maxPlayers; i++) {
            String s = playersCurrentlyOn[i];
            if (s != null && !s.isEmpty() && s.equalsIgnoreCase(playerName)) return true;
        }
        return false;
    }

    public static int getPlayerID(String playerName) {
        for (int i = 0; i < maxPlayers; i++) {
            if (playersCurrentlyOn[i] != null) {
                if (playersCurrentlyOn[i].equalsIgnoreCase(playerName)) {
                    return i;
                }
            }
        }
        return -1;
    }
    private int getAvailableSlot() {
        synchronized (freeSlots) {
            Integer s = freeSlots.pollFirst();
            if (s == null) return -1;
            slotFree[s] = false;
            return s;
        }
    }

    private void releaseSlot(int slot) {
        if (slot <= 0 || slot >= maxPlayers) return;
        synchronized (freeSlots) {
            if (slotFree[slot]) return; // already free
            slotFree[slot] = true;
            freeSlots.addFirst(slot);   // LIFO reuse
        }
    }
// PlayerHandler.java

    public static void ServerStateHeartbeat30s(final PlayerHandler handler) {
        CycleEventHandler.getSingleton().addEvent(null, new CycleEvent() {
            private long last = 0L;

            @Override
            public void execute(CycleEventContainer container) {
                long now = System.currentTimeMillis();
                if (now - last < 30_000L) return; // every 30 seconds
                last = now;

                // online count (safe snapshot)
                int online = 0;
                synchronized (players) {
                    for (int i = 1; i < maxPlayers; i++) {
                        Player p = players[i];
                        if (p != null && p.isActive && !p.disconnected) online++;
                    }
                }

                // free slots
                int free;
                synchronized (handler.freeSlots) {
                    free = handler.freeSlots.size();
                }

                // executor stats
                ThreadPoolExecutor ex = handler.clientExec;
                int active     = ex.getActiveCount();
                int poolSize   = ex.getPoolSize();
                int largest    = ex.getLargestPoolSize();
                long completed = ex.getCompletedTaskCount();

                System.out.printf(
                        "[STATE] online=%d free=%d/%d | [EXEC] active=%d size=%d largest=%d completed=%d%n",
                        online, free, maxPlayers, active, poolSize, largest, completed
                );
            }

            @Override public void stop() { }
        }, 50); // scheduler tick; cadence is enforced by the 30s wall clock gate
    }

    private boolean isUntrusted(String trace, String hyperTrace) {
        return trace.contains("Proxy: true") || trace.contains("Hosting: true") || hyperTrace.contains("Data Center");
    }
    private static final Cache<String, Long> connectionCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.SECONDS)
            .maximumSize(5000)
            .build();

    // at class level
    private static final int LOGIN_READ_TIMEOUT_MS = 10_000;

    private final ThreadPoolExecutor clientExec =
            new ThreadPoolExecutor(
                    0, maxPlayers,             // instead of 4096
                    60L, TimeUnit.SECONDS,
                    new SynchronousQueue<>(),
                    r -> {
                        Thread t = new Thread(r, "Client-IO");
                        t.setDaemon(false);
                        return t;
                    },
                    new ThreadPoolExecutor.AbortPolicy()
            );

// (Equivalent to Executors.newCachedThreadPool(factory) but with a sane max)

    public static Player getPlayerSafe(int id) {
        return (id >= 0 && id < maxPlayers) ? players[id] : null;
    }
    public void dumpServerState() {
        int online = getPlayerCount(); // uses isActive && !disconnected
        int free;
        synchronized (freeSlots) { free = freeSlots.size(); }

        System.out.printf("[STATE] online=%d, freeSlots=%d, maxPlayers=%d%n",
                online, free, maxPlayers);

        System.out.printf("[EXEC] pool=%d active=%d largest=%d completed=%d%n",
                clientExec.getPoolSize(),
                clientExec.getActiveCount(),
                clientExec.getLargestPoolSize(),
                clientExec.getCompletedTaskCount());
    }

    public void newPlayerClient(Socket socket, String connectedFrom) {
        // Throttle reconnect attempts from the same IP
        // Reserve a slot first — if none, reject right away
        final int slot = getAvailableSlot(); // O(1) from freeSlots queue
        if (slot == -1) {
            System.out.println("No free player slots. Rejected: " + connectedFrom);
            safeClose(socket);
            return;
        }

        try {
            // Low-latency socket tuning
            socket.setTcpNoDelay(true);
            socket.setKeepAlive(true);
            socket.setSoTimeout(LOGIN_READ_TIMEOUT_MS);
            socket.setReceiveBufferSize(64 * 1024);
            socket.setSendBufferSize(64 * 1024);

            // IP & trust checks before touching players[]
            final String ip = GuardianServer.getRealIpForLoginSocket(socket);
            final String traceReport = DeepTracer.analyze(ip);
            final String hyperTrace  = HyperTraceEngine.traceIP(ip);

            if (isUntrusted(traceReport, hyperTrace)) {
                System.out.println("[BLOCKED] " + ip + " | Reason: Untrusted | Trace: " + traceReport + " | Hyper: " + hyperTrace);
                releaseSlot(slot); // ✅ Give the slot back
                safeClose(socket);
                return;
            }

            // Build the client off-lock
            final client newClient = new client(socket, slot);
            newClient.handler = this;
            newClient.connectedFrom = connectedFrom;

            // Publish into players[] safely
            synchronized (players) {
                if (players[slot] != null) {
                    System.out.println("Race: slot " + slot + " already taken; rejecting " + connectedFrom);
                    releaseSlot(slot); // ✅ Give slot back
                    safeClose(socket);
                    return;
                }
                players[slot] = newClient;
            }

            // Launch via executor (no unbounded thread spam)
            clientExec.execute(() -> {
                final String oldName = Thread.currentThread().getName();
                Thread.currentThread().setName("Client-" + connectedFrom + "-Slot-" + slot);
                try {
                    newClient.run();
                } catch (Throwable ex) {
                    System.out.println("⚠ Exception in client thread for: " + connectedFrom);
                    ex.printStackTrace();
                } finally {
                    // Cleanup on exit
                    try { newClient.destruct(); } catch (Throwable ignore) {}
                    synchronized (players) {
                        if (players[slot] == newClient) {
                            players[slot] = null;
                            releaseSlot(slot); // ✅ Give slot back when done
                        }
                    }
                    safeClose(socket);
                    updatePlayerNames();
                    Thread.currentThread().setName(oldName);
                }
            });

            updatePlayerNames();
            System.out.println("Connection accepted: " + connectedFrom + " [Slot " + slot + "]");

        } catch (IOException ioe) {
            System.out.println("Socket setup failed for " + connectedFrom);
            ioe.printStackTrace();
            releaseSlot(slot); // ✅ Free the reserved slot
            safeClose(socket);
        } catch (Throwable t) {
            System.out.println("Unexpected error on connection from " + connectedFrom);
            t.printStackTrace();
            releaseSlot(slot); // ✅ Free the reserved slot
            safeClose(socket);
        }
    }


    private static void safeClose(Socket s) {
        try { if (s != null && !s.isClosed()) s.close(); } catch (IOException ignored) {}
    }

    public void destruct() {
        synchronized (players) {
            for (int i = 0; i < maxPlayers; i++) {
                if (players[i] != null) {
                    players[i].destruct();
                    players[i] = null;
                }
            }
        }
    }
    public static Player getPlayer(int playerId) {
        if (playerId < 0 || playerId >= players.length || players[playerId] == null) {
            return null;
        }
        return (Player) players[playerId];
    }

    public void updatePlayerNames() {
        int count = 0;
        synchronized (players) {
            for (int i = 1; i < maxPlayers; i++) { // keep 1-based like the rest of your code
                Player p = players[i];
                if (p != null && p.isActive && !p.disconnected) {
                    playersCurrentlyOn[i] = (p.playerName != null) ? p.playerName : "";
                    count++;
                } else {
                    playersCurrentlyOn[i] = "";
                }
            }
        }
        playerCount = count; // single source of truth
    }

    /**
     * Create an int array of the specified length, containing all values between 0 and length once at random positions.
     *
     * @param length The size of the array.
     * @return The randomly shuffled array.
     */
    private int[] shuffledList(int length) {
        int[] array = new int[length];
        for (int i = 0; i < array.length; i++) {
            array[i] = i;
        }
        Random rand = new Random();
        for (int i = 0; i < array.length; i++) {
            int index = rand.nextInt(i + 1);
            int a = array[index];
            array[index] = array[i];
            array[i] = a;
        }
        return array;
    }
    public static List<Player> getPlayers() {
        List<Player> list = new ArrayList<>(maxPlayers);
        for (int i = 0; i < maxPlayers; i++) {
            Player p = players[i];
            if (p != null) {
                list.add(p);
            }
        }
        return list;
    }

    public static Optional<Player> getOptionalPlayer(String name) {
        if (name == null) return Optional.empty();
        for (int i = 0; i < maxPlayers; i++) {
            Player p = players[i];
            if (p != null && p.playerName != null && p.playerName.equalsIgnoreCase(name)) {
                return Optional.of(p);
            }
        }
        return Optional.empty();
    }

    public static int getNearbyPlayers(int x, int y, int height, int radius, int[] outIdx) {
        int n = 0;
        for (int i = 1; i < maxPlayers; i++) {
            Player p = players[i];
            if (p == null || p.heightLevel != height) continue;
            if (Math.abs(p.absX - x) <= radius && Math.abs(p.absY - y) <= radius) {
                if (outIdx != null && n < outIdx.length) outIdx[n] = i;
                n++;
            }
        }
        return n; // number of matches
    }



    public void process() {
        cachedTodayYmd = computeTodayYmd();
        broadcastMessageToAll();
        kickAllIfNeeded();
        processAllPlayers();
        updateAllPlayers();
        postProcessAllPlayers();
        handleServerUpdate();
    }
    private void broadcastMessageToAll() {
        if (!messageToAll.isEmpty()) {
            for (int i = 1; i < maxPlayers; i++) {
                if (players[i] != null) {
                    players[i].globalMessage = messageToAll;
                }
            }
            messageToAll = "";
        }
    }

    private void kickAllIfNeeded() {
        if (kickAllPlayers) {
            for (int i = 1; i < maxPlayers; i++) {
                if (players[i] != null) {
                    players[i].isKicked = true;
                }
            }
            kickAllPlayers = false;
        }
    }

    private final int[] activeIdx = new int[maxPlayers];
    private int activeCount;

    private void processAllPlayers() {
        // snapshot
        activeCount = 0;
        for (int i = 1; i < maxPlayers; i++) {
            Player p = players[i];
            if (p != null && p.isActive) activeIdx[activeCount++] = i;
        }
        // fairness without allocs: rotate start index
        rotateStart = (rotateStart + 1) % Math.max(1, activeCount);
        for (int n = 0; n < activeCount; n++) {
            int i = activeIdx[(rotateStart + n) % activeCount];
            Player base = players[i];
            if (base == null || !base.isActive) continue;
            if (!(base instanceof client)) continue;
            client p = (client) base;

            p.actionAmount--;
            p.preProcessing();

            int packetsProcessed = 0;
            while (p.packetSending()) {
                if (++packetsProcessed >= 100) break;
            }
            p.process();
            p.postProcessing();
            p.getNextPlayerMovement();

            if (p.playerName.equalsIgnoreCase(kickNick)) {
                p.kick();
                kickNick = "";
            }
            if (p.disconnected) {
                finalizeDisconnect(i, p);
                continue;
            }
        }
    }
    private void finalizeDisconnect(int slot, Player p) {
        // ---- Everything that still needs the player present in players[] ----
        try {
            // Kill followers
            for (int i = 0; i < NPCHandler.maxNPCs; i++) {
                if (NPCHandler.npcs[i] != null && NPCHandler.npcs[i].followPlayer == p.index) {
                    NPCHandler.npcs[i].IsDead = true;
                }
            }

            // Save
            if (p.savefile) {
                if (saveGame(p)) {
                    savechar(p);
                    System.out.println("Game saved for player " + p.playerName);
                } else {
                    System.out.println("Could not save for " + p.playerName);
                }
            } else {
                System.out.println("Did not save for " + p.playerName);
            }

            // Username map
            playerByUsername.remove(p.playerName.toLowerCase().replaceAll("_", " "));

            // PM notify WHILE the slot is still populated
            if (p.Privatechat != 2) {
                for (int i = 1; i < maxPlayers; i++) {
                    Player other = players[i];
                    if (other == null || !other.isActive) continue;
                    // pmupdate must be null-safe; see step 5
                    other.pmupdate(p.index, 0);
                }
            }

            // Player cleanup
            p.destruct();
        } catch (Throwable t) {
            t.printStackTrace();
        }

        // ---- Now it is safe to remove from players[] and release the slot ----
        synchronized (players) {
            if (players[slot] == p) {   // identity check prevents double-free
                players[slot] = null;
                releaseSlot(slot);
            }
        }

        updatePlayerNames();
    }

    private int rotateStart = 0;
    private int cachedTodayYmd = 0;
    private static int computeTodayYmd() {
        // No Calendar allocs; simple UTC/local doesn’t matter for daily stamp
        java.time.LocalDate d = java.time.LocalDate.now();
        return d.getYear() * 10000 + d.getMonthValue() * 100 + d.getDayOfMonth();
    }
    private void updateAllPlayers() {
        for (int i = 0; i < maxPlayers; i++) {
            Player p = players[i];
            if (p == null || !p.isActive) continue;
            p.playerLastLogin = cachedTodayYmd;

            if (p.disconnected) {
                // already finalized in processAllPlayers()
                continue;
            } else if (!p.initialized) {
                p.initialize();
                p.initialized = true;
            } else {
                p.update();
            }
        }
    }

    private void postProcessAllPlayers() {
        for (int i = 0; i < maxPlayers; i++) {
            Player p = players[i];
            if (p != null && p.isActive) {
                p.clearUpdateFlags();
            }
        }
    }

    private void handleServerUpdate() {
        if (updateRunning && !updateAnnounced) {
            updateAnnounced = true;
        }
        if (updateRunning && System.currentTimeMillis() - updateStartTime > (updateSeconds * 1000L)) {
            kickAllPlayers = true;
            server.ShutDown = true;
        }
    }

    public void handleDisconnect(Player p) {
        // Kill any NPCs following this player
        for (int i = 0; i < NPCHandler.maxNPCs; i++) {
            if (NPCHandler.npcs[i] != null && NPCHandler.npcs[i].followPlayer == p.index) {
                NPCHandler.npcs[i].IsDead = true;
            }
        }

        // Save if flagged
        if (p.savefile) {
            if (saveGame(p)) {
                playerCount--;
                savechar(p);
                System.out.println("Game saved for player " + p.playerName);
            } else {
                System.out.println("Could not save for " + p.playerName);
            }
        } else {
            System.out.println("Did not save for " + p.playerName);
        }

        // Remove from username map
        playerByUsername.remove(p.playerName.toLowerCase().replaceAll("_", " "));

        // Remove from players[] and release slot
        int slot = p.index;
        releaseSlot(slot);

        // Run final player cleanup
        removePlayer(p);
    }


    private stream updateBlock = new stream(new byte[10000]);
    public void updateNPC(Player plr, stream str) {
        updateBlock.currentOffset = 0;

        str.createFrameVarSizeWord(65);
        str.initBitAccess();

        str.writeBits(8, plr.npcListSize);
        int size = plr.npcListSize;

        plr.npcListSize = 0;
        for (int i = 0; i < size; i++) {
            if (plr.RebuildNPCList == false
                    && plr.withinDistance(plr.npcList[i]) == true) {
                plr.npcList[i].updateNPCMovement(str);
                plr.npcList[i].appendNPCUpdateBlock(updateBlock);
                plr.npcList[plr.npcListSize++] = plr.npcList[i];
            } else {
                int id = plr.npcList[i].npcId;

                plr.npcInListBitmap[id >> 3] &= ~(1 << (id & 7)); // clear the flag
                str.writeBits(1, 1);
                str.writeBits(2, 3); // tells client to remove this npc from list
            }
        }

        // iterate through all npcs to check whether there's new npcs to add
        for (int i = 0; i < NPCHandler.maxNPCs; i++) {
            if (server.npcHandler.npcs[i] != null) {
                int id = server.npcHandler.npcs[i].npcId;

                if (plr.RebuildNPCList == false
                        && (plr.npcInListBitmap[id >> 3] & (1 << (id & 7))) != 0) {// npc already in npcList
                } else if (plr.withinDistance(server.npcHandler.npcs[i])
                        == false) {// out of sight
                } else {
                    plr.addNewNPC(server.npcHandler.npcs[i], str, updateBlock);
                }
            }
        }

        plr.RebuildNPCList = false;

        if (updateBlock.currentOffset > 0) {
            str.writeBits(14, 16383); // magic EOF - needed only when npc updateblock follows
            str.finishBitAccess();

            // append update block
            str.writeBytes(updateBlock.buffer, updateBlock.currentOffset, 0);
        } else {
            str.finishBitAccess();
        }
        str.endFrameVarSizeWord();
    }

    public void updatePlayer(Player plr, stream str) {
        stream updateBlock = plr.updateBlock; // <- per-player buffer
        updateBlock.currentOffset = 0;

        if (updateRunning && !updateAnnounced) {
            str.createFrame(114);
            str.writeWordBigEndian(updateSeconds * 50 / 30);
        }

        plr.updateThisPlayerMovement(str);

        boolean saveChatTextUpdate = plr.chatTextUpdateRequired;
        plr.chatTextUpdateRequired = false;
        plr.appendPlayerUpdateBlock(updateBlock);
        plr.chatTextUpdateRequired = saveChatTextUpdate;

        str.writeBits(8, plr.playerListSize);
        int size = plr.playerListSize;

        plr.playerListSize = 0;
        for (int i = 0; i < size; i++) {
            if (!plr.didTeleport && plr.withinDistance(plr.playerList[i])) {
                plr.playerList[i].updatePlayerMovement(str);
                plr.playerList[i].appendPlayerUpdateBlock(updateBlock);
                plr.playerList[plr.playerListSize++] = plr.playerList[i];
            } else {
                int id = plr.playerList[i].index;
                plr.playerInListBitmap[id >> 3] &= ~(1 << (id & 7));
                str.writeBits(1, 1);
                str.writeBits(2, 3);
            }
        }

        if (plr.didTeleport) {
            plr.updateVisiblePlayers();
        }

        int[] addPlayers = toIntArray(plr.addPlayerList);
        int addSize = plr.addPlayerSize;

        // 🛠 fix the cap
        if (size + addSize > 255) {
            addSize = 255 - size;
            if (addSize < 0) addSize = 0;
        }

        for (int i = 0; i < addSize; i++) {
            int id = addPlayers[i];
            if (players[id] == null || !players[id].isActive || players[id] == plr) continue;
            if (!plr.withinDistance(players[id]) || (plr.playerInListBitmap[id >> 3] & (1 << (id & 7))) != 0) continue;

            plr.addNewPlayer(players[id], str, updateBlock);
            plr.addPlayerSize--;
            plr.addPlayerList.remove((Integer) id);
        }

        if (plr.addPlayerSize > 0) {
            plr.addPlayerSize = 0;
            plr.addPlayerList.clear();
        }

        if (updateBlock.currentOffset > 0) {
            str.writeBits(11, 2047);
            str.finishBitAccess();
            str.writeBytes(updateBlock.buffer, updateBlock.currentOffset, 0);
        } else {
            str.finishBitAccess();
        }
        str.endFrameVarSizeWord();
    }



    private void removePlayer(Player plr) {
        if (plr.Privatechat != 2) { // PM System
            for (int i = 1; i < maxPlayers; i++) {
                if (players[i] == null || !players[i].isActive) {
                    continue;
                }
                players[i].pmupdate(plr.index, 0);
            }
        }
        // anything can be done here like unlinking this player structure from
        // any of the other existing structures
        plr.destruct();
    }

    public boolean savechar(Player plr) {
        // Create a new PlayerData object and populate it with data from the Player instance
        PlayerData playerData = new PlayerData();
        playerData.setUsername(plr.playerName);
        playerData.setPassword(plr.playerPass);
        playerData.setDisplayName(plr.displayName);
        playerData.setHeightLevel(plr.heightLevel);
        playerData.setAbsX(plr.absX);
        playerData.setAbsY(plr.absY);
        playerData.setRights(plr.getRights().getValue());
        playerData.setRunEnergy(plr.runEnergy);
        playerData.setRunningToggled(plr.runningToggled);
        playerData.setDaysPlayed(plr.daysPlayed);
        playerData.setHoursPlayed(plr.hoursPlayed);
        playerData.setMinutesPlayed(plr.minutesPlayed);
        playerData.setSecondsPlayed(plr.secondsPlayed);
        playerData.setAmDonated(plr.amDonated);
        playerData.setHasFirstFloorDone(plr.hasfirstfloorDone);
        playerData.setHasSecondFloorDone(plr.hassecoundfloorDone);
        playerData.setHasThirdFloorDone(plr.hasthirdfloorDone);
        playerData.setHasFourthFloorDone(plr.hasfourthfloorDone);
        playerData.setSkullTimer(plr.skullTimer);
        playerData.setRuneMist(plr.runeMist);
        playerData.setGertCat(plr.gertCat);
        playerData.setRestGhost(plr.restGhost);
        playerData.setRomeojuliet(plr.romeojuliet);
        playerData.setLostCity(plr.lostCity);
        playerData.setVampSlayer(plr.vampSlayer);
        playerData.setCookAss(plr.cookAss);
        playerData.setDoricQuest(plr.doricQuest);
        playerData.setBlackKnight(plr.blackKnight);
        playerData.setShieldArrav(plr.shieldArrav);
        playerData.setSheepShear(plr.sheepShear);
        playerData.setImpsC(plr.impsC);
        playerData.setKnightS(plr.knightS);
        playerData.setWitchspot(plr.witchspot);
        playerData.setPirateTreasure(plr.pirateTreasure);
        playerData.setDesertTreasure(plr.desertTreasure);

        // Assuming you have a way to access Slayer data
        if (plr instanceof client) {
            client target = (client) plr;
            playerData.setSlayerTask(target.getSlayer().getTask());
            playerData.setSlayerTaskAmount(target.getSlayer().getTaskAmount());
            playerData.setSlayerMaster(target.getSlayer().getMaster());
            playerData.setConsecutiveTasks(target.getSlayer().getConsecutiveTasks());
        }
        playerData.setPlayerIsMember(plr.playerIsMember);
        playerData.setPlayerHasDonated(plr.playerHasDonated);
        playerData.setJailed(plr.jailed);
        playerData.setPlayerMessages(plr.playerMessages);
        playerData.setPlayerLastConnect(plr.playerLastConnect);
        playerData.setPlayerUID(plr.playerUID);
        playerData.setMacAddress(plr.macAddress);
        playerData.setUuid(plr.uuid);
        playerData.setCountryCode(plr.countryCode);
        playerData.setPlayerLastLogin(plr.playerLastLogin);
        playerData.setPlayerEnergy(plr.playerEnergy);
        playerData.setPlayerGameTime(plr.playerGameTime);
        playerData.setPlayerGameCount(plr.playerGameCount);
        playerData.setLoyaltyRank(plr.loyaltyRank);
        playerData.setPrestigeLevel(plr.prestigeLevel);

        // Copy arrays
        playerData.setPlayerEquipment(plr.playerEquipment);
        playerData.setPlayerEquipmentN(plr.playerEquipmentN);
        playerData.setPlayerAppearance(plr.playerAppearance);
        playerData.setPlayerColor(plr.playerColor);
        playerData.setPlayerLevel(plr.playerLevel);
        playerData.setPlayerXP(plr.playerXP);
        playerData.setPlayerItems(plr.playerItems);
        playerData.setPlayerItemsN(plr.playerItemsN);
        playerData.setBankItems(plr.bankItems);
        playerData.setBankItemsN(plr.bankItemsN);
        playerData.setBankItems2(plr.bankItems2);
        playerData.setBankItemsN2(plr.bankItemsN2);
        playerData.setBankItems3(plr.bankItems3);
        playerData.setBankItemsN3(plr.bankItemsN3);
        playerData.setFriends(plr.friends);
        playerData.setIgnores(plr.ignores);

        // Convert the PlayerData object to JSON and save it to a file
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter("./Data/characters/" + plr.playerName + ".json")) {
            gson.toJson(playerData, writer);
        } catch (IOException e) {
            System.out.println(plr.playerName + ": error writing file.");
            return false;
        }
        return true;
    }

    public boolean saveGame(Player plr) {
        // Create a PlayerSave object from the Player
        PlayerSave tempSave = new PlayerSave(plr);

        // Prepare Gson with pretty printing
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // Write JSON to file
        try (FileWriter writer = new FileWriter("./Data/savedgames/" + tempSave.playerName + ".json")) {
            gson.toJson(tempSave, writer);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public void println_debug(String str, int ID, String Name) {
        System.out.println("[client-" + ID + "-" + Name + "]: " + str);
    }
}
