import java.util.Optional;

public class MiningEvent extends Event<client> {
    /**
     * The amount of cycles that must pass before the animation is updated
     */
    private final int ANIMATION_CYCLE_DELAY = 3;

    /**
     * The value in cycles of the last animation
     */
    private int lastAnimation;


    /**
     * The pickaxe being used to mine
     */
    private final Pickaxe pickaxe;

    /**
     * The mineral being mined
     */
    private final Mineral mineral;

    /**
     * The object that we are mning
     */
    private int objectId;

    /**
     * The location of the object we're mining
     */
    private Location3D location;

    /**
     * The npc the player is mining, if any
     */
    private NPC npc;

    public static int gems[] = {ItemIDs.UNCUT_DIAMOND, ItemIDs.UNCUT_EMERALD, ItemIDs.UNCUT_RUBY, ItemIDs.UNCUT_SAPPHIRE};

    public static int randomGems() {
        return gems[(int) (Math.random() * gems.length)];
    }
    /**
     * Constructs a new {@link MiningEvent} for a single player
     * @param player	the player this is created for
     * @param objectId	the id value of the object being mined from
     * @param location	the location of the object being mined from
     * @param mineral	the mineral being mined
     * @param pickaxe	the pickaxe being used to mine
     */
    public MiningEvent(client player, int objectId, Location3D location, Mineral mineral, Pickaxe pickaxe, int time) {
        super("skilling", player, time);
        this.plr = player;
        this.objectId = objectId;
        this.location = location;
        this.mineral = mineral;
        this.pickaxe = pickaxe;
    }

    /**
     * Constructs a new {@link MiningEvent} for a single player
     * @param player	the player this is created for
     * @param npc		the npc being from from
     * @param location	the location of the npc
     * @param mineral	the mineral being mined
     * @param pickaxe	the pickaxe being used to mine
     */
    public MiningEvent(client player, NPC npc, Location3D location, Mineral mineral, Pickaxe pickaxe, int time) {
        super("skilling", player, time);
        this.plr = player;
        this.npc = npc;
        this.location = location;
        this.mineral = mineral;
        this.pickaxe = pickaxe;
    }

    /**
     * Called when the event is executed.
     *
     * @param  The event container, so the event can dynamically change the
     *                  tick time etc.
     */
    @Override
    public void update() {
        if (plr == null || plr.disconnected || plr.IsDead) {
            stop();
            return;
        }
        if (!plr.playerHasItem(pickaxe.getItemId())
                && !plr.isWearingItem(pickaxe.getItemId())) {
            plr.sendMessage("That is strange! The pickaxe could not be found.");
            stop();
            return;
        }
        if (plr.freeSlots() == 0) {
            plr.sendMessage("You have no more free slots.");
            stop();
            return;
        }
		/*if (Misc.random(100) == 0 && plr.getInterfaceEvent().isExecutable()) {
			plr.getInterfaceEvent().execute();
			stop();
			return;
		}*/
        if (objectId > 0) {
            if (server.getGlobalObjects().exists(mineral.getDepleteObject(), location.getX(), location.getY(), location.getZ())) {
                plr.sendMessage("This vein contains no more minerals.");
                plr.resetAnimation();
                stop();
                return;
            }
        } else {
            if (npc == null || npc.IsDead) {
                plr.sendMessage("This vein contains no more minerals.");
                plr.resetAnimation();
                stop();
                return;
            }
        }
        int ANIMATION_CYCLE_DELAY = 3;
        if (super.getElapsedTicks() - lastAnimation > ANIMATION_CYCLE_DELAY) {
            plr.startAnimation(pickaxe.getAnimation());
            lastAnimation = super.getElapsedTicks();
        }
    }
    @Override
    public void execute() {
        if (plr == null || plr.disconnected || plr.IsDead) {
            stop();
            return;
        }
        if (misc.random(35) == 0) {
            if (mineral.getBobleObject() != -1) {
                server.getGlobalObjects().add(new GlobalObject(mineral.getBobleObject(), location.getX(), location.getY(),
                        location.getZ(), 0, 10, mineral.getRespawnRate(), objectId));
                stop();
            }
        }
        if (mineral.isDepletable()) {
            int face = 0;
            Optional<WorldObject2> worldObject = Region.getWorldObject(objectId, location.getX(), location.getY(), 0);
            if (worldObject.isPresent()) {
                face = worldObject.get().getFace();
            }
            if (objectId > 0) {
                server.getGlobalObjects().add(new GlobalObject(mineral.getDepleteObject(), location.getX(), location.getY(),
                        location.getZ(), face, 10, mineral.getRespawnRate(), objectId));
            } else {
                npc.IsDead = true;
                npc.actionTimer = 0;
                npc.NeedRespawn = false;
            }
        }
        int chance20 = misc.random(3);
        // plr.face(location.getX(), location.getY());
        if (mineral.equals(Mineral.ESSENCE)) {
            if (plr.playerLevel[plr.playerMining] >= 30) {
                plr.addItem(7936, 1);
            } else {
                plr.addItem(1436, 1);
            }
        }
        if (misc.random(50) == 0) {
            int randomgem = randomGems();
            plr.addItem(randomgem, 1);
            plr.sM("You find a " + Item.getItemName(randomgem) + ".");
        }
        plr.addItem(mineral.getMineralReturn().generate(), 1);
        int chance = misc.random(300);
        //plr.sendMessage("Your chance to get 100 platinum tokens from skilling was " + chance + " you needed 0.");
		/*if (Misc.random(30) == 0) {
			plr.getPA().rewardPoints(3, "Congrats, You randomly got 3 PK Points from mining!");
		}*/
        //Achievements.increase(plr, AchievementType.MINING, 1);
        if (plr.playerEquipment[plr.playerHat] == 12013 && plr.playerEquipment[plr.playerChest] == 12014 && plr.playerEquipment[plr.playerLegs] == 12015 && plr.playerEquipment[plr.playerFeet] == 12016) {
            plr.addSkillXP(Config.MINING_EXPERIENCE * mineral.getExperience() * 1.2, Skill.MINING.id);
        } else {
            plr.addSkillXP(Config.MINING_EXPERIENCE * mineral.getExperience(), Skill.MINING.id);
        }
    }

    @Override
    public void stop() {
        super.stop();
        if (plr == null) {
            return;
        }
        plr.resetAnimation();
    }
}
