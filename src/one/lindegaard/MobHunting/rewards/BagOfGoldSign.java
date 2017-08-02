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

	private RewardManager rewardManager;

	public BagOfGoldSign(RewardManager rewardManager) {
		this.rewardManager = rewardManager;
		Bukkit.getServer().getPluginManager().registerEvents(this, MobHunting.getInstance());
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
						Reward hrd = Reward.getReward(player.getItemInHand());
						moneyInHand = hrd.getMoney();
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
								player.sendMessage(Messages.getString("mobhunting.bagofgoldsign.line3.not_a_number",
										"number", sign.getLine(2), "everything",
										Messages.getString("mobhunting.bagofgoldsign.line3.everything")));
								return;
							}
						}
						rewardManager.getEconomy().depositPlayer(player, money);
						if (moneyInHand <= moneyOnSign) {
							event.getItem().setAmount(0);
							event.getItem().setType(Material.AIR);
						} else {
							hrd.setMoney(moneyInHand - moneyOnSign);
							ItemMeta im = event.getItem().getItemMeta();
							im.setLore(hrd.getHiddenLore());
							String displayName = MobHunting.getConfigManager().dropMoneyOnGroundItemtype
									.equalsIgnoreCase("ITEM") ? MobHunting.getRewardManager().format(hrd.getMoney())
											: hrd.getDisplayname() + "("
													+ MobHunting.getRewardManager().format(hrd.getMoney()) + ")";
							im.setDisplayName(
									ChatColor.valueOf(MobHunting.getConfigManager().dropMoneyOnGroundTextColor)
											+ displayName);
							event.getItem().setItemMeta(im);
						}
						Messages.debug("%s sold his bag of gold for %s", player.getName(),
								rewardManager.getEconomy().format(money));
						player.sendMessage(Messages.getString("mobhunting.bagofgoldsign.sold", "money",
								rewardManager.getEconomy().format(money), "rewardname",
								ChatColor.valueOf(MobHunting.getConfigManager().dropMoneyOnGroundTextColor)
										+ MobHunting.getConfigManager().dropMoneyOnGroundSkullRewardName));
					} else {
						Messages.debug("Player does not hold a bag of gold in his hand");
						player.sendMessage(Messages.getString("mobhunting.bagofgoldsign.hold_bag_in_hand", "rewardname",
								ChatColor.valueOf(MobHunting.getConfigManager().dropMoneyOnGroundTextColor)
										+ MobHunting.getConfigManager().dropMoneyOnGroundSkullRewardName));
					}

					// BUY BagOfGold Sign
				} else if (signType.equalsIgnoreCase(Messages.getString("mobhunting.bagofgoldsign.line2.buy"))) {
					try {
						moneyOnSign = Double.valueOf(sign.getLine(2));
					} catch (NumberFormatException e) {
						Messages.debug("Line no. 3 is not a number");
						player.sendMessage(Messages.getString("mobhunting.bagofgoldsign.line3.not_a_number", "number",
								sign.getLine(2), "everything",
								Messages.getString("mobhunting.bagofgoldsign.line3.everything")));
						return;
					}
					if (rewardManager.getEconomy().getBalance(player) >= moneyOnSign) {

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
													? MobHunting.getRewardManager()
															.format(newReward.getMoney())
													: newReward.getDisplayname() + "(" + MobHunting
															.getRewardManager().format(newReward.getMoney())
															+ ")";
									im.setDisplayName(
											ChatColor.valueOf(MobHunting.getConfigManager().dropMoneyOnGroundTextColor)
													+ displayName);
									is.setItemMeta(im);
									is.setAmount(1);
									event.setCancelled(true);
									Messages.debug("Added %s to item in slot %s, new value is %s",
											MobHunting.getRewardManager().format(hrd.getMoney()), slot,
											MobHunting.getRewardManager().format(newReward.getMoney()));
									found = true;
									break;
								}
							}
						}

						if (!found) {

							if (player.getInventory().firstEmpty() == -1)
								rewardManager.dropMoneyOnGround(player, null, player.getLocation(),
										Misc.ceil(moneyOnSign));
							else {
								ItemStack is = CustomItems.getCustomtexture(
										UUID.fromString(RewardManager.MH_REWARD_BAG_OF_GOLD_UUID),
										MobHunting.getConfigManager().dropMoneyOnGroundSkullRewardName,
										MobHunting.getConfigManager().dropMoneyOnGroundSkullTextureValue,
										MobHunting.getConfigManager().dropMoneyOnGroundSkullTextureSignature,
										Misc.ceil(moneyOnSign), UUID.randomUUID());
								player.getInventory().addItem(is);
								found = true;
							}

						}

						// IF okay the withdraw money
						if (found) {
							rewardManager.getEconomy().withdrawPlayer(player, moneyOnSign);
							player.sendMessage(Messages.getString("mobhunting.bagofgoldsign.bought", "money",
									rewardManager.getEconomy().format(moneyOnSign), "rewardname",
									ChatColor.valueOf(MobHunting.getConfigManager().dropMoneyOnGroundTextColor)
											+ MobHunting.getConfigManager().dropMoneyOnGroundSkullRewardName));
						}
					} else {
						player.sendMessage(Messages.getString("mobhunting.bagofgoldsign.not_enough_money"));
					}
				}
			} else {
				player.sendMessage(Messages.getString("mobhunting.bagofgoldsign.no_permission_to_use", "perm",
						"mobhunting.bagofgoldsign.use"));
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
					player.sendMessage(Messages.getString("mobhunting.bagofgoldsign.line2.mustbe_sell_or_buy"));
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
						player.sendMessage(Messages.getString("mobhunting.bagofgoldsign.line3.not_a_number", "number",
								event.getLine(2), "everything",
								Messages.getString("mobhunting.bagofgoldsign.line3.everything")));
						event.setLine(3,
								Messages.getString("mobhunting.bagofgoldsign.line4.error_on_sign", "line", "3"));
						return;
					}
				}

				event.setLine(0, Messages.getString("mobhunting.bagofgoldsign.line1", "rewardname",
						MobHunting.getConfigManager().dropMoneyOnGroundSkullRewardName));
				event.setLine(3, Messages.getString("mobhunting.bagofgoldsign.line4.ok"));

			} else {
				player.sendMessage(Messages.getString("mobhunting.bagofgoldsign.no_permission", "perm",
						"mobhunting.bagofgoldsign.create"));
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
							"rewardname", MobHunting.getConfigManager().dropMoneyOnGroundSkullRewardName)))
					|| ChatColor.stripColor(((Sign) block.getState()).getLine(0)).equalsIgnoreCase("[bagofgold]");
		return false;
	}

	public static boolean isBagOfGoldSign(String line) {
		return ChatColor.stripColor(line)
				.equalsIgnoreCase(ChatColor.stripColor(Messages.getString("mobhunting.bagofgoldsign.line1",
						"rewardname", MobHunting.getConfigManager().dropMoneyOnGroundSkullRewardName)))
				|| ChatColor.stripColor(line).equalsIgnoreCase("[bagofgold]");
	}

}
