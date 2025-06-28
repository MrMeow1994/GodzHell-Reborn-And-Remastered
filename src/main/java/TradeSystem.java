import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class TradeSystem {
    public client c;

    public TradeSystem(client c){
        this.c = c;
    }
    public void process() {
        if (c.tradeRequest > 0 && PlayerHandler.players[c.tradeRequest] != null) {
            if(c.macAddress != PlayerHandler.players[c.tradeRequest].macAddress) {
                c.sendMessage(PlayerHandler.players[c.tradeRequest].playerName
                        + ":tradereq:");
                c.tradeRequest = 0;
            } else {
                c.sendMessage("You can not trade your self");
            }
        }
        if (c.tradeOtherDeclined) {
            if (PlayerHandler.players[c.tradeWith] != null) {
                c.sendMessage(PlayerHandler.players[c.tradeWith].playerName
                        + " declined the trade.");
            } else {
                c.sendMessage("Other player declined the trade.");
            }
            c.getPA().RemoveAllWindows();
            DeclineTrade();
            c.tradeOtherDeclined = false;
        }
        if (c.tradeWaitingTime > 0) {
            c.tradeWaitingTime--;
            if (c.tradeWaitingTime <= 0) {
                c.sendMessage("Trade request suspended.");
                resetTrade();
            }
        }
        if (c.AntiTradeScam) {
            c.getPA().sendFrame126("", 3431);
            c.AntiTradeScam = false;
        }
        if (c.tradeWith > 0) {
            if(c.macAddress == PlayerHandler.players[c.tradeWith].macAddress) {
                c.sendMessage("You can not trade your self");
                return;
            }
            if (PlayerHandler.players[c.tradeWith] != null) {
                if (c.tradeStatus == 5) {
                    if (PlayerHandler.players[c.tradeWith].TradeConfirmed) {
                        PlayerHandler.players[c.tradeWith].tradeStatus = 5;
                    }
                    resetTrade();
                } else {
                    int OtherStatus = PlayerHandler.players[c.tradeWith].tradeStatus;
                    if (OtherStatus == 1) {
                        PlayerHandler.players[c.tradeWith].tradeStatus = 2;
                        c.tradeStatus = 2;
                        AcceptTrade();
                        PlayerHandler.players[c.tradeWith].tradeWaitingTime = 0;
                        c.tradeWaitingTime = 0;
                    } else if (OtherStatus == 3) {
                        if (c.tradeStatus == 2) {
                            c.getPA().sendFrame126("Other player has accepted.", 3431);
                        } else if (c.tradeStatus == 3) {
                            TradeGoConfirm();
                        }
                    } else if (OtherStatus == 4) {
                        if (c.tradeStatus == 3) {
                            c.getPA().sendFrame126("Other player has accepted.", 3535);
                        } else if (c.tradeStatus == 4) {
                            ConfirmTrade();
                            if (PlayerHandler.players[c.tradeWith].TradeConfirmed) {
                                PlayerHandler.players[c.tradeWith].tradeStatus = 5;
                            }
                        }
                    }
                    if (c.tradeUpdateOther) {
                        c.resetOTItems(3416);
                        c.tradeUpdateOther = false;
                    }
                }
            } else {
                resetTrade();
            }
        }
        if (c.WanneTrade == 1) {
            if(c.macAddress == PlayerHandler.players[c.tradeRequest].macAddress)
                return;
            if (c.WanneTradeWith > PlayerHandler.maxPlayers) {
                resetTrade();
            } else if (PlayerHandler.players[c.WanneTradeWith] != null) {
                if (c.GoodDistance2(c.absX, c.absY,
                        PlayerHandler.players[c.WanneTradeWith].absX,
                        PlayerHandler.players[c.WanneTradeWith].absY, 1)) {
                    int tt1 = PlayerHandler.players[c.WanneTradeWith].tradeStatus;
                    int tt2 = c.tradeStatus;
                    if (tt1 <= 0
                            && tt2 <= 0
                            && PlayerHandler.players[c.WanneTradeWith].tradeWaitingTime == 0) {
                        if(c.macAddress == PlayerHandler.players[c.WanneTradeWith].macAddress)
                            return;
                        c.tradeWith = c.WanneTradeWith;
                        c.tradeWaitingTime = 40;
                        PlayerHandler.players[c.tradeWith].tradeRequest = c.playerId;
                        c.sendMessage("Sending trade request...");
                    } else if (tt1 <= 0
                            && tt2 <= 0
                            && PlayerHandler.players[c.WanneTradeWith].tradeWaitingTime > 0) {
                        c.tradeWith = c.WanneTradeWith;
                        c.tradeStatus = 1;
                        AcceptTrade();
                    }
                    c.WanneTrade = 0;
                    c.WanneTradeWith = 0;
                }
            } else {
                resetTrade();
            }
        }
        if (c.WanneTrade == 2) {
            try {
                if (c.WanneTradeWith > PlayerHandler.maxPlayers) {
                    resetTrade();
                } else if (PlayerHandler.players[c.WanneTradeWith] != null) {
                    if (c.GoodDistance2(c.absX, c.absY,
                            PlayerHandler.players[c.WanneTradeWith].absX,
                            PlayerHandler.players[c.WanneTradeWith].absY, 1)) {
                        if (PlayerHandler.players[c.WanneTradeWith].tradeWith == c.playerId
                                && PlayerHandler.players[c.WanneTradeWith].tradeWaitingTime > 0) {
                            if(c.macAddress == PlayerHandler.players[c.WanneTradeWith].macAddress) {
                                c.sendMessage("You can not trade your self");
                                return;
                            }
                            c.tradeWith = c.WanneTradeWith;
                            c.tradeStatus = 1;
                            AcceptTrade();
                        } else {
                            if(c.macAddress == PlayerHandler.players[c.WanneTradeWith].macAddress) {
                                c.sendMessage("You can not trade your self");
                                return;
                            }
                            c.tradeWith = c.WanneTradeWith;
                            c.tradeWaitingTime = 40;
                            PlayerHandler.players[c.tradeWith].tradeRequest = c.playerId;
                            c.sendMessage("Sending trade request...");
                        }
                        c.WanneTrade = 0;
                        c.WanneTradeWith = 0;
                    }
                } else {
                    resetTrade();
                }
            } catch (Exception e) {
                c.sendMessage("Ok Then... Your Nice.");
            }
        }
    }
    public void AcceptTrade() {
        c.getPA().sendFrame248(3323, 3321); // trading window + bag
        c.getPA().resetItems(3322);
        c.resetTItems(3415);
        c.resetOTItems(3416);
        c.getPA().sendFrame126("Trading With: "
                + PlayerHandler.players[c.tradeWith].playerName, 3417);
        c.getPA().sendFrame126("", 3431);
    }

    public void DeclineTrade() {
        c.secondTradeWindow = false;
        if (!c.hasAccepted) {
            for (int i = 0; i < c.playerTItems.length; i++) {
                if (c.playerTItems[i] > 0) {
                    if (c.tradeStatus < 4) {
                        fromTrade((c.playerTItems[i] - 1), i, c.playerTItemsN[i]);
                    }
                }
            }
        }
        c.getPA().resetItems(3214);
        resetTrade();
    }

    public void resetTrade() {
        c.tradeWith = 0;
        c.tradeWaitingTime = 0;
        c.tradeStatus = 0;
        c.tradeUpdateOther = false;
        c.tradeOtherDeclined = false;
        c.WanneTrade = 0;
        c.WanneTradeWith = 0;
        c.TradeConfirmed = false;
        for (int i = 0; i < c.playerTItems.length; i++) {
            c.playerTItems[i] = 0;
            c.playerTItemsN[i] = 0;
            c.playerOTItems[i] = 0;
            c.playerOTItemsN[i] = 0;
        }
    }

    public void ConfirmTrade() {
        if (!c.TradeConfirmed) {
            c.getPA().RemoveAllWindows();
            for (int i = 0; i < c.playerOTItems.length; i++) {
                if (c.playerOTItems[i] > 0) {
                    c.addItem((c.playerOTItems[i] - 1), c.playerOTItemsN[i]);
                    BufferedWriter bw = null;
                    try {
                        bw = new BufferedWriter(new FileWriter(
                                "./Data/logs/trades.txt", true));
                        bw.write(PlayerHandler.players[c.tradeWith].playerName
                                + " trades item: " + (c.playerOTItems[i] - 1)
                                + " amount: " + c.playerOTItemsN[i] + " with "
                                + c.playerName);
                        bw.newLine();
                        bw.flush();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    } finally {
                        if (bw != null) {
                            try {
                                bw.close();
                            } catch (IOException ioe2) {
                                c.sendMessage("Error logging trade!");
                            }
                        }
                    }
                    try {
                        bw = new BufferedWriter(
                                new FileWriter(
                                        "./Data/logs/trades.txt",
                                        true));
                        bw.write(PlayerHandler.players[c.tradeWith].playerName
                                + " trades item: " + (c.playerOTItems[i] - 1)
                                + " amount: " + c.playerOTItemsN[i] + " with "
                                + c.playerName);
                        bw.newLine();
                        bw.flush();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    } finally {
                        if (bw != null) {
                            try {
                                bw.close();
                            } catch (IOException ioe2) {
                                c.sendMessage("Error logging trade!");
                            }
                        }
                    }
                }
            }
            c.getPA().resetItems(3214);
            Discord.writeTradeMessages(PlayerHandler.players[c.tradeWith].playerName
                    + " trades item: " + (c.playerOTItems[c.i] - 1)
                    + " amount: " + c.playerOTItemsN[c.i] + " with "
                    + c.playerName);
            c.TradeConfirmed = true;
        }
    }

    public void TradeGoConfirm() {
        c.secondTradeWindow = true;
        c.getPA().sendFrame248(3443, 3213); // trade confirm + normal bag
        c.getPA().resetItems(3214);
        String SendTrade = "Absolutely nothing!";
        String SendAmount = "";
        int Count = 0;
        for (int i = 0; i < c.playerTItems.length; i++) {
            if (c.playerTItems[i] > 0) {
                if (c.playerTItemsN[i] >= 1000 && c.playerTItemsN[i] < 1000000) {
                    SendAmount = "@cya@" + (c.playerTItemsN[i] / 1000)
                            + "K @whi@(" + c.playerTItemsN[i] + ")";
                } else if (c.playerTItemsN[i] >= 1000000) {
                    SendAmount = "@gre@" + (c.playerTItemsN[i] / 1000000)
                            + " million @whi@(" + c.playerTItemsN[i] + ")";
                } else {
                    SendAmount = String.valueOf(c.playerTItemsN[i]);
                }
                if (Count == 0) {
                    SendTrade = c.getItemName((c.playerTItems[i] - 1));
                } else {
                    SendTrade = SendTrade + "\\n"
                            + c.getItemName((c.playerTItems[i] - 1));
                }
                if (Item.itemIsNote[(c.playerTItems[i] - 1)]
                        || Item.itemStackable[(c.playerTItems[i] - 1)]) {
                    SendTrade = SendTrade + " x " + SendAmount;
                }
                Count++;
            }
        }
        c.getPA().sendFrame126(SendTrade, 3557);
        SendTrade = "Absolutely nothing!";
        SendAmount = "";
        Count = 0;
        for (int i = 0; i < c.playerOTItems.length; i++) {
            if (c.playerOTItems[i] > 0) {
                if (c.playerOTItemsN[i] >= 1000 && c.playerOTItemsN[i] < 1000000) {
                    SendAmount = "@cya@" + (c.playerOTItemsN[i] / 1000)
                            + "K @whi@(" + c.playerOTItemsN[i] + ")";
                } else if (c.playerOTItemsN[i] >= 1000000) {
                    SendAmount = "@gre@" + (c.playerOTItemsN[i] / 1000000)
                            + " million @whi@(" + c.playerOTItemsN[i] + ")";
                } else {
                    SendAmount = String.valueOf(c.playerOTItemsN[i]);
                }
                if (Count == 0) {
                    SendTrade = c.getItemName((c.playerOTItems[i] - 1));
                } else {
                    SendTrade = SendTrade + "\\n"
                            + c.getItemName((c.playerOTItems[i] - 1));
                }
                if (Item.itemIsNote[(c.playerOTItems[i] - 1)]
                        || Item.itemStackable[(c.playerOTItems[i] - 1)]) {
                    SendTrade = SendTrade + " x " + SendAmount;
                }
                Count++;
            }
        }
        c.getPA().sendFrame126(SendTrade, 3558);
    }

    public boolean fromTrade(int itemID, int fromSlot, int amount) {
        if (c.secondTradeWindow) {
            return false;
        }
        if (amount > 0 && (itemID + 1) == c.playerTItems[fromSlot]) {
            if (amount > c.playerTItemsN[fromSlot]) {
                amount = c.playerTItemsN[fromSlot];
            }
            c.addItem((c.playerTItems[fromSlot] - 1), amount);
            if (amount == c.playerTItemsN[fromSlot]) {
                c.playerTItems[fromSlot] = 0;
                PlayerHandler.players[c.tradeWith].playerOTItems[fromSlot] = 0;
            }
            c.playerTItemsN[fromSlot] -= amount;
            PlayerHandler.players[c.tradeWith].playerOTItemsN[fromSlot] -= amount;
            c.getPA().resetItems(3322);
            c.resetTItems(3415);
            PlayerHandler.players[c.tradeWith].tradeUpdateOther = true;
            if (PlayerHandler.players[c.tradeWith].tradeStatus == 3) {
                PlayerHandler.players[c.tradeWith].tradeStatus = 2;
                PlayerHandler.players[c.tradeWith].AntiTradeScam = true;
                c.getPA().sendFrame126("", 3431);
            }
            return true;
        }
        return false;
    }

    public boolean tradeItem(int itemID, int fromSlot, int amount) {
        if (c.tradeWith > 0) {
            if (PlayerHandler.players[c.tradeWith] == null) {
                DeclineTrade();
                c.sendMessage("FORCED DECLINE BY SERVER!");
                return false;
            }
        } else {
            DeclineTrade();
            c.sendMessage("FORCED DECLINE BY SERVER!");
            return false;
        }
        if (amount > 0 && itemID == (c.playerItems[fromSlot] - 1)) {
            if (amount > c.playerItemsN[fromSlot]) {
                amount = c.playerItemsN[fromSlot];
            }
            boolean IsInTrade = false;
            for (int i = 0; i < c.playerTItems.length; i++) {
                if (c.playerTItems[i] == c.playerItems[fromSlot]) {
                    if (Item.itemStackable[(c.playerItems[fromSlot] - 1)]
                            || Item.itemIsNote[(c.playerItems[fromSlot] - 1)]) {
                        c.playerTItemsN[i] += amount;
                        PlayerHandler.players[c.tradeWith].playerOTItemsN[i] += amount;
                        IsInTrade = true;
                        break;
                    }
                }
            }
            if (!IsInTrade) {
                for (int i = 0; i < c.playerTItems.length; i++) {
                    if (c.playerTItems[i] <= 0) {
                        c.playerTItems[i] = c.playerItems[fromSlot];
                        c.playerTItemsN[i] = amount;
                        PlayerHandler.players[c.tradeWith].playerOTItems[i] = c.playerItems[fromSlot];
                        PlayerHandler.players[c.tradeWith].playerOTItemsN[i] = amount;
                        break;
                    }
                }
            }
            if (amount == c.playerItemsN[fromSlot]) {
                c.playerItems[fromSlot] = 0;
            }
            c.playerItemsN[fromSlot] -= amount;
            c.getPA().resetItems(3322);
            c.resetTItems(3415);
            PlayerHandler.players[c.tradeWith].tradeUpdateOther = true;
            if (PlayerHandler.players[c.tradeWith].tradeStatus == 3) {
                PlayerHandler.players[c.tradeWith].tradeStatus = 2;
                PlayerHandler.players[c.tradeWith].AntiTradeScam = true;
                c.getPA().sendFrame126("", 3431);
            }
            return true;
        }
        return false;
    }

}
