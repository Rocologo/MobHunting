package one.lindegaard.MobHunting;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import one.lindegaard.MobHunting.compatibility.CitizensCompat;
import one.lindegaard.MobHunting.compatibility.CustomMobsCompat;
import one.lindegaard.MobHunting.compatibility.MyPetCompat;
import one.lindegaard.MobHunting.compatibility.MysteriousHalloweenCompat;
import one.lindegaard.MobHunting.compatibility.MythicMobsCompat;
import one.lindegaard.MobHunting.compatibility.TARDISWeepingAngelsCompat;
import one.lindegaard.MobHunting.mobs.ExtendedMobManager;
import one.lindegaard.MobHunting.rewards.MobRewardData;
import one.lindegaard.MobHunting.util.AutoConfig;
import one.lindegaard.MobHunting.util.ConfigField;
import one.lindegaard.MobHunting.util.Misc;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
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
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Item;
import org.bukkit.entity.Llama;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Mule;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.PolarBear;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Shulker;
import org.bukkit.entity.Silverfish;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Squid;
import org.bukkit.entity.Stray;
import org.bukkit.entity.Vex;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Witch;
import org.bukkit.entity.Wither;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.ZombieHorse;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.entity.SkeletonHorse;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.entity.Vindicator;
import org.bukkit.metadata.MetadataValue;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;

@SuppressWarnings("deprecation")
public class ConfigManager extends AutoConfig {

	public ConfigManager(File file) {
		super(file);

		setCategoryComment("mobs", "########################################################################"
				+ "\nRewards for killing mobs."
				+ "\n########################################################################"
				+ "\nHere is where you set the base prize in $ for killing a mob of each type"
				+ "\nYou can either set a decimal number ex 1.23 or a range 1.23:2.23"
				+ "\nFor each kill you can run a console command to give the player a reward."
				+ "\nYou can use the following variables:"
				+ "\n{killer},{killed},{player},{killed_player},{prize},{world},"
				+ "\n{killerpos},{killedpos}. Killerpos and Killedpos will have the "
				+ "\nformat <x> <y> <z>. Which could be used to /summon items. "
				+ "\nAn example could be /summon apple {killedpos} 2. to summon two apples where"
				+ "\nwhere the mob was killed or /summon apple {killerpos} 1. to summon an"
				+ "\nan apple where the player is." + "\nAnother example could be to give the player permission to fly"
				+ "\nfor 1 hour or use give command to the player items."
				+ "\n\nYou can also specify the message send to the player."
				+ "\nThe text can be color coded with these codes:"
				+ "\nhttp://minecraft.gamepedia.com/Formatting_codes"
				+ "\n\nYou can run many console commands on each line, each command" + "\nmust be separated by |"
				+ "\nThe player will have the cmd run in {mob-cmd-run-frequency} out of"
				+ "\n{mob-cmd-run-frequency-base} times in average. If mob-cmd-run-frequency=0 it"
				+ "\nwill never run. If f.ex. mob-cmd-run-frequency=50 and "
				+ "\nmob-cmd-run-frequency-base=100 it will run run every second time.");

		setCategoryComment("boss",
				"########################################################################"
						+ "\nRewards for killing bosses"
						+ "\n########################################################################"
						+ "\nHere is where you set the base prize in $ for killing the bosses");

		setCategoryComment("villager",
				"########################################################################"
						+ "\nRewards for killing villagers"
						+ "\n########################################################################"
						+ "\nHere is where you set the base prize in $ for killing the villagers");

		setCategoryComment("passive",
				"########################################################################"
						+ "\nRewards for killing passive mobs"
						+ "\n########################################################################"
						+ "\nHere is where you set the base prize in $ for killing passive/friendly mobs."
						+ "\nBy default the player does not get a reward for killing friendly mobs."
						+ "\nIf you make the number negative, the reward will be a fine for killing a passive animal.");

		setCategoryComment("fishing",
				"########################################################################" + "\nRewards for fishing"
						+ "\n########################################################################"
						+ "\nHere is where you set the base prize in $ for catching a fish");

		setCategoryComment("bonus", "########################################################################"
				+ "\n Bonus multipliers" + "\n########################################################################"
				+ "\nThese are bonus multipliers that can modify the base prize. "
				+ "\nREMEMBER: These are not in $ but they are a multiplier. " + "\nSetting to 1 will disable them.");

		setCategoryComment("penalty", "########################################################################"
				+ "\nPenalty multipliers" + "\n########################################################################"
				+ "\nThese are penalty multipliers that can modify the base prize. "
				+ "\nREMEMBER: These are not in $ but they are a multiplier. " + "\nSetting to 1 will disable them.");

		setCategoryComment("achievements", "########################################################################"
				+ "\nSpecial / Achievements rewards"
				+ "\n########################################################################"
				+ "\nHere is where you set the prize in $ for achieving a special kill. "
				+ "\nFor each achievment you can run a console command to give the player a reward. "
				+ "\nYou can use the following variables {player},{world}."
				+ "\nAn example could be to give the player permission to fly "
				+ "\nfor 1 hour or use give command to the player items."
				+ "\nYou can also specify the message send to the player."
				+ "\nYou can run many console commands on each line, each command" + "\nmust be separated by |"
				+ "\nAchievements will not be shown if the prize is 0, unless you set show-achievements-without-reward=true.");

		setCategoryComment("achievement_levels",
				"########################################################################"
						+ "\n Achievement Hunter levels"
						+ "\n########################################################################"
						+ "\nHere is where you set how many mobs to kill to reach next level per mob."
						+ "\nYou can only set the number of mobs to kill to reach level 1. the next"
						+ "\nlevels is automatically calculated this way." + "\nLevel 1: 100   (100 kills)"
						+ "\nLevel 2: x 2.5 (250 kills)" + "\nLevel 3: x 5   (500 kills)"
						+ "\nLevel 4: x 10  (1000 kills)" + "\nLevel 5: x 25  (2500 kills)"
						+ "\nLevel 6: x 50  (5000 kills)" + "\nLevel 7: x 100 (10000 kills)"
						+ "\nLevel Achievements can be disabled by setting the number to 0");

		setCategoryComment("assists",
				"########################################################################"
						+ "\nRewards for assisting killings"
						+ "\n########################################################################"
						+ "\nThey players can get an extra reward if they help each other killing mobs.");

		setCategoryComment("killstreak",
				"########################################################################"
						+ "\nReward for kills in a row"
						+ "\n########################################################################"
						+ "\nSet the multiplier when the player kills 1,2,3,4 mob in a row without getting damage."
						+ "\nKillstreak will be disabled if you set the multiplier: 1.0");

		setCategoryComment("multiplier", "########################################################################"
				+ "\nRank multipliers and world difficulty multipliers"
				+ "\n########################################################################"
				+ "\nYou can add multipliers for players with different ranks/groups. To do this"
				+ "\nyou must set give the user/group permissions with a format like this:"
				+ "\nmobhunting.multiplier.guest" + "\nmobhunting.multiplier.guardian" + "\nmobhunting.multiplier.staff"
				+ "\nmobhunting.multiplier.hasVoted" + "\nmobhunting.multiplier.donator"
				+ "\nmobhunting.multiplier.op <---- Notice 'op' is reserved for OP'ed players!"
				+ "\nOP'ed players will only get the OP multiplier"
				+ "\nyou can make your own permission nodes. You just need to keep the format"
				+ "\nmobhunting.multiplier.name 'value' in your permissions file and the "
				+ "format below in this file.");

		setCategoryComment("pvp", "########################################################################"
				+ "\nPvp rewards" + "\n########################################################################"
				+ "\nPvp configuration. Set pvp-allowed = true if you want give the players a reward when they kill eachother."
				+ "\nYou can alsp run a console command when this happens to give the player a reward or punish him."
				+ "\nYou can you the following variables {player},{world},{killed_player}."
				+ "\nAn example could be to give the player permission to fly "
				+ "\nfor 1 hour or use give command to the player items."
				+ "\nYou can also specify the message send to the player."
				+ "\nYou can run many console commands on each line, each command" + "\nmust be separated by |");

		setCategoryComment("disguises",
				"########################################################################" + "\nDisguises rewards"
						+ "\n########################################################################"
						+ "\nHere is where can define the actions when a player is under disguise (attacker)"
						+ "\n or when the attacked (victim)");

		setCategoryComment("npc",
				"########################################################################"
						+ "\nNPC / Citizens / MasterMobHunter settings."
						+ "\n########################################################################");
		setCategoryComment("bounties",
				"########################################################################" + "\nBounty settings"
						+ "\n########################################################################"
						+ "\nHere you can change the behavior of the Bounty Command or you can disable"
						+ "\nthe command completely.");

		setCategoryComment("stackedmobs",
				"########################################################################" + "\nStacked mobs settings"
						+ "\n########################################################################"
						+ "\nHere you can change the behavior of stacked mobs integration, or you can disable"
						+ "\nintegration completely.");

		setCategoryComment("custommobs",
				"########################################################################" + "\nCustomMob settings"
						+ "\n########################################################################"
						+ "\nHere you can change the behavior of CustomMobs Integration, or you can disable"
						+ "\nintegration completely." + "\nhttps://www.spigotmc.org/resources/custommobs.7339/");

		setCategoryComment("conquestiamobs",
				"########################################################################"
						+ "\nConquestia Mobs settings"
						+ "\n########################################################################"
						+ "\nHere you can change the behavior of ConquestiaMobs Integration, or you can disable"
						+ "\nintegration completely." + "\nhttps://www.spigotmc.org/resources/conquesita-mobs.21307/");

		setCategoryComment("grinding",
				"########################################################################"
						+ "\nGrinding detection settings"
						+ "\n########################################################################"
						+ "\nHere you can change the behavior of the grinding detection.");

		setCategoryComment("plugins",
				"########################################################################"
						+ "\nIntegration to other plugins."
						+ "\n########################################################################");

		setCategoryComment("dropmoneyonground",
				"########################################################################"
						+ "\nDropMoneyOnGround Settings"
						+ "\n########################################################################");
		setCategoryComment("database",
				"########################################################################" + "\nDatabase Settings."
						+ "\n########################################################################");

		setCategoryComment("updates", "########################################################################"
				+ "\nUpdate settings" + "\n########################################################################");

		setCategoryComment("general", "########################################################################"
				+ "\nGeneral Setting." + "\n########################################################################");

	}

	// #####################################################################################
	// Mobs
	// #####################################################################################
	@ConfigField(name = "blaze", category = "mobs")
	public String blazePrize = "10.0";
	@ConfigField(name = "blaze-cmd", category = "mobs")
	public String blazeCmd = "mobhunt head give {player} Blaze|give {player} iron_ingot 1";
	@ConfigField(name = "blaze-cmd-desc", category = "mobs")
	public String blazeCmdDesc = "You got a Blaze skull and an Iron ingot.";
	@ConfigField(name = "blaze-cmd-run-frequency", category = "mobs")
	public int blazeFrequency = 10;
	@ConfigField(name = "blaze-cmd-run-frequency-base", category = "mobs")
	public int blazeFrequencyBase = 100;

	@ConfigField(name = "cave-spider", category = "mobs")
	public String caveSpiderPrize = "10:20";
	@ConfigField(name = "cave-spider-cmd", category = "mobs")
	public String caveSpiderCmd = "mobhunt head give {player} Cave_Spider|give {player} iron_ingot 1";
	@ConfigField(name = "cave-spider-cmd-desc", category = "mobs")
	public String caveSpiderCmdDesc = "You got a Cave Spider skull and an Iron ingot.";
	@ConfigField(name = "cave-spider-cmd-run-frequency", category = "mobs")
	public int caveSpiderFrequency = 10;
	@ConfigField(name = "cave-spider-cmd-run-frequency-base", category = "mobs")
	public int caveSpiderFrequencyBase = 100;

	@ConfigField(name = "creeper", category = "mobs")
	public String creeperPrize = "10.0";
	@ConfigField(name = "creeper-cmd", category = "mobs")
	public String creeperCmd = "mobhunt head give {player} Creeper|give {player} iron_ingot 1";
	@ConfigField(name = "creeper-cmd-desc", category = "mobs")
	public String creeperCmdDesc = "You got a Creeper skull and an Iron ingot.";
	@ConfigField(name = "creeper-cmd-run-frequency", category = "mobs")
	public int creeperFrequency = 5;
	@ConfigField(name = "creeper-cmd-run-frequency-base", category = "mobs")
	public int creeperFrequencyBase = 100;

	@ConfigField(name = "elder-guardian", category = "mobs")
	public String elderGuardianPrize = "40:80";
	@ConfigField(name = "elder-guardian-cmd", category = "mobs")
	public String elderGuardianCmd = "mobhunt head give {player} ElderGuardian|give {player} iron_ingot 1";
	@ConfigField(name = "elder-guardian-cmd-desc", category = "mobs")
	public String elderGuardianCmdDesc = "You got a Elder Guardian skull and an Iron ingot.";
	@ConfigField(name = "elder-guardian-cmd-run-frequency", category = "mobs")
	public int elderGuardianFrequency = 33;
	@ConfigField(name = "elder-guardian-cmd-run-frequency-base", category = "mobs")
	public int elderGuardianFrequencyBase = 100;

	@ConfigField(name = "enderman", category = "mobs")
	public String endermanPrize = "20:40";
	@ConfigField(name = "enderman-cmd", category = "mobs")
	public String endermanCmd = "mobhunt head give {player} Enderman|give {player} gold_ingot 1";
	@ConfigField(name = "enderman-cmd-desc", category = "mobs")
	public String endermanCmdDesc = "You got a Enderman skull and an Gold ingot.";
	@ConfigField(name = "enderman-cmd-run-frequency", category = "mobs")
	public int endermanFrequency = 20;
	@ConfigField(name = "enderman-cmd-run-frequency-base", category = "mobs")
	public int endermanFrequencyBase = 100;

	@ConfigField(name = "endermite", category = "mobs")
	public String endermitePrize = "10";
	@ConfigField(name = "endermite-cmd", category = "mobs")
	public String endermiteCmd = "mobhunt head give {player} Endermite|give {player} iron_ingot 1";
	@ConfigField(name = "endermite-cmd-desc", category = "mobs")
	public String endermiteCmdDesc = "You got a Endermite skull and an Iron ingot.";
	@ConfigField(name = "endermite-cmd-run-frequency", category = "mobs")
	public int endermiteFrequency = 10;
	@ConfigField(name = "endermite-cmd-run-frequency-base", category = "mobs")
	public int endermiteFrequencyBase = 100;

	@ConfigField(name = "evoker", category = "mobs")
	public String evokerPrize = "10";
	@ConfigField(name = "evoker-cmd", category = "mobs")
	public String evokerCmd = "mobhunt head give {player} evoker|give {player} iron_ingot 1";
	@ConfigField(name = "evoker-cmd-desc", category = "mobs")
	public String evokerCmdDesc = "You got a Evoker skull and an Iron ingot.";
	@ConfigField(name = "evoker-cmd-run-chance", category = "mobs")
	public double evokerCmdRunChance = 0.50;

	@ConfigField(name = "ghast", category = "mobs")
	public String ghastPrize = "40:80";
	@ConfigField(name = "ghast-cmd", category = "mobs")
	public String ghastCmd = "mobhunt head give {player} Ghast|give {player} 406 4";
	@ConfigField(name = "ghast-cmd-desc", category = "mobs")
	public String ghastCmdDesc = "You got a Ghast skull and 4 Nether Quartz.";
	@ConfigField(name = "ghast-cmd-run-frequency", category = "mobs")
	public int ghastFrequency = 10;
	@ConfigField(name = "ghast-cmd-run-frequency-base", category = "mobs")
	public int ghastFrequencyBase = 100;

	@ConfigField(name = "giant", category = "mobs")
	public String giantPrize = "5.0";
	@ConfigField(name = "giant-cmd", category = "mobs")
	public String giantCmd = "mobhunt head give {player} Giant|give {player} iron_ingot 1";
	@ConfigField(name = "giant-cmd-desc", category = "mobs")
	public String giantCmdDesc = "You got a Giant head and an iron ingot.";
	@ConfigField(name = "giant-cmd-run-frequency", category = "mobs")
	public int giantFrequency = 5;
	@ConfigField(name = "giant-cmd-run-frequency-base", category = "mobs")
	public int giantFrequencyBase = 100;

