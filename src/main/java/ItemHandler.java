// Scape - The Scape Developers Team
// Phate, AridTag, Dan, DrBone420, Hero

import java.io.*;
import java.util.*;

public class ItemHandler {

	private static final int MAX_GLOBAL_ITEMS = 15000;
	private static final int ITEM_LIST_SIZE = Config.MAX_ITEMS;

	public static final int showItemTimer = 60;
	public static final int hideItemTimer = 60;

	public static int[] globalItemController = new int[MAX_GLOBAL_ITEMS];
	public static int[] globalItemID = new int[MAX_GLOBAL_ITEMS];
	public static int[] globalItemX = new int[MAX_GLOBAL_ITEMS];
	public static int[] globalItemY = new int[MAX_GLOBAL_ITEMS];
	public static int[] globalItemAmount = new int[MAX_GLOBAL_ITEMS];
	public static boolean[] globalItemStatic = new boolean[MAX_GLOBAL_ITEMS];
	public static int[] globalItemTicks = new int[MAX_GLOBAL_ITEMS];

	public static int DropItemCount = 0;
	public static int MaxDropItems = MAX_GLOBAL_ITEMS;
	public static int MaxListedItems = ITEM_LIST_SIZE;
	public static int MaxDropShowDelay = 120;
	public static int SDID = 90;

	public static int[] DroppedItemsID = new int[MaxDropItems];
	public static int[] DroppedItemsX = new int[MaxDropItems];
	public static int[] DroppedItemsY = new int[MaxDropItems];
	public static int[] DroppedItemsN = new int[MaxDropItems];
	public static int[] DroppedItemsH = new int[MaxDropItems];
	public static int[] DroppedItemsDDelay = new int[MaxDropItems];
	public static int[] DroppedItemsSDelay = new int[MaxDropItems];
	public static int[] DroppedItemsDropper = new int[MaxDropItems];
	public static int[] DroppedItemsDeletecount = new int[MaxDropItems];
	public static boolean[] DroppedItemsAlwaysDrop = new boolean[MaxDropItems];

	public ItemList[] ItemList = new ItemList[MaxListedItems];

	public ItemHandler() {
		Arrays.fill(globalItemController, 0);
		Arrays.fill(globalItemID, 0);
		Arrays.fill(globalItemX, 0);
		Arrays.fill(globalItemY, 0);
		Arrays.fill(globalItemAmount, 0);
		Arrays.fill(globalItemTicks, 0);
		Arrays.fill(globalItemStatic, false);

		for (int i = 0; i < MaxDropItems; i++) ResetItem(i);
		for (int i = 0; i < MaxListedItems; i++) ItemList[i] = null;

		loadItemList("./Data/cfg/item.cfg");
		loadDrops("./Data/cfg/drops.cfg");
	}

	public void process() {
		for (int i = 0; i < MAX_GLOBAL_ITEMS; i++) {
			if (globalItemID[i] == 0) continue;
			globalItemTicks[i]++;

			if (globalItemTicks[i] == showItemTimer && !globalItemStatic[i]) {
				createItemAll(globalItemID[i], globalItemX[i], globalItemY[i], globalItemAmount[i], globalItemController[i]);
			}

			if (globalItemTicks[i] >= (showItemTimer + hideItemTimer)) {
				if (!globalItemStatic[i]) {
					removeItemAll(globalItemID[i], globalItemX[i], globalItemY[i]);
					globalItemID[i] = 0;
				}
			}
		}
	}

	public static boolean itemExists(int itemID, int itemX, int itemY) {
		for (int i = 0; i < MAX_GLOBAL_ITEMS; i++) {
			if (globalItemID[i] == itemID && globalItemX[i] == itemX && globalItemY[i] == itemY)
				return true;
		}
		return false;
	}

	public static int itemAmount(int itemID, int itemX, int itemY) {
		for (int i = 0; i < MAX_GLOBAL_ITEMS; i++) {
			if (globalItemID[i] == itemID && globalItemX[i] == itemX && globalItemY[i] == itemY)
				return globalItemAmount[i];
		}
		return 0;
	}

	public static void addItem(int itemID, int itemX, int itemY, int itemAmount, int itemController, boolean itemStatic) {
		for (int i = 0; i < MAX_GLOBAL_ITEMS; i++) {
			if (globalItemID[i] == 0) {
				globalItemController[i] = itemController;
				globalItemID[i] = itemID;
				globalItemX[i] = itemX;
				globalItemY[i] = itemY;
				globalItemAmount[i] = itemAmount;
				globalItemStatic[i] = itemStatic;
				globalItemTicks[i] = 0;

				if (itemController > 0)
					spawnItem(itemID, itemX, itemY, itemAmount, itemController);
				break;
			}
		}
	}

	public static void spawnItem(int itemID, int itemX, int itemY, int itemAmount, int playerFor) {
		if (PlayerHandler.players[playerFor] instanceof client) {
			client person = (client) PlayerHandler.players[playerFor];
			person.createGroundItem(itemID, itemX, itemY, itemAmount);
		}

	}

