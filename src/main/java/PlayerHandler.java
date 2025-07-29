import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public static boolean kickAllPlayers = false;
    public static String messageToAll = "";
    public static int playerCount = 0;
    public static boolean updateAnnounced;
    public static boolean updateRunning;
    public static int updateSeconds;
    public static long updateStartTime;
    public static String[] playersCurrentlyOn = new String[maxPlayers];
    private static final Map<String, Player> playerByUsername = new HashMap<>();
    private final stream updateBlock = new stream(new byte[10000]);
    // where we start searching at when adding a new player
    public int lastchatid = 1; //PM System

    PlayerHandler() {
        Arrays.fill(players, null);
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
        return Arrays.stream(playersCurrentlyOn).anyMatch(s -> s != null && s.equalsIgnoreCase(playerName));
    }
    public static List<Player> getPlayers() {
        Player[] clients = new Player[players.length];
        System.arraycopy(players, 0, clients, 0, players.length);
        return Arrays.asList(clients).stream().filter(Objects::nonNull).collect(Collectors.toList());
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
        for (int i = 1; i < maxPlayers; i++) {
            if (players[i] == null) {
                return i;
            }
        }
        return -1;
    }
    private boolean isUntrusted(String trace, String hyperTrace) {
        return trace.contains("Proxy: true") || trace.contains("Hosting: true") || hyperTrace.contains("Data Center");
    }
    private static final Cache<String, Long> connectionCache = Caffeine.newBuilder()
            .expireAfterWrite(2, TimeUnit.SECONDS)
            .maximumSize(5000)
            .build();

    public void newPlayerClient(Socket socket, String connectedFrom) {
        // Throttle reconnect attempts from the same IP
       if (connectionCache.getIfPresent(connectedFrom) != null) {
            System.out.println("🚫 Fast reconnect blocked: " + connectedFrom);
            return;
        }
        connectionCache.put(connectedFrom, System.currentTimeMillis());

        int slot;

        synchronized (players) {
            slot = getAvailableSlot();

            if (slot == -1) {
                System.err.println("No free player slots. Rejected: " + connectedFrom);
                return;
            }


            try {
                socket.setSoTimeout(10000);

                // Each client must be fully isolated
                client newClient = new client(socket, slot);
                newClient.handler = this;
                newClient.connectedFrom = connectedFrom;
                String ip = GuardianServer.getRealIpForLoginSocket(socket);
                String traceReport = DeepTracer.analyze(ip);
                String hyperTrace = HyperTraceEngine.traceIP(ip);

                if (isUntrusted(traceReport, hyperTrace)) {
                    System.out.println("[BLOCKED] " + ip + " | Reason: Untrusted | Trace: " + traceReport + " | Hyper: " + hyperTrace);
                    //returnCode = 26;
                   // savefile = false;
                   // disconnected = true;
                    return;
                }
                players[slot] = newClient;

                // Kick off the thread safely with logging
                int finalSlot = slot;
                Thread clientThread = new Thread(() -> {
                    Thread.currentThread().setName("Client-" + connectedFrom + "-Slot-" + finalSlot);
                    try {
                        newClient.run();
                    } catch (Exception ex) {
                        System.err.println("⚠Exception in client thread for: " + connectedFrom);
                        ex.printStackTrace();
                    }
                });
                clientThread.setDaemon(false); // ⬅️ Optional: daemon or not depends on shutdown behavior
                clientThread.start();

                updatePlayerNames();

                System.out.println("Connection accepted: " + connectedFrom + " [Slot " + slot + "]");

            } catch (IOException e) {
                System.err.println("Socket setup failed for " + connectedFrom);
                e.printStackTrace();
            } catch (Exception e) {
                System.err.println("Unexpected error on connection from " + connectedFrom);
                e.printStackTrace();
            }
        }
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
        playerCount = 0;
        for (int i = 0; i < maxPlayers; i++) {
            Player p = players[i];
            if (p != null && p.isActive && !p.disconnected) {
                playersCurrentlyOn[i] = p.playerName;
                playerCount++;
            } else {
                playersCurrentlyOn[i] = "";
            }
        }
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
    public static Optional<Player> getOptionalPlayer(String name) {
        return getPlayers().stream().filter(Objects::nonNull).filter(Player -> Player.playerName.equalsIgnoreCase(name)).findFirst();
    }
    public static List<Player> getNearbyPlayers(int x, int y, int height, int radius) {
        List<Player> result = new ArrayList<>();

        for (Player p : players) {
            if (p == null || p.heightLevel != height) continue;

            if (Math.abs(p.absX - x) <= radius && Math.abs(p.absY - y) <= radius) {
                result.add(p);
            }
        }

        return result;
    }


    public void process() {
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

    private void processAllPlayers() {
        int[] randomOrder = shuffledList(maxPlayers);
        for (int i : randomOrder) {
            Player base = players[i];
            if (base == null || !base.isActive) continue;

            // Safe cast to Client, since all active players should be Client instances
            if (!(base instanceof client)) continue;
            client p = (client) base;

            p.actionAmount--;
            p.preProcessing();

            int packetsProcessed = 0;
            while (p.packetSending()) {
                if (++packetsProcessed >= 100) {
                    break; // Prevent abuse or infinite loop
                }
            }
            p.process();
            p.postProcessing();
            p.getNextPlayerMovement();

            if (p.playerName.equalsIgnoreCase(kickNick)) {
                p.kick();
                kickNick = "";
            }

            if (p.disconnected) {
                handleDisconnect(p);
                players[i] = null;
            }
        }
    }


    private void updateAllPlayers() {
        for (int i = 0; i < maxPlayers; i++) {
            Player p = players[i];
            if (p == null || !p.isActive) continue;
            Calendar cal = Calendar.getInstance();
            int calc = ((cal.get(Calendar.YEAR) * 10000) + (cal.get(Calendar.MONTH) * 100) + cal.get(Calendar.DAY_OF_MONTH));
            p.playerLastLogin = calc;

            if (p.disconnected) {
                handleDisconnect(p);
                players[i] = null;
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
        for (int i = 0; i < NPCHandler.maxNPCs; i++) {
            if (NPCHandler.npcs[i] != null && NPCHandler.npcs[i].followPlayer == p.playerId) {
                NPCHandler.npcs[i].IsDead = true;
            }
        }
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
        playerByUsername.remove(p.playerName.toLowerCase().replaceAll("_", " "));
        removePlayer(p);
    }

    public void updateNPC(Player plr, stream str) {
        stream updateBlock = new stream(new byte[5000]); // 👈 LOCAL!
        str.createFrameVarSizeWord(65);
        str.initBitAccess();

        if (plr.npcListSize > 255) plr.npcListSize = 255;
        str.writeBits(8, plr.npcListSize);

        int size = plr.npcListSize;
        plr.npcListSize = 0;

        for (int i = 0; i < size; i++) {
            NPC npc = plr.npcList[i];
            if (npc == null || npc.npcId < 0 || npc.npcId > 16383 || npc.npcType < 0) continue;

            if (!plr.RebuildNPCList && plr.withinDistance(npc)) {
                npc.updateNPCMovement(str);
                if (npc.updateRequired) {
                    npc.appendNPCUpdateBlock(updateBlock);
                }
                plr.npcList[plr.npcListSize++] = npc;
            } else {
                int id = npc.npcId;
                plr.npcInListBitmap[id >> 3] &= ~(1 << (id & 7));
                str.writeBits(1, 1);
                str.writeBits(2, 3);
            }
        }

        for (int i = 0; i < NPCHandler.maxNPCs; i++) {
            NPC npc = NPCHandler.npcs[i];
            if (npc == null || npc.npcType < 0 || npc.npcId < 0 || npc.npcId > 16383) continue;
            if (!plr.withinDistance(npc)) continue;

            int id = npc.npcId;
            if (!plr.RebuildNPCList &&
                    (plr.npcInListBitmap[id >> 3] & (1 << (id & 7))) != 0) {
                continue;
            }

            int deltaX = npc.absX - plr.absX;
            int deltaY = npc.absY - plr.absY;
            if (deltaX < 0) deltaX += 32;
            if (deltaY < 0) deltaY += 32;
            if (deltaX > 31 || deltaY > 31) continue;

            plr.npcInListBitmap[id >> 3] |= (1 << (id & 7));
            plr.npcList[plr.npcListSize++] = npc;

            str.writeBits(14, id);
            str.writeBits(5, deltaY);
            str.writeBits(5, deltaX);
            str.writeBits(1, 0);
            str.writeBits(14, npc.npcType);

            boolean wasUpdateRequired = npc.updateRequired;
            npc.updateRequired = true;
            npc.appendNPCUpdateBlock(updateBlock); // 👈 use local
            npc.updateRequired = wasUpdateRequired;

            str.writeBits(1, 1); // update block follows
        }

        str.writeBits(14, 16383); // end-of-list marker
        str.finishBitAccess();

        if (updateBlock.currentOffset > 0) {
            str.writeBytes(updateBlock.buffer, updateBlock.currentOffset, 0);
        }

        str.endFrameVarSizeWord();
        plr.RebuildNPCList = false;
    }


    // should actually be moved to client.java because it's very client specific
    public void updatePlayer(Player plr, stream str) {
        updateBlock.currentOffset = 0;

        if (updateRunning && !updateAnnounced) {
            str.createFrame(114);
            str.writeWordBigEndian(updateSeconds * 50 / 30);
        }

        // update thisPlayer
        plr.updateThisPlayerMovement(str);        // handles walking/running and teleporting
        // do NOT send chat text back to thisPlayer!
        boolean saveChatTextUpdate = plr.chatTextUpdateRequired;
        plr.chatTextUpdateRequired = false;
        plr.appendPlayerUpdateBlock(updateBlock);
        plr.chatTextUpdateRequired = saveChatTextUpdate;

        str.writeBits(8, plr.playerListSize);
        int size = plr.playerListSize;

        // update/remove players that are already in the playerList
        plr.playerListSize = 0;        // we're going to rebuild the list right away
        for (int i = 0; i < size; i++) {
            // this update packet does not support teleporting of other players directly
            // instead we're going to remove this player here and readd it right away below
            if (!plr.didTeleport && plr.withinDistance(plr.playerList[i])) {
                plr.playerList[i].updatePlayerMovement(str);
                plr.playerList[i].appendPlayerUpdateBlock(updateBlock);
                plr.playerList[plr.playerListSize++] = plr.playerList[i];
            } else {
                int id = plr.playerList[i].playerId;
                plr.playerInListBitmap[id >> 3] &= ~(1 << (id & 7));        // clear the flag
                str.writeBits(1, 1);
                str.writeBits(2, 3);        // tells client to remove this char from list
            }
        }

        // iterate through all players to check whether there's new players to add
        if (plr.didTeleport) {
            plr.updateVisiblePlayers(); // so if we teleport and we are in our original region we are added back to the list for all the players that can see us
        }

        int[] addPlayers = toIntArray(plr.addPlayerList);
        int addSize = plr.addPlayerSize;

        if (size + addSize > 255) {
            addSize = size - 255;
        }

        for (int i = 0; i < addSize; i++) {
            int id = addPlayers[i];

            if (players[id] == null || !players[id].isActive || players[id] == plr)
                continue;

            if (!plr.withinDistance(players[id]) || (plr.playerInListBitmap[id >> 3] & (1 << (id & 7))) != 0) {
                continue;
            }

            plr.addNewPlayer(players[id], str, updateBlock);
            plr.addPlayerSize--; // you could just put these in player.java
            plr.addPlayerList.remove((Integer) id); // but for the sake of the tutorial, it's right here.
        }

        if (plr.addPlayerSize > 0) {
            plr.addPlayerSize = 0;
            plr.addPlayerList.clear();
        }

        if (updateBlock.currentOffset > 0) {
            str.writeBits(11, 2047);    // magic EOF - needed only when player updateblock follows
            str.finishBitAccess();

            // append update block
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
                players[i].pmupdate(plr.playerId, 0);
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

        // Assuming you have a way to access Slayer data
        client target = (client) PlayerHandler.players[plr.playerId];
        playerData.setSlayerTask(target.getSlayer().getTask());
        playerData.setSlayerTaskAmount(target.getSlayer().getTaskAmount());
        playerData.setSlayerMaster(target.getSlayer().getMaster());
        playerData.setConsecutiveTasks(target.getSlayer().getConsecutiveTasks());

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
        PlayerSave tempSave = new PlayerSave(plr);
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("./Data/savedGames/" + tempSave.playerName + ".dat"));
            out.writeObject(tempSave);
            out.close();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public void println_debug(String str, int ID, String Name) {
        System.out.println("[client-" + ID + "-" + Name + "]: " + str);
    }
}
