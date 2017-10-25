package one.lindegaard.MobHunting;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.sainttx.holograms.api.Hologram;

import one.lindegaard.MobHunting.compatibility.HologramsCompat;
import one.lindegaard.MobHunting.compatibility.HologramsHelper;
import one.lindegaard.MobHunting.compatibility.HolographicDisplaysCompat;
import one.lindegaard.MobHunting.compatibility.HolographicDisplaysHelper;
import one.lindegaard.MobHunting.leaderboard.HologramLeaderboard;

public class HologramManager {

	private MobHunting plugin;

	private HashMap<String, HologramLeaderboard> holograms = new HashMap<>();

	public HologramManager(MobHunting plugin) {
		this.plugin = plugin;
	}

	public HashMap<String, HologramLeaderboard> getHolograms() {
		return holograms;
	}

	public void createHologramLeaderboard(HologramLeaderboard hologramLeaderboard) {
		holograms.put(hologramLeaderboard.getHologramName(), hologramLeaderboard);
		if (HologramsCompat.isSupported())
			HologramsHelper.createHologram(hologramLeaderboard);
		else if (HolographicDisplaysCompat.isSupported())
			HolographicDisplaysHelper.createHologram(hologramLeaderboard);
		hologramLeaderboard.update();
	}

	public void deleteHolographicLeaderboard(String hologramName) {
		if (HologramsCompat.isSupported()) {
			Hologram hologram = HologramsCompat.getHologramManager().getHologram(hologramName);
			HologramsCompat.getHologramManager().deleteHologram(hologram);
		} else if (HolographicDisplaysCompat.isSupported()) {
			for (com.gmail.filoghost.holographicdisplays.api.Hologram hologram : HologramsAPI.getHolograms(plugin)) {
				if (hologram.getLocation().equals(holograms.get(hologramName).getLocation())) {
					HolographicDisplaysHelper.deleteHologram(hologram);
					break;
				}
			}
		}
		holograms.remove(hologramName);
	}

	public String listHolographicLeaderboard() {
		String str = "";
		if (HologramsCompat.isSupported() || HolographicDisplaysCompat.isSupported()) {
			if (holograms.size() == 0) {
				str = Messages.getString("mobhunting.holograms.no-holograms");
			} else {
				str = "Holograms: ";
				for (String hologramName : holograms.keySet()) {
					str = str + hologramName + ", ";
				}
				str = str.substring(0, str.length() - 2);
			}
		}
		return str;
	}

	public void updateHolographicLeaderboard(String hologramName) {
		if (HologramsCompat.isSupported() || HolographicDisplaysCompat.isSupported()) {
			holograms.get(hologramName).update();
		}
	}

	// *******************************************************************
	// HOLOGRAM LEADERBOARDS
	// *******************************************************************
	private YamlConfiguration hologramConfig = new YamlConfiguration();
	private File hologramFile = new File(MobHunting.getInstance().getDataFolder(), "hologram-leaderboards.yml");

	public void deleteHologramLeaderboard(String hologramName) throws IllegalArgumentException {

		deleteHolographicLeaderboard(hologramName);
		hologramConfig.set(hologramName, null);

		try {
			hologramConfig.save(hologramFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void saveHologramLeaderboards() {
		hologramConfig.options().header("Always make a backup before changing this file.\n"
				+ "The format of the Holographic Leaderboards can be changed using the Java String.format() syntax.\n"
				+ "(https://docs.oracle.com/javase/8/docs/api/java/util/Formatter.html).\n"
				+ "The colors can be changed using '§' and the normal Minecraft color codes. (http://ess.khhq.net/mc/)\n"
				+ "If you make a wrong format you can always delete the formatting lines and restart the server.");

		for (HologramLeaderboard board : getHolograms().values()) {
			ConfigurationSection section = hologramConfig.createSection(board.getHologramName());
			board.write(section);
		}

		try {
			hologramConfig.save(hologramFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void saveHologramLeaderboard(String hologramName) {
		hologramConfig.options().header("Always make a backup before changing this file.\n"
				+ "The format of the Holographic Leaderboards can be changed using the Java String.format() syntax.\n"
				+ "(https://docs.oracle.com/javase/8/docs/api/java/util/Formatter.html).\n"
				+ "The colors can be changed using '§' and the normal Minecraft color codes. (http://ess.khhq.net/mc/)\n"
				+ "If you make a wrong format you can always delete the formatting lines and restart the server.");

		ConfigurationSection section = hologramConfig.createSection(hologramName);
		HologramLeaderboard board = getHolograms().get(hologramName);
		board.write(section);

		try {
			hologramConfig.save(hologramFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void loadHologramLeaderboards() {

		if (!hologramFile.exists())
			return;

		try {
			hologramConfig.load(hologramFile);
		} catch (IOException | InvalidConfigurationException e) {
			Bukkit.getConsoleSender()
					.sendMessage(ChatColor.RED + "Could not read Hologram Leaderboard file: hologram-leaderboards.yml");
			if (MobHunting.getConfigManager().killDebug)
				e.printStackTrace();
		}

		Iterator<String> keys = hologramConfig.getKeys(false).iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			ConfigurationSection section = hologramConfig.getConfigurationSection(key);
			HologramLeaderboard board = new HologramLeaderboard(plugin);
			try {
				board.read(section);
				createHologramLeaderboard(board);
			} catch (InvalidConfigurationException e) {
				Bukkit.getConsoleSender().sendMessage(ChatColor.RED + e.getMessage());
			}
		}

		if (getHolograms().size() > 0)
			Messages.debug("%s Holographic Leaderboards loaded", getHolograms().size());

	}

	public void loadHologramLeaderboard(String hologramName) {

		if (!hologramFile.exists())
			return;

		try {
			hologramConfig.load(hologramFile);
		} catch (IOException | InvalidConfigurationException e) {
			Bukkit.getConsoleSender()
					.sendMessage(ChatColor.RED + "Could not read Hologram Leaderboard file: hologram-leaderboards.yml");
			if (MobHunting.getConfigManager().killDebug)
				e.printStackTrace();
		}

		ConfigurationSection section = hologramConfig.getConfigurationSection(hologramName);
		HologramLeaderboard board = new HologramLeaderboard(plugin);
		try {
			board.read(section);
			createHologramLeaderboard(board);
		} catch (InvalidConfigurationException e) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + e.getMessage());
		}

		Messages.debug("The Holographic Leaderboard '%s' was loaded from file.", hologramName);

	}

}
