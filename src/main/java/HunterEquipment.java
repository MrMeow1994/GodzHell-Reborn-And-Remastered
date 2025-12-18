public enum HunterEquipment {

    BOX(10008, 19187, 5208, 27),
    BRID_SNARE(10006, 19175, 5207, 1);

    private int itemId, objectId, baseLevel;
    private int pickUpAnimation;

    private HunterEquipment(int itemId, int objectId,
                            int pickUpAnimation, int baseLevel) {
        this.itemId = itemId;
        this.objectId = objectId;
        this.pickUpAnimation = pickUpAnimation;
        this.baseLevel = baseLevel;
    }

    public int getId() {
        return itemId;
    }

    public int getObjectId() {
        return objectId;
    }

    public int getPickUpAnimation() {
        return pickUpAnimation;
    }

    public int getBaseLevel() {
        return baseLevel;
    }
}