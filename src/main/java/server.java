import com.Ghreborn.jagcached.FileServer;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import net.dv8tion.jda.api.EmbedBuilder;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
//import org.Vote.*;


public class server implements Runnable {

    public static final int cycleTime = 600;
    static long tickCount = 0;
    public static int tick = 0;
    public static final String ObjectManager = null;
    public static lottery lottery = new lottery();
    public static GlobalObjects globalObjects = new GlobalObjects();
    public static ControlPanel panel = new ControlPanel(true); // false if you want it off
    /*Highscores*/
    /*For more highscores to be recorded, change the #s in [] to the number you want kept, +1*/
    /*For example, if you want the top 20, put 21 in the [] ([21])*/
    public static int[] ranks = new int[11];
    public static String[] rankPpl = new String[11];
    public static boolean updateServer = false;
    public static boolean loginServerConnected = true;
    public static boolean enforceClient = false;
    public static int Rocks = 0;
    public static DoorHandler doorHandler;
    public static NPCDrops npcDrops = new NPCDrops();
    public static Fishing fishing = new Fishing();
    public static int updateSeconds = 180; //180 because it doesnt make the time jump at the start :P
    public static long startTime;
    public static server clientHandler = null;            // handles all the clients
    public static ServerSocket clientListener = null;
    public static boolean shutdownServer = false;        // set this to true in order to shut down and kill the server
    public static boolean shutdownClientHandler;            // signals ClientHandler to shut down
    public static int serverlistenerPort = 29432; //29432=default 19562= guardian
    public static PlayerHandler playerHandler = null;
    public static potions potions = null;
    public static clickingMost clickingMost = null;
    public static NPCHandler npcHandler = null;
   // public static GarbageCollectorManager garbageCollectorManager = null;
    public static PickableObjects PickableObjects = null;
    public static TextHandler textHandler = null;
    //public static int serverlistenerPort2 = 43594; // 5555=default
    public static ItemHandler itemHandler = null;
    public static ObjectManager objectManager = null;
    public static ShopHandler shopHandler = null;
    public static GlobalDrops GlobalDrops = null;
    public static Fishing Fishing = null;
    public static antilag antilag = null;
    public static itemspawnpoints itemspawnpoints = null;
    public static GraphicsHandler GraphicsHandler = null;
    public static ObjectHandler objectHandler = null;
    public static int EnergyRegian = 0;
    public static int MaxConnections = 100000;
    public static String[] Connections = new String[MaxConnections];
    public static int[] ConnectionCount = new int[MaxConnections];
    public static boolean ShutDown = false;
    public static int ShutDownCounter = 0;
    public static ClanManager clanManager = null;
    private static int waitFails;
    public static long minutesCounter;
    // TODO: yet to figure out proper value for timing, but 500 seems good
    BufferedReader reader;
    BufferedWriter bw = null;
    String connectingIP = null;
    private static ExecutorService executor = createExecutor();
    public static int tickCounter = 0;

