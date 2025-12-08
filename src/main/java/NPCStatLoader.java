import com.everythingrs.lib.gson.Gson;
import com.everythingrs.lib.gson.TypeToken;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NPCStatLoader {

    // The NPCStats class (only stats, no HP, no extra fields)
    public static class NPCStats {
        public int id;
        public String name;
        public int attack;
        public int strength;
        public int defense;
        public int ranged;
        public int magic;
        public int prayer;
        public int maxHit;
        public int attackSpeed;
        public int rangedDefense;
        public int magicDefense;

        // Default constructor for JSON parsing
        public NPCStats() { }

        @Override
        public String toString() {
            return name + " (ID: " + id + ") - MaxHit: " + maxHit + ", Attack: " + attack;
        }
    }

    private static final Map<Integer, NPCStats> npcStatsMap = new HashMap<>();

    // Load JSON from a file
    public static void loadFromFile(String path) {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(path)) {
            Type listType = new TypeToken<List<NPCStats>>() {}.getType();
            List<NPCStats> npcList = gson.fromJson(reader, listType);

            npcStatsMap.clear();
            for (NPCStats stats : npcList) {
                npcStatsMap.put(stats.id, stats);
            }

            System.out.println("Loaded " + npcList.size() + " NPC stats.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /** Initialize the loader (default path or 0 index if needed) */
    public static void init() {
        // You can change the path to your JSON file
        String jsonPath = "./Data/json/npc_stats.json";
        loadFromFile(jsonPath);
    }

    // Get stats by NPC ID
    public NPCStats getStats(int npcId) {
        return npcStatsMap.get(npcId);
    }

}
