package one.lindegaard.MobHunting.rewards;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Donkey;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Endermite;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Evoker;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Giant;
import org.bukkit.entity.Guardian;
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
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.PolarBear;
import org.bukkit.entity.Rabbit;
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
import org.bukkit.entity.Vex;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Vindicator;
import org.bukkit.entity.Witch;
import org.bukkit.entity.Wither;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.ZombieHorse;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.gestern.gringotts.Configuration;
import org.gestern.gringotts.currency.Denomination;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.compatibility.CitizensCompat;
import one.lindegaard.MobHunting.compatibility.CustomMobsCompat;
import one.lindegaard.MobHunting.compatibility.GringottsCompat;
import one.lindegaard.MobHunting.compatibility.HerobrineCompat;
import one.lindegaard.MobHunting.compatibility.MyPetCompat;
import one.lindegaard.MobHunting.compatibility.MysteriousHalloweenCompat;
import one.lindegaard.MobHunting.compatibility.MythicMobsCompat;
import one.lindegaard.MobHunting.compatibility.SmartGiantsCompat;
import one.lindegaard.MobHunting.compatibility.TARDISWeepingAngelsCompat;
import one.lindegaard.MobHunting.mobs.ExtendedMobManager;
import one.lindegaard.MobHunting.mobs.MinecraftMob;
import one.lindegaard.MobHunting.util.Misc;

@SuppressWarnings("deprecation")
public class RewardManager implements Listener {

	// public static final String MH_MONEY = "MH:Money";
	public static final String MH_REWARD_DATA = "MH:HiddenRewardData";
	// Unique random generated UUID for "Bag of gold" rewards
	public static final String MH_REWARD_BAG_OF_GOLD_UUID = "b3f74fad-429f-4801-9e31-b8879cbae96f";
	// Unique random generated UUID for MobHead/Playerhead rewards
	public static final String MH_REWARD_KILLED_UUID = "2351844f-f400-4fa4-9642-35169c5b048a";
	// Unique random generated UUID for ITEM rewards
	public static final String MH_REWARD_ITEM_UUID = "3ffe9c3b-0445-4c35-a952-c2aaf5aeac76";
	// Unique random generated UUID for KILLER head rewards
	public static final String MH_REWARD_KILLER_UUID = "d81f1076-c91c-44c0-98c3-02a2ee88aa97";

	private MobHunting plugin;
	private File file;
	private YamlConfiguration config = new YamlConfiguration();

	private Economy mEconomy;
	private PickupRewards pickupRewards;

	private HashMap<Integer, Double> droppedMoney = new HashMap<Integer, Double>();
	private HashMap<UUID, Reward> placedMoney_Reward = new HashMap<UUID, Reward>();
	private HashMap<UUID, Location> placedMoney_Location = new HashMap<UUID, Location>();

	public RewardManager(MobHunting plugin) {
		this.plugin = plugin;
		file = new File(plugin.getDataFolder(), "rewards.yml");
		RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServicesManager().getRegistration(Economy.class);
		if (economyProvider == null) {
			Bukkit.getLogger().severe(Messages.getString(plugin.getName().toLowerCase() + ".hook.econ"));
			Bukkit.getPluginManager().disablePlugin(plugin);
			return;
		}

		mEconomy = economyProvider.getProvider();
		pickupRewards = new PickupRewards(plugin);

		Bukkit.getPluginManager().registerEvents(new RewardListeners(plugin), plugin);
		if (Misc.isMC18OrNewer())
			Bukkit.getPluginManager().registerEvents(new MoneyMergeEventListener(plugin), plugin);

		if (Misc.isMC112OrNewer() && eventDoesExists())
			Bukkit.getPluginManager().registerEvents(new EntityPickupItemEventListener(pickupRewards), plugin);
		else
			Bukkit.getPluginManager().registerEvents(new PlayerPickupItemEventListener(pickupRewards), plugin);
		loadAllStoredRewards();

		if (MobHunting.getConfigManager().dropMoneyOnGroundUseAsCurrency)
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

	public Economy getEconomy() {
		return mEconomy;
	}

	public HashMap<Integer, Double> getDroppedMoney() {
		return droppedMoney;
	}

	public HashMap<UUID, Reward> getLocations() {
		return placedMoney_Reward;
	}

	public HashMap<UUID, Location> getReward() {
		return placedMoney_Location;
	}

	public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, double amount) {
		EconomyResponse result = mEconomy.withdrawPlayer(offlinePlayer, amount);
		if (!result.transactionSuccess() && offlinePlayer.isOnline())
			((Player) offlinePlayer).sendMessage(ChatColor.RED + "Unable to remove money: " + result.errorMessage);
		return result;
	}

