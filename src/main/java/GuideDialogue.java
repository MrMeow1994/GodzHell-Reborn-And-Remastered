public class GuideDialogue extends Dialogue {

    @Override
    public void execute() {
        switch (getNext()) {
            case 0:
                DialogueManager.sendNpcChat(getPlayer(), getPlayer().talkingNpc, Emotion.HAPPY,
                        "Welcome to GodzHell Reborn.",
                        "We are the first remake of GodzHell,",
                        "started in 2011 and here to help you.");
                setNext(1);
                break;

            case 1:
                DialogueManager.sendNpcChat(getPlayer(), getPlayer().talkingNpc, Emotion.HAPPY,
                        "Brought back in 2024 — and here to stay!",
                        "All old staff will regain their ranks.",
                        "We will never go down again.");
                setNext(2);
                break;

            case 2:
                DialogueManager.sendNpcChat(getPlayer(), getPlayer().talkingNpc, Emotion.CALM,
                        "With the Mystyc Dragon by our side,",
                        "built strong with love and hours of dev time,",
                        "we're back, brothers and sisters!");
                setNext(3);
                break;

            case 3:
                DialogueManager.sendPlayerChat(getPlayer(), Emotion.CALM,
                        "What can I do in the game?");
                setNext(4);
                break;

            case 4:
                DialogueManager.sendNpcChat(getPlayer(), getPlayer().talkingNpc, Emotion.LAUGHING,
                        "What can you do? A lot!",
                        "Classic GodzHell locations are back,",
                        "new areas for training are open,",
                        "and lost dungeons have been rediscovered.");
                setNext(5);
                break;

            case 5:
                DialogueManager.sendNpcChat(getPlayer(), getPlayer().talkingNpc, Emotion.CALM_CONTINUED,
                        "Would you like to hear about...",
                        "The differences between versions,",
                        "or recent updates and how restarts work?");
                setNext(6);
                break;

            case 6:
                DialogueManager.sendOption(getPlayer(),
                        "Yes, tell me the differences.",
                        "No thanks, I'm good.",
                        "Tell me about updates and restart cycle.");
                break;

            case 8: // Option 1 response
                DialogueManager.sendNpcChat(getPlayer(), getPlayer().talkingNpc, Emotion.CALM,
                        "We’ve got Tormented Demons, Corp, GWD!",
                        "Trading and PvP fully work as expected.",
                        "Bank resets are gone — forever.",
                        "Feel safe on GodzHell Reborn & Remastered.");
                setNext(9);
                break;

            case 9:
                DialogueManager.sendNpcChat(getPlayer(), getPlayer().talkingNpc, Emotion.CALM,
                        "Hope you enjoy your stay!",
                        "Remember to vote if you like the server.",
                        "Voting brings more players!");
                setNext(10);
                break;

            case 10:
                getPlayer().getPA().RemoveAllWindows();
                end();
                break;

            case 11: // Option 3 - Updates path begins
                DialogueManager.sendNpcChat(getPlayer(), getPlayer().talkingNpc, Emotion.CALM,
                        "We post frequent updates — new content,",
                        "events, fixes, and behind-the-scenes work.",
                        "Join the Discord to stay informed!");
                setNext(12);
                break;

            case 12:
                DialogueManager.sendNpcChat(getPlayer(), getPlayer().talkingNpc, Emotion.CALM,
                        "We now have two thieving areas to use —",
                        "one in Falador and one near East Ardougne.",
                        "Both give rewards & keep training fast.");
                setNext(13);
                break;

            case 13:
                DialogueManager.sendNpcChat(getPlayer(), getPlayer().talkingNpc, Emotion.CALM,
                        "We also brought back both shop zones.",
                        "One for classic GodzHell feel, and one",
                        "modern zone styled for GHR players.");
                setNext(14);
                break;

            case 14:
                DialogueManager.sendNpcChat(getPlayer(), getPlayer().talkingNpc, Emotion.CALM,
                        "Several skills have been reworked —",
                        "Woodcutting, Mining, and Smithing now",
                        "offer faster XP and better rewards.");
                setNext(15);
                break;

            case 15:
                DialogueManager.sendNpcChat(getPlayer(), getPlayer().talkingNpc, Emotion.CALM,
                        "We also updated Fletching, Crafting,",
                        "and Slayer — with more skill reworks",
                        "planned in upcoming updates!");
                setNext(16);
                break;

            case 16:
                DialogueManager.sendNpcChat(getPlayer(), getPlayer().talkingNpc, Emotion.CALM,
                        "We merged the best of both servers.",
                        "So whether you're old school or new,",
                        "GodzHell Reborn has you covered!");
                setNext(17);
                break;

            case 17:
                DialogueManager.sendNpcChat(getPlayer(), getPlayer().talkingNpc, Emotion.CALM,
                        "The server auto-restarts every 48 hours.",
                        "No data loss, no downtime.",
                        "You'll always get a 5-minute warning.",
                        "GodzHell stays fresh and stable.");
                setNext(10);
                break;
        }
    }

    @Override
    public boolean clickButton(int id) {
        switch (id) {
            case DialogueConstants.OPTIONS_3_1:
                DialogueManager.sendPlayerChat(getPlayer(), Emotion.CALM, "Yes please!");
                setNext(8);
                return true;

            case DialogueConstants.OPTIONS_3_2:
                DialogueManager.sendPlayerChat(getPlayer(), Emotion.CALM, "No thank you.");
                setNext(10);
                return true;

            case DialogueConstants.OPTIONS_3_3:
                DialogueManager.sendPlayerChat(getPlayer(), Emotion.CALM, "Tell me about updates and the restart cycle.");
                setNext(11);
                return true;
        }
        return false;
    }
}
