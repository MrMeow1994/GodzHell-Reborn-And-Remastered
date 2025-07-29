public class DungeonsDialogue extends Dialogue{
    /**
     *
     */
    @Override
    public void execute() {
        switch (getNext()) {
            case 0:
                DialogueManager.sendOption(getPlayer(), "Trains", "Dungeons");
                break;
            case 1:
                DialogueManager.sendOption(getPlayer(), "Brimhaven Dungeon", "Edgeville Dungeon", "Kalphite Cave", "Ancient Cavern", "More Options");
                break;
            case 2:
                DialogueManager.sendOption(getPlayer(), "Fremennik Slayer Dungeon", "Taverley Dungeon", "Asgarnian Ice Caves", "Falador Mole Lair", "More Options");
                break;
            case 3:
                DialogueManager.sendOption(getPlayer(), "Crandor and Karamja Dungeon", "Jogre Dungeon", "Stronghold of Player Safety", "Varrock Sewers", "More Options");
                break;
        }
    }
    @Override
    public boolean clickButton(int id) {
        switch(id) {
            case DialogueConstants.OPTIONS_2_1:
                if(getNext() == 0) {
                    DialogueManager.sendOption(getPlayer(), "Old Gh Train", "Ghr Train", "2nd Level of Gh Train");
                    //getPlayer().movePlayer(3117,9849,0);
                    //end();
                   // getPlayer().getPA().RemoveAllWindows();
                }
                break;
            case DialogueConstants.OPTIONS_2_2:
                if(getNext() == 0) {
                DialogueManager.sendOption(getPlayer(), "Brimhaven Dungeon", "Edgeville Dungeon", "Kalphite Cave", "Ancient Cavern", "More Options");
                    setNext(1);
                }
                break;
            case DialogueConstants.OPTIONS_5_1:
                if(getNext() == 1) {
                    getPlayer().movePlayer(2711,9564,0);
                    end();
                    getPlayer().getPA().RemoveAllWindows();
                }
                break;
            case DialogueConstants.OPTIONS_5_2:
                if(getNext() == 1) {
                    getPlayer().movePlayer(3096,9869,0);
                    end();
                    getPlayer().getPA().RemoveAllWindows();
                }
                break;
            case DialogueConstants.OPTIONS_5_3:
                if(getNext() == 1) {
                    getPlayer().movePlayer(3485,9509,2);
                    end();
                    getPlayer().getPA().RemoveAllWindows();
                }
                break;
            case DialogueConstants.OPTIONS_5_4:
                if(getNext() == 1) {
                    getPlayer().movePlayer(1764,5365,1);
                    end();
                    getPlayer().getPA().RemoveAllWindows();
                }
                break;
            case DialogueConstants.OPTIONS_5_5:
                if(getNext() == 1) {
                    DialogueManager.sendOption(getPlayer(), "Fremennik Slayer Dungeon", "Taverley Dungeon", "Asgarnian Ice Caves", "Falador Mole Lair", "More Options");
                    setNext(2);
                } else if(getNext() == 2) {
                    DialogueManager.sendOption(getPlayer(), "Crandor and Karamja Dungeon", "Jogre Dungeon", "Stronghold of Player Safety", "Varrock Sewers", "More Options");
                    setNext(3);
                }else if(getNext() == 4) {
                    DialogueManager.sendOption(getPlayer(), "Brimhaven Dungeon", "Edgeville Dungeon", "Kalphite Cave", "Ancient Cavern", "More Options");
                    setNext(1);
                }
                break;
        }
        return false;
    }
}
