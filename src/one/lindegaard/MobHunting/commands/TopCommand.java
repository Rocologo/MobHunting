package one.lindegaard.MobHunting.commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.StatType;
import one.lindegaard.MobHunting.storage.IDataCallback;
import one.lindegaard.MobHunting.storage.StatStore;
import one.lindegaard.MobHunting.storage.TimePeriod;

public class TopCommand implements ICommand {

	private MobHunting plugin;
	
	public TopCommand(MobHunting plugin) {
		this.plugin=plugin;
	}

	public TopCommand() {
	}

	@Override
	public String getName() {
		return "top";
	}

	@Override
	public String[] getAliases() {
		return null;
	}

	@Override
	public String getPermission() {
		return "mobhunting.top";
	}

	@Override
	public String[] getUsageString(String label, CommandSender sender) {
		return new String[] { ChatColor.GOLD + label + ChatColor.GREEN + " <type> (day|week|month|year|alltime)"
				+ ChatColor.YELLOW + " [count]" + ChatColor.WHITE + " - to show top players" };
	}

	@Override
	public String getDescription() {
		return Messages.getString("mobhunting.commands.top.description");
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
		if (args.length != 2 && args.length != 3)
			return false;

		StatType selectedType = StatType.parseStat(args[0]);
		if (selectedType == null) {
			plugin.getMessages().senderSendMessage(sender,ChatColor.RED + Messages.getString("mobhunting.commands.top.unknown-stat", "stat",
					ChatColor.YELLOW + args[0] + ChatColor.RED));
			return true;
		}

		// Check the time period
		TimePeriod selectedPeriod = TimePeriod.parsePeriod(args[1]);
		if (selectedPeriod == null) {
			plugin.getMessages().senderSendMessage(sender,ChatColor.RED + Messages.getString("mobhunting.commands.top.unknown-period", "period",
					ChatColor.YELLOW + args[1] + ChatColor.RED));
			return true;
		}

		int count = 10;
		if (args.length >= 3) {
			try {
				count = Integer.parseInt(args[2]);
				if (count <= 0 || count > 100) {
					plugin.getMessages().senderSendMessage(sender,ChatColor.RED + Messages.getString("mobhunting.commands.top.invalid-range"));
					return true;
				}
			} catch (NumberFormatException e) {
				plugin.getMessages().senderSendMessage(sender,ChatColor.RED + Messages.getString("mobhunting.commands.top.invalid-number"));
				return true;
			}
		}

		MobHunting.getDataStoreManager().requestStats(selectedType, selectedPeriod, count,
				new LeaderboardDisplay(sender, count, selectedPeriod));

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String label, String[] args) {
		ArrayList<String> items = new ArrayList<String>();

		if (args.length == 1) {
			for (StatType type : StatType.values())
				if (type != null)
					items.add(type.translateName().replaceAll(" ", "_"));
		} else if (args.length == 2) {
			for (TimePeriod period : TimePeriod.values())
				items.add(period.translateName().replaceAll(" ", "_"));
		} else
			return null;

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

	private static class LeaderboardDisplay implements IDataCallback<List<StatStore>> {
		private CommandSender mSender;
		private int mCount;
		private TimePeriod mPeriod;

		public LeaderboardDisplay(CommandSender sender, int count, TimePeriod period) {
			mSender = sender;
			mCount = count;
			mPeriod = period;
		}

		@Override
		public void onCompleted(List<StatStore> data) {
			ArrayList<String> lines = new ArrayList<String>();
			String name = "";
			if (!data.isEmpty())
				name = data.get(0).getType().translateName();
			else {
				mSender.sendMessage(Messages.getString("mobhunting.commands.top.results.empty", "period",
						mPeriod.translateNameFriendly()));
				return;
			}

			lines.add(ChatColor.GRAY + Messages.getString("mobhunting.commands.top.results.header", "count",
					ChatColor.YELLOW + "" + mCount + ChatColor.GRAY, "period",
					ChatColor.YELLOW + mPeriod.translateNameFriendly() + ChatColor.GRAY, "statname",
					ChatColor.YELLOW + name + ChatColor.GRAY));

			int index = 1;
			for (StatStore stat : data) {
				if (stat.getAmount() == 0)
					continue;

				lines.add(ChatColor.GRAY + "" + index + ": " + ChatColor.GOLD + stat.getPlayer().getName()
						+ ChatColor.GRAY + " - " + ChatColor.GOLD + stat.getAmount());
				++index;
			}

			mSender.sendMessage(lines.toArray(new String[lines.size()]));
		}

		@Override
		public void onError(Throwable error) {
			mSender.sendMessage(ChatColor.RED + Messages.getString("mobhunting.commands.base.error"));
			error.printStackTrace();
		}

	}
}
