public class RasoloDialogue extends Dialogue {

    private static final int YEW_LOG = 1515;
    private static final int RAW_MANTA = 391;
    private static final int TINDERBOX = 590;
    private static final int SOUL_RUNE = 566;
    private static final int COIF = 1169;
    private static final int BRONZE_PLATEBODY = 1103;

    @Override
    public void execute() {
        switch (getNext()) {
            case 0: // Intro dialogue (case 1972)
                DialogueManager.sendNpcChat(getPlayer(), getPlayer().talkingNpc, Emotion.HAPPY,
                        "Hey there!",
                        "I'm a merchanter, and I REALLY need this stuff..",
                        "In return, I'll take you to the shadow area.");
                getPlayer().sendMessage("Get the following:");
                getPlayer().sendMessage("1 yew log");
                getPlayer().sendMessage("1 RAW manta ray");
                getPlayer().sendMessage("1 tinderbox");
                getPlayer().sendMessage("1 soul rune");
                getPlayer().sendMessage("1 coif");
                getPlayer().sendMessage("1 Bronze platebody");
                setNext(1);
                break;

            case 1: // Options (case 1973)
                DialogueManager.sendOption(getPlayer(),
                        "Yea, right here.",
                        "Not yet, sorry");
                break;

            case 2: // Declined / not ready (case 1974)
                DialogueManager.sendNpcChat(getPlayer(), getPlayer().talkingNpc, Emotion.SAD,
                        "Oh.. too bad...",
                        "I was gonna take you to the shadow cave..");
                end();
                break;
        }
    }

    @Override
    public boolean clickButton(int id) {
        switch (id) {
            case DialogueConstants.OPTIONS_2_1:
                if (hasAllItems()) {
                    deleteAllItems();
                    getPlayer().desertTreasure = 3;
                    getPlayer().movePlayer(2901, 9801, 0); // Shadow area coords
                    getPlayer().sendMessage("Rasolo takes you to the shadow cave.");
                } else {
                    getPlayer().desertTreasure = 2;
                    getPlayer().sendMessage("You don't have all the required items.");
                }
                end();
                return true;

            case DialogueConstants.OPTIONS_2_2:
                getPlayer().desertTreasure = 2;
                DialogueManager.sendPlayerChat(getPlayer(), Emotion.CALM, "Not yet, sorry");
                setNext(2);
                return true;
        }
        return false;
    }

    private boolean hasAllItems() {
        return getPlayer().playerHasItem(YEW_LOG)
                && getPlayer().playerHasItem(RAW_MANTA)
                && getPlayer().playerHasItem(TINDERBOX)
                && getPlayer().playerHasItem(SOUL_RUNE)
                && getPlayer().playerHasItem(COIF)
                && getPlayer().playerHasItem(BRONZE_PLATEBODY);
    }

    private void deleteAllItems() {
        getPlayer().deleteItem(YEW_LOG, getPlayer().getItemSlot(YEW_LOG), 1);
        getPlayer().deleteItem(RAW_MANTA, getPlayer().getItemSlot(RAW_MANTA), 1);
        getPlayer().deleteItem(TINDERBOX, getPlayer().getItemSlot(TINDERBOX), 1);
        getPlayer().deleteItem(SOUL_RUNE, getPlayer().getItemSlot(SOUL_RUNE), 1);
        getPlayer().deleteItem(COIF, getPlayer().getItemSlot(COIF), 1);
        getPlayer().deleteItem(BRONZE_PLATEBODY, getPlayer().getItemSlot(BRONZE_PLATEBODY), 1);
    }
}
