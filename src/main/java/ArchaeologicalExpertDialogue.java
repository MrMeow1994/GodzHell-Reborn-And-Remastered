public class ArchaeologicalExpertDialogue extends Dialogue {

    @Override
    public void execute() {
        switch (getNext()) {
            case 0: // case 619
                DialogueManager.sendNpcChat(getPlayer(), getPlayer().talkingNpc, Emotion.DEFAULT,
                        "I need the ice diamond to save my wifes life!",
                        "I heard your in need of Blood Diamond..i could show you..",
                        "Do you have the Ice Diamond though?");
                setNext(1);
                break;

            case 1: // case 620
                DialogueManager.sendOption(getPlayer(),
                        "Yes, I did",
                        "No, I did not");
                break;

            case 2: // case 621
                DialogueManager.sendNpcChat(getPlayer(), getPlayer().talkingNpc, Emotion.ANNOYED,
                        "Then why are you standing here?");
                end();
                break;
        }
    }

    @Override
    public boolean clickButton(int id) {
        switch (id) {
            case DialogueConstants.OPTIONS_2_1:
                if (getPlayer().playerHasItem(4671)) {
                    getPlayer().deleteItem(4671, getPlayer().getItemSlot(4671), 1);
                    getPlayer().movePlayer(3544, 3427, 0);
                    getPlayer().desertTreasure = 4;
                    getPlayer().sendMessage("-He Teleports you then runs away-");
                    getPlayer().sendMessage("Go through the swamp, i would kill those ghasts if i were you..");
                } else {
                    getPlayer().sendMessage("You do not have the ice Diamond in your inventory.");
                }
                getPlayer().desertTreasure = 3;
                getPlayer().getPA().RemoveAllWindows();
                end();
                return true;

            case DialogueConstants.OPTIONS_2_2:
                DialogueManager.sendPlayerChat(getPlayer(), Emotion.CALM, "No, I did not");
                setNext(2); // Send to annoyed follow-up
                return true;
        }
        return false;
    }
}
