import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.ArrayList;

public class NPCHandler {

    public static int maxNPCs = 16000;
    public static int maxListedNPCs = 16000;
    public static int maxNPCDrops = 10000;
    public static NPC[] npcs = new NPC[maxNPCs];
    public static boolean IsDropping = false;
    public NPCList[] NpcList = new NPCList[maxListedNPCs];
    public NPCDrops[] NpcDrops = new NPCDrops[maxNPCDrops];
    public int remove = 2; // 1 = removes equipment, 2 = doesn't remove - xerozcheez
    public static final int MAX_MAP_X = 104;
    public static final int MAX_MAP_Y = 104;
    public static final int MAX_HEIGHT = 4;

    public static boolean[][][] npcOccupied = new boolean[MAX_HEIGHT][MAX_MAP_X][MAX_MAP_Y];


    private static final String NPC_LIST_PATH = "./Data/cfg/npc.cfg";
    private static final String[] AUTOSPAWN_PATHS = {
            "./Data/cfg/autospawn.cfg",
            "./Data/cfg/autospawn2.cfg"
    };

    public NPCHandler() {
        npcs = new NPC[maxNPCs];
        NpcList = new NPCList[maxListedNPCs];
        NpcDrops = new NPCDrops[maxNPCDrops];

        try {
            loadNPCList(NPC_LIST_PATH);
            for (String path : AUTOSPAWN_PATHS) {
                loadAutoSpawn(path);
            }
        } catch (RuntimeException e) {
            System.err.println("💥 Failed to initialize NPCHandler: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void removeNPC(NPC npc) {
        if (npc == null) return;

        for (int i = 0; i < maxNPCs; i++) {
            if (npcs[i] == npc) {
                npc.IsDead = true;
                npc.NeedRespawn = false;
                npc.npcType = -1;
                npcs[i] = null;
                break;
            }
        }
    }


    public static NPC spawnNpc(int npcType, int x, int y, int height, int walkType, int maxHP, int maxHit, int attack, int defence, boolean aggressive, boolean respawn) {
        for (int i = 1; i < maxNPCs; i++) {
            if (npcs[i] == null) {
                NPC newNpc = new NPC(i, npcType);
                newNpc.absX = x;
                newNpc.absY = y;
                newNpc.makeX = x;
                newNpc.makeY = y;
                newNpc.heightLevel = height;
                newNpc.walkingType = walkType;
                newNpc.HP = maxHP;
                newNpc.MaxHP = maxHP;
                newNpc.MaxHit = maxHit;
                newNpc.attack = attack;
                newNpc.NeedRespawn = respawn;
                newNpc.spawnedBy = -1;
                newNpc.facePlayer(0);
                newNpc.updateRequired = true;
                newNpc.IsDead = false;
                newNpc.randomWalk = (walkType == 1);
                npcs[i] = newNpc;
                return newNpc;
            }
        }
        return null; // No available slots
    }

    public void newPetNPC(int npcType, int x, int y, int heightLevel, int rangex1, int rangey1, int rangex2, int rangey2, int WalkingType, int HP, boolean Respawns, int summonedBy) {
        int slot = -1;
        for (int i = 1; i < maxNPCs; i++) {
            if (npcs[i] == null) {
                slot = i;
                break;
            }
        }
        if (slot == -1)
            return;
        if (HP <= 0) {
            HP = 100;
        }
        NPC newNPC = new NPC(slot, npcType);
        newNPC.absX = x;
        newNPC.absY = y;
        newNPC.makeX = x;
        newNPC.makeY = y;
        newNPC.moverangeX1 = rangex1;
        newNPC.moverangeY1 = rangey1;
        newNPC.moverangeX2 = rangex2;
        newNPC.moverangeY2 = rangey2;
        newNPC.walkingType = WalkingType;
        newNPC.HP = HP;
        newNPC.MaxHP = HP;
        newNPC.MaxHit = (int) Math.floor(((double) HP / 10));
        if (newNPC.MaxHit < 1) {
            newNPC.MaxHit = 1;
        }
        newNPC.heightLevel = heightLevel;
        newNPC.Respawns = Respawns;
        newNPC.followPlayer = summonedBy;
        newNPC.followingPlayer = true;
        npcs[slot] = newNPC;
    }

    public int getNpcKillerId(int npcId) {
        Player[] players = server.playerHandler.players;
        int killerId = 0;
        int highestDamage = 0;

        for (int i = 1; i < PlayerHandler.maxPlayers; i++) {
            Player player = players[i];
            if (player == null) continue;

            if (player.lastNpcAttacked == npcId) {
                if (player.totalDamageDealt > highestDamage) {
                    highestDamage = player.totalDamageDealt;
                    killerId = i;
                }
                // Reset damage *after* evaluation — not during comparison
                player.totalDamageDealt = 0;
            }
        }

        return killerId;
    }

    public void handleClipping(int i) {
        NPC npc = npcs[i];
        if (npc.moveX == 1 && npc.moveY == 1) {
            if ((Region.getClipping(npc.absX + 1, npc.absY + 1, npc.heightLevel) & 0x12801e0) != 0) {
                npc.moveX = 0;
                npc.moveY = 0;
                if ((Region.getClipping(npc.absX, npc.absY + 1, npc.heightLevel) & 0x1280120) == 0)
                    npc.moveY = 1;
                else
                    npc.moveX = 1;
            }
        } else if (npc.moveX == -1 && npc.moveY == -1) {
            if ((Region.getClipping(npc.absX - 1, npc.absY - 1, npc.heightLevel) & 0x128010e) != 0) {
                npc.moveX = 0;
                npc.moveY = 0;
                if ((Region.getClipping(npc.absX, npc.absY - 1, npc.heightLevel) & 0x1280102) == 0)
                    npc.moveY = -1;
                else
                    npc.moveX = -1;
            }
        } else if (npc.moveX == 1 && npc.moveY == -1) {
            if ((Region.getClipping(npc.absX + 1, npc.absY - 1, npc.heightLevel) & 0x1280183) != 0) {
                npc.moveX = 0;
                npc.moveY = 0;
                if ((Region.getClipping(npc.absX, npc.absY - 1, npc.heightLevel) & 0x1280102) == 0)
                    npc.moveY = -1;
                else
                    npc.moveX = 1;
            }
        } else if (npc.moveX == -1 && npc.moveY == 1) {
            if ((Region.getClipping(npc.absX - 1, npc.absY + 1, npc.heightLevel) & 0x128013) != 0) {
                npc.moveX = 0;
                npc.moveY = 0;
                if ((Region.getClipping(npc.absX, npc.absY + 1, npc.heightLevel) & 0x1280120) == 0)
                    npc.moveY = 1;
                else
                    npc.moveX = -1;
            }
        } //Checking Diagonal movement.

        if (npc.moveY == -1) {
            if ((Region.getClipping(npc.absX, npc.absY - 1, npc.heightLevel) & 0x1280102) != 0)
                npc.moveY = 0;
        } else if (npc.moveY == 1) {
            if ((Region.getClipping(npc.absX, npc.absY + 1, npc.heightLevel) & 0x1280120) != 0)
                npc.moveY = 0;
        } //Checking Y movement.
        if (npc.moveX == 1) {
            if ((Region.getClipping(npc.absX + 1, npc.absY, npc.heightLevel) & 0x1280180) != 0)
                npc.moveX = 0;
        } else if (npc.moveX == -1) {
            if ((Region.getClipping(npc.absX - 1, npc.absY, npc.heightLevel) & 0x1280108) != 0)
                npc.moveX = 0;
        } //Checking X movement.
    }

    public int npcSize(int i) {
        switch (npcs[i].npcType) {
            case 2883:
            case 2882:
            case 2881:
                return 3;
        }
        return 0;
    }


    public int distanceRequired(int i) {
        switch (npcs[i].npcType) {
            case 2025:
            case 2028:
                return 6;
            case 50:
            case 2562:
                return 2;
            case 2881:// dag kings
            case 2882:
            case 3200:// chaos ele
            case 2743:
            case 2631:
                //case 2837:
            case 2745:
                return 8;
            case 2883:// rex
                return 1;
            case 2552:
            case 2553:
            case 2556:
            case 2557:
            case 2558:
            case 2559:
            case 2560:
            case 2564:
            case 2565:
                return 9;
            // things around dags
            case 2892:
            case 2894:
                return 10;
            default:
                return 1;
        }
    }

    public void loadSpell(NPC npc) {
        switch (npc.npcType) {
            case 3491: // Kolodion
                npc.attackType = 2; // Magic
                npc.projectileId = 130;
                npc.endGfx = 131;
                npc.startAnimation(1979);
                npc.hitDelayTimer = 4;
                npc.MaxHit = 20;
                break;
            case 1913: // Kemli - Ice Barrage
                npc.attackType = 2; // Magic
                npc.startGfx = 368;
                npc.projectileId = 366; // Ice Barrage projectile (OSRS style)
                npc.endGfx = 367; // Ice Barrage end GFX
                npc.hitDelayTimer = 4; // Magic delay
                npc.MaxHit = 30; // Buffed from base barrage
                break;
            case 2025: // Ahrim
                int r = misc.random(2);
                npc.attackType = 2;
                npc.startAnimation(1979);
                switch (r) {
                    case 0: // Fire
                        npc.projectileId = 273;
                        npc.endGfx = 274;
                        break;
                    case 1: // Wind
                        npc.projectileId = 269;
                        npc.endGfx = 270;
                        break;
                    case 2: // Water
                        npc.projectileId = 265;
                        npc.endGfx = 266;
                        break;
                }
                npc.hitDelayTimer = 3;
                npc.MaxHit = 17;
                break;

            case 2028: // Karil
                npc.attackType = 1;
                npc.projectileId = 27;
                npc.endGfx = -1;
                npc.hitDelayTimer = 3;
                npc.MaxHit = 17;
                npc.startAnimation(2075);
                break;

            case 2030: // Dharok
                npc.attackType = 0;
                npc.projectileId = -1;
                npc.endGfx = -1;
                npc.hitDelayTimer = 2;
                npc.MaxHit = 30;
                npc.startAnimation(2066);
                break;

            case 8133: // Corporeal Beast
                int type = misc.random(2);
                if (type == 0) {
                    npc.attackType = 0;
                    npc.startAnimation(10057);
                    npc.projectileId = -1;
                    npc.endGfx = -1;
                    npc.MaxHit = 45;
                } else {
                    npc.attackType = 1;
                    npc.projectileId = 182;
                    npc.endGfx = 183;
                    npc.startAnimation(10058);
                    npc.MaxHit = 30;
                }
                npc.hitDelayTimer = 4;
                break;

            // Add more cases as needed...
        }
    }

    public boolean multiAttacks(int i) {
        switch (npcs[i].npcType) {
            case 6222: // Kree'arra
            case 6223:
            case 1913: // Kemli
            case 6225:
            case 6227:
            case 8133: // Corporeal Beast
            case 2745: // Jad
            case 3162: // Giant mole
            case 8352: // Tormented demon
            case 2881: // Dag King
            case 2882:
            case 2883:
                return true;
        }
        return false;
    }

    public int followDistance(int i) {
        switch (npcs[i].npcType) {
            case 2550:
            case 2551:
            case 2562:
            case 2563:
                return 8;
            case 2883:
                return 4;
            case 2881:
            case 2882:
                return 1;

        }
        return 0;

    }
    public static boolean followPlayer(int i) {
        switch (npcs[i].npcType) {
            case 1456:
            case 2892:
            case 2894:
            case 1532:
            case 1534:
                return false;
        }
        return true;
    }
    public void followPlayer(int i, int playerId) {
        if (server.playerHandler.players[playerId] == null) {
            return;
        }
        if (server.playerHandler.players[playerId].respawnTimer > 0) {
            npcs[i].facePlayer(0);
            npcs[i].RandomWalk = true;
            npcs[i].IsUnderAttack = false;
            return;
        }

        if (npcs[i].npcType == 1532 || npcs[i].npcType == 1534) {
            return;
        }

        if (!followPlayer(i) && npcs[i].npcType != 1532 && npcs[i].npcType != 1534) {
            npcs[i].facePlayer(playerId);
            return;
        }

        int playerX = PlayerHandler.players[playerId].absX;
        int playerY = PlayerHandler.players[playerId].absY;
        npcs[i].randomWalk = false;
        if (goodDistance(npcs[i].getX(), npcs[i].getY(), playerX, playerY, distanceRequired(i))) {
            return;
        }

        NPC    npc    = npcs[i];
        int    x      = npc.absX;
        int    y      = npc.absY;
        Player player = PlayerHandler.players[playerId];
        if (npcs[i].spawnedBy > 0
                || x < npc.makeX + Config.NPC_FOLLOW_DISTANCE
                && x > npc.makeX - Config.NPC_FOLLOW_DISTANCE
                && y < npc.makeY + Config.NPC_FOLLOW_DISTANCE
                && y > npc.makeY - Config.NPC_FOLLOW_DISTANCE) {
            if (npc.heightLevel == player.heightLevel) {
                if (player != null && npc != null) {
                    if (playerX > x && playerY < y) {
                        npc.moveX = GetMove(x, playerX);//Diagonal bottom right
                    } else if (playerX < x && playerY < y) {
                        npc.moveY = GetMove(y, playerY); //Diagonal bottom left
                    } else if (playerX < x && playerY > y) {
                        npc.moveX = GetMove(x, playerX);// Diagonal top left
                    } else if (playerX > x && playerY > y) {
                        npc.moveY = GetMove(y, playerY);// Diagonal top right
                    } else if (playerY < y) {
                        npc.moveX = GetMove(x, playerX); //Move South to player
                        npc.moveY = GetMove(y, playerY);
                    } else if (playerY > y) {
                        npc.moveX = GetMove(x, playerX); //Move North to player
                        npc.moveY = GetMove(y, playerY);
                    } else if (playerX < x) {
                        npc.moveX = GetMove(x, playerX); //Move West to player
                        npc.moveY = GetMove(y, playerY);
                    } else if (playerX > x) {
                        npc.moveX = GetMove(x, playerX); //Move East to player
                        npc.moveY = GetMove(y, playerY);
                    }
                    npc.facePlayer(playerId);
                    handleClipping(i);
                    npc.getNextNPCMovement(i);
                    npc.updateRequired = true;
                }
            }
        } else {
            npc.facePlayer(0);
            npc.randomWalk = true;
            npc.underAttack = false;
        }
    }

    public int getCloseRandomPlayer(int i) {
        ArrayList<Integer> players = new ArrayList<>();
        for (int j = 0; j < server.playerHandler.players.length; j++) {
            if (server.playerHandler.players[j] != null) {
                if (GoodDistance(server.playerHandler.players[j].absX,
                        server.playerHandler.players[j].absY, npcs[i].absX,
                        npcs[i].absY, 2 + distanceRequired(i)
                                + followDistance(i))
                        || isFightCaveNpc(i)) {
                    //if ((server.playerHandler.players[j].underAttackBy <= 0 && server.playerHandler.players[j].underAttackBy2 <= 0))
                    if (server.playerHandler.players[j].heightLevel == npcs[i].heightLevel)
                        players.add(j);
                }
            }
        }
        if (players.size() > 0)
            return players.get(misc.random(players.size() - 1));
        else
            return 0;
    }

    public boolean isFightCaveNpc(int i) {
        switch (npcs[i].npcType) {
            case 2627:
            case 2630:
            case 2631:
            case 2741:
            case 2743:
            case 2745:
                return true;
        }
        return false;
    }

    public void spawnNpc3(client c, int npcType, int x, int y, int heightLevel, int WalkingType, int HP, int maxHit, int attack, int defence, boolean attackPlayer, boolean headIcon, boolean summonFollow) {
        // first, search for a free slot
        int slot = -1;
        for (int i = 1; i < maxNPCs; i++) {
            if (npcs[i] == null) {
                slot = i;
                break;
            }
        }
        if (slot == -1) {
            //Misc.println("No Free Slot");
            return;        // no free slot found
        }
        NPC newNPC = new NPC(slot, npcType);
        newNPC.absX = x;
        newNPC.absY = y;
        newNPC.makeX = x;
        newNPC.makeY = y;
        newNPC.heightLevel = heightLevel;
        newNPC.walkingType = WalkingType;
        newNPC.HP = HP;
        newNPC.MaxHP = HP;
        //newNPC.maxHit = maxHit;
        //newNPC.attack = attack;
        //newNPC.defence = defence;
        newNPC.spawnedBy = c.index;
        newNPC.IsUnderAttack = true;
        newNPC.faceplayer(c.index);
        if (headIcon)
            c.drawHeadicon(1, slot);
        if (summonFollow) {
            newNPC.summoner = true;
            newNPC.summonedBy = c.index;
            c.summonId = npcType;
            c.hasNpc = true;
        }
        if (attackPlayer) {
            newNPC.IsUnderAttack = true;
            if (c != null) {
                newNPC.StartKilling = c.index;
            }
        }
        npcs[slot] = newNPC;
    }

    /**
     * Summon npc, barrows, etc
     **/
    public void spawnNpc(client c, int npcType, int x, int y, int heightLevel,
                         int WalkingType, int HP, int maxHit, int attack, int defence,
                         boolean attackPlayer, boolean headIcon) {
        // first, search for a free slot
        int slot = -1;
        for (int i = 1; i < maxNPCs; i++) {
            if (npcs[i] == null) {
                slot = i;
                break;
            }
        }
        if (slot == -1) {
            // Misc.println("No Free Slot");
            return; // no free slot found
        }
        NPC newNPC = new NPC(slot, npcType);
        newNPC.absX = x;
        newNPC.absY = y;
        newNPC.makeX = x;
        newNPC.makeY = y;
        newNPC.heightLevel = heightLevel;
        newNPC.walkingType = WalkingType;
        newNPC.HP = HP;
        newNPC.MaxHP = HP;
        newNPC.MaxHit = maxHit;
        newNPC.attack = attack;
        newNPC.spawnedBy = c.index;
        if (headIcon)
            c.drawHeadicon(1, slot);
        if (attackPlayer) {
            newNPC.IsUnderAttack = true;
            if (c != null) {
                newNPC.StartKilling = c.index;
            }
        }
        npcs[slot] = newNPC;
    }

    public void newNPC(int npcType, int x, int y, int heightLevel, int rangex1, int rangey1, int rangex2, int rangey2, int WalkingType, int HP, boolean Respawns) {
        // first, search for a free slot
        int slot = -1;

        for (int i = 1; i < maxNPCs; i++) {
            if (npcs[i] == null) {
                slot = i;
                break;
            }
        }

        if (slot == -1) {
            return;
        }        // no free slot found
        if (HP <= 0) { // This will cause client crashes if we don't use this :) - xero
            HP = 30;
        }
        NPC newNPC = new NPC(slot, npcType);

        newNPC.absX = x;
        newNPC.absY = y;
        newNPC.makeX = x;
        newNPC.makeY = y;
        newNPC.moverangeX1 = rangex1;
        newNPC.moverangeY1 = rangey1;
        newNPC.moverangeX2 = rangex2;
        newNPC.moverangeY2 = rangey2;
        newNPC.walkingType = WalkingType;
        newNPC.HP = HP;
        newNPC.MaxHP = HP;
        newNPC.MaxHit = NpcMaxHitCalculator.calculate(npcType, HP);
        if (newNPC.MaxHit < 1) {
            newNPC.MaxHit = 1;
        }
        newNPC.heightLevel = heightLevel;
        newNPC.Respawns = Respawns;
        npcs[slot] = newNPC;
    }
    public void removeNpc(int index) {
        if (index < 0 || index >= npcs.length) {
            return;
        }

        NPC npc = npcs[index];
        if (npc == null) {
            return;
        }

        // Mark as dead/removed so processing loop ignores it
        npc.IsDead = true;
        npc.NeedRespawn = false;
        npcs[index] = null;
    }

    public void newSummonedNPC(int npcType, int x, int y, int heightLevel, int rangex1, int rangey1, int rangex2, int rangey2, int WalkingType, int HP, boolean Respawns, int summonedBy) {
        // first, search for a free slot
        int slot = -1;

        for (int i = 1; i < maxNPCs; i++) {
            if (npcs[i] == null) {
                slot = i;
                break;
            }
        }

        if (slot == -1) {
            return;
        }        // no free slot found
        if (HP <= 0) { // This will cause client crashes if we don't use this :) - xero
            HP = 3000;
        }
        NPC newNPC = new NPC(slot, npcType);

        newNPC.absX = x;
        newNPC.absY = y;
        newNPC.makeX = x;
        newNPC.makeY = y;
        newNPC.moverangeX1 = rangex1;
        newNPC.moverangeY1 = rangey1;
        newNPC.moverangeX2 = rangex2;
        newNPC.moverangeY2 = rangey2;
        newNPC.walkingType = WalkingType;
        newNPC.HP = HP;
        newNPC.MaxHP = HP;
        newNPC.MaxHit = (int) Math.floor(((double) HP / 100));
        if (newNPC.MaxHit < 1) {
            newNPC.MaxHit = 10;
        }
        newNPC.heightLevel = heightLevel;
        newNPC.Respawns = Respawns;
        newNPC.followPlayer = summonedBy;
        newNPC.followingPlayer = true;
        npcs[slot] = newNPC;
    }

    public void newNPCList(int npcType, String npcName, int combat, int HP) {
        // first, search for a free slot
        int slot = -1;

        for (int i = 0; i < maxListedNPCs; i++) {
            if (NpcList[i] == null) {
                slot = i;
                break;
            }
        }

        if (slot == -1) {
            return;
        }        // no free slot found

        NPCList newNPCList = new NPCList(npcType);

        newNPCList.npcName = npcName;
        newNPCList.npcCombat = combat;
        newNPCList.npcHealth = HP;
        NpcList[slot] = newNPCList;
    }

    public boolean rareDrops(int i) {
        return misc.random(NPCDrops.dropRarity.get(npcs[i].npcType)) == 0;
    }

    public void dropItems(int i) {
        // long start = System.currentTimeMillis();
        client c = (client) PlayerHandler.players[GetNpcKiller(i)];
        if (c != null) {
            if (NPCDrops.constantDrops.get(npcs[i].npcType) != null) {
                for (int item : NPCDrops.constantDrops.get(npcs[i].npcType)) {
                    ItemHandler.addItem(item, npcs[i].absX, npcs[i].absY, 1, c.index, false);
                    // if (c.clanId >= 0)
                    // Server.clanChat.handleLootShare(c, item, 1);
                }
            }

            if (NPCDrops.dropRarity.get(npcs[i].npcType) != null) {
                if (rareDrops(i)) {
                    int random = misc.random(NPCDrops.rareDrops
                            .get(npcs[i].npcType).length - 1);
                    ItemHandler.addItem(NPCDrops.rareDrops.get(npcs[i].npcType)[random][0], npcs[i].absX, npcs[i].absY, NPCDrops.rareDrops.get(npcs[i].npcType)[random][1], c.index, false);
                    //if (c.clanId >= 0)
                    //Server.clanChat
                    //.handleLootShare(
                    //c,
                    //NPCDrops.rareDrops.get(npcs[i].npcType)[random][0],
                    //NPCDrops.rareDrops.get(npcs[i].npcType)[random][1]);
                } else {
                    int random = misc.random(NPCDrops.normalDrops
                            .get(npcs[i].npcType).length - 1);
                    ItemHandler.addItem(NPCDrops.normalDrops.get(npcs[i].npcType)[random][0], npcs[i].absX, npcs[i].absY, NPCDrops.normalDrops.get(npcs[i].npcType)[random][1], c.index, false);
                    // Server.clanChat.handleLootShare(c,
                    //NPCDrops.normalDrops.get(npcs[i].npcType)[random][0],
                    //NPCDrops.normalDrops.get(npcs[i].npcType)[random][1]);
                }
            }

        }
        System.out.println("Took: " + (System.currentTimeMillis()));
    }

    /*
     public boolean IsInWorldMap(int coordX, int coordY) {
     for (int i = 0; i < worldmap[0].length; i++) {
     //if (worldmap[0][i] == coordX && worldmap[1][i] == coordY) {
     return true;
     //}
     }
     return false;
     }
     public boolean IsInWorldMap2(int coordX, int coordY) {
     for (int i = 0; i < worldmap2[0].length; i++) {
     if (worldmap2[0][i] == coordX && worldmap2[1][i] == coordY) {
     return true;
     }
     }
     return true;
     }

     public boolean IsInRange(int NPCID, int MoveX, int MoveY) {
     int NewMoveX = (npcs[NPCID].absX + MoveX);
     int NewMoveY = (npcs[NPCID].absY + MoveY);
     if (NewMoveX <= npcs[NPCID].moverangeX1 && NewMoveX >= npcs[NPCID].moverangeX2 && NewMoveY <= npcs[NPCID].moverangeY1 && NewMoveY >= npcs[NPCID].moverangeY2) {
     if ((npcs[NPCID].walkingType == 1 && IsInWorldMap(NewMoveX, NewMoveY) == true) || (npcs[NPCID].walkingType == 2 && IsInWorldMap2(NewMoveX, NewMoveY) == false)) {
     if (MoveX == MoveY) {
     if ((IsInWorldMap(NewMoveX, npcs[NPCID].absY) == true && IsInWorldMap(npcs[NPCID].absX, NewMoveY) == true) || (IsInWorldMap2(NewMoveX, npcs[NPCID].absY) == false && IsInWorldMap2(npcs[NPCID].absX, NewMoveY) == false)) {
     return true;
     }
     return false;
     }
     return true;
     }
     }
     return false;
     }*/
    public int GetMove(int Place1, int Place2) { // Thanks to diablo for this! Fixed my npc follow code <3
        if ((Place1 - Place2) == 0) {
            return 0;
        } else if ((Place1 - Place2) < 0) {
            return 1;
        } else if ((Place1 - Place2) > 0) {
            return -1;
        }
        return 0;
    }


    public void FollowPlayerCB(int NPCID, int playerID) {
        int playerX = server.playerHandler.players[playerID].absX;
        int playerY = server.playerHandler.players[playerID].absY;

        npcs[NPCID].RandomWalk = false;
        if (server.playerHandler.players[playerID] != null) {
            if (playerY < npcs[NPCID].absY) {
                npcs[NPCID].moveX = GetMove(npcs[NPCID].absX, playerX);
                npcs[NPCID].moveY = GetMove(npcs[NPCID].absY, playerY + 1);
            } else if (playerY > npcs[NPCID].absY) {
                npcs[NPCID].moveX = GetMove(npcs[NPCID].absX, playerX);
                npcs[NPCID].moveY = GetMove(npcs[NPCID].absY, playerY - 1);
            } else if (playerX < npcs[NPCID].absX) {
                npcs[NPCID].moveX = GetMove(npcs[NPCID].absX, playerX + 1);
                npcs[NPCID].moveY = GetMove(npcs[NPCID].absY, playerY);
            } else if (playerX > npcs[NPCID].absX) {
                npcs[NPCID].moveX = GetMove(npcs[NPCID].absX, playerX - 1);
                npcs[NPCID].moveY = GetMove(npcs[NPCID].absY, playerY);
            }
            handleClipping(NPCID);
            npcs[NPCID].getNextNPCMovement(NPCID);
            npcs[NPCID].updateRequired = true;
        }
    }

    public void PoisonNPC(int NPCID) {
        npcs[NPCID].PoisonClear = 0;
        npcs[NPCID].PoisonDelay = 40;
    }

    public void Poison(int NPCID) {
        if (npcs[NPCID].PoisonDelay <= 1) {
            int hitDiff = 3 + misc.random(15);

            npcs[NPCID].poisondmg = true;
            npcs[NPCID].hitDiff = hitDiff;
            npcs[NPCID].updateRequired = true;
            npcs[NPCID].hitUpdateRequired = true;
            npcs[NPCID].PoisonClear++;
            npcs[NPCID].PoisonDelay = 40;
        }
    }

    public int summonItemId(int itemId) {
        if (itemId == 1555) return 761;
        if (itemId == 1556) return 762;
        if (itemId == 1557) return 763;
        if (itemId == 1558) return 764;
        if (itemId == 1559) return 765;
        if (itemId == 1560) return 766;
        if (itemId == 1561) return 768;
        if (itemId == 1562) return 769;
        if (itemId == 1563) return 770;
        if (itemId == 1564) return 771;
        if (itemId == 1565) return 772;
        if (itemId == 1566) return 773;
        if (itemId == 7585) return 3507;
        if (itemId == 7584) return 3506;
        if (itemId == 7583) return 3505;
        return 0;
    }

    public boolean isAggressive(int i) {
        if (Boundary.isIn(npcs[i], Boundary.GODWARS_BOSSROOMS)) {
            return true;
        }
        switch (npcs[i].npcType) {
            case 5535:
            case 5867:
            case 8352:
            case 5868:
            case 465:
            case 5869:
            case 5363:
            case 6609:
            case 6342:
            case 6618:
            case 6619:
            case 6611:
            case 2054:
            case 8031:
            case 8091:
            case 8090:
            case 8030:
            case 6615:
            case 2550:
            case 2551:
            case 319:
            case 320:
            case 2562:
            case 2563:
            case 3129:
            case 3132:
            case 3130:
            case 3131:
            case 2205:
            case 2208:
            case 2207:
            case 2206:
            case 6829:
            case 2215:
            case 2218:
            case 2217:
            case 2216:
            case 3163:
            case 3164:
            case 3165:
            case 3162:
            case 494:
            case 498:
            case 3943:
            case 6610:
                return true;
        }
        if (npcs[i].inWild() && npcs[i].MaxHP > 0)
            return true;
        return false;
        // return npcs[i].definition().isAggressive();
    }
    private boolean tryPlayerAggro(NPC npc, int npcId) {
        for (Player p : PlayerHandler.players) {
            if (p == null || p.IsDead || p.heightLevel != npc.heightLevel)
                continue;

            client player = (client) p;
            // 👉 NEW: Do not aggro if player is in the home boundary
            if (Boundary.isIn(player, Boundary.HOME)) {
                continue;
            }
            if (Boundary.isIn(player, Boundary.gh_train)) {
                continue;
            }
            if (Boundary.isIn(player, Boundary.ghr_train)) {
                continue;
            }
            if (Boundary.isIn(player, Boundary.gh_shop_zone)) {
                continue;
            }
            if (player.distanceToPoint(npc.absX, npc.absY) > getDistanceForNpc(npc))
                continue;

            int npcLevel = GetNpcListCombat(npc.npcType);
            int playerLevel = player.combat;

            long now = System.currentTimeMillis();
            boolean canAggro;

            if (npc.alwaysAggressive) {
                canAggro = true;  // Always aggressive NPCs will attack
            } else {
                // Allow high-level NPCs to bypass the npcGetsAnnoyed method
                if (npcLevel > playerLevel * 1.5) {  // Adjust this factor as needed
                    canAggro = true;  // High-level NPCs attack regardless
                } else if (npcGetsAnnoyed(npc)) {
                    canAggro = (playerLevel <= npcLevel * 2) && (now - player.aggrotimer < 600_000);
                } else {
                    canAggro = false;
                }
            }

            if (canAggro) {
                npc.StartKilling = player.index;
                npc.IsUnderAttack = true;
                npc.RandomWalk = false;
                player.underAttackByNpc = npcId;

                attackPlayer(npcId);
                return true;
            }
        }
        return false;
    }

    private boolean tryFactionAggro(NPC npc, NPC other, int otherIndex) {
        if (other == null || other == npc || !other.isAlive()) return false;
        if (npc.heightLevel != other.heightLevel) return false;

        if (misc.goodDistance(npc.getX(), npc.getY(), other.getX(), other.getY(), 1)) {
            if (!sameGodwarsFaction(npc, other)) {
                npc.targetNpcIndex = otherIndex;
                npc.RandomWalk = false;
                return true;
            }
        }
        return false;
    }


    public void handleAggro(int npcId) {
        handleAggro(npcId, -1); // no faction candidate; player-only aggro
    }

    public void handleAggro(int npcId, int candidateOtherIdx) {
        NPC npc = npcs[npcId];
        if (npc == null || !npc.isAlive() || npc.StartKilling > 0 || npc.targetNpcIndex >= 0) return;

        // Throttle scan frequency to once every 3 seconds
        long now = System.currentTimeMillis();
        if (now - npc.lastAggroCheck < 3000) return;
        npc.lastAggroCheck = now;

        boolean gotAggro = tryPlayerAggro(npc, npcId);

        // Only attempt faction aggro if an outer loop handed us a candidate index
        if (!gotAggro && candidateOtherIdx >= 0) {
            NPC other = npcs[candidateOtherIdx];
            gotAggro = tryFactionAggro(npc, other, candidateOtherIdx);
        }

        if (!gotAggro) {
            npc.RandomWalk = true;
            npc.StartKilling = 0;
            npc.targetNpcIndex = -1;
            npc.IsUnderAttack = false;
        }
    }



    private boolean sameGodwarsFaction(NPC a, NPC b) {
        return a.godwarsFaction == b.godwarsFaction;
    }

    public int getDistanceForNpc(NPC npc) {
        if (npc == null) return 1;

        switch (npc.npcType) {
            case 1913:
                return 15;

            case 2705:
            case 2710:
            case 2709:
            case 2708:
            case 2707:
            case 3340:
            case 2702:
            case 2701:
            case 2700:
            case 2698:
            case 2687:
            case 2688:
            case 2689:
            case 2690:
            case 2691:
            case 2692:
            case 2693:
            case 2694:
            case 2695:
            case 2686:
            case 2679:
            case 50:
            case 3500:
            case 1351:
            case 3000:
            case 1155:
            case 1160:
            case 2627:
            case 2630:
            case 2631:
            case 2738:
            case 2741:
            case 2743:
            case 2745:
            case 1472:
                return 100;

            case 2746:
                return 150;

            case 3200:
            case 63:
            case 1459:
            case 111:
            case 125:
            case 59:
            case 91:
            case 912:
            case 913:
            case 914:
            case 78:
            case 941:
            case 82:
            case 83:
            case 1153:
            case 1154:
            case 2263:
            case 2264:
            case 2265:
                return 10;

            case 6261:
            case 6263:
            case 6265:
            case 6222:
            case 6223:
            case 6225:
            case 6227:
            case 6247:
            case 6248:
            case 6250:
            case 6252:
            case 6203:
            case 6204:
            case 6206:
            case 6208:
                return 10;

            default:
                return 10;
        }
    }


    public boolean npcGetsAnnoyed(NPC Npc) {
        switch (Npc.npcType) {
            case 677:
            case 6260:
            case 1588:
            case 191:
            case 1913:
            case 6261:
            case 6263:
            case 6265:
            case 6222:
            case 6223:
            case 6225:
            case 6227:
            case 6247:
                return true;
            case 6248:
                return true;
            case 6250:
                return true;
            case 6252:
                return true;
            case 6203:
                return true;
            case 6204:
            case 6206:
            case 6208:
                return true;
            case 1153:
            case 1154:
            case 1155:
            case 1156:
            case 1157:
                return true;
            case 1160:
                return true;
            case 2881:
            case 2882:
            case 2883:
                return true;
            case 2700:
            case 2707:
            case 2710:
            case 2708:
            case 2679:
            case 2709:
            case 2701:
            case 2705:
            case 2702:
            case 3340:
            case 2698:
            case 2687:
            case 2689:
            case 2694:
            case 3500:
            case 1351:
            case 1338:
            case 50:
            case 3200:
            case 3000:
            case 1459:
            case 2627:
            case 2630:
            case 2631:
            case 2738:
            case 2741:
            case 2743:
            case 2745:
            case 2746:
            case 1472:
            case 63:
            case 111:
            case 125:
            case 59:
            case 91:
            case 912:
            case 913:
            case 914:
            case 78:
            case 941:
            case 82:
            case 83:
            case 2263:
            case 2264:
            case 2265:
                return true;

            default:
                return false;
        }
    }

    private boolean isMageNpc(int npcType) {
        switch (npcType) {
            case 1645:
            case 1241:
            case 1913:
            case 1246:
            case 1159:
            case 54:
            case 8133:
            case 509:
            case 766:
            case 765:
            case 764:
            case 763:
            case 762:
            case 761:
            case 768:
            case 769:
            case 770:
            case 771:
            case 772:
            case 773:
            case 3507:
                return true;
            default:
                return false;
        }
    }

    public static boolean isEnemy(NPC npc, NPC target) {
        return npc.isGodwarsNpc && target.isGodwarsNpc
                && npc.godwarsFaction != -1
                && npc.godwarsFaction != target.godwarsFaction;
    }

    public void processGodwarsTargeting(int npcId) {
        NPC attacker = npcs[npcId];
        if (attacker == null || attacker.IsDead || !attacker.isGodwarsNpc || attacker.targetNpcIndex != -1)
            return;

        for (int i = 0; i < maxNPCs; i++) {
            NPC target = npcs[i];
            if (target == null || target == attacker || target.IsDead)
                continue;

            if (attacker.heightLevel == target.heightLevel &&
                    misc.goodDistance(attacker.absX, attacker.absY, target.absX, target.absY, 10) &&
                    isEnemy(attacker, target)) {

                attacker.targetNpcIndex = i;
                attacker.RandomWalk = false;
                break;
            }
        }
    }

    public void getRealRandomWalk(NPC npc) {
        if (!npc.RandomWalk || npc.freezeTimer > 0) return;

        // Only try to walk every few ticks (optional)
        if (server.tick % 5 != 0) return;

        int[][] directions = {
                {1, 0}, {-1, 0}, {0, 1}, {0, -1},
                {1, 1}, {-1, -1}, {1, -1}, {-1, 1}
        };

        Collections.shuffle(Arrays.asList(directions)); // Randomize directions

        for (int[] dir : directions) {
            int newX = npc.absX + dir[0];
            int newY = npc.absY + dir[1];

            if (!isTileWalkable(newX, newY, npc.heightLevel)) continue;
            if (isTileOccupied(newX, newY, npc.heightLevel)) continue; // Optional

            npc.moveX = dir[0];
            npc.moveY = dir[1];
            return;
        }

        // No valid direction found — don't move this tick
        npc.moveX = 0;
        npc.moveY = 0;
    }

    public void walkTowardsTarget(NPC attacker, NPC target) {
        int dx = target.absX - attacker.absX;
        int dy = target.absY - attacker.absY;

        // Prevent overlap
        if (Math.abs(dx) <= 1 && Math.abs(dy) <= 1) return;

        int moveX = Integer.signum(dx);
        int moveY = Integer.signum(dy);

        int nextX = attacker.absX + moveX;
        int nextY = attacker.absY + moveY;

        if (!isTileOccupied(nextX, nextY, attacker.heightLevel)) {
            attacker.moveX = moveX;
            attacker.moveY = moveY;
            attacker.getNextWalkingDirection(); // Applies the move
            attacker.faceNPC(attacker.targetNpcIndex);
            attacker.randomWalk = false;
        } else {
            // Optional: try to sidestep or fallback logic
            attacker.randomWalk = false;
        }
    }

    public static boolean isTileOccupied(int x, int y, int z) {
        if (z < 0 || z >= MAX_HEIGHT || x < 0 || x >= MAX_MAP_X || y < 0 || y >= MAX_MAP_Y) return false;
        int localX = x % 104;
        int localY = y % 104;
        return npcOccupied[z][localX][localY];
    }

    private static final int[][] hansPath = {
            {3226, 3218}, {3225, 3218}, {3224, 3218}, {3223, 3218}, {3222, 3218}, {3221, 3218},
            {3220, 3218}, {3219, 3218}, {3218, 3218}, {3217, 3218}, {3216, 3219}, {3215, 3220},
            {3215, 3221}, {3215, 3222}, {3215, 3223}, {3215, 3224}, {3215, 3225}, {3215, 3226},
            {3215, 3227}, {3216, 3228}, {3217, 3229}, {3218, 3230}, {3219, 3231}, {3220, 3231},
            {3221, 3231}, {3222, 3231}, {3223, 3231}, {3224, 3230}, {3225, 3229}, {3226, 3228},
            {3227, 3227}, {3227, 3226}, {3227, 3225}, {3227, 3224}, {3227, 3223}, {3226, 3222},
            {3225, 3221}, {3224, 3220}, {3223, 3219}, {3222, 3218}
    };

    public static void processHansWalking(NPC npc) {
        if (npc.hansWalkTimer > 0) {
            npc.hansWalkTimer--;
            return;
        }

        if (npc.walkingQueue == null || npc.walkingQueue.isEmpty()) {
            int[] target = hansPath[npc.hansWaypointIndex];

            if (npc.absX == target[0] && npc.absY == target[1]) {
                npc.hansWaypointIndex = (npc.hansWaypointIndex + 1) % hansPath.length;
                npc.hansWalkTimer = 1;
                return;
            }

            LinkedList<int[]> path = NpcPathFinder.findPathForNpc(npc, target[0], target[1]);
            if (path.isEmpty()) {
                npc.forceChat("Can't get through...");
                npc.hansWalkTimer = 3;
                return;
            }

            npc.walkingQueue = new LinkedList<>();
            npc.walkingQueue.addAll(path);
        }

        if (!npc.walkingQueue.isEmpty()) {
            int[] nextStep = npc.walkingQueue.poll();
            int dx = Integer.compare(nextStep[0], npc.absX);
            int dy = Integer.compare(nextStep[1], npc.absY);

            npc.moveX = dx;
            npc.moveY = dy;
            npc.walkingType = 1;
            npc.updateRequired = true;
            npc.direction = npc.getNextWalkingDirection();
        }

        npc.hansWalkTimer = 1;
    }


    /*   public void process() {

           try {
               for (int i = 0; i < maxNPCs; i++) {
                   if (npcs[i] == null)
                       continue;
                   npcs[i].clearUpdateFlags();

               }

               for (int i = 0; i < maxNPCs; i++) {
                   if (npcs[i] != null) {
   // Cleaned core NPC logic (movement, poison, summoning, and basic walk/turn actions)
                       NPC attacker = npcs[i];
                       if (attacker == null || attacker.IsDead || !attacker.isGodwarsNpc)
                           continue;

                       // Step 1: Acquire target if none and cooldown allows
                       if (attacker.targetNpcIndex == -1 && attacker.targetScanCooldown-- <= 0) {
                           attacker.targetScanCooldown = misc.random(4) + 3; // 3–6 tick cooldown
                           int closestDist = Integer.MAX_VALUE;
                           int closestIndex = -1;

                           for (int j = 0; j < maxNPCs; j++) {
                               NPC candidate = npcs[j];
                               if (candidate == null || candidate == attacker || candidate.IsDead || !candidate.isGodwarsNpc)
                                   continue;

                               if (candidate.godwarsFaction == attacker.godwarsFaction)
                                   continue;

                               if (candidate.absX == attacker.absX && candidate.absY == attacker.absY)
                                   continue;

                               int dist = (int) misc.distance(attacker.absX, attacker.absY, candidate.absX, candidate.absY);
                               if (dist < closestDist) {
                                   closestDist = dist;
                                   closestIndex = j;
                               }
                           }

                           if (closestIndex != -1) {
                               attacker.targetNpcIndex = closestIndex;
                           } else {
                               attacker.randomWalk = true;
                           }
                       }
                       if (attacker.targetNpcIndex < 0 || attacker.targetNpcIndex >= maxNPCs) {
                           attacker.targetNpcIndex = -1;
                           attacker.randomWalk = true;
                           continue;
                       }
                       // Step 2: Attack logic
                       NPC targetnpc = npcs[attacker.targetNpcIndex];
                       if (targetnpc == null || targetnpc.IsDead || targetnpc.godwarsFaction == attacker.godwarsFaction) {
                           attacker.targetNpcIndex = -1;
                           attacker.randomWalk = true;
                           continue;
                       }

                       // Step 3: Attack or move
                       int dx = targetnpc.absX - attacker.absX;
                       int dy = targetnpc.absY - attacker.absY;

                       if (attacker.attackTimer-- <= 0) {
                           if (Math.abs(dx) <= 1 && Math.abs(dy) <= 1) {
                               NpcVsNpc.attack(attacker, targetnpc);
                               attacker.attackTimer = misc.random(3) + 2; // Wait 2–5 ticks before next attack
                               attacker.faceNPC(attacker.targetNpcIndex);
                               attacker.randomWalk = false;
                           } else {
                               walkTowardsTarget(attacker, targetnpc);
                           }
                       }
                       NPC npc = npcs[i];
                       if (npc == null) return;
                       // HANS PATHING LOGIC
                       if (npc.npcType == 0) { // Hans ID (adjust if different)
                           processHansWalking(npc);
                       }
                       client owner = (client) server.playerHandler.players[npc.summonedBy];

                       if (npc.summoner) {
                           if (owner == null) {
                               npc.absX = npc.absY = 0;
                           } else if (owner.hasNpc && (!owner.goodDistance(npc.getX(), npc.getY(), owner.absX, owner.absY, 15)
                                   || owner.heightLevel != npc.heightLevel)) {
                               npc.absX = owner.absX;
                               npc.absY = owner.absY;
                               npc.heightLevel = owner.heightLevel;
                           }
                       }
                       if (npc.actionTimer > 0) {
                           npc.actionTimer--;
                       }

                       if (npc.hitDelayTimer > 0) {
                           npc.hitDelayTimer--;

                           if (npc.hitDelayTimer == 0 && npc.pendingDamage > 0 && npc.StartKilling > 0) {
                               client p = (client) server.playerHandler.players[npc.StartKilling];
                               if (p != null && !p.IsDead) {
                                   p.hitDiff = npc.pendingDamage;
                                   p.hitUpdateRequired = true;
                                   p.updateRequired = true;
                                   p.appearanceUpdateRequired = true;

                                   // Apply damage to HP
                                   p.NewHP -= npc.pendingDamage;
                                   if (p.NewHP <= 0) {
                                       p.NewHP = 0;
                                       p.IsDead = true;
                                       // Trigger death handler if needed here
                                   }
                               }
                               npc.pendingDamage = 0;
                           }
                       }


                       if (npc.npcCombatDelay > 0)
                           npc.npcCombatDelay--;
                       Poison(i);
                       if (npc != null && npc.isAlive() && !npc.IsUnderAttack && npc.StartKilling <= 0 && npc.targetNpcIndex == -1) {
                               handleAggro(i);
                       }

                       npc.PoisonDelay--;
                       if (npc.PoisonClear >= 15) npc.PoisonDelay = 9999999;

   // Despawn if summoner is gone
                       if (npc.spawnedBy > 0) {
                           client spawner = (client) PlayerHandler.players[npc.spawnedBy];
                           if (spawner == null || spawner.heightLevel != npc.heightLevel || spawner.respawnTimer > 0
                                   || !spawner.goodDistance(npc.getX(), npc.getY(), spawner.getX(), spawner.getY(), 20)) {
                               npcs[i] = null;
                               return;
                           }
                       }
                       if (npc == null)
                           continue;
                       if (npc.moveX != 0 || npc.moveY != 0) {
                           npc.move();
                       }
   // NPC walk direction logic
                       if (npc.walkingType >= 2 && npc.walkingType <= 9) {
                           int[][] directions = {
                                   {}, {}, {0, 1}, {0, -1}, {1, 0}, {-1, 0}, {-1, 1}, {1, 1}, {-1, -1}, {1, -1}
                           };
                           int[] dir = directions[npc.walkingType];
                           npc.TurnNpcTo(npc.absX + dir[0], npc.absY + dir[1]);
                       }
                       if (npc.RandomWalk) {
                           getRealRandomWalk(npc);
                       }
   // Special respawn behavior (flower patch NPCs)
                       if (!npc.IsDead && (npc.npcType == 1268 || npc.npcType == 1266)) {
                           for (int j = 1; j < PlayerHandler.maxPlayers; j++) {
                               client p = (client) server.playerHandler.players[j];
                               if (p != null && GoodDistance(npc.absX, npc.absY, p.absX, p.absY, 2) && !npc.IsClose) {
                                   npc.actionTimer = 2;
                                   npc.IsClose = true;
                               }
                           }

                           if (npc.actionTimer == 0 && npc.IsClose) {
                               for (Player p : PlayerHandler.players) {
                                   if (p != null) ((client)p).RebuildNPCList = true;
                               }

                               if (npc.Respawns) {
                                   newNPC(npc.npcType - 1, npc.makeX, npc.makeY, npc.heightLevel,
                                           npc.moverangeX1, npc.moverangeY1, npc.moverangeX2,
                                           npc.moverangeY2, npc.walkingType, npc.MaxHP, true);
                                   npcs[i] = null;
                               }
                           }
                       }

   // Basic attack logic by combat type
                           if (!npc.IsDead && npc.StartKilling > 0) {
                               client target = (client) server.playerHandler.players[npc.StartKilling];

                               if (target != null && !target.IsDead && target.heightLevel == npc.heightLevel) {
                                   // In attack state
                                   if (npc.actionTimer <= 0) {
                                       // Within distance
                                       if (misc.goodDistance(npc.absX, npc.absY, target.absX, target.absY, getDistanceForNpc(npc))) {
                                           attackPlayer(i); // Retry attack when cooldown is up
                                           npc.IsUnderAttack = true;
                                           npc.RandomWalk = false;
                                       } else {
                                           // Too far away
                                           npc.RandomWalk = true;
                                       }
                                   }
                               } else {
                                   resetAttackPlayer(i); // Lost player or invalid
                               }
                           } else if (npc.followingPlayer && npc.followPlayer > 0) {
                               client target = (client) server.playerHandler.players[npc.followPlayer];
                               if (target != null) {
                                   if (target.AttackingOn > 0) {
                                       npc.StartKilling = target.AttackingOn;
                                       npc.RandomWalk = true;
                                       npc.IsUnderAttack = true;

                                           attackPlayer(i);
                                   } else {
                                       FollowPlayer(i);
                                   }
                               }
                           } else if (npc.followingPlayer && npc.followPlayer > 0) {
                               client target = (client) server.playerHandler.players[npc.followPlayer];
                               if (target != null && target.currentHealth > 0 && target.attacknpc > 0) {
                                   npc.attacknpc = target.attacknpc;
                                   npc.IsUnderAttackNpc = npcs[npc.attacknpc].IsUnderAttackNpc = true;
                                   if (isMageNpc(npc.npcType)) {
                                       AttackNPCMage(i);
                                   } else {
                                       AttackNPC(i);
                                   }
                               } else {
                                   FollowPlayer(i);
                               }
                           } else if (npc.IsUnderAttackNpc) {
                               if (isMageNpc(npc.npcType)) {
                                   AttackNPCMage(i);
                               } else {
                                   AttackNPC(i);
                               }
                           }

                           if(npc.npcType == 8352){
                               if(npc.HP <= 1500){
                                   npc.requestTransform(8351);
                                   //NPCHandler.npc.gfx100(1885);
                               } else if(npc.HP <= 1000){
                                   npc.requestTransform(8350);
                                   //NPCHandler.npc.gfx100(1885);
                               } else if(npc.HP <= 500){
                                  npc.requestTransform(8352);
                                   //NPCHandler.npc.gfx100(1885);
                               }
                           }
                           if(npc.npcType == 6268 || npc.npcType == 6269 || npc.npcType == 6270 || npc.npcType == 6271 || npc.npcType == 6272 || npc.npcType == 6273 || npc.npcType == 6274 || npc.npcType == 6275 || npc.npcType == 6276 || npc.npcType == 6277 || npc.npcType == 6278 || npc.npcType == 6279 || npc.npcType == 6280 || npc.npcType == 6281 || npc.npcType == 6282 || npc.npcType == 6283){
                               npc.isGodwarsNpc = true;
                               npc.godwarsFaction = 3; // bandos, for example
                           }
                           if(npc.npcType == 6229 || npc.npcType == 6230 || npc.npcType == 6231 || npc.npcType == 6232 || npc.npcType == 6233 || npc.npcType == 6234 || npc.npcType == 6235 || npc.npcType == 6236 || npc.npcType == 6237 || npc.npcType == 6238 || npc.npcType == 6239 || npc.npcType == 6240 || npc.npcType == 6241 || npc.npcType == 6242 || npc.npcType == 6243 || npc.npcType == 6244 || npc.npcType == 6245 || npc.npcType == 6246){
                               npc.isGodwarsNpc = true;
                               npc.godwarsFaction = 2; // ammy, for example
                           }
                           if(npc.npcType == 6210 || npc.npcType == 6211 || npc.npcType == 6212 || npc.npcType == 6213 || npc.npcType == 6214 || npc.npcType == 6215 || npc.npcType == 6216 || npc.npcType == 6217  || npc.npcType == 6218 || npc.npcType == 6219  || npc.npcType == 6220  || npc.npcType == 6221){
                               npc.isGodwarsNpc = true;
                               npc.godwarsFaction = 1; // Zamorak, for example
                           }
                           if(npc.npcType == 6254 || npc.npcType == 6255 || npc.npcType == 6256 || npc.npcType == 6257 || npc.npcType == 6258 || npc.npcType == 6259){
                               npc.isGodwarsNpc = true;
                               npc.godwarsFaction = 0; // saradomin, for example
                           }
                           if (npc.npcType == 81 || npc.npcType == 397
                                   || npc.npcType == 1766
                                   || npc.npcType == 1767
                                   || npc.npcType == 1768) {
                               if (misc.random2(50) == 1) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Moo";
                               }
                           }
                           if (npc.npcType == 8172) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Talk to me to start Desert Treasure!";
                               }
                           }
                           if (npc.npcType == 619) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "This is as close as im getting.";
                               }
                           }
                           if (npc.npcType == 246) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Do ::inside Sir!";
                               }
                           }
                           if (npc.npcType == 532) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Member Shop!";
                               }
                           }
                           if (npc.npcType == 3005) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Ladder For Mems Only! Get's you behind edge bank!";
                               }
                           }
                           if (npc.npcType == 3006) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Come In if your Mem!, if not go to www.Ghreborn.com";
                               }
                           }
                           if (npc.npcType == 660) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "We are the knights of the party room!";
                               }
                           }
                           if (npc.npcType == 2478) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Nigger You Got Jailed";
                               }
                           }
                           if (npc.npcType == 2478) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Don't Think Of Logging Out";
                               }
                           }
                           if (npc.npcType == 2478) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "or Asking To get unjailed";
                               }
                           }
                           if (npc.npcType == 2478) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Ur Here Because You Was not Being Good To Others";
                               }
                           }
                           if (npc.npcType == 2478) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Think What U have Done!!";
                               }
                           }
                           if (npc.npcType == 660) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Here to Party 24/7!";
                               }
                           }
                           if (npc.npcType == 364) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Mod & Admin Portal Only!";
                               }
                           }
                           if (npc.npcType == 280) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Strength Guild, 99 strength to Enter!";
                               }
                           }
                           if (npc.npcType == 172) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Range/Magic Guild, 99 Range and Magic to Enter!";
                               }
                           }
                           if (npc.npcType == 212) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Welcome to the Mod/Admin Zone..Keep up the Good Work!";
                               }
                           }
                           if (npc.npcType == 945) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Talk to me to learn about the server.";
                               }
                           }
                           if (npc.npcType == 225) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Icon Minigame!";
                               }
                           }
                           if (npc.npcType == 648) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Welcome to Training Made To Own N33bs!";
                               }
                           }
                           if (npc.npcType == 793) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Enchanted Minigame!";
                               }
                           }
                           if (npc.npcType == 2253) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Clan Wars Portal!!";
                               }
                           }
                           if (npc.npcType == 541) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Skill Cape Shop!";
                               }
                           }
                           if (npc.npcType == 1713) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "We are the righteous ones in his eyes alone.";
                               }
                           }
                           if (npc.npcType == 1713) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Let them not infest our cities and towns...";
                               }
                           }
                           if (npc.npcType == 1713) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "And lo, we become the power, indeed the force to stop these monsters in their tracks.";
                               }
                           }
                           if (npc.npcType == 1713) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Verily I urge you, my friends to take up your spades and farm your farms to feed our people in this blessed sanctuary.";
                               }
                           }
                           if (npc.npcType == 1713) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "If thine monsters visage does frighten thee, then tear it off I say... tear it off!";
                               }
                           }
                           if (npc.npcType == 1713) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "For Saradomin will guide our sword arms and smash the enemies of humans till their bones become dust.";
                               }
                           }
                           if (npc.npcType == 1713) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "And let us smite these monsters unto their deaths.";
                               }
                           }
                           if (npc.npcType == 1713) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "For they are not the chosen ones in Saradomin's eyes.";
                               }
                           }
                           if (npc.npcType == 1713) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Where do we go for safety from these monsters... here, my brethren!";
                               }
                           }
                           if (npc.npcType == 1713) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "And let there be no cave or shelter for their spawn until the end of days.";
                               }
                           }
                           if (npc.npcType == 2821) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Fishing Portal!";
                               }
                           }
                           if (npc.npcType == 2304) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Farming Shop!By seed's for patch's!";
                               }
                           }
                           if (npc.npcType == 461) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Magic Shop!";
                               }
                           }
                           if (npc.npcType == 57) {
                               if (misc.random2(30) == 1) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   int players = PlayerHandler.getPlayerCount();
                                   npc.textUpdate = "Players Online: " + players;
                               }
                           }
                           if (npc.npcType == 8206) {
                               if (misc.random2(30) == 1) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   int lottery = server.lottery.lotteryFund / 1000000;
                                   npc.textUpdate = "Lottery is at " + lottery + "m";
                               }
                           }
                           if (npc.npcType == 550) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Pk Shop!";
                               }
                           }
                           if (npc.npcType == 1759) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Shops Here!";
                               }
                           }
                           if (npc.npcType == 1699) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Pur3 Sh0p!";
                               }
                           }
                           if (npc.npcType == 2475) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Training Portal!";
                               }
                           }
                           if (npc.npcType == 28) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Train Your Skills Here!";
                               }
                           }
                           if (npc.npcType == 1917) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Rune Armor Shop!!!";
                               }
                           }
                           if (npc.npcType == 522) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "General Store!";
                               }
                           }
                           if (npc.npcType == 522) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Great deals Here!";
                               }
                           }
                           if (npc.npcType == 548) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Gloves, Robes, Boots Shop!";
                               }
                           }
                           if (npc.npcType == 530) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Skillers Shop!";
                               }
                           }
                           if (npc.npcType == 528) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Woodcutting Store!!";
                               }
                           }
                           if (npc.npcType == 949) {
                               if (misc.random2(30) <= 3) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Welcome to Moderator Island!";
                               }
                           }
                           if (npc.npcType == 2244) {
                               if (misc.random2(30) <= 3) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Welcome to Moderator Island!";
                               }
                           }
                           if (npc.npcType == 213) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "You need the frozen key to get in this portal! Kill the troll for key!";
                               }
                           }
                           if (npc.npcType == 555) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Random Stoof!";
                               }
                           }
                           if (npc.npcType == 561) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Hood Shop!";
                               }
                           }
                           if (npc.npcType == 538) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Slayer Shop!";
                               }
                           }
                           if (npc.npcType == 529) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Farming Shop!";
                               }
                           }

                           if (npc.npcType == 3117) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Click the chests for slayer exp ..";
                               }
                           }
                           if (npc.npcType == 866) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Et .. Phone .. Home!";
                               }
                           }
                           if (npc.npcType == 549) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Smithin' Shop";
                               }
                           }
                           if (npc.npcType == 558) {
                               if (misc.random2(30) <= 2) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Herblore shop!!";
                               }
                           }
                           if (npc.npcType == 1552) {
                               if (misc.random2(30) == 1) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Merry Christmas!!!";
                               }
                           }
                           List<Player> nearbyPlayers = PlayerHandler.getNearbyPlayers(npc.absX, npc.absY, npc.heightLevel, 10);

                           for (Player p : nearbyPlayers) {
                               client person = (client) p;

                               if (p != null) {
                                   if (person.distanceToPoint(npc.absX,
                                           npc.absY)
                                           >= 5) {
                                       if (npc.npcType != 1160
                                               || npc.npcType == 2745 || npc.npcType == 1115 || npc.npcType == 50 || npc.npcType == 3425 || npc.npcType == 53 || npc.npcType == 3847 || npc.npcType == 1558 || npc.npcType == 40 || npc.npcType == 2837 || npc.npcType == 8133 || npc.npcType == 3425) {
                                           npc.RandomWalk = true;
                                       }
                                   }
                               }
                           }
                           for (Player p : nearbyPlayers) {
                               if (p == null || p.heightLevel != npc.heightLevel)
                                   continue;

                               client person = (client) p;
                               int dist = person.distanceToPoint(npc.absX, npc.absY);

                               // Low distance (aggressive NPCs)
                               if (dist <= 2) {
                                   if ((npc.npcType == 2745 || npc.npcType == 1158) && npc.IsUnderAttack) {
                                       npc.StartKilling = person.playerId;
                                       npc.RandomWalk = false; // attack nearby target
                                   } else if ((npc.npcType == 2745 || npc.npcType == 1158) && !npc.IsUnderAttack) {
                                       npc.RandomWalk = true;
                                       npc.IsUnderAttack = false; // disengage
                                   }
                               }

                               // High distance fallback
                               if (dist >= 127) {
                                   if ((npc.npcType == 2745 || npc.npcType == 1158 || npc.npcType == 8352) && npc.IsUnderAttack) {
                                       npc.StartKilling = person.playerId;
                                       npc.RandomWalk = false; // teleport attack maybe?
                                   }
                               }
                           }
                           if (npc.npcType == 1451) {
                               if (misc.random2(30) == 1) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Tele to varrock";
                               }
                           }
                           if (npc.npcType == 33) {
                               if (misc.random2(30) == 1) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Tele to varrock";
                               }
                           }
                           if (npc.npcType == 37) {
                               if (misc.random2(30) == 1) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Welcome To Edgeville...";
                               }
                           }

                           if (npc.npcType == 1201) {
                               if (misc.random2(30) == 1) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "DO YOU DARE ENTER THE BLACK DRAGONS LAIR?";
                               }
                           }

                           if (npc.npcType == 1199) {
                               if (misc.random2(30) == 1) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "GO THROUGH THIS DOOR TO TELEPORT TO THE BLACK DRAGON CAVE";
                               }
                           }

                           if (npc.npcType == 2301) {
                               if (misc.random2(30) == 1) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Tele to the monkey training area";
                               }
                           }
                           if (npc.npcType == 1659) {
                               if (misc.random2(30) == 1) {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "Go to www.projectdestiny.co.nr to buy and sell things!";
                               }
                           } else if (npc.npcType == 3832) {
                               if (misc.random2(50) <= 3) // this is the time delay
                               {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   npc.textUpdate = "I shoulda never sold that crack! Ho HO HO!!";
                               }
                           } else if (npc.npcType == 3832) {
                               if (misc.random2(10) <= 3) // this is the time delay
                               {
                                   npc.updateRequired = true;
                                   npc.textUpdateRequired = true;
                                   if (PlayerHandler.isPlayerOn("Sgsrocks")) {
                                       npc.textUpdate = "Sgsrocks is ONLINE";
                                   } else {
                                       npc.textUpdate = "Sgsrocks is OFFLINE";
                                   }
                               }
                           } else if (npc.IsDead) {// Get killer if not already set

                           if (npc.actionTimer == 0 && !npc.DeadApply && !npc.NeedRespawn) {
                               int killerIndex = npc.StartKilling;
                            // Immediately block further combat and interaction
                               npc.killedBy =  getNpcKillerId(i);
                               npc.IsUnderAttack = false;
                               npc.IsUnderAttackNpc = false;
                               client target = (client) PlayerHandler.players[killerIndex];
                               if (target != null) {
                                   target.sendSound(getNpcDeathSound(npc.npcType), 4, 0);
                               }
                               npc.animNumber = getNpcDeathAnimation(npc.npcType);
                               npc.updateRequired = true;
                               npc.animUpdateRequired = true;
                               npc.DeadApply = true;
                               npc.actionTimer = AnimationLength.getFrameLength(npc.animNumber);

                               if (npc.followingPlayer && server.playerHandler.players[npc.followPlayer] != null) {
                                   server.playerHandler.players[npc.followPlayer].summonedNPCS--;
                               }

                           } else if (npc.actionTimer == 0 && npc.DeadApply && !npc.NeedRespawn) {
                               int killerIndex = npc.killedBy;
                               int dropX = npc.absX, dropY = npc.absY;

                               // Skip drops if killed by NPC
                               if (npc.targetNpcIndex == -1 && killerIndex >= 0 && killerIndex < PlayerHandler.players.length) {
                                   // --- Random drops ---
                                   if (misc.random(25) == 0) {
                                       ItemHandler.addItem(ItemIDs.MYSTERY_BOX, dropX, dropY, 1, killerIndex, false);
                                       broadcastDrop(killerIndex, "Mystery box");
                                   }

                                   if (misc.random(35) == 0) {
                                       ItemHandler.addItem(ItemIDs.PRESENT, dropX, dropY, 1, killerIndex, false);
                                       broadcastDrop(killerIndex, "Present");
                                   }

                                   if (misc.random(20) == 1) {
                                       int combat = GetNpcListCombat(npc.npcType);
                                       int box = combat >= 126 ? 13003 : combat >= 96 ? 13002 : combat >= 62 ? 13001 : -1;
                                       if (box > 0) {
                                           ItemHandler.addItem(box, dropX, dropY, 1, killerIndex, false);
                                       }
                                   }

                                   // --- Slayer & Guild ---
                                   client target = (client) PlayerHandler.players[killerIndex];
                                   if (target != null) {
                                       target.getSlayer().killTaskMonster(npc);
                                       target.getWarriorsGuild().dropDefender(dropX, dropY);

                                       if (AnimatedArmour.isAnimatedArmourNpc(npc.npcId)) {
                                           AnimatedArmour.dropTokens(target, npc.npcType, dropX, dropY);
                                       }
                                   }

                                   dropItems(i);
                                   MonsterDropItem(i);
                               }

                               npc.NeedRespawn = true;
                               npc.actionTimer = 60;
                               npc.absX = npc.makeX;
                               npc.absY = npc.makeY;
                               npc.HP = npc.MaxHP;
                               npc.updateRequired = true;
                               npc.animUpdateRequired = true;
                           } else if (npc.actionTimer == 0 && npc.NeedRespawn) {
                               if (npc.Respawns) {
                                   int type = (npc.npcType == 1267 || npc.npcType == 1265) ? npc.npcType + 1 : npc.npcType;
                                   newNPC(type, npc.makeX, npc.makeY, npc.heightLevel,
                                           npc.moverangeX1, npc.moverangeY1, npc.moverangeX2,
                                           npc.moverangeY2, npc.walkingType, npc.MaxHP, true);
                                   npcs[i] = null;
                               }
                           }
                       }

                   }
               }
           } catch(Exception e){
               e.printStackTrace();
           }
       }*/
    public void process() {
        try {
            for (int i = 0; i < maxNPCs; i++) {
                NPC npc = npcs[i];
                if (npc != null)
                    npc.clearUpdateFlags();
            }

            Map<Integer, List<NPC>> godwarsFactions = new HashMap<>();
            for (int j = 0; j < maxNPCs; j++) {
                NPC other = npcs[j];
                if (other != null && other.isGodwarsNpc && !other.IsDead) {
                    godwarsFactions.computeIfAbsent(other.godwarsFaction, k -> new ArrayList<>()).add(other);
                }
            }

            for (int i = 0; i < maxNPCs; i++) {
                NPC npc = npcs[i];
                if (npc == null) continue;
                handleGodwarsNpcCombat(npc, i, godwarsFactions); // 👈 your new per-NPC combat call
                handleSummoningSync(npc);
                handleTickTimers(npc, i);
                handleAggroIfIdle(npc, i);
            }

            for (int i = 0; i < maxNPCs; i++) {
                NPC npc = npcs[i];
                if (npc == null) continue;
                handleCombat(npc, i);
            }

            for (int i = 0; i < maxNPCs; i++) {
                NPC npc = npcs[i];
                if (npc == null) continue;
                handleMovement(npc);
            }

            for (int i = 0; i < maxNPCs; i++) {
                NPC npc = npcs[i];
                if (npc == null) continue;
                updateNpcTextIfNeeded(npc);
            }

            for (int i = 0; i < maxNPCs; i++) {
                NPC npc = npcs[i];
                if (npc == null) continue;
                handleNpcDeathAndRespawn(npc, i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleGodwarsNpcCombat(NPC attacker, int attackerIndex, Map<Integer, List<NPC>> factions) {
        if (!attacker.isGodwarsNpc || attacker.IsDead) return;

        // Scan for a new target if needed
        if (attacker.targetNpcIndex == -1 && attacker.targetScanCooldown-- <= 0) {
            attacker.targetScanCooldown = misc.random(4) + 3;
            int closestDist = Integer.MAX_VALUE;
            NPC closest = null;

            for (Map.Entry<Integer, List<NPC>> entry : factions.entrySet()) {
                if (entry.getKey() == attacker.godwarsFaction) continue;
                for (NPC target : entry.getValue()) {
                    if (target.IsDead) continue;
                    int dist = (int) misc.distance(attacker.absX, attacker.absY, target.absX, target.absY);
                    if (dist < closestDist) {
                        closestDist = dist;
                        closest = target;
                    }
                }
            }

            if (closest != null) {
                attacker.targetNpcIndex = closest.npcId;
                attacker.randomWalk = false;
            } else {
                attacker.targetNpcIndex = -1;
                attacker.randomWalk = true;
            }
        }

        // Skip if invalid target
        if (attacker.targetNpcIndex < 0 || attacker.targetNpcIndex >= maxNPCs) return;

        NPC target = npcs[attacker.targetNpcIndex];
        if (target == null || target.IsDead || target.godwarsFaction == attacker.godwarsFaction) return;

        int dx = target.absX - attacker.absX;
        int dy = target.absY - attacker.absY;

        if (attacker.attackTimer-- <= 0) {
            if (Math.abs(dx) <= 1 && Math.abs(dy) <= 1) {
                NpcVsNpc.attack(attacker, target);
                attacker.attackTimer = misc.random(3) + 2;
                attacker.faceNPC(attacker.targetNpcIndex);
                attacker.randomWalk = false;
            } else {
                walkTowardsTarget(attacker, target);
            }
        }
    }

    private void handleSummoningSync(NPC npc) {
        client slaveOwner = (client) PlayerHandler.players[npc.summonedBy];
        if (slaveOwner == null && npc.summoner) {
            npc.absX = 0;
            npc.absY = 0;
        }
        if (slaveOwner != null
                && slaveOwner.hasNpc
                && !slaveOwner.goodDistance(npc.getX(),
                npc.getY(), slaveOwner.absX,
                slaveOwner.absY, 15) && npc.summoner) {
            npc.absX = slaveOwner.absX;
            npc.absY = slaveOwner.absY - 1;
        }

        if (slaveOwner != null && slaveOwner.hasNpc && npc.summoner) {
            if (slaveOwner.goodDistance(npc.absX, npc.absY, slaveOwner.absX, slaveOwner.absY, 15)) {
                server.npcHandler.followPlayer(npc.npcId, slaveOwner.index);
            }
        }

    }

    private void handleTickTimers(NPC npc, int index) {
        if (npc.actionTimer > 0) npc.actionTimer--;
        if (npc.hitDelayTimer > 0) {
            npc.hitDelayTimer--;
            if (npc.hitDelayTimer == 0 && npc.pendingDamage > 0 && npc.StartKilling > 0) {
                client p = (client) server.playerHandler.players[npc.StartKilling];
                if (p != null && !p.IsDead) {
                    p.hitDiff = npc.pendingDamage;
                    p.hitUpdateRequired = true;
                    p.updateRequired = true;
                    p.appearanceUpdateRequired = true;
                    p.NewHP = Math.max(0, p.NewHP - npc.pendingDamage);
                    if (p.NewHP == 0) p.IsDead = true;
                }
                npc.pendingDamage = 0;
            }
        }
        if (npc.npcCombatDelay > 0) npc.npcCombatDelay--;
        Poison(index);
        npc.PoisonDelay--;
        if (npc.PoisonClear >= 15) npc.PoisonDelay = 9999999;
    }

    private void handleAggroIfIdle(NPC npc, int index) {
        if (npc.isAlive() && !npc.IsUnderAttack && npc.StartKilling <= 0 && npc.targetNpcIndex == -1) {
            handleAggro(index);
        }
    }

    private void handleCombat(NPC npc, int index) {
        if (npc.IsDead) return;
        if (npc.StartKilling > 0) {
            client target = (client) server.playerHandler.players[npc.StartKilling];
            if (target != null && !target.IsDead && target.heightLevel == npc.heightLevel) {
                if (npc.actionTimer <= 0 && misc.goodDistance(npc.absX, npc.absY, target.absX, target.absY, getDistanceForNpc(npc))) {
                    attackPlayer(index);
                    npc.IsUnderAttack = true;
                    npc.RandomWalk = false;
                }
            } else {
                resetAttackPlayer(index);
            }
        } else if (npc.followingPlayer && npc.followPlayer > 0) {
            client target = (client) server.playerHandler.players[npc.followPlayer];
            if (target != null) {
                if (target.AttackingOn > 0) {
                    npc.StartKilling = target.AttackingOn;
                    npc.RandomWalk = true;
                    npc.IsUnderAttack = true;
                    attackPlayer(index);
                } else {
                    followPlayer(index, target.index);
                }
            }
        } else if (npc.IsUnderAttackNpc) {
            if (isMageNpc(npc.npcType)) {
                AttackNPCMage(index);
            } else {
                AttackNPC(index);
            }
        }
    }

    private void handleMovement(NPC npc) {
        if (npc.moveX != 0 || npc.moveY != 0) npc.move();
        if (npc.walkingType >= 2 && npc.walkingType <= 9) {
            int[][] dirs = {{}, {}, {0,1},{0,-1},{1,0},{-1,0},{-1,1},{1,1},{-1,-1},{1,-1}};
            int[] dir = dirs[npc.walkingType];
            npc.TurnNpcTo(npc.absX + dir[0], npc.absY + dir[1]);
        }
        if (npc.RandomWalk) getRealRandomWalk(npc);
    }

    private void updateNpcTextIfNeeded(NPC npc) {
        // Replace this with a cleaner lookup map of npcType → List<String> messages
        if (npc.npcType == 81 || npc.npcType == 397
                || npc.npcType == 1766
                || npc.npcType == 1767
                || npc.npcType == 1768) {
            if (misc.random2(50) == 1) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "Moo";
            }
        }
        if (npc.npcType == 8172) {
            if (misc.random2(30) <= 2) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "Talk to me to start Desert Treasure!";
            }
        }
        if (npc.npcType == 619) {
            if (misc.random2(30) <= 2) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "This is as close as im getting.";
            }
        }
        if (npc.npcType == 246) {
            if (misc.random2(30) <= 2) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "Do ::inside Sir!";
            }
        }
        if (npc.npcType == 532) {
            if (misc.random2(30) <= 2) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "Member Shop!";
            }
        }
        if (npc.npcType == 3005) {
            if (misc.random2(30) <= 2) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "Ladder For Mems Only! Get's you behind edge bank!";
            }
        }
        if (npc.npcType == 3006) {
            if (misc.random2(30) <= 2) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "Come In if your Mem!, if not go to www.Ghreborn.com";
            }
        }
        if (npc.npcType == 660) {
            if (misc.random2(30) <= 2) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "We are the knights of the party room!";
            }
        }
        if (npc.npcType == 660) {
            if (misc.random2(30) <= 2) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "Here to Party 24/7!";
            }
        }
        if (npc.npcType == 364) {
            if (misc.random2(30) <= 2) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "Mod & Admin Portal Only!";
            }
        }
        if (npc.npcType == 280) {
            if (misc.random2(30) <= 2) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "Strength Guild, 99 strength to Enter!";
            }
        }
        if (npc.npcType == 172) {
            if (misc.random2(30) <= 2) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "Range/Magic Guild, 99 Range and Magic to Enter!";
            }
        }
        if (npc.npcType == 212) {
            if (misc.random2(30) <= 2) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "Welcome to the Mod/Admin Zone..Keep up the Good Work!";
            }
        }
        if (npc.npcType == 945) {
            if (misc.random2(30) <= 2) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "Talk to me to learn about the server.";
            }
        }
        if (npc.npcType == 225) {
            if (misc.random2(30) <= 2) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "Icon Minigame!";
            }
        }
        if (npc.npcType == 648) {
            if (misc.random2(30) <= 2) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "Welcome to Training Made To Own N33bs!";
            }
        }
        if (npc.npcType == 793) {
            if (misc.random2(30) <= 2) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "Enchanted Minigame!";
            }
        }
        if (npc.npcType == 2253) {
            if (misc.random2(30) <= 2) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "Clan Wars Portal!!";
            }
        }
        if (npc.npcType == 541) {
            if (misc.random2(30) <= 2) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "Skill Cape Shop!";
            }
        }
        if (npc.npcType == 1713) {
            if (misc.random2(30) <= 2) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "We are the righteous ones in his eyes alone.";
            }
        }
        if (npc.npcType == 1713) {
            if (misc.random2(30) <= 2) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "Let them not infest our cities and towns...";
            }
        }
        if (npc.npcType == 1713) {
            if (misc.random2(30) <= 2) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "And lo, we become the power, indeed the force to stop these monsters in their tracks.";
            }
        }
        if (npc.npcType == 1713) {
            if (misc.random2(30) <= 2) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "Verily I urge you, my friends to take up your spades and farm your farms to feed our people in this blessed sanctuary.";
            }
        }
        if (npc.npcType == 1713) {
            if (misc.random2(30) <= 2) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "If thine monsters visage does frighten thee, then tear it off I say... tear it off!";
            }
        }
        if (npc.npcType == 1713) {
            if (misc.random2(30) <= 2) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "For Saradomin will guide our sword arms and smash the enemies of humans till their bones become dust.";
            }
        }
        if (npc.npcType == 1713) {
            if (misc.random2(30) <= 2) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "And let us smite these monsters unto their deaths.";
            }
        }
        if (npc.npcType == 1713) {
            if (misc.random2(30) <= 2) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "For they are not the chosen ones in Saradomin's eyes.";
            }
        }
        if (npc.npcType == 1713) {
            if (misc.random2(30) <= 2) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "Where do we go for safety from these monsters... here, my brethren!";
            }
        }
        if (npc.npcType == 1713) {
            if (misc.random2(30) <= 2) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "And let there be no cave or shelter for their spawn until the end of days.";
            }
        }
        if (npc.npcType == 2821) {
            if (misc.random2(30) <= 2) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "Fishing Portal!";
            }
        }
        if (npc.npcType == 2304) {
            if (misc.random2(30) <= 2) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "Farming Shop!By seed's for patch's!";
            }
        }
        if (npc.npcType == 461) {
            if (misc.random2(30) <= 2) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "Magic Shop!";
            }
        }
        if (npc.npcType == 57) {
            if (misc.random2(30) == 1) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                int players = PlayerHandler.getPlayerCount();
                npc.textUpdate = "Players Online: " + players;
            }
        }
        if (npc.npcType == 8206) {
            if (misc.random2(30) == 1) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                int lottery = server.lottery.lotteryFund / 1000000;
                npc.textUpdate = "Lottery is at " + lottery + "m";
            }
        }
        if (npc.npcType == 550) {
            if (misc.random2(30) <= 2) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "Pk Shop!";
            }
        }
        if (npc.npcType == 1759) {
            if (misc.random2(30) <= 2) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "Shops Here!";
            }
        }
        if (npc.npcType == 1699) {
            if (misc.random2(30) <= 2) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "Pur3 Sh0p!";
            }
        }
        if (npc.npcType == 2475) {
            if (misc.random2(30) <= 2) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "Training Portal!";
            }
        }
        if (npc.npcType == 28) {
            if (misc.random2(30) <= 2) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "Train Your Skills Here!";
            }
        }
        if (npc.npcType == 1917) {
            if (misc.random2(30) <= 2) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "Rune Armor Shop!!!";
            }
        }
        if (npc.npcType == 522) {
            if (misc.random2(30) <= 2) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "General Store!";
            }
        }
        if (npc.npcType == 522) {
            if (misc.random2(30) <= 2) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "Great deals Here!";
            }
        }
        if (npc.npcType == 548) {
            if (misc.random2(30) <= 2) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "Gloves, Robes, Boots Shop!";
            }
        }
        if (npc.npcType == 530) {
            if (misc.random2(30) <= 2) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "Skillers Shop!";
            }
        }
        if (npc.npcType == 528) {
            if (misc.random2(30) <= 2) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "Woodcutting Store!!";
            }
        }
        if (npc.npcType == 949) {
            if (misc.random2(30) <= 3) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "Welcome to Moderator Island!";
            }
        }
        if (npc.npcType == 2244) {
            if (misc.random2(30) <= 3) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "Welcome to Moderator Island!";
            }
        }
        if (npc.npcType == 213) {
            if (misc.random2(30) <= 2) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "You need the frozen key to get in this portal! Kill the troll for key!";
            }
        }
        if (npc.npcType == 555) {
            if (misc.random2(30) <= 2) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "Random Stoof!";
            }
        }
        if (npc.npcType == 561) {
            if (misc.random2(30) <= 2) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "Hood Shop!";
            }
        }
        if (npc.npcType == 538) {
            if (misc.random2(30) <= 2) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "Slayer Shop!";
            }
        }
        if (npc.npcType == 529) {
            if (misc.random2(30) <= 2) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "Farming Shop!";
            }
        }

        if (npc.npcType == 3117) {
            if (misc.random2(30) <= 2) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "Click the chests for slayer exp ..";
            }
        }
        if (npc.npcType == 866) {
            if (misc.random2(30) <= 2) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "Et .. Phone .. Home!";
            }
        }
        if (npc.npcType == 549) {
            if (misc.random2(30) <= 2) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "Smithin' Shop";
            }
        }
        if (npc.npcType == 11674) {
            if (misc.random2(30) <= 2) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "Prestige Shop";
            }
        }
        if (npc.npcType == 558) {
            if (misc.random2(30) <= 2) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "Herblore shop!!";
            }
        }
        if (npc.npcType == 1552) {
            if (misc.random2(30) == 1) {
                npc.updateRequired = true;
                npc.textUpdateRequired = true;
                npc.textUpdate = "Merry Christmas!!!";
            }
        }
        // Add more via a config system or centralized map
    }

    private void handleNpcDeathAndRespawn(NPC npc, int index) {
        if (!npc.IsDead) return;

        if (npc.actionTimer == 0 && !npc.DeadApply && !npc.NeedRespawn) {

            npc.killedBy = getNpcKillerId(index);
            npc.IsUnderAttack = npc.IsUnderAttackNpc = false;
            int playerIndex = npc.StartKilling;
            client player = (client) server.playerHandler.players[playerIndex];
            if (player != null) {
                player.sendSound(getNpcDeathSound(npc.npcType), 6, 0);
            }
            npc.animNumber = getNpcDeathAnimation(npc.npcType);
            npc.updateRequired = true;
            npc.animUpdateRequired = true;
            npc.DeadApply = true;
            npc.actionTimer = AnimationLength.getFrameLength(npc.animNumber);
        } else if (npc.actionTimer == 0 && npc.DeadApply && !npc.NeedRespawn) {
            int killerIndex = npc.killedBy;
            int dropX = npc.absX, dropY = npc.absY;

            if (npc.targetNpcIndex == -1 && killerIndex >= 0 && killerIndex < PlayerHandler.players.length) {
                client killer = (client) PlayerHandler.players[killerIndex];
                if (killer != null) {
                    // Slayer task progress
                    killer.getSlayer().killTaskMonster(npc);

                    // Warriors Guild defender drop logic
                    killer.getWarriorsGuild().dropDefender(dropX, dropY);

                    // Optional: Animated Armour special drop
                    if (AnimatedArmour.isAnimatedArmourNpc(npc.npcId)) {
                        AnimatedArmour.dropTokens(killer, npc.npcType, dropX, dropY);
                    }
                }

                // Drops
                dropItems(index);
                MonsterDropItem(index);
            }

            npc.NeedRespawn = true;
            npc.actionTimer = 60;
            npc.absX = npc.makeX;
            npc.absY = npc.makeY;
            npc.HP = npc.MaxHP;
            npc.updateRequired = true;
            npc.animUpdateRequired = true;
        }
        else if (npc.actionTimer == 0 && npc.NeedRespawn && npc.Respawns) {
            newNPC(npc.npcType, npc.makeX, npc.makeY, npc.heightLevel,
                    npc.moverangeX1, npc.moverangeY1, npc.moverangeX2, npc.moverangeY2,
                    npc.walkingType, npc.MaxHP, true);
            npcs[index] = null;
        }
    }

    public boolean isTileWalkable(int x, int y, int z) {
        int clipping = Region.getClipping(x, y, z);
        return (clipping & 0x1280120) == 0; // You can adjust this based on your clip flags
    }
    private void broadcastDrop(int playerIndex, String itemName) {
        if (playerIndex >= 0 && playerIndex < PlayerHandler.players.length) {
            client p = (client) PlayerHandler.players[playerIndex];
            if (p != null) {
                PlayerHandler.messageToAll = p.playerName + " found a " + itemName + " on the ground.";
            }
        }
    }

    public void MonsterDropItem(int NPCID) {
        {
            if (!IsDropping) {
                IsDropping = true;
                int Play = GetNpcKiller(NPCID);
                int Maxi = ItemHandler.DropItemCount;

                for (int i = 0; i <= Maxi; i++) {
                    if (ItemHandler.DroppedItemsID[i] > 0) {
                    } else {

                        System.out.println("Npc id =" + NPCID);
                        if (npcs[NPCID] != null
                                && server.playerHandler.players[Play] != null
                                && server.playerHandler.players[GetNpcKiller(NPCID)]
                                != null) {
                            if (npcs[NPCID].npcType == 275) {
                                ItemHandler.addItem(4273, npcs[NPCID].absX,
                                        npcs[NPCID].absY, 1, GetNpcKiller(NPCID),
                                        false);
                            }
                            if (npcs[NPCID].npcType == 18) {
                                ItemHandler.addItem(Item3.randomguard(),
                                        npcs[NPCID].absX, npcs[NPCID].absY, 1,
                                        GetNpcKiller(NPCID), false);
                            }
                            if (npcs[NPCID].npcType == 21) {
                                ItemHandler.addItem(Item3.randomhero(),
                                        npcs[NPCID].absX, npcs[NPCID].absY, 1,
                                        GetNpcKiller(NPCID), false);
                            }
                            if (npcs[NPCID].npcType == 2256) {
                                ItemHandler.addItem(Item3.randomguardz(),
                                        npcs[NPCID].absX, npcs[NPCID].absY, 1,
                                        GetNpcKiller(NPCID), false);
                            }
                            if (npcs[NPCID].npcType == 1021) {
                                int Player = npcs[NPCID].StartKilling;
                                client ppl = (client) server.playerHandler.players[Player];

                                ppl.teleportToX = 2660;
                                ppl.teleportToY = 4839;
                            }
                            if (npcs[NPCID].npcType == 2468) {
                                int Player = npcs[NPCID].StartKilling;
                                client ppl = (client) server.playerHandler.players[Player];

                                newNPC(2468, ppl.absX, ppl.absY, ppl.heightLevel,
                                        ppl.absX + 3, ppl.absY + 3, ppl.absX + -3, ppl.absY + -3, 1, server.npcHandler.GetNpcListHP(2627), false);
                                newNPC(2468, ppl.absX, ppl.absY, ppl.heightLevel,
                                        ppl.absX + 3, ppl.absY + 3, ppl.absX + -3, ppl.absY + -3, 1, server.npcHandler.GetNpcListHP(2627), false);

                            }
                            if (npcs[NPCID].npcType == 1020) {
                                int Player = npcs[NPCID].StartKilling;
                                client ppl = (client) server.playerHandler.players[Player];

                                ppl.teleportToX = 2713; // Change coordinates
                                ppl.teleportToY = 4836; // Change coordinates
                            }
                            if (npcs[NPCID].npcType == 752) {
                                int Player = npcs[NPCID].StartKilling;
                                client ppl = (client) server.playerHandler.players[Player];

                                ppl.teleportToX = 2542; // Change coordinates
                                ppl.teleportToY = 3029; // Change coordinates
                            }
                            if (npcs[NPCID].npcType == 275) {
                                int Player = npcs[NPCID].StartKilling;
                                client ppl = (client) server.playerHandler.players[Player];

                                ppl.teleportToX = 2608; // Change coordinates
                                ppl.teleportToY = 3163; // Change coordinates
                            }
                            if (npcs[NPCID].npcType == 477) {
                                int Player = npcs[NPCID].StartKilling;
                                client ppl = (client) server.playerHandler.players[Player];

                                ppl.teleportToX = 2608; // Change coordinates
                                ppl.teleportToY = 3159; // Change coordinates
                            }
                            if (npcs[NPCID].npcType == 1919) {
                                int Player = npcs[NPCID].StartKilling;
                                client ppl = (client) server.playerHandler.players[Player];

                                ppl.teleportToX = 2866; // Change coordinates
                                ppl.teleportToY = 9952; // Change coordinates
                            }
                            if (npcs[NPCID].npcType == 509) {
                                int Player = npcs[NPCID].StartKilling;
                                client ppl = (client) server.playerHandler.players[Player];

                                ppl.teleportToX = 2792; // Change coordinates
                                ppl.teleportToY = 9325; // Change coordinates
                            }
                            if (npcs[NPCID].npcType == 274) {
                                int Player = npcs[NPCID].StartKilling;
                                client ppl = (client) server.playerHandler.players[Player];

                                ppl.teleportToX = 2540; // Change coordinates
                                ppl.teleportToY = 3019; // Change coordinates
                            }
                            if (npcs[NPCID].npcType == 1022) {

                                int Player = npcs[NPCID].StartKilling;
                                client ppl = (client) server.playerHandler.players[Player];

                                ppl.teleportToX = 2584; // Change coordinates
                                ppl.teleportToY = 4836; // Change coordinates
                            }
                            if (npcs[NPCID].npcType == 1019) {
                                int Player = npcs[NPCID].StartKilling;
                                client ppl = (client) server.playerHandler.players[Player];

                                ppl.teleportToX = 2464; // Change coordinates
                                ppl.teleportToY = 4834; // Change coordinates
                            }
                            if (npcs[NPCID].npcType == 2026) {
                                int Player = npcs[NPCID].StartKilling;
                                client ppl = (client) server.playerHandler.players[Player];

                                ppl.teleportToX = 3551; // Change coordinates
                                ppl.teleportToY = 9677; // Change coordinates
                                ppl.addItem(1543, 1);
                                ppl.sendMessage("Good Job Now Kill Verac To Go To Torag!");
                            }
                            if (npcs[NPCID].npcType == 2745) {
                                int Player = npcs[NPCID].StartKilling;
                                client ppl = (client) server.playerHandler.players[Player];

                                PlayerHandler.messageToAll = "@red@" + ppl.playerName + " @bla@Has Killed Jad!";
                            }
                            if (npcs[NPCID].npcType == 8133) {
                                int Player = npcs[NPCID].StartKilling;
                                client ppl = (client) server.playerHandler.players[Player];

                                PlayerHandler.messageToAll = "@red@" + ppl.playerName + " @bla@Has Killed corp!";
                            }
                            if (npcs[NPCID].npcType == 5666) {
                                int Player = npcs[NPCID].StartKilling;
                                client ppl = (client) server.playerHandler.players[Player];

                                PlayerHandler.messageToAll = "@red@" + ppl.playerName + " @bla@Has Killed Barrelchest!";
                            }
                            if (npcs[NPCID].npcType == 3847) {
                                int Player = npcs[NPCID].StartKilling;
                                client ppl = (client) server.playerHandler.players[Player];

                                PlayerHandler.messageToAll = "@red@" + ppl.playerName + " @bla@Has Killed The Sea Queen!";
                            }
                            if (npcs[NPCID].npcType == 50) {
                                int Player = npcs[NPCID].StartKilling;
                                client ppl = (client) server.playerHandler.players[Player];

                                PlayerHandler.messageToAll = "@red@" + ppl.playerName + " @bla@Someone Has Killed Kbd!";
                            }
                            if (npcs[NPCID].npcType == 2030) {
                                int Player = npcs[NPCID].StartKilling;
                                client ppl = (client) server.playerHandler.players[Player];

                                ppl.teleportToX = 3551; // Change coordinates
                                ppl.teleportToY = 9711; // Change coordinates
                                ppl.addItem(1544, 1);
                                ppl.sendMessage("Good Job Now Kill Torag To Go To Ahrims!");
                            }
                            if (npcs[NPCID].npcType == 2029) {
                                int Player = npcs[NPCID].StartKilling;
                                client ppl = (client) server.playerHandler.players[Player];

                                ppl.teleportToX = 3537; // Change coordinates
                                ppl.teleportToY = 9712; // Change coordinates
                                ppl.addItem(1545, 1);
                                ppl.sendMessage("Good Job Now Kill Ahrim To Go To Guthan!");
                            }
                            if (npcs[NPCID].npcType == 2025) {
                                int Player = npcs[NPCID].StartKilling;
                                client ppl = (client) server.playerHandler.players[Player];

                                ppl.teleportToX = 3535; // Change coordinates
                                ppl.teleportToY = 9694; // Change coordinates
                                ppl.addItem(1546, 1);
                                ppl.sendMessage("Good Job Now Kill Guthan To Go To Karil!");
                            }
                            if (npcs[NPCID].npcType == 193) //Druid
                            {
                                int playerId = npcs[NPCID].StartKilling;
                                client c = (client) server.playerHandler.players[playerId];
                                c.Druidkills += 1;
                            }

                            if (npcs[NPCID].npcType == 2837) {
                                int playerId = npcs[NPCID].StartKilling;
                                client c = (client) server.playerHandler.players[playerId];
                                c.Zombiekills += 1;
                            }
                            if (npcs[NPCID].npcType == 104) //Ghost
                            {
                                int playerId = npcs[NPCID].StartKilling;
                                client c = (client) server.playerHandler.players[playerId];
                                c.Ghostkills += 1;
                            }
                            if (npcs[NPCID].npcType == 111) {
                                int playerId = npcs[NPCID].StartKilling;
                                client c = (client) server.playerHandler.players[playerId];
                                c.Giantkills += 1;
                            }
                            if (npcs[NPCID].npcType == 752) //Lesser Demon
                            {
                                int playerId = npcs[NPCID].StartKilling;
                                client c = (client) server.playerHandler.players[playerId];
                                c.Demonkills += 1;
                            }
                            if (npcs[NPCID].npcType == 7552) //General Khazard
                            {
                                int playerId = npcs[NPCID].StartKilling;
                                client c = (client) server.playerHandler.players[playerId];
                                c.Generalkills += 1;
                            }
                            if (npcs[NPCID].npcType == 1472) //Jungle demon
                            {
                                int playerId = npcs[NPCID].StartKilling;
                                client c = (client) server.playerHandler.players[playerId];
                                c.JDemonkills += 1;
                            }
                            if (npcs[NPCID].npcType == 752) //Lesser Demon
                            {
                                int Player = npcs[NPCID].StartKilling;
                                client ppl = (client) server.playerHandler.players[Player];
                                ppl.sendMessage("Good! Now kill the General!");
                                ppl.teleportToX = 3182;
                                ppl.teleportToY = 6829;
                            }
                            if (npcs[NPCID].npcType == 7552) //General Khazard
                            {
                                int Player = npcs[NPCID].StartKilling;
                                client ppl = (client) server.playerHandler.players[Player];
                                ppl.sendMessage("Wow, you have made it this far! Kill Him to beat the Mini game!");
                                ppl.teleportToX = 3136;
                                ppl.teleportToY = 6853;
                            }
                            if (npcs[NPCID].npcType == 1472) //Jungle demon
                            {
                                int Player = npcs[NPCID].StartKilling;
                                client ppl = (client) server.playerHandler.players[Player];
                                ppl.sendMessage("You finished the Mini game! Click on the Chest to claim your reward!");
                                ppl.teleportToX = 3143;
                                ppl.teleportToY = 6806;
                            }

                            if (npcs[NPCID].npcType == 2027) {
                                int Player = npcs[NPCID].StartKilling;
                                client ppl = (client) server.playerHandler.players[Player];

                                ppl.teleportToX = 3534; // Change coordinates
                                ppl.teleportToY = 9677; // Change coordinates
                                ppl.addItem(1547, 1);
                                ppl.sendMessage("Good Job Now Kill Karil To Go To Chaos Elemental!!");
                            }

                            if (npcs[NPCID].npcType == 2028) {
                                int Player = npcs[NPCID].StartKilling;
                                client ppl = (client) server.playerHandler.players[Player];

                                ppl.teleportToX = 3045; // Change coordinates
                                ppl.teleportToY = 3743; // Change coordinates
                                ppl.addItem(1548, 1);
                                ppl.sendMessage("Good Job Now Kill Chaos Elemental To Go To The Chest!!");
                            }
                            if (npcs[NPCID].npcType == 3200) {
                                int Player = npcs[NPCID].StartKilling;
                                client ppl = (client) server.playerHandler.players[Player];

                                ppl.teleportToX = 3045; // Change coordinates
                                ppl.teleportToY = 3751; // Change coordinates
                                ppl.addItem(2399, 1);
                                ppl.sendMessage("SWEET Y0U DID IT! CLICK THE CHEST!");
                            }

                            if (npcs[NPCID].npcType == 17) {
                                int Player = npcs[NPCID].StartKilling;
                                client ppl = (client) server.playerHandler.players[Player];

                                ppl.sendMessage("Grab The Dropped Item.....");
                            }

                            if (npcs[NPCID].npcType == 35) {
                                int Player = npcs[NPCID].StartKilling;
                                client ppl = (client) server.playerHandler.players[Player];

                                ppl.sendMessage("Grab The Dropped Item.....");
                            }

                            if (npcs[NPCID].npcType == 113) {
                                int Player = npcs[NPCID].StartKilling;
                                client ppl = (client) server.playerHandler.players[Player];

                                ppl.sendMessage("Grab The Dropped Item.....");
                            }

                            if (npcs[NPCID].npcType == 86) {
                                int Player = npcs[NPCID].StartKilling;
                                client ppl = (client) server.playerHandler.players[Player];

                                ppl.sendMessage("Grab The Dropped Item.....");
                            }

                            if (npcs[NPCID].npcType == 55) {
                                int Player = npcs[NPCID].StartKilling;
                                client ppl = (client) server.playerHandler.players[Player];

                                ppl.sendMessage("Grab The Dropped Item.....");
                            }

                            if (npcs[NPCID].npcType == 1007) {
                                ItemHandler.addItem(6754, npcs[NPCID].absX,
                                        npcs[NPCID].absY, 1, GetNpcKiller(NPCID),
                                        false);
                            }
                            if (npcs[NPCID].npcType == 49) {
                                ItemHandler.addItem(4272, npcs[NPCID].absX,
                                        npcs[NPCID].absY, 1, GetNpcKiller(NPCID),
                                        false);
                            }
                            if (npcs[NPCID].npcType == 795) {
                                ItemHandler.addItem(4078, npcs[NPCID].absX,
                                        npcs[NPCID].absY, 1, GetNpcKiller(NPCID),
                                        false);
                            }
                            if (npcs[NPCID].npcType == 509) {
                                ItemHandler.addItem(6104, npcs[NPCID].absX,
                                        npcs[NPCID].absY, 1, GetNpcKiller(NPCID),
                                        false);
                            }
                            if (npcs[NPCID].npcType == 2880) {
                                ItemHandler.addItem(5585, npcs[NPCID].absX,
                                        npcs[NPCID].absY, 1, GetNpcKiller(NPCID),
                                        false);
                            }
                            if (npcs[NPCID].npcType == 2745) {
                                ItemHandler.addItem(6570, npcs[NPCID].absX,
                                        npcs[NPCID].absY, 1, GetNpcKiller(NPCID),
                                        false);
                            }
                            if (npcs[NPCID].npcType == 3847) {
                                ItemHandler.addItem(13487, npcs[NPCID].absX,
                                        npcs[NPCID].absY, 1, GetNpcKiller(NPCID),
                                        false);
                            }
                            if (npcs[NPCID].npcType == 1859) {
                                ItemHandler.addItem(6529, npcs[NPCID].absX,
                                        npcs[NPCID].absY, 10000000,
                                        GetNpcKiller(NPCID), false);
                            }
                            if (npcs[NPCID].npcType == 1160) {
                                ItemHandler.addItem(Item2.randomKQ(),
                                        npcs[NPCID].absX, npcs[NPCID].absY, 1,
                                        GetNpcKiller(NPCID), false);
                            }
                            if (npcs[NPCID].npcType == 391) {
                                ItemHandler.addItem(Item2.randomtroll(),
                                        npcs[NPCID].absX, npcs[NPCID].absY, 1,
                                        GetNpcKiller(NPCID), false);
                            }
                            if (npcs[NPCID].npcType == 951) {
                                ItemHandler.addItem(Item2.randomchicken(),
                                        npcs[NPCID].absX, npcs[NPCID].absY, 1,
                                        GetNpcKiller(NPCID), false);
                            }
                            if (npcs[NPCID].npcType == 90) {
                                ItemHandler.addItem(Item2.randomskeleton(),
                                        npcs[NPCID].absX, npcs[NPCID].absY, 1,
                                        GetNpcKiller(NPCID), false);
                            }
                            if (npcs[NPCID].npcType == 1648) {
                                ItemHandler.addItem(Item2.randomcrawlinghand(),
                                        npcs[NPCID].absX, npcs[NPCID].absY, 1,
                                        GetNpcKiller(NPCID), false);
                            }
                            if (npcs[NPCID].npcType == 1832) {
                                ItemHandler.addItem(Item2.randomcavebug(),
                                        npcs[NPCID].absX, npcs[NPCID].absY, 1,
                                        GetNpcKiller(NPCID), false);
                            }
                            if (npcs[NPCID].npcType == 1637) {
                                ItemHandler.addItem(Item2.randomjelly(),
                                        npcs[NPCID].absX, npcs[NPCID].absY, 1,
                                        GetNpcKiller(NPCID), false);
                            }
                            if (npcs[NPCID].npcType == 1604) {
                                ItemHandler.addItem(
                                        Item2.randomaberrantspecter(),
                                        npcs[NPCID].absX, npcs[NPCID].absY, 1,
                                        GetNpcKiller(NPCID), false);
                            }
                            if (npcs[NPCID].npcType == 1615) {
                                ItemHandler.addItem(Item2.randomabyssaldemon(),
                                        npcs[NPCID].absX, npcs[NPCID].absY, 1,
                                        GetNpcKiller(NPCID), false);
                            }
                            if (npcs[NPCID].npcType == 2783) {
                                ItemHandler.addItem(Item2.randomdarkbeast(),
                                        npcs[NPCID].absX, npcs[NPCID].absY, 1,
                                        GetNpcKiller(NPCID), false);
                            }
                            if (npcs[NPCID].npcType == 89) {
                                ItemHandler.addItem(Item2.randomunicorn(),
                                        npcs[NPCID].absX, npcs[NPCID].absY, 1,
                                        GetNpcKiller(NPCID), false);
                            }
                            if (npcs[NPCID].npcType == 912) {
                                ItemHandler.addItem(Item2.randombattlemagesara(),
                                        npcs[NPCID].absX, npcs[NPCID].absY, 1,
                                        GetNpcKiller(NPCID), false);
                            }
                            if (npcs[NPCID].npcType == 913) {
                                ItemHandler.addItem(
                                        Item2.randombattlemagezammy(),
                                        npcs[NPCID].absX, npcs[NPCID].absY, 1,
                                        GetNpcKiller(NPCID), false);
                            }
                            if (npcs[NPCID].npcType == 914) {
                                ItemHandler.addItem(
                                        Item2.randombattlemageguthix(),
                                        npcs[NPCID].absX, npcs[NPCID].absY, 1,
                                        GetNpcKiller(NPCID), false);
                            }
                            if (npcs[NPCID].npcType == 86) {
                                ItemHandler.addItem(Item2.randomrat(),
                                        npcs[NPCID].absX, npcs[NPCID].absY, 1,
                                        GetNpcKiller(NPCID), false);
                            }
                            if (npcs[NPCID].npcType == 35) {
                                ItemHandler.addItem(Item2.randomsoldier(),
                                        npcs[NPCID].absX, npcs[NPCID].absY, 1,
                                        GetNpcKiller(NPCID), false);
                            }
                            if (npcs[NPCID].npcType == 114) {
                                ItemHandler.addItem(Item2.randomogre(),
                                        npcs[NPCID].absX, npcs[NPCID].absY, 1,
                                        GetNpcKiller(NPCID), false);
                            }
                            if (npcs[NPCID].npcType == 19) {
                                ItemHandler.addItem(Item2.randomwhiteknight(), npcs[NPCID].absX, npcs[NPCID].absY, 1, GetNpcKiller(NPCID), false);
                            }

                            if (npcs[NPCID].npcType == 3260) {
                                ItemHandler.addItem(Item2.randombarbarian(),
                                        npcs[NPCID].absX, npcs[NPCID].absY, 1,
                                        GetNpcKiller(NPCID), false);
                            }
                            if (npcs[NPCID].npcType == 3425) {
                                ItemHandler.addItem(Item2.randomfishy(),
                                        npcs[NPCID].absX, npcs[NPCID].absY, 1,
                                        GetNpcKiller(NPCID), false);
                            }
                            if (npcs[NPCID].npcType == 55) {
                                ItemHandler.addItem(Item2.randombluedragon(),
                                        npcs[NPCID].absX, npcs[NPCID].absY, 1,
                                        GetNpcKiller(NPCID), false);
                            }
                            if (npcs[NPCID].npcType == 112) {
                                ItemHandler.addItem(532,
                                        npcs[NPCID].absX, npcs[NPCID].absY, 1,
                                        GetNpcKiller(NPCID), false);
                                ItemHandler.addItem(Item2.randomMossGiants(),
                                        npcs[NPCID].absX, npcs[NPCID].absY, 1,
                                        GetNpcKiller(NPCID), false);
                            }
                            if (npcs[NPCID].npcType == 1591) {
                                ItemHandler.addItem(Item2.randomirondragon(),
                                        npcs[NPCID].absX, npcs[NPCID].absY, 1,
                                        GetNpcKiller(NPCID), false);
                            }
                            if (npcs[NPCID].npcType == 1338) {
                                ItemHandler.addItem(Item2.randomDagannoths(),
                                        npcs[NPCID].absX, npcs[NPCID].absY, 1,
                                        GetNpcKiller(NPCID), false);
                            }
                            if (npcs[NPCID].npcType == 3494) {
                                ItemHandler.addItem(Item2.randomFlambeed(),
                                        npcs[NPCID].absX, npcs[NPCID].absY, 1,
                                        GetNpcKiller(NPCID), false);
                            }
                            if (npcs[NPCID].npcType == 1859) {
                                ItemHandler.addItem(Item2.randomArzinian_Being_of_Bordanzan(),
                                        npcs[NPCID].absX, npcs[NPCID].absY, 1,
                                        GetNpcKiller(NPCID), false);
                            }
                            if (npcs[NPCID].npcType == 1575) {
                                ItemHandler.addItem(Item2.randomSkeleton_Hellhound(),
                                        npcs[NPCID].absX, npcs[NPCID].absY, 1,
                                        GetNpcKiller(NPCID), false);
                            }
                            if (npcs[NPCID].npcType == 84) {
                                ItemHandler.addItem(Item2.randomBlack_Demon(),
                                        npcs[NPCID].absX, npcs[NPCID].absY, 1,
                                        GetNpcKiller(NPCID), false);
                            }
                            if (npcs[NPCID].npcType == 111) {
                                ItemHandler.addItem(Item2.randomIce_giant(),
                                        npcs[NPCID].absX, npcs[NPCID].absY, 1,
                                        GetNpcKiller(NPCID), false);
                            }
                            if (npcs[NPCID].npcType == 2919) {
                                ItemHandler.addItem(Item2.randomAgrith_Naar(),
                                        npcs[NPCID].absX, npcs[NPCID].absY, 1,
                                        GetNpcKiller(NPCID), false);
                            }

                            if (npcs[NPCID].npcType == 188) {
                                ItemHandler.addItem(16, npcs[NPCID].absX,
                                        npcs[NPCID].absY, 1, GetNpcKiller(NPCID),
                                        false);
                            }
                            if (npcs[NPCID].npcType == 1625
                                    || npcs[NPCID].npcType == 1604
                                    || npcs[NPCID].npcType == 2035) {
                                ItemHandler.addItem(Item.randomSlayeritem65(),
                                        npcs[NPCID].absX, npcs[NPCID].absY, 1,
                                        GetNpcKiller(NPCID), false);
                            }
                            if (npcs[NPCID].npcType == 1625
                                    || npcs[NPCID].npcType == 1604) {
                                ItemHandler.addItem(Item.randomSlayeritem75(),
                                        npcs[NPCID].absX, npcs[NPCID].absY, 1,
                                        GetNpcKiller(NPCID), false);
                            }
                            if (npcs[NPCID].npcType == 1605) {
                                ItemHandler.addItem(Item.randomSlayer99item(),
                                        npcs[NPCID].absX, npcs[NPCID].absY, 1,
                                        GetNpcKiller(NPCID), false);
                            }
                        }

                        if (i == Maxi) {
                            if (ItemHandler.DropItemCount
                                    >= (ItemHandler.MaxDropItems + 1)) {
                                ItemHandler.DropItemCount = 0;
                                println("! Notify item resterting !");
                            }
                        }
                        break;
                    }
                }
                IsDropping = false;
            }
        }
    }

    public int GetNpcKiller(int NPCID) {
        int Killer = 0;
        int Count = 0;

        for (int i = 1; i < PlayerHandler.maxPlayers; i++) {
            if (Killer == 0) {
                Killer = i;
                Count = 1;
            } else {
                if (npcs[NPCID].Killing[i] > npcs[NPCID].Killing[Killer]) {
                    Killer = i;
                    Count = 1;
                } else if (npcs[NPCID].Killing[i] == npcs[NPCID].Killing[Killer]) {
                    Count++;
                }
            }
        }
        if (Count > 1
                && npcs[NPCID].Killing[npcs[NPCID].StartKilling]
                == npcs[NPCID].Killing[Killer]) {
            Killer = npcs[NPCID].StartKilling;
        }
        return Killer;
    }

    public void gfxAll(int id, int Y, int X) {
        for (Player p : server.playerHandler.players) {
            if (p != null) {
                client person = (client) p;

                if ((person.playerName != null || person.playerName != "null")) {
                    if (person.distanceToPoint(X, Y) <= 60) {
                        person.stillgfx2(id, Y, X);
                    }
                }
            }
        }
    }
    public int getNpcDeathSound(int npcType) {
        NPCCacheDefinition def = NPCCacheDefinition.forID(npcType);

        String npc = def != null && def.getName() != null
                ? def.getName().toLowerCase()
                : "";

        switch(npcType){
            case 2455:
            case 1341:
                return 1621;
            case 3260:
                return 502;
            case 118:
                return 418;
        }
        if (npc.contains("bat")) {
            return 293; // Bat death screech
        }
        if (npc.equalsIgnoreCase("moss giant") || npc.equalsIgnoreCase("fire giant")) {
            return 450; // Confirmed zombie death
        }
        if (npc.equals("rock crab")) {
            return 719; // Rock crab crumble
        }
        if (npc.contains("cow")) {
            return 532; // Cow moo death
        }
        if (npc.contains("chicken")) {
            return 537; // Chicken death squawk
        }
        if (npc.contains("imp")) {
            return 535; // Imp death cackle
        }
        if (npc.equals("rat") || npc.equals("giant rat")) {
            return 711; // Rat death squeal
        }
        if (npc.contains("duck")) {
            return 537; // Shares with chicken
        }
        if (npc.contains("wolf")) {
            return 584; // Wolf dying growl
        }
        if (npc.contains("bear")) {
            return 597; // Bear groan on death
        }
        if (npc.contains("dragon")) {
            return 409; // Dragon death roar (same as attack — reused)
        }
        if (npc.contains("ghost")) {
            return 438; // Ghost wail
        }
        if (npc.contains("goblin")) {
            return 471; // Goblin death yell
        }
        if (npc.contains("skeleton")) {
            return 777; // Bone collapse
        }
        if (npc.contains("demon") || npc.contains("ogre") || npc.contains("giant") || npc.contains("jad")) {
            return 793; // Large monster death
        }
        if (npc.contains("zombie")) {
            return 1140; // Confirmed zombie death
        }
        if (npc.equalsIgnoreCase("frost dragon")) {
            return 96; // Confirmed zombie death
        }

// Death Sounds
        if (npc.equalsIgnoreCase("tzhaar-hur")) return 252;
        if (npc.equalsIgnoreCase("tzhaar-ket")) return 256;
        if (npc.equalsIgnoreCase("tzhaar-mej")) return 263;
        if (npc.equalsIgnoreCase("tzhaar-xil")) return 270;
        // Generic fallback death sound for unknowns
        return 512;
    }

    public int getNpcDeathAnimation(int npcType) {
        switch (npcType) {
            case 118:
                return 102;
            case 2455:
            case 1341:
                return 1342;
            case 677: return 67;
            case 5362:
            case 53:
            case 54:
            case 55:
            case 941:
                return 92;
            case 2615:
            case 2616:
            case 2605:
            case 2607:
                return 9288;
            case 10773: return 13153;
            case 8352:
            case 8350:
            case 8351:// torm demon
                return 10924;

            case 1265:
                return 1314;
                case 2591: return 9288;
            case 105: return 4929;
            case 132: return 223;
            case 128: return 278;
                case 1648:
            case 1649:
            case 1650:
            case 1651:
            case 1652:
                    return 9126;
            case 1653:
                return 9445;
            case 111:
                return 4673;
            case 104:
                return 5534;
            case 708:
            case 709:
                return 172;
            case 2313:
            case 2314:
            case 2315:
            case 41:
                return 5389;
            case 4397:
            case 4398:
            case 4399:
                return 4270;
            case 4400:
                return 5329;
            case 4401:
                return 6256;
            case 4402:
            case 4403:
                return 6251;
            case 5529:
                return 5784;
            case 4395: return 4935;
            case 81:
            case 397:
            case 1766:
            case 1767:
            case 1768:
                return 5851; // Cows and related NPCs
            case 1618:
                return 9131; // Unicorn
            case 1610:
                return 9455; // Unicorn
            case 2881:
            case 2882:
            case 2883:
                return 2856; // Dagannoth Kings
            case 6260:
                return 7062; // General Graardor
            case 6261:
            case 6263:
            case 6265:
                return 6156; // Sergeants of Bandos
            case 6222:
                return 6975; // Kree'arra
            case 6225:
            case 6223:
            case 6227:
                return 6956; // Aviansies
            case 6247:
                return 6965; // Commander Zilyana
            case 6248:
                return 6377; // Starlight
            case 6250:
                return 7016; // Growler
            case 6252:
                return 7011; // Bree
            case 6203:
                return 6946; // K'ril Tsutsaroth
            case 6204:
            case 6206:
            case 6208:
                return 67; // Lesser demons of Zamorak
            case 1153:
            case 1154:
            case 1155:
            case 1156:
            case 1157:
                return 6228; // Kalphite Workers and Guardians
            case 1160:
                return 6233; // Kalphite Queen
            case 4353:
                return 4233; // Cave horror
            case 117:
                return 4653; // Hill Giant
            case 8133:
                return 10059; // Corporeal Beast
            case 7160:
            case 7159:
            case 7158:
                return 8790; // Cockroaches
            case 87:
                return 0x08D; // Rat
            case 113:
                return 8576; // Jogre
            case 107:
            case 144:
                return 6256; // Specific NPCs

            case 4415:
                return 2707; // Rat
            case 4413:
            case 4414:
                return 6576; // Wolf
            case 4404:
            case 4405:
            case 4406:
                return 4265; // Minotaur
            case 4407:
            case 4408:
            case 4409:
            case 4410:
            case 4411:
            case 4412:
                return 6190; // Goblin
            case 86:
            case 88:
                return 4935; // Rat
            case 4393:
            case 4394:
            case 5377:
            case 5378:
            case 5379:
            case 5380: return 5575;
            case 90:
                return 5491; // Skeleton
            case 103:
            case 491:
                return 5542; // Ghost
            case 4389:
            case 4390:
            case 4391:return 1187;
            case 2598:
            case 2599:
            case 2600:
                return 9288;
            default:
                return 0x900; // Default death animation for unspecified NPCs
        }
    }
    public int getNpcAttackSound(int npcType) {
        NPCCacheDefinition def = NPCCacheDefinition.forID(npcType);

        String npc = def != null && def.getName() != null
                ? def.getName().toLowerCase()
                : "";
        switch(npcType){
            case 2455:
            case 1341:
                return 1615;
            case 3260:
                return 501;
            case 118:
                return 417;
        }
        if (npc.equalsIgnoreCase("frost dragon")) {
            return 85; // Confirmed zombie death
        }
        if (npc.equalsIgnoreCase("moss giant")) {
            return 449; // Confirmed zombie death
        }
        if (npc.equalsIgnoreCase("fire giant")) {
            return 447; // Confirmed zombie death
        }
        if (npc.contains("bat")) {
            return 292; // (Still valid — no updated version found)
        }
        if (npc.contains("cow")) {
            return 531; // OSRS/614 cow attack
        }
        if (npc.contains("chicken")) {
            return 538; // Chicken peck/hit
        }
        if (npc.contains("imp")) {
            return 534; // Imp attack laugh
        }
        if (npc.equals("rat") || npc.equals("giant rat")) {
            return 710; // Generic rat attack
        }
        if (npc.contains("duck")) {
            return 538; // Same as chicken, reused
        }
        if (npc.contains("wolf")) {
            return 583; // Wolf snarl
        }
        if (npc.contains("bear")) {
            return 596; // Bear growl/slash
        }

        if (npc.contains("dragon")) {
            return 408; // Dragon attack roar (used in 614+)
        }
        if (npc.equals("rock crab")) {
            return 718; // Classic rock crab
        }
        if (npc.contains("ghost")) {
            return 436; // Ghostly moan
        }
        if (npc.equalsIgnoreCase("barbarian woman")) {
            return 506; // Ghostly moan
        }
        if (npc.contains("goblin")) {
            return 469; // Goblin attack grunt
        }
        if (npc.contains("skeleton")) {
            return 776; // Skeleton bone clash
        }
        if (npc.contains("demon") || npc.contains("ogre")
                || npc.contains("giant")
                || npc.contains("jad")) {
            return 64; // Strong generic monster (original), optionally: return 793;
        }
        // Attack Sounds
        if (npc.equalsIgnoreCase("tzhaar-hur")) return 251;
        if (npc.equalsIgnoreCase("tzhaar-ket")) return 250;
        if (npc.equalsIgnoreCase("tzhaar-mej")) return 260;
        if (npc.equalsIgnoreCase("tzhaar-xil")) return 266;

        if (npc.contains("zombie")) {
            return 1155; // Still used in 600+ (confirmed zombie grunt)
        }
        if (npc.contains("man") || npc.contains("woman")
                || npc.contains("monk") || npc.contains("thief")) {
            return 417; // Human grunt (may upgrade to 586 or 787)
        }
        if (npc.contains("wizard")) {
            return 1002; // Still valid — classic magic sound
        }
        if (npc.contains("guard") || npc.contains("farmer")) {
            return 403; // Light NPC attack
        }

        // Fallback random melee swing sounds (OSRS-style)
        return misc.random(6) > 3 ? 793 : 789;
    }

    public int getNpcAttackAnimation(int npcId) {
        NPC npc = npcs[npcId];
        if (npc == null) {
            return 0; // or a default animation
        }

        switch (npc.npcType) {
            case 118:
                return 99;
            case 2455:
            case 1341:
                return 1341;
            case 677: return 64;
            case 5362:
            case 53:
            case 54:
            case 55:
            case 941:
                return 91;
            case 2605:
            case 2607:
            case 2616:

                return 9286;
            case 2598:
            case 2599:
            case 2600:
                return 9286;
            case 10773: return 13151;
            case 19:
                return 7048;
            case 2615:
                return 9286;
            case 8352:
            case 8350:
            case 8351://tormented demon
                if (npc.attackType == 2)
                    return 10917;
                else if (npc.attackType == 1)
                    return 10918;
                else if (npc.attackType == 0)
                    return 10922;
            case 1265:
                return 1312;
            case 2591: return 9286;
            case 105:
                return 4925;
            case 132: return 220;
            case 128: return 275;
            case 1648:
            case 1649:
            case 1650:
            case 1651:
            case 1652:return 9125;
            case 1653: return 9444;
            case 111:
                return 4672;
            case 708:
            case 709:
                return 169;
            case 2313:
            case 2314:
            case 2315:
            case 41:
                return 5387;
            case 4397:
            case 4398:
            case 4399:
                return 4272;
            case 4400:
                return 5327;
            case 4401:
                return 6249;
            case 4402:
            case 4403:
                return 6254;
            case 4407:
            case 4408:
            case 4409:
            case 4410:
            case 4411:
            case 4412: return 6188;
            case 3260:
            case 191: return 799;
            case 35:
            case 9: return 12311;
            case 3200:
            case 752: return 0x326;
            case 4395: return 4933;
            case 4393:
            case 4394:
            case 5377:
            case 5378:
            case 5379:
            case 5380: return misc.random(3) == 1 ? 5571 : 5573;
            case 50: return misc.random(3) == 1 ? 81 : 84;
            case 113: return 8577;
            case 81:
            case 1768:
            case 1767:
            case 1766:return 5849;
            case 107:
            case 144: return 6254;
            case 2745: return misc.random(2) == 0 ? 9276 : 9277;
            case 86:
            case 87: return 4933;
            case 90: return 5485;
            case 103:
            case 491: return 5540;
            case 104:
                return 5532;
            case 78: return 4915;
            case 119:
            case 82:
            case 83:
            case 84:
            case 1913: return 1979;
            case 1585: return 4666;
            case 1588: return 4658;
            case 1593: return 6562;
            case 4413:
            case 4414: return 6579;
            case 4404:
            case 4405:
            case 4406: return 4266;
            case 6260: return 7060;
            case 1160: return 6235;
            case 4353: return 4234;
            case 117: return 4652;
            case 4389:
            case 4390:
            case 4391:return 1184;
            case 5529:
                return 5782;
            case 7158:
                return 8786;
            default: return 0x326;
        }
    }

    public int getNpcBlockSound(int npcType) {
        NPCCacheDefinition def = NPCCacheDefinition.forID(npcType);

        String npc = def != null && def.getName() != null
                ? def.getName().toLowerCase()
                : "";
        switch(npcType){
            case 2455:
            case 1341:
                return 1622;
            case 118:
                return 419;
        }
        if (npc.contains("bat")) {
            return 294; // Bat block screech
        }
        if (npc.equalsIgnoreCase("frost dragon")) {
            return 88; // Confirmed zombie death
        }
        if (npc.equals("rock crab")) {
            return 720; // Reused generic melee block
        }
        if (npc.equalsIgnoreCase("moss giant") || npc.equalsIgnoreCase("fire giant")) {
            return 451; // Confirmed zombie death
        }
        if (npc.contains("cow")) {
            return 533; // Cow block
        }
        if (npc.contains("chicken") || npc.contains("duck")) {
            return 536; // Chicken/duck block peck
        }
        if (npc.contains("imp")) {
            return 536; // Imp block (still has attitude 😈)
        }
        if (npc.equals("rat") || npc.equals("giant rat")) {
            return 713; // Rat block squeal
        }
        if (npc.contains("wolf")) {
            return 585; // Wolf deflect
        }
        if (npc.contains("bear")) {
            return 598; // Bear grunt/block
        }
        if (npc.contains("dragon")) {
            return 410; // Dragon defensive growl
        }
        if (npc.contains("ghost")) {
            return 439; // Echo block
        }
        if (npc.contains("goblin")) {
            return 471; // Goblin block grunt
        }
        if (npc.contains("skeleton")) {
            return 778; // Rattle/block
        }
        if (npc.equalsIgnoreCase("barbarian woman")) {
            return 509; // Ghostly moan
        }
        if (npc.contains("demon") || npc.contains("ogre") || npc.contains("giant")
                || npc.contains("jad")) {
            return 1154; // Large creature block
        }
        if (npc.contains("zombie")) {
            return 1151; // Confirmed zombie block
        }
        // Hit Sounds
        if (npc.equalsIgnoreCase("tzhaar-hur")) return 253;
        if (npc.equalsIgnoreCase("tzhaar-ket")) return 257;
        if (npc.equalsIgnoreCase("tzhaar-mej")) return 264;
        if (npc.equalsIgnoreCase("tzhaar-xil")) return 271;

        if (npc.contains("man") && !npc.contains("woman")) {
            return 816; // Male NPC block
        }
        if (npc.contains("monk") || npc.contains("guard") || npc.contains("farmer")
                || npc.contains("thief") || npc.contains("druid") || npc.contains("wizard")) {
            return 816; // Same group block type
        }
        if (!npc.contains("man") && npc.contains("woman")) {
            return 818; // Female NPC block
        }

        return 511; // Fallback block sound
    }

    public int GetNPCBlockAnim(int id) {
        switch (id) {
            case 118:
                return 100;
            case 2455:
            case 1341:
                return 1340;
            case 677: return 65;
            case 5362:
            case 53:
            case 54:
            case 55:
            case 941:
                return 89;
            case 2605:
            case 2616:
            case 2607:
                return 9287;
            case 2598:
            case 2599:
            case 2600:
                return 9287;
            case 10773: return 13154;
            case 19:
                return 7050;
            case 2615:
                return 9287;
            case 8352: case 8350: case 8351:
                return 10923;
            case 1265:
                return 1313;
            case 2591: return 9287;
            case 105: return 4927;
            case 132: return 221;
            case 128: return 276;
            case 1648:
            case 1649:
            case 1650:
            case 1651:
            case 1652: return 9127;
            case 1653: return 9446;
            case 111:
                return 4671;
            case 708:
            case 709:
                return 170;
            case 2313:
            case 2314:
            case 2315:
                case 41:
                return 5388;
            case 4400:
                return 5328;
            case 4397:
            case 4398:
            case 4399:
                return 4273;
            case 4401:
                return 6250;
            case 4402:
            case 4403:
                return 6255;
            case 4393:
            case 4394:
            case 5377:
            case 5378:
            case 5379:
            case 5380: return 5574;
            case 4395: return 4934;
            case 5529:
                return 5783;
            case 4404:
            case 4405:
            case 4406:
                return 4267;
            case 4407:
            case 4408:
            case 4409:
            case 4410:
            case 4411:
            case 4412:
                return 6189;
            case 4415:
                return 2706;
            case 4413:
            case 4414:
                return 6578;
            case 4353:
                return 4232;
            case 117:
                return 4651;
            case 1618:
                return 9132;
            case 6260:
                return 7061;
            case 6261:
            case 6263:
            case 6265:
                return 6155;
            case 6222:
                return 6974;
            case 6223:
            case 6225:
            case 6227:
                return 6955;
            case 6247:
                return 6966;
            case 6248:
                return 6375;
            case 6250:
                return 7017;
            case 6252:
                return 7010;
            case 6203:
                return 6944;
            case 6204:
            case 6206:
            case 6208:
                return 65;
            case 1153:
            case 1154:
            case 1155:
            case 1156:
            case 1157:
                return 6225;
            case 1160:
                return 6237;
            case 2881:
            case 2882:
            case 2883:
                return 2852;
            case 86:
            case 87:
                return 4934;
            case 82:
            case 83:
            case 84:
                return 65;
            case 1585:
                return 4664;
            case 1588:
                return 4657;
            case 1593:
                return 4657;
            case 134:
                return 5328;
            case 49:
                return 6563;
            case 90:
                return 5489;
            case 103:
            case 491:
                return 5541;
            case 104:
                return 5533;
            case 119:
                return 100;
            case 78:
                return 4916;
            case 127:
                return 186;
            case 52:
                return 26;
            case 1610:
                return 9454;

            case 107:
            case 144:
                return 6255;
            case 3260:
                return 430;
            case 113:
                return 8578;
            case 81:
            case 1768:
            case 1767:
            case 1766:
                return 5850;
            case 89:
                return 6375;
            case 50: // kbd
                return 89;
            case 7160://Cockroach soldier
            case 7159:
            case 7158:
                return 8793;
            case 8133://corp
                return 10386;

            case 2256:
                return 403;



            case 21:
                return 403;

            case 112:
                return 4657;

            case 2745:
                return 9278;

            case 18:
                return 403;

            case 92:
                return 0;

            case 4389:
            case 4390:
            case 4391:return 1186;

            default:
                return 1834;

        }
    }

    public int attackSpeed(int i){
        switch(i){
            default:
                return 4;
        }
    }
    public int offset(int npcid) {
        switch (npcid) {
            case 50:
                return 2;
        }
        return 0;
    }

    public int getNpcAttackType(NPC npc) {
        switch (npc.npcType) {
            case 2025: // Ahrim
                return 2; // Magic only

            case 2028: // Karil
                return 1; // Ranged only

            case 2030: // Dharok
            case 2026: // Guthan
                return 0; // Melee only

            case 8133: // Corporeal Beast
                return misc.random(1) == 0 ? 0 : 1; // Melee or Range

            case 6222: // Kree'arra (Armadyl boss)
                return misc.random(1) == 0 ? 1 : 2; // Range or Magic

            case 2881: // Supreme (Dag King - mage)
            case 1913:
                return 2;

            case 2882: // Prime (Dag King - range)
                return 1;

            case 2883: // Rex (Dag King - melee)
                return 0;

            case 2745: // Jad
                return misc.random(1) == 0 ? 1 : 2;

            case 3491: // Kolodion
                return 2;

            default:
                return 0; // Default to melee
        }
    }
    public boolean goodDistance(int x1, int y1, int x2, int y2, int distance) {
        return Math.abs(x1 - x2) <= distance && Math.abs(y1 - y2) <= distance;
    }
    public int getHitDelay(NPC npc) {
        return switch (npc.attackType) {
            case 0 -> 1; // Melee
            case 1 -> 2; // Ranged
            case 2 -> 3; // Magic
            default -> 1;
        };
    }

    public boolean attackPlayer(int npcId) {
        NPC npc = npcs[npcId];
        if (npc == null || npc.IsDead) return false;

        int playerIndex = npc.StartKilling;
        if (!isPlayerValid(playerIndex)) {
            resetAttackPlayer(npcId);
            return false;
        }

        client player = (client) server.playerHandler.players[playerIndex];

        if (!playerHasDirection(player)) {
            handleClipping(npcId);
            return false;
        }

        handleFacing(npc, player);

        // Determine attack range based on attack type
        int allowedDistance = switch (npc.attackType) {
            case 2 -> getDistanceForNpc(npc);         // Magic
            case 1 -> Math.min(7, getDistanceForNpc(npc)); // Ranged
            case 0 -> 1;                                // Melee
            default -> 1;
        };

        if (!misc.goodDistance(npc.absX, npc.absY, player.absX, player.absY, allowedDistance)) {
            return false;
        }

        if (npc.actionTimer > 0 || player.IsDead) {
            return false;
        }

        if (shouldTriggerRingOfLife(player)) {
            player.SafeMyLife = true;
            return true;
        }

        npc.attackType = getNpcAttackType(npc); // Optional per-tick logic
        handleNpcTransform(npc);

        // Spell preloading
        if (npc.attackType == 2) {
            loadSpell(npc);
        }

        int damage = calculateHit(npc, player);
        if (damage > player.NewHP) damage = player.NewHP;

        // === Multi-Attack Logic ===
        if (multiAttacks(npcId)) {
            for (Player p : server.playerHandler.players) {
                if (p == null || p.IsDead || !p.inMulti()) continue;
                if (goodDistance(npc.absX, npc.absY, p.absX, p.absY, 1)) {
                    client c = (client) p;
                    int multiDamage = calculateHit(npc, c);
                    if (npc.attackType == 2) {
                        doMagicGFX(npc, c);
                    }
                    c.hitDiff = multiDamage;
                    c.hitUpdateRequired = true;
                    c.updateRequired = true;
                    c.appearanceUpdateRequired = true;
                    c.startAnimation(c.GetBlockAnim(c.playerEquipment[c.playerWeapon]));
                }
            }
        }

        // === Attack Type Logic ===
        switch (npc.attackType) {
            case 0 -> { // Melee
                npc.animNumber = getNpcAttackAnimation(npcId);
                player.sendSound(getNpcAttackSound(npc.npcType), 4, 0);
            }
            case 1 -> { // Ranged
                npc.animNumber = getNpcAttackAnimation(npcId);
                player.sendSound(getNpcAttackSound(npc.npcType), 4, 0);
                npc.projectileId = getProjectile(npc, 1);
                if (npc.projectileId > 0) sendProjectile(npc, player);
            }
            case 2 -> { // Magic
                npc.animNumber = getNpcAttackAnimation(npcId);
                player.sendSound(getNpcAttackSound(npc.npcType), 4, 0);
                if (npc.projectileId > 0) sendProjectile(npc, player);
                doMagicGFX(npc, player);
                damage = getMagicDamage(npc, player);
                if (damage > player.NewHP) damage = player.NewHP;
            }
        }

        // Block animation
        player.sendSound(soundConfig.getPlayerBlockSounds(player), 4, 0);
        player.startAnimation(player.GetBlockAnim(player.playerEquipment[player.playerWeapon]));

        npc.animUpdateRequired = true;
        npc.updateRequired = true;

        npc.hitDelayTimer = getHitDelay(npc); // Schedule delayed hit
        npc.pendingDamage = damage;

        int animId = npc.animNumber;
        int actualLength = AnimationLength.getFrameLength(animId);
        npc.actionTimer = Math.max(4, actualLength);

        return true;
    }


    private int getMagicDamage(NPC npc, client p) {
        switch (npc.npcType) {
            case 1645: return 6 + misc.random(43);
            case 509:  return 8 + misc.random(20);
            case 1241: return 2 + misc.random(19);
            case 124:  return 4 + misc.random(35);
            case 1246: return 4 + misc.random(35);
            case 1159: return 2 + misc.random(88);
            case 54:   return 2 + misc.random(96);
            default:   return misc.random(npc.MaxHit);
        }
    }
    private int getProjectile(NPC npc, int type) {
        switch (npc.npcType) {
            case 3231: return 9;
            // Add more custom cases if needed
            default: return 0;
        }
    }
    private void doMagicGFX(NPC npc, client p) {
        if (npc == null || p == null)
            return;

        switch (npc.npcType) {
            case 1645:
                npc.stillgfx(368, npc.absY, npc.absX); // Caster gfx
                p.stillgfx(369, p.absY, p.absX); // Target impact
                break;

            case 1241:
                p.stillgfx(363, p.absY, p.absX);
                break;

            case 1246:
                npc.stillgfx(368, npc.absY, npc.absX);
                p.stillgfx(367, p.absY, p.absX);
                break;

            case 1159:
                npc.stillgfx(553, npc.absY, npc.absX); // Caster flash
                p.stillgfx(552, p.absY, p.absX);       // Target hit
                break;

            case 54:
                npc.stillgfx(196, npc.absY, npc.absX);
                p.stillgfx(197, p.absY, p.absX);
                break;

            case 1913: // 🔮 Kemli - Ice Barrage
                npc.stillgfx(368, npc.absY, npc.absX); // Ice casting splash
                p.stillgfx(369, p.absY, p.absX);       // Ice explosion on player
                break;

            case 3491: // Kolodion - Ancient spell
                npc.stillgfx(130, npc.absY, npc.absX); // Launch
                p.stillgfx(131, p.absY, p.absX);       // Hit
                break;

            default:
                if (npc.endGfx > 0)
                    p.stillgfx(npc.endGfx, p.absY, p.absX);
                if (npc.startGfx > 0)
                    npc.stillgfx(npc.startGfx, npc.absY, npc.absX);
                break;
        }
    }


    public void resetAttackPlayer(int npcId) {
        if (npcId < 0 || npcId >= npcs.length || npcs[npcId] == null) return;

        NPC npc = npcs[npcId];
        int playerIndex = npc.StartKilling;

        if (playerIndex >= 0 && playerIndex < server.playerHandler.players.length) {
            client player = (client) server.playerHandler.players[playerIndex];
            if (player != null && player.IsAttackingNPC && player.attacknpc == npcId) {
                player.IsAttackingNPC = false;
                player.attacknpc = -1;
            }
        }

        npc.StartKilling = 0;
        npc.RandomWalk = true;
        npc.face = 0;
        npc.faceUpdateRequired = true;
        npc.followPlayer = -1;
        npc.killedBy = 0;
    }

    private static final int RING_OF_LIFE_ID = 2570;

    private boolean isPlayerValid(int index) {
        return index >= 0 && index < server.playerHandler.players.length && server.playerHandler.players[index] != null;
    }

    private boolean playerHasDirection(client player) {
        return player.DirectionCount >= 2;
    }

    private void handleFacing(NPC npc, client player) {
        npc.faceplayer(player.index);
        npc.enemyX = player.absX;
        npc.enemyY = player.absY;

        if (npc.absX != player.absX || npc.absY != player.absY) {
            player.viewTo(npc.absX, npc.absY);
        }

        player.face = npc.npcId;
        player.faceUpdateRequired = true;

        FollowPlayerCB(npc.npcId, player.index);
        handleClipping(npc.npcId);
    }

    private boolean isInRange(NPC npc, client player) {
        return GoodDistance(npc.absX, npc.absY, player.absX, player.absY, npc.getNPCSize());
    }

    private boolean shouldTriggerRingOfLife(client player) {
        int currentHP = player.playerLevel[player.playerHitpoints];
        int maxHP = getLevelForXP(player.playerXP[player.playerHitpoints]);
        return player.playerEquipment[player.playerRing] == RING_OF_LIFE_ID && currentHP <= maxHP / 10;
    }

    private void handleNpcTransform(NPC npc) {
        int hp = npc.HP;

        switch (npc.npcType) {
            case 8352:
                if (hp >= 1000 && hp <= 1500) npc.requestTransform(8351);
                break;
            case 8351:
                if (hp >= 500 && hp <= 1000) npc.requestTransform(8350);
                break;
            case 8350:
                if (hp <= 500) npc.requestTransform(8352);
                break;
        }
    }
    private int getPlayerDefense(client player, int npcAttackType) {
        int baseDef = 10; // everyone has a minimum defense

        int bonus;
        switch (npcAttackType) {
            case 0: // Melee
                bonus = player.playerBonus[5]; // melee defense
                break;
            case 1: // Ranged
                bonus = player.playerBonus[6]; // ranged defense
                break;
            case 2: // Magic
                bonus = player.playerBonus[7]; // magic defense
                break;
            default:
                bonus = 0;
        }

        // Optional: add a proxy defense level if Allstar tracks it
        int defLevel = player.playerLevel[1]; // Defense level

        return bonus + defLevel + baseDef;
    }

    private int calculateHit(NPC npc, client player) {
        // Load NPC stats
        NPCStatLoader.NPCStats stats = server.npcStatLoader.getStats(npc.npcType);
        if (stats == null) {
            stats = new NPCStatLoader.NPCStats();
            stats.maxHit = npc.MaxHit; // fallback
            stats.attack = 1;
            stats.strength = 1;
        }

        int maxHit = stats.maxHit > 0 ? stats.maxHit : 1;

        // --- ACCURACY CHECK ---
        // NPC attack level + bonus vs player defense + bonus
        int npcAttackLevel = stats.attack;
        int npcAttackBonus = stats.attack; // optionally, use separate bonus if available

        int playerDefLevel = player.playerLevel[1]; // Defense level
        int playerArmorBonus = player.playerBonus[getBonusIndex(npc.attackType)];

        // Simple OSRS-style formula for hit chance
        int hitChance = (npcAttackLevel + npcAttackBonus) * 2;
        int defenseChance = (playerDefLevel + playerArmorBonus);

        boolean hits = misc.random(hitChance) > misc.random(defenseChance);
        if (!hits) return 0;

        // --- DAMAGE CALCULATION ---
        int strength = stats.strength;
        int baseDamage = misc.random(maxHit); // Max hit is usually precomputed based on strength

        // --- PRAYER REDUCTION ---
        if (player.prayerActive[18] && npc.attackType == 0) baseDamage /= 4; // melee
        if (player.prayerActive[17] && npc.attackType == 1) baseDamage /= 4; // ranged
        if (player.prayerActive[16] && npc.attackType == 2) baseDamage /= 4; // magic

        // --- LIMIT TO CURRENT HP ---
        return Math.min(baseDamage, player.NewHP);
    }

    /** Helper: get correct bonus index based on attack type */
    private int getBonusIndex(int attackType) {
        return switch (attackType) {
            case 0 -> 0; // Melee defense bonus index (stab)
            case 1 -> 1; // Ranged defense bonus
            case 2 -> 2; // Magic defense bonus
            default -> 0;
        };
    }


    private void playAttackEffects(NPC npc, client player) {
        player.sendSound(getNpcAttackSound(npc.npcType), 4, 0);
        npc.animNumber = getNpcAttackAnimation(npc.npcType);
        npc.animUpdateRequired = true;
        npc.updateRequired = true;

        if (npc.projectileId > 0) {
            sendProjectile(npc, player);
        }

        player.sendSound(soundConfig.getPlayerBlockSounds(player), 5, 0);
        player.startAnimation(player.GetBlockAnim(player.playerEquipment[player.playerWeapon]));
    }

    private void sendProjectile(NPC npc, client player) {
        int startX = npc.getX() + offset(npc.npcType);
        int startY = npc.getY() + offset(npc.npcType);
        int targetX = player.getX();
        int targetY = player.getY();
        int offX = (startX - targetX) * -1;
        int offY = (startY - targetY) * -1;
        int centerX = startX + npc.getNPCSize() / 2;
        int centerY = startY + npc.getNPCSize() / 2;

        player.createPlayersProjectile(
                centerX, centerY, offX, offY, 50,
                getProjectileSpeed(npc.npcType),
                npc.projectileId,
                getProjectileStartHeight(npc.npcId, npc.projectileId),
                getProjectileEndHeight(npc.npcId, npc.projectileId),
                -player.index - 1, 65
        );
    }

    private void applyDamage(client player, int damage) {
        player.hitDiff = damage;
        player.updateRequired = true;
        player.hitUpdateRequired = true;
        player.appearanceUpdateRequired = true;
    }

    private int getProjectileStartHeight (int npcId, int projectileId) {
        switch(npcId){
            case 3132:
                return 9;
            default:
                return 43;
        }
    }

    private int getProjectileSpeed (int npcType) {
        return 70;
    }

    private int getProjectileEndHeight (int npcId, int projectileId) {

        switch(npcId){
            case 3132:
                return 9;
            default:
                return 31;
        }
    }

    public boolean AttackNPCMage(int NPCID) {
        int EnemyX = npcs[npcs[NPCID].attacknpc].absX;
        int EnemyY = npcs[npcs[NPCID].attacknpc].absY;
        int EnemyHP = npcs[npcs[NPCID].attacknpc].HP;
        int hitDiff = 0;
        int Npchitdiff = 0;
        int wepdelay = 0;

        // hitDiff = misc.random(npcs[NPCID].MaxHit);
        if (npcs[NPCID].actionTimer == 0) {
            if (npcs[npcs[NPCID].attacknpc].IsDead == true) {
                ResetAttackNPC(NPCID);
                // npcs[NPCID].textUpdate = "Oh yeah I win bitch!";
                // npcs[NPCID].textUpdateRequired = true;
                npcs[NPCID].animNumber = 2103;
                npcs[NPCID].animUpdateRequired = true;
                npcs[NPCID].updateRequired = true;
            } else {
                npcs[NPCID].animNumber = 711; // mage attack
                if (npcs[NPCID].npcType == 1645) {
                    gfxAll(369, EnemyY, EnemyX);
                    hitDiff = 6 + misc.random(43);
                }
                if (npcs[NPCID].npcType == 1645) {
                    gfxAll(369, EnemyY, EnemyX);
                    hitDiff = 6 + misc.random(43);
                }
                if (npcs[NPCID].npcType == 509) {
                    hitDiff = 8 + misc.random(20);
                }
                if (npcs[NPCID].npcType == 1241) {
                    gfxAll(363, EnemyY, EnemyX);
                    hitDiff = 2 + misc.random(19);
                }
                if (npcs[NPCID].npcType == 1246) {
                    gfxAll(368, npcs[NPCID].absY, npcs[NPCID].absX);
                    gfxAll(367, EnemyY, EnemyX);
                    hitDiff = 4 + misc.random(35);
                }
                if (npcs[NPCID].npcType == 1159) {
                    gfxAll(552, EnemyY, EnemyX);
                    hitDiff = 2 + misc.random(88);
                }
                if (npcs[NPCID].npcType == 54) {
                    gfxAll(197, EnemyY, EnemyX);
                    hitDiff = 2 + misc.random(96);
                }
                npcs[NPCID].animUpdateRequired = true;
                npcs[NPCID].updateRequired = true;
                if ((EnemyHP - hitDiff) < 0) {
                    hitDiff = EnemyHP;
                }
                npcs[npcs[NPCID].attacknpc].hitDiff = hitDiff;
                npcs[npcs[NPCID].attacknpc].attacknpc = NPCID;
                npcs[npcs[NPCID].attacknpc].updateRequired = true;
                npcs[npcs[NPCID].attacknpc].hitUpdateRequired = true;
                npcs[NPCID].actionTimer = AnimationLength.getFrameLength(npcs[NPCID].animNumber);
                return true;
            }
            return false;
        }
        return false;
    }
    public boolean AttackNPCRange(int NPCID) {
        int EnemyX = npcs[npcs[NPCID].attacknpc].absX;
        int EnemyY = npcs[npcs[NPCID].attacknpc].absY;
        int EnemyHP = npcs[npcs[NPCID].attacknpc].HP;
        int hitDiff = 0;
        int Npchitdiff = 0;
        int wepdelay = 0;

        // hitDiff = misc.random(npcs[NPCID].MaxHit);
        if (npcs[NPCID].actionTimer == 0) {
            if (npcs[npcs[NPCID].attacknpc].IsDead == true) {
                ResetAttackNPC(NPCID);
                // npcs[NPCID].textUpdate = "Oh yeah I win bitch!";
                // npcs[NPCID].textUpdateRequired = true;
                npcs[NPCID].animNumber = 2103;
                npcs[NPCID].animUpdateRequired = true;
                npcs[NPCID].updateRequired = true;
            } else {
                npcs[NPCID].animNumber = 711; // mage attack
                if (npcs[NPCID].npcType == 1645) {
                    gfxAll(369, EnemyY, EnemyX);
                    hitDiff = 6 + misc.random(43);
                }
                if (npcs[NPCID].npcType == 1645) {
                    gfxAll(369, EnemyY, EnemyX);
                    hitDiff = 6 + misc.random(43);
                }
                if (npcs[NPCID].npcType == 509) {
                    hitDiff = 8 + misc.random(20);
                }
                if (npcs[NPCID].npcType == 1241) {
                    gfxAll(363, EnemyY, EnemyX);
                    hitDiff = 2 + misc.random(19);
                }
                if (npcs[NPCID].npcType == 1246) {
                    gfxAll(368, npcs[NPCID].absY, npcs[NPCID].absX);
                    gfxAll(367, EnemyY, EnemyX);
                    hitDiff = 4 + misc.random(35);
                }
                if (npcs[NPCID].npcType == 1159) {
                    gfxAll(552, EnemyY, EnemyX);
                    hitDiff = 2 + misc.random(88);
                }
                if (npcs[NPCID].npcType == 54) {
                    gfxAll(197, EnemyY, EnemyX);
                    hitDiff = 2 + misc.random(96);
                }
                npcs[NPCID].animUpdateRequired = true;
                npcs[NPCID].updateRequired = true;
                if ((EnemyHP - hitDiff) < 0) {
                    hitDiff = EnemyHP;
                }
                npcs[npcs[NPCID].attacknpc].hitDiff = hitDiff;
                npcs[npcs[NPCID].attacknpc].attacknpc = NPCID;
                npcs[npcs[NPCID].attacknpc].updateRequired = true;
                npcs[npcs[NPCID].attacknpc].hitUpdateRequired = true;
                npcs[NPCID].actionTimer = AnimationLength.getFrameLength(npcs[NPCID].animNumber);
                return true;
            }
            return false;
        }
        return false;
    }
    public boolean AttackNPC(int NPCID) {
        NPC attacker = npcs[NPCID];
        int targetId = attacker.attacknpc;

        if (targetId < 0 || targetId >= npcs.length || npcs[targetId] == null) {
            return false;
        }

        NPC target = npcs[targetId];

        if (!GoodDistance(target.absX, target.absY, attacker.absX, attacker.absY, 1)) {
            return false;
        }

        if (target.IsDead || target.HP <= 0) {
            ResetAttackNPC(NPCID);
            attacker.textUpdate = "Oh yeah I win bitch!";
            attacker.textUpdateRequired = true;
            attacker.animNumber = 2103;
            attacker.animUpdateRequired = true;
            attacker.updateRequired = true;
            return true;
        }

        int hitDiff = misc.random(attacker.MaxHit);
        if (hitDiff > target.HP) hitDiff = target.HP;

        // Animations per NPC type
        switch (attacker.npcType) {
            case 9:
            case 1605:
                attacker.animNumber = 386;
                break;
            case 3200:
                attacker.animNumber = 0x326;
                break;
        }

        attacker.animUpdateRequired = true;
        attacker.updateRequired = true;
        attacker.actionTimer = AnimationLength.getFrameLength(attacker.animNumber);
        attacker.IsAttackingNPC = true;
        attacker.RandomWalk = false;

        target.hitDiff = hitDiff;
        target.hitUpdateRequired = true;
        target.updateRequired = true;
        target.attacknpc = NPCID;
        target.IsUnderAttackNpc = true;

        return true;
    }

    public boolean ResetAttackNPC(int NPCID) {
        npcs[NPCID].IsUnderAttackNpc = false;
        npcs[NPCID].IsAttackingNPC = false;
        npcs[NPCID].attacknpc = -1;
        npcs[NPCID].RandomWalk = true;
        npcs[NPCID].animNumber = 0x328;
        npcs[NPCID].animUpdateRequired = true;
        npcs[NPCID].updateRequired = true;
        return true;
    }

    public int getLevelForXP(int exp) {
        int points = 0;
        int output = 0;

        for (int lvl = 1; lvl <= 135; lvl++) {
            points += Math.floor(
                    (double) lvl + 300.0 * Math.pow(2.0, (double) lvl / 7.0));
            output = (int) Math.floor(points / 4);
            if (output >= exp) {
                return lvl;
            }
        }
        return 0;
    }

    public boolean GoodDistance(int objectX, int objectY, int playerX, int playerY, int distance) {
        for (int i = 0; i <= distance; i++) {
            for (int j = 0; j <= distance; j++) {
                if ((objectX + i) == playerX
                        && ((objectY + j) == playerY || (objectY - j) == playerY
                        || objectY == playerY)) {
                    return true;
                } else if ((objectX - i) == playerX
                        && ((objectY + j) == playerY || (objectY - j) == playerY
                        || objectY == playerY)) {
                    return true;
                } else if (objectX == playerX
                        && ((objectY + j) == playerY || (objectY - j) == playerY
                        || objectY == playerY)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean ResetAttackPlayer(int NPCID) {
        npcs[NPCID].IsUnderAttack = false;
        npcs[NPCID].StartKilling = 0;
        npcs[NPCID].RandomWalk = true;
        npcs[NPCID].animNumber = 0x328;
        npcs[NPCID].animUpdateRequired = true;
        npcs[NPCID].updateRequired = true;
        return true;
    }

    public boolean loadAutoSpawn(String FileName) {
        String line = "";
        String token = "";
        String token2 = "";
        String token2_2 = "";
        String[] token3 = new String[10];
        boolean EndOfFile = false;
        int ReadMode = 0;
        BufferedReader characterfile = null;

        try {
            characterfile = new BufferedReader(new FileReader("./" + FileName));
        } catch (FileNotFoundException fileex) {
            misc.println(FileName + ": file not found.");
            return false;
        }
        try {
            line = characterfile.readLine();
        } catch (IOException ioexception) {
            misc.println(FileName + ": error loading file.");
            return false;
        }
        while (!EndOfFile && line != null) {
            line = line.trim();
            int spot = line.indexOf("=");

            if (spot > -1) {
                token = line.substring(0, spot);
                token = token.trim();
                token2 = line.substring(spot + 1);
                token2 = token2.trim();
                token2_2 = token2.replaceAll("\t\t", "\t");
                token2_2 = token2_2.replaceAll("\t\t", "\t");
                token2_2 = token2_2.replaceAll("\t\t", "\t");
                token2_2 = token2_2.replaceAll("\t\t", "\t");
                token2_2 = token2_2.replaceAll("\t\t", "\t");
                token3 = token2_2.split("\t");
                if (token.equals("spawn")) {
                    newNPC(Integer.parseInt(token3[0]),
                            Integer.parseInt(token3[1]),
                            Integer.parseInt(token3[2]),
                            Integer.parseInt(token3[3]),
                            Integer.parseInt(token3[4]),
                            Integer.parseInt(token3[5]),
                            Integer.parseInt(token3[6]),
                            Integer.parseInt(token3[7]),
                            Integer.parseInt(token3[8]),
                            GetNpcListHP(Integer.parseInt(token3[0])), true);
                }
            } else {
                if (line.equals("[ENDOFSPAWNLIST]")) {
                    try {
                        characterfile.close();
                    } catch (IOException ioexception) {
                    }
                    return true;
                }
            }
            try {
                line = characterfile.readLine();
            } catch (IOException ioexception1) {
                EndOfFile = true;
            }
        }
        try {
            characterfile.close();
        } catch (IOException ioexception) {
        }
        return false;
    }


    public int GetNpcListHP(int NpcID) {
        for (int i = 0; i < maxListedNPCs; i++) {
            if (NpcList[i] != null) {
                if (NpcList[i].npcId == NpcID) {
                    return NpcList[i].npcHealth;
                }
            }
        }
        return 0;
    }
    public int GetNpcListCombat(int NpcID) {
        for (int i = 0; i < maxListedNPCs; i++) {
            if (NpcList[i] != null) {
                if (NpcList[i].npcId == NpcID) {
                    return NpcList[i].npcCombat;
                }
            }
        }
        return 0;
    }
    public String GetNpcName(int NpcID) {
        for (int i = 0; i < maxListedNPCs; i++) {
            if (NpcList[i] != null) {
                if (NpcList[i].npcId == NpcID) {
                    return NpcList[i].npcName.replace("_", " ");
                }
            }
        }
        return null;
    }

    public boolean loadNPCList(String FileName) {
        String line = "";
        String token = "";
        String token2 = "";
        String token2_2 = "";
        String[] token3 = new String[10];
        boolean EndOfFile = false;
        int ReadMode = 0;
        BufferedReader characterfile = null;

        try {
            characterfile = new BufferedReader(new FileReader("./" + FileName));
        } catch (FileNotFoundException fileex) {
            misc.println(FileName + ": file not found.");
            return false;
        }
        try {
            line = characterfile.readLine();
        } catch (IOException ioexception) {
            misc.println(FileName + ": error loading file.");
            return false;
        }
        while (!EndOfFile && line != null) {
            line = line.trim();
            int spot = line.indexOf("=");

            if (spot > -1) {
                token = line.substring(0, spot);
                token = token.trim();
                token2 = line.substring(spot + 1);
                token2 = token2.trim();
                token2_2 = token2.replaceAll("\t\t", "\t");
                token2_2 = token2_2.replaceAll("\t\t", "\t");
                token2_2 = token2_2.replaceAll("\t\t", "\t");
                token2_2 = token2_2.replaceAll("\t\t", "\t");
                token2_2 = token2_2.replaceAll("\t\t", "\t");
                token3 = token2_2.split("\t");
                if (token.equals("npc")) {
                    newNPCList(Integer.parseInt(token3[0]), token3[1],
                            Integer.parseInt(token3[2]),
                            Integer.parseInt(token3[3]));
                }
            } else {
                if (line.equals("[ENDOFNPCLIST]")) {
                    try {
                        characterfile.close();
                    } catch (IOException ioexception) {
                    }
                    return true;
                }
            }
            try {
                line = characterfile.readLine();
            } catch (IOException ioexception1) {
                EndOfFile = true;
            }
        }
        try {
            characterfile.close();
        } catch (IOException ioexception) {
        }
        return false;
    }


    public void println(String str) {
        System.out.println(str);
    }
}
