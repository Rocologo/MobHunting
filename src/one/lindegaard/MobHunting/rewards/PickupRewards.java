package one.lindegaard.MobHunting.rewards;

import one.lindegaard.BagOfGold.BagOfGold;
import one.lindegaard.BagOfGold.storage.PlayerSettings;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.compatibility.BagOfGoldCompat;
import one.lindegaard.MobHunting.compatibility.ProtocolLibCompat;
import one.lindegaard.MobHunting.compatibility.ProtocolLibHelper;
import one.lindegaard.MobHunting.util.Misc;

import org.bukkit.GameMode;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

public class PickupRewards {

	private MobHunting plugin;

	public PickupRewards(MobHunting plugin) {
		this.plugin = plugin;
	}

	public void rewardPlayer(Player player, Item item, CallBack callBack) {

		if (Reward.isReward(item)) {
			Reward reward = Reward.getReward(item);
			if (reward.isBagOfGoldReward()) {
				item.remove();
				if (plugin.getRewardManager().getDroppedMoney().containsKey(item.getEntityId()))
					plugin.getRewardManager().getDroppedMoney().remove(item.getEntityId());
				if (ProtocolLibCompat.isSupported())
					ProtocolLibHelper.pickupMoney(player, item);
				callBack.setCancelled(true);
				if (BagOfGoldCompat.isSupported()) {
					if (player.getGameMode() == GameMode.SURVIVAL) {
						PlayerSettings ps = BagOfGold.getInstance().getPlayerSettingsManager()
								.getPlayerSettings(player);
						ps.setBalance(Misc.round(ps.getBalance() + reward.getMoney()));
						BagOfGold.getInstance().getPlayerSettingsManager().setPlayerSettings(player, ps);
						BagOfGold.getInstance().getDataStoreManager().updatePlayerSettings(player, ps);
					} 
				} else if (reward.getMoney() != 0 && !plugin.getConfigManager().dropMoneyOnGroundUseAsCurrency) {
					// If not Gringotts
					plugin.getRewardManager().depositPlayer(player, reward.getMoney());
				} else {
					plugin.getRewardManager().addBagOfGoldPlayer_RewardManager(player, reward.getMoney());
				}
			}
			if (reward.getMoney() == 0) {
				Messages.debug("%s picked up a %s (# of rewards left=%s)", player.getName(), reward.getDisplayname(),
						plugin.getRewardManager().getDroppedMoney().size());
			} else {
				Messages.debug("%s picked up a %s with a value:%s (# of rewards left=%s)(PickupRewards)",
						player.getName(), reward.getDisplayname(),
						plugin.getRewardManager().format(Misc.round(reward.getMoney())),
						plugin.getRewardManager().getDroppedMoney().size());
				plugin.getMessages().playerActionBarMessage(player, Messages.getString("mobhunting.moneypickup",
						"money", plugin.getRewardManager().format(reward.getMoney()),"rewardname",reward.getDisplayname()));
		
			}
		}
	}

	public interface CallBack {

		void setCancelled(boolean canceled);

	}

}
