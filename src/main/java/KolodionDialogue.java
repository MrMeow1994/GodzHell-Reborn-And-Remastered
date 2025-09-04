public class KolodionDialogue extends Dialogue {

    @Override
    public void execute() {
        switch (getNext()) {
            case 0:
                DialogueManager.sendNpcChat(getPlayer(), getPlayer().talkingNpc, Emotion.SAD,
                        "Hey, " + getPlayer().playerName + "...",
                        "A powerful diamond was stolen from me.",
                        "Will you retrieve it?");
                setNext(1);
                break;

            case 1:
                DialogueManager.sendOption(getPlayer(),
                        "Yes, I have it right here.",
                        "No, not yet.");
                break;

            case 2: // If "No" selected
                DialogueManager.sendNpcChat(getPlayer(), getPlayer().talkingNpc, Emotion.SAD,
                        "I see... But I would've shown you the Smoke Dungeon.");
                end();
                break;
        }
    }

    @Override
    public boolean clickButton(int id) {
        switch (id) {
            case DialogueConstants.OPTIONS_2_1: // "Yes"
                if (getPlayer().playerHasItem(4673)) { // Shadow Diamond
                    getPlayer().deleteItem(4673, getPlayer().getItemSlot(4673), 1);
                    getPlayer().sendMessage("Ah... thanksss.");
                    getPlayer().sendMessage("- You get teleported to your last task.");
                    getPlayer().desertTreasure = 7;
                    getPlayer().IsSnowing = 5;
                    getPlayer().movePlayer(3206, 9378, 0); // Smoke dungeon entrance
                    getPlayer().getPA().RemoveAllWindows();
                } else {
                    getPlayer().desertTreasure = 6;
                    getPlayer().sendMessage("You do not have the Shadow Diamond in your inventory.");
                    getPlayer().getPA().RemoveAllWindows();
                }
                end();
                return true;

            case DialogueConstants.OPTIONS_2_2: // "No"
                getPlayer().desertTreasure = 6;
                DialogueManager.sendPlayerChat(getPlayer(), Emotion.CALM, "Not yet.");
                setNext(2);
                return true;
        }
        return false;
    }
}
