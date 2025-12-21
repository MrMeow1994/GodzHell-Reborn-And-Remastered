public class TextHandler {

    /*
     *  (C) ??
     */

    public TextHandler() {// Nothing needs to be put in here
    }

    public void process(int a) {

        client p = (client) server.playerHandler.players[a];

        // ---Start of prayer list---
        p.getPA().sendQuest("@whi@ Prayer", 687);
        p.getPA().sendQuest("@whi@", 2437);
        p.getPA().sendQuest("@gre@", 2438);
        p.getPA().sendQuest("@whi@", 2439);
        p.getPA().sendQuest("@whi@Attack Style :", 2427);
        p.getPA().sendQuest("@gre@Weapon: ", 2425);
        p.getPA().sendQuest("@gre@", 2440);
        p.getPA().sendQuest("@whi@Attack", 2441);
        p.getPA().sendQuest("@whi@Accurate", 2445);
        p.getPA().sendQuest("@whi@Defence", 2442);
        p.getPA().sendQuest("@gre@Strength", 2443);
        p.getPA().sendQuest("", 2444);
        p.getPA().sendQuest("", 2446);
        p.getPA().sendQuest("", 2447);
        p.getPA().sendQuest("", 2448);
        p.getPA().sendQuest("@whi@U T Z", 1084);
        p.getPA().sendQuest("@gre@Smith Like a Beast!", 1117);

        p.getPA().sendQuest("@whi@Yes", 12466);
        p.getPA().sendQuest("@whi@No", 12467);

        p.getPA().sendQuest("@gre@Mouse Buttons", 918);
        p.getPA().sendQuest("@whi@Dark", 919);
        p.getPA().sendQuest("@whi@Normal", 920);
        p.getPA().sendQuest("@whi@Bright", 921);
        p.getPA().sendQuest("@whi@V-Bright", 922);

        p.getPA().sendQuest("@gre@Mouse Buttons", 923);
        p.getPA().sendQuest("@whi@One", 925);
        p.getPA().sendQuest("@whi@Two", 924);
        p.getPA().sendQuest("@gre@Chat Effects", 926);
        p.getPA().sendQuest("@whi@On", 928);
        p.getPA().sendQuest("@whi@Off", 927);

        p.getPA().sendQuest("@whi@No", 960);
        p.getPA().sendQuest("@whi@Yes", 959);
        p.getPA().sendQuest("@gre@Split Chat", 956);
        p.getPA().sendQuest("@gre@Accept Aid",  12463);
        p.getPA().sendQuest("@gre@Music\\n@gre@Volume",  929);
        p.getPA().sendQuest("@gre@Effect\\n@gre@Volume",  940);
        p.getPA().sendQuest("@whi@Off", 935);
        p.getPA().sendQuest("@whi@Off", 946);
        p.getPA().sendQuest("@whi@1", 936);
        p.getPA().sendQuest("@whi@1", 947);
        p.getPA().sendQuest("@whi@2", 937);
        p.getPA().sendQuest("@whi@2", 948);
        p.getPA().sendQuest("@whi@3", 938);
        p.getPA().sendQuest("@whi@3", 949);
        p.getPA().sendQuest("@whi@4", 939);
        p.getPA().sendQuest("@whi@4", 950);
        p.getPA().sendQuest("      @whi@Teleports", 174);
        p.getPA().sendQuest("Combat Lvl: "+p.combat, 19758);
        p.getPA().sendQuest("Combat Lvl: "+p.combat, 19906);
        p.getPA().sendQuest("Combat Lvl: "+p.combat, 20050);
        p.getPA().sendQuest("Combat Lvl: "+p.combat, 19797);
        p.getPA().sendQuest("Combat Lvl: "+p.combat, 20702);
        p.getPA().sendQuest("Combat Lvl: "+p.combat, 19834);
        p.getPA().sendQuest("Combat Lvl: "+p.combat, 20013);
        p.getPA().sendQuest("Combat Lvl: "+p.combat, 19978);
        p.getPA().sendQuest("Combat Lvl: "+p.combat, 19978);
        p.getPA().sendQuest(" Change your character looks!", 3649);
        p.getPA().sendQuest("Rearrange mode:", 5390);
        p.getPA().sendQuest("Withdraw as -", 5388);
        p.getPA().sendQuest("Swap", 8133);
        p.getPA().sendQuest("Insert", 8132);
        p.getPA().sendQuest("Item", 5389);
        p.getPA().sendQuest("Note", 5391);

        // Range
        p.getPA().sendQuest("", 4450);
        p.getPA().sendQuest("Accurate!", 1776);
        p.getPA().sendQuest("Rapid!", 1777);
        p.getPA().sendQuest("Long Range", 1778);
        p.getPA().sendQuest("Attacking Style:", 1768);

        // staff
        p.getPA().sendQuest("@gre@Attacking Style:", 332);
        p.getPA().sendQuest("@gre@Crush", 340);
        p.getPA().sendQuest("@whi@Crunch!", 341);
        p.getPA().sendQuest("@gre@Block that!", 342);
        p.getPA().sendQuest("@whi@attack with", 351);
        p.getPA().sendQuest("iN0n3!", 352);
        p.getPA().sendQuest("Ch00s3", 354);
        p.getPA().sendQuest("Sp3ll", 355);
        p.getPA().sendQuest("", 343);
        p.getPA().sendQuest("", 344);
        p.getPA().sendQuest("", 345);
        p.getPA().sendQuest("", 346);
        p.getPA().sendQuest("", 347);
        p.getPA().sendQuest("", 348);
        p.getPA().sendQuest("Please enter your PIN using the buttons below.", 14920);
        p.getPA().sendQuest("First click the FIRST digit", 15313);
        p.getPA().sendQuest("0", 14883);
        p.getPA().sendQuest("1", 14884);
        p.getPA().sendQuest("2", 14885);
        p.getPA().sendQuest("3", 14886);
        p.getPA().sendQuest("4", 14887);
        p.getPA().sendQuest("5", 14888);
        p.getPA().sendQuest("6", 14889);
        p.getPA().sendQuest("7", 14890);
        p.getPA().sendQuest("8", 14891);
        p.getPA().sendQuest("9", 14892);
        p.getPA().sendQuest("I don't know it.", 14921);
        p.getPA().sendQuest("Exit", 14922);
        p.getPA().sendQuest("", 15075);
        p.getPA().sendQuest("", 15076);
        p.getPA().sendQuest("", 15176);
        p.getPA().sendQuest("", 15171);
        p.getPA().sendQuest("", 15079);
        p.getPA().sendQuest("", 15080);
        p.getPA().sendQuest("The Bank of Godzhell Reborn - Bank Pin", 14923);
        p.getPA().sendQuest("", 15107);
        p.getPA().sendQuest("Set Pin", 15078);
        p.getPA().sendQuest("Delete Pin", 15082);
        p.getPA().sendQuest("", 15107);
        p.getPA().sendQuest("Set Pin", 15078);

        // IDK
        p.getPA().sendQuest("@gre@Attacking Style:", 2427);
        p.getPA().sendQuest("@gre@Stab!", 2439);
        p.getPA().sendQuest("@whi@Smash!", 2438);
        p.getPA().sendQuest("@gre@Chop it up!", 2437);
        p.getPA().sendQuest("@whi@Blockit up!", 2440);
        p.getPA().sendQuest("", 2441);
        p.getPA().sendQuest("", 2442);
        p.getPA().sendQuest("", 2443);
        p.getPA().sendQuest("", 2444);
        p.getPA().sendQuest("", 2445);
        p.getPA().sendQuest("", 2446);
        p.getPA().sendQuest("", 2447);
        p.getPA().sendQuest("", 2448);

        // ----Friends & Ignores----
        p.getPA().sendQuest("@whi@Friends List", 5067);
        p.getPA().sendQuest("@whi@Ignore List", 5717);
        p.getPA().sendQuest("@whi@Add Friend", 5070);
        p.getPA().sendQuest("@whi@Del Friend", 5071);
        p.getPA().sendQuest("@whi@Add Name", 5720);
        p.getPA().sendQuest("@whi@Del Name", 5721);

        // ----Shop----
        p.getPA().sendQuest(
                "@whi@Right click to buy, Choose ammount you want, Click item for the price.",
                3903);

        // ----Bonuses----
        p.getPA().sendQuest("@gre@Attack bonus", 1673);
        p.getPA().sendQuest("@gre@Defence bonus", 1674);
        p.getPA().sendQuest("@gre@Other bonuses", 1685);

        // Hands
        p.getPA().sendQuest("@gre@Choose Attack Style", 5858);
        p.getPA().sendQuest("@whi@Punch!", 5866);
        p.getPA().sendQuest("@gre@Kick", 5867);
        p.getPA().sendQuest("@whi@Block", 5868);
        p.getPA().sendQuest("", 5869);
        p.getPA().sendQuest("", 5870);
        p.getPA().sendQuest("", 5871);
        p.getPA().sendQuest("", 5872);
        p.getPA().sendQuest("", 5873);
        p.getPA().sendQuest("", 5874);
        p.getPA().sendQuest("Weapon:", 5856);
        p.getPA().sendQuest("Spec", 7761);

        // barrows?
        p.getPA().sendQuest("@gre@Choose Attack Style", 5858);
        p.getPA().sendQuest("@whi@Punch!", 5866);
        p.getPA().sendQuest("@gre@Kick", 5867);
        p.getPA().sendQuest("@whi@Block", 5868);
        p.getPA().sendQuest("", 5869);
        p.getPA().sendQuest("", 5870);
        p.getPA().sendQuest("", 5871);
        p.getPA().sendQuest("", 5872);
        p.getPA().sendQuest("", 5873);
        p.getPA().sendQuest("", 5874);
        p.getPA().sendQuest("Weapon:", 5856);
        p.getPA().sendQuest("Special Attack", 7761);

        // whip ****
        p.getPA().sendQuest("@gre@Choose Attack Style", 12294);
        p.getPA().sendQuest("@whi@Flick", 12302);
        p.getPA().sendQuest("@gre@Lash", 12303);
        p.getPA().sendQuest("@whi@Deflect", 12304);
        p.getPA().sendQuest("", 12305);
        p.getPA().sendQuest("", 12306);
        p.getPA().sendQuest("", 12307);
        p.getPA().sendQuest("", 12308);
        p.getPA().sendQuest("", 12309);
        p.getPA().sendQuest("", 12310);
        p.getPA().sendQuest("Weapon:", 12292);

        p.getPA().sendFrame126("", 180);
        p.getPA().sendFrame126("@gre@Train", 181);
        p.getPA().sendFrame126("@bla@Cape", 178);
        p.getPA().sendFrame126("@gre@Skillz", 175);
        p.getPA().sendFrame126("@red@Edge", 177);
        p.getPA().sendFrame126("@gre@Fish", 185);
        p.getPA().sendFrame126("@yel@Hang", 186);
        p.getPA().sendFrame126("@gre@Barrw", 173);
        p.getPA().sendFrame126("@gre@Shops", 179);
        p.getPA().sendFrame126("@red@WC", 187);
        p.getPA().sendFrame126("@gre@Naay", 176);
        p.getPA().sendFrame126("@gre@Mining", 13371);
        p.getPA().sendFrame126("@gre@Kbd", 13372);
        p.getPA().sendFrame126("@gre@Ports", 13373);
        p.getPA().sendFrame126("@gre@dag", 13374);
        p.getPA().sendFrame126("@gre@Pk", 13376);
        p.getPA().sendFrame126("@gre@Dance", 13378);
        p.getPA().sendFrame126("@gre@Phat", 13380);
        p.getPA().sendFrame126("@red@GwD", 13381);
        p.getPA().sendFrame126("", 13382);
        p.getPA().sendFrame126("", 11102);
        p.getPA().sendFrame126("", 13379);
        p.getPA().sendFrame126("", 13377);
        p.getPA().sendFrame126("", 13375);
        p.getPA().sendFrame126("", 11103);
        p.getPA().sendFrame126("@red@Walk", 160);
        p.getPA().sendFrame126("@gre@Run", 159);
        p.getPA().sendFrame126("@gre@Ene@yel@rgy @red@Le@bla@ft:", 148);
        p.getPA().sendFrame126(" @gre@Move Speed", 158);
        p.getPA().sendFrame126("    @red@PKeR", 155);
        p.getPA().sendFrame126("@gre@Thiev", 182);
        p.getPA().sendFrame126("@whi@Closewindow", 3902);
        p.getPA().sendFrame126("@gre@On", 157);
        p.getPA().sendFrame126("@red@Off", 156);

        // --Ancients--
        p.getPA().sendQuest("@gre@Level 50 : Smoke Rush", 12941);
        p.getPA().sendQuest("@whi@A single target smoke attack", 12942);
        p.getPA().sendQuest("@gre@Level 52 : Shadow Rush", 12989);
        p.getPA().sendQuest("@whi@A single target shadow attack", 12990);
        p.getPA().sendQuest("@gre@Teleporting Spell", 13037);
        p.getPA().sendQuest("@whi@", 13038);
        p.getPA().sendQuest("@gre@Level 56 : Blood Rush", 12903);
        p.getPA().sendQuest("@whi@A single target blood attack", 12904);
        p.getPA().sendQuest("@gre@Level 58 : Ice Rush", 12863);
        p.getPA().sendQuest("@whi@A single target ice attack", 12864);
        p.getPA().sendQuest("@gre@Teleporting Spell", 13047);
        p.getPA().sendQuest("@whi@", 13048);
        p.getPA().sendQuest("@gre@Level 62 : Smoke Burst", 12965);
        p.getPA().sendQuest("@whi@A multi-target smoke attack", 12966);
        p.getPA().sendQuest("@gre@Level 64 : Shadow Burst", 13013);
        p.getPA().sendQuest("@whi@A multi-target shadow attack", 13014);
        p.getPA().sendQuest("@gre@Teleporting Spell", 13055);
        p.getPA().sendQuest("@whi@", 13056);
        p.getPA().sendQuest("@gre@Level 68 : Blood Burst", 12921);
        p.getPA().sendQuest("@whi@A multi-target blood attack", 12922);
        p.getPA().sendQuest("@gre@Level 70 : Ice Burst", 12883);
        p.getPA().sendQuest("@whi@A multi-target ice attack", 12884);
        p.getPA().sendQuest("@gre@Level 72 : Lassar Teleport", 13063);
        p.getPA().sendQuest("@whi@Ice mountain", 13064);
        p.getPA().sendQuest("@gre@Level 74 : Smoke Blitz", 12953);
        p.getPA().sendQuest("@whi@A single target strong smoke attack", 12954);
        p.getPA().sendQuest("@gre@Level 76 : Shadow Blitz", 13001);
        p.getPA().sendQuest("@whi@A single target strong shadow attack", 13002);
        p.getPA().sendQuest("@gre@Teleporting Spell", 13071);
        p.getPA().sendQuest("@whi@", 13072);
        p.getPA().sendQuest("@gre@Level 80 : Blood Blitz", 12913);
        p.getPA().sendQuest("@whi@A single target strong blood attack", 12914);
        p.getPA().sendQuest("@gre@Level 82 : Ice Blitz", 12873);
        p.getPA().sendQuest("@whi@A single target strong ice attack", 12874);
        p.getPA().sendQuest("@gre@Teleporting Spell", 13081);
        p.getPA().sendQuest("@whi@", 13082);
        p.getPA().sendQuest("@gre@Level 86 : Smoke Barrage", 12977);
        p.getPA().sendQuest("@whi@A multi-target strong smoke attack", 12978);
        p.getPA().sendQuest("@gre@Level 88 : Shadow Barrage", 13025);
        p.getPA().sendQuest("@whi@A multi-target strong shadow attack", 13026);
        p.getPA().sendQuest("@gre@Teleporting Spell", 13089);
        p.getPA().sendQuest("@whi@", 13090);
        p.getPA().sendQuest("@gre@Level 92 : Blood Barrage", 12931);
        p.getPA().sendQuest("@whi@A multi-target strong blood attack", 12932);
        p.getPA().sendQuest("@gre@Level 94 : Ice Barrage", 12893);
        p.getPA().sendQuest("@whi@A multi-target strong ice attack", 12894);
        p.getPA().sendQuest("@gre@Teleporting Spell", 13097);
        p.getPA().sendQuest("@whi@", 13098);


//godwars
        p.getPA().sendQuest("                          0", 16216);
        p.getPA().sendQuest("                          0", 16217);
        p.getPA().sendQuest("                          0", 16218);
        p.getPA().sendQuest("                          0", 16219);
        p.getPA().sendQuest("                  NPC killcount", 16211);
        p.getPA().sendQuest("                  Armadyl kills:", 16212);
        p.getPA().sendQuest("                   Bandos kills:", 16213);
        p.getPA().sendQuest("                Saradomin kills:", 16214);
        p.getPA().sendQuest("                  Zamorak kills:", 16215);
    }
}
