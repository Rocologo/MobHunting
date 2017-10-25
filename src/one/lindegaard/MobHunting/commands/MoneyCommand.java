package one.lindegaard.MobHunting.commands;

import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.compatibility.BossShopCompat;
import one.lindegaard.MobHunting.compatibility.BossShopHelper;
import one.lindegaard.MobHunting.rewards.CustomItems;
import one.lindegaard.MobHunting.rewards.Reward;
import one.lindegaard.MobHunting.rewards.RewardManager;
import one.lindegaard.MobHunting.storage.PlayerSettings;
import one.lindegaard.MobHunting.util.Misc;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.clip.placeholderapi.PlaceholderAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MoneyCommand implements ICommand {

	private MobHunting plugin;

	public MoneyCommand(MobHunting plugin) {
		this.plugin = plugin;
	}

	// Admin command
	// /mh money drop <amount> - to drop <amount money> where player look.
	// Permission needed mobhunt.money.drop

	// /mh money drop <playername> <amount> - to drop <amount money> where
	// player look.
	// Permission needed mobhunt.money.drop

	// /mh money sell - to sell all bag of gold you are holding in your hand.
	// Permission needed mobhunt.money.sell

	// /mh money sell <amount> - to sell <amount money> of the bag of gold you
	// have in your hand.
	// Permission needed mobhunt.money.sell

	@Override
	public String getName() {
		return "money";
	}

	@Override
	public String[] getAliases() {
		return new String[] { "gold", "bag", MobHunting.getConfigManager().dropMoneyOnGroundMoneyCommandAlias };
	}

	@Override
	public String getPermission() {
		return null; // "mobhunting.money";
	}

	@Override
	public String[] getUsageString(String label, CommandSender sender) {
		return new String[] {
				ChatColor.GOLD + MobHunting.getConfigManager().dropMoneyOnGroundMoneyCommandAlias + ChatColor.GREEN
						+ " drop <amount>" + ChatColor.WHITE + " - to drop <amount> of "
						+ MobHunting.getConfigManager().dropMoneyOnGroundSkullRewardName.trim() + ", where you look.",
				ChatColor.GOLD + MobHunting.getConfigManager().dropMoneyOnGroundMoneyCommandAlias + ChatColor.GREEN
						+ " drop <playername> " + ChatColor.YELLOW + "<amount>" + ChatColor.WHITE
						+ " - to drop <amount> of "
						+ MobHunting.getConfigManager().dropMoneyOnGroundSkullRewardName.trim()
						+ " 3 block in front of the <player>.",
				ChatColor.GOLD + MobHunting.getConfigManager().dropMoneyOnGroundMoneyCommandAlias + ChatColor.GREEN
						+ " give <player>" + ChatColor.YELLOW + " <amount>" + ChatColor.WHITE
						+ " - to give the player a "
						+ MobHunting.getConfigManager().dropMoneyOnGroundSkullRewardName.trim() + " in his inventory.",
				ChatColor.GOLD + MobHunting.getConfigManager().dropMoneyOnGroundMoneyCommandAlias + ChatColor.GREEN
						+ " take <player>" + ChatColor.YELLOW + " <amount>" + ChatColor.WHITE
						+ " - to take <amount> gold from the "
						+ MobHunting.getConfigManager().dropMoneyOnGroundSkullRewardName.trim()
						+ " in the players inventory",
				ChatColor.GOLD + MobHunting.getConfigManager().dropMoneyOnGroundMoneyCommandAlias + ChatColor.GREEN
						+ " sell" + ChatColor.WHITE + " - to sell the "
						+ MobHunting.getConfigManager().dropMoneyOnGroundSkullRewardName.trim() + " in your hand.",
				ChatColor.GOLD + MobHunting.getConfigManager().dropMoneyOnGroundMoneyCommandAlias + ChatColor.GREEN
						+ " sell" + ChatColor.YELLOW + " <amount>" + ChatColor.WHITE
						+ " - to sell some of the gold in your "
						+ MobHunting.getConfigManager().dropMoneyOnGroundSkullRewardName.trim() + " and get the money.",
				ChatColor.GOLD + MobHunting.getConfigManager().dropMoneyOnGroundMoneyCommandAlias + ChatColor.GREEN
						+ " buy" + ChatColor.YELLOW + " <amount>" + ChatColor.WHITE
						+ " - to buy some more gold with your money and put it into your "
						+ MobHunting.getConfigManager().dropMoneyOnGroundSkullRewardName.trim() + ".",
				ChatColor.GOLD + MobHunting.getConfigManager().dropMoneyOnGroundMoneyCommandAlias + ChatColor.GREEN
						+ " shop" + ChatColor.WHITE + " - to open the MobHunting BossShop." };
	}

	@Override
	public String getDescription() {
		return Messages.getString("mobhunting.commands.money.description", "rewardname",
				MobHunting.getConfigManager().dropMoneyOnGroundSkullRewardName.trim());
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

		CustomItems customItems = new CustomItems(plugin);

		if (args.length == 1) {
			// /mh money help
			// Show help
			if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?"))
				return false;
		}

		if (args.length == 0
				|| (args.length >= 1 && (args[0].equalsIgnoreCase("balance") || args[0].equalsIgnoreCase("bal")))) {
			// mh money
			// mh money balance
			// mh money balance <player>
			// show the total amount of "bag of gold" in the players inventory.

			if (sender.hasPermission("mobhunting.money.balance") || sender.hasPermission("mobhunting.money.*")) {
				OfflinePlayer offlinePlayer = null;
				boolean other = false;
				if (args.length <= 1) {
					if (!(sender instanceof Player)) {
						plugin.getMessages().senderSendMessage(sender, ChatColor.RED
								+ Messages.getString("mobhunting.commands.base.noconsole", "command", "'money sell'"));
						return true;
					} else
						offlinePlayer = (Player) sender;

				} else {
					if (sender.hasPermission("mobhunting.money.balance.other")
							|| sender.hasPermission("mobhunting.money.*")) {
						offlinePlayer = Bukkit.getServer().getOfflinePlayer(args[1]);
						other = true;
					} else {
						plugin.getMessages().senderSendMessage(sender,
								ChatColor.RED + Messages.getString("mobhunting.commands.base.nopermission", "perm",
										"mobhunting.money.balance.other", "command", "money <playername>"));
						return true;
					}
				}

				double balance = plugin.getPlayerSettingsmanager().getBalance(offlinePlayer);

				if (other)
					plugin.getMessages().senderSendMessage(sender,
							ChatColor.GREEN + Messages.getString("mobhunting.commands.money.balance", "playername",
									offlinePlayer.getName(), "money",
									plugin.getRewardManager().getEconomy().format(balance), "rewardname",
									MobHunting.getConfigManager().dropMoneyOnGroundSkullRewardName.trim()));
				else
					plugin.getMessages().senderSendMessage(sender,
							ChatColor.GREEN + Messages.getString("mobhunting.commands.money.balance", "playername",
									"You", "money", plugin.getRewardManager().getEconomy().format(balance),
									"rewardname",
									MobHunting.getConfigManager().dropMoneyOnGroundSkullRewardName.trim()));
			} else {
				plugin.getMessages().senderSendMessage(sender,
						ChatColor.RED + Messages.getString("mobhunting.commands.base.nopermission", "perm",
								"mobhunting.money.balance", "command", "money"));
			}
			return true;

		} else if (args.length == 1 && Bukkit.getServer().getOfflinePlayer(args[0]) == null) {
			plugin.getMessages().senderSendMessage(sender, ChatColor.RED
					+ Messages.getString("mobhunting.commands.base.unknown_playername", "playername", args[0]));
			return true;

		} else if (args.length == 1 && args[0].equalsIgnoreCase("shop")) {
			// /mh money shop - to open a shop, where the player can buy or sell
			// "Bag of gold"

			// MobHunting.registerPlugin(BossShopCompat.class, "BossShop");

			if (sender instanceof Player) {
				Player player = (Player) sender;
				if (BossShopCompat.isSupported()) {
					if (player.hasPermission("mobhunting.money.shop") || sender.hasPermission("mobhunting.money.*")) {
						BossShopHelper.openShop(plugin, player, "Menu");
						return true;
					} else {
						plugin.getMessages().senderSendMessage(sender,
								ChatColor.RED + Messages.getString("mobhunting.commands.base.nopermission", "perm",
										"mobhunting.money.shop", "command", "shop"));
						return true;
					}
				} else {
					plugin.getMessages().senderSendMessage(sender,
							ChatColor.RED + Messages.getString("mobhunting.commands.money.no-bossshop"));
					return true;
				}
			} else {
				// not allowed in console
				plugin.getMessages().senderSendMessage(sender, ChatColor.RED
						+ Messages.getString("mobhunting.commands.base.noconsole", "command", "'money shop'"));
				return true;
			}
		}

		else if (args.length >= 2 && args[0].equalsIgnoreCase("drop") || args[0].equalsIgnoreCase("place"))

		{
			// /mh money drop <amount>
			// /mh money drop <player> <amount>
			if (sender.hasPermission("mobhunting.money.drop") || sender.hasPermission("mobhunting.money.*")) {
				if (args.length == 2 && !(sender instanceof Player)) {
					plugin.getMessages().senderSendMessage(sender,
							ChatColor.RED + Messages.getString("mobhunting.commands.base.playername-missing"));
				} else {
					if (args[1].matches("\\d+(\\.\\d+)?")) {
						Player player = (Player) sender;
						Location location = Misc.getTargetBlock(player, 20).getLocation();
						Messages.debug("The Bag of gold was dropped at %s", location);
						plugin.getRewardManager().dropMoneyOnGround(player, null, location,
								Misc.floor(Double.valueOf(args[1])));
						plugin.getMessages().playerActionBarMessage(player, Messages.getString("mobhunting.moneydrop",
								"rewardname", MobHunting.getConfigManager().dropMoneyOnGroundSkullRewardName.trim(),
								"money",
								plugin.getRewardManager().getEconomy().format(Misc.floor(Double.valueOf(args[1])))));
					} else if (Bukkit.getServer().getOfflinePlayer(args[1]).isOnline()) {
						if (args[2].matches("\\d+(\\.\\d+)?")) {
							Player player = ((Player) Bukkit.getServer().getOfflinePlayer(args[1]));
							Location location = Misc.getTargetBlock(player, 3).getLocation();
							Messages.debug("The Bag of gold was dropped at %s", location);
							plugin.getRewardManager().dropMoneyOnGround(player, null, location,
									Misc.floor(Double.valueOf(args[2])));
							plugin.getMessages().playerActionBarMessage(player,
									Messages.getString("mobhunting.moneydrop", "rewardname",
											MobHunting.getConfigManager().dropMoneyOnGroundSkullRewardName.trim(),
											"money", plugin.getRewardManager().getEconomy()
													.format(Misc.floor(Double.valueOf(args[2])))));
						} else {
							plugin.getMessages().senderSendMessage(sender, ChatColor.RED
									+ Messages.getString("mobhunting.commands.base.not_a_number", "number", args[2]));
						}
					} else {
						plugin.getMessages().senderSendMessage(sender, ChatColor.RED
								+ Messages.getString("mobhunting.commands.base.playername-missing", "player", args[1]));
					}
				}
			} else {
				plugin.getMessages().senderSendMessage(sender,
						ChatColor.RED + Messages.getString("mobhunting.commands.base.nopermission", "perm",
								"mobhunting.money.drop", "command", "money drop"));
			}
			return true;

		} else if (args.length >= 2 && args[0].equalsIgnoreCase("give")) {
			// /mh money give <player> <amount>
			if (sender.hasPermission("mobhunting.money.give") || sender.hasPermission("mobhunting.money.*")) {
				if (args.length == 2 && !(sender instanceof Player)) {
					plugin.getMessages().senderSendMessage(sender,
							ChatColor.RED + Messages.getString("mobhunting.commands.base.playername-missing"));
				} else {
					if (Bukkit.getServer().getOfflinePlayer(args[1]).isOnline()) {
						if (args[2].matches("\\d+(\\.\\d+)?")) {
							Player player = ((Player) Bukkit.getServer().getOfflinePlayer(args[1]));
							if (player.getInventory().firstEmpty() == -1)
								plugin.getRewardManager().dropMoneyOnGround(player, null, player.getLocation(),
										Misc.floor(Double.valueOf(args[2])));
							else {
								ItemStack is = customItems.getCustomtexture(
										UUID.fromString(RewardManager.MH_REWARD_BAG_OF_GOLD_UUID),
										MobHunting.getConfigManager().dropMoneyOnGroundSkullRewardName.trim(),
										MobHunting.getConfigManager().dropMoneyOnGroundSkullTextureValue,
										MobHunting.getConfigManager().dropMoneyOnGroundSkullTextureSignature,
										Misc.floor(Double.valueOf(args[2])), UUID.randomUUID(),
										UUID.fromString(RewardManager.MH_REWARD_BAG_OF_GOLD_UUID));
								player.getInventory().addItem(is);
							}
							plugin.getMessages().playerActionBarMessage(player,
									Messages.getString("mobhunting.commands.money.give", "rewardname",
											MobHunting.getConfigManager().dropMoneyOnGroundSkullRewardName.trim(),
											"money", plugin.getRewardManager().getEconomy()
													.format(Misc.floor(Double.valueOf(args[2])))));
							plugin.getMessages().senderSendMessage(sender, Messages.getString(
									"mobhunting.commands.money.give-sender", "rewardname",
									MobHunting.getConfigManager().dropMoneyOnGroundSkullRewardName.trim(), "money",
									plugin.getRewardManager().getEconomy().format(Misc.floor(Double.valueOf(args[2]))),
									"player", player.getName()));
						} else {
							plugin.getMessages().senderSendMessage(sender, ChatColor.RED
									+ Messages.getString("mobhunting.commands.base.not_a_number", "number", args[2]));
						}
					} else {
						plugin.getMessages().senderSendMessage(sender, ChatColor.RED
								+ Messages.getString("mobhunting.commands.base.playername-missing", "player", args[1]));
					}
				}
			} else {
				plugin.getMessages().senderSendMessage(sender,
						ChatColor.RED + Messages.getString("mobhunting.commands.base.nopermission", "perm",
								"mobhunting.money.give", "command", "money give"));
			}
			return true;
		}

		else if (args.length >= 2 && args[0].equalsIgnoreCase("take")) {
			// /mh money take <player> <amount>
			if (sender.hasPermission("mobhunting.money.take") || sender.hasPermission("mobhunting.money.*")) {
				if (args.length == 2 && !(sender instanceof Player)) {
					plugin.getMessages().senderSendMessage(sender,
							ChatColor.RED + Messages.getString("mobhunting.commands.base.playername-missing"));
				} else {
					if (Bukkit.getServer().getOfflinePlayer(args[1]).isOnline()) {
						if (args[2].matches("\\d+(\\.\\d+)?")) {
							Player player = ((Player) Bukkit.getServer().getOfflinePlayer(args[1]));
							double rest = Misc.floor(Double.valueOf(args[2]));
							double taken = plugin.getRewardManager().adjustBagOfGoldInPlayerInventory(player, rest);
							plugin.getMessages().playerActionBarMessage(player,
									Messages.getString("mobhunting.commands.money.take", "rewardname",
											MobHunting.getConfigManager().dropMoneyOnGroundSkullRewardName.trim(),
											"money", plugin.getRewardManager().getEconomy().format(taken)));
							plugin.getMessages().senderSendMessage(sender,
									Messages.getString("mobhunting.commands.money.take-sender", "rewardname",
											MobHunting.getConfigManager().dropMoneyOnGroundSkullRewardName.trim(),
											"money", plugin.getRewardManager().getEconomy().format(taken), "player",
											player.getName()));
						} else {
							plugin.getMessages().senderSendMessage(sender, ChatColor.RED
									+ Messages.getString("mobhunting.commands.base.not_a_number", "number", args[2]));
						}
					} else {
						plugin.getMessages().senderSendMessage(sender, ChatColor.RED
								+ Messages.getString("mobhunting.commands.base.playername-missing", "player", args[1]));
					}
				}
			} else {
				plugin.getMessages().senderSendMessage(sender,
						ChatColor.RED + Messages.getString("mobhunting.commands.base.nopermission", "perm",
								"mobhunting.money.take", "command", "money take"));
			}
			return true;

		} else if ((args.length == 1 && args[0].equalsIgnoreCase("sell"))
				|| (args.length == 2 && args[0].equalsIgnoreCase("sell") && (args[1].matches("\\d+(\\.\\d+)?")))) {
			// /mh money sell
			// /mh money sell <amount>
			if (sender.hasPermission("mobhunting.money.sell") || sender.hasPermission("mobhunting.money.*")) {
				if (!(sender instanceof Player)) {
					plugin.getMessages().senderSendMessage(sender, ChatColor.RED
							+ Messages.getString("mobhunting.commands.base.noconsole", "command", "'money sell'"));
					return true;
				}

				Player player = (Player) sender;
				if (args.length == 1) {
					ItemStack is = player.getItemInHand();
					if (Reward.isReward(is)) {
						Reward hiddenRewardData = Reward.getReward(is);
						plugin.getRewardManager().getEconomy().depositPlayer(player, hiddenRewardData.getMoney());
						is.setType(Material.AIR);
						is.setAmount(0);
						is.setItemMeta(null);
						player.setItemInHand(is);
						plugin.getMessages().playerActionBarMessage(player,
								Messages.getString("mobhunting.commands.money.sell", "rewardname",
										MobHunting.getConfigManager().dropMoneyOnGroundSkullRewardName.trim(), "money",
										plugin.getRewardManager().getEconomy().format(hiddenRewardData.getMoney())));
					}
				} else if ((args[0].equalsIgnoreCase("sell") && (args[1].matches("\\d+(\\.\\d+)?")))) {
					double sold = 0;
					double toBeSold = Misc.floor(Double.valueOf(args[1]));
					for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
						ItemStack is = player.getInventory().getItem(slot);
						if (Reward.isReward(is) && Reward.getReward(is).getRewardUUID()
								.equals(UUID.fromString(RewardManager.MH_REWARD_BAG_OF_GOLD_UUID))) {
							Reward reward = Reward.getReward(is);
							double saldo = Misc.floor(reward.getMoney());
							if (saldo > toBeSold) {
								reward.setMoney(saldo - toBeSold);
								is = customItems.getCustomtexture(reward.getRewardUUID(),
										MobHunting.getConfigManager().dropMoneyOnGroundSkullRewardName.trim(),
										MobHunting.getConfigManager().dropMoneyOnGroundSkullTextureValue,
										MobHunting.getConfigManager().dropMoneyOnGroundSkullTextureSignature,
										saldo - toBeSold, UUID.randomUUID(), reward.getSkinUUID());
								player.getInventory().setItem(slot, is);
								sold = sold + toBeSold;
								toBeSold = 0;
								break;
							} else {
								is.setItemMeta(null);
								is.setType(Material.AIR);
								is.setAmount(0);
								player.getInventory().setItem(slot, is);
								sold = sold + saldo;
								toBeSold = toBeSold - saldo;
							}
						}
					}
					plugin.getRewardManager().getEconomy().depositPlayer(player, sold);
					plugin.getMessages().playerActionBarMessage(player,
							Messages.getString("mobhunting.commands.money.sell", "rewardname",
									MobHunting.getConfigManager().dropMoneyOnGroundSkullRewardName.trim(), "money",
									plugin.getRewardManager().getEconomy().format(sold)));
				}
			} else {
				plugin.getMessages().senderSendMessage(sender,
						ChatColor.RED + Messages.getString("mobhunting.commands.base.nopermission", "perm",
								"mobhunting.money.sell", "command", "money sell"));
			}
			return true;
		}

		else if (args.length >= 2 && args[0].equalsIgnoreCase("buy")) {
			// /mh money buy <amount>
			if (sender.hasPermission("mobhunting.money.buy") || sender.hasPermission("mobhunting.money.*")) {
				if (args.length == 2 && args[1].matches("\\d+(\\.\\d+)?")) {
					Player player = (Player) sender;
					if (plugin.getRewardManager().getEconomy().has(player, Misc.floor(Double.valueOf(args[1])))) {
						if (player.getInventory().firstEmpty() == -1)
							plugin.getRewardManager().dropMoneyOnGround(player, null, player.getLocation(),
									Misc.floor(Double.valueOf(args[1])));
						else {
							ItemStack is = customItems.getCustomtexture(
									UUID.fromString(RewardManager.MH_REWARD_BAG_OF_GOLD_UUID),
									MobHunting.getConfigManager().dropMoneyOnGroundSkullRewardName.trim(),
									MobHunting.getConfigManager().dropMoneyOnGroundSkullTextureValue,
									MobHunting.getConfigManager().dropMoneyOnGroundSkullTextureSignature,
									Misc.floor(Double.valueOf(args[1])), UUID.randomUUID(),
									UUID.fromString(RewardManager.MH_REWARD_BAG_OF_GOLD_UUID));
							player.getInventory().addItem(is);
						}
						plugin.getRewardManager().getEconomy().withdrawPlayer(player,
								Misc.floor(Double.valueOf(args[1])));
						plugin.getMessages().playerActionBarMessage(player, Messages.getString(
								"mobhunting.commands.money.buy", "rewardname",
								MobHunting.getConfigManager().dropMoneyOnGroundSkullRewardName, "money",
								plugin.getRewardManager().getEconomy().format(Misc.floor(Double.valueOf(args[1])))));
					} else {
						plugin.getMessages().senderSendMessage(sender, ChatColor.RED
								+ Messages.getString("mobhunting.commands.money.not-enough-money", "money", args[1]));
					}
				} else {
					plugin.getMessages().senderSendMessage(sender, ChatColor.RED
							+ Messages.getString("mobhunting.commands.base.not_a_number", "number", args[1]));
				}
			} else {
				plugin.getMessages().senderSendMessage(sender,
						ChatColor.RED + Messages.getString("mobhunting.commands.base.nopermission", "perm",
								"mobhunting.money.buy", "command", "money buy"));
			}
			return true;
		}

		return false;

	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String label, String[] args) {
		ArrayList<String> items = new ArrayList<String>();
		if (args.length == 1) {
			items.add("drop");
			items.add("give");
			items.add("take");
			items.add("sell");
			items.add("buy");
			items.add("shop");
		} else if (args.length == 2)
			for (Player player : Bukkit.getOnlinePlayers())
				items.add(player.getName());

		if (!args[args.length - 1].trim().isEmpty()) {
			String match = args[args.length - 1].trim().toLowerCase();

			items.removeIf(name -> !name.toLowerCase().startsWith(match));
		}
		return items;
	}
}
