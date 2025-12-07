public class LevelUpData {
    public enum Skill {
        ATTACK(0, 6247, 9747, 9748, 9749, "attack"),
        STRENGTH(1, 6206, 9750, 9751, 9752, "strength"),
        DEFENCE(2, 6253, 9753, 9754, 9755, "defence"),
        HITPOINTS(3, 6216, 9768, 9769, 9770, "hitpoints"),
        RANGED(4, 4443, 9756, 9757, 9758, "ranged"),
        PRAYER(5, 6242, 9759, 9760, 9761, "prayer"),
        MAGIC(6, 6211, 9762, 9763, 9764, "magic"),
        COOKING(7, 6226, 9801, 9802, 9803, "cooking"),
        WOODCUTTING(8, 4272, 9807, 9808, 9809, "woodcutting"),
        FLETCHING(9, 6231, 9783, 9784, 9785, "fletching"),
        FISHING(10, 6258, 9798, 9799, 9800, "fishing"),
        FIREMAKING(11, 4282, 9804, 9805, 9806, "firemaking"),
        CRAFTING(12, 6263, 9780, 9781, 9782, "crafting"),
        SMITHING(13, 6221, 9795, 9796, 9797, "smithing"),
        MINING(14, 4416, 9792, 9793, 9794, "mining"),
        HERBLORE(15, 6237, 9774, 9775, 9776, "herblore"),
        AGILITY(16, 4277, 9771, 9772, 9773, "agility"),
        THIEVING(17, 4261, 9777, 9778, 9779, "thieving"),
        SLAYER(18, 12122, 9786, 9787, 9788, "slayer"),
        FARMING(19, 25267, 9810, 9811, 9812, "farming"),
        RUNECRAFTING(20, 4267, 9765, 9766, 9767, "runecrafting"),
        CONSTRUCTION(21, 7267, ItemIDs.CONSTRUCT_CAPE, ItemIDs.CONSTRUCT_CAPE_T, ItemIDs.CONSTRUCT_HOOD, "construction"),
        HUNTER(22, 29267, ItemIDs.HUNTER_CAPE, ItemIDs.HUNTER_CAPE_T, ItemIDs.HUNTER_HOOD, "hunter"),
        SUMMONING(23, 9267, ItemIDs.SUMMONING_CAPE, ItemIDs.SUMMONING_CAPE_T, ItemIDs.SUMMONING_HOOD, "summoning"),
        DUNGEONEERING(24, 32267, ItemIDs.DUNGEONEERING_CAPE_2, ItemIDs.DUNGEONEERING_CAPE_T, ItemIDs.DUNGEONEERING_HOOD, "dungeoneering");

        private final int id;
        private final int frameId;
        private final int cape;
        private final int capeTrim;
        private final int hood;
        private final String displayName;

        Skill(int id, int frameId, int cape, int capeTrim, int hood, String displayName) {
            this.id = id;
            this.frameId = frameId;
            this.cape = cape;
            this.capeTrim = capeTrim;
            this.hood = hood;
            this.displayName = displayName;
        }

        public int getId() { return id; }
        public int getFrameId() { return frameId; }
        public int getCape() { return cape; }
        public int getCapeTrim() { return capeTrim; }
        public int getHood() { return hood; }
        public String getDisplayName() { return displayName; }
    }

}
