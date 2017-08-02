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

public class AchievementsCommand implements ICommand {

	@Override
	public String getName() {
		return "achievements";
	}

	@Override
	public String[] getAliases() {
		return new String[] { "listachievements", "specialkills", "kills" };
	}

	@Override
	public String getPermission() {
		return "mobhunting.listachievements";
	}

	@Override
	public String[] getUsageString(String label, CommandSender sender) {
		if (sender instanceof ConsoleCommandSender)
			return new String[] { ChatColor.GOLD + label + ChatColor.GREEN + " <player>" };
		else {
			if (sender.hasPermission("mobhunting.listachievements.other"))
				return new String[] { ChatColor.GOLD + label + ChatColor.GREEN + " [<player>] [nogui|gui]" };
			else
				return new String[] { ChatColor.GOLD + label + ChatColor.GREEN + " [nogui|gui]" };
		}
	}

	@Override
	public String getDescription() {
		return Messages.getString("mobhunting.commands.listachievements.description");
	}

	@Override
	public boolean canBeConsole() {
		return true;
	}

	@Override
	public boolean canBeCommandBlock() {
		return false;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(final CommandSender sender, String label, String[] args) {
		if (args.length > 2)
			return false;

		if (sender instanceof ConsoleCommandSender && args.length != 1)
			return false;

		OfflinePlayer player = null;

		if (sender instanceof Player)
			player = (Player) sender;

		final boolean self = (player == sender);

		if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
			sender.sendMessage("list all archivement descriptions");
			MobHunting.getAchievementManager().listAllAchievements(sender);

		} else if (args.length == 1 && (args[0].equalsIgnoreCase("nogui") || args[0].equalsIgnoreCase("gui"))) {
			MobHunting.getAchievementManager().showAllAchievements((Player) player, player, args[0].equalsIgnoreCase("gui"),
					self);
		} else {

			OfflinePlayer otherPlayer;
			if (args.length == 1)
				if (!sender.hasPermission("mobhunting.listachievements.other"))
					return false;

			if (args.length == 1
					|| (args.length == 2 && (args[1].equalsIgnoreCase("nogui") || args[1].equalsIgnoreCase("gui")))) {
				String name = args[0];
				otherPlayer = Bukkit.getOfflinePlayer(name);

				if (otherPlayer == null)
					otherPlayer = MobHunting.getDataStoreManager().getPlayerByName(name);

				if (otherPlayer == null) {
					sender.sendMessage(ChatColor.RED
							+ Messages.getString("mobhunting.commands.listachievements.player-not-exist"));
					return true;
				}

				if (args.length == 2 && (args[1].equalsIgnoreCase("nogui") || args[1].equalsIgnoreCase("gui")))
					MobHunting.getAchievementManager().showAllAchievements(sender, otherPlayer,
							args[1].equalsIgnoreCase("gui"), self);
				else if (sender instanceof ConsoleCommandSender)
					MobHunting.getAchievementManager().showAllAchievements(sender, otherPlayer, false, self);
				else
					MobHunting.getAchievementManager().showAllAchievements(sender, otherPlayer,
							MobHunting.getConfigManager().useGuiForAchievements, self);
			} else {
				MobHunting.getAchievementManager().showAllAchievements(sender, player,
						MobHunting.getConfigManager().useGuiForAchievements, self);
			}
		}

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String label, String[] args) {
		if (!sender.hasPermission("mobhunting.listachievements.other"))
			return null;

		if (args.length == 0)
			return null;

		String partial = args[0].toLowerCase();

		ArrayList<String> names = new ArrayList<>();
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player.getName().toLowerCase().startsWith(partial))
				names.add(player.getName());
		}

		return names;
	}

}
