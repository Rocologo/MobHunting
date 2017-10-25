package one.lindegaard.MobHunting.bounty;

import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.achievements.AchievementManager;
import one.lindegaard.MobHunting.compatibility.EssentialsCompat;
import one.lindegaard.MobHunting.compatibility.VanishNoPacketCompat;
import one.lindegaard.MobHunting.rewards.CustomItems;
import one.lindegaard.MobHunting.storage.IDataCallback;
import one.lindegaard.MobHunting.storage.UserNotFoundException;
import one.lindegaard.MobHunting.util.Misc;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

import java.util.*;

public class BountyManager implements Listener {

	private MobHunting plugin;

	// mBounties contains all bounties on the OfflinePlayer and the Bounties put
	// on other players
	private Set<Bounty> mOpenBounties = new HashSet<Bounty>();

	public BountyManager(MobHunting plugin) {
		this.plugin = plugin;
		if (MobHunting.getConfigManager().enableRandomBounty) {
			Bukkit.getPluginManager().registerEvents(this, plugin);
			Bukkit.getScheduler().runTaskTimer(plugin, this::createRandomBounty,
					MobHunting.getConfigManager().timeBetweenRandomBounties * 20 * 60,
					MobHunting.getConfigManager().timeBetweenRandomBounties * 20 * 60);
			Bukkit.getScheduler().runTaskTimer(MobHunting.getInstance(), () -> {
				for (Bounty bounty : mOpenBounties) {
					if (bounty.getEndDate() < System.currentTimeMillis() && bounty.isOpen()) {
						bounty.setStatus(BountyStatus.expired);
						MobHunting.getDataStoreManager().updateBounty(bounty);
						Messages.debug("BountyManager: Expired Bounty %s", bounty.toString());
						mOpenBounties.remove(bounty);
					}
				}
			}, 600, 7200);
		}
	}

	public Set<Bounty> getAllBounties() {
		return mOpenBounties;
	}

	public Set<OfflinePlayer> getWantedPlayers() {
		Set<OfflinePlayer> wantedPlayers = new HashSet<>();
		for (Bounty b : mOpenBounties) {
			if (b.isOpen() && !wantedPlayers.contains(b.getWantedPlayer()))
				wantedPlayers.add(b.getWantedPlayer());
		}
		return wantedPlayers;
	}

	public Bounty getOpenBounty(String worldGroup, OfflinePlayer wantedPlayer, OfflinePlayer bountyOwner) {
		for (Bounty bounty : mOpenBounties) {
			if (!bounty.isOpen() || !bounty.getWantedPlayer().equals(wantedPlayer))
				continue;
			return check(worldGroup, wantedPlayer, bountyOwner, bounty);
		}
		return null;
	}

	public Bounty getBounty(String worldGroup, OfflinePlayer wantedPlayer, OfflinePlayer bountyOwner) {
		return mOpenBounties.stream().findFirst().map(bounty -> check(worldGroup, wantedPlayer, bountyOwner, bounty))
				.orElse(null);
	}

	public Bounty check(String worldGroup, OfflinePlayer wantedPlayer, OfflinePlayer bountyOwner, Bounty bounty) {
		if (bounty.getBountyOwner() == null) {
			if (bountyOwner == null) {
				if (bounty.getWantedPlayer().equals(wantedPlayer) && bounty.getWorldGroup().equals(worldGroup)) {
					return bounty;
				}
			}
		} else {
			if (bounty.getBountyOwner().equals(bountyOwner) && bounty.getWantedPlayer().equals(wantedPlayer)
					&& bounty.getWorldGroup().equals(worldGroup)) {
				return bounty;
			}
		}
		return null;
	}

	public Bounty getOpenBounty(Bounty bounty) {
		for (Bounty b : mOpenBounties) {
			if (b.isOpen() && b.equals(bounty))
				return b;
		}
		return null;
	}

	public Set<Bounty> getOpenBounties(String worldGroup, OfflinePlayer wantedPlayer) {
		Set<Bounty> bounties = new HashSet<Bounty>();
		for (Bounty bounty : mOpenBounties) {
			if (bounty.isOpen() && bounty.getWantedPlayer().equals(wantedPlayer)
					&& bounty.getWorldGroup().equals(worldGroup)) {
				bounties.add(bounty);
			}
		}
		return bounties;
	}

