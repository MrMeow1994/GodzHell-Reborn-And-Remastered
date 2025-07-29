public class CooksAssistant {

    private static final int EGG = 1944;
    private static final int MILK = 1927;
    private static final int FLOUR = 1933;

    public static void showInformation(client client) {
        for (int i = 8144; i < 8195; i++) {
            client.getPA().sendFrame126("", i);
        }
        client.getPA().sendFrame126("@dre@Cook's Assistant", 8144);
        client.getPA().sendFrame126("", 8145);
        if (client.cookAss == 0) {
            client.getPA().sendFrame126("Cook's Assistant", 8144);
            client.getPA().sendFrame126("I can start this quest by speaking to the Cook in the", 8147);
            client.getPA().sendFrame126("Lumbridge Castle kitchen.", 8148);
            client.getPA().sendFrame126("", 8149);
            client.getPA().sendFrame126("There are no minimum requirements.", 8150);
        } else if (client.cookAss == 1) {
            client.getPA().sendFrame126("Cook's Assistant", 8144);
            client.getPA().sendFrame126("@str@I've talked to the cook.", 8147);
            client.getPA().sendFrame126("He wants me to gather the following materials:", 8148);
            if (client.playerHasItem(EGG, 1)) {
                client.getPA().sendFrame126("@str@1 egg", 8149);
            } else {
                client.getPA().sendFrame126("@red@1 egg", 8149);
            }
            if (client.playerHasItem(MILK, 1)) {
                client.getPA().sendFrame126("@str@1 bucket of milk", 8150);
            } else {
                client.getPA().sendFrame126("@red@1 bucket of milk", 8150);
            }
            if (client.playerHasItem(FLOUR, 1)) {
                client.getPA().sendFrame126("@str@1 heap of flour", 8151);
            } else {
                client.getPA().sendFrame126("@red@1 pot of flour", 8151);
            }
        } else if (client.cookAss == 2) {
            client.getPA().sendFrame126("Cook's Assistant", 8144);
            client.getPA().sendFrame126("@str@I talked to the cook.", 8147);
            client.getPA().sendFrame126("@str@I gave the cook his items.", 8148);
            client.getPA().sendFrame126("I should go speak to the cook.", 8149);
        } else if (client.cookAss == 3) {
            client.getPA().sendFrame126("Cook's Assistant", 8144);
            client.getPA().sendFrame126("@str@I talked to the cook.", 8147);
            client.getPA().sendFrame126("@str@I gave him his items.", 8148);
            client.getPA().sendFrame126("@red@     QUEST COMPLETE", 8150);
            client.getPA().sendFrame126("As a reward, I gained 150 Cooking Experience.", 8151);
        }
        client.getPA().showInterface(8134);
    }

}