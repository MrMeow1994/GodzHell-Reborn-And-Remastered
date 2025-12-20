import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public final class IpConnectionState {
    final AtomicInteger connections = new AtomicInteger(0);
    final AtomicInteger strikes = new AtomicInteger(0);
    final AtomicLong lastSeen = new AtomicLong(System.currentTimeMillis());
}
