public enum Hitmark {
    MISS(0),
    HIT(1),
    POISON(2);

    /**
     * The id of the hitmark
     */
    private final int id;

    /**
     * Creates a new hitmark with an id
     *
     * @param id the id
     */
    Hitmark(int id) {
        this.id = id;
    }

    /**
     * The identification value for this hitmark
     *
     * @return the value
     */
    public int getId() {
        return id;
    }

    /**
     * Determines if this hitmark is blue, a miss.
     *
     * @return true if the hitmark signifies a miss
     */
    public boolean isMiss() {
        return equals(MISS);
    }

}
