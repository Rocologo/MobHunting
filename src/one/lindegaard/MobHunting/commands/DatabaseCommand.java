package one.lindegaard.MobHunting.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import one.lindegaard.CustomItemsLib.Core;
import one.lindegaard.CustomItemsLib.Tools;
import one.lindegaard.CustomItemsLib.storage.DataStoreException;
import one.lindegaard.MobHunting.MobHunting;

public class DatabaseCommand implements ICommand, Listener {

	private MobHunting plugin;

	public DatabaseCommand(MobHunting plugin) {
		this.plugin = plugin;
	}

	@Override
	public String getName() {
		return "database";
	}

	@Override
	public String[] getAliases() {
		return new String[] { "db" };
	}

	@Override
	public String getPermission() {
		return "mobhunting.database";
	}

	@Override
	public String[] getUsageString(String label, CommandSender sender) {
		return new String[] { ChatColor.GOLD + label + ChatColor.GREEN + " fixLeaderboard",
				ChatColor.GOLD + label + ChatColor.GREEN + " convert-to-utf8",
				ChatColor.GOLD + label + ChatColor.GREEN + " reset-achievements" + ChatColor.WHITE
						+ " - delete achievements and start over.",
				ChatColor.GOLD + label + ChatColor.GREEN + " reset-statistics" + ChatColor.WHITE
						+ " - delete statistics and start over.",
				ChatColor.GOLD + label + ChatColor.GREEN + " reset-bounties" + ChatColor.WHITE
						+ " - delete all bounties and start over.",
				ChatColor.GOLD + label + ChatColor.GREEN + " deleteoldplayers" + ChatColor.WHITE
						+ " - delete players not know on this server." };
	}

	@Override
	public String getDescription() {
		return plugin.getMessages().getString("mobhunting.commands.database.description");
	}

	@Override
	public boolean canBeConsole() {
		return true;
	}

	@Override
	public boolean canBeCommandBlock() {
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String label, String[] args) {

		ArrayList<String> items = new ArrayList<String>();
		if (args.length == 1) {
			items.add("fixLeaderboard");
			items.add("convert-to-utf8");
			items.add("reset-achievements");
			items.add("reset-statistics");
			items.add("reset-bounties");
			items.add("deleteoldplayers");
			// items.add("backup");
			// items.add("restore");
			// items.add("deletebackup");
		}
		if (!args[args.length - 1].trim().isEmpty()) {
			String match = args[args.length - 1].trim().toLowerCase();
			items.removeIf(name -> !name.toLowerCase().startsWith(match));
		}
		return items;
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args) {
		if (args.length == 0)
			return false;
		if (args.length == 1 && (args[0].equalsIgnoreCase("fixleaderboard"))) {
			try {
				plugin.getStoreManager().databaseFixLeaderboard();
			} catch (DataStoreException e) {
				e.printStackTrace();
			}
			return true;
		} else if (args.length == 1 && args[0].equalsIgnoreCase("reset-statistics")) {
			try {
				plugin.getStoreManager().resetStatistics();
			} catch (DataStoreException e) {
				e.printStackTrace();
			}
			return true;

		} else if (args[0].equalsIgnoreCase("reset-bounties")) {
			try {
				plugin.getStoreManager().resetBounties();
				plugin.getBountyManager().deleteAllBounties();
				for (Player player : Tools.getOnlinePlayers())
					plugin.getBountyManager().load(player);
			} catch (DataStoreException e) {
				e.printStackTrace();
			}
			return true;

		} else if (args[0].equalsIgnoreCase("reset-achievements")) {
			try {
				plugin.getStoreManager().resetAchievements();
				plugin.getAchievementManager().deleteAllAchivements();
				for (Player player : Tools.getOnlinePlayers())
					plugin.getAchievementManager().load(player);
			} catch (DataStoreException e) {
				e.printStackTrace();
			}
			return true;

		} else if (args.length == 2 && (args[0].equalsIgnoreCase("convert-to-utf8"))) {
			String database_name = args[1];
			try {
				plugin.getStoreManager().databaseConvertToUtf8(database_name);
			} catch (DataStoreException e) {
				e.printStackTrace();
			}
			return true;
		} else if (args.length == 1 && (args[0].equalsIgnoreCase("deleteoldplayers"))) {
			try {
				plugin.getStoreManager().databaseDeleteOldPlayers();
				Core.getStoreManager().databaseDeleteOldPlayers();
			} catch (DataStoreException e) {
				e.printStackTrace();
			}
			return true;
		} else if (args.length == 1 && args[0].equalsIgnoreCase("backup")) {
			// TODO: create a backup
			plugin.getMessages().senderSendMessage(sender, "Backup feature is not implemented yet.");
			return true;
		} else if (args.length == 1 && args[0].equalsIgnoreCase("restore")) {
			// TODO: restore a backup
			plugin.getMessages().senderSendMessage(sender, "Restore feature is not implemented yet.");
			return true;
		} else if (args.length == 1 && args[0].equalsIgnoreCase("deletebackup")) {
			// TODO: restore a backup
			plugin.getMessages().senderSendMessage(sender, "Deletebackup feature is not implemented yet.");
			return true;
		}
		return false;
	}
}
