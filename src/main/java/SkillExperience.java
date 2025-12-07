public enum SkillExperience {

    INSTANCE;

    public final int[] EXP_ARRAY = new int[121]; // supports index 0–120
    public final int EXPERIENCE_FOR_99;
    public final int EXPERIENCE_FOR_120;

    SkillExperience() {

        int points = 0;

        EXP_ARRAY[1] = 0; // Level 1 = 0 XP

        for (int lvl = 2; lvl <= 120; lvl++) {

            int delta = (int) Math.floor(
                    lvl + 300.0 * Math.pow(2.0, lvl / 7.0)
            );

            points += delta;
            EXP_ARRAY[lvl] = points / 4;
        }

        EXPERIENCE_FOR_99 = EXP_ARRAY[99];
        EXPERIENCE_FOR_120 = EXP_ARRAY[120];
    }
}
