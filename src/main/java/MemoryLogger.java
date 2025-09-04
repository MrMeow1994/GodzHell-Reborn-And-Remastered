import java.io.*;
import java.lang.management.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class MemoryLogger implements Runnable {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final long STACK_DUMP_COOLDOWN = 5 * 60 * 1000;
    private static final long HISTOGRAM_TRIGGER_MB = 400;
    private static final boolean ENABLE_HISTOGRAM_DUMP = true;

    private long lastHeapUsed = -1;
    private long lastStackDumpTime = 0;

    private String getLogFile(String type) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String dirPath = "./Data/logs/memory/" + date;
        new File(dirPath).mkdirs();
        return dirPath + "/" + type + ".log";
    }

    @Override
    public void run() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        var gcBeans = ManagementFactory.getGarbageCollectorMXBeans();

        while (true) {
            try (PrintWriter out = new PrintWriter(new FileWriter(getLogFile("heap"), true))) {
                MemoryUsage heap = memoryMXBean.getHeapMemoryUsage();
                MemoryUsage nonHeap = memoryMXBean.getNonHeapMemoryUsage();

                long usedHeap = heap.getUsed() / (1024 * 1024);
                long maxHeap = heap.getMax() / (1024 * 1024);
                long usedNonHeap = nonHeap.getUsed() / (1024 * 1024);

                String time = TIME_FORMAT.format(LocalTime.now());
                out.printf("[%s] Heap: %dMB / %dMB, Non-Heap: %dMB%n", time, usedHeap, maxHeap, usedNonHeap);

                lastHeapUsed = usedHeap;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            try (PrintWriter out = new PrintWriter(new FileWriter(getLogFile("gc"), true))) {
                for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
                    out.printf("[%s] GC [%s]: count=%d, time=%dms%n",
                            TIME_FORMAT.format(LocalTime.now()),
                            gc.getName(), gc.getCollectionCount(), gc.getCollectionTime());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            try (PrintWriter out = new PrintWriter(new FileWriter(getLogFile("threads"), true))) {
                out.printf("[%s] Threads: current=%d, peak=%d, totalStarted=%d%n",
                        TIME_FORMAT.format(LocalTime.now()),
                        threadMXBean.getThreadCount(),
                        threadMXBean.getPeakThreadCount(),
                        threadMXBean.getTotalStartedThreadCount());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            long now = System.currentTimeMillis();
            boolean memorySpike = lastHeapUsed > 0 && lastHeapUsed >= HISTOGRAM_TRIGGER_MB;

            if ((memorySpike) && (now - lastStackDumpTime) >= STACK_DUMP_COOLDOWN) {
                lastStackDumpTime = now;

                try (PrintWriter out = new PrintWriter(new FileWriter(getLogFile("stacktrace"), true))) {
                    out.printf("[%s] ⚠️ Stack trace dump:%n", TIME_FORMAT.format(LocalTime.now()));
                    long[] ids = threadMXBean.getAllThreadIds();
                    ThreadInfo[] infos = threadMXBean.getThreadInfo(ids, Integer.MAX_VALUE);
                    for (ThreadInfo info : infos) {
                        if (info == null) continue;
                        out.printf("🧵 Thread: \"%s\" [%s]%n", info.getThreadName(), info.getThreadState());
                        for (StackTraceElement ste : info.getStackTrace()) {
                            out.println("    at " + ste);
                        }
                        out.println();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                if (ENABLE_HISTOGRAM_DUMP) {
                    try (PrintWriter out = new PrintWriter(new FileWriter(getLogFile("histogram"), true))) {
                        dumpMemoryHistogram(out);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            try {
                Thread.sleep(30 * 1000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private static void dumpMemoryHistogram(PrintWriter out) {
        try {
            String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
            Process proc = Runtime.getRuntime().exec("jcmd " + pid + " GC.class_histogram");
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            out.printf("[%s] 🔍 Memory Histogram:%n", TIME_FORMAT.format(LocalTime.now()));
            String line;
            int lines = 0;
            while ((line = reader.readLine()) != null && lines++ < 100) {
                out.println(line);
            }
        } catch (Exception e) {
            out.println("⚠️ Failed to dump memory histogram: " + e.getMessage());
        }
    }
}