	public void sort() {
		Set<Bounty> sortedSet = new TreeSet<>(new BountyComparator()).descendingSet();
		sortedSet.addAll(mOpenBounties);
		mOpenBounties = sortedSet;
	}

	class BountyComparator implements Comparator<Bounty> {
		@Override
		public int compare(Bounty b1, Bounty b2) {
			if (b1.equals(b2))
				return Double.compare(b1.getPrize(), b2.getPrize());
			else if (b1.getWantedPlayer().getName().equals(b2.getWantedPlayer().getName()))
				if (b1.getBountyOwner() == null)
					return -1;
				else if (b2.getBountyOwner() == null)
					return 1;
				else
					return b1.getBountyOwner().getName().compareTo(b2.getBountyOwner().getName());
			else
				return b1.getWantedPlayer().getName().compareTo(b2.getWantedPlayer().getName());
		}
	}

	// Tests
	public boolean hasOpenBounty(String worldGroup, OfflinePlayer wantedPlayer, OfflinePlayer bountyOwner) {
		for (Bounty bounty : mOpenBounties) {
			if (!bounty.isOpen() || !bounty.getWorldGroup().equals(worldGroup)
					|| !bounty.getWantedPlayer().equals(wantedPlayer)) {
				continue;
			}

			if (bounty.getBountyOwner() != null && bountyOwner != null) {
				return bounty.getBountyOwner().equals(bountyOwner);
			} else if (bounty.getBountyOwner() == null && bountyOwner == null) {
				return true;
			}

		}
		return false;

	}

	public boolean hasOpenBounty(Bounty b) {
		for (Bounty bounty : mOpenBounties) {
			if (bounty.isOpen() && bounty.equals(b))
				return true;
		}
		return false;
	}

	public boolean hasOpenBounties(OfflinePlayer wantedPlayer) {
		for (Bounty bounty : mOpenBounties) {
			if (bounty.isOpen() && bounty.getWantedPlayer().equals(wantedPlayer))
				return true;
		}
		return false;
	}

