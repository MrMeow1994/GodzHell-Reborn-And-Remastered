import java.util.Map;
import java.util.Optional;

public class PlayerData {
    private String username;
    private String password;
    private String displayName;
    private int heightLevel;
    private int absX;
    private int absY;
    private int rights;
    private int runEnergy;
    private boolean runningToggled;
    private long daysPlayed;
    private long hoursPlayed;
    private long minutesPlayed;
    private double secondsPlayed;
    private int amDonated;
    private boolean hasFirstFloorDone;
    private boolean hasSecondFloorDone;
    private boolean hasThirdFloorDone;
    private boolean hasFourthFloorDone;
    private int skullTimer;
    private int playerIsMember;
    private int playerHasDonated;
    private int jailed;
    private int playerMessages;
    private String playerLastConnect;
    private int playerUID;
    private String macAddress;
    private String uuid;
    private String countryCode;
    private int playerLastLogin;
    private int playerEnergy;
    private int playerGameTime;
    private int playerGameCount;
    private int prestigeLevel;
    private int prestigePoints;
    private int autoRet;
    private Map<Integer, int[]> colorMeta;
    private int Spirit_shard_pack_Amount;

    // Slayer data
    private Optional<SlayerTask> slayerTask = Optional.empty(); // Optional field for SlayerTask

    private int slayerMaster;
    private int slayerTaskAmount;
    private int slayerPoints;
    private boolean slayerRecipe;
    private boolean slayerHelmet;
    private boolean slayerImbuedHelmet;
    private boolean biggerBossTasks;
    private boolean cerberusRoute;
    private boolean superiorSlayer;

