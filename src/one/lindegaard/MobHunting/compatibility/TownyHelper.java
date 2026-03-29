package one.lindegaard.MobHunting.compatibility;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import one.lindegaard.MobHunting.MobHunting;

public class TownyHelper {

	private static boolean initialized = false;
	private static boolean bridgeReady = false;
	private static boolean runtimeWarningLogged = false;

	private static Method townyApiGetInstanceMethod;
	private static Method townyApiGetResidentMethod;
	private static Method townyApiGetTownBlockMethod;
	private static Method residentGetTownMethod;
	private static Method residentGetTownOrNullMethod;
	private static Method townBlockGetTownMethod;
	private static Method townBlockGetTownOrNullMethod;

	public static synchronized boolean initializeBridge() {
		if (initialized) {
			return bridgeReady;
		}

		initialized = true;
		bridgeReady = false;

		try {
			Class<?> townyApiClass = Class.forName("com.palmergames.bukkit.towny.TownyAPI");
			Class<?> residentClass = Class.forName("com.palmergames.bukkit.towny.object.Resident");
			Class<?> townBlockClass = Class.forName("com.palmergames.bukkit.towny.object.TownBlock");

			townyApiGetInstanceMethod = townyApiClass.getMethod("getInstance");
			townyApiGetResidentMethod = townyApiClass.getMethod("getResident", Player.class);
			townyApiGetTownBlockMethod = townyApiClass.getMethod("getTownBlock", Location.class);

			residentGetTownOrNullMethod = findMethod(residentClass, "getTownOrNull");
			residentGetTownMethod = findMethod(residentClass, "getTown");
			townBlockGetTownOrNullMethod = findMethod(townBlockClass, "getTownOrNull");
			townBlockGetTownMethod = findMethod(townBlockClass, "getTown");

			if ((residentGetTownOrNullMethod == null && residentGetTownMethod == null)
					|| (townBlockGetTownOrNullMethod == null && townBlockGetTownMethod == null)) {
				Bukkit.getConsoleSender().sendMessage(MobHunting.PREFIX_WARNING
						+ "Towny compatibility disabled: unsupported Towny API (missing town resolver methods). ");
				return false;
			}

			Object townyApi = townyApiGetInstanceMethod.invoke(null);
			if (townyApi == null) {
				Bukkit.getConsoleSender().sendMessage(
						MobHunting.PREFIX_WARNING + "Towny compatibility disabled: TownyAPI.getInstance() returned null.");
				return false;
			}

			bridgeReady = true;
			return true;
		} catch (Throwable t) {
			Bukkit.getConsoleSender()
					.sendMessage(MobHunting.PREFIX_WARNING + "Towny compatibility disabled: " + shortError(t));
			return false;
		}
	}

	public static boolean isInHomeTown(Player player) {
		if (!TownyCompat.isSupported() || player == null || !initializeBridge()) {
			return false;
		}

		try {
			Object townyApi = townyApiGetInstanceMethod.invoke(null);
			if (townyApi == null) {
				warnRuntime("TownyAPI.getInstance() returned null.", null);
				return false;
			}

			Object resident = townyApiGetResidentMethod.invoke(townyApi, player);
			if (resident == null) {
				MobHunting.getInstance().getMessages().debug("%s is not a Towny resident", player.getName());
				return false;
			}

			Object townBlock = townyApiGetTownBlockMethod.invoke(townyApi, player.getLocation());
			if (townBlock == null) {
				return false;
			}

			Object residentTown = resolveResidentTown(resident);
			Object townBlockTown = resolveTownBlockTown(townBlock);
			if (residentTown == null || townBlockTown == null) {
				return false;
			}

			boolean inHomeTown = residentTown.equals(townBlockTown);
			if (inHomeTown) {
				MobHunting.getInstance().getMessages().debug("%s is in his HomeTown", player.getName());
			}
			return inHomeTown;
		} catch (InvocationTargetException e) {
			warnRuntime("resolving Towny home-town check", e.getTargetException());
			return false;
		} catch (Throwable t) {
			warnRuntime("resolving Towny home-town check", t);
			return false;
		}
	}

	public static boolean isInAnyTomn(Player player) {
		if (!TownyCompat.isSupported() || player == null || !initializeBridge()) {
			return false;
		}

		try {
			Object townyApi = townyApiGetInstanceMethod.invoke(null);
			if (townyApi == null) {
				warnRuntime("TownyAPI.getInstance() returned null.", null);
				return false;
			}

			Object townBlock = townyApiGetTownBlockMethod.invoke(townyApi, player.getLocation());
			return townBlock != null;
		} catch (InvocationTargetException e) {
			warnRuntime("resolving Towny any-town check", e.getTargetException());
			return false;
		} catch (Throwable t) {
			warnRuntime("resolving Towny any-town check", t);
			return false;
		}
	}

	private static Method findMethod(Class<?> type, String methodName) {
		try {
			return type.getMethod(methodName);
		} catch (NoSuchMethodException e) {
			return null;
		}
	}

	private static Object resolveResidentTown(Object resident) throws IllegalAccessException, InvocationTargetException {
		if (resident == null) {
			return null;
		}

		if (residentGetTownOrNullMethod != null) {
			return residentGetTownOrNullMethod.invoke(resident);
		}

		if (residentGetTownMethod != null) {
			return residentGetTownMethod.invoke(resident);
		}

		return null;
	}

	private static Object resolveTownBlockTown(Object townBlock) throws IllegalAccessException, InvocationTargetException {
		if (townBlock == null) {
			return null;
		}

		if (townBlockGetTownOrNullMethod != null) {
			return townBlockGetTownOrNullMethod.invoke(townBlock);
		}

		if (townBlockGetTownMethod != null) {
			return townBlockGetTownMethod.invoke(townBlock);
		}

		return null;
	}

	private static void warnRuntime(String context, Throwable t) {
		if (runtimeWarningLogged) {
			return;
		}
		runtimeWarningLogged = true;

		String suffix = (t != null) ? (": " + shortError(t)) : "";
		Bukkit.getConsoleSender().sendMessage(MobHunting.PREFIX_WARNING
				+ "Towny compatibility runtime check failed while " + context + suffix + ".");
		MobHunting.getInstance().getMessages().debug("Towny runtime compatibility failure while %s%s", context,
				(t != null) ? (": " + shortError(t)) : "");
	}

	private static String shortError(Throwable t) {
		String message = t.getMessage();
		if (message == null || message.trim().isEmpty()) {
			return t.getClass().getSimpleName();
		}
		return t.getClass().getSimpleName() + ": " + message;
	}

}
