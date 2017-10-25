package one.lindegaard.MobHunting.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.Sign;

import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.StatType;
import one.lindegaard.MobHunting.leaderboard.WorldLeaderboard;
import one.lindegaard.MobHunting.storage.TimePeriod;

public class LeaderboardCommand implements ICommand, Listener {
	private WeakHashMap<Player, BoardState> mWaitingStates = new WeakHashMap<Player, BoardState>();

	private MobHunting plugin;
	public LeaderboardCommand(MobHunting plugin) {
		this.plugin=plugin;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	@Override
	public String getName() {
		return "leaderboard";
	}

	@Override
	public String[] getAliases() {
		return new String[] { "lb", "board" };
	}

	@Override
	public String getPermission() {
		return "mobhunting.leaderboard";
	}

	@Override
	public String[] getUsageString(String label, CommandSender sender) {
		return new String[] {
				ChatColor.GOLD + label + ChatColor.GREEN + " create <type> <period> [isHorizonal?] " + ChatColor.YELLOW
						+ "[<width> <height>]",
				ChatColor.GOLD + label + ChatColor.GREEN + " delete <id>",
				ChatColor.GOLD + label + ChatColor.GREEN + " edit (type|period|horizontal|addtype|addperiod) <value>" };
	}

	@Override
	public String getDescription() {
		return Messages.getString("mobhunting.commands.leaderboard.description");
	}

	@Override
	public boolean canBeConsole() {
		return false;
	}

	@Override
	public boolean canBeCommandBlock() {
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

	private boolean onDelete(CommandSender sender, String[] args) {
		if (args.length != 2)
			return false;

		String id = args[1];

		try {
			MobHunting.getLeaderboardManager().deleteLegacyLeaderboard(id);
			plugin.getMessages().senderSendMessage(sender,
					ChatColor.GREEN + Messages.getString("mobhunting.commands.leaderboard.delete", "leaderboard", id));
		} catch (IllegalArgumentException e) {
			plugin.getMessages().senderSendMessage(sender,ChatColor.RED + e.getMessage());
		}

		return true;
	}

	private boolean onEdit(CommandSender sender, String[] args) {
		if (args.length != 3)
			return false;

		BoardState state = mWaitingStates.get(sender);
		if (state == null)
			state = new BoardState();

		if (args[1].equalsIgnoreCase("type")) {
			try {
				state.type = parseTypes(args[2]);
			} catch (IllegalArgumentException e) {
				plugin.getMessages().senderSendMessage(sender,ChatColor.RED + Messages.getString("mobhunting.commands.top.unknown-stat", "stat",
						ChatColor.YELLOW + e.getMessage() + ChatColor.RED));
				return true;
			}
		} else if (args[1].equalsIgnoreCase("addtype")) {
			try {
				state.addType = parseTypes(args[2]);
			} catch (IllegalArgumentException e) {
				plugin.getMessages().senderSendMessage(sender,ChatColor.RED + Messages.getString("mobhunting.commands.top.unknown-stat", "stat",
						ChatColor.YELLOW + e.getMessage() + ChatColor.RED));
				return true;
			}
		} else if (args[1].equalsIgnoreCase("period")) {
			try {
				state.period = parsePeriods(args[2]);
			} catch (IllegalArgumentException e) {
				plugin.getMessages().senderSendMessage(sender,ChatColor.RED + Messages.getString("mobhunting.commands.top.unknown-period",
						"period", ChatColor.YELLOW + e.getMessage() + ChatColor.RED));
				return true;
			}
		} else if (args[1].equalsIgnoreCase("addperiod")) {
			try {
				state.addPeriod = parsePeriods(args[2]);
			} catch (IllegalArgumentException e) {
				plugin.getMessages().senderSendMessage(sender,ChatColor.RED + Messages.getString("mobhunting.commands.top.unknown-period",
						"period", ChatColor.YELLOW + e.getMessage() + ChatColor.RED));
				return true;
			}
		} else if (args[1].equalsIgnoreCase("horizontal")) {
			state.horizontal = Boolean.parseBoolean(args[2]);
		} else {
			plugin.getMessages().senderSendMessage(sender,ChatColor.RED
					+ Messages.getString("mobhunting.commands.leaderboard.edit.unknown", "setting", args[1]));
			return true;
		}

		// TODO: Create new strings in Message
		mWaitingStates.put((Player) sender, state);
		if (state.create)
			plugin.getMessages().senderSendMessage(sender,ChatColor.GOLD + "Changes saved, right click a wall sign to create the board.");
		else
			plugin.getMessages().senderSendMessage(sender,ChatColor.GOLD + "Changes saved, right click a leaderboard to apply changes.");

		return true;
	}

	private boolean onCreate(CommandSender sender, String[] args) {
		if (args.length < 3 || args.length > 6)
			return false;

		StatType[] types;
		try {
			types = parseTypes(args[1]);
		} catch (IllegalArgumentException e) {
			plugin.getMessages().senderSendMessage(sender,ChatColor.RED + Messages.getString("mobhunting.commands.top.unknown-stat", "stat",
					ChatColor.YELLOW + e.getMessage() + ChatColor.RED));
			return true;
		}

		TimePeriod[] periods;

		try {
			periods = parsePeriods(args[2]);
		} catch (IllegalArgumentException e) {
			plugin.getMessages().senderSendMessage(sender,ChatColor.RED + Messages.getString("mobhunting.commands.top.unknown-period", "period",
					ChatColor.YELLOW + e.getMessage() + ChatColor.RED));
			return true;
		}

		boolean horizontal = true;
		int width = 3;
		int height = 3;

		if (args.length == 4)
			horizontal = Boolean.parseBoolean(args[3]);
		else if (args.length > 4) {
			try {
				width = Integer.parseInt(args[args.length - 2]);
				if (width < 1) {
					// TODO: Create new strings in Message
					plugin.getMessages().senderSendMessage(sender,ChatColor.RED + "Width is too small. Must be at least 1");
					return true;
				}
			} catch (NumberFormatException e) {
				// TODO: Create new strings in Message
				plugin.getMessages().senderSendMessage(sender,ChatColor.RED + "Width must be a whole number of at least 1");
				return true;
			}

			try {
				height = Integer.parseInt(args[args.length - 1]);
				if (height < 1) {
					// TODO: Create new strings in Message
					plugin.getMessages().senderSendMessage(sender,ChatColor.RED + "Height is too small. Must be at least 1");
					return true;
				}
			} catch (NumberFormatException e) {
				// TODO: Create new strings in Message
				plugin.getMessages().senderSendMessage(sender,ChatColor.RED + "Height must be a whole number of at least 1");
				return true;
			}

			if (args.length == 6)
				horizontal = Boolean.parseBoolean(args[3]);
		}

		BoardState state = new BoardState();
		state.create = true;
		state.height = height;
		state.width = width;
		state.horizontal = horizontal;
		state.period = periods;
		state.type = types;

		mWaitingStates.put((Player) sender, state);

		// TODO: create string in messages.
		plugin.getMessages().senderSendMessage(sender,ChatColor.GOLD + "Click a wall sign to create the leaderboard");

		return true;
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args) {
		if (args.length == 0)
			return false;

		if (args[0].equalsIgnoreCase("create"))
			return onCreate(sender, args);
		else if (args[0].equalsIgnoreCase("delete"))
			return onDelete(sender, args);
		else if (args[0].equalsIgnoreCase("edit"))
			return onEdit(sender, args);

		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String label, String[] args) {
		ArrayList<String> items = new ArrayList<String>();

		if (args.length == 1) {
			items.add("create");
			items.add("delete");
			items.add("edit");
		} else if (args.length > 1) {
			if (args[0].equalsIgnoreCase("create")) {
				if (args.length == 2) {
					for (StatType type : StatType.values())
						if (type != null)
							items.add(ChatColor.stripColor(type.translateName().replaceAll(" ", "_")));
							//items.add(type.getDBColumn());
				} else if (args.length == 3)
					for (TimePeriod period : TimePeriod.values())
						items.add(period.translateName().replaceAll(" ", "_"));
			} else if (args[0].equalsIgnoreCase("edit")) {
				if (args.length == 2) {
					items.add("type");
					items.add("period");
					items.add("addtype");
					items.add("addperiod");
					items.add("horizontal");
				} else if (args.length == 3) {
					if (args[1].equalsIgnoreCase("type") || args[1].equalsIgnoreCase("addtype"))
						for (StatType type : StatType.values())
							items.add(ChatColor.stripColor(type.translateName().replaceAll(" ", "_")));
					else if (args[1].equalsIgnoreCase("period") || args[1].equalsIgnoreCase("addperiod"))
						for (TimePeriod period : TimePeriod.values())
							items.add(ChatColor.stripColor(period.translateName().replaceAll(" ", "_")));
					else if (args[1].equalsIgnoreCase("horizontal")) {
						items.add("true");
						items.add("false");
					}
				}
			}
		}

		if (!args[args.length - 1].trim().isEmpty()) {
			String match = args[args.length - 1].trim().toLowerCase();

			items.removeIf(name -> !name.toLowerCase().startsWith(match));
		}
		return items;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	private void onClickSign(PlayerInteractEvent event) {
		if (!event.hasBlock())
			return;

		BoardState state = mWaitingStates.remove(event.getPlayer());
		if (state == null)
			return;

		if (event.getClickedBlock().getType() != Material.WALL_SIGN) {
			// TODO: Create new strings in Messages.
			if (state.create)
				event.getPlayer().sendMessage(ChatColor.RED + "Leaderboard creation cancelled.");
			else
				event.getPlayer().sendMessage(ChatColor.RED + "Leaderboard edit cancelled.");
			return;
		}

		if (state.create) {
			BlockFace face = ((Sign) event.getClickedBlock().getState().getData()).getFacing();

			try {
				// MobHunting.debug("leaderBoardCommand:
				// parm=%s,%s,%s,%s,%s,%s", face,state.type[0],
				// state.period[0],state.horizontal,state.width,state.height);
				MobHunting.getLeaderboardManager().createLeaderboard(event.getClickedBlock().getLocation(), face,
						state.type, state.period, state.horizontal, state.width, state.height);
				// TODO: Create new strings in Message
				event.getPlayer().sendMessage(ChatColor.GREEN + "Leaderboard created");
			} catch (IllegalArgumentException e) {
				event.getPlayer().sendMessage(ChatColor.RED + e.getMessage());
			}

		} else {
			WorldLeaderboard board = MobHunting.getLeaderboardManager()
					.getLeaderboardAt(event.getClickedBlock().getLocation());
			if (board != null) {
				if (state.type != null)
					board.setType(state.type);

				if (state.addType != null) {
					StatType[] original = board.getTypes();
					StatType[] types = Arrays.copyOf(original, original.length + state.addType.length);
					for (int i = 0; i < state.addType.length; ++i)
						types[i + original.length] = state.addType[i];
					board.setType(types);
				}

				if (state.period != null)
					board.setPeriod(state.period);

				if (state.addPeriod != null) {
					TimePeriod[] original = board.getPeriods();
					TimePeriod[] types = Arrays.copyOf(original, original.length + state.addPeriod.length);
					for (int i = 0; i < state.addPeriod.length; ++i)
						types[i + original.length] = state.addPeriod[i];
					board.setPeriod(types);
				}

				if (state.horizontal != null)
					board.setHorizontal(state.horizontal);

				board.update();
				// TODO: Create new strings in Message
				event.getPlayer().sendMessage(ChatColor.GREEN + "Leaderboard edited");

				MobHunting.getLeaderboardManager().saveWorld(board.getWorld());
			} else
				event.getPlayer().sendMessage(ChatColor.RED + "That is not a leaderboard. Edit cancelled.");
		}

		event.setCancelled(true);
	}

	private static class BoardState {
		Integer width;
		Integer height;
		Boolean horizontal;
		public StatType[] type;
		public TimePeriod[] period;
		StatType[] addType;
		TimePeriod[] addPeriod;
		public boolean create;
	}
}
