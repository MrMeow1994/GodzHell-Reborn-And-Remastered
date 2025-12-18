public class Hunter {

    /**
     * Hunter's equipment.
     */
    private HunterEquipment hunt;
    /**
     * Gets the amount of traps a player can set.
     *
     * @param player
     *            The player.
     * @return Tramp amount.
     */
    public int getTrapAmount(client player) {
        int level = 20;
        int trapAmount = 2;
        for (int i = 0; i < 2; i++) {
            if (player.playerLevel[player.playerHunter] >= level) {
                trapAmount++;
                level += 20;
            }
        }
        return trapAmount;
    }

    /**
     * Constructs a new {@code Fishing} {@code Object}.
     *
     * @param hunt
     *            The hunter's equipment.
     */
    public Hunter(HunterEquipment hunt) {
        this.hunt = hunt;
    }
}
