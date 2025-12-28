import java.util.LinkedList;
import java.util.Queue;

public class NPC extends Entity {
    public final int[][] fearShadow = new int[100][100];
    public final int[][] CONTAIN_THIS = new int[2][2];
    public boolean faceUpdateRequired;
    public int face;
    public int npcId;
    public int prayerUsed = 0;
    public int extraHitDelay = 0, glod, deadCyle = 1;
    public int diliHits = 30, nexStage = 0, cooldown = 0, mustDie = 0;
    /**
     * Face
     */
    //public int FocusPointX = -1, FocusPointY = -1;
    //public int face = 0;
    public boolean fighting = false;
    public int hitDiff2 = 0;
    public boolean hitUpdateRequired2;
    public int PoisonDelay = 999999;
    public int PoisonClear = 0;
    public int heightLevel;
    public boolean FaceDirection;
    public int FocusPointX;
    public int FocusPointY;
    public int makeX, makeY, moverangeX1, moverangeY1, moverangeX2, moverangeY2, moveX, moveY, direction, walkingType, attacknpc, followPlayer;
    public int spawnX, spawnY;
    public int viewX, viewY;
    public boolean canAttackNpcs = false; // default: off
    public int targetNpcIndex = -1;       // which NPC it's attacking
    public int faction = -1; // -1 = neutral, 0 = humans, 1 = demons, etc.
    // Inside your NPC class
    public int targetScanCooldown = 0;
    public boolean walkingHome, underAttack;
    public int HP, MaxHP, hitDiff, MaxHit, actionTimer, hitDelayTimer, pendingDamage,StartKilling, enemyX, enemyY;
    public boolean IsDead, DeadApply, NeedRespawn, IsUnderAttack, IsClose, Respawns, IsUnderAttackNpc, IsAttackingNPC, poisondmg, walkingToPlayer, followingPlayer;
    public int[] Killing = new int[PlayerHandler.maxPlayers];
    public boolean RandomWalk;
    public boolean dirUpdateRequired;
    public boolean hitUpdateRequired;
    public boolean textUpdateRequired;
    public boolean faceToUpdateRequired;
    public boolean attackable = true;
    public String textUpdate;
    public int spawnedBy;
    public int Killes;
    public int endGfx;
    public int freezeTimer, killerId;
    public long lastDamageTaken;
    public int underAttackBy;
    public boolean isGodwarsNpc = false;
    public int godwarsFaction = -1;
    public int npcCombatDelay = 0;
    public int attackTimer = 0;      // cooldown for attacking
    public boolean summoner;
    public int summonedBy;
    public int projectileId;
    public int oldIndex;
    public int attack;
    public int killedBy;
    public int lastX;
    public int lastY;
    public boolean randomWalk;
    public long lastAttacked;
    public long lastAggroTick = 0;
    public int combatLevel;
    public boolean alwaysAggressive = false; // True for dragons, demons, bosses, etc.

    public int startGfx;
    public long lastAggroCheck;
    int npcSize;
    private Tile currentTile;
    private boolean transformUpdateRequired = false;
    public int transformId;
    private boolean gfxUpdateRequired = false;
    private boolean faceEntityUpdateRequired = false, isTransformed = false;
    private boolean forceMovementUpdateRequired;
    private long lastRandomWalk;
    private long lastRandomWalkHome;

    private long randomWalkDelay;
    private long randomStopDelay;
    private boolean teleporting;
    protected Hitmark hitmark1;
    protected Hitmark hitmark2;
    // Inside NPC.java or as runtime fields
    public int hansWalkTimer = 0;
    public int hansWaypointIndex = 0;
    public int attackType = 0; // -1 = unknown, 0 = melee, 1 = range, 2 = magic, etc.
    public NPCStatLoader.NPCStats stats;

