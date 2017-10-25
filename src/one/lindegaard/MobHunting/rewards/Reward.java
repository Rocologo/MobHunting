package one.lindegaard.MobHunting.rewards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;

import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.mobs.MinecraftMob;

public class Reward {

	private String displayname = "";;
	private double money = 0;
	private UUID uuid = null;
	private UUID uniqueId;
	private UUID skinUUID;

	Reward() {
		this.displayname = "Skull";
		this.money = 0;
		this.uuid = UUID.randomUUID();
		this.uniqueId = UUID.randomUUID();
	}

	Reward(Reward reward) {
		this.displayname = reward.getDisplayname();
		this.money = reward.getMoney();
		this.uuid = reward.getRewardUUID();
		this.skinUUID = reward.getSkinUUID();
		this.uniqueId = reward.getUniqueUUID();
	}

	Reward(String displayName, double money, UUID uuid, UUID uniqueId, UUID skinUUID) {
		this.displayname = displayName.startsWith("Hidden:") ? displayName.substring(7) : displayName;
		this.money = money;
		this.uuid = uuid;
		this.uniqueId = uniqueId;
		this.skinUUID = skinUUID;
	}

	Reward(List<String> lore) {
		this.displayname = lore.get(0).startsWith("Hidden:") ? lore.get(0).substring(7) : lore.get(0);
		this.money = Double.valueOf(lore.get(1).startsWith("Hidden:") ? lore.get(1).substring(7) : lore.get(1));
		this.uuid = (lore.get(2).startsWith("Hidden:")) ? UUID.fromString(lore.get(2).substring(7))
				: UUID.fromString(lore.get(2));
		if (this.money == 0)
			this.uniqueId = UUID.randomUUID();
		else
			this.uniqueId = (lore.get(3).startsWith("Hidden:")) ? UUID.fromString(lore.get(3).substring(7))
					: UUID.fromString(lore.get(3));
		if (lore.size() >= 5 && !lore.get(4).equalsIgnoreCase("Hidden:")
				&& !lore.get(4).equalsIgnoreCase("Hidden:null"))
			this.skinUUID = (lore.get(4).startsWith("Hidden:")) ? UUID.fromString(lore.get(4).substring(7))
					: UUID.fromString(lore.get(4));
		else {
			if (uuid.equals(UUID.fromString(RewardManager.MH_REWARD_BAG_OF_GOLD_UUID)))
				this.skinUUID = UUID.fromString(RewardManager.MH_REWARD_BAG_OF_GOLD_UUID);
		}
	}

	public void setReward(List<String> lore) {
		this.displayname = lore.get(0).startsWith("Hidden:") ? lore.get(0).substring(7) : lore.get(0);
		this.money = Double.valueOf(lore.get(1).startsWith("Hidden:") ? lore.get(1).substring(7) : lore.get(1));
		this.uuid = (lore.get(2).startsWith("Hidden:")) ? UUID.fromString(lore.get(2).substring(7))
				: UUID.fromString(lore.get(2));
		if (this.money == 0)
			this.uniqueId = UUID.randomUUID();
		else
			this.uniqueId = (lore.get(3).startsWith("Hidden:")) ? UUID.fromString(lore.get(3).substring(7))
					: UUID.fromString(lore.get(3));
		if (lore.size() >= 5 && !lore.get(4).equalsIgnoreCase("Hidden:"))
			this.skinUUID = (lore.get(4).startsWith("Hidden:")) ? UUID.fromString(lore.get(4).substring(7))
					: UUID.fromString(lore.get(4));
		else {
			if (uuid.equals(UUID.fromString(RewardManager.MH_REWARD_BAG_OF_GOLD_UUID)))
				this.skinUUID = UUID.fromString(RewardManager.MH_REWARD_BAG_OF_GOLD_UUID);
		}
	}

	public ArrayList<String> getHiddenLore() {
		return new ArrayList<String>(
				Arrays.asList("Hidden:" + displayname, "Hidden:" + String.format(Locale.ENGLISH, "%.5f", money),
						"Hidden:" + uuid.toString(), money == 0 ? "Hidden:" : "Hidden:" + uniqueId.toString(),
						"Hidden:" + (skinUUID == null ? "" : skinUUID.toString())));
	}

	/**
	 * @return the displayname
	 */
	public String getDisplayname() {
		return displayname;
	}

	/**
	 * @return the money
	 */
	public double getMoney() {
		return money;
	}

	/**
	 * @return the uuid
	 */
	public UUID getRewardUUID() {
		return uuid;
	}

	/**
	 * @return the Unique
	 */
	public UUID getUniqueUUID() {
		return uniqueId;
	}

	/**
	 * @param displayName
	 *            the displayName to set
	 */
	public void setDisplayname(String displayName) {
		this.displayname = displayName.startsWith("Hidden:") ? displayName.substring(7) : displayName;
	}

	/**
	 * @param money
	 *            the money to set
	 */
	public void setMoney(double money) {
		this.money = money;
	}

	/**
	 * @param uuid
	 *            the uuid to set
	 */
	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	/**
	 * @param uniqueId
	 *            the uniqueId to set
	 */
	public void setUniqueId(UUID uniqueId) {
		this.uniqueId = uniqueId;
	}

