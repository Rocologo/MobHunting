package one.lindegaard.MobHunting.commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.mobs.MinecraftMob;
import one.lindegaard.MobHunting.rewards.RewardManager;
import one.lindegaard.MobHunting.util.Misc;

public class HeadCommand implements ICommand, Listener {

	public static final String MH_HEAD = "MH:Head";
	public static final String MH_REWARD = "MobHunting Reward";

	public HeadCommand(MobHunting instance) {
	}

	// Used case
	// /mh head give [toPlayer] [mobname|playername] [displayname] [amount] - to
	// give a head to a player.
	// /mh head rename [displayname] - to rename the head holding in the hand.
	// /mh head drop <head>
	// /mh head drop <head> <player>
	// /mh head drop <head> <x> <y> <z> <world>

	@Override
	public String getName() {
		return "head";
	}

	@Override
	public String[] getAliases() {
		return new String[] { "ph", "playerhead", "heads", "mobhead", "spawn" };
	}

	@Override
	public String getPermission() {
		return "mobhunting.head";
	}

	@Override
	public String[] getUsageString(String label, CommandSender sender) {
		return new String[] {
				ChatColor.GOLD + label + ChatColor.GREEN + " give" + " [toPlayername] [playername|mobname]"
						+ ChatColor.YELLOW + " [displayname] [amount] [silent]" + ChatColor.WHITE + " - to give a head",
				ChatColor.GOLD + label + ChatColor.GREEN + " rename [new displayname]" + ChatColor.WHITE
						+ " - to rename the head in players name",
				ChatColor.GOLD + label + ChatColor.GREEN + " drop" + " [playername|mobname]" + ChatColor.YELLOW
						+ " [toPlayername] " + ChatColor.WHITE + " - to drop a head",
				ChatColor.GOLD + label + ChatColor.GREEN + " drop" + " [playername|mobname]" + ChatColor.YELLOW
						+ " [xpoxs] [ypos] [zpos] [worldname] " + ChatColor.WHITE
						+ " - to drop a head at the position" };
	}

	@Override
	public String getDescription() {
		return Messages.getString("mobhunting.commands.head.description");
	}

	@Override
	public boolean canBeConsole() {
		return true;
	}

