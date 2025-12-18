import java.util.HashMap;
import java.util.Map;

public enum HunterNPC {

    CARNIVOROUS_CHINCHOMPA(5080, 10034, 63, 265, HunterEquipment.BOX, 28558), FERRT(
            5081, 10092, 27, 115, HunterEquipment.BOX, 19189), GECKO(6916,
            12184, 27, 100, HunterEquipment.BOX, 19190), RACCOON(7272,
            12487, 27, 100, HunterEquipment.BOX, 19191), MONKEY(6942,
            12201, 27, 100, HunterEquipment.BOX, 28557), CRIMSON_SWIFT(
            5073, 10088, 1, 34, HunterEquipment.BRID_SNARE, 19180), GOLDEN_WARBLER(
            5075, 1583, 5, 48, HunterEquipment.BRID_SNARE, 19184), COPPER_LONGTAIL(
            5076, 10091, 9, 61, HunterEquipment.BRID_SNARE, 19186), CERULEAN_TWITCH(
            5074, 10089, 11, 64.67, HunterEquipment.BRID_SNARE, 19182), TROPICAL_WAGTAIL(
            5072, 10087, 19, 95.2, HunterEquipment.BRID_SNARE, 19178), WIMPY_BIRD(
            7031, 11525, 39, 167, HunterEquipment.BRID_SNARE, 28930);

    private int npcId, level, item, transformObjectId;
    private double xp;
    private HunterEquipment hunter;

    static final Map<Integer, HunterNPC> npc = new HashMap<Integer, HunterNPC>();
    static final Map<Integer, HunterNPC> object = new HashMap<Integer, HunterNPC>();

    public static HunterNPC forId(int id) {
        return npc.get(id);
    }

    static {
        for (HunterNPC npcs : HunterNPC.values())
            npc.put(npcs.npcId, npcs);
        for (HunterNPC objets : HunterNPC.values())
            object.put(objets.transformObjectId, objets);
    }

    public static HunterNPC forObjectId(int id) {
        return object.get(id);
    }

    private HunterNPC(int npcId, int item, int level, double xp,
                      HunterEquipment hunter, int transformObjectId) {
        this.npcId = npcId;
        this.item = item;
        this.level = level;
        this.xp = xp;
        this.hunter = hunter;
        this.transformObjectId = transformObjectId;
    }

    public int getLevel() {
        return level;
    }

    public int getNpcId() {
        return npcId;
    }

    public double getXp() {
        return xp;
    }

    public int getItem() {
        return item;
    }

    public HunterEquipment getEquipment() {
        return hunter;
    }

    public int getTransformObjectId() {
        return transformObjectId;
    }
}