import java.util.HashSet;

public class BobTheCatManager {
    private static final int NPC_ID = 1091; // Bob the Cat's NPC ID
    private static NPC bob = null;

    private static final String[] locationNames = {
            "Lumbridge", "Varrock", "Falador", "Al Kharid", "Catherby",
            "Ardougne", "Yanille", "Edgeville", "Draynor", "Port Sarim"
    };

    private static final int[][] bobLocations = {
            {3222, 3218}, {3210, 3424}, {3094, 3496}, {2955, 3214}, {2757, 3478},
            {2662, 3306}, {2604, 3093}, {2908, 3537}, {3025, 3218}, {3050, 3250}
    };

    private static final HashSet<String> visitedTiles = new HashSet<>();

    public static void init() {
        CycleEventHandler.getSingleton().addEvent(null, new CycleEvent() {
            @Override
            public void execute(CycleEventContainer container) {
                spawnBob();
            }

            @Override
            public void stop() {}
        }, 3000); // Every 3 minutes
    }

    private static void spawnBob() {
        if (bob != null) {
            NPCHandler.removeNPC(bob);
            bob = null;
        }

        visitedTiles.clear();

        int index = misc.random(bobLocations.length - 1);
        int[] loc = bobLocations[index];
        String locationName = locationNames[index];

        int x = loc[0];
        int y = loc[1];

        bob = NPCHandler.spawnNpc(NPC_ID, x, y, 0, 0, 0, 0, 0, 0, false, false);
        bob.forceChat("Meow.");
        bob.walkingType = 1;
        PlayerHandler.messageToAll = "Bob the cat has been seen around " + locationName + "!";
        startRoaming(bob);
    }

    private static void startRoaming(NPC bob) {
        CycleEventHandler.getSingleton().addEvent(bob, new CycleEvent() {
            @Override
            public void execute(CycleEventContainer c) {
                if (bob == null || bob.IsDead) {
                    c.stop();
                    return;
                }

                int[][] directions = {
                        {-1, -1}, {0, -1}, {1, -1},
                        {-1,  0},          {1,  0},
                        {-1,  1}, {0,  1}, {1,  1}
                };

                for (int i = 0; i < 10; i++) {
                    int[] dir = directions[misc.random(directions.length - 1)];
                    int dx = dir[0];
                    int dy = dir[1];

                    int targetX = bob.absX + dx;
                    int targetY = bob.absY + dy;
                    String key = targetX + "," + targetY;

                    if (!visitedTiles.contains(key) && Region.canMove(bob.absX, bob.absY, targetX, targetY, bob.heightLevel, 1, 1)) {
                        visitedTiles.add(key);
                        bob.moveX = dx;
                        bob.moveY = dy;
                        bob.walkingType = 1;
                        bob.updateRequired = true;
                        bob.direction = bob.getNextWalkingDirection();
                        break;
                    }
                }
            }

            @Override
            public void stop() {}
        }, 600); // Every tick (600ms)
    }
}
