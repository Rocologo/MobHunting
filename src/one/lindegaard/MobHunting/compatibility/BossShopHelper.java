package one.lindegaard.MobHunting.compatibility;

import java.util.UUID;

import org.black_ixx.bossshop.core.BSBuy;
import org.black_ixx.bossshop.core.BSShop;
import org.black_ixx.bossshop.core.enums.BSBuyType;
import org.black_ixx.bossshop.core.enums.BSPriceType;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.rewards.CustomItems;
import one.lindegaard.MobHunting.rewards.RewardManager;

public class BossShopHelper {

	public static boolean openShop(MobHunting plugin, Player p, String shop_name) {

		//BossShopCompat.getBossShop().getAPI().
		BSShop shop = BossShopCompat.getBossShop().getAPI().getShop(shop_name);

		if (shop == null) {
			p.sendMessage(ChatColor.RED + "Shop " + shop_name + " not found...");
			return false;
		}

		BSBuy buy = BossShopCompat.getBossShop().getAPI().createBSBuy(BSBuyType.Shop, BSPriceType.Free, "item_shop",
				null, null, 15, "OpenShop.Item_Shop");
		BSBuy sell = BossShopCompat.getBossShop().getAPI().createBSBuy(BSBuyType.Money, BSPriceType.Money, "item_shop",
				10, "bought bag of gold", 17, null);
		
		BossShopCompat.getBossShop().getAPI().openShop(p, shop);

		UUID uuid = UUID.fromString(RewardManager.MH_REWARD_BAG_OF_GOLD_UUID);
		
		ItemStack is = new CustomItems(plugin).getCustomtexture(uuid,
				MobHunting.getConfigManager().dropMoneyOnGroundSkullRewardName.trim(),
				MobHunting.getConfigManager().dropMoneyOnGroundSkullTextureValue,
				MobHunting.getConfigManager().dropMoneyOnGroundSkullTextureSignature, 10, UUID.randomUUID(), uuid);

		// ItemStack menu_item = new ItemStack(is);
		BossShopCompat.getBossShop().getAPI().addItemToShop(is, buy, shop);
		BossShopCompat.getBossShop().getAPI().addItemToShop(is, sell, shop);

		BossShopCompat.getBossShop().getAPI().finishedAddingItemsToShop(shop);
		
		return true;
	}

}
