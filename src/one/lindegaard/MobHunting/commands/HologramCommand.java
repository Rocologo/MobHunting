package one.lindegaard.MobHunting.commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.StatType;
import one.lindegaard.MobHunting.compatibility.CitizensCompat;
import one.lindegaard.MobHunting.compatibility.CompatibilityManager;
import one.lindegaard.MobHunting.leaderboard.HologramLeaderboard;
import one.lindegaard.MobHunting.storage.TimePeriod;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class HologramCommand implements ICommand, Listener {

	private MobHunting plugin;

	public HologramCommand(MobHunting plugin) {
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	// Used case (???)
	// /mh hologram create hologramName <stat type> <period> <number>
	// /mh hologram remove hologramName
	// /mh hologram update hologramName
	// /mh hologram list

	@Override
	public String getName() {
		return "hologram";
	}

	@Override
	public String[] getAliases() {
		return new String[] { "hg", "holographicdisplay", "holograms" };
	}

	@Override
	public String getPermission() {
		return "mobhunting.hologram";
	}

	@Override
	public String[] getUsageString(String label, CommandSender sender) {
		return new String[] {
				ChatColor.GOLD + label + ChatColor.GREEN + " create <hologramidName> <stattype> <period> <number>"
						+ ChatColor.WHITE + " - to create a Holographic Leadaderboard",
				ChatColor.GOLD + label + ChatColor.GREEN + " delete <hologramName>" + ChatColor.WHITE
						+ " - to remove a Holographic Leadaderboard",
				ChatColor.GOLD + label + ChatColor.GREEN + " list" + ChatColor.WHITE
						+ " - to list the created Holographic Leaderboards",
				ChatColor.GOLD + label + ChatColor.GREEN + " update <hologramName>" + ChatColor.WHITE
						+ " - to load and update a Holographic Leadaderboard" };
	}

	@Override
	public String getDescription() {
		return Messages.getString("mobhunting.commands.hologram.description");
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

		String[] subcmds = { "create", "delete", "list", "update" };
		ArrayList<String> items = new ArrayList<String>();
		if (CompatibilityManager.isPluginLoaded(CitizensCompat.class)) {
			if (args.length == 1) {
				for (String cmd : subcmds)
					items.add(cmd);
			} else if (args.length == 2) {
				if (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("update"))
					for (String hologramName : MobHunting.getLeaderboardManager().getHologramManager().getHolograms()
							.keySet())
						items.add(hologramName);
			} else if (args.length == 3) {
				if (args[0].equalsIgnoreCase("create")) {
					StatType[] values = StatType.values();
					for (int i = 0; i < values.length; i++)
						if (values[i] != null)
							items.add(ChatColor.stripColor(values[i].translateName().replaceAll(" ", "_")));
				}
			} else if (args.length == 4) {
				if (args[0].equalsIgnoreCase("create")) {
					TimePeriod[] values = TimePeriod.values();
					for (int i = 0; i < values.length; i++)
						items.add(ChatColor.stripColor(values[i].translateName().replaceAll(" ", "_")));
				}
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

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args) {

		if (args.length == 0)
			return false;

		if (args.length == 2 && (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("delete"))) {

			String hologramName = args[1];
			if (MobHunting.getLeaderboardManager().getHologramManager().getHolograms().containsKey(hologramName)) {
				MobHunting.getLeaderboardManager().getHologramManager().deleteHologramLeaderboard(hologramName);
				plugin.getMessages().senderSendMessage(sender,
						Messages.getString("mobhunting.commands.hologram.deleted", "hologramid", hologramName));
			} else
				plugin.getMessages().senderSendMessage(sender,ChatColor.RED
						+ Messages.getString("mobhunting.commands.hologram.unknown", "hologramid", args[1]));
			return true;

		} else if (args.length == 2 && args[0].equalsIgnoreCase("update")) {
			String hologramName = args[1];
			if (MobHunting.getLeaderboardManager().getHologramManager().getHolograms().containsKey(hologramName)) {
				MobHunting.getLeaderboardManager().getHologramManager().deleteHolographicLeaderboard(hologramName);
				MobHunting.getLeaderboardManager().getHologramManager().loadHologramLeaderboard(hologramName);
				MobHunting.getLeaderboardManager().getHologramManager().updateHolographicLeaderboard(hologramName);
				plugin.getMessages().senderSendMessage(sender,
						Messages.getString("mobhunting.commands.hologram.updating", "hologramid", hologramName));
			} else
				plugin.getMessages().senderSendMessage(sender,ChatColor.RED
						+ Messages.getString("mobhunting.commands.hologram.unknown", "hologramid", args[1]));
			return true;

		} else if (args.length == 1 && args[0].equalsIgnoreCase("select")) {
			plugin.getMessages().senderSendMessage(sender,Messages.getString("mobhunting.commands.hologram.selected", "hologramid", args[1]));
			return true;

		} else if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
			String res = MobHunting.getLeaderboardManager().getHologramManager().listHolographicLeaderboard();
			plugin.getMessages().senderSendMessage(sender,res);
			return true;
		} else if (args.length == 5 && args[0].equalsIgnoreCase("create")) {
			String hologramName = args[1];
			if (!MobHunting.getLeaderboardManager().getHologramManager().getHolograms().containsKey(hologramName)) {

				StatType[] types;
				try {
					types = parseTypes(args[2]);
				} catch (IllegalArgumentException e) {
					plugin.getMessages().senderSendMessage(sender,ChatColor.RED + Messages.getString("mobhunting.commands.top.unknown-stat",
							"stat", ChatColor.YELLOW + e.getMessage() + ChatColor.RED));
					return true;
				}

				TimePeriod[] periods;
				try {
					periods = parsePeriods(args[3]);
				} catch (IllegalArgumentException e) {
					plugin.getMessages().senderSendMessage(sender,ChatColor.RED + Messages.getString("mobhunting.commands.top.unknown-period",
							"period", ChatColor.YELLOW + e.getMessage() + ChatColor.RED));
					return true;
				}

				int no_of_lines = Integer.valueOf(args[4]);
				if (no_of_lines < 1 || no_of_lines > 25) {
					plugin.getMessages().senderSendMessage(sender,ChatColor.RED + Messages.getString("mobhunting.commands.hologram.too_many_lines",
							"no_of_lines", args[4]));
					return true;
				}

				// create hologram
				Location location = ((Player) sender).getLocation();
				location.setPitch(0);
				location.setYaw(0);
				HologramLeaderboard hologramLeaderboard = new HologramLeaderboard(plugin, hologramName, types, periods,
						no_of_lines, location.add(0, 2, 0));
				MobHunting.getLeaderboardManager().getHologramManager().createHologramLeaderboard(hologramLeaderboard);
				MobHunting.getLeaderboardManager().getHologramManager().saveHologramLeaderboard(hologramName);
				plugin.getMessages().senderSendMessage(sender,ChatColor.GREEN
						+ Messages.getString("mobhunting.commands.hologram.created", "hologramid", hologramName));
				Messages.debug("Creating Hologram Leaderbard: id=%s,stat=%s,per=%s,rank=%s", hologramName, args[2],
						args[3], no_of_lines);
				return true;
			} else {
				plugin.getMessages().senderSendMessage(sender,
						Messages.getString("mobhunting.commands.hologram.hologram_exists", "hologramid", hologramName));
				return true;
			}
		}

		return false;
	}

	private StatType[] parseTypes(String typeString) throws IllegalArgumentException {
		String[] parts = typeString.split(",");
		StatType[] types = new StatType[parts.length];
		for (int i = 0; i < parts.length; ++i) {
			types[i] = StatType.parseStat(parts[i]);
			if (types[i] == null)
				throw new IllegalArgumentException(parts[i]);
		}

		return types;
	}

	private TimePeriod[] parsePeriods(String periodString) throws IllegalArgumentException {
		String[] parts = periodString.split(",");
		TimePeriod[] periods = new TimePeriod[parts.length];
		for (int i = 0; i < parts.length; ++i) {
			periods[i] = TimePeriod.parsePeriod(parts[i]);
			if (periods[i] == null)
				throw new IllegalArgumentException(parts[i]);
		}

		return periods;
	}
}
