package one.lindegaard.MobHunting.rewards;

import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
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

		if (Misc.isMC19OrNewer() && (event.getHand() == null || event.getHand().equals(EquipmentSlot.OFF_HAND)))
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
				// SELL BagOfGold Sign
				if (signType.equalsIgnoreCase(Messages.getString("mobhunting.bagofgoldsign.line2.sell"))) {
					if (player.getItemInHand().getType().equals(Material.SKULL_ITEM)
							&& Reward.isReward(player.getItemInHand())) {
						Reward reward = Reward.getReward(player.getItemInHand());
						moneyInHand = reward.getMoney();
						if (sign.getLine(2).isEmpty() || sign.getLine(2)
								.equalsIgnoreCase(Messages.getString("mobhunting.bagofgoldsign.line3.everything"))) {
							money = moneyInHand;
							moneyOnSign = moneyInHand;
						} else {
							try {
								moneyOnSign = Double.valueOf(sign.getLine(2));
								money = moneyInHand <= moneyOnSign ? moneyInHand : moneyOnSign;
							} catch (NumberFormatException e) {
								Messages.debug("Line no. 3 is not a number");
								plugin.getMessages().playerSendMessage(player,
										Messages.getString("mobhunting.bagofgoldsign.line3.not_a_number", "number",
												sign.getLine(2), "everything", Messages
														.getString("mobhunting.bagofgoldsign.line3.everything")));
								return;
							}
						}
						plugin.getRewardManager().getEconomy().depositPlayer(player, money);
						if (moneyInHand <= moneyOnSign) {
							if (Misc.isMC19OrNewer()) {
								event.getItem().setAmount(0);
								event.getItem().setItemMeta(null);
								event.getItem().setType(Material.AIR);
							} else {
								event.getPlayer().setItemInHand(new ItemStack(0));
							}
						} else {
							reward.setMoney(moneyInHand - moneyOnSign);
							ItemMeta im = event.getItem().getItemMeta();
							im.setLore(reward.getHiddenLore());
							String displayName = MobHunting.getConfigManager().dropMoneyOnGroundItemtype
									.equalsIgnoreCase("ITEM") ? plugin.getRewardManager().format(reward.getMoney())
											: reward.getDisplayname() + " ("
													+ plugin.getRewardManager().format(reward.getMoney()) + ")";
							im.setDisplayName(
									ChatColor.valueOf(MobHunting.getConfigManager().dropMoneyOnGroundTextColor)
											+ displayName);
							event.getItem().setItemMeta(im);
						}
						Messages.debug("%s sold his %s for %s", player.getName(), reward.getDisplayname(),
								plugin.getRewardManager().getEconomy().format(money));
						plugin.getMessages().playerSendMessage(player,
								Messages.getString("mobhunting.bagofgoldsign.sold", "money",
										plugin.getRewardManager().getEconomy().format(money), "rewardname", ChatColor
												.valueOf(MobHunting.getConfigManager().dropMoneyOnGroundTextColor)
												+ reward.getDisplayname().trim()));
					} else {
						Messages.debug("Player does not hold a bag of gold in his hand");
						plugin.getMessages().playerSendMessage(player, Messages.getString(
								"mobhunting.bagofgoldsign.hold_bag_in_hand", "rewardname",
								ChatColor.valueOf(MobHunting.getConfigManager().dropMoneyOnGroundTextColor)
										+ MobHunting.getConfigManager().dropMoneyOnGroundSkullRewardName.trim()));
					}

					// BUY BagOfGold Sign
				} else if (signType.equalsIgnoreCase(Messages.getString("mobhunting.bagofgoldsign.line2.buy"))) {
					try {
						moneyOnSign = Double.valueOf(sign.getLine(2));
					} catch (NumberFormatException e) {
						Messages.debug("Line no. 3 is not a number");
						plugin.getMessages().playerSendMessage(player,
								Messages.getString("mobhunting.bagofgoldsign.line3.not_a_number", "number",
										sign.getLine(2), "everything",
										Messages.getString("mobhunting.bagofgoldsign.line3.everything")));
						return;
					}
					if (plugin.getRewardManager().getEconomy().getBalance(player) >= moneyOnSign) {

						boolean found = false;
						for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
							ItemStack is = player.getInventory().getItem(slot);
							if (Reward.isReward(is)) {
								Reward hrd = Reward.getReward(is);
								if ((hrd.isBagOfGoldReward() || hrd.isItemReward())
										&& hrd.getRewardUUID().equals(hrd.getRewardUUID())) {
									ItemMeta im = is.getItemMeta();
									Reward newReward = Reward.getReward(is);
									newReward.setMoney(newReward.getMoney() + moneyOnSign);
									im.setLore(newReward.getHiddenLore());
									String displayName = MobHunting.getConfigManager().dropMoneyOnGroundItemtype
											.equalsIgnoreCase("ITEM")
													? plugin.getRewardManager().format(newReward.getMoney())
													: newReward.getDisplayname() + " ("
															+ plugin.getRewardManager().format(newReward.getMoney())
															+ ")";
									im.setDisplayName(
											ChatColor.valueOf(MobHunting.getConfigManager().dropMoneyOnGroundTextColor)
													+ displayName);
									is.setItemMeta(im);
									is.setAmount(1);
									event.setCancelled(true);
									Messages.debug("Added %s to item in slot %s, new value is %s",
											plugin.getRewardManager().format(hrd.getMoney()), slot,
											plugin.getRewardManager().format(newReward.getMoney()));
									found = true;
									break;
								}
							}
						}

						if (!found) {

							if (player.getInventory().firstEmpty() == -1)
								plugin.getRewardManager().dropMoneyOnGround(player, null, player.getLocation(),
										Misc.ceil(moneyOnSign));
							else {
								ItemStack is = new CustomItems(plugin).getCustomtexture(
										UUID.fromString(RewardManager.MH_REWARD_BAG_OF_GOLD_UUID),
										MobHunting.getConfigManager().dropMoneyOnGroundSkullRewardName.trim(),
										MobHunting.getConfigManager().dropMoneyOnGroundSkullTextureValue,
										MobHunting.getConfigManager().dropMoneyOnGroundSkullTextureSignature,
										Misc.ceil(moneyOnSign), UUID.randomUUID(),
										UUID.fromString(RewardManager.MH_REWARD_BAG_OF_GOLD_UUID));
								player.getInventory().addItem(is);
								found = true;
							}

						}

						// IF okay the withdraw money
						if (found) {
							plugin.getRewardManager().getEconomy().withdrawPlayer(player, moneyOnSign);
							plugin.getMessages().playerSendMessage(player, Messages.getString(
									"mobhunting.bagofgoldsign.bought", "money",
									plugin.getRewardManager().getEconomy().format(moneyOnSign), "rewardname",
									ChatColor.valueOf(MobHunting.getConfigManager().dropMoneyOnGroundTextColor)
											+ MobHunting.getConfigManager().dropMoneyOnGroundSkullRewardName.trim()));
						}
					} else {
						plugin.getMessages().playerSendMessage(player,
								Messages.getString("mobhunting.bagofgoldsign.not_enough_money"));
					}
				}
			} else {
				plugin.getMessages().playerSendMessage(player, Messages.getString(
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
						ChatColor.stripColor(Messages.getString("mobhunting.bagofgoldsign.line2.sell")))) {
					event.setLine(1, Messages.getString("mobhunting.bagofgoldsign.line2.sell"));

				} else if (ChatColor.stripColor(event.getLine(1)).equalsIgnoreCase(
						ChatColor.stripColor(Messages.getString("mobhunting.bagofgoldsign.line2.buy")))) {
					event.setLine(1, Messages.getString("mobhunting.bagofgoldsign.line2.buy"));
				} else {
					plugin.getMessages().playerSendMessage(player,
							Messages.getString("mobhunting.bagofgoldsign.line2.mustbe_sell_or_buy"));
					event.setLine(3, Messages.getString("mobhunting.bagofgoldsign.line4.error_on_sign", "line", "2"));
					return;
				}

				// Check line 3
				if (event.getLine(2).isEmpty() || ChatColor.stripColor(event.getLine(2)).equalsIgnoreCase(
						ChatColor.stripColor(Messages.getString("mobhunting.bagofgoldsign.line3.everything")))) {
					event.setLine(2, Messages.getString("mobhunting.bagofgoldsign.line3.everything"));
				} else {
					try {
						if (Double.valueOf(event.getLine(2)) > 0) {
							Messages.debug("%s created a Bag of gold Sign", event.getPlayer().getName());
						}
					} catch (NumberFormatException e) {
						Messages.debug("Line no. 3 is not positive a number");
						plugin.getMessages().playerSendMessage(player,
								Messages.getString("mobhunting.bagofgoldsign.line3.not_a_number", "number",
										event.getLine(2), "everything",
										Messages.getString("mobhunting.bagofgoldsign.line3.everything")));
						event.setLine(3,
								Messages.getString("mobhunting.bagofgoldsign.line4.error_on_sign", "line", "3"));
						return;
					}
				}

				event.setLine(0, Messages.getString("mobhunting.bagofgoldsign.line1", "rewardname",
						MobHunting.getConfigManager().dropMoneyOnGroundSkullRewardName.trim()));
				event.setLine(3, Messages.getString("mobhunting.bagofgoldsign.line4.ok"));

			} else {
				plugin.getMessages().playerSendMessage(player, Messages.getString(
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
				Messages.debug("%s destroyed a BagOfGold sign", event.getPlayer().getName());
			} else {
				Messages.debug("%s tried to destroy a BagOfGold sign without permission", event.getPlayer().getName());
				event.getPlayer().sendMessage(Messages.getString("mobhunting.bagofgoldsign.no_permission", "perm",
						"mobhunting.bagofgoldsign.destroy"));
				event.setCancelled(true);
			}
		}
	}

	// ************************************************************************************
	// TESTS
	// ************************************************************************************

	public static boolean isBagOfGoldSign(Block block) {
		if (Misc.isSign(block))
			return ChatColor.stripColor(((Sign) block.getState()).getLine(0))
					.equalsIgnoreCase(ChatColor.stripColor(Messages.getString("mobhunting.bagofgoldsign.line1",
							"rewardname", MobHunting.getConfigManager().dropMoneyOnGroundSkullRewardName.trim())))
					|| ChatColor.stripColor(((Sign) block.getState()).getLine(0)).equalsIgnoreCase("[bagofgold]");
		return false;
	}

	public static boolean isBagOfGoldSign(String line) {
		return ChatColor.stripColor(line)
				.equalsIgnoreCase(ChatColor.stripColor(Messages.getString("mobhunting.bagofgoldsign.line1",
						"rewardname", MobHunting.getConfigManager().dropMoneyOnGroundSkullRewardName.trim())))
				|| ChatColor.stripColor(line).equalsIgnoreCase("[bagofgold]");
	}

}
