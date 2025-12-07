public class soundConfig {
    public static int getPlayerBlockSounds(client c) {

        return 511;
    }
    public static int getWeaponSounds(client c) {
        if (c.playerEquipment[c.playerWeapon] <= 0) {
            return 2566;
        }
        if (c.playerEquipment[c.playerWeapon] >= 1) {
            String wep = Item.getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase();


            if (c.playerEquipment[c.playerWeapon] == 4718) {// Dharok's
                // Greataxe
                return 1320;
            }
            if (c.playerEquipment[c.playerWeapon] == 4734) {// Karil's Crossbow
                return 1081;
            }
            if (c.playerEquipment[c.playerWeapon] == 4747) {// Torag's Hammers
                return 1330;
            }
            if (c.playerEquipment[c.playerWeapon] == 4710) {// Ahrim's Staff
                return 2555;
            }
            if (c.playerEquipment[c.playerWeapon] == 4755) {// Verac's Flail
                return 1323;
            }
            if (c.playerEquipment[c.playerWeapon] == 4726) {// Guthan's
                // Warspear
                return 2562;
            }

            if (c.playerEquipment[c.playerWeapon] == 772
                    || c.playerEquipment[c.playerWeapon] == 1379
                    || c.playerEquipment[c.playerWeapon] == 1381
                    || c.playerEquipment[c.playerWeapon] == 1383
                    || c.playerEquipment[c.playerWeapon] == 1385
                    || c.playerEquipment[c.playerWeapon] == 1387
                    || c.playerEquipment[c.playerWeapon] == 1389
                    || c.playerEquipment[c.playerWeapon] == 1391
                    || c.playerEquipment[c.playerWeapon] == 1393
                    || c.playerEquipment[c.playerWeapon] == 1395
                    || c.playerEquipment[c.playerWeapon] == 1397
                    || c.playerEquipment[c.playerWeapon] == 1399
                    || c.playerEquipment[c.playerWeapon] == 1401
                    || c.playerEquipment[c.playerWeapon] == 1403
                    || c.playerEquipment[c.playerWeapon] == 1405
                    || c.playerEquipment[c.playerWeapon] == 1407
                    || c.playerEquipment[c.playerWeapon] == 1409
                    || c.playerEquipment[c.playerWeapon] == 9100) { // Staff
                // wack
                return 394;
            }
            if (c.playerEquipment[c.playerWeapon] == 839
                    || c.playerEquipment[c.playerWeapon] == 841
                    || c.playerEquipment[c.playerWeapon] == 843
                    || c.playerEquipment[c.playerWeapon] == 845
                    || c.playerEquipment[c.playerWeapon] == 847
                    || c.playerEquipment[c.playerWeapon] == 849
                    || c.playerEquipment[c.playerWeapon] == 851
                    || c.playerEquipment[c.playerWeapon] == 853
                    || c.playerEquipment[c.playerWeapon] == 855
                    || c.playerEquipment[c.playerWeapon] == 857
                    || c.playerEquipment[c.playerWeapon] == 859
                    || c.playerEquipment[c.playerWeapon] == 861
                    || c.playerEquipment[c.playerWeapon] == 4734
                    || c.playerEquipment[c.playerWeapon] == 2023 // RuneC'Bow
                    || c.playerEquipment[c.playerWeapon] == 4212
                    || c.playerEquipment[c.playerWeapon] == 4214
                    || c.playerEquipment[c.playerWeapon] == 4934
                    || c.playerEquipment[c.playerWeapon] == 9104
                    || c.playerEquipment[c.playerWeapon] == 9107) { // Bows/Crossbows
                return 370;
            }
            if (c.playerEquipment[c.playerWeapon] == 1363
                    || c.playerEquipment[c.playerWeapon] == 1365
                    || c.playerEquipment[c.playerWeapon] == 1367
                    || c.playerEquipment[c.playerWeapon] == 1369
                    || c.playerEquipment[c.playerWeapon] == 1371
                    || c.playerEquipment[c.playerWeapon] == 1373
                    || c.playerEquipment[c.playerWeapon] == 1375
                    || c.playerEquipment[c.playerWeapon] == 1377
                    || c.playerEquipment[c.playerWeapon] == 1349
                    || c.playerEquipment[c.playerWeapon] == 1351
                    || c.playerEquipment[c.playerWeapon] == 1353
                    || c.playerEquipment[c.playerWeapon] == 1355
                    || c.playerEquipment[c.playerWeapon] == 1357
                    || c.playerEquipment[c.playerWeapon] == 1359
                    || c.playerEquipment[c.playerWeapon] == 1361
                    || c.playerEquipment[c.playerWeapon] == 9109) { // BattleAxes/Axes
                return 399;
            }
            if (c.playerEquipment[c.playerWeapon] == 4718
                    || c.playerEquipment[c.playerWeapon] == 7808) { // Dharok
                // GreatAxe
                return 1320;
            }
            if (c.playerEquipment[c.playerWeapon] == 6609
                    || c.playerEquipment[c.playerWeapon] == 1307
                    || c.playerEquipment[c.playerWeapon] == 1309
                    || c.playerEquipment[c.playerWeapon] == 1311
                    || c.playerEquipment[c.playerWeapon] == 1313
                    || c.playerEquipment[c.playerWeapon] == 1315
                    || c.playerEquipment[c.playerWeapon] == 1317
                    || c.playerEquipment[c.playerWeapon] == 1319) { // 2h
                return 2502;
            }
            if (wep.contains("scimitar") || wep.contains("longsword")) {
                return 2503;
            }
            if (wep.contains("halberd")) {
                return 2524;
            }
            if (wep.contains("long")) {
                return 396;
            }
            if (wep.contains("knife")) {
                return 368;
            }
            if (wep.contains("javelin")) {
                return 364;
            }
            if (c.playerEquipment[c.playerWeapon] == 1205 //Daggers
                    || c.playerEquipment[c.playerWeapon] == 1203
                    || c.playerEquipment[c.playerWeapon] == 1207
                    || c.playerEquipment[c.playerWeapon] == 1209
                    || c.playerEquipment[c.playerWeapon] == 1211
                    || c.playerEquipment[c.playerWeapon] == 1213) {
                if (c.fightMode == 0)
                    return 403;
                else
                    return 396;
            }

            if (c.playerEquipment[c.playerWeapon] == 1215) //Dragon Dagger
                return 403;

            if (c.playerEquipment[c.playerWeapon] == 1277 //Swords
                    || c.playerEquipment[c.playerWeapon] == 1279
                    || c.playerEquipment[c.playerWeapon] == 1281
                    || c.playerEquipment[c.playerWeapon] == 1283
                    || c.playerEquipment[c.playerWeapon] == 1285
                    || c.playerEquipment[c.playerWeapon] == 1287
                    || c.playerEquipment[c.playerWeapon] == 1289) {
                if (c.fightMode == 0)
                    return 2547;
                else
                    return 2548;
            }

            if (c.playerEquipment[c.playerWeapon] == 9110) {
                return 401;
            }
            if (c.playerEquipment[c.playerWeapon] == 4755) {
                return 1059;
            }
            if (c.playerEquipment[c.playerWeapon] == 4153) {
                return 1079;
            }
            if (c.playerEquipment[c.playerWeapon] == 9103) {
                return 385;
            }
            if (c.playerEquipment[c.playerWeapon] == -1) { // fists
                return 417;
            }
            if (c.playerEquipment[c.playerWeapon] == 2745
                    || c.playerEquipment[c.playerWeapon] == 2746
                    || c.playerEquipment[c.playerWeapon] == 2747
                    || c.playerEquipment[c.playerWeapon] == 2748) { // Godswords
                return 390;
            }
            if (c.playerEquipment[c.playerWeapon] == 4151) {
                return 2720;
            }
        }
        return 2566;
    }

    public static int specialSounds(int id) {
        if (id == 4151) {// whip
            return 1081;
        } else if (id == 5698 || id == 1231 || id == 1215 || id == 5680) {// dds
            return 385;
        } else if (id == 1434) {// Mace
            return 387;
        } else if (id == 3204) {// halberd
            return 420;
        } else if (id == 4153) { // gmaul
            return 1082;
        } else if (id == 7158) { // d2h
            return 426;
        } else if (id == 4587) { // dscim
            return 1305;
        } else if (id == 1305) { // D Long
            return 390;
        } else if (id == 861 || id == 11235 || id == 859) { // MSB, Darkbow
            return 386;
        } else if (id == 1377) { // DBAxe
            return 389;
        }
        return -1;
    }
}
