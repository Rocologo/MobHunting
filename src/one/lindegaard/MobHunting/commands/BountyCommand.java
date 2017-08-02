package one.lindegaard.MobHunting.commands;

import java.util.ArrayList;
import java.util.Iterator;
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
import one.lindegaard.MobHunting.bounty.BountyManager;
import one.lindegaard.MobHunting.storage.UserNotFoundException;

public class BountyCommand implements ICommand {

	public BountyCommand() {
	}

	// Used case
	// /mh bounty [gui|nogui]- to show a list of player with a bounty
	// /mh bounty <player> - to check if there is a bounty on <player>
	// /mh bounty <player> <prize> - to put a prize on the player
	// /mh bounty remove <player> - to remove a bounty from player
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
			sender.sendMessage("You can't use /mh bounty from the console");
			return true;
		}
		@SuppressWarnings("deprecation")
		OfflinePlayer bountyOwner = Bukkit.getOfflinePlayer(sender.getName());
		String worldGroupName = MobHunting.getWorldGroupManager().getCurrentWorldGroup((Player) bountyOwner);
		if (args.length == 0) {

			// /mh bounty
			// Show list of Bounties on all wantedPlayers
			BountyManager.showMostWanted(sender, worldGroupName, MobHunting.getConfigManager().useGuiForBounties);
			return true;

		} else if (args.length == 1) {

			// /mh bounty help
			// Show help
			if (args[0].equalsIgnoreCase("help"))
				return false;

			// /mh bounty [gui|nogui]
			if (args[0].equalsIgnoreCase("gui") || args[0].equalsIgnoreCase("nogui")) {
				BountyManager.showMostWanted(sender, worldGroupName, args[0].equalsIgnoreCase("gui"));
				return true;
			}

			// /mh bounty <player>
			// Show list of Bounties on for player <player>
			// check if args[0] is a known playername
			@SuppressWarnings("deprecation")
			OfflinePlayer wantedPlayer = Bukkit.getOfflinePlayer(args[0]);
			if (wantedPlayer != null) {
				BountyManager.showOpenBounties(sender, worldGroupName, wantedPlayer,
						MobHunting.getConfigManager().useGuiForBounties);
				return true;
			} else {
				sender.sendMessage(
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
				BountyManager.showOpenBounties(sender, worldGroupName, wantedPlayer, args[1].equalsIgnoreCase("gui"));
				return true;
			} else {
				sender.sendMessage(
						Messages.getString("mobhunting.commands.bounty.unknown-player", "wantedplayer", args[0]));
				return true;
			}

		} else if (args.length == 2 && (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("drop"))) {
			// /mh bounty drop <player> - to drop the bounty on the player
			// Remove a bounty on player <player>
			@SuppressWarnings("deprecation")
			OfflinePlayer wantedPlayer = Bukkit.getOfflinePlayer(args[1]);
			if (wantedPlayer != null) {
				if (MobHunting.getBountyManager().hasOpenBounty(worldGroupName, wantedPlayer, bountyOwner)) {
					Bounty bounty = MobHunting.getBountyManager().getOpenBounty(worldGroupName, wantedPlayer,
							bountyOwner);
					//bounty.setStatus(BountyStatus.canceled);
					MobHunting.getBountyManager().cancel(bounty);
					int pct = MobHunting.getConfigManager().bountyReturnPct;
					MobHunting.getRewardManager().depositPlayer(bountyOwner, bounty.getPrize() * pct / 100);
					sender.sendMessage(Messages.getString("mobhunting.commands.bounty.bounty-removed", "wantedplayer",
							wantedPlayer.getName(), "money", String.format("%.2f", bounty.getPrize() * pct / 100)));
					return true;
				} else {
					sender.sendMessage(Messages.getString("mobhunting.commands.bounty.no-bounties-player",
							"wantedplayer", wantedPlayer.getName()));
					return true;
				}
			} else {
				sender.sendMessage(
						Messages.getString("mobhunting.commands.bounty.unknown-player", "wantedplayer", args[1]));
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
				sender.sendMessage(Messages.getString("mobhunting.commands.bounty.unknown-player", "wantedplayer",
						wantedPlayer.getName()));
				return true;
			}
			if (wantedPlayer != null && playerId != 0) {
				// TODO: allow this agin
				// if (bountyOwner.equals(wantedPlayer)) {
				// sender.sendMessage(Messages.getString("mobhunting.commands.bounty.no-bounty-on-yourself"));
				// return true;
				// }
				double prize = Double.valueOf(args[1]);
				if (!MobHunting.getRewardManager().has(bountyOwner, prize)) {
					sender.sendMessage(Messages.getString("mobhunting.commands.bounty.no-money", "money", prize));
					return true;
				}
				if (prize <= 0)
					return false;
				String message = "";
				for (int i = 2; i < args.length; i++) {
					message = message + args[i] + " ";
				}
				// Messages.debug("args[0]=%s,args[1]=%s,args[2-]=%s",
				// args[0], args[1], message);
				Bounty bounty;
				bounty = new Bounty(worldGroupName, bountyOwner, wantedPlayer, prize, message);
				if (MobHunting.getBountyManager().hasOpenBounty(worldGroupName, wantedPlayer, bountyOwner)) {
					sender.sendMessage(Messages.getString("mobhunting.commands.bounty.bounty-added", "wantedplayer",
							wantedPlayer.getName()));
				} else {
					sender.sendMessage(Messages.getString("mobhunting.commands.bounty.bounty", "money", prize,
							"wantedplayer", wantedPlayer.getName()));
				}

				MobHunting.getBountyManager().save(bounty);
				MobHunting.getRewardManager().withdrawPlayer(bountyOwner, prize);
				sender.sendMessage(Messages.getString("mobhunting.commands.bounty.money-withdrawn", "money", prize));

				Messages.debug("%s has put %s on %s with the message %s", bountyOwner.getName(), prize,
						wantedPlayer.getName(), message);
				return true;
			} else {
				sender.sendMessage(
						Messages.getString("mobhunting.commands.bounty.unknown-player", "wantedplayer", args[0]));
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
			for (OfflinePlayer wantedPlayer : MobHunting.getBountyManager().getWantedPlayers()) {
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
