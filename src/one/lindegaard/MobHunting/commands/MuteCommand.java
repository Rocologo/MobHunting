package one.lindegaard.MobHunting.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.storage.DataStoreManager;
import one.lindegaard.MobHunting.storage.PlayerSettings;

public class MuteCommand implements ICommand {

	private MobHunting plugin;

	public MuteCommand(MobHunting plugin) {
		this.plugin = plugin;
	}

	// Used case
	// /mh mute - No args, args.length = 0 || arg[0]=""

	@Override
	public String getName() {
		return "mute";
	}

	@Override
	public String[] getAliases() {
		return new String[] { "silent", "notify" };
	}

	@Override
	public String getPermission() {
		return "mobhunting.mute";
	}

	@Override
	public String[] getUsageString(String label, CommandSender sender) {
		return new String[] { ChatColor.GOLD + label + ChatColor.WHITE + " - to mute/unmute.",
				ChatColor.GOLD + label + ChatColor.GREEN + " <playername>" + ChatColor.WHITE
						+ " - to mute/unmute a the notifications for a specific player." };
	}

	@Override
	public String getDescription() {
		return Messages.getString("mobhunting.commands.mute.description");
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
	public List<String> onTabComplete(CommandSender sender, String label, String[] args) {
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args) {

		if (args.length == 0) {
			togglePlayerMuteMode((Player) sender);
			return true;
		} else if (args.length == 1) {
			MobHunting.getInstance();
			DataStoreManager ds = MobHunting.getDataStoreManager();
			Player player = (Player) ds.getPlayerByName(args[0]);
			if (player != null) {
				if (sender.hasPermission("mobhunting.mute.other") || sender instanceof ConsoleCommandSender) {
					togglePlayerMuteMode(player);
				} else {
					plugin.getMessages().senderSendMessage(sender,
							ChatColor.RED + "You dont have permission " + ChatColor.AQUA + "'mobhunting.mute.other'");
				}
				return true;
			} else {
				plugin.getMessages().senderSendMessage(sender,ChatColor.RED + "Player " + args[0] + " is not online.");
				return false;
			}
		}
		return false;
	}

	private void togglePlayerMuteMode(Player player) {
		DataStoreManager ds = MobHunting.getDataStoreManager();
		PlayerSettings ps = plugin.getPlayerSettingsmanager().getPlayerSettings(player);
		boolean lm = ps.isLearningMode();
		if (ps.isMuted()) {
			ds.updatePlayerSettings(player, lm, false, ps.getBalance(), ps.getBalanceChanges(), ps.getBankBalance(),
					ps.getBankBalanceChanges());
			plugin.getPlayerSettingsmanager().setPlayerSettings(player, new PlayerSettings(player, lm, false,
					ps.getBalance(), ps.getBalanceChanges(), ps.getBankBalance(), ps.getBankBalanceChanges()));
			player.sendMessage(Messages.getString("mobhunting.commands.mute.unmuted", "player", player.getName()));
		} else {
			ds.updatePlayerSettings(player, lm, true, ps.getBalance(), ps.getBalanceChanges(), ps.getBankBalance(),
					ps.getBankBalanceChanges());
			plugin.getPlayerSettingsmanager().setPlayerSettings(player, new PlayerSettings(player, lm, true,
					ps.getBalance(), ps.getBalanceChanges(), ps.getBankBalance(), ps.getBankBalanceChanges()));
			player.sendMessage(Messages.getString("mobhunting.commands.mute.muted", "player", player.getName()));
		}
	}
}
