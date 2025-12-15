package tools;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MusicTrackSorterGUI extends JFrame {

    private static final Pattern ENTRY =
            Pattern.compile("\\[\\s*\\d+\\s*,\\s*\"([^\"]*)\"\\s*\\]");

    private final JTextArea inputArea = new JTextArea();
    private final JTextArea outputArea = new JTextArea();
    private final JTextArea logArea = new JTextArea();

    public MusicTrackSorterGUI() {
        setTitle("Music Track Sorter");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);

        inputArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        outputArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));

        outputArea.setEditable(false);
        logArea.setEditable(false);

        JButton sortButton = new JButton("Sort Tracks");
        sortButton.addActionListener(e -> process());

        JSplitPane verticalSplit = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                wrap("Input (paste raw code here)", inputArea),
                wrap("Output (Java String[])", outputArea)
        );
        verticalSplit.setResizeWeight(0.55);

        JSplitPane mainSplit = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                verticalSplit,
                wrap("Log", logArea)
        );
        mainSplit.setResizeWeight(0.75);

        add(mainSplit, BorderLayout.CENTER);
        add(sortButton, BorderLayout.SOUTH);
    }

    private JScrollPane wrap(String title, JTextArea area) {
        JScrollPane pane = new JScrollPane(area);
        pane.setBorder(BorderFactory.createTitledBorder(title));
        return pane;
    }

    private void process() {
        outputArea.setText("");
        logArea.setText("");

        String input = inputArea.getText();
        Matcher matcher = ENTRY.matcher(input);

        List<String> tracks = new ArrayList<>();
        int found = 0;

        while (matcher.find()) {
            found++;
            String name = matcher.group(1);

            if (name.trim().isEmpty()) {
                log("Skipped empty entry");
                continue;
            }

            tracks.add(name);
            log("Found: " + name);
        }

        log("\nTotal parsed entries: " + found);
        log("Valid tracks: " + tracks.size());
        log("\nSorting...\n");

        tracks.sort(trackComparator());

        StringBuilder out = new StringBuilder();
        out.append("String[] musicTracks = {\n");

        for (String track : tracks) {
            out.append("    \"").append(escape(track)).append("\",\n");
        }

        out.append("};");

        outputArea.setText(out.toString());
        log("Done.");
    }

    private void log(String msg) {
        logArea.append(msg + "\n");
    }

    private static Comparator<String> trackComparator() {
        return (a, b) -> {
            boolean aNum = startsWithDigit(a);
            boolean bNum = startsWithDigit(b);

            if (aNum && !bNum) return -1;
            if (!aNum && bNum) return 1;

            return a.compareToIgnoreCase(b);
        };
    }

    private static boolean startsWithDigit(String s) {
        return !s.isEmpty() && Character.isDigit(s.charAt(0));
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
                new MusicTrackSorterGUI().setVisible(true)
        );
    }
}