    public Queue<int[]> walkingQueue = new LinkedList<>();
    public NPC(int _npcId, int _npcType) {
        npcId = _npcId;
        index = _npcType;
        direction = -1;
        IsDead = false;
        DeadApply = false;
        actionTimer = 0;
        RandomWalk = true;
        StartKilling = 0;
        IsUnderAttack = false;
        IsClose = false;
        for (int i = 0; i < Killing.length; i++) {
            Killing[i] = 0;
        }
    }
    public long getLastRandomWalk() {
        return lastRandomWalk;
    }

    public long getLastRandomWalkhome() {
        return lastRandomWalkHome;
    }

    public void setLastRandomWalkHome(long lastRandomWalkHome) {
        this.lastRandomWalkHome = lastRandomWalkHome;
    }
    public long getRandomWalkDelay() {
        return randomWalkDelay;
    }
    /**
     * Teleport
     * @param x
     * @param y
     * @param z
     */
    public void teleport(int x, int y, int z) {
        teleporting = true;
        absX = x;
        absY = y;
        heightLevel = z;
    }
    public void move() {
        absX += moveX;
        absY += moveY;
        moveX = 0;
        moveY = 0;
    }
    public void dealDoubleDamage(int primary, int secondary, Hitmark hitmark) {
        // No point applying damage to a dead NPC
        if (HP <= 0 || IsDead) {
            IsDead = true;
            return;
        }

        // Default to HIT if something passed a MISS with damage > 0
        if (hitmark == null) {
            hitmark = Hitmark.HIT;
        } else if (hitmark == Hitmark.MISS && (primary > 0 || secondary > 0)) {
            hitmark = Hitmark.HIT;
        }

        // Clamp damage to not exceed current HP
        int totalDamage = primary + secondary;
        if (totalDamage > HP) totalDamage = HP;

        // If both hits present, scale them proportionally
        if (primary > 0 && secondary > 0) {
            float ratio = primary / (float)(primary + secondary);
            primary = Math.round(totalDamage * ratio);
            secondary = totalDamage - primary;
        } else {
            primary = totalDamage;
            secondary = 0;
        }

        HP -= totalDamage;
        if (HP <= 0) {
            HP = 0;
            IsDead = true;
        }

        // Avoid hitting if total damage is 0
        if (totalDamage == 0) {
            hitDiff = 0;
            hitmark1 = Hitmark.MISS;
            hitUpdateRequired = true;
        } else {
            hitDiff = primary;
            hitmark1 = hitmark;
            hitUpdateRequired = true;

            if (secondary > 0) {
                hitDiff2 = secondary;
                hitmark2 = hitmark;
                hitUpdateRequired2 = true;
            }
        }

        updateRequired = true;
    }


    private boolean supportsDoubleHit() {
        return false; // or true if your client supports it
    }





    public void setRandomWalkDelay(long randomWalkDelay) {
        this.randomWalkDelay = randomWalkDelay;
    }
    public void faceplayer(int i) {
        face = i + 32768;
        faceEntityUpdateRequired = true;
        updateRequired = true;
    }

    public void updateface(stream stream1) {
        stream1.writeWord(face);
    }

    public int getNPCSize() {
        return NPCSize.get(index);
    }

    public String Glod() {
        int talk = misc.random(2);
        switch (talk) {
            case 1:
                return "Glod Angry!";
            case 2:
                return "Glod Bash!";
        }
        return "Glod Smash!";
    }

    public void updateNPCMovement(stream str) {
        if (direction == -1) {
            // don't have to update the npc position, because the npc is just standing
            if (updateRequired) {
                // tell client there's an update block appended at the end
                str.writeBits(1, 1);
                str.writeBits(2, 0);
            } else {
                str.writeBits(1, 0);
            }
        } else {
            // send "walking packet"
            str.writeBits(1, 1);
            str.writeBits(2, 1);        // updateType
            str.writeBits(3, misc.xlateDirectionToClient[direction]);
            if (updateRequired) {
                str.writeBits(1, 1);        // tell client there's an update block appended at the end
            } else {
                str.writeBits(1, 0);
            }
        }
    }

