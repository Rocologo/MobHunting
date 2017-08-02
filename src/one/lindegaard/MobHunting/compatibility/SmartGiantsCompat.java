package one.lindegaard.MobHunting.compatibility;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

import me.jjm_223.smartgiants.SmartGiants;
import me.jjm_223.smartgiants.api.util.IGiantTools;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.mobs.MobPlugin;
import one.lindegaard.MobHunting.rewards.RewardData;

public class SmartGiantsCompat implements Listener {

	private static Plugin mPlugin;
	private static boolean supported = false;
	private static HashMap<String, RewardData> mMobRewardData = new HashMap<String, RewardData>();
	private static File file = new File(MobHunting.getInstance().getDataFolder(), "smartgiants-rewards.yml");
	private static YamlConfiguration config = new YamlConfiguration();
	public static final String MH_SMARTGIANTS = "MH:SMARTGIANTS";
	public static final String MONSTER_NAME = "SmartGiant"; 

	// https://www.spigotmc.org/threads/smartgiants.55208/

	public SmartGiantsCompat() {
		if (isDisabledInConfig()) {
			Bukkit.getLogger().info("[MobHunting] Compatibility with SmartGiants is disabled in config.yml");
		} else {
			mPlugin = Bukkit.getPluginManager().getPlugin("SmartGiants");

			if (mPlugin.getDescription().getVersion().compareTo("2.3.3") >= 0) {

				Bukkit.getLogger().info("[MobHunting] Enabling compatibility with SmartGiants ("
						+ mPlugin.getDescription().getVersion() + ")");

				supported = true;

				Bukkit.getPluginManager().registerEvents(this, MobHunting.getInstance());

				loadSmartGiantsData();
				saveSmartGiantsData();
			} else {
				ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
				console.sendMessage(ChatColor.RED + "[MobHunting] Your current version of SmartGiants ("
						+ mPlugin.getDescription().getVersion()
						+ ") has no API implemented. Please update to V2.3.3 or newer.");
			}
		}
	}

	// **************************************************************************
	// OTHER
	// **************************************************************************

	public Plugin getPlugin() {
		return mPlugin;
	}

	public static boolean isSupported() {
		return supported;
	}

	public static boolean isDisabledInConfig() {
		return MobHunting.getConfigManager().disableIntegrationSmartGiants;
	}

	public static boolean isEnabledInConfig() {
		return !MobHunting.getConfigManager().disableIntegrationSmartGiants;
	}

	public static boolean isSmartGiants(Entity entity) {
		if (supported) {
			IGiantTools tools = ((SmartGiants) mPlugin).getGiantTools();
			return tools.isSmartGiant(entity);
		}
		return false;
	}

	public static boolean isSmartGiants(String mob) {
		if (supported) {
			return mob.equalsIgnoreCase(MONSTER_NAME);
		}
		return false;
	}

	public static HashMap<String, RewardData> getMobRewardData() {
		return mMobRewardData;
	}

	public static int getProgressAchievementLevel1(String mobtype) {
		return mMobRewardData.get(mobtype).getAchivementLevel1();
	}

	public static String getSmartGiantsMobType(Entity killed) {
		if (killed.hasMetadata(MH_SMARTGIANTS)) {
			List<MetadataValue> data = killed.getMetadata(MH_SMARTGIANTS);
			MetadataValue value = data.get(0);
			return ((RewardData) value.value()).getMobType();
		} else
			return MONSTER_NAME;
	}

	// **************************************************************************
	// LOAD & SAVE
	// **************************************************************************
	public static void loadSmartGiantsData() {
		try {
			if (!file.exists()) {
				String monster = "SmartGiant";
				mMobRewardData.put(monster, new RewardData(MobPlugin.SmartGiants, monster, monster, "100:200",
						"minecraft:give {player} iron_sword 1", "You got an Iron sword.", 1, 1, 0.02));
				saveSmartGiantsData(mMobRewardData.get(monster).getMobType());
				return;
			}

			config.load(file);
			for (String key : config.getKeys(false)) {
				ConfigurationSection section = config.getConfigurationSection(key);
				RewardData mob = new RewardData();
				mob.read(section);
				mob.setMobType(key);
				mMobRewardData.put(key, mob);
				MobHunting.getStoreManager().insertSmartGiants(key);
			}
			Messages.injectMissingMobNamesToLangFiles();
			Messages.debug("Loaded %s SmartGiants", mMobRewardData.size());
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}

	}

	public static void loadSmartGiantsData(String key) {
		try {
			if (!file.exists()) {
				return;
			}

			config.load(file);
			ConfigurationSection section = config.getConfigurationSection(key);
			RewardData mob = new RewardData();
			mob.read(section);
			mob.setMobType(key);
			mMobRewardData.put(key, mob);
			MobHunting.getStoreManager().insertSmartGiants(key);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}

	public static void saveSmartGiantsData() {
		try {
			config.options().header("This a extra MobHunting config data for the SmartGiants on your server.");

			if (mMobRewardData.size() > 0) {

				int n = 0;
				for (String str : mMobRewardData.keySet()) {
					ConfigurationSection section = config.createSection(str);
					mMobRewardData.get(str).save(section);
					n++;
				}

				if (n != 0) {
					Messages.debug("Saving Mobhunting extra SmartGiants data.");
					config.save(file);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void saveSmartGiantsData(String key) {
		try {
			if (mMobRewardData.containsKey(key)) {
				ConfigurationSection section = config.createSection(key);
				mMobRewardData.get(key).save(section);
				Messages.debug("Saving extra SmartGiants data for mob=%s (%s)", key,
						mMobRewardData.get(key).getMobName());
				config.save(file);
			} else {
				Messages.debug("ERROR! SmartGiants ID (%s) is not found in mMobRewardData", key);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// **************************************************************************
	// EVENTS
	// **************************************************************************

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	private void onSmartGiantsSpawnEvent(EntitySpawnEvent event) {

		Entity entity = event.getEntity();

		if (isSmartGiants(entity)) {
			Messages.debug("A SmartGiant was spawned at %s,%s,%s in %s", 
					event.getEntity().getLocation().getBlockX(),
					event.getEntity().getLocation().getBlockY(),
					event.getEntity().getLocation().getBlockZ(),
					event.getEntity().getLocation().getWorld().getName());
			String mobtype = MONSTER_NAME;
			if (mMobRewardData != null && !mMobRewardData.containsKey(mobtype)) {
				Messages.debug("New SmartGiants mob found=%s (%s)", mobtype, mobtype.toString());
				mMobRewardData.put(mobtype, new RewardData(MobPlugin.SmartGiants, mobtype, mobtype, "100:200",
						"minecraft:give {player} iron_sword 1", "You got an Iron sword.", 1, 1, 0.02));
				saveSmartGiantsData(mobtype);
				MobHunting.getStoreManager().insertSmartGiants(mobtype);
				// Update mob loaded into memory
				MobHunting.getExtendedMobManager().updateExtendedMobs();
				Messages.injectMissingMobNamesToLangFiles();
			}
			event.getEntity().setMetadata(MH_SMARTGIANTS, new FixedMetadataValue(mPlugin, mMobRewardData.get(mobtype)));
		}
	}

}
