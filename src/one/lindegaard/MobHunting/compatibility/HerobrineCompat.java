package one.lindegaard.MobHunting.compatibility;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import net.theprogrammersworld.herobrine.Herobrine;
import net.theprogrammersworld.herobrine.nms.entity.MobType;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.StatType;
import one.lindegaard.MobHunting.mobs.MinecraftMob;
import one.lindegaard.MobHunting.rewards.RewardData;

public class HerobrineCompat implements Listener {

	// https://www.theprogrammersworld.net/Herobrine/

	private static boolean supported = false;
	private static Plugin mPlugin;
	private static HashMap<String, RewardData> mMobRewardData = new HashMap<String, RewardData>();
	private static Herobrine api;
	public static final String MH_HEROBRINEMOBS = "MH:Herobrine";
	private static File file = new File(MobHunting.getInstance().getDataFolder(), "herobrine-rewards.yml");
	private static YamlConfiguration config = new YamlConfiguration();
	private static HashMap<Integer, MobType> mobList = new HashMap<Integer, MobType>();

	public HerobrineCompat() {
		if (isDisabledInConfig()) {
			Bukkit.getConsoleSender()
					.sendMessage("[MobHunting] Compatibility with Herobrine is disabled in config.yml");
		} else {
			mPlugin = Bukkit.getPluginManager().getPlugin(CompatPlugin.Herobrine.getName());

			Bukkit.getPluginManager().registerEvents(this, MobHunting.getInstance());

			Bukkit.getConsoleSender().sendMessage("[MobHunting] Enabling Compatibility with Herobrine ("
					+ mPlugin.getDescription().getVersion() + ")");

			api = (Herobrine) mPlugin;
			supported = true;
		}
	}

	// **************************************************************************
	// OTHER FUNCTIONS
	// **************************************************************************

	public static boolean isSupported() {
		return supported;
	}

	public static boolean isHerobrineMob(Entity entity) {
		if (isSupported()) {
			return entity.hasMetadata(MH_HEROBRINEMOBS) || entity.hasMetadata("NPC")
					|| entity.getEntityId() == Herobrine.herobrineEntityID
					|| api.getEntityManager().isCustomMob(entity.getEntityId());
		}
		return false;
	}

	public static boolean isHerobrineMob(String key) {
		if (isSupported()) {
			for (int i : mobList.keySet()) {
				if (mobList.get(i).name() == key)
					return true;
			}
		}
		return false;
	}
	
	public static String getHerobrineMobType(Entity entity) {
		return api.getEntityManager().getMobType(entity.getEntityId()).getMobType().name();
	}

	public static HashMap<String, RewardData> getMobRewardData() {
		return mMobRewardData;
	}

	public static boolean isDisabledInConfig() {
		return MobHunting.getConfigManager().disableIntegrationHerobrine;
	}

	public static boolean isEnabledInConfig() {
		return !MobHunting.getConfigManager().disableIntegrationHerobrine;
	}

	// **************************************************************************
	// EVENTS
	// **************************************************************************
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	private void onHerobrineMobDeathEvent(EntityDeathEvent event) {
		Entity entity = event.getEntity();
		if (isHerobrineMob(entity)) {
			entity.setMetadata(MH_HEROBRINEMOBS, new FixedMetadataValue(MobHunting.getInstance(), true));
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	private void onHerobrineMobSpawnEvent(EntitySpawnEvent event) {
		if (isSupported()) {
			Entity entity = event.getEntity();
			if (isHerobrineMob(entity)) {
				mobList.put(entity.getEntityId(), api.getEntityManager().getMobType(entity.getEntityId()).getMobType());
				Messages.debug("A Herobrine Mob (%s) was spawned at %s,%s,%s in %s",
						api.getEntityManager().getMobType(entity.getEntityId()).getMobType().name(),
						event.getEntity().getLocation().getBlockX(), event.getEntity().getLocation().getBlockY(),
						event.getEntity().getLocation().getBlockZ(),
						event.getEntity().getLocation().getWorld().getName());
				saveHerobrineMobsData(api.getEntityManager().getMobType(entity.getEntityId()).getMobType().name());
				event.getEntity().setMetadata(MH_HEROBRINEMOBS, new FixedMetadataValue(mPlugin, true));
			}
		}
	}

	public static int getProgressAchievementLevel1(String mobtype) {
		MinecraftMob mob = MinecraftMob.valueOf(mobtype);
		if (mob != null)
			return mob.getProgressAchievementLevel1();
		else
			return 100;
	}

	// **************************************************************************
	// LOAD & SAVE
	// **************************************************************************

	public static void loadHerobrineMobsData() {
		try {
			if (!file.exists())
				return;
			Messages.debug("Loading extra MobRewards for Herobrine mobs.");

			config.load(file);
			int n = 0;
			for (String key : config.getKeys(false)) {
				ConfigurationSection section = config.getConfigurationSection(key);
				if (isHerobrineMob(key)) {
					RewardData mob = new RewardData();
					mob.read(section);
					mob.setMobType(key);
					mMobRewardData.put(key, mob);
					MobHunting.getStoreManager().insertMissingHerobrineMobs(key);
					n++;
				} else {
					Messages.debug("The mob=%s can't be found in Herobrine configuration files", key);
				}
			}
			Messages.injectMissingMobNamesToLangFiles();
			Messages.debug("Loaded %s HerobrineMobs", n);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}

	}

	public static void loadHerobrineMobsData(String key) {
		try {
			if (!file.exists())
				return;

			config.load(file);
			ConfigurationSection section = config.getConfigurationSection(key);
			if (isHerobrineMob(key)) {
				RewardData mob = new RewardData();
				mob.read(section);
				mob.setMobType(key);
				mMobRewardData.put(key, mob);
				int n = StatType.values().length;
				StatType.values()[n + 1] = new StatType(mob.getMobType() + "_kill", mob.getMobName());
				StatType.values()[n + 2] = new StatType(mob.getMobType() + "_assist", mob.getMobName());
				MobHunting.getStoreManager().insertMissingHerobrineMobs(key);
			} else {
				Messages.debug("The mob=%s can't be found in Herobrine configuration files", key);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}

	public static void saveHerobrineMobsData() {
		try {
			config.options().header("This a extra MobHunting config data for the Herobrine on your server.");

			if (mMobRewardData.size() > 0) {

				int n = 0;
				for (String str : mMobRewardData.keySet()) {
					ConfigurationSection section = config.createSection(str);
					mMobRewardData.get(str).save(section);
					n++;
				}

				if (n != 0) {
					Messages.debug("Saving Mobhunting extra Herobrine data.");
					config.save(file);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void saveHerobrineMobsData(String key) {
		try {
			if (mMobRewardData.containsKey(key)) {
				ConfigurationSection section = config.createSection(key);
				mMobRewardData.get(key).save(section);
				Messages.debug("Saving Mobhunting extra Herobrine data.");
				config.save(file);
			} else {
				Messages.debug("ERROR! Herobrine ID (%s) is not found in mMobRewardData", key);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
}