    // Arrays
    private int[] playerEquipment;
    private int[] playerEquipmentN;
    private int[] playerAppearance;
    private int[] playerColor;
    private int[] playerLevel;
    private int[] playerXP;
    private int[] playerItems;
    private int[] playerItemsN;
    private int[] bankItems;
    private int[] bankItemsN;
    private int[] bankItems2;
    private int[] bankItemsN2;
    private int[] bankItems3;
    private int[] bankItemsN3;
    private long[] friends;
    private long[] ignores;
    private int consecutiveTasks; // Field for consecutive tasks
    private int loyaltyRank; // Field for loyalty rank
    public int runeMist, gertCat, restGhost,
            romeojuliet, lostCity, vampSlayer, cookAss, doricQuest, blackKnight, shieldArrav,
            sheepShear, impsC, knightS, witchspot, pirateTreasure, desertTreasure;
    public static PlayerData from(Player plr) {
        PlayerData data = new PlayerData();

        data.username = plr.playerName;
        data.password = plr.playerPass;
        data.displayName = plr.displayName;
        data.heightLevel = plr.heightLevel;
        data.absX = plr.absX;
        data.absY = plr.absY;
        data.rights = plr.getRights().getValue();
        data.runEnergy = plr.runEnergy;
        data.runningToggled = plr.runningToggled;
        data.daysPlayed = plr.daysPlayed;
        data.hoursPlayed = plr.hoursPlayed;
        data.minutesPlayed = plr.minutesPlayed;
        data.secondsPlayed = plr.secondsPlayed;
        data.amDonated = plr.amDonated;
        data.hasFirstFloorDone = plr.hasfirstfloorDone;
        data.hasSecondFloorDone = plr.hassecoundfloorDone;
        data.hasThirdFloorDone = plr.hasthirdfloorDone;
        data.hasFourthFloorDone = plr.hasfourthfloorDone;
        data.skullTimer = plr.skullTimer;
        data.runeMist = plr.runeMist;
        data.gertCat = plr.gertCat;
        data.restGhost = plr.restGhost;
        data.romeojuliet = plr.romeojuliet;
        data.lostCity = plr.lostCity;
        data.vampSlayer = plr.vampSlayer;
        data.cookAss = plr.cookAss;
        data.doricQuest = plr.doricQuest;
        data.blackKnight = plr.blackKnight;
        data.shieldArrav = plr.shieldArrav;
        data.sheepShear = plr.sheepShear;
        data.impsC = plr.impsC;
        data.knightS = plr.knightS;
        data.witchspot = plr.witchspot;
        data.pirateTreasure = plr.pirateTreasure;
        data.desertTreasure = plr.desertTreasure;
        data.autoRet = plr.autoRet;
        data.Spirit_shard_pack_Amount = plr.Spirit_shard_pack_Amount;
        client target = (client) plr;
        data.slayerTask = target.getSlayer().getTask();
        data.slayerTaskAmount = target.getSlayer().getTaskAmount();
        data.slayerMaster = target.getSlayer().getMaster();
        data.consecutiveTasks = target.getSlayer().getConsecutiveTasks();

        data.playerIsMember = plr.playerIsMember;
        data.playerHasDonated = plr.playerHasDonated;
        data.jailed = plr.jailed;
        data.playerMessages = plr.playerMessages;
        data.playerLastConnect = plr.playerLastConnect;
        data.playerUID = plr.playerUID;
        data.macAddress = plr.macAddress;
        data.uuid = plr.uuid;
        data.countryCode = plr.countryCode;
        data.playerLastLogin = plr.playerLastLogin;
        data.playerEnergy = plr.playerEnergy;
        data.playerGameTime = plr.playerGameTime;
        data.playerGameCount = plr.playerGameCount;
        data.loyaltyRank = plr.loyaltyRank;
        data.prestigeLevel = plr.prestigeLevel;
        data.prestigePoints = plr.prestigePoints;
        data.playerEquipment = plr.playerEquipment;
        data.playerEquipmentN = plr.playerEquipmentN;
        data.playerAppearance = plr.playerAppearance;
        data.playerColor = plr.playerColor;
        data.playerLevel = plr.playerLevel;
        data.playerXP = plr.playerXP;
        data.playerItems = plr.playerItems;
        data.playerItemsN = plr.playerItemsN;
        data.bankItems = plr.bankItems;
        data.bankItemsN = plr.bankItemsN;
        data.bankItems2 = plr.bankItems2;
        data.bankItemsN2 = plr.bankItemsN2;
        data.bankItems3 = plr.bankItems3;
        data.bankItemsN3 = plr.bankItemsN3;
        data.friends = plr.friends;
        data.ignores = plr.ignores;
        data.colorMeta = plr.getColorManager().getItems();

        return data;
    }
    // Add these methods to the class:
    public int getLoyaltyRank() {
        return loyaltyRank;
    }

    public void setLoyaltyRank(int loyaltyRank) {
        this.loyaltyRank = loyaltyRank;
    }

    // Add these methods to the class:
    public int getConsecutiveTasks() {
        return consecutiveTasks;
    }

    public void setConsecutiveTasks(int consecutiveTasks) {
        this.consecutiveTasks = consecutiveTasks;
    }
    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getHeightLevel() {
        return heightLevel;
    }

    public void setHeightLevel(int heightLevel) {
        this.heightLevel = heightLevel;
    }

    public int getAbsX() {
        return absX;
    }

    public void setAbsX(int absX) {
        this.absX = absX;
    }

    public int getAbsY() {
        return absY;
    }

    public void setAbsY(int absY) {
        this.absY = absY;
    }

    public int getRights() {
        return rights;
    }

    public void setRights(int rights) {
        this.rights = rights;
    }

    public int getRunEnergy() {
        return runEnergy;
    }

    public void setRunEnergy(int runEnergy) {
        this.runEnergy = runEnergy;
    }

    public boolean isRunningToggled() {
        return runningToggled;
    }

    public void setRunningToggled(boolean runningToggled) {
        this.runningToggled = runningToggled;
    }

    public long getDaysPlayed() {
        return daysPlayed;
    }

    public void setDaysPlayed(long daysPlayed) {
        this.daysPlayed = daysPlayed;
    }

    public long getHoursPlayed() {
        return hoursPlayed;
    }

    public void setHoursPlayed(long hoursPlayed) {
        this.hoursPlayed = hoursPlayed;
    }

