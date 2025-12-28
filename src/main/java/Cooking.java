import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;

public class Cooking extends SkillHandler {

    private static final Logger log = LoggerFactory.getLogger(Cooking.class);
    private static SecureRandom cookingRandom = new SecureRandom(); // The random factor

    private static enum CookingItems {
        //raw, cooked, burnt, levelreq, exp, stopburn, stopburn w/gloves, name
        BREAD(ItemIDs.BREAD_DOUGH, ItemIDs.BREAD, ItemIDs.BURNT_BREAD, 1, 40, 38, 30, "bread"),
        SHRIMP(317, 315, 7954, 1, 30, 34, 30, "shrimp"),
        SARDINE(327, 325, 369, 1, 40, 38, 38, "sardine"),
        HERRING(345, 347, 357, 5, 50, 41, 41, "herring"),
        Scrambled_egg(7076, 7078, 7080, 13, 50, 41,41, "scrambled egg"),
        Fried_onions(1871, 7084, 7092, 42, 60, 77,77, "fried onions"),
        TROUT(335, 333, 343, 15, 70, 50, 50, "trout"),
        TUNA(359, 361, 367, 30, 100, 64, 63, "tuna"),
        ANCHOVIES(321, 319, 323, 5, 45, 34, 34, "anchovies"),
        RAW_BEEF(2132, 2142, 2146, 1, 30, 33, 33, "raw beef"),
        RAW_RAT(2134, 2142, 2146, 1, 30, 33, 33, "raw rat meat"),
        BURNT_MEAT(2142, 2146, 2146, 1, 1, 100, 100, "cooked meat"),
        RAW_CHICKEN(2138, 2140, 2144, 1, 30, 33, 33, "raw chicken"),
        RAW_RABBIT(ItemIDs.RAW_RABBIT, ItemIDs.COOKED_RABBIT, ItemIDs.BURNT_RABBIT, 1, 30, 33, 33, "raw rabbit"),
        RAW_BEAR_MEAT(2136, 2142, 2146, 1, 30, 33, 33, "raw bear meat"),
        //Botanical_pie(19656, 19662, 2329, 52, 180, 60, 60, "Uncooked botanical pie"),
        MACKERAL(353, 355, 357, 10, 60, 45, 45, "mackeral"),
        SALMON(331, 329, 343, 25, 90, 58, 55, "salmon"),
        UNCOOKED_BERRY_PIE(2321, 2325, 2329, 10, 78, 50, 50, "uncooked pie"),
        UNCOOKED_WILD_PIE(7206, 7208, 2329, 85, 240, 99, 99, "uncooked wild pie"),
        PIKE(349, 351, 343, 20, 80, 59, 59, "pike"),
        KARAMBWAN(3142, 3144, 3146, 1, 80, 20, 20, "karambwan"),
        LOBSTER(377, 379, 381, 40, 120, 74, 68, "lobster"),
        RAW_BASS(363, 365, 367, 43, 130, 80, 75, "raw bass"),
        SWORDFISH(371, 373, 375, 50, 140, 86, 81, "swordfish"),
        MONKFISH(7944, 7946, 7948, 62, 150, 92, 90,	"monkfish"),
        SHARK(383, 385, 387, 76, 210, 100, 94, "shark"),
        MANTA_RAY(389, 391, 393, 91, 216, 100, 100, "manta ray"),
        SEAWEED(401, 1781, 1781, 1, 1, 1, 1, "sea weed"),
        CURRY(2009, 2011, 2013, 60, 280, 74, 74, "curry"),
       // ANGLERFISH(13439, 13441, 13443, 84, 230, 98, 98, "angler fish"),
       // DARK_CRAB(11934, 11936, 11938, 90, 215, 100, 100, "dark crab"),
        SEA_TURTLE(ItemIDs.RAW_SEA_TURTLE, ItemIDs.SEA_TURTLE, ItemIDs.BURNT_SEA_TURTLE, 82, 211, 150, 150, "sea turtle"),
       // SPECIAL_FISH(20857, 20858, 20854, 30, 150, 52, 65, "Special fish"),
        RAINBOW_fISH(10138,10136,10140,1,30,100,100,"Rainbow Fish"),
        CAKE(ItemIDs.UNCOOKED_CAKE, ItemIDs.CAKE, ItemIDs.BURNT_CAKE, 40, 180, 74, 74, "Cake");

        int rawItem, cookedItem, burntItem, levelReq, xp, stopBurn, stopBurnGloves;
        String name;

        private CookingItems(int rawItem, int cookedItem, int burntItem, int levelReq, int xp, int stopBurn, int stopBurnGloves, String name) {
            this.rawItem = rawItem;
            this.cookedItem = cookedItem;
            this.burntItem = burntItem;
            this.levelReq = levelReq;
            this.xp = xp;
            this.stopBurn = stopBurn;
            this.name = name;
        }

        private int getRawItem() {
            return rawItem;
        }

        private int getCookedItem() {
            return cookedItem;
        }

        private int getBurntItem() {
            return burntItem;
        }

        private int getLevelReq() {
            return levelReq;
        }

        private int getXp() {
            return xp;
        }

        private int getStopBurn() {
            return stopBurn;
        }

