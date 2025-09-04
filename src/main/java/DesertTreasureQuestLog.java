public class DesertTreasureQuestLog {

    private static final int YEW_LOG = 1515;
    private static final int RAW_MANTA = 391;
    private static final int TINDERBOX = 590;
    private static final int SOUL_RUNE = 566;
    private static final int COIF = 1169;
    private static final int BRONZE_PLATEBODY = 1103;
    private static final int ICE_DIAMOND = 4671;
    private static final int BLOOD_DIAMOND = 4670;
    private static final int SHADOW_DIAMOND = 4673;
    private static final int SMOKE_DIAMOND = 4672;

    public static void showInformation(client client) {
        for (int i = 8144; i < 8195; i++) {
            client.getPA().sendFrame126("", i);
        }
        client.getPA().sendFrame126("@dre@Desert Treasure", 8144);
        client.getPA().sendFrame126("", 8145);

        if (client.desertTreasure == 0) {
            client.getPA().sendFrame126("Desert Treasure", 8144);
            client.getPA().sendFrame126("I can start this quest by speaking to Dimintheis.", 8147);
            client.getPA().sendFrame126("He's located at home.", 8148);
            client.getPA().sendFrame126("", 8149);
            client.getPA().sendFrame126("Requirements: 82 Magic, 56 Herblore, 200M coins.", 8150);

        } else if (client.desertTreasure == 1) {
            client.getPA().sendFrame126("Desert Treasure", 8144);
            client.getPA().sendFrame126("@str@I spoke with Dimintheis.", 8147);
            client.getPA().sendFrame126("He told me to gather the following items:", 8148);
            checkItem(client, YEW_LOG, "1 yew log", 8149);
            checkItem(client, RAW_MANTA, "1 raw manta ray", 8150);
            checkItem(client, TINDERBOX, "1 tinderbox", 8151);
            checkItem(client, SOUL_RUNE, "1 soul rune", 8152);
            checkItem(client, COIF, "1 coif", 8153);
            checkItem(client, BRONZE_PLATEBODY, "1 bronze platebody", 8154);

        } else if (client.desertTreasure == 2) {
            client.getPA().sendFrame126("Desert Treasure", 8144);
            client.getPA().sendFrame126("@str@I gave Dimintheis the items.", 8147);
            client.getPA().sendFrame126("He told me to speak to Rasolo to gain access to the shadow realm.", 8148);
            client.getPA().sendFrame126("I must deliver the gathered items to him.", 8149);

        } else if (client.desertTreasure == 3) {
            client.getPA().sendFrame126("Desert Treasure", 8144);
            client.getPA().sendFrame126("@str@I gave the items to Rasolo.", 8147);
            client.getPA().sendFrame126("He teleported me to the Archaeological Expert.", 8148);
            client.getPA().sendFrame126("Now I must retrieve the Ice Diamond.", 8149);
            if (client.playerHasItem(ICE_DIAMOND, 1)) {
                client.getPA().sendFrame126("@str@I obtained the Ice Diamond.", 8150);
            } else {
                client.getPA().sendFrame126("@red@I still need to obtain the Ice Diamond.", 8150);
            }

        } else if (client.desertTreasure == 4) {
            client.getPA().sendFrame126("Desert Treasure", 8144);
            client.getPA().sendFrame126("@str@I brought the Ice Diamond to the Expert.", 8147);
            client.getPA().sendFrame126("He teleported me to the swamp to fight the ghasts.", 8148);
            client.getPA().sendFrame126("Next, I must find Malak for the Blood Diamond.", 8149);

        } else if (client.desertTreasure == 5) {
            client.getPA().sendFrame126("Desert Treasure", 8144);
            client.getPA().sendFrame126("@str@I encountered Malak, a vampiric being.", 8147);
            client.getPA().sendFrame126("He seeks the Blood Diamond in exchange for help.", 8148);
            if (client.playerHasItem(BLOOD_DIAMOND, 1)) {
                client.getPA().sendFrame126("@str@I obtained the Blood Diamond.", 8149);
            } else {
                client.getPA().sendFrame126("@red@I still need to obtain the Blood Diamond.", 8149);
            }

        } else if (client.desertTreasure == 6) {
            client.getPA().sendFrame126("Desert Treasure", 8144);
            client.getPA().sendFrame126("@str@I gave the Blood Diamond to Malak.", 8147);
            client.getPA().sendFrame126("He told me to find Kolodion for the Smoke Diamond.", 8148);
            if (client.playerHasItem(SHADOW_DIAMOND, 1)) {
                client.getPA().sendFrame126("@str@I have the Shadow Diamond.", 8149);
            } else {
                client.getPA().sendFrame126("@red@I still need to obtain the Shadow Diamond.", 8149);
            }

        } else if (client.desertTreasure == 7) {
            client.getPA().sendFrame126("Desert Treasure", 8144);
            client.getPA().sendFrame126("@str@I gave the Shadow Diamond to Kolodion.", 8147);
            client.getPA().sendFrame126("He teleported me to the Smoke Dungeon.", 8148);
            if (client.playerHasItem(SMOKE_DIAMOND, 1)) {
                client.getPA().sendFrame126("@str@I have the Smoke Diamond.", 8149);
            } else {
                client.getPA().sendFrame126("@red@I still need to obtain the Smoke Diamond.", 8149);
            }

        } else if (client.desertTreasure == 8) {
            client.getPA().sendFrame126("Desert Treasure", 8144);
            client.getPA().sendFrame126("@str@I gave the Smoke Diamond to Elizabeth.", 8147);
            client.getPA().sendFrame126("The final ritual was completed.", 8148);
            client.getPA().sendFrame126("@red@     QUEST COMPLETE", 8149);
            client.getPA().sendFrame126("I unlocked Ancient Magicks as a reward.", 8150);
        }

        client.getPA().showInterface(8134);
    }

    private static void checkItem(client client, int itemId, String label, int frameId) {
        if (client.playerHasItem(itemId, 1)) {
            client.getPA().sendFrame126("@str@" + label, frameId);
        } else {
            client.getPA().sendFrame126("@red@" + label, frameId);
        }
    }
}
