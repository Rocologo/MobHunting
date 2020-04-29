package one.lindegaard.MobHunting.compatibility;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

import me.lorinth.rpgmobs.LorinthsRpgMobs;
import one.lindegaard.Core.compatibility.CompatPlugin;
import one.lindegaard.MobHunting.MobHunting;

public class LorinthsRpgMobsCompat implements Listener {

	// https://dev.bukkit.org/projects/lorinthsrpgmobs

	private static boolean supported = false;
	private static Plugin mPlugin;
	public static final String MH_LORINTHS_RPG_MOBS = "MH:LorinthsRpgMobs";

	public LorinthsRpgMobsCompat() {
		if (!isEnabledInConfig()) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "[MobHunting] " + ChatColor.RESET
					+ "Compatibility with LorinthsRpgMobs is disabled in config.yml");
		} else {
			mPlugin = Bukkit.getPluginManager().getPlugin(CompatPlugin.LorinthsRpgMobs.getName());

			if (mPlugin.getDescription().getVersion().compareTo("1.0.2") >= 0) {
				Bukkit.getPluginManager().registerEvents(this, MobHunting.getInstance());
				Bukkit.getConsoleSender()
						.sendMessage(ChatColor.GOLD + "[MobHunting] " + ChatColor.RESET
								+ "Enabling Compatibility with LorinthsRpgMobs ("
								+ getLorinthsRpgMobs().getDescription().getVersion() + ")");
				supported = true;
			} else {
				Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "[MobHunting] " + ChatColor.RED
						+ "Your current version of LorinthsRpgMobs (" + mPlugin.getDescription().getVersion()
						+ ") is not supported by MobHunting. Please update LorinthsRpgMobs to version 1.0.2 or newer.");
			}
		}

	}

	// **************************************************************************
	// OTHER FUNCTIONS
	// **************************************************************************
	public static Plugin getLorinthsRpgMobs() {
		return mPlugin;
	}

	public static boolean isSupported() {
		return supported;
	}

	public static boolean isLorinthsRpgMobs(Entity entity) {
		if (isSupported()) {
			return LorinthsRpgMobs.GetLevelOfEntity(entity) != null;
		}
		return false;
	}

	public static Integer getLorinthsRpgMobsLevel(Entity entity) {
		if (isSupported() && (entity instanceof Creature))
			return LorinthsRpgMobs.GetLevelOfCreature((Creature) entity);
		return 1;
	}

	public static double getLorinthsRpgMobsMultiplier(Entity killed) {
		List<MetadataValue> data = killed.getMetadata(MH_LORINTHS_RPG_MOBS);
		MetadataValue value = data.get(0);
		return (double) value.value();
	}

	public static boolean isEnabledInConfig() {
		return MobHunting.getInstance().getConfigManager().enableIntegrationLorinthsRpgMobs;
	}

	// **************************************************************************
	// EVENTS
	// **************************************************************************

	/**
	 * Detects when a LorinthsRpgMobs is spawned
	 * 
	 * @param event
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void LorinthsRpgMobsSpawnEvent(EntitySpawnEvent event) {
		if (isSupported()) {
			Entity entity = event.getEntity();
			if (isLorinthsRpgMobs(entity)) {
				int level = getLorinthsRpgMobsLevel(entity);
				// Messages.debug("LorinthsRpgMobsSpawnEvent:
				// MinecraftMobtype=%s Level=%s", entity.getType(), level);
				entity.setMetadata(MH_LORINTHS_RPG_MOBS, new FixedMetadataValue(mPlugin, level));
			}
		}
	}

}
