import java.util.Optional;

public enum CombatSpell {
    WIND_STRIKE(1152, 1, 711, 1, 4, 91, 92,
            220 /* cast */, 221 /* hit */,
            new int[][]{{556,1},{558,1}}),

    WATER_STRIKE(1154, 5, 711, 1, 6, 94, 95,
            211 /* cast */, 212 /* hit */,
            new int[][]{{556,1},{558,1},{555,1}}),

    EARTH_STRIKE(1156, 9, 711, 2, 8, 97, 98,
            132 /* cast */, 133 /* hit */,
            new int[][]{{556,1},{558,1},{557,2}}),

    FIRE_STRIKE(1158, 13, 711, 2, 10, 100, 101,
            160 /* cast */, 161 /* hit */,
            new int[][]{{556,2},{558,1},{554,3}}),

    WIND_BOLT(1160, 17, 711, 3, 12, 118, 119,
            218 /* cast */, 219 /* hit */,
            new int[][]{{556,2},{562,1}}),

    WATER_BOLT(1163, 23, 711, 3, 14, 121, 122,
            209 /* cast */, 210 /* hit */,
            new int[][]{{556,2},{562,1},{555,2}}),

    EARTH_BOLT(1166, 29, 711, 4, 16, 124, 125,
            130 /* cast */, 131 /* hit */,
            new int[][]{{556,2},{562,1},{557,3}}),

    FIRE_BOLT(1169, 35, 711, 4, 18, 127, 128,
            157 /* cast */, 158 /* hit */,
            new int[][]{{556,3},{562,1},{554,4}}),

    WIND_BLAST(1172, 41, 711, 5, 20, 133, 134,
            216 /* cast */, 217 /* hit */,
            new int[][]{{556,3},{560,1}}),

    WATER_BLAST(1175, 47, 711, 5, 22, 136, 137,
            207 /* cast */, 208 /* hit */,
            new int[][]{{555,3},{556,3},{560,1}}),

    EARTH_BLAST(1177, 53, 711, 6, 24, 139, 140,
            128 /* cast */, 129 /* hit */,
            new int[][]{{555,3},{557,4},{560,1}}),

    FIRE_BLAST(1181, 59, 711, 7, 26, 130, 131,
            155 /* cast */, 156 /* hit */,
            new int[][]{{555,4},{554,5},{560,1}}),

    WIND_WAVE(1183, 62, 711, 7, 28, 159, 160,
            222 /* cast */, 223 /* hit */,
            new int[][]{{556,5},{565,1}}),

    WATER_WAVE(1185, 65, 711, 8, 30, 162, 163,
            213 /* cast */, 214 /* hit */,
            new int[][]{{556,5},{565,1},{555,7}}),

    EARTH_WAVE(1188, 70, 711, 8, 32, 165, 166,
            134 /* cast */, 135 /* hit */,
            new int[][]{{556,5},{565,1},{557,7}}),

    FIRE_WAVE(1189, 75, 711, 9, 34, 130, 157,
            162 /* cast */, 163 /* hit */,
            new int[][]{{556,5},{565,1},{554,7}}),
    // Ancient Magicks
    SMOKE_RUSH(12939, 50, 1978, 15, 5, 384, 385, 171, 108, new int[][]{{560, 2}, {562, 2}, {554, 1}, {556, 1}}),
    SHADOW_RUSH(12987, 52, 1978, 15, 5, 378, 379, 169, 108, new int[][]{{560, 2}, {562, 2}, {556, 1}, {566, 1}}),
    BLOOD_RUSH(12901, 56, 1978, 15, 5, 372, 373, 173, 108, new int[][]{{560, 2}, {562, 2}, {565, 1}}),
    ICE_RUSH(12861, 58, 1978, 15, 5, 360, 361, 165, 108, new int[][]{{560, 2}, {562, 2}, {555, 2}}),

