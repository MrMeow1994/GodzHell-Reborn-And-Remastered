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

            String[] parts = line.split(",\\s*");
            if (parts.length < 4) continue;

            try {
                int npcId = Integer.parseInt(parts[0]);
                int plane = Integer.parseInt(parts[1]);
                int x = Integer.parseInt(parts[2]);
                int y = Integer.parseInt(parts[3]);

                int rangeX1, rangeY1, rangeX2, rangeY2;

                if (parts.length >= 8) {
                    // Explicit walk range
                    rangeX1 = Integer.parseInt(parts[4]);
                    rangeY1 = Integer.parseInt(parts[5]);
                    rangeX2 = Integer.parseInt(parts[6]);
                    rangeY2 = Integer.parseInt(parts[7]);
                } else {
                    // Auto-walk: 1-tile radius
                    rangeX1 = x + 1;
                    rangeY1 = y + 1;
                    rangeX2 = x - 1;
                    rangeY2 = y - 1;
                }

                int walkType = 1; // Always walkable
                String name = "";

                sb.append(String.format(
                        "spawn = %d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%s%n",
                        npcId,
                        x,
                        y,
                        plane,
                        rangeX1,
                        rangeY1,
                        rangeX2,
                        rangeY2,
                        walkType,
                        name
                ));

            } catch (NumberFormatException ignored) {}
        }

        return sb.toString();
    }

}