	// ****************************************************************************
	// Events
	// ****************************************************************************

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(final PlayerJoinEvent event) {
		if (MobHunting.getConfigManager().disablePlayerBounties)
			return;

		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			@Override
			public void run() {
				load(event.getPlayer());
			}
		}, (long) 5);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void onPlayerQuit(PlayerQuitEvent event) {
		Set<Bounty> toBeRemoved = new HashSet<Bounty>();
		Iterator<Bounty> itr = getAllBounties().iterator();
		int n = 0;
		while (itr.hasNext()) {
			Bounty bounty = itr.next();
			if (bounty.getWantedPlayer().equals(event.getPlayer())) {
				toBeRemoved.add(bounty);
				n++;
			}
		}
		if (n > 0) {
			mOpenBounties.removeAll(toBeRemoved);
			Messages.debug("%s bounties on %s was removed when player quit", n, event.getPlayer().getName());
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public void onInventoryClick(InventoryClickEvent event) {
		if (ChatColor.stripColor(event.getInventory().getName()).startsWith("MostWanted:")
				|| ChatColor.stripColor(event.getInventory().getName()).startsWith("Wanted:")) {
			event.setCancelled(true);
			event.getWhoClicked().closeInventory();
			inventoryMap.remove(event.getWhoClicked());
		}
	}

	// ****************************************************************************
	// Save & Load
	// ****************************************************************************
	public void load(final Player player) {
		MobHunting.getDataStoreManager().requestBounties(BountyStatus.open, player, new IDataCallback<Set<Bounty>>() {

			@Override
			public void onCompleted(Set<Bounty> data) {
				boolean sort = false;
				int n = 0;
				Iterator<Bounty> itr = data.iterator();
				while (itr.hasNext()) {
					Bounty bounty = itr.next();
					// Check is the Bounty already is in memory
					if (!hasOpenBounty(bounty)) {
						if (bounty.getEndDate() > System.currentTimeMillis() && bounty.isOpen()) {
							mOpenBounties.add(bounty);
							n++;
						} else {
							Messages.debug("BountyManager: Expired onLoad Bounty %s", bounty.toString());
							bounty.setStatus(BountyStatus.expired);
							bounty.setPrize(0);
							MobHunting.getDataStoreManager().updateBounty(bounty);
							delete(bounty);
						}
						sort = true;
					}
				}
				if (sort)
					sort();
				Messages.debug("%s bounties for %s was loaded.", n, player.getName());
				if (n > 0 && hasOpenBounties(player)) {
					plugin.getMessages().playerSendMessage(player,
							Messages.getString("mobhunting.bounty.youarewanted"));
					if (!EssentialsCompat.isVanishedModeEnabled(player)
							&& !VanishNoPacketCompat.isVanishedModeEnabled(player))
						plugin.getMessages().broadcast(
								Messages.getString("mobhunting.bounty.playeriswanted", "playername", player.getName()),
								player);
				}
			}

			@Override
			public void onError(Throwable error) {
				if (error instanceof UserNotFoundException)
					if (player.isOnline()) {
						Player p = player;
						p.sendMessage(Messages.getString("mobhunting.bounty.user-not-found"));
					} else {
						error.printStackTrace();
						if (player.isOnline()) {
							Player p = player;
							p.sendMessage(Messages.getString("mobhunting.bounty.load-fail"));
						}
					}
			}

		});
	}

	/**
	 * put/add a bounty on the set of Bounties.
	 *
	 * @param bounty
	 */
	public void save(Bounty bounty) {
		if (hasOpenBounty(bounty)) {
			Messages.debug("adding bounty %s+%s", getOpenBounty(bounty).getPrize(), bounty.getPrize());
			getOpenBounty(bounty).setPrize(getOpenBounty(bounty).getPrize() + bounty.getPrize());
			getOpenBounty(bounty).setMessage(bounty.getMessage());
			MobHunting.getDataStoreManager().updateBounty(getOpenBounty(bounty));
		} else {
			mOpenBounties.add(bounty);
			MobHunting.getDataStoreManager().updateBounty(bounty);
			Messages.debug("adding bounty %s", getOpenBounty(bounty).getPrize());
		}
	}

	public void cancel(Bounty bounty) {
		Bounty b1 = getOpenBounty(bounty);
		if (b1 != null) {
			b1.setStatus(BountyStatus.canceled);
			b1.setPrize(0);
			MobHunting.getDataStoreManager().updateBounty(b1);
			mOpenBounties.removeIf(b -> b.equals(bounty));
		}
	}

	public void delete(Bounty bounty) {
		Bounty b1 = getOpenBounty(bounty);
		if (b1 != null) {
			b1.setStatus(BountyStatus.deleted);
			b1.setPrize(0);
			MobHunting.getDataStoreManager().updateBounty(b1);
			mOpenBounties.removeIf(b -> b.equals(bounty));
		}
	}

	// *************************************************************************************
	// BOUNTY GUI
	// *************************************************************************************

	private HashMap<CommandSender, Inventory> inventoryMap = new HashMap<CommandSender, Inventory>();

	public void showOpenBounties(CommandSender sender, String worldGroupName, OfflinePlayer wantedPlayer,
			boolean useGui) {
		if (sender instanceof Player) {

			if (hasOpenBounties(wantedPlayer)) {
				Set<Bounty> bountiesOnWantedPlayer = getOpenBounties(worldGroupName, wantedPlayer);
				if (useGui) {
					CustomItems customItems = new CustomItems(plugin);
					final Inventory inventory = Bukkit.createInventory(null, 54,
							ChatColor.BLUE + "" + ChatColor.BOLD + "Wanted:" + wantedPlayer.getName());
					int n = 0;
					for (Bounty bounty : bountiesOnWantedPlayer) {
						if (bounty.isOpen()) {
							if (bounty.getBountyOwner() != null)
								AchievementManager.addInventoryDetails(
										customItems.getPlayerHead(wantedPlayer.getUniqueId(), 1, bounty.getPrize()),
										inventory, n, ChatColor.GREEN + wantedPlayer.getName(),
										new String[] { ChatColor.WHITE + "", Messages.getString(
												"mobhunting.commands.bounty.bounties", "bountyowner",
												bounty.getBountyOwner().getName(), "prize",
												plugin.getRewardManager().format(bounty.getPrize()), "wantedplayer",
												bounty.getWantedPlayer().getName(), "daysleft",
												(bounty.getEndDate() - System.currentTimeMillis()) / (86400000L)) });
							else
								AchievementManager.addInventoryDetails(
										customItems.getPlayerHead(wantedPlayer.getUniqueId(), 1, bounty.getPrize()),
										inventory, n, ChatColor.GREEN + wantedPlayer.getName(),
										new String[] { ChatColor.WHITE + "", Messages.getString(
												"mobhunting.commands.bounty.bounties", "bountyowner", "Random Bounty",
												"prize", plugin.getRewardManager().format(bounty.getPrize()),
												"wantedplayer", bounty.getWantedPlayer().getName(), "daysleft",
												(bounty.getEndDate() - System.currentTimeMillis()) / (86400000L)) });
							if (n < 53)
								n++;
						}
					}
					inventoryMap.put(sender, inventory);
					((Player) sender).openInventory(inventoryMap.get(sender));

				} else {
					sender.sendMessage(Messages.getString("mobhunting.commands.bounty.bounties-header"));
					sender.sendMessage("-----------------------------------");
					for (Bounty bounty : bountiesOnWantedPlayer) {
						if (bounty.isOpen())
							if (bounty.getBountyOwner() != null)
								sender.sendMessage(Messages.getString("mobhunting.commands.bounty.bounties",
										"bountyowner", bounty.getBountyOwner().getName(), "prize",
										plugin.getRewardManager().format(bounty.getPrize()), "wantedplayer",
										bounty.getWantedPlayer().getName(), "daysleft",
										(bounty.getEndDate() - System.currentTimeMillis()) / (86400000L)));
							else
								sender.sendMessage(Messages.getString("mobhunting.commands.bounty.bounties",
										"bountyowner", "Random Bounty", "prize",
										plugin.getRewardManager().format(bounty.getPrize()), "wantedplayer",
										bounty.getWantedPlayer().getName(), "daysleft",
										(bounty.getEndDate() - System.currentTimeMillis()) / (86400000L)));
					}
				}
			} else {
				sender.sendMessage(Messages.getString("mobhunting.commands.bounty.no-bounties-player", "wantedplayer",
						wantedPlayer.getName()));
			}
		} else {
			sender.sendMessage("[MobHunting] You cant use this command in the console");
		}
	}

	public void showMostWanted(CommandSender sender, String worldGroupName, boolean useGui) {
		if (sender instanceof Player) {
			if (!mOpenBounties.isEmpty()) {
				if (useGui) {
					CustomItems customItems = new CustomItems(plugin);
					Inventory inventory = Bukkit.createInventory(null, 54,
							ChatColor.BLUE + "" + ChatColor.BOLD + "MostWanted:");
					int n = 0;
					for (Bounty bounty : mOpenBounties) {
						if (bounty.getBountyOwner() != null)
							AchievementManager.addInventoryDetails(
									customItems.getPlayerHead(bounty.getWantedPlayer().getUniqueId(), 1,
											bounty.getPrize()),
									inventory, n, ChatColor.GREEN + bounty.getWantedPlayer().getName(),
									new String[] { ChatColor.WHITE + "", Messages.getString(
											"mobhunting.commands.bounty.bounties", "bountyowner",
											bounty.getBountyOwner().getName(), "prize",
											plugin.getRewardManager().format(bounty.getPrize()), "wantedplayer",
											bounty.getWantedPlayer().getName(), "daysleft",
											(bounty.getEndDate() - System.currentTimeMillis()) / (86400000L)) });
						else
							AchievementManager.addInventoryDetails(
									customItems.getPlayerHead(bounty.getWantedPlayer().getUniqueId(), 1,
											bounty.getPrize()),
									inventory, n, ChatColor.GREEN + bounty.getWantedPlayer().getName(),
									new String[] { ChatColor.WHITE + "", Messages.getString(
											"mobhunting.commands.bounty.bounties", "bountyowner", "Random Bounty",
											"prize", plugin.getRewardManager().format(bounty.getPrize()),
											"wantedplayer", bounty.getWantedPlayer().getName(), "daysleft",
											(bounty.getEndDate() - System.currentTimeMillis()) / (86400000L)) });
						if (n < 53)
							n++;
					}
					inventoryMap.put(sender, inventory);
					((Player) sender).openInventory(inventoryMap.get(sender));
				} else {
					sender.sendMessage(Messages.getString("mobhunting.commands.bounty.bounties-header"));
					sender.sendMessage("-----------------------------------");
					for (Bounty bounty : mOpenBounties) {
						if (bounty.getBountyOwner() != null)
							sender.sendMessage(Messages.getString("mobhunting.commands.bounty.bounties", "bountyowner",
									bounty.getBountyOwner().getName(), "prize",
									plugin.getRewardManager().format(bounty.getPrize()), "wantedplayer",
									bounty.getWantedPlayer().getName(), "daysleft",
									(bounty.getEndDate() - System.currentTimeMillis()) / (86400000L)));
						else
							sender.sendMessage(Messages.getString("mobhunting.commands.bounty.bounties", "bountyowner",
									"Random Bounty", "prize", plugin.getRewardManager().format(bounty.getPrize()),
									"wantedplayer", bounty.getWantedPlayer().getName(), "daysleft",
									(bounty.getEndDate() - System.currentTimeMillis()) / (86400000L)));
					}
				}
			} else {
				sender.sendMessage(Messages.getString("mobhunting.commands.bounty.no-bounties"));
			}
		} else {
			sender.sendMessage("[MobHunting] You cant use this command in the console");
		}
	}

	// ***********************************************************
	// RANDOM BOUNTY
	// ***********************************************************

	public void createRandomBounty() {
		boolean createBounty = MobHunting.getMobHuntingManager().mRand
				.nextDouble() <= MobHunting.getConfigManager().chanceToCreateBounty;
		if (createBounty) {
			int noOfPlayers = MobHunting.getMobHuntingManager().getOnlinePlayersAmount();
			int noOfPlayersNotVanished = noOfPlayers;
			for (Player player : MobHunting.getMobHuntingManager().getOnlinePlayers()) {
				if (EssentialsCompat.isVanishedModeEnabled(player) || VanishNoPacketCompat.isVanishedModeEnabled(player)
						|| player.hasPermission("mobhunting.bounty.randombounty.exempt"))
					noOfPlayersNotVanished--;
			}
			Player randomPlayer = null;
			if (MobHunting.getConfigManager().minimumNumberOfOnlinePlayers <= noOfPlayersNotVanished) {

				int random = MobHunting.getMobHuntingManager().mRand.nextInt(noOfPlayersNotVanished);
				int n = 0;
				for (Player player : MobHunting.getMobHuntingManager().getOnlinePlayers()) {
					if (n == random && !EssentialsCompat.isVanishedModeEnabled(player)
							&& !VanishNoPacketCompat.isVanishedModeEnabled(player)
							&& !player.hasPermission("mobhunting.bounty.randombounty.exempt")) {
						randomPlayer = player;
						break;
					} else
						n++;
				}
				if (randomPlayer != null) {
					String worldGroup = MobHunting.getWorldGroupManager().getCurrentWorldGroup(randomPlayer);
					Bounty randomBounty = new Bounty(plugin, worldGroup, randomPlayer, Misc.round(
							plugin.getRewardManager().getRandomPrice(MobHunting.getConfigManager().randomBounty)),
							"Random Bounty");
					save(randomBounty);
					for (Player player : MobHunting.getMobHuntingManager().getOnlinePlayers()) {
						if (player.getName().equals(randomPlayer.getName()))
							plugin.getMessages().playerSendMessage(player,
									Messages.getString("mobhunting.bounty.randombounty.self", "prize",
											plugin.getRewardManager().format(randomBounty.getPrize())));
						else
							plugin.getMessages().playerSendMessage(player,
									Messages.getString("mobhunting.bounty.randombounty", "prize",
											plugin.getRewardManager().format(randomBounty.getPrize()), "playername",
											randomPlayer.getName()));
					}
				}
			}
		}
	}

}