	public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, double amount) {
		EconomyResponse result = mEconomy.depositPlayer(offlinePlayer, amount);
		if (!result.transactionSuccess() && offlinePlayer.isOnline())
			((Player) offlinePlayer).sendMessage(ChatColor.RED + "Unable to add money: " + result.errorMessage);
		return result;
	}

	public String format(double amount) {
		return mEconomy.format(Misc.round(amount));
	}

	public double getBalance(OfflinePlayer offlinePlayer) {
		return mEconomy.getBalance(offlinePlayer);
	}

	public boolean has(OfflinePlayer offlinePlayer, double amount) {
		return mEconomy.has(offlinePlayer, amount);
	}

	public void dropMoneyOnGround(Player player, Entity killedEntity, Location location, double money) {
		Item item = null;
		money = Misc.ceil(money);
		if (GringottsCompat.isSupported()) {
			List<Denomination> denoms = Configuration.CONF.currency.denominations();
			int unit = Configuration.CONF.currency.unit;
			double rest = money;
			for (Denomination d : denoms) {
				ItemStack is = new ItemStack(d.key.type.getType(), 1);
				while (rest >= (d.value / unit)) {
					item = location.getWorld().dropItem(location, is);
					rest = rest - (d.value / unit);
				}
			}
		} else {
			ItemStack is;
			UUID uuid = null, skinuuid = null;
			if (MobHunting.getConfigManager().dropMoneyOnGroundItemtype.equalsIgnoreCase("KILLED")) {
				MinecraftMob mob = MinecraftMob.getMinecraftMobType(killedEntity);
				uuid = UUID.fromString(MH_REWARD_KILLED_UUID);
				skinuuid = mob.getPlayerUUID();
				is = new CustomItems(plugin).getCustomHead(mob, mob.getFriendlyName(), 1, money, skinuuid);

			} else if (MobHunting.getConfigManager().dropMoneyOnGroundItemtype.equalsIgnoreCase("SKULL")) {
				uuid = UUID.fromString(MH_REWARD_BAG_OF_GOLD_UUID);
				skinuuid = uuid;
				is = new CustomItems(plugin).getCustomtexture(uuid,
						MobHunting.getConfigManager().dropMoneyOnGroundSkullRewardName.trim(),
						MobHunting.getConfigManager().dropMoneyOnGroundSkullTextureValue,
						MobHunting.getConfigManager().dropMoneyOnGroundSkullTextureSignature, money, UUID.randomUUID(),
						skinuuid);

			} else if (MobHunting.getConfigManager().dropMoneyOnGroundItemtype.equalsIgnoreCase("KILLER")) {
				uuid = UUID.fromString(MH_REWARD_KILLER_UUID);
				skinuuid = player.getUniqueId();
				is = new CustomItems(plugin).getPlayerHead(player.getUniqueId(), 1, money);

			} else { // ITEM
				uuid = UUID.fromString(MH_REWARD_ITEM_UUID);
				skinuuid = null;
				is = new ItemStack(Material.valueOf(MobHunting.getConfigManager().dropMoneyOnGroundItem), 1);
			}

			item = location.getWorld().dropItem(location, is);
			getDroppedMoney().put(item.getEntityId(), money);
			item.setMetadata(MH_REWARD_DATA,
					new FixedMetadataValue(plugin,
							new Reward(
									MobHunting.getConfigManager().dropMoneyOnGroundItemtype.equalsIgnoreCase("ITEM")
											? "" : Reward.getReward(is).getDisplayname(),
									money, uuid, UUID.randomUUID(), skinuuid)));
			if (Misc.isMC18OrNewer()) {
				item.setCustomName(ChatColor.valueOf(MobHunting.getConfigManager().dropMoneyOnGroundTextColor)
						+ (MobHunting.getConfigManager().dropMoneyOnGroundItemtype.equalsIgnoreCase("ITEM")
								? plugin.getRewardManager().format(money)
								: Reward.getReward(is).getDisplayname() + " (" + plugin.getRewardManager().format(money)
										+ ")"));
				item.setCustomNameVisible(true);
			}
		}
		if (item != null)
			Messages.debug("%s was dropped on the ground as item %s (# of rewards=%s)", format(money),
					MobHunting.getConfigManager().dropMoneyOnGroundItemtype, droppedMoney.size());
	}

	public void saveReward(UUID uuid) {
		try {
			config.options().header("This is the rewards placed as blocks. Do not edit this file manually!");
			if (placedMoney_Reward.containsKey(uuid)) {
				Location location = placedMoney_Location.get(uuid);
				if (location != null && location.getBlock().getType() == Material.SKULL) {
					Reward reward = placedMoney_Reward.get(uuid);
					ConfigurationSection section = config.createSection(uuid.toString());
					section.set("location", location);
					reward.save(section);
					Messages.debug("Saving a reward placed as a block.");
					config.save(file);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void loadAllStoredRewards() {
		int n = 0;
		int deleted = 0;
		try {

			if (!file.exists())
				return;

			config.load(file);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}

		try {
			for (String key : config.getKeys(false)) {
				ConfigurationSection section = config.getConfigurationSection(key);
				Reward reward = new Reward();
				reward.read(section);
				Location location = (Location) section.get("location");
				if (location != null && location.getBlock().getType() == Material.SKULL) {
					location.getBlock().setMetadata(MH_REWARD_DATA,
							new FixedMetadataValue(MobHunting.getInstance(), new Reward(reward)));
					placedMoney_Reward.put(UUID.fromString(key), reward);
					placedMoney_Location.put(UUID.fromString(key), location);
					n++;
				} else {
					deleted++;
					config.set(key, null);
				}
			}
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}

		try {

			if (deleted > 0) {
				Messages.debug("Deleted %s rewards from the rewards.yml file", deleted);
				File file_copy = new File(MobHunting.getInstance().getDataFolder(), "rewards.yml.old");
				Files.copy(file.toPath(), file_copy.toPath(), StandardCopyOption.COPY_ATTRIBUTES,
						StandardCopyOption.REPLACE_EXISTING);
				config.save(file);
			}
			if (n > 0) {
				Messages.debug("Loaded %s rewards from the rewards.yml file", n);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ItemStack setDisplayNameAndHiddenLores(ItemStack skull, String mDisplayName, double money, UUID uuid,
			UUID skinuuid) {
		ItemMeta skullMeta = skull.getItemMeta();
		skullMeta.setLore(new ArrayList<String>(Arrays.asList("Hidden:" + mDisplayName, "Hidden:" + money,
				"Hidden:" + uuid, money == 0 ? "Hidden:" : "Hidden:" + UUID.randomUUID(), "Hidden:" + skinuuid)));
		if (money == 0)
			skullMeta.setDisplayName(
					ChatColor.valueOf(MobHunting.getConfigManager().dropMoneyOnGroundTextColor) + mDisplayName);
		else
			skullMeta.setDisplayName(ChatColor.valueOf(MobHunting.getConfigManager().dropMoneyOnGroundTextColor)
					+ (MobHunting.getConfigManager().dropMoneyOnGroundItemtype.equalsIgnoreCase("ITEM") ? format(money)
							: mDisplayName + " (" + format(money) + ")"));
		skull.setItemMeta(skullMeta);
		return skull;
	}

	public double adjustBagOfGoldInPlayerInventory(Player player, double amount) {
		double taken = 0;
		double toBeTaken = -Misc.floor(amount);
		CustomItems customItems = new CustomItems(plugin);
		for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
			ItemStack is = player.getInventory().getItem(slot);
			if (Reward.isReward(is)) {
				Reward reward = Reward.getReward(is);
				double saldo = Misc.floor(reward.getMoney());
				if (saldo > toBeTaken) {
					reward.setMoney(saldo - toBeTaken);
					is = customItems.getCustomtexture(reward.getRewardUUID(),
							MobHunting.getConfigManager().dropMoneyOnGroundSkullRewardName.trim(),
							MobHunting.getConfigManager().dropMoneyOnGroundSkullTextureValue,
							MobHunting.getConfigManager().dropMoneyOnGroundSkullTextureSignature, saldo - toBeTaken,
							UUID.randomUUID(), reward.getSkinUUID());
					player.getInventory().setItem(slot, is);
					taken = taken + toBeTaken;
					toBeTaken = 0;
					break;
				} else {
					is.setItemMeta(null);
					is.setType(Material.AIR);
					is.setAmount(0);
					player.getInventory().setItem(slot, is);
					taken = taken + saldo;
					toBeTaken = toBeTaken - saldo;
				}
			}
		}
		return taken;
	}

	public double getPlayerKilledByMobPenalty(Player playerToBeRobbed) {
		if (MobHunting.getConfigManager().mobKillsPlayerPenalty == null
				|| MobHunting.getConfigManager().mobKillsPlayerPenalty.equals("")
				|| MobHunting.getConfigManager().mobKillsPlayerPenalty.equals("0%")
				|| MobHunting.getConfigManager().mobKillsPlayerPenalty.equals("0")
				|| MobHunting.getConfigManager().mobKillsPlayerPenalty.isEmpty()) {
			return 0;
		} else if (MobHunting.getConfigManager().mobKillsPlayerPenalty.contains(":")) {
			String[] str1 = MobHunting.getConfigManager().mobKillsPlayerPenalty.split(":");
			double prize = (MobHunting.getMobHuntingManager().mRand.nextDouble()
					* (Double.valueOf(str1[1]) - Double.valueOf(str1[0])) + Double.valueOf(str1[0]));
			return Misc.round(prize);
		} else if (MobHunting.getConfigManager().mobKillsPlayerPenalty.endsWith("%")) {
			double prize = Math.floor(Double
					.valueOf(MobHunting.getConfigManager().mobKillsPlayerPenalty.substring(0,
							MobHunting.getConfigManager().mobKillsPlayerPenalty.length() - 1))
					* plugin.getRewardManager().getBalance(playerToBeRobbed) / 100);
			return Misc.round(prize);
		} else if (MobHunting.getConfigManager().mobKillsPlayerPenalty.contains(":")) {
			String[] str1 = MobHunting.getConfigManager().mobKillsPlayerPenalty.split(":");
			double prize2 = (MobHunting.getMobHuntingManager().mRand.nextDouble()
					* (Double.valueOf(str1[1]) - Double.valueOf(str1[0])) + Double.valueOf(str1[0]));
			return Misc.round(Double.valueOf(prize2));
		} else
			return Double.valueOf(MobHunting.getConfigManager().mobKillsPlayerPenalty);
	}

	public double getRandomPrice(String str) {
		if (str == null || str.equals("") || str.isEmpty()) {
			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[MobHunting] [WARNING]" + ChatColor.RESET
					+ " The random_bounty_prize is not set in config.yml. Please set the prize to 0 or a positive number.");
			return 0;
		} else if (str.contains(":")) {
			String[] str1 = str.split(":");
			double prize = (MobHunting.getMobHuntingManager().mRand.nextDouble()
					* (Double.valueOf(str1[1]) - Double.valueOf(str1[0])) + Double.valueOf(str1[0]));
			return Misc.round(prize);
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
			Messages.debug("TARDISWeepingAngel %s has no reward data",
					TARDISWeepingAngelsCompat.getWeepingAngelMonsterType(mob).getName());
			return 0;

		} else if (MythicMobsCompat.isMythicMob(mob)) {
			if (MythicMobsCompat.getMobRewardData().containsKey(MythicMobsCompat.getMythicMobType(mob)))
				return getPrice(mob, MythicMobsCompat.getMobRewardData().get(MythicMobsCompat.getMythicMobType(mob))
						.getRewardPrize());
			Messages.debug("MythicMob %s has no reward data", MythicMobsCompat.getMythicMobType(mob));
			return 0;

		} else if (CitizensCompat.isSentryOrSentinelOrSentries(mob)) {
			NPC npc = CitizensAPI.getNPCRegistry().getNPC(mob);
			String key = String.valueOf(npc.getId());
			if (CitizensCompat.getMobRewardData().containsKey(key)) {
				return getPrice(mob, CitizensCompat.getMobRewardData().get(key).getRewardPrize());
			}
			Messages.debug("Citizens mob %s has no reward data", npc.getName());
			return 0;

		} else if (CustomMobsCompat.isCustomMob(mob)) {
			if (CustomMobsCompat.getMobRewardData().containsKey(CustomMobsCompat.getCustomMobType(mob)))
				return getPrice(mob, CustomMobsCompat.getMobRewardData().get(CustomMobsCompat.getCustomMobType(mob))
						.getRewardPrize());
			Messages.debug("CustomMob %s has no reward data", CustomMobsCompat.getCustomMobType(mob));
			return 0;

		} else if (MysteriousHalloweenCompat.isMysteriousHalloween(mob)) {
			if (MysteriousHalloweenCompat.getMobRewardData()
					.containsKey(MysteriousHalloweenCompat.getMysteriousHalloweenType(mob).name()))
				return getPrice(mob, MysteriousHalloweenCompat.getMobRewardData()
						.get(MysteriousHalloweenCompat.getMysteriousHalloweenType(mob).name()).getRewardPrize());
			Messages.debug("MysteriousHalloween %s has no reward data",
					MysteriousHalloweenCompat.getMysteriousHalloweenType(mob).name());
			return 0;

		} else if (SmartGiantsCompat.isSmartGiants(mob)) {
			if (SmartGiantsCompat.getMobRewardData().containsKey(SmartGiantsCompat.getSmartGiantsMobType(mob)))
				return getPrice(mob, SmartGiantsCompat.getMobRewardData()
						.get(SmartGiantsCompat.getSmartGiantsMobType(mob)).getRewardPrize());
			Messages.debug("SmartGiantsS %s has no reward data", SmartGiantsCompat.getSmartGiantsMobType(mob));
			return 0;

		} else if (MyPetCompat.isMyPet(mob)) {
			Messages.debug("Tried to find a prize for a MyPet: %s (Owner=%s)", MyPetCompat.getMyPet(mob),
					MyPetCompat.getMyPetOwner(mob));
			return getPrice(mob, MobHunting.getConfigManager().wolfPrize);

		} else if (HerobrineCompat.isHerobrineMob(mob)) {
			if (HerobrineCompat.getMobRewardData().containsKey(HerobrineCompat.getHerobrineMobType(mob)))
				return getPrice(mob, HerobrineCompat.getMobRewardData().get(HerobrineCompat.getHerobrineMobType(mob))
						.getRewardPrize());
			Messages.debug("Herobrine mob %s has no reward data", HerobrineCompat.getHerobrineMobType(mob));
			return 0;

		} else {
			if (Misc.isMC112OrNewer())
				if (mob instanceof Parrot)
					return getPrice(mob, MobHunting.getConfigManager().parrotPrize);
				else if (mob instanceof Illusioner)
					return getPrice(mob, MobHunting.getConfigManager().illusionerPrize);

			if (Misc.isMC111OrNewer())
				if (mob instanceof Llama)
					return getPrice(mob, MobHunting.getConfigManager().llamaPrize);
				else if (mob instanceof Vex)
					return getPrice(mob, MobHunting.getConfigManager().vexPrize);
				else if (mob instanceof Vindicator)
					return getPrice(mob, MobHunting.getConfigManager().vindicatorPrize);
				else if (mob instanceof Evoker)
					return getPrice(mob, MobHunting.getConfigManager().evokerPrize);
				else if (mob instanceof Donkey)
					return getPrice(mob, MobHunting.getConfigManager().donkeyPrize);
				else if (mob instanceof Mule)
					return getPrice(mob, MobHunting.getConfigManager().mulePrize);
				else if (mob instanceof SkeletonHorse)
					return getPrice(mob, MobHunting.getConfigManager().skeletonhorsePrize);
				else if (mob instanceof ZombieHorse)
					return getPrice(mob, MobHunting.getConfigManager().zombiehorsePrize);
				else if (mob instanceof Stray)
					return getPrice(mob, MobHunting.getConfigManager().strayPrize);
				else if (mob instanceof Husk)
					return getPrice(mob, MobHunting.getConfigManager().huskPrize);
				else if (mob instanceof ZombieVillager)
					return getPrice(mob, MobHunting.getConfigManager().zombieVillagerPrize);
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.NITWIT)
					return getPrice(mob, MobHunting.getConfigManager().nitwitPrize);

			if (Misc.isMC110OrNewer())
				if (mob instanceof PolarBear)
					return getPrice(mob, MobHunting.getConfigManager().polarBearPrize);
				else if (mob instanceof Skeleton && ((Skeleton) mob).getSkeletonType() == SkeletonType.STRAY)
					return getPrice(mob, MobHunting.getConfigManager().strayPrize);
				else if (mob instanceof Zombie && ((Zombie) mob).getVillagerProfession() == Profession.HUSK)
					return getPrice(mob, MobHunting.getConfigManager().huskPrize);
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.NORMAL)
					return getPrice(mob, MobHunting.getConfigManager().villagerPrize);
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.PRIEST)
					return getPrice(mob, MobHunting.getConfigManager().priestPrize);
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.BUTCHER)
					return getPrice(mob, MobHunting.getConfigManager().butcherPrize);
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.BLACKSMITH)
					return getPrice(mob, MobHunting.getConfigManager().blacksmithPrize);
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.LIBRARIAN)
					return getPrice(mob, MobHunting.getConfigManager().librarianPrize);
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.FARMER)
					return getPrice(mob, MobHunting.getConfigManager().farmerPrize);

			if (Misc.isMC19OrNewer())
				if (mob instanceof Shulker)
					return getPrice(mob, MobHunting.getConfigManager().shulkerPrize);

			if (Misc.isMC18OrNewer())
				if (mob instanceof Guardian && ((Guardian) mob).isElder())
					return getPrice(mob, MobHunting.getConfigManager().elderGuardianPrize);
				else if (mob instanceof Guardian)
					return getPrice(mob, MobHunting.getConfigManager().guardianPrize);
				else if (mob instanceof Endermite)
					return getPrice(mob, MobHunting.getConfigManager().endermitePrize);
				else if (mob instanceof Rabbit)
					if (((Rabbit) mob).getRabbitType() == Rabbit.Type.THE_KILLER_BUNNY)
						return getPrice(mob, MobHunting.getConfigManager().killerrabbitPrize);
					else
						return getPrice(mob, MobHunting.getConfigManager().rabbitPrize);

			// Minecraft 1.7.10 and older entities
			if (mob instanceof Player) {
				if (MobHunting.getConfigManager().pvpKillPrize.endsWith("%")) {
					double prize = Math.floor(Double
							.valueOf(MobHunting.getConfigManager().pvpKillPrize.substring(0,
									MobHunting.getConfigManager().pvpKillPrize.length() - 1))
							* plugin.getRewardManager().getBalance((Player) mob) / 100);
					return Misc.round(prize);
				} else if (MobHunting.getConfigManager().pvpKillPrize.contains(":")) {
					String[] str1 = MobHunting.getConfigManager().pvpKillPrize.split(":");
					double prize2 = (MobHunting.getMobHuntingManager().mRand.nextDouble()
							* (Double.valueOf(str1[1]) - Double.valueOf(str1[0])) + Double.valueOf(str1[0]));
					return Misc.round(Double.valueOf(prize2));
				} else
					return Double.valueOf(MobHunting.getConfigManager().pvpKillPrize);
			} else if (mob instanceof Blaze)
				return getPrice(mob, MobHunting.getConfigManager().blazePrize);
			else if (mob instanceof Creeper)
				return getPrice(mob, MobHunting.getConfigManager().creeperPrize);
			else if (mob instanceof Silverfish)
				return getPrice(mob, MobHunting.getConfigManager().silverfishPrize);
			else if (mob instanceof Enderman)
				return getPrice(mob, MobHunting.getConfigManager().endermanPrize);
			else if (mob instanceof Giant)
				return getPrice(mob, MobHunting.getConfigManager().giantPrize);
			else if (mob instanceof Skeleton && ((Skeleton) mob).getSkeletonType() == SkeletonType.NORMAL)
				return getPrice(mob, MobHunting.getConfigManager().skeletonPrize);
			else if (mob instanceof Skeleton && ((Skeleton) mob).getSkeletonType() == SkeletonType.WITHER)
				return getPrice(mob, MobHunting.getConfigManager().witherSkeletonPrize);
			else if (mob instanceof CaveSpider)
				return getPrice(mob, MobHunting.getConfigManager().caveSpiderPrize);
			else if (mob instanceof Spider)
				return getPrice(mob, MobHunting.getConfigManager().spiderPrize);
			else if (mob instanceof Witch)
				return getPrice(mob, MobHunting.getConfigManager().witchPrize);
			else if (mob instanceof PigZombie)
				// PigZombie is a subclass of Zombie.
				if (((PigZombie) mob).isBaby())
					return Misc.round(getPrice(mob, MobHunting.getConfigManager().zombiePigmanPrize)
							* MobHunting.getConfigManager().babyMultiplier);
				else
					return getPrice(mob, MobHunting.getConfigManager().zombiePigmanPrize);
			else if (mob instanceof Zombie)
				if (((Zombie) mob).isBaby())
					return Misc.round(getPrice(mob, MobHunting.getConfigManager().zombiePrize)
							* MobHunting.getConfigManager().babyMultiplier);
				else
					return getPrice(mob, MobHunting.getConfigManager().zombiePrize);
			else if (mob instanceof Ghast)
				return getPrice(mob, MobHunting.getConfigManager().ghastPrize);
			else if (mob instanceof MagmaCube)
				// MagmaCube is a subclass of Slime
				return getPrice(mob, MobHunting.getConfigManager().magmaCubePrize) * ((MagmaCube) mob).getSize();
			else if (mob instanceof Slime)
				return getPrice(mob, MobHunting.getConfigManager().slimeTinyPrize) * ((Slime) mob).getSize();
			else if (mob instanceof EnderDragon)
				return getPrice(mob, MobHunting.getConfigManager().enderdragonPrize);
			else if (mob instanceof Wither)
				return getPrice(mob, MobHunting.getConfigManager().witherPrize);
			else if (mob instanceof IronGolem)
				return getPrice(mob, MobHunting.getConfigManager().ironGolemPrize);

			// Passive mobs
			else if (mob instanceof Bat)
				return getPrice(mob, MobHunting.getConfigManager().batPrize);
			else if (mob instanceof Chicken)
				return getPrice(mob, MobHunting.getConfigManager().chickenPrize);
			else if (mob instanceof Cow)
				if (mob instanceof MushroomCow)
					// MushroomCow is a subclass of Cow
					return getPrice(mob, MobHunting.getConfigManager().mushroomCowPrize);
				else
					return getPrice(mob, MobHunting.getConfigManager().cowPrize);
			else if (mob instanceof Horse)
				return getPrice(mob, MobHunting.getConfigManager().horsePrize);
			else if (mob instanceof Ocelot)
				return getPrice(mob, MobHunting.getConfigManager().ocelotPrize);
			else if (mob instanceof Pig)
				return getPrice(mob, MobHunting.getConfigManager().pigPrize);
			else if (mob instanceof Sheep)
				return getPrice(mob, MobHunting.getConfigManager().sheepPrize);
			else if (mob instanceof Snowman)
				return getPrice(mob, MobHunting.getConfigManager().snowmanPrize);
			else if (mob instanceof Squid)
				return getPrice(mob, MobHunting.getConfigManager().squidPrize);
			else if (mob instanceof Villager)
				return getPrice(mob, MobHunting.getConfigManager().villagerPrize);
			else if (mob instanceof Wolf) {
				return getPrice(mob, MobHunting.getConfigManager().wolfPrize);
			} else if (mob instanceof Item && ((Item) mob).getItemStack().getType() == Material.RAW_FISH) {
				ItemStack is = ((Item) mob).getItemStack();
				if (is.getData().getData() == (byte) 0) {
					return getPrice(mob, MobHunting.getConfigManager().rawFishPrize);
				} else if (is.getData().getData() == (byte) 1) {
					return getPrice(mob, MobHunting.getConfigManager().rawSalmonPrize);
				} else if (is.getData().getData() == (byte) 2) {
					return getPrice(mob, MobHunting.getConfigManager().clownfishPrize);
				} else if (is.getData().getData() == (byte) 3) {
					return getPrice(mob, MobHunting.getConfigManager().pufferfishPrize);
				}
			}
		}
		// Messages.debug("Mobhunting could not find the prize for killing this
		// mob: %s (%s)",
		// ExtendedMobManager.getMobName(mob), mob.getType());
		return 0;
	}

	private double getPrice(Entity mob, String str) {
		if (str == null || str.equals("") || str.isEmpty()) {
			Bukkit.getServer().getConsoleSender()
					.sendMessage(ChatColor.RED + "[MobHunting] [WARNING]" + ChatColor.RESET
							+ " The prize for killing a " + ExtendedMobManager.getMobName(mob)
							+ " is not set in config.yml. Please set the prize to 0 or a positive or negative number.");
			return 0;
		} else if (str.startsWith(":")) {
			Bukkit.getServer().getConsoleSender()
					.sendMessage(ChatColor.RED + "[MobHunting] [WARNING]" + ChatColor.RESET
							+ " The prize for killing a " + ExtendedMobManager.getMobName(mob)
							+ " in config.yml has a wrong format. The prize can't start with \":\"");
			if (str.length() > 1)
				return getPrice(mob, str.substring(1, str.length()));
			else
				return 0;
		} else if (str.contains(":")) {
			String[] str1 = str.split(":");
			double prize = (MobHunting.getMobHuntingManager().mRand.nextDouble()
					* (Double.valueOf(str1[1]) - Double.valueOf(str1[0])) + Double.valueOf(str1[0]));
			return Misc.round(prize);
		} else
			return Double.valueOf(str);
	}

	/**
	 * Get the command to be run when the player kills a Mob.
	 * 
	 * @param mob
	 * @return a number of commands to be run in the console. Each command must
	 *         be separeted by a "|"
	 */
	public String getKillConsoleCmd(Entity mob) {
		if (TARDISWeepingAngelsCompat.isWeepingAngelMonster(mob)) {
			if (TARDISWeepingAngelsCompat.getMobRewardData()
					.containsKey(TARDISWeepingAngelsCompat.getWeepingAngelMonsterType(mob).name()))
				return TARDISWeepingAngelsCompat.getMobRewardData()
						.get(TARDISWeepingAngelsCompat.getWeepingAngelMonsterType(mob).name()).getConsoleRunCommand();
			return "";

		} else if (MythicMobsCompat.isMythicMob(mob)) {
			if (MythicMobsCompat.getMobRewardData().containsKey(MythicMobsCompat.getMythicMobType(mob)))
				return MythicMobsCompat.getMobRewardData().get(MythicMobsCompat.getMythicMobType(mob))
						.getConsoleRunCommand();
			return "";

		} else if (CitizensCompat.isNPC(mob) && CitizensCompat.isSentryOrSentinelOrSentries(mob)) {
			NPC npc = CitizensAPI.getNPCRegistry().getNPC(mob);
			String key = String.valueOf(npc.getId());
			if (CitizensCompat.getMobRewardData().containsKey(key)) {
				return CitizensCompat.getMobRewardData().get(key).getConsoleRunCommand();
			}
			return "";

		} else if (CustomMobsCompat.isCustomMob(mob)) {
			if (mob.hasMetadata(CustomMobsCompat.MH_CUSTOMMOBS)) {
				List<MetadataValue> data = mob.getMetadata(CustomMobsCompat.MH_CUSTOMMOBS);
				for (MetadataValue value : data)
					if (value.value() instanceof RewardData)
						return ((RewardData) value.value()).getConsoleRunCommand();
			} else if (CustomMobsCompat.getMobRewardData().containsKey(CustomMobsCompat.getCustomMobType(mob)))
				return CustomMobsCompat.getMobRewardData().get(CustomMobsCompat.getCustomMobType(mob))
						.getConsoleRunCommand();
			return "";

		} else if (MysteriousHalloweenCompat.isMysteriousHalloween(mob)) {
			if (MysteriousHalloweenCompat.getMobRewardData()
					.containsKey(MysteriousHalloweenCompat.getMysteriousHalloweenType(mob).name()))
				return MysteriousHalloweenCompat.getMobRewardData()
						.get(MysteriousHalloweenCompat.getMysteriousHalloweenType(mob).name()).getConsoleRunCommand();
			return "";

		} else if (SmartGiantsCompat.isSmartGiants(mob)) {
			if (SmartGiantsCompat.getMobRewardData().containsKey(SmartGiantsCompat.getSmartGiantsMobType(mob)))
				return SmartGiantsCompat.getMobRewardData().get(SmartGiantsCompat.getSmartGiantsMobType(mob))
						.getConsoleRunCommand();
			return "";

		} else if (HerobrineCompat.isHerobrineMob(mob)) {
			if (HerobrineCompat.getMobRewardData().containsKey(HerobrineCompat.getHerobrineMobType(mob)))
				return HerobrineCompat.getMobRewardData().get(HerobrineCompat.getHerobrineMobType(mob))
						.getConsoleRunCommand();
			return "";

		} else if (MyPetCompat.isMyPet(mob)) {
			return MobHunting.getConfigManager().wolfCmd;

		} else {
			if (Misc.isMC112OrNewer())
				if (mob instanceof Parrot)
					return MobHunting.getConfigManager().parrotCmd;
				else if (mob instanceof Illusioner)
					return MobHunting.getConfigManager().illusionerCmd;

			if (Misc.isMC111OrNewer())
				if (mob instanceof Llama)
					return MobHunting.getConfigManager().llamaCmd;
				else if (mob instanceof Vex)
					return MobHunting.getConfigManager().vexCmd;
				else if (mob instanceof Vindicator)
					return MobHunting.getConfigManager().vindicatorCmd;
				else if (mob instanceof Evoker)
					return MobHunting.getConfigManager().evokerCmd;
				else if (mob instanceof Donkey)
					return MobHunting.getConfigManager().donkeyCmd;
				else if (mob instanceof Mule)
					return MobHunting.getConfigManager().muleCmd;
				else if (mob instanceof SkeletonHorse)
					return MobHunting.getConfigManager().skeletonhorseCmd;
				else if (mob instanceof ZombieHorse)
					return MobHunting.getConfigManager().zombiehorseCmd;
				else if (mob instanceof Stray)
					return MobHunting.getConfigManager().strayCmd;
				else if (mob instanceof Husk)
					return MobHunting.getConfigManager().huskCmd;
				else if (mob instanceof ZombieVillager)
					return MobHunting.getConfigManager().zombieVillagerCmd;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.NITWIT)
					return MobHunting.getConfigManager().nitwitCmd;

			if (Misc.isMC110OrNewer())
				if (mob instanceof PolarBear)
					return MobHunting.getConfigManager().polarBearCmd;
				else if (mob instanceof Skeleton && ((Skeleton) mob).getSkeletonType() == SkeletonType.STRAY)
					return MobHunting.getConfigManager().strayCmd;
				else if (mob instanceof Zombie && ((Zombie) mob).getVillagerProfession() == Profession.HUSK)
					return MobHunting.getConfigManager().huskCmd;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.NORMAL)
					return MobHunting.getConfigManager().villagerCmd;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.PRIEST)
					return MobHunting.getConfigManager().priestCmd;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.BUTCHER)
					return MobHunting.getConfigManager().butcherCmd;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.BLACKSMITH)
					return MobHunting.getConfigManager().blacksmithCmd;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.LIBRARIAN)
					return MobHunting.getConfigManager().librarianCmd;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.FARMER)
					return MobHunting.getConfigManager().farmerCmd;

			if (Misc.isMC19OrNewer())
				if (mob instanceof Shulker)
					return MobHunting.getConfigManager().shulkerCmd;

			if (Misc.isMC18OrNewer())
				if (mob instanceof Guardian && ((Guardian) mob).isElder())
					return MobHunting.getConfigManager().elderGuardianCmd;
				else if (mob instanceof Guardian)
					return MobHunting.getConfigManager().guardianCmd;
				else if (mob instanceof Endermite)
					return MobHunting.getConfigManager().endermiteCmd;
				else if (mob instanceof Rabbit)
					if ((((Rabbit) mob).getRabbitType()) == Rabbit.Type.THE_KILLER_BUNNY)
						return MobHunting.getConfigManager().killerrabbitCmd;
					else
						return MobHunting.getConfigManager().rabbitCmd;

			if (mob instanceof Player)
				return MobHunting.getConfigManager().pvpKillCmd;
			else if (mob instanceof Blaze)
				return MobHunting.getConfigManager().blazeCmd;
			else if (mob instanceof Creeper)
				return MobHunting.getConfigManager().creeperCmd;
			else if (mob instanceof Silverfish)
				return MobHunting.getConfigManager().silverfishCmd;
			else if (mob instanceof Enderman)
				return MobHunting.getConfigManager().endermanCmd;
			else if (mob instanceof Giant)
				return MobHunting.getConfigManager().giantCmd;
			else if (mob instanceof Skeleton && ((Skeleton) mob).getSkeletonType() == SkeletonType.NORMAL)
				return MobHunting.getConfigManager().skeletonCmd;
			else if (mob instanceof Skeleton && ((Skeleton) mob).getSkeletonType() == SkeletonType.WITHER)
				return MobHunting.getConfigManager().witherSkeletonCmd;
			else if (mob instanceof Spider)
				if (mob instanceof CaveSpider)
					// CaveSpider is a sub class of Spider
					return MobHunting.getConfigManager().caveSpiderCmd;
				else
					return MobHunting.getConfigManager().spiderCmd;
			else if (mob instanceof Witch)
				return MobHunting.getConfigManager().witchCmd;
			else if (mob instanceof Zombie)
				if (mob instanceof PigZombie)
					return MobHunting.getConfigManager().zombiePigmanCmd;
				else
					return MobHunting.getConfigManager().zombieCmd;
			else if (mob instanceof Ghast)
				return MobHunting.getConfigManager().ghastCmd;
			else if (mob instanceof MagmaCube)
				// Magmacube is an instance of slime and must be checked before
				// the Slime itself
				return MobHunting.getConfigManager().magmaCubeCmd;
			else if (mob instanceof Slime)
				return MobHunting.getConfigManager().slimeCmd;
			else if (mob instanceof EnderDragon)
				return MobHunting.getConfigManager().enderdragonCmd;
			else if (mob instanceof Wither)
				return MobHunting.getConfigManager().witherCmd;
			else if (mob instanceof IronGolem)
				return MobHunting.getConfigManager().ironGolemCmd;

			// Passive mobs
			else if (mob instanceof Bat)
				return MobHunting.getConfigManager().batCmd;
			else if (mob instanceof Chicken)
				return MobHunting.getConfigManager().chickenCmd;

			else if (mob instanceof Cow)
				if (mob instanceof MushroomCow)
					return MobHunting.getConfigManager().mushroomCowCmd;
				else
					return MobHunting.getConfigManager().cowCmd;
			else if (mob instanceof Horse)
				return MobHunting.getConfigManager().horseCmd;
			else if (mob instanceof Ocelot)
				return MobHunting.getConfigManager().ocelotCmd;
			else if (mob instanceof Pig)
				return MobHunting.getConfigManager().pigCmd;
			else if (mob instanceof Sheep)
				return MobHunting.getConfigManager().sheepCmd;
			else if (mob instanceof Snowman)
				return MobHunting.getConfigManager().snowmanCmd;
			else if (mob instanceof Squid)
				return MobHunting.getConfigManager().squidCmd;
			else if (mob instanceof Villager)
				return MobHunting.getConfigManager().villagerCmd;
			else if (mob instanceof Wolf)
				return MobHunting.getConfigManager().wolfCmd;
			else if (mob instanceof Item && ((Item) mob).getItemStack().getType() == Material.RAW_FISH) {
				ItemStack is = ((Item) mob).getItemStack();
				if (is.getData().getData() == (byte) 0) {
					return MobHunting.getConfigManager().rawFishCmd;
				} else if (is.getData().getData() == (byte) 1) {
					return MobHunting.getConfigManager().rawSalmonCmd;
				} else if (is.getData().getData() == (byte) 2) {
					return MobHunting.getConfigManager().clownfishCmd;
				} else if (is.getData().getData() == (byte) 3) {
					return MobHunting.getConfigManager().pufferfishCmd;
				}
			}

		}
		return "";
	}

	/**
	 * Get the text to be send to the player describing the reward
	 * 
	 * @param mob
	 * @return String
	 */
	public String getKillRewardDescription(Entity mob) {
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

		} else if (MyPetCompat.isMyPet(mob)) {
			return MobHunting.getConfigManager().wolfCmdDesc;

		} else {
			if (Misc.isMC112OrNewer())
				if (mob instanceof Parrot)
					return MobHunting.getConfigManager().parrotCmdDesc;
				else if (mob instanceof Illusioner)
					return MobHunting.getConfigManager().illusionerCmdDesc;

			if (Misc.isMC111OrNewer())
				if (mob instanceof Llama)
					return MobHunting.getConfigManager().llamaCmdDesc;
				else if (mob instanceof Vex)
					return MobHunting.getConfigManager().vexCmdDesc;
				else if (mob instanceof Vindicator)
					return MobHunting.getConfigManager().vindicatorCmdDesc;
				else if (mob instanceof Evoker)
					return MobHunting.getConfigManager().evokerCmdDesc;
				else if (mob instanceof Donkey)
					return MobHunting.getConfigManager().donkeyCmdDesc;
				else if (mob instanceof Mule)
					return MobHunting.getConfigManager().muleCmdDesc;
				else if (mob instanceof SkeletonHorse)
					return MobHunting.getConfigManager().skeletonhorseCmdDesc;
				else if (mob instanceof ZombieHorse)
					return MobHunting.getConfigManager().zombiehorseCmdDesc;
				else if (mob instanceof Stray)
					return MobHunting.getConfigManager().strayCmdDesc;
				else if (mob instanceof Husk)
					return MobHunting.getConfigManager().huskCmdDesc;
				else if (mob instanceof ZombieVillager)
					return MobHunting.getConfigManager().zombieVillagerCmdDesc;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.NITWIT)
					return MobHunting.getConfigManager().nitwitCmdDesc;

			if (Misc.isMC110OrNewer())
				if (mob instanceof PolarBear)
					return MobHunting.getConfigManager().polarBearCmdDesc;
				else if (mob instanceof Skeleton && ((Skeleton) mob).getSkeletonType() == SkeletonType.STRAY)
					return MobHunting.getConfigManager().strayCmdDesc;
				else if (mob instanceof Zombie && ((Zombie) mob).getVillagerProfession() == Profession.HUSK)
					return MobHunting.getConfigManager().huskCmdDesc;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.NORMAL)
					return MobHunting.getConfigManager().villagerCmdDesc;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.PRIEST)
					return MobHunting.getConfigManager().priestCmdDesc;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.BUTCHER)
					return MobHunting.getConfigManager().butcherCmdDesc;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.BLACKSMITH)
					return MobHunting.getConfigManager().blacksmithCmdDesc;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.LIBRARIAN)
					return MobHunting.getConfigManager().librarianCmdDesc;
				else if (mob instanceof Villager && ((Villager) mob).getProfession() == Profession.FARMER)
					return MobHunting.getConfigManager().farmerCmdDesc;

			if (Misc.isMC19OrNewer())
				if (mob instanceof Shulker)
					return MobHunting.getConfigManager().shulkerCmdDesc;

			if (Misc.isMC18OrNewer())
				if (mob instanceof Guardian && ((Guardian) mob).isElder())
					return MobHunting.getConfigManager().elderGuardianCmdDesc;
				else if (mob instanceof Guardian)
					return MobHunting.getConfigManager().guardianCmdDesc;
				else if (mob instanceof Endermite)
					return MobHunting.getConfigManager().endermiteCmdDesc;
				else if (mob instanceof Rabbit)
					if ((((Rabbit) mob).getRabbitType()) == Rabbit.Type.THE_KILLER_BUNNY)
						return MobHunting.getConfigManager().killerrabbitCmdDesc;
					else
						return MobHunting.getConfigManager().rabbitCmdDesc;

			// MC1.7 or older
			if (mob instanceof Player)
				return MobHunting.getConfigManager().pvpKillCmdDesc;
			else if (mob instanceof Blaze)
				return MobHunting.getConfigManager().blazeCmdDesc;
			else if (mob instanceof Creeper)
				return MobHunting.getConfigManager().creeperCmdDesc;
			else if (mob instanceof Silverfish)
				return MobHunting.getConfigManager().silverfishCmdDesc;
			else if (mob instanceof Enderman)
				return MobHunting.getConfigManager().endermanCmdDesc;
			else if (mob instanceof Giant)
				return MobHunting.getConfigManager().giantCmdDesc;
			else if (mob instanceof Skeleton && ((Skeleton) mob).getSkeletonType() == SkeletonType.NORMAL)
				return MobHunting.getConfigManager().skeletonCmdDesc;
			else if (mob instanceof Skeleton && ((Skeleton) mob).getSkeletonType() == SkeletonType.WITHER)
				return MobHunting.getConfigManager().witherSkeletonCmdDesc;
			else if (mob instanceof CaveSpider)
				// CaveSpider is a Subclass of Spider
				return MobHunting.getConfigManager().caveSpiderCmdDesc;
			else if (mob instanceof Spider)
				return MobHunting.getConfigManager().spiderCmdDesc;
			else if (mob instanceof Witch)
				return MobHunting.getConfigManager().witchCmdDesc;
			else if (mob instanceof PigZombie)
				// PigZombie is a subclass of Zombie
				return MobHunting.getConfigManager().zombiePigmanCmdDesc;
			else if (mob instanceof Zombie)
				return MobHunting.getConfigManager().zombieCmdDesc;
			else if (mob instanceof Ghast)
				return MobHunting.getConfigManager().ghastCmdDesc;
			else if (mob instanceof MagmaCube)
				// MagmaCube is a subclass of Slime
				return MobHunting.getConfigManager().magmaCubeCmdDesc;
			else if (mob instanceof Slime)
				return MobHunting.getConfigManager().slimeCmdDesc;
			else if (mob instanceof EnderDragon)
				return MobHunting.getConfigManager().enderdragonCmdDesc;
			else if (mob instanceof Wither)
				return MobHunting.getConfigManager().witherCmdDesc;
			else if (mob instanceof IronGolem)
				return MobHunting.getConfigManager().ironGolemCmdDesc;

			// Passive mobs
			else if (mob instanceof Bat)
				return MobHunting.getConfigManager().batCmdDesc;
			else if (mob instanceof Chicken)
				return MobHunting.getConfigManager().chickenCmdDesc;
			else if (mob instanceof Cow)
				if (mob instanceof MushroomCow)
					// MushroomCow is a subclass of Cow
					return MobHunting.getConfigManager().mushroomCowCmdDesc;
				else
					return MobHunting.getConfigManager().cowCmdDesc;
			else if (mob instanceof Horse)
				return MobHunting.getConfigManager().horseCmdDesc;
			else if (mob instanceof Ocelot)
				return MobHunting.getConfigManager().ocelotCmdDesc;
			else if (mob instanceof Pig)
				return MobHunting.getConfigManager().pigCmdDesc;
			else if (mob instanceof Sheep)
				return MobHunting.getConfigManager().sheepCmdDesc;
			else if (mob instanceof Snowman)
				return MobHunting.getConfigManager().snowmanCmdDesc;
			else if (mob instanceof Squid)
				return MobHunting.getConfigManager().squidCmdDesc;
			else if (mob instanceof Villager)
				return MobHunting.getConfigManager().villagerCmdDesc;
			else if (mob instanceof Wolf)
				return MobHunting.getConfigManager().wolfCmdDesc;
			else if (mob instanceof Item && ((Item) mob).getItemStack().getType() == Material.RAW_FISH) {
				ItemStack is = ((Item) mob).getItemStack();
				if (is.getData().getData() == (byte) 0) {
					return MobHunting.getConfigManager().rawFishCmdDesc;
				} else if (is.getData().getData() == (byte) 1) {
					return MobHunting.getConfigManager().rawSalmonCmdDesc;
				} else if (is.getData().getData() == (byte) 2) {
					return MobHunting.getConfigManager().clownfishCmdDesc;
				} else if (is.getData().getData() == (byte) 3) {
					return MobHunting.getConfigManager().pufferfishCmdDesc;
				}
			}

		}
		return "";
	}

	public double getCmdRunChance(Entity killed) {
		if (TARDISWeepingAngelsCompat.isWeepingAngelMonster(killed)) {
			if (TARDISWeepingAngelsCompat.getMobRewardData()
					.containsKey(TARDISWeepingAngelsCompat.getWeepingAngelMonsterType(killed).name()))
				return TARDISWeepingAngelsCompat.getMobRewardData()
						.get(TARDISWeepingAngelsCompat.getWeepingAngelMonsterType(killed).name()).getChance();
			return 0;

		} else if (MythicMobsCompat.isMythicMob(killed)) {
			if (MythicMobsCompat.getMobRewardData().containsKey(MythicMobsCompat.getMythicMobType(killed)))
				return MythicMobsCompat.getMobRewardData().get(MythicMobsCompat.getMythicMobType(killed)).getChance();
			return 0;

		} else if (CitizensCompat.isNPC(killed) && CitizensCompat.isSentryOrSentinelOrSentries(killed)) {
			NPCRegistry registry = CitizensAPI.getNPCRegistry();
			NPC npc = registry.getNPC(killed);
			String key = String.valueOf(npc.getId());
			if (CitizensCompat.getMobRewardData().containsKey(key)) {
				return CitizensCompat.getMobRewardData().get(key).getChance();
			}
			return 0;

		} else if (CustomMobsCompat.isCustomMob(killed)) {
			if (CustomMobsCompat.getMobRewardData().containsKey(CustomMobsCompat.getCustomMobType(killed)))
				return CustomMobsCompat.getMobRewardData().get(CustomMobsCompat.getCustomMobType(killed)).getChance();
			return 0;

		} else if (MysteriousHalloweenCompat.isMysteriousHalloween(killed)) {
			if (MysteriousHalloweenCompat.getMobRewardData()
					.containsKey(MysteriousHalloweenCompat.getMysteriousHalloweenType(killed).name()))
				return MysteriousHalloweenCompat.getMobRewardData()
						.get(MysteriousHalloweenCompat.getMysteriousHalloweenType(killed).name()).getChance();
			return 0;

		} else if (SmartGiantsCompat.isSmartGiants(killed)) {
			if (SmartGiantsCompat.getMobRewardData().containsKey(SmartGiantsCompat.getSmartGiantsMobType(killed)))
				return SmartGiantsCompat.getMobRewardData().get(SmartGiantsCompat.getSmartGiantsMobType(killed))
						.getChance();
			return 0;

		} else if (HerobrineCompat.isHerobrineMob(killed)) {
			if (HerobrineCompat.getMobRewardData().containsKey(HerobrineCompat.getHerobrineMobType(killed)))
				return HerobrineCompat.getMobRewardData().get(HerobrineCompat.getHerobrineMobType(killed)).getChance();
			return 0;

		} else if (MyPetCompat.isMyPet(killed)) {
			return MobHunting.getConfigManager().wolfCmdRunChance;

		} else {
			if (Misc.isMC112OrNewer())
				if (killed instanceof Parrot)
					return MobHunting.getConfigManager().parrotCmdRunChance;
				else if (killed instanceof Illusioner)
					return MobHunting.getConfigManager().illusionerCmdRunChance;

			if (Misc.isMC111OrNewer())
				if (killed instanceof Llama)
					return MobHunting.getConfigManager().llamaCmdRunChance;
				else if (killed instanceof Vex)
					return MobHunting.getConfigManager().vexCmdRunChance;
				else if (killed instanceof Vindicator)
					return MobHunting.getConfigManager().vindicatorCmdRunChance;
				else if (killed instanceof Evoker)
					return MobHunting.getConfigManager().evokerCmdRunChance;
				else if (killed instanceof Donkey)
					return MobHunting.getConfigManager().donkeyCmdRunChance;
				else if (killed instanceof Mule)
					return MobHunting.getConfigManager().muleCmdRunChance;
				else if (killed instanceof SkeletonHorse)
					return MobHunting.getConfigManager().skeletonhorseCmdRunChance;
				else if (killed instanceof ZombieHorse)
					return MobHunting.getConfigManager().zombiehorseCmdRunChance;
				else if (killed instanceof Stray)
					return MobHunting.getConfigManager().strayCmdRunChance;
				else if (killed instanceof Husk)
					return MobHunting.getConfigManager().huskCmdRunChance;
				else if (killed instanceof ZombieVillager)
					return MobHunting.getConfigManager().zombieVillagerCmdRunChance;
				else if (killed instanceof Villager && ((Villager) killed).getProfession() == Profession.NITWIT)
					return MobHunting.getConfigManager().nitwitCmdRunChance;

			if (Misc.isMC110OrNewer())
				if (killed instanceof PolarBear)
					return MobHunting.getConfigManager().polarBearCmdRunChance;
				else if (killed instanceof Skeleton && ((Skeleton) killed).getSkeletonType() == SkeletonType.STRAY)
					return MobHunting.getConfigManager().strayCmdRunChance;
				else if (killed instanceof Zombie && ((Zombie) killed).getVillagerProfession() == Profession.HUSK)
					return MobHunting.getConfigManager().huskCmdRunChance;

				else if (killed instanceof Villager && ((Villager) killed).getProfession() == Profession.NORMAL)
					return MobHunting.getConfigManager().villagerCmdRunChance;
				else if (killed instanceof Villager && ((Villager) killed).getProfession() == Profession.PRIEST)
					return MobHunting.getConfigManager().priestCmdRunChance;
				else if (killed instanceof Villager && ((Villager) killed).getProfession() == Profession.BUTCHER)
					return MobHunting.getConfigManager().butcherCmdRunChance;
				else if (killed instanceof Villager && ((Villager) killed).getProfession() == Profession.BLACKSMITH)
					return MobHunting.getConfigManager().blacksmithCmdRunChance;
				else if (killed instanceof Villager && ((Villager) killed).getProfession() == Profession.LIBRARIAN)
					return MobHunting.getConfigManager().librarianCmdRunChance;
				else if (killed instanceof Villager && ((Villager) killed).getProfession() == Profession.FARMER)
					return MobHunting.getConfigManager().farmerCmdRunChance;

			if (Misc.isMC19OrNewer())
				if (killed instanceof Shulker)
					return MobHunting.getConfigManager().shulkerCmdRunChance;

			if (Misc.isMC18OrNewer())
				if (killed instanceof Guardian && ((Guardian) killed).isElder())
					return MobHunting.getConfigManager().elderGuardianCmdRunChance;
				else if (killed instanceof Guardian)
					return MobHunting.getConfigManager().guardianCmdRunChance;
				else if (killed instanceof Endermite)
					return MobHunting.getConfigManager().endermiteCmdRunChance;
				else if (killed instanceof Rabbit)
					if ((((Rabbit) killed).getRabbitType()) == Rabbit.Type.THE_KILLER_BUNNY)
						return MobHunting.getConfigManager().killerrabbitCmdRunChance;
					else
						return MobHunting.getConfigManager().rabbitCmdRunChance;

			// MC1.7 or older
			if (killed instanceof Player) {
				return MobHunting.getConfigManager().pvpKillCmdRunChance;
			} else if (killed instanceof Blaze)
				return MobHunting.getConfigManager().blazeCmdRunChance;
			else if (killed instanceof Creeper)
				return MobHunting.getConfigManager().creeperCmdRunChance;
			else if (killed instanceof Silverfish)
				return MobHunting.getConfigManager().silverfishCmdRunChance;
			else if (killed instanceof Enderman)
				return MobHunting.getConfigManager().endermanCmdRunChance;
			else if (killed instanceof Giant)
				return MobHunting.getConfigManager().giantCmdRunChance;
			else if (killed instanceof Skeleton && ((Skeleton) killed).getSkeletonType() == SkeletonType.NORMAL)
				return MobHunting.getConfigManager().skeletonCmdRunChance;
			else if (killed instanceof Skeleton && ((Skeleton) killed).getSkeletonType() == SkeletonType.WITHER)
				return MobHunting.getConfigManager().witherSkeletonCmdRunChance;
			else if (killed instanceof CaveSpider)
				// CaveSpider is a subclass of Spider
				return MobHunting.getConfigManager().caveSpiderRunChance;
			else if (killed instanceof Spider)
				return MobHunting.getConfigManager().spiderCmdRunChance;
			else if (killed instanceof Witch)
				return MobHunting.getConfigManager().witchCmdRunChance;
			else if (killed instanceof PigZombie)
				// PigZombie is a subclass of Zombie.
				return MobHunting.getConfigManager().zombiepigmanCmdRunChance;
			else if (killed instanceof Zombie)
				return MobHunting.getConfigManager().zombieCmdRunChance;
			else if (killed instanceof Ghast)
				return MobHunting.getConfigManager().ghastCmdRunChance;
			else if (killed instanceof MagmaCube)
				// MagmaCube is a subclass of Slime
				return MobHunting.getConfigManager().magmaCubeCmdRunChance;
			else if (killed instanceof Slime)
				return MobHunting.getConfigManager().slimeCmdRunChance;
			else if (killed instanceof EnderDragon)
				return MobHunting.getConfigManager().enderdragonCmdRunChance;
			else if (killed instanceof Wither)
				return MobHunting.getConfigManager().witherCmdRunChance;
			else if (killed instanceof IronGolem)
				return MobHunting.getConfigManager().ironGolemCmdRunChance;

			// Passive mobs
			else if (killed instanceof Bat)
				return MobHunting.getConfigManager().batCmdRunChance;
			else if (killed instanceof Chicken)
				return MobHunting.getConfigManager().chickenCmdRunChance;
			else if (killed instanceof Cow)
				if (killed instanceof MushroomCow)
					// MushroomCow is a subclass of Cow
					return MobHunting.getConfigManager().mushroomCowCmdRunChance;
				else
					return MobHunting.getConfigManager().cowCmdRunChance;
			else if (killed instanceof Horse)
				return MobHunting.getConfigManager().horseCmdRunChance;
			else if (killed instanceof Ocelot)
				return MobHunting.getConfigManager().ocelotCmdRunChance;
			else if (killed instanceof Pig)
				return MobHunting.getConfigManager().pigCmdRunChance;
			else if (killed instanceof Sheep)
				return MobHunting.getConfigManager().sheepCmdRunChance;
			else if (killed instanceof Snowman)
				return MobHunting.getConfigManager().snowmanCmdRunChance;
			else if (killed instanceof Squid)
				return MobHunting.getConfigManager().squidCmdRunChance;
			else if (killed instanceof Villager)
				return MobHunting.getConfigManager().villagerCmdRunChance;
			else if (killed instanceof Wolf)
				return MobHunting.getConfigManager().wolfCmdRunChance;
			else if (killed instanceof Item && ((Item) killed).getItemStack().getType() == Material.RAW_FISH) {
				ItemStack is = ((Item) killed).getItemStack();
				if (is.getData().getData() == (byte) 0) {
					return MobHunting.getConfigManager().rawFishCmdRunChance;
				} else if (is.getData().getData() == (byte) 1) {
					return MobHunting.getConfigManager().rawSalmonCmdRunChance;
				} else if (is.getData().getData() == (byte) 2) {
					return MobHunting.getConfigManager().clownfishCmdRunChance;
				} else if (is.getData().getData() == (byte) 3) {
					return MobHunting.getConfigManager().pufferfishCmdRunChance;
				}
			}
		}
		return 0;
	}

	public double getMcMMOChance(Entity killed) {
		if (TARDISWeepingAngelsCompat.isWeepingAngelMonster(killed)) {
			if (TARDISWeepingAngelsCompat.getMobRewardData()
					.containsKey(TARDISWeepingAngelsCompat.getWeepingAngelMonsterType(killed).name()))
				return TARDISWeepingAngelsCompat.getMobRewardData()
						.get(TARDISWeepingAngelsCompat.getWeepingAngelMonsterType(killed).name())
						.getMcMMOSkillRewardChance();
			return 0;

		} else if (MythicMobsCompat.isMythicMob(killed)) {
			if (MythicMobsCompat.getMobRewardData().containsKey(MythicMobsCompat.getMythicMobType(killed)))
				return MythicMobsCompat.getMobRewardData().get(MythicMobsCompat.getMythicMobType(killed))
						.getMcMMOSkillRewardChance();
			return 0;

		} else if (CitizensCompat.isNPC(killed) && CitizensCompat.isSentryOrSentinelOrSentries(killed)) {
			NPCRegistry registry = CitizensAPI.getNPCRegistry();
			NPC npc = registry.getNPC(killed);
			String key = String.valueOf(npc.getId());
			if (CitizensCompat.getMobRewardData().containsKey(key)) {
				return CitizensCompat.getMobRewardData().get(key).getMcMMOSkillRewardChance();
			}
			return 0;

		} else if (CustomMobsCompat.isCustomMob(killed)) {
			if (CustomMobsCompat.getMobRewardData().containsKey(CustomMobsCompat.getCustomMobType(killed)))
				return CustomMobsCompat.getMobRewardData().get(CustomMobsCompat.getCustomMobType(killed))
						.getMcMMOSkillRewardChance();
			return 0;

		} else if (MysteriousHalloweenCompat.isMysteriousHalloween(killed)) {
			if (MysteriousHalloweenCompat.getMobRewardData()
					.containsKey(MysteriousHalloweenCompat.getMysteriousHalloweenType(killed).name()))
				return MysteriousHalloweenCompat.getMobRewardData()
						.get(MysteriousHalloweenCompat.getMysteriousHalloweenType(killed).name())
						.getMcMMOSkillRewardChance();
			return 0;

		} else if (SmartGiantsCompat.isSmartGiants(killed)) {
			if (SmartGiantsCompat.getMobRewardData().containsKey(SmartGiantsCompat.getSmartGiantsMobType(killed)))
				return SmartGiantsCompat.getMobRewardData().get(SmartGiantsCompat.getSmartGiantsMobType(killed))
						.getMcMMOSkillRewardChance();
			return 0;

		} else if (HerobrineCompat.isHerobrineMob(killed)) {
			if (HerobrineCompat.getMobRewardData().containsKey(HerobrineCompat.getHerobrineMobType(killed)))
				return HerobrineCompat.getMobRewardData().get(HerobrineCompat.getHerobrineMobType(killed))
						.getMcMMOSkillRewardChance();
			return 0;

		} else if (MyPetCompat.isMyPet(killed)) {
			return MobHunting.getConfigManager().wolfMcMMOSkillRewardChance;

		} else {
			if (Misc.isMC112OrNewer())
				if (killed instanceof Parrot)
					return MobHunting.getConfigManager().parrotMcMMOSkillRewardChance;
				else if (killed instanceof Illusioner)
					return MobHunting.getConfigManager().illusionerMcMMOSkillRewardChance;

			if (Misc.isMC111OrNewer())
				if (killed instanceof Llama)
					return MobHunting.getConfigManager().llamaMcMMOSkillRewardChance;
				else if (killed instanceof Vex)
					return MobHunting.getConfigManager().vexMcMMOSkillRewardChance;
				else if (killed instanceof Vindicator)
					return MobHunting.getConfigManager().vindicatorMcMMOSkillRewardChance;
				else if (killed instanceof Evoker)
					return MobHunting.getConfigManager().evokerMcMMOSkillRewardChance;
				else if (killed instanceof Donkey)
					return MobHunting.getConfigManager().donkeyMcMMOSkillRewardChance;
				else if (killed instanceof Mule)
					return MobHunting.getConfigManager().muleMcMMOSkillRewardChance;
				else if (killed instanceof SkeletonHorse)
					return MobHunting.getConfigManager().skeletonHorseMcMMOSkillRewardChance;
				else if (killed instanceof ZombieHorse)
					return MobHunting.getConfigManager().zombieHorseMcMMOSkillRewardChance;
				else if (killed instanceof Stray)
					return MobHunting.getConfigManager().strayMcMMOSkillRewardChance;
				else if (killed instanceof Husk)
					return MobHunting.getConfigManager().huskMcMMOSkillRewardChance;
				else if (killed instanceof ZombieVillager)
					return MobHunting.getConfigManager().zombieVillagerMcMMOSkillRewardChance;
				else if (killed instanceof Villager && ((Villager) killed).getProfession() == Profession.NITWIT)
					return MobHunting.getConfigManager().nitwitMcMMOSkillRewardChance;

			if (Misc.isMC110OrNewer())
				if (killed instanceof PolarBear)
					return MobHunting.getConfigManager().polarBearMcMMOSkillRewardChance;
				else if (killed instanceof Skeleton && ((Skeleton) killed).getSkeletonType() == SkeletonType.STRAY)
					return MobHunting.getConfigManager().strayMcMMOSkillRewardChance;
				else if (killed instanceof Zombie && ((Zombie) killed).getVillagerProfession() == Profession.HUSK)
					return MobHunting.getConfigManager().huskMcMMOSkillRewardChance;

				else if (killed instanceof Villager && ((Villager) killed).getProfession() == Profession.NORMAL)
					return MobHunting.getConfigManager().villagerMcMMOSkillRewardChance;
				else if (killed instanceof Villager && ((Villager) killed).getProfession() == Profession.PRIEST)
					return MobHunting.getConfigManager().priestMcMMOSkillRewardChance;
				else if (killed instanceof Villager && ((Villager) killed).getProfession() == Profession.BUTCHER)
					return MobHunting.getConfigManager().butcherMcMMOSkillRewardChance;
				else if (killed instanceof Villager && ((Villager) killed).getProfession() == Profession.BLACKSMITH)
					return MobHunting.getConfigManager().blacksmithMcMMOSkillRewardChance;
				else if (killed instanceof Villager && ((Villager) killed).getProfession() == Profession.LIBRARIAN)
					return MobHunting.getConfigManager().librarianMcMMOSkillRewardChance;
				else if (killed instanceof Villager && ((Villager) killed).getProfession() == Profession.FARMER)
					return MobHunting.getConfigManager().farmerMcMMOSkillRewardChance;

			if (Misc.isMC19OrNewer())
				if (killed instanceof Shulker)
					return MobHunting.getConfigManager().shulkerMcMMOSkillRewardChance;

			if (Misc.isMC18OrNewer())
				if (killed instanceof Guardian && ((Guardian) killed).isElder())
					return MobHunting.getConfigManager().elderGuardianMcMMOSkillRewardChance;
				else if (killed instanceof Guardian)
					return MobHunting.getConfigManager().guardianMcMMOSkillRewardChance;
				else if (killed instanceof Endermite)
					return MobHunting.getConfigManager().endermiteMcMMOSkillRewardChance;
				else if (killed instanceof Rabbit)
					if ((((Rabbit) killed).getRabbitType()) == Rabbit.Type.THE_KILLER_BUNNY)
						return MobHunting.getConfigManager().killerRabbitMcMMOSkillRewardChance;
					else
						return MobHunting.getConfigManager().rabbitMcMMOSkillRewardChance;

			// MC1.7 or older
			if (killed instanceof Player) {
				return MobHunting.getConfigManager().pvpPlayerMcMMOSkillRewardChance;
			} else if (killed instanceof Blaze)
				return MobHunting.getConfigManager().blazeMcMMOSkillRewardChance;
			else if (killed instanceof Creeper)
				return MobHunting.getConfigManager().creeperMcMMOSkillRewardChance;
			else if (killed instanceof Silverfish)
				return MobHunting.getConfigManager().silverfishMcMMOSkillRewardChance;
			else if (killed instanceof Enderman)
				return MobHunting.getConfigManager().endermanMcMMOSkillRewardChance;
			else if (killed instanceof Giant)
				return MobHunting.getConfigManager().giantMcMMOSkillRewardChance;
			else if (killed instanceof Skeleton && ((Skeleton) killed).getSkeletonType() == SkeletonType.NORMAL)
				return MobHunting.getConfigManager().skeletonMcMMOSkillRewardChance;
			else if (killed instanceof Skeleton && ((Skeleton) killed).getSkeletonType() == SkeletonType.WITHER)
				return MobHunting.getConfigManager().witherSkeletonMcMMOSkillRewardChance;
			else if (killed instanceof CaveSpider)
				// CaveSpider is a subclass of Spider
				return MobHunting.getConfigManager().caveSpiderMcMMOSkillRewardChance;
			else if (killed instanceof Spider)
				return MobHunting.getConfigManager().spiderMcMMOSkillRewardChance;
			else if (killed instanceof Witch)
				return MobHunting.getConfigManager().witchMcMMOSkillRewardChance;
			else if (killed instanceof PigZombie)
				// PigZombie is a subclass of Zombie.
				return MobHunting.getConfigManager().zombiePigManMcMMOSkillRewardChance;
			else if (killed instanceof Zombie)
				return MobHunting.getConfigManager().zombieMcMMOSkillRewardChance;
			else if (killed instanceof Ghast)
				return MobHunting.getConfigManager().ghastMcMMOSkillRewardChance;
			else if (killed instanceof MagmaCube)
				// MagmaCube is a subclass of Slime
				return MobHunting.getConfigManager().magmaCubeMcMMOSkillRewardChance;
			else if (killed instanceof Slime)
				return MobHunting.getConfigManager().slimeMcMMOSkillRewardChance;
			else if (killed instanceof EnderDragon)
				return MobHunting.getConfigManager().enderdragonMcMMOSkillRewardChance;
			else if (killed instanceof Wither)
				return MobHunting.getConfigManager().witherMcMMOSkillRewardChance;
			else if (killed instanceof IronGolem)
				return MobHunting.getConfigManager().ironGolemMcMMOSkillRewardChance;

			// Passive mobs
			else if (killed instanceof Bat)
				return MobHunting.getConfigManager().batMcMMOSkillRewardChance;
			else if (killed instanceof Chicken)
				return MobHunting.getConfigManager().chickenMcMMOSkillRewardChance;
			else if (killed instanceof Cow)
				if (killed instanceof MushroomCow)
					// MushroomCow is a subclass of Cow
					return MobHunting.getConfigManager().mushroomCowMcMMOSkillRewardChance;
				else
					return MobHunting.getConfigManager().cowMcMMOSkillRewardChance;
			else if (killed instanceof Horse)
				return MobHunting.getConfigManager().horseMcMMOSkillRewardChance;
			else if (killed instanceof Ocelot)
				return MobHunting.getConfigManager().ocelotMcMMOSkillRewardChance;
			else if (killed instanceof Pig)
				return MobHunting.getConfigManager().pigMcMMOSkillRewardChance;
			else if (killed instanceof Sheep)
				return MobHunting.getConfigManager().sheepMcMMOSkillRewardChance;
			else if (killed instanceof Snowman)
				return MobHunting.getConfigManager().snowmanMcMMOSkillRewardChance;
			else if (killed instanceof Squid)
				return MobHunting.getConfigManager().squidMcMMOSkillRewardChance;
			else if (killed instanceof Villager)
				return MobHunting.getConfigManager().villagerMcMMOSkillRewardChance;
			else if (killed instanceof Wolf)
				return MobHunting.getConfigManager().wolfMcMMOSkillRewardChance;
			else if (killed instanceof Item && ((Item) killed).getItemStack().getType() == Material.RAW_FISH) {
				ItemStack is = ((Item) killed).getItemStack();
				if (is.getData().getData() == (byte) 0) {
					return MobHunting.getConfigManager().rawfishMcMMOSkillRewardChance;
				} else if (is.getData().getData() == (byte) 1) {
					return MobHunting.getConfigManager().rawsalmonMcMMOSkillRewardChance;
				} else if (is.getData().getData() == (byte) 2) {
					return MobHunting.getConfigManager().clownfishMcMMOSkillRewardChance;
				} else if (is.getData().getData() == (byte) 3) {
					return MobHunting.getConfigManager().pufferfishMcMMOSkillRewardChance;
				}
			}
		}
		return 0;
	}

	private int getMcMMOXP(Entity mob, String str) {
		if (str == null || str.equals("") || str.isEmpty()) {
			Bukkit.getServer().getConsoleSender()
					.sendMessage(ChatColor.RED + "[MobHunting] [WARNING]" + ChatColor.RESET
							+ " The McMMO XP for killing a " + ExtendedMobManager.getMobName(mob)
							+ " is not set in config.yml. Please set the McMMO XP to 0 or a positive number.");
			return 0;
		} else if (str.startsWith(":")) {
			Bukkit.getServer().getConsoleSender()
					.sendMessage(ChatColor.RED + "[MobHunting] [WARNING]" + ChatColor.RESET
							+ " The McMMO XP for killing a " + ExtendedMobManager.getMobName(mob)
							+ " in config.yml has a wrong format. The prize can't start with \":\"");
			if (str.length() > 1)
				return getMcMMOXP(mob, str.substring(1, str.length()));
			else
				return 0;
		} else if (str.contains(":")) {
			String[] str1 = str.split(":");
			Integer prize = MobHunting.getMobHuntingManager().mRand.nextInt(Integer.valueOf(str1[1]))
					+ Integer.valueOf(str1[0]);
			return prize;
		} else
			return Integer.valueOf(str);
	}

	public int getMcMMOLevel(Entity killed) {
		if (TARDISWeepingAngelsCompat.isWeepingAngelMonster(killed)) {
			if (TARDISWeepingAngelsCompat.getMobRewardData()
					.containsKey(TARDISWeepingAngelsCompat.getWeepingAngelMonsterType(killed).name()))
				return TARDISWeepingAngelsCompat.getMobRewardData()
						.get(TARDISWeepingAngelsCompat.getWeepingAngelMonsterType(killed).name())
						.getMcMMOSkillRewardAmount();
			return 0;

		} else if (MythicMobsCompat.isMythicMob(killed)) {
			if (MythicMobsCompat.getMobRewardData().containsKey(MythicMobsCompat.getMythicMobType(killed)))
				return MythicMobsCompat.getMobRewardData().get(MythicMobsCompat.getMythicMobType(killed))
						.getMcMMOSkillRewardAmount();
			return 0;

		} else if (CitizensCompat.isNPC(killed) && CitizensCompat.isSentryOrSentinelOrSentries(killed)) {
			NPCRegistry registry = CitizensAPI.getNPCRegistry();
			NPC npc = registry.getNPC(killed);
			String key = String.valueOf(npc.getId());
			if (CitizensCompat.getMobRewardData().containsKey(key)) {
				return CitizensCompat.getMobRewardData().get(key).getMcMMOSkillRewardAmount();
			}
			return 0;

		} else if (CustomMobsCompat.isCustomMob(killed)) {
			if (CustomMobsCompat.getMobRewardData().containsKey(CustomMobsCompat.getCustomMobType(killed)))
				return CustomMobsCompat.getMobRewardData().get(CustomMobsCompat.getCustomMobType(killed))
						.getMcMMOSkillRewardAmount();
			return 0;

		} else if (MysteriousHalloweenCompat.isMysteriousHalloween(killed)) {
			if (MysteriousHalloweenCompat.getMobRewardData()
					.containsKey(MysteriousHalloweenCompat.getMysteriousHalloweenType(killed).name()))
				return MysteriousHalloweenCompat.getMobRewardData()
						.get(MysteriousHalloweenCompat.getMysteriousHalloweenType(killed).name())
						.getMcMMOSkillRewardAmount();
			return 0;

		} else if (SmartGiantsCompat.isSmartGiants(killed)) {
			if (SmartGiantsCompat.getMobRewardData().containsKey(SmartGiantsCompat.getSmartGiantsMobType(killed)))
				return SmartGiantsCompat.getMobRewardData().get(SmartGiantsCompat.getSmartGiantsMobType(killed))
						.getMcMMOSkillRewardAmount();
			return 0;

		} else if (HerobrineCompat.isHerobrineMob(killed)) {
			if (HerobrineCompat.getMobRewardData().containsKey(HerobrineCompat.getHerobrineMobType(killed)))
				return HerobrineCompat.getMobRewardData().get(HerobrineCompat.getHerobrineMobType(killed))
						.getMcMMOSkillRewardAmount();
			return 0;

		} else if (MyPetCompat.isMyPet(killed)) {
			return getMcMMOXP(killed, MobHunting.getConfigManager().wolfMcMMOSkillRewardAmount);

		} else {
			if (Misc.isMC112OrNewer())
				if (killed instanceof Parrot)
					return getMcMMOXP(killed, MobHunting.getConfigManager().parrotMcMMOSkillRewardAmount);
				else if (killed instanceof Illusioner)
					return getMcMMOXP(killed, MobHunting.getConfigManager().illusionerMcMMOSkillRewardAmount);

			if (Misc.isMC111OrNewer())
				if (killed instanceof Llama)
					return getMcMMOXP(killed, MobHunting.getConfigManager().llamaMcMMOSkillRewardAmount);
				else if (killed instanceof Vex)
					return getMcMMOXP(killed, MobHunting.getConfigManager().vexMcMMOSkillRewardAmount);
				else if (killed instanceof Vindicator)
					return getMcMMOXP(killed, MobHunting.getConfigManager().vindicatorMcMMOSkillRewardAmount);
				else if (killed instanceof Evoker)
					return getMcMMOXP(killed, MobHunting.getConfigManager().evokerMcMMOSkillRewardAmount);
				else if (killed instanceof Donkey)
					return getMcMMOXP(killed, MobHunting.getConfigManager().donkeyMcMMOSkillRewardAmount);
				else if (killed instanceof Mule)
					return getMcMMOXP(killed, MobHunting.getConfigManager().muleMcMMOSkillRewardAmount);
				else if (killed instanceof SkeletonHorse)
					return getMcMMOXP(killed, MobHunting.getConfigManager().skeletonHorseMcMMOSkillRewardAmount);
				else if (killed instanceof ZombieHorse)
					return getMcMMOXP(killed, MobHunting.getConfigManager().zombieHorseMcMMOSkillRewardAmount);
				else if (killed instanceof Stray)
					return getMcMMOXP(killed, MobHunting.getConfigManager().strayMcMMOSkillRewardAmount);
				else if (killed instanceof Husk)
					return getMcMMOXP(killed, MobHunting.getConfigManager().huskMcMMOSkillRewardAmount);
				else if (killed instanceof ZombieVillager)
					return getMcMMOXP(killed, MobHunting.getConfigManager().zombieVillagerMcMMOSkillRewardAmount);
				else if (killed instanceof Villager && ((Villager) killed).getProfession() == Profession.NITWIT)
					return getMcMMOXP(killed, MobHunting.getConfigManager().nitwitMcMMOSkillRewardAmount);

			if (Misc.isMC110OrNewer())
				if (killed instanceof PolarBear)
					return getMcMMOXP(killed, MobHunting.getConfigManager().polarBearMcMMOSkillRewardAmount);
				else if (killed instanceof Skeleton && ((Skeleton) killed).getSkeletonType() == SkeletonType.STRAY)
					return getMcMMOXP(killed, MobHunting.getConfigManager().strayMcMMOSkillRewardAmount);
				else if (killed instanceof Zombie && ((Zombie) killed).getVillagerProfession() == Profession.HUSK)
					return getMcMMOXP(killed, MobHunting.getConfigManager().huskMcMMOSkillRewardAmount);

				else if (killed instanceof Villager && ((Villager) killed).getProfession() == Profession.NORMAL)
					return getMcMMOXP(killed, MobHunting.getConfigManager().villagerMcMMOSkillRewardAmount);
				else if (killed instanceof Villager && ((Villager) killed).getProfession() == Profession.PRIEST)
					return getMcMMOXP(killed, MobHunting.getConfigManager().priestMcMMOSkillRewardAmount);
				else if (killed instanceof Villager && ((Villager) killed).getProfession() == Profession.BUTCHER)
					return getMcMMOXP(killed, MobHunting.getConfigManager().butcherMcMMOSkillRewardAmount);
				else if (killed instanceof Villager && ((Villager) killed).getProfession() == Profession.BLACKSMITH)
					return getMcMMOXP(killed, MobHunting.getConfigManager().blacksmithMcMMOSkillRewardAmount);
				else if (killed instanceof Villager && ((Villager) killed).getProfession() == Profession.LIBRARIAN)
					return getMcMMOXP(killed, MobHunting.getConfigManager().librarianMcMMOSkillRewardAmount);
				else if (killed instanceof Villager && ((Villager) killed).getProfession() == Profession.FARMER)
					return getMcMMOXP(killed, MobHunting.getConfigManager().farmerMcMMOSkillRewardAmount);

			if (Misc.isMC19OrNewer())
				if (killed instanceof Shulker)
					return getMcMMOXP(killed, MobHunting.getConfigManager().shulkerMcMMOSkillRewardAmount);

			if (Misc.isMC18OrNewer())
				if (killed instanceof Guardian && ((Guardian) killed).isElder())
					return getMcMMOXP(killed, MobHunting.getConfigManager().elderGuardianMcMMOSkillRewardAmount);
				else if (killed instanceof Guardian)
					return getMcMMOXP(killed, MobHunting.getConfigManager().guardianMcMMOSkillRewardAmount);
				else if (killed instanceof Endermite)
					return getMcMMOXP(killed, MobHunting.getConfigManager().endermiteMcMMOSkillRewardAmount);
				else if (killed instanceof Rabbit)
					if ((((Rabbit) killed).getRabbitType()) == Rabbit.Type.THE_KILLER_BUNNY)
						return getMcMMOXP(killed, MobHunting.getConfigManager().killerRabbitMcMMOSkillRewardAmount);
					else
						return getMcMMOXP(killed, MobHunting.getConfigManager().rabbitMcMMOSkillRewardAmount);

			// MC1.7 or older
			if (killed instanceof Player) {
				return getMcMMOXP(killed, MobHunting.getConfigManager().pvpPlayerMcMMOSkillRewardAmount);
			} else if (killed instanceof Blaze)
				return getMcMMOXP(killed, MobHunting.getConfigManager().blazeMcMMOSkillRewardAmount);
			else if (killed instanceof Creeper)
				return getMcMMOXP(killed, MobHunting.getConfigManager().creeperMcMMOSkillRewardAmount);
			else if (killed instanceof Silverfish)
				return getMcMMOXP(killed, MobHunting.getConfigManager().silverfishMcMMOSkillRewardAmount);
			else if (killed instanceof Enderman)
				return getMcMMOXP(killed, MobHunting.getConfigManager().endermanMcMMOSkillRewardAmount);
			else if (killed instanceof Giant)
				return getMcMMOXP(killed, MobHunting.getConfigManager().giantMcMMOSkillRewardAmount);
			else if (killed instanceof Skeleton && ((Skeleton) killed).getSkeletonType() == SkeletonType.NORMAL)
				return getMcMMOXP(killed, MobHunting.getConfigManager().skeletonMcMMOSkillRewardAmount);
			else if (killed instanceof Skeleton && ((Skeleton) killed).getSkeletonType() == SkeletonType.WITHER)
				return getMcMMOXP(killed, MobHunting.getConfigManager().witherSkeletonMcMMOSkillRewardAmount);
			else if (killed instanceof CaveSpider)
				// CaveSpider is a subclass of Spider
				return getMcMMOXP(killed, MobHunting.getConfigManager().caveSpiderMcMMOSkillRewardAmount);
			else if (killed instanceof Spider)
				return getMcMMOXP(killed, MobHunting.getConfigManager().spiderMcMMOSkillRewardAmount);
			else if (killed instanceof Witch)
				return getMcMMOXP(killed, MobHunting.getConfigManager().witchMcMMOSkillRewardAmount);
			else if (killed instanceof PigZombie)
				// PigZombie is a subclass of Zombie.
				return getMcMMOXP(killed, MobHunting.getConfigManager().zombiePigManMcMMOSkillRewardAmount);
			else if (killed instanceof Zombie)
				return getMcMMOXP(killed, MobHunting.getConfigManager().zombieMcMMOSkillRewardAmount);
			else if (killed instanceof Ghast)
				return getMcMMOXP(killed, MobHunting.getConfigManager().ghastMcMMOSkillRewardAmount);
			else if (killed instanceof MagmaCube)
				// MagmaCube is a subclass of Slime
				return getMcMMOXP(killed, MobHunting.getConfigManager().magmaCubeMcMMOSkillRewardAmount);
			else if (killed instanceof Slime)
				return getMcMMOXP(killed, MobHunting.getConfigManager().slimeMcMMOSkillRewardAmount);
			else if (killed instanceof EnderDragon)
				return getMcMMOXP(killed, MobHunting.getConfigManager().enderdragonMcMMOSkillRewardAmount);
			else if (killed instanceof Wither)
				return getMcMMOXP(killed, MobHunting.getConfigManager().witherMcMMOSkillRewardAmount);
			else if (killed instanceof IronGolem)
				return getMcMMOXP(killed, MobHunting.getConfigManager().ironGolemMcMMOSkillRewardAmount);

			// Passive mobs
			else if (killed instanceof Bat)
				return getMcMMOXP(killed, MobHunting.getConfigManager().batMcMMOSkillRewardAmount);
			else if (killed instanceof Chicken)
				return getMcMMOXP(killed, MobHunting.getConfigManager().chickenMcMMOSkillRewardAmount);
			else if (killed instanceof Cow)
				if (killed instanceof MushroomCow)
					// MushroomCow is a subclass of Cow
					return getMcMMOXP(killed, MobHunting.getConfigManager().mushroomCowMcMMOSkillRewardAmount);
				else
					return getMcMMOXP(killed, MobHunting.getConfigManager().cowMcMMOSkillRewardAmount);
			else if (killed instanceof Horse)
				return getMcMMOXP(killed, MobHunting.getConfigManager().horseMcMMOSkillRewardAmount);
			else if (killed instanceof Ocelot)
				return getMcMMOXP(killed, MobHunting.getConfigManager().ocelotMcMMOSkillRewardAmount);
			else if (killed instanceof Pig)
				return getMcMMOXP(killed, MobHunting.getConfigManager().pigMcMMOSkillRewardAmount);
			else if (killed instanceof Sheep)
				return getMcMMOXP(killed, MobHunting.getConfigManager().sheepMcMMOSkillRewardAmount);
			else if (killed instanceof Snowman)
				return getMcMMOXP(killed, MobHunting.getConfigManager().snowmanMcMMOSkillRewardAmount);
			else if (killed instanceof Squid)
				return getMcMMOXP(killed, MobHunting.getConfigManager().squidMcMMOSkillRewardAmount);
			else if (killed instanceof Villager)
				return getMcMMOXP(killed, MobHunting.getConfigManager().villagerMcMMOSkillRewardAmount);
			else if (killed instanceof Wolf)
				return getMcMMOXP(killed, MobHunting.getConfigManager().wolfMcMMOSkillRewardAmount);
			else if (killed instanceof Item && ((Item) killed).getItemStack().getType() == Material.RAW_FISH) {
				ItemStack is = ((Item) killed).getItemStack();
				if (is.getData().getData() == (byte) 0) {
					return getMcMMOXP(killed, MobHunting.getConfigManager().rawfishMcMMOSkillRewardAmount);
				} else if (is.getData().getData() == (byte) 1) {
					return getMcMMOXP(killed, MobHunting.getConfigManager().rawsalmonMcMMOSkillRewardAmount);
				} else if (is.getData().getData() == (byte) 2) {
					return getMcMMOXP(killed, MobHunting.getConfigManager().clownfishMcMMOSkillRewardAmount);
				} else if (is.getData().getData() == (byte) 3) {
					return getMcMMOXP(killed, MobHunting.getConfigManager().pufferfishMcMMOSkillRewardAmount);
				}
			}
		}
		return 0;
	}

	public boolean isCmdGointToBeExcuted(Entity killed) {
		double randomDouble = MobHunting.getMobHuntingManager().mRand.nextDouble();
		double runChanceDouble = getCmdRunChance(killed);
		Messages.debug("Command will be run if chance: %s > %s (random number)", runChanceDouble, randomDouble);
		if (killed instanceof Player)
			return randomDouble < MobHunting.getConfigManager().pvpKillCmdRunChance;
		else
			return !getKillConsoleCmd(killed).equals("") && randomDouble < runChanceDouble;
	}

}
