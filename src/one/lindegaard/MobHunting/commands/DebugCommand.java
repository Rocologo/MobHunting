package one.lindegaard.MobHunting.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;

public class DebugCommand implements ICommand {

private MobHunting plugin;
	
	public DebugCommand(MobHunting plugin) {
		this.plugin=plugin;
	}

	// Used case
	// /mh debug - args.length = 0 || arg[0]=""

	@Override
	public String getName() {
		return "debug";
	}

	@Override
	public String[] getAliases() {
		return new String[] { "debugmode" };
	}

	@Override
	public String getPermission() {
		return "mobhunting.debug";
	}

	@Override
	public String[] getUsageString(String label, CommandSender sender) {
		return new String[] { ChatColor.GOLD + label + ChatColor.WHITE + " - to enable/disable debugmode." };
	}

	@Override
	public String getDescription() {
		return Messages.getString("mobhunting.commands.debug.description");
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
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args) {
		if (args.length == 0) {
			toggledebugMode(sender);
			return true;
		}
		return false;
	}

	private void toggledebugMode(CommandSender sender) {
		boolean debug = MobHunting.getConfigManager().killDebug;
		if (debug) {
			MobHunting.getConfigManager().killDebug = false;
			plugin.getMessages().senderSendMessage(sender,"[MobHunting] " + Messages.getString("mobhunting.commands.debug.disabled"));
			MobHunting.getConfigManager().saveConfig();
		} else {
			MobHunting.getConfigManager().killDebug = true;
			plugin.getMessages().senderSendMessage(sender,"[MobHunting] " + Messages.getString("mobhunting.commands.debug.enabled"));
			MobHunting.getConfigManager().saveConfig();
		}

	}

}
