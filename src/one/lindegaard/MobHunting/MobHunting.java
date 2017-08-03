package one.lindegaard.MobHunting;

import java.io.File;

import one.lindegaard.MobHunting.achievements.*;
import one.lindegaard.MobHunting.bounty.BountyManager;
import one.lindegaard.MobHunting.bounty.WorldGroup;
import one.lindegaard.MobHunting.commands.BountyCommand;
import one.lindegaard.MobHunting.commands.CheckGrindingCommand;
import one.lindegaard.MobHunting.commands.ClearGrindingCommand;
import one.lindegaard.MobHunting.commands.CommandDispatcher;
import one.lindegaard.MobHunting.commands.DatabaseCommand;
import one.lindegaard.MobHunting.commands.DebugCommand;
import one.lindegaard.MobHunting.commands.HappyHourCommand;
import one.lindegaard.MobHunting.commands.HeadCommand;
import one.lindegaard.MobHunting.commands.LeaderboardCommand;
import one.lindegaard.MobHunting.commands.LearnCommand;
import one.lindegaard.MobHunting.commands.MoneyCommand;
import one.lindegaard.MobHunting.commands.AchievementsCommand;
import one.lindegaard.MobHunting.commands.BlacklistAreaCommand;
import one.lindegaard.MobHunting.commands.MuteCommand;
import one.lindegaard.MobHunting.commands.NpcCommand;
import one.lindegaard.MobHunting.commands.RegionCommand;
import one.lindegaard.MobHunting.commands.ReloadCommand;
import one.lindegaard.MobHunting.commands.SelectCommand;
import one.lindegaard.MobHunting.commands.TopCommand;
import one.lindegaard.MobHunting.commands.UpdateCommand;
import one.lindegaard.MobHunting.commands.VersionCommand;
import one.lindegaard.MobHunting.commands.WhitelistAreaCommand;
import one.lindegaard.MobHunting.compatibility.*;
import one.lindegaard.MobHunting.grinding.GrindingManager;
import one.lindegaard.MobHunting.leaderboard.LeaderboardManager;
import one.lindegaard.MobHunting.mobs.ExtendedMobManager;
import one.lindegaard.MobHunting.rewards.BagOfGoldSign;
import one.lindegaard.MobHunting.rewards.CustomItems;
import one.lindegaard.MobHunting.rewards.RewardManager;
import one.lindegaard.MobHunting.storage.DataStoreException;
import one.lindegaard.MobHunting.storage.DataStoreManager;
import one.lindegaard.MobHunting.storage.IDataStore;
import one.lindegaard.MobHunting.storage.MySQLDataStore;
import one.lindegaard.MobHunting.storage.SQLiteDataStore;
import one.lindegaard.MobHunting.update.Updater;
import one.lindegaard.MobHunting.util.Misc;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.JavaPlugin;

import io.chazza.advancementapi.AdvancementManager;

public class MobHunting extends JavaPlugin {

	// Constants
	private final static String pluginName = "mobhunting";

	private static MobHunting instance;

	private  RewardManager mRewardManager;
	private  MobHuntingManager mMobHuntingManager;
	private  FishingManager mFishingManager;
	private  GrindingManager mAreaManager;
	private  LeaderboardManager mLeaderboardManager;
	private  AchievementManager mAchievementManager;
	private  BountyManager mBountyManager;
	private  ParticleManager mParticleManager = new ParticleManager();
	private  MetricsManager mMetricsManager;
	private  PlayerSettingsManager mPlayerSettingsManager;
	private  WorldGroup mWorldGroupManager;
	private  ExtendedMobManager mExtendedMobManager;
	private  IDataStore mStore;
	private  DataStoreManager mStoreManager;
	private  ConfigManager mConfig;
	private  AdvancementManager mAdvancementManager;

	private ProtocolLibCompat mProtocolLibCompat;
	private CustomItems customItems;

