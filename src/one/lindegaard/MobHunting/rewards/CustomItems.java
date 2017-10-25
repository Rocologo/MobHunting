package one.lindegaard.MobHunting.rewards;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.mobs.MinecraftMob;

public class CustomItems {

	private MobHunting plugin;

	public CustomItems(MobHunting plugin) {
		this.plugin = plugin;
	}

	/**
	 * Return an ItemStack with the Players head texture.
	 *
	 * @param name
	 * @param money
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public ItemStack getPlayerHead(String name, int amount, double money, UUID skinUUID) {
		ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1);
		skull.setDurability((short) 3);
		SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
		skullMeta.setLore(new ArrayList<String>(Arrays.asList("Hidden:" + name,
				"Hidden:" + String.format(Locale.ENGLISH,"%.5f", money), "Hidden:" + RewardManager.MH_REWARD_KILLER_UUID,
				money == 0 ? "Hidden:" : "Hidden:" + UUID.randomUUID(), "Hidden:"+skinUUID)));
		skullMeta.setOwner(name);
		if (money == 0)
			skullMeta.setDisplayName(name);
		else
			skullMeta.setDisplayName(name + " (" + plugin.getRewardManager().getEconomy().format(money) + ")");
		skull.setAmount(amount);
		skull.setItemMeta(skullMeta);
		return skull;
	}

	/**
	 * Return an ItemStack with the Players head texture.
	 *
	 * @param player uuid
	 * @param money
	 * @return
	 */
	public ItemStack getPlayerHead(UUID uuid, int amount, double money) {
		ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1);
		skull.setDurability((short) 3);
		SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
		String name = Bukkit.getOfflinePlayer(uuid).getName();
		skullMeta.setLore(new ArrayList<String>(Arrays.asList("Hidden:" + name,
				"Hidden:" + String.format(Locale.ENGLISH,"%.5f", money), "Hidden:" + RewardManager.MH_REWARD_KILLER_UUID,
				money == 0 ? "Hidden:" : "Hidden:" + UUID.randomUUID(), "Hidden:"+uuid)));
		skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
		if (money == 0)
			skullMeta.setDisplayName(name);
		else
			skullMeta.setDisplayName(name + " (" + plugin.getRewardManager().getEconomy().format(money) + ")");
		skull.setAmount(amount);
		skull.setItemMeta(skullMeta);
		return skull;
	}

	/**
	 * Return an ItemStack with a custom texture. If Mojang changes the way they
	 * calculate Signatures this method will stop working.
	 *
	 * @param mPlayerUUID
	 * @param mDisplayName
	 * @param mTextureValue
	 * @param mTextureSignature
	 * @param money
	 * @return ItemStack with custom texture.
	 */
	public ItemStack getCustomtexture(UUID mPlayerUUID, String mDisplayName, String mTextureValue,
			String mTextureSignature, double money, UUID uniqueRewardUuid, UUID skinUuid) {
		ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);

		if (mTextureSignature.isEmpty() || mTextureValue.isEmpty())
			return skull;

		ItemMeta skullMeta = skull.getItemMeta();

		GameProfile profile = new GameProfile(mPlayerUUID, mDisplayName);
		profile.getProperties().put("textures", new Property("textures", mTextureValue, mTextureSignature));
		Field profileField = null;

		try {
			profileField = skullMeta.getClass().getDeclaredField("profile");
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
			return skull;
		}

		profileField.setAccessible(true);

		try {
			profileField.set(skullMeta, profile);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}

		skullMeta.setLore(
				new ArrayList<String>(Arrays.asList("Hidden:" + mDisplayName, "Hidden:" + String.format(Locale.ENGLISH,"%.5f", money),
						"Hidden:" + mPlayerUUID, money == 0 ? "Hidden:" : "Hidden:" + uniqueRewardUuid, "Hidden:"+skinUuid)));
		if (money == 0)
			skullMeta.setDisplayName(
					ChatColor.valueOf(MobHunting.getConfigManager().dropMoneyOnGroundTextColor) + mDisplayName);
		else
			skullMeta.setDisplayName(ChatColor.valueOf(MobHunting.getConfigManager().dropMoneyOnGroundTextColor)
					+ mDisplayName + " (" + plugin.getRewardManager().format(money) + ")");

		skull.setItemMeta(skullMeta);
		return skull;
	}

	public ItemStack getCustomHead(MinecraftMob minecraftMob, String name, int amount, double money, UUID skinUUID) {
		ItemStack skull;
		switch (minecraftMob) {
		case Skeleton:
			skull = new ItemStack(Material.SKULL_ITEM, amount, (short) 0);
			skull = plugin.getRewardManager().setDisplayNameAndHiddenLores(skull, minecraftMob.getFriendlyName(), money,
					UUID.fromString(RewardManager.MH_REWARD_KILLED_UUID),skinUUID);
			break;

		case WitherSkeleton:
			skull = new ItemStack(Material.SKULL_ITEM, amount, (short) 1);
			skull = plugin.getRewardManager().setDisplayNameAndHiddenLores(skull, minecraftMob.getFriendlyName(), money,
					UUID.fromString(RewardManager.MH_REWARD_KILLED_UUID),skinUUID);
			break;

		case Zombie:
			skull = new ItemStack(Material.SKULL_ITEM, amount, (short) 2);
			skull = plugin.getRewardManager().setDisplayNameAndHiddenLores(skull, minecraftMob.getFriendlyName(), money,
					UUID.fromString(RewardManager.MH_REWARD_KILLED_UUID),skinUUID);
			break;

		case PvpPlayer:
			skull = getPlayerHead(name, amount, money, skinUUID);
			break;

		case Creeper:
			skull = new ItemStack(Material.SKULL_ITEM, amount, (short) 4);
			skull = plugin.getRewardManager().setDisplayNameAndHiddenLores(skull, minecraftMob.getFriendlyName(), money,
					UUID.fromString(RewardManager.MH_REWARD_KILLED_UUID),skinUUID);
			break;

		case EnderDragon:
			skull = new ItemStack(Material.SKULL_ITEM, amount, (short) 5);
			skull = plugin.getRewardManager().setDisplayNameAndHiddenLores(skull, minecraftMob.getFriendlyName(), money,
					UUID.fromString(RewardManager.MH_REWARD_KILLED_UUID),skinUUID);
			break;

		default:
			ItemStack is = new ItemStack(getCustomtexture(UUID.fromString(RewardManager.MH_REWARD_KILLED_UUID),
					minecraftMob.getFriendlyName(), minecraftMob.getTextureValue(), minecraftMob.getTextureSignature(),
					money, UUID.randomUUID(), skinUUID));
			is.setAmount(amount);
			return is;
		}
		return skull;
	}

}
