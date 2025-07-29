public class QuestRewards {

    public static String QUEST_NAME;

    public static void questReward(client player, String questName, String Line1, String Line2, String Line3, String Line4, String Line5, String Line6, int itemID) {
        player.getPA().sendFrame126("You have completed " + questName + "!", 12144);
        player.getPA().sendFrame126("" + player.questPoints, 12147);
        player.getPA().sendFrame126(Line1, 12150);
        player.getPA().sendFrame126(Line2, 12151);
        player.getPA().sendFrame126(Line3, 12152);
        player.getPA().sendFrame126(Line4, 12153);
        player.getPA().sendFrame126(Line5, 12154);
        player.getPA().sendFrame126(Line6, 12155);
        if (itemID > 0) {
            player.getPA().sendFrame246(12145, 250, itemID);
        }
        player.getPA().showInterface(12140);
        player.sendMessage("You completed " + questName + "!");
        QuestAssistant.sendStages(player);
        player.getPA().sendQuickSong(93, 0);
    }

    public static void knightsReward(client player) {
        questReward(player, "Knight's Sword Quest", "1 Quest Point", "12,725 Smithing XP", "", "", "", "", 0);
        QUEST_NAME = "The Knight's Sword";
        player.getPA().sendFrame126("@gre@" + QUEST_NAME + "", 7346);
        player.addSkillXP(12725, player.playerSmithing);
        player.questPoints ++;
        player.knightS = 9;
    }

    public static void gertFinish(client player) {
        questReward(player, "Gertrude's Cat", "1 Quest Point", "1,525 Cooking XP", "A kitten!", "Ability to raise cats", "A chocolate cake", "A bowl of stew", 1897);
        QUEST_NAME = "Gertrude's Cat";
        player.getPA().sendFrame126("@gre@" + QUEST_NAME + "", 7360);
        player.addItemorDrop(1897, 1);
        player.addItemorDrop(2003, 1);
        player.addItemorDrop(1560, 1);
        player.addSkillXP(1525, player.playerCooking);
        player.questPoints++;
        player.gertCat = 7;
    }

    public static void pirateFinish(client c) {
        questReward(c, "Pirate's Treasure", "2 Quest Points", "One-Eyed Hector's Treasure", "", "", "", "", 2714);
        QUEST_NAME = "Pirate's Treasure";
        c.getPA().sendFrame126("@gre@" + QUEST_NAME + "", 7341);
        c.addItemorDrop(2714, 1);
        c.questPoints += 2;
        c.pirateTreasure = 6;
    }

    public static void witchFinish(client client) {
        questReward(client, "Witch's Potion", "1 Quest Point", "325 Magic XP", "", "", "", "", 325);
        QUEST_NAME = "Witch's Potion";
        client.getPA().sendFrame126("@gre@" + QUEST_NAME + "", 7348);
        client.addSkillXP(325, client.playerMagic);
        client.questPoints++;
        client.witchspot = 3;
    }

    public static void julietFinish(client player) {
        questReward(player, "Romeo and Juliet", "5 Quest Points", "", "", "", "", "", 0);
        QUEST_NAME = "Romeo and Juliet";
        player.getPA().sendFrame126("@gre@" + QUEST_NAME + "", 7343);
        player.questPoints += 5;
        player.romeojuliet = 9;
    }

    public static void restFinish(client client) {
        questReward(client, "Restless Ghost", "1 Quest Point", "125 Prayer XP", "", "", "", "", 0);
        QUEST_NAME = "Restless Ghost";
        client.getPA().sendFrame126("@gre@" + QUEST_NAME + "", 7337);
        client.addSkillXP(125, client.playerPrayer);
        client.questPoints++;
        client.restGhost = 5;
    }

    public static void vampFinish(client player) {
        questReward(player, "Vampyre Slayer", "3 Quest Points", "4,825 Attack XP", "", "", "", "", 0);
        QUEST_NAME = "Vampyre Slayer";
        player.getPA().sendFrame126("@gre@" + QUEST_NAME + "", 7347);
        player.addSkillXP(4825, player.playerAttack);
        player.questPoints += 3;
        player.vampSlayer = 5;
    }

    public static void runeFinish(client player) {
        questReward(player, "Rune Mysteries", "1 Quest Point", "Air Talisman", "", "", "", "", 1438);
        QUEST_NAME = "Rune Mysteries";
        player.getPA().sendFrame126("@gre@" + QUEST_NAME + "", 7335);
        player.addItemorDrop(1438, 1);
        player.questPoints++;
        player.runeMist = 4;
    }

    public static void sheepFinish(client player) {
        questReward(player, "Sheep Shearer", "1 Quest Point", "150 Crafting Exp", "60 Coins", "", "", "", 995);
        QUEST_NAME = "Sheep Shearer";
        player.getPA().sendFrame126("@gre@" + QUEST_NAME + "", 7344);
        player.addItemorDrop(995, 60);
        player.addSkillXP(150, player.playerCrafting);
        player.questPoints++;
        player.sheepShear = 2;
    }

    public static void doricFinish(client player) {
        questReward(player, "Doric's Quest", "1 Quest Point", "1,300 Mining XP", "180 Coins", "", "", "", 995);
        QUEST_NAME = "Doric's Quest";
        player.getPA().sendFrame126("@gre@" + QUEST_NAME + "", 7336);
        player.addItemorDrop(995, 180);
        player.addSkillXP(1300, player.playerMining);
        player.questPoints++;
        player.doricQuest = 3;
    }

    public static void impFinish(client player) {
        questReward(player, "Imp Catcher", "1 Quest Point", "875 Magic XP", "Amulet of Accuracy", "", "", "", 1478);
        QUEST_NAME = "Imp Catcher";
        player.getPA().sendFrame126("@gre@" + QUEST_NAME + "", 7340);
        player.addItemorDrop(1478, 1);
        player.addSkillXP(875, player.playerMagic);
        player.questPoints++;
        player.impsC = 2;
    }

    public static void cookReward(client player) {
        questReward(player, "Cook's Assistant", "1 Quest Point", "500 Coins", "300 Cooking XP", "", "", "", 326);
        QUEST_NAME = "Cook's Assistant";
        player.getPA().sendFrame126("@gre@" + QUEST_NAME + "", 7333);
        player.addItemorDrop(995, 500);
        player.addSkillXP(300, player.playerCooking);
        player.questPoints++;
        player.cookAss = 3;
    }

    public static void blackKnightReward(client player) {
        questReward(player, "Black Knights' Fortress", "3 Quest Points", "2,500 Coins", "", "", "", "", 0);
        QUEST_NAME = "Black Knights' Fortress";
        player.getPA().sendFrame126("@gre@" + QUEST_NAME + "", 7332);
        player.addItemorDrop(995, 2500);
        player.questPoints += 3;
        player.blackKnight = 3;
    }

    public static void shieldArravReward(client player) {
        questReward(player, "Shield of Arrav", "1 Quest Point", "1,200 Coins", "", "", "", "", 767);
        QUEST_NAME = "Shield of Arrav";
        player.getPA().sendFrame126("@gre@" + QUEST_NAME + "", 7345);
        player.addItemorDrop(995, 1200);
        player.questPoints++;
        player.shieldArrav = 8;
    }

    public static void lostCityReward(client player) {
        questReward(player, "Lost City", "3 Quest Points", "Access to Zanaris", "", "", "", "", 0);
        QUEST_NAME = "Lost City";
        player.getPA().sendFrame126("@gre@" + QUEST_NAME + "", 7367);
        player.questPoints += 3;
        player.lostCity = 3;
    }
}