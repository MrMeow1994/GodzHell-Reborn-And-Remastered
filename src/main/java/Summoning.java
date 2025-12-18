public class Summoning {

    /* =======================================================
       SUMMON FAMILIAR
       ======================================================= */

	public static void summonFamiliar(client player, int pouchItemId, int slot) {

		SummoningFamiliar familiar = SummoningFamiliar.forPouch(pouchItemId);
		if (familiar == null) {
			return;
		}
	if(player.playerLevel[player.playerSummoning] >= familiar.getSummoninglevel()) {

		// Hard guard – mirrors pet system
		if (player.hasNpc) {
			player.sM("You already have a familiar.");
			return;
		}

		// Remove pouch
		int amount = player.playerItemsN[slot];
		if (amount <= 0) {
			return;
		}

		player.deleteItem(pouchItemId, slot, amount);

		int spawnX = player.absX;
		int spawnY = player.absY;

		// Same safe-adjacent logic as pets
		if (Region.getClipping(spawnX - 1, spawnY, player.heightLevel, -1, 0)) {
			spawnX--;
		} else if (Region.getClipping(spawnX + 1, spawnY, player.heightLevel, 1, 0)) {
			spawnX++;
		} else if (Region.getClipping(spawnX, spawnY - 1, player.heightLevel, 0, -1)) {
			spawnY--;
		} else if (Region.getClipping(spawnX, spawnY + 1, player.heightLevel, 0, 1)) {
			spawnY++;
		}
		server.npcHandler.spawnNpc3(
				player,
				familiar.getNpcId(),
				spawnX,
				spawnY,
				player.heightLevel,
				0,
				100,
				25,
				200,
				200,
				false,
				false,
				true
		);

		player.hasNpc = true;
		player.summonId = familiar.getNpcId();
		player.activeFamiliar = familiar; // store enum ref if you want
		player.summoningPoints -= familiar.getSummoninglevel() * 10 / 10;
		player.playerLevel[player.playerSummoning] -= familiar.getSummoninglevel() * 10 / 10;
		player.refreshSkill(player.playerSummoning);
		// Find the baby NPC and transform it in-place
		for (int i = 0; i < server.npcHandler.npcs.length; i++) {
			NPC npc = server.npcHandler.npcs[i];
			if (npc == null) continue;
			if (npc.npcType == familiar.getNpcId() && npc.summonedBy == player.index) {
				npc.gfx0(1315);
			}
		}
		player.startSummoningDrain(familiar);

		player.sM("You summon a familiar.");
		player.savechar();
	} else {
		player.sendMessage("You need a Summoning Level of "+familiar.getSummoninglevel()+" to summon this.");
	}
	}

    /* =======================================================
       USE SCROLL
       ======================================================= */

	public static void useScroll(client player, int scrollItemId) {

		SummoningFamiliar familiar = SummoningFamiliar.forScroll(scrollItemId);
		if (familiar == null) {
			return;
		}

		if (!player.hasNpc || player.summonId != familiar.getNpcId()) {
			player.sM("You do not have the correct familiar summoned.");
			return;
		}

		if (!player.playerHasItem(scrollItemId)) {
			return;
		}

		player.deleteItem2(scrollItemId, 1);

		activateScroll(player, familiar);
	}

    /* =======================================================
       SCROLL EFFECTS (STUB)
       ======================================================= */

	private static void activateScroll(client player, SummoningFamiliar familiar) {

		switch (familiar) {

			case SPIRIT_WOLF:
				player.sM("Your spirit wolf lets out a fearsome howl.");
				break;

			case DREADFOWL:
				player.sM("Your dreadfowl strikes swiftly.");
				break;

			default:
				player.sM("Your familiar uses a special move.");
				break;
		}
	}

    /* =======================================================
       DISMISS FAMILIAR
       ======================================================= */

	public static void dismissFamiliar(client player) {

		if (!player.hasNpc) {
			return;
		}

		for (int i = 0; i < server.npcHandler.npcs.length; i++) {
			NPC npc = server.npcHandler.npcs[i];
			if (npc == null) continue;

			if (npc.npcType == player.summonId && npc.summonedBy == player.index) {
				npc.absX = 0;
				npc.absY = 0;
				npc.IsDead = true;
				npc.NeedRespawn = false;
				server.npcHandler.npcs[i] = null;
				break;
			}
		}

		player.hasNpc = false;
		player.summonId = 0;
		player.activeFamiliar = null;
		player.familiarActive = false;

		player.sM("You dismiss your familiar.");
	}

    /* =======================================================
       SAFETY CLEANUP (LOGOUT / DEATH)
       ======================================================= */

	public static void cleanup(client player) {
		dismissFamiliar(player);
	}
	private final static int SHARD = 18016; // 14015
	private final static int POUCH = 12155;
	private static final String[][] summoningPouchData = {
			// Summoning pouch making
			// Pouch id, pouch charm, item1, Shardamount, LVL, Spec scroll, NPCID
			//TODO need to add NPCDEF's
			{ "Spirit wolf pouch", "Gold Charm", "Wolf bones", "7", "1",
					"Howl scroll" , "6829"},
			{ "Dreadfowl pouch", "Gold Charm", "Raw chicken", "8", "4",
					"Dreadfowl strike scroll" , "6825"},
			{ "Spirit spider pouch", "Gold Charm", "Spider carcass", "8", "10",
					"Egg spawn scroll" , "6841"},
			{ "Thorny Snail pouch", "Gold Charm", "Thin snail", "9", "13",
					"Slime spray scroll" , "6806"},
			{ "Granite Crab pouch", "Gold Charm", "Iron ore", "7", "16",
					"Stony shell scroll" , "6796"},
			{ "Mosquito pouch", "Gold Charm", "Proboscis", "1", "17",
					"Pester scroll" , "7331"},
			{ "Desert wyrm pouch", "Green Charm", "Bucket of sand", "45", "18",
					"Electric lash scroll" , "6831"},
			{ "Spirit Scorpion pouch", "Crimson Charm", "Bronze claws", "57",
					"19", "Venom shot scroll" , "6837"},
			{ "Spirit tz-kih pouch", "crimson charm", "Obsidian charm", "64",
					"22", "Fireball assault scroll" , "7361"},
			{ "Albino rat pouch", "Blue Charm", "Raw rat meat", "75", "23",
					"Cheese feast scroll" , "6847"},
			{ "Spirit kalphite pouch", "blue Charm", "potato cactus", "51",
					"25", "Sandstorm scroll" , "6994"},
			{ "Compost mound pouch", "Green charm", "compost", "47", "28",
					"Generate compost scroll" , "6871"},
			{ "Giant chinchompa pouch", "Blue Charm", "Chinchompa", "84", "29",
					"Explode scroll" , "7353"},
			{ "Vampire bat pouch", "Crimson Charm", "Vampire dust", "81", "31",
					"Vampire touch scroll" , "6835"},
			{ "Honey badger pouch", "Crimson Charm", "Honeycomb", "84", "32",
					"Insane ferocity scroll" , "6845"},
			{ "Beaver pouch", "Green Charm", "Willow logs", "72", "33",
					"Multichop scroll" , "6807"},
			{ "Void ravager pouch", "green Charm", "Ravager Charm", "74", "34",
					"Call to arms scroll" , "7370"},
			{ "Void shifter pouch", "blue charm", "Shifter charm", "74", "34",
					"Call to arms scroll" , "7367"},
			{ "void spinner pouch", "blue Charm", "spinner Charm", "74", "34",
					"Call to arms scroll" , "7333"},
			{ "Void Torcher pouch", "blue Charm", "Torcher Charm", "74", "34",
					"Call to arms scroll" , "7351"},
			{ "Bronze minotaur pouch", "Blue Charm", "Bronze bar", "102", "36",
					"Bronze bull rush scroll" , "6853"},
			{ "Bull ant pouch", "gold Charm", "Marigolds", "11", "40",
					"Unburden scroll" , "6867"},
			{ "Macaw pouch", "green Charm", "Clean guam", "78", "41",
					"Herbcall scroll" , "6851"},

			{ "Evil turnip pouch", "crimson Charm", "Carved turnip", "104",
					"42", "Evil flames scroll" , "6833"},


			{ "Iron minotaur pouch", "Blue Charm", "Iron bar", "125", "46",
					"Iron bull rush scroll" , "6855"},
			{ "Pyrelord pouch", "Crimson Charm", "Tinderbox", "111", "46",
					"Immense heat scroll" , "7377"},
			{ "Magpie pouch", "green Charm", "Gold ring", "88", "47",
					"Thieving fingers scroll" , "6824"},

			{ "Bloated leech pouch", "Crimson Charm", "Raw beef", "117", "49",
					"Blood drain scroll" , "6843"},
			{ "Spirit terrorbird pouch", "Gold Charm", "Raw bird meat", "12",
					"52", "Tireless run scroll" , "3596"},
			{ "Abyssal parasite pouch", "green Charm", "Abyssal charm", "106",
					"54", "Abyssal drain scroll" , "6818"},
			{ "Spirit jelly pouch", "blue Charm", "Jug of water", "151", "55",
					"Dissolve scroll" , "6922"},
			{ "Steel minotaur pouch", "blue Charm", "steel bar", "141", "56",
					"Fish rain scroll" , "6857"},
			{ "Ibis pouch", "green Charm", "Harpoon", "109", "56",
					"Steel bull rush scroll" , "6991"},
			{ "Spirit Graahk pouch", "blue Charm", "graahk fur", "154", "57",
					"Ambush scroll" , "3588"},
			{ "Spirit Kyatt pouch", "blue Charm", "Kyatt fur", "153", "57",
					"Rending scroll" , "7365"},
			{ "Spirit larupia pouch", "blue Charm", "larupia fur", "155", "57",
					"Goad scroll" , "7337"},
			{ "Karamthulhu overlord pouch", "blue Charm", "Empty fishbowl",
					"144", "58", "Doomsphere scroll" , "6809"},
			{ "Smoke devil pouch", "Crimson Charm", "Goat horn dust", "141",
					"61", "Dust cloud scroll" , "6865"},
			{ "Abyssal lurker", "green Charm", "Abyssal charm", "119", "62",
					"Abyssal stealth scroll" , "6820"},
			{ "Spirit cobra pouch", "Crimson Charm", "Snake hide", "116", "63",
					"Ophidian incubation scroll" , "6802"},
			{ "Stranger plant pouch", "Crimson Charm", "Bagged plant", "128",
					"64", "Poisonous blast scroll" , "6827"},
			{ "Mithril minotaur pouch", "Blue Charm", "Mithril bar", "152",
					"66", "Mithril bull rush scroll" , "6859"},
			{ "Barker toad pouch", "Gold Charm", "Swamp toad", "11", "66",
					"Toad bark scroll" , "6889"},
			{ "War tortoise pouch", "Gold Charm", "Tortoise shell", "1", "67",
					"Testudo scroll" , "6815"},
			{ "Bunyip pouch", "Green Charm", "Raw shark", "110", "68",
					"Swallow whole scroll" , "6813"},
			{ "Fruit bat pouch", "Green Charm", "Banana", "130", "69",
					"Fruitfall scroll" , "6817"},
			{ "Ravenous Locust pouch", "Crimson Charm", "Pot of Flour", "79",
					"70", "Famine scroll" , "7372"},
			{ "Arctic bear pouch", "Gold Charm", "Polar kebbit fur", "14",
					"71", "Arctic blast scroll" , "6839"},
			{ "Phoenix pouch", "Crimson Charm", "Phoenix Quill", "165", "72",
					"Phoenix unknown scroll" , "1911"},
			{ "Obsidian Golem pouch", "Blue Charm", "Obsidian Charm", "195",
					"73", "Volcanic strength scroll" , "7345"},
			{ "Granite lobster pouch", "Crimson Charm", "Granite (500g)",
					"166", "74", "Crushing claw scroll" , "6849"},
			{ "Praying mantis pouch", "Crimson Charm", "Flowers", "168", "75",
					"Mantis strike scroll" , "6798"},
			{ "Adamant minotaur pouch", "Blue Charm", "Adamant Bar", "144",
					"76", "Inferno scroll" , "6861"},
			{ "Forge Regent pouch", "Green Charm", "Ruby harvest", "141", "76",
					"Adamant bull rush scroll" , "7335"},
			{ "Talon Beast pouch", "Crimson Charm", "Talon Beast charm", "174",
					"77", "Deadly claw scroll" , "7347"},
			{ "Giant ent pouch", "Green Charm", "Willow branch", "124", "78",
					"Acorn missile scroll" , "6800"},
			{ "Fire titan pouch", "Blue Charm", "Fire talisman", "198", "79",
					"Titan's constitution scroll" , "7355"},
			{ "Ice titan pouch", "Blue Charm", "Water talisman", "198", "79",
					"Titan's constitution scroll" , "7359"},
			{ "Moss titan pouch", "Blue Charm", "Earth talisman", "202", "79",
					"Titan's constitution scroll" , "7357"},
			{ "Hydra pouch", "Green Charm", "Water orb", "128", "80",
					"Regrowth scroll" , "6811"},
			{ "Spirit dagannoth", "Crimson Charm", "Dagannoth hide", "122", "83",
					"Spike shot scroll" , "6804"},
			{ "Lava titan pouch", "Blue Charm", "Obsidian Charm", "219", "83",
					"Ebon thunder scroll" , "7341"},
			{ "Swamp titan pouch", "Blue Charm", "Swamp lizard", "150", "85",
					"Swamp plague scroll" , "7329"},
			{ "Rune minotaur pouch", "Blue Charm", "Rune bar", "111", "86",
					"Rune bull rush scroll" , "6863"},
			{ "Unicorn stallion pouch", "green Charm", "Unicorn Horn", "140",
					"88", "Healing aura scroll" , "3592"},
			{ "Geyser titan pouch", "blue Charm", "Water talisman", "222",
					"89", "Boil scroll" , "7339"},
			{ "Wolpertinger pouch", "crimson Charm", "Raw rabbit", "203", "92",
					"Magic focus scroll" , "3593"},
			{ "Abyssal titan pouch", "green Charm", "Abyssal charm", "113",
					"93", "Essence shipment scroll" , "7349"},
			{ "Iron titan pouch", "crimson Charm", "Iron platebody", "198",
					"95", "Iron within scroll" , "7375"},
			{ "Pack yak pouch", "Crimson Charm", "Yak-hide", "211", "96",
					"Winter storage scroll" , "3594"},
			{ "Steel titan pouch", "Blue Charm", "Steel platebody", "178",
					"99", "Steel of legends scroll", "3591"},

	};

	private static int getActionButton(int deltaA) {


		int deltaB = 8*(deltaA); /* Just so i look smart :D */

		if(deltaA == 7 && deltaA < 48&& deltaA < 59) {
			return 92000;
		}
		if(deltaA > 7 && deltaA < 48 && deltaA < 59) {
			return 92000+deltaB;
		}
		if(deltaA == 48) {
			return 93000;
		}

		if(deltaA == 59) {
			return 93080;
		}
		if(deltaA > 59) {
			return 93000+deltaB;
		}

		if(deltaA > 48) {
			return 93000+deltaA;
		}
		if(deltaA != 0 && deltaA < 7 && deltaA < 48 && deltaA < 59) {
			return 91184+deltaB;
		}

		if(deltaA == 0) {
			return 91184;
		}

		return 0;
	}

	private static boolean NEED(client c, int i) {
		if(c != null) {
			if(c.playerHasItem(POUCH) && c.playerHasItem(SHARD, Integer.parseInt(summoningPouchData[i][3]))
					&& c.playerHasItem(c.getItemId(summoningPouchData[i][1]))
					&& c.playerHasItem(
					c.getItemId(summoningPouchData[i][2]))) {

				return true;
			}


		}
		return false;
	}


	public static void makeSummoningPouch(client c, int usedItem, int usedWith) {
		for (int i = 0; i < summoningPouchData.length; i++) {
			if (usedItem == POUCH ) {
				if(usedWith == c.getItemId(summoningPouchData[i][2])) {
					if(c.playerHasItem(POUCH) && c.playerHasItem(SHARD, Integer.parseInt(summoningPouchData[i][3]))
							&& c.playerHasItem(c.getItemId(summoningPouchData[i][1]))
							&& c.playerHasItem(
							c.getItemId(summoningPouchData[i][2]))) {
						if(c.playerLevel[21] >= Integer
								.parseInt(summoningPouchData[i][4])) {
							c.deleteItem(POUCH, 1);
							c.deleteItem(SHARD,
									Integer.parseInt(summoningPouchData[i][3]));
							c.deleteItem(
									c.getItemId(summoningPouchData[i][1]), 1);
							c.deleteItem(
									c.getItemId(summoningPouchData[i][2]), 1);
							c.addItem(
									c.getItemId(summoningPouchData[i][0]), 1);
							c.addSkillXP(Integer.parseInt(summoningPouchData[i][4]) * 900, 21);
							break;
						} else {
							c.sendMessage("You do not have the required level to make this pouch");
							break;
						}
					} else {
						c.sendMessage("You do not have the required items to make this pouch.");
						c.sendMessage("You need: "+summoningPouchData[i][3]+" Shards ");
						c.sendMessage("You need a "+summoningPouchData[i][1]+" and a  "+summoningPouchData[i][2]+"");
						break;
					}
				}
			}
		}
	}
}
