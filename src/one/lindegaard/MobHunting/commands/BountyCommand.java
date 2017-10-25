package one.lindegaard.MobHunting.commands;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.bounty.Bounty;
import one.lindegaard.MobHunting.storage.UserNotFoundException;
import one.lindegaard.MobHunting.util.Misc;

public class BountyCommand implements ICommand {

	private MobHunting plugin;

	public BountyCommand(MobHunting plugin) {
		this.plugin = plugin;
	}

	// Used case
	// /mh bounty [gui|nogui]- to show a list of player with a bounty
	// /mh bounty <player> - to check if there is a bounty on <player>
	// /mh bounty <player> <prize> - to put a prize on the player
	// /mh bounty remove <wantedplayer> - to remove a bounty from player
	// /mh bounty remove <wantedplayer> <bountyowner> - to remove a bounty on
	// wantedplayer created by bountyowner
	// /mh bounty list <#page> - to make list of the most wanted players = /mh
	// bounty
	// /mh bounty help - to get help to see this list
	// /mh bounty locate <player> - to get chunk(?)/last known place
	// /mh bounty top <#page>- show the most active bounty hunters

	@Override
	public String getName() {
		return "bounty";
	}

	@Override
	public String[] getAliases() {
		return new String[] { "headhunt", "prize" };
	}

	@Override
	public String getPermission() {
		return "mobhunting.bounty";
	}

	@Override
	public String[] getUsageString(String label, CommandSender sender) {
		return new String[] {
				ChatColor.GOLD + label + ChatColor.YELLOW + " [gui|nogui]" + ChatColor.WHITE
						+ " - to show open bounties on your head.",
				ChatColor.GOLD + label + ChatColor.YELLOW + " <player> [gui|nogui]" + ChatColor.GREEN
						+ " - to check if there is a bounty on <player>",
				ChatColor.GOLD + label + ChatColor.GREEN + " <player> <prize>" + ChatColor.YELLOW + " <message>"
						+ ChatColor.WHITE + " - put a bounty on <player> and deliver the message when killed.",
				ChatColor.GOLD + label + ChatColor.GREEN + " remove <player> " + ChatColor.WHITE
						+ " - to remove bounty on <player> with a " + MobHunting.getConfigManager().bountyReturnPct
						+ "% reduction",
				ChatColor.GOLD + label + ChatColor.GREEN + " remove <wantedplayer> <bountyowner>" + ChatColor.WHITE
						+ " - to remove bounty on <player> with a " + MobHunting.getConfigManager().bountyReturnPct
						+ "% reduction" };
	}

	@Override
	public String getDescription() {
		return Messages.getString("mobhunting.commands.bounty.description");
	}

	@Override
	public boolean canBeConsole() {
		return false;
	}

