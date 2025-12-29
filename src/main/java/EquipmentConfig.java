public class EquipmentConfig {
 
    public static int[] CRUSH_SWORDS = {
            ItemIDs.BRONZE_2H_SWORD,
            ItemIDs.IRON_2H_SWORD,
            ItemIDs.STEEL_2H_SWORD,
            ItemIDs.BLACK_2H_SWORD,
            ItemIDs.MITHRIL_2H_SWORD,
            ItemIDs.ADAMANT_2H_SWORD,
            ItemIDs.RUNE_2H_SWORD,
            ItemIDs.WHITE_2H_SWORD,
            ItemIDs.DRAGON_2H_SWORD,
            ItemIDs.SARADOMIN_SWORD,
            ItemIDs.SARADOMIN_GODSWORD,
            ItemIDs.BANDOS_GODSWORD,
            ItemIDs.ARMADYL_GODSWORD,
            ItemIDs.ZAMORAK_GODSWORD,
            ItemIDs.SHADOW_SWORD,
            ItemIDs.SPATULA
    };
    public static final int[] BATTLEAXES = {
            // Battleaxes
            ItemIDs.DRAGON_BATTLEAXE, ItemIDs.IRON_BATTLEAXE, ItemIDs.STEEL_BATTLEAXE, ItemIDs.BLACK_BATTLEAXE, ItemIDs.MITHRIL_BATTLEAXE,
            ItemIDs.ADAMANT_BATTLEAXE, ItemIDs.RUNE_BATTLEAXE, ItemIDs.ANGER_BATTLEAXE,
            ItemIDs.DHAROKS_GREATAXE,
    };
    public static final int[] AXES = {
            ItemIDs.BRONZE_HATCHET, ItemIDs.IRON_HATCHET, ItemIDs.STEEL_HATCHET, ItemIDs.BLACK_HATCHET, ItemIDs.MITHRIL_HATCHET, ItemIDs.ADAMANT_HATCHET, ItemIDs.RUNE_HATCHET, ItemIDs.DRAGON_HATCHET,
    };
    public static final int[] PICKAXES = {
            ItemIDs.RUNE_PICKAXE, ItemIDs.BRONZE_PICKAXE, ItemIDs.IRON_PICKAXE, ItemIDs.STEEL_PICKAXE, ItemIDs.MITHRIL_PICKAXE, ItemIDs.ADAMANT_PICKAXE, ItemIDs.RUNE_PICKAXE,
            ItemIDs.DRAGON_PICKAXE,
    };
    public static final int[] WHIP = {
            ItemIDs.ABYSSAL_WHIP, 27885, 28013, 28014, 28015, 28016, 28017, 28018, 28019,
            29523, 26017, 26018, 26019, 26020, 26021, 26022,26023, 26024, 26025, 26026, 26027,
            26028, 26029, 25624, 25618, 25612, 25344, 25000, 25001, 24555, 24562, 24119, 24108,
            24107, 24106, 24105, 24104, 24103, 24102, 24101
    };
    public static final int[] HALBERDS = {
            ItemIDs.BRONZE_HALBERD,
            ItemIDs.IRON_HALBERD,
            ItemIDs.STEEL_HALBERD,
            ItemIDs.BLACK_HALBERD,
            ItemIDs.MITHRIL_HALBERD,
            ItemIDs.ADAMANT_HALBERD,
            ItemIDs.RUNE_HALBERD,
            ItemIDs.DRAGON_HALBERD,
            28935
    };
    public static boolean isBattleaxe(int itemId) {
        for (int id : BATTLEAXES) {
            if (id == itemId) return true;
        }
        return false;
    }
    public static boolean isAxe(int itemId) {
        for (int id : AXES) {
            if (id == itemId) return true;
        }
        return false;
    }
    public static boolean isPickaxe(int itemId) {
        for (int id : PICKAXES) {
            if (id == itemId) return true;
        }
        return false;
    }
    public static boolean isWhip(int itemId) {
        for (int id : WHIP) {
            if (id == itemId) return true;
        }
        return false;
    }
    public static  boolean isTwoHander(int itemId) {
        for (int id : CRUSH_SWORDS) {
            if (id == itemId) return true;
        }
        return false;
    }
    public static  boolean isHalberd(int itemId) {
        for (int id : HALBERDS) {
            if (id == itemId) return true;
        }
        return false;
    }
}
