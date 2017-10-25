package one.lindegaard.MobHunting.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.update.Updater;
import one.lindegaard.MobHunting.update.UpdateStatus;

public class VersionCommand implements ICommand {

	private MobHunting plugin;
	
	public VersionCommand(MobHunting plugin) {
		this.plugin=plugin;
	}

	@Override
	public String getName() {
		return "version";
	}

	@Override
	public String[] getAliases() {
		return new String[] { "ver", "-v" };
	}

	@Override
	public String getPermission() {
		return "mobhunting.version";
	}

	@Override
	public String[] getUsageString(String label, CommandSender sender) {
		return new String[] { ChatColor.GOLD + label + ChatColor.GREEN + " version"
				+ ChatColor.WHITE + " - to get the version number" };
	}

	@Override
	public String getDescription() {
		return Messages.getString("mobhunting.commands.version.description");
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
	public boolean onCommand(CommandSender sender, String label, String[] args) {

		plugin.getMessages().senderSendMessage(sender,ChatColor.GREEN
				+ Messages.getString(
						"mobhunting.commands.version.currentversion","currentversion",
						MobHunting.getInstance().getDescription().getVersion()));
		if (Updater.getUpdateAvailable() == UpdateStatus.AVAILABLE)
			plugin.getMessages().senderSendMessage(sender,ChatColor.GREEN
					+ Messages.getString(
							"mobhunting.commands.version.newversion","newversion",
							Updater.getBukkitUpdate().getVersionName()));
		if (sender.hasPermission("mobhunting.update")) {
			Updater.pluginUpdateCheck(sender, true, true);
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String label,
			String[] args) {
		return null;
	}

}
