package one.lindegaard.MobHunting.compatibility;

import java.util.HashSet;
import java.util.Iterator;

import org.bukkit.Bukkit;

public class CompatibilityManager {
	private static HashSet<Object> mCompatClasses = new HashSet<Object>();

	/**
	 * Registers the compatability handler if the plugin specified is loaded
	 * 
	 * @param compatibilityHandler
	 *            The class that will be created
	 * @param pluginName
	 *            The name of the plugin to check
	 */
	public static void register(Class<?> compatibilityHandler, String pluginName) {
		if (Bukkit.getPluginManager().isPluginEnabled(pluginName)) {
			try {
				mCompatClasses.add(compatibilityHandler.newInstance());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * detect if the compatibility class is loaded.
	 * 
	 * @param class1
	 *            - The Compatibility class ex. "WorldGuardCompat.class"
	 * @return true if loaded.
	 */
	public static boolean isPluginLoaded(Class<?> class1) {
		Iterator<Object> i = mCompatClasses.iterator();
		while (i.hasNext()) {
			Class<?> c = i.next().getClass();
			if (c.getName().equalsIgnoreCase(class1.getName()))
				return true;
		}
		return false;
	}

}
