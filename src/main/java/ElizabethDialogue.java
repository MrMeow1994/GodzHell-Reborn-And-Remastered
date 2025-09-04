public class ElizabethDialogue extends Dialogue {

    @Override
    public void execute() {
        switch (getNext()) {
            case 0:
                DialogueManager.sendNpcChat(getPlayer(), getPlayer().talkingNpc, Emotion.CALM,
                        "Your task here is simple.",
                        "Go through the cave and kill him.",
                        "Then return to me with the diamond.");
                setNext(1);
                break;

            case 1:
                DialogueManager.sendOption(getPlayer(),
                        "Yes, I have the diamond.",
                        "No, not yet.");
                break;

            case 2:
                DialogueManager.sendNpcChat(getPlayer(), getPlayer().talkingNpc, Emotion.CALM,
                        "Alright, but you're so close...",
                        "Remember the reward that awaits.");
                getPlayer().getPA().RemoveAllWindows();
                end();
                break;
        }
    }

    @Override
    public boolean clickButton(int id) {
        switch (id) {
            case DialogueConstants.OPTIONS_2_1: { // Yes
                if (getPlayer().playerHasItem(4672)) { // Smoke Diamond
                    getPlayer().deleteItem(4672, getPlayer().getItemSlot(4672), 1);
                    getPlayer().IsSnowing = 3;
                    getPlayer().desertTreasure = 8;
                    getPlayer().teleportToX = 3233;
                    getPlayer().teleportToY = 9317;
                    getPlayer().heightLevel = 0;

                    getPlayer().addItem(6099, 1); // Ancient staff?
                    getPlayer().addItem(565, 300); // Bloods
                    getPlayer().addItem(560, 300); // Deaths
                    getPlayer().addItem(555, 500); // Waters
                    getPlayer().addItem(562, 300); // Chaos
                    getPlayer().deleteItem(275, getPlayer().getItemSlot(275), 1); // Possibly quest token

                    getPlayer().sendMessage("CONGRATULATIONS");
                    getPlayer().sendMessage("YOU HAVE UNLOCKED ANCIENT MAGIKS!");

                    getPlayer().addSkillXP((100_000 * getPlayer().playerLevel[6]), 6); // Magic XP
                    PlayerHandler.messageToAll = getPlayer().playerName + " has just completed Desert Treasure!";
                } else {
                    getPlayer().sendMessage("You do not have the Smoke Diamond in your inventory.");
                }
                getPlayer().desertTreasure = 7;
                getPlayer().getPA().RemoveAllWindows();
                end();
                return true;
            }

            case DialogueConstants.OPTIONS_2_2: // No
                getPlayer().desertTreasure = 7;
                DialogueManager.sendPlayerChat(getPlayer(), Emotion.CALM, "No, not yet.");
                setNext(2);
                return true;
        }
        return false;
    }
}
