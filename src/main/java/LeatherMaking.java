public class LeatherMaking extends CraftingData {

    public static void craftLeatherDialogue(final client c, final int itemUsed,
                                            final int usedWith) {
        for (final leatherData l : leatherData.values()) {
            final int leather = itemUsed == 1733 ? usedWith : itemUsed;
            if (leather == l.getLeather()) {
                if (l.getLeather() == 1741) {
                    c.getPA().showInterface(2311);
                    c.leatherType = leather;
                    return;
                }
                String[] name = { "Body", "Chaps", "Bandana", "Boots", "Vamb", };
                if (l.getLeather() == 1743) {
                    c.getPA().sendFrame164(4429);
                    c.getPA().sendFrame246(1746, 200, 1131);
                    c.getPA().sendFrame126(
                            Item.getItemName(1131), 2799);
                    c.leatherType = leather;
                }
                if (l.getLeather() == 6289) {
                    c.getPA().sendFrame164(8938);
                    c.getPA().sendFrame246(8941, 180, 6322);
                    c.getPA().sendFrame246(8942, 180, 6324);
                    c.getPA().sendFrame246(8943, 180, 6326);
                    c.getPA().sendFrame246(8944, 180, 6328);
                    c.getPA().sendFrame246(8945, 180, 6330);
                    for (int i = 0; i < name.length; i++) {
                        c.getPA().sendFrame126(name[i], 8949 + i * 4);
                    }
                    c.leatherType = leather;
                    return;
                }
            }
        }
        for (final leatherDialogueData d : leatherDialogueData.values()) {
            final int leather = itemUsed == 1733 ? usedWith : itemUsed;
            String[] name = { "Vamb", "Body", "Chaps", };
            if (leather == d.getLeather()) {
                c.getPA().sendFrame164(8880);
                c.getPA().sendFrame246(8883, 180, d.getVamb());
                c.getPA().sendFrame246(8884, 180, d.getChaps());
                c.getPA().sendFrame246(8885, 180, d.getBody());
                for (int i = 0; i < name.length; i++) {
                    c.getPA().sendFrame126(name[i], 8889 + i * 4);
                }
                c.leatherType = leather;
                return;
            }
        }
    }

    private static int amount;

    public static void craftLeather(final client player, final int buttonId) {
        if (player.isCrafting) {
            return;
        }
        for (final leatherData l : leatherData.values()) {
            if (buttonId == l.getButtonId(buttonId)) {
                if (player.leatherType == l.getLeather()) {
                    if (player.playerLevel[12] < l.getLevel()) {
                        player.sendMessage(
                                "You need a crafting level of " + l.getLevel()
                                        + " to make this.");
                        player.getPA().RemoveAllWindows();
                        return;
                    }
                    if (!player.playerHasItem(1734)) {
                        player.sendMessage(
                                "You need some thread to make this.");
                        player.getPA().RemoveAllWindows();
                        return;
                    }
                    if (!player.playerHasItem(player.leatherType,
                            l.getHideAmount())) {
                        player.sendMessage(
                                "You need "
                                        + l.getHideAmount()
                                        + " "
                                        + Item.getItemName(
                                        player.leatherType).toLowerCase()
                                        + " to make "
                                        + Item.getItemName(
                                        l.getProduct()).toLowerCase()
                                        + ".");
                        player.getPA().RemoveAllWindows();
                        return;
                    }
                    player.startAnimation(1249);
                    player.getPA().RemoveAllWindows();
                    player.isCrafting = true;
                    amount = l.getAmount(buttonId);
                    EventManager.getSingleton().addEvent(player,
                            new Event() {

                                @Override
                                public void execute(
                                        EventContainer container) {
                                    if (player.isCrafting) {
                                        if (!player
                                                .playerHasItem(1734)) {
                                            player
                                                    .sendMessage(
                                                            "You have run out of thread.");
                                            container.stop();
                                            return;
                                        }
                                        if (!player
                                                .playerHasItem(player.leatherType,
                                                        l.getHideAmount())) {
                                            player
                                                    .sendMessage(
                                                            "You have run out of leather.");
                                            container.stop();
                                            return;
                                        }
                                        if (amount == 0) {
                                            container.stop();
                                            return;
                                        }
                                        player.deleteItem(
                                                1734,
                                                player
                                                        .getItemSlot(1734), 1);
                                        player.deleteItem(
                                                player.leatherType,
                                                l.getHideAmount());
                                        player.addItem(
                                                l.getProduct(), 1);
                                        player
                                                .sendMessage(
                                                        "You make "
                                                                + (Item
                                                                .getItemName(
                                                                        l.getProduct())
                                                                .contains(
                                                                        "body") ? "a"
                                                                : "some")
                                                                + " "
                                                                + Item
                                                                .getItemName(l
                                                                        .getProduct())
                                                                + ".");
                                        player.addSkillXP(
                                                (int) l.getXP()* Config.CRAFTING_EXPERIENCE, 12);
                                        player.startAnimation(1249);
                                        amount--;
                                        if (!player
                                                .playerHasItem(1734)) {
                                            player
                                                    .sendMessage(
                                                            "You have run out of thread.");
                                            container.stop();
                                            return;
                                        }
                                        if (!player
                                                .playerHasItem(player.leatherType,
                                                        l.getHideAmount())) {
                                            player
                                                    .sendMessage(
                                                            "You have run out of leather.");
                                            container.stop();
                                            return;
                                        }
                                    } else {
                                        container.stop();
                                    }
                                }

                                @Override
                                public void stop() {
                                    player.isCrafting = false;
                                }
                            }, 5+600);
                }
            }
        }
    }
}