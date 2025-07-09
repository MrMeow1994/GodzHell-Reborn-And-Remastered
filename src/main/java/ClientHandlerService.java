public class ClientHandlerService implements Runnable {
    private final server server;

    public ClientHandlerService(server server) {
        this.server = server;
    }

    @Override
    public void run() {
        server.run(); // reuse your logic
    }
}
