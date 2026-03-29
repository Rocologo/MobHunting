package one.lindegaard.MobHunting.rewards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Allay;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Cat;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cod;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Dolphin;
import org.bukkit.entity.Donkey;
import org.bukkit.entity.Drowned;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Endermite;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Evoker;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Frog;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Giant;
import org.bukkit.entity.GlowSquid;
import org.bukkit.entity.Goat;
import org.bukkit.entity.Guardian;
import org.bukkit.entity.Hoglin;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Husk;
import org.bukkit.entity.Illusioner;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Item;
import org.bukkit.entity.Llama;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Mule;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.PiglinBrute;
import org.bukkit.entity.Pillager;
import org.bukkit.entity.Player;
import org.bukkit.entity.PolarBear;
import org.bukkit.entity.PufferFish;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Ravager;
import org.bukkit.entity.Salmon;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Shulker;
import org.bukkit.entity.Silverfish;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.SkeletonHorse;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Squid;
import org.bukkit.entity.Stray;
import org.bukkit.entity.Strider;
import org.bukkit.entity.Tadpole;
import org.bukkit.entity.TraderLlama;
import org.bukkit.entity.TropicalFish;
import org.bukkit.entity.Turtle;
import org.bukkit.entity.Vex;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Vindicator;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.entity.Warden;
import org.bukkit.entity.Witch;
import org.bukkit.entity.Wither;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zoglin;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.ZombieHorse;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.gestern.gringotts.Configuration;
import org.gestern.gringotts.currency.Denomination;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import one.lindegaard.CustomItemsLib.Core;
import one.lindegaard.CustomItemsLib.Tools;
import one.lindegaard.CustomItemsLib.mobs.MobType;
import one.lindegaard.CustomItemsLib.rewards.CoreCustomItems;
import one.lindegaard.CustomItemsLib.rewards.MoneyMergeEventListener;
import one.lindegaard.CustomItemsLib.rewards.Reward;
import one.lindegaard.CustomItemsLib.rewards.RewardType;
import one.lindegaard.CustomItemsLib.server.Servers;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.compatibility.BagOfGoldCompat;
import one.lindegaard.MobHunting.compatibility.BossCompat;
import one.lindegaard.MobHunting.compatibility.CitizensCompat;
import one.lindegaard.MobHunting.compatibility.CustomMobsCompat;
import one.lindegaard.MobHunting.compatibility.EliteMobsCompat;
import one.lindegaard.MobHunting.compatibility.GringottsCompat;
import one.lindegaard.MobHunting.compatibility.HerobrineCompat;
import one.lindegaard.MobHunting.compatibility.MyPetCompat;
import one.lindegaard.MobHunting.compatibility.MysteriousHalloweenCompat;
import one.lindegaard.MobHunting.compatibility.MythicMobsCompat;
import one.lindegaard.MobHunting.compatibility.SmartGiantsCompat;
import one.lindegaard.MobHunting.compatibility.TARDISWeepingAngelsCompat;
import one.lindegaard.MobHunting.mobs.ExtendedMobRewardData;

public class RewardManager {

	private MobHunting plugin;

	public RewardManager(MobHunting plugin) {
		this.plugin = plugin;
		if (!BagOfGoldCompat.isSupported()) {

			Bukkit.getPluginManager().registerEvents(new RewardListeners(plugin), plugin);

			if (!BagOfGoldCompat.isSupported())
				Bukkit.getPluginManager().registerEvents(new MoneyMergeEventListener(plugin), plugin);

		}
		if (BagOfGoldCompat.isSupported() || plugin.getConfigManager().dropMoneyOnGroundUseItemAsCurrency)
			new BagOfGoldSign(plugin);
	}

	private boolean eventDoesExists() {
		try {
			@SuppressWarnings({ "rawtypes", "unused" })
			Class cls = Class.forName("org.bukkit.event.entity.EntityPickupItemEvent");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}

	}

	public boolean depositPlayer(OfflinePlayer offlinePlayer, double amount) {
		boolean succes = plugin.getEconomyManager().depositPlayer(offlinePlayer, amount);
		if (!succes && offlinePlayer.isOnline())
			((Player) offlinePlayer).sendMessage(ChatColor.RED + "Unable to add money.");
		return succes;
	}

	public boolean withdrawPlayer(OfflinePlayer offlinePlayer, double amount) {
		boolean succes = plugin.getEconomyManager().withdrawPlayer(offlinePlayer, amount);
		if (!succes && offlinePlayer.isOnline())
			((Player) offlinePlayer).sendMessage(ChatColor.RED + "Unable to remove money.");
		return succes;
	}

	public String format(double amount) {
		if (plugin.getConfigManager().dropMoneyOnGroundUseItemAsCurrency && !BagOfGoldCompat.isSupported())
			return Tools.format(amount);
		else
			return plugin.getEconomyManager().getFormattedBalance(amount);
	}

	public double getBalance(OfflinePlayer offlinePlayer) {
		if (BagOfGoldCompat.isSupported() || !plugin.getConfigManager().dropMoneyOnGroundUseItemAsCurrency)
			return plugin.getEconomyManager().getBalance(offlinePlayer);
		else if (offlinePlayer.isOnline()) {
			return getAmountInInventory((Player) offlinePlayer);
		} else {
			return 0;
		}
	}

	public double getAmountInInventory(Player player) {
		double amountInInventory = 0;
		for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
			ItemStack is = player.getInventory().getItem(slot);
			if (Reward.isReward(is)) {
				Reward reward = Reward.getReward(is);
				if (reward.isBagOfGoldReward() || reward.isItemReward())
					amountInInventory = amountInInventory + reward.getMoney();
			}
		}
		return amountInInventory;
	}

	public double addBagOfGoldPlayer(Player player, double amount) {

		boolean found = false;
		double moneyLeftToGive = amount;
		double addedMoney = 0;
		for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
			ItemStack is = player.getInventory().getItem(slot);
			if (Reward.isReward(is)) {
				Reward rewardInSlot = Reward.getReward(is);
				if ((rewardInSlot.isBagOfGoldReward() || rewardInSlot.isItemReward())) {
					if (rewardInSlot.getMoney() < Core.getConfigManager().limitPerBag) {
						double space = Core.getConfigManager().limitPerBag - rewardInSlot.getMoney();
						if (space > moneyLeftToGive) {
							rewardInSlot.setMoney(rewardInSlot.getMoney() + moneyLeftToGive);
							addedMoney = addedMoney + moneyLeftToGive;
							moneyLeftToGive = 0;
						} else {
							addedMoney = addedMoney + space;
							rewardInSlot.setMoney(Core.getConfigManager().limitPerBag);
							moneyLeftToGive = moneyLeftToGive - space;
						}
						if (rewardInSlot.getMoney() == 0)
							player.getInventory().clear(slot);
						else
							is = Reward.setDisplayNameAndHiddenLores(is, rewardInSlot);
						plugin.getMessages().debug("Added %s to %s's item in slot %s, new value is %s", format(amount),
								player.getName(), slot, format(rewardInSlot.getMoney()));
						if (moneyLeftToGive <= 0) {
							found = true;
							break;
						}
					}
				}
			}
		}
		if (!found) {
			while (Tools.round(moneyLeftToGive) > 0 && Core.getCoreRewardManager().canPickupMoney(player)) {
				double nextBag = 0;
				if (moneyLeftToGive > Core.getConfigManager().limitPerBag) {
					nextBag = Core.getConfigManager().limitPerBag;
					moneyLeftToGive = moneyLeftToGive - nextBag;
				} else {
					nextBag = moneyLeftToGive;
					moneyLeftToGive = 0;
				}
				if (player.getInventory().firstEmpty() == -1)
					dropMoneyOnGround_RewardManager(player, null, player.getLocation(), Tools.round(nextBag));
				else {
					addedMoney = addedMoney + nextBag;
					ItemStack is;
					if (Core.getConfigManager().rewardItemtype.equalsIgnoreCase("SKULL"))
						is = CoreCustomItems.getCustomtexture(
								new Reward(Core.getConfigManager().bagOfGoldName.trim(), Tools.round(nextBag),
										RewardType.BAGOFGOLD, UUID.fromString(RewardType.BAGOFGOLD.getUUID())),
								Core.getConfigManager().skullTextureValue,
								Core.getConfigManager().skullTextureSignature);
					else {
						is = new ItemStack(Material.valueOf(Core.getConfigManager().rewardItem), 1);
						is = Reward.setDisplayNameAndHiddenLores(is,
								new Reward(Core.getConfigManager().bagOfGoldName.trim(), Tools.round(nextBag),
										RewardType.ITEM, null));
					}
					player.getInventory().addItem(is);
				}
			}
		}
		return addedMoney;
	}

	public double removeBagOfGoldPlayer(Player player, double amount) {
		double taken = 0;
		double toBeTaken = Tools.floor(amount);
		for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
			ItemStack is = player.getInventory().getItem(slot);
			if (Reward.isReward(is)) {
				Reward reward = Reward.getReward(is);
				if (reward.isBagOfGoldReward()) {
					double saldo = Tools.floor(reward.getMoney());
					if (saldo > toBeTaken) {
						reward.setMoney(saldo - toBeTaken);
						is = CoreCustomItems.getCustomtexture(
								new Reward(Core.getConfigManager().bagOfGoldName.trim(), saldo - toBeTaken,
										reward.getRewardType(), reward.getSkinUUID()),
								Core.getConfigManager().skullTextureValue,
								Core.getConfigManager().skullTextureSignature);
						player.getInventory().setItem(slot, is);
						taken = taken + toBeTaken;
						toBeTaken = 0;
						return taken;
					} else {
						is.setItemMeta(null);
						is.setType(Material.AIR);
						is.setAmount(0);
						player.getInventory().setItem(slot, is);
						taken = taken + saldo;
						toBeTaken = toBeTaken - saldo;
						return taken;
					}
				}
			}
		}

		return amount;

	}

	public void dropMoneyOnGround_RewardManager(Player player, Entity killedEntity, Location location, double money) {
		Item item = null;
		money = Tools.ceil(money);
		if (GringottsCompat.isSupported()) {
			List<Denomination> denoms = Configuration.CONF.getCurrency().getDenominations();
			int unit = Configuration.CONF.getCurrency().getUnit();
			double rest = money;
			for (Denomination d : denoms) {
				ItemStack is = new ItemStack(d.getKey().type.getType(), 1);
				while (rest >= (d.getValue() / unit)) {
					item = location.getWorld().dropItem(location, is);
					rest = rest - (d.getValue() / unit);
				}
			}
		} else {
			ItemStack is;
			UUID skinuuid = null;
			RewardType rewardType;
			if (Core.getConfigManager().rewardItemtype.equalsIgnoreCase("KILLED")) {
				MobType mob = MobType.getMobType(killedEntity);
				rewardType = RewardType.KILLED;
				skinuuid = mob.getSkinUUID();
				is = CoreCustomItems.getCustomHead(mob, mob.getFriendlyName(), 1, money, skinuuid);

			} else if (Core.getConfigManager().rewardItemtype.equalsIgnoreCase("SKULL")) {
				rewardType = RewardType.BAGOFGOLD;
				skinuuid = UUID.fromString(RewardType.BAGOFGOLD.getUUID());
				is = CoreCustomItems.getCustomtexture(
						new Reward(Core.getConfigManager().bagOfGoldName.trim(), money, rewardType, skinuuid),
						Core.getConfigManager().skullTextureValue, Core.getConfigManager().skullTextureSignature);

			} else if (Core.getConfigManager().rewardItemtype.equalsIgnoreCase("KILLER")) {
				rewardType = RewardType.KILLER;
				skinuuid = player.getUniqueId();
				is = CoreCustomItems.getPlayerHead(player.getUniqueId(), player.getName(), 1, money);

			} else { // ITEM
				rewardType = RewardType.ITEM;
				skinuuid = null;
				is = new ItemStack(Material.valueOf(Core.getConfigManager().rewardItem), 1);
			}

			Reward reward = new Reward(
					ChatColor.valueOf(Core.getConfigManager().rewardTextColor) + Core.getConfigManager().bagOfGoldName,
					money, rewardType, skinuuid);
			is = Reward.setDisplayNameAndHiddenLores(is, reward);
			item = location.getWorld().dropItem(location, is);
			Core.getCoreRewardManager().getDroppedMoney().put(item.getEntityId(), money);
			item.setMetadata(Reward.MH_REWARD_DATA_NEW, new FixedMetadataValue(plugin, new Reward(reward)));
			item.setCustomName(is.getItemMeta().getDisplayName());
			item.setCustomNameVisible(Core.getConfigManager().showCustomDisplayname);
		}
		if (item != null)
			plugin.getMessages().debug("%s was dropped on the ground as item %s (# of rewards=%s)", format(money),
					Core.getConfigManager().rewardItemtype, Core.getCoreRewardManager().getDroppedMoney().size());
	}

	/**
	 * Dropes an Reward Item at the specified location
	 * 
	 * @param location - where the Item is dropped.
	 * @param reward   - the reward to be dropped
	 */
	public void dropRewardOnGround(Location location, Reward reward) {
		if (reward.isBagOfGoldReward()) {
			dropMoneyOnGround_RewardManager(null, null, location, reward.getMoney());
		} else if (reward.isItemReward()) {
			ItemStack is = new ItemStack(Material.valueOf(Core.getConfigManager().rewardItem), 1);
			Item item = location.getWorld().dropItem(location, is);
			Core.getCoreRewardManager().getDroppedMoney().put(item.getEntityId(), reward.getMoney());
		} else if (reward.isKilledHeadReward()) {
			MobType mob = MobType.getMobType(reward.getSkinUUID());
			if (mob != null) {
				ItemStack is = CoreCustomItems.getCustomHead(mob, reward.getDisplayName(), 1,
						reward.getMoney(), reward.getSkinUUID());
				Item item = location.getWorld().dropItem(location, is);
				item.setMetadata(Reward.MH_REWARD_DATA_NEW, new FixedMetadataValue(plugin, new Reward(reward)));
				Core.getCoreRewardManager().getDroppedMoney().put(item.getEntityId(), reward.getMoney());
			}
		} else if (reward.isKillerHeadReward()) {
			ItemStack is = CoreCustomItems.getPlayerHead(reward.getSkinUUID(), reward.getDisplayName(), 1,
					reward.getMoney());
			Item item = location.getWorld().dropItem(location, is);
			item.setMetadata(Reward.MH_REWARD_DATA_NEW, new FixedMetadataValue(plugin, new Reward(reward)));
			Core.getCoreRewardManager().getDroppedMoney().put(item.getEntityId(), reward.getMoney());
		} else {
			Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "[MobHunting] " + ChatColor.RED
					+ "Unhandled reward type in RewardManager (DropRewardOnGround).");
		}
	}