	@ConfigField(name = "iron-golem", category = "mobs")
	public String ironGolemPrize = "20:40";
	@ConfigField(name = "iron-golem-cmd", category = "mobs")
	public String ironGolemCmd = "mobhunt head give {player} Iron_Golem|give {player} iron_ingot 4";
	@ConfigField(name = "iron-golem-cmd-desc", category = "mobs")
	public String ironGolemCmdDesc = "You got an Iron Golem skull and 4 Iron ingot.";
	@ConfigField(name = "iron-golem-cmd-run-frequency", category = "mobs")
	public int ironGolemFrequency = 10;
	@ConfigField(name = "iron-golem-cmd-run-frequency-base", category = "mobs")
	public int ironGolemFrequencyBase = 100;

	@ConfigField(name = "guardian", category = "mobs")
	public String guardianPrize = "20:40";
	@ConfigField(name = "guardian-cmd", category = "mobs")
	public String guardianCmd = "mobhunt head give {player} Guardian|give {player} iron_ingot 1";
	@ConfigField(name = "guardian-cmd-desc", category = "mobs")
	public String guardianCmdDesc = "You got a Guardian skull and an Iron ingot.";
	@ConfigField(name = "guardian-cmd-run-frequency", category = "mobs")
	public int guardianFrequency = 10;
	@ConfigField(name = "guardian-cmd-run-frequency-base", category = "mobs")
	public int guardianFrequencyBase = 100;

	@ConfigField(name = "husk", category = "mobs")
	public String huskPrize = "9:13";
	@ConfigField(name = "husk-cmd", category = "mobs")
	public String huskCmd = "mobhunt head give {player} Husk|give {player} iron_ingot 1";
	@ConfigField(name = "husk-cmd-desc", category = "mobs")
	public String huskCmdDesc = "You got a Zombie Husk skull and an Iron ingot.";
	@ConfigField(name = "husk-cmd-run-frequency", category = "mobs")
	public int huskFrequency = 50;
	@ConfigField(name = "husk-cmd-run-frequency-base", category = "mobs")
	public int huskFrequencyBase = 100;

	@ConfigField(name = "killerrabbit", category = "mobs")
	public String killerrabbitPrize = "200";
	@ConfigField(name = "killerrabbit-cmd", category = "mobs")
	public String killerrabbitCmd = "mobhunt head give {player} KillerRabbit|give {player} iron_ingot 1";
	@ConfigField(name = "killerrabbit-cmd-desc", category = "mobs")
	public String killerrabbitCmdDesc = "You got Killer Rabbit Skull and an iron ingot.";
	@ConfigField(name = "killerrabbit-cmd-run-frequency", category = "mobs")
	public int killerrabbitFrequency = 25;
	@ConfigField(name = "killerrabbit-cmd-run-frequency-base", category = "mobs")
	public int killerrabbitFrequencyBase = 100;

	@ConfigField(name = "magma-cube", category = "mobs", comment = "This is multiplied by the size of the magma cube. So a big natural magma cube is 4x this value")
	public String magmaCubePrize = "10:20";
	@ConfigField(name = "magma-cube-cmd", category = "mobs")
	public String magmaCubeCmd = "mobhunt head give {player} MAGMA_CUBE|give {player} iron_ingot 1";
	@ConfigField(name = "magma-cube-cmd-desc", category = "mobs")
	public String magmaCubeCmdDesc = "You got a Magma Cube skull and an Iron ingot.";
	@ConfigField(name = "magma-cube-cmd-run-frequency", category = "mobs")
	public int magmaCubeFrequency = 10;
	@ConfigField(name = "magma-cube-cmd-run-frequency-base", category = "mobs")
	public int magmaCubeFrequencyBase = 100;

	@ConfigField(name = "polar-bear", category = "mobs")
	public String polarBearPrize = "25";
	@ConfigField(name = "polar-bear-cmd", category = "mobs")
	public String polarBearCmd = "mobhunt head give {player} Polar_Bear|give {player} iron_ingot 1";
	@ConfigField(name = "polar-bear-cmd-desc", category = "mobs")
	public String polarBearCmdDesc = "You got a Polar Bear skull and an Iron ingot.";
	@ConfigField(name = "polar-bear-cmd-run-frequency", category = "mobs")
	public int polarBearFrequency = 50;
	@ConfigField(name = "polar-bear-cmd-run-frequency-base", category = "mobs")
	public int polarBearFrequencyBase = 100;

	@ConfigField(name = "slime-base", category = "mobs", comment = "This is multiplied by the size of the slime. So a big natural slime is 4x this value")
	public String slimeTinyPrize = "25";
	@ConfigField(name = "slime-cmd", category = "mobs")
	public String slimeCmd = "mobhunt head give {player} Slime|give {player} iron_ingot 1";
	@ConfigField(name = "slime-cmd-desc", category = "mobs")
	public String slimeCmdDesc = "You got a Slime skull and an Iron ingot.";
	@ConfigField(name = "slime-cmd-run-frequency", category = "mobs")
	public int slimeFrequency = 5;
	@ConfigField(name = "slime-cmd-run-frequency-base", category = "mobs")
	public int slimeFrequencyBase = 100;

	@ConfigField(name = "shulker", category = "mobs")
	public String shulkerPrize = "25";
	@ConfigField(name = "shulker-cmd", category = "mobs")
	public String shulkerCmd = "mobhunt head give {player} Shulker|give {player} iron_ingot 1";
	@ConfigField(name = "shulker-cmd-desc", category = "mobs")
	public String shulkerCmdDesc = "You got a Shulker skull and an Iron ingot.";
	@ConfigField(name = "shulker-cmd-run-frequency", category = "mobs")
	public int shulkerFrequency = 50;
	@ConfigField(name = "shulker-cmd-run-frequency-base", category = "mobs")
	public int shulkerFrequencyBase = 100;

	@ConfigField(name = "stray", category = "mobs")
	public String strayPrize = "15:35";
	@ConfigField(name = "stray-cmd", category = "mobs")
	public String strayCmd = "mobhunt head give {player} Stray|give {player} iron_ingot 1";
	@ConfigField(name = "stray-cmd-desc", category = "mobs")
	public String strayCmdDesc = "You got a Skeleton Stray skull and an Iron ingot.";
	@ConfigField(name = "stray-cmd-run-frequency", category = "mobs")
	public int strayFrequency = 50;
	@ConfigField(name = "stray-cmd-run-frequency-base", category = "mobs")
	public int strayFrequencyBase = 100;

	@ConfigField(name = "silverfish", category = "mobs")
	public String silverfishPrize = "10";
	@ConfigField(name = "silverfish-cmd", category = "mobs")
	public String silverfishCmd = "mobhunt head give {player} SilverFish|give {player} iron_ingot 1";
	@ConfigField(name = "silver-cmd-desc", category = "mobs")
	public String silverfishCmdDesc = "You got a SilverFish head and an iron ingot.";
	@ConfigField(name = "silverfish-cmd-run-frequency", category = "mobs")
	public int silverfishFrequency = 10;
	@ConfigField(name = "silverfish-cmd-run-frequency-base", category = "mobs")
	public int silverfishFrequencyBase = 100;

	@ConfigField(name = "skeleton", category = "mobs")
	public String skeletonPrize = "10:30";
	@ConfigField(name = "skeleton-cmd", category = "mobs")
	public String skeletonCmd = "mobhunt head give {player} Skeleton|give {player} dye 1 4";
	@ConfigField(name = "skeleton-cmd-desc", category = "mobs")
	public String skeletonCmdDesc = "You got 1 Skeleton skull and 1 Lapis Lazuli.";
	@ConfigField(name = "skeleton-cmd-run-frequency", category = "mobs")
	public int skeletonFrequency = 5;
	@ConfigField(name = "skeleton-cmd-run-frequency-base", category = "mobs")
	public int skeletonFrequencyBase = 100;

	@ConfigField(name = "spider", category = "mobs")
	public String spiderPrize = "5.5:10.5";
	@ConfigField(name = "spider-cmd", category = "mobs")
	public String spiderCmd = "mobhunt head give {player} Spider|give {player} iron_ingot 1";
	@ConfigField(name = "spider-cmd-desc", category = "mobs")
	public String spiderCmdDesc = "You got a Spider skull and an Iron ingot.";
	@ConfigField(name = "spider-cmd-run-frequency", category = "mobs")
	public int spiderFrequency = 5;
	@ConfigField(name = "spider-cmd-run-frequency-base", category = "mobs")
	public int spiderFrequencyBase = 100;

	@ConfigField(name = "zombie", category = "mobs")
	public String zombiePrize = "7:11";
	@ConfigField(name = "zombie-cmd", category = "mobs")
	public String zombieCmd = "mobhunt head give {player} Zombie|give {player} iron_ingot 1";
	@ConfigField(name = "zombie-cmd-desc", category = "mobs")
	public String zombieCmdDesc = "You got a Zombie skull and an Iron ingot.";
	@ConfigField(name = "zombie-cmd-run-frequency", category = "mobs")
	public int zombieFrequency = 50;
	@ConfigField(name = "zombie-cmd-run-frequency-base", category = "mobs")
	public int zombieFrequencyBase = 1000;

	@ConfigField(name = "zombie-pigman", category = "mobs")
	public String zombiePigmanPrize = "4:8";
	@ConfigField(name = "zombie-pigman-cmd", category = "mobs")
	public String zombiePigmanCmd = "mobhunt head give {player} Pig_Zombie|give {player} iron_ingot 1";
	@ConfigField(name = "zombie-pigman-cmd-desc", category = "mobs")
	public String zombiePigmanCmdDesc = "You got a Zombie Pigman skull and an Iron ingot.";
	@ConfigField(name = "zombie-pigman-cmd-run-frequency", category = "mobs")
	public int zombiePigmanFrequency = 10;
	@ConfigField(name = "zombie-pigman-cmd-run-frequency-base", category = "mobs")
	public int zombiePigmanFrequencyBase = 100;

	@ConfigField(name = "vex", category = "mobs")
	public String vexPrize = "10:15";
	@ConfigField(name = "vex-cmd", category = "mobs")
	public String vexCmd = "mobhunt head give {player} Witch|give {player} gold_ingot 1";
	@ConfigField(name = "vex-cmd-desc", category = "mobs")
	public String vexCmdDesc = "You got a Vex skull and an Gold ingot.";
	@ConfigField(name = "vex-cmd-run-chance", category = "mobs")
	public double vexCmdRunChance = 0.5;

	@ConfigField(name = "vindicator", category = "mobs")
	public String vindicatorPrize = "10:15";
	@ConfigField(name = "vindicator-cmd", category = "mobs")
	public String vindicatorCmd = "mobhunt head give {player} Witch|give {player} gold_ingot 1";
	@ConfigField(name = "vindicator-cmd-desc", category = "mobs")
	public String vindicatorCmdDesc = "You got a Vindicator skull and an Gold ingot.";
	@ConfigField(name = "vindicator-cmd-run-chance", category = "mobs")
	public double vindicatorCmdRunChance = 0.05;

	@ConfigField(name = "witch", category = "mobs")
	public String witchPrize = "10:15";
	@ConfigField(name = "witch-cmd", category = "mobs")
	public String witchCmd = "mobhunt head give {player} Witch|give {player} gold_ingot 1";
	@ConfigField(name = "witch-cmd-desc", category = "mobs")
	public String witchCmdDesc = "You got a Witch skull and an Gold ingot.";
	@ConfigField(name = "witch-cmd-run-frequency", category = "mobs")
	public int witchFrequency = 5;
	@ConfigField(name = "witch-cmd-run-frequency-base", category = "mobs")
	public int witchFrequencyBase = 100;

	@ConfigField(name = "wither-skeleton", category = "mobs")
	public String witherSkeletonPrize = "30:50";
	@ConfigField(name = "wither-skeleton-cmd", category = "mobs")
	public String witherSkeletonCmd = "mobhunt head give {player} WitherSkeleton|give {player} gold_ingot 1";
	@ConfigField(name = "wither-skeleton-cmd-desc", category = "mobs")
	public String witherSkeletonCmdDesc = "You got 1 Wither Skeleton skull and 1 Gold ingot.";
	@ConfigField(name = "wither-skeleton-cmd-run-frequency", category = "mobs")
	public int witherSkeletonFrequency = 10;
	@ConfigField(name = "wither-skeleton-cmd-run-frequency-base", category = "mobs")
	public int witherSkeletonFrequencyBase = 100;

	// #####################################################################################
	// Bosses
	// #####################################################################################
	@ConfigField(name = "wither", category = "boss")
	public String witherPrize = "1000.0:2000.0";
	@ConfigField(name = "wither-cmd", category = "boss")
	public String witherCmd = "mobhunt head give {player} Wither|give {player} diamond 10";
	@ConfigField(name = "wither-cmd-desc", category = "boss")
	public String witherCmdDesc = "You got a Wither skull and 10 Diamonds.";
	@ConfigField(name = "wither-cmd-run-frequency", category = "boss")
	public int witherFrequency = 100;
	@ConfigField(name = "wither-cmd-run-frequency-base", category = "boss")
	public int witherFrequencyBase = 100;

	@ConfigField(name = "enderdragon", category = "boss")
	public String enderdragonPrize = "2000.0:5000.0";
	@ConfigField(name = "enderdragon-cmd", category = "boss")
	public String enderdragonCmd = "mobhunt head give {player} Enderdragon|give {player} diamond 10";
	@ConfigField(name = "enderdragon-cmd-desc", category = "boss")
	public String enderdragonCmdDesc = "You got a Enderdragon skull and 10 Diamonds.";
	@ConfigField(name = "enderdragon-cmd-run-frequency", category = "boss")
	public int enderdragonFrequency = 100;
	@ConfigField(name = "enderdragon-cmd-run-frequency-base", category = "boss")
	public int enderdragonFrequencyBase = 100;

	// Usage: /summon <EntityName> [x] [y] [z] [dataTag]
	// Try this!!!! /summon Minecart ~ ~ ~20 {Riding:{id:EnderDragon}}
	// Then enter to the minecart
	// WITH THAT YOU CAN RIDE AN ENDERDRAGON!!!

	// /summon Minecart ~ ~ ~ {Riding:{Creeper,Riding:{id:Ozelot}}}
	// ...Yes..Ocelot need to be spelled Ozelot..

	// /summon Skeleton ~ ~ ~
	// {Riding:{id:Spider},Equipment:[{id:57},{id:310},{id:310},{id:310},{id:310}]}

	// #####################################################################################
	// Villagers
	// #####################################################################################
	@ConfigField(name = "cartographer", category = "villager")
	public String cartographerPrize = "1:2";
	@ConfigField(name = "cartographer-cmd", category = "villager")
	public String cartographerCmd = "";
	@ConfigField(name = "cartographer-cmd-desc", category = "villager")
	public String cartographerCmdDesc = "";
	@ConfigField(name = "cartographer-cmd-run-chance", category = "villager")
	public double cartographerCmdRunChance = 1.00;

	@ConfigField(name = "nitwit", category = "villager")
	public String nitwitPrize = "1:2";
	@ConfigField(name = "nitwit-cmd", category = "villager")
	public String nitwitCmd = "";
	@ConfigField(name = "nitwit-cmd-desc", category = "villager")
	public String nitwitCmdDesc = "";
	@ConfigField(name = "nitwit-cmd-run-chance", category = "villager")
	public double nitwitCmdRunChance = 1.00;

	@ConfigField(name = "farmer", category = "villager")
	public String farmerPrize = "1:2";
	@ConfigField(name = "farmer-cmd", category = "villager")
	public String farmerCmd = "";
	@ConfigField(name = "farmer-cmd-desc", category = "villager")
	public String farmerCmdDesc = "";
	@ConfigField(name = "farmer-cmd-run-chance", category = "villager")
	public double farmerCmdRunChance = 1.00;

	@ConfigField(name = "priest", category = "villager")
	public String priestPrize = "1:2";
	@ConfigField(name = "priest-cmd", category = "villager")
	public String priestCmd = "";
	@ConfigField(name = "priest-cmd-desc", category = "villager")
	public String priestCmdDesc = "";
	@ConfigField(name = "priest-cmd-run-chance", category = "villager")
	public double priestCmdRunChance = 1.00;

