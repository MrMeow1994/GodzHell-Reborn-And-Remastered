/**
 * NPC size utility backed by cache definitions.
 */
public final class NPCSize {

	private NPCSize() {}

	/**
	 * Gets the size (tile width) of an NPC from cache definitions.
	 *
	 * @param npcId the NPC id
	 * @return size in tiles (minimum 1)
	 */
	public static int get(int npcId) {
		NPCCacheDefinition def = NPCCacheDefinition.forID(npcId);

		if (def == null)
			return 1;

		int size = def.getSize(); // or def.tileSize / def.boundSize

		return size > 0 ? size : 1;
	}
}
