package one.lindegaard.MobHunting.compatibility;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import one.lindegaard.MobHunting.MobHunting;
import uk.antiperson.stackmob.tools.extras.*;

public class StackMobCompat implements Listener {

	// https://www.spigotmc.org/resources/stackmob.29999/

	private static boolean supported = false;
	private static Plugin mPlugin;

	public StackMobCompat() {
		if (isDisabledInConfig()) {
			Bukkit.getLogger().info("[MobHunting] Compatibility with StackMob is disabled in config.yml");
		} else {
			mPlugin = Bukkit.getPluginManager().getPlugin(CompatPlugin.StackMob.getName());
			if (mPlugin.getDescription().getVersion().compareTo("2.0.0") >= 0) {
				Bukkit.getLogger().info("[MobHunting] Enabling compatibility with StackMob ("
						+ mPlugin.getDescription().getVersion() + ").");
				Bukkit.getPluginManager().registerEvents(this, MobHunting.getInstance());
				supported = true;
			} else {
				ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
				console.sendMessage(ChatColor.RED + "[MobHunting] Your current version of StackMob ("
						+ mPlugin.getDescription().getVersion()
						+ ") is not supported by MobHunting, please upgrade to 2.0.7 or newer.");
			}
		}
	}

	// **************************************************************************
	// OTHER FUNCTIONS
	// **************************************************************************

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
			return entity.hasMetadata(getTag());
		}
		return false;
	}

	public static int getStackSize(Entity entity) {
		if (entity.hasMetadata(getTag())) {
			return (Integer) entity.getMetadata(getTag()).get(0).value();
		}
		return 1;
	}

	public static boolean killHoleStackOnDeath(Entity entity) {
		return mPlugin.getConfig().getBoolean("kill-all.enabled");
	}

	public static boolean isGrindingStackedMobsAllowed() {
		return MobHunting.getConfigManager().isGrindingStackedMobsAllowed;
	}

	private static String getTag() {
		return GlobalValues.metaTag;
	}

}