	@ConfigField(name = "blacksmith", category = "villager")
	public String blacksmithPrize = "1:2";
	@ConfigField(name = "blacksmith-cmd", category = "villager")
	public String blacksmithCmd = "";
	@ConfigField(name = "blacksmith-cmd-desc", category = "villager")
	public String blacksmithCmdDesc = "";
	@ConfigField(name = "blacksmith-cmd-run-chance", category = "villager")
	public double blacksmithCmdRunChance = 1.00;

	@ConfigField(name = "butcher", category = "villager")
	public String butcherPrize = "1:2";
	@ConfigField(name = "butcher-cmd", category = "villager")
	public String butcherCmd = "";
	@ConfigField(name = "butcher-cmd-desc", category = "villager")
	public String butcherCmdDesc = "";
	@ConfigField(name = "butcher-cmd-run-chance", category = "villager")
	public double butcherCmdRunChance = 1.00;

	@ConfigField(name = "librarian", category = "villager")
	public String librarianPrize = "1:2";
	@ConfigField(name = "librarian-cmd", category = "villager")
	public String librarianCmd = "";
	@ConfigField(name = "librarian-cmd-desc", category = "villager")
	public String librarianCmdDesc = "";
	@ConfigField(name = "librarian-cmd-run-chance", category = "villager")
	public double librarianCmdRunChance = 1.00;

	@ConfigField(name = "villager", category = "villager")
	public String villagerPrize = "1";
	@ConfigField(name = "villager-cmd", category = "villager")
	public String villagerCmd = "mobhunt head give {player} Villager";
	@ConfigField(name = "villager-cmd-desc", category = "villager")
	public String villagerCmdDesc = "You got a Villager Skull";
	@ConfigField(name = "villager-cmd-run-frequency", category = "villager")
	public int villagerFequency = 0;
	@ConfigField(name = "villager-cmd-run-frequency-base", category = "villager")
	public int villagerFrequencyBase = 100;

	@ConfigField(name = "zombie-villager", category = "villager")
	public String zombieVillagerPrize = "1:2";
	@ConfigField(name = "zombie-villager-cmd", category = "villager")
	public String zombieVillagerCmd = "";
	@ConfigField(name = "zombie-villager-cmd-desc", category = "villager")
	public String zombieVillagerCmdDesc = "";
	@ConfigField(name = "zombie-villager-cmd-run-chance", category = "villager")
	public double zombieVillagerCmdRunChance = 1.00;

	// #####################################################################################
	// Passive Mobs
	// #####################################################################################
	@ConfigField(name = "bat", category = "passive")
	public String batPrize = "0";
	@ConfigField(name = "bat-cmd", category = "passive")
	public String batCmd = "mobhunt head give {player} Bat";
	@ConfigField(name = "bat-cmd-desc", category = "passive")
	public String batCmdDesc = "You got a Bat Skull";
	@ConfigField(name = "bat-cmd-run-frequency", category = "passive")
	public int batFrequency = 0;
	@ConfigField(name = "bat-cmd-run-frequency-base", category = "passive")
	public int batFrequencyBase = 100;

	@ConfigField(name = "chicken", category = "passive")
	public String chickenPrize = "0";
	@ConfigField(name = "chicken-cmd", category = "passive")
	public String chickenCmd = "mobhunt head give {player} Chicken";
	@ConfigField(name = "chicken-cmd-desc", category = "passive")
	public String chickenCmdDesc = "You got a Chicken Skull";
	@ConfigField(name = "chicken-cmd-run-frequency", category = "passive")
	public int chickenFrequency = 0;
	@ConfigField(name = "chicken-cmd-run-frequency-base", category = "passive")
	public int chickenFrequencyBase = 100;

	@ConfigField(name = "cow", category = "passive")
	public String cowPrize = "5";
	@ConfigField(name = "cow-cmd", category = "passive")
	public String cowCmd = "mobhunt head give {player} Cow";
	@ConfigField(name = "cow-cmd-desc", category = "passive")
	public String cowCmdDesc = "You got a Cow Skull";
	@ConfigField(name = "cow-cmd-run-frequency", category = "passive")
	public int cowFrequency = 0;
	@ConfigField(name = "cow-cmd-run-frequency-base", category = "passive")
	public int cowFrequencyBase = 100;

	@ConfigField(name = "donkey", category = "passive")
	public String donkeyPrize = "5";
	@ConfigField(name = "donkey-cmd", category = "passive")
	public String donkeyCmd = "mobhunt head give {player} donkey";
	@ConfigField(name = "donkey-cmd-desc", category = "passive")
	public String donkeyCmdDesc = "You got a Donkey Skull";
	@ConfigField(name = "donkey-cmd-run-chance", category = "passive")
	public double donkeyCmdRunChance = 0.00;

	@ConfigField(name = "horse", category = "passive")
	public String horsePrize = "0";
	@ConfigField(name = "horse-cmd", category = "passive")
	public String horseCmd = "mobhunt head give {player} Horse";
	@ConfigField(name = "horse-cmd-desc", category = "passive")
	public String horseCmdDesc = "You got a Horse Skull";
	@ConfigField(name = "horse-cmd-run-frequency", category = "passive")
	public int horseFrequency = 0;
	@ConfigField(name = "horse-cmd-run-frequency-base", category = "passive")
	public int horseFrequencyBase = 100;

	@ConfigField(name = "llama", category = "passive")
	public String llamaPrize = "0";
	@ConfigField(name = "llama-cmd", category = "passive")
	public String llamaCmd = "mobhunt head give {player} llama";
	@ConfigField(name = "llama-cmd-desc", category = "passive")
	public String llamaCmdDesc = "You got a Llama Skull";
	@ConfigField(name = "llama-cmd-run-chance", category = "passive")
	public double llamaCmdRunChance = 0.00;

	@ConfigField(name = "mule", category = "passive")
	public String mulePrize = "0";
	@ConfigField(name = "mule-cmd", category = "passive")
	public String muleCmd = "mobhunt head give {player} mule";
	@ConfigField(name = "mule-cmd-desc", category = "passive")
	public String muleCmdDesc = "You got a Mule Skull";
	@ConfigField(name = "mule-cmd-run-chance", category = "passive")
	public double muleCmdRunChance = 0.00;

	@ConfigField(name = "mushroom-cow", category = "passive")
	public String mushroomCowPrize = "0";
	@ConfigField(name = "mushroom-cow-cmd", category = "passive")
	public String mushroomCowCmd = "mobhunt head give {player} Mushroom_Cow";
	@ConfigField(name = "mushroom-cow-cmd-desc", category = "passive")
	public String mushroomCowCmdDesc = "You got a Mushroom Cow Skull";
	@ConfigField(name = "mushroom-cow-cmd-run-frequency", category = "passive")
	public int mushroomCowFrequency = 0;
	@ConfigField(name = "mushroom-cow-cmd-run-frequency-base", category = "passive")
	public int mushroomCowFrequencyBase = 100;

	@ConfigField(name = "ocelot", category = "passive")
	public String ocelotPrize = "0";
	@ConfigField(name = "ocelot-cmd", category = "passive")
	public String ocelotCmd = "mobhunt head give {player} Ocelot";
	@ConfigField(name = "ocelot-cmd-desc", category = "passive")
	public String ocelotCmdDesc = "";
	@ConfigField(name = "ocelot-cmd-run-frequency", category = "passive")
	public int ocelotFrequency = 0;
	@ConfigField(name = "ocelot-cmd-run-frequency-base", category = "passive")
	public int ocelotFrequencyBase = 100;

	@ConfigField(name = "pig", category = "passive")
	public String pigPrize = "0";
	@ConfigField(name = "pig-cmd", category = "passive")
	public String pigCmd = "mobhunt head give {player} Pig";
	@ConfigField(name = "pig-cmd-desc", category = "passive")
	public String pigCmdDesc = "You got a Pig Skull";
	@ConfigField(name = "pig-cmd-run-frequency", category = "passive")
	public int pigFrequency = 0;
	@ConfigField(name = "pig-cmd-run-frequency-base", category = "passive")
	public int pigFrequencyBase = 100;

	@ConfigField(name = "rabbit", category = "passive")
	public String rabbitPrize = "0";
	@ConfigField(name = "rabbit-cmd", category = "passive")
	public String rabbitCmd = "mobhunt head give {player} Rabbit";
	@ConfigField(name = "rabbit-cmd-desc", category = "passive")
	public String rabbitCmdDesc = "You got a Rabbit Skull";
	@ConfigField(name = "rabbit-cmd-run-frequency", category = "passive")
	public int rabbitFrequency = 0;
	@ConfigField(name = "rabbit-cmd-run-frequency-base", category = "passive")
	public int rabbitFrequencyBase = 100;

	@ConfigField(name = "sheep", category = "passive")
	public String sheepPrize = "0";
	@ConfigField(name = "sheep-cmd", category = "passive")
	public String sheepCmd = "mobhunt head give {player} Sheep";
	@ConfigField(name = "sheep-cmd-desc", category = "passive")
	public String sheepCmdDesc = "You got a Sheep Skull";
	@ConfigField(name = "sheep-cmd-run-frequency", category = "passive")
	public int sheepFrequency = 0;
	@ConfigField(name = "sheep-cmd-run-frequency-base", category = "passive")
	public int sheepFrequencyBase = 100;

	@ConfigField(name = "skeletonhorse", category = "passive")
	public String skeletonhorsePrize = "-10";
	@ConfigField(name = "skeletonhorse-cmd", category = "passive")
	public String skeletonhorseCmd = "mobhunt head give {player} skeletonhorse";
	@ConfigField(name = "skeletonhorse-cmd-desc", category = "passive")
	public String skeletonhorseCmdDesc = "You got a SkeletonHorse Skull";
	@ConfigField(name = "skeletonhorse-cmd-run-chance", category = "passive")
	public double skeletonhorseCmdRunChance = 0.00;

	@ConfigField(name = "snowman", category = "passive")
	public String snowmanPrize = "0";
	@ConfigField(name = "snowman-cmd", category = "passive")
	public String snowmanCmd = "mobhunt head give {player} SnowMan";
	@ConfigField(name = "snowman-cmd-desc", category = "passive")
	public String snowmanCmdDesc = "You got a Snowman Skull";
	@ConfigField(name = "snowman-cmd-run-frequency", category = "passive")
	public int snowmanFrequency = 0;
	@ConfigField(name = "snowman-cmd-run-frequency-base", category = "passive")
	public int snowmanFrequencyBase = 100;

	@ConfigField(name = "squid", category = "passive")
	public String squidPrize = "0";
	@ConfigField(name = "squid-cmd", category = "passive")
	public String squidCmd = "mobhunt head give {player} Squid";
	@ConfigField(name = "squid-cmd-desc", category = "passive")
	public String squidCmdDesc = "You got a Squid Skull";
	@ConfigField(name = "squid-cmd-run-frequency", category = "passive")
	public int squidFrequency = 0;
	@ConfigField(name = "squid-cmd-run-frequency-base", category = "passive")
	public int squidFrequencyBase = 100;

	@ConfigField(name = "wolf", category = "passive")
	public String wolfPrize = "-10";
	@ConfigField(name = "wolf-cmd", category = "passive")
	public String wolfCmd = "mobhunt head give {player} Wolf";
	@ConfigField(name = "wolf-cmd-desc", category = "passive")
	public String wolfCmdDesc = "You got a Wolf Skull";
	@ConfigField(name = "wolf-cmd-run-frequency", category = "passive")
	public int wolfFequency = 0;
	@ConfigField(name = "wolf-cmd-run-frequency-base", category = "passive")
	public int wolfFrequencyBase = 100;

	@ConfigField(name = "zombiehorse", category = "passive")
	public String zombiehorsePrize = "-10";
	@ConfigField(name = "zombiehorse-cmd", category = "passive")
	public String zombiehorseCmd = "mobhunt head give {player} zombiehorse";
	@ConfigField(name = "zombiehorse-cmd-desc", category = "passive")
	public String zombiehorseCmdDesc = "You got a ZombieHorse Skull";
	@ConfigField(name = "zombiehorse-cmd-run-chance", category = "passive")
	public double zombiehorseCmdRunChance = 0.25;

	// #####################################################################################
	// Fish / Fishing
	// #####################################################################################
	@ConfigField(name = "disable-fishing-rewards", category = "fishing", comment = "Set this to true if you want to disable all fishing rewards / features.")
	public boolean disableFishingRewards = false;

	@ConfigField(name = "rawfish", category = "fishing")
	public String rawFishPrize = "1:3";
	@ConfigField(name = "rawfish-cmd", category = "fishing")
	public String rawFishCmd = "mobhunt head give {player} rawfish Raw_Fish 1 silent";
	@ConfigField(name = "rawfish-cmd-desc", category = "fishing")
	public String rawFishCmdDesc = "You got a head of a Raw Fish";
	@ConfigField(name = "rawfish-cmd-run-chance", category = "fishing")
	public double rawFishCmdRunChance = 0.05;

	@ConfigField(name = "rawsalmon", category = "fishing")
	public String rawSalmonPrize = "2:8";
	@ConfigField(name = "rawsalmon-cmd", category = "fishing")
	public String rawSalmonCmd = "mobhunt head give {player} rawsalmon Raw_Salmon 1 silent";
	@ConfigField(name = "rawsalmon-cmd-desc", category = "fishing")
	public String rawSalmonCmdDesc = "You got a head of a Raw Salmon";
	@ConfigField(name = "rawsalmon-cmd-run-chance", category = "fishing")
	public double rawSalmonCmdRunChance = 0.1;

	@ConfigField(name = "clownfish", category = "fishing")
	public String clownfishPrize = "20:40";
	@ConfigField(name = "clownfish-cmd", category = "fishing")
	public String clownfishCmd = "mobhunt head give {player} clownfish Clowfish 1 silent";
	@ConfigField(name = "clownfish-cmd-desc", category = "fishing")
	public String clownfishCmdDesc = "You got a head of a Clownfish";
	@ConfigField(name = "clownfish-cmd-run-chance", category = "fishing")
	public double clownfishCmdRunChance = 0.5;

	@ConfigField(name = "pufferfish", category = "fishing")
	public String pufferfishPrize = "5:15";
	@ConfigField(name = "pufferfish-cmd", category = "fishing")
	public String pufferfishCmd = "mobhunt head give {player} pufferfish Pufferfish 1 silent";
	@ConfigField(name = "pufferfish-cmd-desc", category = "fishing")
	public String pufferfishCmdDesc = "You got a head of a Pufferfish";
	@ConfigField(name = "pufferfish-cmd-run-chance", category = "fishing")
	public double pufferfishCmdRunChance = 0.4;

	// #####################################################################################
	// Bonuses - multipliers
	// #####################################################################################
	@ConfigField(name = "sneaky", category = "bonus")
	public double bonusSneaky = 2.0;
	@ConfigField(name = "return-to-sender", category = "bonus")
	public double bonusReturnToSender = 2.0;
	@ConfigField(name = "push-off-cliff", category = "bonus")
	public double bonusSendFalling = 2.0;
	@ConfigField(name = "no-weapon", category = "bonus")
	public double bonusNoWeapon = 2.0;
	@ConfigField(name = "far-shot", category = "bonus", comment = "This is the PRO-Sniper bonus. The Sniper bonus is calulated as half of PRO-Sniper bonus."
			+ "\nIf If PRO Sniper (far-shot) is 2, then Sniper will be = 1+((far-shot-1)/2)=1.5")
	public double bonusFarShot = 2.0;
	@ConfigField(name = "mounted", category = "bonus")
	public double bonusMounted = 1.5;
	@ConfigField(name = "friendly-fire", category = "bonus")
	public double bonusFriendlyFire = 4;
	@ConfigField(name = "bonus-mob", category = "bonus")
	public double bonusBonusMob = 10;
	@ConfigField(name = "critical", category = "bonus")
	public double bonusCritical = 2;
	@ConfigField(name = "bonus-mob-chance", category = "bonus", comment = "This is the chance (% chance 0-100) that a bonus mob will spawn.")
	public double bonusMobChance = 0.2;
	@ConfigField(name = "babyMultiplier", category = "bonus", comment = "Bonus for killing a Baby mob.")
	public double babyMultiplier = 1.2;