    SMOKE_BURST(12963, 62, 1979, 21, 7, 386, 387, 172, 108, new int[][]{{560, 2}, {562, 2}, {554, 2}, {556, 2}}),
    SHADOW_BURST(13011, 64, 1979, 21, 7, 380, 381, 170, 108, new int[][]{{560, 2}, {562, 2}, {556, 2}, {566, 2}}),
    BLOOD_BURST(12919, 68, 1979, 21, 7, 374, 375, 174, 108, new int[][]{{560, 2}, {562, 2}, {565, 2}}),
    ICE_BURST(12881, 70, 1979, 21, 7, 362, 363, 166, 108, new int[][]{{560, 2}, {562, 2}, {555, 2}}),

    SMOKE_BLITZ(12951, 74, 1978, 27, 8, 388, 389, 175, 108, new int[][]{{560, 2}, {565, 2}, {554, 2}, {556, 2}}),
    SHADOW_BLITZ(12999, 76, 1978, 27, 8, 382, 383, 176, 108, new int[][]{{560, 2}, {565, 2}, {556, 2}, {566, 2}}),
    BLOOD_BLITZ(12911, 80, 1978, 27, 8, 376, 377, 177, 108, new int[][]{{560, 2}, {565, 2}, {565, 2}}),
    ICE_BLITZ(12871, 82, 1978, 27, 8, 364, 365, 178, 108, new int[][]{{560, 2}, {565, 2}, {555, 3}}),

    SMOKE_BARRAGE(12975, 86, 1979, 30, 9, 390, 391, 179, 108, new int[][]{{560, 4}, {565, 2}, {554, 4}, {556, 4}}),
    SHADOW_BARRAGE(13023, 88, 1979, 30, 9, 384, 385, 180, 108, new int[][]{{560, 4}, {565, 2}, {556, 4}, {566, 4}}),
    BLOOD_BARRAGE(12929, 92, 1979, 30, 9, 378, 379, 181, 108, new int[][]{{560, 4}, {565, 4}, {565, 1}}),
    ICE_BARRAGE(12891, 94, 1979, 30, 9, 366, 367, 182, 108, new int[][]{{560, 4}, {565, 2}, {555, 6}});
    public final int spellId;
    public final int levelRequirement;
    public final int animationId;
    public final int baseDamage;
    public final int randomDamage;
    public final int projectileId;
    public final int endGfxId;
    public final int soundId;
    public final int impactSoundId;
    public final int[][] requiredRunes;

    CombatSpell(int spellId, int levelRequirement, int animationId, int baseDamage, int randomDamage,
                int projectileId, int endGfxId, int soundId, int impactSoundId, int[][] requiredRunes) {
        this.spellId = spellId;
        this.levelRequirement = levelRequirement;
        this.animationId = animationId;
        this.baseDamage = baseDamage;
        this.randomDamage = randomDamage;
        this.projectileId = projectileId;
        this.endGfxId = endGfxId;
        this.soundId = soundId;
        this.impactSoundId = impactSoundId;
        this.requiredRunes = requiredRunes;
    }

    public int getMaxHit() {
        return baseDamage + misc.random(randomDamage);
    }

    public static Optional<CombatSpell> forId(int spellId) {
        for (CombatSpell spell : values()) {
            if (spell.spellId == spellId) {
                return Optional.of(spell);
            }
        }
        return Optional.empty();
    }

    public boolean hasRunes(client c) {
        for (int[] rune : requiredRunes) {
            if (!c.playerHasItemAmount(rune[0], rune[1])) {
                return false;
            }
        }
        return true;
    }

    public void deleteRunes(client c) {
        for (int[] rune : requiredRunes) {
            c.deleteItem(rune[0], c.getItemSlot(rune[0]), rune[1]);
        }
    }

