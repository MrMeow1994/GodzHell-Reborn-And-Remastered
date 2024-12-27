public class ShopslocationsDialogue extends Dialogue {

    @Override
    public void execute() {
        switch (getNext()) {
            case 0:
                DialogueManager.sendOption(getPlayer(), "GodzHell Shop Area", "GodzHell Reborn Shop Area");
                break;

        }
    }
    @Override
    public boolean clickButton(int id) {

        switch(id) {
            case DialogueConstants.OPTIONS_2_1://fishing guild
                    getPlayer().movePlayer(3351, 3343, 0);
                getPlayer().sendMessage(
                        "You teleport to the GodzHell Shopping Area!");
                    getPlayer().RemoveAllWindows();
                end();
                break;
            case DialogueConstants.OPTIONS_2_2://mining guild
                getPlayer().movePlayer(2805,2787,1);
                getPlayer().sendMessage(
                        "You teleport to the GodzHell Reborn Shopping Area!");
                getPlayer().RemoveAllWindows();
                end();
                break;
        }
        return false;
    }
}
