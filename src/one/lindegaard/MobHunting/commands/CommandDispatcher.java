package one.lindegaard.MobHunting.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.command.TabCompleter;

import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;

/**
 * This allows sub commands to be handled in a clean easily expandable way. Just
 * create a new command that implements ICommand Then register it with
 * registerCommand() in the static constructor
 * <p>
 * Try to keep names and aliases in lowercase
 *
 * @author Schmoller
 */
public class CommandDispatcher implements CommandExecutor, TabCompleter {

	private MobHunting plugin;

	private HashMap<String, ICommand> mCommands;
	private String mRootCommandName;
	private String mRootCommandDescription;

	public CommandDispatcher(MobHunting plugin, String commandName, String description) {

		this.plugin = plugin;

		mRootCommandName = commandName;
		mRootCommandDescription = description;

		mCommands = new HashMap<>();

		registerCommand(new InternalHelp());
	}

	/**
	 * Registers a command to be handled by this dispatcher
	 *
	 * @param command
	 */
	public void registerCommand(ICommand command) {
		mCommands.put(command.getName().toLowerCase(), command);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			displayUsage(sender, label, null);
			return true;
		}

		String subCommand = args[0].toLowerCase();
		String[] subArgs = (args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[0]);

		ICommand com = isSubCommand(subCommand);

		// Was not found
		if (com == null) {
			displayUsage(sender, label, subCommand);
			return true;
		}

		// Check that the sender is correct
		if (!com.canBeConsole()
				&& (sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender)) {
			plugin.getMessages().senderSendMessage(sender, ChatColor.RED + Messages
					.getString("mobhunting.commands.base.noconsole", "command", "/" + label + " " + subCommand));
			return true;
		}
		if (!com.canBeCommandBlock() && sender instanceof BlockCommandSender) {
			plugin.getMessages().senderSendMessage(sender, ChatColor.RED + Messages
					.getString("mobhunting.commands.base.nocommandblock", "command", "/" + label + " " + subCommand));
			return true;
		}

		// Check that they have permission
		if (com.getPermission() != null && !sender.hasPermission(com.getPermission())) {
			plugin.getMessages().senderSendMessage(sender,
					ChatColor.RED + Messages.getString("mobhunting.commands.base.nopermission", "command",
							"/" + label + " " + subCommand, "perm", com.getPermission()));
			return true;
		}

		if (!com.onCommand(sender, subCommand, subArgs)) {
			String[] lines = com.getUsageString(subCommand, sender);
			String usageString = "";

			for (String line : lines) {
				if (lines.length > 1)
					usageString += "\n    ";

				usageString += ChatColor.GRAY + "/" + label + " " + line;
			}

			usageString = ChatColor.RED + Messages.getString("mobhunting.commands.base.usage", "usage", usageString);
			plugin.getMessages().senderSendMessage(sender, usageString);
		}

		return true;
	}

	private void displayUsage(CommandSender sender, String label, String subcommand) {
		String usage = "";

		if (subcommand != null) {
			plugin.getMessages().senderSendMessage(sender,
					ChatColor.RED + Messages.getString("mobhunting.commands.base.unknowncommand", "command",
							ChatColor.RESET + "/" + label + " " + ChatColor.GOLD + subcommand));
			plugin.getMessages().senderSendMessage(sender,
					Messages.getString("mobhunting.commands.base.validcommands"));
		} else {
			plugin.getMessages().senderSendMessage(sender,
					ChatColor.RED + Messages.getString("mobhunting.commands.base.nocommand", "command",
							ChatColor.RESET + "/" + label + ChatColor.GOLD + " <command>"));
			plugin.getMessages().senderSendMessage(sender,
					Messages.getString("mobhunting.commands.base.validcommands"));
		}

		boolean first = true;
		boolean odd = true;
		// Build the list
		for (Entry<String, ICommand> ent : mCommands.entrySet()) {
			if (odd)
				usage += ChatColor.WHITE;
			else
				usage += ChatColor.GRAY;
			odd = !odd;

			if (first)
				usage += ent.getKey();
			else
				usage += ", " + ent.getKey();

			first = false;
		}

		plugin.getMessages().senderSendMessage(sender, usage);

		if (subcommand == null) {
			plugin.getMessages().senderSendMessage(sender, Messages.getString("mobhunting.commands.base.morehelp"));
		}

	}

	public ICommand isSubCommand(String subCommand) {
		if (mCommands.containsKey(subCommand)) {
			return mCommands.get(subCommand);
		} else {
			// Check aliases
			for (Entry<String, ICommand> ent : mCommands.entrySet()) {
				if (ent.getValue().getAliases() != null) {
					String[] aliases = ent.getValue().getAliases();
					for (String alias : aliases) {
						if (subCommand.equalsIgnoreCase(alias)) {
							return ent.getValue();
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		List<String> results = new ArrayList<String>();
		if (args.length == 1) // Tab completing the sub command
		{
			for (ICommand registeredCommand : mCommands.values()) {
				if (registeredCommand.getName().toLowerCase().startsWith(args[0].toLowerCase()))
					results.add(registeredCommand.getName());
			}
		} else {
			// Find the command to use
			String subCommand = args[0].toLowerCase();

			ICommand com = isSubCommand(subCommand);

			// Was not found
			if (com == null) {
				return results;
			}

			// Check that the sender is correct
			if (!com.canBeConsole() && sender instanceof ConsoleCommandSender) {
				return results;
			}

			// Check that they have permission
			if (com.getPermission() != null && !sender.hasPermission(com.getPermission())) {
				return results;
			}

			String[] subArgs = (args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[0]);
			results = com.onTabComplete(sender, subCommand, subArgs);
			if (results == null)
				// return new ArrayList<String>();
				return results;

		}
		return results;
	}

	private class InternalHelp implements ICommand {

		@Override
		public String getName() {
			return "help";
		}

		@Override
		public String[] getAliases() {
			return null;
		}

		@Override
		public String getPermission() {
			return null;
		}

		@Override
		public String[] getUsageString(String label, CommandSender sender) {
			return new String[] { label };
		}

		@Override
		public String getDescription() {
			return Messages.getString("mobhunting.commands.base.help.description");
		}

		@Override
		public boolean canBeConsole() {
			return true;
		}

		@Override
		public boolean canBeCommandBlock() {
			return true;
		}

		@Override
		public boolean onCommand(CommandSender sender, String label, String[] args) {
			if (args.length != 0)
				return false;

			plugin.getMessages().senderSendMessage(sender, ChatColor.GOLD + mRootCommandDescription);
			plugin.getMessages().senderSendMessage(sender,
					ChatColor.GOLD + Messages.getString("mobhunting.commands.base.help.commands"));

			for (ICommand command : mCommands.values()) {
				// Dont show commands that are irrelevant
				if (!command.canBeCommandBlock() && sender instanceof BlockCommandSender)
					continue;
				if (!command.canBeConsole()
						&& (sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender))
					continue;

				if (command.getPermission() != null && !sender.hasPermission(command.getPermission()))
					continue;

				String usageString = "";
				boolean first = true;
				for (String line : command.getUsageString(command.getName(), sender)) {
					if (!first)
						usageString += "\n";

					first = false;

					usageString += ChatColor.GOLD + "/" + mRootCommandName + " " + line;
				}

				plugin.getMessages().senderSendMessage(sender,
						usageString + "\n  " + ChatColor.WHITE + command.getDescription());
			}
			return true;
		}

		@Override
		public List<String> onTabComplete(CommandSender sender, String label, String[] args) {
			return null;
		}

	}
}
