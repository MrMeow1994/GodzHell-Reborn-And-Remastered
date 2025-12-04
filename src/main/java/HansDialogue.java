public class HansDialogue extends Dialogue {

    @Override
    public void execute() {
        switch(getNext()) {
            case 0:
                DialogueManager.sendNpcChat(getPlayer(), 0, Emotion.DEFAULT, "Hello, what are you doing here?");
                setNext(1);
                break;
            case 1:
                DialogueManager.sendOption(getPlayer(), "I'm looking for whoever is in charge of this place.", "I have come to kill everyone in this castle!", "I don't know. I'm lost. Where am I?", "Can you tell me how long I've been here?", "Nothing.");
                break;
            case 2:
                getPlayer().getPA().RemoveAllWindows();
                end();
                break;
            case 3:
                DialogueManager.sendNpcChat(getPlayer(), 0, Emotion.DEFAULT, "Who, the Duke? He's in his study, on the first floor.");
                setNext(2);
                break;
            case 4:
                DialogueManager.sendNpcChat(getPlayer(), 0, Emotion.DEFAULT, "You are in Lumbridge Castle.");
                setNext(2);
                break;
            case 5:
                final NPC npc = NPCHandler.npcs[player.npcClickIndex];
                if (npc == null) return;

// Force some flavor dialogue
                npc.forceChat("Help! help!");

// 8-way direction offsets
                int[][] directions = {
                        {0, 1},   // N
                        {1, 1},   // NE
                        {1, 0},   // E
                        {1, -1},  // SE
                        {0, -1},  // S
                        {-1, -1}, // SW
                        {-1, 0},  // W
                        {-1, 1}   // NW
                };

// Mutable flee direction
                final int[] fleeDir = {0, 0};

                CycleEventHandler.getSingleton().addEvent(npc, new CycleEvent() {
                    int steps = 0;

                    @Override
                    public void execute(CycleEventContainer container) {
                        if (npc == null || npc.IsDead || steps >= 10) {
                            container.stop();
                            return;
                        }

                        int newX = npc.absX + fleeDir[0];
                        int newY = npc.absY + fleeDir[1];

                        // If current direction is invalid or uninitialized, pick a new one
                        if (fleeDir[0] == 0 && fleeDir[1] == 0 || !Region.canMove(npc.absX, npc.absY, newX, newY, npc.heightLevel, 1, 1)) {
                            boolean foundNewPath = false;

                            for (int i = 0; i < directions.length; i++) {
                                int[] dir = directions[misc.random(directions.length - 1)];
                                int checkX = npc.absX + dir[0];
                                int checkY = npc.absY + dir[1];

                                if (Region.canMove(npc.absX, npc.absY, checkX, checkY, npc.heightLevel, 1, 1)) {
                                    fleeDir[0] = dir[0];
                                    fleeDir[1] = dir[1];
                                    foundNewPath = true;
                                    break;
                                }
                            }

                            if (!foundNewPath) {
                                npc.forceChat("I'm surrounded!");
                                container.stop();
                                return;
                            }

                            // Recalculate newX and newY with new direction
                            newX = npc.absX + fleeDir[0];
                            newY = npc.absY + fleeDir[1];
                        }

                        // Apply movement
                        npc.moveX = fleeDir[0];
                        npc.moveY = fleeDir[1];
                        npc.walkingType = 1;
                        npc.updateRequired = true;
                        npc.direction = npc.getNextWalkingDirection();
                        steps++;
                    }

                    @Override
                    public void stop() {

                    }
                }, 1 * 600);



// Optional: do something after a few ticks
                setNext(2); // if you're in a dialogue script, this continues the dialogue sequence

                break;
        }
        // TODO Auto-generated method stub

    }
    @Override
    public boolean clickButton(int id) {

        switch(id) {
            case DialogueConstants.OPTIONS_5_1://fishing guild
                DialogueManager.sendPlayerChat(getPlayer(), Emotion.CALM, "I'm looking for whoever is in charge of this place.");
                setNext(3);
                break;
            case DialogueConstants.OPTIONS_5_2://mining guild
                DialogueManager.sendPlayerChat(getPlayer(), Emotion.DELIGHTED_EVIL, "I have come to kill everyone in this castle!");
                setNext(5);
                break;
            case DialogueConstants.OPTIONS_5_3://crafting guild
                DialogueManager.sendPlayerChat(getPlayer(), Emotion.CALM, "I don't know. I'm lost. Where am I?");
                setNext(4);
                break;
            case DialogueConstants.OPTIONS_5_4://crafting guild
                setNext(2);
                break;
            case DialogueConstants.OPTIONS_5_5://crafting guild
                DialogueManager.sendPlayerChat(getPlayer(), Emotion.ANNOYED, "Nothing.");
                setNext(2);
                break;
        }
        return false;
    }


}