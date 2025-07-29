public class CooksDialogue extends Dialogue {

    @Override
    public void execute() {
        switch(getNext()) {
            case 0:
                if (getPlayer().cookAss == 0) {
                    DialogueManager.sendNpcChat(getPlayer(), 278, Emotion.SAD, "What am I to do?");
                    setNext(1);
                } else if (getPlayer().cookAss == 1) {
                    if (getPlayer().playerHasItem(1944, 1)
                            && getPlayer().playerHasItem(1927, 1)
                            && getPlayer().playerHasItem(1933, 1)) {
                        DialogueManager.sendPlayerChat(getPlayer(), Emotion.CALM, "Here's all the items!");
                        setNext(9);
                    } else {
                        DialogueManager.sendPlayerChat(getPlayer(), Emotion.CALM, "I don't have all the items yet.");
                        setNext(11);
                    }
                } else if (getPlayer().cookAss == 2) {
                    DialogueManager.sendPlayerChat(getPlayer(), Emotion.CALM, "So do I get to go to the Duke's Party?");
                    setNext(12);
                } else if (getPlayer().cookAss == 3) {
                    DialogueManager.sendNpcChat(getPlayer(), 278, Emotion.DEFAULT, "Thanks for helping me out friend!");
                    setNext(8);
                }
                break;
            case 1:
                DialogueManager.sendOption(getPlayer(), "What's wrong?", "Can you cook me a cake?",
                        "You don't look very happy.", "Nice hat.");
                break;
            case 2:
                DialogueManager.sendNpcChat(getPlayer(), 278, Emotion.SAD, "Oh dear, oh dear, oh dear, I'm in a terrible terrible",
                        "mess! It's the Duke's birthday today, and I should be",
                        "making him a lovely big birthday cake!");
                setNext(3);
                break;
            case 3:
                DialogueManager.sendNpcChat(getPlayer(), 278, Emotion.SAD, "I've forgotten to buy the ingredients. I'll never get",
                        "them in time now. He'll sack me! What will I do? I have",
                        "four children and a goat to look after. Would you help",
                        "me? Please?");
                setNext(4);
                break;
            case 4:
                DialogueManager.sendOption(getPlayer(), "I'm always happy to help a cook in distress.",
                        "I can't right now, Maybe later.");
                break;
            case 5:
                DialogueManager.sendNpcChat(getPlayer(), 278, Emotion.HAPPY, "Oh thank you, thank you. I need milk, an egg, and",
                        "flour. I'd be very grateful if you can get them for me.");
                getPlayer().cookAss = 1;
                QuestAssistant.sendStages(getPlayer());
                setNext(6);
                break;
            case 6:
                DialogueManager.sendPlayerChat(getPlayer(), Emotion.CALM, "So where do I find these ingredients then?");
                setNext(7);
                break;
            case 7:
                DialogueManager.sendNpcChat(getPlayer(), 278, Emotion.CALM, "You can find flour in any of the shops here.",
                        "You can find eggs by killing chickens.",
                        "You can find milk by using a bucket on a cow");
                setNext(8);
                break;
            case 8:
                getPlayer().getPA().RemoveAllWindows();
                end();
                break;
            case 9:
                DialogueManager.sendNpcChat(getPlayer(), 278, Emotion.CALM, "You brought me everything I need! I'm saved!",
                        "Thank you!");
                getPlayer().deleteItem(1944, 1);
                getPlayer().deleteItem(1927, 1);
                getPlayer().deleteItem(1933, 1);
                getPlayer().cookAss = 2;
                setNext(10);
                break;
            case 10:
                DialogueManager.sendPlayerChat(getPlayer(), Emotion.CALM, "So do I get to go to the Duke's Party?");
                setNext(12);
                break;
            case 11:
                DialogueManager.sendNpcChat(getPlayer(), 278, Emotion.SAD, "Oh please! Hurry then!");
                setNext(8);
                break;
            case 12:
                DialogueManager.sendNpcChat(getPlayer(), 278, Emotion.CALM, "I'm afraid not, only the big cheeses get to dine with the",
                        "Duke.");
                setNext(13);
                break;
            case 13:
                DialogueManager.sendNpcChat(getPlayer(), 278, Emotion.CALM, "Well, maybe one day I'll be important enough to sit on",
                        "the Duke's table");
                setNext(14);
                break;
            case 14:
                DialogueManager.sendNpcChat(getPlayer(), 278, Emotion.CALM, "Maybe, but I won't be holding my breath.");
                setNext(15);
                break;
            case 15:
                QuestRewards.cookReward(getPlayer());
                end();
                break;
        }
    }
    @Override
    public boolean clickButton(int id) {

        switch(id) {
            case DialogueConstants.OPTIONS_4_1://fishing guild
                DialogueManager.sendPlayerChat(getPlayer(), Emotion.CALM, "What's wrong?");
                setNext(2);
                break;
            case DialogueConstants.OPTIONS_2_1://fishing guild
                DialogueManager.sendPlayerChat(getPlayer(), Emotion.CALM,"Yes, I'll help you.");
                setNext(5);
                break;
            case DialogueConstants.OPTIONS_2_2://fishing guild
                DialogueManager.sendPlayerChat(getPlayer(), Emotion.CALM, "I can't right now, Maybe later.");
                setNext(8);
                break;
            case DialogueConstants.OPTIONS_4_2://mining guild
                DialogueManager.sendNpcChat(getPlayer(), 278, Emotion.ANGRY_1, "Does it look like I have the time?");
                setNext(8);
                break;
            case DialogueConstants.OPTIONS_4_3://crafting guild
                DialogueManager.sendPlayerChat(getPlayer(), Emotion.CALM, "You don't look so happy.");
                setNext(2);
                break;
            case DialogueConstants.OPTIONS_4_4://crafting guild
                DialogueManager.sendNpcChat(getPlayer(), 278, Emotion.ANGRY_1, "I don't have time for your jibber-jabber!");
                setNext(8);
                break;
        }
        return false;
    }


}