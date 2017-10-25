package one.lindegaard.MobHunting.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;

public class HappyHourCommand implements ICommand {

	private MobHunting plugin;

	public HappyHourCommand(MobHunting plugin) {
		this.plugin = plugin;
	}

	private int minutesToRun = 0;
	public static int minutesLeft = 0;
	public static double multiplier = 1;
	private long starttime;

	private BukkitTask happyhourevent = null;
	private BukkitTask happyhoureventStop = null;

	public HappyHourCommand(int minutesToRun) {// , int minutesLeft, double
												// multiplier) {

		this.minutesToRun = minutesToRun;
		// this.minutesLeft = minutesLeft;
		// this.multiplier = multiplier;
	}

	// Used case
	// /mh happyhour <time> <multiplier>
	// time is default 60 minuts, multiplier is default 2
	// if happhyhour is called while ongoing, it shows multiplier and time left

	@Override
	public String getName() {
		return "happyhour";
	}

	@Override
	public String[] getAliases() {
		return new String[] { "moneyevent" };
	}

	@Override
	public String getPermission() {
		return "mobhunting.happyhour";
	}

	@Override
	public String[] getUsageString(String label, CommandSender sender) {
		return new String[] {
				ChatColor.GOLD + label + ChatColor.YELLOW + " <time in minuets> <multiplier>" + ChatColor.WHITE
						+ " - to create an happy hour",
				ChatColor.GOLD + label + ChatColor.WHITE + " - to show current status", ChatColor.GOLD + label
						+ ChatColor.GREEN + " cancel" + ChatColor.WHITE + " - to stop an ongoing happy hour." };
	}

	@Override
	public String getDescription() {
		return Messages.getString("mobhunting.commands.happyhour.description");
	}

	@Override
	public boolean canBeConsole() {
		return true;
	}

	@Override
	public boolean canBeCommandBlock() {
		return true;
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args) {

		if (args.length == 0) {

			// mh happyhour
			// status of happyhour
			if (happyhourevent != null && (Bukkit.getScheduler().isCurrentlyRunning(happyhourevent.getTaskId())
					|| Bukkit.getScheduler().isQueued(happyhourevent.getTaskId()))) {
				minutesLeft = minutesToRun - ((int) (System.currentTimeMillis() - starttime) / (1000 * 60));
				Messages.debug("The happy hour ends in %s minutes", minutesLeft);
				for (Player player : MobHunting.getMobHuntingManager().getOnlinePlayers()) {
					plugin.getMessages().playerSendTitlesMessage(player,
							Messages.getString("mobhunting.commands.happyhour.ongoing_title"),
							Messages.getString("mobhunting.commands.happyhour.ongoing_subtitle", "multiplier",
									multiplier, "minutes", minutesLeft),
							20, 100, 20);
				}
			} else {
				plugin.getMessages().senderSendMessage(sender,"The Happy Hour event is not started");
			}
			return true;

		} else if (args.length == 1) {

			// /mh happyhour help
			// Show help
			if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?"))
				return false;

			// /mh happyhour cancel|stop
			if (args[0].equalsIgnoreCase("cancel") || args[0].equalsIgnoreCase("stop")) {
				if (happyhourevent != null && (Bukkit.getScheduler().isCurrentlyRunning(happyhourevent.getTaskId())
						|| Bukkit.getScheduler().isQueued(happyhourevent.getTaskId()))) {
					happyhourevent.cancel();
					happyhoureventStop.cancel();
					minutesToRun = 0;
					minutesLeft = 0;
					multiplier = 1;
					Messages.debug("Happy hour was cancelled");
					for (Player player : MobHunting.getMobHuntingManager().getOnlinePlayers()) {
						plugin.getMessages().playerSendTitlesMessage(player,
								Messages.getString("mobhunting.commands.happyhour.cancelled_title"),
								Messages.getString("mobhunting.commands.happyhour.cancelled_subtitle"), 20, 100, 20);
					}
				} else
					plugin.getMessages().senderSendMessage(sender,"Its not happy hour now");
				return true;
			}

		} else if (args[0].matches("\\d+(\\d+)?") && args[1].matches("\\d+(\\.\\d+)?")) {

			// /mh happyhour <minutes> <multiplier>

			minutesToRun = Integer.valueOf(args[0]);
			minutesLeft = minutesToRun;
			multiplier = Double.valueOf(args[1]);
			starttime = System.currentTimeMillis();

			if (minutesToRun <= 0 || multiplier <= 0)
				return false;

			if (happyhourevent != null && (Bukkit.getScheduler().isCurrentlyRunning(happyhourevent.getTaskId())
					|| Bukkit.getScheduler().isQueued(happyhourevent.getTaskId()))) {
				happyhourevent.cancel();
				happyhoureventStop.cancel();
				Messages.debug("Happy hour restarted, minutes left:%s", minutesLeft);
			} else {
				Messages.debug("Happy hour started, minutes left:%s", minutesLeft);
			}

			Messages.debug("getMessages=%s", plugin.getMessages());
			for (Player player : MobHunting.getMobHuntingManager().getOnlinePlayers()) {
				plugin.getMessages().playerSendTitlesMessage(player,
						Messages.getString("mobhunting.commands.happyhour.started_title"),
						Messages.getString("mobhunting.commands.happyhour.started_subtitle", "multiplier", multiplier,
								"minutes", minutesToRun),
						20, 100, 20);
			}

			happyhourevent = Bukkit.getScheduler().runTaskTimer(MobHunting.getInstance(), () -> {
				minutesLeft = minutesToRun - ((int) (System.currentTimeMillis() - starttime) / (1000 * 60));
				Messages.debug("The happy hour ends in %s minutes", minutesLeft);
			}, 18000, 18000);

			happyhoureventStop = Bukkit.getScheduler().runTaskLater(MobHunting.getInstance(), () -> {
				minutesLeft = 0;
				minutesToRun = 0;
				multiplier = 1;
				happyhourevent.cancel();
				Messages.debug("Happy hour ended");
				for (Player player : MobHunting.getMobHuntingManager().getOnlinePlayers()) {
					plugin.getMessages().playerSendTitlesMessage(player,
							Messages.getString("mobhunting.commands.happyhour.ended_title"),
							Messages.getString("mobhunting.commands.happyhour.ended_subtitle"), 20, 100, 20);
				}
			}, (long) (minutesToRun * 20 * 60));

			return true;
		}
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String label, String[] args) {
		ArrayList<String> items = new ArrayList<>();
		if (args.length == 1) {
			items.add("5");
			items.add("60");
			items.add("120");
			items.add("240");
			items.add("3600");
			items.add("cancel");
		} else if (args.length == 2) {
			if (items.isEmpty()) {
				Messages.debug("arg[1]=(%s)", args[1]);
				items.add("1");
				items.add("2");
				items.add("3");
				items.add("4");
				items.add("5");
			}
		}
		return items;
	}

}
