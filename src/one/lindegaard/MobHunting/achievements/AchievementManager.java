package one.lindegaard.MobHunting.achievements;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.compatibility.CitizensCompat;
import one.lindegaard.MobHunting.compatibility.CustomMobsCompat;
import one.lindegaard.MobHunting.compatibility.InfernalMobsCompat;
import one.lindegaard.MobHunting.compatibility.MysteriousHalloweenCompat;
import one.lindegaard.MobHunting.compatibility.MythicMobsCompat;
import one.lindegaard.MobHunting.compatibility.SmartGiantsCompat;
import one.lindegaard.MobHunting.compatibility.TARDISWeepingAngelsCompat;
import one.lindegaard.MobHunting.mobs.ExtendedMob;
import one.lindegaard.MobHunting.mobs.MinecraftMob;
import one.lindegaard.MobHunting.mobs.MobPlugin;
import one.lindegaard.MobHunting.storage.AchievementStore;
import one.lindegaard.MobHunting.storage.IDataCallback;
import one.lindegaard.MobHunting.storage.UserNotFoundException;
import one.lindegaard.MobHunting.util.Misc;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class AchievementManager implements Listener {

	private MobHunting plugin;
	// String contains ID
	private HashMap<String, Achievement> mAchievements = new HashMap<String, Achievement>();
	private WeakHashMap<UUID, PlayerStorage> mStorage = new WeakHashMap<UUID, PlayerStorage>();

	public AchievementManager(MobHunting plugin) {
		this.plugin = plugin;
		registerAchievements();

		// this is only need when server owner upgrades from very old
		// version of Mobhunting
		if (upgradeAchievements())
			MobHunting.getDataStoreManager().waitForUpdates();

		Bukkit.getPluginManager().registerEvents(this, MobHunting.getInstance());
	}

	public Achievement getAchievement(String id) {
		if (id == null)
			return null;

		if (!mAchievements.containsKey(id.toLowerCase()))
			throw new IllegalArgumentException("There is no achievement by the id: " + id);
		return mAchievements.get(id.toLowerCase());
	}

	private void registerAchievement(Achievement achievement) {
		Validate.notNull(achievement);

		if (achievement instanceof ProgressAchievement) {
			if (((ProgressAchievement) achievement).inheritFrom() != null
					&& ((ProgressAchievement) achievement).getNextLevel() != 0) {
				Validate.isTrue(
						mAchievements.containsKey(((ProgressAchievement) achievement).inheritFrom().toLowerCase()));
				Validate.isTrue(mAchievements.get(((ProgressAchievement) achievement).inheritFrom()
						.toLowerCase()) instanceof ProgressAchievement);
			}
		}

		mAchievements.put(achievement.getID().toLowerCase(), achievement);

		if (achievement instanceof Listener)
			Bukkit.getPluginManager().registerEvents((Listener) achievement, MobHunting.getInstance());
	}

	private void registerAchievements() {
		registerAchievement(new TheHuntBegins(plugin));
		registerAchievement(new AxeMurderer(plugin));
		registerAchievement(new CreeperBoxing(plugin));
		registerAchievement(new Electrifying(plugin));
		registerAchievement(new RecordHungry(plugin));
		registerAchievement(new InFighting(plugin));
		registerAchievement(new ByTheBook(plugin));
		registerAchievement(new Creepercide(plugin));
		registerAchievement(new ItsMagic(plugin));
		registerAchievement(new FancyPants(plugin));
		registerAchievement(new MasterSniper(plugin));
		registerAchievement(new JustInTime(plugin));
		registerAchievement(new WolfKillAchievement(plugin));
		if (SmartGiantsCompat.isSupported())
			registerAchievement(new DavidAndGoliath(plugin));

		for (MinecraftMob type : MinecraftMob.values()) {
			ExtendedMob extendedMob = new ExtendedMob(MobPlugin.Minecraft, type.name());
			registerAchievement(new BasicHuntAchievement(plugin, extendedMob));
			registerAchievement(new SecondHuntAchievement(plugin, extendedMob));
			registerAchievement(new ThirdHuntAchievement(plugin, extendedMob));
			registerAchievement(new FourthHuntAchievement(plugin, extendedMob));
			registerAchievement(new FifthHuntAchievement(plugin, extendedMob));
			registerAchievement(new SixthHuntAchievement(plugin, extendedMob));
			registerAchievement(new SeventhHuntAchievement(plugin, extendedMob));
			registerAchievement(new EighthHuntAchievement(plugin, extendedMob));
		}

		if (MythicMobsCompat.isSupported())
			for (String type : MythicMobsCompat.getMobRewardData().keySet()) {
				ExtendedMob extendedMob = new ExtendedMob(MobPlugin.MythicMobs, type);
				registerAchievement(new BasicHuntAchievement(plugin, extendedMob));
				registerAchievement(new SecondHuntAchievement(plugin, extendedMob));
				registerAchievement(new ThirdHuntAchievement(plugin, extendedMob));
				registerAchievement(new FourthHuntAchievement(plugin, extendedMob));
				registerAchievement(new FifthHuntAchievement(plugin, extendedMob));
				registerAchievement(new SixthHuntAchievement(plugin, extendedMob));
				registerAchievement(new SeventhHuntAchievement(plugin, extendedMob));
				registerAchievement(new EighthHuntAchievement(plugin, extendedMob));
			}

		if (CitizensCompat.isSupported())
			for (String type : CitizensCompat.getMobRewardData().keySet()) {
				ExtendedMob extendedMob = new ExtendedMob(MobPlugin.Citizens, type);
				registerAchievement(new BasicHuntAchievement(plugin, extendedMob));
				registerAchievement(new SecondHuntAchievement(plugin, extendedMob));
				registerAchievement(new ThirdHuntAchievement(plugin, extendedMob));
				registerAchievement(new FourthHuntAchievement(plugin, extendedMob));
				registerAchievement(new FifthHuntAchievement(plugin, extendedMob));
				registerAchievement(new SixthHuntAchievement(plugin, extendedMob));
				registerAchievement(new SeventhHuntAchievement(plugin, extendedMob));
				registerAchievement(new EighthHuntAchievement(plugin, extendedMob));
			}

		if (MysteriousHalloweenCompat.isSupported())
			for (String type : MysteriousHalloweenCompat.getMobRewardData().keySet()) {
				ExtendedMob extendedMob = new ExtendedMob(MobPlugin.MysteriousHalloween, type);
				registerAchievement(new BasicHuntAchievement(plugin, extendedMob));
				registerAchievement(new SecondHuntAchievement(plugin, extendedMob));
				registerAchievement(new ThirdHuntAchievement(plugin, extendedMob));
				registerAchievement(new FourthHuntAchievement(plugin, extendedMob));
				registerAchievement(new FifthHuntAchievement(plugin, extendedMob));
				registerAchievement(new SixthHuntAchievement(plugin, extendedMob));
				registerAchievement(new SeventhHuntAchievement(plugin, extendedMob));
				registerAchievement(new EighthHuntAchievement(plugin, extendedMob));
			}

		if (CustomMobsCompat.isSupported())
			for (String type : CustomMobsCompat.getMobRewardData().keySet()) {
				ExtendedMob extendedMob = new ExtendedMob(MobPlugin.CustomMobs, type);
				registerAchievement(new BasicHuntAchievement(plugin, extendedMob));
				registerAchievement(new SecondHuntAchievement(plugin, extendedMob));
				registerAchievement(new ThirdHuntAchievement(plugin, extendedMob));
				registerAchievement(new FourthHuntAchievement(plugin, extendedMob));
				registerAchievement(new FifthHuntAchievement(plugin, extendedMob));
				registerAchievement(new SixthHuntAchievement(plugin, extendedMob));
				registerAchievement(new SeventhHuntAchievement(plugin, extendedMob));
				registerAchievement(new EighthHuntAchievement(plugin, extendedMob));
			}

		if (TARDISWeepingAngelsCompat.isSupported())
			for (String type : TARDISWeepingAngelsCompat.getMobRewardData().keySet()) {
				ExtendedMob extendedMob = new ExtendedMob(MobPlugin.TARDISWeepingAngels, type);
				registerAchievement(new BasicHuntAchievement(plugin, extendedMob));
				registerAchievement(new SecondHuntAchievement(plugin, extendedMob));
				registerAchievement(new ThirdHuntAchievement(plugin, extendedMob));
				registerAchievement(new FourthHuntAchievement(plugin, extendedMob));
				registerAchievement(new FifthHuntAchievement(plugin, extendedMob));
				registerAchievement(new SixthHuntAchievement(plugin, extendedMob));
				registerAchievement(new SeventhHuntAchievement(plugin, extendedMob));
				registerAchievement(new EighthHuntAchievement(plugin, extendedMob));
			}

		if (SmartGiantsCompat.isSupported()) {
			ExtendedMob extendedMob = new ExtendedMob(MobPlugin.SmartGiants, SmartGiantsCompat.MONSTER_NAME);
			registerAchievement(new BasicHuntAchievement(plugin, extendedMob));
			registerAchievement(new SecondHuntAchievement(plugin, extendedMob));
			registerAchievement(new ThirdHuntAchievement(plugin, extendedMob));
			registerAchievement(new FourthHuntAchievement(plugin, extendedMob));
			registerAchievement(new FifthHuntAchievement(plugin, extendedMob));
			registerAchievement(new SixthHuntAchievement(plugin, extendedMob));
			registerAchievement(new SeventhHuntAchievement(plugin, extendedMob));
			registerAchievement(new EighthHuntAchievement(plugin, extendedMob));
		}

		if (InfernalMobsCompat.isSupported()) {
			for (MinecraftMob type : MinecraftMob.values()) {
				ExtendedMob extendedMob = new ExtendedMob(MobPlugin.InfernalMobs, type.name());
				registerAchievement(new BasicHuntAchievement(plugin, extendedMob));
				registerAchievement(new SecondHuntAchievement(plugin, extendedMob));
				registerAchievement(new ThirdHuntAchievement(plugin, extendedMob));
				registerAchievement(new FourthHuntAchievement(plugin, extendedMob));
				registerAchievement(new FifthHuntAchievement(plugin, extendedMob));
				registerAchievement(new SixthHuntAchievement(plugin, extendedMob));
				registerAchievement(new SeventhHuntAchievement(plugin, extendedMob));
				registerAchievement(new EighthHuntAchievement(plugin, extendedMob));
			}
		}
	}

	public boolean hasAchievement(String achievement, OfflinePlayer player) {
		return hasAchievement(getAchievement(achievement), player);
	}

	public boolean hasAchievement(Achievement achievement, OfflinePlayer player) {
		if (achievement == null)
			return false;
		PlayerStorage storage = mStorage.get(player.getUniqueId());
		if (storage == null)
			return false;

		return storage.gainedAchievements.contains(achievement.getID());
	}

	private boolean achievementsEnabledFor(OfflinePlayer player) {
		PlayerStorage storage = mStorage.get(player.getUniqueId());
		if (storage == null)
			return false;

		return storage.enableAchievements;
	}

	public int getProgress(String achievement, OfflinePlayer player) {
		Achievement a = getAchievement(achievement);
		Validate.isTrue(a instanceof ProgressAchievement, "This achievement does not have progress");

		return getProgress((ProgressAchievement) a, player);
	}

	public int getProgress(ProgressAchievement achievement, OfflinePlayer player) {
		PlayerStorage storage = mStorage.get(player.getUniqueId());
		if (storage == null)
			return 0;

		Integer progress = storage.progressAchievements.get(achievement.getID());

		if (progress == null)
			return (storage.gainedAchievements.contains(achievement.getID()) ? achievement.getNextLevel() : 0);
		return progress;
	}

	public void requestCompletedAchievements(OfflinePlayer player,
			final IDataCallback<List<Map.Entry<Achievement, Integer>>> callback) {
		if (player.isOnline()) {
			List<Map.Entry<Achievement, Integer>> achievements = new ArrayList<Map.Entry<Achievement, Integer>>();
			ArrayList<Map.Entry<Achievement, Integer>> toRemove = new ArrayList<Map.Entry<Achievement, Integer>>();

			for (Achievement achievement : mAchievements.values()) {
				if (hasAchievement(achievement, player.getPlayer())) {
					achievements.add(new AbstractMap.SimpleImmutableEntry<Achievement, Integer>(achievement, -1));

					// If the achievement is a higher level, remove the lower
					// level from the list
					if (achievement instanceof ProgressAchievement
							&& ((ProgressAchievement) achievement).inheritFrom() != null) {
						toRemove.add(new AbstractMap.SimpleImmutableEntry<Achievement, Integer>(
								getAchievement(((ProgressAchievement) achievement).inheritFrom().toLowerCase()), -1));
					}
				} else if (achievement instanceof ProgressAchievement
						&& getProgress((ProgressAchievement) achievement, player.getPlayer()) > 0) {
					achievements.add(new AbstractMap.SimpleImmutableEntry<Achievement, Integer>(achievement,
							getProgress((ProgressAchievement) achievement, player.getPlayer())));
				}
			}

			callback.onCompleted(achievements);
			return;
		}

		// Look through the data store for offline players
		MobHunting.getDataStoreManager().requestAllAchievements(player, new IDataCallback<Set<AchievementStore>>() {
			@Override
			public void onError(Throwable error) {
				callback.onError(error);
			}

			@Override
			public void onCompleted(Set<AchievementStore> data) {
				List<Map.Entry<Achievement, Integer>> achievements = new ArrayList<Map.Entry<Achievement, Integer>>();
				ArrayList<Map.Entry<Achievement, Integer>> toRemove = new ArrayList<Map.Entry<Achievement, Integer>>();

				for (AchievementStore stored : data) {
					if (mAchievements.containsKey(stored.id)) {
						Achievement achievement = mAchievements.get(stored.id);
						achievements.add(new AbstractMap.SimpleImmutableEntry<Achievement, Integer>(achievement,
								stored.progress));
						if (achievement instanceof ProgressAchievement
								&& ((ProgressAchievement) achievement).inheritFrom() != null)
							toRemove.add(new AbstractMap.SimpleImmutableEntry<Achievement, Integer>(
									getAchievement(((ProgressAchievement) achievement).inheritFrom().toLowerCase()),
									-1));
					}
				}

				callback.onCompleted(achievements);
			}
		});
	}

	/**
	 * Get a Collection of all Achievements
	 * 
	 * @return a Collection of achievements.
	 */
	public Collection<Achievement> getAllAchievements() {
		List<Achievement> list = new ArrayList<Achievement>();
		list.addAll(mAchievements.values());
		Comparator<Achievement> comparator = new Comparator<Achievement>() {
			@Override
			public int compare(Achievement left, Achievement right) {
				String id1 = left.getID(), id2 = right.getID();
				if (id1.startsWith("hunting-level") && id2.startsWith("hunting-level")) {
					id1 = id1.substring(12, id1.length());
					id2 = id2.substring(12, id2.length());
					String[] str1 = id1.split("-");
					String[] str2 = id2.split("-");
					return (str1[1] + str1[0]).compareTo(str2[1] + str2[0]);
				} else
					return left.getID().compareTo(right.getID());
			}
		};
		Collections.sort(list, comparator);
		return Collections.unmodifiableCollection(list);
	}

	/**
	 * List all Achievements done by the player / command sender
	 * 
	 * @param sender
	 */
	public void listAllAchievements(CommandSender sender) {
		Iterator<Achievement> itr = Collections.unmodifiableCollection(mAchievements.values()).iterator();
		while (itr.hasNext()) {
			Achievement a = itr.next();
			sender.sendMessage(a.getID() + "---" + a.getName() + "---" + a.getDescription());
		}
	}

	/**
	 * Award the player when he make an Achievement
	 * 
	 * @param achievement
	 * @param player
	 */
	public void awardAchievement(String achievement, Player player, ExtendedMob mob) {
		awardAchievement(getAchievement(achievement), player, mob);
	}

	/**
	 * Award the player if/when he make an Achievement
	 * 
	 * @param achievement
	 * @param player
	 */
	public void awardAchievement(Achievement achievement, Player player, ExtendedMob mob) {
		if (!achievementsEnabledFor(player)) {
			Messages.debug("[AchievementBlocked] Achievements is disabled for player %s", player.getName());
			return;
		}

		if (hasAchievement(achievement, player)) {
			return;
		}

		for (String world : MobHunting.getConfigManager().disableAchievementsInWorlds)
			if (world.equalsIgnoreCase(player.getWorld().getName())) {
				Messages.debug("[AchievementBlocked] Achievements is disabled in world:%s", world);
				return;
			}

		if (!MobHunting.getConfigManager().disableMobHuntingAdvancements && Misc.isMC112OrNewer())
			plugin.getAdvancementManager().grantAdvancement(player, achievement);

		PlayerStorage storage = mStorage.get(player.getUniqueId());
		if (storage == null) {
			storage = new PlayerStorage();
			storage.enableAchievements = true;
		}

		Messages.debug("RecordAchievement: %s achieved.", achievement.getID());
		MobHunting.getDataStoreManager().recordAchievement(player, achievement, mob);
		storage.gainedAchievements.add(achievement.getID());
		mStorage.put(player.getUniqueId(), storage);

		plugin.getMessages().playerSendMessage(player, ChatColor.GOLD + Messages.getString("mobhunting.achievement.awarded", "name",
				"" + ChatColor.WHITE + ChatColor.ITALIC + achievement.getName()));
		plugin.getMessages().playerSendMessage(player, ChatColor.BLUE + "" + ChatColor.ITALIC + achievement.getDescription());
		plugin.getMessages().playerSendMessage(player, 
				ChatColor.WHITE + "" + ChatColor.ITALIC + Messages.getString("mobhunting.achievement.awarded.prize",
						"prize", plugin.getRewardManager().format(achievement.getPrize())));

		plugin.getRewardManager().depositPlayer(player, achievement.getPrize());

		if (MobHunting.getConfigManager().broadcastAchievement
				&& (!(achievement instanceof TheHuntBegins) || MobHunting.getConfigManager().broadcastFirstAchievement))
			plugin.getMessages()
					.broadcast(ChatColor.GOLD + Messages.getString("mobhunting.achievement.awarded.broadcast", "player",
							player.getName(), "name", "" + ChatColor.WHITE + ChatColor.ITALIC + achievement.getName()),
							player);

		// Run console commands as a reward
		String playername = player.getName();
		String worldname = player.getWorld().getName();
		String playerpos = player.getLocation().getBlockX() + " " + player.getLocation().getBlockY() + " "
				+ player.getLocation().getBlockZ();
		String prizeCommand = achievement.getPrizeCmd().replaceAll("\\{player\\}", playername)
				.replaceAll("\\{world\\}", worldname).replaceAll("\\{killerpos\\}", playerpos)
				.replaceAll("\\{monstertype\\}", mob.getMobName());

		if (!achievement.getPrizeCmd().equals("")) {
			Messages.debug("Command to be run:" + prizeCommand);
			String str = prizeCommand;
			do {
				if (str.contains("|")) {
					int n = str.indexOf("|");
					Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), str.substring(0, n));
					str = str.substring(n + 1, str.length()).toString();
				}
			} while (str.contains("|"));
			Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), str);
		}
		if (!achievement.getPrizeCmdDescription().equals("")) {
			plugin.getMessages().playerSendMessage(player, ChatColor.WHITE + "" + ChatColor.ITALIC
					+ achievement.getPrizeCmdDescription().replaceAll("\\{player\\}", playername)
							.replaceAll("\\{world\\}", worldname).replaceAll("\\{monstertype\\}", mob.getMobName()));
		}

		if (Misc.isMC19OrNewer())
			player.getWorld().playSound(player.getLocation(), Sound.valueOf("ENTITY_PLAYER_LEVELUP"), 1.0f, 1.0f);
		else
			player.getWorld().playSound(player.getLocation(), Sound.valueOf("LEVEL_UP"), 1.0f, 1.0f);

		FireworkEffect effect = FireworkEffect.builder().withColor(Color.ORANGE, Color.YELLOW).flicker(true)
				.trail(false).build();
		Firework firework = player.getWorld().spawn(player.getLocation(), Firework.class);
		FireworkMeta meta = firework.getFireworkMeta();
		meta.setPower(1);
		meta.addEffect(effect);
		firework.setFireworkMeta(meta);
	}

	public void awardAchievementProgress(String achievement, Player player, ExtendedMob mob, int amount) {
		Achievement a = getAchievement(achievement);
		Validate.isTrue(a instanceof ProgressAchievement,
				"You need to award normal achievements with awardAchievement()");

		awardAchievementProgress((ProgressAchievement) a, player, mob, amount);
	}

	public void awardAchievementProgress(ProgressAchievement achievement, Player player, ExtendedMob mob, int amount) {
		if (!achievementsEnabledFor(player) || hasAchievement(achievement, player))
			return;

		for (String world : MobHunting.getConfigManager().disableAchievementsInWorlds)
			if (world.equalsIgnoreCase(player.getWorld().getName())) {
				Messages.debug("[AchievementBlocked] Achievements is disabled in world:%s", world);
				return;
			}

		if (achievement.getExtendedMob().getProgressAchievementLevel1() == 0) {
			Messages.debug(
					"[AchievementBlocked] ProgressAchievement for killing a %s is disabled (%s_level1 is 0 in config.yml)",
					achievement.getExtendedMob().getMobtype().toLowerCase(),
					achievement.getExtendedMob().getMobtype().toLowerCase());
			return;
		}

		Validate.isTrue(amount > 0);

		PlayerStorage storage = mStorage.get(player.getUniqueId());
		if (storage == null) {
			storage = new PlayerStorage();
			storage.enableAchievements = true;
		}

		int curProgress = getProgress(achievement, player);

		while (achievement.inheritFrom() != null && curProgress == 0) {
			// This allows us to just mark progress against the highest level
			// version and have it automatically given to the lower level ones
			if (!hasAchievement(achievement.inheritFrom(), player)) {
				achievement = (ProgressAchievement) getAchievement(achievement.inheritFrom().toLowerCase());
				curProgress = getProgress(achievement, player);
			} else {
				curProgress = ((ProgressAchievement) getAchievement(achievement.inheritFrom().toLowerCase()))
						.getNextLevel();
			}
		}

		int maxProgress = achievement.getNextLevel();
		int nextProgress = Math.min(maxProgress, curProgress + amount);

		if (nextProgress == maxProgress && maxProgress != 0)
			awardAchievement(achievement, player, mob);
		else {
			storage.progressAchievements.put(achievement.getID(), nextProgress);

			Messages.debug("RecordAchievement: %s has %s kills", achievement.getID(), nextProgress);
			MobHunting.getDataStoreManager().recordAchievementProgress(player, achievement, nextProgress);

			int segment = Math.min(25, maxProgress / 2);

			if (curProgress / segment < nextProgress / segment || curProgress == 0 && nextProgress > 0) {
				plugin.getMessages().playerSendMessage(player, ChatColor.BLUE + Messages.getString("mobhunting.achievement.progress", "name",
						"" + ChatColor.WHITE + ChatColor.ITALIC + achievement.getName()));
				plugin.getMessages().playerSendMessage(player, ChatColor.GRAY + "" + nextProgress + " / " + maxProgress);
			}
		}
		mStorage.put(player.getUniqueId(), storage);
	}

	@SuppressWarnings({ "unchecked", "deprecation" })
	public boolean upgradeAchievements() {
		File file = new File(MobHunting.getInstance().getDataFolder(), "awards.yml");

		if (!file.exists())
			return false;

		MobHunting.getInstance().getLogger().info("Upgrading old awards.yml file");

		YamlConfiguration config = new YamlConfiguration();
		try {
			config.load(file);

			for (String player : config.getKeys(false)) {
				if (config.isList(player)) {
					for (Object obj : (List<Object>) config.getList(player)) {
						if (obj instanceof String) {
							MobHunting.getInstance();
							MobHunting.getDataStoreManager().recordAchievement(Bukkit.getOfflinePlayer(player),
									getAchievement((String) obj), null);
						} else if (obj instanceof Map) {
							Map<String, Integer> map = (Map<String, Integer>) obj;
							String id = map.keySet().iterator().next();
							MobHunting.getInstance();
							MobHunting.getDataStoreManager().recordAchievementProgress(Bukkit.getOfflinePlayer(player),
									(ProgressAchievement) getAchievement(id), (Integer) map.get(id));
						}
					}
				}
			}

			Files.delete(file.toPath());

			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}

		return false;
	}

	public void load(final Player player) {
		if (!player.hasPermission("mobhunting.achievements.disabled") || player.hasPermission("*")) {

			if (!mStorage.containsKey(player.getUniqueId())) {
				Messages.debug("Loading %s's Achievements", player.getName());

				final PlayerStorage storage = new PlayerStorage();
				storage.enableAchievements = false;

				final Player p = player;
				MobHunting.getDataStoreManager().requestAllAchievements(player,
						new IDataCallback<Set<AchievementStore>>() {
							@Override
							public void onError(Throwable error) {
								if (error instanceof UserNotFoundException)
									storage.enableAchievements = true;
								else {
									error.printStackTrace();
									p.sendMessage(Messages.getString("achievements.load-fail"));
									storage.enableAchievements = false;
								}
							}

							@Override
							public void onCompleted(Set<AchievementStore> data) {
								Messages.debug("Loaded %s Achievements.", data.size());
								for (AchievementStore achievementStore : data) {
									if (achievementStore.progress == -1)
										storage.gainedAchievements.add(achievementStore.id);
									else {
										// Check if there is progress
										// achievements with a wrong status
										Achievement achievement = null;
										try {
											achievement = getAchievement(achievementStore.id);
										} catch (IllegalArgumentException e) {

										}
										if (achievement instanceof ProgressAchievement && achievementStore.progress != 0
												&& achievementStore.progress != ((ProgressAchievement) getAchievement(
														achievementStore.id)).getNextLevel()
												&& ((ProgressAchievement) getAchievement(achievementStore.id))
														.inheritFrom() != null) {
											boolean gained = false;
											for (AchievementStore as : data) {
												if (as.id.equalsIgnoreCase(
														((ProgressAchievement) getAchievement(achievementStore.id))
																.nextLevelId())) {
													Messages.debug(
															"Error in mh_Achievements: %s=%s. Changing status to completed. ",
															achievementStore.id, achievementStore.progress);
													MobHunting.getDataStoreManager().recordAchievementProgress(player,
															(ProgressAchievement) getAchievement(achievementStore.id),
															-1);
													storage.gainedAchievements.add(achievementStore.id);
													gained = true;
													break;
												}
											}
											if (!gained)
												storage.progressAchievements.put(achievementStore.id,
														achievementStore.progress);
										} else {
											storage.progressAchievements.put(achievementStore.id,
													achievementStore.progress);
										}
									}

								}
								storage.enableAchievements = true;
								mStorage.put(p.getUniqueId(), storage);

								if (!MobHunting.getConfigManager().disableMobHuntingAdvancements
										&& Misc.isMC112OrNewer())
									plugin.getAdvancementManager().updatePlayerAdvancements(player);

							}
						});
			} else {
				Messages.debug("Using cached achievements for %s", player.getName());
				PlayerStorage storage = mStorage.get(player.getUniqueId());
				if (!storage.enableAchievements) {
					Messages.debug("Enabling achievements in cache for %s.", player.getName());
					storage.enableAchievements = true;
					mStorage.put(player.getUniqueId(), storage);
				}
			}
		} else {
			Messages.debug("achievements is disabled with permission 'mobhunting.achievements.disabled' for player %s",
					player.getName());
		}

	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onPlayerJoin(final PlayerJoinEvent event) {
		Bukkit.getScheduler().runTaskLater(MobHunting.getInstance(), new Runnable() {

			@Override
			public void run() {
				load(event.getPlayer());
			}
		}, (long) 5);

	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	private void onPlayerQuit(PlayerQuitEvent event) {
		if (mStorage.containsKey(event.getPlayer().getUniqueId()))
			mStorage.remove(event.getPlayer().getUniqueId());
	}

	// *************************************************************************************
	// ACHIEVEMENTS GUI
	// *************************************************************************************
	private static WeakHashMap<CommandSender, Inventory> inventoryMapCompleted = new WeakHashMap<CommandSender, Inventory>();
	private static WeakHashMap<CommandSender, Inventory> inventoryMapOngoing = new WeakHashMap<CommandSender, Inventory>();
	private static WeakHashMap<CommandSender, Inventory> inventoryMapNotStarted = new WeakHashMap<CommandSender, Inventory>();

	public void showAllAchievements(final CommandSender sender, final OfflinePlayer player, final boolean gui,
			final boolean self) {

		final Inventory inventoryCompleted = Bukkit.createInventory(null, 54,
				ChatColor.BLUE + "" + ChatColor.BOLD + "Completed:" + player.getName());
		final Inventory inventoryOngoing = Bukkit.createInventory(null, 54,
				ChatColor.BLUE + "" + ChatColor.BOLD + "Ongoing:" + player.getName());
		final Inventory inventoryNotStarted = Bukkit.createInventory(null, 54,
				ChatColor.BLUE + "" + ChatColor.BOLD + "Not started:" + player.getName());

		requestCompletedAchievements(player, new IDataCallback<List<Entry<Achievement, Integer>>>() {

			@Override
			public void onError(Throwable error) {
				if (error instanceof UserNotFoundException) {
					sender.sendMessage(ChatColor.GRAY + Messages.getString(
							"mobhunting.commands.listachievements.player-empty", "player", player.getName()));
				} else {
					sender.sendMessage(ChatColor.RED + "An internal error occured while getting the achievements");
					error.printStackTrace();
				}
			}

			@Override
			public void onCompleted(List<Entry<Achievement, Integer>> data) {

				List<Entry<Achievement, Integer>> list = data;
				Comparator<Entry<Achievement, Integer>> comparator = new Comparator<Entry<Achievement, Integer>>() {
					@Override
					public int compare(Entry<Achievement, Integer> left, Entry<Achievement, Integer> right) {
						String id1 = left.getKey().getID(), id2 = right.getKey().getID();
						if (id1.startsWith("hunting-level") && id2.startsWith("hunting-level")) {
							id1 = id1.substring(12, id1.length());
							id2 = id2.substring(12, id2.length());
							String[] str1 = id1.split("-");
							String[] str2 = id2.split("-");
							return (str1[1] + str1[0]).compareTo(str2[1] + str2[0]);
						} else
							return left.getKey().getID().compareTo(right.getKey().getID());
					}
				};
				list.sort(comparator);
				data = list;

				int outOf = getAllAchievements().size();

				int count = 0;
				for (Map.Entry<Achievement, Integer> achievement : data) {
					if ((achievement.getValue() == -1) || ((achievement.getKey() instanceof ProgressAchievement
							&& ((ProgressAchievement) achievement.getKey()).nextLevelId() != null
							&& hasAchievement(((ProgressAchievement) achievement.getKey()).nextLevelId(), player))))
						++count;
				}

				// Build the output
				ArrayList<String> lines = new ArrayList<String>();

				if (!gui) {
					if (self)
						lines.add(ChatColor.GRAY
								+ Messages.getString("mobhunting.commands.listachievements.completed.self", "num",
										ChatColor.YELLOW + "" + count + ChatColor.GRAY, "max",
										ChatColor.YELLOW + "" + outOf + ChatColor.GRAY));
					else
						lines.add(ChatColor.GRAY
								+ Messages.getString("mobhunting.commands.listachievements.completed.other", "player",
										player.getName(), "num", ChatColor.YELLOW + "" + count + ChatColor.GRAY, "max",
										ChatColor.YELLOW + "" + outOf + ChatColor.GRAY));
				}

				boolean inProgress = false;
				int n = 0;
				for_loop: for (Map.Entry<Achievement, Integer> achievement : data) {
					if (achievement.getValue() == -1
							&& (achievement.getKey().getPrize() != 0 || !achievement.getKey().getPrizeCmd().isEmpty()
									|| MobHunting.getConfigManager().showAchievementsWithoutAReward)) {
						if (achievement.getKey() instanceof ProgressAchievement
								&& ((ProgressAchievement) achievement.getKey()).nextLevelId() != null
								&& hasAchievement(((ProgressAchievement) achievement.getKey()).nextLevelId(), player))
							continue for_loop;

						if (!gui) {
							lines.add(ChatColor.YELLOW + " " + achievement.getKey().getName());
							lines.add(
									ChatColor.GRAY + "    " + ChatColor.ITALIC + achievement.getKey().getDescription());
						} else if (sender instanceof Player)
							if (n <= 53) {
								if (self)
									addInventoryDetails(achievement.getKey().getSymbol(), inventoryCompleted, n,
											ChatColor.YELLOW + achievement.getKey().getName(),
											new String[] { ChatColor.GRAY + "" + ChatColor.ITALIC,
													achievement.getKey().getDescription(), "",
													Messages.getString(
															"mobhunting.commands.listachievements.completed.self",
															"num", ChatColor.YELLOW + "" + count + ChatColor.GRAY,
															"max", ChatColor.YELLOW + "" + outOf + ChatColor.GRAY) });
								else {
									addInventoryDetails(achievement.getKey().getSymbol(), inventoryCompleted, n,
											ChatColor.YELLOW + achievement.getKey().getName(),
											new String[] { ChatColor.GRAY + "" + ChatColor.ITALIC,
													achievement.getKey().getDescription(), "",
													Messages.getString(
															"mobhunting.commands.listachievements.completed.other",
															"player", player.getName(), "num",
															ChatColor.YELLOW + "" + count + ChatColor.GRAY, "max",
															ChatColor.YELLOW + "" + outOf + ChatColor.GRAY) });
								}
								n++;
							} else {
								Messages.debug("No room for more Achievements");
								break for_loop;
							}
					} else {
						if (achievement.getKey() instanceof ProgressAchievement && hasAchievement(
								(((ProgressAchievement) achievement.getKey()).nextLevelId()), player)) {
							Messages.debug("Error in DB %s is not done, but %s is... skipping",
									achievement.getKey().getID(),
									((ProgressAchievement) achievement.getKey()).nextLevelId());
						} else
							inProgress = true;
					}
				}

				n = 0;
				if (inProgress) {
					if (!gui) {
						lines.add("");
						lines.add(
								ChatColor.YELLOW + Messages.getString("mobhunting.commands.listachievements.progress"));
					}

					for_loop: for (Map.Entry<Achievement, Integer> achievement : data) {
						if (achievement.getValue() != -1 && achievement.getKey() instanceof ProgressAchievement
								&& (achievement.getKey().getPrize() != 0
										|| !achievement.getKey().getPrizeCmd().isEmpty()
										|| MobHunting.getConfigManager().showAchievementsWithoutAReward)) {

							if (achievement.getKey() instanceof ProgressAchievement
									&& ((ProgressAchievement) achievement.getKey()).nextLevelId() != null
									&& hasAchievement(((ProgressAchievement) achievement.getKey()).nextLevelId(),
											player))
								continue for_loop;

							if (((ProgressAchievement) achievement.getKey()).getNextLevel() != 0
									&& ((ProgressAchievement) achievement.getKey()).getExtendedMob()
											.getProgressAchievementLevel1() != 0) {
								if (!gui)
									lines.add(ChatColor.GRAY + " " + achievement.getKey().getName() + ChatColor.WHITE
											+ "  " + achievement.getValue() + " / "
											+ ((ProgressAchievement) achievement.getKey()).getNextLevel());
								else if (sender instanceof Player)
									if (n <= 53) {
										addInventoryDetails(achievement.getKey().getSymbol(), inventoryOngoing, n,
												ChatColor.YELLOW + achievement.getKey().getName(),
												new String[] { ChatColor.GRAY + "" + ChatColor.ITALIC,
														achievement.getKey().getDescription(), "",
														Messages.getString(
																"mobhunting.commands.listachievements.progress") + " "
																+ ChatColor.WHITE + achievement.getValue() + " / "
																+ ((ProgressAchievement) achievement.getKey())
																		.getNextLevel() });
										n++;
									} else {
										Messages.debug("No room for more achievements");
										break for_loop;
									}
							}
						}
					}
				}
				// Achievements NOT started.
				int m = 0;
				// Normal Achievement
				if (sender instanceof Player) {
					for_loop: for (Achievement achievement : getAllAchievements()) {
						if (!(achievement instanceof ProgressAchievement)) {
							if (!isOnGoingOrCompleted(achievement, data)) {
								if (achievement.getPrize() != 0 || !achievement.getPrizeCmd().isEmpty()
										|| MobHunting.getConfigManager().showAchievementsWithoutAReward) {
									if (m <= 53) {
										addInventoryDetails(achievement.getSymbol(), inventoryNotStarted, m,
												ChatColor.YELLOW + achievement.getName(),
												new String[] { ChatColor.GRAY + "" + ChatColor.ITALIC,
														achievement.getDescription(), "", Messages.getString(
																"mobhunting.commands.listachievements.notstarted") });

										m++;
									} else {
										Messages.debug("No room for achievement: %s", achievement.getName());
										break for_loop;
									}
								}
							}
						}
					}
					// ProgressAchivement
					for_loop: for (Achievement achievement : getAllAchievements()) {
						if ((achievement instanceof ProgressAchievement
								&& (achievement.getPrize() != 0 || !achievement.getPrizeCmd().isEmpty()
										|| MobHunting.getConfigManager().showAchievementsWithoutAReward)
								&& ((ProgressAchievement) achievement).getNextLevel() != 0)) {
							boolean ongoing = isOnGoingOrCompleted(achievement, data);
							if (!ongoing) {
								boolean nextLevelBegun = isNextLevelBegun((ProgressAchievement) achievement, data);
								boolean previousLevelCompleted = isPreviousLevelCompleted3(
										(ProgressAchievement) achievement, data);
								if (!nextLevelBegun && previousLevelCompleted) {
									if (m <= 53) {
										addInventoryDetails(achievement.getSymbol(), inventoryNotStarted, m,
												ChatColor.YELLOW + achievement.getName(),
												new String[] { ChatColor.GRAY + "" + ChatColor.ITALIC,
														achievement.getDescription(), "", Messages.getString(
																"mobhunting.commands.listachievements.notstarted") });

										m++;
									} else {
										Messages.debug("No room for achievement: %s", achievement.getName());
										break for_loop;
									}
								}
							}
						}
					}
				}
				if (!gui)
					sender.sendMessage(lines.toArray(new String[lines.size()]));
				else if (sender instanceof Player) {
					inventoryMapCompleted.put(sender, inventoryCompleted);
					inventoryMapOngoing.put(sender, inventoryOngoing);
					inventoryMapNotStarted.put(sender, inventoryNotStarted);
					((Player) sender).openInventory(inventoryMapCompleted.get(sender));
				}
			}

		});

	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getInventory() != null
				&& (ChatColor.stripColor(event.getInventory().getName()).startsWith("Completed:"))) {
			event.setCancelled(true);
			event.getWhoClicked().closeInventory();
			inventoryMapCompleted.remove(event.getWhoClicked());
			event.getWhoClicked().openInventory(inventoryMapOngoing.get(event.getWhoClicked()));
		}
		if (event.getInventory() != null
				&& (ChatColor.stripColor(event.getInventory().getName()).startsWith("Ongoing:"))) {
			event.setCancelled(true);
			event.getWhoClicked().closeInventory();
			inventoryMapOngoing.remove(event.getWhoClicked());
			event.getWhoClicked().openInventory(inventoryMapNotStarted.get(event.getWhoClicked()));
		}
		if (event.getInventory() != null
				&& (ChatColor.stripColor(event.getInventory().getName()).startsWith("Not started:"))) {
			event.setCancelled(true);
			event.getWhoClicked().closeInventory();
			inventoryMapNotStarted.remove(event.getWhoClicked());
		}
	}

	private boolean isNextLevelBegun(ProgressAchievement achievement, List<Entry<Achievement, Integer>> data) {
		if (achievement.nextLevelId() != null) {
			if (isOnGoingOrCompleted(achievement, data))
				return true;
			else
				return isNextLevelBegun((ProgressAchievement) getAchievement(achievement.nextLevelId().toLowerCase()),
						data);
		} else
			return false;
	}

	private boolean isPreviousLevelCompleted3(ProgressAchievement achievement, List<Entry<Achievement, Integer>> data) {
		if (achievement.inheritFrom() != null) {
			if (isCompleted((ProgressAchievement) getAchievement(achievement.inheritFrom().toLowerCase()), data))
				return true;
			else
				return false;
		} else
			return true;
	}

	private boolean isOnGoingOrCompleted(Achievement achievement, List<Entry<Achievement, Integer>> data) {
		for (Map.Entry<Achievement, Integer> achievement2 : data) {
			if (achievement.getID().equalsIgnoreCase(achievement2.getKey().getID())) {
				return true;
			}
		}
		return false;
	}

	private boolean isCompleted(Achievement achievement, List<Entry<Achievement, Integer>> data) {
		for (Map.Entry<Achievement, Integer> achievement2 : data) {
			if (achievement.getID().equalsIgnoreCase(achievement2.getKey().getID())) {
				return achievement2.getValue() == -1;
			}
		}
		return false;
	}

	public static void addInventoryDetails(ItemStack itemStack, Inventory inv, int Slot, String name, String[] lores) {
		final int max = 40;
		ItemMeta meta = itemStack.getItemMeta();
		meta.setDisplayName(name);
		ArrayList<String> lore = new ArrayList<String>();
		for (int n = 0; n < lores.length; n = n + 2) {
			String color = lores[n];
			String line, rest = lores[n + 1];
			while (!rest.isEmpty()) {
				if (rest.length() < max) {
					lore.add(color + rest);
					break;
				} else {
					int splitPos = rest.substring(0, max).lastIndexOf(" ");
					if (splitPos != -1) {
						line = rest.substring(0, splitPos);
						rest = rest.substring(splitPos + 1);
					} else {
						line = rest.substring(0, max);
						rest = rest.substring(max);
					}
					lore.add(color + line);
				}
			}
		}
		meta.setLore(lore);
		itemStack.setItemMeta(meta);

		inv.setItem(Slot, itemStack);
	}
}
