package one.lindegaard.MobHunting.compatibility;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import uk.antiperson.stackmob.StackMob;
import uk.antiperson.stackmob.api.StackMobAPI;

public class StackMobCompat implements Listener {

	// https://www.spigotmc.org/resources/stackmob.29999/

	private static boolean supported = false;
	private static Plugin mPlugin;

	public StackMobCompat() {
		if (isDisabledInConfig()) {
			Bukkit.getLogger().info("[MobHunting] Compatibility with StackMob is disabled in config.yml");
		} else {
			mPlugin = Bukkit.getPluginManager().getPlugin("StackMob");

			Bukkit.getPluginManager().registerEvents(this, MobHunting.getInstance());

			Bukkit.getLogger().info("[MobHunting] Enabling Compatibility with StackMob ("
					+ mPlugin.getDescription().getVersion() + ")");
			supported = true;
		}
	}

	// **************************************************************************
	// OTHER FUNCTIONS
	// **************************************************************************
	public static StackMobAPI getStackMobAPI() {
		return ((StackMob) mPlugin).getAPI();
	}

	public static boolean isSupported() {
		return supported;
	}

	private static boolean isDisabledInConfig() {
		return MobHunting.getConfigManager().disableIntegrationStackMob;
	}

	@SuppressWarnings("unused")
	private static boolean isEnabledInConfig() {
		return !MobHunting.getConfigManager().disableIntegrationStackMob;
	}

	public static boolean isStackedMob(Entity entity) {
		if (isSupported()) {
			//return ((StackMob) Bukkit.getPluginManager().getPlugin("StackMob")).amountMap.containsKey(entity.getUniqueId());
			return getStackMobAPI().getEntityManager().isStackedEntity(entity);
		}
		return false;
	}

	public static int getStackSize(Entity entity) {
		return getStackMobAPI().getEntityManager().getStackedEntity(entity).getStackAmount();
	}

	public static boolean killHoleStackOnDeath(Entity entity) {
		return mPlugin.getConfig().getBoolean("creature.kill-all.enabled");
	}

	public static boolean isGrindingStackedMobsAllowed() {
		return MobHunting.getConfigManager().isGrindingStackedMobsAllowed;
	}

}
