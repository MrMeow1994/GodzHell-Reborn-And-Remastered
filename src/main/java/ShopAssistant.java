public class ShopAssistant {

    private final client c;

    public ShopAssistant(client client) {
        this.c = client;
    }

    public void openUpShop(int ShopID) {
        // setScrollHeight(ShopID);
        // resetScrollPosition(64015);
        c.getPA().sendFrame126(ShopHandler.ShopName[ShopID], 64003);
        c.getPA().sendFrame248(64000, 3822);
        c.getPA().resetItems(3823);
        resetShop(ShopID);
        c.IsShopping = true;
        c.MyShopID = ShopID;
    }
    public void resetShop(int ShopID) {
        int TotalItems = 0;

        for (int i = 0; i < ShopHandler.MaxShopItems; i++) {
            if (ShopHandler.ShopItems[ShopID][i] > 0) {
                TotalItems++;
            }
        }
        if (TotalItems > ShopHandler.MaxShopItems) {
            TotalItems = ShopHandler.MaxShopItems;
        }
        c.getOutStream().createFrameVarSizeWord(53);
        c.getOutStream().writeWord(64016);
        c.getOutStream().writeWord(TotalItems);
        int TotalCount = 0;

        for (int i = 0; i < ShopHandler.ShopItems.length; i++) {
            if (ShopHandler.ShopItems[ShopID][i] > 0
                    || i <= ShopHandler.ShopItemsStandard[ShopID]) {
                if (ShopHandler.ShopItemsN[ShopID][i] > 254) {
                    c.getOutStream().writeByte(255); // item's stack count. if over 254, write byte 255
                    c.getOutStream().writeDWord_v2(
                            ShopHandler.ShopItemsN[ShopID][i]); // and then the real value with writeDWord_v2
                } else {
                    c.getOutStream().writeByte(ShopHandler.ShopItemsN[ShopID][i]);
                }
                if (ShopHandler.ShopItems[ShopID][i] > Config.MAX_ITEMS
                        || ShopHandler.ShopItems[ShopID][i] < 0) {
                    ShopHandler.ShopItems[ShopID][i] = Config.MAX_ITEMS;
                }
                c.getOutStream().writeWordBigEndianA(
                        ShopHandler.ShopItems[ShopID][i]); // item id
                TotalCount++;
            }
            if (TotalCount > TotalItems) {
                break;
            }
        }
        c.getOutStream().endFrameVarSizeWord();
    }
    public boolean sellItem(int itemID, int fromSlot, int amount) {
        ItemCacheDefinition def = ItemCacheDefinition.forID(itemID);
        if (amount > 0 && itemID == (c.playerItems[fromSlot] - 1)) {
            if (ShopHandler.ShopSModifier[c.MyShopID] > 1) {
                boolean IsIn = false;

                for (int i = 0; i
                        <= ShopHandler.ShopItemsStandard[c.MyShopID]; i++) {
                    if (itemID
                            == (ShopHandler.ShopItems[c.MyShopID][i] - 1)) {
                        IsIn = true;
                        break;
                    }
                }
                if (!IsIn) {
                    c.sendMessage(
                            "You cannot sell " + c.GetItemName(itemID)
                                    + " in this store.");
                    return false;
                }
            }
            if (!def.istradable()) {
                c.sendMessage("I cannot sell " + c.GetItemName(itemID) + ".");
                return false;
            }
            if (amount > c.playerItemsN[fromSlot]
                    && (def.isNoted()
                    || def.isStackable())) {
                amount = c.playerItemsN[fromSlot];
            } else if (amount > c.getItemAmount(itemID)
                    && !def.isNoted()
                    && !def.isStackable()) {
                amount = c.getItemAmount(itemID);
            }
            double ShopValue;
            double TotPrice;
            int TotPrice2;
            int Overstock;

            for (int i = amount; i > 0; i--) {
                TotPrice2 = (int) Math.floor(
                        c.GetItemShopValue(itemID, 1, fromSlot));
                if (c.MyShopID != 99 && c.MyShopID != 113 && c.MyShopID != 114 && c.MyShopID != 115 && c.MyShopID != 239) {
                    if (c.freeSlots() >= 1) {
                        if (!def.isNoted()) {
                            c.deleteItem2(itemID, 1);
                        } else {
                            c.deleteItem2(itemID, 1);
                        }
                        c.addItem(ItemIDs.COINS, TotPrice2);
                        addShopItem(itemID, 1);
                    } else {
                        c.sendMessage("Not enough space in your inventory.");
                        break;
                    }
                }
                else if (c.MyShopID != 99 && c.MyShopID != 113 && c.MyShopID != 114 && c.MyShopID != 115 && c.MyShopID == 239) {
                    if (c.freeSlots() >= 1) {
                        if (!def.isNoted()) {
                            c.deleteItem2(itemID, 1);
                        } else {
                            c.deleteItem2(itemID, 1);
                        }
                        c.addItem(ItemIDs.COINS, TotPrice2 * 9);
                        addShopItem(itemID, 1);
                    } else {
                        c.sendMessage("Not enough space in your inventory.");
                        break;
                    }
                }else if (c.MyShopID != 99  && c.MyShopID != 239 && c.MyShopID == 113 && c.MyShopID == 114 && c.MyShopID == 115) {
                    if (c.freeSlots() >= 1) {
                        if (!def.isNoted()) {
                            c.deleteItem2(itemID, 1);
                        } else {
                            c.deleteItem2(itemID, 1);
                        }
                        c.addItem(6529, TotPrice2);
                        addShopItem(itemID, 1);
                    } else {
                        c.sendMessage("Not enough space in your inventory.");
                        break;
                    }
                }
            }
            c.getPA().resetItems(3823);
            resetShop(c.MyShopID);
            UpdatePlayerShop();
            return true;
        }
        return true;
    }

    public boolean buyItem(int itemID, int fromSlot, int amount) {
        ItemCacheDefinition def = ItemCacheDefinition.forID(itemID);
        if (amount > 0) {
            if (fromSlot >= ShopHandler.ShopItemsN[c.MyShopID].length) {
                c.sendMessage("There was a problem buying that item, please report it to staff!");
                return false;
            }
            if (amount > ShopHandler.ShopItemsN[c.MyShopID][fromSlot]) {
                amount = ShopHandler.ShopItemsN[c.MyShopID][fromSlot];
            }
            double ShopValue;
            double TotPrice;
            int TotPrice2;
            int Overstock;
            int Slot = 0;
            int Slot1 = 0;
            int Slot2 = 0;

            int boughtAmount = 0;
            for (int i = amount; i > 0; i--) {
                TotPrice2 = (int) Math.floor(
                        c.GetItemShopValue(itemID, 0, fromSlot));
                Slot = c.GetItemSlot(ItemIDs.COINS);
                Slot1 = c.GetItemSlot(6306);
                Slot2 = c.GetItemSlot(6529);
                if (Slot == -1 && c.MyShopID != 99 && c.MyShopID != 113 && c.MyShopID != 114 && c.MyShopID != 115) {
                    c.sendMessage("You don't have enough coins.");
                    break;
                }
                if (Slot1 == -1 && c.MyShopID == 99 && c.MyShopID != 113 && c.MyShopID != 114 && c.MyShopID != 115) {
                    c.sendMessage("You don't have enough Trading Sticks.");
                    break;
                }
                if (Slot2 == -1 && c.MyShopID != 99 && c.MyShopID == 113 && c.MyShopID == 114 && c.MyShopID == 115) {
                    c.sendMessage("You don't have enough Tokkul.");
                    break;
                }
                if (TotPrice2 <= 1) {
                    TotPrice2 = (int) Math.floor(c.GetItemShopValue(itemID, 0, fromSlot));
                    TotPrice2 *= 1.66;
                }
                if (c.MyShopID != 99 && c.MyShopID != 113 && c.MyShopID != 114 && c.MyShopID != 115) {
                    if (c.playerHasItem(995, TotPrice2) || TotPrice2 == 0) {
                        if (c.freeSlots() > 0) {
                            c.deleteItem2(ItemIDs.COINS, TotPrice2 * amount);
                            c.addItem(itemID, 1);
                            boughtAmount++;
                            ShopHandler.ShopItemsN[c.MyShopID][fromSlot]--; // decrement by 1
                            ShopHandler.ShopItemsDelay[c.MyShopID][fromSlot] = 0;
                            // remove from shop if empty
                            if (ShopHandler.ShopItemsN[c.MyShopID][fromSlot] <= 0) {
                                ShopHandler.ShopItems[c.MyShopID][fromSlot] = 0;
                            }
                        } else {
                            c.sendMessage("Not enough space in your inventory.");
                            break;
                        }
                    } else {
                        c.sendMessage("You don't have enough coins.");
                        break;
                    }
                } else if (c.MyShopID == 99 && c.MyShopID != 113 && c.MyShopID != 114 && c.MyShopID != 115) {
                    if (c.playerHasItem(6306, TotPrice2) || TotPrice2 == 0) {
                        if (c.freeSlots() > 0) {
                            c.deleteItem2(6306, TotPrice2 * amount);
                            c.addItem(itemID, amount);
                            ShopHandler.ShopItemsN[c.MyShopID][fromSlot] -= 1;
                            ShopHandler.ShopItemsDelay[c.MyShopID][fromSlot] = 0;
                            if ((fromSlot + 1)
                                    > ShopHandler.ShopItemsStandard[c.MyShopID]) {
                                ShopHandler.ShopItems[c.MyShopID][fromSlot] = 0;
                            }
                        } else {
                            c.sM("Not enough space in your inventory.");
                            break;
                        }
                    } else {
                        c.sM("Not enough Trading ticks for this item.");
                        break;
                    }
                } else if (c.MyShopID == 113 && c.MyShopID != 99) {
                    if (c.playerHasItem(6529, TotPrice2) || TotPrice2 == 0) {
                        if (c.freeSlots() > 0) {
                            c.deleteItem2(6529, TotPrice2 * amount);
                            c.addItem(itemID, amount);
                            ShopHandler.ShopItemsN[c.MyShopID][fromSlot] -= 1;
                            ShopHandler.ShopItemsDelay[c.MyShopID][fromSlot] = 0;
                            if ((fromSlot + 1)
                                    > ShopHandler.ShopItemsStandard[c.MyShopID]) {
                                ShopHandler.ShopItems[c.MyShopID][fromSlot] = 0;
                            }
                        } else {
                            c.sM("Not enough space in your inventory.");
                            break;
                        }
                    } else {
                        c.sM("Not enough Tokkul for this item.");
                        break;
                    }
                } else if (c.MyShopID == 114 && c.MyShopID != 99) {
                    if (c.playerHasItem(6529, TotPrice2) || TotPrice2 == 0) {
                        if (c.freeSlots() > 0) {
                            c.deleteItem2(6529, TotPrice2 * amount);
                            c.addItem(itemID, amount);
                            ShopHandler.ShopItemsN[c.MyShopID][fromSlot] -= 1;
                            ShopHandler.ShopItemsDelay[c.MyShopID][fromSlot] = 0;
                            if ((fromSlot + 1)
                                    > ShopHandler.ShopItemsStandard[c.MyShopID]) {
                                ShopHandler.ShopItems[c.MyShopID][fromSlot] = 0;
                            }
                        } else {
                            c.sM("Not enough space in your inventory.");
                            break;
                        }
                    } else {
                        c.sM("Not enough Tokkul for this item.");
                        break;
                    }
                } else if (c.MyShopID == 115 && c.MyShopID != 99) {
                    if (c.playerHasItem(6529, TotPrice2) || TotPrice2 == 0) {
                        if (c.freeSlots() > 0) {
                            c.deleteItem2(6529, TotPrice2 * amount);
                            c.addItem(itemID, amount);
                            ShopHandler.ShopItemsN[c.MyShopID][fromSlot] -= 1;
                            ShopHandler.ShopItemsDelay[c.MyShopID][fromSlot] = 0;
                            if ((fromSlot + 1)
                                    > ShopHandler.ShopItemsStandard[c.MyShopID]) {
                                ShopHandler.ShopItems[c.MyShopID][fromSlot] = 0;
                            }
                        } else {
                            c.sM("Not enough space in your inventory.");
                            break;
                        }
                    } else {
                        c.sM("Not enough Tokkul for this item.");
                        break;
                    }
                }

            }
            c.getPA().resetItems(3823);
            resetShop(c.MyShopID);
            UpdatePlayerShop();
            return true;
        }
        return false;
    }

    public void UpdatePlayerShop() {
        for (int i = 1; i < PlayerHandler.maxPlayers; i++) {
            if (PlayerHandler.players[i] != null) {
                if (PlayerHandler.players[i].IsShopping
                        && PlayerHandler.players[i].MyShopID == c.MyShopID
                        && i != c.index) {
                    PlayerHandler.players[i].UpdateShop = true;
                }
            }
        }
    }

    public boolean addShopItem(int itemID, int amount) {
        boolean Added = false;
        ItemCacheDefinition def = ItemCacheDefinition.forID(itemID);

        if (amount <= 0) {
            return false;
        }
        if (def.isNoted()) {
            itemID = itemID - 1;
        }
        for (int i = 0; i < ShopHandler.ShopItems.length; i++) {
            if ((ShopHandler.ShopItems[c.MyShopID][i] - 1) == itemID) {
                ShopHandler.ShopItemsN[c.MyShopID][i] += amount;
                Added = true;
            }
        }
        if (!Added) {
            for (int i = 0; i < ShopHandler.ShopItems.length; i++) {
                if (ShopHandler.ShopItems[c.MyShopID][i] == 0) {
                    ShopHandler.ShopItems[c.MyShopID][i] = (itemID + 1);
                    ShopHandler.ShopItemsN[c.MyShopID][i] = amount;
                    ShopHandler.ShopItemsDelay[c.MyShopID][i] = 0;
                    break;
                }
            }
        }
        return true;
    }
}
