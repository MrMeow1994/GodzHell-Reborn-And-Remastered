public abstract class Entity {

    /**
     * The index in the list that the player resides
     */
    protected int index;
    public int absX, absY;// absolute x/y coordinates
    public int heightLevel;		// 0-3 supported by the client
    public int FocusPointX = -1, FocusPointY = -1;
    public boolean updateRequired;
    public int animationRequest = -1, animationWaitCycles = 0;
    protected boolean animationUpdateRequired;
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
   protected abstract void appendAnimationRequest(stream str);
    protected abstract void appendSetFocusDestination(stream str);
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

    public boolean nonWild() {
        if((absX == 3125 && absY == 9845) || (absX == 3125 && absY == 9844) || (absX == 3122 && absY == 9836) || (absX == 3122 && absY == 9835) || (absX == 3119 && absY == 9831)) {
            return false;
        }
        if(Boundary.isIn( this, Boundary.rdleveloftrain)){
            return true;
        }
        if(Boundary.isIn( this, Boundary.SUMMONING)){
            return true;
        }
        if(Boundary.isIn( this, Boundary.reousce_dung_one)){
            return true;
        }
        if(Boundary.isIn( this, Boundary.VARROCK_BOUNDARY)){
            return true;
        }
        if(Boundary.isIn( this, Boundary.ghr_train)){
            return true;
        }
        if(Boundary.isIn( this, Boundary.prestige)){
            return true;
        }
        if(Boundary.isIn( this, Boundary.ARDOUGNE_BOUNDARY)){
            return true;
        }
        if(Boundary.isIn( this, Boundary.ARDOUGNE_ZOO_BRIDGE_BOUNDARY)){
            return true;
        }
        if(Boundary.isIn( this, Boundary.FALADOR_BOUNDARY)){
            return true;
        }
        if(Boundary.isIn( this, Boundary.CRAFTING_GUILD_BOUNDARY)){
            return true;
        }
        if(Boundary.isIn( this, Boundary.TAVERLY_BOUNDARY)){
            return true;
        }
        if(Boundary.isIn( this, Boundary.dog_ofwar)){
            return true;
        }
        if(Boundary.isIn( this, Boundary.TZHAAR_CITY_BOUNDARY)){
            return true;
        }
        if(Boundary.isIn( this, Boundary.LUMRIDGE_BOUNDARY)){
            return true;
        }
        if(Boundary.isIn( this, Boundary.DRAYNOR_DUNGEON_BOUNDARY)){
            return true;
        }
        if(Boundary.isIn( this, Boundary.AL_KHARID_BOUNDARY)){
            return true;
        }
        if(Boundary.isIn( this, Boundary.DRAYNOR_MANOR_BOUNDARY)){
            return true;
        }
        if(Boundary.isIn( this, Boundary.DRAYNOR_BOUNDARY)){
            return true;
        }
        if(Boundary.isIn( this, Boundary.KARAMJA_BOUNDARY)){
            return true;
        }
        if(Boundary.isIn( this, Boundary.BRIMHAVEN_BOUNDARY)){
            return true;
        }
        if(Boundary.isIn( this, Boundary.BRIMHAVEN_DUNGEON_BOUNDARY)){
            return true;
        }
        if(Boundary.isIn( this, Boundary.CATHERBY_BOUNDARY)){
            return true;
        }
        if(Boundary.isIn( this, Boundary.CANIFIS_BOUNDARY)){
            return true;
        }
        if(Boundary.isIn( this, Boundary.SEERS_BOUNDARY)){
            return true;
        }
        if(Boundary.isIn( this, Boundary.RELLEKKA_BOUNDARY)){
            return true;
        }

        if(Boundary.isIn( this, Boundary.SKILLZ)){
            return true;
        }
        if(Boundary.isIn( this, Boundary.DUNGEONS)){
            return true;
        }
        if(Boundary.isIn( this, Boundary.UMBYSWAPES)){
            return true;
        }
        if(Boundary.isIn( this, Boundary.GODWARS_BOSSROOMS)){
            return true;
        }
        if(Boundary.isIn( this, Boundary.GH_NONWILD)){
            return true;
        }
        if(Boundary.isIn( this, Boundary.TZHAAR_CITY_BOUNDARY)){
            return true;
        }
        if(Boundary.isIn( this, Boundary.SLAYER_TOWER_BOUNDARY)){
            return true;
        }
        if(Boundary.isIn( this, Boundary.LUNAR_ISLE_BOUNDARY)){
            return true;
        }
        if(Boundary.isIn( this, Boundary.FREMENNIK_ISLES_BOUNDARY)){
            return true;
        }
        if(Boundary.isIn( this, Boundary.WATERBIRTH_ISLAND_BOUNDARY)){
            return true;
        }
        if(Boundary.isIn( this, Boundary.MISCELLANIA_BOUNDARY)){
            return true;
        }
        if(Boundary.isIn( this, Boundary.APE_ATOLL_BOUNDARY)){
            return true;
        }
        if(Boundary.isIn( this, Boundary.FELDIP_HILLS_BOUNDARY)){
            return true;
        }
        if(Boundary.isIn( this, Boundary.YANILLE_BOUNDARY)){
            return true;
        }
        if(Boundary.isIn( this, Boundary.DESERT_BOUNDARY)){
            return true;
        }
        if(Boundary.isIn( this, Boundary.LLETYA_BOUNDARY)){
            return true;
        }
        if(Boundary.isIn( this, Boundary.GNOME_STRONGHOLD_BOUNDARY)){
            return true;
        }
        if(Boundary.isIn( this, Boundary.HANG)){
            return true;
        }
        if(Boundary.isIn( this, Boundary.Theive)){
            return true;
        }
        if (Boundary.isIn(this, Boundary.ANCHENT)) {
            return true;
        }
        if(inSafePvP()){
            return false;
        }
        return false;
    }

    public boolean inMulti() {
        if (Boundary.isIn(this, Boundary.BANDIT_CAMP_BOUNDARY) ||
                Boundary.isIn(this, Boundary.TzHarr_City)) {
            return true;
        }
        if (Boundary.isIn(this, Boundary.Train)) {
            return true;
        }
        if (Boundary.isIn(this, Boundary.ANCHENT)) {
            return true;
        }
        if (Boundary.isIn(this, Boundary.CORP)) {
            return true;
        }
        if((absX >= 3136 && absX <= 3327 && absY >= 3519 && absY <= 3607) ||
                (absX >= 3190 && absX <= 3327 && absY >= 2568 && absY <= 3839) ||
                (absX >= 3200 && absX <= 3390 && absY >= 3840 && absY <= 3967) ||
                (absX >= 2992 && absX <= 3007 && absY >= 3912 && absY <= 3967) ||
                (absX >= 2946 && absX <= 2959 && absY >= 3816 && absY <= 3831) ||
                (absX >= 3008 && absX <= 3199 && absY >= 3856 && absY <= 3903) ||
                (absX >= 3008 && absX <= 3071 && absY >= 3600 && absY <= 3711) ||
                (absX >= 2624 && absX <= 2690 && absY >= 2550 && absY <= 2619) ||
                (absX >= 2371 && absX <= 2422 && absY >= 5062 && absY <= 5117) ||
                (absX >= 2896 && absX <= 2927 && absY >= 3595 && absY <= 3630) ||
                (absX >= 2892 && absX <= 2932 && absY >= 4435 && absY <= 4464) ||
                (absX >= 2256 && absX <= 2287 && absY >= 4680 && absY <= 4711)) {
            return true;
        }
        return false;
    }





    public boolean inSafePvP() {
        return absX >= 1889 && absX <= 1910 && absY >= 5345 && absY <= 5366 && heightLevel == 2;
    }

}
