package tools;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class NPCSpawnEditor extends JFrame {

    private JTextArea fileContent;

    private final File spawnDir = new File("./Data/cfg/spawns");

    private final Map<Integer, List<RegionSpawn>> spawnsByRegion = new HashMap<>();
    private int currentRegion = -1;
    private JTextField searchField;
    private JList<Integer> regionList;
    private DefaultListModel<Integer> regionListModel;

    public NPCSpawnEditor() {
        super("NPC Spawn Editor (BIN)");
        initUI();
        loadAllBins();
    }

    private void initUI() {
        setSize(1000, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // ===== Region list =====
        regionListModel = new DefaultListModel<>();
        regionList = new JList<>(regionListModel);
        regionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        regionList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Integer regionId = regionList.getSelectedValue();
                if (regionId != null) {
                    loadRegion(regionId);
                }
            }
        });

        JScrollPane regionScroll = new JScrollPane(regionList);
        regionScroll.setPreferredSize(new Dimension(160, 0));
        add(regionScroll, BorderLayout.WEST);

        // ===== Editor =====
        fileContent = new JTextArea();
        add(new JScrollPane(fileContent), BorderLayout.CENTER);

        // ===== Top bar =====
        JPanel top = new JPanel();

        searchField = new JTextField(8);
        JButton load = new JButton("Load Region");
        JButton save = new JButton("Save Region");

        load.addActionListener(e -> searchRegion());
        save.addActionListener(e -> saveRegion());

        top.add(new JLabel("Region ID:"));
        top.add(searchField);
        top.add(load);
        top.add(save);

        add(top, BorderLayout.NORTH);
    }

    private void searchRegion() {
        try {
            int regionId = Integer.parseInt(searchField.getText());

            int index = regionListModel.indexOf(regionId);
            if (index == -1) {
                JOptionPane.showMessageDialog(this, "Region not found: " + regionId);
                return;
            }

            regionList.setSelectedIndex(index);
            regionList.ensureIndexIsVisible(index);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid region ID");
        }
    }


    // ======================
    // Core BIN handling
    // ======================

    private void loadAllBins() {
        spawnsByRegion.clear();

        File[] files = spawnDir.listFiles((d, n) -> n.endsWith(".bin"));
        if (files == null) return;

        for (File file : files) {
            try (DataInputStream in =
                         new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {

                while (in.available() > 0) {
                    RegionSpawn s = RegionSpawn.read(in);
                    spawnsByRegion
                            .computeIfAbsent(s.regionId(), k -> new ArrayList<>())
                            .add(s);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        regionListModel.clear();

        List<Integer> regions = new ArrayList<>(spawnsByRegion.keySet());
        Collections.sort(regions);

        for (int regionId : regions) {
            regionListModel.addElement(regionId);
        }

    }

    private void loadRegion(int regionId) {
        currentRegion = regionId;

        List<RegionSpawn> list = spawnsByRegion.get(regionId);
        if (list == null || list.isEmpty()) {
            fileContent.setText("// No spawns for region " + regionId);
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (RegionSpawn s : list) {
            sb.append(s).append('\n');
        }
        fileContent.setText(sb.toString());
    }

    private void saveRegion() {
        if (currentRegion == -1) {
            JOptionPane.showMessageDialog(this, "No region loaded");
            return;
        }

        File out = new File(spawnDir, currentRegion + ".bin");

        try (DataOutputStream outStream =
                     new DataOutputStream(new BufferedOutputStream(new FileOutputStream(out)))) {

            String[] lines = fileContent.getText().split("\n");
            for (String line : lines) {
                if (line.isBlank() || line.startsWith("//")) continue;
                RegionSpawn.fromText(line).write(outStream);
            }

            JOptionPane.showMessageDialog(this, "Region saved: " + currentRegion);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ======================
    // Data Model
    // ======================

    static final class RegionSpawn {
        int npcId;
        int x, y;
        int plane;
        int rx1, ry1, rx2, ry2;
        int walkType;

        static RegionSpawn read(DataInputStream in) throws IOException {
            RegionSpawn s = new RegionSpawn();
            s.npcId = in.readInt();
            s.x = in.readShort();
            s.y = in.readShort();
            s.plane = in.readByte();
            s.rx1 = in.readShort();
            s.ry1 = in.readShort();
            s.rx2 = in.readShort();
            s.ry2 = in.readShort();
            s.walkType = in.readByte();
            return s;
        }

        void write(DataOutputStream out) throws IOException {
            out.writeInt(npcId);
            out.writeShort(x);
            out.writeShort(y);
            out.writeByte(plane);
            out.writeShort(rx1);
            out.writeShort(ry1);
            out.writeShort(rx2);
            out.writeShort(ry2);
            out.writeByte(walkType);
        }

        int regionId() {
            return (x >> 6 << 8) | (y >> 6);
        }

        static RegionSpawn fromText(String line) {
            String[] p = line.split(",\\s*");
            RegionSpawn s = new RegionSpawn();
            s.npcId = Integer.parseInt(p[0]);
            s.x = Integer.parseInt(p[1]);
            s.y = Integer.parseInt(p[2]);
            s.plane = Integer.parseInt(p[3]);
            s.rx1 = Integer.parseInt(p[4]);
            s.ry1 = Integer.parseInt(p[5]);
            s.rx2 = Integer.parseInt(p[6]);
            s.ry2 = Integer.parseInt(p[7]);
            s.walkType = Integer.parseInt(p[8]);
            return s;
        }

        @Override
        public String toString() {
            return npcId + ", " + x + ", " + y + ", " + plane + ", "
                    + rx1 + ", " + ry1 + ", " + rx2 + ", " + ry2 + ", " + walkType;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new NPCSpawnEditor().setVisible(true));
    }
}