// Moved to Core
//	public boolean canPickupMoney(Player player) {
//		if (player.getInventory().firstEmpty() != -1)
//			return true;
//		for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
//			ItemStack is = player.getInventory().getItem(slot);
//			if (Reward.isReward(is)) {
//				Reward rewardInSlot = Reward.getReward(is);
//				if (rewardInSlot.isMoney()) {
//					if (rewardInSlot.getMoney() < Core.getConfigManager().limitPerBag)
//						return true;
//				}
//			}
//		}
//		return false;
//	}

	public double getPlayerKilledByMobPenalty(Player playerToBeRobbed, List<ItemStack> droplist) {
		if (plugin.getConfigManager().mobKillsPlayerPenalty == null
				|| plugin.getConfigManager().mobKillsPlayerPenalty.trim().equals("")
				|| plugin.getConfigManager().mobKillsPlayerPenalty.trim().equals("0%")
				|| plugin.getConfigManager().mobKillsPlayerPenalty.trim().equals("0")
				|| plugin.getConfigManager().mobKillsPlayerPenalty.trim().isEmpty()) {
			return 0;
		} else if (plugin.getConfigManager().mobKillsPlayerPenalty.trim().contains(":")) {
			String[] str1 = plugin.getConfigManager().mobKillsPlayerPenalty.trim().split(":");
			double penalty = (plugin.mRand.nextDouble() * (Double.valueOf(str1[1]) - Double.valueOf(str1[0]))
					+ Double.valueOf(str1[0]));
			return Tools.round(penalty);
		} else if (plugin.getConfigManager().mobKillsPlayerPenalty.trim().endsWith("%")) {
			double penalty = 0;
			double balance = 0;
			if (BagOfGoldCompat.isSupported()) {
				for (ItemStack is : droplist) {
					plugin.getMessages().debug("Dropped item: %s", is.getType());
					if (Reward.isReward(is)) {
						Reward reward = Reward.getReward(is);
						if (reward.isBagOfGoldReward() || reward.isItemReward())
							balance = balance + reward.getMoney();
					}
				}
			} else {
				balance = getBalance(playerToBeRobbed);
			}
			penalty = Math
					.round(Double
							.valueOf(plugin.getConfigManager().mobKillsPlayerPenalty.trim().substring(0,
									plugin.getConfigManager().mobKillsPlayerPenalty.trim().length() - 1))
							* balance / 100);
			return Tools.round(penalty);
		} else
			return Double.valueOf(plugin.getConfigManager().mobKillsPlayerPenalty.trim());
	}

	public double getRandomPrice(String str) {
		if (str == null || str.equals("") || str.isEmpty()) {
			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[MobHunting] [WARNING]" + ChatColor.RESET
					+ " The random_bounty_prize is not set in config.yml. Please set the prize to 0 or a positive number.");
			return 0;
		} else if (str.contains(":")) {
			String[] str1 = str.split(":");
			double prize = (plugin.mRand.nextDouble() * (Double.valueOf(str1[1]) - Double.valueOf(str1[0]))
					+ Double.valueOf(str1[0]));
			return Tools.round(prize);
		} else
			return Double.valueOf(str);
	}

	/**
	 * Return the reward money for a given mob
	 * 
	 * @param mob
	 * @return value
	 */
	public double getBaseKillPrize(Entity mob) {
		if (TARDISWeepingAngelsCompat.isWeepingAngelMonster(mob)) {
			if (TARDISWeepingAngelsCompat.getMobRewardData()
					.containsKey(TARDISWeepingAngelsCompat.getWeepingAngelMonsterType(mob).name()))
				return getPrice(mob, TARDISWeepingAngelsCompat.getMobRewardData()
						.get(TARDISWeepingAngelsCompat.getWeepingAngelMonsterType(mob).name()).getRewardPrize());
			plugin.getMessages().debug("TARDISWeepingAngel %s has no reward data",
					TARDISWeepingAngelsCompat.getWeepingAngelMonsterType(mob).getName());
			return 0;

		} else if (MythicMobsCompat.isMythicMob(mob)) {
			if (MythicMobsCompat.getMobRewardData().containsKey(MythicMobsCompat.getMythicMobType(mob)))
				return getPrice(mob, MythicMobsCompat.getMobRewardData().get(MythicMobsCompat.getMythicMobType(mob))
						.getRewardPrize());
			plugin.getMessages().debug("MythicMob %s has no reward data", MythicMobsCompat.getMythicMobType(mob));
			return 0;

		} else if (CitizensCompat.isSentryOrSentinelOrSentries(mob)) {
			NPC npc = CitizensAPI.getNPCRegistry().getNPC(mob);
			String key = String.valueOf(npc.getId());
			if (CitizensCompat.getMobRewardData().containsKey(key)) {
				return getPrice(mob, CitizensCompat.getMobRewardData().get(key).getRewardPrize());
			}
			plugin.getMessages().debug("Citizens mob %s has no reward data", npc.getName());
			return 0;

		} else if (CustomMobsCompat.isCustomMob(mob)) {
			if (CustomMobsCompat.getMobRewardData().containsKey(CustomMobsCompat.getCustomMobType(mob)))
				return getPrice(mob, CustomMobsCompat.getMobRewardData().get(CustomMobsCompat.getCustomMobType(mob))
						.getRewardPrize());
			plugin.getMessages().debug("CustomMob %s has no reward data", CustomMobsCompat.getCustomMobType(mob));
			return 0;

		} else if (MysteriousHalloweenCompat.isMysteriousHalloween(mob)) {
			if (MysteriousHalloweenCompat.getMobRewardData()
					.containsKey(MysteriousHalloweenCompat.getMysteriousHalloweenType(mob).name()))
				return getPrice(mob, MysteriousHalloweenCompat.getMobRewardData()
						.get(MysteriousHalloweenCompat.getMysteriousHalloweenType(mob).name()).getRewardPrize());
			plugin.getMessages().debug("MysteriousHalloween %s has no reward data",
					MysteriousHalloweenCompat.getMysteriousHalloweenType(mob).name());
			return 0;

		} else if (SmartGiantsCompat.isSmartGiants(mob)) {
			if (SmartGiantsCompat.getMobRewardData().containsKey(SmartGiantsCompat.getSmartGiantsMobType(mob)))
				return getPrice(mob, SmartGiantsCompat.getMobRewardData()
						.get(SmartGiantsCompat.getSmartGiantsMobType(mob)).getRewardPrize());
			plugin.getMessages().debug("SmartGiantsS %s has no reward data",
					SmartGiantsCompat.getSmartGiantsMobType(mob));
			return 0;

		} else if (MyPetCompat.isMyPet(mob)) {
			plugin.getMessages().debug("Tried to find a prize for a MyPet: %s (Owner=%s)", MyPetCompat.getMyPet(mob),
					MyPetCompat.getMyPetOwner(mob));
			return getPrice(mob, plugin.getConfigManager().wolfMoney);

		} else if (HerobrineCompat.isHerobrineMob(mob)) {
			if (HerobrineCompat.getMobRewardData().containsKey(HerobrineCompat.getHerobrineMobType(mob)))
				return getPrice(mob, HerobrineCompat.getMobRewardData().get(HerobrineCompat.getHerobrineMobType(mob))
						.getRewardPrize());
			plugin.getMessages().debug("Herobrine mob %s has no reward data", HerobrineCompat.getHerobrineMobType(mob));
			return 0;

		} else if (EliteMobsCompat.isEliteMobs(mob)) {
			if (EliteMobsCompat.getMobRewardData().containsKey(EliteMobsCompat.getEliteMobsType(mob).getName()))
				return getPrice(mob, EliteMobsCompat.getMobRewardData()
						.get(EliteMobsCompat.getEliteMobsType(mob).getName()).getRewardPrize());
			plugin.getMessages().debug("EliteMob %s has no reward data", EliteMobsCompat.getEliteMobsType(mob));
			return 0;

		} else if (BossCompat.isBossMob(mob)) {
			if (BossCompat.getMobRewardData().containsKey(BossCompat.getBossType(mob)))
				return getPrice(mob, BossCompat.getMobRewardData().get(BossCompat.getBossType(mob)).getRewardPrize());
			plugin.getMessages().debug("Boss mob %s has no reward data", BossCompat.getBossType(mob));
			return 0;

		} else {
			// 1.21+ compatibility aliases for entities not exposed as dedicated sections yet.
			if (isEntityType(mob, "ARMADILLO"))
				return getPrice(mob, plugin.getConfigManager().turtleMoney);
			else if (isEntityType(mob, "BOGGED"))
				return getPrice(mob, plugin.getConfigManager().strayMoney);
			else if (isEntityType(mob, "BREEZE"))
				return getPrice(mob, plugin.getConfigManager().vexMoney);
			else if (isEntityType(mob, "CREAKING"))
				return getPrice(mob, plugin.getConfigManager().wardenMoney);

			if (Servers.isMC119OrNewer())
				if (mob instanceof Allay)
					return getPrice(mob, plugin.getConfigManager().allayMoney);
				else if (mob instanceof Frog)
					return getPrice(mob, plugin.getConfigManager().frogMoney);
				else if (mob instanceof Tadpole)
					return getPrice(mob, plugin.getConfigManager().tadpoleMoney);
				else if (mob instanceof Warden)
					return getPrice(mob, plugin.getConfigManager().wardenMoney);

			if (Servers.isMC117OrNewer())
				if (mob instanceof Axolotl)
					return getPrice(mob, plugin.getConfigManager().axolotlMoney);
				else if (mob instanceof Goat)
					return getPrice(mob, plugin.getConfigManager().goatMoney);
				else if (mob instanceof GlowSquid)
					return getPrice(mob, plugin.getConfigManager().glowsquidMoney);

			if (Servers.isMC1162OrNewer())
				if (mob instanceof PiglinBrute)
					return getPrice(mob, plugin.getConfigManager().piglinBruteMoney);

			if (Servers.isMC116OrNewer())
				if (mob instanceof Hoglin)
					return getPrice(mob, plugin.getConfigManager().hoglinMoney);
				else if (mob instanceof Piglin)
					return getPrice(mob, plugin.getConfigManager().piglinMoney);
				else if (mob instanceof Strider)
					return getPrice(mob, plugin.getConfigManager().striderMoney);
				else if (mob instanceof Zoglin)
					return getPrice(mob, plugin.getConfigManager().zoglinMoney);

			if (Servers.isMC115OrNewer())
				if (mob instanceof Bee)
					return getPrice(mob, plugin.getConfigManager().beeMoney);

			if (Servers.isMC114OrNewer())
				if (mob instanceof Cat)
					return getPrice(mob, plugin.getConfigManager().catMoney);
				else if (mob instanceof Fox)
					return getPrice(mob, plugin.getConfigManager().foxMoney);
				else if (mob instanceof Panda)
					return getPrice(mob, plugin.getConfigManager().pandaMoney);
				else if (mob instanceof Pillager)
					return getPrice(mob, plugin.getConfigManager().pillagerMoney);
				else if (mob instanceof Ravager)
					return getPrice(mob, plugin.getConfigManager().ravagerMoney);
				else if (mob instanceof Husk)
					return getPrice(mob, plugin.getConfigManager().huskMoney);
				else if (mob instanceof Stray)
					return getPrice(mob, plugin.getConfigManager().strayMoney);
				else if (mob instanceof TraderLlama)
					return getPrice(mob, plugin.getConfigManager().traderLlamaMoney);
				else if (mob instanceof WanderingTrader)
					return getPrice(mob, plugin.getConfigManager().wanderingTraderMoney);
				else if (mob instanceof Villager)
					if (((Villager) mob).getProfession() == Profession.ARMORER)
						return getPrice(mob, plugin.getConfigManager().armorerMoney);
					else if (((Villager) mob).getProfession() == Profession.BUTCHER)
						return getPrice(mob, plugin.getConfigManager().butcherMoney);
					else if (((Villager) mob).getProfession() == Profession.CARTOGRAPHER)
						return getPrice(mob, plugin.getConfigManager().cartographerMoney);
					else if (((Villager) mob).getProfession() == Profession.CLERIC)
						return getPrice(mob, plugin.getConfigManager().clericMoney);
					else if (((Villager) mob).getProfession() == Profession.FARMER)
						return getPrice(mob, plugin.getConfigManager().farmerMoney);
					else if (((Villager) mob).getProfession() == Profession.FISHERMAN)
						return getPrice(mob, plugin.getConfigManager().fishermanMoney);
					else if (((Villager) mob).getProfession() == Profession.FLETCHER)
						return getPrice(mob, plugin.getConfigManager().fletcherMoney);
					else if (((Villager) mob).getProfession() == Profession.LEATHERWORKER)
						return getPrice(mob, plugin.getConfigManager().leatherworkerMoney);
					else if (((Villager) mob).getProfession() == Profession.LIBRARIAN)
						return getPrice(mob, plugin.getConfigManager().librarianMoney);
					else if (((Villager) mob).getProfession() == Profession.MASON)
						return getPrice(mob, plugin.getConfigManager().masonMoney);
					else if (((Villager) mob).getProfession() == Profession.NITWIT)
						return getPrice(mob, plugin.getConfigManager().nitwitMoney);
					else if (((Villager) mob).getProfession() == Profession.NONE)
						return getPrice(mob, plugin.getConfigManager().villagerMoney);
					else if (((Villager) mob).getProfession() == Profession.SHEPHERD)
						return getPrice(mob, plugin.getConfigManager().shepherdMoney);
					else if (((Villager) mob).getProfession() == Profession.TOOLSMITH)
						return getPrice(mob, plugin.getConfigManager().toolsmithMoney);
					else if (((Villager) mob).getProfession() == Profession.WEAPONSMITH)
						return getPrice(mob, plugin.getConfigManager().weaponsmithMoney);

			if (Servers.isMC113OrNewer())
				if (mob instanceof Dolphin)
					return getPrice(mob, plugin.getConfigManager().dolphinMoney);
				else if (mob instanceof Drowned)
					return getPrice(mob, plugin.getConfigManager().drownedMoney);
				else if (mob instanceof Cod)
					return getPrice(mob, plugin.getConfigManager().codMoney);
				else if (mob instanceof Salmon)
					return getPrice(mob, plugin.getConfigManager().salmonMoney);
				else if (mob instanceof TropicalFish)
					return getPrice(mob, plugin.getConfigManager().tropicalFishMoney);
				else if (mob instanceof PufferFish)
					return getPrice(mob, plugin.getConfigManager().pufferfishMoney);
				else if (mob instanceof Phantom)
					return getPrice(mob, plugin.getConfigManager().phantomMoney);

				else if (mob instanceof Turtle)
					return getPrice(mob, plugin.getConfigManager().turtleMoney);

			if (Servers.isMC112OrNewer())
				if (mob instanceof Parrot)
					return getPrice(mob, plugin.getConfigManager().parrotMoney);
				else if (mob instanceof Illusioner)
					return getPrice(mob, plugin.getConfigManager().illusionerMoney);

			if (Servers.isMC111OrNewer())
				if (mob instanceof Llama)
					return getPrice(mob, plugin.getConfigManager().llamaMoney);
				else if (mob instanceof Vex)
					return getPrice(mob, plugin.getConfigManager().vexMoney);
				else if (mob instanceof Vindicator)
					return getPrice(mob, plugin.getConfigManager().vindicatorMoney);
				else if (mob instanceof Evoker)
					return getPrice(mob, plugin.getConfigManager().evokerMoney);
				else if (mob instanceof Donkey)
					return getPrice(mob, plugin.getConfigManager().donkeyMoney);
				else if (mob instanceof Mule)
					return getPrice(mob, plugin.getConfigManager().muleMoney);
				else if (mob instanceof SkeletonHorse)
					return getPrice(mob, plugin.getConfigManager().skeletonHorseMoney);
				else if (mob instanceof ZombieHorse)
					return getPrice(mob, plugin.getConfigManager().zombieHorseMoney);
				else if (mob instanceof Stray)
					return getPrice(mob, plugin.getConfigManager().strayMoney);
				else if (mob instanceof Husk)
					return getPrice(mob, plugin.getConfigManager().huskMoney);
				else if (mob instanceof ZombieVillager)
					return getPrice(mob, plugin.getConfigManager().zombieVillagerMoney);
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.NITWIT)
					return getPrice(mob, plugin.getConfigManager().nitwitMoney);

			if (Servers.isMC110OrNewer())
				if (mob instanceof PolarBear)
					return getPrice(mob, plugin.getConfigManager().polarBearMoney);
				else if (mob instanceof Skeleton && ((Skeleton) mob).getSkeletonType() == SkeletonType.STRAY)
					return getPrice(mob, plugin.getConfigManager().strayMoney);

			// Handle old villagers
			if (Servers.isMC110OrNewer() && !Servers.isMC114OrNewer())
				if (mob instanceof Zombie && ((Zombie) mob).getVillagerProfession() == Profession.valueOf("HUSK"))
					return getPrice(mob, plugin.getConfigManager().huskMoney);
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.valueOf("NORMAL"))
					return getPrice(mob, plugin.getConfigManager().villagerMoney);
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.valueOf("PRIEST"))
					return getPrice(mob, plugin.getConfigManager().priestMoney);
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.valueOf("BUTCHER"))
					return getPrice(mob, plugin.getConfigManager().butcherMoney);
				else if (mob instanceof Villager
						&& ((Villager) mob).getProfession() == Profession.valueOf("BLACKSMITH"))
					return getPrice(mob, plugin.getConfigManager().blacksmithMoney);
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.valueOf("LIBRARIAN"))
					return getPrice(mob, plugin.getConfigManager().librarianMoney);
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.valueOf("FARMER"))
					return getPrice(mob, plugin.getConfigManager().farmerMoney);

			if (Servers.isMC19OrNewer())
				if (mob instanceof Shulker)
					return getPrice(mob, plugin.getConfigManager().shulkerMoney);

			// Minecraft 1.8 and older entities
			if (mob instanceof Guardian && ((Guardian) mob).isElder())
				return getPrice(mob, plugin.getConfigManager().elderGuardianMoney);
			else if (mob instanceof Guardian)
				return getPrice(mob, plugin.getConfigManager().guardianMoney);
			else if (mob instanceof Endermite)
				return getPrice(mob, plugin.getConfigManager().endermiteMoney);
			else if (mob instanceof Rabbit)
				if (((Rabbit) mob).getRabbitType() == Rabbit.Type.THE_KILLER_BUNNY)
					return getPrice(mob, plugin.getConfigManager().killerRabbitMoney);
				else
					return getPrice(mob, plugin.getConfigManager().rabbitMoney);
			else if (mob instanceof Player) {
				if (plugin.getConfigManager().pvpKillMoney.trim().endsWith("%")) {
					double prize = 0;
					prize = Math.round(Double
							.valueOf(plugin.getConfigManager().pvpKillMoney.trim().substring(0,
									plugin.getConfigManager().pvpKillMoney.trim().length() - 1))
							* getBalance((Player) mob) / 100);
					return Tools.round(prize);
				} else if (plugin.getConfigManager().pvpKillMoney.contains(":")) {
					String[] str1 = plugin.getConfigManager().pvpKillMoney.split(":");
					double prize2 = (plugin.mRand.nextDouble() * (Double.valueOf(str1[1]) - Double.valueOf(str1[0]))
							+ Double.valueOf(str1[0]));
					return Tools.round(Double.valueOf(prize2));
				} else
					return Double.valueOf(plugin.getConfigManager().pvpKillMoney.trim());
			} else if (mob instanceof Blaze)
				return getPrice(mob, plugin.getConfigManager().blazeMoney);
			else if (mob instanceof Creeper)
				return getPrice(mob, plugin.getConfigManager().creeperMoney);
			else if (mob instanceof Silverfish)
				return getPrice(mob, plugin.getConfigManager().silverfishMoney);
			else if (mob instanceof Enderman)
				return getPrice(mob, plugin.getConfigManager().endermanMoney);
			else if (mob instanceof Giant)
				return getPrice(mob, plugin.getConfigManager().giantMoney);
			else if (mob instanceof Skeleton && ((Skeleton) mob).getSkeletonType() == SkeletonType.NORMAL)
				return getPrice(mob, plugin.getConfigManager().skeletonMoney);
			else if (mob instanceof Skeleton && ((Skeleton) mob).getSkeletonType() == SkeletonType.WITHER)
				return getPrice(mob, plugin.getConfigManager().witherSkeletonMoney);
			else if (mob instanceof CaveSpider)
				return getPrice(mob, plugin.getConfigManager().caveSpiderMoney);
			else if (mob instanceof Spider)
				return getPrice(mob, plugin.getConfigManager().spiderMoney);
			else if (mob instanceof Witch)
				return getPrice(mob, plugin.getConfigManager().witchMoney);
			else if (mob instanceof PigZombie)
				// PigZombie is a subclass of Zombie.
				if (((PigZombie) mob).isBaby())
					return Tools.round(getPrice(mob, plugin.getConfigManager().zombiePigmanMoney)
							* plugin.getConfigManager().babyMultiplier);
				else
					return getPrice(mob, plugin.getConfigManager().zombiePigmanMoney);
			else if (mob instanceof Zombie)
				if (((Zombie) mob).isBaby())
					return Tools.round(getPrice(mob, plugin.getConfigManager().zombieMoney)
							* plugin.getConfigManager().babyMultiplier);
				else
					return getPrice(mob, plugin.getConfigManager().zombieMoney);
			else if (mob instanceof Ghast)
				return getPrice(mob, plugin.getConfigManager().ghastMoney);
			else if (mob instanceof MagmaCube)
				// MagmaCube is a subclass of Slime
				return getPrice(mob, plugin.getConfigManager().magmaCubeMoney) * ((MagmaCube) mob).getSize();
			else if (mob instanceof Slime)
				return getPrice(mob, plugin.getConfigManager().slimeMoney) * ((Slime) mob).getSize();
			else if (mob instanceof EnderDragon)
				return getPrice(mob, plugin.getConfigManager().enderDragonMoney);
			else if (mob instanceof Wither)
				return getPrice(mob, plugin.getConfigManager().witherMoney);
			else if (mob instanceof IronGolem)
				return getPrice(mob, plugin.getConfigManager().ironGolemMoney);

			// Passive mobs
			else if (mob instanceof Bat)
				return getPrice(mob, plugin.getConfigManager().batMoney);
			else if (mob instanceof Chicken)
				return getPrice(mob, plugin.getConfigManager().chickenMoney);
			else if (mob instanceof Cow)
				if (mob instanceof MushroomCow)
					// MushroomCow is a subclass of Cow
					return getPrice(mob, plugin.getConfigManager().mushroomCowMoney);
				else
					return getPrice(mob, plugin.getConfigManager().cowPrize);
			else if (mob instanceof Horse)
				return getPrice(mob, plugin.getConfigManager().horseMoney);
			else if (mob instanceof Ocelot)
				return getPrice(mob, plugin.getConfigManager().ocelotMoney);
			else if (mob instanceof Pig)
				return getPrice(mob, plugin.getConfigManager().pigMoney);
			else if (mob instanceof Sheep)
				return getPrice(mob, plugin.getConfigManager().sheepMoney);
			else if (mob instanceof Snowman)
				return getPrice(mob, plugin.getConfigManager().snowmanMoney);
			else if (mob instanceof Squid)
				return getPrice(mob, plugin.getConfigManager().squidMoney);
			else if (mob instanceof Villager)
				return getPrice(mob, plugin.getConfigManager().villagerMoney);
			else if (mob instanceof Wolf)
				return getPrice(mob, plugin.getConfigManager().wolfMoney);
			else if (mob instanceof Item && ((Item) mob).getItemStack().getType() == Material.COD)
				return getPrice(mob, plugin.getConfigManager().codMoney);
			else if (mob instanceof Item && ((Item) mob).getItemStack().getType() == Material.SALMON)
				return getPrice(mob, plugin.getConfigManager().salmonMoney);
			else if (mob instanceof Item && ((Item) mob).getItemStack().getType() == Material.TROPICAL_FISH)
				return getPrice(mob, plugin.getConfigManager().tropicalFishMoney);
			else if (mob instanceof Item && ((Item) mob).getItemStack().getType() == Material.PUFFERFISH)
				return getPrice(mob, plugin.getConfigManager().pufferfishMoney);
		}
		// plugin.getMessages().debug("Mobhunting could not find the prize for
		// killing this
		// mob: %s (%s)",
		// ExtendedMobManager.getMobName(mob), mob.getType());
		return 0;
	}

	private double getPrice(Entity mob, String str) {
		try {
			if (str == null || str.equals("") || str.isEmpty()) {
				Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[MobHunting] [WARNING]"
						+ ChatColor.RESET + " The prize for killing a " + mob.getName()
						+ " is not set in config.yml. Please set the prize to 0 or a positive or negative number.");
				return 0;
			} else if (str.startsWith(":")) {
				Bukkit.getServer().getConsoleSender()
						.sendMessage(ChatColor.RED + "[MobHunting] [WARNING]" + ChatColor.RESET
								+ " The prize for killing a " + mob.getName()
								+ " in config.yml has a wrong format. The prize can't start with \":\"");
				if (str.length() > 1)
					return getPrice(mob, str.substring(1, str.length()));
				else
					return 0;
			} else if (str.contains(":")) {
				String[] str1 = str.split(":");
				double prize = (plugin.mRand.nextDouble() * (Double.valueOf(str1[1]) - Double.valueOf(str1[0]))
						+ Double.valueOf(str1[0]));
				return Tools.round(prize);
			} else
				return Double.valueOf(str);
		} catch (NumberFormatException e) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[MobHunting] [WARNING]" + ChatColor.RESET
					+ " The prize for killing a " + mob.getName() + " has an unknown format in config.yml.");
			// e.printStackTrace();
			return 0;
		}
	}

	private boolean isEntityType(Entity mob, String entityType) {
		return mob != null && mob.getType() != null && mob.getType().name().equals(entityType);
	}

	/**
	 * Get the command to be run when the player kills a Mob.
	 * 
	 * @param mob
	 * @return a number of commands to be run in the console. Each command must be
	 *         separeted by a "|"
	 */
	public List<HashMap<String, String>> getKillCommands(Entity mob) {
		if (TARDISWeepingAngelsCompat.isWeepingAngelMonster(mob)) {
			if (TARDISWeepingAngelsCompat.getMobRewardData()
					.containsKey(TARDISWeepingAngelsCompat.getWeepingAngelMonsterType(mob).name()))
				return TARDISWeepingAngelsCompat.getMobRewardData()
						.get(TARDISWeepingAngelsCompat.getWeepingAngelMonsterType(mob).name()).getConsoleRunCommand();
			return new ArrayList<>();

		} else if (MythicMobsCompat.isMythicMob(mob)) {
			if (MythicMobsCompat.getMobRewardData().containsKey(MythicMobsCompat.getMythicMobType(mob)))
				return MythicMobsCompat.getMobRewardData().get(MythicMobsCompat.getMythicMobType(mob))
						.getConsoleRunCommand();
			return new ArrayList<>();

		} else if (CitizensCompat.isNPC(mob) && CitizensCompat.isSentryOrSentinelOrSentries(mob)) {
			NPC npc = CitizensAPI.getNPCRegistry().getNPC(mob);
			String key = String.valueOf(npc.getId());
			if (CitizensCompat.getMobRewardData().containsKey(key)) {
				return CitizensCompat.getMobRewardData().get(key).getConsoleRunCommand();
			}
			return new ArrayList<>();

		} else if (CustomMobsCompat.isCustomMob(mob)) {
			if (mob.hasMetadata(CustomMobsCompat.MH_CUSTOMMOBS)) {
				List<MetadataValue> data = mob.getMetadata(CustomMobsCompat.MH_CUSTOMMOBS);
				for (MetadataValue value : data)
					if (value.value() instanceof ExtendedMobRewardData)
						return ((ExtendedMobRewardData) value.value()).getConsoleRunCommand();
			} else if (CustomMobsCompat.getMobRewardData().containsKey(CustomMobsCompat.getCustomMobType(mob)))
				return CustomMobsCompat.getMobRewardData().get(CustomMobsCompat.getCustomMobType(mob))
						.getConsoleRunCommand();
			return new ArrayList<>();

		} else if (MysteriousHalloweenCompat.isMysteriousHalloween(mob)) {
			if (MysteriousHalloweenCompat.getMobRewardData()
					.containsKey(MysteriousHalloweenCompat.getMysteriousHalloweenType(mob).name()))
				return MysteriousHalloweenCompat.getMobRewardData()
						.get(MysteriousHalloweenCompat.getMysteriousHalloweenType(mob).name()).getConsoleRunCommand();
			return new ArrayList<>();

		} else if (SmartGiantsCompat.isSmartGiants(mob)) {
			if (SmartGiantsCompat.getMobRewardData().containsKey(SmartGiantsCompat.getSmartGiantsMobType(mob)))
				return SmartGiantsCompat.getMobRewardData().get(SmartGiantsCompat.getSmartGiantsMobType(mob))
						.getConsoleRunCommand();
			return new ArrayList<>();

		} else if (HerobrineCompat.isHerobrineMob(mob)) {
			if (HerobrineCompat.getMobRewardData().containsKey(HerobrineCompat.getHerobrineMobType(mob)))
				return HerobrineCompat.getMobRewardData().get(HerobrineCompat.getHerobrineMobType(mob))
						.getConsoleRunCommand();
			return new ArrayList<>();

		} else if (EliteMobsCompat.isEliteMobs(mob)) {
			if (EliteMobsCompat.getMobRewardData().containsKey(EliteMobsCompat.getEliteMobsType(mob).getName()))
				return EliteMobsCompat.getMobRewardData().get(EliteMobsCompat.getEliteMobsType(mob).getName())
						.getConsoleRunCommand();
			return new ArrayList<>();

		} else if (BossCompat.isBossMob(mob)) {
			if (BossCompat.getMobRewardData().containsKey(BossCompat.getBossType(mob)))
				return BossCompat.getMobRewardData().get(BossCompat.getBossType(mob)).getConsoleRunCommand();
			return new ArrayList<>();

		} else if (MyPetCompat.isMyPet(mob)) {
			return plugin.getConfigManager().wolfCommands;

		} else {
			if (isEntityType(mob, "ARMADILLO"))
				return plugin.getConfigManager().turtleCommands;
			else if (isEntityType(mob, "BOGGED"))
				return plugin.getConfigManager().strayCommands;
			else if (isEntityType(mob, "BREEZE"))
				return plugin.getConfigManager().vexCommands;
			else if (isEntityType(mob, "CREAKING"))
				return plugin.getConfigManager().wardenCommands;

			if (Servers.isMC119OrNewer())
				if (mob instanceof Allay)
					return plugin.getConfigManager().allayCommands;
				else if (mob instanceof Frog)
					return plugin.getConfigManager().frogCommands;
				else if (mob instanceof Tadpole)
					return plugin.getConfigManager().tadpoleCommands;
				else if (mob instanceof Warden)
					return plugin.getConfigManager().wardenCommands;

			if (Servers.isMC117OrNewer())
				if (mob instanceof Axolotl)
					return plugin.getConfigManager().axolotlCommands;
				else if (mob instanceof Goat)
					return plugin.getConfigManager().goatCommands;
				else if (mob instanceof GlowSquid)
					return plugin.getConfigManager().glowsquidCommands;

			if (Servers.isMC1162OrNewer())
				if (mob instanceof PiglinBrute)
					return plugin.getConfigManager().piglinBruteCommands;

			if (Servers.isMC116OrNewer())
				if (mob instanceof Hoglin)
					return plugin.getConfigManager().hoglinCommands;
				else if (mob instanceof Piglin)
					return plugin.getConfigManager().piglinCommands;
				else if (mob instanceof Strider)
					return plugin.getConfigManager().striderCommands;
				else if (mob instanceof Zoglin)
					return plugin.getConfigManager().zoglinCommands;

			if (Servers.isMC115OrNewer())
				if (mob instanceof Bee)
					return plugin.getConfigManager().beeCommands;

			if (Servers.isMC114OrNewer())
				if (mob instanceof Cat)
					return plugin.getConfigManager().catCommands;
				else if (mob instanceof Fox)
					return plugin.getConfigManager().foxCommands;
				else if (mob instanceof Panda)
					return plugin.getConfigManager().pandaCommands;
				else if (mob instanceof Pillager)
					return plugin.getConfigManager().pillagerCommands;
				else if (mob instanceof Ravager)
					return plugin.getConfigManager().ravagerCommands;
				else if (mob instanceof Husk)
					return plugin.getConfigManager().huskCommands;
				else if (mob instanceof Stray)
					return plugin.getConfigManager().strayCommands;
				else if (mob instanceof TraderLlama)
					return plugin.getConfigManager().traderllamaCommands;
				else if (mob instanceof WanderingTrader)
					return plugin.getConfigManager().wanderingTraderCommands;
				else if (mob instanceof Villager)
					if (((Villager) mob).getProfession() == Profession.ARMORER)
						return plugin.getConfigManager().armorerCommands;
					else if (((Villager) mob).getProfession() == Profession.BUTCHER)
						return plugin.getConfigManager().butcherCommands;
					else if (((Villager) mob).getProfession() == Profession.CARTOGRAPHER)
						return plugin.getConfigManager().cartographerCommands;
					else if (((Villager) mob).getProfession() == Profession.CLERIC)
						return plugin.getConfigManager().clericCommands;
					else if (((Villager) mob).getProfession() == Profession.FARMER)
						return plugin.getConfigManager().farmerCommands;
					else if (((Villager) mob).getProfession() == Profession.FISHERMAN)
						return plugin.getConfigManager().fishermanCommands;
					else if (((Villager) mob).getProfession() == Profession.FLETCHER)
						return plugin.getConfigManager().fletcherCommands;
					else if (((Villager) mob).getProfession() == Profession.LEATHERWORKER)
						return plugin.getConfigManager().leatherworkerCommands;
					else if (((Villager) mob).getProfession() == Profession.LIBRARIAN)
						return plugin.getConfigManager().librarianCommands;
					else if (((Villager) mob).getProfession() == Profession.MASON)
						return plugin.getConfigManager().masonCommands;
					else if (((Villager) mob).getProfession() == Profession.NITWIT)
						return plugin.getConfigManager().nitwitCommands;
					else if (((Villager) mob).getProfession() == Profession.NONE)
						return plugin.getConfigManager().villagerCommands;
					else if (((Villager) mob).getProfession() == Profession.SHEPHERD)
						return plugin.getConfigManager().shepherdCommands;
					else if (((Villager) mob).getProfession() == Profession.TOOLSMITH)
						return plugin.getConfigManager().toolsmithCommands;
					else if (((Villager) mob).getProfession() == Profession.WEAPONSMITH)
						return plugin.getConfigManager().weaponsmithCommands;

			if (Servers.isMC113OrNewer())
				if (mob instanceof Dolphin)
					return plugin.getConfigManager().dolphinCommands;
				else if (mob instanceof Drowned)
					return plugin.getConfigManager().drownedCommands;
				else if (mob instanceof Cod)
					return plugin.getConfigManager().codCommands;
				else if (mob instanceof Salmon)
					return plugin.getConfigManager().salmonCommands;
				else if (mob instanceof TropicalFish)
					return plugin.getConfigManager().tropicalFishCommands;
				else if (mob instanceof PufferFish)
					return plugin.getConfigManager().pufferfishCommands;
				else if (mob instanceof Phantom)
					return plugin.getConfigManager().phantomCommands;
				else if (mob instanceof Turtle)
					return plugin.getConfigManager().turtleCommands;

			if (Servers.isMC112OrNewer())
				if (mob instanceof Parrot)
					return plugin.getConfigManager().parrotCommands;
				else if (mob instanceof Illusioner)
					return plugin.getConfigManager().illusionerCommands;

			if (Servers.isMC111OrNewer())
				if (mob instanceof Llama)
					return plugin.getConfigManager().llamaCommands;
				else if (mob instanceof Vex)
					return plugin.getConfigManager().vexCommands;
				else if (mob instanceof Vindicator)
					return plugin.getConfigManager().vindicatorCommands;
				else if (mob instanceof Evoker)
					return plugin.getConfigManager().evokerCommands;
				else if (mob instanceof Donkey)
					return plugin.getConfigManager().donkeyCommands;
				else if (mob instanceof Mule)
					return plugin.getConfigManager().muleCommands;
				else if (mob instanceof SkeletonHorse)
					return plugin.getConfigManager().skeletonHorseCommands;
				else if (mob instanceof ZombieHorse)
					return plugin.getConfigManager().zombieHorseCommands;
				else if (mob instanceof Stray)
					return plugin.getConfigManager().strayCommands;
				else if (mob instanceof Husk)
					return plugin.getConfigManager().huskCommands;
				else if (mob instanceof ZombieVillager)
					return plugin.getConfigManager().zombieVillagerCommands;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.NITWIT)
					return plugin.getConfigManager().nitwitCommands;

			if (Servers.isMC110OrNewer())
				if (mob instanceof PolarBear)
					return plugin.getConfigManager().polarBearCommands;
				else if (mob instanceof Skeleton && ((Skeleton) mob).getSkeletonType() == SkeletonType.STRAY)
					return plugin.getConfigManager().strayCommands;

			// Handle old villagers
			if (Servers.isMC110OrNewer() && !Servers.isMC114OrNewer())
				if (mob instanceof Zombie
						&& ((Zombie) mob).getVillagerProfession() == Villager.Profession.valueOf("HUSK"))
					return plugin.getConfigManager().huskCommands;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.valueOf("NORMAL"))
					return plugin.getConfigManager().villagerCommands;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.valueOf("PRIEST"))
					return plugin.getConfigManager().priestCommands;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.valueOf("BUTCHER"))
					return plugin.getConfigManager().butcherCommands;
				else if (mob instanceof Villager
						&& ((Villager) mob).getProfession() == Profession.valueOf("BLACKSMITH"))
					return plugin.getConfigManager().blacksmithCommands;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.valueOf("LIBRARIAN"))
					return plugin.getConfigManager().librarianCommands;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.valueOf("FARMER"))
					return plugin.getConfigManager().farmerCommands;

			if (Servers.isMC19OrNewer())
				if (mob instanceof Shulker)
					return plugin.getConfigManager().shulkerCommands;

			if (mob instanceof Guardian && ((Guardian) mob).isElder())
				return plugin.getConfigManager().elderGuardianCommands;
			else if (mob instanceof Guardian)
				return plugin.getConfigManager().guardianCommands;
			else if (mob instanceof Endermite)
				return plugin.getConfigManager().endermiteCommands;
			else if (mob instanceof Rabbit)
				if ((((Rabbit) mob).getRabbitType()) == Rabbit.Type.THE_KILLER_BUNNY)
					return plugin.getConfigManager().killerRabbitCommands;
				else
					return plugin.getConfigManager().rabbitCommands;
			else if (mob instanceof Player)
				return plugin.getConfigManager().pvpCmdNew;
			else if (mob instanceof Blaze)
				return plugin.getConfigManager().blazeCommands;
			else if (mob instanceof Creeper)
				return plugin.getConfigManager().creeperCommands;
			else if (mob instanceof Silverfish)
				return plugin.getConfigManager().silverfishCommands;
			else if (mob instanceof Enderman)
				return plugin.getConfigManager().endermanCommands;
			else if (mob instanceof Giant)
				return plugin.getConfigManager().giantCommands;
			else if (mob instanceof Skeleton && ((Skeleton) mob).getSkeletonType() == SkeletonType.NORMAL)
				return plugin.getConfigManager().skeletonCommands;
			else if (mob instanceof Skeleton && ((Skeleton) mob).getSkeletonType() == SkeletonType.WITHER)
				return plugin.getConfigManager().witherSkeletonCommands;
			else if (mob instanceof Spider)
				if (mob instanceof CaveSpider)
					// CaveSpider is a sub class of Spider
					return plugin.getConfigManager().caveSpiderCommands;
				else
					return plugin.getConfigManager().spiderCommands;
			else if (mob instanceof Witch)
				return plugin.getConfigManager().witchCommands;
			else if (mob instanceof Zombie)
				if (mob instanceof PigZombie)
					return plugin.getConfigManager().zombiePigmanCommands;
				else
					return plugin.getConfigManager().zombieCommands;
			else if (mob instanceof Ghast)
				return plugin.getConfigManager().ghastCommands;
			else if (mob instanceof MagmaCube)
				// Magmacube is an instance of slime and must be checked before
				// the Slime itself
				return plugin.getConfigManager().magmaCubeCommands;
			else if (mob instanceof Slime)
				return plugin.getConfigManager().slimeCommands;
			else if (mob instanceof EnderDragon)
				return plugin.getConfigManager().enderDragonCommands;
			else if (mob instanceof Wither)
				return plugin.getConfigManager().witherCommands;
			else if (mob instanceof IronGolem)
				return plugin.getConfigManager().ironGolemCommands;

			// Passive mobs
			else if (mob instanceof Bat)
				return plugin.getConfigManager().batCommands;
			else if (mob instanceof Chicken)
				return plugin.getConfigManager().chickenCommands;

			else if (mob instanceof Cow)
				if (mob instanceof MushroomCow)
					return plugin.getConfigManager().mushroomCowCommands;
				else
					return plugin.getConfigManager().cowCmdNew;
			else if (mob instanceof Horse)
				return plugin.getConfigManager().horseCommands;
			else if (mob instanceof Ocelot)
				return plugin.getConfigManager().ocelotCommands;
			else if (mob instanceof Pig)
				return plugin.getConfigManager().pigCommands;
			else if (mob instanceof Sheep)
				return plugin.getConfigManager().sheepCommands;
			else if (mob instanceof Snowman)
				return plugin.getConfigManager().snowmanCommands;
			else if (mob instanceof Squid)
				return plugin.getConfigManager().squidCommands;
			else if (mob instanceof Villager)
				return plugin.getConfigManager().villagerCommands;
			else if (mob instanceof Wolf)
				return plugin.getConfigManager().wolfCommands;
			else if (mob instanceof Item && ((Item) mob).getItemStack().getType() == Material.COD)
				return plugin.getConfigManager().codCommands;
			else if (mob instanceof Item && ((Item) mob).getItemStack().getType() == Material.SALMON)
				return plugin.getConfigManager().salmonCommands;
			else if (mob instanceof Item && ((Item) mob).getItemStack().getType() == Material.TROPICAL_FISH)
				return plugin.getConfigManager().tropicalFishCommands;
			else if (mob instanceof Item && ((Item) mob).getItemStack().getType() == Material.PUFFERFISH)
				return plugin.getConfigManager().pufferfishCommands;
		}
		return new ArrayList<>();
	}

	/**
	 * Get the text to be send to the player describing the reward
	 * 
	 * @param mob
	 * @return String
	 */
	public String getKillMessage(Entity mob) {
		if (TARDISWeepingAngelsCompat.isWeepingAngelMonster(mob)) {
			if (TARDISWeepingAngelsCompat.getMobRewardData()
					.containsKey(TARDISWeepingAngelsCompat.getWeepingAngelMonsterType(mob).name()))
				return TARDISWeepingAngelsCompat.getMobRewardData()
						.get(TARDISWeepingAngelsCompat.getWeepingAngelMonsterType(mob).name()).getRewardDescription();
			return "";

		} else if (MythicMobsCompat.isMythicMob(mob)) {
			if (MythicMobsCompat.getMobRewardData().containsKey(MythicMobsCompat.getMythicMobType(mob)))
				return MythicMobsCompat.getMobRewardData().get(MythicMobsCompat.getMythicMobType(mob))
						.getRewardDescription();
			return "";

		} else if (CitizensCompat.isNPC(mob) && CitizensCompat.isSentryOrSentinelOrSentries(mob)) {
			NPC npc = CitizensAPI.getNPCRegistry().getNPC(mob);
			String key = String.valueOf(npc.getId());
			if (CitizensCompat.getMobRewardData().containsKey(key)) {
				return CitizensCompat.getMobRewardData().get(key).getRewardDescription();
			}
			return "";

		} else if (CustomMobsCompat.isCustomMob(mob)) {
			if (CustomMobsCompat.getMobRewardData().containsKey(CustomMobsCompat.getCustomMobType(mob)))
				return CustomMobsCompat.getMobRewardData().get(CustomMobsCompat.getCustomMobType(mob))
						.getRewardDescription();
			return "";

		} else if (MysteriousHalloweenCompat.isMysteriousHalloween(mob)) {
			if (MysteriousHalloweenCompat.getMobRewardData()
					.containsKey(MysteriousHalloweenCompat.getMysteriousHalloweenType(mob).name()))
				return MysteriousHalloweenCompat.getMobRewardData()
						.get(MysteriousHalloweenCompat.getMysteriousHalloweenType(mob).name()).getRewardDescription();
			return "";

		} else if (SmartGiantsCompat.isSmartGiants(mob)) {
			if (SmartGiantsCompat.getMobRewardData().containsKey(SmartGiantsCompat.getSmartGiantsMobType(mob)))
				return SmartGiantsCompat.getMobRewardData().get(SmartGiantsCompat.getSmartGiantsMobType(mob))
						.getRewardDescription();
			return "";

		} else if (HerobrineCompat.isHerobrineMob(mob)) {
			if (HerobrineCompat.getMobRewardData().containsKey(HerobrineCompat.getHerobrineMobType(mob)))
				return HerobrineCompat.getMobRewardData().get(HerobrineCompat.getHerobrineMobType(mob))
						.getRewardDescription();
			return "";

		} else if (EliteMobsCompat.isEliteMobs(mob)) {
			if (EliteMobsCompat.getMobRewardData().containsKey(EliteMobsCompat.getEliteMobsType(mob).getName()))
				return EliteMobsCompat.getMobRewardData().get(EliteMobsCompat.getEliteMobsType(mob).getName())
						.getRewardDescription();
			return "";

		} else if (BossCompat.isBossMob(mob)) {
			if (BossCompat.getMobRewardData().containsKey(BossCompat.getBossType(mob)))
				return BossCompat.getMobRewardData().get(BossCompat.getBossType(mob)).getRewardDescription();
			return "";

		} else if (MyPetCompat.isMyPet(mob)) {
			return plugin.getConfigManager().wolfMessage;

		} else {
			if (isEntityType(mob, "ARMADILLO"))
				return plugin.getConfigManager().turtleMessage;
			else if (isEntityType(mob, "BOGGED"))
				return plugin.getConfigManager().strayMessage;
			else if (isEntityType(mob, "BREEZE"))
				return plugin.getConfigManager().vexMessage;
			else if (isEntityType(mob, "CREAKING"))
				return plugin.getConfigManager().wardenMessage;

			if (Servers.isMC119OrNewer())
				if (mob instanceof Allay)
					return plugin.getConfigManager().allayMessage;
				else if (mob instanceof Frog)
					return plugin.getConfigManager().frogMessage;
				else if (mob instanceof Tadpole)
					return plugin.getConfigManager().tadpoleMessage;
				else if (mob instanceof Warden)
					return plugin.getConfigManager().wardenMessage;

			if (Servers.isMC117OrNewer())
				if (mob instanceof Axolotl)
					return plugin.getConfigManager().axolotlMessage;
				else if (mob instanceof Goat)
					return plugin.getConfigManager().goatMessage;
				else if (mob instanceof GlowSquid)
					return plugin.getConfigManager().glowsquidMessage;

			if (Servers.isMC1162OrNewer())
				if (mob instanceof PiglinBrute)
					return plugin.getConfigManager().piglinBruteMessage;

			if (Servers.isMC116OrNewer())
				if (mob instanceof Hoglin)
					return plugin.getConfigManager().hoglinMessage;
				else if (mob instanceof Piglin)
					return plugin.getConfigManager().piglinMessage;
				else if (mob instanceof Strider)
					return plugin.getConfigManager().striderMessage;
				else if (mob instanceof Zoglin)
					return plugin.getConfigManager().zoglinMessage;

			if (Servers.isMC115OrNewer())
				if (mob instanceof Bee)
					return plugin.getConfigManager().beeMessage;

			if (Servers.isMC114OrNewer())
				if (mob instanceof Cat)
					return plugin.getConfigManager().catMessage;
				else if (mob instanceof Fox)
					return plugin.getConfigManager().foxMessage;
				else if (mob instanceof Panda)
					return plugin.getConfigManager().pandaMessage;
				else if (mob instanceof Pillager)
					return plugin.getConfigManager().pillagerMessage;
				else if (mob instanceof Ravager)
					return plugin.getConfigManager().ravagerMessage;
				else if (mob instanceof Husk)
					return plugin.getConfigManager().huskMessage;
				else if (mob instanceof Stray)
					return plugin.getConfigManager().strayMessage;
				else if (mob instanceof TraderLlama)
					return plugin.getConfigManager().traderLlamaMessage;
				else if (mob instanceof WanderingTrader)
					return plugin.getConfigManager().wanderingTraderMessage;
				else if (mob instanceof Villager)
					if (((Villager) mob).getProfession() == Profession.ARMORER)
						return plugin.getConfigManager().armorerMessage;
					else if (((Villager) mob).getProfession() == Profession.BUTCHER)
						return plugin.getConfigManager().butcherMessage;
					else if (((Villager) mob).getProfession() == Profession.CARTOGRAPHER)
						return plugin.getConfigManager().cartographerMessage;
					else if (((Villager) mob).getProfession() == Profession.CLERIC)
						return plugin.getConfigManager().clericMessage;
					else if (((Villager) mob).getProfession() == Profession.FARMER)
						return plugin.getConfigManager().farmerMessage;
					else if (((Villager) mob).getProfession() == Profession.FISHERMAN)
						return plugin.getConfigManager().fishermanMessage;
					else if (((Villager) mob).getProfession() == Profession.FLETCHER)
						return plugin.getConfigManager().fletcherMessage;
					else if (((Villager) mob).getProfession() == Profession.LEATHERWORKER)
						return plugin.getConfigManager().leatherworkerMessage;
					else if (((Villager) mob).getProfession() == Profession.LIBRARIAN)
						return plugin.getConfigManager().librarianMessage;
					else if (((Villager) mob).getProfession() == Profession.MASON)
						return plugin.getConfigManager().masonMessage;
					else if (((Villager) mob).getProfession() == Profession.NITWIT)
						return plugin.getConfigManager().nitwitMessage;
					else if (((Villager) mob).getProfession() == Profession.NONE)
						return plugin.getConfigManager().villagerMessage;
					else if (((Villager) mob).getProfession() == Profession.SHEPHERD)
						return plugin.getConfigManager().shepherdMessage;
					else if (((Villager) mob).getProfession() == Profession.TOOLSMITH)
						return plugin.getConfigManager().toolsmithMessage;
					else if (((Villager) mob).getProfession() == Profession.WEAPONSMITH)
						return plugin.getConfigManager().weaponsmithMessage;

			if (Servers.isMC113OrNewer())
				if (mob instanceof Dolphin)
					return plugin.getConfigManager().dolphinMessage;
				else if (mob instanceof Drowned)
					return plugin.getConfigManager().drownedMessage;
				else if (mob instanceof Cod)
					return plugin.getConfigManager().codMessage;
				else if (mob instanceof Salmon)
					return plugin.getConfigManager().salmonMessage;
				else if (mob instanceof TropicalFish)
					return plugin.getConfigManager().tropicalFishMessage;
				else if (mob instanceof PufferFish)
					return plugin.getConfigManager().pufferfishMessage;
				else if (mob instanceof Phantom)
					return plugin.getConfigManager().phantomMessage;
				else if (mob instanceof Turtle)
					return plugin.getConfigManager().turtleMessage;

			if (Servers.isMC112OrNewer())
				if (mob instanceof Parrot)
					return plugin.getConfigManager().parrotMessage;
				else if (mob instanceof Illusioner)
					return plugin.getConfigManager().illusionerMessage;

			if (Servers.isMC111OrNewer())
				if (mob instanceof Llama)
					return plugin.getConfigManager().llamaMessage;
				else if (mob instanceof Vex)
					return plugin.getConfigManager().vexMessage;
				else if (mob instanceof Vindicator)
					return plugin.getConfigManager().vindicatorMessage;
				else if (mob instanceof Evoker)
					return plugin.getConfigManager().evokerMessage;
				else if (mob instanceof Donkey)
					return plugin.getConfigManager().donkeyMessage;
				else if (mob instanceof Mule)
					return plugin.getConfigManager().muleMessage;
				else if (mob instanceof SkeletonHorse)
					return plugin.getConfigManager().skeletonHorseMessage;
				else if (mob instanceof ZombieHorse)
					return plugin.getConfigManager().zombieHorseMessage;
				else if (mob instanceof Stray)
					return plugin.getConfigManager().strayMessage;
				else if (mob instanceof Husk)
					return plugin.getConfigManager().huskMessage;
				else if (mob instanceof ZombieVillager)
					return plugin.getConfigManager().zombieVillagerMessage;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.NITWIT)
					return plugin.getConfigManager().nitwitMessage;

			if (Servers.isMC110OrNewer())
				if (mob instanceof PolarBear)
					return plugin.getConfigManager().polarBearMessage;
				else if (mob instanceof Skeleton && ((Skeleton) mob).getSkeletonType() == SkeletonType.STRAY)
					return plugin.getConfigManager().strayMessage;

			if (Servers.isMC110OrNewer() && !Servers.isMC114OrNewer())
				if (mob instanceof Zombie
						&& ((Zombie) mob).getVillagerProfession() == Villager.Profession.valueOf("HUSK"))
					return plugin.getConfigManager().huskMessage;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.valueOf("NORMAL"))
					return plugin.getConfigManager().villagerMessage;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.valueOf("PRIEST"))
					return plugin.getConfigManager().priestMessage;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.BUTCHER)
					return plugin.getConfigManager().butcherMessage;
				else if (mob instanceof Villager
						&& ((Villager) mob).getProfession() == Profession.valueOf("BLACKSMITH"))
					return plugin.getConfigManager().blacksmithMessage;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.LIBRARIAN)
					return plugin.getConfigManager().librarianMessage;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.FARMER)
					return plugin.getConfigManager().farmerMessage;

			if (Servers.isMC19OrNewer())
				if (mob instanceof Shulker)
					return plugin.getConfigManager().shulkerMessage;

			// MC1.8 or older
			if (mob instanceof Guardian && ((Guardian) mob).isElder())
				return plugin.getConfigManager().elderGuardianMessage;
			else if (mob instanceof Guardian)
				return plugin.getConfigManager().guardianMessge;
			else if (mob instanceof Endermite)
				return plugin.getConfigManager().endermiteMessage;
			else if (mob instanceof Rabbit)
				if ((((Rabbit) mob).getRabbitType()) == Rabbit.Type.THE_KILLER_BUNNY)
					return plugin.getConfigManager().killerRabbitMessage;
				else
					return plugin.getConfigManager().rabbitMessage;
			else if (mob instanceof Player)
				return plugin.getConfigManager().pvpKillMessage;
			else if (mob instanceof Blaze)
				return plugin.getConfigManager().blazeMessage;
			else if (mob instanceof Creeper)
				return plugin.getConfigManager().creeperMessage;
			else if (mob instanceof Silverfish)
				return plugin.getConfigManager().silverfishMessage;
			else if (mob instanceof Enderman)
				return plugin.getConfigManager().endermanMessage;
			else if (mob instanceof Giant)
				return plugin.getConfigManager().giantMessage;
			else if (mob instanceof Skeleton && ((Skeleton) mob).getSkeletonType() == SkeletonType.NORMAL)
				return plugin.getConfigManager().skeletonMessage;
			else if (mob instanceof Skeleton && ((Skeleton) mob).getSkeletonType() == SkeletonType.WITHER)
				return plugin.getConfigManager().witherSkeletonMessage;
			else if (mob instanceof CaveSpider)
				// CaveSpider is a Subclass of Spider
				return plugin.getConfigManager().caveSpiderMessage;
			else if (mob instanceof Spider)
				return plugin.getConfigManager().spiderMessage;
			else if (mob instanceof Witch)
				return plugin.getConfigManager().witchMessage;
			else if (mob instanceof PigZombie)
				// PigZombie is a subclass of Zombie
				return plugin.getConfigManager().zombiePigmanMessage;
			else if (mob instanceof Zombie)
				return plugin.getConfigManager().zombieMessage;
			else if (mob instanceof Ghast)
				return plugin.getConfigManager().ghastMessage;
			else if (mob instanceof MagmaCube)
				// MagmaCube is a subclass of Slime
				return plugin.getConfigManager().magmaCubeMessage;
			else if (mob instanceof Slime)
				return plugin.getConfigManager().slimeMessage;
			else if (mob instanceof EnderDragon)
				return plugin.getConfigManager().enderDragonMessage;
			else if (mob instanceof Wither)
				return plugin.getConfigManager().witherMessage;
			else if (mob instanceof IronGolem)
				return plugin.getConfigManager().ironGolemMessage;

			// Passive mobs
			else if (mob instanceof Bat)
				return plugin.getConfigManager().batMessage;
			else if (mob instanceof Chicken)
				return plugin.getConfigManager().chickenMessage;
			else if (mob instanceof Cow)
				if (mob instanceof MushroomCow)
					// MushroomCow is a subclass of Cow
					return plugin.getConfigManager().mushroomCowMessage;
				else
					return plugin.getConfigManager().cowCmdDesc;
			else if (mob instanceof Horse)
				return plugin.getConfigManager().horseMessage;
			else if (mob instanceof Ocelot)
				return plugin.getConfigManager().ocelotMessage;
			else if (mob instanceof Pig)
				return plugin.getConfigManager().pigMessage;
			else if (mob instanceof Sheep)
				return plugin.getConfigManager().sheepMessage;
			else if (mob instanceof Snowman)
				return plugin.getConfigManager().snowmanMessage;
			else if (mob instanceof Squid)
				return plugin.getConfigManager().squidMessage;
			else if (mob instanceof Villager)
				return plugin.getConfigManager().villagerMessage;
			else if (mob instanceof Wolf)
				return plugin.getConfigManager().wolfMessage;
			else if (mob instanceof Item && ((Item) mob).getItemStack().getType() == Material.COD)
				return plugin.getConfigManager().codMessage;
			else if (mob instanceof Item && ((Item) mob).getItemStack().getType() == Material.SALMON)
				return plugin.getConfigManager().salmonMessage;
			else if (mob instanceof Item && ((Item) mob).getItemStack().getType() == Material.TROPICAL_FISH)
				return plugin.getConfigManager().tropicalFishMessage;
			else if (mob instanceof Item && ((Item) mob).getItemStack().getType() == Material.PUFFERFISH)
				return plugin.getConfigManager().pufferfishMessage;

		}
		return "";
	}

	public double getMoneyChance(Entity mob) {
		if (TARDISWeepingAngelsCompat.isWeepingAngelMonster(mob)) {
			if (TARDISWeepingAngelsCompat.getMobRewardData()
					.containsKey(TARDISWeepingAngelsCompat.getWeepingAngelMonsterType(mob).name()))
				return TARDISWeepingAngelsCompat.getMobRewardData()
						.get(TARDISWeepingAngelsCompat.getWeepingAngelMonsterType(mob).name()).getChance();
			return 0;

		} else if (MythicMobsCompat.isMythicMob(mob)) {
			if (MythicMobsCompat.getMobRewardData().containsKey(MythicMobsCompat.getMythicMobType(mob)))
				return MythicMobsCompat.getMobRewardData().get(MythicMobsCompat.getMythicMobType(mob)).getChance();
			return 0;

		} else if (CitizensCompat.isNPC(mob) && CitizensCompat.isSentryOrSentinelOrSentries(mob)) {
			NPCRegistry registry = CitizensAPI.getNPCRegistry();
			NPC npc = registry.getNPC(mob);
			String key = String.valueOf(npc.getId());
			if (CitizensCompat.getMobRewardData().containsKey(key)) {
				return CitizensCompat.getMobRewardData().get(key).getChance();
			}
			return 0;

		} else if (CustomMobsCompat.isCustomMob(mob)) {
			if (CustomMobsCompat.getMobRewardData().containsKey(CustomMobsCompat.getCustomMobType(mob)))
				return CustomMobsCompat.getMobRewardData().get(CustomMobsCompat.getCustomMobType(mob)).getChance();
			return 0;

		} else if (MysteriousHalloweenCompat.isMysteriousHalloween(mob)) {
			if (MysteriousHalloweenCompat.getMobRewardData()
					.containsKey(MysteriousHalloweenCompat.getMysteriousHalloweenType(mob).name()))
				return MysteriousHalloweenCompat.getMobRewardData()
						.get(MysteriousHalloweenCompat.getMysteriousHalloweenType(mob).name()).getChance();
			return 0;

		} else if (SmartGiantsCompat.isSmartGiants(mob)) {
			if (SmartGiantsCompat.getMobRewardData().containsKey(SmartGiantsCompat.getSmartGiantsMobType(mob)))
				return SmartGiantsCompat.getMobRewardData().get(SmartGiantsCompat.getSmartGiantsMobType(mob))
						.getChance();
			return 0;

		} else if (HerobrineCompat.isHerobrineMob(mob)) {
			if (HerobrineCompat.getMobRewardData().containsKey(HerobrineCompat.getHerobrineMobType(mob)))
				return HerobrineCompat.getMobRewardData().get(HerobrineCompat.getHerobrineMobType(mob)).getChance();
			return 0;

		} else if (EliteMobsCompat.isEliteMobs(mob)) {
			if (EliteMobsCompat.getMobRewardData().containsKey(EliteMobsCompat.getEliteMobsType(mob).getName()))
				return EliteMobsCompat.getMobRewardData().get(EliteMobsCompat.getEliteMobsType(mob).getName())
						.getChance();
			return 0;

		} else if (BossCompat.isBossMob(mob)) {
			if (BossCompat.getMobRewardData().containsKey(BossCompat.getBossType(mob)))
				return BossCompat.getMobRewardData().get(BossCompat.getBossType(mob)).getChance();
			return 0;

		} else if (MyPetCompat.isMyPet(mob)) {
			return plugin.getConfigManager().wolfCmdRunChance;

		} else {
			if (isEntityType(mob, "ARMADILLO"))
				return plugin.getConfigManager().turtleMoneyChance;
			else if (isEntityType(mob, "BOGGED"))
				return plugin.getConfigManager().strayMoneyChance;
			else if (isEntityType(mob, "BREEZE"))
				return plugin.getConfigManager().vexMoneyChance;
			else if (isEntityType(mob, "CREAKING"))
				return plugin.getConfigManager().wardenMoneyChance;

			if (Servers.isMC119OrNewer())
				if (mob instanceof Allay)
					return plugin.getConfigManager().allayMoneyChance;
				else if (mob instanceof Frog)
					return plugin.getConfigManager().frogMoneyChance;
				else if (mob instanceof Tadpole)
					return plugin.getConfigManager().tadpoleMoneyChance;
				else if (mob instanceof Warden)
					return plugin.getConfigManager().wardenMoneyChance;

			if (Servers.isMC117OrNewer())
				if (mob instanceof Axolotl)
					return plugin.getConfigManager().axolotlMoneyChance;
				else if (mob instanceof Goat)
					return plugin.getConfigManager().goatMoneyChance;
				else if (mob instanceof GlowSquid)
					return plugin.getConfigManager().glowsquidMoneyChance;

			if (Servers.isMC1162OrNewer())
				if (mob instanceof PiglinBrute)
					return plugin.getConfigManager().piglinBruteMoneyChance;

			if (Servers.isMC116OrNewer())
				if (mob instanceof Hoglin)
					return plugin.getConfigManager().hoglinMoneyChance;
				else if (mob instanceof Piglin)
					return plugin.getConfigManager().piglinMoneyChance;
				else if (mob instanceof Strider)
					return plugin.getConfigManager().striderMoneyChance;
				else if (mob instanceof Zoglin)
					return plugin.getConfigManager().zoglinMoneyChance;

			if (Servers.isMC115OrNewer())
				if (mob instanceof Bee)
					return plugin.getConfigManager().beeMoneyChance;

			if (Servers.isMC114OrNewer())
				if (mob instanceof Cat)
					return plugin.getConfigManager().catMoneyChance;
				else if (mob instanceof Fox)
					return plugin.getConfigManager().foxMoneyChance;
				else if (mob instanceof Panda)
					return plugin.getConfigManager().pandaMoneyChance;
				else if (mob instanceof Pillager)
					return plugin.getConfigManager().pillagerMoneyChance;
				else if (mob instanceof Ravager)
					return plugin.getConfigManager().ravagerMoneyChance;
				else if (mob instanceof Husk)
					return plugin.getConfigManager().huskMoneyChance;
				else if (mob instanceof Stray)
					return plugin.getConfigManager().strayMoneyChance;
				else if (mob instanceof TraderLlama)
					return plugin.getConfigManager().traderLlamaCmdRunChance;
				else if (mob instanceof WanderingTrader)
					return plugin.getConfigManager().wanderingTraderCmdRunChance;
				else if (mob instanceof Villager)
					if (((Villager) mob).getProfession() == Profession.ARMORER)
						return plugin.getConfigManager().armorerMoneyChance;
					else if (((Villager) mob).getProfession() == Profession.BUTCHER)
						return plugin.getConfigManager().butcherMoneyChance;
					else if (((Villager) mob).getProfession() == Profession.CARTOGRAPHER)
						return plugin.getConfigManager().cartographerMoneyChance;
					else if (((Villager) mob).getProfession() == Profession.CLERIC)
						return plugin.getConfigManager().clericMoneyChance;
					else if (((Villager) mob).getProfession() == Profession.FARMER)
						return plugin.getConfigManager().farmerMoneyChance;
					else if (((Villager) mob).getProfession() == Profession.FISHERMAN)
						return plugin.getConfigManager().fishermanMoneyChance;
					else if (((Villager) mob).getProfession() == Profession.FLETCHER)
						return plugin.getConfigManager().fletcherMoneyChance;
					else if (((Villager) mob).getProfession() == Profession.LEATHERWORKER)
						return plugin.getConfigManager().leatherworkerMoneyChance;
					else if (((Villager) mob).getProfession() == Profession.LIBRARIAN)
						return plugin.getConfigManager().librarianMoneyChance;
					else if (((Villager) mob).getProfession() == Profession.MASON)
						return plugin.getConfigManager().masonMoneyChance;
					else if (((Villager) mob).getProfession() == Profession.NITWIT)
						return plugin.getConfigManager().nitwitMoneyChance;
					else if (((Villager) mob).getProfession() == Profession.NONE)
						return plugin.getConfigManager().villagerMoneyChance;
					else if (((Villager) mob).getProfession() == Profession.SHEPHERD)
						return plugin.getConfigManager().shepherdMoneyChance;
					else if (((Villager) mob).getProfession() == Profession.TOOLSMITH)
						return plugin.getConfigManager().toolsmithMoneyChance;
					else if (((Villager) mob).getProfession() == Profession.WEAPONSMITH)
						return plugin.getConfigManager().weaponsmithMoneyChance;

			if (Servers.isMC113OrNewer())
				if (mob instanceof Dolphin)
					return plugin.getConfigManager().dolphinMoneyChance;
				else if (mob instanceof Drowned)
					return plugin.getConfigManager().drownedMoneyChance;
				else if (mob instanceof Cod)
					return plugin.getConfigManager().codCmdRunChance;
				else if (mob instanceof Salmon)
					return plugin.getConfigManager().salmonCmdRunChance;
				else if (mob instanceof TropicalFish)
					return plugin.getConfigManager().tropicalFishCmdRunChance;
				else if (mob instanceof PufferFish)
					return plugin.getConfigManager().pufferfishCmdRunChance;
				else if (mob instanceof Phantom)
					return plugin.getConfigManager().phantomMoneyChance;
				else if (mob instanceof Turtle)
					return plugin.getConfigManager().turtleMoneyChance;

			if (Servers.isMC112OrNewer())
				if (mob instanceof Parrot)
					return plugin.getConfigManager().parrotMoneyChance;
				else if (mob instanceof Illusioner)
					return plugin.getConfigManager().illusionerMoneyChance;

			if (Servers.isMC111OrNewer())
				if (mob instanceof Llama)
					return plugin.getConfigManager().llamaMoneyChance;
				else if (mob instanceof Vex)
					return plugin.getConfigManager().vexMoneyChance;
				else if (mob instanceof Vindicator)
					return plugin.getConfigManager().vindicatorMoneyChance;
				else if (mob instanceof Evoker)
					return plugin.getConfigManager().evokerMoneyChance;
				else if (mob instanceof Donkey)
					return plugin.getConfigManager().donkeyMoneyChance;
				else if (mob instanceof Mule)
					return plugin.getConfigManager().muleMoneyChance;
				else if (mob instanceof SkeletonHorse)
					return plugin.getConfigManager().skeletonHorseMoneyChance;
				else if (mob instanceof ZombieHorse)
					return plugin.getConfigManager().zombieHorseMoneyChance;
				else if (mob instanceof Stray)
					return plugin.getConfigManager().strayMoneyChance;
				else if (mob instanceof Husk)
					return plugin.getConfigManager().huskMoneyChance;
				else if (mob instanceof ZombieVillager)
					return plugin.getConfigManager().zombieVillagerMoneyChance;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.NITWIT)
					return plugin.getConfigManager().nitwitMoneyChance;

			if (Servers.isMC110OrNewer())
				if (mob instanceof PolarBear)
					return plugin.getConfigManager().polarBearMoneyChance;
				else if (mob instanceof Skeleton && ((Skeleton) mob).getSkeletonType() == SkeletonType.STRAY)
					return plugin.getConfigManager().strayMoneyChance;

			// Handle old villagers
			if (Servers.isMC110OrNewer() && !Servers.isMC114OrNewer())
				if (mob instanceof Zombie
						&& ((Zombie) mob).getVillagerProfession() == Villager.Profession.valueOf("HUSK"))
					return plugin.getConfigManager().huskMoneyChance;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.valueOf("NORMAL"))
					return plugin.getConfigManager().villagerMoneyChance;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.valueOf("PRIEST"))
					return plugin.getConfigManager().priestMoneyChance;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.BUTCHER)
					return plugin.getConfigManager().butcherMoneyChance;
				else if (mob instanceof Villager
						&& ((Villager) mob).getProfession() == Profession.valueOf("BLACKSMITH"))
					return plugin.getConfigManager().blacksmithMoneyChance;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.LIBRARIAN)
					return plugin.getConfigManager().librarianMoneyChance;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.FARMER)
					return plugin.getConfigManager().farmerMoneyChance;

			if (Servers.isMC19OrNewer())
				if (mob instanceof Shulker)
					return plugin.getConfigManager().shulkerMoneyChance;

			// MC1.8 or older
			if (mob instanceof Guardian && ((Guardian) mob).isElder())
				return plugin.getConfigManager().elderGuardianMoneyChance;
			else if (mob instanceof Guardian)
				return plugin.getConfigManager().guardianMoneyChance;
			else if (mob instanceof Endermite)
				return plugin.getConfigManager().endermiteMoneyChance;
			else if (mob instanceof Rabbit)
				if ((((Rabbit) mob).getRabbitType()) == Rabbit.Type.THE_KILLER_BUNNY)
					return plugin.getConfigManager().killerRabbitMoneyChance;
				else
					return plugin.getConfigManager().rabbitCmdRunChance;
			else if (mob instanceof Player)
				return plugin.getConfigManager().pvpCmdRunChance;
			else if (mob instanceof Blaze)
				return plugin.getConfigManager().blazeMoneyChance;
			else if (mob instanceof Creeper)
				return plugin.getConfigManager().creeperMoneyChance;
			else if (mob instanceof Silverfish)
				return plugin.getConfigManager().silverfishMoneyChance;
			else if (mob instanceof Enderman)
				return plugin.getConfigManager().endermanMoneyChance;
			else if (mob instanceof Giant)
				return plugin.getConfigManager().giantMoneyChance;
			else if (mob instanceof Skeleton && ((Skeleton) mob).getSkeletonType() == SkeletonType.NORMAL)
				return plugin.getConfigManager().skeletonMoneyChance;
			else if (mob instanceof Skeleton && ((Skeleton) mob).getSkeletonType() == SkeletonType.WITHER)
				return plugin.getConfigManager().witherSkeletonMoneyChance;
			else if (mob instanceof CaveSpider)
				// CaveSpider is a subclass of Spider
				return plugin.getConfigManager().caveSpiderMoneyChance;
			else if (mob instanceof Spider)
				return plugin.getConfigManager().spiderMoneyChance;
			else if (mob instanceof Witch)
				return plugin.getConfigManager().witchMoneyChance;
			else if (mob instanceof PigZombie)
				// PigZombie is a subclass of Zombie.
				return plugin.getConfigManager().zombiePigmanMoneyChance;
			else if (mob instanceof Zombie)
				return plugin.getConfigManager().zombieMoneyChance;
			else if (mob instanceof Ghast)
				return plugin.getConfigManager().ghastMoneyChance;
			else if (mob instanceof MagmaCube)
				// MagmaCube is a subclass of Slime
				return plugin.getConfigManager().magmaCubeMoneyChance;
			else if (mob instanceof Slime)
				return plugin.getConfigManager().slimeMoneyChance;
			else if (mob instanceof EnderDragon)
				return plugin.getConfigManager().enderDragonMoneyChance;
			else if (mob instanceof Wither)
				return plugin.getConfigManager().witherMoneyChance;
			else if (mob instanceof IronGolem)
				return plugin.getConfigManager().ironGolemMoneyChance;

			// Passive mobs
			else if (mob instanceof Bat)
				return plugin.getConfigManager().batCmdRunChance;
			else if (mob instanceof Chicken)
				return plugin.getConfigManager().chickenCmdRunChance;
			else if (mob instanceof Cow)
				if (mob instanceof MushroomCow)
					// MushroomCow is a subclass of Cow
					return plugin.getConfigManager().mushroomCowCmdRunChance;
				else
					return plugin.getConfigManager().cowCmdRunChance;
			else if (mob instanceof Horse)
				return plugin.getConfigManager().horseCmdRunChance;
			else if (mob instanceof Ocelot)
				return plugin.getConfigManager().ocelotCmdRunChance;
			else if (mob instanceof Pig)
				return plugin.getConfigManager().pigCmdRunChance;
			else if (mob instanceof Sheep)
				return plugin.getConfigManager().sheepCmdRunChance;
			else if (mob instanceof Snowman)
				return plugin.getConfigManager().snowmanCmdRunChance;
			else if (mob instanceof Squid)
				return plugin.getConfigManager().squidCmdRunChance;
			else if (mob instanceof Villager)
				return plugin.getConfigManager().villagerMoneyChance;
			else if (mob instanceof Wolf)
				return plugin.getConfigManager().wolfCmdRunChance;
			else if (mob instanceof Item && ((Item) mob).getItemStack().getType() == Material.COD)
				return plugin.getConfigManager().codCmdRunChance;
			else if (mob instanceof Item && ((Item) mob).getItemStack().getType() == Material.SALMON)
				return plugin.getConfigManager().salmonCmdRunChance;
			else if (mob instanceof Item && ((Item) mob).getItemStack().getType() == Material.TROPICAL_FISH)
				return plugin.getConfigManager().tropicalFishCmdRunChance;
			else if (mob instanceof Item && ((Item) mob).getItemStack().getType() == Material.PUFFERFISH)
				return plugin.getConfigManager().pufferfishCmdRunChance;
		}
		return 0;
	}

	public double getMcMMOChance(Entity mob) {
		if (TARDISWeepingAngelsCompat.isWeepingAngelMonster(mob)) {
			if (TARDISWeepingAngelsCompat.getMobRewardData()
					.containsKey(TARDISWeepingAngelsCompat.getWeepingAngelMonsterType(mob).name()))
				return TARDISWeepingAngelsCompat.getMobRewardData()
						.get(TARDISWeepingAngelsCompat.getWeepingAngelMonsterType(mob).name())
						.getMcMMOSkillRewardChance();
			return 0;

		} else if (MythicMobsCompat.isMythicMob(mob)) {
			if (MythicMobsCompat.getMobRewardData().containsKey(MythicMobsCompat.getMythicMobType(mob)))
				return MythicMobsCompat.getMobRewardData().get(MythicMobsCompat.getMythicMobType(mob))
						.getMcMMOSkillRewardChance();
			return 0;

		} else if (CitizensCompat.isNPC(mob) && CitizensCompat.isSentryOrSentinelOrSentries(mob)) {
			NPCRegistry registry = CitizensAPI.getNPCRegistry();
			NPC npc = registry.getNPC(mob);
			String key = String.valueOf(npc.getId());
			if (CitizensCompat.getMobRewardData().containsKey(key)) {
				return CitizensCompat.getMobRewardData().get(key).getMcMMOSkillRewardChance();
			}
			return 0;

		} else if (CustomMobsCompat.isCustomMob(mob)) {
			if (CustomMobsCompat.getMobRewardData().containsKey(CustomMobsCompat.getCustomMobType(mob)))
				return CustomMobsCompat.getMobRewardData().get(CustomMobsCompat.getCustomMobType(mob))
						.getMcMMOSkillRewardChance();
			return 0;

		} else if (MysteriousHalloweenCompat.isMysteriousHalloween(mob)) {
			if (MysteriousHalloweenCompat.getMobRewardData()
					.containsKey(MysteriousHalloweenCompat.getMysteriousHalloweenType(mob).name()))
				return MysteriousHalloweenCompat.getMobRewardData()
						.get(MysteriousHalloweenCompat.getMysteriousHalloweenType(mob).name())
						.getMcMMOSkillRewardChance();
			return 0;

		} else if (SmartGiantsCompat.isSmartGiants(mob)) {
			if (SmartGiantsCompat.getMobRewardData().containsKey(SmartGiantsCompat.getSmartGiantsMobType(mob)))
				return SmartGiantsCompat.getMobRewardData().get(SmartGiantsCompat.getSmartGiantsMobType(mob))
						.getMcMMOSkillRewardChance();
			return 0;

		} else if (HerobrineCompat.isHerobrineMob(mob)) {
			if (HerobrineCompat.getMobRewardData().containsKey(HerobrineCompat.getHerobrineMobType(mob)))
				return HerobrineCompat.getMobRewardData().get(HerobrineCompat.getHerobrineMobType(mob))
						.getMcMMOSkillRewardChance();
			return 0;

		} else if (EliteMobsCompat.isEliteMobs(mob)) {
			if (EliteMobsCompat.getMobRewardData().containsKey(EliteMobsCompat.getEliteMobsType(mob).getName()))
				return EliteMobsCompat.getMobRewardData().get(EliteMobsCompat.getEliteMobsType(mob).getName())
						.getMcMMOSkillRewardChance();
			return 0;

		} else if (BossCompat.isBossMob(mob)) {
			if (BossCompat.getMobRewardData().containsKey(BossCompat.getBossType(mob)))
				return BossCompat.getMobRewardData().get(BossCompat.getBossType(mob)).getMcMMOSkillRewardChance();
			return 0;

		} else if (MyPetCompat.isMyPet(mob)) {
			return plugin.getConfigManager().wolfMcMMOSkillRewardChance;

		} else {
			if (isEntityType(mob, "ARMADILLO"))
				return plugin.getConfigManager().turtleMcMMOSkillRewardChance;
			else if (isEntityType(mob, "BOGGED"))
				return plugin.getConfigManager().strayMcMMOSkillRewardChance;
			else if (isEntityType(mob, "BREEZE"))
				return plugin.getConfigManager().vexMcMMOSkillRewardChance;
			else if (isEntityType(mob, "CREAKING"))
				return plugin.getConfigManager().wardenMcMMOSkillRewardChance;

			if (Servers.isMC119OrNewer())
				if (mob instanceof Allay)
					return plugin.getConfigManager().allayMcMMOSkillRewardChance;
				else if (mob instanceof Frog)
					return plugin.getConfigManager().frogMcMMOSkillRewardChance;
				else if (mob instanceof Tadpole)
					return plugin.getConfigManager().tadpoleMcMMOSkillRewardChance;
				else if (mob instanceof Warden)
					return plugin.getConfigManager().wardenMcMMOSkillRewardChance;

			if (Servers.isMC117OrNewer())
				if (mob instanceof Axolotl)
					return plugin.getConfigManager().axolotlMcMMOSkillRewardChance;
				else if (mob instanceof Goat)
					return plugin.getConfigManager().goatMcMMOSkillRewardChance;
				else if (mob instanceof GlowSquid)
					return plugin.getConfigManager().glowsquidMcMMOSkillRewardChance;

			if (Servers.isMC1162OrNewer())
				if (mob instanceof PiglinBrute)
					return plugin.getConfigManager().piglinBruteMcMMOSkillRewardChance;

			if (Servers.isMC116OrNewer())
				if (mob instanceof Hoglin)
					return plugin.getConfigManager().hoglinMcMMOSkillRewardChance;
				else if (mob instanceof Piglin)
					return plugin.getConfigManager().piglinMcMMOSkillRewardChance;
				else if (mob instanceof Strider)
					return plugin.getConfigManager().striderMcMMOSkillRewardChance;
				else if (mob instanceof Zoglin)
					return plugin.getConfigManager().zoglinMcMMOSkillRewardChance;

			if (Servers.isMC115OrNewer())
				if (mob instanceof Bee)
					return plugin.getConfigManager().beeMcMMOSkillRewardChance;

			if (Servers.isMC114OrNewer())
				if (mob instanceof Cat)
					return plugin.getConfigManager().catMcMMOSkillRewardChance;
				else if (mob instanceof Fox)
					return plugin.getConfigManager().foxMcMMOSkillRewardChance;
				else if (mob instanceof Panda)
					return plugin.getConfigManager().pandaMcMMOSkillRewardChance;
				else if (mob instanceof Pillager)
					return plugin.getConfigManager().pillagerMcMMOSkillRewardChance;
				else if (mob instanceof Ravager)
					return plugin.getConfigManager().ravagerMcMMOSkillRewardChance;
				else if (mob instanceof Husk)
					return plugin.getConfigManager().huskMcMMOSkillRewardChance;
				else if (mob instanceof Stray)
					return plugin.getConfigManager().strayMcMMOSkillRewardChance;
				else if (mob instanceof TraderLlama)
					return plugin.getConfigManager().traderLlamaMcMMOSkillRewardChance;
				else if (mob instanceof WanderingTrader)
					return plugin.getConfigManager().wanderingTraderMcMMOSkillRewardChance;
				else if (mob instanceof Villager)
					if (((Villager) mob).getProfession() == Profession.ARMORER)
						return plugin.getConfigManager().armorerMcMMOSkillRewardChance;
					else if (((Villager) mob).getProfession() == Profession.BUTCHER)
						return plugin.getConfigManager().butcherMcMMOSkillRewardChance;
					else if (((Villager) mob).getProfession() == Profession.CARTOGRAPHER)
						return plugin.getConfigManager().cartographerMcMMOSkillRewardChance;
					else if (((Villager) mob).getProfession() == Profession.CLERIC)
						return plugin.getConfigManager().clericMcMMOSkillRewardChance;
					else if (((Villager) mob).getProfession() == Profession.FARMER)
						return plugin.getConfigManager().farmerMcMMOSkillRewardChance;
					else if (((Villager) mob).getProfession() == Profession.FISHERMAN)
						return plugin.getConfigManager().fishermanMcMMOSkillRewardChance;
					else if (((Villager) mob).getProfession() == Profession.FLETCHER)
						return plugin.getConfigManager().fletcherMcMMOSkillRewardChance;
					else if (((Villager) mob).getProfession() == Profession.LEATHERWORKER)
						return plugin.getConfigManager().leatherworkerMcMMOSkillRewardChance;
					else if (((Villager) mob).getProfession() == Profession.LIBRARIAN)
						return plugin.getConfigManager().librarianMcMMOSkillRewardChance;
					else if (((Villager) mob).getProfession() == Profession.MASON)
						return plugin.getConfigManager().masonMcMMOSkillRewardChance;
					else if (((Villager) mob).getProfession() == Profession.NITWIT)
						return plugin.getConfigManager().nitwitMcMMOSkillRewardChance;
					else if (((Villager) mob).getProfession() == Profession.NONE)
						return plugin.getConfigManager().villagerMcMMOSkillRewardChance;
					else if (((Villager) mob).getProfession() == Profession.SHEPHERD)
						return plugin.getConfigManager().shepherdMcMMOSkillRewardChance;
					else if (((Villager) mob).getProfession() == Profession.TOOLSMITH)
						return plugin.getConfigManager().toolsmithMcMMOSkillRewardChance;
					else if (((Villager) mob).getProfession() == Profession.WEAPONSMITH)
						return plugin.getConfigManager().weaponsmithMcMMOSkillRewardChance;

			if (Servers.isMC113OrNewer())
				if (mob instanceof Dolphin)
					return plugin.getConfigManager().dolphinMcMMOSkillRewardChance;
				else if (mob instanceof Drowned)
					return plugin.getConfigManager().drownedMcMMOSkillRewardChance;
				else if (mob instanceof Cod)
					return plugin.getConfigManager().rawfishMcMMOSkillRewardChance;
				else if (mob instanceof Salmon)
					return plugin.getConfigManager().rawsalmonMcMMOSkillRewardChance;
				else if (mob instanceof TropicalFish)
					return plugin.getConfigManager().clownfishMcMMOSkillRewardChance;
				else if (mob instanceof PufferFish)
					return plugin.getConfigManager().pufferfishMcMMOSkillRewardChance;
				else if (mob instanceof Phantom)
					return plugin.getConfigManager().phantomMcMMOSkillRewardChance;
				else if (mob instanceof Turtle)
					return plugin.getConfigManager().turtleMcMMOSkillRewardChance;

			if (Servers.isMC112OrNewer())
				if (mob instanceof Parrot)
					return plugin.getConfigManager().parrotMcMMOSkillRewardChance;
				else if (mob instanceof Illusioner)
					return plugin.getConfigManager().illusionerMcMMOSkillRewardChance;

			if (Servers.isMC111OrNewer())
				if (mob instanceof Llama)
					return plugin.getConfigManager().llamaMcMMOSkillRewardChance;
				else if (mob instanceof Vex)
					return plugin.getConfigManager().vexMcMMOSkillRewardChance;
				else if (mob instanceof Vindicator)
					return plugin.getConfigManager().vindicatorMcMMOSkillRewardChance;
				else if (mob instanceof Evoker)
					return plugin.getConfigManager().evokerMcMMOSkillRewardChance;
				else if (mob instanceof Donkey)
					return plugin.getConfigManager().donkeyMcMMOSkillRewardChance;
				else if (mob instanceof Mule)
					return plugin.getConfigManager().muleMcMMOSkillRewardChance;
				else if (mob instanceof SkeletonHorse)
					return plugin.getConfigManager().skeletonHorseMcMMOSkillRewardChance;
				else if (mob instanceof ZombieHorse)
					return plugin.getConfigManager().zombieHorseMcMMOSkillRewardChance;
				else if (mob instanceof Stray)
					return plugin.getConfigManager().strayMcMMOSkillRewardChance;
				else if (mob instanceof Husk)
					return plugin.getConfigManager().huskMcMMOSkillRewardChance;
				else if (mob instanceof ZombieVillager)
					return plugin.getConfigManager().zombieVillagerMcMMOSkillRewardChance;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.NITWIT)
					return plugin.getConfigManager().nitwitMcMMOSkillRewardChance;

			if (Servers.isMC110OrNewer())
				if (mob instanceof PolarBear)
					return plugin.getConfigManager().polarBearMcMMOSkillRewardChance;
				else if (mob instanceof Skeleton && ((Skeleton) mob).getSkeletonType() == SkeletonType.STRAY)
					return plugin.getConfigManager().strayMcMMOSkillRewardChance;

			// Handle old villagers
			if (Servers.isMC110OrNewer() && !Servers.isMC114OrNewer())
				if (mob instanceof Zombie
						&& ((Zombie) mob).getVillagerProfession() == Villager.Profession.valueOf("HUSK"))
					return plugin.getConfigManager().huskMcMMOSkillRewardChance;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.valueOf("NORMAL"))
					return plugin.getConfigManager().villagerMcMMOSkillRewardChance;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.valueOf("PRIEST"))
					return plugin.getConfigManager().priestMcMMOSkillRewardChance;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.BUTCHER)
					return plugin.getConfigManager().butcherMcMMOSkillRewardChance;
				else if (mob instanceof Villager
						&& ((Villager) mob).getProfession() == Profession.valueOf("BLACKSMITH"))
					return plugin.getConfigManager().blacksmithMcMMOSkillRewardChance;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.LIBRARIAN)
					return plugin.getConfigManager().librarianMcMMOSkillRewardChance;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.FARMER)
					return plugin.getConfigManager().farmerMcMMOSkillRewardChance;

			if (Servers.isMC19OrNewer())
				if (mob instanceof Shulker)
					return plugin.getConfigManager().shulkerMcMMOSkillRewardChance;

			// MC1.8 or older
			if (mob instanceof Guardian && ((Guardian) mob).isElder())
				return plugin.getConfigManager().elderGuardianMcMMOSkillRewardChance;
			else if (mob instanceof Guardian)
				return plugin.getConfigManager().guardianMcMMOSkillRewardChance;
			else if (mob instanceof Endermite)
				return plugin.getConfigManager().endermiteMcMMOSkillRewardChance;
			else if (mob instanceof Rabbit)
				if ((((Rabbit) mob).getRabbitType()) == Rabbit.Type.THE_KILLER_BUNNY)
					return plugin.getConfigManager().killerRabbitMcMMOSkillRewardChance;
				else
					return plugin.getConfigManager().rabbitMcMMOSkillRewardChance;
			else if (mob instanceof Player)
				return plugin.getConfigManager().pvpPlayerMcMMOSkillRewardChance;
			else if (mob instanceof Blaze)
				return plugin.getConfigManager().blazeMcMMOSkillRewardChance;
			else if (mob instanceof Creeper)
				return plugin.getConfigManager().creeperMcMMOSkillRewardChance;
			else if (mob instanceof Silverfish)
				return plugin.getConfigManager().silverfishMcMMOSkillRewardChance;
			else if (mob instanceof Enderman)
				return plugin.getConfigManager().endermanMcMMOSkillRewardChance;
			else if (mob instanceof Giant)
				return plugin.getConfigManager().giantMcMMOSkillRewardChance;
			else if (mob instanceof Skeleton && ((Skeleton) mob).getSkeletonType() == SkeletonType.NORMAL)
				return plugin.getConfigManager().skeletonMcMMOSkillRewardChance;
			else if (mob instanceof Skeleton && ((Skeleton) mob).getSkeletonType() == SkeletonType.WITHER)
				return plugin.getConfigManager().witherSkeletonMcMMOSkillRewardChance;
			else if (mob instanceof CaveSpider)
				// CaveSpider is a subclass of Spider
				return plugin.getConfigManager().caveSpiderMcMMOSkillRewardChance;
			else if (mob instanceof Spider)
				return plugin.getConfigManager().spiderMcMMOSkillRewardChance;
			else if (mob instanceof Witch)
				return plugin.getConfigManager().witchMcMMOSkillRewardChance;
			else if (mob instanceof PigZombie)
				// PigZombie is a subclass of Zombie.
				return plugin.getConfigManager().zombiePigManMcMMOSkillRewardChance;
			else if (mob instanceof Zombie)
				return plugin.getConfigManager().zombieMcMMOSkillRewardChance;
			else if (mob instanceof Ghast)
				return plugin.getConfigManager().ghastMcMMOSkillRewardChance;
			else if (mob instanceof MagmaCube)
				// MagmaCube is a subclass of Slime
				return plugin.getConfigManager().magmaCubeMcMMOSkillRewardChance;
			else if (mob instanceof Slime)
				return plugin.getConfigManager().slimeMcMMOSkillRewardChance;
			else if (mob instanceof EnderDragon)
				return plugin.getConfigManager().enderdragonMcMMOSkillRewardChance;
			else if (mob instanceof Wither)
				return plugin.getConfigManager().witherMcMMOSkillRewardChance;
			else if (mob instanceof IronGolem)
				return plugin.getConfigManager().ironGolemMcMMOSkillRewardChance;

			// Passive mobs
			else if (mob instanceof Bat)
				return plugin.getConfigManager().batMcMMOSkillRewardChance;
			else if (mob instanceof Chicken)
				return plugin.getConfigManager().chickenMcMMOSkillRewardChance;
			else if (mob instanceof Cow)
				if (mob instanceof MushroomCow)
					// MushroomCow is a subclass of Cow
					return plugin.getConfigManager().mushroomCowMcMMOSkillRewardChance;
				else
					return plugin.getConfigManager().cowMcMMOSkillRewardChance;
			else if (mob instanceof Horse)
				return plugin.getConfigManager().horseMcMMOSkillRewardChance;
			else if (mob instanceof Ocelot)
				return plugin.getConfigManager().ocelotMcMMOSkillRewardChance;
			else if (mob instanceof Pig)
				return plugin.getConfigManager().pigMcMMOSkillRewardChance;
			else if (mob instanceof Sheep)
				return plugin.getConfigManager().sheepMcMMOSkillRewardChance;
			else if (mob instanceof Snowman)
				return plugin.getConfigManager().snowmanMcMMOSkillRewardChance;
			else if (mob instanceof Squid)
				return plugin.getConfigManager().squidMcMMOSkillRewardChance;
			else if (mob instanceof Villager)
				return plugin.getConfigManager().villagerMcMMOSkillRewardChance;
			else if (mob instanceof Wolf)
				return plugin.getConfigManager().wolfMcMMOSkillRewardChance;
			else if (mob instanceof Item && ((Item) mob).getItemStack().getType() == Material.COD)
				return plugin.getConfigManager().rawfishMcMMOSkillRewardChance;
			else if (mob instanceof Item && ((Item) mob).getItemStack().getType() == Material.SALMON)
				return plugin.getConfigManager().rawsalmonMcMMOSkillRewardChance;
			else if (mob instanceof Item && ((Item) mob).getItemStack().getType() == Material.TROPICAL_FISH)
				return plugin.getConfigManager().clownfishMcMMOSkillRewardChance;
			else if (mob instanceof Item && ((Item) mob).getItemStack().getType() == Material.PUFFERFISH)
				return plugin.getConfigManager().pufferfishMcMMOSkillRewardChance;
		}
		return 0;
	}

	private int getMcMMOXP(Entity mob, String str) {
		if (str == null || str.equals("") || str.isEmpty()) {
			Bukkit.getServer().getConsoleSender()
					.sendMessage(ChatColor.RED + "[MobHunting] [WARNING]" + ChatColor.RESET
							+ " The McMMO XP for killing a " + mob.getName()
							+ " is not set in config.yml. Please set the McMMO XP to 0 or a positive number.");
			return 0;
		} else if (str.startsWith(":")) {
			Bukkit.getServer().getConsoleSender()
					.sendMessage(ChatColor.RED + "[MobHunting] [WARNING]" + ChatColor.RESET
							+ " The McMMO XP for killing a " + mob.getName()
							+ " in config.yml has a wrong format. The prize can't start with \":\"");
			if (str.length() > 1)
				return getMcMMOXP(mob, str.substring(1, str.length()));
			else
				return 0;
		} else if (str.contains(":")) {
			String[] str1 = str.split(":");
			Integer prize = plugin.mRand.nextInt(Integer.valueOf(str1[1])) + Integer.valueOf(str1[0]);
			return prize;
		} else
			return Integer.valueOf(str);
	}

	public int getMcMMOLevel(Entity mob) {
		if (TARDISWeepingAngelsCompat.isWeepingAngelMonster(mob)) {
			if (TARDISWeepingAngelsCompat.getMobRewardData()
					.containsKey(TARDISWeepingAngelsCompat.getWeepingAngelMonsterType(mob).name()))
				return TARDISWeepingAngelsCompat.getMobRewardData()
						.get(TARDISWeepingAngelsCompat.getWeepingAngelMonsterType(mob).name())
						.getMcMMOSkillRewardAmount();
			return 0;

		} else if (MythicMobsCompat.isMythicMob(mob)) {
			if (MythicMobsCompat.getMobRewardData().containsKey(MythicMobsCompat.getMythicMobType(mob)))
				return MythicMobsCompat.getMobRewardData().get(MythicMobsCompat.getMythicMobType(mob))
						.getMcMMOSkillRewardAmount();
			return 0;

		} else if (CitizensCompat.isNPC(mob) && CitizensCompat.isSentryOrSentinelOrSentries(mob)) {
			NPCRegistry registry = CitizensAPI.getNPCRegistry();
			NPC npc = registry.getNPC(mob);
			String key = String.valueOf(npc.getId());
			if (CitizensCompat.getMobRewardData().containsKey(key)) {
				return CitizensCompat.getMobRewardData().get(key).getMcMMOSkillRewardAmount();
			}
			return 0;

		} else if (CustomMobsCompat.isCustomMob(mob)) {
			if (CustomMobsCompat.getMobRewardData().containsKey(CustomMobsCompat.getCustomMobType(mob)))
				return CustomMobsCompat.getMobRewardData().get(CustomMobsCompat.getCustomMobType(mob))
						.getMcMMOSkillRewardAmount();
			return 0;

		} else if (MysteriousHalloweenCompat.isMysteriousHalloween(mob)) {
			if (MysteriousHalloweenCompat.getMobRewardData()
					.containsKey(MysteriousHalloweenCompat.getMysteriousHalloweenType(mob).name()))
				return MysteriousHalloweenCompat.getMobRewardData()
						.get(MysteriousHalloweenCompat.getMysteriousHalloweenType(mob).name())
						.getMcMMOSkillRewardAmount();
			return 0;

		} else if (SmartGiantsCompat.isSmartGiants(mob)) {
			if (SmartGiantsCompat.getMobRewardData().containsKey(SmartGiantsCompat.getSmartGiantsMobType(mob)))
				return SmartGiantsCompat.getMobRewardData().get(SmartGiantsCompat.getSmartGiantsMobType(mob))
						.getMcMMOSkillRewardAmount();
			return 0;

		} else if (HerobrineCompat.isHerobrineMob(mob)) {
			if (HerobrineCompat.getMobRewardData().containsKey(HerobrineCompat.getHerobrineMobType(mob)))
				return HerobrineCompat.getMobRewardData().get(HerobrineCompat.getHerobrineMobType(mob))
						.getMcMMOSkillRewardAmount();
			return 0;

		} else if (EliteMobsCompat.isEliteMobs(mob)) {
			if (EliteMobsCompat.getMobRewardData().containsKey(EliteMobsCompat.getEliteMobsType(mob).getName()))
				return EliteMobsCompat.getMobRewardData().get(EliteMobsCompat.getEliteMobsType(mob).getName())
						.getMcMMOSkillRewardAmount();
			return 0;

		} else if (BossCompat.isBossMob(mob)) {
			if (BossCompat.getMobRewardData().containsKey(BossCompat.getBossType(mob)))
				return BossCompat.getMobRewardData().get(BossCompat.getBossType(mob)).getMcMMOSkillRewardAmount();
			return 0;

		} else if (MyPetCompat.isMyPet(mob)) {
			return getMcMMOXP(mob, plugin.getConfigManager().wolfMcMMOSkillRewardAmount);

		} else {
			if (isEntityType(mob, "ARMADILLO"))
				return getMcMMOXP(mob, plugin.getConfigManager().turtleMcMMOSkillRewardAmount);
			else if (isEntityType(mob, "BOGGED"))
				return getMcMMOXP(mob, plugin.getConfigManager().strayMcMMOSkillRewardAmount);
			else if (isEntityType(mob, "BREEZE"))
				return getMcMMOXP(mob, plugin.getConfigManager().vexMcMMOSkillRewardAmount);
			else if (isEntityType(mob, "CREAKING"))
				return getMcMMOXP(mob, plugin.getConfigManager().wardenMcMMOSkillRewardAmount);

			if (Servers.isMC119OrNewer())
				if (mob instanceof Allay)
					return getMcMMOXP(mob, plugin.getConfigManager().allayMcMMOSkillRewardAmount);
				else if (mob instanceof Frog)
					return getMcMMOXP(mob, plugin.getConfigManager().frogMcMMOSkillRewardAmount);
				else if (mob instanceof Tadpole)
					return getMcMMOXP(mob, plugin.getConfigManager().tadpoleMcMMOSkillRewardAmount);
				else if (mob instanceof Warden)
					return getMcMMOXP(mob, plugin.getConfigManager().wardenMcMMOSkillRewardAmount);

			if (Servers.isMC117OrNewer())
				if (mob instanceof Axolotl)
					return getMcMMOXP(mob, plugin.getConfigManager().axolotlMcMMOSkillRewardAmount);
				else if (mob instanceof Goat)
					return getMcMMOXP(mob, plugin.getConfigManager().goatMcMMOSkillRewardAmount);
				else if (mob instanceof GlowSquid)
					return getMcMMOXP(mob, plugin.getConfigManager().glowsquidMcMMOSkillRewardAmount);

			if (Servers.isMC1162OrNewer())
				if (mob instanceof PiglinBrute)
					return getMcMMOXP(mob, plugin.getConfigManager().piglinBruteMcMMOSkillRewardAmount);

			if (Servers.isMC116OrNewer())
				if (mob instanceof Hoglin)
					return getMcMMOXP(mob, plugin.getConfigManager().hoglinMcMMOSkillRewardAmount);
				else if (mob instanceof Piglin)
					return getMcMMOXP(mob, plugin.getConfigManager().piglinMcMMOSkillRewardAmount);
				else if (mob instanceof Strider)
					return getMcMMOXP(mob, plugin.getConfigManager().striderMcMMOSkillRewardAmount);
				else if (mob instanceof Zoglin)
					return getMcMMOXP(mob, plugin.getConfigManager().zoglinMcMMOSkillRewardAmount);

			if (Servers.isMC115OrNewer())
				if (mob instanceof Bee)
					return getMcMMOXP(mob, plugin.getConfigManager().beeMcMMOSkillRewardAmount);

			if (Servers.isMC114OrNewer())
				if (mob instanceof Cat)
					return getMcMMOXP(mob, plugin.getConfigManager().catMcMMOSkillRewardAmount);
				else if (mob instanceof Fox)
					return getMcMMOXP(mob, plugin.getConfigManager().foxMcMMOSkillRewardAmount);
				else if (mob instanceof Panda)
					return getMcMMOXP(mob, plugin.getConfigManager().pandaMcMMOSkillRewardAmount);
				else if (mob instanceof Pillager)
					return getMcMMOXP(mob, plugin.getConfigManager().pillagerMcMMOSkillRewardAmount);
				else if (mob instanceof Ravager)
					return getMcMMOXP(mob, plugin.getConfigManager().ravagerMcMMOSkillRewardAmount);
				else if (mob instanceof Husk)
					return getMcMMOXP(mob, plugin.getConfigManager().huskMcMMOSkillRewardAmount);
				else if (mob instanceof Stray)
					return getMcMMOXP(mob, plugin.getConfigManager().strayMcMMOSkillRewardAmount);
				else if (mob instanceof TraderLlama)
					return getMcMMOXP(mob, plugin.getConfigManager().traderLlamaMcMMOSkillRewardAmount);
				else if (mob instanceof WanderingTrader)
					return getMcMMOXP(mob, plugin.getConfigManager().wanderingTraderMcMMOSkillRewardAmount);
				else if (mob instanceof Villager)
					if (((Villager) mob).getProfession() == Profession.ARMORER)
						return getMcMMOXP(mob, plugin.getConfigManager().armorerMcMMOSkillRewardAmount);
					else if (((Villager) mob).getProfession() == Profession.BUTCHER)
						return getMcMMOXP(mob, plugin.getConfigManager().butcherMcMMOSkillRewardAmount);
					else if (((Villager) mob).getProfession() == Profession.CARTOGRAPHER)
						return getMcMMOXP(mob, plugin.getConfigManager().cartographerMcMMOSkillRewardAmount);
					else if (((Villager) mob).getProfession() == Profession.CLERIC)
						return getMcMMOXP(mob, plugin.getConfigManager().clericMcMMOSkillRewardAmount);
					else if (((Villager) mob).getProfession() == Profession.FARMER)
						return getMcMMOXP(mob, plugin.getConfigManager().farmerMcMMOSkillRewardAmount);
					else if (((Villager) mob).getProfession() == Profession.FISHERMAN)
						return getMcMMOXP(mob, plugin.getConfigManager().fishermanMcMMOSkillRewardAmount);
					else if (((Villager) mob).getProfession() == Profession.FLETCHER)
						return getMcMMOXP(mob, plugin.getConfigManager().fletcherMcMMOSkillRewardAmount);
					else if (((Villager) mob).getProfession() == Profession.LEATHERWORKER)
						return getMcMMOXP(mob, plugin.getConfigManager().leatherworkerMcMMOSkillRewardAmount);
					else if (((Villager) mob).getProfession() == Profession.LIBRARIAN)
						return getMcMMOXP(mob, plugin.getConfigManager().librarianMcMMOSkillRewardAmount);
					else if (((Villager) mob).getProfession() == Profession.MASON)
						return getMcMMOXP(mob, plugin.getConfigManager().masonMcMMOSkillRewardAmount);
					else if (((Villager) mob).getProfession() == Profession.NITWIT)
						return getMcMMOXP(mob, plugin.getConfigManager().nitwitMcMMOSkillRewardAmount);
					else if (((Villager) mob).getProfession() == Profession.NONE)
						return getMcMMOXP(mob, plugin.getConfigManager().villagerMcMMOSkillRewardAmount);
					else if (((Villager) mob).getProfession() == Profession.SHEPHERD)
						return getMcMMOXP(mob, plugin.getConfigManager().shepherdMcMMOSkillRewardAmount);
					else if (((Villager) mob).getProfession() == Profession.TOOLSMITH)
						return getMcMMOXP(mob, plugin.getConfigManager().toolsmithMcMMOSkillRewardAmount);
					else if (((Villager) mob).getProfession() == Profession.WEAPONSMITH)
						return getMcMMOXP(mob, plugin.getConfigManager().weaponsmithMcMMOSkillRewardAmount);

			if (Servers.isMC113OrNewer())
				if (mob instanceof Dolphin)
					return getMcMMOXP(mob, plugin.getConfigManager().dolphinMcMMOSkillRewardAmount);
				else if (mob instanceof Drowned)
					return getMcMMOXP(mob, plugin.getConfigManager().drownedMcMMOSkillRewardAmount);
				else if (mob instanceof Cod)
					return getMcMMOXP(mob, plugin.getConfigManager().rawfishMcMMOSkillRewardAmount);
				else if (mob instanceof Salmon)
					return getMcMMOXP(mob, plugin.getConfigManager().rawsalmonMcMMOSkillRewardAmount);
				else if (mob instanceof TropicalFish)
					return getMcMMOXP(mob, plugin.getConfigManager().clownfishMcMMOSkillRewardAmount);
				else if (mob instanceof PufferFish)
					return getMcMMOXP(mob, plugin.getConfigManager().pufferfishMcMMOSkillRewardAmount);
				else if (mob instanceof Phantom)
					return getMcMMOXP(mob, plugin.getConfigManager().phantomMcMMOSkillRewardAmount);
				else if (mob instanceof Turtle)
					return getMcMMOXP(mob, plugin.getConfigManager().turtleMcMMOSkillRewardAmount);

			if (Servers.isMC112OrNewer())
				if (mob instanceof Parrot)
					return getMcMMOXP(mob, plugin.getConfigManager().parrotMcMMOSkillRewardAmount);
				else if (mob instanceof Illusioner)
					return getMcMMOXP(mob, plugin.getConfigManager().illusionerMcMMOSkillRewardAmount);

			if (Servers.isMC111OrNewer())
				if (mob instanceof Llama)
					return getMcMMOXP(mob, plugin.getConfigManager().llamaMcMMOSkillRewardAmount);
				else if (mob instanceof Vex)
					return getMcMMOXP(mob, plugin.getConfigManager().vexMcMMOSkillRewardAmount);
				else if (mob instanceof Vindicator)
					return getMcMMOXP(mob, plugin.getConfigManager().vindicatorMcMMOSkillRewardAmount);
				else if (mob instanceof Evoker)
					return getMcMMOXP(mob, plugin.getConfigManager().evokerMcMMOSkillRewardAmount);
				else if (mob instanceof Donkey)
					return getMcMMOXP(mob, plugin.getConfigManager().donkeyMcMMOSkillRewardAmount);
				else if (mob instanceof Mule)
					return getMcMMOXP(mob, plugin.getConfigManager().muleMcMMOSkillRewardAmount);
				else if (mob instanceof SkeletonHorse)
					return getMcMMOXP(mob, plugin.getConfigManager().skeletonHorseMcMMOSkillRewardAmount);
				else if (mob instanceof ZombieHorse)
					return getMcMMOXP(mob, plugin.getConfigManager().zombieHorseMcMMOSkillRewardAmount);
				else if (mob instanceof Stray)
					return getMcMMOXP(mob, plugin.getConfigManager().strayMcMMOSkillRewardAmount);
				else if (mob instanceof Husk)
					return getMcMMOXP(mob, plugin.getConfigManager().huskMcMMOSkillRewardAmount);
				else if (mob instanceof ZombieVillager)
					return getMcMMOXP(mob, plugin.getConfigManager().zombieVillagerMcMMOSkillRewardAmount);
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.NITWIT)
					return getMcMMOXP(mob, plugin.getConfigManager().nitwitMcMMOSkillRewardAmount);

			if (Servers.isMC110OrNewer())
				if (mob instanceof PolarBear)
					return getMcMMOXP(mob, plugin.getConfigManager().polarBearMcMMOSkillRewardAmount);
				else if (mob instanceof Skeleton && ((Skeleton) mob).getSkeletonType() == SkeletonType.STRAY)
					return getMcMMOXP(mob, plugin.getConfigManager().strayMcMMOSkillRewardAmount);

			// Handle old villagers
			if (Servers.isMC110OrNewer() && !Servers.isMC114OrNewer())
				if (mob instanceof Zombie
						&& ((Zombie) mob).getVillagerProfession() == Villager.Profession.valueOf("HUSK"))
					return getMcMMOXP(mob, plugin.getConfigManager().huskMcMMOSkillRewardAmount);
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.valueOf("NORMAL"))
					return getMcMMOXP(mob, plugin.getConfigManager().villagerMcMMOSkillRewardAmount);
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.valueOf("PRIEST"))
					return getMcMMOXP(mob, plugin.getConfigManager().priestMcMMOSkillRewardAmount);
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.BUTCHER)
					return getMcMMOXP(mob, plugin.getConfigManager().butcherMcMMOSkillRewardAmount);
				else if (mob instanceof Villager
						&& ((Villager) mob).getProfession() == Profession.valueOf("BLACKSMITH"))
					return getMcMMOXP(mob, plugin.getConfigManager().blacksmithMcMMOSkillRewardAmount);
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.LIBRARIAN)
					return getMcMMOXP(mob, plugin.getConfigManager().librarianMcMMOSkillRewardAmount);
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.FARMER)
					return getMcMMOXP(mob, plugin.getConfigManager().farmerMcMMOSkillRewardAmount);

			if (Servers.isMC19OrNewer())
				if (mob instanceof Shulker)
					return getMcMMOXP(mob, plugin.getConfigManager().shulkerMcMMOSkillRewardAmount);

			// MC1.8 or older
			if (mob instanceof Guardian && ((Guardian) mob).isElder())
				return getMcMMOXP(mob, plugin.getConfigManager().elderGuardianMcMMOSkillRewardAmount);
			else if (mob instanceof Guardian)
				return getMcMMOXP(mob, plugin.getConfigManager().guardianMcMMOSkillRewardAmount);
			else if (mob instanceof Endermite)
				return getMcMMOXP(mob, plugin.getConfigManager().endermiteMcMMOSkillRewardAmount);
			else if (mob instanceof Rabbit)
				if ((((Rabbit) mob).getRabbitType()) == Rabbit.Type.THE_KILLER_BUNNY)
					return getMcMMOXP(mob, plugin.getConfigManager().killerRabbitMcMMOSkillRewardAmount);
				else
					return getMcMMOXP(mob, plugin.getConfigManager().rabbitMcMMOSkillRewardAmount);
			else if (mob instanceof Player)
				return getMcMMOXP(mob, plugin.getConfigManager().pvpPlayerMcMMOSkillRewardAmount);
			else if (mob instanceof Blaze)
				return getMcMMOXP(mob, plugin.getConfigManager().blazeMcMMOSkillRewardAmount);
			else if (mob instanceof Creeper)
				return getMcMMOXP(mob, plugin.getConfigManager().creeperMcMMOSkillRewardAmount);
			else if (mob instanceof Silverfish)
				return getMcMMOXP(mob, plugin.getConfigManager().silverfishMcMMOSkillRewardAmount);
			else if (mob instanceof Enderman)
				return getMcMMOXP(mob, plugin.getConfigManager().endermanMcMMOSkillRewardAmount);
			else if (mob instanceof Giant)
				return getMcMMOXP(mob, plugin.getConfigManager().giantMcMMOSkillRewardAmount);
			else if (mob instanceof Skeleton && ((Skeleton) mob).getSkeletonType() == SkeletonType.NORMAL)
				return getMcMMOXP(mob, plugin.getConfigManager().skeletonMcMMOSkillRewardAmount);
			else if (mob instanceof Skeleton && ((Skeleton) mob).getSkeletonType() == SkeletonType.WITHER)
				return getMcMMOXP(mob, plugin.getConfigManager().witherSkeletonMcMMOSkillRewardAmount);
			else if (mob instanceof CaveSpider)
				// CaveSpider is a subclass of Spider
				return getMcMMOXP(mob, plugin.getConfigManager().caveSpiderMcMMOSkillRewardAmount);
			else if (mob instanceof Spider)
				return getMcMMOXP(mob, plugin.getConfigManager().spiderMcMMOSkillRewardAmount);
			else if (mob instanceof Witch)
				return getMcMMOXP(mob, plugin.getConfigManager().witchMcMMOSkillRewardAmount);
			else if (mob instanceof PigZombie)
				// PigZombie is a subclass of Zombie.
				return getMcMMOXP(mob, plugin.getConfigManager().zombiePigManMcMMOSkillRewardAmount);
			else if (mob instanceof Zombie)
				return getMcMMOXP(mob, plugin.getConfigManager().zombieMcMMOSkillRewardAmount);
			else if (mob instanceof Ghast)
				return getMcMMOXP(mob, plugin.getConfigManager().ghastMcMMOSkillRewardAmount);
			else if (mob instanceof MagmaCube)
				// MagmaCube is a subclass of Slime
				return getMcMMOXP(mob, plugin.getConfigManager().magmaCubeMcMMOSkillRewardAmount);
			else if (mob instanceof Slime)
				return getMcMMOXP(mob, plugin.getConfigManager().slimeMcMMOSkillRewardAmount);
			else if (mob instanceof EnderDragon)
				return getMcMMOXP(mob, plugin.getConfigManager().enderdragonMcMMOSkillRewardAmount);
			else if (mob instanceof Wither)
				return getMcMMOXP(mob, plugin.getConfigManager().witherMcMMOSkillRewardAmount);
			else if (mob instanceof IronGolem)
				return getMcMMOXP(mob, plugin.getConfigManager().ironGolemMcMMOSkillRewardAmount);

			// Passive mobs
			else if (mob instanceof Bat)
				return getMcMMOXP(mob, plugin.getConfigManager().batMcMMOSkillRewardAmount);
			else if (mob instanceof Chicken)
				return getMcMMOXP(mob, plugin.getConfigManager().chickenMcMMOSkillRewardAmount);
			else if (mob instanceof Cow)
				if (mob instanceof MushroomCow)
					// MushroomCow is a subclass of Cow
					return getMcMMOXP(mob, plugin.getConfigManager().mushroomCowMcMMOSkillRewardAmount);
				else
					return getMcMMOXP(mob, plugin.getConfigManager().cowMcMMOSkillRewardAmount);
			else if (mob instanceof Horse)
				return getMcMMOXP(mob, plugin.getConfigManager().horseMcMMOSkillRewardAmount);
			else if (mob instanceof Ocelot)
				return getMcMMOXP(mob, plugin.getConfigManager().ocelotMcMMOSkillRewardAmount);
			else if (mob instanceof Pig)
				return getMcMMOXP(mob, plugin.getConfigManager().pigMcMMOSkillRewardAmount);
			else if (mob instanceof Sheep)
				return getMcMMOXP(mob, plugin.getConfigManager().sheepMcMMOSkillRewardAmount);
			else if (mob instanceof Snowman)
				return getMcMMOXP(mob, plugin.getConfigManager().snowmanMcMMOSkillRewardAmount);
			else if (mob instanceof Squid)
				return getMcMMOXP(mob, plugin.getConfigManager().squidMcMMOSkillRewardAmount);
			else if (mob instanceof Villager)
				return getMcMMOXP(mob, plugin.getConfigManager().villagerMcMMOSkillRewardAmount);
			else if (mob instanceof Wolf)
				return getMcMMOXP(mob, plugin.getConfigManager().wolfMcMMOSkillRewardAmount);
			else if (mob instanceof Item && ((Item) mob).getItemStack().getType() == Material.COD)
				return getMcMMOXP(mob, plugin.getConfigManager().rawfishMcMMOSkillRewardAmount);
			else if (mob instanceof Item && ((Item) mob).getItemStack().getType() == Material.SALMON)
				return getMcMMOXP(mob, plugin.getConfigManager().rawsalmonMcMMOSkillRewardAmount);
			else if (mob instanceof Item && ((Item) mob).getItemStack().getType() == Material.TROPICAL_FISH)
				return getMcMMOXP(mob, plugin.getConfigManager().clownfishMcMMOSkillRewardAmount);
			else if (mob instanceof Item && ((Item) mob).getItemStack().getType() == Material.PUFFERFISH)
				return getMcMMOXP(mob, plugin.getConfigManager().pufferfishMcMMOSkillRewardAmount);
		}
		return 0;
	}

	public boolean getMobEnabled(Entity mob) {
		if (TARDISWeepingAngelsCompat.isWeepingAngelMonster(mob)) {
			if (TARDISWeepingAngelsCompat.getMobRewardData()
					.containsKey(TARDISWeepingAngelsCompat.getWeepingAngelMonsterType(mob).name()))
				return TARDISWeepingAngelsCompat.getMobRewardData()
						.get(TARDISWeepingAngelsCompat.getWeepingAngelMonsterType(mob).name()).isMobEnabled();
			return false;

		} else if (MythicMobsCompat.isMythicMob(mob)) {
			if (MythicMobsCompat.getMobRewardData().containsKey(MythicMobsCompat.getMythicMobType(mob)))
				return MythicMobsCompat.getMobRewardData().get(MythicMobsCompat.getMythicMobType(mob)).isMobEnabled();
			return false;

		} else if (CitizensCompat.isNPC(mob) && CitizensCompat.isSentryOrSentinelOrSentries(mob)) {
			NPCRegistry registry = CitizensAPI.getNPCRegistry();
			NPC npc = registry.getNPC(mob);
			String key = String.valueOf(npc.getId());
			if (CitizensCompat.getMobRewardData().containsKey(key)) {
				return CitizensCompat.getMobRewardData().get(key).isMobEnabled();
			}
			return false;

		} else if (CustomMobsCompat.isCustomMob(mob)) {
			if (CustomMobsCompat.getMobRewardData().containsKey(CustomMobsCompat.getCustomMobType(mob)))
				return CustomMobsCompat.getMobRewardData().get(CustomMobsCompat.getCustomMobType(mob)).isMobEnabled();
			return false;

		} else if (MysteriousHalloweenCompat.isMysteriousHalloween(mob)) {
			if (MysteriousHalloweenCompat.getMobRewardData()
					.containsKey(MysteriousHalloweenCompat.getMysteriousHalloweenType(mob).name()))
				return MysteriousHalloweenCompat.getMobRewardData()
						.get(MysteriousHalloweenCompat.getMysteriousHalloweenType(mob).name()).isMobEnabled();
			return false;

		} else if (SmartGiantsCompat.isSmartGiants(mob)) {
			if (SmartGiantsCompat.getMobRewardData().containsKey(SmartGiantsCompat.getSmartGiantsMobType(mob)))
				return SmartGiantsCompat.getMobRewardData().get(SmartGiantsCompat.getSmartGiantsMobType(mob))
						.isMobEnabled();
			return false;

		} else if (HerobrineCompat.isHerobrineMob(mob)) {
			if (HerobrineCompat.getMobRewardData().containsKey(HerobrineCompat.getHerobrineMobType(mob)))
				return HerobrineCompat.getMobRewardData().get(HerobrineCompat.getHerobrineMobType(mob)).isMobEnabled();
			return false;

		} else if (EliteMobsCompat.isEliteMobs(mob)) {
			if (EliteMobsCompat.getMobRewardData().containsKey(EliteMobsCompat.getEliteMobsType(mob).getName()))
				return EliteMobsCompat.getMobRewardData().get(EliteMobsCompat.getEliteMobsType(mob).getName())
						.isMobEnabled();
			return false;

		} else if (BossCompat.isBossMob(mob)) {
			if (BossCompat.getMobRewardData().containsKey(BossCompat.getBossType(mob)))
				return BossCompat.getMobRewardData().get(BossCompat.getBossType(mob)).isMobEnabled();
			return false;

		} else if (MyPetCompat.isMyPet(mob)) {
			return plugin.getConfigManager().wolfEnabled;

		} else {
			if (isEntityType(mob, "ARMADILLO"))
				return plugin.getConfigManager().turtleEnabled;
			else if (isEntityType(mob, "BOGGED"))
				return plugin.getConfigManager().strayEnabled;
			else if (isEntityType(mob, "BREEZE"))
				return plugin.getConfigManager().vexEnabled;
			else if (isEntityType(mob, "CREAKING"))
				return plugin.getConfigManager().wardenEnabled;

			if (Servers.isMC119OrNewer())
				if (mob instanceof Allay)
					return plugin.getConfigManager().allayEnabled;
				else if (mob instanceof Frog)
					return plugin.getConfigManager().frogEnabled;
				else if (mob instanceof Tadpole)
					return plugin.getConfigManager().tadpoleEnabled;
				else if (mob instanceof Warden)
					return plugin.getConfigManager().wardenEnabled;

			if (Servers.isMC117OrNewer())
				if (mob instanceof Axolotl)
					return plugin.getConfigManager().axolotlEnabled;
				else if (mob instanceof Goat)
					return plugin.getConfigManager().goatEnabled;
				else if (mob instanceof GlowSquid)
					return plugin.getConfigManager().glowsquidEnabled;

			if (Servers.isMC1162OrNewer())
				if (mob instanceof PiglinBrute)
					return plugin.getConfigManager().piglinBruteEnabled;

			if (Servers.isMC116OrNewer())
				if (mob instanceof Hoglin)
					return plugin.getConfigManager().hoglinEnabled;
				else if (mob instanceof Piglin)
					return plugin.getConfigManager().piglinEnabled;
				else if (mob instanceof Strider)
					return plugin.getConfigManager().striderEnabled;
				else if (mob instanceof Zoglin)
					return plugin.getConfigManager().zoglinEnabled;

			if (Servers.isMC115OrNewer())
				if (mob instanceof Bee)
					return plugin.getConfigManager().beeEnabled;

			if (Servers.isMC114OrNewer())
				if (mob instanceof Cat)
					return plugin.getConfigManager().catEnabled;
				else if (mob instanceof Fox)
					return plugin.getConfigManager().foxEnabled;
				else if (mob instanceof Panda)
					return plugin.getConfigManager().pandaEnabled;
				else if (mob instanceof Pillager)
					return plugin.getConfigManager().pillagerEnabled;
				else if (mob instanceof Ravager)
					return plugin.getConfigManager().ravagerEnabled;
				else if (mob instanceof Husk)
					return plugin.getConfigManager().huskEnabled;
				else if (mob instanceof Stray)
					return plugin.getConfigManager().strayEnabled;
				else if (mob instanceof TraderLlama)
					return plugin.getConfigManager().traderLlamaEnabled;
				else if (mob instanceof WanderingTrader)
					return plugin.getConfigManager().wanderingTraderEnabled;
				else if (mob instanceof Villager)
					if (((Villager) mob).getProfession() == Profession.ARMORER)
						return plugin.getConfigManager().armorerEnabled;
					else if (((Villager) mob).getProfession() == Profession.BUTCHER)
						return plugin.getConfigManager().butcherEnabled;
					else if (((Villager) mob).getProfession() == Profession.CARTOGRAPHER)
						return plugin.getConfigManager().cartographerEnabled;
					else if (((Villager) mob).getProfession() == Profession.CLERIC)
						return plugin.getConfigManager().clericEnabled;
					else if (((Villager) mob).getProfession() == Profession.FARMER)
						return plugin.getConfigManager().farmerEnabled;
					else if (((Villager) mob).getProfession() == Profession.FISHERMAN)
						return plugin.getConfigManager().fishermanEnabled;
					else if (((Villager) mob).getProfession() == Profession.FLETCHER)
						return plugin.getConfigManager().fletcherEnabled;
					else if (((Villager) mob).getProfession() == Profession.LEATHERWORKER)
						return plugin.getConfigManager().leatherworkerEnabled;
					else if (((Villager) mob).getProfession() == Profession.LIBRARIAN)
						return plugin.getConfigManager().librarianEnabled;
					else if (((Villager) mob).getProfession() == Profession.MASON)
						return plugin.getConfigManager().masonEnabled;
					else if (((Villager) mob).getProfession() == Profession.NITWIT)
						return plugin.getConfigManager().nitwitEnabled;
					else if (((Villager) mob).getProfession() == Profession.NONE)
						return plugin.getConfigManager().villagerEnabled;
					else if (((Villager) mob).getProfession() == Profession.SHEPHERD)
						return plugin.getConfigManager().shepherdEnabled;
					else if (((Villager) mob).getProfession() == Profession.TOOLSMITH)
						return plugin.getConfigManager().toolsmithEnabled;
					else if (((Villager) mob).getProfession() == Profession.WEAPONSMITH)
						return plugin.getConfigManager().weaponsmithEnabled;

			if (Servers.isMC113OrNewer())
				if (mob instanceof Dolphin)
					return plugin.getConfigManager().dolphinEnabled;
				else if (mob instanceof Drowned)
					return plugin.getConfigManager().drownedEnabled;
				else if (mob instanceof Cod)
					return plugin.getConfigManager().codEnabled;
				else if (mob instanceof Salmon)
					return plugin.getConfigManager().salmonEnabled;
				else if (mob instanceof TropicalFish)
					return plugin.getConfigManager().tropicalFishEnabled;
				else if (mob instanceof PufferFish)
					return plugin.getConfigManager().pufferfishEnabled;
				else if (mob instanceof Phantom)
					return plugin.getConfigManager().phantomEnabled;
				else if (mob instanceof Turtle)
					return plugin.getConfigManager().turtleEnabled;

			if (Servers.isMC112OrNewer())
				if (mob instanceof Parrot)
					return plugin.getConfigManager().parrotEnabled;
				else if (mob instanceof Illusioner)
					return plugin.getConfigManager().illusionerEnabled;

			if (Servers.isMC111OrNewer())
				if (mob instanceof Llama)
					return plugin.getConfigManager().llamaEnabled;
				else if (mob instanceof Vex)
					return plugin.getConfigManager().vexEnabled;
				else if (mob instanceof Vindicator)
					return plugin.getConfigManager().vindicatorEnabled;
				else if (mob instanceof Evoker)
					return plugin.getConfigManager().evokerEnabled;
				else if (mob instanceof Donkey)
					return plugin.getConfigManager().donkeyEnabled;
				else if (mob instanceof Mule)
					return plugin.getConfigManager().muleEnabled;
				else if (mob instanceof SkeletonHorse)
					return plugin.getConfigManager().skeletonHorseEnabled;
				else if (mob instanceof ZombieHorse)
					return plugin.getConfigManager().zombieHorseEnabled;
				else if (mob instanceof Stray)
					return plugin.getConfigManager().strayEnabled;
				else if (mob instanceof Husk)
					return plugin.getConfigManager().huskEnabled;
				else if (mob instanceof ZombieVillager)
					return plugin.getConfigManager().zombieVillagerEnabled;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.NITWIT)
					return plugin.getConfigManager().nitwitEnabled;

			if (Servers.isMC110OrNewer())
				if (mob instanceof PolarBear)
					return plugin.getConfigManager().polarBearEnabled;
				else if (mob instanceof Skeleton && ((Skeleton) mob).getSkeletonType() == SkeletonType.STRAY)
					return plugin.getConfigManager().strayEnabled;

			// Handle old villagers
			if (Servers.isMC110OrNewer() && !Servers.isMC114OrNewer())
				if (mob instanceof Zombie && ((Zombie) mob).getVillagerProfession() == Profession.valueOf("HUSK"))
					return plugin.getConfigManager().huskEnabled;

				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.valueOf("NORMAL"))
					return plugin.getConfigManager().villagerEnabled;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.valueOf("PRIEST"))
					return plugin.getConfigManager().priestEnabled;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.BUTCHER)
					return plugin.getConfigManager().butcherEnabled;
				else if (mob instanceof Villager
						&& ((Villager) mob).getProfession() == Profession.valueOf("BLACKSMITH"))
					return plugin.getConfigManager().blacksmithEnabled;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.LIBRARIAN)
					return plugin.getConfigManager().librarianEnabled;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.FARMER)
					return plugin.getConfigManager().farmerEnabled;

			if (Servers.isMC19OrNewer())
				if (mob instanceof Shulker)
					return plugin.getConfigManager().shulkerEnabled;

			// MC1.8 or older
			if (mob instanceof Guardian && ((Guardian) mob).isElder())
				return plugin.getConfigManager().elderGuardianEnabled;
			else if (mob instanceof Guardian)
				return plugin.getConfigManager().guardianEnabled;
			else if (mob instanceof Endermite)
				return plugin.getConfigManager().endermiteEnabled;
			else if (mob instanceof Rabbit)
				if ((((Rabbit) mob).getRabbitType()) == Rabbit.Type.THE_KILLER_BUNNY)
					return plugin.getConfigManager().killerRabbitEnabled;
				else
					return plugin.getConfigManager().rabbitEnabled;
			else if (mob instanceof Player)
				return plugin.getConfigManager().pvpAllowed;
			else if (mob instanceof Blaze)
				return plugin.getConfigManager().blazeEnabled;
			else if (mob instanceof Creeper)
				return plugin.getConfigManager().creeperEnabled;
			else if (mob instanceof Silverfish)
				return plugin.getConfigManager().silverfishEnabled;
			else if (mob instanceof Enderman)
				return plugin.getConfigManager().endermanEnabled;
			else if (mob instanceof Giant)
				return plugin.getConfigManager().giantEnabled;
			else if (mob instanceof Skeleton && ((Skeleton) mob).getSkeletonType() == SkeletonType.NORMAL)
				return plugin.getConfigManager().skeletonEnabled;
			else if (mob instanceof Skeleton && ((Skeleton) mob).getSkeletonType() == SkeletonType.WITHER)
				return plugin.getConfigManager().witherSkeletonEnabled;
			else if (mob instanceof CaveSpider)
				// CaveSpider is a subclass of Spider
				return plugin.getConfigManager().caveSpiderEnabled;
			else if (mob instanceof Spider)
				return plugin.getConfigManager().spiderEnabled;
			else if (mob instanceof Witch)
				return plugin.getConfigManager().witchEnabled;
			else if (mob instanceof PigZombie)
				// PigZombie is a subclass of Zombie.
				return plugin.getConfigManager().zombiePigmanEnabled;
			else if (mob instanceof Zombie)
				return plugin.getConfigManager().zombieEnabled;
			else if (mob instanceof Ghast)
				return plugin.getConfigManager().ghastEnabled;
			else if (mob instanceof MagmaCube)
				// MagmaCube is a subclass of Slime
				return plugin.getConfigManager().magmaCubeEnabled;
			else if (mob instanceof Slime)
				return plugin.getConfigManager().slimeEnabled;
			else if (mob instanceof EnderDragon)
				return plugin.getConfigManager().enderDragonEnabled;
			else if (mob instanceof Wither)
				return plugin.getConfigManager().witherEnabled;
			else if (mob instanceof IronGolem)
				return plugin.getConfigManager().ironGolemEnabled;

			// Passive mobs
			else if (mob instanceof Bat)
				return plugin.getConfigManager().batEnabled;
			else if (mob instanceof Chicken)
				return plugin.getConfigManager().chickenEnabled;
			else if (mob instanceof Cow)
				if (mob instanceof MushroomCow)
					// MushroomCow is a subclass of Cow
					return plugin.getConfigManager().mushroomCowEnabled;
				else
					return plugin.getConfigManager().cowEnabled;
			else if (mob instanceof Horse)
				return plugin.getConfigManager().horseEnabled;
			else if (mob instanceof Ocelot)
				return plugin.getConfigManager().ocelotEnabled;
			else if (mob instanceof Pig)
				return plugin.getConfigManager().pigEnabled;
			else if (mob instanceof Sheep)
				return plugin.getConfigManager().sheepEnabled;
			else if (mob instanceof Snowman)
				return plugin.getConfigManager().snowmanEnabled;
			else if (mob instanceof Squid)
				return plugin.getConfigManager().squidEnabled;
			else if (mob instanceof Villager)
				return plugin.getConfigManager().villagerEnabled;
			else if (mob instanceof Wolf)
				return plugin.getConfigManager().wolfEnabled;
			else if (mob instanceof Item && ((Item) mob).getItemStack().getType() == Material.COD)
				return plugin.getConfigManager().codEnabled;
			else if (mob instanceof Item && ((Item) mob).getItemStack().getType() == Material.SALMON)
				return plugin.getConfigManager().salmonEnabled;
			else if (mob instanceof Item && ((Item) mob).getItemStack().getType() == Material.TROPICAL_FISH)
				return plugin.getConfigManager().tropicalFishEnabled;
			else if (mob instanceof Item && ((Item) mob).getItemStack().getType() == Material.PUFFERFISH)
				return plugin.getConfigManager().pufferfishEnabled;
		}
		return false;
	}

	public boolean getHeadDropHead(Entity mob) {
		if (TARDISWeepingAngelsCompat.isWeepingAngelMonster(mob)) {
			if (TARDISWeepingAngelsCompat.getMobRewardData()
					.containsKey(TARDISWeepingAngelsCompat.getWeepingAngelMonsterType(mob).name()))
				return false;
			// return TARDISWeepingAngelsCompat.getMobRewardData()
			// .get(TARDISWeepingAngelsCompat.getWeepingAngelMonsterType(killed).name()).getMobEnabled();
			return false;

		} else if (MythicMobsCompat.isMythicMob(mob)) {
			if (MythicMobsCompat.getMobRewardData().containsKey(MythicMobsCompat.getMythicMobType(mob)))
				return false;
			// return
			// MythicMobsCompat.getMobRewardData().get(MythicMobsCompat.getMythicMobType(killed)).isMobEnabled();
			return false;

		} else if (CitizensCompat.isNPC(mob) && CitizensCompat.isSentryOrSentinelOrSentries(mob)) {
			NPCRegistry registry = CitizensAPI.getNPCRegistry();
			NPC npc = registry.getNPC(mob);
			String key = String.valueOf(npc.getId());
			if (CitizensCompat.getMobRewardData().containsKey(key)) {
				return false;
				// return
				// CitizensCompat.getMobRewardData().get(key).isMobEnabled();
			}
			return false;

		} else if (CustomMobsCompat.isCustomMob(mob)) {
			if (CustomMobsCompat.getMobRewardData().containsKey(CustomMobsCompat.getCustomMobType(mob)))
				return false;
			// return
			// CustomMobsCompat.getMobRewardData().get(CustomMobsCompat.getCustomMobType(killed)).isMobEnabled();
			return false;

		} else if (MysteriousHalloweenCompat.isMysteriousHalloween(mob)) {
			if (MysteriousHalloweenCompat.getMobRewardData()
					.containsKey(MysteriousHalloweenCompat.getMysteriousHalloweenType(mob).name()))
				return false;
			// return MysteriousHalloweenCompat.getMobRewardData()
			// .get(MysteriousHalloweenCompat.getMysteriousHalloweenType(killed).name()).isMobEnabled();
			return false;

		} else if (SmartGiantsCompat.isSmartGiants(mob)) {
			if (SmartGiantsCompat.getMobRewardData().containsKey(SmartGiantsCompat.getSmartGiantsMobType(mob)))
				return false;
			// return
			// SmartGiantsCompat.getMobRewardData().get(SmartGiantsCompat.getSmartGiantsMobType(killed))
			// .isMobEnabled();
			return false;

		} else if (HerobrineCompat.isHerobrineMob(mob)) {
			if (HerobrineCompat.getMobRewardData().containsKey(HerobrineCompat.getHerobrineMobType(mob)))
				return false;
			// return
			// HerobrineCompat.getMobRewardData().get(HerobrineCompat.getHerobrineMobType(killed)).isMobEnabled();
			return false;

		} else if (EliteMobsCompat.isEliteMobs(mob)) {
			if (EliteMobsCompat.getMobRewardData().containsKey(EliteMobsCompat.getEliteMobsType(mob).getName()))
				return false;
			return false;

		} else if (BossCompat.isBossMob(mob)) {
			if (BossCompat.getMobRewardData().containsKey(BossCompat.getBossType(mob)))
				return false;
			return false;

		} else if (MyPetCompat.isMyPet(mob)) {
			return plugin.getConfigManager().wolfHeadDropHead;

		} else {
			if (isEntityType(mob, "ARMADILLO"))
				return plugin.getConfigManager().turtleHeadDropHead;
			else if (isEntityType(mob, "BOGGED"))
				return plugin.getConfigManager().strayHeadDropHead;
			else if (isEntityType(mob, "BREEZE"))
				return plugin.getConfigManager().vexHeadDropHead;
			else if (isEntityType(mob, "CREAKING"))
				return plugin.getConfigManager().wardenHeadDropHead;

			if (Servers.isMC119OrNewer())
				if (mob instanceof Allay)
					return plugin.getConfigManager().allayHeadDropHead;
				else if (mob instanceof Frog)
					return plugin.getConfigManager().frogHeadDropHead;
				else if (mob instanceof Tadpole)
					return plugin.getConfigManager().tadpoleHeadDropHead;
				else if (mob instanceof Warden)
					return plugin.getConfigManager().wardenHeadDropHead;

			if (Servers.isMC117OrNewer())
				if (mob instanceof Axolotl)
					return plugin.getConfigManager().axolotlHeadDropHead;
				else if (mob instanceof Goat)
					return plugin.getConfigManager().goatHeadDropHead;
				else if (mob instanceof GlowSquid)
					return plugin.getConfigManager().glowsquidHeadDropHead;

			if (Servers.isMC1162OrNewer())
				if (mob instanceof PiglinBrute)
					return plugin.getConfigManager().piglinBruteHeadDropHead;

			if (Servers.isMC116OrNewer())
				if (mob instanceof Hoglin)
					return plugin.getConfigManager().hoglinHeadDropHead;
				else if (mob instanceof Piglin)
					return plugin.getConfigManager().piglinHeadDropHead;
				else if (mob instanceof Strider)
					return plugin.getConfigManager().striderHeadDropHead;
				else if (mob instanceof Zoglin)
					return plugin.getConfigManager().zoglinHeadDropHead;

			if (Servers.isMC115OrNewer())
				if (mob instanceof Bee)
					return plugin.getConfigManager().beeHeadDropHead;

			if (Servers.isMC114OrNewer())
				if (mob instanceof Cat)
					return plugin.getConfigManager().catHeadDropHead;
				else if (mob instanceof Fox)
					return plugin.getConfigManager().foxHeadDropHead;
				else if (mob instanceof Panda)
					return plugin.getConfigManager().pandaHeadDropHead;
				else if (mob instanceof Pillager)
					return plugin.getConfigManager().pillagerHeadDropHead;
				else if (mob instanceof Ravager)
					return plugin.getConfigManager().ravagerHeadDropHead;
				else if (mob instanceof Husk)
					return plugin.getConfigManager().huskHeadDropHead;
				else if (mob instanceof Stray)
					return plugin.getConfigManager().strayHeadDropHead;
				else if (mob instanceof TraderLlama)
					return plugin.getConfigManager().traderLlamaHeadDropHead;
				else if (mob instanceof WanderingTrader)
					return plugin.getConfigManager().wanderingTraderHeadDropHead;
				else if (mob instanceof Villager)
					if (((Villager) mob).getProfession() == Profession.ARMORER)
						return plugin.getConfigManager().armorerHeadDropHead;
					else if (((Villager) mob).getProfession() == Profession.BUTCHER)
						return plugin.getConfigManager().butcherHeadDropHead;
					else if (((Villager) mob).getProfession() == Profession.CARTOGRAPHER)
						return plugin.getConfigManager().cartographerHeadDropHead;
					else if (((Villager) mob).getProfession() == Profession.CLERIC)
						return plugin.getConfigManager().clericHeadDropHead;
					else if (((Villager) mob).getProfession() == Profession.FARMER)
						return plugin.getConfigManager().farmerHeadDropHead;
					else if (((Villager) mob).getProfession() == Profession.FISHERMAN)
						return plugin.getConfigManager().fishermanHeadDropHead;
					else if (((Villager) mob).getProfession() == Profession.FLETCHER)
						return plugin.getConfigManager().fletcherHeadDropHead;
					else if (((Villager) mob).getProfession() == Profession.LEATHERWORKER)
						return plugin.getConfigManager().leatherworkerHeadDropHead;
					else if (((Villager) mob).getProfession() == Profession.LIBRARIAN)
						return plugin.getConfigManager().librarianHeadDropHead;
					else if (((Villager) mob).getProfession() == Profession.MASON)
						return plugin.getConfigManager().masonHeadDropHead;
					else if (((Villager) mob).getProfession() == Profession.NITWIT)
						return plugin.getConfigManager().nitwitHeadDropHead;
					else if (((Villager) mob).getProfession() == Profession.NONE)
						return plugin.getConfigManager().villagerHeadDropHead;
					else if (((Villager) mob).getProfession() == Profession.SHEPHERD)
						return plugin.getConfigManager().shepherdHeadDropHead;
					else if (((Villager) mob).getProfession() == Profession.TOOLSMITH)
						return plugin.getConfigManager().toolsmithHeadDropHead;
					else if (((Villager) mob).getProfession() == Profession.WEAPONSMITH)
						return plugin.getConfigManager().weaponsmithHeadDropHead;

			if (Servers.isMC113OrNewer())
				if (mob instanceof Dolphin)
					return plugin.getConfigManager().dolphinHeadDropHead;
				else if (mob instanceof Drowned)
					return plugin.getConfigManager().drownedHeadDropHead;
				else if (mob instanceof Cod)
					return plugin.getConfigManager().codHeadDropHead;
				else if (mob instanceof Salmon)
					return plugin.getConfigManager().salmonHeadDropHead;
				else if (mob instanceof TropicalFish)
					return plugin.getConfigManager().tropicalFishHeadDropHead;
				else if (mob instanceof PufferFish)
					return plugin.getConfigManager().pufferfishHeadDropHead;
				else if (mob instanceof Phantom)
					return plugin.getConfigManager().phantomHeadDropHead;
				else if (mob instanceof Turtle)
					return plugin.getConfigManager().turtleHeadDropHead;

			if (Servers.isMC112OrNewer())
				if (mob instanceof Parrot)
					return plugin.getConfigManager().parrotHeadDropHead;
				else if (mob instanceof Illusioner)
					return plugin.getConfigManager().illusionerHeadDropHead;

			if (Servers.isMC111OrNewer())
				if (mob instanceof Llama)
					return plugin.getConfigManager().llamaHeadDropHead;
				else if (mob instanceof Vex)
					return plugin.getConfigManager().vexHeadDropHead;
				else if (mob instanceof Vindicator)
					return plugin.getConfigManager().vindicatorHeadDropHead;
				else if (mob instanceof Evoker)
					return plugin.getConfigManager().evokerHeadDropHead;
				else if (mob instanceof Donkey)
					return plugin.getConfigManager().donkeyHeadDropHead;
				else if (mob instanceof Mule)
					return plugin.getConfigManager().muleHeadDropHead;
				else if (mob instanceof SkeletonHorse)
					return plugin.getConfigManager().skeletonHorseHeadDropHead;
				else if (mob instanceof ZombieHorse)
					return plugin.getConfigManager().zombieHorseHeadDropHead;
				else if (mob instanceof Stray)
					return plugin.getConfigManager().strayHeadDropHead;
				else if (mob instanceof Husk)
					return plugin.getConfigManager().huskHeadDropHead;
				else if (mob instanceof ZombieVillager)
					return plugin.getConfigManager().zombieVillagerHeadDropHead;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.NITWIT)
					return plugin.getConfigManager().nitwitHeadDropHead;

			if (Servers.isMC110OrNewer())
				if (mob instanceof PolarBear)
					return plugin.getConfigManager().polarBearHeadDropHead;
				else if (mob instanceof Skeleton && ((Skeleton) mob).getSkeletonType() == SkeletonType.STRAY)
					return plugin.getConfigManager().strayHeadDropHead;

			// Handle old villagers
			if (Servers.isMC110OrNewer() && !Servers.isMC114OrNewer())
				if (mob instanceof Zombie
						&& ((Zombie) mob).getVillagerProfession() == Villager.Profession.valueOf("HUSK"))
					return plugin.getConfigManager().huskHeadDropHead;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.valueOf("NORMAL"))
					return plugin.getConfigManager().villagerHeadDropHead;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.valueOf("PRIEST"))
					return plugin.getConfigManager().priestHeadDropHead;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.BUTCHER)
					return plugin.getConfigManager().butcherHeadDropHead;
				else if (mob instanceof Villager
						&& ((Villager) mob).getProfession() == Profession.valueOf("BLACKSMITH"))
					return plugin.getConfigManager().blacksmithHeadDropHead;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.LIBRARIAN)
					return plugin.getConfigManager().librarianHeadDropHead;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.FARMER)
					return plugin.getConfigManager().farmerHeadDropHead;

			if (Servers.isMC19OrNewer())
				if (mob instanceof Shulker)
					return plugin.getConfigManager().shulkerHeadDropHead;

			// MC1.8 or older
			if (mob instanceof Guardian && ((Guardian) mob).isElder())
				return plugin.getConfigManager().elderGuardianHeadDropHead;
			else if (mob instanceof Guardian)
				return plugin.getConfigManager().guardianHeadDropHead;
			else if (mob instanceof Endermite)
				return plugin.getConfigManager().endermiteHeadDropHead;
			else if (mob instanceof Rabbit)
				if ((((Rabbit) mob).getRabbitType()) == Rabbit.Type.THE_KILLER_BUNNY)
					return plugin.getConfigManager().killerRabbitHeadDropHead;
				else
					return plugin.getConfigManager().rabbitHeadDropHead;
			else if (mob instanceof Player) {
				return plugin.getConfigManager().pvpHeadDropHead;
			} else if (mob instanceof Blaze)
				return plugin.getConfigManager().blazeHeadDropHead;
			else if (mob instanceof Creeper)
				return plugin.getConfigManager().creeperHeadDropHead;
			else if (mob instanceof Silverfish)
				return plugin.getConfigManager().silverfishHeadDropHead;
			else if (mob instanceof Enderman)
				return plugin.getConfigManager().endermanHeadDropHead;
			else if (mob instanceof Giant)
				return plugin.getConfigManager().giantHeadDropHead;
			else if (mob instanceof Skeleton && ((Skeleton) mob).getSkeletonType() == SkeletonType.NORMAL)
				return plugin.getConfigManager().skeletonHeadDropHead;
			else if (mob instanceof Skeleton && ((Skeleton) mob).getSkeletonType() == SkeletonType.WITHER)
				return plugin.getConfigManager().witherSkeletonHeadDropHead;
			else if (mob instanceof CaveSpider)
				// CaveSpider is a subclass of Spider
				return plugin.getConfigManager().caveSpiderHeadDropHead;
			else if (mob instanceof Spider)
				return plugin.getConfigManager().spiderHeadDropHead;
			else if (mob instanceof Witch)
				return plugin.getConfigManager().witchHeadDropHead;
			else if (mob instanceof PigZombie)
				// PigZombie is a subclass of Zombie.
				return plugin.getConfigManager().zombiePigmanHeadDropHead;
			else if (mob instanceof Zombie)
				return plugin.getConfigManager().zombieHeadDropHead;
			else if (mob instanceof Ghast)
				return plugin.getConfigManager().ghastHeadDropHead;
			else if (mob instanceof MagmaCube)
				// MagmaCube is a subclass of Slime
				return plugin.getConfigManager().magmaCubeHeadDropHead;
			else if (mob instanceof Slime)
				return plugin.getConfigManager().slimeHeadDropHead;
			else if (mob instanceof EnderDragon)
				return plugin.getConfigManager().enderDragonHeadDropHead;
			else if (mob instanceof Wither)
				return plugin.getConfigManager().witherHeadDropHead;
			else if (mob instanceof IronGolem)
				return plugin.getConfigManager().ironGolemHeadDropHead;

			// Passive mobs
			else if (mob instanceof Bat)
				return plugin.getConfigManager().batHeadDropHead;
			else if (mob instanceof Chicken)
				return plugin.getConfigManager().chickenHeadDropHead;
			else if (mob instanceof Cow)
				if (mob instanceof MushroomCow)
					// MushroomCow is a subclass of Cow
					return plugin.getConfigManager().mushroomCowHeadDropHead;
				else
					return plugin.getConfigManager().cowHeadDropHead;
			else if (mob instanceof Horse)
				return plugin.getConfigManager().horseHeadDropHead;
			else if (mob instanceof Ocelot)
				return plugin.getConfigManager().ocelotHeadDropHead;
			else if (mob instanceof Pig)
				return plugin.getConfigManager().pigHeadDropHead;
			else if (mob instanceof Sheep)
				return plugin.getConfigManager().sheepHeadDropHead;
			else if (mob instanceof Snowman)
				return plugin.getConfigManager().snowmanHeadDropHead;
			else if (mob instanceof Squid)
				return plugin.getConfigManager().squidHeadDropHead;
			else if (mob instanceof Villager)
				return plugin.getConfigManager().villagerHeadDropHead;
			else if (mob instanceof Wolf)
				return plugin.getConfigManager().wolfHeadDropHead;
			else if (mob instanceof Item && ((Item) mob).getItemStack().getType() == Material.COD)
				return plugin.getConfigManager().codHeadDropHead;
			else if (mob instanceof Item && ((Item) mob).getItemStack().getType() == Material.SALMON)
				return plugin.getConfigManager().salmonHeadDropHead;
			else if (mob instanceof Item && ((Item) mob).getItemStack().getType() == Material.TROPICAL_FISH)
				return plugin.getConfigManager().tropicalFishHeadDropHead;
			else if (mob instanceof Item && ((Item) mob).getItemStack().getType() == Material.PUFFERFISH)
				return plugin.getConfigManager().pufferfishHeadDropHead;
		}
		return false;
	}

	public double getHeadDropChance(Entity mob) {
		if (TARDISWeepingAngelsCompat.isWeepingAngelMonster(mob)) {
			if (TARDISWeepingAngelsCompat.getMobRewardData()
					.containsKey(TARDISWeepingAngelsCompat.getWeepingAngelMonsterType(mob).name()))
				return 0;
			// return TARDISWeepingAngelsCompat.getMobRewardData()
			// .get(TARDISWeepingAngelsCompat.getWeepingAngelMonsterType(killed).name()).getMobEnabled();
			return 0;

		} else if (MythicMobsCompat.isMythicMob(mob)) {
			if (MythicMobsCompat.getMobRewardData().containsKey(MythicMobsCompat.getMythicMobType(mob)))
				return 0;
			// return
			// MythicMobsCompat.getMobRewardData().get(MythicMobsCompat.getMythicMobType(killed)).isMobEnabled();
			return 0;

		} else if (CitizensCompat.isNPC(mob) && CitizensCompat.isSentryOrSentinelOrSentries(mob)) {
			NPCRegistry registry = CitizensAPI.getNPCRegistry();
			NPC npc = registry.getNPC(mob);
			String key = String.valueOf(npc.getId());
			if (CitizensCompat.getMobRewardData().containsKey(key)) {
				return 0;
				// return
				// CitizensCompat.getMobRewardData().get(key).isMobEnabled();
			}
			return 0;

		} else if (CustomMobsCompat.isCustomMob(mob)) {
			if (CustomMobsCompat.getMobRewardData().containsKey(CustomMobsCompat.getCustomMobType(mob)))
				return 0;
			// return
			// CustomMobsCompat.getMobRewardData().get(CustomMobsCompat.getCustomMobType(killed)).isMobEnabled();
			return 0;

		} else if (MysteriousHalloweenCompat.isMysteriousHalloween(mob)) {
			if (MysteriousHalloweenCompat.getMobRewardData()
					.containsKey(MysteriousHalloweenCompat.getMysteriousHalloweenType(mob).name()))
				return 0;
			// return MysteriousHalloweenCompat.getMobRewardData()
			// .get(MysteriousHalloweenCompat.getMysteriousHalloweenType(killed).name()).isMobEnabled();
			return 0;

		} else if (SmartGiantsCompat.isSmartGiants(mob)) {
			if (SmartGiantsCompat.getMobRewardData().containsKey(SmartGiantsCompat.getSmartGiantsMobType(mob)))
				return 0;
			// return
			// SmartGiantsCompat.getMobRewardData().get(SmartGiantsCompat.getSmartGiantsMobType(killed))
			// .isMobEnabled();
			return 0;

		} else if (HerobrineCompat.isHerobrineMob(mob)) {
			if (HerobrineCompat.getMobRewardData().containsKey(HerobrineCompat.getHerobrineMobType(mob)))
				return 0;
			// return
			// HerobrineCompat.getMobRewardData().get(HerobrineCompat.getHerobrineMobType(killed)).isMobEnabled();
			return 0;

		} else if (EliteMobsCompat.isEliteMobs(mob)) {
			if (EliteMobsCompat.getMobRewardData().containsKey(EliteMobsCompat.getEliteMobsType(mob).getName()))
				return 0;
			// return
			// HerobrineCompat.getMobRewardData().get(HerobrineCompat.getHerobrineMobType(killed)).isMobEnabled();
			return 0;

		} else if (BossCompat.isBossMob(mob)) {
			if (BossCompat.getMobRewardData().containsKey(BossCompat.getBossType(mob)))
				return 0;
			return 0;

		} else if (MyPetCompat.isMyPet(mob)) {
			return plugin.getConfigManager().wolfHeadDropChance;

		} else {
			if (isEntityType(mob, "ARMADILLO"))
				return plugin.getConfigManager().turtleHeadDropChance;
			else if (isEntityType(mob, "BOGGED"))
				return plugin.getConfigManager().strayHeadDropChance;
			else if (isEntityType(mob, "BREEZE"))
				return plugin.getConfigManager().vexHeadDropChance;
			else if (isEntityType(mob, "CREAKING"))
				return plugin.getConfigManager().wardenHeadDropChance;

			if (Servers.isMC119OrNewer())
				if (mob instanceof Allay)
					return plugin.getConfigManager().allayHeadDropChance;
				else if (mob instanceof Frog)
					return plugin.getConfigManager().frogHeadDropChance;
				else if (mob instanceof Tadpole)
					return plugin.getConfigManager().tadpoleHeadDropChance;
				else if (mob instanceof Warden)
					return plugin.getConfigManager().wardenHeadDropChance;

			if (Servers.isMC117OrNewer())
				if (mob instanceof Axolotl)
					return plugin.getConfigManager().axolotlHeadDropChance;
				else if (mob instanceof Goat)
					return plugin.getConfigManager().goatHeadDropChance;
				else if (mob instanceof GlowSquid)
					return plugin.getConfigManager().glowsquidHeadDropChance;

			if (Servers.isMC1162OrNewer())
				if (mob instanceof PiglinBrute)
					return plugin.getConfigManager().piglinBruteHeadDropChance;

			if (Servers.isMC116OrNewer())
				if (mob instanceof Hoglin)
					return plugin.getConfigManager().hoglinHeadDropChance;
				else if (mob instanceof Piglin)
					return plugin.getConfigManager().piglinHeadDropChance;
				else if (mob instanceof Strider)
					return plugin.getConfigManager().striderHeadDropChance;
				else if (mob instanceof Zoglin)
					return plugin.getConfigManager().zoglinHeadDropChance;

			if (Servers.isMC115OrNewer())
				if (mob instanceof Bee)
					return plugin.getConfigManager().beeHeadDropChance;

			if (Servers.isMC114OrNewer())
				if (mob instanceof Cat)
					return plugin.getConfigManager().catHeadDropChance;
				else if (mob instanceof Fox)
					return plugin.getConfigManager().foxHeadDropChance;
				else if (mob instanceof Panda)
					return plugin.getConfigManager().pandaHeadDropChance;
				else if (mob instanceof Pillager)
					return plugin.getConfigManager().pillagerHeadDropChance;
				else if (mob instanceof Ravager)
					return plugin.getConfigManager().ravagerHeadDropChance;
				else if (mob instanceof Husk)
					return plugin.getConfigManager().huskHeadDropChance;
				else if (mob instanceof Stray)
					return plugin.getConfigManager().strayHeadDropChance;
				else if (mob instanceof TraderLlama)
					return plugin.getConfigManager().traderLlamaHeadDropChance;
				else if (mob instanceof WanderingTrader)
					return plugin.getConfigManager().wanderingTraderHeadDropChance;
				else if (mob instanceof Villager)
					if (((Villager) mob).getProfession() == Profession.ARMORER)
						return plugin.getConfigManager().armorerHeadDropChance;
					else if (((Villager) mob).getProfession() == Profession.BUTCHER)
						return plugin.getConfigManager().butcherHeadDropChance;
					else if (((Villager) mob).getProfession() == Profession.CARTOGRAPHER)
						return plugin.getConfigManager().cartographerHeadDropChance;
					else if (((Villager) mob).getProfession() == Profession.CLERIC)
						return plugin.getConfigManager().clericHeadDropChance;
					else if (((Villager) mob).getProfession() == Profession.FARMER)
						return plugin.getConfigManager().farmerHeadDropChance;
					else if (((Villager) mob).getProfession() == Profession.FISHERMAN)
						return plugin.getConfigManager().fishermanHeadDropChance;
					else if (((Villager) mob).getProfession() == Profession.FLETCHER)
						return plugin.getConfigManager().fletcherHeadDropChance;
					else if (((Villager) mob).getProfession() == Profession.LEATHERWORKER)
						return plugin.getConfigManager().leatherworkerHeadDropChance;
					else if (((Villager) mob).getProfession() == Profession.LIBRARIAN)
						return plugin.getConfigManager().librarianHeadDropChance;
					else if (((Villager) mob).getProfession() == Profession.MASON)
						return plugin.getConfigManager().masonHeadDropChance;
					else if (((Villager) mob).getProfession() == Profession.NITWIT)
						return plugin.getConfigManager().nitwitHeadDropChance;
					else if (((Villager) mob).getProfession() == Profession.NONE)
						return plugin.getConfigManager().villagerHeadDropChance;
					else if (((Villager) mob).getProfession() == Profession.SHEPHERD)
						return plugin.getConfigManager().shepherdHeadDropChance;
					else if (((Villager) mob).getProfession() == Profession.TOOLSMITH)
						return plugin.getConfigManager().toolsmithHeadDropChance;
					else if (((Villager) mob).getProfession() == Profession.WEAPONSMITH)
						return plugin.getConfigManager().weaponsmithHeadDropChance;

			if (Servers.isMC113OrNewer())
				if (mob instanceof Dolphin)
					return plugin.getConfigManager().dolphinHeadDropChance;
				else if (mob instanceof Drowned)
					return plugin.getConfigManager().drownedHeadDropChance;
				else if (mob instanceof Cod)
					return plugin.getConfigManager().codHeadDropChance;
				else if (mob instanceof Salmon)
					return plugin.getConfigManager().salmonHeadDropChance;
				else if (mob instanceof TropicalFish)
					return plugin.getConfigManager().tropicalFishHeadDropChance;
				else if (mob instanceof PufferFish)
					return plugin.getConfigManager().pufferfishHeadDropChance;
				else if (mob instanceof Phantom)
					return plugin.getConfigManager().phantomHeadDropChance;
				else if (mob instanceof Turtle)
					return plugin.getConfigManager().turtleHeadDropChance;

			if (Servers.isMC112OrNewer())
				if (mob instanceof Parrot)
					return plugin.getConfigManager().parrotHeadDropChance;
				else if (mob instanceof Illusioner)
					return plugin.getConfigManager().illusionerHeadDropChance;

			if (Servers.isMC111OrNewer())
				if (mob instanceof Llama)
					return plugin.getConfigManager().llamaHeadDropChance;
				else if (mob instanceof Vex)
					return plugin.getConfigManager().vexHeadDropChance;
				else if (mob instanceof Vindicator)
					return plugin.getConfigManager().vindicatorHeadDropChance;
				else if (mob instanceof Evoker)
					return plugin.getConfigManager().evokerHeadDropChance;
				else if (mob instanceof Donkey)
					return plugin.getConfigManager().donkeyHeadDropChance;
				else if (mob instanceof Mule)
					return plugin.getConfigManager().muleHeadDropChance;
				else if (mob instanceof SkeletonHorse)
					return plugin.getConfigManager().skeletonHorseHeadDropChance;
				else if (mob instanceof ZombieHorse)
					return plugin.getConfigManager().zombieHorseHeadDropChance;
				else if (mob instanceof Stray)
					return plugin.getConfigManager().strayHeadDropChance;
				else if (mob instanceof Husk)
					return plugin.getConfigManager().huskHeadDropChance;
				else if (mob instanceof ZombieVillager)
					return plugin.getConfigManager().zombieVillagerHeadDropChance;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.NITWIT)
					return plugin.getConfigManager().nitwitHeadDropChance;

			if (Servers.isMC110OrNewer())
				if (mob instanceof PolarBear)
					return plugin.getConfigManager().polarBearHeadDropChance;
				else if (mob instanceof Skeleton && ((Skeleton) mob).getSkeletonType() == SkeletonType.STRAY)
					return plugin.getConfigManager().strayHeadDropChance;

			if (Servers.isMC110OrNewer() && !Servers.isMC114OrNewer())
				if (mob instanceof Zombie
						&& ((Zombie) mob).getVillagerProfession() == Villager.Profession.valueOf("HUSK"))
					return plugin.getConfigManager().huskHeadDropChance;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.valueOf("NORMAL"))
					return plugin.getConfigManager().villagerHeadDropChance;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.valueOf("PRIEST"))
					return plugin.getConfigManager().priestHeadDropChance;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.BUTCHER)
					return plugin.getConfigManager().butcherHeadDropChance;
				else if (mob instanceof Villager
						&& ((Villager) mob).getProfession() == Profession.valueOf("BLACKSMITH"))
					return plugin.getConfigManager().blacksmithHeadDropChance;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.LIBRARIAN)
					return plugin.getConfigManager().librarianHeadDropChance;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.FARMER)
					return plugin.getConfigManager().farmerHeadDropChance;

			if (Servers.isMC19OrNewer())
				if (mob instanceof Shulker)
					return plugin.getConfigManager().shulkerHeadDropChance;

			// MC1.8 or older
			if (mob instanceof Guardian && ((Guardian) mob).isElder())
				return plugin.getConfigManager().elderGuardianHeadDropChance;
			else if (mob instanceof Guardian)
				return plugin.getConfigManager().guardianHeadDropChance;
			else if (mob instanceof Endermite)
				return plugin.getConfigManager().endermiteHeadDropChance;
			else if (mob instanceof Rabbit)
				if ((((Rabbit) mob).getRabbitType()) == Rabbit.Type.THE_KILLER_BUNNY)
					return plugin.getConfigManager().killerRabbitHeadDropChance;
				else
					return plugin.getConfigManager().rabbitHeadDropChance;
			else if (mob instanceof Player)
				return plugin.getConfigManager().pvpHeadDropChance;
			else if (mob instanceof Blaze)
				return plugin.getConfigManager().blazeHeadDropChance;
			else if (mob instanceof Creeper)
				return plugin.getConfigManager().creeperHeadDropChance;
			else if (mob instanceof Silverfish)
				return plugin.getConfigManager().silverfishHeadDropChance;
			else if (mob instanceof Enderman)
				return plugin.getConfigManager().endermanHeadDropChance;
			else if (mob instanceof Giant)
				return plugin.getConfigManager().giantHeadDropChance;
			else if (mob instanceof Skeleton && ((Skeleton) mob).getSkeletonType() == SkeletonType.NORMAL)
				return plugin.getConfigManager().skeletonHeadDropChance;
			else if (mob instanceof Skeleton && ((Skeleton) mob).getSkeletonType() == SkeletonType.WITHER)
				return plugin.getConfigManager().witherSkeletonHeadDropChance;
			else if (mob instanceof CaveSpider)
				// CaveSpider is a subclass of Spider
				return plugin.getConfigManager().caveSpiderHeadDropChance;
			else if (mob instanceof Spider)
				return plugin.getConfigManager().spiderHeadDropChance;
			else if (mob instanceof Witch)
				return plugin.getConfigManager().witchHeadDropChance;
			else if (mob instanceof PigZombie)
				// PigZombie is a subclass of Zombie.
				return plugin.getConfigManager().zombiePigmanHeadDropChance;
			else if (mob instanceof Zombie)
				return plugin.getConfigManager().zombieHeadDropChance;
			else if (mob instanceof Ghast)
				return plugin.getConfigManager().ghastHeadDropChance;
			else if (mob instanceof MagmaCube)
				// MagmaCube is a subclass of Slime
				return plugin.getConfigManager().magmaCubeHeadDropChance;
			else if (mob instanceof Slime)
				return plugin.getConfigManager().slimeHeadDropChance;
			else if (mob instanceof EnderDragon)
				return plugin.getConfigManager().enderDragonHeadDropChance;
			else if (mob instanceof Wither)
				return plugin.getConfigManager().witherHeadDropChance;
			else if (mob instanceof IronGolem)
				return plugin.getConfigManager().ironGolemHeadDropChance;

			// Passive mobs
			else if (mob instanceof Bat)
				return plugin.getConfigManager().batHeadDropChance;
			else if (mob instanceof Chicken)
				return plugin.getConfigManager().chickenHeadDropChance;
			else if (mob instanceof Cow)
				if (mob instanceof MushroomCow)
					// MushroomCow is a subclass of Cow
					return plugin.getConfigManager().mushroomCowHeadDropChance;
				else
					return plugin.getConfigManager().cowHeadDropChance;
			else if (mob instanceof Horse)
				return plugin.getConfigManager().horseHeadDropChance;
			else if (mob instanceof Ocelot)
				return plugin.getConfigManager().ocelotHeadDropChance;
			else if (mob instanceof Pig)
				return plugin.getConfigManager().pigHeadDropChance;
			else if (mob instanceof Sheep)
				return plugin.getConfigManager().sheepHeadDropChance;
			else if (mob instanceof Snowman)
				return plugin.getConfigManager().snowmanHeadDropChance;
			else if (mob instanceof Squid)
				return plugin.getConfigManager().squidHeadDropChance;
			else if (mob instanceof Villager)
				return plugin.getConfigManager().villagerHeadDropChance;
			else if (mob instanceof Wolf)
				return plugin.getConfigManager().wolfHeadDropChance;
			else if (mob instanceof Item && ((Item) mob).getItemStack().getType() == Material.COD)
				return plugin.getConfigManager().codHeadDropChance;
			else if (mob instanceof Item && ((Item) mob).getItemStack().getType() == Material.SALMON)
				return plugin.getConfigManager().salmonHeadDropChance;
			else if (mob instanceof Item && ((Item) mob).getItemStack().getType() == Material.TROPICAL_FISH)
				return plugin.getConfigManager().tropicalFishHeadDropChance;
			else if (mob instanceof Item && ((Item) mob).getItemStack().getType() == Material.PUFFERFISH)
				return plugin.getConfigManager().pufferfishHeadDropChance;
		}
		return 0;
	}

	public String getHeadDropMessage(Entity mob) {
		if (TARDISWeepingAngelsCompat.isWeepingAngelMonster(mob)) {
			if (TARDISWeepingAngelsCompat.getMobRewardData()
					.containsKey(TARDISWeepingAngelsCompat.getWeepingAngelMonsterType(mob).name()))
				return "";
			// return TARDISWeepingAngelsCompat.getMobRewardData()
			// .get(TARDISWeepingAngelsCompat.getWeepingAngelMonsterType(killed).name()).getMobEnabled();
			return "";

		} else if (MythicMobsCompat.isMythicMob(mob)) {
			if (MythicMobsCompat.getMobRewardData().containsKey(MythicMobsCompat.getMythicMobType(mob)))
				return "";
			// return
			// MythicMobsCompat.getMobRewardData().get(MythicMobsCompat.getMythicMobType(killed)).isMobEnabled();
			return "";

		} else if (CitizensCompat.isNPC(mob) && CitizensCompat.isSentryOrSentinelOrSentries(mob)) {
			NPCRegistry registry = CitizensAPI.getNPCRegistry();
			NPC npc = registry.getNPC(mob);
			String key = String.valueOf(npc.getId());
			if (CitizensCompat.getMobRewardData().containsKey(key)) {
				return "";
				// return
				// CitizensCompat.getMobRewardData().get(key).isMobEnabled();
			}
			return "";

		} else if (CustomMobsCompat.isCustomMob(mob)) {
			if (CustomMobsCompat.getMobRewardData().containsKey(CustomMobsCompat.getCustomMobType(mob)))
				return "";
			// return
			// CustomMobsCompat.getMobRewardData().get(CustomMobsCompat.getCustomMobType(killed)).isMobEnabled();
			return "";

		} else if (MysteriousHalloweenCompat.isMysteriousHalloween(mob)) {
			if (MysteriousHalloweenCompat.getMobRewardData()
					.containsKey(MysteriousHalloweenCompat.getMysteriousHalloweenType(mob).name()))
				return "";
			// return MysteriousHalloweenCompat.getMobRewardData()
			// .get(MysteriousHalloweenCompat.getMysteriousHalloweenType(killed).name()).isMobEnabled();
			return "";

		} else if (SmartGiantsCompat.isSmartGiants(mob)) {
			if (SmartGiantsCompat.getMobRewardData().containsKey(SmartGiantsCompat.getSmartGiantsMobType(mob)))
				return "";
			// return
			// SmartGiantsCompat.getMobRewardData().get(SmartGiantsCompat.getSmartGiantsMobType(killed))
			// .isMobEnabled();
			return "";

		} else if (HerobrineCompat.isHerobrineMob(mob)) {
			if (HerobrineCompat.getMobRewardData().containsKey(HerobrineCompat.getHerobrineMobType(mob)))
				return "";
			// return
			// HerobrineCompat.getMobRewardData().get(HerobrineCompat.getHerobrineMobType(killed)).isMobEnabled();
			return "";

		} else if (EliteMobsCompat.isEliteMobs(mob)) {
			if (EliteMobsCompat.getMobRewardData().containsKey(EliteMobsCompat.getEliteMobsType(mob).getName()))
				return "";
			// return
			// HerobrineCompat.getMobRewardData().get(HerobrineCompat.getHerobrineMobType(killed)).isMobEnabled();
			return "";

		} else if (BossCompat.isBossMob(mob)) {
			if (BossCompat.getMobRewardData().containsKey(BossCompat.getBossType(mob)))
				return "";
			return "";

		} else if (MyPetCompat.isMyPet(mob)) {
			return plugin.getConfigManager().wolfHeadMessage;

		} else {
			if (isEntityType(mob, "ARMADILLO"))
				return plugin.getConfigManager().turtleHeadMessage;
			else if (isEntityType(mob, "BOGGED"))
				return plugin.getConfigManager().strayHeadMessage;
			else if (isEntityType(mob, "BREEZE"))
				return plugin.getConfigManager().vexHeadMessage;
			else if (isEntityType(mob, "CREAKING"))
				return plugin.getConfigManager().wardenHeadMessage;

			if (Servers.isMC119OrNewer())
				if (mob instanceof Allay)
					return plugin.getConfigManager().allayHeadMessage;
				else if (mob instanceof Frog)
					return plugin.getConfigManager().frogHeadMessage;
				else if (mob instanceof Tadpole)
					return plugin.getConfigManager().tadpoleHeadMessage;
				else if (mob instanceof Warden)
					return plugin.getConfigManager().wardenHeadMessage;

			if (Servers.isMC117OrNewer())
				if (mob instanceof Axolotl)
					return plugin.getConfigManager().axolotlHeadMessage;
				else if (mob instanceof Goat)
					return plugin.getConfigManager().goatHeadMessage;
				else if (mob instanceof GlowSquid)
					return plugin.getConfigManager().glowsquidHeadMessage;

			if (Servers.isMC1162OrNewer())
				if (mob instanceof PiglinBrute)
					return plugin.getConfigManager().piglinBruteHeadMessage;

			if (Servers.isMC116OrNewer())
				if (mob instanceof Hoglin)
					return plugin.getConfigManager().hoglinHeadMessage;
				else if (mob instanceof Piglin)
					return plugin.getConfigManager().piglinHeadMessage;
				else if (mob instanceof Strider)
					return plugin.getConfigManager().striderHeadMessage;
				else if (mob instanceof Zoglin)
					return plugin.getConfigManager().zoglinHeadMessage;

			if (Servers.isMC115OrNewer())
				if (mob instanceof Bee)
					return plugin.getConfigManager().beeHeadMessage;

			if (Servers.isMC114OrNewer())
				if (mob instanceof Cat)
					return plugin.getConfigManager().catHeadMessage;
				else if (mob instanceof Fox)
					return plugin.getConfigManager().foxHeadMessage;
				else if (mob instanceof Panda)
					return plugin.getConfigManager().pandaHeadMessage;
				else if (mob instanceof Pillager)
					return plugin.getConfigManager().pillagerHeadMessage;
				else if (mob instanceof Ravager)
					return plugin.getConfigManager().ravagerHeadMessage;
				else if (mob instanceof Husk)
					return plugin.getConfigManager().huskHeadMessage;
				else if (mob instanceof Stray)
					return plugin.getConfigManager().strayHeadMessage;
				else if (mob instanceof TraderLlama)
					return plugin.getConfigManager().traderLlamaHeadMessage;
				else if (mob instanceof WanderingTrader)
					return plugin.getConfigManager().wanderingTraderHeadMessage;
				else if (mob instanceof Villager)
					if (((Villager) mob).getProfession() == Profession.ARMORER)
						return plugin.getConfigManager().armorerHeadMessage;
					else if (((Villager) mob).getProfession() == Profession.BUTCHER)
						return plugin.getConfigManager().butcherHeadMessage;
					else if (((Villager) mob).getProfession() == Profession.CARTOGRAPHER)
						return plugin.getConfigManager().cartographerHeadMessage;
					else if (((Villager) mob).getProfession() == Profession.CLERIC)
						return plugin.getConfigManager().clericHeadMessage;
					else if (((Villager) mob).getProfession() == Profession.FARMER)
						return plugin.getConfigManager().farmerHeadMessage;
					else if (((Villager) mob).getProfession() == Profession.FISHERMAN)
						return plugin.getConfigManager().fishermanHeadMessage;
					else if (((Villager) mob).getProfession() == Profession.FLETCHER)
						return plugin.getConfigManager().fletcherHeadMessage;
					else if (((Villager) mob).getProfession() == Profession.LEATHERWORKER)
						return plugin.getConfigManager().leatherworkerHeadMessage;
					else if (((Villager) mob).getProfession() == Profession.LIBRARIAN)
						return plugin.getConfigManager().librarianHeadMessage;
					else if (((Villager) mob).getProfession() == Profession.MASON)
						return plugin.getConfigManager().masonHeadMessage;
					else if (((Villager) mob).getProfession() == Profession.NITWIT)
						return plugin.getConfigManager().nitwitHeadMessage;
					else if (((Villager) mob).getProfession() == Profession.NONE)
						return plugin.getConfigManager().villagerHeadMessage;
					else if (((Villager) mob).getProfession() == Profession.SHEPHERD)
						return plugin.getConfigManager().shepherdHeadMessage;
					else if (((Villager) mob).getProfession() == Profession.TOOLSMITH)
						return plugin.getConfigManager().toolsmithHeadMessage;
					else if (((Villager) mob).getProfession() == Profession.WEAPONSMITH)
						return plugin.getConfigManager().weaponsmithHeadMessage;

			if (Servers.isMC113OrNewer())
				if (mob instanceof Dolphin)
					return plugin.getConfigManager().dolphinHeadMessage;
				else if (mob instanceof Drowned)
					return plugin.getConfigManager().drownedHeadMessage;
				else if (mob instanceof Cod)
					return plugin.getConfigManager().codHeadMessage;
				else if (mob instanceof Salmon)
					return plugin.getConfigManager().salmonHeadMessage;
				else if (mob instanceof TropicalFish)
					return plugin.getConfigManager().tropicalFishHeadMessage;
				else if (mob instanceof PufferFish)
					return plugin.getConfigManager().pufferfishHeadMessage;
				else if (mob instanceof Phantom)
					return plugin.getConfigManager().phantomHeadMessage;
				else if (mob instanceof Turtle)
					return plugin.getConfigManager().turtleHeadMessage;

			if (Servers.isMC112OrNewer())
				if (mob instanceof Parrot)
					return plugin.getConfigManager().parrotHeadMessage;
				else if (mob instanceof Illusioner)
					return plugin.getConfigManager().illusionerHeadMessage;

			if (Servers.isMC111OrNewer())
				if (mob instanceof Llama)
					return plugin.getConfigManager().llamaHeadMessage;
				else if (mob instanceof Vex)
					return plugin.getConfigManager().vexHeadMessage;
				else if (mob instanceof Vindicator)
					return plugin.getConfigManager().vindicatorHeadMessage;
				else if (mob instanceof Evoker)
					return plugin.getConfigManager().evokerHeadMessage;
				else if (mob instanceof Donkey)
					return plugin.getConfigManager().donkeyHeadMessage;
				else if (mob instanceof Mule)
					return plugin.getConfigManager().muleHeadMessage;
				else if (mob instanceof SkeletonHorse)
					return plugin.getConfigManager().skeletonHorseHeadMessage;
				else if (mob instanceof ZombieHorse)
					return plugin.getConfigManager().zombieHorseHeadMessage;
				else if (mob instanceof Stray)
					return plugin.getConfigManager().strayHeadMessage;
				else if (mob instanceof Husk)
					return plugin.getConfigManager().huskHeadMessage;
				else if (mob instanceof ZombieVillager)
					return plugin.getConfigManager().zombieVillagerHeadMessage;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.NITWIT)
					return plugin.getConfigManager().nitwitHeadMessage;

			if (Servers.isMC110OrNewer())
				if (mob instanceof PolarBear)
					return plugin.getConfigManager().polarBearHeadMessage;
				else if (mob instanceof Skeleton && ((Skeleton) mob).getSkeletonType() == SkeletonType.STRAY)
					return plugin.getConfigManager().strayHeadMessage;

			// Handle old villagers
			if (Servers.isMC110OrNewer() && !Servers.isMC114OrNewer())
				if (mob instanceof Zombie
						&& ((Zombie) mob).getVillagerProfession() == Villager.Profession.valueOf("HUSK"))
					return plugin.getConfigManager().huskHeadMessage;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.valueOf("NORMAL"))
					return plugin.getConfigManager().villagerHeadMessage;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.valueOf("PRIEST"))
					return plugin.getConfigManager().priestHeadMessage;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.BUTCHER)
					return plugin.getConfigManager().butcherHeadMessage;
				else if (mob instanceof Villager
						&& ((Villager) mob).getProfession() == Profession.valueOf("BLACKSMITH"))
					return plugin.getConfigManager().blacksmithHeadMessage;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.LIBRARIAN)
					return plugin.getConfigManager().librarianHeadMessage;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.FARMER)
					return plugin.getConfigManager().farmerHeadMessage;

			if (Servers.isMC19OrNewer())
				if (mob instanceof Shulker)
					return plugin.getConfigManager().shulkerHeadMessage;

			// MC1.8 or older
			if (mob instanceof Guardian && ((Guardian) mob).isElder())
				return plugin.getConfigManager().elderGuardianHeadMessage;
			else if (mob instanceof Guardian)
				return plugin.getConfigManager().guardianHeadMessage;
			else if (mob instanceof Endermite)
				return plugin.getConfigManager().endermiteHeadMessage;
			else if (mob instanceof Rabbit)
				if ((((Rabbit) mob).getRabbitType()) == Rabbit.Type.THE_KILLER_BUNNY)
					return plugin.getConfigManager().killerRabbitHeadMessage;
				else
					return plugin.getConfigManager().rabbitHeadMessage;
			else if (mob instanceof Player)
				return plugin.getConfigManager().pvpHeadMessage;
			else if (mob instanceof Blaze)
				return plugin.getConfigManager().blazeHeadMessage;
			else if (mob instanceof Creeper)
				return plugin.getConfigManager().creeperHeadMessage;
			else if (mob instanceof Silverfish)
				return plugin.getConfigManager().silverfishHeadMessage;
			else if (mob instanceof Enderman)
				return plugin.getConfigManager().endermanHeadMessage;
			else if (mob instanceof Giant)
				return plugin.getConfigManager().giantHeadMessage;
			else if (mob instanceof Skeleton && ((Skeleton) mob).getSkeletonType() == SkeletonType.NORMAL)
				return plugin.getConfigManager().skeletonHeadMessage;
			else if (mob instanceof Skeleton && ((Skeleton) mob).getSkeletonType() == SkeletonType.WITHER)
				return plugin.getConfigManager().witherSkeletonHeadMessage;
			else if (mob instanceof CaveSpider)
				// CaveSpider is a subclass of Spider
				return plugin.getConfigManager().caveSpiderHeadMessage;
			else if (mob instanceof Spider)
				return plugin.getConfigManager().spiderHeadMessage;
			else if (mob instanceof Witch)
				return plugin.getConfigManager().witchHeadMessage;
			else if (mob instanceof PigZombie)
				// PigZombie is a subclass of Zombie.
				return plugin.getConfigManager().zombiePigmanHeadMessage;
			else if (mob instanceof Zombie)
				return plugin.getConfigManager().zombieHeadMessage;
			else if (mob instanceof Ghast)
				return plugin.getConfigManager().ghastHeadMessage;
			else if (mob instanceof MagmaCube)
				// MagmaCube is a subclass of Slime
				return plugin.getConfigManager().magmaCubeHeadMessage;
			else if (mob instanceof Slime)
				return plugin.getConfigManager().slimeHeadMessage;
			else if (mob instanceof EnderDragon)
				return plugin.getConfigManager().enderDragonHeadMessage;
			else if (mob instanceof Wither)
				return plugin.getConfigManager().witherHeadMessage;
			else if (mob instanceof IronGolem)
				return plugin.getConfigManager().ironGolemHeadMessage;

			// Passive mobs
			else if (mob instanceof Bat)
				return plugin.getConfigManager().batHeadMessage;
			else if (mob instanceof Chicken)
				return plugin.getConfigManager().chickenHeadMessage;
			else if (mob instanceof Cow)
				if (mob instanceof MushroomCow)
					// MushroomCow is a subclass of Cow
					return plugin.getConfigManager().mushroomCowHeadMessage;
				else
					return plugin.getConfigManager().cowHeadMessage;
			else if (mob instanceof Horse)
				return plugin.getConfigManager().horseHeadMessage;
			else if (mob instanceof Ocelot)
				return plugin.getConfigManager().ocelotHeadMessage;
			else if (mob instanceof Pig)
				return plugin.getConfigManager().pigHeadMessage;
			else if (mob instanceof Sheep)
				return plugin.getConfigManager().sheepHeadMessage;
			else if (mob instanceof Snowman)
				return plugin.getConfigManager().snowmanHeadMessage;
			else if (mob instanceof Squid)
				return plugin.getConfigManager().squidHeadMessage;
			else if (mob instanceof Villager)
				return plugin.getConfigManager().villagerHeadMessage;
			else if (mob instanceof Wolf)
				return plugin.getConfigManager().wolfHeadMessage;
			else if (mob instanceof Item && ((Item) mob).getItemStack().getType() == Material.COD)
				return plugin.getConfigManager().codHeadMessage;
			else if (mob instanceof Item && ((Item) mob).getItemStack().getType() == Material.SALMON)
				return plugin.getConfigManager().salmonHeadMessage;
			else if (mob instanceof Item && ((Item) mob).getItemStack().getType() == Material.TROPICAL_FISH)
				return plugin.getConfigManager().tropicalFishHeadMessage;
			else if (mob instanceof Item && ((Item) mob).getItemStack().getType() == Material.PUFFERFISH)
				return plugin.getConfigManager().pufferfishHeadMessage;
		}
		return "";
	}

	public double getHeadValue(Entity mob) {
		if (TARDISWeepingAngelsCompat.isWeepingAngelMonster(mob)) {
			if (TARDISWeepingAngelsCompat.getMobRewardData()
					.containsKey(TARDISWeepingAngelsCompat.getWeepingAngelMonsterType(mob).name()))
				return 0;
			// return TARDISWeepingAngelsCompat.getMobRewardData()
			// .get(TARDISWeepingAngelsCompat.getWeepingAngelMonsterType(killed).name()).getMobEnabled();
			return 0;

		} else if (MythicMobsCompat.isMythicMob(mob)) {
			if (MythicMobsCompat.getMobRewardData().containsKey(MythicMobsCompat.getMythicMobType(mob)))
				return 0;
			// return
			// MythicMobsCompat.getMobRewardData().get(MythicMobsCompat.getMythicMobType(killed)).isMobEnabled();
			return 0;

		} else if (CitizensCompat.isNPC(mob) && CitizensCompat.isSentryOrSentinelOrSentries(mob)) {
			NPCRegistry registry = CitizensAPI.getNPCRegistry();
			NPC npc = registry.getNPC(mob);
			String key = String.valueOf(npc.getId());
			if (CitizensCompat.getMobRewardData().containsKey(key)) {
				return 0;
				// return
				// CitizensCompat.getMobRewardData().get(key).isMobEnabled();
			}
			return 0;

		} else if (CustomMobsCompat.isCustomMob(mob)) {
			if (CustomMobsCompat.getMobRewardData().containsKey(CustomMobsCompat.getCustomMobType(mob)))
				return 0;
			// return
			// CustomMobsCompat.getMobRewardData().get(CustomMobsCompat.getCustomMobType(killed)).isMobEnabled();
			return 0;

		} else if (MysteriousHalloweenCompat.isMysteriousHalloween(mob)) {
			if (MysteriousHalloweenCompat.getMobRewardData()
					.containsKey(MysteriousHalloweenCompat.getMysteriousHalloweenType(mob).name()))
				return 0;
			// return MysteriousHalloweenCompat.getMobRewardData()
			// .get(MysteriousHalloweenCompat.getMysteriousHalloweenType(killed).name()).isMobEnabled();
			return 0;

		} else if (SmartGiantsCompat.isSmartGiants(mob)) {
			if (SmartGiantsCompat.getMobRewardData().containsKey(SmartGiantsCompat.getSmartGiantsMobType(mob)))
				return 0;
			// return
			// SmartGiantsCompat.getMobRewardData().get(SmartGiantsCompat.getSmartGiantsMobType(killed))
			// .isMobEnabled();
			return 0;

		} else if (HerobrineCompat.isHerobrineMob(mob)) {
			if (HerobrineCompat.getMobRewardData().containsKey(HerobrineCompat.getHerobrineMobType(mob)))
				return 0;
			// return
			// HerobrineCompat.getMobRewardData().get(HerobrineCompat.getHerobrineMobType(killed)).isMobEnabled();
			return 0;

		} else if (EliteMobsCompat.isEliteMobs(mob)) {
			if (EliteMobsCompat.getMobRewardData().containsKey(EliteMobsCompat.getEliteMobsType(mob).getName()))
				return 0;
			// return
			// HerobrineCompat.getMobRewardData().get(HerobrineCompat.getHerobrineMobType(killed)).isMobEnabled();
			return 0;

		} else if (BossCompat.isBossMob(mob)) {
			if (BossCompat.getMobRewardData().containsKey(BossCompat.getBossType(mob)))
				return 0;
			return 0;

		} else if (MyPetCompat.isMyPet(mob)) {
			return getPrice(mob, plugin.getConfigManager().wolfHeadPrize);

		} else {
			if (isEntityType(mob, "ARMADILLO"))
				return getPrice(mob, plugin.getConfigManager().turtleHeadPrize);
			else if (isEntityType(mob, "BOGGED"))
				return getPrice(mob, plugin.getConfigManager().strayHeadPrize);
			else if (isEntityType(mob, "BREEZE"))
				return getPrice(mob, plugin.getConfigManager().vexHeadPrize);
			else if (isEntityType(mob, "CREAKING"))
				return getPrice(mob, plugin.getConfigManager().wardenHeadPrize);

			if (Servers.isMC119OrNewer())
				if (mob instanceof Allay)
					return getPrice(mob, plugin.getConfigManager().allayHeadPrize);
				else if (mob instanceof Frog)
					return getPrice(mob, plugin.getConfigManager().frogHeadPrize);
				else if (mob instanceof Tadpole)
					return getPrice(mob, plugin.getConfigManager().tadpoleHeadPrize);
				else if (mob instanceof Warden)
					return getPrice(mob, plugin.getConfigManager().wardenHeadPrize);

			if (Servers.isMC117OrNewer())
				if (mob instanceof Axolotl)
					return getPrice(mob, plugin.getConfigManager().axolotlHeadPrize);
				else if (mob instanceof Goat)
					return getPrice(mob, plugin.getConfigManager().goatHeadPrize);
				else if (mob instanceof GlowSquid)
					return getPrice(mob, plugin.getConfigManager().glowsquidHeadPrize);

			if (Servers.isMC1162OrNewer())
				if (mob instanceof PiglinBrute)
					return getPrice(mob, plugin.getConfigManager().piglinBruteHeadPrize);

			if (Servers.isMC116OrNewer())
				if (mob instanceof Hoglin)
					return getPrice(mob, plugin.getConfigManager().hoglinHeadPrize);
				else if (mob instanceof Piglin)
					return getPrice(mob, plugin.getConfigManager().piglinHeadPrize);
				else if (mob instanceof Strider)
					return getPrice(mob, plugin.getConfigManager().striderHeadPrize);
				else if (mob instanceof Zoglin)
					return getPrice(mob, plugin.getConfigManager().zoglinHeadPrize);

			if (Servers.isMC115OrNewer())
				if (mob instanceof Bee)
					return getPrice(mob, plugin.getConfigManager().beeHeadPrize);

			if (Servers.isMC114OrNewer())
				if (mob instanceof Cat)
					return getPrice(mob, plugin.getConfigManager().catHeadPrize);
				else if (mob instanceof Fox)
					return getPrice(mob, plugin.getConfigManager().foxHeadPrize);
				else if (mob instanceof Panda)
					return getPrice(mob, plugin.getConfigManager().pandaHeadPrize);
				else if (mob instanceof Pillager)
					return getPrice(mob, plugin.getConfigManager().pillagerHeadPrize);
				else if (mob instanceof Ravager)
					return getPrice(mob, plugin.getConfigManager().ravagerHeadPrize);
				else if (mob instanceof Husk)
					return getPrice(mob, plugin.getConfigManager().huskHeadPrize);
				else if (mob instanceof Stray)
					return getPrice(mob, plugin.getConfigManager().strayHeadPrize);
				else if (mob instanceof TraderLlama)
					return getPrice(mob, plugin.getConfigManager().traderLlamaHeadPrize);
				else if (mob instanceof WanderingTrader)
					return getPrice(mob, plugin.getConfigManager().wanderingTraderHeadPrize);
				else if (mob instanceof Villager)
					if (((Villager) mob).getProfession() == Profession.ARMORER)
						return getPrice(mob, plugin.getConfigManager().armorerHeadPrize);
					else if (((Villager) mob).getProfession() == Profession.BUTCHER)
						return getPrice(mob, plugin.getConfigManager().butcherHeadPrize);
					else if (((Villager) mob).getProfession() == Profession.CARTOGRAPHER)
						return getPrice(mob, plugin.getConfigManager().cartographerHeadPrize);
					else if (((Villager) mob).getProfession() == Profession.CLERIC)
						return getPrice(mob, plugin.getConfigManager().clericHeadPrize);
					else if (((Villager) mob).getProfession() == Profession.FARMER)
						return getPrice(mob, plugin.getConfigManager().farmerHeadPrize);
					else if (((Villager) mob).getProfession() == Profession.FISHERMAN)
						return getPrice(mob, plugin.getConfigManager().fishermanHeadPrize);
					else if (((Villager) mob).getProfession() == Profession.FLETCHER)
						return getPrice(mob, plugin.getConfigManager().fletcherHeadPrize);
					else if (((Villager) mob).getProfession() == Profession.LEATHERWORKER)
						return getPrice(mob, plugin.getConfigManager().leatherworkerHeadPrize);
					else if (((Villager) mob).getProfession() == Profession.LIBRARIAN)
						return getPrice(mob, plugin.getConfigManager().librarianHeadPrize);
					else if (((Villager) mob).getProfession() == Profession.MASON)
						return getPrice(mob, plugin.getConfigManager().masonHeadPrize);
					else if (((Villager) mob).getProfession() == Profession.NITWIT)
						return getPrice(mob, plugin.getConfigManager().nitwitHeadPrize);
					else if (((Villager) mob).getProfession() == Profession.NONE)
						return getPrice(mob, plugin.getConfigManager().villagerHeadPrize);
					else if (((Villager) mob).getProfession() == Profession.SHEPHERD)
						return getPrice(mob, plugin.getConfigManager().shepherdHeadPrize);
					else if (((Villager) mob).getProfession() == Profession.TOOLSMITH)
						return getPrice(mob, plugin.getConfigManager().toolsmithHeadPrize);
					else if (((Villager) mob).getProfession() == Profession.WEAPONSMITH)
						return getPrice(mob, plugin.getConfigManager().weaponsmithHeadPrize);

			if (Servers.isMC113OrNewer())
				if (mob instanceof Dolphin)
					return getPrice(mob, plugin.getConfigManager().dolphinHeadPrize);
				else if (mob instanceof Drowned)
					return getPrice(mob, plugin.getConfigManager().drownedHeadPrize);
				else if (mob instanceof Cod)
					return getPrice(mob, plugin.getConfigManager().codHeadPrize);
				else if (mob instanceof Salmon)
					return getPrice(mob, plugin.getConfigManager().salmonHeadPrize);
				else if (mob instanceof TropicalFish)
					return getPrice(mob, plugin.getConfigManager().tropicalFishHeadPrize);
				else if (mob instanceof PufferFish)
					return getPrice(mob, plugin.getConfigManager().pufferfishHeadPrize);
				else if (mob instanceof Phantom)
					return getPrice(mob, plugin.getConfigManager().phantomHeadPrize);
				else if (mob instanceof Turtle)
					return getPrice(mob, plugin.getConfigManager().turtleHeadPrize);

			if (Servers.isMC112OrNewer())
				if (mob instanceof Parrot)
					return getPrice(mob, plugin.getConfigManager().parrotHeadPrize);
				else if (mob instanceof Illusioner)
					return getPrice(mob, plugin.getConfigManager().illusionerHeadPrize);

			if (Servers.isMC111OrNewer())
				if (mob instanceof Llama)
					return getPrice(mob, plugin.getConfigManager().llamaHeadPrize);
				else if (mob instanceof Vex)
					return getPrice(mob, plugin.getConfigManager().vexHeadPrize);
				else if (mob instanceof Vindicator)
					return getPrice(mob, plugin.getConfigManager().vindicatorHeadPrize);
				else if (mob instanceof Evoker)
					return getPrice(mob, plugin.getConfigManager().evokerHeadPrize);
				else if (mob instanceof Donkey)
					return getPrice(mob, plugin.getConfigManager().donkeyHeadPrize);
				else if (mob instanceof Mule)
					return getPrice(mob, plugin.getConfigManager().muleHeadPrize);
				else if (mob instanceof SkeletonHorse)
					return getPrice(mob, plugin.getConfigManager().skeletonHorseHeadPrize);
				else if (mob instanceof ZombieHorse)
					return getPrice(mob, plugin.getConfigManager().zombieHorseHeadPrize);
				else if (mob instanceof Stray)
					return getPrice(mob, plugin.getConfigManager().strayHeadPrize);
				else if (mob instanceof Husk)
					return getPrice(mob, plugin.getConfigManager().huskHeadPrize);
				else if (mob instanceof ZombieVillager)
					return getPrice(mob, plugin.getConfigManager().zombieVillagerHeadPrize);
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.NITWIT)
					return getPrice(mob, plugin.getConfigManager().nitwitHeadPrize);

			if (Servers.isMC110OrNewer())
				if (mob instanceof PolarBear)
					return getPrice(mob, plugin.getConfigManager().polarBearHeadPrize);
				else if (mob instanceof Skeleton && ((Skeleton) mob).getSkeletonType() == SkeletonType.STRAY)
					return getPrice(mob, plugin.getConfigManager().strayHeadPrize);

			// Handle old villagers
			if (Servers.isMC110OrNewer() && !Servers.isMC114OrNewer())
				if (mob instanceof Zombie
						&& ((Zombie) mob).getVillagerProfession() == Villager.Profession.valueOf("HUSK"))
					return getPrice(mob, plugin.getConfigManager().huskHeadPrize);
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.valueOf("NORMAL"))
					return getPrice(mob, plugin.getConfigManager().villagerHeadPrize);
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.valueOf("PRIEST"))
					return getPrice(mob, plugin.getConfigManager().priestHeadPrize);
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.BUTCHER)
					return getPrice(mob, plugin.getConfigManager().butcherHeadPrize);
				else if (mob instanceof Villager
						&& ((Villager) mob).getProfession() == Profession.valueOf("BLACKSMITH"))
					return getPrice(mob, plugin.getConfigManager().blacksmithHeadPrize);
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.LIBRARIAN)
					return getPrice(mob, plugin.getConfigManager().librarianHeadPrize);
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.FARMER)
					return getPrice(mob, plugin.getConfigManager().farmerHeadPrize);

			if (Servers.isMC19OrNewer())
				if (mob instanceof Shulker)
					return getPrice(mob, plugin.getConfigManager().shulkerHeadPrize);

			// MC1.8 or older
			if (mob instanceof Guardian && ((Guardian) mob).isElder())
				return getPrice(mob, plugin.getConfigManager().elderGuardianHeadPrize);
			else if (mob instanceof Guardian)
				return getPrice(mob, plugin.getConfigManager().guardianHeadPrize);
			else if (mob instanceof Endermite)
				return getPrice(mob, plugin.getConfigManager().endermiteHeadPrize);
			else if (mob instanceof Rabbit)
				if ((((Rabbit) mob).getRabbitType()) == Rabbit.Type.THE_KILLER_BUNNY)
					return getPrice(mob, plugin.getConfigManager().killerRabbitHeadPrize);
				else
					return getPrice(mob, plugin.getConfigManager().rabbitHeadPrize);
			else if (mob instanceof Player)
				return getPrice(mob, plugin.getConfigManager().pvpHeadPrize);
			else if (mob instanceof Blaze)
				return getPrice(mob, plugin.getConfigManager().blazeHeadPrize);
			else if (mob instanceof Creeper)
				return getPrice(mob, plugin.getConfigManager().creeperHeadPrize);
			else if (mob instanceof Silverfish)
				return getPrice(mob, plugin.getConfigManager().silverfishHeadPrize);
			else if (mob instanceof Enderman)
				return getPrice(mob, plugin.getConfigManager().endermanHeadPrize);
			else if (mob instanceof Giant)
				return getPrice(mob, plugin.getConfigManager().giantHeadPrize);
			else if (mob instanceof Skeleton && ((Skeleton) mob).getSkeletonType() == SkeletonType.NORMAL)
				return getPrice(mob, plugin.getConfigManager().skeletonHeadPrize);
			else if (mob instanceof Skeleton && ((Skeleton) mob).getSkeletonType() == SkeletonType.WITHER)
				return getPrice(mob, plugin.getConfigManager().witherSkeletonHeadPrize);
			else if (mob instanceof CaveSpider)
				// CaveSpider is a subclass of Spider
				return getPrice(mob, plugin.getConfigManager().caveSpiderHeadPrize);
			else if (mob instanceof Spider)
				return getPrice(mob, plugin.getConfigManager().spiderHeadPrize);
			else if (mob instanceof Witch)
				return getPrice(mob, plugin.getConfigManager().witchHeadPrize);
			else if (mob instanceof PigZombie)
				// PigZombie is a subclass of Zombie.
				return getPrice(mob, plugin.getConfigManager().zombiePigmanHeadPrize);
			else if (mob instanceof Zombie)
				return getPrice(mob, plugin.getConfigManager().zombieHeadPrize);
			else if (mob instanceof Ghast)
				return getPrice(mob, plugin.getConfigManager().ghastHeadPrize);
			else if (mob instanceof MagmaCube)
				// MagmaCube is a subclass of Slime
				return getPrice(mob, plugin.getConfigManager().magmaCubeHeadPrize);
			else if (mob instanceof Slime)
				return getPrice(mob, plugin.getConfigManager().slimeHeadPrize);
			else if (mob instanceof EnderDragon)
				return getPrice(mob, plugin.getConfigManager().enderDragonHeadPrize);
			else if (mob instanceof Wither)
				return getPrice(mob, plugin.getConfigManager().witherHeadPrize);
			else if (mob instanceof IronGolem)
				return getPrice(mob, plugin.getConfigManager().ironGolemHeadPrize);

			// Passive mobs
			else if (mob instanceof Bat)
				return getPrice(mob, plugin.getConfigManager().batHeadPrize);
			else if (mob instanceof Chicken)
				return getPrice(mob, plugin.getConfigManager().chickenHeadPrize);
			else if (mob instanceof Cow)
				if (mob instanceof MushroomCow)
					// MushroomCow is a subclass of Cow
					return getPrice(mob, plugin.getConfigManager().mushroomCowHeadPrize);
				else
					return getPrice(mob, plugin.getConfigManager().cowHeadPrize);
			else if (mob instanceof Horse)
				return getPrice(mob, plugin.getConfigManager().horseHeadPrize);
			else if (mob instanceof Ocelot)
				return getPrice(mob, plugin.getConfigManager().ocelotHeadPrize);
			else if (mob instanceof Pig)
				return getPrice(mob, plugin.getConfigManager().pigHeadPrize);
			else if (mob instanceof Sheep)
				return getPrice(mob, plugin.getConfigManager().sheepHeadPrize);
			else if (mob instanceof Snowman)
				return getPrice(mob, plugin.getConfigManager().snowmanHeadPrize);
			else if (mob instanceof Squid)
				return getPrice(mob, plugin.getConfigManager().squidHeadPrize);
			else if (mob instanceof Villager)
				return getPrice(mob, plugin.getConfigManager().villagerHeadPrize);
			else if (mob instanceof Wolf)
				return getPrice(mob, plugin.getConfigManager().wolfHeadPrize);
			else if (mob instanceof Item && ((Item) mob).getItemStack().getType() == Material.COD)
				return getPrice(mob, plugin.getConfigManager().codHeadPrize);
			else if (mob instanceof Item && ((Item) mob).getItemStack().getType() == Material.SALMON)
				return getPrice(mob, plugin.getConfigManager().salmonHeadPrize);
			else if (mob instanceof Item && ((Item) mob).getItemStack().getType() == Material.TROPICAL_FISH)
				return getPrice(mob, plugin.getConfigManager().tropicalFishHeadPrize);
			else if (mob instanceof Item && ((Item) mob).getItemStack().getType() == Material.PUFFERFISH)
				return getPrice(mob, plugin.getConfigManager().pufferfishHeadPrize);
		}
		return 0;
	}

}
