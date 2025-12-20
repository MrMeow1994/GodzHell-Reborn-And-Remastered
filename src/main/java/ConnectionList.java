import java.net.InetAddress;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

public final class ConnectionList {
    public static int MAX_CONNECTIONS_PER_IP = 2000;
    private static final int RATE_LIMIT = 10; // max 10 connects
    private static final long RATE_WINDOW_MS = 1000; // 1 second

    private static ConnectionList instance = null;

    // Tracks total active connections
    private final ConcurrentHashMap<String, Integer> connectionMap = new ConcurrentHashMap<>();

    // Tracks recent connection timestamps for rate-limiting
    private final ConcurrentHashMap<String, Deque<Long>> connectionTimes = new ConcurrentHashMap<>();

    private ConnectionList() { }

    public static ConnectionList getInstance() {
        if (instance == null) instance = new ConnectionList();
        return instance;
    }

    /** Filter IP by total connections AND rate limit */
    public boolean filter(InetAddress address) {
        String ip = address.getHostAddress();

        // Check total active connections
        if (connectionMap.getOrDefault(ip, 0) >= MAX_CONNECTIONS_PER_IP) {
            return false;
        }

        // Check recent connection rate
        long now = System.currentTimeMillis();
        Deque<Long> deque = connectionTimes.computeIfAbsent(ip, k -> new ConcurrentLinkedDeque<>());

        // Remove timestamps older than RATE_WINDOW_MS
        while (!deque.isEmpty() && now - deque.peekFirst() > RATE_WINDOW_MS) {
            deque.pollFirst();
        }

        // Too many connections in the last window
        if (deque.size() >= RATE_LIMIT) {
            return false;
        }

        // Allow: record this attempt
        deque.addLast(now);
        return true;
    }

    /** Adds one active connection */
    public boolean addConnection(InetAddress address) {
        String ip = address.getHostAddress();
        connectionMap.merge(ip, 1, Integer::sum);
        return true;
    }

    /** Removes one active connection */
    public void removeConnection(InetAddress address) {
        String ip = address.getHostAddress();
        connectionMap.computeIfPresent(ip, (k, v) -> (v > 1) ? v - 1 : null);
    }
}