    public void cast(client c, int targetX, int targetY, int offsetX, int offsetY, int targetIndex) {
        if (c.playerLevel[6] < levelRequirement) {
            c.sendMessage("You need a magic level of " + levelRequirement + " to cast this spell.");
            return;
        }

        if (!hasRunes(c)) {
            c.sendMessage("You do not have enough runes to cast this spell.");
            return;
        }

        final int[] hit = {getMaxHit()};
        c.hitDiff = hit[0];
        c.inCombat();
        c.setAnimation(animationId);
        c.PkingDelay = 15;

        if (projectileId > 0) {
            c.createProjectile(c.absY, c.absX, offsetX, offsetY, 50, 80, projectileId, 43, 31, -targetIndex);
        }

        if (soundId > 0) {
            c.sendSound(soundId, 7, 0);
        }

        if (endGfxId > 0 || impactSoundId > 0) {
            CycleEventHandler.getSingleton().addEvent(c, new CycleEvent() {
                @Override
                public void execute(CycleEventContainer container) {
                    boolean isBurstOrBarrage = spellId >= 12963 && spellId <= 13023;
                    int aoeRadius = (spellId >= 12975 || spellId == 12891) ? 2 : (spellId >= 12963 && spellId <= 13011) ? 1 : 0;

                    if (endGfxId > 0) {
                        c.stillgfx(endGfxId, targetY, targetX);
                    }

                    if (impactSoundId > 0) {
                        c.sendSound(impactSoundId, 7, 1);
                    }
                    if (isBurstOrBarrage) {
                        for (Player p : PlayerHandler.players) {
                            if (p == null || p == c || p.nonWild()) continue;
                            if (Math.abs(p.absX - targetX) <= aoeRadius && Math.abs(p.absY - targetY) <= aoeRadius) {
                                if(p.IsDead) {
                                    container.stop();
                                    return;
                                }
                                p.hitDiff = hit[0];
                                p.hitUpdateRequired = true;
                                p.playerLevel[3] -= hit[0];
                                c.stillgfx(endGfxId, p.absY, p.absX);
                                if (p.playerLevel[3] <= 0) p.IsDead = true;
                                p.updateRequired = true;
                            }
                        }

                        for (NPC npc : NPCHandler.npcs) {
                            if (npc == null) continue;
                            if (Math.abs(npc.absX - targetX) <= aoeRadius && Math.abs(npc.absY - targetY) <= aoeRadius) {
                               if(npc.IsDead) {
                                   container.stop();
                                   return;
                               }
                                if (npc.HP - npc.hitDiff < 0) {
                                    hit[0] = npc.HP;
                                }
                                c.stillgfx2(endGfxId, npc.absY, npc.absX);
                                npc.StartKilling = c.index;
                                npc.randomWalk = false;
                                npc.IsUnderAttack = true;
                                npc.hitDiff = hit[0];
                                npc.updateRequired = true;
                                npc.hitUpdateRequired = true;
                            }
                        }
                    }

                    switch (spellId) {
                        case 12901: case 12919: case 12911: case 12929:
                            int heal = hit[0] / 4;
                            c.playerLevel[3] = Math.min(c.playerLevel[3] + heal, c.getLevelForXP(c.playerXP[3]));
                            c.refreshSkill(3);
                            break;
                        case 12861: case 12881: case 12871: case 12891:
                            c.freezeTimer = switch (spellId) {
                                case 12861 -> 5;
                                case 12881 -> 10;
                                case 12871 -> 15;
                                case 12891 -> 20;
                                default -> 0;
                            };
                            break;
                        case 12939: case 12963: case 12951: case 12975:
                           // c.getPA().applyPoison(6);
                            break;
                        case 12987: case 13011: case 12999: case 13023:
                          //  c.accuracyDebuffUntil = System.currentTimeMillis() + 5000;
                            break;
                    }
                    container.stop();
                }

                @Override
                public void stop () {

                }
            }, 1200);
        }

        c.addSkillXP(hit[0] * 15, 6);
        deleteRunes(c);
        c.teleportToX = c.absX;
        c.teleportToY = c.absY;
    }
}