	private boolean mInitialized = false;
	private ProtocolLibHelper mProtocolLibHelper;

	@Override
	public void onLoad() {
	}

	@Override
	public void onEnable() {

		instance = this;

		Messages.exportDefaultLanguages(this);

		mConfig = new ConfigManager(new File(getDataFolder(), "config.yml"));



		if (mConfig.loadConfig()) {
			if (mConfig.dropMoneyOnGroundTextColor.equals("&0"))
				mConfig.dropMoneyOnGroundTextColor = "WHITE";
			mConfig.saveConfig();
		} else
			throw new RuntimeException(Messages.getString(pluginName + ".config.fail"));
		if (mConfig.pvpKillCmd.toLowerCase().contains("skullowner")
				&& mConfig.pvpKillCmd.toLowerCase().contains("mobhunt")) {
			Bukkit.getConsoleSender().sendMessage(
					ChatColor.RED + "[Mobhunting]==================WARNING=================================");
			Bukkit.getConsoleSender().sendMessage(
					ChatColor.RED + "Potential error in your config.yml. pvp-kill-cmd contains SkullOwner,");
			Bukkit.getConsoleSender().sendMessage(
					ChatColor.RED + "which indicates that pvp-kill-cmd is outdated. Check the head command");
			Bukkit.getConsoleSender()
					.sendMessage(ChatColor.RED + "or delete the line pvp-kill-cmd, and then reload the plugin. The ");
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "correct syntax to get a player head is:");
			Bukkit.getConsoleSender().sendMessage(
					ChatColor.RED + "\"mobhunt head give {player} {killed_player} {killed_player} 1 silent\"");
			Bukkit.getConsoleSender().sendMessage(
					ChatColor.RED + "[Mobhunting]=========================================================");
		}