	// #####################################################################################
	// Specials / Achievements
	// #####################################################################################
	@ConfigField(name = "disable-achievements-in-worlds", category = "achievements", comment = "Put the names of the worlds here where you want to disable achievements."
			+ "\nPlayers will still get rewards for killings.")
	public String[] disableAchievementsInWorlds = { "worldname" };
	@ConfigField(name = "show-achievements-without-reward", category = "achievements", comment = "Set this to true if you want to see achievements when you use /mobhunt achievements"
			+ "\nallthough there is no reward for this.")
	public boolean showAchievementsWithoutAReward = false;
	@ConfigField(name = "charged-kill", category = "achievements", comment = "Achievements")
	public double specialCharged = 1000;
	@ConfigField(name = "charged-kill-cmd", category = "achievements")
	public String specialChargedCmd = "give {player} gold_ingot 1";
	@ConfigField(name = "charged-kill-cmd-desc", category = "achievements")
	public String specialChargedCmdDesc = "";
	@ConfigField(name = "creeper-punch", category = "achievements")
	public double specialCreeperPunch = 1000;
	@ConfigField(name = "creeper-punch-cmd", category = "achievements")
	public String specialCreeperPunchCmd = "give {player} gold_ingot 1";
	@ConfigField(name = "creeper-punch-cmd-desc", category = "achievements")
	public String specialCreeperPunchCmdDesc = "";
	@ConfigField(name = "axe-murderer", category = "achievements")
	public double specialAxeMurderer = 1000;
	@ConfigField(name = "axe-murderer-cmd", category = "achievements")
	public String specialAxeMurdererCmd = "give {player} gold_ingot 1";
	@ConfigField(name = "axe-murderer-cmd-desc", category = "achievements")
	public String specialAxeMurdererCmdDesc = "";
	@ConfigField(name = "recordhungry", category = "achievements")
	public double specialRecordHungry = 1000;
	@ConfigField(name = "recordhungry-cmd", category = "achievements")
	public String specialRecordHungryCmd = "give {player} gold_ingot 1";
	@ConfigField(name = "recordhungry-cmd-desc", category = "achievements")
	public String specialRecordHungryCmdDesc = "";
	@ConfigField(name = "infighting", category = "achievements")
	public double specialInfighting = 2000;
	@ConfigField(name = "infighting-cmd", category = "achievements")
	public String specialInfightingCmd = "give {player} gold_ingot 1";
	@ConfigField(name = "infighting-cmd-desc", category = "achievements")
	public String specialInfightingCmdDesc = "";
	@ConfigField(name = "by-the-book", category = "achievements")
	public double specialByTheBook = 1000;
	@ConfigField(name = "by-the-book-cmd", category = "achievements")
	public String specialByTheBookCmd = "give {player} gold_ingot 1";
	@ConfigField(name = "by-the-book-cmd-desc", category = "achievements")
	public String specialByTheBookCmdDesc = "";
	@ConfigField(name = "creepercide", category = "achievements")
	public double specialCreepercide = 1000;
	@ConfigField(name = "creepercide-cmd", category = "achievements")
	public String specialCreepercideCmd = "give {player} gold_ingot 1";
	@ConfigField(name = "creepercide-cmd-desc", category = "achievements")
	public String specialCreepercideCmdDesc = "";
	@ConfigField(name = "hunt-begins", category = "achievements")
	public double specialHuntBegins = 500;
	@ConfigField(name = "hunt-begins-cmd", category = "achievements")
	public String specialHuntBeginsCmd = "";
	@ConfigField(name = "hunt-begins-cmd-desc", category = "achievements")
	public String specialHuntBeginsCmdDesc = "";
	@ConfigField(name = "itsmagic", category = "achievements")
	public double specialItsMagic = 2000;
	@ConfigField(name = "itsmagic-cmd", category = "achievements")
	public String specialItsMagicCmd = "give {player} gold_ingot 1";
	@ConfigField(name = "itsmagic-cmd-desc", category = "achievements")
	public String specialItsMagicCmdDesc = "Enjoy you Gold Ingot";
	@ConfigField(name = "fancypants", category = "achievements")
	public double specialFancyPants = 1000;
	@ConfigField(name = "fancypants-cmd", category = "achievements")
	public String specialFancyPantsCmd = "give {player} gold_ingot 1";
	@ConfigField(name = "fancypants-cmd-desc", category = "achievements")
	public String specialFancyPantsCmdDesc = "Enjoy you Gold Ingots";
	@ConfigField(name = "master-sniper", category = "achievements")
	public double specialMasterSniper = 2000;
	@ConfigField(name = "master-sniper-cmd", category = "achievements")
	public String specialMasterSniperCmd = "give {player} gold_ingot 1";
	@ConfigField(name = "master-sniper-cmd-desc", category = "achievements")
	public String specialMasterSniperCmdDesc = "Enjoy you Gold Ingots";
	@ConfigField(name = "justintime", category = "achievements")
	public double specialJustInTime = 1000;
	@ConfigField(name = "justintime-cmd", category = "achievements")
	public String specialJustInTimeCmd = "give {player} gold_ingot 1";
	@ConfigField(name = "justintime-cmd-desc", category = "achievements")
	public String specialJustInTimeCmdDesc = "Enjoy you Gold Ingots";
	@ConfigField(name = "fangmaster", category = "achievements")
	public double specialFangMaster = 1000;
	@ConfigField(name = "fangmaster-cmd", category = "achievements")
	public String specialFangMasterCmd = "give {player} gold_ingot 1";
	@ConfigField(name = "fangmaster-cmd-desc", category = "achievements")
	public String specialFangMasterCmdDesc = "Enjoy your Gold Ingot";
	@ConfigField(name = "hunter1", category = "achievements")
	public double specialHunter1 = 1000;
	@ConfigField(name = "hunter1-cmd", category = "achievements")
	public String specialHunter1Cmd = "give {player} gold_ingot 5";
	@ConfigField(name = "hunter1-cmd-desc", category = "achievements")
	public String specialHunter1CmdDesc = "Enjoy your 5 Gold Ingots";
	@ConfigField(name = "hunter2", category = "achievements")
	public double specialHunter2 = 2500;
	@ConfigField(name = "hunter2-cmd", category = "achievements")
	public String specialHunter2Cmd = "give {player} gold_ingot 10";
	@ConfigField(name = "hunter2-cmd-desc", category = "achievements")
	public String specialHunter2CmdDesc = "Enjoy your 10 Gold Ingots";
	@ConfigField(name = "hunter3", category = "achievements")
	public double specialHunter3 = 5000;
	@ConfigField(name = "hunter3-cmd", category = "achievements")
	public String specialHunter3Cmd = "give {player} gold_ingot 20";
	@ConfigField(name = "hunter3-cmd-desc", category = "achievements")
	public String specialHunter3CmdDesc = "Enjoy your 20 Gold Ingots";
	@ConfigField(name = "hunter4", category = "achievements")
	public double specialHunter4 = 10000;
	@ConfigField(name = "hunter4-cmd", category = "achievements")
	public String specialHunter4Cmd = "give {player} gold_ingot 25";
	@ConfigField(name = "hunter4-cmd-desc", category = "achievements")
	public String specialHunter4CmdDesc = "Enjoy your 25 Gold Ingots";
	@ConfigField(name = "hunter5", category = "achievements")
	public double specialHunter5 = 20000;
	@ConfigField(name = "hunter5-cmd", category = "achievements")
	public String specialHunter5Cmd = "give {player} gold_ingot 40";
	@ConfigField(name = "hunter5-cmd-desc", category = "achievements")
	public String specialHunter5CmdDesc = "Enjoy your 40 Gold Ingots";
	@ConfigField(name = "hunter6", category = "achievements")
	public double specialHunter6 = 40000;
	@ConfigField(name = "hunter6-cmd", category = "achievements")
	public String specialHunter6Cmd = "give {player} gold_ingot 50";
	@ConfigField(name = "hunter6-cmd-desc", category = "achievements")
	public String specialHunter6CmdDesc = "Enjoy your 50 Gold Ingots";
	@ConfigField(name = "hunter7", category = "achievements")
	public double specialHunter7 = 80000;
	@ConfigField(name = "hunter7-cmd", category = "achievements")
	public String specialHunter7Cmd = "give {player} gold_ingot 60";
	@ConfigField(name = "hunter7-cmd-desc", category = "achievements")
	public String specialHunter7CmdDesc = "Enjoy your 60 Gold Ingots";

	// #####################################################################################
	// Achievement Hunter Levels
	// #####################################################################################
	@ConfigField(name = "blaze_level1", category = "achievement_levels")
	public int blazeLevel1 = 80;

	@ConfigField(name = "creeper_level1", category = "achievement_levels")
	public int creeperLevel1 = 100;

	@ConfigField(name = "silverfish_level1", category = "achievement_levels")
	public int silverfishLevel1 = 100;

	@ConfigField(name = "zombie-pigman_level1", category = "achievement_levels")
	public int zombiePigmanLevel1 = 100;

	@ConfigField(name = "enderman_level1", category = "achievement_levels")
	public int endermanLevel1 = 100;;

	@ConfigField(name = "giant_level1", category = "achievement_levels")
	public int giantLevel1 = 100;

	@ConfigField(name = "skeleton_level1", category = "achievement_levels")
	public int skeletonLevel1 = 100;

	@ConfigField(name = "wither-skeleton_level1", category = "achievement_levels")
	public int witherSkeletonLevel1 = 80;

	@ConfigField(name = "spider_level1", category = "achievement_levels")
	public int spiderLevel1 = 100;

	@ConfigField(name = "cave-spider_level1", category = "achievement_levels")
	public int caveSpiderLevel1 = 100;

	@ConfigField(name = "witch_level1", category = "achievement_levels")
	public int witchLevel1 = 80;

	@ConfigField(name = "zombie_level1", category = "achievement_levels")
	public int zombieLevel1 = 100;

	@ConfigField(name = "ghast_level1", category = "achievement_levels")
	public int ghastLevel1 = 80;

	@ConfigField(name = "iron-golem_level1", category = "achievement_levels")
	public int ironGolemLevel1 = 100;

	@ConfigField(name = "magma-cube_level1", category = "achievement_levels")
	public int magmaCubeLevel1 = 100;

	@ConfigField(name = "endermite_level1", category = "achievement_levels")
	public int endermiteLevel1 = 100;;

	@ConfigField(name = "guardian_level1", category = "achievement_levels")
	public int guardianLevel1 = 100;

	@ConfigField(name = "elder_guardian_level1", category = "achievement_levels")
	public int elderGuardianLevel1 = 50;

	@ConfigField(name = "killerrabbit_level1", category = "achievement_levels")
	public int killerRabbitLevel1 = 100;

	@ConfigField(name = "slime-base_level1", category = "achievement_levels")
	public int slimeLevel1 = 100;

	@ConfigField(name = "shulker_level1", category = "achievement_levels")
	public int shulkerLevel1 = 100;

	@ConfigField(name = "bat_level1", category = "achievement_levels")
	public int batLevel1 = 100;

	@ConfigField(name = "chicken_level1", category = "achievement_levels")
	public int chickenLevel1 = 100;

	@ConfigField(name = "cow_level1", category = "achievement_levels")
	public int cowLevel1 = 100;

	@ConfigField(name = "horse_level1", category = "achievement_levels")
	public int horseLevel1 = 100;

	@ConfigField(name = "mushroom-cow_level1", category = "achievement_levels")
	public int mushroomCowLevel1 = 100;

	@ConfigField(name = "ocelot_level1", category = "achievement_levels")
	public int ocelotLevel1 = 100;

	@ConfigField(name = "pig_level1", category = "achievement_levels")
	public int pigLevel1 = 100;

	@ConfigField(name = "rabbit_level1", category = "achievement_levels")
	public int rabbitLevel1 = 100;

	@ConfigField(name = "sheep_level1", category = "achievement_levels")
	public int sheepLevel1 = 100;

	@ConfigField(name = "snowman_level1", category = "achievement_levels")
	public int snowmanLevel1 = 100;

	@ConfigField(name = "squid_level1", category = "achievement_levels")
	public int squidLevel1 = 100;

	@ConfigField(name = "villager_level1", category = "achievement_levels")
	public int villagerLevel1 = 100;

	@ConfigField(name = "wolf_level1", category = "achievement_levels")
	public int wolfLevel1 = 100;

	@ConfigField(name = "pvpplayer_level1", category = "achievement_levels")
	public int pvpPlayerLevel1 = 100;

	@ConfigField(name = "bonusmob_level1", category = "achievement_levels")
	public int bonusMobLevel1 = 20;

	@ConfigField(name = "polar_bear_level1", category = "achievement_levels")
	public int polarBearLevel1 = 100;

	@ConfigField(name = "stray_level1", category = "achievement_levels")
	public int strayLevel1 = 100;

	@ConfigField(name = "husk_level1", category = "achievement_levels")
	public int huskLevel1 = 100;

	@ConfigField(name = "wither_level1", category = "achievement_levels")
	public int witherLevel1 = 20;

	@ConfigField(name = "enderdragon_level1", category = "achievement_levels")
	public int enderdragonLevel1 = 20;

	@ConfigField(name = "cartographer_level1", category = "achievement_levels")
	public int cartographerLevel1 = 100;

	@ConfigField(name = "nitwit_level1", category = "achievement_levels")
	public int nitwitLevel1 = 100;

	@ConfigField(name = "farmer_level1", category = "achievement_levels")
	public int farmerLevel1 = 100;
	@ConfigField(name = "librarian_level1", category = "achievement_levels")
	public int librarianLevel1 = 100;
	@ConfigField(name = "priest_level1", category = "achievement_levels")
	public int priestLevel1 = 100;
	@ConfigField(name = "butcher_level1", category = "achievement_levels")
	public int butcherLevel1 = 100;
	@ConfigField(name = "blacksmith_level1", category = "achievement_levels")
	public int blacksmithLevel1 = 100;
	@ConfigField(name = "donkey_level1", category = "achievement_levels")
	public int donkeyLevel1 = 100;

	@ConfigField(name = "evoker_level1", category = "achievement_levels")
	public int evokerLevel1 = 50;

	@ConfigField(name = "llama_level1", category = "achievement_levels")
	public int llamaLevel1 = 100;

	@ConfigField(name = "mule_level1", category = "achievement_levels")
	public int muleLevel1 = 100;

	@ConfigField(name = "vex_level1", category = "achievement_levels")
	public int vexLevel1 = 100;

	@ConfigField(name = "vindicator_level1", category = "achievement_levels")
	public int vindicatorLevel1 = 100;

	@ConfigField(name = "zombiehorse_level1", category = "achievement_levels")
	public int zombieHorseLevel1 = 100;

	@ConfigField(name = "skeletonhorse_level1", category = "achievement_levels")
	public int skeletonHorseLevel1 = 100;

	@ConfigField(name = "zombie_villager_level1", category = "achievement_levels")
	public int zombieVillagerLevel1 = 100;

	@ConfigField(name = "rawfish_level1", category = "achievement_levels")
	public int rawfishLevel1 = 100;
	@ConfigField(name = "rawsalmon_level1", category = "achievement_levels")
	public int rawsalmonLevel1 = 100;
	@ConfigField(name = "clownfish_level1", category = "achievement_levels")
	public int clownfishLevel1 = 100;
	@ConfigField(name = "pufferfish_level1", category = "achievement_levels")
	public int pufferfishLevel1 = 100;

	// #####################################################################################
	// Assists
	// #####################################################################################
	@ConfigField(name = "enable", category = "assists", comment = "Enabling assist allows the second last player to attack a mob to get some money from it")
	public boolean enableAssists = true;
	@ConfigField(name = "multiplier", category = "assists", comment = "This should be a value that is multiplied against the mobs base kill value."
			+ "\nThis is used to determine how much money an assister gets.")
	public double assistMultiplier = 0.25;
	@ConfigField(name = "allow-killstreak", category = "assists", comment = "Should killstreak be applied to assists")
	public boolean assistAllowKillstreak = false;
	@ConfigField(name = "timeout", category = "assists", comment = "Time in seconds after attacking a mob that can be counted as an assist")
	public int assistTimeout = 4;