    public long getMinutesPlayed() {
        return minutesPlayed;
    }

    public void setMinutesPlayed(long minutesPlayed) {
        this.minutesPlayed = minutesPlayed;
    }

    public double getSecondsPlayed() {
        return secondsPlayed;
    }

    public void setSecondsPlayed(double secondsPlayed) {
        this.secondsPlayed = secondsPlayed;
    }

    public int getAmDonated() {
        return amDonated;
    }

    public void setAmDonated(int amDonated) {
        this.amDonated = amDonated;
    }

    public boolean isHasFirstFloorDone() {
        return hasFirstFloorDone;
    }

    public void setHasFirstFloorDone(boolean hasFirstFloorDone) {
        this.hasFirstFloorDone = hasFirstFloorDone;
    }

    public boolean isHasSecondFloorDone() {
        return hasSecondFloorDone;
    }

    public void setHasSecondFloorDone(boolean hasSecondFloorDone) {
        this.hasSecondFloorDone = hasSecondFloorDone;
    }

    public boolean isHasThirdFloorDone() {
        return hasThirdFloorDone;
    }

    public void setHasThirdFloorDone(boolean hasThirdFloorDone) {
        this.hasThirdFloorDone = hasThirdFloorDone;
    }

    public boolean isHasFourthFloorDone() {
        return hasFourthFloorDone;
    }

    public void setHasFourthFloorDone(boolean hasFourthFloorDone) {
        this.hasFourthFloorDone = hasFourthFloorDone;
    }

    public int getSkullTimer() {
        return skullTimer;
    }

    public void setSkullTimer(int skullTimer) {
        this.skullTimer = skullTimer;
    }

    public int getPlayerIsMember() {
        return playerIsMember;
    }

    public void setPlayerIsMember(int playerIsMember) {
        this.playerIsMember = playerIsMember;
    }

    public int getPlayerHasDonated() {
        return playerHasDonated;
    }

    public void setPlayerHasDonated(int playerHasDonated) {
        this.playerHasDonated = playerHasDonated;
    }

    public int getJailed() {
        return jailed;
    }

    public void setJailed(int jailed) {
        this.jailed = jailed;
    }

    public int getPlayerMessages() {
        return playerMessages;
    }

    public void setPlayerMessages(int playerMessages) {
        this.playerMessages = playerMessages;
    }

    public String getPlayerLastConnect() {
        return playerLastConnect;
    }

    public void setPlayerLastConnect(String playerLastConnect) {
        this.playerLastConnect = playerLastConnect;
    }

    public int getPlayerUID() {
        return playerUID;
    }

