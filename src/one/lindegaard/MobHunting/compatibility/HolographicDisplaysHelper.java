package one.lindegaard.MobHunting.compatibility;

import org.bukkit.inventory.ItemStack;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.leaderboard.HologramLeaderboard;

public class HolographicDisplaysHelper {

	public static void createHologram(HologramLeaderboard board) {
		HologramsAPI.createHologram(MobHunting.getInstance(), board.getLocation());
	}

	public static void addTextLine(Hologram hologram, String text) {
		hologram.appendTextLine(text);
	}

	public static void editTextLine(Hologram hologram, String text, int i) {
		hologram.insertTextLine(i, text);
	}

	public static void addItemLine(Hologram hologram, ItemStack itemstack) {
		hologram.appendItemLine(new ItemStack(itemstack));
	}

	public static void deleteHologram(Hologram hologram) {
		hologram.delete();
	}

	public static void hideHologram(Hologram hologram) {
		hologram.getVisibilityManager().setVisibleByDefault(true);
	}

}