	// #####################################################################################
	// Grinding detection
	// #####################################################################################
	@ConfigField(name = "enable-grinding-penalty", category = "grinding", comment = "Grinding detection."
			+ "\nEnabling this prevents a player from earning too much money from using a mob grinder."
			+ "\nSet 'enable-grinding-penalty: false' to disable the grinding detection."
			+ "\nOBS: You can whitelist an area to allow grinding using '/mobhunt whitelistarea <add|remove>'"
			+ "\nif the area is detected as a grinding area. See also '/mobhunt checkgrinding'")
	public boolean penaltyGrindingEnable = true;
	@ConfigField(name = "grinding-range-detection", category = "grinding", comment = "For each kill MobHunting check number of kills within this number of blocks."
			+ "\nIf number of kills exceeds 10, the reward will decrese with 10% until 20 kills with"
			+ "\nthe range, whereafter the reward will be zero.")
	public int grindingRangeDetection = 15;
	@ConfigField(name = "grinding-stacked-mobs-allowed", category = "grinding", comment = "Killing stacked mobs (created by a mob stacking plugin) "
			+ "\nis by nature detected as grinding and by default allowed. If you want to the the grinding detection to detect"
			+ "\nkillings of stacked to be detected as gring, you must set grinding-stacked-mobs-allowed to false.")
	public boolean isGrindingStackedMobsAllowed = true;

	// #####################################################################################
	// Penalties
	// #####################################################################################
	@ConfigField(name = "flyingPenalty", category = "penalty", comment = "If a player flies at any point in a fight, this penalty will be applied")
	public double penaltyFlying = 0.5;

	@ConfigField(name = "mob-rob-from-player", category = "penalty", comment = "This is the penalty if the player gets killed by a mob."
			+ "\nSet mob-rob-from-player=10 to let the mob steal 10 dollars"
			+ "\n or 10% to let the mob steal 10% of the players balance."
			+ "\nSet mob-rob-from-player=0 to disable this")
	public String mobKillsPlayerPenalty = "0%";

	// #####################################################################################
	// Killstreaks
	// #####################################################################################
	@ConfigField(name = "level1", category = "killstreak")
	public int killstreakLevel1 = 5;
	@ConfigField(name = "level1-multiplier", category = "killstreak")
	public double killstreakLevel1Mult = 1.5;
	@ConfigField(name = "level2", category = "killstreak")
	public int killstreakLevel2 = 10;
	@ConfigField(name = "level2-multiplier", category = "killstreak")
	public double killstreakLevel2Mult = 2;
	@ConfigField(name = "level3", category = "killstreak")
	public int killstreakLevel3 = 20;
	@ConfigField(name = "level3-multiplier", category = "killstreak")
	public double killstreakLevel3Mult = 3;
	@ConfigField(name = "level4", category = "killstreak")
	public int killstreakLevel4 = 40;
	@ConfigField(name = "level4-multiplier", category = "killstreak")
	public double killstreakLevel4Mult = 4;

	// #####################################################################################
	// Multiplier by rank / permission
	// #####################################################################################
	@ConfigField(name = "rank-multiplier", category = "multiplier", comment = "Ranks")
	public HashMap<String, String> rankMultiplier = new HashMap<String, String>();
	{
		rankMultiplier.put("mobhunting.multiplier.guest", "0.9");
		rankMultiplier.put("mobhunting.multiplier.guardian", "1.02");
		rankMultiplier.put("mobhunting.multiplier.staff", "1.05");
		rankMultiplier.put("mobhunting.multiplier.hasVoted", "2");
		rankMultiplier.put("mobhunting.multiplier.donator", "3");
	}

	// #####################################################################################
	// Multiplier pr World Difficulty
	// #####################################################################################
	@ConfigField(name = "world-difficulty-multiplier", category = "multiplier", comment = "WorldDifficulty")
	public HashMap<String, String> difficultyMultiplier = new HashMap<String, String>();
	{
		difficultyMultiplier.put("difficulty.multiplier.peaceful", "0.5");
		difficultyMultiplier.put("difficulty.multiplier.easy", "0.75");
		difficultyMultiplier.put("difficulty.multiplier.normal", "1");
		difficultyMultiplier.put("difficulty.multiplier.hard", "2");
	}

	// #####################################################################################
	// PVP Propality
	// #####################################################################################
	@ConfigField(name = "pvp-allowed", category = "pvp", comment = "Set pvpAllowed=false to disable rewards on killing other players.")
	public boolean pvpAllowed = true;
	@ConfigField(name = "rob-from-victim", category = "pvp", comment = "Set rob-from-victim=true to steal from the victim or "
			+ "\nrob-from-victim=false to get the reward money from the server.")
	public boolean robFromVictim = true;

	@ConfigField(name = "pvp-kill-prize", category = "pvp", comment = "The kill prize can be a number to steal x dollars from the killed player,"
			+ "\nor it can be a cut in percent of his balance.")
	public String pvpKillPrize = "1.5%";
	@ConfigField(name = "pvp-kill-cmd", category = "pvp", comment = "One or more console commands to be run when a player kills another player.")
	public String pvpKillCmd = "give {player} 397 1 3 {SkullOwner:\"{killed_player}\"}|give {player} diamond 1";
	@ConfigField(name = "pvp-kill-cmd-desc", category = "pvp", comment = "Write the message to the killer, describing the reward / console commands")
	public String pvpKillCmdDesc = "You got {killed_player}\'s skull";
	@ConfigField(name = "pvp-kill-cmd-run-chance", category = "pvp", comment = "This is the chance for running the command. 1 = 100% (each time the player is killed), 0.5 ~ 50% and 0.001 = 0.1% (very rare) ")
	public double pvpKillCmdRunChance = 0.5;

	// #####################################################################################
	// Disguises
	// #####################################################################################
	@ConfigField(name = "disable-integration-i-disguise", category = "disguises", comment = "Disable integration with iDisguise")
	public boolean disableIntegrationIDisguise = false;

	@ConfigField(name = "disable-integration-disguisecraft", category = "disguises", comment = "Disable integration with DisguiseCcraft")
	public boolean disableIntegrationDisguiseCraft = false;

	@ConfigField(name = "disable-integration-libsdisguises", category = "disguises", comment = "Disable integration with LibsDisguises")
	public boolean disableIntegrationLibsDisguises = false;

	@ConfigField(name = "remove-disguise-when-attacking", category = "disguises", comment = "Set pvpAllowed=false to disable rewards on killing other players.")
	public boolean removeDisguiseWhenAttacking = true;

	@ConfigField(name = "remove-disguise-when-attacked", category = "disguises", comment = "Set pvpAllowed=false to disable rewards on killing other players.")
	public boolean removeDisguiseWhenAttacked = true;

	@ConfigField(name = "undercover-multiplier", category = "disguises", comment = "Bonus multiplier for killing while disgused."
			+ "\nCan be both positive an negative = reward or penalty"
			+ "\nand over and under 1 = raise or lower the reward. ")
	public double undercoverMultiplier = 0.95;
	@ConfigField(name = "cover-blown-multiplier", category = "disguises", comment = "Bonus multiplier for killing a disgused player."
			+ "\nCan be both positive an negative = reward or penalty"
			+ "\nand over and under 1 = raise or lower the reward. ")
	public double coverBlownMultiplier = 1.2;

	// #####################################################################################
	// NPC / Citizens / MasterMobHunter Settings
	// #####################################################################################
	@ConfigField(name = "disable-integration-citizens", category = "npc", comment = "Disable integration with Citizens2")
	public boolean disableIntegrationCitizens = false;
	@ConfigField(name = "masterMobHunter_check_every", category = "npc", comment = "Set the number of seconds between each check. Recommended setting is"
			+ "\nmasterMobHunter_check_every: 300 ~ to update all MasterMobHunters every 5th minute.")
	public int masterMobHuntercheckEvery = 300;

	// #####################################################################################
	// Bounty Settings
	// #####################################################################################
	@ConfigField(name = "disable-player-bounties", category = "bounties", comment = "Set to true if you want to disable players to be able to put bounties on each other.")
	public boolean disablePlayerBounties = false;
	@ConfigField(name = "bounty-return-pct", category = "bounties", comment = "Here you set how much of a bound the bounty owner get back if "
			+ "\nhe drop the bounty on another player")
	public int bountyReturnPct = 50;
	@ConfigField(name = "bounty_duration", category = "bounties", comment = "Here you set the number of days the Bounty is collectable."
			+ "\nAfter the number of days the Bounty will be removed automatically")
	public int bountyDaysToLive = 30;
	@ConfigField(name = "enable_random_bounty", category = "bounties", comment = "Set enable_random_bounty=false to disable random bounties")
	public boolean enableRandomBounty = true;
	@ConfigField(name = "time_between_random_bounties", category = "bounties", comment = "Time between Random Bounty is created in minutes")
	public int timeBetweenRandomBounties = 60;
	@ConfigField(name = "minimum_number_of_online_players", category = "bounties", comment = "Minimum number of players before the server starts to make random bounties")
	public int minimumNumberOfOnlinePlayers = 5;
	@ConfigField(name = "chance_to_create_a_random_bounty", category = "bounties", comment = "Chance that a bounty is created on a player after the minimum time. Must be a number between 0 and 1. (0 = never, 0.5 = 50% 1 = always)")
	public double chanceToCreateBounty = 0.5;
	@ConfigField(name = "random_bounty_prize", category = "bounties", comment = "Random Bounty. Can be a number 100 or a range 100:200")
	public String randomBounty = "50:100";

	// #####################################################################################
	// Stacked Mobs Settings
	// #####################################################################################
	@ConfigField(name = "disable-integration-mobstacker", category = "stackedmobs", comment = "Disable integration with MobStacker."
			+ "\nhttps://www.spigotmc.org/resources/mobstacker.15596/")
	public boolean disableIntegrationMobStacker = false;

	@ConfigField(name = "disable-integration-stackmob", category = "stackedmobs", comment = "Disable integration with StackMob."
			+ "\nhttps://www.spigotmc.org/resources/stackmob.29999/")
	public boolean disableIntegrationStackMob = false;

	@ConfigField(name = "get-reward-from-stacked-mobs", category = "stackedmobs", comment = "Set to true if you want stacked mobs to pay a reward.")
	public boolean getRewardFromStackedMobs = true;

	// #####################################################################################
	// CustomMobs Settings
	// #####################################################################################
	@ConfigField(name = "disable-integration-custommobs", category = "custommobs", comment = "Disable integration with CustomMobs"
			+ "\nhttps://dev.bukkit.org/bukkit-plugins/custom-mobs/")
	public boolean disableIntegrationCustomMobs = false;

	@ConfigField(name = "allow_custom_mobspawners_and_eggs", category = "custommobs", comment = "Can the players earn money on mobs spawned from CustomMobs Spawners and eggs?")
	public boolean allowCustomMobsSpawners = false;

	// #####################################################################################
	// ConquestiaMobs Settings
	// #####################################################################################
	@ConfigField(name = "disable-integration-conquestiamobs", category = "conquestiamobs", comment = "Disable integration with ConquestiaMobs"
			+ "\nhttps://www.spigotmc.org/resources/conquesita-mobs.21307/")
	public boolean disableIntegrationConquestiaMobs = false;

	@ConfigField(name = "multiplier_per_level", category = "conquestiamobs", comment = "This is the multiplier per level mutiplied with the basic reward."
			+ "\nBecareful not to ruin the server economy by making the multiplier to big."
			+ "\nExample: If the reward is 10 and the multiplier is 1.05, the calculated" + "\nreward is:"
			+ "\nLevel 1: reward=10" + "\nLevel 2: reward=10*1.05=10.5" + "\nLevel 3: reward=10*1.05*1.05=11.03"
			+ "\nLevel 4: reward=10*1.05*1.05*1.05=11.58" + "\nLevel 5: reward=10*1.05*1.05*1.05*1.05=12.16"
			+ "\nLevel 6: reward=10*1.05*1.05*1.05*1.05*1.05=12.76"
			+ "\nLevel 7: reward=10*1.05*1.05*1.05*1.05*1.05*1.05=13.40"
			+ "\nLevel 8: reward=10*1.05*1.05*1.05*1.05*1.05*1.05*1.05=14.07"
			+ "\nLevel 9: reward=10*1.05*1.05*1.05*1.05*1.05*1.05*1.05*1.05=14.77"
			+ "\nLevel 10: reward=10*1.05*1.05*1.05*.....=15.51" + "\nLevel 20: reward=10*1.05*1.05*1.05*.....=25..27"
			+ "\nLevel 30: reward=10*1.05*1.05*1.05*.....=41.61" + "\nLevel 40: reward=10*1.05*1.05*1.05*.....=67.05"
			+ "\nLevel 50: reward=10*1.05*1.05*1.05*.....=109.21"
			+ "\nLevel 100: reward=10*1.05*1.05*1.05*.....=1252.39")
	public double mulitiplierPerLevel = 1.05;

	// #####################################################################################
	// DropMoneyOnGround settings
	// #####################################################################################
	@ConfigField(name = "drop-money-on-ground", category = "dropmoneyonground", comment = "When a player get a money reward for a kill, the money will go directly"
			+ "\ninto his pocket. If you set dropMoneyOnGround=true the reward will "
			+ "\ndropped on ground to be picked up by the player."
			+ "\nNegative rewards will always be taken from the player. ")
	public boolean dropMoneyOnGroup = true;

	@ConfigField(name = "deny-hoppers-to-pickup-money-on-ground", category = "dropmoneyonground", comment = "Dark room mobspawners usually collect items in a HOPPER. This is denied by default."
			+ "\nIf you want HOPPERS to collect MobHunting Money rewards "
			+ "\nset \"deny-hoppers-to-pickup-money-on-ground\"=true")
	public boolean denyHoppersToPickUpMoney = true;

