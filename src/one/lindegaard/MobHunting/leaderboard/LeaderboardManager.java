package one.lindegaard.MobHunting.leaderboard;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.collect.HashMultimap;

import one.lindegaard.MobHunting.HologramManager;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.StatType;
import one.lindegaard.MobHunting.storage.StatStore;
import one.lindegaard.MobHunting.storage.TimePeriod;

public class LeaderboardManager implements Listener {

	private MobHunting plugin;
	private Set<LegacyLeaderboard> mLegacyLeaderboards = new HashSet<LegacyLeaderboard>();
	private HashMultimap<World, WorldLeaderboard> mLeaderboards = HashMultimap.create();
	private HologramManager hologramManager;
	private HashMap<String, LegacyLeaderboard> mLegacyNameMap = new HashMap<String, LegacyLeaderboard>();
	private BukkitTask mUpdater = null;

	public LeaderboardManager(MobHunting instance) {
		this.plugin = instance;
		int leaderboardUpdatePeriod = MobHunting.getConfigManager().leaderboardUpdatePeriod;
		if (leaderboardUpdatePeriod < 1200) {
			leaderboardUpdatePeriod = 1200;
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED
					+ "[MobHunting][Warning] leaderboard-update-period: in your config.yml is too low. Please raise it to 1200 or higher. Reccommended is 6000. ");
			MobHunting.getConfigManager().leaderboardUpdatePeriod=1200;
			MobHunting.getConfigManager().saveConfig();
		}
		mUpdater = Bukkit.getScheduler().runTaskTimer(MobHunting.getInstance(), new Updater(), 120L,
				leaderboardUpdatePeriod);

		loadLegacyboards();

		for (World world : Bukkit.getWorlds())
			loadWorld(world);

		hologramManager = new HologramManager(plugin);
		hologramManager.loadHologramLeaderboards();
		hologramManager.saveHologramLeaderboards();

