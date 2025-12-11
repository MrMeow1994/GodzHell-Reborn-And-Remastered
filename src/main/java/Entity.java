public abstract class Entity {

    /**
     * The index in the list that the player resides
     */
    protected int index;
    /**
     * Sends some information to the Stream regarding a possible new hit on the
     * entity.
     *
     * @param str
     *            the stream for the entity
     */
    protected abstract void appendHitUpdate(stream str);

    /**
     * Sends some information to the Stream regarding a possible new hit on the
     * entity.
     *
     * @param str
     *            the stream for the entity
     */
    protected abstract void appendHitUpdate2(stream str);

    /**
     * The index value where the {@link Entity} resides along with other common
     * counterparts.
     *
     * @return the index of the array where this object resides
     */
    public int getIndex() {
        return index;
    }

    /**
     * Set the index.
     * @param index the index of the array where this object resides
     */
    public void setIndex(int index) {
        this.index = index;
    }

}