	@ConfigField(name = "drop-money-on-ground-itemtype", category = "dropmoneyonground", comment = "Here you can set the type of the ITEM to be dropped."
			+ "\nYou can choose between \"ITEM\",\"KILLED\",\"SKULL\",\"KILLER\". The default is ITEM."
			+ "\nThe value will be showed above the item." + "\nITEM: The reward is dropped as a normal Minecraft item."
			+ "\nKILLED: The reward is dropped as the head of the mob/player you killed."
			+ "\nSKULL: The reward is dropped as a SKULL with a custom texture. You can generate custom texture value"
			+ "\nand custom texture signature at http://mineskin.org"
			+ "\nKILLER: The reward is dropped as the killers head."
			+ "\n\nOBS: If the Gringotts plugin is installed and support not disabled, the droped item will be the Gringotts chosen item."
			+ "\nExamples:" + "\n\nBag of gold: (https://mineskin.org/3384)"
			+ "\n\ndrop-money-on-ground-skull-playerUUID: '3eb9e46c-72ca-374d-8314-058a96cd0e8d'"
			+ "\ndrop-money-on-ground-skull-texture-value: 'eyJ0aW1lc3RhbXAiOjE0NzQzMzI0MzY1MDYsInByb2ZpbGVJZCI6IjNlMjZiMDk3MWFjZDRjNmQ5MzVjNmFkYjE1YjYyMDNhIiwicHJvZmlsZU5hbWUiOiJOYWhlbGUiLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzg2NzczZDc0Y2Y1MDhmZDc3Yzc4MmZmZDI5ZGYyZmU0N2ZiNzE0YjViMGQ3ZGU2N2Q1Mjg2OTMxZTJmMWRmMiJ9fX0='"
			+ "\ndrop-money-on-ground-skull-texture-signature: 'JdvJksowuxYQ0eqf56J+Dmczg7zvlw2DbIc58Q33kRt65uMUNn2iRCQsbNpztC1cAAgyYMOyFDiOUZQeIK03CSRoPLDtWp2u501YoGKqhjgrE0V0UDh3JetWKz4Ob0KmATtY+4R2vSoMjHFEFppM0Oq+8ZER12FAiVEMAzeseFN3Z9fWAMc/V10LoquGBpq6ExTfSCEEMDEGZopF1T8ZBKL0vf4DVendfz4v3yl7bRBzISZEAnF+ECTa9z36r8HRqS8+s0eO/AWYQcRaKIu9H+wSK5F/1v+rgifeSlMAnt1Na8m1b5tMfNuq6pXxWCq4nUGgYVTOLUinqs9ZcFz3Z6Mtx5YtymKk2M0mzxmTm9+AeOL4s3K/UrJYQlcmLBJSv4hd6EigJXoashzWNCHKmFDYCdEhh4FArq4G9vRZtoudcTeMsvi0VmXIgER8U5iSfoTtzXcGbf/GT0ECtgfeA40f5oCqyE4nXreudMmvlDCBr/KHbILQWeeH/jhtYqQ6OwJb3Ji2Bs9F5fQmICSqk7X4yKzexf8rdDhOG1z+/TCot7K8unPVuQx46sXPeP7t2hCiHOXMAnOMt8vuL3gQUURIEM6fMryjmlKsgvk8Jo0gawavRCIZQtA6vT0JRRnSAchzEOA7QP1iiVV3LnwX9Yqw7oMJ/+REV1hWesuzDOc='"
			+ "\n\nChest: (https://mineskin.org/3136)"
			+ "\n\ndrop-money-on-ground-skull-playerUUID: 'e572342f-5552-3c82-b163-c776e3b57da4'"
			+ "\ndrop-money-on-ground-skull-texture-value: 'eyJ0aW1lc3RhbXAiOjE0NzI4Mzk3Nzk2ODMsInByb2ZpbGVJZCI6ImIwZDRiMjhiYzFkNzQ4ODlhZjBlODY2MWNlZTk2YWFiIiwicHJvZmlsZU5hbWUiOiJJbnZlbnRpdmVHYW1lcyIsInNpZ25hdHVyZVJlcXVpcmVkIjp0cnVlLCJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTY5NDcxMjQ1YmNhN2M0ZmUwNjQ0MGQ5YjRiOWY3NDIxN2VkNzM0M2FhZDU5YTc5MThiMWExZDYxZDhiYTZkYSJ9fX0='"
			+ "\ndrop-money-on-ground-skull-texture-signature: 'lVA2QIbvybpzhcXof5yWz/7nkHdhG/3MGO+1DyD1txdRCALV6BRwsDUBwIUg06MkLUpBkjmiOvFcCRgal/jDE/xkkJPyk2tb/w4NtQ5PiPiAe0oInVnuiSIVFIE4tnsCdvX0joll3uKwVu6XY3t1KEsqJATcPhA5hslVn1iOp/IfMziIfuCzzob04rScpwcw0mLNtbtbMVAl6LYR9gXVuOkAfXujuYq4lbI/iW0yuLxSAzr8i9QWBP2ftup4qQHwocQRTdUE6/G5G9LwJWXhhnqKWjgjfvL0y2FRFJkgN1cvuq7DvUDBVsePnRIHwU5YvBPMjcZe/KE8VPTSodsN84/+++5p95Puxe1DXMX822xR71IQsxM7eax7Ffrr/Tzxw2rSDh9ivGGlRAB85OHwp/ouUgWNSrT8inNMYImque9EuZku9p3OFet8iZsFhkMXANeNtTVL7LKV7/L/0YWwoeyBnw5QQqvGyWKw3dac5eDkRNCyCtdDIntM5vsd8FxnIFj36zxLWgmrJmOM9hg5PBM4gcDxxryBcug8jSe+W9XDU39OOJotXajj8dgSL8yUn+d7l4Qvat/vJbAE8lonMl7P0P9QBPzmcIUvlRMuHSpRZQYkoCbwc2Filahd/5INtm7I4Y28XYzzupdwLk3cavKfOloL5YrWNqaZr/+9Tbk='"
			+ "\n\nBirthday present: (https://mineskin.org/4743)"
			+ "\n\ndrop-money-on-ground-skull-playerUUID: '6eb23602-2c22-34b8-b6b1-1847931c1a6d'"
			+ "\ndrop-money-on-ground-skull-texture-value: 'eyJ0aW1lc3RhbXAiOjE0Nzk5MzEzNDMxMjgsInByb2ZpbGVJZCI6IjNlMjZiMDk3MWFjZDRjNmQ5MzVjNmFkYjE1YjYyMDNhIiwicHJvZmlsZU5hbWUiOiJOYWhlbGUiLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2NmNDRkZjIzMjBiNzYzMTI0N2FhZGY1OWMwZWNlOTdhNGJiNTdkZjI4YzFjZWU3OTM0ZjZhZTI4YWY4OTg5In19fQ=='"
			+ "\ndrop-money-on-ground-skull-texture-signature: 'k1xQ6E1NuxG1ZN7nlQqRJltYrJn44XHVhNA9pSEu2Pt2mkuixMxhIDj2Tg6o+JWlTyGfXtPVWLxygeGymmeSGaVcmDTaCALg7PL11ZfSzSWSxaIufNbj1EcSi264jg5FrAa/2/DnFsgu16wjlWiIGtjCzgx2QabY8YofoPKw6Y6Y5FHZJVXpT8Rsxs8ok6ZHtfm/ZyyTgvRSzh2mKmVyQIYJ1ZKxuqWhDQfbtBpu3dlEzMAEJo85Dvb7uIFYa7WFitjFJue/c9qpqAnazWFLrx33nYpjjeYhcfAvsaNQW3JVFEkyxzEgzOHbdsbiZcqTCwO+49whu175xOqT7XhouEubDT7A3H1jiSvQvkUZJv/GzUF4qFYHSfxhr6OWoBrRGwWmPdcrYx7fUWKo43CAqa5inaiTV4gU70BWrx5i3LhIJxpnspAyTXs8tZBxeoh8IizWD7uXkYYqh3j9cwuHoxfwZuMpOx9CPTC6R/YwJ1YK5OgJBY1+QhNw+NOilWT3jTok82elFvOLm3a5yLyVs+/UPmLD7rZsFm7/DD3VnRcpgjKRiyy2j9vYsYLyNE2BVLVJxBVk2yyy9u7L4VR6PO+8v2dh9DQl7vM2ORCxKPl2lt6woHWM2+eT1PXr16LtMtAOGYT8mlKFhp8Ou2+9fu4AqWkX7n3swU6XLiK5cJs='"
			+ "\n\nChoose between \"ITEM\",\"KILLED\",\"SKULL\",\"KILLER\"")
	public String dropMoneyOnGroundItemtype = "SKULL";

	@ConfigField(name = "drop-money-on-ground-item", category = "dropmoneyonground", comment = "Here you can set which item should be used when you have chosen drop-money-on-ground-itemtype: ITEM. "
			+ "\nUse Minecraft Item names like: " + "\nGOLD_NUGGET, DIAMOND, GOLD_INGOT, EMERALD, GOLDEN_APPLE ")
	public String dropMoneyOnGroundItem = "GOLD_INGOT";

	@ConfigField(name = "drop-money-on-ground-text-color", category = "dropmoneyonground", comment = "Here you can set of the color of the number above the dropped item. \nUse color names like WHITE, RED, BLUE")
	public String dropMoneyOnGroundTextColor = "WHITE";

	@ConfigField(name = "drop-money-on-ground-skull-playerUUID", category = "dropmoneyonground", comment = "This is the Custom player UUID generated at http://mineskin.org")
	public String dropMoneyOnGroundSkullPlayerUUID = "3eb9e46c-72ca-374d-8314-058a96cd0e8d";

	@ConfigField(name = "drop-money-on-ground-skull-texture-value", category = "dropmoneyonground", comment = "This is the Custom Texture Value generated at http://mineskin.org")
	public String dropMoneyOnGroundSkullTextureValue = "eyJ0aW1lc3RhbXAiOjE0NzQzMzI0MzY1MDYsInByb2ZpbGVJZCI6IjNlMjZiMDk3MWFjZDRjNmQ5MzVjNmFkYjE1YjYyMDNhIiwicHJvZmlsZU5hbWUiOiJOYWhlbGUiLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzg2NzczZDc0Y2Y1MDhmZDc3Yzc4MmZmZDI5ZGYyZmU0N2ZiNzE0YjViMGQ3ZGU2N2Q1Mjg2OTMxZTJmMWRmMiJ9fX0=";

	@ConfigField(name = "drop-money-on-ground-skull-texture-signature", category = "dropmoneyonground", comment = "This is the Custom Texture Signature generated at http:\\\\mineskin.org")
	public String dropMoneyOnGroundSkullTextureSignature = "JdvJksowuxYQ0eqf56J+Dmczg7zvlw2DbIc58Q33kRt65uMUNn2iRCQsbNpztC1cAAgyYMOyFDiOUZQeIK03CSRoPLDtWp2u501YoGKqhjgrE0V0UDh3JetWKz4Ob0KmATtY+4R2vSoMjHFEFppM0Oq+8ZER12FAiVEMAzeseFN3Z9fWAMc/V10LoquGBpq6ExTfSCEEMDEGZopF1T8ZBKL0vf4DVendfz4v3yl7bRBzISZEAnF+ECTa9z36r8HRqS8+s0eO/AWYQcRaKIu9H+wSK5F/1v+rgifeSlMAnt1Na8m1b5tMfNuq6pXxWCq4nUGgYVTOLUinqs9ZcFz3Z6Mtx5YtymKk2M0mzxmTm9+AeOL4s3K/UrJYQlcmLBJSv4hd6EigJXoashzWNCHKmFDYCdEhh4FArq4G9vRZtoudcTeMsvi0VmXIgER8U5iSfoTtzXcGbf/GT0ECtgfeA40f5oCqyE4nXreudMmvlDCBr/KHbILQWeeH/jhtYqQ6OwJb3Ji2Bs9F5fQmICSqk7X4yKzexf8rdDhOG1z+/TCot7K8unPVuQx46sXPeP7t2hCiHOXMAnOMt8vuL3gQUURIEM6fMryjmlKsgvk8Jo0gawavRCIZQtA6vT0JRRnSAchzEOA7QP1iiVV3LnwX9Yqw7oMJ/+REV1hWesuzDOc=";

	// #####################################################################################
	// Plugin integration
	// #####################################################################################
	@ConfigField(name = "disable-integration-mobarena", category = "plugins", comment = "Disable integration with MobArena")
	public boolean disableIntegrationMobArena = false;

	@ConfigField(name = "mobarena-get-rewards", category = "plugins", comment = "Set to true if you want the players to get rewards while playing MobArena.")
	public boolean mobarenaGetRewards = false;

	@ConfigField(name = "disable-integration-pvparena", category = "plugins", comment = "Disable integration with PvpArena")
	public boolean disableIntegrationPvpArena = false;

	@ConfigField(name = "pvparena-get-rewards", category = "plugins", comment = "Set to true if you want the players to get rewards while playing pvpArena.")
	public boolean pvparenaGetRewards = false;

	@ConfigField(name = "disable-integration-mythicmobs", category = "plugins", comment = "Disable integration with MythicMobs")
	public boolean disableIntegrationMythicmobs = false;

	@ConfigField(name = "disable-integration-mypet", category = "plugins", comment = "Disable integration with MyPet")
	public boolean disableIntegrationMyPet = false;

	@ConfigField(name = "disable-integration-minigames", category = "plugins", comment = "Disable integration with MiniGames")
	public boolean disableIntegrationMinigames = false;

	@ConfigField(name = "disable-integration-minigameslib", category = "plugins", comment = "Disable integration with MiniGamesLib"
			+ "\nhttps://www.spigotmc.org/resources/minigameslib.23844/")
	public boolean disableIntegrationMinigamesLib = false;

	@ConfigField(name = "disable-integration-worldguard", category = "plugins", comment = "Disable integration with WorldGuard")
	public boolean disableIntegrationWorldGuard = false;

	@ConfigField(name = "disable-integration-essentials", category = "plugins", comment = "Disable integration with Essentials"
			+ "\nhttp://dev.bukkit.org/bukkit-plugins/essentialsx/")
	public boolean disableIntegrationEssentials = false;

	@ConfigField(name = "disable-integration-battlearena", category = "plugins", comment = "Disable integration with BattleArena")
	public boolean disableIntegrationBattleArena = false;

	@ConfigField(name = "disable-integration-bossbarapi", category = "plugins", comment = "Disable integration with BossBarAPI. If you want messages in player chat you can set this to true.")
	public boolean disableIntegrationBossBarAPI = false;

	@ConfigField(name = "disable-integration-barapi", category = "plugins", comment = "Disable integration with BarAPI. If you want messages in player chat you can set this to true."
			+ "\nhttps://dev.bukkit.org/projects/bar-api")
	public boolean disableIntegrationBarAPI = false;

	@ConfigField(name = "disable-integration-titleapi", category = "plugins", comment = "Disable integration with TitleAPI")
	public boolean disableIntegrationTitleAPI = false;

	@ConfigField(name = "disable-integration-vanishnopacket", category = "plugins", comment = "Disable integration with VanishNoPacket")
	public boolean disableIntegrationVanishNoPacket = false;

	@ConfigField(name = "disable-integration-titlemanager", category = "plugins", comment = "Disable integration with TitleManger. If you want messages in player chat you can set this to true."
			+ "\nhttps://www.spigotmc.org/resources/titlemanager.1049/")
	public boolean disableIntegrationTitleManager = false;

	@ConfigField(name = "disable-integration-actionbar", category = "plugins", comment = "Disable integration with Actionbar. If you want messages in player chat you can set this to true.")
	public boolean disableIntegrationActionbar = false;

	@ConfigField(name = "disable-integration-actionbarapi", category = "plugins", comment = "Disable integration with ActionBarAPI. If you want messages in player chat you can set this to true."
			+ "\nhttps://www.spigotmc.org/resources/actionbarapi-1-8-1-9-1-10.1315/")
	public boolean disableIntegrationActionBarAPI = false;

	@ConfigField(name = "disable-integration-actionannouncer", category = "plugins", comment = "Disable integration with ActionAnnouncer. If you want messages in player chat you can set this to true."
			+ "\nhttps://www.spigotmc.org/resources/actionannouncer.1320/")
	public boolean disableIntegrationActionAnnouncer = false;

	@ConfigField(name = "disable-integration-gringotts", category = "plugins", comment = "Disable integration with Gringotts Economy."
			+ "\nhttp://dev.bukkit.org/bukkit-plugins/gringotts/")
	public boolean disableIntegrationGringotts = false;

	@ConfigField(name = "disable-integration-tardis-weeping-angels", category = "plugins", comment = "Disable integration with TARDIS Weeping Angels."
			+ "\nhttp://dev.bukkit.org/bukkit-plugins/tardisweepingangels/")
	public boolean disableIntegrationTARDISWeepingAngels = false;

	@ConfigField(name = "disable-integration-protocollib", category = "plugins", comment = "Disable integration with ProtocolLib."
			+ "\nhttps://www.spigotmc.org/resources/protocollib.1997/")
	public boolean disableIntegrationProtocolLib = false;

	@ConfigField(name = "disable-integration-mysterious-halloween", category = "plugins", comment = "Disable integration with MysteriousHalloween."
			+ "\nhttps://www.spigotmc.org/resources/mysterioushalloween.13059/")
	public boolean disableIntegrationMysteriousHalloween = false;

	@ConfigField(name = "disable-integration-mcmmo", category = "plugins", comment = "Disable integration with McMMO."
			+ "\nhttps://www.spigotmc.org/resources/mcmmo.2445/")
	public boolean disableIntegrationMcMMO = false;

	// #####################################################################################
	// Database
	// #####################################################################################
	@ConfigField(name = "type", category = "database", comment = "Type of database to use. Valid values are: sqlite, mysql")
	public String databaseType = "sqlite";

	@ConfigField(name = "username", category = "database")
	public String databaseUsername = "user";

	@ConfigField(name = "password", category = "database")
	public String databasePassword = "password";

	@ConfigField(name = "host", category = "database")
	public String databaseHost = "localhost:3306";

	@ConfigField(name = "database", category = "database")
	public String databaseName = "mobhunting";

	@ConfigField(name = "debug_sql", category = "database", comment = "sql_debug is only meant to be used by the developer. Setting this to to true can produre a a big log file!")
	public boolean debugSQL = false;

	@ConfigField(name = "database_version", category = "database", comment = "FOR INTERNAL USE ONLY. DONT CHANGE THIS VALUE!")
	public int databaseVersion = 0;

	// #####################################################################################
	// Update Settings
	// #####################################################################################
	@ConfigField(name = "update-check", category = "updates", comment = "Check if there is a new version of the plugin available.")
	public boolean updateCheck = true;

	@ConfigField(name = "check_every", category = "updates", comment = "Set the number of seconds between each check. Recommended setting is"
			+ "\ncheck_every: 7200 ~ to check every second hour.")
	public int checkEvery = 7200;

	@ConfigField(name = "autoupdate", category = "updates", comment = "Set 'autoupdate: true' if you want new updates downloaded and installed."
			+ "\nYou will still have to reboot the server manually.")
	public boolean autoupdate = false;

	// #####################################################################################
	// Generel settings
	// #####################################################################################
	@ConfigField(name = "disabled-in-worlds", category = "general", comment = "Put the names of the worlds here that you do not wish for mobhunting to be enabled in.")
	public String[] disabledInWorlds = { "worldname" };

