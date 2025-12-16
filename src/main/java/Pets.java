import java.util.Iterator;
import java.util.Map;

public class Pets {

    public static final int RATS_NEEDED_TO_GROW = 10;

    //npc id, item id
    public enum Pet {

        // Cats
        HELL_KITTEN(3504, 7583),
        LAZY_HELL_KITTEN(3506, 7584),
        WILY_HELLCAT(3507, 7585),

        PET_CAT_1(768, 1561),
        PET_CAT_2(769, 1562),
        PET_CAT_3(770, 1563),
        PET_CAT_4(771, 1564),
        PET_CAT_5(772, 1565),
        PET_CAT_6(773, 1566),

        PET_KITTEN_1(761, 1555, PET_CAT_1, ItemIDs.BUCKET_OF_MILK, 10, "Meow"),
        PET_KITTEN_2(762, 1556, PET_CAT_2, ItemIDs.BUCKET_OF_MILK, 10, "Meow"),
        PET_KITTEN_3(763, 1557, PET_CAT_3, ItemIDs.BUCKET_OF_MILK, 10, "Meow"),
        PET_KITTEN_4(764, 1558, PET_CAT_4, ItemIDs.BUCKET_OF_MILK, 10, "Meow"),
        PET_KITTEN_5(765, 1559, PET_CAT_5, ItemIDs.BUCKET_OF_MILK, 10, "Meow"),
        PET_KITTEN_6(766, 1560, PET_CAT_6, ItemIDs.BUCKET_OF_MILK, 10, "Meow"),
        // Dragons
        Hatchling_dragon(6900, 12469),
        Baby_dragon(6901, 12470),
        Hatchling_dragon2(6902, 12471),
        Baby_dragon2(6903, 12472),
        Hatchling_dragon3(6904, 12473),
        Baby_dragon3(6905, 12474),
        Hatchling_dragon4(6906, 12475),
        Baby_dragon4(6907, 12476),

        // Penguins
        Baby_penguin(6908, 12481),
        Penguin(6909, 12482),

        // Ravens
        Raven_chick(6911, 12484),
        Raven(6912, 12485),

        // Raccoons
        Baby_raccoon(6913, 12486),
        Raccoon(6914, 12487),

        // Geckos

        GECKO(6916, 12489),
        BABY_GECKO(6915, 12488, GECKO, 12125, 10, "geek"),
        // Squirrels

        squirrel(6920, 12491),
        Baby_squirrel(6919, 12490, squirrel, 12130, 20, "chirp"),

        // Chameleons
        Baby_chameleon(6922, 12492),
        Chameleon(6923, 12493),

        // Monkeys

        Monkey(6943, 12497),
        Baby_monkey(6942, 12496, Monkey, ItemIDs.BANANA, 10, "whoop"),

        // Vultures

        Vulture(6946, 12499),
        Vulture_chick(6945, 12498, Vulture, ItemIDs.GROUND_FISHING_BAIT, 25, "Kree"),
        // Crabs
        Baby_giant_crab(6947, 12500),
        Giant_crab(6948, 12501),

        // Saradomin birds
        Saradomin_chick(6949, 12503),
        Saradomin_bird(6950, 12504),
        Saradomin_owl(6951, 12505),

        // Zamorak birds
        Zamorak_chick(6952, 12506),
        Zamorak_bird(6953, 12507),
        Zamorak_hawk(6954, 12508),

        // Guthix birds
        Guthix_chick(6955, 12509),
        Guthix_bird(6956, 12510),
        Guthix_raptor(6957, 12511),

        // Dogs
        Terrier(6959, 12513),
        Terrier_puppy(6958, 12512, Terrier, ItemIDs.RAW_BEEF, 10, "Woof"),
        Greyhound(6961, 12515),
        Greyhound_puppy(6960, 12514, Greyhound, ItemIDs.RAW_BEEF, 10, "Woof"),
        Labrador(6963, 12517),
        Labrador_puppy(6962, 12516, Labrador, ItemIDs.RAW_BEEF, 10, "Woof"),
        Dalmatian(6965, 12519),
        Dalmatian_puppy(6964, 12518, Dalmatian, ItemIDs.RAW_BEEF, 10, "Woof"),
        Sheepdog(6967, 12521),
        Sheepdog_puppy(6966, 12520, Sheepdog, ItemIDs.RAW_BEEF, 10, "Woof"),
        Bulldog(6968, 12523),
        Bulldog_puppy(6969, 12522, Bulldog, ItemIDs.RAW_BEEF, 10, "Woof");


        private final int npcId;
        private final int itemId;
        private Pet growsInto;
        private int foodItemId;
        private int feedsRequired;
        private String txt4;

