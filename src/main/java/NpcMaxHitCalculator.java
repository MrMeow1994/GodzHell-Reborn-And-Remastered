import java.util.HashMap;
import java.util.Map;

public class NpcMaxHitCalculator {

    // Manual overrides for bosses or edge cases
    private static final Map<Integer, Integer> overrides = new HashMap<>();

    static {
        overrides.put(50, 25);     // King Black Dragon
        overrides.put(2745, 97);   // TzTok-Jad


    }

    public static int calculate(int npcId, int hp) {
        // Return override if exists
        if (overrides.containsKey(npcId)) {
            return overrides.get(npcId);
        }

        // Base formula: scaled square root with light HP factor
        double base = Math.sqrt(hp) * 0.55;
        double linear = hp / 300.0;

        int hit = (int) Math.floor(base + linear);

        return Math.max(1, Math.min(hit, 90)); // 1 min, 90 cap
    }
}
