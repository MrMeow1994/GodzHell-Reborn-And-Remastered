package tools;

import java.io.*;
import java.util.*;

public class SoundSwitchGenerator {

    public static void main(String[] args) throws IOException {
        File file = new File("sounds.txt");
        if (!file.exists()) {
            System.out.println("❌ sounds.txt not found.");
            return;
        }

        Map<Integer, Integer> itemToSound = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || !line.contains("(")) continue;

                int soundId = Integer.parseInt(line.substring(1, line.indexOf(',')));
                String itemsRaw = line.substring(line.indexOf('[') + 1, line.indexOf(']'));
                String[] itemIds = itemsRaw.split(",");

                for (String item : itemIds) {
                    try {
                        int itemId = Integer.parseInt(item.trim());
                        itemToSound.put(itemId, soundId);
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        System.out.println("public int getSoundForItem(int itemId) {");
        System.out.println("    switch (itemId) {");

        itemToSound.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> System.out.println("        case " + entry.getKey() + ": return " + entry.getValue() + ";"));

        System.out.println("        default: return -1;");
        System.out.println("    }");
        System.out.println("}");
    }
}