	@ConfigField(name = "language", category = "general", comment = "The language (file) to use. You can put the name of the language file as the language code "
			+ "\n(eg. en_US, de_DE, fr_FR, ect.) or you can specify the name of a custom file without the .lang\nPlease check the lang/ folder for a list of all available translations.")
	public String language = "en_US";

	@ConfigField(name = "allow_mobspawners_and_eggs", category = "general", comment = "Can the players earn money on mobs spawned from mobspawners and eggs?")
	public boolean allowMobSpawners = false;

	@ConfigField(name = "use-actionbar-for-broadcasts", category = "general", comment = "Broadcast messages will be send in the ActionBar if MobHunting finds a supported ActionBar plugin.")
	public boolean useActionBarforBroadcasts = true;

	@ConfigField(name = "broadcast-achievement", category = "general", comment = "Should achievements be broadcasted?")
	public boolean broadcastAchievement = true;

	@ConfigField(name = "broadcast-first-achievement", category = "general", comment = "Should the hunt begins achievement be broadcasted?")
	public boolean broadcastFirstAchievement = true;

	@ConfigField(name = "save-period", category = "general", comment = "Time between saves in ticks (20 ticks ~ 1 sec)")
	public int savePeriod = 6000;

	@ConfigField(name = "leaderboard-update-period", category = "general", comment = "Time between leaderboard updates in ticks (20 ticks ~ 1 sec)")
	public int leaderboardUpdatePeriod = 1200;

	@ConfigField(name = "kill-timeout", category = "general", comment = "Time in seconds after attacking a mob that can be counted as a kill")
	public int killTimeout = 4;

	@ConfigField(name = "kill-debug", category = "general", comment = "If kills are not being registered in mob hunting. Enable this to see why they arent")
	public boolean killDebug = false;

	@ConfigField(name = "reward_rounding", category = "general", comment = "Rounding of rewards when you uses a range or %. (ex creeperPrize=10:30) the reward."
			+ "\nAll numbers except 0 can be used. "
			+ "\nSet rounding_reward=1 if you want integers. IE. 10,11,12,13,14..."
			+ "\nSet rounding_reward=0.01 if you want 2 decimals 10.00, 10.01, 10.02... integers."
			+ "\nSet rounding_reward=5 if you want multipla of 5 IE. 10,15,20,25..."
			+ "\nSet rounding_reward=2 if you want multipla of 2 IE. 10,12,14,16...")
	public double rewardRounding = 0.01;

	@ConfigField(name = "minimum_reward", category = "general", comment = "This is the minimum reward which will which will be paid to the player 0.01 will be fine"
			+ "\nin most installation, but Gringott users who want very low rewards (like 0.001  for killing"
			+ "\na mob) will have to lower the minimum reward. Remember that some multipliers are less than 1"
			+ "\n and grinding detection and penalties. The minimum_reward should therefor be less than 10%"
			+ "\n of smallest reward. In the Gringotts example minimum_reward should be 0.0001 or 0.00005.")
	public double minimumReward = 0.01;

	@ConfigField(name = "newplayer_learning_mode", category = "general", comment = "When a new playerjoins the server he will by default start"
			+ "\nin 'LEARNING MODE' and get extra information about when he get rewards and not,"
			+ "\nwhen killing Mobs. The player can disable this InGame by using the command '/mobhunt learn'")
	public boolean learningMode = false;

	@ConfigField(name = "use_gui_for_achievements", category = "general", comment = "When use_gui_for_achivements=true the status of players achievements will"
			+ "\nbe showed in a Inventory GUI.")
	public boolean useGuiForAchievements = true;

	@ConfigField(name = "use_gui_for_bounties", category = "general", comment = "When use_gui_for_bounties=true the open bounties and most wanted players will"
			+ "\nbe showed in a Inventory GUI.")
	public boolean useGuiForBounties = true;

	@ConfigField(name = "try-to-cancel-natural-drops", category = "general", comment = "Try to cancel natural drops when a mob is killed while the MobHunting reward is cancelled "
			+ "\n(because player is grinding or protected by Worldguard)"
			+ "\nIf you want the mobs to drops normal rewards set " + "\n\"try-to-cancel-natural-drops\"=false")
	public boolean tryToCancelNaturalDrops = true;

	@ConfigField(name = "try-to-cancel-xp-drops", category = "general", comment = "Try to cancel XP drops when a mob is killed while the MobHunting reward is cancelled "
			+ "\n(because player is grinding or protected by Worldguard)"
			+ "\nIf you want the mobs to drop normal XP set " + "\n\"try-to-cancel-xp-drops\"=false")
	public boolean tryToCancelXPDrops = true;

	@ConfigField(name = "try-to-cancel-natural-drops-when-in-creative", category = "general", comment = "Try to cancel natural drops when a mob is killed the player is in creative mode."
			+ "\nIf you want the mobs to drops normal rewards set "
			+ "\n\"try-to-cancel-natural-drops-when-in-creative\"=false")
	public boolean tryToCancelNaturalDropsWhenInCreative = true;

	@ConfigField(name = "try-to-cancel-xp-drops-when-in-creative", category = "general", comment = "Try to cancel XP drops when a mob is killed while the player is in creative mode."
			+ "\nIf you want the mobs to drop normal XP set " + "\n\"try-to-cancel-xp-drops-when-in-creative\"=false")
	public boolean tryToCancelXPDropsWhenInCreative = true;