		Bukkit.getPluginManager().registerEvents(this, MobHunting.getInstance());
	}

	private class Updater implements Runnable {
		@Override
		public void run() {
			for (LegacyLeaderboard board : mLegacyLeaderboards)
				board.updateBoard();

			for (WorldLeaderboard board : mLeaderboards.values())
				board.update();

			for (HologramLeaderboard board : hologramManager.getHolograms().values())
				board.update();

			Messages.debug("Refreshed %s leaderboards.",
					hologramManager.getHolograms().size() + mLegacyLeaderboards.size() + mLeaderboards.size());
		}
	}

	public LegacyLeaderboard getLeaderboard(String id) {
		return mLegacyNameMap.get(id.toLowerCase());
	}

	public void shutdown() {
		mUpdater.cancel();
	}

	public HologramManager getHologramManager() {
		return hologramManager;
	}

	// *******************************************************************
	// LEADERBOARDS
	// *******************************************************************
	public void createLeaderboard(Location location, BlockFace facing, StatType[] type, TimePeriod[] period,
			boolean horizontal, int width, int height) throws IllegalArgumentException {
		WorldLeaderboard board = new WorldLeaderboard(plugin, location, facing, width, height, horizontal, type,
				period);
		if (!board.isSpaceAvailable())
			throw new IllegalArgumentException("There is not enough room for the signs.");

		mLeaderboards.put(location.getWorld(), board);
		board.update();
		saveWorld(location.getWorld());
	}

	public WorldLeaderboard getLeaderboardAt(Location location) {
		for (WorldLeaderboard board : mLeaderboards.get(location.getWorld())) {
			if (board.getLocation().equals(location))
				return board;
		}

		return null;
	}

	public HashMultimap<World, WorldLeaderboard> getWorldLeaderBoards() {
		return mLeaderboards;
	}

	// *******************************************************************
	// LEGACYLEADERBOARDS
	// *******************************************************************
	public void deleteLegacyLeaderboard(String id) throws IllegalArgumentException {
		if (!mLegacyNameMap.containsKey(id.toLowerCase()))
			throw new IllegalArgumentException(Messages.getString("leaderboard.notexists", "leaderboard", id));

		mLegacyLeaderboards.remove(mLegacyNameMap.remove(id.toLowerCase()));
		saveLegacyBoards();
	}

	public Set<LegacyLeaderboard> getAllLegacyBoards() {
		return Collections.unmodifiableSet(mLegacyLeaderboards);
	}

	public void saveLegacyBoards() {
		try {
			YamlConfiguration config = new YamlConfiguration();
			config.options().header(
					"This file is automatically generated. Do NOT edit this file manually or you risk losing all leaderboards if you mistype something.");

			ArrayList<Object> key = new ArrayList<Object>();

			for (LegacyLeaderboard leaderboard : mLegacyLeaderboards) {
				key.add(leaderboard.write());
			}

			config.set("boards", key);

			config.save(new File(MobHunting.getInstance().getDataFolder(), "boards.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private void loadLegacyboards() {
		try {
			File file = new File(MobHunting.getInstance().getDataFolder(), "boards.yml");

			if (!file.exists())
				return;

			YamlConfiguration config = new YamlConfiguration();
			config.load(file);

			List<Object> boards = (List<Object>) config.getList("boards");

			if (boards == null)
				return;

			mLegacyLeaderboards.clear();

			for (Object board : boards) {
				if (!(board instanceof Map))
					continue;

				LegacyLeaderboard leaderboard = new LegacyLeaderboard();
				leaderboard.read((Map<String, Object>) board);
				mLegacyLeaderboards.add(leaderboard);
				mLegacyNameMap.put(leaderboard.getId(), leaderboard);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}

	// *******************************************************************
	// WORLDLEADERBOARDS
	// *******************************************************************
	private void loadWorld(World world) {
		File file = new File(MobHunting.getInstance().getDataFolder(), "boards-" + world.getName() + ".yml");
		if (!file.exists())
			return;

		YamlConfiguration config = new YamlConfiguration();
		try {
			config.load(file);
		} catch (IOException | InvalidConfigurationException e) {
			Bukkit.getConsoleSender().sendMessage(
					ChatColor.RED + "Could not read world leaderboard file: boards-" + world.getName() + ".yml");
			if (MobHunting.getConfigManager().killDebug)
				e.printStackTrace();
		}

		Iterator<String> keys = config.getKeys(false).iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			ConfigurationSection section = config.getConfigurationSection(key);
			WorldLeaderboard board = new WorldLeaderboard(plugin);
			try {
				board.read(section);
				board.update();
				board.refresh();
				mLeaderboards.put(world, board);
			} catch (InvalidConfigurationException e) {
				Bukkit.getConsoleSender().sendMessage(ChatColor.RED + e.getMessage());
			}
		}

		if (mLeaderboards.size() > 0)
			Messages.debug("%s Leaderboards in '%s' loaded from file: %s!", mLeaderboards.size(), world.getName(),
					MobHunting.getInstance().getDataFolder(), "boards-" + world.getName() + ".yml");

	}

	public void saveWorld(World world) {
		File file = new File(MobHunting.getInstance().getDataFolder(), "boards-" + world.getName() + ".yml");
		YamlConfiguration config = new YamlConfiguration();
		config.options().header(
				"This file is automatically generated. Do NOT edit this file manually or you risk losing all leaderboards if you mistype something.");

		int i = 0;
		for (WorldLeaderboard board : mLeaderboards.get(world)) {
			ConfigurationSection section = config.createSection(String.valueOf(i++));
			board.save(section);
		}
		Messages.debug("Leaderboards saved to file: %s!", MobHunting.getInstance().getDataFolder(),
				"boards-" + world.getName() + ".yml");

		try {
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// *******************************************************************
	// Listeners
	// *******************************************************************

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onWorldLoad(WorldLoadEvent event) {
		loadWorld(event.getWorld());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onWorldUnload(WorldUnloadEvent event) {
		mLeaderboards.removeAll(event.getWorld());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onChunkLoad(ChunkLoadEvent event) {
		for (WorldLeaderboard board : mLeaderboards.get(event.getWorld())) {
			if (board.isInChunk(event.getChunk())) {
				board.refresh();
			}

		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	private void onLeaderboardClick(PlayerInteractEvent event) {
		if (!event.hasBlock())
			return;

		Block block = event.getClickedBlock();

		if (block.getType() != Material.WALL_SIGN)
			return;

		for (WorldLeaderboard board : mLeaderboards.get(block.getWorld())) {
			if (board.isInBounds(block.getLocation())) {
				if (block.getLocation().equals(board.getLocation())) {
					// Main sign
					// TODO: What should be the effect of clicking the main
					// sign?
					board.update();
					board.refresh();
					return;
				} else {
					int place = 1;

					for (Block sign : board.getSignBlocks()) {
						if (sign.equals(block)) {
							if (place <= board.getCurrentStats().size()) {
								StatStore stat = board.getCurrentStats().get(place - 1);
								event.getPlayer().sendMessage(ChatColor.GOLD + ChatColor.BOLD.toString()
										+ String.valueOf(place) + ". " + ChatColor.YELLOW + stat.getPlayer().getName());
								event.getPlayer().sendMessage(ChatColor.WHITE + " " + String.valueOf(stat.getAmount())
										+ " " + ChatColor.GRAY + stat.getType().translateName());
							}

							if (place + 1 <= board.getCurrentStats().size()) {
								StatStore stat = board.getCurrentStats().get(place);
								event.getPlayer().sendMessage(
										ChatColor.GOLD + ChatColor.BOLD.toString() + String.valueOf(place + 1) + ". "
												+ ChatColor.YELLOW + stat.getPlayer().getName());
								event.getPlayer().sendMessage(ChatColor.WHITE + " " + String.valueOf(stat.getAmount())
										+ " " + ChatColor.GRAY + stat.getType().translateName());
							}
							return;
						}
						place += 2;
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	private void onBlockBreak(BlockBreakEvent event) {
		Block block = event.getBlock();

		if (block.getType() != Material.WALL_SIGN) {
			if (block.getRelative(BlockFace.NORTH).getType() != Material.WALL_SIGN
					&& block.getRelative(BlockFace.SOUTH).getType() != Material.WALL_SIGN
					&& block.getRelative(BlockFace.EAST).getType() != Material.WALL_SIGN
					&& block.getRelative(BlockFace.WEST).getType() != Material.WALL_SIGN)
				// return if the sign is not a leaderboard
				return;
		}

		for (WorldLeaderboard board : mLeaderboards.get(block.getWorld())) {
			if (board.isInBounds(block.getLocation())) {

				if (event.getPlayer().hasPermission("mobhunting.leaderboard")
						&& block.getLocation().equals(board.getLocation()))
					// Allow the block to be broken if player has permission to
					return;

				if (block.getType() == Material.WALL_SIGN)
					event.setCancelled(true);
				else if (block.getRelative(board.getFacing()).getType() == Material.WALL_SIGN)
					event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onBlockBreakFinal(BlockBreakEvent event) {
		Block block = event.getBlock();
		if (block.getType() != Material.WALL_SIGN || !event.getPlayer().hasPermission("mobhunting.leaderboard"))
			return;

		// TODO: break does not remove the signs??????
		for (WorldLeaderboard board : mLeaderboards.get(block.getWorld())) {
			if (block.getLocation().equals(board.getLocation())) {
				board.removeSigns();
				mLeaderboards.remove(block.getWorld(), board);
				saveWorld(board.getWorld());
				Messages.debug("Leaderboard removed: %s", block.getLocation().toString());
				return;
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	private void onBlockPiston(BlockPistonExtendEvent event) {
		for (Block block : event.getBlocks()) {
			if (block.getType() != Material.WALL_SIGN) {
				if (block.getRelative(BlockFace.NORTH).getType() != Material.WALL_SIGN
						&& block.getRelative(BlockFace.SOUTH).getType() != Material.WALL_SIGN
						&& block.getRelative(BlockFace.EAST).getType() != Material.WALL_SIGN
						&& block.getRelative(BlockFace.WEST).getType() != Material.WALL_SIGN)
					continue;
			}

			for (WorldLeaderboard board : mLeaderboards.get(block.getWorld())) {
				if (board.isInBounds(block.getLocation())) {
					if (block.getType() == Material.WALL_SIGN)
						event.setCancelled(true);
					else if (block.getRelative(board.getFacing()).getType() == Material.WALL_SIGN)
						event.setCancelled(true);
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	private void onBlockPiston(BlockPistonRetractEvent event) {
		if (event.isSticky()) {
			Block block = event.getRetractLocation().getBlock();
			if (block.getType() != Material.WALL_SIGN) {
				if (block.getRelative(BlockFace.NORTH).getType() != Material.WALL_SIGN
						&& block.getRelative(BlockFace.SOUTH).getType() != Material.WALL_SIGN
						&& block.getRelative(BlockFace.EAST).getType() != Material.WALL_SIGN
						&& block.getRelative(BlockFace.WEST).getType() != Material.WALL_SIGN)
					return;
			}

			for (WorldLeaderboard board : mLeaderboards.get(block.getWorld())) {
				if (board.isInBounds(block.getLocation())) {
					if (block.getType() == Material.WALL_SIGN)
						event.setCancelled(true);
					else if (block.getRelative(board.getFacing()).getType() == Material.WALL_SIGN)
						event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	private void onBlockBurn(BlockBurnEvent event) {
		Block block = event.getBlock();
		if (block.getType() != Material.WALL_SIGN) {
			if (block.getRelative(BlockFace.NORTH).getType() != Material.WALL_SIGN
					&& block.getRelative(BlockFace.SOUTH).getType() != Material.WALL_SIGN
					&& block.getRelative(BlockFace.EAST).getType() != Material.WALL_SIGN
					&& block.getRelative(BlockFace.WEST).getType() != Material.WALL_SIGN)
				return;
		}

		for (WorldLeaderboard board : mLeaderboards.get(block.getWorld())) {
			if (board.isInBounds(block.getLocation())) {
				if (block.getType() == Material.WALL_SIGN)
					event.setCancelled(true);
				else if (block.getRelative(board.getFacing()).getType() == Material.WALL_SIGN)
					event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	private void onBlockExplode(EntityExplodeEvent event) {
		for (Block block : event.blockList()) {
			if (block.getType() != Material.WALL_SIGN) {
				if (block.getRelative(BlockFace.NORTH).getType() != Material.WALL_SIGN
						&& block.getRelative(BlockFace.SOUTH).getType() != Material.WALL_SIGN
						&& block.getRelative(BlockFace.EAST).getType() != Material.WALL_SIGN
						&& block.getRelative(BlockFace.WEST).getType() != Material.WALL_SIGN)
					continue;
			}

			for (WorldLeaderboard board : mLeaderboards.get(block.getWorld())) {
				if (board.isInBounds(block.getLocation())) {
					if (block.getType() == Material.WALL_SIGN)
						event.setCancelled(true);
					else if (block.getRelative(board.getFacing()).getType() == Material.WALL_SIGN)
						event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	private void onBlockPickup(EntityChangeBlockEvent event) {
		if (!event.getTo().isSolid()) {
			Block block = event.getBlock();
			if (block.getType() != Material.WALL_SIGN) {
				if (block.getRelative(BlockFace.NORTH).getType() != Material.WALL_SIGN
						&& block.getRelative(BlockFace.SOUTH).getType() != Material.WALL_SIGN
						&& block.getRelative(BlockFace.EAST).getType() != Material.WALL_SIGN
						&& block.getRelative(BlockFace.WEST).getType() != Material.WALL_SIGN)
					return;
			}

			for (WorldLeaderboard board : mLeaderboards.get(block.getWorld())) {
				if (board.isInBounds(block.getLocation())) {
					if (block.getType() == Material.WALL_SIGN)
						event.setCancelled(true);
					else if (block.getRelative(board.getFacing()).getType() == Material.WALL_SIGN)
						event.setCancelled(true);
				}
			}
		}
	}

}