    public void setPlayerUID(int playerUID) {
        this.playerUID = playerUID;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public int getPlayerLastLogin() {
        return playerLastLogin;
    }

    public void setPlayerLastLogin(int playerLastLogin) {
        this.playerLastLogin = playerLastLogin;
    }

    public int getPlayerEnergy() {
        return playerEnergy;
    }

    public void setPlayerEnergy(int playerEnergy) {
        this.playerEnergy = playerEnergy;
    }

    public int getPlayerGameTime() {
        return playerGameTime;
    }

    public void setPlayerGameTime(int playerGameTime) {
        this.playerGameTime = playerGameTime;
    }

    public int getPlayerGameCount() {
        return playerGameCount;
    }

    public void setPlayerGameCount(int playerGameCount) {
        this.playerGameCount = playerGameCount;
    }

    public int getPrestigeLevel() {
        return prestigeLevel;
    }

    public void setPrestigeLevel(int prestigeLevel) {
        this.prestigeLevel = prestigeLevel;
    }

    // Getter for slayerTask
    public Optional<SlayerTask> getSlayerTask() {
        return slayerTask;
    }

    // Setter for slayerTask
    public void setSlayerTask(Optional<SlayerTask> slayerTask) {
        this.slayerTask = slayerTask;
    }

    // Method to set a non-optional SlayerTask
    public void setSlayerTask(SlayerTask task) {
        this.slayerTask = Optional.ofNullable(task);
    }

    public int getSlayerMaster() {
        return slayerMaster;
    }

    public void setSlayerMaster(int slayerMaster) {
        this.slayerMaster = slayerMaster;
    }

    public int getSlayerTaskAmount() {
        return slayerTaskAmount;
    }

    public void setSlayerTaskAmount(int slayerTaskAmount) {
        this.slayerTaskAmount = slayerTaskAmount;
    }

    public int getSlayerPoints() {
        return slayerPoints;
    }

    public void setSlayerPoints(int slayerPoints) {
        this.slayerPoints = slayerPoints;
    }

    public boolean isSlayerRecipe() {
        return slayerRecipe;
    }

    public void setSlayerRecipe(boolean slayerRecipe) {
        this.slayerRecipe = slayerRecipe;
    }

    public boolean isSlayerHelmet() {
        return slayerHelmet;
    }

    public void setSlayerHelmet(boolean slayerHelmet) {
        this.slayerHelmet = slayerHelmet;
    }

    public boolean isSlayerImbuedHelmet() {
        return slayerImbuedHelmet;
    }

    public void setSlayerImbuedHelmet(boolean slayerImbuedHelmet) {
        this.slayerImbuedHelmet = slayerImbuedHelmet;
    }

    public boolean isBiggerBossTasks() {
        return biggerBossTasks;
    }

    public void setBiggerBossTasks(boolean biggerBossTasks) {
        this.biggerBossTasks = biggerBossTasks;
    }

    public boolean isCerberusRoute() {
        return cerberusRoute;
    }

    public void setCerberusRoute(boolean cerberusRoute) {
        this.cerberusRoute = cerberusRoute;
    }

    public boolean isSuperiorSlayer() {
        return superiorSlayer;
    }

    public void setSuperiorSlayer(boolean superiorSlayer) {
        this.superiorSlayer = superiorSlayer;
    }

    // Array getters and setters
    public int[] getPlayerEquipment() {
        return playerEquipment;
    }

    public void setPlayerEquipment(int[] playerEquipment) {
        this.playerEquipment = playerEquipment;
    }

    public int[] getPlayerEquipmentN() {
        return playerEquipmentN;
    }

    public void setPlayerEquipmentN(int[] playerEquipmentN) {
        this.playerEquipmentN = playerEquipmentN;
    }

    public int[] getPlayerAppearance() {
        return playerAppearance;
    }

    public void setPlayerAppearance(int[] playerAppearance) {
        this.playerAppearance = playerAppearance;
    }

    public int[] getPlayerColor() {
        return playerColor;
    }

    public void setPlayerColor(int[] playerColor) {
        this.playerColor = playerColor;
    }

    public int[] getPlayerLevel() {
        return playerLevel;
    }

    public void setPlayerLevel(int[] playerLevel) {
        this.playerLevel = playerLevel;
    }

    public int[] getPlayerXP() {
        return playerXP;
    }

    public void setPlayerXP(int[] playerXP) {
        this.playerXP = playerXP;
    }

    public int[] getPlayerItems() {
        return playerItems;
    }

    public void setPlayerItems(int[] playerItems) {
        this.playerItems = playerItems;
    }

    public int[] getPlayerItemsN() {
        return playerItemsN;
    }

    public void setPlayerItemsN(int[] playerItemsN) {
        this.playerItemsN = playerItemsN;
    }

    public int[] getBankItems() {
        return bankItems;
    }

    public void setBankItems(int[] bankItems) {
        this.bankItems = bankItems;
    }

    public int[] getBankItemsN() {
        return bankItemsN;
    }

    public void setBankItemsN(int[] bankItemsN) {
        this.bankItemsN = bankItemsN;
    }

    public int[] getBankItems2() {
        return bankItems2;
    }

    public void setBankItems2(int[] bankItems2) {
        this.bankItems2 = bankItems2;
    }

    public int[] getBankItemsN2() {
        return bankItemsN2;
    }

    public void setBankItemsN2(int[] bankItemsN2) {
        this.bankItemsN2 = bankItemsN2;
    }

    public int[] getBankItems3() {
        return bankItems3;
    }

    public void setBankItems3(int[] bankItems3) {
        this.bankItems3 = bankItems3;
    }

    public int[] getBankItemsN3() {
        return bankItemsN3;
    }

    public void setBankItemsN3(int[] bankItemsN3) {
        this.bankItemsN3 = bankItemsN3;
    }

    public long[] getFriends() {
        return friends;
    }

    public void setFriends(long[] friends) {
        this.friends = friends;
    }

    public long[] getIgnores() {
        return ignores;
    }

    public void setIgnores(long[] ignores) {
        this.ignores = ignores;
    }
    public int getRuneMist() {
        return runeMist;
    }
    public void setRuneMist(int runeMist){
        this.runeMist = runeMist;
    }
    public int getGertCat() {
        return gertCat;
    }

    public void setGertCat(int gertCat) {
        this.gertCat = gertCat;
    }

    public int getRestGhost() {
        return restGhost;
    }

    public void setRestGhost(int restGhost) {
        this.restGhost = restGhost;
    }

    public int getRomeojuliet() {
        return romeojuliet;
    }

    public void setRomeojuliet(int romeojuliet) {
        this.romeojuliet = romeojuliet;
    }

    public int getLostCity() {
        return lostCity;
    }

    public void setLostCity(int lostCity) {
        this.lostCity = lostCity;
    }

    public int getVampSlayer() {
        return vampSlayer;
    }

    public void setVampSlayer(int vampSlayer) {
        this.vampSlayer = vampSlayer;
    }

    public int getCookAss() {
        return cookAss;
    }

    public void setCookAss(int cookAss) {
        this.cookAss = cookAss;
    }

    public int getDoricQuest() {
        return doricQuest;
    }

    public void setDoricQuest(int doricQuest) {
        this.doricQuest = doricQuest;
    }

    public int getBlackKnight() {
        return blackKnight;
    }

    public void setBlackKnight(int blackKnight) {
        this.blackKnight = blackKnight;
    }

    public int getShieldArrav() {
        return shieldArrav;
    }

    public void setShieldArrav(int shieldArrav) {
        this.shieldArrav = shieldArrav;
    }

    public int getSheepShear() {
        return sheepShear;
    }

    public void setSheepShear(int sheepShear) {
        this.sheepShear = sheepShear;
    }

    public int getImpsC() {
        return impsC;
    }

    public void setImpsC(int impsC) {
        this.impsC = impsC;
    }

    public int getKnightS() {
        return knightS;
    }

    public void setKnightS(int knightS) {
        this.knightS = knightS;
    }

    public int getWitchspot() {
        return witchspot;
    }

    public void setWitchspot(int witchspot) {
        this.witchspot = witchspot;
    }

    public int getPirateTreasure() {
        return pirateTreasure;
    }

    public void setPirateTreasure(int pirateTreasure) {
        this.pirateTreasure = pirateTreasure;
    }

    public int getDesertTreasure() {
        return desertTreasure;
    }

    public void setDesertTreasure(int desertTreasure) {
        this.desertTreasure = desertTreasure;
    }
public int getAutoRet(){
        return autoRet;
}
public void setAutoRet(int autoRet){
        this.autoRet = autoRet;
}
    public Map<Integer, int[]> getColorMeta() {
        return colorMeta;
    }

    public void setColorMeta(Map<Integer, int[]> colorMeta) {
        this.colorMeta = colorMeta;
    }
public int getPrestigePoints(){
        return prestigePoints;
}
public void setPrestigePoints(int prestigePoints){
        this.prestigePoints = prestigePoints;
}
public int getSpirit_shard_pack_Amount(){
        return Spirit_shard_pack_Amount;
}
public void setSpirit_shard_pack_Amount(int Spirit_shard_pack_Amount){
        this.Spirit_shard_pack_Amount = Spirit_shard_pack_Amount;
}
}



