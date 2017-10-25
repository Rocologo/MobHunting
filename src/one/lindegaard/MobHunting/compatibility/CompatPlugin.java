package one.lindegaard.MobHunting.compatibility;

public enum CompatPlugin {
	ActionAnnouncer("ActionAnnouncer", 0), //
	ActionBarApi("ActionBarAPI", 1), //
	Actionbar("Actionbar", 2), //
	BarApi("BarAPI", 3), //
	BattleArena("BattleArena", 4), //
	BossBarApi("BossBarAPI", 5), //
	BossShop("BossShop", 6), //
	Citizens("Citizens", 7), //
	ConquestiaMobs("ConquestiaMobs", 8), //
	CrackShot("CrackShot", 9), //
	CustomMobs("CustomMobs", 10), //
	DisguiseCraft("DisguiseCraft", 11), //
	Essentials("Essentials", 12), //
	ExtraHardMode("ExtraHardMode", 13), //
	Factions("Factions", 14), //
	Gringotts("Gringotts", 15), //
	Herobrine("Herobrine", 16), //
	Heroes("Heroes", 17), //
	iDisguise("iDisguise", 18), //
	InfernalMobs("InfernalMobs", 19), //
	LibsDisguises("LibsDisguises", 20), //
	mcMMO("mcMMO", 21), //
	Minigames("Minigames", 22), //
	MinigamesLib("MinigamesLib", 23), //
	MobArena("MobArena", 24), //
	MobDungeon("MobDungeon", 25), MobStacker("MobStacker", 26), //
	MyPet("MyPet", 27), //
	MysteriousHalloween("MysteriousHalloween", 28), //
	MythicMobs("MythicMobs", 29), //
	PlaceholderAPI("PlaceholderAPI", 30), //
	ProtocolLib("ProtocolLib", 31), //
	PVPArena("PVPArena", 32), //
	Residence("Residence", 33), //
	SmartGiants("SmartGiants", 34), //
	StackMob("StackMob", 35), //
	TARDISWeepingAngels("TARDISWeepingAngels", 36), //
	TitleAPI("TitleAPI", 37), //
	TitleManager("TitleManager", 38), Towny("Towny", 39), //
	VanishNoPacket("VanishNoPacket", 40), //
	War("War", 41), //
	WorldEdit("WorldEdit", 42), //
	WorldGuard("WorldGuard", 43), //
	Holograms("Holograms",44), //
	HolographicDisplays("HolographicDisplays",45), 
	PreciousStones ("PreciousStones",46);

	private final String name;
	private final Integer id;

	private CompatPlugin(String name, Integer id) {
		this.name = name;
		this.id = id;
	}

	public Integer getId() {
		return id;
	}

	public boolean equalsName(String otherName) {
		return (otherName != null) && name.equals(otherName);
	}

	public String toString() {
		return name;
	}

	public CompatPlugin valueOf(int id) {
		return CompatPlugin.values()[id];
	}

	public String getName() {
		return name;
	}

}