    public void forceChat(String text) {
        textUpdate = text;
        textUpdateRequired = true;
        updateRequired = true;
    }

    public void faceNPC(int i) {
        face = i;
        faceEntityUpdateRequired = true;
        updateRequired = true;
    }

    public void facePlayer(int player) {
        face = player + 32768;
        faceEntityUpdateRequired = true;
        updateRequired = true;
    }

    public boolean animals() {
        switch (index) {
            case 5103:
            case 5104:
            case 5105:
                return true;
            default:
                return false;
        }
    }

    public void gethurt(int amount) {
        if (hitUpdateRequired && !hitUpdateRequired2) {
            hitUpdateRequired = true;
            hitDiff2 = amount;
        }
        if (!hitUpdateRequired) {
            hitUpdateRequired = true;
            hitDiff = amount;
        }
        updateRequired = true;
        HP -= amount;
    }

    public int getKiller() {
        int Killer = 0;
        int Count = 0;
        for (int i = 1; i < PlayerHandler.maxPlayers; i++) {
            if (false) {
                Killer = i;
                Count = 1;
            } else {
                if (Killing[i] > Killing[Killer]) {
                    Killer = i;
                    Count = 1;
                } else if (Killing[i] == Killing[Killer]) {
                    Count++;
                }
            }
        }
        return Killer;
    }
    public void shearSheep(client player, int itemNeeded, int itemGiven, int animation, final int currentId, final int newId, int transformTime) {
        if (!player.playerHasItem(itemNeeded)) {
            player.sendMessage("You need " + Item.getItemName(itemNeeded).toLowerCase() + " to do that.");
            return;
        }
        if (transformId == newId) {
            player.sendMessage("This sheep has already been shorn.");
            return;
        }
        if (NPCHandler.npcs[npcId].isTransformed) {
            return;
        }
        if (animation > 0) {
            player.startAnimation(animation);
        }
        requestTransform(newId);
        player.addItem(itemGiven, 1);
        player.sendMessage("You get some " + Item.getItemName(itemGiven).toLowerCase() + ".");
        CycleEventHandler.getSingleton().addEvent(player,new CycleEvent() {

            @Override
            public void execute(CycleEventContainer container) {
                requestTransform(currentId);
                container.stop();
            }

            @Override
            public void stop() {
                NPCHandler.npcs[npcId].isTransformed = false;
            }
        }, transformTime * 600);
    }

    public void appendNPCUpdateBlock(stream str) {
        if (!updateRequired) return;

        int updateMask = buildUpdateMask();
        if (updateMask >= 0x100) {
            // Old WRONG version:
            // updateMask |= 0x10; ❌ BAD: Conflicts with animation block!

            // ✅ CORRECT VERSION:
            updateMask |= 0x100;
            str.writeByte(updateMask & 0xFF);
            str.writeByte(updateMask >> 8);
        } else {
            str.writeByte(updateMask);
        }

        // Write update blocks in correct order
        if (animationRequest != -1) appendAnimationRequest(str);
        if (!IsDead && hitUpdateRequired2) appendHitUpdate2(str);
        if (gfxUpdateRequired) appendMask80Update(str);
        if (faceEntityUpdateRequired) appendFaceEntity(str);
        if (textUpdateRequired) str.writeString(textUpdate);
        if (hitUpdateRequired && !IsDead) appendHitUpdate(str);
        if (transformUpdateRequired) appendTransformUpdate(str);
        if (FocusPointX != -1) appendSetFocusDestination(str);

        // Apply actual damage logic after updates sent
    }
    private int buildUpdateMask() {
        int mask = 0;
        if (animationRequest != -1) mask |= 0x10;
        if (hitUpdateRequired2) mask |= 0x08;
        if (gfxUpdateRequired) mask |= 0x80;
        if (faceEntityUpdateRequired) mask |= 0x20;
        if (textUpdateRequired) mask |= 0x01;
        if (hitUpdateRequired) mask |= 0x40;
        if (transformUpdateRequired) mask |= 0x02;
        if (FocusPointX != -1) mask |= 0x04;
        return mask;
    }

