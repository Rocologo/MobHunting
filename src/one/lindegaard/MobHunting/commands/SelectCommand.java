package one.lindegaard.MobHunting.commands;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.SelectionHelper;

public class SelectCommand implements ICommand {
	
	private MobHunting plugin;

	public SelectCommand(MobHunting plugin) {
		this.plugin=plugin;
	}

	@Override
	public String getName() {
		return "select";
	}

	@Override
	public String[] getAliases() {
		return new String[] { "sel" };
	}

	@Override
	public String getPermission() {
		return "mobhunting.select";
	}

	@Override
	public String[] getUsageString(String label, CommandSender sender) {
		return new String[] { ChatColor.GOLD + label + ChatColor.GREEN + " (1|2)" };
	}

	@Override
	public String getDescription() {
		return Messages.getString("mobhunting.commands.select.description");
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
		if (args.length != 1)
			return false;

		Player player = (Player) sender;

		boolean pointA = false;
		if (args[0].equalsIgnoreCase("1"))
			pointA = true;
		else if (!args[0].equalsIgnoreCase("2")) {
			plugin.getMessages().senderSendMessage(sender,
					ChatColor.RED + Messages.getString("mobhunting.commands.select.unknown-point", "point", args[0]));
			return true;
		}

		Set<Material> transparent = new HashSet<Material>();
		transparent.add(Material.AIR);

		Block target = player.getTargetBlock(transparent, 10);
		if (target == null) {
			plugin.getMessages().senderSendMessage(sender,ChatColor.RED + Messages.getString("mobhunting.commands.select.too-far"));
			return true;
		}

		if (pointA)
			SelectionHelper.setPointA(player, target.getLocation());
		else
			SelectionHelper.setPointB(player, target.getLocation());

		plugin.getMessages().senderSendMessage(sender,ChatColor.GREEN + Messages.getString("mobhunting.commands.select.done", "point", args[0],
				"coords", String.format("%d, %d, %d", target.getX(), target.getY(), target.getZ())));
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String label, String[] args) {
		return null;
	}

}
