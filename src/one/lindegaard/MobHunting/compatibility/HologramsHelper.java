package one.lindegaard.MobHunting.compatibility;

import org.bukkit.inventory.ItemStack;
import com.sainttx.holograms.api.Hologram;
import com.sainttx.holograms.api.line.ItemLine;
import com.sainttx.holograms.api.line.TextLine;

import one.lindegaard.MobHunting.leaderboard.HologramLeaderboard;

public class HologramsHelper {

	public static void createHologram(HologramLeaderboard board) {
		HologramsCompat.getHologramManager().addActiveHologram(new Hologram(board.getHologramName(), board.getLocation())); 
	}

	public static void addTextLine(Hologram hologram, String text) {
		hologram.addLine(new TextLine(hologram, text));
	}

	public static void removeLine(Hologram hologram, int i) {
		hologram.removeLine(hologram.getLine(i));
	}

	public static void editTextLine(Hologram hologram, String text, int i) {
		if (hologram.getLines().size()>i)
			hologram.getLines().remove(i);
		hologram.addLine(new TextLine(hologram, text), i);
	}

	public static void addItemLine(Hologram hologram, ItemStack itemstack) {
		hologram.addLine(new ItemLine(hologram, itemstack));
	}

	public static void deleteHologram(Hologram hologram) {
		HologramsCompat.getHologramManager().deleteHologram(hologram);
	}

	public static void hideHologram(Hologram hologram) {
		hologram.despawn();
		HologramsCompat.getHologramManager().removeActiveHologram(hologram);
	}

}