    public int mask80var1 = 0;
    public int mask80var2 = 0;
    public void appendMask80Update(stream str) {
        str.writeUnsignedWord(mask80var1);
        str.writeDWord(mask80var2);
    }
    public void gfx100(int gfx) {
        mask80var1 = gfx;
        mask80var2 = 6553600;
        gfxUpdateRequired = true;
        updateRequired = true;
    }
    public void stillgfx(int gfx, int y, int x, int height) {
        this.mask80var1 = gfx;
        this.mask80var2 = 65536 * height;
        this.gfxUpdateRequired = true;
        this.updateRequired = true;
    }
    public void stillgfx(int gfx, int y, int x) {
        this.mask80var1 = gfx;
        this.mask80var2 = 65536; // default height 100
        this.gfxUpdateRequired = true;
        this.updateRequired = true;
    }

    public void gfx100(int gfx, int height) {
        mask80var1 = gfx;
        mask80var2 = 65536 * height;
        gfxUpdateRequired = true;
        updateRequired = true;
    }
    public void gfx0(int gfx) {
        mask80var1 = gfx;
        mask80var2 = 65536;
        gfxUpdateRequired = true;
        updateRequired = true;
    }
    public void requestTransform(int id) {
        transformId = id;
        transformUpdateRequired = true;
        updateRequired = true;
    }
    public void requestPetTransform(int id) {
        transformId = id;
        index = id;
        transformUpdateRequired = true;
        updateRequired = true;
    }
    public void clearUpdateFlags() {
        updateRequired = false;
        animationUpdateRequired = false;
        hitUpdateRequired2 = false;
        gfxUpdateRequired = false;
        faceEntityUpdateRequired = false;
        textUpdateRequired = false;
        hitUpdateRequired = false;
        transformUpdateRequired = false;
        textUpdate = null;
        moveX = 0;
        moveY = 0;
        teleporting = false;
        direction = -1;
        FocusPointX = -1;
        FocusPointY = -1;
    }


    // returns 0-7 for next walking direction or -1, if we're not moving
    public int getNextWalkingDirection() {
        int dir;

        dir = misc.direction(absX, absY, (absX + moveX), (absY + moveY));
        if (dir == -1) {
            return -1;
        }
        if (teleporting)
            return -1;
        dir >>= 1;
        absX += moveX;
        absY += moveY;
        return dir;
    }

    public void getNextNPCMovement(final int i) {
        direction = -1;
        if (freezeTimer == 0) {
            direction = getNextWalkingDirection();
        }
    }
    public void appendFaceEntity(stream str) {
        str.writeUnsignedWord(face);
    }
    @Override
    protected void appendHitUpdate(stream str) {
        HP -= hitDiff;
        if (HP <= 0) {
            IsDead = true;
        }
        if (hitmark1 != null && !hitmark1.isMiss() && hitDiff == 0) {
            hitDiff = 0;
            hitmark1 = Hitmark.MISS;
        }

        str.writeByteC(hitDiff); // What the perseon got 'hit' for
        if (hitDiff > 0 && !poisondmg) {
            str.writeByteS(1); // 0: red hitting - 1: blue hitting
        } else if (hitDiff > 0 && poisondmg) {
            str.writeByteS(2); // 0: red hitting - 1: blue hitting
        } else {
            str.writeByteS(0); // 0: red hitting - 1: blue hitting
        }
        str.writeUnsignedWord(HP);              // Current HP (NOT predicted!)
        str.writeUnsignedWord(MaxHP);           // Max HP
        poisondmg = false;
    }

