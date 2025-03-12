package one.lindegaard.MobHunting.compatibility;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import me.stumper66.levelledmobs.LevelInterface;
import me.stumper66.levelledmobs.LevelledMobs;
import one.lindegaard.CustomItemsLib.compatibility.CompatPlugin;
import one.lindegaard.MobHunting.MobHunting;

public class LevelledMobsCompat implements Listener {

	// https://www.spigotmc.org/resources/levelledmobs-for-1-14-x-1-17-x.74304/

	private static boolean supported = false;
	private static Plugin mPlugin;

	public LevelledMobsCompat() {
		if (!isEnabledInConfig()) {
			Bukkit.getConsoleSender().sendMessage(
					MobHunting.PREFIX_WARNING + "Compatibility with LevelledMobs is disabled in config.yml");
		} else {
			mPlugin = Bukkit.getPluginManager().getPlugin(CompatPlugin.LevelledMobs.getName());

			if (mPlugin.getDescription().getVersion().compareTo("3.0.7") >= 0) {
				Bukkit.getPluginManager().registerEvents(this, MobHunting.getInstance());
				Bukkit.getConsoleSender().sendMessage(MobHunting.PREFIX + "Enabling Compatibility with LevelledMobs ("
						+ getLevelledMobs().getDescription().getVersion() + ")");
				supported = true;
			} else {
				Bukkit.getConsoleSender().sendMessage(MobHunting.PREFIX_WARNING
						+ "Your current version of LevelledMobs (" + mPlugin.getDescription().getVersion()
						+ ") is not supported by MobHunting. Please update LevelledMobs to version 3.0.7 or newer.");
			}
		}

	}

	// **************************************************************************
	// OTHER FUNCTIONS
	// **************************************************************************
	public static Plugin getLevelledMobs() {
		return mPlugin;
	}

	public static boolean isSupported() {
		return supported;
	}

	private static final LevelInterface getLevelInterfac() {
		return ((LevelledMobs) getLevelledMobs()).levelInterface;
	}

	public static boolean isLevelledMobs(Entity entity) {
		if (!isSupported())
			return false;
		if (!(entity instanceof LivingEntity))
			return false;

		return getLevelInterfac().isLevelled((LivingEntity) entity);
	}

	public static Integer getLevelledMobsLevel(Entity entity) {
		if (!isSupported())
			return 1;

		return getLevelInterfac().getLevelOfMob((LivingEntity) entity);
	}

	public static boolean isEnabledInConfig() {
		return MobHunting.getInstance().getConfigManager().enableIntegrationLevelledMobs;
	}

	// **************************************************************************
	// EVENTS
	// **************************************************************************

	/**
	 * @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	 *                        private void LevelledMobsSpawnEvent(EntitySpawnEvent
	 *                        event) { Entity entity = event.getEntity(); if
	 *                        (isLevelledMobs(entity)) { int level =
	 *                        getMobLevel(entity); //
	 *                        Core.getMessages().debug("LevelledMobsSpawnEvent:
	 *                        MinecraftMobtype=%s // Level=%s", entity.getType(),
	 *                        level); } }
	 **/

}