    private static ExecutorService createExecutor() {
        return Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setName("ClientHandlerThread");
            t.setDaemon(false);
            return t;
        });
    }
    /**
     * The task scheduler.
     */
    private static final TaskScheduler scheduler = new TaskScheduler();

    /**
     * Gets the task scheduler.
     * @return The task scheduler.
     */
    public static TaskScheduler getTaskScheduler() {
        return scheduler;
    }
    //public static MainLoader vote = new MainLoader("127.0.0.1", "admin", "q2L65yAjhS3FeyAP", "vote");
    public server() {
        // the current way of controlling the server at runtime and a great debugging/testing tool
        //jserv js = new jserv(this);
       // js.start();

    }
    public static void resetExecutor() {
        executor = createExecutor();
    }

    public static void ServerbroadcastGlobal() {
        EventManager.getSingleton().addEvent(null, new Event() {
            private int ticks = 0;

            @Override
            public void execute(EventContainer container) {
                if (ticks++ >= 600) {
                    for (Player p : PlayerHandler.players) { // loop so it effects all players
                        if (p != null) {
                            client castOn = (client) p; // specific player's client
                            castOn.sendMessage("<shad=A9a9a9><col=7851a9>**********************************************************************************</shad></col>");
                            castOn.sendMessage("<shad=A9a9a9><col=7851a9>Have you voted today?? if not go vote!! more votes = more players =)</shad></col>");
                            castOn.sendMessage("<shad=A9a9a9><col=7851a9>Newest Update: Started to work on the stronghold of security.</shad></col>");
                            castOn.sendMessage("<shad=A9a9a9><col=7851a9>We back Guys! Remember if You lose items on death!</shad></col>");
                            castOn.sendMessage("<shad=A9a9a9><col=7851a9>Type ::help and ::commands. </shad></col>");
                            castOn.sendMessage("<shad=A9a9a9><col=7851a9>**********************************************************************************</shad></col>");
                        }
                    }
                    ticks = 0;
                }
            }

            @Override
            public void stop() {
            }

        }, 600);
    }

    public static long getTickCount() {
        return tickCount;
    }

    /**
     * Starts the minute counter
     */
    public static void main(String[] args) {

        startServer();
        new Thread(new MemoryLogger(), "MemoryLogger").start();
    }

    public static void startServer() {
        ServerbroadcastGlobal();
        EventManager.initialise();
        NPCCacheDefinition.unpackConfig();
        AnimationLength.startup();
        BobTheCatManager.init();
        lottery.loadLists();
       // server.lottery.loadLists();
        Region.init();
        textHandler = new TextHandler();
        clanManager = new ClanManager();

        npcHandler = new NPCHandler();
        // discordBot.init();
        itemHandler = new ItemHandler();
        //WalkingCheck.check();
        doorHandler = new DoorHandler();
        potions = new potions();
        objectManager = new ObjectManager();
        shopHandler = new ShopHandler();
        PickableObjects = new PickableObjects();
      //  garbageCollectorManager = new GarbageCollectorManager();
        clickingMost = new clickingMost();
        fishing = new Fishing();
        antilag = new antilag();
        itemspawnpoints = new itemspawnpoints();
        GraphicsHandler = new GraphicsHandler();
        objectHandler = new ObjectHandler();
        GlobalDrops = new GlobalDrops();
        npcDrops = new NPCDrops();

        clientHandler = new server();
        resetExecutor(); // Make sure we have a fresh executor
        executor.submit(new ClientHandlerService(clientHandler));
        playerHandler = new PlayerHandler();
        ConnectionList.getInstance();
        PlayerHandler.ServerStateHeartbeat30s(playerHandler);
        scheduler.schedule(new Task() {
            @Override
            protected void execute() {
            // could do game updating stuff in here...
            // maybe do all the major stuff here in a big loop and just do the packet
            // sending/receiving in the client subthreads. The actual packet forming code
            // will reside within here and all created packets are then relayed by the subthreads.
            // This way we avoid all the sync'in issues
            // The rough outline could look like:
                tickCount++;
                tick++;
                tickCounter++;

                if (tickCounter >= 100) { // 100 ticks = 1 minute
                    tickCounter = 0; // reset
                    setMinutesCounter(getMinutesCounter() + 1); // increment minute counter
                }
            playerHandler.process();            // updates all player related stuff
            npcHandler.process();
            itemHandler.process();
            //garbageCollectorManager.process();
            shopHandler.process();
             lottery.process();
                globalObjects.pulse();
            objectManager.process();
            antilag.process();
            //GlobalDrops.process();
            itemspawnpoints.process();
            objectHandler.process();
            objectHandler.firemaking_process();
            //System.gc();// might of fiuxed the issue noty need this anymore
            // doNpcs()		// all npc related stuff
            // doObjects()
            // doWhatever()
//            discordBot.init();
            }
        });
    }
    public static boolean checkStatus(int world) {
        ServerSocket socket = null;
        try {
            socket = new ServerSocket(world);
        } catch (IOException e) {
            return false;
        } finally {
            if (socket != null)
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return true;
    }

    public static void logError(String message) {
        misc.println(message);
    }

    public static void addUidToFile(String UUID) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter("./Data/bans/UUIDBans.txt", true));
            try {
                out.newLine();
                out.write(UUID);
            } finally {
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void setMinutesCounter(long minutesCounter) {
        server.minutesCounter = minutesCounter;
        try {
            BufferedWriter minuteCounter = new BufferedWriter(new FileWriter("./Data/minutes.log"));
            minuteCounter.write(Long.toString(getMinutesCounter()));
            minuteCounter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static long getMinutesCounter() {
        return minutesCounter;
    }

    public static boolean isPublic() {
        return true;
    }
    // Whether we're in flood testing mode (ignore bans, count all connections)
    public static boolean floodTestMode = false;
    public static int floodTestConnectionCount = 0;
    private final ExecutorService socketHandlerPool = Executors.newCachedThreadPool(); // adjust as needed
    private final LoadingCache<String, Boolean> threatCache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build(ip -> isIpBanned(ip) || isVpnIp(ip));

    public void startAccepting(ServerSocket serverSocket) {
        try {
            serverSocket.setSoTimeout(5000);
        } catch (SocketException e) {
            System.err.println("Failed to set socket timeout: " + e.getMessage());
            return;
        }

        while (!shutdownClientHandler && !shutdownServer) {
            try {
                if (serverSocket.isClosed()) {
                    System.out.println("[SOCKET] ServerSocket closed. Shutting down accept loop.");
                    break;
                }

                final Socket socket = serverSocket.accept(); // blocking 5s max
                socketHandlerPool.submit(() -> handleIncomingConnection(socket));

            } catch (SocketTimeoutException ignored) {
                // No connection this tick, loop continues
            } catch (IOException e) {
                System.err.println("Exception during socket accept: " + e.getMessage());
                e.printStackTrace();
                break;
            }
        }
    }

    private void handleIncomingConnection(Socket socket) {
        String ip = socket.getInetAddress().getHostAddress();
        int readByte = -1;

        boolean accepted = false;

        try {
            readByte = socket.getInputStream().read();

            if (floodTestMode) {
                floodTestConnectionCount++;
                logConnection(ip, readByte, "flood-test");
                handleAcceptedConnection(socket);
                accepted = true;
                return;
            }

            boolean isBlocked = threatCache.get(ip);
            boolean isValidHandshake = (readByte & 0xFF) == 14;

            logConnection(ip, readByte, isBlocked ? "blocked" : "attempt");

            if (!isBlocked && isValidHandshake) {
                logConnection(ip, readByte, "accepted");
                handleAcceptedConnection(socket);
                accepted = true; // 👈 prevent closing it below
            } else {
                logConnection(ip, readByte, "rejected");
                sendDenialMessage(socket, isBlocked ? "Blocked (VPN or banned)" : "Invalid handshake");
            }

        } catch (IOException e) {
            System.err.println("Error reading handshake from " + ip + ": " + e.getMessage());
        } finally {
            if (!accepted) {
                try {
                    if (!socket.isClosed()) socket.close();
                } catch (IOException ignored) {}
            }
        }
    }


    private void handleAcceptedConnection(Socket socket) {
        InetAddress ipAddress = socket.getInetAddress();
        String hostName = ipAddress.getHostName();

        try {
            socket.setTcpNoDelay(true); // Disable Nagle for lower latency

            if (ConnectionList.getInstance().filter(ipAddress)) {
                System.out.printf("[PLAYER] Accepting client from %s (%s)%n", hostName, ipAddress.getHostAddress());

                // === KEY LINE: Spawning the player client just like before ===
                playerHandler.newPlayerClient(socket, hostName);

                // Track the IP for connection limiting
                ConnectionList.getInstance().addConnection(ipAddress);
            } else {
                System.out.printf("[BLOCKED] Connection denied by ConnectionList: %s%n", ipAddress.getHostAddress());
                sendDenialMessage(socket, "Connection rejected by server policy.");
                socket.close();
            }

        } catch (IOException e) {
            System.err.printf("[ERROR] Failed to accept client from %s: %s%n", hostName, e.getMessage());
            try {
                socket.close();
            } catch (IOException ex) {
                System.err.printf("[ERROR] Failed to close rejected socket from %s: %s%n", hostName, ex.getMessage());
            }
        }
    }


    private void sendDenialMessage(Socket socket, String reason) {
        try {
            socket.getOutputStream().write(("DENIED: " + reason + "\n").getBytes(StandardCharsets.UTF_8));
        } catch (IOException ignored) {}
    }

    private void logConnection(String ip, int byteVal, String status) {
        String msg = String.format("[CONN] %-8s from %-15s | byte=%-3d | t=%d",
                status.toUpperCase(), ip, byteVal, System.currentTimeMillis());
        System.out.println(msg);
        // You could also push this to GUI, Discord logger, or file here
    }


    // HashSet to store VPN IPs for quick lookup
    private static HashSet<String> vpnIpSet = new HashSet<>();

    static {
        String folderPath = "./Data/zones";  // Modify this to your folder path

        // Load the VPN IP ranges from all .zone files in the folder
        loadVpnIpsFromFolder(folderPath);
    }



    // Method to load the list of VPN IPs from a folder containing .zone files
    public static void loadVpnIpsFromFolder(String folderPath) {
        File folder = new File(folderPath);

        // Get all .zone files in the folder
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".zone"));
        if (files != null) {
            for (File file : files) {
                loadVpnIpsFromFile(file);
            }
            System.out.println("Loaded IP ranges from " + files.length + " zone files.");
        } else {
            System.out.println("No .zone files found in folder: " + folderPath);
        }
    }

    // Method to load VPN IP ranges from a specific .zone file
    public static void loadVpnIpsFromFile(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                vpnIpSet.add(line.trim());  // Add each IP range to the list
            }
            System.out.println("Loaded " + vpnIpSet.size() + " VPN IP ranges from " + file.getName());
        } catch (IOException e) {
            System.err.println("Error loading VPN IPs from file " + file.getName() + ": " + e.getMessage());
        }
    }

    // Method to check if a given IP belongs to any of the VPN IP ranges (CIDR format)
    public static boolean isVpnIp(String ip) {
        for (String range : vpnIpSet) {
            if (isInRange(ip, range)) {
                return true;
            }
        }
        return false;
    }

    // Method to check if an IP is in a specific CIDR range
    public static boolean isInRange(String ip, String cidr) {
        try {
            String[] parts = cidr.split("/");
            String ipRange = parts[0];
            int prefixLength = Integer.parseInt(parts[1]);

            InetAddress targetAddress = InetAddress.getByName(ip);
            InetAddress rangeAddress = InetAddress.getByName(ipRange);

            byte[] targetBytes = targetAddress.getAddress();
            byte[] rangeBytes = rangeAddress.getAddress();

            int byteCount = prefixLength / 8;
            int bitCount = prefixLength % 8;

            // Compare full byte blocks
            for (int i = 0; i < byteCount; i++) {
                if (targetBytes[i] != rangeBytes[i]) {
                    return false;
                }
            }

            // Compare remaining bits
            if (bitCount > 0) {
                int mask = 0xFF << (8 - bitCount);
                if ((targetBytes[byteCount] & mask) != (rangeBytes[byteCount] & mask)) {
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    @Override
    public void run() {
        try {
            shutdownClientHandler = false;
            clientListener = new ServerSocket(serverlistenerPort, 50, null);
            System.out.println("- Godzhell Reborn and Remastered is Online at port " + clientListener.getLocalPort());

            // THIS is the new intake loop
            startAccepting(clientListener);

        } catch (IOException ioe) {
            if (shutdownClientHandler) {
                System.out.println("ClientHandler was shut down.");
            } else {
                System.err.println("Error: Unable to start listener on " + serverlistenerPort + " - port already in use?");
            }
        }
    }





    boolean isIpBanned(String ip) {
        String line = null;
        try {
            reader = new BufferedReader(new FileReader("data/bannedips.txt"));
            while ((line = reader.readLine()) != null) {
                if (line.equals(ip)) {
                    System.out.println("Client rejected from " + ip);
                    return true;
                }
            }
        } catch (Exception e) {
        }
        return false;
    }
    private static boolean shuttingDown = false;

    public static boolean isShuttingDown() {
        return shuttingDown;
    }
    public static boolean hasActiveConnections() {
        return playerHandler.getPlayerCount() > 0;  // Assuming playerHandler tracks connected players
    }
    public static boolean hasRunningThreads() {
        return false; // Skip JVM thread check — assume it's fine to reboot
    }

    public void killServer() {
        try {
            shuttingDown = true;
            shutdownClientHandler = true;

            if (clientListener != null && !clientListener.isClosed()) {
                clientListener.close();
                clientListener = null;
                System.out.println("Server listener socket closed.");
            }

            if (executor != null && !executor.isShutdown()) {
                executor.shutdownNow();
                executor.awaitTermination(5, TimeUnit.SECONDS);
            }

            EventManager.getSingleton().shutdown();
            scheduler.terminate();
            if (playerHandler != null) {
                playerHandler.destruct();
                playerHandler.fullyWipePlayerPresence(); // <- Wipe the ghosts
            }

            System.out.println("Server shutdown complete.");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            shuttingDown = false;
        }
    }





    public static GlobalObjects getGlobalObjects() {
        return globalObjects;
    }

}