        Pet(int npcId, int itemId) {
            this.npcId = npcId;
            this.itemId = itemId;
        }
        // Constructor for babies
        Pet(int npcId, int itemId, Pet growsInto, int foodItemId, int feedsRequired, String txt4) {
            this.npcId = npcId;
            this.itemId = itemId;
            this.growsInto = growsInto;
            this.foodItemId = foodItemId;
            this.feedsRequired = feedsRequired;
            this.txt4 = txt4;
        }
        private final int growthTicksPerFeed = 40; // every feed = 40 ticks


        public int getNpcId() {
            return npcId;
        }

        public int getItemId() {
            return itemId;
        }
        public Pet getGrowsInto() { return growsInto; }
        public int getFoodItemId() { return foodItemId; }
        public int getFeedsRequired() { return feedsRequired; }
        public int getGrowthTicksPerFeed() { return growthTicksPerFeed; }
        public String getTxt4() {
            return txt4;
        }
        /* ---------------- lookup helpers ---------------- */

        public static boolean isCatItem(int itemId) {
            for (Pet cat : values()) {
                if (cat.itemId == itemId) {
                    return true;
                }
            }
            return false;
        }

        public static boolean isCatNpc(int npcId) {
            for (Pet cat : values()) {
                if (cat.npcId == npcId) {
                    return true;
                }
            }
            return false;
        }

        public static Pet forItem(int itemId) {
            for (Pet cat : values()) {
                if (cat.itemId == itemId) {
                    return cat;
                }
            }
            return null;
        }

        public static Pet forNpc(int npcId) {
            for (Pet cat : values()) {
                if (cat.npcId == npcId) {
                    return cat;
                }
            }
            return null;
        }
    }
    private static final int TICKS_PER_FEED = 120;

    public static void petGrowthTick() {

        for (Player p : server.playerHandler.players) {
            if (p == null) continue;
            client player = (client) p;

            // Iterate through all pets the player has dropped
            Iterator<Map.Entry<Integer, Integer>> growthIterator = player.petGrowthTicks.entrySet().iterator();
            while (growthIterator.hasNext()) {
                Map.Entry<Integer, Integer> entry = growthIterator.next();
                int npcId = entry.getKey();
                int growthTicks = entry.getValue();

                Pet pet = Pet.forNpc(npcId);
                if (pet == null || pet.getGrowsInto() == null) continue; // skip pets that don't grow

                // Increment hunger tick for this pet
                int hungerTicks = player.petHungerTicks.getOrDefault(npcId, 0);
                hungerTicks += 1;
                player.petHungerTicks.put(npcId, hungerTicks);
               // player.sendMessage("hungerticks"+player.petHungerTicks);

                // Only allow feeding every TICKS_PER_FEED ticks
                if (hungerTicks < TICKS_PER_FEED) {
                    continue;
                }

                // Pet is hungry, ready for next feed cycle
                // Increment growth ticks

                if(hungerTicks == 120) {
                    for (int i = 0; i < NPCHandler.maxNPCs; i++) {
                        NPC npc = NPCHandler.npcs[i];
                        if (npc != null) {
                            if (pet.npcId == npc.npcType) {
                                npc.forceChat(pet.getTxt4());
                            }
                        }
                    }
                        if (player.pethasfeed) {
                            growthTicks += 1;
                            player.petHungerTicks.put(npcId, 0);
                            player.pethasfeed = false;
                        }
                }
                player.petGrowthTicks.put(npcId, growthTicks);
                // Optional: notify player when pet is hungry
                if (hungerTicks == TICKS_PER_FEED) {
                    player.sM("Your pet is hungry!");
                    return;
                }


                // Check if pet has reached full growth
                if (growthTicks >= pet.getFeedsRequired() * TICKS_PER_FEED) {
                    transformPet(player, npcId); // your existing in-place transform method

                    // Reset counters
                    growthIterator.remove();
                    player.petHungerTicks.remove(npcId);
                }
            }
        }
    }


    public void feedPet(client player, int npcId, int foodItemId) {
        if(!player.pethasfeed) {
            Pet pet = Pet.forNpc(npcId);
            NPCCacheDefinition def = NPCCacheDefinition.forID(npcId);

            if (pet == null || pet.getGrowsInto() == null) {
                player.sM("This pet cannot be fed or grown.");
                return;
            }

            if (foodItemId != pet.getFoodItemId()) {
                player.sM("This food cannot be used on this pet.");
                return;
            }

            // Get hunger ticks and growth ticks
            int hungerTicks = player.petHungerTicks.getOrDefault(npcId, 0);
            int growthTicks = player.petGrowthTicks.getOrDefault(npcId, 0);
            player.pethasfeed = true;

            // Remove the food item
            player.deleteItem2(foodItemId, 1);

            // Special case for milk
            if (foodItemId == ItemIDs.BUCKET_OF_MILK) {
                player.addItem(ItemIDs.BUCKET, 1);
            }

            // Reset hunger ticks
            player.petHungerTicks.put(npcId, 0);

            // Add growth ticks per feed
            growthTicks += 40;
            player.petGrowthTicks.put(npcId, growthTicks);


            player.sM("You feed your " + def.getName() +
                    " (" + growthTicks + "/" + pet.getFeedsRequired() * 40 + " ticks)");

            // Check if growth threshold is reached
            if (growthTicks >= pet.getFeedsRequired() * 40) {
                transformPet(player, npcId); // transform the pet in-place
                player.petGrowthTicks.remove(npcId); // reset growth counter
                player.petHungerTicks.remove(npcId); // reset hunger counter
                player.sM("Your " + def.getName() +
                        " has grown into a " + server.npcHandler.GetNpcName(pet.getGrowsInto().getNpcId()) + "!");
            }
        }
    }



