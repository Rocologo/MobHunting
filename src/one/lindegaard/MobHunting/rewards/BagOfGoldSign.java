package one.lindegaard.MobHunting.rewards;

import one.lindegaard.Core.Core;
import one.lindegaard.Core.materials.Materials;
import one.lindegaard.Core.rewards.CoreCustomItems;
import one.lindegaard.Core.rewards.Reward;
import one.lindegaard.Core.rewards.RewardType;
import one.lindegaard.Core.server.Servers;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.compatibility.BagOfGoldCompat;
import one.lindegaard.MobHunting.util.Misc;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BagOfGoldSign implements Listener {

	private MobHunting plugin;

	// The Constructor is called from the RewardManager and only if
	// dropMoneyOnGroundUseAsCurrency is true
	public BagOfGoldSign(MobHunting plugin) {
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	// ****************************************************************************'
	// Events
	// ****************************************************************************'
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public void onPlayerInteractEvent(PlayerInteractEvent event) {
		if (event.isCancelled())
			return;

		if (Servers.isMC19OrNewer() && (event.getHand() == null || event.getHand().equals(EquipmentSlot.OFF_HAND)))
			return;

		Block clickedBlock = event.getClickedBlock();
		if (clickedBlock != null && isBagOfGoldSign(clickedBlock) && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Player player = event.getPlayer();
			if (player.hasPermission("mobhunting.bagofgoldsign.use")) {
				Sign sign = ((Sign) clickedBlock.getState());
				String signType = sign.getLine(1);
				double money = 0;
				double moneyInHand = 0;
				double moneyOnSign = 0;
				int numberOfHeads = 1;
				// SELL BagOfGold Sign
				if (signType.equalsIgnoreCase(plugin.getMessages().getString("mobhunting.bagofgoldsign.line2.sell"))) {
					if (Reward.isReward(player.getItemInHand())) {
						Reward reward = Reward.getReward(player.getItemInHand());

						if (BagOfGoldCompat.isSupported() && reward.isBagOfGoldReward()) {
							plugin.getMessages().playerSendMessage(player,
									plugin.getMessages().getString("mobhunting.money.you_cant_sell_and_buy_bagofgold",
											"itemname", reward.getDisplayName()));
							return;
						}

						numberOfHeads = player.getItemInHand().getAmount();
						moneyInHand = reward.getMoney() * numberOfHeads;
						if (moneyInHand == 0) {
							plugin.getMessages().playerSendMessage(player, plugin.getMessages().getString(
									"mobhunting.bagofgoldsign.item_has_no_value", "itemname", reward.getDisplayName()));
							return;
						}

						if (sign.getLine(2).isEmpty() || sign.getLine(2).equalsIgnoreCase(
								plugin.getMessages().getString("mobhunting.bagofgoldsign.line3.everything"))) {
							money = moneyInHand;
							moneyOnSign = moneyInHand;
						} else {
							try {
								moneyOnSign = Double.valueOf(sign.getLine(2));
								money = moneyInHand <= moneyOnSign ? moneyInHand : moneyOnSign;
							} catch (NumberFormatException e) {
								plugin.getMessages().debug("Line no. 3 is not a number");
								plugin.getMessages().playerSendMessage(player, plugin.getMessages().getString(
										"mobhunting.bagofgoldsign.line3.not_a_number", "number", sign.getLine(2),
										"everything",
										plugin.getMessages().getString("mobhunting.bagofgoldsign.line3.everything")));
								return;
							}
						}
						plugin.getEconomyManager().depositPlayer(player, money);
						if (moneyInHand <= moneyOnSign) {
							if (Servers.isMC19OrNewer()) {
								event.getItem().setAmount(0);
								event.getItem().setItemMeta(null);
								event.getItem().setType(Material.AIR);
							} else {
								event.getPlayer().setItemInHand(new ItemStack(Material.AIR));
							}
						} else {
							reward.setMoney((moneyInHand - moneyOnSign)/numberOfHeads);
							ItemMeta im = event.getItem().getItemMeta();
							im.setLore(reward.getHiddenLore());
							String displayName = Core.getConfigManager().rewardItemtype
									.equalsIgnoreCase("ITEM") ? plugin.getRewardManager().format(reward.getMoney())
											: reward.getDisplayName() + " ("
													+ plugin.getRewardManager().format(reward.getMoney()) + ")";
							im.setDisplayName(ChatColor.valueOf(Core.getConfigManager().rewardTextColor)
									+ displayName);
							event.getItem().setItemMeta(im);
						}
						plugin.getMessages().debug("%s sold his %s for %s", player.getName(), reward.getDisplayName(),
								plugin.getEconomyManager().format(money));
						plugin.getMessages().playerSendMessage(player, plugin.getMessages().getString(
								"mobhunting.bagofgoldsign.sold", "money", plugin.getEconomyManager().format(money),
								"rewardname",
								ChatColor.valueOf(Core.getConfigManager().rewardTextColor)
										+ (Core.getConfigManager().rewardItemtype.equalsIgnoreCase("ITEM")
												? Core.getConfigManager().bagOfGoldName.trim()
												: reward.getDisplayName())));
					} else {
						plugin.getMessages().debug("Player does not hold a bag of gold in his hand");
						plugin.getMessages().playerSendMessage(player,
								plugin.getMessages().getString("mobhunting.bagofgoldsign.hold_reward_in_hand",
										"rewardname",
										ChatColor.valueOf(Core.getConfigManager().rewardTextColor)
												+ Core.getConfigManager().bagOfGoldName.trim()));
					}

					// BUY BagOfGold Sign
				} else if (signType
						.equalsIgnoreCase(plugin.getMessages().getString("mobhunting.bagofgoldsign.line2.buy"))) {
					if (BagOfGoldCompat.isSupported()) {
						plugin.getMessages().playerSendMessage(player,
								plugin.getMessages().getString("mobhunting.money.you_cant_sell_and_buy_bagofgold",
										"itemname",
										ChatColor.valueOf(Core.getConfigManager().rewardTextColor)
												+ Core.getConfigManager().bagOfGoldName.trim()));
						return;
					}
					try {
						moneyOnSign = Double.valueOf(sign.getLine(2));
					} catch (NumberFormatException e) {
						plugin.getMessages().debug("Line no. 3 is not a number");
						plugin.getMessages().playerSendMessage(player,
								plugin.getMessages().getString("mobhunting.bagofgoldsign.line3.not_a_number", "number",
										sign.getLine(2), "everything",
										plugin.getMessages().getString("mobhunting.bagofgoldsign.line3.everything")));
						return;
					}
					if (plugin.getEconomyManager().getBalance(player) >= moneyOnSign) {

						boolean found = false;
						for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
							ItemStack is = player.getInventory().getItem(slot);
							if (Reward.isReward(is)) {
								Reward hrd = Reward.getReward(is);
								if ((hrd.isBagOfGoldReward() || hrd.isItemReward())
										&& hrd.getRewardType().equals(hrd.getRewardType())) {
									ItemMeta im = is.getItemMeta();
									Reward newReward = Reward.getReward(is);
									newReward.setMoney(newReward.getMoney() + moneyOnSign);
									im.setLore(newReward.getHiddenLore());
									String displayName = Core.getConfigManager().rewardItemtype
											.equalsIgnoreCase("ITEM")
													? plugin.getRewardManager().format(newReward.getMoney())
													: newReward.getDisplayName() + " ("
															+ plugin.getRewardManager().format(newReward.getMoney())
															+ ")";
									im.setDisplayName(
											ChatColor.valueOf(Core.getConfigManager().rewardTextColor)
													+ displayName);
									is.setItemMeta(im);
									is.setAmount(1);
									event.setCancelled(true);
									plugin.getMessages().debug("Added %s to item in slot %s, new value is %s",
											plugin.getRewardManager().format(hrd.getMoney()), slot,
											plugin.getRewardManager().format(newReward.getMoney()));
									found = true;
									break;
								}
							}
						}

						if (!found) {

							if (player.getInventory().firstEmpty() == -1)
								plugin.getRewardManager().dropMoneyOnGround_RewardManager(player, null,
										player.getLocation(), Misc.ceil(moneyOnSign));
							else {
								ItemStack is;
								if (Core.getConfigManager().rewardItemtype.equalsIgnoreCase("ITEM")) {
									is = Reward.setDisplayNameAndHiddenLores(
											new ItemStack(
													Material.valueOf(Core.getConfigManager().rewardItem)),
											new Reward(
													Core.getConfigManager().bagOfGoldName.trim(),
													Misc.ceil(moneyOnSign), RewardType.ITEM, null));
								} else
									is = new CoreCustomItems().getCustomtexture(new Reward(
											Core.getConfigManager().bagOfGoldName.trim(),
											Misc.ceil(moneyOnSign), RewardType.BAGOFGOLD, UUID.fromString(RewardType.BAGOFGOLD.getUUID())),
											Core.getConfigManager().skullTextureValue,
											Core.getConfigManager().skullTextureSignature);
								player.getInventory().addItem(is);
								found = true;
							}

						}

						// IF okay the withdraw money
						if (found) {
							plugin.getEconomyManager().withdrawPlayer(player, moneyOnSign);
							plugin.getMessages().playerSendMessage(player, plugin.getMessages().getString(
									"mobhunting.bagofgoldsign.bought", "money",
									plugin.getEconomyManager().format(moneyOnSign), "rewardname",
									ChatColor.valueOf(Core.getConfigManager().rewardTextColor)
											+ Core.getConfigManager().bagOfGoldName.trim()));
						}
					} else {
						plugin.getMessages().playerSendMessage(player,
								plugin.getMessages().getString("mobhunting.bagofgoldsign.not_enough_money"));
					}
				} else if (signType
						.equalsIgnoreCase(plugin.getMessages().getString("mobhunting.bagofgoldsign.line2.balance"))) {
					if (BagOfGoldCompat.isSupported() || !plugin.getConfigManager().dropMoneyOnGroundUseItemAsCurrency)
						if (plugin.getEconomyManager().isBankOwner(player.getUniqueId().toString(), player)) {
							plugin.getMessages().playerActionBarMessageQueue(player,
									plugin.getMessages().getString("mobhunting.bagofgoldsign.balance2", "balance",
											plugin.getEconomyManager()
													.format(plugin.getEconomyManager().getBalance(player)),
											"bankbalance", plugin.getEconomyManager()
													.format(plugin.getEconomyManager().getBankBalance(player))));
						} else {
							plugin.getMessages().playerActionBarMessageQueue(player, plugin.getMessages().getString(
									"mobhunting.bagofgoldsign.balance1", "balance",
									plugin.getEconomyManager().format(plugin.getEconomyManager().getBalance(player))));

						}
					else {
						double amountInInventory = 0;
						for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
							ItemStack is = player.getInventory().getItem(slot);
							if (Reward.isReward(is)) {
								Reward reward = Reward.getReward(is);
								if (reward.isBagOfGoldReward())
									amountInInventory = amountInInventory + reward.getMoney();
							}
						}
						plugin.getMessages().playerActionBarMessageQueue(player,
								plugin.getMessages().getString("mobhunting.bagofgoldsign.balance1", "balance",
										plugin.getEconomyManager().format(amountInInventory)));
					}
				}
			} else {
				plugin.getMessages().playerSendMessage(player, plugin.getMessages().getString(
						"mobhunting.bagofgoldsign.no_permission_to_use", "perm", "mobhunting.bagofgoldsign.use"));
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public void onSignChangeEvent(SignChangeEvent event) {
		if (event.isCancelled())
			return;

		Player player = event.getPlayer();
		if (isBagOfGoldSign(event.getLine(0))) {
			if (event.getPlayer().hasPermission("mobhunting.bagofgoldsign.create")) {

				// Check line 2
				if (ChatColor.stripColor(event.getLine(1)).equalsIgnoreCase(
						ChatColor.stripColor(plugin.getMessages().getString("mobhunting.bagofgoldsign.line2.sell")))) {
					event.setLine(1, plugin.getMessages().getString("mobhunting.bagofgoldsign.line2.sell"));

				} else if (ChatColor.stripColor(event.getLine(1)).equalsIgnoreCase(
						ChatColor.stripColor(plugin.getMessages().getString("mobhunting.bagofgoldsign.line2.buy")))) {
					event.setLine(1, plugin.getMessages().getString("mobhunting.bagofgoldsign.line2.buy"));
				} else if (ChatColor.stripColor(event.getLine(1)).equalsIgnoreCase(ChatColor
						.stripColor(plugin.getMessages().getString("mobhunting.bagofgoldsign.line2.balance")))) {
					event.setLine(1, plugin.getMessages().getString("mobhunting.bagofgoldsign.line2.balance"));
				} else {
					plugin.getMessages().playerSendMessage(player,
							plugin.getMessages().getString("mobhunting.bagofgoldsign.line2.mustbe_sell_or_buy"));
					event.setLine(3, plugin.getMessages().getString("mobhunting.bagofgoldsign.line4.error_on_sign",
							"line", "2"));
					return;
				}

				// Check line 3
				if (ChatColor.stripColor(event.getLine(1)).equalsIgnoreCase(ChatColor
						.stripColor(plugin.getMessages().getString("mobhunting.bagofgoldsign.line2.balance")))) {
					event.setLine(2, "");
					event.setLine(3, "");
				} else {
					if (event.getLine(2).isEmpty() || ChatColor.stripColor(event.getLine(2)).equalsIgnoreCase(ChatColor
							.stripColor(plugin.getMessages().getString("mobhunting.bagofgoldsign.line3.everything")))) {
						event.setLine(2, plugin.getMessages().getString("mobhunting.bagofgoldsign.line3.everything"));
					} else {

						try {
							if (Double.valueOf(event.getLine(2)) > 0) {
								plugin.getMessages().debug("%s created a BagOfGold Sign", event.getPlayer().getName());
							}
						} catch (NumberFormatException e) {
							plugin.getMessages().debug("Line no. 3 is not positive a number");
							plugin.getMessages().playerSendMessage(player, plugin.getMessages().getString(
									"mobhunting.bagofgoldsign.line3.not_a_number", "number", event.getLine(2),
									"everything",
									plugin.getMessages().getString("mobhunting.bagofgoldsign.line3.everything")));
							event.setLine(3, plugin.getMessages()
									.getString("mobhunting.bagofgoldsign.line4.error_on_sign", "line", "3"));
							return;
						}
					}
				}

				event.setLine(0, plugin.getMessages().getString("mobhunting.bagofgoldsign.line1", "rewardname",
						Core.getConfigManager().bagOfGoldName.trim()));
				event.setLine(3, plugin.getMessages().getString("mobhunting.bagofgoldsign.line4.ok"));

			} else {
				plugin.getMessages().playerSendMessage(player, plugin.getMessages().getString(
						"mobhunting.bagofgoldsign.no_permission", "perm", "mobhunting.bagofgoldsign.create"));
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public void onBlockBreakEvent(BlockBreakEvent event) {
		if (event.isCancelled())
			return;

		Block b = event.getBlock();
		if (isBagOfGoldSign(b)) {
			if (event.getPlayer().hasPermission("mobhunting.bagofgoldsign.destroy")) {
				plugin.getMessages().debug("%s destroyed a BagOfGold sign", event.getPlayer().getName());
			} else {
				plugin.getMessages().debug("%s tried to destroy a BagOfGold sign without permission",
						event.getPlayer().getName());
				event.getPlayer().sendMessage(plugin.getMessages().getString("mobhunting.bagofgoldsign.no_permission",
						"perm", "mobhunting.bagofgoldsign.destroy"));
				event.setCancelled(true);
			}
		}
	}

	// ************************************************************************************
	// TESTS
	// ************************************************************************************

	public boolean isBagOfGoldSign(Block block) {
		if (Materials.isSign(block))
			return ChatColor.stripColor(((Sign) block.getState()).getLine(0)).equalsIgnoreCase(
					ChatColor.stripColor(plugin.getMessages().getString("mobhunting.bagofgoldsign.line1", "rewardname",
							Core.getConfigManager().bagOfGoldName.trim())))
					|| ChatColor.stripColor(((Sign) block.getState()).getLine(0)).equalsIgnoreCase("[bagofgold]");
		return false;
	}

	public boolean isBagOfGoldSign(String line) {
		return ChatColor.stripColor(line)
				.equalsIgnoreCase(ChatColor.stripColor(plugin.getMessages().getString("mobhunting.bagofgoldsign.line1",
						"rewardname", Core.getConfigManager().bagOfGoldName.trim())))
				|| ChatColor.stripColor(line).equalsIgnoreCase("[bagofgold]");
	}

}
