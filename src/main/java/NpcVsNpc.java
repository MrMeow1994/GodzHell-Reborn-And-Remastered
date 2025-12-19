import java.util.Random;

public class NpcVsNpc {

	private static final Random random = new Random();

	public static void attack(NPC attacker, NPC target) {
		if (attacker == null || target == null) {
			//System.out.println("attacker or target is null");
			return;
		}

		if (attacker.IsDead || target.IsDead) {
			return;
		}
		// Combat delay logic
		if (attacker.npcCombatDelay > 0)
			return; // Still cooling down
		if (isRanging(attacker.npcId)) {
			rangeAttack(attacker, target);
		} else {
			meleeAttack(attacker, target);
		}
		attacker.npcCombatDelay = getAttackSpeed(attacker.npcId); // Default 4 ticks (2.4s)
	}

	public static void meleeAttack(NPC attacker, NPC target) {
		if (attacker == null || target == null || attacker.IsDead || target.IsDead)
			return;

		// Respect action timers or hit delays
		if (attacker.actionTimer > 0)
			return;

		int maxHit = getNpcMeleeAttack(attacker.npcId);
		int hit = random.nextInt(maxHit + 1);


		// Set animation
		attacker.animationRequest = server.npcHandler.getNpcAttackAnimation(attacker.npcId);
		attacker.animationUpdateRequired = true;
		attacker.updateRequired = true;

		// Set delay before next attack
		attacker.actionTimer = AnimationLength.getFrameLength(server.npcHandler.getNpcAttackAnimation(attacker.npcId));

		// Deal damage
		target.dealDoubleDamage(hit, 0, hit == 0 ? Hitmark.MISS : Hitmark.HIT);

		// Optional: add aggro return
		target.underAttackBy = attacker.npcId;
		target.lastAttacked = System.currentTimeMillis();
	}


	public static void rangeAttack(NPC attacker, NPC target) {
		try {
			int maxHit = getNpcRangeAttack(attacker.npcId);
			int hit = random.nextInt(maxHit + 1);

			attacker.animationRequest =  server.npcHandler.getNpcAttackAnimation(attacker.npcId);
			attacker.animationUpdateRequired = true;
			target.dealDoubleDamage(hit, 0, Hitmark.HIT); // Again, your NPC class needs this
			//System.out.println(attacker.npcId + " ranges " + target.npcId + " for " + hit + " damage.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static int getAttackSpeed(int npcId) {
		switch (npcId) {

			default:
				return 4; // Default 4 ticks = 2.4 seconds
		}
	}

	public static boolean isRanging(int npcId) {
		return npcId == 1158 || npcId == 8160 || npcId == 8133 || npcId == 8127 || npcId == 50;
	}

	public static int getNpcMeleeAttack(int npcId) {
		switch (npcId) {
			default:
				return 10;
		}
	}

	public static int getNpcRangeAttack(int npcId) {
		switch (npcId) {

			default:
				return 10;
		}
	}

}
