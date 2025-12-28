import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.management.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MemoryMonitorGUI implements Runnable {

    private JFrame frame;
    private JLabel heapLabel, nonHeapLabel, threadLabel;
    private JProgressBar heapBar, nonHeapBar;
    private JTable classTable;
    private DefaultTableModel classTableModel;
    private JTextField filterField;
    private JCheckBox autoRefreshToggle;
    private JButton refreshButton;

    private final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    private final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    private final List<Long> heapHistory = new ArrayList<>();
    private final List<Long> nonHeapHistory = new ArrayList<>();
    private final int historySize = 60;

    private HeapGraphPanel heapGraphPanel;
    private LineGraphPanel lineGraphPanel;
    private final Object histogramLock = new Object();
    private volatile boolean histogramRunning = false;
    private volatile long lastHistogramRun = 0L;

    // 15 seconds is sane for a real profiler
    private static final long HISTOGRAM_INTERVAL_MS = 15_000;

    @Override
    public void run() {
        System.setProperty("java.awt.headless", "false");
        SwingUtilities.invokeLater(() -> {
            initGUI();
            startAutoRefresh();
        });
    }

    private void initGUI() {
        frame = new JFrame("Advanced Memory Profiler");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1400, 900);

        // Top Layer: Circular Graph
        heapGraphPanel = new HeapGraphPanel();
        heapGraphPanel.setPreferredSize(new Dimension(400, 250));

        heapLabel = new JLabel("Heap: ");
        nonHeapLabel = new JLabel("Non-Heap: ");
        threadLabel = new JLabel("Threads: ");
        heapBar = new JProgressBar(0, 100);
        nonHeapBar = new JProgressBar(0, 100);

        JPanel topInfoPanel = new JPanel(new GridLayout(2, 3));
        topInfoPanel.add(heapLabel);
        topInfoPanel.add(nonHeapLabel);
        topInfoPanel.add(threadLabel);
        topInfoPanel.add(heapBar);
        topInfoPanel.add(nonHeapBar);
        topInfoPanel.add(new JLabel(""));

        JPanel topLayer = new JPanel(new BorderLayout());
        topLayer.add(heapGraphPanel, BorderLayout.CENTER);
        topLayer.add(topInfoPanel, BorderLayout.SOUTH);

        // Middle Layer: Class Table
        classTableModel = new DefaultTableModel(
                new Object[]{"Class", "Package / Source", "Instances", "Bytes", "Usage"}, 0);
        classTable = new JTable(classTableModel);
        classTable.setFillsViewportHeight(true);
        classTable.setAutoCreateRowSorter(true);
        classTable.getColumn("Usage").setCellRenderer(new UsageBarRenderer());

        // Row coloring
        classTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String packageName = (String) table.getValueAt(row, 1);
                if (packageName.equals("(unknown)")) setBackground(new Color(255, 200, 200));
                else if (packageName.startsWith("com.yourserverpackage")) setBackground(new Color(200, 255, 200));
                else if (packageName.startsWith("java.")) setBackground(new Color(220, 220, 220));
                else setBackground(Color.WHITE);
                if (isSelected) setBackground(table.getSelectionBackground());
                return this;
            }
        });

        // Double-click
        classTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = classTable.getSelectedRow();
                    if (row < 0) return;
                    String className = (String) classTable.getValueAt(row, 0);
                    executor.submit(() -> openClassInIDE(className));
                }
            }
        });

        addClassTableContextMenu(); // Add right-click context menu

        JScrollPane middleScroll = new JScrollPane(classTable);

        JPanel middleControlPanel = new JPanel();
        filterField = new JTextField(20);
        filterField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                executor.submit(MemoryMonitorGUI.this::updateClassHistogramAsync);
            }
        });
        autoRefreshToggle = new JCheckBox("Auto Refresh", true);
        refreshButton = new JButton("Refresh Now");
        refreshButton.addActionListener(e -> executor.submit(this::updateStats));

        middleControlPanel.add(new JLabel("Filter: "));
        middleControlPanel.add(filterField);
        middleControlPanel.add(autoRefreshToggle);
        middleControlPanel.add(refreshButton);

        JPanel middleLayer = new JPanel(new BorderLayout());
        middleLayer.add(middleScroll, BorderLayout.CENTER);
        middleLayer.add(middleControlPanel, BorderLayout.SOUTH);

        // Bottom Layer: Line Graph
        lineGraphPanel = new LineGraphPanel();
        lineGraphPanel.setPreferredSize(new Dimension(1300, 250));

        // Split layout
        JSplitPane splitTopMiddle = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topLayer, middleLayer);
        splitTopMiddle.setResizeWeight(0.3);
        JSplitPane splitAll = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitTopMiddle, lineGraphPanel);
        splitAll.setResizeWeight(0.7);

        frame.setLayout(new BorderLayout());
        frame.add(splitAll, BorderLayout.CENTER);
        frame.setVisible(true);
        frame.toFront();
    }

    private void addClassTableContextMenu() {
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem openInIDE = new JMenuItem("Open in IDE");
        openInIDE.addActionListener(e -> {
            int row = classTable.getSelectedRow();
            if (row < 0) return;
            String className = (String) classTable.getValueAt(row, 0);
            executor.submit(() -> openClassInIDE(className));
        });

        JMenuItem showProfiler = new JMenuItem("Show Profiler");
        showProfiler.addActionListener(e -> {
            int row = classTable.getSelectedRow();
            if (row < 0) return;
            String className = (String) classTable.getValueAt(row, 0);
            executor.submit(() -> showClassProfilerAsync(className));
        });

        popupMenu.add(openInIDE);
        popupMenu.add(showProfiler);

        classTable.setComponentPopupMenu(popupMenu);
    }

    private void openClassInIDE(String className) {
        try {
            Class<?> cls = Class.forName(className);
            // Get folder containing the compiled class
            String location = cls.getProtectionDomain().getCodeSource().getLocation().getPath();
            java.io.File classDir = new java.io.File(location);

            // No packages, so class file is directly in the folder
            java.io.File fullPath = new java.io.File(classDir, className + ".java");

            if (fullPath.exists()) {
                // Open in IntelliJ
                new ProcessBuilder("idea", fullPath.getAbsolutePath()).start();
            } else {
                // fallback to profiler if file not found
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(frame,
                                "Cannot locate source for " + className + ", opening profiler.",
                                "Source Not Found", JOptionPane.WARNING_MESSAGE));
                showClassProfilerAsync(className);
            }

        } catch (Exception ex) {
            SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(frame,
                            "Cannot open class " + className + ", opening profiler.",
                            "Error", JOptionPane.WARNING_MESSAGE));
            showClassProfilerAsync(className);
        }
    }




    private void startAutoRefresh() {
        new java.util.Timer(true).scheduleAtFixedRate(new java.util.TimerTask() {
            public void run() {
                if (autoRefreshToggle.isSelected()) executor.submit(MemoryMonitorGUI.this::updateStats);
            }
        }, 0, 3000);
    }

    private void updateStats() {
        try {
            MemoryUsage heap = memoryMXBean.getHeapMemoryUsage();
            MemoryUsage nonHeap = memoryMXBean.getNonHeapMemoryUsage();
            long heapUsedMB = heap.getUsed() / 1024 / 1024;
            long heapMaxMB = heap.getMax() / 1024 / 1024;
            long nonHeapUsedMB = nonHeap.getUsed() / 1024 / 1024;

            synchronized (heapHistory) { heapHistory.add(heapUsedMB); if (heapHistory.size() > historySize) heapHistory.remove(0); }
            synchronized (nonHeapHistory) { nonHeapHistory.add(nonHeapUsedMB); if (nonHeapHistory.size() > historySize) nonHeapHistory.remove(0); }

            int heapPercent = (int) (100.0 * heapUsedMB / heapMaxMB);
            int nonHeapPercent = (int) (100.0 * nonHeapUsedMB / (nonHeapUsedMB + 50));

            SwingUtilities.invokeLater(() -> {
                heapLabel.setText(String.format("Heap: %d MB / %d MB", heapUsedMB, heapMaxMB));
                nonHeapLabel.setText(String.format("Non-Heap: %d MB", nonHeapUsedMB));
                threadLabel.setText(String.format("Threads: %d (peak: %d)", threadMXBean.getThreadCount(), threadMXBean.getPeakThreadCount()));
                heapBar.setValue(heapPercent);
                nonHeapBar.setValue(nonHeapPercent);
                heapGraphPanel.updateValues(heapUsedMB, heapMaxMB, nonHeapUsedMB);
                lineGraphPanel.repaint();
            });

            updateClassHistogramAsync();

        } catch (Exception e) { e.printStackTrace(); }
    }

    private void updateClassHistogramAsync() {
        executor.submit(() -> {

            // --- throttle ---
            long now = System.currentTimeMillis();
            if (now - lastHistogramRun < HISTOGRAM_INTERVAL_MS) {
                return;
            }

            synchronized (histogramLock) {
                if (histogramRunning) return;
                histogramRunning = true;
                lastHistogramRun = now;
            }

            try {
                String pid = ManagementFactory.getRuntimeMXBean()
                        .getName()
                        .split("@")[0];

                Process proc = new ProcessBuilder(
                        "jcmd", pid, "GC.class_histogram"
                ).start();

                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(proc.getInputStream()));

                String line;
                boolean start = false;
                String filter = filterField.getText().trim().toLowerCase();

                List<Object[]> rows = new ArrayList<>();
                long maxBytes = 1;

                while ((line = reader.readLine()) != null) {
                    if (line.startsWith(" num")) {
                        start = true;
                        continue;
                    }
                    if (!start) continue;

                    line = line.trim();
                    if (line.isEmpty()) continue;

                    String[] parts = line.split("\\s+");
                    if (parts.length < 4) continue;

                    String className = parts[3];
                    if (!filter.isEmpty() &&
                            !className.toLowerCase().contains(filter)) {
                        continue;
                    }

                    String instances = parts[1];
                    long bytes = Long.parseLong(parts[2]);
                    maxBytes = Math.max(maxBytes, bytes);

                    String pkg = "(unknown)";
                    int dot = className.lastIndexOf('.');
                    if (dot != -1) pkg = className.substring(0, dot);

                    rows.add(new Object[]{
                            className,
                            pkg,
                            instances,
                            bytes,
                            0 // usage filled later
                    });
                }

                final long finalMaxBytes = maxBytes;

                SwingUtilities.invokeLater(() -> {
                    // map existing rows so we UPDATE instead of clear()
                    Map<String, Integer> rowIndex = new HashMap<>();
                    for (int i = 0; i < classTableModel.getRowCount(); i++) {
                        rowIndex.put(
                                (String) classTableModel.getValueAt(i, 0),
                                i
                        );
                    }

                    for (Object[] r : rows) {
                        String className = (String) r[0];
                        String pkg = (String) r[1];
                        String instances = (String) r[2];
                        long bytes = (long) r[3];

                        int usage =
                                (int) ((bytes * 100.0) / finalMaxBytes);

                        if (rowIndex.containsKey(className)) {
                            int row = rowIndex.get(className);
                            classTableModel.setValueAt(instances, row, 2);
                            classTableModel.setValueAt(bytes, row, 3);
                            classTableModel.setValueAt(usage, row, 4);
                        } else {
                            classTableModel.addRow(new Object[]{
                                    className,
                                    pkg,
                                    instances,
                                    bytes,
                                    usage
                            });
                        }
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                histogramRunning = false;
            }
        });
    }

    private void showClassProfilerAsync(String className) {
        executor.submit(() -> {
            try {
                StringBuilder sb = new StringBuilder();
                sb.append("Memory Profiler for ").append(className).append("\n\n");
                long[] ids = threadMXBean.getAllThreadIds();
                ThreadInfo[] infos = threadMXBean.getThreadInfo(ids, Integer.MAX_VALUE);
                for (ThreadInfo info : infos) {
                    if (info == null) continue;
                    sb.append("Thread: ").append(info.getThreadName()).append(" [").append(info.getThreadState()).append("]\n");
                    for (StackTraceElement ste : info.getStackTrace()) sb.append("    at ").append(ste).append("\n");
                    sb.append("\n");
                }

                JTextArea textArea = new JTextArea(sb.toString());
                textArea.setEditable(false);
                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(1000, 600));

                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(frame, scrollPane, "Profiler: " + className, JOptionPane.INFORMATION_MESSAGE));

            } catch (Exception e) { e.printStackTrace(); }
        });
    }

    static class UsageBarRenderer extends JProgressBar implements javax.swing.table.TableCellRenderer {
        public UsageBarRenderer() { super(0, 100); setStringPainted(true); }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            int val = value instanceof Integer ? (Integer) value : 0;
            setValue(val); setString(val + "%");
            if (isSelected) setBackground(table.getSelectionBackground()); else setBackground(table.getBackground());
            return this;
        }
    }

    static class HeapGraphPanel extends JPanel {
        private long heapUsed=0, heapMax=1, nonHeapUsed=0;
        public void updateValues(long heapUsed, long heapMax, long nonHeapUsed) { this.heapUsed=heapUsed; this.heapMax=heapMax; this.nonHeapUsed=nonHeapUsed; repaint(); }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g); Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w=getWidth(), h=getHeight(), size=Math.min(w,h)-40, x=(w-size)/2, y=(h-size)/2;
            double heapAngle = 360.0*heapUsed/heapMax;
            g2.setColor(new Color(0x66CCFF)); g2.fillArc(x,y,size,size,90,-(int)heapAngle);
            g2.setColor(new Color(0xFF9966)); g2.fillArc(x,y,size,size,90-(int)heapAngle,-(int)(360-heapAngle));
            g2.setColor(Color.BLACK); g2.drawOval(x,y,size,size);
            g2.drawString("Heap: "+heapUsed+" MB",x+10,y+20); g2.drawString("Non-Heap: "+nonHeapUsed+" MB",x+10,y+40);
        }
    }

    class LineGraphPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g); Graphics2D g2=(Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w=getWidth(), h=getHeight();
            g2.setColor(Color.WHITE); g2.fillRect(0,0,w,h); g2.setColor(Color.LIGHT_GRAY); g2.drawRect(0,0,w-1,h-1);
            synchronized(heapHistory){
                if(heapHistory.isEmpty()) return;
                int size=heapHistory.size();
                long maxHeapVal=Collections.max(heapHistory);
                long maxNonHeapVal=Collections.max(nonHeapHistory);
                long maxVal=Math.max(maxHeapVal,maxNonHeapVal);
                int prevX=0, prevYHeap=0, prevYNonHeap=0;
                for(int i=0;i<size;i++){
                    int x=i*w/historySize;
                    int yHeap=h-(int)(heapHistory.get(i)*h/(maxVal+1));
                    int yNonHeap=h-(int)(nonHeapHistory.get(i)*h/(maxVal+1));
                    g2.setColor(Color.BLUE); if(i>0) g2.drawLine(prevX,prevYHeap,x,yHeap);
                    g2.setColor(Color.ORANGE); if(i>0) g2.drawLine(prevX,prevYNonHeap,x,yNonHeap);
                    prevX=x; prevYHeap=yHeap; prevYNonHeap=yNonHeap;
                }
            }
            g2.setColor(Color.BLACK); g2.drawString("Heap (blue), Non-Heap (orange)",10,15);
        }
    }

}