		if (isbStatsEnabled())
			Messages.debug("bStat is enabled");
		else {
			Bukkit.getConsoleSender().sendMessage(
					ChatColor.RED + "[Mobhunting]=====================WARNING=============================");
			Bukkit.getConsoleSender()
					.sendMessage(ChatColor.RED + "The statistics collection is disabled. As developer I need the");
			Bukkit.getConsoleSender()
					.sendMessage(ChatColor.RED + "statistics from bStats.org. The statistics is 100% anonymous.");
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "https://bstats.org/plugin/bukkit/MobHunting");
			Bukkit.getConsoleSender().sendMessage(
					ChatColor.RED + "Please enable this in /plugins/bStats/config.yml and get rid of this");
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "message. Loading will continue in 15 sec.");
			Bukkit.getConsoleSender().sendMessage(
					ChatColor.RED + "[Mobhunting]=========================================================");
			long now = System.currentTimeMillis();
			while (System.currentTimeMillis() < now + 15000L) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
				}
			}
		}

		mWorldGroupManager = new WorldGroup();
		mWorldGroupManager.load();

        this.customItems = new CustomItems();
		mRewardManager = new RewardManager(this,mProtocolLibCompat,mProtocolLibHelper,mRewardManager,customItems);
		if (mRewardManager.getEconomy() == null)
			return;



		this.mProtocolLibHelper = new ProtocolLibHelper();
		this.mProtocolLibCompat = new ProtocolLibCompat(mProtocolLibHelper);



		mAreaManager = new GrindingManager(this);

		if (mConfig.databaseType.equalsIgnoreCase("mysql"))
			mStore = new MySQLDataStore();
		else
			mStore = new SQLiteDataStore();

		try {
			mStore.initialize();
		} catch (DataStoreException e) {
			e.printStackTrace();

			try {
				mStore.shutdown();
			} catch (DataStoreException e1) {
				e1.printStackTrace();
			}
			setEnabled(false);
			return;
		}

		Updater.setCurrentJarFile(this.getFile().getName());

		mStoreManager = new DataStoreManager(mStore);

		mPlayerSettingsManager = new PlayerSettingsManager();

		// Handle compatibility stuff
		registerPlugin(EssentialsCompat.class, "Essentials");
		registerPlugin(GringottsCompat.class, "Gringotts");

		// Protection plugins
		registerPlugin(WorldEditCompat.class, "WorldEdit");
		registerPlugin(WorldGuardCompat.class, "WorldGuard");
		registerPlugin(FactionsCompat.class, "Factions");
		registerPlugin(TownyCompat.class, "Towny");
		registerPlugin(ResidenceCompat.class, "Residence");

		// Other plugins
		registerPlugin(McMMOCompat.class, "mcMMO");
		registerPlugin(ProtocolLibCompat.class, "ProtocolLib");
		registerPlugin(MyPetCompat.class, "MyPet");
		registerPlugin(BossShopCompat.class, "BossShop");

		// Minigame plugins
		registerPlugin(MinigamesCompat.class, "Minigames");
		registerPlugin(MinigamesLibCompat.class, "MinigamesLib");
		registerPlugin(MobArenaCompat.class, "MobArena");
		registerPlugin(PVPArenaCompat.class, "PVPArena");
		registerPlugin(BattleArenaCompat.class, "BattleArena");

		// Disguise and Vanish plugins
		registerPlugin(LibsDisguisesCompat.class, "LibsDisguises");
		registerPlugin(DisguiseCraftCompat.class, "DisguiseCraft");
		registerPlugin(IDisguiseCompat.class, "iDisguise");
		registerPlugin(VanishNoPacketCompat.class, "VanishNoPacket");

		// Plugins used for presentation information in the BossBar, ActionBar,
		// Title or Subtitle
		registerPlugin(BossBarAPICompat.class, "BossBarAPI");
		registerPlugin(TitleAPICompat.class, "TitleAPI");
		registerPlugin(BarAPICompat.class, "BarAPI");
		registerPlugin(TitleManagerCompat.class, "TitleManager");
		registerPlugin(ActionbarCompat.class, "Actionbar");
		registerPlugin(ActionBarAPICompat.class, "ActionBarAPI");
		registerPlugin(ActionAnnouncerCompat.class, "ActionAnnouncer");
		registerPlugin(PlaceholderAPICompat.class, "PlaceholderAPI");

		// Plugins where the reward is a multiplier
		registerPlugin(StackMobCompat.class, "StackMob");
		registerPlugin(MobStackerCompat.class, "MobStacker");
		registerPlugin(ConquestiaMobsCompat.class, "ConquestiaMobs");

		// ExtendedMob Plugins where special mobs are created
		registerPlugin(MythicMobsCompat.class, "MythicMobs");
		registerPlugin(TARDISWeepingAngelsCompat.class, "TARDISWeepingAngels");
		registerPlugin(CustomMobsCompat.class, "CustomMobs");
		registerPlugin(MysteriousHalloweenCompat.class, "MysteriousHalloween");
		registerPlugin(CitizensCompat.class, "Citizens");
		registerPlugin(SmartGiantsCompat.class, "SmartGiants");
		registerPlugin(InfernalMobsCompat.class, "InfernalMobs");
		registerPlugin(HerobrineCompat.class, "Herobrine");

		registerPlugin(ExtraHardModeCompat.class, "ExtraHardMode");
		registerPlugin(CrackShotCompat.class, "CrackShot");

		mExtendedMobManager = new ExtendedMobManager();

		// Register commands
		CommandDispatcher cmd = new CommandDispatcher("mobhunt",
				Messages.getString("mobhunting.command.base.description") + getDescription().getVersion());
		getCommand("mobhunt").setExecutor(cmd);
		getCommand("mobhunt").setTabCompleter(cmd);
		cmd.registerCommand(new AchievementsCommand());
		cmd.registerCommand(new BlacklistAreaCommand());
		cmd.registerCommand(new CheckGrindingCommand());
		cmd.registerCommand(new ClearGrindingCommand());
		cmd.registerCommand(new DatabaseCommand());
		cmd.registerCommand(new HeadCommand(this));
		cmd.registerCommand(new LeaderboardCommand(this));
		cmd.registerCommand(new LearnCommand());
		cmd.registerCommand(new MuteCommand());
		if (CompatibilityManager.isPluginLoaded(CitizensCompat.class) && CitizensCompat.isSupported()) {
			cmd.registerCommand(new NpcCommand(this));
		}
		cmd.registerCommand(new ReloadCommand());
		if (WorldGuardCompat.isSupported())
			cmd.registerCommand(new RegionCommand());
		if (CompatibilityManager.isPluginLoaded(WorldEditCompat.class) && WorldEditCompat.isSupported())
			cmd.registerCommand(new SelectCommand());
		cmd.registerCommand(new TopCommand());
		cmd.registerCommand(new WhitelistAreaCommand());
		cmd.registerCommand(new UpdateCommand());
		cmd.registerCommand(new VersionCommand());
		cmd.registerCommand(new DebugCommand());
		if (!mConfig.disablePlayerBounties)
			cmd.registerCommand(new BountyCommand());
		cmd.registerCommand(new HappyHourCommand());
		cmd.registerCommand(new MoneyCommand(mRewardManager));

		mLeaderboardManager = new LeaderboardManager(this);

		mAchievementManager = new AchievementManager();

		mMobHuntingManager = new MobHuntingManager(this);
		if (!mConfig.disableFishingRewards)
			mFishingManager = new FishingManager();

		if (!mConfig.disablePlayerBounties)
			mBountyManager = new BountyManager(this);

		// Check for new MobHuntig updates
		Updater.hourlyUpdateCheck(getServer().getConsoleSender(), mConfig.updateCheck, false);

		if (!getServer().getName().toLowerCase().contains("glowstone")) {
			mMetricsManager = new MetricsManager(this, mProtocolLibCompat);
			mMetricsManager.startMetrics();
		}
		mMetricsManager.startBStatsMetrics();

		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				Messages.injectMissingMobNamesToLangFiles();
			}
		}, 20 * 5);

		// Handle online players when server admin do a /reload or /mh reload
		if (mMobHuntingManager.getOnlinePlayersAmount() > 0) {
			Messages.debug("Reloading %s player settings from the database",
					mMobHuntingManager.getOnlinePlayersAmount());
			for (Player player : mMobHuntingManager.getOnlinePlayers()) {
				mPlayerSettingsManager.load(player);
				mAchievementManager.load(player);
				if (!mConfig.disablePlayerBounties)
					mBountyManager.load(player);
				mMobHuntingManager.setHuntEnabled(player, true);
			}
		}

		if (getConfigManager().dropMoneyOnGroundUseAsCurrency)
			new BagOfGoldSign(mRewardManager);

		Messages.debug("Updating advancements");
		if (!getConfigManager().disableMobHuntingAdvancements && Misc.isMC112OrNewer()) {
			mAdvancementManager = new AdvancementManager();
			mAdvancementManager.getAdvancementsFromAchivements();
		}
		// for (int i = 0; i < 2; i++)
		// Messages.debug("Random uuid = %s", UUID.randomUUID());

		mInitialized = true;

	}

	public  void registerPlugin(@SuppressWarnings("rawtypes") Class c, String pluginName) {
		try {
			CompatibilityManager.register(c, pluginName);
		} catch (Exception e) {
			Bukkit.getServer().getConsoleSender()
					.sendMessage(ChatColor.RED + "[MobHunting][ERROR] MobHunting could not register with [" + pluginName
							+ "] please check if [" + pluginName + "] is compatible with the server ["
							+ Bukkit.getServer().getBukkitVersion() + "]");
			if (getConfigManager().killDebug)
				e.printStackTrace();
		}
	}

	@Override
	public void onDisable() {
		if (!mInitialized)
			return;

		Messages.debug("Shutdown LeaderBoardManager");
		mLeaderboardManager.shutdown();
		Messages.debug("Shutdown AreaManager");
		mAreaManager.saveData();
		getMobHuntingManager().getHuntingModifiers().clear();
		if (!mConfig.disableFishingRewards)
			getFishingManager().getFishingModifiers().clear();

		try {
			Messages.debug("Shutdown StoreManager");
			mStoreManager.shutdown();
			Messages.debug("Shutdown Store");
			mStore.shutdown();
		} catch (DataStoreException e) {
			e.printStackTrace();
		}
		Messages.debug("Shutdown CitizensCompat");
		CitizensCompat.shutdown();
		Messages.debug("Shutdown WorldGroupManager");
		mWorldGroupManager.save();
		Messages.debug("MobHunting disabled.");
	}

	private boolean isbStatsEnabled() {
		File bStatsFolder = new File(instance.getDataFolder().getParentFile(), "bStats");
		File configFile = new File(bStatsFolder, "config.yml");
		YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
		return config.getBoolean("enabled", true);
	}

	// ************************************************************************************
	// Managers and handlers
	// ************************************************************************************
	public static MobHunting getInstance() {
		return instance;
	}

	public  ConfigManager getConfigManager() {
		return mConfig;
	}

	/**
	 * Gets the MobHuntingHandler
	 * 
	 * @return MobHuntingManager
	 */
	public  MobHuntingManager getMobHuntingManager() {
		return mMobHuntingManager;
	}

	/**
	 * Get all Achievements for all players.
	 * 
	 * @return
	 */
	public  AchievementManager getAchievementManager() {
		return mAchievementManager;
	}

	/**
	 * Gets the Store Manager
	 * 
	 * @return
	 */
	public  IDataStore getStoreManager() {
		return mStore;
	}

	/**
	 * Gets the Database Store Manager
	 * 
	 * @return
	 */
	public  DataStoreManager getDataStoreManager() {
		return mStoreManager;
	}

	/**
	 * Gets the LeaderboardManager
	 * 
	 * @return
	 */
	public  LeaderboardManager getLeaderboardManager() {
		return mLeaderboardManager;
	}

	/**
	 * Get the BountyManager
	 * 
	 * @return
	 */
	public  BountyManager getBountyManager() {
		return mBountyManager;
	}

	/**
	 * Get the AreaManager
	 * 
	 * @return
	 */
	public  GrindingManager getGrindingManager() {
		return mAreaManager;
	}

	/**
	 * Get all WorldGroups and their worlds
	 * 
	 * @return
	 */
	public  WorldGroup getWorldGroupManager() {
		return mWorldGroupManager;
	}

	/**
	 * Get the PlayerSettingsManager
	 * 
	 * @return
	 */
	public  PlayerSettingsManager getPlayerSettingsmanager() {
		return mPlayerSettingsManager;
	}

	/**
	 * Get the RewardManager
	 * 
	 * @return
	 */
	public  RewardManager getRewardManager() {
		return mRewardManager;
	}

	/**
	 * Get the ParticleManager
	 * 
	 * @return
	 */
	public  ParticleManager getParticleManager() {
		return mParticleManager;
	}

	/**
	 * Get the MobManager
	 * 
	 * @return
	 */
	public  ExtendedMobManager getExtendedMobManager() {
		return mExtendedMobManager;
	}

	/**
	 * Get the FishingManager
	 * 
	 * @return
	 */
	public  FishingManager getFishingManager() {
		return mFishingManager;
	}

	/**
	 * Get the AdvancementManager
	 * 
	 * @return
	 */
	public  AdvancementManager getAdvancementManager() {
		return mAdvancementManager;
	}


	public ProtocolLibHelper getProtocolLibHelper(){return mProtocolLibHelper;}

}
