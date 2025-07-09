import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.management.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class MemoryLogger implements Runnable {

    private static final String LOG_FILE = "./Data/logs/memory.log";
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
    private long lastHeapUsed = -1;

    @Override
    public void run() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        var gcBeans = ManagementFactory.getGarbageCollectorMXBeans();

        while (true) {
            try (PrintWriter out = new PrintWriter(new FileWriter(LOG_FILE, true))) {
                MemoryUsage heap = memoryMXBean.getHeapMemoryUsage();
                MemoryUsage nonHeap = memoryMXBean.getNonHeapMemoryUsage();

                long usedHeap = heap.getUsed() / (1024 * 1024);
                long maxHeap = heap.getMax() / (1024 * 1024);
                long usedNonHeap = nonHeap.getUsed() / (1024 * 1024);

                String time = TIME_FORMAT.format(new Date());
                out.printf("[%s] Heap: %dMB / %dMB, Non-Heap: %dMB%n", time, usedHeap, maxHeap, usedNonHeap);

                // GC Info
                for (GarbageCollectorMXBean gc : gcBeans) {
                    out.printf("GC [%s]: count=%d, time=%dms%n",
                            gc.getName(), gc.getCollectionCount(), gc.getCollectionTime());
                }

                // Thread count info
                out.printf("Threads: current=%d, peak=%d, totalStarted=%d%n",
                        threadMXBean.getThreadCount(),
                        threadMXBean.getPeakThreadCount(),
                        threadMXBean.getTotalStartedThreadCount());

                // Spike detection
                if (lastHeapUsed > 0 && usedHeap - lastHeapUsed > 10) {
                    out.println("⚠️ Memory spike detected — Dumping all thread stack traces:");

                    Map<Thread, StackTraceElement[]> traces = Thread.getAllStackTraces();
                    for (Map.Entry<Thread, StackTraceElement[]> entry : traces.entrySet()) {
                        Thread t = entry.getKey();
                        StackTraceElement[] stack = entry.getValue();
                        out.printf("🧵 Thread: \"%s\" [%s]%n", t.getName(), t.getState());
                        for (StackTraceElement ste : stack) {
                            out.println("    at " + ste);
                        }
                        out.println();
                    }
                }

                lastHeapUsed = usedHeap;

            } catch (Exception e) {
                System.err.println("Failed to write memory log: " + e.getMessage());
                e.printStackTrace();
            }

            try {
                Thread.sleep(30 * 1000); // 30 seconds
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
