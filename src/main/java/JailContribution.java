public enum JailContribution {

    COAL(453, 1),
    COPPER_ORE(ItemIDs.COPPER_ORE, 1),
    TIN_ORE(ItemIDs.TIN_ORE, 1),
    CLAY(ItemIDs.CLAY, 1),
    IRON_ORE(440, 1),
    GOLD_ORE(444, 1),
    MITHRIL_ORE(ItemIDs.MITHRIL_ORE, 1),
    ADAMANTITE_ORE(ItemIDs.ADAMANTITE_ORE, 1);

    private final int itemId;
    private final int pointsPerItem;

    JailContribution(int itemId, int pointsPerItem) {
        this.itemId = itemId;
        this.pointsPerItem = pointsPerItem;
    }

    public int getItemId() {
        return itemId;
    }

    public int getPointsPerItem() {
        return pointsPerItem;
    }

    public static JailContribution forItem(int itemId) {
        for (JailContribution jc : values()) {
            if (jc.itemId == itemId) {
                return jc;
            }
        }
        return null;
    }

    public static boolean isAllowed(int itemId) {
        return forItem(itemId) != null;
    }
}