	@Override
	public boolean canBeCommandBlock() {
		return true;
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args) {
		if (sender instanceof ConsoleCommandSender) {
			plugin.getMessages().senderSendMessage(sender,"You can't use /mh bounty from the console");
			return true;
		}
		@SuppressWarnings("deprecation")
		OfflinePlayer bountyOwner = Bukkit.getOfflinePlayer(sender.getName());
		String worldGroupName = MobHunting.getWorldGroupManager().getCurrentWorldGroup((Player) bountyOwner);
		if (args.length == 0) {

			// /mh bounty
			// Show list of Bounties on all wantedPlayers
			plugin.getBountyManager().showMostWanted(sender, worldGroupName,
					MobHunting.getConfigManager().useGuiForBounties);
			return true;

		} else if (args.length == 1) {

			// /mh bounty help
			// Show help
			if (args[0].equalsIgnoreCase("help"))
				return false;

			// /mh bounty [gui|nogui]
			if (args[0].equalsIgnoreCase("gui") || args[0].equalsIgnoreCase("nogui")) {
				plugin.getBountyManager().showMostWanted(sender, worldGroupName, args[0].equalsIgnoreCase("gui"));
				return true;
			}

			// /mh bounty <player>
			// Show list of Bounties on for player <player>
			// check if args[0] is a known playername
			@SuppressWarnings("deprecation")
			OfflinePlayer wantedPlayer = Bukkit.getOfflinePlayer(args[0]);
			if (wantedPlayer != null) {
				plugin.getBountyManager().showOpenBounties(sender, worldGroupName, wantedPlayer,
						MobHunting.getConfigManager().useGuiForBounties);
				return true;
			} else {
				plugin.getMessages().senderSendMessage(sender,
						Messages.getString("mobhunting.commands.bounty.unknown-player", "wantedplayer", args[0]));
				return true;
			}

		} else if (args.length == 2 && (args[1].equalsIgnoreCase("gui") || args[1].equalsIgnoreCase("nogui"))) {
			// /mh bounty <player>
			// Show list of Bounties on for player <player> [gui|nogui]
			// check if args[0] is a known playername

			@SuppressWarnings("deprecation")
			OfflinePlayer wantedPlayer = Bukkit.getOfflinePlayer(args[0]);
			if (wantedPlayer != null) {
				plugin.getBountyManager().showOpenBounties(sender, worldGroupName, wantedPlayer,
						args[1].equalsIgnoreCase("gui"));
				return true;
			} else {
				plugin.getMessages().senderSendMessage(sender,
						Messages.getString("mobhunting.commands.bounty.unknown-player", "wantedplayer", args[0]));
				return true;
			}

		} else if (args.length == 2 && args[0].equalsIgnoreCase("locate")) {

			// /mh bounty locate <player> - to locate the area on the player
			// TODO: locate not implemented yet.
			return true;

		} else if (args.length == 2 && args[0].equalsIgnoreCase("top")) {

			// /mh bounty drop <player> - to show the most active headhuners
			// TODO: locate not implemented yet.
			return true;

		} else if (args[1].matches("\\d+(\\.\\d+)?")) {

			// /mh bounty <player> <prize> <message>
			// put or add a bounty on player
			@SuppressWarnings("deprecation")
			OfflinePlayer wantedPlayer = Bukkit.getOfflinePlayer(args[0]);
			int playerId;
			try {
				playerId = MobHunting.getDataStoreManager().getPlayerId(wantedPlayer);
			} catch (UserNotFoundException e) {
				plugin.getMessages().senderSendMessage(sender,Messages.getString("mobhunting.commands.bounty.unknown-player", "wantedplayer",
						wantedPlayer.getName()));
				return true;
			}
			if (wantedPlayer != null && playerId != 0) {
				double prize = Misc.round(Double.valueOf(args[1]));
				if (!plugin.getRewardManager().has(bountyOwner, prize)) {
					plugin.getMessages().senderSendMessage(sender,Messages.getString("mobhunting.commands.bounty.no-money", "money",
							plugin.getRewardManager().format(prize)));
					return true;
				}
				if (prize <= 0)
					return false;
				String message = "";
				for (int i = 2; i < args.length; i++) {
					message = message + args[i] + " ";
				}
				Bounty bounty;
				bounty = new Bounty(plugin, worldGroupName, bountyOwner, wantedPlayer, prize, message);
				if (plugin.getBountyManager().hasOpenBounty(bounty)) {
					plugin.getMessages().senderSendMessage(sender,Messages.getString("mobhunting.commands.bounty.bounty-added", "wantedplayer",
							wantedPlayer.getName()));
				} else {
					plugin.getMessages().senderSendMessage(sender,Messages.getString("mobhunting.commands.bounty.bounty", "money",
							plugin.getRewardManager().format(prize), "wantedplayer", wantedPlayer.getName()));
				}

				plugin.getBountyManager().save(bounty);
				plugin.getRewardManager().withdrawPlayer(bountyOwner, prize);
				plugin.getMessages().senderSendMessage(sender,Messages.getString("mobhunting.commands.bounty.money-withdrawn", "money",
						plugin.getRewardManager().format(prize)));

				Messages.debug("%s has put %s on %s with the message %s", bountyOwner.getName(),
						plugin.getRewardManager().format(prize), wantedPlayer.getName(), message);
				return true;
			} else {
				plugin.getMessages().senderSendMessage(sender,
						Messages.getString("mobhunting.commands.bounty.unknown-player", "wantedplayer", args[0]));
				return true;
			}
		} else if (args.length >= 2 && (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("drop"))) {
			// /mh bounty drop <player> - to drop the bounty on the <player>
			// /mh bounty drop <player> <bountyOwnwer> - to remove
			// <bountyOwner>'s bounty on <player>. Permission
			// mobhunting.bounty.admin needed.

			OfflinePlayer wantedPlayer = Bukkit.getOfflinePlayer(args[1]);
			if (wantedPlayer == null) {
				plugin.getMessages().senderSendMessage(sender,
						Messages.getString("mobhunting.commands.bounty.unknown-player", "wantedplayer", args[1]));
				return true;
			}

			if (args.length == 2) {
				if (plugin.getBountyManager().hasOpenBounty(worldGroupName, wantedPlayer, bountyOwner)) {
					Bounty bounty = plugin.getBountyManager().getOpenBounty(worldGroupName, wantedPlayer, bountyOwner);
					int pct = MobHunting.getConfigManager().bountyReturnPct;
					plugin.getRewardManager().depositPlayer(bountyOwner, bounty.getPrize() * pct / 100);
					plugin.getBountyManager().cancel(bounty);
					plugin.getMessages().senderSendMessage(sender,Messages.getString("mobhunting.commands.bounty.bounty-removed", "wantedplayer",
							wantedPlayer.getName(), "money", String.format("%.2f", bounty.getPrize() * pct / 100)));
					return true;
				} else {
					if (sender.hasPermission("mobhunting.bounty.admin")) {
						if (plugin.getBountyManager().hasOpenBounty(worldGroupName, wantedPlayer, null)) {
							Bounty bounty = plugin.getBountyManager().getOpenBounty(worldGroupName, wantedPlayer, null);
							plugin.getBountyManager().cancel(bounty);
							plugin.getMessages().senderSendMessage(sender,Messages.getString("mobhunting.bounty.randombounty.removed.admin",
									"playername", wantedPlayer.getName()));
							if (wantedPlayer.isOnline() && !wantedPlayer.equals(bountyOwner))
								((Player) wantedPlayer)
										.sendMessage(Messages.getString("mobhunting.bounty.randombounty.removed.player",
												"adminname", sender.getName()));
							Messages.debug("%s removed the Random Bounty from %s", sender.getName(),
									wantedPlayer.getName());
							return true;
						} else {
							plugin.getMessages().senderSendMessage(sender,Messages.getString("mobhunting.commands.bounty.no-randombounty",
									"playername", wantedPlayer.getName()));
							return true;
						}
					} else {
						plugin.getMessages().senderSendMessage(sender,Messages.getString("mobhunting.commands.bounty.no-permission", "perm",
								"mobhunting.bounty.admin"));
						return true;
					}
					//plugin.getMessages().senderSendMessage(sender,Messages.getString("mobhunting.commands.bounty.no-bounties-player",
					//		"wantedplayer", wantedPlayer.getName()));
					//return true;
				}
			} else if (args.length == 3 && sender.hasPermission("mobhunting.bounty.admin")) {
				OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
				if (offlinePlayer != null)
					bountyOwner = offlinePlayer;
				else {
					plugin.getMessages().senderSendMessage(sender,Messages.getString("mobhunting.commands.bounty.unknown-bountyowner",
							"bountyowner", args[2]));
					return true;
				}
				if (plugin.getBountyManager().hasOpenBounty(worldGroupName, wantedPlayer, bountyOwner)) {
					Bounty bounty = plugin.getBountyManager().getOpenBounty(worldGroupName, wantedPlayer, bountyOwner);
					plugin.getBountyManager().cancel(bounty);
					plugin.getMessages().senderSendMessage(sender,Messages.getString("mobhunting.commands.bounty.bounty-removed-admin",
							"wantedplayer", wantedPlayer.getName(), "bountyowner", bountyOwner.getName(), "money",
							String.format("%.2f", bounty.getPrize())));
					return true;
				} else {
					plugin.getMessages().senderSendMessage(sender,Messages.getString("mobhunting.commands.bounty.no-bounties-player-admin",
							"wantedplayer", wantedPlayer.getName(), "bountyowner", bountyOwner.getName()));
					return true;
				}
			} else {
				plugin.getMessages().senderSendMessage(sender,Messages.getString("mobhunting.commands.bounty.no-permission2", "permission",
						"mobhunting.bounty.admin", "bountyowner", bountyOwner.getName()));
				return true;
			}
		}
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String label, String[] args) {
		ArrayList<String> items = new ArrayList<String>();
		if (args.length == 1) {
			if (items.isEmpty()) {
				items.add("remove");
				items.add("top");
				items.add("gui");
			}
			String partial = args[0].toLowerCase();
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (player.getName().toLowerCase().startsWith(partial))
					items.add(player.getName());
			}
		} else if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
			String partial = args[1].toLowerCase();
			for (OfflinePlayer wantedPlayer : plugin.getBountyManager().getWantedPlayers()) {
				if (wantedPlayer.getName().toLowerCase().startsWith(partial))
					items.add(wantedPlayer.getName());
			}
		}

		if (!args[args.length - 1].trim().isEmpty()) {
			String match = args[args.length - 1].trim().toLowerCase();
			items.removeIf(name -> !name.toLowerCase().startsWith(match));
		}
		return items;
	}
}
