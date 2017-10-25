package one.lindegaard.MobHunting.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;

import one.lindegaard.MobHunting.MobHunting;

public class Misc {
	public static boolean isAxe(ItemStack item) {
		return item != null && (item.getType() == Material.DIAMOND_AXE || item.getType() == Material.GOLD_AXE
				|| item.getType() == Material.IRON_AXE || item.getType() == Material.STONE_AXE
				|| item.getType() == Material.WOOD_AXE);
	}

	public static boolean isSword(ItemStack item) {
		return item != null && (item.getType() == Material.DIAMOND_SWORD || item.getType() == Material.GOLD_SWORD
				|| item.getType() == Material.IRON_SWORD || item.getType() == Material.STONE_SWORD
				|| item.getType() == Material.WOOD_SWORD);
	}

	public static boolean isPick(ItemStack item) {
		return item != null && (item.getType() == Material.DIAMOND_PICKAXE || item.getType() == Material.GOLD_PICKAXE
				|| item.getType() == Material.IRON_PICKAXE || item.getType() == Material.STONE_PICKAXE
				|| item.getType() == Material.WOOD_PICKAXE);
	}

	public static boolean isBow(ItemStack item) {
		return item != null && (item.getType() == Material.BOW);
	}

	public static boolean isUnarmed(ItemStack item) {
		return (item == null || item.getType() == Material.AIR);
	}

	public static boolean isSign(Block block) {
		if (block.getType().equals(Material.SIGN_POST) || block.getType().equals(Material.WALL_SIGN))
			return true;
		else
			return false;
	}

	public static boolean isSign(Material material) {
		if (material.equals(Material.SIGN_POST) || material.equals(Material.WALL_SIGN))
			return true;
		else
			return false;
	}

	public static Map<String, Object> toMap(Location loc) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("X", loc.getX());
		map.put("Y", loc.getY());
		map.put("Z", loc.getZ());

		map.put("Yaw", (double) loc.getYaw());
		map.put("Pitch", (double) loc.getPitch());

		if (loc.getWorld() != null)
			map.put("W", loc.getWorld().getUID().toString());

		return map;
	}

	public static Location fromMap(Map<String, Object> map) {
		double x, y, z;
		float yaw, pitch;
		UUID world;

		x = (Double) map.get("X");
		y = (Double) map.get("Y");
		z = (Double) map.get("Z");

		yaw = (float) (double) (Double) map.get("Yaw");
		pitch = (float) (double) (Double) map.get("Pitch");

		if (map.containsKey("W")) {
			world = UUID.fromString((String) map.get("W"));
			return new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
		} else
			return new Location(null, x, y, z, yaw, pitch);
	}

	public static boolean isMC112() {
		return Bukkit.getBukkitVersion().contains("1.12");
	}

	public static boolean isMC111() {
		return Bukkit.getBukkitVersion().contains("1.11");
	}

	public static boolean isMC110() {
		return Bukkit.getBukkitVersion().contains("1.10");
	}

	public static boolean isMC19() {
		return Bukkit.getBukkitVersion().contains("1.9");
	}

	public static boolean isMC18() {
		return Bukkit.getBukkitVersion().contains("1.8");
	}

	public static boolean isMC17() {
		return Bukkit.getBukkitVersion().contains("1.7");
	}

	public static boolean isMC112OrNewer() {
		if (isMC112())
			return true;
		else if (isMC111() || isMC110() || isMC19() || isMC18() || isMC17())
			return false;
		return true;
	}

	public static boolean isMC111OrNewer() {
		if (isMC111())
			return true;
		else if (isMC110() || isMC19() || isMC18() || isMC17())
			return false;
		return true;
	}

	public static boolean isMC110OrNewer() {
		if (isMC110())
			return true;
		else if (isMC19() || isMC18() || isMC17())
			return false;
		return true;
	}

	public static boolean isMC19OrNewer() {
		if (isMC19())
			return true;
		else if (isMC18() || isMC17())
			return false;
		return true;
	}

	public static boolean isMC18OrNewer() {
		if (isMC18())
			return true;
		else if (isMC17())
			return false;
		return true;
	}

	public static Player getOnlinePlayer(OfflinePlayer offlinePlayer) {
		for (Player player : MobHunting.getMobHuntingManager().getOnlinePlayers()) {
			if (player.getName().equals(offlinePlayer.getName()))
				return player;
		}
		return null;
	}

	public static String trimSignText(String string) {
		return string.length() > 15 ? string.substring(0, 14).trim() : string;
	}

	public static double round(double d) {
		return Math.round(d / MobHunting.getConfigManager().rewardRounding)
				* MobHunting.getConfigManager().rewardRounding;
	}

	public static double ceil(double d) {
		return Math.ceil(d / MobHunting.getConfigManager().rewardRounding)
				* MobHunting.getConfigManager().rewardRounding;
	}

	public static double floor(double d) {
		return Math.floor(d / MobHunting.getConfigManager().rewardRounding)
				* MobHunting.getConfigManager().rewardRounding;
	}

	public static final Block getTargetBlock(Player player, int range) {
		BlockIterator iter = new BlockIterator(player, range);
		Block lastBlock = iter.next();
		while (iter.hasNext()) {
			lastBlock = iter.next();
			if (lastBlock.getType() == Material.AIR) {
				continue;
			}
			break;
		}
		return lastBlock;
	}

}