	@Override
	protected void onPostLoad() throws InvalidConfigurationException {
		Messages.setLanguage(language);
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
					* MobHunting.getRewardManager().getBalance(playerToBeRobbed) / 100);
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
			if (mob.hasMetadata(TARDISWeepingAngelsCompat.MH_TARDISWEEPINGANGELS)) {
				List<MetadataValue> data = mob.getMetadata(TARDISWeepingAngelsCompat.MH_TARDISWEEPINGANGELS);
				for (MetadataValue value : data)
					if (value.value() instanceof MobRewardData)
						return getPrice(mob, ((MobRewardData) value.value()).getRewardPrize());
			} else if (TARDISWeepingAngelsCompat.getMobRewardData()
					.containsKey(TARDISWeepingAngelsCompat.getWeepingAngelMonsterType(mob).name()))
				return getPrice(mob, TARDISWeepingAngelsCompat.getMobRewardData()
						.get(TARDISWeepingAngelsCompat.getWeepingAngelMonsterType(mob).name()).getRewardPrize());
			Messages.debug("TARDISWeepingAngel %s has no reward data",
					TARDISWeepingAngelsCompat.getWeepingAngelMonsterType(mob).getName());
			return 0;

		} else if (MythicMobsCompat.isMythicMob(mob)) {
			if (mob.hasMetadata(MythicMobsCompat.MH_MYTHICMOBS)) {
				List<MetadataValue> data = mob.getMetadata(MythicMobsCompat.MH_MYTHICMOBS);
				for (MetadataValue value : data)
					if (value.value() instanceof MobRewardData)
						return getPrice(mob, ((MobRewardData) value.value()).getRewardPrize());
			} else if (MythicMobsCompat.getMobRewardData().containsKey(MythicMobsCompat.getMythicMobType(mob)))
				return getPrice(mob, MythicMobsCompat.getMobRewardData().get(MythicMobsCompat.getMythicMobType(mob))
						.getRewardPrize());
			Messages.debug("MythicMob %s has no reward data", MythicMobsCompat.getMythicMobType(mob));
			return 0;

		} else if (CitizensCompat.isSentryOrSentinel(mob)) {
			NPC npc = CitizensAPI.getNPCRegistry().getNPC(mob);
			String key = String.valueOf(npc.getId());
			if (mob.hasMetadata(CitizensCompat.MH_CITIZENS)) {
				List<MetadataValue> data = mob.getMetadata(CitizensCompat.MH_CITIZENS);
				for (MetadataValue value : data)
					if (value.value() instanceof MobRewardData)
						return getPrice(mob, ((MobRewardData) value.value()).getRewardPrize());
			} else if (CitizensCompat.getMobRewardData().containsKey(key)) {
				return getPrice(mob, CitizensCompat.getMobRewardData().get(key).getRewardPrize());
			}
			Messages.debug("Citizens mob %s has no reward data", npc.getName());
			return 0;

		} else if (CustomMobsCompat.isCustomMob(mob)) {
			if (mob.hasMetadata(CustomMobsCompat.MH_CUSTOMMOBS)) {
				List<MetadataValue> data = mob.getMetadata(CustomMobsCompat.MH_CUSTOMMOBS);
				for (MetadataValue value : data)
					if (value.value() instanceof MobRewardData)
						return getPrice(mob, ((MobRewardData) value.value()).getRewardPrize());
			} else if (CustomMobsCompat.getMobRewardData().containsKey(CustomMobsCompat.getCustomMobType(mob)))
				return getPrice(mob, CustomMobsCompat.getMobRewardData().get(CustomMobsCompat.getCustomMobType(mob))
						.getRewardPrize());
			Messages.debug("CustomMob %s has no reward data", CustomMobsCompat.getCustomMobType(mob));
			return 0;

		} else if (MysteriousHalloweenCompat.isMysteriousHalloween(mob)) {
			if (mob.hasMetadata(MysteriousHalloweenCompat.MH_MYSTERIOUSHALLOWEEN)) {
				List<MetadataValue> data = mob.getMetadata(MysteriousHalloweenCompat.MH_MYSTERIOUSHALLOWEEN);
				for (MetadataValue value : data)
					if (value.value() instanceof MobRewardData)
						return getPrice(mob, ((MobRewardData) value.value()).getRewardPrize());
			} else if (MysteriousHalloweenCompat.getMobRewardData()
					.containsKey(MysteriousHalloweenCompat.getMysteriousHalloweenType(mob).name()))
				return getPrice(mob, MysteriousHalloweenCompat.getMobRewardData()
						.get(MysteriousHalloweenCompat.getMysteriousHalloweenType(mob).name()).getRewardPrize());
			Messages.debug("MysteriousHalloween %s has no reward data",
					MysteriousHalloweenCompat.getMysteriousHalloweenType(mob).name());
			return 0;

		} else if (MyPetCompat.isMyPet(mob)) {
			Messages.debug("Tried to find a prize for a MyPet: %s (Owner=%s)", MyPetCompat.getMyPet(mob),
					MyPetCompat.getMyPetOwner(mob));
			return getPrice(mob, MobHunting.getConfigManager().wolfPrize);

		} else {
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
							* MobHunting.getRewardManager().getBalance((Player) mob) / 100);
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
				return Misc.round(getPrice(mob, MobHunting.getConfigManager().zombiePigmanPrize) * MobHunting.getConfigManager().babyMultiplier);
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
		Messages.debug("Mobhunting could not find the prize for killing this mob: %s (%s)",
				ExtendedMobManager.getMobName(mob), mob.getType());
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
		if (TARDISWeepingAngelsCompat.isSupported() && TARDISWeepingAngelsCompat.isWeepingAngelMonster(mob)) {
			if (mob.hasMetadata(TARDISWeepingAngelsCompat.MH_TARDISWEEPINGANGELS)) {
				List<MetadataValue> data = mob.getMetadata(TARDISWeepingAngelsCompat.MH_TARDISWEEPINGANGELS);
				for (MetadataValue value : data)
					if (value.value() instanceof MobRewardData)
						return ((MobRewardData) value.value()).getConsoleRunCommand();
			} else if (TARDISWeepingAngelsCompat.getMobRewardData()
					.containsKey(TARDISWeepingAngelsCompat.getWeepingAngelMonsterType(mob).name()))
				return TARDISWeepingAngelsCompat.getMobRewardData()
						.get(TARDISWeepingAngelsCompat.getWeepingAngelMonsterType(mob).name()).getConsoleRunCommand();
			return "";

		} else if (MythicMobsCompat.isSupported() && MythicMobsCompat.isMythicMob(mob)) {
			if (mob.hasMetadata(MythicMobsCompat.MH_MYTHICMOBS)) {
				List<MetadataValue> data = mob.getMetadata(MythicMobsCompat.MH_MYTHICMOBS);
				for (MetadataValue value : data)
					if (value.value() instanceof MobRewardData)
						return ((MobRewardData) value.value()).getConsoleRunCommand();
			} else if (MythicMobsCompat.getMobRewardData().containsKey(MythicMobsCompat.getMythicMobType(mob)))
				return MythicMobsCompat.getMobRewardData().get(MythicMobsCompat.getMythicMobType(mob))
						.getConsoleRunCommand();
			return "";

		} else if (CitizensCompat.isSupported() && CitizensCompat.isNPC(mob)
				&& CitizensCompat.isSentryOrSentinel(mob)) {
			NPC npc = CitizensAPI.getNPCRegistry().getNPC(mob);
			String key = String.valueOf(npc.getId());
			if (mob.hasMetadata(CitizensCompat.MH_CITIZENS)) {
				List<MetadataValue> data = mob.getMetadata(CitizensCompat.MH_CITIZENS);
				for (MetadataValue value : data)
					if (value.value() instanceof MobRewardData)
						return ((MobRewardData) value.value()).getConsoleRunCommand();
			} else if (CitizensCompat.getMobRewardData().containsKey(key)) {
				return CitizensCompat.getMobRewardData().get(key).getConsoleRunCommand();
			}
			return "";

		} else if (CustomMobsCompat.isSupported() && CustomMobsCompat.isCustomMob(mob)) {
			if (mob.hasMetadata(CustomMobsCompat.MH_CUSTOMMOBS)) {
				List<MetadataValue> data = mob.getMetadata(CustomMobsCompat.MH_CUSTOMMOBS);
				for (MetadataValue value : data)
					if (value.value() instanceof MobRewardData)
						return ((MobRewardData) value.value()).getConsoleRunCommand();
			} else if (CustomMobsCompat.getMobRewardData().containsKey(CustomMobsCompat.getCustomMobType(mob)))
				return CustomMobsCompat.getMobRewardData().get(CustomMobsCompat.getCustomMobType(mob))
						.getConsoleRunCommand();
			return "";

		} else if (MysteriousHalloweenCompat.isSupported() && MysteriousHalloweenCompat.isMysteriousHalloween(mob)) {
			if (mob.hasMetadata(MysteriousHalloweenCompat.MH_MYSTERIOUSHALLOWEEN)) {
				List<MetadataValue> data = mob.getMetadata(MysteriousHalloweenCompat.MH_MYSTERIOUSHALLOWEEN);
				for (MetadataValue value : data)
					if (value.value() instanceof MobRewardData)
						return ((MobRewardData) value.value()).getConsoleRunCommand();
			} else if (MysteriousHalloweenCompat.getMobRewardData()
					.containsKey(MysteriousHalloweenCompat.getMysteriousHalloweenType(mob).name()))
				return MysteriousHalloweenCompat.getMobRewardData()
						.get(MysteriousHalloweenCompat.getMysteriousHalloweenType(mob).name()).getConsoleRunCommand();
			return "";

		} else if (MyPetCompat.isMyPet(mob)) {
			return MobHunting.getConfigManager().wolfCmd;

		} else {
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
		if (TARDISWeepingAngelsCompat.isSupported() && TARDISWeepingAngelsCompat.isWeepingAngelMonster(mob)) {
			if (mob.hasMetadata(TARDISWeepingAngelsCompat.MH_TARDISWEEPINGANGELS)) {
				List<MetadataValue> data = mob.getMetadata(TARDISWeepingAngelsCompat.MH_TARDISWEEPINGANGELS);
				for (MetadataValue value : data)
					if (value.value() instanceof MobRewardData)
						return ((MobRewardData) value.value()).getRewardDescription();
			} else if (TARDISWeepingAngelsCompat.getMobRewardData()
					.containsKey(TARDISWeepingAngelsCompat.getWeepingAngelMonsterType(mob).name()))
				return TARDISWeepingAngelsCompat.getMobRewardData()
						.get(TARDISWeepingAngelsCompat.getWeepingAngelMonsterType(mob).name()).getRewardDescription();
			return "";

		} else if (MythicMobsCompat.isSupported() && MythicMobsCompat.isMythicMob(mob)) {
			if (mob.hasMetadata(MythicMobsCompat.MH_MYTHICMOBS)) {
				List<MetadataValue> data = mob.getMetadata(MythicMobsCompat.MH_MYTHICMOBS);
				for (MetadataValue value : data)
					if (value.value() instanceof MobRewardData)
						return ((MobRewardData) value.value()).getRewardDescription();
			} else if (MythicMobsCompat.getMobRewardData().containsKey(MythicMobsCompat.getMythicMobType(mob)))
				return MythicMobsCompat.getMobRewardData().get(MythicMobsCompat.getMythicMobType(mob))
						.getRewardDescription();
			return "";

		} else if (CitizensCompat.isSupported() && CitizensCompat.isNPC(mob)
				&& CitizensCompat.isSentryOrSentinel(mob)) {
			NPC npc = CitizensAPI.getNPCRegistry().getNPC(mob);
			String key = String.valueOf(npc.getId());
			if (mob.hasMetadata(CitizensCompat.MH_CITIZENS)) {
				List<MetadataValue> data = mob.getMetadata(CitizensCompat.MH_CITIZENS);
				for (MetadataValue value : data)
					if (value.value() instanceof MobRewardData)
						return ((MobRewardData) value.value()).getRewardDescription();
			} else if (CitizensCompat.getMobRewardData().containsKey(key)) {
				return CitizensCompat.getMobRewardData().get(key).getRewardDescription();
			}
			return "";

		} else if (CustomMobsCompat.isSupported() && CustomMobsCompat.isCustomMob(mob)) {
			if (mob.hasMetadata(CustomMobsCompat.MH_CUSTOMMOBS)) {
				List<MetadataValue> data = mob.getMetadata(CustomMobsCompat.MH_CUSTOMMOBS);
				for (MetadataValue value : data)
					if (value.value() instanceof MobRewardData)
						return ((MobRewardData) value.value()).getRewardDescription();
			} else if (CustomMobsCompat.getMobRewardData().containsKey(CustomMobsCompat.getCustomMobType(mob)))
				return CustomMobsCompat.getMobRewardData().get(CustomMobsCompat.getCustomMobType(mob))
						.getRewardDescription();
			return "";

		} else if (MysteriousHalloweenCompat.isSupported() && MysteriousHalloweenCompat.isMysteriousHalloween(mob)) {
			if (mob.hasMetadata(MysteriousHalloweenCompat.MH_MYSTERIOUSHALLOWEEN)) {
				List<MetadataValue> data = mob.getMetadata(MysteriousHalloweenCompat.MH_MYSTERIOUSHALLOWEEN);
				for (MetadataValue value : data)
					if (value.value() instanceof MobRewardData)
						return ((MobRewardData) value.value()).getRewardDescription();
			} else if (MysteriousHalloweenCompat.getMobRewardData()
					.containsKey(MysteriousHalloweenCompat.getMysteriousHalloweenType(mob).name()))
				return MysteriousHalloweenCompat.getMobRewardData()
						.get(MysteriousHalloweenCompat.getMysteriousHalloweenType(mob).name()).getRewardDescription();
			return "";

		} else if (MyPetCompat.isMyPet(mob)) {
			return MobHunting.getConfigManager().wolfCmdDesc;

		} else {
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
		if (TARDISWeepingAngelsCompat.isSupported() && TARDISWeepingAngelsCompat.isWeepingAngelMonster(killed)) {
			if (killed.hasMetadata(TARDISWeepingAngelsCompat.MH_TARDISWEEPINGANGELS)) {
				List<MetadataValue> data = killed.getMetadata(TARDISWeepingAngelsCompat.MH_TARDISWEEPINGANGELS);
				for (MetadataValue value : data)
					if (value.value() instanceof MobRewardData)
						return ((MobRewardData) value.value()).getChance();
			} else if (TARDISWeepingAngelsCompat.getMobRewardData()
					.containsKey(TARDISWeepingAngelsCompat.getWeepingAngelMonsterType(killed).name()))
				return TARDISWeepingAngelsCompat.getMobRewardData()
						.get(TARDISWeepingAngelsCompat.getWeepingAngelMonsterType(killed).name()).getChance();
			return 0;

		} else if (MythicMobsCompat.isSupported() && MythicMobsCompat.isMythicMob(killed)) {
			if (killed.hasMetadata(MythicMobsCompat.MH_MYTHICMOBS)) {
				List<MetadataValue> data = killed.getMetadata(MythicMobsCompat.MH_MYTHICMOBS);
				for (MetadataValue value : data)
					if (value.value() instanceof MobRewardData)
						return ((MobRewardData) value.value()).getChance();

			} else if (MythicMobsCompat.getMobRewardData().containsKey(MythicMobsCompat.getMythicMobType(killed)))
				return MythicMobsCompat.getMobRewardData().get(MythicMobsCompat.getMythicMobType(killed)).getChance();
			return 0;

		} else if (CitizensCompat.isSupported() && CitizensCompat.isNPC(killed)
				&& CitizensCompat.isSentryOrSentinel(killed)) {
			NPCRegistry registry = CitizensAPI.getNPCRegistry();
			NPC npc = registry.getNPC(killed);
			String key = String.valueOf(npc.getId());
			if (killed.hasMetadata(CitizensCompat.MH_CITIZENS)) {
				List<MetadataValue> data = killed.getMetadata(CitizensCompat.MH_CITIZENS);
				for (MetadataValue value : data)
					if (value.value() instanceof MobRewardData)
						return ((MobRewardData) value.value()).getChance();
			} else if (CitizensCompat.getMobRewardData().containsKey(key)) {
				return CitizensCompat.getMobRewardData().get(key).getChance();
			}
			return 0;

		} else if (CustomMobsCompat.isSupported() && CustomMobsCompat.isCustomMob(killed)) {
			if (killed.hasMetadata(CustomMobsCompat.MH_CUSTOMMOBS)) {
				List<MetadataValue> data = killed.getMetadata(CustomMobsCompat.MH_CUSTOMMOBS);
				for (MetadataValue value : data)
					if (value.value() instanceof MobRewardData)
						return ((MobRewardData) value.value()).getChance();
			} else if (CustomMobsCompat.getMobRewardData().containsKey(CustomMobsCompat.getCustomMobType(killed)))
				return CustomMobsCompat.getMobRewardData().get(CustomMobsCompat.getCustomMobType(killed)).getChance();
			return 0;

		} else if (MysteriousHalloweenCompat.isSupported() && MysteriousHalloweenCompat.isMysteriousHalloween(killed)) {
			if (killed.hasMetadata(MysteriousHalloweenCompat.MH_MYSTERIOUSHALLOWEEN)) {
				List<MetadataValue> data = killed.getMetadata(MysteriousHalloweenCompat.MH_MYSTERIOUSHALLOWEEN);
				for (MetadataValue value : data)
					if (value.value() instanceof MobRewardData)
						return ((MobRewardData) value.value()).getChance();
			} else if (MysteriousHalloweenCompat.getMobRewardData()
					.containsKey(MysteriousHalloweenCompat.getMysteriousHalloweenType(killed).name()))
				return MysteriousHalloweenCompat.getMobRewardData()
						.get(MysteriousHalloweenCompat.getMysteriousHalloweenType(killed).name()).getChance();
			return 0;

		} else if (MyPetCompat.isMyPet(killed)) {
			return (double) MobHunting.getConfigManager().wolfFequency
					/ (double) MobHunting.getConfigManager().wolfFrequencyBase;

		} else {
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
					return (double) MobHunting.getConfigManager().strayFrequency
							/ (double) MobHunting.getConfigManager().strayFrequencyBase;
				else if (killed instanceof Husk)
					return (double) MobHunting.getConfigManager().huskFrequency
							/ (double) MobHunting.getConfigManager().huskFrequencyBase;
				else if (killed instanceof ZombieVillager)
					return MobHunting.getConfigManager().zombieVillagerCmdRunChance;
				else if (killed instanceof Villager && ((Villager) killed).getProfession() == Profession.NITWIT)
					return MobHunting.getConfigManager().nitwitCmdRunChance;

			if (Misc.isMC110OrNewer())
				if (killed instanceof PolarBear)
					return (double) MobHunting.getConfigManager().polarBearFrequency
							/ (double) MobHunting.getConfigManager().polarBearFrequencyBase;
				else if (killed instanceof Skeleton && ((Skeleton) killed).getSkeletonType() == SkeletonType.STRAY)
					return (double) MobHunting.getConfigManager().strayFrequency
							/ (double) MobHunting.getConfigManager().strayFrequencyBase;
				else if (killed instanceof Zombie && ((Zombie) killed).getVillagerProfession() == Profession.HUSK)
					return (double) MobHunting.getConfigManager().huskFrequency
							/ (double) MobHunting.getConfigManager().huskFrequencyBase;

				else if (killed instanceof Villager && ((Villager) killed).getProfession() == Profession.NORMAL)
					return (double) MobHunting.getConfigManager().villagerFequency
							/ (double) MobHunting.getConfigManager().villagerFrequencyBase;
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
					return (double) MobHunting.getConfigManager().shulkerFrequency
							/ (double) MobHunting.getConfigManager().shulkerFrequencyBase;

			if (Misc.isMC18OrNewer())
				if (killed instanceof Guardian && ((Guardian) killed).isElder())
					return (double) MobHunting.getConfigManager().elderGuardianFrequency
							/ (double) MobHunting.getConfigManager().elderGuardianFrequencyBase;
				else if (killed instanceof Guardian)
					return (double) MobHunting.getConfigManager().guardianFrequency
							/ (double) MobHunting.getConfigManager().guardianFrequencyBase;
				else if (killed instanceof Endermite)
					return (double) MobHunting.getConfigManager().endermiteFrequency
							/ (double) MobHunting.getConfigManager().endermiteFrequencyBase;
				else if (killed instanceof Rabbit)
					if ((((Rabbit) killed).getRabbitType()) == Rabbit.Type.THE_KILLER_BUNNY)
						return (double) MobHunting.getConfigManager().killerrabbitFrequency
								/ (double) MobHunting.getConfigManager().killerrabbitFrequencyBase;
					else
						return (double) MobHunting.getConfigManager().rabbitFrequency
								/ (double) MobHunting.getConfigManager().rabbitFrequencyBase;

			// MC1.7 or older
			if (killed instanceof Player) {
				return pvpKillCmdRunChance;
			} else if (killed instanceof Blaze)
				return (double) MobHunting.getConfigManager().blazeFrequency
						/ (double) MobHunting.getConfigManager().blazeFrequencyBase;
			else if (killed instanceof Creeper)
				return (double) MobHunting.getConfigManager().creeperFrequency
						/ (double) MobHunting.getConfigManager().creeperFrequencyBase;
			else if (killed instanceof Silverfish)
				return (double) MobHunting.getConfigManager().silverfishFrequency
						/ (double) MobHunting.getConfigManager().silverfishFrequencyBase;
			else if (killed instanceof Enderman)
				return (double) MobHunting.getConfigManager().endermanFrequency
						/ (double) MobHunting.getConfigManager().endermanFrequencyBase;
			else if (killed instanceof Giant)
				return (double) MobHunting.getConfigManager().giantFrequency
						/ (double) MobHunting.getConfigManager().giantFrequencyBase;
			else if (killed instanceof Skeleton && ((Skeleton) killed).getSkeletonType() == SkeletonType.NORMAL)
				return (double) MobHunting.getConfigManager().skeletonFrequency
						/ (double) MobHunting.getConfigManager().skeletonFrequencyBase;
			else if (killed instanceof Skeleton && ((Skeleton) killed).getSkeletonType() == SkeletonType.WITHER)
				return (double) MobHunting.getConfigManager().witherSkeletonFrequency
						/ (double) MobHunting.getConfigManager().witherSkeletonFrequencyBase;
			else if (killed instanceof CaveSpider)
				// CaveSpider is a subclass of Spider
				return (double) MobHunting.getConfigManager().caveSpiderFrequency
						/ (double) MobHunting.getConfigManager().caveSpiderFrequencyBase;
			else if (killed instanceof Spider)
				return (double) MobHunting.getConfigManager().spiderFrequency
						/ (double) MobHunting.getConfigManager().spiderFrequencyBase;
			else if (killed instanceof Witch)
				return (double) MobHunting.getConfigManager().witchFrequency
						/ (double) MobHunting.getConfigManager().witchFrequencyBase;
			else if (killed instanceof PigZombie)
				// PigZombie is a subclass of Zombie.
				return (double) MobHunting.getConfigManager().zombiePigmanFrequency
						/ (double) MobHunting.getConfigManager().zombiePigmanFrequencyBase;
			else if (killed instanceof Zombie)
				return (double) MobHunting.getConfigManager().zombieFrequency
						/ (double) MobHunting.getConfigManager().zombieFrequencyBase;
			else if (killed instanceof Ghast)
				return (double) MobHunting.getConfigManager().ghastFrequency
						/ (double) MobHunting.getConfigManager().ghastFrequencyBase;
			else if (killed instanceof MagmaCube)
				// MagmaCube is a subclass of Slime
				return (double) MobHunting.getConfigManager().magmaCubeFrequency
						/ (double) MobHunting.getConfigManager().magmaCubeFrequencyBase;
			else if (killed instanceof Slime)
				return (double) MobHunting.getConfigManager().slimeFrequency
						/ (double) MobHunting.getConfigManager().slimeFrequencyBase;
			else if (killed instanceof EnderDragon)
				return (double) MobHunting.getConfigManager().enderdragonFrequency
						/ (double) MobHunting.getConfigManager().enderdragonFrequencyBase;
			else if (killed instanceof Wither)
				return (double) MobHunting.getConfigManager().witherFrequency
						/ (double) MobHunting.getConfigManager().witherFrequencyBase;
			else if (killed instanceof IronGolem)
				return (double) MobHunting.getConfigManager().ironGolemFrequency
						/ (double) MobHunting.getConfigManager().ironGolemFrequencyBase;

			// Passive mobs
			else if (killed instanceof Bat)
				return (double) MobHunting.getConfigManager().batFrequency
						/ (double) MobHunting.getConfigManager().batFrequencyBase;
			else if (killed instanceof Chicken)
				return (double) MobHunting.getConfigManager().chickenFrequency
						/ (double) MobHunting.getConfigManager().chickenFrequencyBase;
			else if (killed instanceof Cow)
				if (killed instanceof MushroomCow)
					// MushroomCow is a subclass of Cow
					return (double) MobHunting.getConfigManager().mushroomCowFrequency
							/ (double) MobHunting.getConfigManager().mushroomCowFrequencyBase;
				else
					return (double) MobHunting.getConfigManager().cowFrequency
							/ (double) MobHunting.getConfigManager().cowFrequencyBase;
			else if (killed instanceof Horse)
				return (double) MobHunting.getConfigManager().horseFrequency
						/ (double) MobHunting.getConfigManager().horseFrequencyBase;
			else if (killed instanceof Ocelot)
				return (double) MobHunting.getConfigManager().ocelotFrequency
						/ (double) MobHunting.getConfigManager().ocelotFrequencyBase;
			else if (killed instanceof Pig)
				return (double) MobHunting.getConfigManager().pigFrequency
						/ (double) MobHunting.getConfigManager().pigFrequencyBase;
			else if (killed instanceof Sheep)
				return (double) MobHunting.getConfigManager().sheepFrequency
						/ (double) MobHunting.getConfigManager().sheepFrequencyBase;
			else if (killed instanceof Snowman)
				return (double) MobHunting.getConfigManager().snowmanFrequency
						/ (double) MobHunting.getConfigManager().snowmanFrequencyBase;
			else if (killed instanceof Squid)
				return (double) MobHunting.getConfigManager().squidFrequency
						/ (double) MobHunting.getConfigManager().squidFrequencyBase;
			else if (killed instanceof Villager)
				return (double) MobHunting.getConfigManager().villagerFequency
						/ (double) MobHunting.getConfigManager().villagerFrequencyBase;
			else if (killed instanceof Wolf)
				return (double) MobHunting.getConfigManager().wolfFequency
						/ (double) MobHunting.getConfigManager().wolfFrequencyBase;
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

	public boolean isCmdGointToBeExcuted(Entity killed) {
		double randomDouble = MobHunting.getMobHuntingManager().mRand.nextDouble();
		double runChanceDouble = getCmdRunChance(killed);
		Messages.debug("random double=%s < chance=%s", randomDouble, runChanceDouble);
		if (killed instanceof Player)
			return randomDouble < pvpKillCmdRunChance;
		else
			return !getKillConsoleCmd(killed).equals("") && randomDouble < runChanceDouble;
	}

}
