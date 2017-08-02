package one.lindegaard.MobHunting.bounty;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;

/**
 * @author Rocologo
 *
 */
public class WorldGroup {

	private static File file = new File(MobHunting.getInstance().getDataFolder(), "worldGroups.yml");
	private static YamlConfiguration config = new YamlConfiguration();
	private static HashMap<String, List<String>> worldGroups = new HashMap<String, List<String>>();

	public WorldGroup() {
		if (worldGroups.isEmpty()) {
			worldGroups.put("DefaultGroup", Arrays.asList("world", "world_nether", "world_the_end"));
			worldGroups.put("CreativeGroup", Collections.singletonList("creative"));
			worldGroups.put("SurvivalGroup", Collections.singletonList("survival"));
		}
	}

	public void add(String world) {
		List<String> list = worldGroups.get("Default");
		if (!list.contains(world))
			list.add(world);
		worldGroups.put("Default", list);
	}

	public void add(String world, String worldGroup) {
		List<String> list = worldGroups.get(worldGroup);
		if (!list.contains(world))
			list.add(world);
		worldGroups.put(worldGroup, list);
	}

	public List<String> getWorlds() {
		return worldGroups.get("Default");
	}

	public List<String> getWorlds(String worldGroup) {
		return worldGroups.get(worldGroup);
	}

	public static String getWorldGroup(String world) {
		for (String worldGroup : worldGroups.keySet()) {
			if (worldGroup.contains(world))
				return worldGroup;
		}
		return "Default";
	}

	public String getCurrentWorldGroup(Player player) {
		return getWorldGroup(player.getWorld().getName());
	}
	
	// ***************************************************************
	// write & read
	// ***************************************************************
	public void save() {
		try {
			config.options()
					.header("----------------------------------------------------------"
							+ "\nWorldGroups. New world are added in the Default Group"
							+ "\n----------------------------------------------------------"
							+ "\nThese worldgroups arer only use for Player Bounties. An"
							+ "\nbounty created in one worldgroup can not be claimed in"
							+ "\nanother worldgroup. This is to make sure that economies"
							+ "\nare not mixed.");
			Messages.debug("Saving worldGroups");
			ConfigurationSection section = config.createSection("WorldGroups");
			section.set("WorldGroups", worldGroups);
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void load() {
		if (!file.exists())
			return;
		Messages.debug("Loading WorldGroups");
		try {
			config.load(file);
			ConfigurationSection section = config.getConfigurationSection("WorldGroups");
			if (section != null)
				section.get("WorldGroups", worldGroups);
		} catch (IllegalStateException | InvalidConfigurationException | IOException e) {
			e.printStackTrace();
		}

	}

}
