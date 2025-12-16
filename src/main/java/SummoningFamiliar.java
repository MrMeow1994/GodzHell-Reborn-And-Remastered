public enum SummoningFamiliar {

    SPIRIT_TERRORBIRD(12008, 6794, 12441), // Tireless run
    GRANITE_CRAB(12009, 6796, 12424), // Fish rain
    PRAYING_MANTIS(12011, 6798, 12450), // Mantis strike
    GIANT_ENT(12013, 6800, 12457), // Acorn missile
    SPIRIT_COBRA(12015, 6802, 12432), // Venom shot
    SPIRIT_DAGANNOTH(12017, 6804, 12455), // Doomsphere
    THORNY_SNAIL(12019, 6806, 12449), // Crushing claw
    BEAVER(12021, 6808, 12429), // Multichop
    KARAMTHULHU_OVERLORD(12023, 6809, 12438), // Swallow whole
    HYDRA(12025, 6811, 12460), // Electric lash
    SPIRIT_JELLY(12027, 6813, 12459), // Slime spray
    BUNYIP(12029, 6813, 12430), // Cheese feast
    WAR_TORTOISE(12031, 6815, 12439), // Testudo
    FRUIT_BAT(12033, 6817, 12423), // Fruitfall
    ABYSSAL_PARASITE(12035, 6818, 12427), // Abyssal stealth
    ABYSSAL_LURKER(12037, 6820, 12454), // Abyssal drain
    UNICORN_STALLION(12039, 6822, 12434), // Healing aura
    MAGPIE(12041, 6824, 12426), // Thieving fingers
    DREADFOWL(12043, 6825, 12445), // Dreadfowl strike
    STRANGER_PLANT(12045, 6827, 12428), // Egg spawn
    SPIRIT_WOLF(12047, 6829, 12425), // Howl
    DESERT_WYRM(12049, 6831, 12436), // Oph. incubation
    EVIL_TURNIP(12051, 6833, 12448), // Evil flames
    VAMPIRE_BAT(12053, 6835, 12447), // Vampire touch
    SPIRIT_SCORPION(12055, 6837, 12456), // Spike shot
    ARCTIC_BEAR(12057, 6839, 12451), // Arctic blast
    SPIRIT_SPIDER(12059, 6841, 12467), // Poisonous blast
    BLOATED_LEECH(12061, 6843, 12444), // Blood drain
    SPIRIT_KALPHITE(12063, 6845, 12422), // Herbcall
    HONEY_BADGER(12065, 6845, 12446), // Sandstorm
    ALBINO_RAT(12067, 6847, 12452), // Toad bark
    GRANITE_LOBSTER(12069, 6849, 12424), // Fish rain
    MACAW(12071, 6851, 12437), // Magic focus

    BRONZE_MINOTAUR(12073, 6853, 12461),
    IRON_MINOTAUR(12075, 6855, 12462),
    STEEL_MINOTAUR(12077, 6857, 12463),
    MITHRIL_MINOTAUR(12079, 6859, 12464),
    ADAMANT_MINOTAUR(12081, 6861, 12465),
    RUNE_MINOTAUR(12083, 6863, 12466),

    SMOKE_DEVIL(12085, 6865, 12468), // Dust cloud
    BULL_ANT(12087, 6867, 12431), // Unburden
    WOLPERTINGER(12089, 6869, 12433), // Insane ferocity
    COMPOST_MOUND(12091, 6871, 12440), // Generate compost
    PACK_YAK(12093, 6873, 12435), // Winter storage

    SPIRIT_COCKATRICE(12095, 6875, 12458),
    SPIRIT_GUTHATRICE(12097, 6877, 12458),
    SPIRIT_SARATRICE(12099, 6879, 12458),
    SPIRIT_ZAMATRICE(12101, 6881, 12458),
    SPIRIT_PENGATRICE(12103, 6883, 12458),
    SPIRIT_CORAXATRICE(12105, 6885, 12458),
    SPIRIT_VULATRICE(12107, 6887, 12458);

    private final int pouchItemId;
    private final int npcId;
    private final int scrollItemId;

    SummoningFamiliar(int pouchItemId, int npcId, int scrollItemId) {
        this.pouchItemId = pouchItemId;
        this.npcId = npcId;
        this.scrollItemId = scrollItemId;
    }

    public int getPouchItemId() {
        return pouchItemId;
    }

    public int getNpcId() {
        return npcId;
    }

    public int getScrollItemId() {
        return scrollItemId;
    }

    public static SummoningFamiliar forPouch(int itemId) {
        for (SummoningFamiliar f : values()) {
            if (f.pouchItemId == itemId) {
                return f;
            }
        }
        return null;
    }

    public static SummoningFamiliar forScroll(int itemId) {
        for (SummoningFamiliar f : values()) {
            if (f.scrollItemId == itemId) {
                return f;
            }
        }
        return null;
    }
}
