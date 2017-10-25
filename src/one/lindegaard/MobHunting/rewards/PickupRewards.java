package one.lindegaard.MobHunting.rewards;

import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.compatibility.ProtocolLibCompat;
import one.lindegaard.MobHunting.compatibility.ProtocolLibHelper;
import org.bukkit.ChatColor;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashMap;

public class PickupRewards {

	private MobHunting plugin;

	public PickupRewards(MobHunting plugin) {
		this.plugin = plugin;
	}

	public void rewardPlayer(Player player, Item item, CallBack callBack) {

		if (Reward.isReward(item)) {
			Reward reward = Reward.getReward(item);
			// If not Gringotts
			if (reward.getMoney() != 0)
				if (!MobHunting.getConfigManager().dropMoneyOnGroundUseAsCurrency) {
					plugin.getRewardManager().depositPlayer(player, reward.getMoney());
					if (ProtocolLibCompat.isSupported())
						ProtocolLibHelper.pickupMoney(player, item);
					item.remove();
					callBack.setCancelled(true);
					plugin.getMessages().playerActionBarMessage(player, Messages.getString("mobhunting.moneypickup",
							"money", plugin.getRewardManager().format(reward.getMoney())));
				} else {
					boolean found = false;
					HashMap<Integer, ? extends ItemStack> slots = player.getInventory()
							.all(item.getItemStack().getType());
					for (int slot : slots.keySet()) {
						ItemStack is = player.getInventory().getItem(slot);
						if (Reward.isReward(is)) {
							Reward rewardInSlot = Reward.getReward(is);
							if ((reward.isBagOfGoldReward() || reward.isItemReward())
									&& (rewardInSlot.getRewardUUID().equals(reward.getRewardUUID())
											&& rewardInSlot.getDisplayname().equals(reward.getDisplayname()))) {
								ItemMeta im = is.getItemMeta();
								Reward newReward = Reward.getReward(is);
								newReward.setMoney(newReward.getMoney() + reward.getMoney());
								im.setLore(newReward.getHiddenLore());
								String displayName = MobHunting.getConfigManager().dropMoneyOnGroundItemtype
										.equalsIgnoreCase("ITEM")
												? plugin.getRewardManager().format(newReward.getMoney())
												: newReward.getDisplayname() + " ("
														+ plugin.getRewardManager().format(newReward.getMoney()) + ")";
								im.setDisplayName(
										ChatColor.valueOf(MobHunting.getConfigManager().dropMoneyOnGroundTextColor)
												+ displayName);
								is.setItemMeta(im);
								is.setAmount(1);
								callBack.setCancelled(true);
								if (ProtocolLibCompat.isSupported())
									ProtocolLibHelper.pickupMoney(player, item);
								item.remove();
								Messages.debug("Added %s to item in slot %s, new value is %s",
										plugin.getRewardManager().format(reward.getMoney()), slot,
										plugin.getRewardManager().format(newReward.getMoney()));
								found = true;
								break;
							}
						}
					}

					if (!found) {
						ItemStack is = item.getItemStack();
						ItemMeta im = is.getItemMeta();
						String displayName = MobHunting.getConfigManager().dropMoneyOnGroundItemtype
								.equalsIgnoreCase("ITEM") ? plugin.getRewardManager().format(reward.getMoney())
										: reward.getDisplayname() + " ("
												+ plugin.getRewardManager().format(reward.getMoney()) + ")";
						im.setDisplayName(ChatColor.valueOf(MobHunting.getConfigManager().dropMoneyOnGroundTextColor)
								+ displayName);
						im.setLore(reward.getHiddenLore());
						is.setItemMeta(im);
						item.setItemStack(is);
						item.setMetadata(RewardManager.MH_REWARD_DATA,
								new FixedMetadataValue(MobHunting.getInstance(), new Reward(reward)));
					}
				}
			if (plugin.getRewardManager().getDroppedMoney().containsKey(item.getEntityId()))
				plugin.getRewardManager().getDroppedMoney().remove(item.getEntityId());
			if (reward.getMoney() == 0)
				Messages.debug("%s picked up a %s (# of rewards left=%s)", player.getName(), reward.getDisplayname(),
						plugin.getRewardManager().getDroppedMoney().size());
			else
				Messages.debug("%s picked up a %s with a value:%s (# of rewards left=%s)", player.getName(),
						reward.getDisplayname(), plugin.getRewardManager().format(reward.getMoney()),
						plugin.getRewardManager().getDroppedMoney().size());

		}
	}

	public interface CallBack {

		void setCancelled(boolean canceled);

	}

}
