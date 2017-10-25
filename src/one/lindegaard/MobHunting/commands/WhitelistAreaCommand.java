package one.lindegaard.MobHunting.commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.compatibility.ProtocolLibHelper;
import one.lindegaard.MobHunting.grinding.Area;

public class WhitelistAreaCommand implements ICommand {

	private MobHunting plugin;

	public WhitelistAreaCommand(MobHunting plugin) {
		this.plugin=plugin;
	}

	@Override
	public String getName() {
		return "whitelistarea";
	}

	@Override
	public String[] getAliases() {
		return null;
	}

	@Override
	public String getPermission() {
		return "mobhunting.whitelist";
	}

	@Override
	public String[] getUsageString(String label, CommandSender sender) {
		return new String[] { ChatColor.GOLD + label + ChatColor.GREEN + " [add|remove]" + ChatColor.WHITE
				+ " - to whitelist an area." };
	}

	@Override
	public String getDescription() {
		return Messages.getString("mobhunting.commands.whitelistarea.description");
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
		Location loc = ((Player) sender).getLocation();

		if (args.length == 0) {
			if (MobHunting.getGrindingManager().isWhitelisted(loc)) {
				plugin.getMessages().senderSendMessage(sender,
						ChatColor.GREEN + Messages.getString("mobhunting.commands.whitelistarea.iswhitelisted"));
				Area area = MobHunting.getGrindingManager().getWhitelistArea(loc);
				ProtocolLibHelper.showGrindingArea((Player) sender, area, loc);
			} else
				plugin.getMessages().senderSendMessage(sender,
						ChatColor.RED + Messages.getString("mobhunting.commands.whitelistarea.notwhitelisted"));
		} else if (args.length == 1) {
			if (args[0].equalsIgnoreCase("remove")) {
				MobHunting.getGrindingManager().unWhitelistArea(loc);
				plugin.getMessages().senderSendMessage(sender,
						ChatColor.GREEN + Messages.getString("mobhunting.commands.whitelistarea.remove.done"));
			} else if (args[0].equalsIgnoreCase("add")) {
				Area area = new Area(loc, MobHunting.getConfigManager().grindingDetectionRange, 0);
				MobHunting.getGrindingManager().whitelistArea(area);
				plugin.getMessages().senderSendMessage(sender,ChatColor.GREEN + Messages.getString("mobhunting.commands.whitelistarea.done"));
				ProtocolLibHelper.showGrindingArea((Player) sender, area, loc);
			} else
				return false;
		} else
			return false;

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String label, String[] args) {
		ArrayList<String> items = new ArrayList<String>();
		if (args.length == 1) {
			if (items.isEmpty()) {
				items.add("add");
				items.add("remove");
				items.add("");
			}
		}

		if (!args[args.length - 1].trim().isEmpty()) {
			String match = args[args.length - 1].trim().toLowerCase();

			Iterator<String> it = items.iterator();
			while (it.hasNext()) {
				String name = it.next();
				if (!name.toLowerCase().startsWith(match))
					it.remove();
			}
		}
		return items;
	}

}
