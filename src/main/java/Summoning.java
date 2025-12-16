public class Summoning {

    /* =======================================================
       SUMMON FAMILIAR
       ======================================================= */

	public static void summonFamiliar(client player, int pouchItemId, int slot) {

		SummoningFamiliar familiar = SummoningFamiliar.forPouch(pouchItemId);
		if (familiar == null) {
			return;
		}

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
		// Find the baby NPC and transform it in-place
		for (int i = 0; i < server.npcHandler.npcs.length; i++) {
			NPC npc = server.npcHandler.npcs[i];
			if (npc == null) continue;
			if (npc.npcType == familiar.getNpcId() && npc.summonedBy == player.index) {
				npc.gfx0(1315);
			}
		}
		player.sM("You summon a familiar.");
		player.savechar();
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

		player.sM("You dismiss your familiar.");
	}

    /* =======================================================
       SAFETY CLEANUP (LOGOUT / DEATH)
       ======================================================= */

	public static void cleanup(client player) {
		dismissFamiliar(player);
	}
}
