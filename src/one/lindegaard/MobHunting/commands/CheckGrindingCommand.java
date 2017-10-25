package one.lindegaard.MobHunting.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import one.lindegaard.MobHunting.HuntData;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.compatibility.ProtocolLibHelper;
import one.lindegaard.MobHunting.grinding.Area;

public class CheckGrindingCommand implements ICommand {

private MobHunting plugin;
	
	public CheckGrindingCommand(MobHunting plugin) {
		this.plugin=plugin;
	}

	@Override
	public String getName() {
		return "checkgrinding";
	}

	@Override
	public String[] getAliases() {
		return new String[] { "isgrinding", "grinding", "checkarea" };
	}

	@Override
	public String getPermission() {
		return "mobhunting.checkgrinding";
	}

	@Override
	public String[] getUsageString(String label, CommandSender sender) {
		return new String[] { ChatColor.GOLD + label };
	}

	@Override
	public String getDescription() {
		return Messages.getString("mobhunting.commands.grinding.description");
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

		if (MobHunting.getGrindingManager().isWhitelisted(loc)) {
			plugin.getMessages().senderSendMessage(sender,ChatColor.RED + Messages.getString("mobhunting.commands.grinding.whitelisted"));
			Area area = MobHunting.getGrindingManager().getWhitelistArea(loc);
			ProtocolLibHelper.showGrindingArea((Player) sender, area, loc);
		} else if (MobHunting.getGrindingManager().isGrindingArea(loc)) {
			plugin.getMessages().senderSendMessage(sender,ChatColor.RED + Messages.getString("mobhunting.commands.grinding.blacklisted"));
			Area area = MobHunting.getGrindingManager().getGrindingArea(loc);
			ProtocolLibHelper.showGrindingArea((Player) sender, area, loc);
		} else {
			Area area = null;
			ArrayList<Player> players = new ArrayList<Player>();
			for (Player player : Bukkit.getOnlinePlayers()) {
				HuntData data = new HuntData(player);
				area = data.getPlayerSpecificGrindingArea(loc);
				if (area != null)
					players.add(player);
			}

			if (players.isEmpty())
				plugin.getMessages().senderSendMessage(sender,ChatColor.GREEN + Messages.getString("mobhunting.commands.grinding.not-grinding"));
			else {
				String playerList = "";

				for (Player player : players) {
					if (!playerList.isEmpty())
						playerList += ", ";

					playerList += player.getName();
				}

				plugin.getMessages().senderSendMessage(sender,ChatColor.RED
						+ Messages.getString("mobhunting.commands.grinding.player-grinding", "players", playerList));
				ProtocolLibHelper.showGrindingArea((Player) sender, area, loc);
			}
		}

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String label, String[] args) {
		return null;
	}

}
