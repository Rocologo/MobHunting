package one.lindegaard.MobHunting.commands;

import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import one.lindegaard.MobHunting.HuntData;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;

public class ClearGrindingCommand implements ICommand {

private MobHunting plugin;
	
	public ClearGrindingCommand(MobHunting plugin) {
		this.plugin=plugin;
	}

	@Override
	public String getName() {
		return "cleargrinding";
	}

	@Override
	public String[] getAliases() {
		return null;
	}

	@Override
	public String getPermission() {
		return "mobhunting.cleargrinding";
	}

	@Override
	public String[] getUsageString(String label, CommandSender sender) {
		return new String[] { ChatColor.GOLD + label };
	}

	@Override
	public String getDescription() {
		return Messages.getString("mobhunting.commands.cleargrinding.description");
	}

	@Override
	public boolean canBeConsole() {
		return false;
	}

	@Override
	public boolean canBeCommandBlock() {
		return false;
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args) {
		if (args.length != 0)
			return false;

		Location loc = ((Player) sender).getLocation();
		MobHunting.getGrindingManager().clearGrindingArea(loc);

		for (Player player : Bukkit.getOnlinePlayers()) {
			HuntData data = new HuntData(player);
			data.clearGrindingArea(loc);
		}

		plugin.getMessages().senderSendMessage(sender,ChatColor.GREEN + Messages.getString("mobhunting.commands.cleargrinding.done"));

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String label, String[] args) {
		return Collections.emptyList();
	}

}