        private int getStopBurnGloves() {
            return stopBurnGloves;
        }

        private String getName() {
            return name;
        }
    }

    public static CookingItems forId(int itemId) {
        for (CookingItems item : CookingItems.values()) {
            if (itemId == item.getRawItem()) {
                return item;
            }
        }
        return null;
    }



    public static void setCooking(client player, boolean isCooking) {
        player.playerIsCooking = isCooking;
        player.stopPlayerSkill = isCooking;
    }

    private static void viewCookInterface(client c, int item) {
        c.getPA().sendFrame164(1743);
        c.getPA().sendFrame246(13716, view190 ? 190 : 170, item);
        c.getPA().sendFrame126(getLine(c) + "" + Item.getItemName(item) + "", 13717);
    }

    public static boolean startCooking(client c, int itemId, int objectId) {
        CookingItems item = forId(itemId);
        if (item != null) {
            if (c.playerLevel[c.playerCooking] < item.getLevelReq()) {
                c.getPA().RemoveAllWindows();
                c.sendStatement("You need a Cooking level of " + item.getLevelReq() + " to cook this.");
                c.NpcDialogue = -1;
                c.NpcDialogueSend = false;
                return false;
            }
            if (c.playerIsCooking) {
                c.getPA().RemoveAllWindows();
                return false;
            }
            if (!COOKING) {
                c.sendMessage("This skill is currently disabled.");
                return false;
            }
            // save the id of the item and object for the cooking interface.
            c.cookingItem = itemId;
            c.cookingObject = objectId;
            viewCookInterface(c, item.getRawItem());
            return true;
        }
        return false;
    }

    private static boolean getSuccess(client c, int burnBonus, int levelReq, int stopBurn) {
        if (c.playerLevel[c.playerCooking] >= stopBurn) {
            return true;
        }
        double burn_chance = 55.0 - burnBonus;
        double cook_level = c.playerLevel[c.playerCooking];
        double lev_needed = levelReq;
        double burn_stop = stopBurn;
        double multi_a = burn_stop - lev_needed;
        double burn_dec = burn_chance / multi_a;
        double multi_b = cook_level - lev_needed;
        burn_chance -= multi_b * burn_dec;
        double randNum = cookingRandom.nextDouble() * 100.0;
        return burn_chance <= randNum;
    }

    public static void cookItem(final client player, final int itemId, final int amount, final int objectId) {
        CycleEventHandler.getSingleton().stopEvents(player, "cookingEvent".hashCode());
        final CookingItems item = forId(itemId);
        if (item != null) {
            setCooking(player, true);
            //RandomEventHandler.addRandom(player);
            player.getPA().RemoveAllWindows();
            player.doAmount = amount;
            if (player.doAmount > player.getItemAmount(itemId)) {
                player.doAmount = player.getItemAmount(itemId);
            }
            if (objectId > 0) {
                player.sendSound(2199, 10, 0);
                player.startAnimation(objectId == 2732 ? 897 : 896);
            }
            CycleEventHandler.getSingleton().addEvent("cookingEvent".hashCode(), player, new CycleEvent() {
                @Override
                public void execute(CycleEventContainer container) {
                    if (!player.playerIsCooking) {
                        setCooking(player, false);
                        container.stop();
                        return;
                    }
                    if (!player.playerHasItem(item.getRawItem(), 1)) {
                        player.sendMessage("You have run out of " + item.getName() + " to cook.");
                        setCooking(player, false);
                        container.stop();
                        return;
                    }
                    boolean burn;
                    if (player.playerEquipment[9] == 775) {
                        burn = !getSuccess(player, 3, item.getLevelReq(), item.getStopBurnGloves());
                    } else {
                        burn = !getSuccess(player, 3, item.getLevelReq(), item.getStopBurn());
                    }
                    player.deleteItem(item.getRawItem(),
                            player.getItemSlot(itemId), 1);
                    if (!burn) {
                        player.sendMessage("You successfully cook the " + item.getName().toLowerCase() + ".");
                        switch (itemId) {
                        }

                        player.addSkillXP(item.getXp(), player.playerCooking);
                        if(item.getName().toLowerCase().contains("cake")){
                            player.addItem(ItemIDs.CAKE_TIN, 1);
                        }
                        player.addItem(item.getCookedItem(), 1);
                    } else {
                        player.sendMessage(
                                "Oops! You accidentally burnt the "
                                        + item.getName().toLowerCase() + "!");
                        player.addItem(item.getBurntItem(), 1);
                    }
                    player.doAmount--;
                    if (player.disconnected) {
                        container.stop();
                        return;
                    }
                    if (objectId < 0) {
                        container.stop();
                        return;
                    }
                    if (player.playerIsCooking && !misc.goodDistance(player.objectX, player.objectY, player.absX, player.absY, 2)) {
                        container.stop();
                        return;
                    }
                    if (player.doAmount > 0) {
                        if (objectId > 0) {
                            player.sendSound(2199, 10, 0);
                            player.startAnimation(objectId == 2732 ? 897 : 896);
                        }
                    } else if (player.doAmount == 0) {
                        setCooking(player, false);
                        container.stop();
                    }
                }

                public void stop() {

                }
            }, 4);
        }
    }

}