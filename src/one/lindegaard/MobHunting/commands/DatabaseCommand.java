package one.lindegaard.MobHunting.commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;

import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.storage.DataStoreException;

public class DatabaseCommand implements ICommand, Listener {

	// private IDataStore mStore;

	// private DataStoreManager mStoreManager;

	public DatabaseCommand() {
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
				ChatColor.GOLD + label + ChatColor.GREEN + " convert-to-utf8"};
	}

	@Override
	public String getDescription() {
		return Messages.getString("mobhunting.commands.database.description");
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
			// items.add("reset-statictics");
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
				MobHunting.getStoreManager().databaseFixLeaderboard();
			} catch (DataStoreException e) {
				e.printStackTrace();
			}
			return true;
		} else if (args.length == 2 && (args[0].equalsIgnoreCase("convert-to-utf8"))) {
			String database_name = args[1];
			try {
				MobHunting.getStoreManager().databaseConvertToUtf8(database_name);
			} catch (DataStoreException e) {
				e.printStackTrace();
			}
			return true;
		} else if (args.length == 1 && args[0].equalsIgnoreCase("backup")) {
			// TODO: create a backup
			sender.sendMessage("Backup feature is not implemented yet.");
			return true;
		} else if (args.length == 1 && args[0].equalsIgnoreCase("restore")) {
			// TODO: restore a backup
			sender.sendMessage("Restore feature is not implemented yet.");
			return true;
		} else if (args.length == 1 && args[0].equalsIgnoreCase("deletebackup")) {
			// TODO: restore a backup
			sender.sendMessage("Deletebackup feature is not implemented yet.");
			return true;
		}
		return false;
	}
}
