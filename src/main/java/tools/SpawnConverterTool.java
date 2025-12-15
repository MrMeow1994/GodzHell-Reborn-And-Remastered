package tools;

import javax.swing.*;
import java.awt.*;

public class SpawnConverterTool {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SpawnConverterTool::new);
    }

    public SpawnConverterTool() {
        JFrame frame = new JFrame("Spawn Converter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 600);

        JTextArea input = new JTextArea();
        JTextArea output = new JTextArea();
        output.setEditable(false);

        JButton convert = new JButton("Convert");

        convert.addActionListener(e -> output.setText(convertSpawns(input.getText())));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(input), new JScrollPane(output));
        split.setDividerLocation(480);

        frame.setLayout(new BorderLayout());
        frame.add(split, BorderLayout.CENTER);
        frame.add(convert, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private String convertSpawns(String text) {
        StringBuilder sb = new StringBuilder();

        for (String line : text.split("\\n")) {
            line = line.trim();
            if (line.isEmpty()) continue;

            // Expected input:
            // npcId, plane, x, y, -1, false
            String[] parts = line.split(",\\s*");
            if (parts.length < 4) continue;

            try {
                int npcId = Integer.parseInt(parts[0]);
                int plane = Integer.parseInt(parts[1]);
                int x = Integer.parseInt(parts[2]);
                int y = Integer.parseInt(parts[3]);

                // These are NOT stats — just fixed placeholders to satisfy the format
                int walkType = 1000;
                int a = 5151;
                int b = 4242;
                int c = 5151;
                int d = 0;

                // Name intentionally left blank for you to fill later
                String name = "";

                sb.append(String.format(
                        "spawn = %d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%s%n",
                        npcId, x, y, plane, walkType, a, b, c, d, name
                ));

            } catch (NumberFormatException ignored) {}
        }

        return sb.toString();
    }
}
