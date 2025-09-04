public class MalakDialogue extends Dialogue {

    @Override
    public void execute() {
        switch (getNext()) {
            case 0: // case 1920
                DialogueManager.sendNpcChat(getPlayer(), getPlayer().talkingNpc, Emotion.ANNOYED,
                        "i need you to obtain a blood diamond *Hissss*",
                        "Do you have it already?",
                        "@bla@i @red@N@red@E@red@E@red@D@bla@ IT! *hiss*");
                setNext(1);
                break;

            case 1: // case 1921
                DialogueManager.sendOption(getPlayer(),
                        "Yea, i got it right here.",
                        "No, I dont have it yet");
                break;

            case 2: // case 1922
                DialogueManager.sendNpcChat(getPlayer(), getPlayer().talkingNpc, Emotion.ANGRY_4,
                        "@red@THEN FIND IT!!!@red@*Hisssss*");
                end();
                break;
        }
    }

    @Override
    public boolean clickButton(int id) {
        switch (id) {
            case DialogueConstants.OPTIONS_2_1:
                if (getPlayer().playerHasItem(4670)) {
                    getPlayer().deleteItem(4670, getPlayer().getItemSlot(4670), 1);
                    getPlayer().IsSnowing = 3;
                    getPlayer().desertTreasure = 6;
                    getPlayer().movePlayer(2545, 3422, 0);
                } else {
                    getPlayer().sendMessage("You do not have the Blood Diamond in your inventory.");
                }
                getPlayer().desertTreasure = 5;
                getPlayer().getPA().RemoveAllWindows();
                end();
                return true;

            case DialogueConstants.OPTIONS_2_2:
                DialogueManager.sendPlayerChat(getPlayer(), Emotion.CALM, "No, I dont have it yet");
                setNext(2);
                return true;
        }
        return false;
    }
}