    @Override
    protected void appendHitUpdate2(stream str) {
        HP -= hitDiff2;
        if (HP <= 0) {
            IsDead = true;
        }
        if (hitmark2 != null && !hitmark2.isMiss() && hitDiff2 == 0) {
            hitDiff2 = 0;
            hitmark2 = Hitmark.MISS;
        }
        str.writeByteA(hitDiff2);         // Hit amount
        if (hitDiff2 > 0 && !poisondmg) {
            str.writeByteC(1); // 0: red hitting - 1: blue hitting
        } else if (hitDiff2 > 0 && poisondmg) {
            str.writeByteC(2); // 0: red hitting - 1: blue hitting
        } else {
            str.writeByteC(0); // 0: red hitting - 1: blue hitting
        }
        str.writeUnsignedWord(HP);              // Current HP (already reduced!)
        str.writeUnsignedWord(MaxHP);           // Max HP
        poisondmg = false;
    }



    public void TurnNpcTo(int i, int j) {
        FocusPointX = 2 * i + 1;
        FocusPointY = 2 * j + 1;
        updateRequired = true;
        FaceDirection = true;
    }

    @Override
    protected void appendSetFocusDestination(stream stream1) {
        if (stream1 != null) {
            stream1.writeWordBigEndian(FocusPointX);
            stream1.writeWordBigEndian(FocusPointY);
        }
    }


    public void appendAnimationRequest(stream str) {
        str.writeWordBigEndian((animationRequest==-1) ? 65535 : animationRequest);
        str.writeByte(animationWaitCycles);
    }
    public void appendTransformUpdate(stream str) {
        str.writeWordBigEndianA(transformId);
    }

    public void appendDirUpdate(stream str) {
        str.writeWord(direction);
    }

    public void appendFaceToUpdate(stream str) {
        str.writeWordBigEndian(viewX);
        str.writeWordBigEndian(viewY);
    }

    public boolean inMulti() {
        return (absX >= 3136 && absX <= 3327 && absY >= 3519 && absY <= 3607)
                || (absX >= 3190 && absX <= 3327 && absY >= 3648 && absY <= 3839)
                || (absX >= 3200 && absX <= 3390 && absY >= 3840 && absY <= 3967)
                || (absX >= 2992 && absX <= 3007 && absY >= 3912 && absY <= 3967)
                || (absX >= 2946 && absX <= 2959 && absY >= 3816 && absY <= 3831)
                || (absX >= 3008 && absX <= 3199 && absY >= 3856 && absY <= 3903)
                || (absX >= 3008 && absX <= 3071 && absY >= 3600 && absY <= 3711)
                || (absX >= 3072 && absX <= 3327 && absY >= 3608 && absY <= 3647)
                || (absX >= 2624 && absX <= 2690 && absY >= 2550 && absY <= 2619)
                || (absX >= 2371 && absX <= 2422 && absY >= 5062 && absY <= 5117)
                || (absX >= 2896 && absX <= 2927 && absY >= 3595 && absY <= 3630)
                || (absX >= 2892 && absX <= 2932 && absY >= 4435 && absY <= 4464)
                || (absX >= 2256 && absX <= 2287 && absY >= 4680 && absY <= 4711);
    }

    public boolean inWild() {
        return absX > 2941 && absX < 3392 && absY > 3518 && absY < 3966
                || absX > 2941 && absX < 3392 && absY > 9918 && absY < 10366;
    }

    public int getX() {
        return absX;
    }

    public int getY() {
        return absY;
    }

    public int getId() {
        // TODO Auto-generated method stub
        return index;
    }

    public int getProjectileDelay () {
        return 0;
    }

    public boolean isAlive() {
        return !IsDead;
    }

    public void startAnimation(int i) {
        animationRequest = i;
        updateRequired = true;
        animationUpdateRequired = true;
    }
}