    public static void transformPet(client player, int babyNpcId) {
        Pet baby = Pet.forNpc(babyNpcId);
        if (baby == null || baby.getGrowsInto() == null) return;

        Pet adult = baby.getGrowsInto();

        // Find the baby NPC and transform it in-place
        for (int i = 0; i < server.npcHandler.npcs.length; i++) {
            NPC npc = server.npcHandler.npcs[i];
            if (npc == null) continue;

            if (npc.npcType == baby.getNpcId() && npc.summonedBy == player.index) {
                // Transform the NPC in-place
                npc.requestPetTransform(adult.getNpcId()); // optional depending on your system
                player.summonId = adult.getNpcId();
                // Reset growth counters
                player.petGrowthTicks.remove(baby.getNpcId());
                player.petHungerTicks.remove(baby.getNpcId());

                player.sM("Your " + server.npcHandler.GetNpcName(baby.getNpcId()) +
                        " has grown into a " + server.npcHandler.GetNpcName(adult.getNpcId()) + "!");
                return;
            }
        }
    }

    public static void dropPet(client player, int itemId, int slot) {

        // Resolve pet NPC once
        int petNpcId = summonItemId(itemId);
        String petName = NPCCacheDefinition.forID(petNpcId).getName();

        // Hard guard – do this FIRST
        if (player.hasNpc) {
            player.sM("You already dropped your " + petName + ".");
            return;
        }

        // Mark immediately to prevent double invocation
        player.hasNpc = true;

        // Remove the item safely
        int amount = player.playerItemsN[slot];
        if (amount <= 0) {
            player.hasNpc = false; // rollback just in case
            return;
        }
        player.deleteItem(itemId, slot, amount);

        player.sM("You drop your " + petName + ".");

        // Find a valid adjacent tile
        int spawnX = player.absX;
        int spawnY = player.absY;

        if (Region.getClipping(spawnX - 1, spawnY, player.heightLevel, -1, 0)) {
            spawnX--;
        } else if (Region.getClipping(spawnX + 1, spawnY, player.heightLevel, 1, 0)) {
            spawnX++;
        } else if (Region.getClipping(spawnX, spawnY - 1, player.heightLevel, 0, -1)) {
            spawnY--;
        } else if (Region.getClipping(spawnX, spawnY + 1, player.heightLevel, 0, 1)) {
            spawnY++;
        }

        // Spawn the pet
        server.npcHandler.spawnNpc3(
                player,
                petNpcId,
                spawnX,
                spawnY,
                player.heightLevel,
                0,
                120,
                25,
                200,
                200,
                false,
                false,
                true
        );

        player.savechar();
    }

    public void quickPickup(client player, int id) {
        for (int i = 0; i < server.npcHandler.npcs.length; i++) {
            NPC npc = server.npcHandler.npcs[i];
            if (npc == null) {
                continue;
            }
            if (npc.npcType == id) {
                Pet cat = Pet.forNpc(id);
                if (cat == null) {
                    return; // not a cat pet
                }
                player.addItem(cat.getItemId(), 1);
                npc.absX = 0;
                npc.absY = 0;
                npc.IsDead = true;
                npc.NeedRespawn = false;
                npc.npcType = -1;
                server.npcHandler.npcs[i] = null;
                player.hasNpc = false;
            }
        }
    }


    public void pickUpPet(client player, int npcId) {

        if (!player.hasFreeSlots(1)) {
            player.sM("You do not have enough space in your inventory to do that.");
            return;
        }

        Pet cat = Pet.forNpc(npcId);
        if (cat == null) {
            return; // not a cat pet
        }

        for (int i = 0; i < server.npcHandler.npcs.length; i++) {
            NPC npc = server.npcHandler.npcs[i];
            if (npc == null) {
                continue;
            }

            if (npc.npcType == npcId && npc.summonedBy == player.index) {
                player.startAnimation(827);

                npc.absX = 0;
                npc.absY = 0;
                npc.IsDead = true;
                npc.NeedRespawn = false;
                npc.npcType = -1;
                server.npcHandler.npcs[i] = null;
                // Give item back

                player.addItem(cat.getItemId(), 1);
                player.hasNpc = false;

                return;
            }
        }
    }


    public static int summonItemId(int itemId) {
        Pet cat = Pet.forItem(itemId);
        return cat != null ? cat.getNpcId() : 0;
    }


}