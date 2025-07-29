import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class RebootSystem {

    private static final long REBOOT_INTERVAL = 48 * 60 * 60 * 1000; // 12 hours
    private static final int[] WARNING_TIMES = {
            35, 34, 33, 32, 31, 30, 29, 28, 27, 26, 25,
            24, 23, 22, 21, 20, 19, 18, 17, 16, 15,
            14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1
    };

    private static final Timer timer = new Timer();

    public static void main(String[] args) {
        // Start the server and assign it to the live instance
        server.clientHandler = new server();
        server liveServer = server.clientHandler;
        liveServer.startServer();

        // Console command listener
        new Thread(RebootSystem::commandListener).start();

        // Schedule warnings and reboot
        for (int time : WARNING_TIMES) {
            timer.schedule(new CountdownTask(time), REBOOT_INTERVAL - time * 60 * 1000);
        }
        timer.schedule(new RebootTask(), REBOOT_INTERVAL);
    }

    private static void commandListener() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("\n>> ");
            String input = scanner.nextLine().trim().toLowerCase();

            server liveServer = server.clientHandler;
            if (liveServer == null) {
                System.out.print("\nLive server instance not found.");
                continue;
            }

            switch (input) {
                case "reboot":
                    System.out.print("\nManual reboot initiated.");
                    new RebootTask().run();
                    break;

                case "status":
                    System.out.print("\nServer Running: " + !server.shutdownServer);
                    System.out.print("\nUptime (minutes): " + server.getMinutesCounter());
                    break;

                case "threads":
                    System.out.print("\nActive Threads: " + Thread.activeCount());
                    break;

                case "connections":
                    System.out.print("\nActive Connections: " + server.hasActiveConnections());
                    break;

                case "forcekill":
                    System.out.print("\nForce killing server...");
                    liveServer.killServer();
                    break;

                case "exit":
                    System.out.print("\nConsole exiting. Server still running.");
                    return;

                default:
                    System.out.print("\nUnknown command: " + input);
                    System.out.print("\nAvailable: reboot, status, threads, connections, forcekill, exit");
                    break;
            }
        }
    }

    static class RebootTask extends TimerTask {
        @Override
        public void run() {
            server liveServer = server.clientHandler;

            synchronized (liveServer) {
                try {
                    System.out.print("\nRebooting now...");
                    shutdownServerGracefully(liveServer);
                    Thread.sleep(10000); // short cooldown

                    // Fully reinitialize the server instance
                    server.clientHandler = new server();
                    server.clientHandler.startServer();
                    System.out.print("S\nerver restarted successfully.");
                } catch (InterruptedException e) {
                    System.out.print("\nReboot interrupted.");
                    Thread.currentThread().interrupt();
                }
            }
        }

        private void shutdownServerGracefully(server liveServer) {
            if (liveServer.isShuttingDown()) {
                System.out.print("\nServer is already shutting down. Aborting reboot.");
                return;
            }
            System.out.println("Saving all active players before shutdown...");

            for (int i = 0; i < server.playerHandler.maxPlayers; i++) {
                Player p = server.playerHandler.players[i];
                if (p != null && p.isActive) {
                    server.playerHandler.handleDisconnect(p); // ✅ force it
                }
            }



            System.out.print("\nShutting down server...");
            liveServer.killServer();

            while (liveServer.hasActiveConnections() || liveServer.hasRunningThreads()) {
                System.out.print("\nWaiting for all connections and threads to finish...");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.out.print("\nInterrupted while waiting for shutdown.");
                    Thread.currentThread().interrupt();
                }
            }

            System.out.print("\nShutdown complete.");
        }
    }

    static class CountdownTask extends TimerTask {
        private final int time;

        public CountdownTask(int time) {
            this.time = time;
        }

        @Override
        public void run() {
            System.out.print("\n* * * Server Rebooting in " + time + " minutes * * *");
            PlayerHandler.messageToAll = "@blu@* * * Server Rebooting in " + time + " minutes * * *";
        }
    }
}