	@Override
	public boolean canBeCommandBlock() {
		return true;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args) {
		// /mh head give [toPlayername] [mobname|playername] [displayname]
		// [amount] [silent]
		if (args.length >= 2 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("spawn"))) {
			if (args.length >= 3) {
				OfflinePlayer offlinePlayer = null, toPlayer = null;
				String displayName;
				int amount = 1;

				// get toPlayerName
				toPlayer = Bukkit.getOfflinePlayer(args[1]);
				if (toPlayer == null || !toPlayer.isOnline()) {
					sender.sendMessage(Messages.getString("mobhunting.commands.head.online", "playername", args[1]));
					return true;
				}

				// get MobType / PlayerName
				MinecraftMob mob = MinecraftMob.getMinecraftMobType(args[2]);
				if (mob == null) {
					offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
					if (offlinePlayer != null) {
						mob = MinecraftMob.PvpPlayer;
					} else {
						sender.sendMessage(
								Messages.getString("mobhunting.commands.head.unknown_name", "playername", args[2]));
						return true;
					}
				}
				// get displayname
				// if (args.length >= 4) {
				// displayName = args[3].replace("_", " ");
				// } else {
				if (mob != MinecraftMob.PvpPlayer)
					displayName = mob.getFriendlyName().replace("_", " ");
				else
					displayName = offlinePlayer.getName();
				// }
				// get amount
				if (args.length >= 5) {
					try {
						amount = Integer.valueOf(args[4]);
					} catch (NumberFormatException e) {
						sender.sendMessage(
								Messages.getString("mobhunting.commands.base.not_a_number", "number", args[4]));
						return false;
					}
				}
				// silent
				boolean silent = false;
				if (args.length >= 6 && (args[5].equalsIgnoreCase("silent") || args[5].equalsIgnoreCase("true")
						|| args[5].equalsIgnoreCase("1"))) {
					silent = true;
				}
				if (Misc.isMC18OrNewer()) {
                    // Use GameProfile
                    ItemStack head = mob.getCustomHead(displayName, amount, mob.getHeadPrize());
                    // ItemStack head = mob.getHead(displayName, 1,
                    // mob.getHeadPrize());
                    ((Player) toPlayer).getWorld().dropItem(((Player) toPlayer).getLocation(), head);

                } else {
                    String cmdString = mob.getCommandString().replace("{player}", toPlayer.getName())
                            .replace("{displayname}", displayName).replace("{lore}", MH_REWARD)
                            .replace("{playerid}", mob.getPlayerUUID())
                            .replace("{texturevalue}", mob.getTextureValue())
                            .replace("{amount}", String.valueOf(amount)).replace("{playername}",
                                    offlinePlayer != null ? offlinePlayer.getName() : mob.getPlayerProfile());
                    Messages.debug("%s Cmd=%s", mob.getFriendlyName(), cmdString);
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), cmdString);
                }
				if (toPlayer.isOnline() && !silent)
                    Messages.playerSendMessage((Player) toPlayer,
                            Messages.getString("mobhunting.commands.head.you_got_a_head", "mobname", displayName));

			}

			return true;

		} else if (args.length > 1 && (args[0].equalsIgnoreCase("rename"))) {
			// mh head rename [displayname] - to rename the head in hand.
			if (sender instanceof Player) {
				Player player = (Player) sender;
				ItemStack itemInHand = player.getItemInHand();
				if (itemInHand.hasItemMeta() && itemInHand.getItemMeta().hasLore()
						&& itemInHand.getItemMeta().getLore().get(0).equals(MH_REWARD)) {
					String displayname = "";
					for (int i = 1; i < args.length; i++) {
						if (i != (args.length - 1))
							displayname = displayname + args[i] + " ";
						else
							displayname = displayname + args[i];
					}
					ItemMeta im = itemInHand.getItemMeta();
					im.setDisplayName(displayname);
					itemInHand.setItemMeta(im);
				} else {
					sender.sendMessage(Messages.getString("mobhunting.commands.head.headmustbeinhand"));
				}
			} else {
				sender.sendMessage("You can only rename heads ingame.");
			}
			return true;
		} else if (args.length >= 1 && (args[0].equalsIgnoreCase("drop") || args[0].equalsIgnoreCase("place"))) {
			// /mh head drop <head>
			// /mh head drop <head> <player>
			// /mh head drop <head> <x> <y> <z> <world>
			if (sender.hasPermission("mobhunting.money.drop")) {

				// /mh head drop
				MinecraftMob mob = MinecraftMob.getMinecraftMobType(args[1]);

				if (mob == null && Bukkit.getServer().getOfflinePlayer(args[1]).getName().equalsIgnoreCase(args[1]))
					mob = MinecraftMob.PvpPlayer;

				if (mob != null) {
					// double money = mob.getHeadPrize();
					double money = 0;
					if (args.length == 2) {
						Player player = (Player) sender;
						Location location = Misc.getTargetBlock(player, 20).getLocation();
						if (mob == MinecraftMob.PvpPlayer)
							player.getWorld().dropItem(location, mob.getCustomHead(args[1], 1, money));
						else
							player.getWorld().dropItem(location, mob.getCustomHead(mob.getFriendlyName(), 1, money));

					} else if (args.length == 3) {
						if (Bukkit.getServer().getOfflinePlayer(args[2]).isOnline()) {
							Player player = ((Player) Bukkit.getServer().getOfflinePlayer(args[2]));
							Location location = Misc.getTargetBlock(player, 3).getLocation();
							if (mob == MinecraftMob.PvpPlayer)
								player.getWorld().dropItem(location, mob.getCustomHead(args[1], 1, money));
							else
								player.getWorld().dropItem(location,
										mob.getCustomHead(mob.getFriendlyName(), 1, money));

						} else {
							sender.sendMessage(ChatColor.RED + Messages
									.getString("mobhunting.commands.base.playername-missing", "player", args[2]));
						}
					} else if ((args.length == 5 || args.length == 6) && args[2].matches("-?\\d+(\\d+)?")
							&& args[3].matches("-?\\d+(\\d+)?") && args[4].matches("-?\\d+(\\d+)?")) {
						int xpos = Integer.valueOf(args[2]);
						int ypos = Integer.valueOf(args[3]);
						int zpos = Integer.valueOf(args[4]);
						World world;
						if (args.length == 6)
							world = Bukkit.getWorld(args[5]);
						else if (sender instanceof Player) {
							world = ((Player) sender).getWorld();
						} else
							return false;
						Location location = new Location(world, xpos, ypos, zpos);
						if (mob == MinecraftMob.PvpPlayer) {
							Player player = ((Player) Bukkit.getServer().getOfflinePlayer(args[1]));
							ItemStack head = mob.getCustomHead(args[1], 1, money);
							RewardManager.setDisplayNameAndHiddenLores(head, args[1], money, player.getUniqueId());
							world.dropItem(location, head);
						} else {
							ItemStack head = mob.getCustomHead(mob.getFriendlyName(), 1, money);
							RewardManager.setDisplayNameAndHiddenLores(head, mob.getFriendlyName(), money,
									UUID.fromString(mob.getPlayerUUID().toString()));
							world.dropItem(location, head);
						}
					}
				} else {
					sender.sendMessage(
							Messages.getString("mobhunting.commands.head.unknown_name", "playername", args[1]));
				}

			} else {
				sender.sendMessage(ChatColor.RED + Messages.getString("mobhunting.commands.base.nopermission", "perm",
						"mobhunting.head", "command", "head"));
			}
			return true;
		}
		// show help
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String label, String[] args) {
		ArrayList<String> items = new ArrayList<String>();
		if (args.length == 1) {
			if (items.isEmpty()) {
				items.add("give");
				items.add("drop");
				items.add("rename");
			}
		} else if (args.length == 2 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("spawn"))) {
			for (Player player : Bukkit.getOnlinePlayers()) 
					items.add(ChatColor.stripColor(player.getName()));
		} else if ((args.length == 3 && args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("spawn"))) {
			for (MinecraftMob mob : MinecraftMob.values()) 
					items.add(ChatColor.stripColor(mob.getFriendlyName().replace(" ", "_")));
			for (Player player : Bukkit.getOnlinePlayers()) 
					items.add(ChatColor.stripColor(player.getName()));
		} else if (args.length == 2 && args[0].equalsIgnoreCase("drop")) {
			for (MinecraftMob mob : MinecraftMob.values()) 
					items.add(ChatColor.stripColor(mob.getFriendlyName().replace(" ", "_")));
			for (Player player : Bukkit.getOnlinePlayers()) 
					items.add(ChatColor.stripColor(player.getName()));
		} else if (args.length == 3 && args[0].equalsIgnoreCase("drop")) {
			for (Player player : Bukkit.getOnlinePlayers()) 
					items.add(ChatColor.stripColor(player.getName()));
		}
		
		if (!args[args.length - 1].trim().isEmpty()) {
			String match = args[args.length - 1].trim().toLowerCase();
			items.removeIf(name -> !name.toLowerCase().startsWith(match));
		}
		return items;
	}

}
