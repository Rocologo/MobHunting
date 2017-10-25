package one.lindegaard.MobHunting.commands;

import java.util.ArrayList;
import java.util.List;

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
import one.lindegaard.MobHunting.rewards.CustomItems;
import one.lindegaard.MobHunting.rewards.Reward;
import one.lindegaard.MobHunting.util.Misc;

public class HeadCommand implements ICommand, Listener {

	private MobHunting plugin;
	public static final String MH_HEAD = "MH:Head";
	public static final String MH_REWARD = "MobHunting Reward";

	public HeadCommand(MobHunting plugin) {
		this.plugin = plugin;
	}

	// Used case
	// /mh head give [toPlayer] [mobname|playername] [displayname] [amount] - to
	// give a head to a player.
	// /mh head rename [displayname] - to rename the head holding in the hand.
	// /mh head value <new value> - to give the head a new value
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
						+ " - to rename the head",

				ChatColor.GOLD + label + ChatColor.GREEN + " value [new value]" + ChatColor.WHITE
						+ " - to give the head a new value",

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
		CustomItems customItems = new CustomItems(plugin);
		if (args.length >= 2 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("spawn"))) {
			if (args.length >= 3) {
				OfflinePlayer offlinePlayer = null, toPlayer = null;
				String displayName;
				int amount = 1;

				// get toPlayerName
				toPlayer = Bukkit.getOfflinePlayer(args[1]);
				if (toPlayer == null || !toPlayer.isOnline()) {
					plugin.getMessages().senderSendMessage(sender,
							Messages.getString("mobhunting.commands.head.online", "playername", args[1]));
					return true;
				}

				// get MobType / PlayerName
				MinecraftMob mob = MinecraftMob.getMinecraftMobType(args[2]);
				if (mob == null) {
					offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
					if (offlinePlayer != null) {
						mob = MinecraftMob.PvpPlayer;
					} else {
						plugin.getMessages().senderSendMessage(sender,
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
						plugin.getMessages().senderSendMessage(sender,
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
					ItemStack head;
					if (mob == MinecraftMob.PvpPlayer)
						head = customItems.getPlayerHead(offlinePlayer.getUniqueId(), amount, mob.getHeadPrize());
					else
						head = customItems.getCustomHead(mob, displayName, amount, mob.getHeadPrize(),
								mob.getPlayerUUID());
					((Player) toPlayer).getWorld().dropItem(((Player) toPlayer).getLocation(), head);

				} else {
					String cmdString = mob.getCommandString().replace("{player}", toPlayer.getName())
							.replace("{displayname}", displayName).replace("{lore}", MH_REWARD)
							.replace("{playerid}", mob.getPlayerUUID().toString())
							.replace("{texturevalue}", mob.getTextureValue())
							.replace("{amount}", String.valueOf(amount)).replace("{playername}",
									offlinePlayer != null ? offlinePlayer.getName() : mob.getPlayerProfile());
					Messages.debug("%s Cmd=%s", mob.getFriendlyName(), cmdString);
					Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), cmdString);
				}
				if (toPlayer.isOnline() && !silent)
					plugin.getMessages().playerSendMessage((Player) toPlayer,
							Messages.getString("mobhunting.commands.head.you_got_a_head", "mobname", displayName));

			}

			return true;

		} else if (args.length > 1 && (args[0].equalsIgnoreCase("rename"))) {
			// mh head rename [displayname] - to rename the head in hand.
			if (sender instanceof Player) {
				Player player = (Player) sender;
				if (sender.hasPermission("mobhunting.head.rename")) {
					ItemStack itemInHand = player.getItemInHand();
					if (Reward.isReward(itemInHand)) {
						Reward reward = Reward.getReward(itemInHand);
						String displayName = "";
						for (int i = 1; i < args.length; i++) {
							if (i != (args.length - 1))
								displayName = displayName + args[i] + " ";
							else
								displayName = displayName + args[i];
						}
						reward.setDisplayname(displayName);
						displayName = MobHunting.getConfigManager().dropMoneyOnGroundItemtype.equalsIgnoreCase("ITEM")
								? plugin.getRewardManager().format(reward.getMoney())
								: (reward.getMoney() == 0 ? reward.getDisplayname()
										: reward.getDisplayname() + " ("
												+ plugin.getRewardManager().format(reward.getMoney()) + ")");
						ItemMeta im = itemInHand.getItemMeta();
						im.setDisplayName(ChatColor.valueOf(MobHunting.getConfigManager().dropMoneyOnGroundTextColor)
								+ displayName);
						im.setLore(reward.getHiddenLore());
						itemInHand.setItemMeta(im);
					} else {
						plugin.getMessages().senderSendMessage(sender,
								Messages.getString("mobhunting.commands.head.headmustbeinhand"));
					}
				} else {
					plugin.getMessages().senderSendMessage(sender,
							ChatColor.RED + Messages.getString("mobhunting.commands.base.nopermission", "perm",
									"mobhunting.head.rename", "command", "head"));
				}
			} else {
				plugin.getMessages().senderSendMessage(sender, "You can only rename heads ingame.");
			}
			return true;
		} else if (args.length == 2 && (args[0].equalsIgnoreCase("value"))) {
			// mh head value [number] - to change the value of the item in
			// hand.
			if (sender instanceof Player) {
				Player player = (Player) sender;
				if (sender.hasPermission("mobhunting.head.value")) {
					ItemStack itemInHand = player.getItemInHand();
					if (Reward.isReward(itemInHand)) {
						Reward reward = Reward.getReward(itemInHand);

						// get amount
						Double money;
						try {
							money = Double.valueOf(args[1]);
						} catch (NumberFormatException e) {
							plugin.getMessages().senderSendMessage(sender,
									Messages.getString("mobhunting.commands.base.not_a_number", "number", args[1]));
							return false;
						}
						reward.setMoney(money);
						String displayName = MobHunting.getConfigManager().dropMoneyOnGroundItemtype
								.equalsIgnoreCase("ITEM")
										? plugin.getRewardManager().format(reward.getMoney())
										: (reward.getMoney() == 0 ? reward.getDisplayname()
												: reward.getDisplayname() + " ("
														+ plugin.getRewardManager().format(reward.getMoney()) + ")");
						ItemMeta im = itemInHand.getItemMeta();
						im.setDisplayName(ChatColor.valueOf(MobHunting.getConfigManager().dropMoneyOnGroundTextColor)
								+ displayName);
						im.setLore(reward.getHiddenLore());
						itemInHand.setItemMeta(im);
					} else {
						plugin.getMessages().senderSendMessage(sender,
								Messages.getString("mobhunting.commands.head.headmustbeinhand"));
					}
				} else {
					plugin.getMessages().senderSendMessage(sender,
							ChatColor.RED + Messages.getString("mobhunting.commands.base.nopermission", "perm",
									"mobhunting.head.value", "command", "head"));
				}
			} else {
				plugin.getMessages().senderSendMessage(sender, "You can only give a head a new value ingame.");
			}
			return true;

		} else if (args.length >= 1 && (args[0].equalsIgnoreCase("drop") || args[0].equalsIgnoreCase("place"))) {
			// /mh head drop <head>
			// /mh head drop <head> <player>
			// /mh head drop <head> <x> <y> <z> <world>
			if (sender.hasPermission("mobhunting.head.drop")) {

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
						if (mob == MinecraftMob.PvpPlayer) {
							OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
							player.getWorld().dropItem(location,
									customItems.getCustomHead(mob, args[1], 1, money, offlinePlayer.getUniqueId()));
						} else
							player.getWorld().dropItem(location, customItems.getCustomHead(mob, mob.getFriendlyName(),
									1, money, mob.getPlayerUUID()));

					} else if (args.length == 3) {
						if (Bukkit.getServer().getOfflinePlayer(args[2]).isOnline()) {
							OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
							Player player = ((Player) Bukkit.getServer().getOfflinePlayer(args[2]));
							Location location = Misc.getTargetBlock(player, 3).getLocation();
							if (mob == MinecraftMob.PvpPlayer)
								player.getWorld().dropItem(location,
										customItems.getCustomHead(mob, args[1], 1, money, offlinePlayer.getUniqueId()));
							else
								player.getWorld().dropItem(location, customItems.getCustomHead(mob,
										mob.getFriendlyName(), 1, money, offlinePlayer.getUniqueId()));

						} else {
							plugin.getMessages().senderSendMessage(sender, ChatColor.RED + Messages
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
							ItemStack head = customItems.getCustomHead(mob, args[1], 1, money, player.getUniqueId());
							plugin.getRewardManager().setDisplayNameAndHiddenLores(head, args[1], money,
									player.getUniqueId(), player.getUniqueId());
							world.dropItem(location, head);
						} else {
							ItemStack head = customItems.getCustomHead(mob, mob.getFriendlyName(), 1, money,
									mob.getPlayerUUID());
							plugin.getRewardManager().setDisplayNameAndHiddenLores(head, mob.getFriendlyName(), money,
									mob.getPlayerUUID(), mob.getPlayerUUID());
							world.dropItem(location, head);
						}
					}
				} else {
					plugin.getMessages().senderSendMessage(sender,
							Messages.getString("mobhunting.commands.head.unknown_name", "playername", args[1]));
				}

			} else {
				plugin.getMessages().senderSendMessage(sender,
						ChatColor.RED + Messages.getString("mobhunting.commands.base.nopermission", "perm",
								"mobhunting.head.drop", "command", "head"));
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
				items.add("value");
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