	public static void removeItem(int itemID, int itemX, int itemY, int itemAmount) {
		for (int i = 0; i < MAX_GLOBAL_ITEMS; i++) {
			if (globalItemID[i] == itemID && globalItemX[i] == itemX && globalItemY[i] == itemY && globalItemAmount[i] == itemAmount) {
				removeItemAll(itemID, itemX, itemY);
				globalItemController[i] = 0;
				globalItemID[i] = 0;
				globalItemX[i] = 0;
				globalItemY[i] = 0;
				globalItemAmount[i] = 0;
				globalItemTicks[i] = 0;
				globalItemStatic[i] = false;
			}
		}
	}

	public static void createItemAll(int itemID, int itemX, int itemY, int itemAmount, int itemController) {
		for (Player p : server.playerHandler.players) {
			if (p instanceof client) {
				client person = (client) p;
				if (person.playerName != null && person.playerId != itemController) {
					if (person.distanceToPoint(itemX, itemY) <= 60) {
						person.createGroundItem(itemID, itemX, itemY, itemAmount);
					}
				}
			}
		}
	}

	public static void removeItemAll(int itemID, int itemX, int itemY) {
		for (Player p : server.playerHandler.players) {
			if (p instanceof client) {
				client person = (client) p;
				if (person.playerName != null) {
					if (person.distanceToPoint(itemX, itemY) <= 60) {
						person.removeGroundItem(itemX, itemY, itemID);
					}
				}
			}
		}
	}


	public void ResetItem(int index) {
		DroppedItemsID[index] = -1;
		DroppedItemsX[index] = -1;
		DroppedItemsY[index] = -1;
		DroppedItemsN[index] = -1;
		DroppedItemsH[index] = -1;
		DroppedItemsDDelay[index] = -1;
		DroppedItemsSDelay[index] = 0;
		DroppedItemsDropper[index] = -1;
		DroppedItemsDeletecount[index] = 0;
		DroppedItemsAlwaysDrop[index] = false;
	}

	public boolean loadDrops(String FileName) {
		try (BufferedReader characterfile = new BufferedReader(new FileReader("./" + FileName))) {
			String line;
			while ((line = characterfile.readLine()) != null) {
				line = line.trim();
				if (line.equals("[ENDOFDROPLIST]")) return true;
				int spot = line.indexOf("=");
				if (spot > -1) {
					String token = line.substring(0, spot).trim();
					String[] token3 = line.substring(spot + 1).trim().replaceAll("\t+", "\t").split("\t");
					if (token.equals("drop")) {
						int id = Integer.parseInt(token3[0]);
						int x = Integer.parseInt(token3[1]);
						int y = Integer.parseInt(token3[2]);
						int amount = Integer.parseInt(token3[3]);
						int height = Integer.parseInt(token3[4]);
						for (int i = 0; i < MAX_GLOBAL_ITEMS; i++) {
							createItemAll(id, x, y, amount, globalItemController[i]);
						}
					}
				}
			}
		} catch (IOException e) {
			misc.println(FileName + ": error loading file.");
			return false;
		}
		return false;
	}

	public void newItemList(int ItemId, String ItemName, String ItemDescription, double ShopValue, double LowAlch, double HighAlch, int Bonuses[]) {
		int slot = -1;
		for (int i = 0; i < 29999; i++) {
			if (ItemList[i] == null) {
				slot = i;
				break;
			}
		}
		if (slot == -1) return;
		ItemList newItemList = new ItemList(ItemId);
		newItemList.itemName = ItemName;
		newItemList.itemDescription = ItemDescription;
		newItemList.ShopValue = ShopValue;
		newItemList.LowAlch = LowAlch;
		newItemList.HighAlch = HighAlch;
		newItemList.Bonuses = Bonuses;
		ItemList[slot] = newItemList;
	}

	public boolean loadItemList(String FileName) {
		try (BufferedReader characterfile = new BufferedReader(new FileReader("./" + FileName))) {
			String line;
			while ((line = characterfile.readLine()) != null) {
				line = line.trim();
				if (line.equals("[ENDOFITEMLIST]")) return true;
				int spot = line.indexOf("=");
				if (spot > -1) {
					String token = line.substring(0, spot).trim();
					String[] token3 = line.substring(spot + 1).trim().replaceAll("\t+", "\t").split("\t");
					if (token.equals("item")) {
						int[] Bonuses = new int[12];
						for (int i = 0; i < 12 && (6 + i) < token3.length; i++) {
							Bonuses[i] = Integer.parseInt(token3[6 + i]);
						}
						newItemList(
								Integer.parseInt(token3[0]),
								token3[1].replaceAll("_", " "),
								token3[2].replaceAll("_", " "),
								Double.parseDouble(token3[4]),
								Double.parseDouble(token3[4]),
								Double.parseDouble(token3[6]),
								Bonuses
						);
					}
				}
			}
		} catch (IOException e) {
			misc.println(FileName + ": error loading file.");
			return false;
		}
		return false;
	}
}
