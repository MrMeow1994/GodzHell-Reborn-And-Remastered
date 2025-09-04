public class DimintheisDialogue extends Dialogue {

    @Override
    public void execute() {
        switch (getNext()) {

            case 0: // case 664
                if(getPlayer().desertTreasure == 0) {
                    DialogueManager.sendNpcChat(getPlayer(), getPlayer().talkingNpc, Emotion.HAPPY,
                            "I..Heard of tresure....",
                            "A treasure...so great...",
                            "the knowledge..of Ancient Magiks!");
                    setNext(1);
                }
                break;

            case 1: // case 665
                DialogueManager.sendOption(getPlayer(),
                        "Yes..im ready..",
                        "No..i dont wanna die..");
                break;

            case 2: // case 666
                DialogueManager.sendNpcChat(getPlayer(), getPlayer().talkingNpc, Emotion.ANNOYED,
                        "yes..thats fine...",
                        "Ill ask the next non coward");
                getPlayer().sendMessage("If i were you..i would get the following items in your bank.");
                getPlayer().sendMessage("Get the following,");
                getPlayer().sendMessage("1 yew log");
                getPlayer().sendMessage("1 RAW manta ray");
                getPlayer().sendMessage("1 tinderbox");
                getPlayer().sendMessage("1 soul rune");
                getPlayer().sendMessage("1 coif");
                getPlayer().sendMessage("1 Bronze platebody");
                getPlayer().sendMessage("You will not need these right now..but later, they will be needed");
                getPlayer().getPA().RemoveAllWindows();
                end();
                break;


        }
    }

    @Override
    public boolean clickButton(int id) {
        switch (id) {
            case DialogueConstants.OPTIONS_2_1:
                if(getNext() == 1) {
                    if (getPlayer().playerHasItem(995)
                            && getPlayer().playerHasItemAmount(995, 199_999_999)
                            && getPlayer().playerLevel[15] >= 56
                            && getPlayer().playerLevel[6] >= 82) {

                        getPlayer().deleteItem(995, getPlayer().getItemSlot(995), 199_999_999);
                        getPlayer().sendMessage("If i were you..i would get the following items in your bank.");
                        getPlayer().sendMessage("Get the following,");
                        getPlayer().sendMessage("1 yew log");
                        getPlayer().sendMessage("1 RAW manta ray");
                        getPlayer().sendMessage("1 tinderbox");
                        getPlayer().sendMessage("1 soul rune");
                        getPlayer().sendMessage("1 coif");
                        getPlayer().sendMessage("1 Bronze platebody");
                        getPlayer().sendMessage("You will not need these right now..but later, they will be needed");
                        getPlayer().IsSnowing = 3;
                        getPlayer().desertTreasure = 2;
                        getPlayer().movePlayer(2843, 3674, 0);
                    } else {
                        getPlayer().sendMessage("You must have 82 magic & 56 Herblore as well as 200m to start this quest.");
                    }
                    getPlayer().getPA().RemoveAllWindows();
                    end();
                }
                return true;

            case DialogueConstants.OPTIONS_2_2:
                DialogueManager.sendPlayerChat(getPlayer(), Emotion.CALM, "No");
                setNext(2);
                return true;
        }
        return false;
    }
} 