	/**
	 * Get the skin UUID for the reward
	 * 
	 * @return
	 */
	public UUID getSkinUUID() {
		return skinUUID;
	}

	/**
	 * Set the skin UUID for the reward
	 * 
	 * @param skinUUID
	 */
	public void setSkinUUID(UUID skinUUID) {
		this.skinUUID = skinUUID;
	}

	public String toString() {
		return "{Description=" + displayname + ", money=" + String.format(Locale.ENGLISH, "%.5f", money) + ", UUID="
				+ uuid + ", UniqueID=" + uniqueId + ", Skin=" + skinUUID + "}";
	}

	public void save(ConfigurationSection section) {
		section.set("description", displayname);
		section.set("money", String.format(Locale.ENGLISH, "%.5f", money));
		section.set("uuid", uuid.toString());
		section.set("uniqueid", uniqueId.toString());
		section.set("skinuuid", skinUUID == null ? "" : skinUUID.toString());
	}

	public void read(ConfigurationSection section) throws InvalidConfigurationException {
		displayname = section.getString("description");
		money = Double.valueOf(section.getString("money").replace(",", "."));
		uuid = UUID.fromString(section.getString("uuid"));
		uniqueId = UUID.fromString(section.getString("uniqueid"));
		String str = section.getString("skinuuid", "");
		if (str.equalsIgnoreCase("")) {
			if (uuid.equals(UUID.fromString(RewardManager.MH_REWARD_BAG_OF_GOLD_UUID)))
				this.skinUUID = UUID.fromString(RewardManager.MH_REWARD_BAG_OF_GOLD_UUID);
			else if (uuid.equals(UUID.fromString(RewardManager.MH_REWARD_KILLER_UUID))) {
				OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(displayname);
				if (offlinePlayer != null)
					skinUUID = offlinePlayer.getUniqueId();
			} else if (uuid.equals(UUID.fromString(RewardManager.MH_REWARD_KILLED_UUID))) {
				MinecraftMob mob = MinecraftMob.getMinecraftMobType(displayname);
				if (mob != null) {
					skinUUID = mob.getPlayerUUID();
				} else
					this.skinUUID = null;
			} else
				this.skinUUID = null;
		} else
			skinUUID = UUID.fromString(section.getString("skinuuid"));
	}

	public boolean isBagOfGoldReward() {
		return uuid.toString().equalsIgnoreCase(RewardManager.MH_REWARD_BAG_OF_GOLD_UUID);
	}

	public boolean isKilledHeadReward() {
		return uuid.toString().equalsIgnoreCase(RewardManager.MH_REWARD_KILLED_UUID);
	}

	public boolean isKillerHeadReward() {
		return uuid.toString().equalsIgnoreCase(RewardManager.MH_REWARD_KILLER_UUID);
	}

	public boolean isItemReward() {
		return uuid.toString().equalsIgnoreCase(RewardManager.MH_REWARD_ITEM_UUID);
	}

	public static boolean isReward(Item item) {
		return item.hasMetadata(RewardManager.MH_REWARD_DATA) || isReward(item.getItemStack());
	}

	public static Reward getReward(Item item) {
		if (item.hasMetadata(RewardManager.MH_REWARD_DATA))
			for (MetadataValue mv : item.getMetadata(RewardManager.MH_REWARD_DATA)) {
				if (mv.value() instanceof Reward)
					return (Reward) item.getMetadata(RewardManager.MH_REWARD_DATA).get(0).value();
			}
		return getReward(item.getItemStack());
	}

	public static boolean isReward(ItemStack itemStack) {
		if (itemStack != null && itemStack.hasItemMeta() && itemStack.getItemMeta().hasLore()) {
			for (int i = 0; i < itemStack.getItemMeta().getLore().size(); i++) {
				if (itemStack.getItemMeta().getLore().get(i)
						.equals("Hidden:" + RewardManager.MH_REWARD_BAG_OF_GOLD_UUID)
						|| itemStack.getItemMeta().getLore().get(i)
								.equals("Hidden:" + RewardManager.MH_REWARD_KILLED_UUID)
						|| itemStack.getItemMeta().getLore().get(i)
								.equals("Hidden:" + RewardManager.MH_REWARD_KILLER_UUID)
						|| itemStack.getItemMeta().getLore().get(i)
								.equals("Hidden:" + RewardManager.MH_REWARD_ITEM_UUID)) {
					return true;
				}
			}
		}
		return false;
	}

	public static Reward getReward(ItemStack itemStack) {
		return new Reward(itemStack.getItemMeta().getLore());
	}

	public static boolean hasReward(Block block) {
		return block.getType() == Material.SKULL && block.hasMetadata(RewardManager.MH_REWARD_DATA);
	}

	public static Reward getReward(Block block) {
		return (Reward) block.getMetadata(RewardManager.MH_REWARD_DATA).get(0).value();
	}

	public static boolean isReward(Entity entity) {
		return entity.hasMetadata(RewardManager.MH_REWARD_DATA);
	}

	public static Reward getReward(Entity entity) {
		return (Reward) entity.getMetadata(RewardManager.MH_REWARD_DATA).get(0).value();
	}

}
