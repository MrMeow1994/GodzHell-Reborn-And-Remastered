import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileWriter;
import java.io.IOException;

public class PlayerHandler {

    // Remark: the player structures are just a temporary solution for now
    // Later we will avoid looping through all the players for each player
    // by making use of a hash table maybe based on map regions (8x8 granularity should be ok)
    public static final int maxPlayers = 12000;
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
    private static final Cache<String, Long> connectionCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.SECONDS)
            .maximumSize(5000)
            .build();

    public void newPlayerClient(Socket socket, String connectedFrom) {
        // Throttle reconnect attempts from the same IP
      /* if (connectionCache.getIfPresent(connectedFrom) != null) {
            System.out.println("🚫 Fast reconnect blocked: " + connectedFrom);
            return;
        }
        connectionCache.put(connectedFrom, System.currentTimeMillis());
*/
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
                if (++packetsProcessed >= 10) {
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

    private void handleDisconnect(Player p) {
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

    // --- Refactored updatePlayer to reduce memory churn ---
    public void updatePlayer(Player plr, stream str) {
        if (plr.getOutStream() == null)
            return;

        plr.updateBlock.currentOffset = 0; // Use per-player updateBlock

        if (updateRunning && !updateAnnounced) {
            str.createFrame(114);
            str.writeWordBigEndian(updateSeconds * 50 / 30);
        }

        plr.updateThisPlayerMovement(str);

        boolean saveChatTextUpdate = plr.chatTextUpdateRequired;
        plr.chatTextUpdateRequired = false;
        plr.appendPlayerUpdateBlock(plr.updateBlock);
        plr.chatTextUpdateRequired = saveChatTextUpdate;

        str.writeBits(8, plr.playerListSize);
        int size = plr.playerListSize;
        if (size >= 250) size = 250;
        plr.playerListSize = 0;

        for (int i = 0; i < size; i++) {
            if (!plr.didTeleport && !plr.playerList[i].didTeleport && plr.withinDistance(plr.playerList[i])) {
                plr.playerList[i].updatePlayerMovement(str);
                plr.playerList[i].appendPlayerUpdateBlock(plr.updateBlock);
                plr.playerList[plr.playerListSize++] = plr.playerList[i];
            } else {
                int id = plr.playerList[i].playerId;
                plr.playerInListBitmap[id >> 3] &= ~(1 << (id & 7));
                str.writeBits(1, 1);
                str.writeBits(2, 3);
            }
        }

        for (int i = 0; i < Config.MAX_PLAYERS; i++) {
            if (players[i] == null || !players[i].isActive || players[i] == plr)
                continue;
            int id = players[i].playerId;
            if ((plr.playerInListBitmap[id >> 3] & (1 << (id & 7))) != 0)
                continue;
            if (!plr.withinDistance(players[i]))
                continue;
            plr.addNewPlayer(players[id], str, plr.updateBlock);
        }

        if (plr.updateBlock.currentOffset > 0) {
            str.writeBits(11, 2047);
            str.finishBitAccess();
            str.writeBytes(plr.updateBlock.buffer, plr.updateBlock.currentOffset, 0);
        } else {
            str.finishBitAccess();
        }
        str.endFrameVarSizeWord();
    }

    // --- Refactored updateNPC to reduce memory churn ---
    public void updateNPC(Player plr, stream str) {
        if (plr.getOutStream() == null) return;
        plr.updateBlock.currentOffset = 0;

        str.createFrameVarSizeWord(65);
        str.initBitAccess();

        str.writeBits(8, plr.npcListSize);
        int size = plr.npcListSize;
        plr.npcListSize = 0;

        for (int i = 0; i < size; i++) {
            if (!plr.RebuildNPCList && plr.withinDistance(plr.npcList[i])) {
                plr.npcList[i].updateNPCMovement(str);
                plr.npcList[i].appendNPCUpdateBlock(plr.updateBlock);
                plr.npcList[plr.npcListSize++] = plr.npcList[i];
            } else {
                int id = plr.npcList[i].npcId;
                plr.npcInListBitmap[id >> 3] &= ~(1 << (id & 7));
                str.writeBits(1, 1);
                str.writeBits(2, 3);
            }
        }

        Arrays.fill(plr.npcList, plr.npcListSize, plr.npcList.length, null);

        int newNpcs = 0;
        for (int i = 0; i < NPCHandler.maxNPCs; i++) {
            if (NPCHandler.npcs[i] == null) continue;
            int id = NPCHandler.npcs[i].npcId;
            if (!plr.RebuildNPCList && (plr.npcInListBitmap[id >> 3] & (1 << (id & 7))) != 0)
                continue;
            if (!plr.withinDistance(server.npcHandler.npcs[i]))
                continue;

            plr.addNewNPC(server.npcHandler.npcs[i], str, plr.updateBlock);
            if (++newNpcs >= 20) break;
        }

        plr.RebuildNPCList = false;

        if (plr.updateBlock.currentOffset > 0) {
            str.writeBits(14, 16383);
            str.finishBitAccess();
            str.writeBytes(plr.updateBlock.buffer, plr.updateBlock.currentOffset, 0);
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
