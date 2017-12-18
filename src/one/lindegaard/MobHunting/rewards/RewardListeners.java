package one.lindegaard.MobHunting.rewards;

import java.util.Iterator;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.event.block.Action;

import one.lindegaard.BagOfGold.BagOfGold;
import one.lindegaard.BagOfGold.storage.PlayerSettings;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.compatibility.BagOfGoldCompat;
import one.lindegaard.MobHunting.compatibility.ProtocolLibCompat;
import one.lindegaard.MobHunting.compatibility.ProtocolLibHelper;
import one.lindegaard.MobHunting.mobs.MinecraftMob;
import one.lindegaard.MobHunting.util.Misc;

public class RewardListeners implements Listener {

	private MobHunting plugin;

	public RewardListeners(MobHunting plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerDropReward(PlayerDropItemEvent event) {
		if (event.isCancelled())
			return;

		Item item = event.getItemDrop();
		Player player = event.getPlayer();

		if (Reward.isReward(item)) {
			Reward reward = Reward.getReward(item);
			double money = reward.getMoney();
			if (money == 0) {
				item.setCustomName(ChatColor.valueOf(plugin.getConfigManager().dropMoneyOnGroundTextColor)
						+ reward.getDisplayname());
				Messages.debug("%s dropped a %s (# of rewards left=%s)", player.getName(), reward.getDisplayname(),
						plugin.getRewardManager().getDroppedMoney().size());
			} else {
				if (reward.isItemReward())
					item.setCustomName(ChatColor.valueOf(plugin.getConfigManager().dropMoneyOnGroundTextColor)
							+ plugin.getRewardManager().format(money));
				else
					item.setCustomName(ChatColor.valueOf(plugin.getConfigManager().dropMoneyOnGroundTextColor)
							+ reward.getDisplayname() + " (" + plugin.getRewardManager().format(money) + ")");

				plugin.getRewardManager().getDroppedMoney().put(item.getEntityId(), money);
				if (!plugin.getConfigManager().dropMoneyOnGroundUseAsCurrency)
					plugin.getRewardManager().getEconomy().withdrawPlayer(player, money);

				Messages.debug("%s dropped %s money. (# of rewards left=%s)", player.getName(),
						plugin.getRewardManager().format(money), plugin.getRewardManager().getDroppedMoney().size());
				plugin.getMessages().playerActionBarMessage(player, Messages.getString("mobhunting.moneydrop", "money",
						plugin.getRewardManager().format(money), "rewardname", reward.getDisplayname()));
				if (BagOfGoldCompat.isSupported() && (reward.isBagOfGoldReward() || reward.isItemReward())) {
					if (player.getGameMode() == GameMode.SURVIVAL)
						BagOfGold.getInstance().getEconomyManager().removeMoneyFromBalance(player, money);
				}
			}
			item.setCustomNameVisible(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onDespawnRewardEvent(ItemDespawnEvent event) {
		if (event.isCancelled())
			return;

		if (Reward.isReward(event.getEntity())) {
			if (plugin.getRewardManager().getDroppedMoney().containsKey(event.getEntity().getEntityId())) {
				plugin.getRewardManager().getDroppedMoney().remove(event.getEntity().getEntityId());
				Messages.debug("The reward was lost - despawned (# of rewards left=%s)",
						plugin.getRewardManager().getDroppedMoney().size());
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onInventoryPickupRewardEvent(InventoryPickupItemEvent event) {
		if (event.isCancelled())
			return;

		Item item = event.getItem();
		if (!item.hasMetadata(Reward.MH_REWARD_DATA))
			return;

		if (plugin.getConfigManager().denyHoppersToPickUpMoney
				&& event.getInventory().getType() == InventoryType.HOPPER) {
			// Messages.debug("A %s tried to pick up the the reward, but this is
			// disabled in config.yml",
			// event.getInventory().getType());
			event.setCancelled(true);
		} else {
			// Messages.debug("The reward was picked up by %s",
			// event.getInventory().getType());
			if (plugin.getRewardManager().getDroppedMoney().containsKey(item.getEntityId()))
				plugin.getRewardManager().getDroppedMoney().remove(item.getEntityId());
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerMoveOverRewardEvent(PlayerMoveEvent event) {
		if (event.isCancelled())
			return;

		Player player = event.getPlayer();
		if (player.getInventory().firstEmpty() == -1) {
			Iterator<Entity> entityList = ((Entity) player).getNearbyEntities(1, 1, 1).iterator();
			while (entityList.hasNext()) {
				Entity entity = entityList.next();
				if (!(entity instanceof Item))
					continue;

				Item item = (Item) entity;

				if (Reward.isReward(item)
						&& plugin.getRewardManager().getDroppedMoney().containsKey(entity.getEntityId())) {
					Reward reward = Reward.getReward(item);
					if (reward.isBagOfGoldReward()) {
						item.remove();
						if (plugin.getRewardManager().getDroppedMoney().containsKey(entity.getEntityId()))
							plugin.getRewardManager().getDroppedMoney().remove(entity.getEntityId());
						if (ProtocolLibCompat.isSupported())
							ProtocolLibHelper.pickupMoney(player, item);
						if (BagOfGoldCompat.isSupported() && (reward.isBagOfGoldReward() || reward.isItemReward())) {
							if (player.getGameMode() == GameMode.SURVIVAL) {
								PlayerSettings ps = BagOfGold.getInstance().getPlayerSettingsManager()
										.getPlayerSettings(player);
								ps.setBalance(Misc.round(ps.getBalance() + reward.getMoney()));
								BagOfGold.getInstance().getPlayerSettingsManager().setPlayerSettings(player, ps);
								BagOfGold.getInstance().getDataStoreManager().updatePlayerSettings(player, ps);
							}
						} else if (reward.getMoney() != 0
								&& !plugin.getConfigManager().dropMoneyOnGroundUseAsCurrency) {
							// If not Gringotts
							plugin.getRewardManager().depositPlayer(player, reward.getMoney());
						} else {
							plugin.getRewardManager().addBagOfGoldPlayer_RewardManager(player, reward.getMoney());
						}
						if (reward.getMoney() == 0) {
							Messages.debug("%s picked up a %s (# of rewards left=%s)", player.getName(),
									reward.getDisplayname(), plugin.getRewardManager().getDroppedMoney().size());
						} else {
							Messages.debug("%s picked up a %s with a value:%s (# of rewards left=%s)(PickupRewards)",
									player.getName(), reward.getDisplayname(),
									plugin.getRewardManager().format(Misc.round(reward.getMoney())),
									plugin.getRewardManager().getDroppedMoney().size());
							plugin.getMessages().playerActionBarMessage(player,
									Messages.getString("mobhunting.moneypickup", "money",
											plugin.getRewardManager().format(reward.getMoney()), "rewardname",
											reward.getDisplayname()));
						}
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onProjectileHitRewardEvent(ProjectileHitEvent event) {
		Projectile projectile = event.getEntity();
		Entity targetEntity = null;
		Iterator<Entity> nearby = projectile.getNearbyEntities(1, 1, 1).iterator();
		while (nearby.hasNext()) {
			targetEntity = nearby.next();
			if (Reward.isReward(targetEntity)) {
				if (plugin.getRewardManager().getDroppedMoney().containsKey(targetEntity.getEntityId()))
					plugin.getRewardManager().getDroppedMoney().remove(targetEntity.getEntityId());
				targetEntity.remove();
				Messages.debug("The reward was hit by %s and removed. (# of rewards left=%s)", projectile.getType(),
						plugin.getRewardManager().getDroppedMoney().size());
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onRewardBlockPlace(BlockPlaceEvent event) {
		if (event.isCancelled())
			return;

		Player player = event.getPlayer();
		ItemStack is = event.getItemInHand();
		Block block = event.getBlockPlaced();
		if (Reward.isReward(is)) {
			Reward reward = Reward.getReward(is);
			if (event.getPlayer().getGameMode() != GameMode.SURVIVAL) {
				reward.setMoney(0);
				plugin.getMessages().learn(event.getPlayer(), Messages.getString("mobhunting.learn.no-duplication"));
			}
			if (reward.getMoney() == 0)
				reward.setUniqueId(UUID.randomUUID());
			Messages.debug("Placed Reward Block:%s", reward.toString());
			block.setMetadata(Reward.MH_REWARD_DATA, new FixedMetadataValue(plugin, reward));
			plugin.getRewardManager().getLocations().put(reward.getUniqueUUID(), reward);
			plugin.getRewardManager().getReward().put(reward.getUniqueUUID(), block.getLocation());
			plugin.getRewardManager().saveReward(reward.getUniqueUUID());
			if (BagOfGoldCompat.isSupported() && (reward.isBagOfGoldReward() || reward.isItemReward())) {
				if (player.getGameMode() == GameMode.SURVIVAL)
					BagOfGold.getInstance().getEconomyManager().removeMoneyFromBalance(player, reward.getMoney());
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onRewardBlockBreak(BlockBreakEvent event) {
		if (event.isCancelled())
			return;

		CustomItems customItems = new CustomItems(plugin);

		Block block = event.getBlock();
		if (Reward.hasReward(block)) {
			Reward reward = Reward.getReward(block);
			block.getDrops().clear();
			block.setType(Material.AIR);
			block.removeMetadata(Reward.MH_REWARD_DATA, plugin);
			ItemStack is;
			if (reward.isBagOfGoldReward()) {
				is = customItems.getCustomtexture(reward.getRewardUUID(), reward.getDisplayname(),
						plugin.getConfigManager().dropMoneyOnGroundSkullTextureValue,
						plugin.getConfigManager().dropMoneyOnGroundSkullTextureSignature, reward.getMoney(),
						reward.getUniqueUUID(), reward.getSkinUUID());
			} else {
				MinecraftMob mob = (reward.getSkinUUID() != null)
						? MinecraftMob.getMinecraftMobType(reward.getSkinUUID())
						: MinecraftMob.getMinecraftMobType(reward.getDisplayname());
				if (mob != null) {
					is = customItems.getCustomHead(mob, reward.getDisplayname(), 1, reward.getMoney(),
							reward.getSkinUUID());
				} else {
					@SuppressWarnings("deprecation")
					OfflinePlayer player = Bukkit.getOfflinePlayer(reward.getDisplayname());
					if (player != null) {
						is = customItems.getPlayerHead(player.getUniqueId(), 1, reward.getMoney());
					} else {
						plugin.getLogger().warning("[MobHunting] The mobtype could not be detected from displayname:"
								+ reward.getDisplayname());
						is = new ItemStack(Material.SKULL_ITEM, 1);
					}
				}
			}
			Item item = block.getWorld().dropItemNaturally(block.getLocation(), is);

			String displayName = plugin.getConfigManager().dropMoneyOnGroundItemtype.equalsIgnoreCase("ITEM")
					? plugin.getRewardManager().format(reward.getMoney())
					: (reward.getMoney() == 0 ? reward.getDisplayname()
							: reward.getDisplayname() + " (" + plugin.getRewardManager().format(reward.getMoney())
									+ ")");
			item.setCustomName(ChatColor.valueOf(plugin.getConfigManager().dropMoneyOnGroundTextColor) + displayName);
			item.setCustomNameVisible(true);
			item.setMetadata(Reward.MH_REWARD_DATA, new FixedMetadataValue(plugin, new Reward(reward.getHiddenLore())));

			if (plugin.getRewardManager().getLocations().containsKey(reward.getUniqueUUID()))
				plugin.getRewardManager().getLocations().remove(reward.getUniqueUUID());
			if (plugin.getRewardManager().getReward().containsKey(reward.getUniqueUUID()))
				plugin.getRewardManager().getReward().remove(reward.getUniqueUUID());
		}

	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.NORMAL)
	public void onInventoryClickReward(InventoryClickEvent event) {
		if (event.isCancelled())
			return;

		InventoryAction action = event.getAction();
		ItemStack isCurrentSlot = event.getCurrentItem();
		ItemStack isCursor = event.getCursor();
		Player player = (Player) event.getWhoClicked();

		if (player.getGameMode() == GameMode.CREATIVE
				&& (Reward.isReward(isCursor) || Reward.isReward(isCurrentSlot))) {
			plugin.getMessages().learn(player, Messages.getString("mobhunting.learn.rewards.creative"));
			event.setCancelled(true);
			return;
		}

		if (action == InventoryAction.SWAP_WITH_CURSOR
				&& (isCurrentSlot.getType() == Material.SKULL_ITEM
						|| isCurrentSlot.getType() == Material.valueOf(plugin.getConfigManager().dropMoneyOnGroundItem))
				&& isCurrentSlot.getType() == isCursor.getType()) {
			if (Reward.isReward(isCurrentSlot) && Reward.isReward(isCursor)) {
				event.setCancelled(true);
				ItemMeta imCurrent = isCurrentSlot.getItemMeta();
				ItemMeta imCursor = isCursor.getItemMeta();
				Reward reward1 = new Reward(imCurrent.getLore());
				Reward reward2 = new Reward(imCursor.getLore());
				if ((reward1.isBagOfGoldReward() || reward1.isItemReward())
						&& reward1.getRewardUUID().equals(reward2.getRewardUUID())) {
					reward2.setMoney(reward1.getMoney() + reward2.getMoney());
					imCursor.setLore(reward2.getHiddenLore());
					imCursor.setDisplayName(ChatColor.valueOf(plugin.getConfigManager().dropMoneyOnGroundTextColor)
							+ (plugin.getConfigManager().dropMoneyOnGroundItemtype.equalsIgnoreCase("ITEM")
									? plugin.getRewardManager().format(reward2.getMoney())
									: reward2.getDisplayname() + " ("
											+ plugin.getRewardManager().format(reward2.getMoney()) + ")"));
					isCursor.setItemMeta(imCursor);
					isCurrentSlot.setAmount(0);
					isCurrentSlot.setType(Material.AIR);
					Messages.debug("%s merged two rewards", player.getName());
				}
			}

		} else if (action == InventoryAction.PICKUP_HALF && isCursor.getType() == Material.AIR
				&& (isCurrentSlot.getType() == Material.SKULL_ITEM || isCurrentSlot.getType() == Material
						.valueOf(plugin.getConfigManager().dropMoneyOnGroundItem))) {
			if (Reward.isReward(isCurrentSlot)) {
				Reward reward = Reward.getReward(isCurrentSlot);
				if (reward.isBagOfGoldReward() || reward.isItemReward()) {
					double currentSlotMoney = Misc.floor(reward.getMoney() / 2);
					double cursorMoney = Misc.round(reward.getMoney() - currentSlotMoney);
					if (currentSlotMoney >= plugin.getConfigManager().minimumReward) {
						event.setCancelled(true);

						isCurrentSlot = plugin.getRewardManager().setDisplayNameAndHiddenLores(isCurrentSlot.clone(),
								reward.getDisplayname(), currentSlotMoney, reward.getRewardUUID(),
								reward.getSkinUUID());
						event.setCurrentItem(isCurrentSlot);

						isCursor = plugin.getRewardManager().setDisplayNameAndHiddenLores(isCurrentSlot.clone(),
								reward.getDisplayname(), cursorMoney, reward.getRewardUUID(), reward.getSkinUUID());
						event.setCursor(isCursor);

						Messages.debug("%s halfed a reward in two (%s,%s)", player.getName(),
								plugin.getRewardManager().format(currentSlotMoney),
								plugin.getRewardManager().format(cursorMoney));
					}
				}
			}
		} else if (action == InventoryAction.COLLECT_TO_CURSOR && Reward.isReward(isCursor)) {
			Reward cursor = Reward.getReward(isCursor);
			if (cursor.getMoney() > 0 && (cursor.isBagOfGoldReward() || cursor.isItemReward())) {
				double saldo = Misc.floor(cursor.getMoney());
				for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
					ItemStack is = player.getInventory().getItem(slot);
					if (Reward.isReward(is)) {
						Reward reward = Reward.getReward(is);
						if (cursor.getRewardUUID().equals(reward.getRewardUUID()) && reward.getMoney() > 0) {
							saldo = saldo + reward.getMoney();
							player.getInventory().clear(slot);
						}
					}
				}
				isCursor = plugin.getRewardManager().setDisplayNameAndHiddenLores(isCursor.clone(),
						cursor.getDisplayname(), saldo, cursor.getRewardUUID(), cursor.getSkinUUID());
				event.setCursor(isCursor);
			}
		}
	}

	@EventHandler
	public void onPlayerInteractEvent(PlayerInteractEvent event) {
		if (event.isCancelled())
			return;

		if (event.getClickedBlock() == null)
			return;

		if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		if (Misc.isMC19OrNewer() && event.getHand() != EquipmentSlot.HAND)
			return;

		Player player = event.getPlayer();

		Block block = event.getClickedBlock();

		if (Reward.hasReward(block)) {
			Reward reward = Reward.getReward(block);
			if (reward.getMoney() == 0)
				plugin.getMessages().playerActionBarMessage(player,
						ChatColor.valueOf(plugin.getConfigManager().dropMoneyOnGroundTextColor)
								+ reward.getDisplayname());
			else
				plugin.getMessages().playerActionBarMessage(player,
						ChatColor.valueOf(plugin.getConfigManager().dropMoneyOnGroundTextColor)
								+ (plugin.getConfigManager().dropMoneyOnGroundItemtype.equalsIgnoreCase("ITEM")
										? plugin.getRewardManager().format(reward.getMoney())
										: reward.getDisplayname() + " ("
												+ plugin.getRewardManager().format(reward.getMoney()) + ")"));
		} else if (block.getType() == Material.SKULL) {
			Skull skullState = (Skull) block.getState();
			switch (skullState.getSkullType()) {
			case PLAYER:
				if (Misc.isMC19OrNewer()) {
					OfflinePlayer owner = skullState.getOwningPlayer();
					if (owner != null && owner.getName() != null) {
						plugin.getMessages().playerActionBarMessage(player,
								ChatColor.valueOf(plugin.getConfigManager().dropMoneyOnGroundTextColor)
										+ owner.getName());
					} else
						plugin.getMessages().playerActionBarMessage(player,
								ChatColor.valueOf(plugin.getConfigManager().dropMoneyOnGroundTextColor)
										+ Messages.getString("mobhunting.reward.customtexture"));
				} else if (skullState.hasOwner()) {
					@SuppressWarnings("deprecation")
					String owner = skullState.getOwner();
					if (!owner.equalsIgnoreCase("")) {
						plugin.getMessages().playerActionBarMessage(player,
								ChatColor.valueOf(plugin.getConfigManager().dropMoneyOnGroundTextColor) + owner);
					} else
						plugin.getMessages().playerActionBarMessage(player,
								ChatColor.valueOf(plugin.getConfigManager().dropMoneyOnGroundTextColor)
										+ Messages.getString("mobhunting.reward.customtexture"));
				} else
					plugin.getMessages().playerActionBarMessage(player,
							ChatColor.valueOf(plugin.getConfigManager().dropMoneyOnGroundTextColor)
									+ Messages.getString("mobhunting.reward.steve"));
				break;
			case CREEPER:
				plugin.getMessages().playerActionBarMessage(player,
						ChatColor.valueOf(plugin.getConfigManager().dropMoneyOnGroundTextColor)
								+ Messages.getString("mobs.Creeper.name"));
				break;
			case SKELETON:
				plugin.getMessages().playerActionBarMessage(player,
						ChatColor.valueOf(plugin.getConfigManager().dropMoneyOnGroundTextColor)
								+ Messages.getString("mobs.Skeleton.name"));
				break;
			case WITHER:
				plugin.getMessages().playerActionBarMessage(player,
						ChatColor.valueOf(plugin.getConfigManager().dropMoneyOnGroundTextColor)
								+ Messages.getString("mobs.Wither.name"));
				break;
			case ZOMBIE:
				plugin.getMessages().playerActionBarMessage(player,
						ChatColor.valueOf(plugin.getConfigManager().dropMoneyOnGroundTextColor)
								+ Messages.getString("mobs.Zombie.name"));
				break;
			case DRAGON:
				plugin.getMessages().playerActionBarMessage(player,
						ChatColor.valueOf(plugin.getConfigManager().dropMoneyOnGroundTextColor)
								+ Messages.getString("mobs.EnderDragon.name"));
				break;
			default:
				break;
			}
		}
	}

}
