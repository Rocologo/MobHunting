package one.lindegaard.MobHunting.commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.bukkit.RegionQuery;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.compatibility.CompatibilityManager;
import one.lindegaard.MobHunting.compatibility.WorldGuardCompat;
import one.lindegaard.MobHunting.compatibility.WorldGuardHelper;

public class RegionCommand implements ICommand {

private MobHunting plugin;
	
	public RegionCommand(MobHunting plugin) {
		this.plugin=plugin;
	}

	public RegionCommand() {
	}

	// Used case (???)
	// /mh region <id> MobHunting allow - args.length = 3 || arg[1]="mobhunting"
	// /mh region <id> MobHunting deny - args.length = 3 || arg[1]="mobhunting"
	// /mh region <id> MobHunting - args.length = 2 || arg[1]="mobhunting"
	// /mh region MobHunting allow - args.length = 2 || arg[0]="mobhunting"
	// /mh region MobHunting deny - args.length = 2 || arg[0]="mobhunting"
	// /mh region MobHunting - args.length = 1 || arg[0]="mobhunting"

	@Override
	public String getName() {
		return "region";
	}

	@Override
	public String[] getAliases() {
		return new String[] { "rg", "worldguard", "flag" };
	}

	@Override
	public String getPermission() {
		return "mobhunting.region";
	}

	@Override
	public String[] getUsageString(String label, CommandSender sender) {
		return new String[] {
				ChatColor.GOLD + label + ChatColor.GREEN + " <id>" + ChatColor.WHITE + " mobhunting allow",
				ChatColor.GOLD + label + ChatColor.GREEN + " <id>" + ChatColor.WHITE + " mobhunting deny",
				ChatColor.GOLD + label + ChatColor.GREEN + " <id>" + ChatColor.WHITE + " mobhunting",
				ChatColor.GOLD + label + ChatColor.GREEN + " mobhunting allow",
				label + ChatColor.GREEN + " mobhunting deny",
				ChatColor.GOLD + label + ChatColor.GREEN + " mobhunting" + ChatColor.WHITE + " - to remove the flag" };
	}

	@Override
	public String getDescription() {
		return Messages.getString("mobhunting.commands.region.description");
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

		ArrayList<String> items = new ArrayList<String>();
		if (CompatibilityManager.isPluginLoaded(WorldGuardCompat.class)) {
			if (args.length == 1) {
				if (sender instanceof Player) {

					RegionManager regionManager = WorldGuardCompat.getWorldGuardPlugin()
							.getRegionManager(((Player) sender).getWorld());
					ApplicableRegionSet set = regionManager.getApplicableRegions(((Player) sender).getLocation());
					if (set.size() > 0) {
						// player is in one or more regions, show regions on
						// this loction
						Iterator<ProtectedRegion> it = set.iterator();
						while (it.hasNext()) {
							ProtectedRegion area = it.next();
							items.add(area.getId());
						}
					} else {
						// Player is not in a region, show all regions in
						// world.
						RegionManager rm = WorldGuardHelper.getRegionContainer().get(((Player) sender).getWorld());
						Iterator<Entry<String, ProtectedRegion>> i = rm.getRegions().entrySet().iterator();
						while (i.hasNext()) {
							ProtectedRegion pr = i.next().getValue();
							items.add(pr.getId());
						}
					}
				}
			} else if (args.length == 2) {
				if (args[1].equalsIgnoreCase("mobhunting")) {
					items.add("allow");
					items.add("deny");
					items.add(" ");
				} else
					items.add("mobhunting");
			} else if (args.length == 3) {
				if (args[2].equalsIgnoreCase("mobhunting")) {
					items.add("allow");
					items.add("deny");
					items.add(" ");
				} else
					items.add("mobhunting");
			}
		}
		return items;
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args) {
		if (args.length == 0)
			return false;

		if (CompatibilityManager.isPluginLoaded(WorldGuardCompat.class)) {
			if (sender instanceof Player) {
				RegionQuery query = WorldGuardHelper.getRegionContainer().createQuery();
				ApplicableRegionSet set = query.getApplicableRegions(((Player) sender).getLocation());
				if (set.size() == 1) {
					// player is standing on a location with single region
					ProtectedRegion region = set.getRegions().iterator().next();
					if ((args.length == 1) && args[0].equalsIgnoreCase("mobhunting"))
						return WorldGuardHelper.removeCurrentRegionFlag(sender, region,
								WorldGuardHelper.getMobHuntingFlag());
					else if ((args.length >= 2) && args[0].equalsIgnoreCase("mobhunting"))
						return WorldGuardHelper.setCurrentRegionFlag(sender, region,
								WorldGuardHelper.getMobHuntingFlag(), args[1]);
				} else {
					// player is standing on a location with more than one
					// region
					if ((args.length == 2) && args[1].equalsIgnoreCase("mobhunting")) {
						Iterator<ProtectedRegion> i = set.getRegions().iterator();
						while (i.hasNext()) {
							ProtectedRegion pr = i.next();
							if (pr.getId().equalsIgnoreCase(args[0])) {
								return WorldGuardHelper.removeCurrentRegionFlag(sender, pr,
										WorldGuardHelper.getMobHuntingFlag());
							}
						}
						plugin.getMessages().senderSendMessage(sender,ChatColor.RED + Messages
								.getString("mobhunting.commands.region.unknownRegionId", "regionid", args[0]));
					} else if ((args.length >= 3) && args[1].equalsIgnoreCase("mobhunting")) {
						RegionManager rm = WorldGuardHelper.getRegionContainer().get(((Player) sender).getWorld());
						Iterator<Entry<String, ProtectedRegion>> i = rm.getRegions().entrySet().iterator();
						while (i.hasNext()) {
							ProtectedRegion pr = i.next().getValue();
							if (pr.getId().equalsIgnoreCase(args[0])) {
								return WorldGuardHelper
										.setCurrentRegionFlag(sender,
												WorldGuardHelper.getRegionContainer().get(((Player) sender).getWorld())
														.getRegion(args[0]),
												WorldGuardHelper.getMobHuntingFlag(), args[2]);
							}
						}
						plugin.getMessages().senderSendMessage(sender,ChatColor.RED + Messages
								.getString("mobhunting.commands.region.unknownRegionId", "regionid", args[0]));
					} else {
						plugin.getMessages().senderSendMessage(sender,
								ChatColor.RED + Messages.getString("mobhunting.commands.region.specifyRegionId"));
					}
				}
			} else {
				plugin.getMessages().senderSendMessage(sender,
						ChatColor.RED + Messages.getString("mobhunting.commands.region.noWorldguardSupport"));
			}
		}

		return false;
	}
}
