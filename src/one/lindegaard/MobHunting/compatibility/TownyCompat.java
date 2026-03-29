package one.lindegaard.MobHunting.compatibility;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import one.lindegaard.CustomItemsLib.compatibility.CompatPlugin;
import one.lindegaard.MobHunting.MobHunting;

public class TownyCompat {

	private static Plugin mPlugin;
	private static boolean supported = false;

	// http://towny.palmergames.com/

	public TownyCompat() {
		supported = false;
		if (!isEnabledInConfig()) {
			Bukkit.getConsoleSender()
					.sendMessage(MobHunting.PREFIX_WARNING + "Compatibility with Towny is disabled in config.yml");
			return;
		}

		mPlugin = Bukkit.getPluginManager().getPlugin(CompatPlugin.Towny.getName());
		if (mPlugin == null) {
			Bukkit.getConsoleSender()
					.sendMessage(MobHunting.PREFIX_WARNING + "Towny plugin not found. Compatibility is disabled.");
			return;
		}

		try {
			Class.forName("com.palmergames.bukkit.towny.TownyAPI");
			if (TownyHelper.initializeBridge()) {
				Bukkit.getConsoleSender().sendMessage(
						MobHunting.PREFIX + "Enabling compatibility with Towny (" + mPlugin.getDescription().getVersion() + ")");
				supported = true;
			} else {
				Bukkit.getConsoleSender().sendMessage(MobHunting.PREFIX_WARNING + "Your version of Towny ("
						+ mPlugin.getDescription().getVersion()
						+ ") is not compatible with this MobHunting Towny integration (Towny 0.102+ API expected).");
			}
		} catch (ClassNotFoundException e) {
			Bukkit.getConsoleSender().sendMessage(MobHunting.PREFIX_WARNING + "Your version of Towny ("
					+ mPlugin.getDescription().getVersion()
					+ ") is not compatible with this MobHunting Towny integration (Towny 0.102+ API expected).");
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

	public static boolean isEnabledInConfig() {
		return MobHunting.getInstance().getConfigManager().enableIntegrationTowny;
	}

	public static boolean isInHomeTown(Player player) {
		if (supported) {
			return TownyHelper.isInHomeTown(player);
		}
		return false;
	}

	public static boolean isInAnyTown(Player player) {
		if (supported) {
			return TownyHelper.isInAnyTomn(player);
		}
		return false;
	}

}
