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
            if(c.MyShopID == 240) {
                c.sendMessage(
                        "You cannot sell items in this store.");
                return false;
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
    /**
     * Returns the price in points for a given item in Shop 240.
     * @param itemID The ID of the item.
     * @return The price in points, or 0 if the item cannot be bought with points.
     */
    public int getPrestigePointPrice(int itemID) {
        switch (itemID) {
            case 24090:
                return 50;
            case 24091:
                return 50;
            case 24092:
                return 50;
            case 24093:
                return 50;
            case 24094:
                return 50;
            case 24095:
                return 50;
            // Add more items here
            default:
                return 0; // Items with no price cannot be bought
        }
    }

    public boolean buyItem(int itemID, int fromSlot, int amount) {
        if (amount <= 0) return false;

        // Validate slot
        if (fromSlot >= ShopHandler.ShopItemsN[c.MyShopID].length) {
            c.sendMessage("There was a problem buying that item, please report it to staff!");
            return false;
        }

        // Limit amount to available stock
        if (amount > ShopHandler.ShopItemsN[c.MyShopID][fromSlot]) {
            amount = ShopHandler.ShopItemsN[c.MyShopID][fromSlot];
        }

        int boughtAmount = 0;
        int TotPrice2 = 0;

        for (int i = 0; i < amount; i++) {
            int price = 0;
            int currencyItemID = 0;

            // Determine price and currency based on shop
            switch (c.MyShopID) {
                case 99: // Trading Stick shop
                    currencyItemID = 6306;
                    price = TotPrice2 = (int) Math.floor(c.GetItemShopValue(itemID, 0, fromSlot));
                    break;

                case 113: // Tokkul shop
                case 114:
                case 115:
                    currencyItemID = 6529;
                    price = TotPrice2 = (int) Math.floor(c.GetItemShopValue(itemID, 0, fromSlot));
                    break;

                case 240: // Prestige points shop
                    currencyItemID = -1; // flag to indicate prestigePoints
                    price = TotPrice2 = getPrestigePointPrice(itemID);
                    if (price <= 0) {
                        c.sM("This item cannot be purchased with prestige points.");
                        return false;
                    }
                    break;

                default: // Normal coin shop
                    currencyItemID = 995; // Coins
                    price = TotPrice2 = (int) Math.floor(c.GetItemShopValue(itemID, 0, fromSlot));
                    if (price <= 1) price = (int) (price * 1.66);
                    break;
            }

            // Check if player has enough currency
            boolean hasCurrency;
            if (currencyItemID == -1) { // prestigePoints shop
                hasCurrency = c.prestigePoints >= TotPrice2;
            } else {
                hasCurrency = c.playerHasItem(currencyItemID, TotPrice2);
            }

            if (!hasCurrency) {
                String currencyName = currencyItemID == 995 ? "coins" :
                        currencyItemID == 6306 ? "Trading Sticks" :
                                currencyItemID == 6529 ? "Tokkul" : "prestige points";
                c.sM("You don't have enough " + currencyName + ".");
                break;
            }

            // Check for inventory space
            if (c.freeSlots() <= 0) {
                c.sM("Not enough space in your inventory.");
                break;
            }

            // Deduct currency and give item
            if (currencyItemID == -1) { // prestigePoints
                c.prestigePoints -= TotPrice2;
            } else {
                c.deleteItem2(currencyItemID, TotPrice2);
            }
            c.addItem(itemID, 1);
            boughtAmount++;

            // Update shop stock
            ShopHandler.ShopItemsN[c.MyShopID][fromSlot]--;
            ShopHandler.ShopItemsDelay[c.MyShopID][fromSlot] = 0;
            if (ShopHandler.ShopItemsN[c.MyShopID][fromSlot] <= 0) {
                ShopHandler.ShopItems[c.MyShopID][fromSlot] = 0;
            }
        }

        // Update shop interface
        c.getPA().resetItems(3823);
        resetShop(c.MyShopID);
        UpdatePlayerShop();

        return boughtAmount > 0;
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
