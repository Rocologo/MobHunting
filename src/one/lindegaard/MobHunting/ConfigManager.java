package one.lindegaard.MobHunting;

import java.io.File;
import java.util.HashMap;
import one.lindegaard.MobHunting.util.AutoConfig;
import one.lindegaard.MobHunting.util.ConfigField;
import org.bukkit.configuration.InvalidConfigurationException;

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
				+ "\nThe player will have the cmd run in {mob-cmd-run-chance} times in average. If mob-cmd-run-chance=0 it"
				+ "\nwill never run. If f.ex. mob-cmd-run-chance=0.50 and it will run run every second time in average."
				+ "\n\nThe mobname-head-prize is only used if you want the dropped heads after killing a mob to have a value."
				+ "\nPlease also check the \"dropmoneyonground\" section in this file.");

		setCategoryComment("boss",
				"########################################################################"
						+ "\nRewards for killing bosses"
						+ "\n########################################################################"
						+ "\nHere is where you set the base prize in $ for killing the bosses");

		setCategoryComment("villager",
				"########################################################################"
						+ "\nRewards for killing villagers"
						+ "\n########################################################################"
						+ "\nHere is where you set the base prize in $ for killing the villagers"
						+ "\nMobHunting only handle Villagers on profession level, all careers is "
						+ "\nhandles as their profession. Info anbout Profession and Caarer:"
						+ "\nhttp://minecraft.gamepedia.com/Villager#Professions_and_careers");

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

		setCategoryComment("achievements",
				"########################################################################"
						+ "\nSpecial / Achievements rewards"
						+ "\n########################################################################"
						+ "\nHere is where you set the prize in $ for achieving a special kill. "
						+ "\nFor each achievment you can run a console command to give the player a reward. "
						+ "\nYou can use the following variables {player},{world}, {killerpos},"
						+ "\n{monstertype} and more can be added on request."
						+ "\nmonstertype is the monstername. A valid list can be found in your "
						+ "\nlang file. Ex. if it is mobs.skeleton.name, monstertype will return skeleton"
						+ "\nAn example command could be to give the player permission to fly "
						+ "\nfor 1 hour or use give command to the player items."
						+ "\nYou can also specify the message send to the player."
						+ "\nYou can run many console commands on each line, each command" + "\nmust be separated by |"
						+ "\nAchievements will not be shown in the GUI if there is a reward for killing the mob,"
						+ "\nunless you set show-achievements-without-reward=true.");

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

		setCategoryComment("infernalmobs",
				"########################################################################" + "\nInfernalMobs settings"
						+ "\n########################################################################"
						+ "\nHere you can change the behavior of InfernalMobs Integration, or you can disable"
						+ "\nintegration completely." + "\nhttps://www.spigotmc.org/resources/infernal-mobs.2156/");

		setCategoryComment("conquestiamobs",
				"########################################################################"
						+ "\nConquestia Mobs settings"
						+ "\n########################################################################"
						+ "\nHere you can change the behavior of ConquestiaMobs Integration, or you can disable"
						+ "\nintegration completely." + "\nhttps://www.spigotmc.org/resources/conquesita-mobs.21307/");

		setCategoryComment("factions",
				"########################################################################" + "\nFactions settings"
						+ "\n########################################################################"
						+ "\nHere you can change the behavior of the Factions integration, or you can disable"
						+ "\nintegration completely." + "\nhttps://www.spigotmc.org/resources/factions.1900/");

		setCategoryComment("towny",
				"########################################################################" + "\nTowny settings"
						+ "\n########################################################################"
						+ "\nHere you can change the behavior of the Towny integration, or you can disable"
						+ "\nintegration completely." + "\nhttp://towny.palmergames.com/");

		setCategoryComment("residence",
				"########################################################################" + "\nTowny settings"
						+ "\n########################################################################"
						+ "\nHere you can change the behavior of the Residence integration, or you can disable"
						+ "\nintegration completely."
						+ "\nhttps://www.spigotmc.org/resources/residence-1-7-10-up-to-1-11.11480/");

		setCategoryComment("grinding",
				"########################################################################"
						+ "\nGrinding detection settings"
						+ "\n########################################################################"
						+ "\nHere you can change the behavior of the grinding detection.");

		setCategoryComment("mcmmo",
				"########################################################################" + "\nIntegration to McMMO"
						+ "\n########################################################################"
						+ "\nThis section only relevant if you use McMMO."
						+ "\nHere you configure if the player will get McMMO Levels for MobHunting kills and"
						+ "\nand the chance to get the xp.");

		setCategoryComment("crackshot",
				"########################################################################"
						+ "\nIntegration to CrackShot"
						+ "\n########################################################################"
						+ "\nThis section only relevant if you use CrackShot."
						+ "\nHere you configure if the player will get a multiplier for using a CrackShot weapon");

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

		setCategoryComment("economy", "########################################################################"
				+ "\nEconomy Settings" + "\n########################################################################");

		setCategoryComment("general", "########################################################################"
				+ "\nGeneral Settings" + "\n########################################################################");

	}

	// #####################################################################################
	// Mobs
	// #####################################################################################
	@ConfigField(name = "blaze", category = "mobs")
	public String blazePrize = "10.0";
	@ConfigField(name = "blaze-cmd", category = "mobs")
	public String blazeCmd = "mobhunt head give {player} Blaze Blaze 1 silent|give {player} Iron_ingot 1";
	@ConfigField(name = "blaze-cmd-desc", category = "mobs")
	public String blazeCmdDesc = "You got a Blaze skull and an Iron ingot.";
	@ConfigField(name = "blaze-cmd-run-chance", category = "mobs")
	public double blazeCmdRunChance = 0.10;
	@ConfigField(name = "blaze-head-prize", category = "mobs")
	public String blazeHeadPrize = "0";

	@ConfigField(name = "cave-spider", category = "mobs")
	public String caveSpiderPrize = "10:20";
	@ConfigField(name = "cave-spider-cmd", category = "mobs")
	public String caveSpiderCmd = "mobhunt head give {player} Cave_Spider Cave_Spider 1 silent|give {player} Iron_ingot 1";
	@ConfigField(name = "cave-spider-cmd-desc", category = "mobs")
	public String caveSpiderCmdDesc = "You got a Cave Spider skull and an Iron ingot.";
	@ConfigField(name = "cave-spider-cmd-run-chance", category = "mobs")
	public double caveSpiderRunChance = 0.10;
	@ConfigField(name = "cave-spider-head-prize", category = "mobs")
	public String caveSpiderHeadPrize = "0";

	@ConfigField(name = "creeper", category = "mobs")
	public String creeperPrize = "10.0";
	@ConfigField(name = "creeper-cmd", category = "mobs")
	public String creeperCmd = "mobhunt head give {player} Creeper Creeper 1 silent|give {player} Iron_ingot 1";
	@ConfigField(name = "creeper-cmd-desc", category = "mobs")
	public String creeperCmdDesc = "You got a Creeper skull and an Iron ingot.";
	@ConfigField(name = "creeper-cmd-run-chance", category = "mobs")
	public double creeperCmdRunChance = 0.05;
	@ConfigField(name = "creeper-head-prize", category = "mobs")
	public String creeperHeadPrize = "0";

	@ConfigField(name = "elder-guardian", category = "mobs")
	public String elderGuardianPrize = "40:80";
	@ConfigField(name = "elder-guardian-cmd", category = "mobs")
	public String elderGuardianCmd = "mobhunt head give {player} ElderGuardian ElderGuardian 1 silent|give {player} Iron_ingot 1";
	@ConfigField(name = "elder-guardian-cmd-desc", category = "mobs")
	public String elderGuardianCmdDesc = "You got an Elder Guardian skull and an Iron ingot.";
	@ConfigField(name = "elder-guardian-cmd-run-chance", category = "mobs")
	public double elderGuardianCmdRunChance = 0.33;
	@ConfigField(name = "elder-guardian-head-prize", category = "mobs")
	public String elderGuardianHeadPrize = "0";

	@ConfigField(name = "enderman", category = "mobs")
	public String endermanPrize = "20:40";
	@ConfigField(name = "enderman-cmd", category = "mobs")
	public String endermanCmd = "mobhunt head give {player} Enderman Enderman 1 silent|give {player} gold_ingot 1";
	@ConfigField(name = "enderman-cmd-desc", category = "mobs")
	public String endermanCmdDesc = "You got an Enderman skull and a Gold ingot.";
	@ConfigField(name = "enderman-cmd-run-chance", category = "mobs")
	public double endermanCmdRunChance = 0.20;
	@ConfigField(name = "enderman-head-prize", category = "mobs")
	public String endermanHeadPrize = "0";

	@ConfigField(name = "endermite", category = "mobs")
	public String endermitePrize = "10";
	@ConfigField(name = "endermite-cmd", category = "mobs")
	public String endermiteCmd = "mobhunt head give {player} Endermite Endermite 1 silent|give {player} Iron_ingot 1";
	@ConfigField(name = "endermite-cmd-desc", category = "mobs")
	public String endermiteCmdDesc = "You got an Endermite skull and an Iron ingot.";
	@ConfigField(name = "endermite-cmd-run-chance", category = "mobs")
	public double endermiteCmdRunChance = 0.10;
	@ConfigField(name = "endermite-head-prize", category = "mobs")
	public String endermiteHeadPrize = "0";

	@ConfigField(name = "ghast", category = "mobs")
	public String ghastPrize = "40:80";
	@ConfigField(name = "ghast-cmd", category = "mobs")
	public String ghastCmd = "mobhunt head give {player} Ghast Ghast 1 silent|give {player} 406 4";
	@ConfigField(name = "ghast-cmd-desc", category = "mobs")
	public String ghastCmdDesc = "You got a Ghast skull and 4 Nether Quartz.";
	@ConfigField(name = "ghast-cmd-run-chance", category = "mobs")
	public double ghastCmdRunChance = 0.10;
	@ConfigField(name = "ghast-head-prize", category = "mobs")
	public String ghastHeadPrize = "0";

	@ConfigField(name = "giant", category = "mobs")
	public String giantPrize = "5.0";
	@ConfigField(name = "giant-cmd", category = "mobs")
	public String giantCmd = "mobhunt head give {player} Giant Giant 1 silent|give {player} Iron_ingot 1";
	@ConfigField(name = "giant-cmd-desc", category = "mobs")
	public String giantCmdDesc = "You got a Giant head and an Iron ingot.";
	@ConfigField(name = "giant-cmd-run-chance", category = "mobs")
	public double giantCmdRunChance = 0.05;
	@ConfigField(name = "giant-head-prize", category = "mobs")
	public String giantHeadPrize = "0";

	@ConfigField(name = "Iron-golem", category = "mobs")
	public String ironGolemPrize = "20:40";
	@ConfigField(name = "iron-golem-cmd", category = "mobs")
	public String ironGolemCmd = "mobhunt head give {player} Iron_Golem Iron_Golem 1 silent|give {player} Iron_ingot 4";
	@ConfigField(name = "iron-golem-cmd-desc", category = "mobs")
	public String ironGolemCmdDesc = "You got an Iron Golem skull and 4 Iron ingots.";
	@ConfigField(name = "iron-golem-cmd-run-chance", category = "mobs")
	public double ironGolemCmdRunChance = 0.10;
	@ConfigField(name = "iron-golem-head-prize", category = "mobs")
	public String ironGolemHeadPrize = "0";

	@ConfigField(name = "guardian", category = "mobs")
	public String guardianPrize = "20:40";
	@ConfigField(name = "guardian-cmd", category = "mobs")
	public String guardianCmd = "mobhunt head give {player} Guardian Guardian 1 silent|give {player} Iron_ingot 1";
	@ConfigField(name = "guardian-cmd-desc", category = "mobs")
	public String guardianCmdDesc = "You got a Guardian skull and an Iron ingot.";
	@ConfigField(name = "guardian-cmd-run-chance", category = "mobs")
	public double guardianCmdRunChance = 0.10;
	@ConfigField(name = "guardian-head-prize", category = "mobs")
	public String guardianHeadPrize = "0";

	@ConfigField(name = "husk", category = "mobs")
	public String huskPrize = "9:13";
	@ConfigField(name = "husk-cmd", category = "mobs")
	public String huskCmd = "mobhunt head give {player} Husk Husk 1 silent|give {player} Iron_ingot 1";
	@ConfigField(name = "husk-cmd-desc", category = "mobs")
	public String huskCmdDesc = "You got a Zombie Husk skull and an Iron ingot.";
	@ConfigField(name = "husk-cmd-run-chance", category = "mobs")
	public double huskCmdRunChance = 0.20;
	@ConfigField(name = "husk-head-prize", category = "mobs")
	public String huskHeadPrize = "0";

	@ConfigField(name = "killerrabbit", category = "mobs")
	public String killerrabbitPrize = "200";
	@ConfigField(name = "killerrabbit-cmd", category = "mobs")
	public String killerrabbitCmd = "mobhunt head give {player} KillerRabbit KillerRabbit 1 silent|give {player} Iron_ingot 1";
	@ConfigField(name = "killerrabbit-cmd-desc", category = "mobs")
	public String killerrabbitCmdDesc = "You got Killer Rabbit Skull and an Iron ingot.";
	@ConfigField(name = "killerrabbit-cmd-run-chance", category = "mobs")
	public double killerrabbitCmdRunChance = 0.25;
	@ConfigField(name = "killerrabbit-head-prize", category = "mobs")
	public String killerrabbitHeadPrize = "0";

	@ConfigField(name = "magma-cube", category = "mobs", comment = "This is multiplied by the size of the magma cube. So a big natural magma cube is 4x this value")
	public String magmaCubePrize = "10:20";
	@ConfigField(name = "magma-cube-cmd", category = "mobs")
	public String magmaCubeCmd = "mobhunt head give {player} MAGMA_CUBE MAGMA_CUBE 1 silent|give {player} Iron_ingot 1";
	@ConfigField(name = "magma-cube-cmd-desc", category = "mobs")
	public String magmaCubeCmdDesc = "You got a Magma Cube skull and an Iron ingot.";
	@ConfigField(name = "magma-cube-cmd-run-chance", category = "mobs")
	public double magmaCubeCmdRunChance = 0.10;
	@ConfigField(name = "magma-cube-head-prize", category = "mobs")
	public String magmaCubeHeadPrize = "0";

	@ConfigField(name = "polar-bear", category = "mobs")
	public String polarBearPrize = "25";
	@ConfigField(name = "polar-bear-cmd", category = "mobs")
	public String polarBearCmd = "mobhunt head give {player} Polar_Bear Polar_Bear 1 silent|give {player} Iron_ingot 1";
	@ConfigField(name = "polar-bear-cmd-desc", category = "mobs")
	public String polarBearCmdDesc = "You got a Polar Bear skull and an Iron ingot.";
	@ConfigField(name = "polar-bear-cmd-run-chance", category = "mobs")
	public double polarBearCmdRunChance = 0.25;
	@ConfigField(name = "polar-bear-head-prize", category = "mobs")
	public String polarBearHeadPrize = "0";

	@ConfigField(name = "slime-base", category = "mobs", comment = "This is multiplied by the size of the slime. So a big natural slime is 4x this value")
	public String slimeTinyPrize = "25";
	@ConfigField(name = "slime-cmd", category = "mobs")
	public String slimeCmd = "mobhunt head give {player} Slime Slime 1 silent|give {player} Iron_ingot 1";
	@ConfigField(name = "slime-cmd-desc", category = "mobs")
	public String slimeCmdDesc = "You got a Slime skull and an Iron ingot.";
	@ConfigField(name = "slime-cmd-run-chance", category = "mobs")
	public double slimeCmdRunChance = 0.05;
	@ConfigField(name = "slime-head-prize", category = "mobs")
	public String slimeHeadPrize = "0";

	@ConfigField(name = "shulker", category = "mobs")
	public String shulkerPrize = "25";
	@ConfigField(name = "shulker-cmd", category = "mobs")
	public String shulkerCmd = "mobhunt head give {player} Shulker Shulker 1 silent|give {player} Iron_ingot 1";
	@ConfigField(name = "shulker-cmd-desc", category = "mobs")
	public String shulkerCmdDesc = "You got a Shulker skull and an Iron ingot.";
	@ConfigField(name = "shulker-cmd-run-chance", category = "mobs")
	public double shulkerCmdRunChance = 0.50;
	@ConfigField(name = "shulker-head-prize", category = "mobs")
	public String shulkerHeadPrize = "0";

	@ConfigField(name = "stray", category = "mobs")
	public String strayPrize = "15:35";
	@ConfigField(name = "stray-cmd", category = "mobs")
	public String strayCmd = "mobhunt head give {player} Stray Stray 1 silent|give {player} Iron_ingot 1";
	@ConfigField(name = "stray-cmd-desc", category = "mobs")
	public String strayCmdDesc = "You got a Skeleton Stray skull and an Iron ingot.";
	@ConfigField(name = "stray-cmd-run-chance", category = "mobs")
	public double strayCmdRunChance = 0.20;
	@ConfigField(name = "stray-head-prize", category = "mobs")
	public String strayHeadPrize = "0";

	@ConfigField(name = "silverfish", category = "mobs")
	public String silverfishPrize = "10";
	@ConfigField(name = "silverfish-cmd", category = "mobs")
	public String silverfishCmd = "mobhunt head give {player} Silverfish Silverfish 1 silent|give {player} Iron_ingot 1";
	@ConfigField(name = "silverfish-cmd-desc", category = "mobs")
	public String silverfishCmdDesc = "You got a Silverfish head and an Iron ingot.";
	@ConfigField(name = "silverfish-cmd-run-chance", category = "mobs")
	public double silverfishCmdRunChance = 0.20;
	@ConfigField(name = "silverfish-head-prize", category = "mobs")
	public String silverfishHeadPrize = "0";

	@ConfigField(name = "skeleton", category = "mobs")
	public String skeletonPrize = "10:30";
	@ConfigField(name = "skeleton-cmd", category = "mobs")
	public String skeletonCmd = "mobhunt head give {player} Skeleton Skeleton 1 silent|give {player} dye 1 4";
	@ConfigField(name = "skeleton-cmd-desc", category = "mobs")
	public String skeletonCmdDesc = "You got a Skeleton skull and one Lapis Lazuli.";
	@ConfigField(name = "skeleton-cmd-run-chance", category = "mobs")
	public double skeletonCmdRunChance = 0.05;
	@ConfigField(name = "skeleton-head-prize", category = "mobs")
	public String skeletonHeadPrize = "0";

	@ConfigField(name = "spider", category = "mobs")
	public String spiderPrize = "5.5:10.5";
	@ConfigField(name = "spider-cmd", category = "mobs")
	public String spiderCmd = "mobhunt head give {player} Spider Spider 1 silent|give {player} Iron_ingot 1";
	@ConfigField(name = "spider-cmd-desc", category = "mobs")
	public String spiderCmdDesc = "You got a Spider skull and an Iron ingot.";
	@ConfigField(name = "spider-cmd-run-chance", category = "mobs")
	public double spiderCmdRunChance = 0.05;
	@ConfigField(name = "spider-head-prize", category = "mobs")
	public String spiderHeadPrize = "0";

	@ConfigField(name = "zombie", category = "mobs")
	public String zombiePrize = "7:11";
	@ConfigField(name = "zombie-cmd", category = "mobs")
	public String zombieCmd = "mobhunt head give {player} Zombie Zombie 1 silent|give {player} Iron_ingot 1";
	@ConfigField(name = "zombie-cmd-desc", category = "mobs")
	public String zombieCmdDesc = "You got a Zombie skull and an Iron ingot.";
	@ConfigField(name = "zombie-cmd-run-chance", category = "mobs")
	public double zombieCmdRunChance = 0.005;
	@ConfigField(name = "zombie-head-prize", category = "mobs")
	public String zombieHeadPrize = "0";

	@ConfigField(name = "zombie-pigman", category = "mobs")
	public String zombiePigmanPrize = "4:8";
	@ConfigField(name = "zombie-pigman-cmd", category = "mobs")
	public String zombiePigmanCmd = "mobhunt head give {player} Zombie_Pigman Zombie_Pigman 1 silent|give {player} Iron_ingot 1";
	@ConfigField(name = "zombie-pigman-cmd-desc", category = "mobs")
	public String zombiePigmanCmdDesc = "You got a Zombie Pigman skull and an Iron ingot.";
	@ConfigField(name = "zombie-pigman-cmd-run-chance", category = "mobs")
	public double zombiepigmanCmdRunChance = 0.10;
	@ConfigField(name = "zombie-pigman-head-prize", category = "mobs")
	public String zombiePigmanHeadPrize = "0";

	@ConfigField(name = "vex", category = "mobs")
	public String vexPrize = "10:15";
	@ConfigField(name = "vex-cmd", category = "mobs")
	public String vexCmd = "mobhunt head give {player} Vex Vex 1 silent|give {player} gold_ingot 1";
	@ConfigField(name = "vex-cmd-desc", category = "mobs")
	public String vexCmdDesc = "You got a Vex skull and a Gold ingot.";
	@ConfigField(name = "vex-cmd-run-chance", category = "mobs")
	public double vexCmdRunChance = 0.5;
	@ConfigField(name = "vex-head-prize", category = "mobs")
	public String vexHeadPrize = "0";

	@ConfigField(name = "witch", category = "mobs")
	public String witchPrize = "10:15";
	@ConfigField(name = "witch-cmd", category = "mobs")
	public String witchCmd = "mobhunt head give {player} Witch Witch 1 silent|give {player} gold_ingot 1";
	@ConfigField(name = "witch-cmd-desc", category = "mobs")
	public String witchCmdDesc = "You got a Witch skull and a Gold ingot.";
	@ConfigField(name = "witch-cmd-run-chance", category = "mobs")
	public double witchCmdRunChance = 0.05;
	@ConfigField(name = "witch-head-prize", category = "mobs")
	public String witchHeadPrize = "0";

	@ConfigField(name = "wither-skeleton", category = "mobs")
	public String witherSkeletonPrize = "30:50";
	@ConfigField(name = "wither-skeleton-cmd", category = "mobs")
	public String witherSkeletonCmd = "mobhunt head give {player} WitherSkeleton WitherSkeleton 1 silent|give {player} gold_ingot 1";
	@ConfigField(name = "wither-skeleton-cmd-desc", category = "mobs")
	public String witherSkeletonCmdDesc = "You got a Wither Skeleton skull and a Gold ingot.";
	@ConfigField(name = "wither-skeleton-cmd-run-chance", category = "mobs")
	public double witherSkeletonCmdRunChance = 0.10;
	@ConfigField(name = "wither-skeleton-head-prize", category = "mobs")
	public String witherSkeletonHeadPrize = "0";

	// #####################################################################################
	// Bosses
	// #####################################################################################
	@ConfigField(name = "wither", category = "boss")
	public String witherPrize = "1000.0:2000.0";
	@ConfigField(name = "wither-cmd", category = "boss")
	public String witherCmd = "mobhunt head give {player} Wither Wither 1 silent|give {player} diamond 10";
	@ConfigField(name = "wither-cmd-desc", category = "boss")
	public String witherCmdDesc = "You got a Wither skull and 10 Diamonds.";
	@ConfigField(name = "wither-cmd-run-chance", category = "boss")
	public double witherCmdRunChance = 0.5;
	@ConfigField(name = "wither-head-prize", category = "boss")
	public String witherHeadPrize = "0";

	@ConfigField(name = "enderdragon", category = "boss")
	public String enderdragonPrize = "2000.0:5000.0";
	@ConfigField(name = "enderdragon-cmd", category = "boss")
	public String enderdragonCmd = "mobhunt head give {player} Enderdragon Enderdragon 1 silent|give {player} diamond 10";
	@ConfigField(name = "enderdragon-cmd-desc", category = "boss")
	public String enderdragonCmdDesc = "You got an Enderdragon skull and ten Diamonds.";
	@ConfigField(name = "enderdragon-cmd-run-chance", category = "boss")
	public double enderdragonCmdRunChance = 0.50;
	@ConfigField(name = "enderdragon-head-prize", category = "boss")
	public String enderdragonHeadPrize = "0";

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
	@ConfigField(name = "blacksmith", category = "villager")
	public String blacksmithPrize = "1:2";
	@ConfigField(name = "blacksmith-cmd", category = "villager")
	public String blacksmithCmd = "mobhunt head give {player} Blacksmith Blacksmith 1";
	@ConfigField(name = "blacksmith-cmd-desc", category = "villager")
	public String blacksmithCmdDesc = "You got a Blacksmith skull";
	@ConfigField(name = "blacksmith-cmd-run-chance", category = "villager")
	public double blacksmithCmdRunChance = 1.00;
	@ConfigField(name = "blacksmith-head-prize", category = "villager")
	public String blacksmithHeadPrize = "0";

	@ConfigField(name = "butcher", category = "villager")
	public String butcherPrize = "1:2";
	@ConfigField(name = "butcher-cmd", category = "villager")
	public String butcherCmd = "mobhunt head give {player} Butcher Butcher 1";
	@ConfigField(name = "butcher-cmd-desc", category = "villager")
	public String butcherCmdDesc = "You got a Butcher skull";
	@ConfigField(name = "butcher-cmd-run-chance", category = "villager")
	public double butcherCmdRunChance = 1.00;
	@ConfigField(name = "butcher-head-prize", category = "villager")
	public String butcherHeadPrize = "0";

	@ConfigField(name = "evoker", category = "villager")
	public String evokerPrize = "10";
	@ConfigField(name = "evoker-cmd", category = "villager")
	public String evokerCmd = "mobhunt head give {player} Evoker Evoker 1 silent|give {player} Iron_ingot 1";
	@ConfigField(name = "evoker-cmd-desc", category = "villager")
	public String evokerCmdDesc = "You got a Evoker skull and an Iron ingot.";
	@ConfigField(name = "evoker-cmd-run-chance", category = "villager")
	public double evokerCmdRunChance = 0.50;
	@ConfigField(name = "evoker-head-prize", category = "villager")
	public String evokerHeadPrize = "0";

	@ConfigField(name = "farmer", category = "villager")
	public String farmerPrize = "1:2";
	@ConfigField(name = "farmer-cmd", category = "villager")
	public String farmerCmd = "mobhunt head give {player} farmer Farmer 1";
	@ConfigField(name = "farmer-cmd-desc", category = "villager")
	public String farmerCmdDesc = "You got a Farmer skull";
	@ConfigField(name = "farmer-cmd-run-chance", category = "villager")
	public double farmerCmdRunChance = 1.00;
	@ConfigField(name = "farmer-head-prize", category = "villager")
	public String farmerHeadPrize = "0";

	@ConfigField(name = "illusioner", category = "villager")
	public String illusionerPrize = "30:50";
	@ConfigField(name = "illusioner-cmd", category = "villager")
	public String illusionerCmd = "mobhunt head give {player} illusioner illusioner 1 silent|give {player} Iron_ingot 1";
	@ConfigField(name = "illusioner-cmd-desc", category = "villager")
	public String illusionerCmdDesc = "You got an Illusioner skull and an Iron ingot.";
	@ConfigField(name = "illusioner-cmd-run-chance", category = "villager")
	public double illusionerCmdRunChance = 0.10;
	@ConfigField(name = "illusioner-head-prize", category = "villager")
	public String illusionerHeadPrize = "0";

	@ConfigField(name = "librarian", category = "villager")
	public String librarianPrize = "1:2";
	@ConfigField(name = "librarian-cmd", category = "villager")
	public String librarianCmd = "mobhunt head give {player} Librarian Librarian 1";
	@ConfigField(name = "librarian-cmd-desc", category = "villager")
	public String librarianCmdDesc = "You got a Librarian skull";
	@ConfigField(name = "librarian-cmd-run-chance", category = "villager")
	public double librarianCmdRunChance = 1.00;
	@ConfigField(name = "librarian-head-prize", category = "villager")
	public String librarianHeadPrize = "0";

	@ConfigField(name = "nitwit", category = "villager")
	public String nitwitPrize = "1:2";
	@ConfigField(name = "nitwit-cmd", category = "villager")
	public String nitwitCmd = "mobhunt head give {player} nitwit Nitwit 1";
	@ConfigField(name = "nitwit-cmd-desc", category = "villager")
	public String nitwitCmdDesc = "You got a Nitwit skull";
	@ConfigField(name = "nitwit-cmd-run-chance", category = "villager")
	public double nitwitCmdRunChance = 1.00;
	@ConfigField(name = "nitwit-head-prize", category = "villager")
	public String nitwitHeadPrize = "0";

	@ConfigField(name = "priest", category = "villager")
	public String priestPrize = "1:2";
	@ConfigField(name = "priest-cmd", category = "villager")
	public String priestCmd = "mobhunt head give {player} Priest Priest 1";
	@ConfigField(name = "priest-cmd-desc", category = "villager")
	public String priestCmdDesc = "You got a Priest skull";
	@ConfigField(name = "priest-cmd-run-chance", category = "villager")
	public double priestCmdRunChance = 1.00;
	@ConfigField(name = "priest-head-prize", category = "villager")
	public String priestHeadPrize = "0";

	@ConfigField(name = "villager", category = "villager", comment = "This is the Villager in MC1.7.10")
	public String villagerPrize = "1";
	@ConfigField(name = "villager-cmd", category = "villager")
	public String villagerCmd = "mobhunt head give {player} Villager";
	@ConfigField(name = "villager-cmd-desc", category = "villager")
	public String villagerCmdDesc = "You got a Villager skull";
	@ConfigField(name = "villager-cmd-run-chance", category = "villager")
	public double villagerCmdRunChance = 0.30;
	@ConfigField(name = "villager-head-prize", category = "villager")
	public String villagerHeadPrize = "0";

	@ConfigField(name = "vindicator", category = "villager")
	public String vindicatorPrize = "10:15";
	@ConfigField(name = "vindicator-cmd", category = "villager")
	public String vindicatorCmd = "mobhunt head give {player} vindicator Vindicator 1 silent|give {player} gold_ingot 1";
	@ConfigField(name = "vindicator-cmd-desc", category = "villager")
	public String vindicatorCmdDesc = "You got a Vindicator skull and a Gold ingot.";
	@ConfigField(name = "vindicator-cmd-run-chance", category = "villager")
	public double vindicatorCmdRunChance = 0.05;
	@ConfigField(name = "vindicator-head-prize", category = "villager")
	public String vindicatorHeadPrize = "0";

	@ConfigField(name = "zombie-villager", category = "villager")
	public String zombieVillagerPrize = "1:2";
	@ConfigField(name = "zombie-villager-cmd", category = "villager")
	public String zombieVillagerCmd = "mobhunt head give {player} ZombieVillager ZombieVillager 1 silent";
	@ConfigField(name = "zombie-villager-cmd-desc", category = "villager")
	public String zombieVillagerCmdDesc = "You got a ZombieVillager skull";
	@ConfigField(name = "zombie-villager-cmd-run-chance", category = "villager")
	public double zombieVillagerCmdRunChance = 0.05;
	@ConfigField(name = "zombie-head-prize", category = "villager")
	public String zombieVillagerHeadPrize = "0";

	// #####################################################################################
	// Passive Mobs
	// #####################################################################################
	@ConfigField(name = "bat", category = "passive")
	public String batPrize = "0";
	@ConfigField(name = "bat-cmd", category = "passive")
	public String batCmd = "mobhunt head give {player} Bat Bat 1 silent";
	@ConfigField(name = "bat-cmd-desc", category = "passive")
	public String batCmdDesc = "You got a Bat skull";
	@ConfigField(name = "bat-cmd-run-chance", category = "passive")
	public double batCmdRunChance = 0.05;
	@ConfigField(name = "bat-head-prize", category = "passive")
	public String batHeadPrize = "0";

	@ConfigField(name = "chicken", category = "passive")
	public String chickenPrize = "0";
	@ConfigField(name = "chicken-cmd", category = "passive")
	public String chickenCmd = "mobhunt head give {player} Chicken Chicken 1 silent";
	@ConfigField(name = "chicken-cmd-desc", category = "passive")
	public String chickenCmdDesc = "You got a Chicken skull";
	@ConfigField(name = "chicken-cmd-run-chance", category = "passive")
	public double chickenCmdRunChance = 0.05;
	@ConfigField(name = "chicken-head-prize", category = "passive")
	public String chickenHeadPrize = "0";

	@ConfigField(name = "cow", category = "passive")
	public String cowPrize = "5";
	@ConfigField(name = "cow-cmd", category = "passive")
	public String cowCmd = "mobhunt head give {player} Cow Cow 1 silent";
	@ConfigField(name = "cow-cmd-desc", category = "passive")
	public String cowCmdDesc = "You got a Cow skull";
	@ConfigField(name = "cow-cmd-run-chance", category = "passive")
	public double cowCmdRunChance = 0.05;
	@ConfigField(name = "cow-head-prize", category = "passive")
	public String cowHeadPrize = "0";

	@ConfigField(name = "donkey", category = "passive")
	public String donkeyPrize = "5";
	@ConfigField(name = "donkey-cmd", category = "passive")
	public String donkeyCmd = "mobhunt head give {player} donkey Donkey 1 silent";
	@ConfigField(name = "donkey-cmd-desc", category = "passive")
	public String donkeyCmdDesc = "You got a Donkey skull";
	@ConfigField(name = "donkey-cmd-run-chance", category = "passive")
	public double donkeyCmdRunChance = 0.05;
	@ConfigField(name = "donkey-head-prize", category = "passive")
	public String donkeyHeadPrize = "0";

	@ConfigField(name = "horse", category = "passive")
	public String horsePrize = "0";
	@ConfigField(name = "horse-cmd", category = "passive")
	public String horseCmd = "mobhunt head give {player} Horse Horse 1 silent";
	@ConfigField(name = "horse-cmd-desc", category = "passive")
	public String horseCmdDesc = "You got a Horse skull";
	@ConfigField(name = "horse-cmd-run-chance", category = "passive")
	public double horseCmdRunChance = 0.05;
	@ConfigField(name = "horse-head-prize", category = "passive")
	public String horseHeadPrize = "0";

	@ConfigField(name = "llama", category = "passive")
	public String llamaPrize = "0";
	@ConfigField(name = "llama-cmd", category = "passive")
	public String llamaCmd = "mobhunt head give {player} llama Llama 1";
	@ConfigField(name = "llama-cmd-desc", category = "passive")
	public String llamaCmdDesc = "You got a Llama skull";
	@ConfigField(name = "llama-cmd-run-chance", category = "passive")
	public double llamaCmdRunChance = 0.05;
	@ConfigField(name = "llama-head-prize", category = "passive")
	public String llamaHeadPrize = "0";

	@ConfigField(name = "mule", category = "passive")
	public String mulePrize = "0";
	@ConfigField(name = "mule-cmd", category = "passive")
	public String muleCmd = "mobhunt head give {player} mule Mule 1 silent";
	@ConfigField(name = "mule-cmd-desc", category = "passive")
	public String muleCmdDesc = "You got a Mule skull";
	@ConfigField(name = "mule-cmd-run-chance", category = "passive")
	public double muleCmdRunChance = 0.05;
	@ConfigField(name = "mule-head-prize", category = "passive")
	public String muleHeadPrize = "0";

	@ConfigField(name = "mushroom-cow", category = "passive")
	public String mushroomCowPrize = "0";
	@ConfigField(name = "mushroom-cow-cmd", category = "passive")
	public String mushroomCowCmd = "mobhunt head give {player} Mushroom_Cow Mushroom_Cow 1 silent";
	@ConfigField(name = "mushroom-cow-cmd-desc", category = "passive")
	public String mushroomCowCmdDesc = "You got a Mushroom Cow skull";
	@ConfigField(name = "mushroom-cow-cmd-run-chance", category = "passive")
	public double mushroomCowCmdRunChance = 0.05;
	@ConfigField(name = "mushroom-cow-head-prize", category = "passive")
	public String mushroomCowHeadPrize = "0";

	@ConfigField(name = "ocelot", category = "passive")
	public String ocelotPrize = "0";
	@ConfigField(name = "ocelot-cmd", category = "passive")
	public String ocelotCmd = "mobhunt head give {player} Ocelot Ocelot 1 silent";
	@ConfigField(name = "ocelot-cmd-desc", category = "passive")
	public String ocelotCmdDesc = "You got an Ocelot skull";
	@ConfigField(name = "ocelot-cmd-run-chance", category = "passive")
	public double ocelotCmdRunChance = 0.05;
	@ConfigField(name = "ocelot-head-prize", category = "passive")
	public String ocelotHeadPrize = "0";

	@ConfigField(name = "parrot", category = "mobs")
	public String parrotPrize = "2";
	@ConfigField(name = "parrot-cmd", category = "mobs")
	public String parrotCmd = "mobhunt head give {player} Parrot Parrot 1 silent";
	@ConfigField(name = "parrot-cmd-desc", category = "mobs")
	public String parrotCmdDesc = "You got a Parrot skull.";
	@ConfigField(name = "parrot-cmd-run-chance", category = "mobs")
	public double parrotCmdRunChance = 0.10;
	@ConfigField(name = "parrot-head-prize", category = "mobs")
	public String parrotHeadPrize = "0";

	@ConfigField(name = "pig", category = "passive")
	public String pigPrize = "0";
	@ConfigField(name = "pig-cmd", category = "passive")
	public String pigCmd = "mobhunt head give {player} Pig Pig 1 silent";
	@ConfigField(name = "pig-cmd-desc", category = "passive")
	public String pigCmdDesc = "You got a Pig skull";
	@ConfigField(name = "pig-cmd-run-chance", category = "passive")
	public double pigCmdRunChance = 0.05;
	@ConfigField(name = "pig-head-prize", category = "passive")
	public String pigHeadPrize = "0";

	@ConfigField(name = "rabbit", category = "passive")
	public String rabbitPrize = "0";
	@ConfigField(name = "rabbit-cmd", category = "passive")
	public String rabbitCmd = "mobhunt head give {player} Rabbit Rabbit 1 silent";
	@ConfigField(name = "rabbit-cmd-desc", category = "passive")
	public String rabbitCmdDesc = "You got a Rabbit skull";
	@ConfigField(name = "rabbit-cmd-run-chance", category = "passive")
	public double rabbitCmdRunChance = 0.05;
	@ConfigField(name = "rabbit-head-prize", category = "passive")
	public String rabbitHeadPrize = "0";

	@ConfigField(name = "sheep", category = "passive")
	public String sheepPrize = "0";
	@ConfigField(name = "sheep-cmd", category = "passive")
	public String sheepCmd = "mobhunt head give {player} Sheep Sheep 1 silent";
	@ConfigField(name = "sheep-cmd-desc", category = "passive")
	public String sheepCmdDesc = "You got a Sheep skull";
	@ConfigField(name = "sheep-cmd-run-chance", category = "passive")
	public double sheepCmdRunChance = 0.05;
	@ConfigField(name = "sheep-head-prize", category = "passive")
	public String sheepHeadPrize = "0";

	@ConfigField(name = "skeletonhorse", category = "passive")
	public String skeletonhorsePrize = "-10";
	@ConfigField(name = "skeletonhorse-cmd", category = "passive")
	public String skeletonhorseCmd = "mobhunt head give {player} skeletonhorse SkeletonHorse 1 silent";
	@ConfigField(name = "skeletonhorse-cmd-desc", category = "passive")
	public String skeletonhorseCmdDesc = "You got a SkeletonHorse skull";
	@ConfigField(name = "skeletonhorse-cmd-run-chance", category = "passive")
	public double skeletonhorseCmdRunChance = 0.05;
	@ConfigField(name = "skeletonhorse-head-prize", category = "passive")
	public String skeletonHorseHeadPrize = "0";

	@ConfigField(name = "snowman", category = "passive")
	public String snowmanPrize = "0";
	@ConfigField(name = "snowman-cmd", category = "passive")
	public String snowmanCmd = "mobhunt head give {player} SnowMan Snowman 1 silent";
	@ConfigField(name = "snowman-cmd-desc", category = "passive")
	public String snowmanCmdDesc = "You got a Snowman skull";
	@ConfigField(name = "snowman-cmd-run-chance", category = "passive")
	public double snowmanCmdRunChance = 0.05;
	@ConfigField(name = "snowman-head-prize", category = "passive")
	public String snowmanHeadPrize = "0";

	@ConfigField(name = "squid", category = "passive")
	public String squidPrize = "0";
	@ConfigField(name = "squid-cmd", category = "passive")
	public String squidCmd = "mobhunt head give {player} Squid Squid 1 silent";
	@ConfigField(name = "squid-cmd-desc", category = "passive")
	public String squidCmdDesc = "You got a Squid skull";
	@ConfigField(name = "squid-cmd-run-chance", category = "passive")
	public double squidCmdRunChance = 0.05;
	@ConfigField(name = "squid-head-prize", category = "passive")
	public String squidHeadPrize = "0";

	@ConfigField(name = "wolf", category = "passive")
	public String wolfPrize = "-10";
	@ConfigField(name = "wolf-cmd", category = "passive")
	public String wolfCmd = "mobhunt head give {player} Wolf Wolf 1 silent";
	@ConfigField(name = "wolf-cmd-desc", category = "passive")
	public String wolfCmdDesc = "You got a Wolf skull";
	@ConfigField(name = "wolf-cmd-run-chance", category = "passive")
	public double wolfCmdRunChance = 0.05;
	@ConfigField(name = "wolf-head-prize", category = "passive")
	public String wolfHeadPrize = "0";

	@ConfigField(name = "zombiehorse", category = "passive")
	public String zombiehorsePrize = "-10";
	@ConfigField(name = "zombiehorse-cmd", category = "passive")
	public String zombiehorseCmd = "mobhunt head give {player} zombiehorse ZombieHorse 1 silent";
	@ConfigField(name = "zombiehorse-cmd-desc", category = "passive")
	public String zombiehorseCmdDesc = "You got a ZombieHorse skull";
	@ConfigField(name = "zombiehorse-cmd-run-chance", category = "passive")
	public double zombiehorseCmdRunChance = 0.25;
	@ConfigField(name = "zombiehorse-head-prize", category = "passive")
	public String zombiehorseHeadPrize = "0";

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
	public String rawFishCmdDesc = "You got the head of a Raw Fish";
	@ConfigField(name = "rawfish-cmd-run-chance", category = "fishing")
	public double rawFishCmdRunChance = 0.05;
	@ConfigField(name = "rawfish-head-prize", category = "fishing")
	public String rawFishHeadPrize = "0";

	@ConfigField(name = "rawsalmon", category = "fishing")
	public String rawSalmonPrize = "2:8";
	@ConfigField(name = "rawsalmon-cmd", category = "fishing")
	public String rawSalmonCmd = "mobhunt head give {player} rawsalmon Raw_Salmon 1 silent";
	@ConfigField(name = "rawsalmon-cmd-desc", category = "fishing")
	public String rawSalmonCmdDesc = "You got the head of a Raw Salmon";
	@ConfigField(name = "rawsalmon-cmd-run-chance", category = "fishing")
	public double rawSalmonCmdRunChance = 0.1;
	@ConfigField(name = "rawSalmon-head-prize", category = "fishing")
	public String rawSalmonHeadPrize = "0";

	@ConfigField(name = "clownfish", category = "fishing")
	public String clownfishPrize = "20:40";
	@ConfigField(name = "clownfish-cmd", category = "fishing")
	public String clownfishCmd = "mobhunt head give {player} clownfish Clowfish 1 silent";
	@ConfigField(name = "clownfish-cmd-desc", category = "fishing")
	public String clownfishCmdDesc = "You got the head of a Clownfish";
	@ConfigField(name = "clownfish-cmd-run-chance", category = "fishing")
	public double clownfishCmdRunChance = 0.5;
	@ConfigField(name = "clownfish-head-prize", category = "fishing")
	public String clownfishHeadPrize = "0";

	@ConfigField(name = "pufferfish", category = "fishing")
	public String pufferfishPrize = "5:15";
	@ConfigField(name = "pufferfish-cmd", category = "fishing")
	public String pufferfishCmd = "mobhunt head give {player} pufferfish Pufferfish 1 silent";
	@ConfigField(name = "pufferfish-cmd-desc", category = "fishing")
	public String pufferfishCmdDesc = "You got the head of a Pufferfish";
	@ConfigField(name = "pufferfish-cmd-run-chance", category = "fishing")
	public double pufferfishCmdRunChance = 0.4;
	@ConfigField(name = "pufferfish-head-prize", category = "fishing")
	public String pufferfishHeadPrize = "0";

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
	@ConfigField(name = "bonusMob-head-prize", category = "bonus")
	public String bonusMobHeadPrize = "100";

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
	@ConfigField(name = "david-and-goliath", category = "achievements")
	public double davidAndGoliat = 1000;
	@ConfigField(name = "david-and-goliath-cmd", category = "achievements")
	public String davidAndGoliatCmd = "give {player} diamond_helmet 1";
	@ConfigField(name = "david-and-goliath-cmd-desc", category = "achievements")
	public String davidAndGoliatCmdDesc = "You got 1000 and a Diamond Helmet for the kill";
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
	public String specialItsMagicCmdDesc = "Enjoy you Gold ingot";
	@ConfigField(name = "fancypants", category = "achievements")
	public double specialFancyPants = 1000;
	@ConfigField(name = "fancypants-cmd", category = "achievements")
	public String specialFancyPantsCmd = "give {player} gold_ingot 1";
	@ConfigField(name = "fancypants-cmd-desc", category = "achievements")
	public String specialFancyPantsCmdDesc = "Enjoy you Gold ingot";
	@ConfigField(name = "master-sniper", category = "achievements")
	public double specialMasterSniper = 2000;
	@ConfigField(name = "master-sniper-cmd", category = "achievements")
	public String specialMasterSniperCmd = "give {player} gold_ingot 1";
	@ConfigField(name = "master-sniper-cmd-desc", category = "achievements")
	public String specialMasterSniperCmdDesc = "Enjoy you Gold ingot";
	@ConfigField(name = "justintime", category = "achievements")
	public double specialJustInTime = 1000;
	@ConfigField(name = "justintime-cmd", category = "achievements")
	public String specialJustInTimeCmd = "give {player} gold_ingot 1";
	@ConfigField(name = "justintime-cmd-desc", category = "achievements")
	public String specialJustInTimeCmdDesc = "Enjoy you Gold ingot";
	@ConfigField(name = "fangmaster", category = "achievements")
	public double specialFangMaster = 1000;
	@ConfigField(name = "fangmaster-cmd", category = "achievements")
	public String specialFangMasterCmd = "give {player} gold_ingot 1";
	@ConfigField(name = "fangmaster-cmd-desc", category = "achievements")
	public String specialFangMasterCmdDesc = "Enjoy your Gold ingot";
	@ConfigField(name = "hunter1", category = "achievements")
	public double specialHunter1 = 1000;
	@ConfigField(name = "hunter1-cmd", category = "achievements")
	public String specialHunter1Cmd = "give {player} gold_ingot 5";
	@ConfigField(name = "hunter1-cmd-desc", category = "achievements")
	public String specialHunter1CmdDesc = "Enjoy your 5 Gold ingots";
	@ConfigField(name = "hunter2", category = "achievements")
	public double specialHunter2 = 2500;
	@ConfigField(name = "hunter2-cmd", category = "achievements")
	public String specialHunter2Cmd = "give {player} gold_ingot 10";
	@ConfigField(name = "hunter2-cmd-desc", category = "achievements")
	public String specialHunter2CmdDesc = "Enjoy your 10 Gold ingots";
	@ConfigField(name = "hunter3", category = "achievements")
	public double specialHunter3 = 5000;
	@ConfigField(name = "hunter3-cmd", category = "achievements")
	public String specialHunter3Cmd = "give {player} gold_ingot 20";
	@ConfigField(name = "hunter3-cmd-desc", category = "achievements")
	public String specialHunter3CmdDesc = "Enjoy your 20 Gold ingots";
	@ConfigField(name = "hunter4", category = "achievements")
	public double specialHunter4 = 10000;
	@ConfigField(name = "hunter4-cmd", category = "achievements")
	public String specialHunter4Cmd = "give {player} gold_ingot 25";
	@ConfigField(name = "hunter4-cmd-desc", category = "achievements")
	public String specialHunter4CmdDesc = "Enjoy your 25 Gold ingots";
	@ConfigField(name = "hunter5", category = "achievements")
	public double specialHunter5 = 20000;
	@ConfigField(name = "hunter5-cmd", category = "achievements")
	public String specialHunter5Cmd = "give {player} gold_ingot 40";
	@ConfigField(name = "hunter5-cmd-desc", category = "achievements")
	public String specialHunter5CmdDesc = "Enjoy your 40 Gold ingots";
	@ConfigField(name = "hunter6", category = "achievements")
	public double specialHunter6 = 40000;
	@ConfigField(name = "hunter6-cmd", category = "achievements")
	public String specialHunter6Cmd = "give {player} gold_ingot 50";
	@ConfigField(name = "hunter6-cmd-desc", category = "achievements")
	public String specialHunter6CmdDesc = "Enjoy your 50 Gold ingots";
	@ConfigField(name = "hunter7", category = "achievements")
	public double specialHunter7 = 80000;
	@ConfigField(name = "hunter7-cmd", category = "achievements")
	public String specialHunter7Cmd = "give {player} gold_ingot 60";
	@ConfigField(name = "hunter7-cmd-desc", category = "achievements")
	public String specialHunter7CmdDesc = "Enjoy your 60 Gold ingots";
	@ConfigField(name = "hunter8", category = "achievements")
	public double specialHunter8 = 160000;
	@ConfigField(name = "hunter8-cmd", category = "achievements")
	public String specialHunter8Cmd = "give {player} gold_ingot 120";
	@ConfigField(name = "hunter8-cmd-desc", category = "achievements")
	public String specialHunter8CmdDesc = "Enjoy your 120 Gold ingots";

	// #####################################################################################
	// Achievement Hunter Levels
	// #####################################################################################
	@ConfigField(name = "bat_level1", category = "achievement_levels")
	public int batLevel1 = 100;

	@ConfigField(name = "blaze_level1", category = "achievement_levels")
	public int blazeLevel1 = 80;

	@ConfigField(name = "blacksmith_level1", category = "achievement_levels")
	public int blacksmithLevel1 = 100;

	@ConfigField(name = "bonusmob_level1", category = "achievement_levels")
	public int bonusMobLevel1 = 20;

	@ConfigField(name = "butcher_level1", category = "achievement_levels")
	public int butcherLevel1 = 100;
	@ConfigField(name = "cartographer_level1", category = "achievement_levels")
	public int cartographerLevel1 = 100;

	@ConfigField(name = "cave-spider_level1", category = "achievement_levels")
	public int caveSpiderLevel1 = 100;

	@ConfigField(name = "chicken_level1", category = "achievement_levels")
	public int chickenLevel1 = 100;

	@ConfigField(name = "clownfish_level1", category = "achievement_levels")
	public int clownfishLevel1 = 100;

	@ConfigField(name = "cow_level1", category = "achievement_levels")
	public int cowLevel1 = 100;

	@ConfigField(name = "creeper_level1", category = "achievement_levels")
	public int creeperLevel1 = 100;

	@ConfigField(name = "donkey_level1", category = "achievement_levels")
	public int donkeyLevel1 = 100;

	@ConfigField(name = "elder_guardian_level1", category = "achievement_levels")
	public int elderGuardianLevel1 = 50;

	@ConfigField(name = "enderdragon_level1", category = "achievement_levels")
	public int enderdragonLevel1 = 20;

	@ConfigField(name = "enderman_level1", category = "achievement_levels")
	public int endermanLevel1 = 100;

	@ConfigField(name = "endermite_level1", category = "achievement_levels")
	public int endermiteLevel1 = 100;

	@ConfigField(name = "evoker_level1", category = "achievement_levels")
	public int evokerLevel1 = 50;

	@ConfigField(name = "farmer_level1", category = "achievement_levels")
	public int farmerLevel1 = 100;

	@ConfigField(name = "ghast_level1", category = "achievement_levels")
	public int ghastLevel1 = 80;

	@ConfigField(name = "giant_level1", category = "achievement_levels")
	public int giantLevel1 = 100;

	@ConfigField(name = "guardian_level1", category = "achievement_levels")
	public int guardianLevel1 = 100;

	@ConfigField(name = "horse_level1", category = "achievement_levels")
	public int horseLevel1 = 100;

	@ConfigField(name = "husk_level1", category = "achievement_levels")
	public int huskLevel1 = 100;

	@ConfigField(name = "illusioner_level1", category = "achievement_levels")
	public int illusionerLevel1 = 100;

	@ConfigField(name = "iron-golem_level1", category = "achievement_levels")
	public int ironGolemLevel1 = 100;

	@ConfigField(name = "killerrabbit_level1", category = "achievement_levels")
	public int killerRabbitLevel1 = 100;

	@ConfigField(name = "librarian_level1", category = "achievement_levels")
	public int librarianLevel1 = 100;

	@ConfigField(name = "llama_level1", category = "achievement_levels")
	public int llamaLevel1 = 100;

	@ConfigField(name = "magma-cube_level1", category = "achievement_levels")
	public int magmaCubeLevel1 = 100;

	@ConfigField(name = "mule_level1", category = "achievement_levels")
	public int muleLevel1 = 100;

	@ConfigField(name = "mushroom-cow_level1", category = "achievement_levels")
	public int mushroomCowLevel1 = 100;

	@ConfigField(name = "nitwit_level1", category = "achievement_levels")
	public int nitwitLevel1 = 100;

	@ConfigField(name = "ocelot_level1", category = "achievement_levels")
	public int ocelotLevel1 = 100;

	@ConfigField(name = "parrot_level1", category = "achievement_levels")
	public int parrotLevel1 = 100;

	@ConfigField(name = "pig_level1", category = "achievement_levels")
	public int pigLevel1 = 100;

	@ConfigField(name = "polar_bear_level1", category = "achievement_levels")
	public int polarBearLevel1 = 100;

	@ConfigField(name = "priest_level1", category = "achievement_levels")
	public int priestLevel1 = 100;

	@ConfigField(name = "pvpplayer_level1", category = "achievement_levels")
	public int pvpPlayerLevel1 = 100;

	@ConfigField(name = "pufferfish_level1", category = "achievement_levels")
	public int pufferfishLevel1 = 100;

	@ConfigField(name = "rabbit_level1", category = "achievement_levels")
	public int rabbitLevel1 = 100;

	@ConfigField(name = "rawfish_level1", category = "achievement_levels")
	public int rawfishLevel1 = 100;

	@ConfigField(name = "rawsalmon_level1", category = "achievement_levels")
	public int rawsalmonLevel1 = 100;

	@ConfigField(name = "sheep_level1", category = "achievement_levels")
	public int sheepLevel1 = 100;

	@ConfigField(name = "shulker_level1", category = "achievement_levels")
	public int shulkerLevel1 = 100;

	@ConfigField(name = "silverfish_level1", category = "achievement_levels")
	public int silverfishLevel1 = 100;

	@ConfigField(name = "skeleton_level1", category = "achievement_levels")
	public int skeletonLevel1 = 100;

	@ConfigField(name = "skeletonhorse_level1", category = "achievement_levels")
	public int skeletonHorseLevel1 = 100;

	@ConfigField(name = "slime-base_level1", category = "achievement_levels")
	public int slimeLevel1 = 100;

	@ConfigField(name = "snowman_level1", category = "achievement_levels")
	public int snowmanLevel1 = 100;

	@ConfigField(name = "spider_level1", category = "achievement_levels")
	public int spiderLevel1 = 100;

	@ConfigField(name = "squid_level1", category = "achievement_levels")
	public int squidLevel1 = 100;

	@ConfigField(name = "stray_level1", category = "achievement_levels")
	public int strayLevel1 = 100;

	@ConfigField(name = "vex_level1", category = "achievement_levels")
	public int vexLevel1 = 100;

	@ConfigField(name = "villager_level1", category = "achievement_levels")
	public int villagerLevel1 = 100;

	@ConfigField(name = "vindicator_level1", category = "achievement_levels")
	public int vindicatorLevel1 = 100;

	@ConfigField(name = "witch_level1", category = "achievement_levels")
	public int witchLevel1 = 80;

	@ConfigField(name = "wither_level1", category = "achievement_levels")
	public int witherLevel1 = 20;

	@ConfigField(name = "wither-skeleton_level1", category = "achievement_levels")
	public int witherSkeletonLevel1 = 80;

	@ConfigField(name = "wolf_level1", category = "achievement_levels")
	public int wolfLevel1 = 100;

	@ConfigField(name = "zombie_level1", category = "achievement_levels")
	public int zombieLevel1 = 100;

	@ConfigField(name = "zombiehorse_level1", category = "achievement_levels")
	public int zombieHorseLevel1 = 100;

	@ConfigField(name = "zombie-pigman_level1", category = "achievement_levels")
	public int zombiePigmanLevel1 = 100;

	@ConfigField(name = "zombie_villager_level1", category = "achievement_levels")
	public int zombieVillagerLevel1 = 100;

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
	@ConfigField(name = "enable-grinding-detection", category = "grinding", comment = "Grinding detection."
			+ "\nEnabling this prevents a player from earning too much money from using a mob grinder."
			+ "\nSet 'enable-grinding-detection: false' to disable the grinding detection."
			+ "\nOBS: You can whitelist an area to allow grinding using '/mobhunt whitelistarea <add|remove>'"
			+ "\nif the area is detected as a grinding area. See also '/mobhunt checkgrinding'"
			+ "\nFor each kill MobHunting check the number of kills within the range"
			+ "\nIf number of kills exceeds 10, the reward will decrese with 10% until the 'number of deaths'"
			+ "\nis reached, whereafter the reward will be zero.")
	public boolean grindingDetectionEnabled = true;
	@ConfigField(name = "grinding-detection-range", category = "grinding")
	public int grindingDetectionRange = 15;
	@ConfigField(name = "grinding-detection-number-of-death", category = "grinding")
	public int grindingDetectionNumberOfDeath = 20;
	@ConfigField(name = "disable-natural-item-drops-on-player-grinding", category = "grinding")
	public boolean disableNaturalItemDropsOnPlayerGrinding = false;
	@ConfigField(name = "disable-natural-xp-drops-on-player-grinding", category = "grinding")
	public boolean disableNaturalXPDropsOnPlayerGrinding = false;
	@ConfigField(name = "blacklist-player-grinding-spots-as-server-worldwide-spots", category = "grinding")
	public boolean blacklistPlayerGrindingSpotsServerWorldWide = false;
	@ConfigField(name = "grinding-stacked-mobs-allowed", category = "grinding", comment = "Killing stacked mobs (created by a mob stacking plugin) "
			+ "\nis by nature detected as grinding and by default allowed. If you want to the the grinding detection to detect"
			+ "\nkillings of stacked to be detected as gring, you must set grinding-stacked-mobs-allowed to false.")
	public boolean isGrindingStackedMobsAllowed = true;

	// Farm detection
	@ConfigField(name = "detect-farms", category = "grinding", comment = "Detect Grinding Farms."
			+ "\nWhen this is true, the plugin will try to detect if the players has build a Mob Grinding Farm."
			+ "\nFarm detection can be completly disabled or you can whitelist an area using the whitelist"
			+ "\ncommand if you want the players to harvest mobs from a farm.")
	public boolean detectFarms = true;
	// NetherGoldXPFarm
	@ConfigField(name = "detect-nether-gold-farms", category = "grinding", comment = "Nether Gold Farm detection."
			+ "\nWhen this is true, the plugin will try to detect if the players has build a Nether Gold Farm."
			+ "\nThere is no guarantie that the plugin can detect all types of Nether Gold farms, but it has"
			+ "\nbeen testet on this one: https://www.youtube.com/watch?v=jQWG9Q7HoUA"
			+ "\nWhen searching for grinding the plugin measures how many mobs dies per timeframe within a range."
			+ "\nBe careful if you change this number there is a risk for false positives.")
	public boolean detectNetherGoldFarms = true;
	@ConfigField(name = "disable-grinding-detection-in-worlds", category = "grinding", comment = "Put the names of the worlds here where you want to disable grinding detection"
			+ "\nYou would typically do this in creative worlds.")
	public String[] disableGrindingDetectionInWorlds = { "worldname" };
	@ConfigField(name = "seconds-to-search-for-grinding-on-nether-gold-farms", category = "grinding")
	public int secondsToSearchForGrinding = 30;
	@ConfigField(name = "range-to-search-for-grinding-on-nether-gold-farms", category = "grinding")
	public double rangeToSearchForGrinding = 4;
	@ConfigField(name = "number-of-deaths-when-searching-for-grinding-on-nether-gold-farms", category = "grinding")
	public int numberOfDeathsWhenSearchingForGringding = 5;
	@ConfigField(name = "disable-natural-item-drops-on-nether-gold-farms", category = "grinding")
	public boolean disableNaturalItemDropsOnNetherGoldFarms = false;
	@ConfigField(name = "disable-natural-xp-drops-on-nether-gold-farms", category = "grinding")
	public boolean disableNaturalXPDropsOnNetherGoldFarms = false;

	@ConfigField(name = "detect-other-farms", category = "grinding", comment = "Other Farm detection."
			+ "\nWhen this is true, the plugin will try to detect if the players has build other Farms"
			+ "\nwhere different mobs is falling into death. The plugin is still counting mobs which"
			+ "\ndies from falling, with in a range and a time frame.")
	public boolean detectOtherFarms = true;
	@ConfigField(name = "seconds-to-search-for-grinding-on-other-farms", category = "grinding")
	public int secondsToSearchForGrindingOnOtherFarms = 30;
	@ConfigField(name = "range-to-search-for-grinding-on-other-farms", category = "grinding")
	public double rangeToSearchForGrindingOnOtherFarms = 4;
	@ConfigField(name = "number-of-deaths-when-searching-for-grinding-on-other-farms", category = "grinding")
	public int numberOfDeathsWhenSearchingForGringdingOnOtherFarms = 10;
	@ConfigField(name = "disable-natural-item-drops-on-other-farms", category = "grinding")
	public boolean disableNaturalItemDropsOnOtherFarms = false;
	@ConfigField(name = "disable-natural-xp-drops-on-other-farms", category = "grinding")
	public boolean disableNaturalXPDropsOnOtherFarms = false;

	// #####################################################################################
	// Penalties
	// #####################################################################################
	@ConfigField(name = "flyingPenalty", category = "penalty", comment = "If a player flies at any point in a fight, this penalty will be applied")
	public double penaltyFlying = 0.2;

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
	@ConfigField(name = "world-difficulty-multiplier", category = "multiplier", comment = "This is the reward multiplier for the WorldDifficulty. Note that extrahard is "
			+ "\nused for worlds where the plugin ExtraHardMode is enabled.")
	public HashMap<String, String> difficultyMultiplier = new HashMap<String, String>();
	{
		difficultyMultiplier.put("difficulty.multiplier.peaceful", "0.5");
		difficultyMultiplier.put("difficulty.multiplier.easy", "0.75");
		difficultyMultiplier.put("difficulty.multiplier.normal", "1");
		difficultyMultiplier.put("difficulty.multiplier.hard", "2");
		difficultyMultiplier.put("difficulty.multiplier.extrahard", "2.5");
	}

	// #####################################################################################
	// PVP
	// #####################################################################################
	@ConfigField(name = "pvp-allowed", category = "pvp", comment = "Set pvpAllowed=false to disable rewards on killing other players.")
	public boolean pvpAllowed = true;
	@ConfigField(name = "rob-from-victim", category = "pvp", comment = "Set rob-from-victim=true to steal from the victim or "
			+ "\nrob-from-victim=false to get the reward money from the server.")
	public boolean robFromVictim = true;
	@ConfigField(name = "pvp-kill-prize", category = "pvp", comment = "The kill prize can be a number to steal x dollars from the killed player,"
			+ "\nor it can be a cut in percent of his balance. Rob from victiom is about where the money comes from."
			+ "\nIf FALSE the money comes from from the server, if TRUE the money comes from the dead player."
			+ "\nIf you dont want the player to get any money for PVP kills, you MUST set pvp-kill-prize: 0")
	public String pvpKillPrize = "1.0%";
	@ConfigField(name = "pvp-kill-cmd", category = "pvp", comment = "One or more console commands to be run when a player kills another player.")
	public String pvpKillCmd = "mobhunt head give {player} {killed_player} {killed_player} 1 silent";
	@ConfigField(name = "pvp-kill-cmd-desc", category = "pvp", comment = "Write the message to the killer, describing the reward / console commands")
	public String pvpKillCmdDesc = "You got {killed_player}\'s skull";
	@ConfigField(name = "pvp-kill-cmd-run-chance", category = "pvp", comment = "This is the chance for running the command. 1 = 100% (each time the player is killed), 0.5 ~ 50% and 0.001 = 0.1% (very rare) ")
	public double pvpKillCmdRunChance = 0.5;
	@ConfigField(name = "pvp-head-prize", category = "pvp", comment = "The Head price if you want playerheads to have a value like the bag of gold.")
	public String pvpHeadPrize = "10";

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
			+ "\nmasterMobHunter_check_every: 300 ~ to update all MasterMobHunters every 5th minute."
			+ "\nBe careful not to lower this number too much. It can cause lag and server crashes "
			+ "\nbecause of database lockings.")
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
	// InfernalMobs Settings
	// #####################################################################################
	@ConfigField(name = "disable-integration-infernalmobs", category = "infernalmobs", comment = "Disable integration with InfernalMobs")
	public boolean disableIntegrationInfernalMobs = false;

	@ConfigField(name = "multiplier_per_level", category = "infernalmobs", comment = "For InfernalMobs mob prize is calculated by the minecraft reward x multiplier_per_level^Infernal_Level"
			+ "\nEx.If multiplier=1.2 and level is 3 normal reward will be multiplied with 1.2*1.2*1.2=1,728")
	public double multiplierPerInfernalLevel = 1.25;

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
	// Factions Settings
	// #####################################################################################
	@ConfigField(name = "disable-integration-factions", category = "factions", comment = "Disable integration with Factions."
			+ "\nhttps://www.massivecraft.com/")
	public boolean disableIntegrationFactions = false;

	@ConfigField(name = "factions_warzone_multiplier", category = "factions", comment = "This is the bonus when a player kills a mob or a player in a Factions WarZone.")
	public double factionWarZoneBonusMultiplier = 1.1;

	// #####################################################################################
	// Towny Settings
	// #####################################################################################
	@ConfigField(name = "disable-integration-towny", category = "towny", comment = "Disable integration with Towny."
			+ "\nhttp://towny.palmergames.com/")
	public boolean disableIntegrationTowny = false;

	@ConfigField(name = "disable-rewards-in-home-town", category = "towny", comment = "Disable rewards when the player is in his hometown."
			+ "\nhttp://towny.palmergames.com/")
	public boolean disableRewardsInHomeTown = true;

	@ConfigField(name = "disable-naturally-drops-and-xp-in-home-town", category = "towny", comment = "Disable naturally drops and xp drops when the player kill mobs in his home town.")
	public boolean disableNaturallyRewardsInHomeTown = false;

	// #####################################################################################
	// Residence Settings
	// #####################################################################################
	@ConfigField(name = "disable-integration-residence", category = "residence", comment = "Disable integration with Residence."
			+ "\nhttp://towny.palmergames.com/")
	public boolean disableIntegrationResidence = false;

	@ConfigField(name = "disable-rewards-in-home-town", category = "residence", comment = "Disable rewards when the player is protected against damage."
			+ "\nhttp://towny.palmergames.com/")
	public boolean disableRewardsInHomeResidence = true;

	@ConfigField(name = "disable-naturally-drops-and-xp-in-protected-residence", category = "residence", comment = "Disable naturally drops and xp drops when the player kill mobs in his home town.")
	public boolean disableNaturallyRewardsInProtectedResidence = false;

	// #####################################################################################
	// DropMoneyOnGround settings
	// #####################################################################################
	@ConfigField(name = "drop-money-on-ground", category = "dropmoneyonground", comment = "When a player get a money reward for a kill, the money will go directly"
			+ "\ninto his pocket. If you set dropMoneyOnGround=true the reward will "
			+ "\ndropped on ground to be picked up by the player."
			+ "\nNegative rewards will always be taken from the player. ")
	public boolean dropMoneyOnGroup = true;

	@ConfigField(name = "drop-money-on-ground-itemtype", category = "dropmoneyonground", comment = "Here you can set the type of the ITEM to be dropped."
			+ "\nYou can choose between \"ITEM\",\"KILLED\",\"SKULL\",\"KILLER\". The default is ITEM."
			+ "\nThe value will be showed above the item." + "\nITEM: The reward is dropped as a normal Minecraft item."
			+ "\nKILLED: The reward is dropped as the head of the mob/player you killed."
			+ "\nSKULL: The reward is dropped as a SKULL with a custom texture. You can generate custom texture value"
			+ "\nand custom texture signature at http://mineskin.org"
			+ "\nKILLER: The reward is dropped as the killers head."
			+ "\n\nOBS: If the Gringotts plugin is installed and support not disabled, the droped item will be the Gringotts chosen item."
			+ "\nExamples:" + "\n\nBag of gold: (https://mineskin.org/6875)"
			+ "\n\ndrop-money-on-ground-skull-reward-name: 'Bag of gold'"
			+ "\ndrop-money-on-ground-skull-texture-value: 'eyJ0aW1lc3RhbXAiOjE0ODU5MTIwNjk3OTgsInByb2ZpbGVJZCI6IjdkYTJhYjNhOTNjYTQ4ZWU4MzA0OGFmYzNiODBlNjhlIiwicHJvZmlsZU5hbWUiOiJHb2xkYXBmZWwiLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzM5NmNlMTNmZjYxNTVmZGYzMjM1ZDhkMjIxNzRjNWRlNGJmNTUxMmYxYWRlZGExYWZhM2ZjMjgxODBmM2Y3In19fQ=='"
			+ "\ndrop-money-on-ground-skull-texture-signature: 'm8u2ChI43ySVica7pcY0CsCuMCGgAdN7c9f/ZOxDZsPzJY8eiDrwxLIh6oPY1rvE1ja/rmftPSmdnbeHYrzLQ18QBzehFp8ZVegPsd9iNHc4FuD7nr1is2FD8M8AWAZOViiwlUKnfd8avb3SKfvFmhmVhQtE+atJYQrXhJwiqR4S+KTccA6pjIESM3AWlbCOmykg31ey7MQWB4YgtRp8NyFD3HNTLZ8alcEXBuG3t58wYBEME1UaOFah45tHuV1FW+iGBHHFWLu1UsAbg0Uw87Pp+KSTUGrhdwSc/55czILulI8IUnUfxmkaThRjd7g6VpH/w+9jLvm+7tOwfMQZlXp9104t9XMVnTAchzQr6mB3U6drCsGnuZycQzEgretQsUh3hweN7Jzz5knl6qc1n3Sn8t1yOvaIQLWG1f3l6irPdl28bwEd4Z7VDrGqYgXsd2GsOK/gCQ7rChNqbJ2p+jCja3F3ZohfmTYOU8W7DJ8Ne+xaofSuPnWODnZN9x+Y+3RE3nzH9tzP+NBMsV3YQXpvUD7Pepg7ScO+k9Fj3/F+KfBje0k6xfl+75s7kR3pNWQI5EVrO6iuky6dMuFPUBfNfq33fZV6Tqr/7o24aKpfA4WwJf91G9mC18z8NCgFR6iK4cPGmkTMvNtxUQ3MoB0LCOkRcbP0i7qxHupt8xE='"
			+ "\n\nBag of gold (alternative): (https://mineskin.org/3384)"
			+ "\n\ndrop-money-on-ground-skull-reward-name: 'Bag of gold'"
			+ "\ndrop-money-on-ground-skull-texture-value: 'eyJ0aW1lc3RhbXAiOjE0NzQzMzI0MzY1MDYsInByb2ZpbGVJZCI6IjNlMjZiMDk3MWFjZDRjNmQ5MzVjNmFkYjE1YjYyMDNhIiwicHJvZmlsZU5hbWUiOiJOYWhlbGUiLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzg2NzczZDc0Y2Y1MDhmZDc3Yzc4MmZmZDI5ZGYyZmU0N2ZiNzE0YjViMGQ3ZGU2N2Q1Mjg2OTMxZTJmMWRmMiJ9fX0='"
			+ "\ndrop-money-on-ground-skull-texture-signature: 'JdvJksowuxYQ0eqf56J+Dmczg7zvlw2DbIc58Q33kRt65uMUNn2iRCQsbNpztC1cAAgyYMOyFDiOUZQeIK03CSRoPLDtWp2u501YoGKqhjgrE0V0UDh3JetWKz4Ob0KmATtY+4R2vSoMjHFEFppM0Oq+8ZER12FAiVEMAzeseFN3Z9fWAMc/V10LoquGBpq6ExTfSCEEMDEGZopF1T8ZBKL0vf4DVendfz4v3yl7bRBzISZEAnF+ECTa9z36r8HRqS8+s0eO/AWYQcRaKIu9H+wSK5F/1v+rgifeSlMAnt1Na8m1b5tMfNuq6pXxWCq4nUGgYVTOLUinqs9ZcFz3Z6Mtx5YtymKk2M0mzxmTm9+AeOL4s3K/UrJYQlcmLBJSv4hd6EigJXoashzWNCHKmFDYCdEhh4FArq4G9vRZtoudcTeMsvi0VmXIgER8U5iSfoTtzXcGbf/GT0ECtgfeA40f5oCqyE4nXreudMmvlDCBr/KHbILQWeeH/jhtYqQ6OwJb3Ji2Bs9F5fQmICSqk7X4yKzexf8rdDhOG1z+/TCot7K8unPVuQx46sXPeP7t2hCiHOXMAnOMt8vuL3gQUURIEM6fMryjmlKsgvk8Jo0gawavRCIZQtA6vT0JRRnSAchzEOA7QP1iiVV3LnwX9Yqw7oMJ/+REV1hWesuzDOc='"
			+ "\n\nChest: (https://mineskin.org/3136)" + "\n\ndrop-money-on-ground-skull-reward-name: 'Treasure chest'"
			+ "\ndrop-money-on-ground-skull-texture-value: 'eyJ0aW1lc3RhbXAiOjE0NzI4Mzk3Nzk2ODMsInByb2ZpbGVJZCI6ImIwZDRiMjhiYzFkNzQ4ODlhZjBlODY2MWNlZTk2YWFiIiwicHJvZmlsZU5hbWUiOiJJbnZlbnRpdmVHYW1lcyIsInNpZ25hdHVyZVJlcXVpcmVkIjp0cnVlLCJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTY5NDcxMjQ1YmNhN2M0ZmUwNjQ0MGQ5YjRiOWY3NDIxN2VkNzM0M2FhZDU5YTc5MThiMWExZDYxZDhiYTZkYSJ9fX0='"
			+ "\ndrop-money-on-ground-skull-texture-signature: 'lVA2QIbvybpzhcXof5yWz/7nkHdhG/3MGO+1DyD1txdRCALV6BRwsDUBwIUg06MkLUpBkjmiOvFcCRgal/jDE/xkkJPyk2tb/w4NtQ5PiPiAe0oInVnuiSIVFIE4tnsCdvX0joll3uKwVu6XY3t1KEsqJATcPhA5hslVn1iOp/IfMziIfuCzzob04rScpwcw0mLNtbtbMVAl6LYR9gXVuOkAfXujuYq4lbI/iW0yuLxSAzr8i9QWBP2ftup4qQHwocQRTdUE6/G5G9LwJWXhhnqKWjgjfvL0y2FRFJkgN1cvuq7DvUDBVsePnRIHwU5YvBPMjcZe/KE8VPTSodsN84/+++5p95Puxe1DXMX822xR71IQsxM7eax7Ffrr/Tzxw2rSDh9ivGGlRAB85OHwp/ouUgWNSrT8inNMYImque9EuZku9p3OFet8iZsFhkMXANeNtTVL7LKV7/L/0YWwoeyBnw5QQqvGyWKw3dac5eDkRNCyCtdDIntM5vsd8FxnIFj36zxLWgmrJmOM9hg5PBM4gcDxxryBcug8jSe+W9XDU39OOJotXajj8dgSL8yUn+d7l4Qvat/vJbAE8lonMl7P0P9QBPzmcIUvlRMuHSpRZQYkoCbwc2Filahd/5INtm7I4Y28XYzzupdwLk3cavKfOloL5YrWNqaZr/+9Tbk='"
			+ "\n\nBirthday present: (https://mineskin.org/4743)"
			+ "\n\ndrop-money-on-ground-skull-reward-name: 'Birthday present'"
			+ "\ndrop-money-on-ground-skull-texture-value: 'eyJ0aW1lc3RhbXAiOjE0Nzk5MzEzNDMxMjgsInByb2ZpbGVJZCI6IjNlMjZiMDk3MWFjZDRjNmQ5MzVjNmFkYjE1YjYyMDNhIiwicHJvZmlsZU5hbWUiOiJOYWhlbGUiLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2NmNDRkZjIzMjBiNzYzMTI0N2FhZGY1OWMwZWNlOTdhNGJiNTdkZjI4YzFjZWU3OTM0ZjZhZTI4YWY4OTg5In19fQ=='"
			+ "\ndrop-money-on-ground-skull-texture-signature: 'k1xQ6E1NuxG1ZN7nlQqRJltYrJn44XHVhNA9pSEu2Pt2mkuixMxhIDj2Tg6o+JWlTyGfXtPVWLxygeGymmeSGaVcmDTaCALg7PL11ZfSzSWSxaIufNbj1EcSi264jg5FrAa/2/DnFsgu16wjlWiIGtjCzgx2QabY8YofoPKw6Y6Y5FHZJVXpT8Rsxs8ok6ZHtfm/ZyyTgvRSzh2mKmVyQIYJ1ZKxuqWhDQfbtBpu3dlEzMAEJo85Dvb7uIFYa7WFitjFJue/c9qpqAnazWFLrx33nYpjjeYhcfAvsaNQW3JVFEkyxzEgzOHbdsbiZcqTCwO+49whu175xOqT7XhouEubDT7A3H1jiSvQvkUZJv/GzUF4qFYHSfxhr6OWoBrRGwWmPdcrYx7fUWKo43CAqa5inaiTV4gU70BWrx5i3LhIJxpnspAyTXs8tZBxeoh8IizWD7uXkYYqh3j9cwuHoxfwZuMpOx9CPTC6R/YwJ1YK5OgJBY1+QhNw+NOilWT3jTok82elFvOLm3a5yLyVs+/UPmLD7rZsFm7/DD3VnRcpgjKRiyy2j9vYsYLyNE2BVLVJxBVk2yyy9u7L4VR6PO+8v2dh9DQl7vM2ORCxKPl2lt6woHWM2+eT1PXr16LtMtAOGYT8mlKFhp8Ou2+9fu4AqWkX7n3swU6XLiK5cJs='"
			+ "\n\nChoose between \"ITEM\",\"KILLED\",\"SKULL\",\"KILLER\"")
	public String dropMoneyOnGroundItemtype = "SKULL";

	@ConfigField(name = "drop-money-use-item-as-currency", category = "dropmoneyonground", comment = "Use the reward as a currency (bag of gold) which can be sold, bought, stored in a"
			+ "\nprotected chest or a protected area (a Bank?). Check the command /mh money sell."
			+ "\nIf false the bag of gold will be picked up as money, if true the bag of gold "
			+ "\nwill be picked up as an item.")
	public boolean dropMoneyOnGroundUseAsCurrency = true;

	@ConfigField(name = "drop-money-command-alias", category = "dropmoneyonground", comment = "Here you can change the command /mh money ... to /mh <alias> ..."
			+ "\nExample: gold,bag,silver,coin,???? ")
	public String dropMoneyOnGroundMoneyCommandAlias = "money";

	@ConfigField(name = "drop-money-on-ground-item", category = "dropmoneyonground", comment = "Here you can set which item should be used when you have chosen drop-money-on-ground-itemtype: ITEM. "
			+ "\nUse Minecraft Item names like: " + "\nGOLD_NUGGET, DIAMOND, GOLD_INGOT, EMERALD, GOLDEN_APPLE ")
	public String dropMoneyOnGroundItem = "GOLD_INGOT";

	@ConfigField(name = "drop-money-on-ground-text-color", category = "dropmoneyonground", comment = "Here you can set of the color of the number above the dropped item. \nUse color names like WHITE, RED, BLUE, GOLD")
	public String dropMoneyOnGroundTextColor = "WHITE";

	@ConfigField(name = "drop-money-on-ground-skull-reward-name", category = "dropmoneyonground", comment = "This is the name of the reward")
	public String dropMoneyOnGroundSkullRewardName = "Bag of gold";

	@ConfigField(name = "drop-money-on-ground-skull-reward-name-plural", category = "dropmoneyonground", comment = "This is the name of the reward in plural")
	public String dropMoneyOnGroundSkullRewardNamePlural = "Bag of gold";

	@ConfigField(name = "drop-money-on-ground-skull-texture-value", category = "dropmoneyonground", comment = "This is the Custom Texture Value generated at http://mineskin.org")
	public String dropMoneyOnGroundSkullTextureValue = "eyJ0aW1lc3RhbXAiOjE0ODU5MTIwNjk3OTgsInByb2ZpbGVJZCI6IjdkYTJhYjNhOTNjYTQ4ZWU4MzA0OGFmYzNiODBlNjhlIiwicHJvZmlsZU5hbWUiOiJHb2xkYXBmZWwiLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzM5NmNlMTNmZjYxNTVmZGYzMjM1ZDhkMjIxNzRjNWRlNGJmNTUxMmYxYWRlZGExYWZhM2ZjMjgxODBmM2Y3In19fQ==";

	@ConfigField(name = "drop-money-on-ground-skull-texture-signature", category = "dropmoneyonground", comment = "This is the Custom Texture Signature generated at http://mineskin.org")
	public String dropMoneyOnGroundSkullTextureSignature = "m8u2ChI43ySVica7pcY0CsCuMCGgAdN7c9f/ZOxDZsPzJY8eiDrwxLIh6oPY1rvE1ja/rmftPSmdnbeHYrzLQ18QBzehFp8ZVegPsd9iNHc4FuD7nr1is2FD8M8AWAZOViiwlUKnfd8avb3SKfvFmhmVhQtE+atJYQrXhJwiqR4S+KTccA6pjIESM3AWlbCOmykg31ey7MQWB4YgtRp8NyFD3HNTLZ8alcEXBuG3t58wYBEME1UaOFah45tHuV1FW+iGBHHFWLu1UsAbg0Uw87Pp+KSTUGrhdwSc/55czILulI8IUnUfxmkaThRjd7g6VpH/w+9jLvm+7tOwfMQZlXp9104t9XMVnTAchzQr6mB3U6drCsGnuZycQzEgretQsUh3hweN7Jzz5knl6qc1n3Sn8t1yOvaIQLWG1f3l6irPdl28bwEd4Z7VDrGqYgXsd2GsOK/gCQ7rChNqbJ2p+jCja3F3ZohfmTYOU8W7DJ8Ne+xaofSuPnWODnZN9x+Y+3RE3nzH9tzP+NBMsV3YQXpvUD7Pepg7ScO+k9Fj3/F+KfBje0k6xfl+75s7kR3pNWQI5EVrO6iuky6dMuFPUBfNfq33fZV6Tqr/7o24aKpfA4WwJf91G9mC18z8NCgFR6iK4cPGmkTMvNtxUQ3MoB0LCOkRcbP0i7qxHupt8xE=";

	@ConfigField(name = "deny-hoppers-to-pickup-money-on-ground", category = "dropmoneyonground", comment = "Dark room mobspawners usually collect items in a HOPPER. This is allowed by default."
			+ "\nIf you want to deny HOPPERS to collect MobHunting Money rewards "
			+ "\nset \"deny-hoppers-to-pickup-money-on-ground\"=false")
	public boolean denyHoppersToPickUpMoney = true;

	// #####################################################################################
	// McMMO integration
	// #####################################################################################
	@ConfigField(name = "disable-integration-mcmmo", category = "mcmmo", comment = "Here you can disable the integration with McMMO."
			+ "\nhttps://www.spigotmc.org/resources/mcmmo.2445/")
	public boolean disableIntegrationMcMMO = false;

	@ConfigField(name = "enable-mcmmo-level-rewards", category = "mcmmo", comment = "Set 'enable-mcmmo-level-rewards: true' to let the players get Level as a MobHunting reward.")
	public boolean enableMcMMOLevelRewards = true;

	@ConfigField(name = "bat-mcmmo-skillreward-amount", category = "mcmmo")
	public String batMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "bat-mcmmo-skillreward-chance", category = "mcmmo")
	public double batMcMMOSkillRewardChance = 0.025;

	@ConfigField(name = "blacksmith-mcmmo-skillreward-amount", category = "mcmmo")
	public String blacksmithMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "blacksmith-mcmmo-skillreward-chance", category = "mcmmo")
	public double blacksmithMcMMOSkillRewardChance = 0.025;
	// Passive mob, risk free

	@ConfigField(name = "blaze-mcmmo-skillreward-amount", category = "mcmmo")
	public String blazeMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "blaze-mcmmo-skillreward-chance", category = "mcmmo")
	public double blazeMcMMOSkillRewardChance = 0.05;
	// Hostile, normal challenge

	@ConfigField(name = "bonusmob-mcmmo-skillreward-amount", category = "mcmmo")
	public String bonusMobMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "bonusmob-mcmmo-skillreward-chance", category = "mcmmo")
	public double bonusMobMcMMOSkillRewardChance = 0.05;
	// No opinion yet, I'm not quite sure what a bonus mob is

	@ConfigField(name = "butcher-mcmmo-skillreward-amount", category = "mcmmo")
	public String butcherMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "butcher-mcmmo-skillreward-chance", category = "mcmmo")
	public double butcherMcMMOSkillRewardChance = 0.025;
	// Passive mob, risk free

	@ConfigField(name = "cartographer-mcmmo-skillreward-amount", category = "mcmmo")
	public String cartographerMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "cartographer-mcmmo-skillreward-chance", category = "mcmmo")
	public double cartographerMcMMOSkillRewardChance = 0.025;
	// Passive mob, risk free

	@ConfigField(name = "cave-spider-mcmmo-skillreward-amount", category = "mcmmo")
	public String caveSpiderMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "cave-spider-mcmmo-skillreward-chance", category = "mcmmo")
	public double caveSpiderMcMMOSkillRewardChance = 0.04;
	// Hostile mob, easy

	@ConfigField(name = "chicken-mcmmo-skillreward-amount", category = "mcmmo")
	public String chickenMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "chicken-mcmmo-skillreward-chance", category = "mcmmo")
	public double chickenMcMMOSkillRewardChance = 0.025;
	// Passive mob, risk free

	@ConfigField(name = "clownfish-mcmmo-skillreward-amount", category = "mcmmo")
	public String clownfishMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "clownfish-mcmmo-skillreward-chance", category = "mcmmo")
	public double clownfishMcMMOSkillRewardChance = 0.075;
	// Fishing Hard

	@ConfigField(name = "cow-mcmmo-skillreward-amount", category = "mcmmo")
	public String cowMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "cow-mcmmo-skillreward-chance", category = "mcmmo")
	public double cowMcMMOSkillRewardChance = 0.025;
	// Passive mob, risk free

	@ConfigField(name = "creeper-mcmmo-skillreward-amount", category = "mcmmo")
	public String creeperMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "creeper-mcmmo-skillreward-chance", category = "mcmmo")
	public double creeperMcMMOSkillRewardChance = 0.04;
	// Hostile mob, easy

	@ConfigField(name = "donkey-mcmmo-skillreward-amount", category = "mcmmo")
	public String donkeyMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "donkey-mcmmo-skillreward-chance", category = "mcmmo")
	public double donkeyMcMMOSkillRewardChance = 0.025;
	// Passive mob, risk free

	@ConfigField(name = "elder_guardian-mcmmo-skillreward-amount", category = "mcmmo")
	public String elderGuardianMcMMOSkillRewardAmount = "1:2";
	@ConfigField(name = "elder_guardian-mcmmo-skillreward-chance", category = "mcmmo")
	public double elderGuardianMcMMOSkillRewardChance = 0.1;
	// Hostile mob, challenging

	@ConfigField(name = "enderdragon-mcmmo-skillreward-amount", category = "mcmmo")
	public String enderdragonMcMMOSkillRewardAmount = "5";
	@ConfigField(name = "enderdragon-mcmmo-skillreward-chance", category = "mcmmo")
	public double enderdragonMcMMOSkillRewardChance = 0.33;
	// Hostile mob, hard

	@ConfigField(name = "enderman-mcmmo-skillreward-amount", category = "mcmmo")
	public String endermanMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "enderman-mcmmo-skillreward-chance", category = "mcmmo")
	public double endermanMcMMOSkillRewardChance = 0.04;
	// Hostile mob, easy

	@ConfigField(name = "endermite-mcmmo-skillreward-amount", category = "mcmmo")
	public String endermiteMcMMOSkillRewardAmount = "1:2";
	@ConfigField(name = "endermite-mcmmo-skillreward-chance", category = "mcmmo")
	public double endermiteMcMMOSkillRewardChance = 0.2;
	// Hostile mob, easy (but rare)

	@ConfigField(name = "evoker-mcmmo-skillreward-amount", category = "mcmmo")
	public String evokerMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "evoker-mcmmo-skillreward-chance", category = "mcmmo")
	public double evokerMcMMOSkillRewardChance = 0.05;
	// Hostile mob, normal

	@ConfigField(name = "farmer-mcmmo-skillreward-amount", category = "mcmmo")
	public String farmerMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "farmer-mcmmo-skillreward-chance", category = "mcmmo")
	public double farmerMcMMOSkillRewardChance = 0.025;
	// Passive mob, risk free

	@ConfigField(name = "ghast-mcmmo-skillreward-amount", category = "mcmmo")
	public String ghastMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "ghast-mcmmo-skillreward-chance", category = "mcmmo")
	public double ghastMcMMOSkillRewardChance = 0.05;
	// Hostile mob, normal

	@ConfigField(name = "giant-mcmmo-skillreward-amount", category = "mcmmo")
	public String giantMcMMOSkillRewardAmount = "1:2";
	@ConfigField(name = "giant-mcmmo-skillreward-chance", category = "mcmmo")
	public double giantMcMMOSkillRewardChance = 0.1;
	// Not really a part of the standard game, but I imagine they are Hostile,
	// Challenging

	@ConfigField(name = "guardian-mcmmo-skillreward-amount", category = "mcmmo")
	public String guardianMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "guardian-mcmmo-skillreward-chance", category = "mcmmo")
	public double guardianMcMMOSkillRewardChance = 0.05;
	// Hostile mob, normal (because of the terrain and beam attack)

	@ConfigField(name = "horse-mcmmo-skillreward-amount", category = "mcmmo")
	public String horseMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "horse-mcmmo-skillreward-chance", category = "mcmmo")
	public double horseMcMMOSkillRewardChance = 0.025;
	// Passive mob, risk free

	@ConfigField(name = "husk-mcmmo-skillreward-amount", category = "mcmmo")
	public String huskMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "husk-mcmmo-skillreward-chance", category = "mcmmo")
	public double huskMcMMOSkillRewardChance = 0.04;
	// Passive mob, easy

	@ConfigField(name = "illusioner-mcmmo-skillreward-amount", category = "mcmmo")
	public String illusionerMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "illusioner-mcmmo-skillreward-chance", category = "mcmmo")
	public double illusionerMcMMOSkillRewardChance = 0.05;
	// Hostile mob, looks normal on Youtube videos (haven't tried 1.12)

	@ConfigField(name = "iron-golem-mcmmo-skillreward-amount", category = "mcmmo")
	public String ironGolemMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "iron-golem-mcmmo-skillreward-chance", category = "mcmmo")
	public double ironGolemMcMMOSkillRewardChance = 0.05;
	// Hostile mob, normal

	@ConfigField(name = "killerrabbit-mcmmo-skillreward-amount", category = "mcmmo")
	public String killerRabbitMcMMOSkillRewardAmount = "5";
	@ConfigField(name = "killerrabbit-mcmmo-skillreward-chance", category = "mcmmo")
	public double killerRabbitMcMMOSkillRewardChance = 1.0;
	// Hostile mob, easy (but extremely rare)

	@ConfigField(name = "llama-mcmmo-skillreward-amount", category = "mcmmo")
	public String llamaMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "llama-mcmmo-skillreward-chance", category = "mcmmo")
	public double llamaMcMMOSkillRewardChance = 0.025;
	// Passive mob, risk free

	@ConfigField(name = "librarian-mcmmo-skillreward-amount", category = "mcmmo")
	public String librarianMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "librarian-mcmmo-skillreward-chance", category = "mcmmo")
	public double librarianMcMMOSkillRewardChance = 0.025;
	// Passive mob, risk free

	@ConfigField(name = "magma-cube-mcmmo-skillreward-amount", category = "mcmmo")
	public String magmaCubeMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "magma-cube-mcmmo-skillreward-chance", category = "mcmmo")
	public double magmaCubeMcMMOSkillRewardChance = 0.04;
	// Hostile mob, easy

	@ConfigField(name = "mule-mcmmo-skillreward-amount", category = "mcmmo")
	public String muleMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "mule-mcmmo-skillreward-chance", category = "mcmmo")
	public double muleMcMMOSkillRewardChance = 0.025;
	// Passive mob, risk free

	@ConfigField(name = "mushroom-cow-mcmmo-skillreward-amount", category = "mcmmo")
	public String mushroomCowMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "mushroom-cow-mcmmo-skillreward-chance", category = "mcmmo")
	public double mushroomCowMcMMOSkillRewardChance = 0.025;
	// Passive mob, risk free

	@ConfigField(name = "nitwit-mcmmo-skillreward-amount", category = "mcmmo")
	public String nitwitMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "nitwit-mcmmo-skillreward-chance", category = "mcmmo")
	public double nitwitMcMMOSkillRewardChance = 0.025;
	// Passive mob, risk free

	@ConfigField(name = "ocelot-mcmmo-skillreward-amount", category = "mcmmo")
	public String ocelotMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "ocelot-mcmmo-skillreward-chance", category = "mcmmo")
	public double ocelotMcMMOSkillRewardChance = 0.025;
	// Passive mob, risk free

	@ConfigField(name = "parrot-mcmmo-skillreward-amount", category = "mcmmo")
	public String parrotMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "parrot-mcmmo-skillreward-chance", category = "mcmmo")
	public double parrotMcMMOSkillRewardChance = 0.025;
	// Passive mob, risk free

	@ConfigField(name = "pig-mcmmo-skillreward-amount", category = "mcmmo")
	public String pigMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "pig-mcmmo-skillreward-chance", category = "mcmmo")
	public double pigMcMMOSkillRewardChance = 0.025;
	// Passive mob, risk free

	@ConfigField(name = "polar_bear-mcmmo-skillreward-amount", category = "mcmmo")
	public String polarBearMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "polar_bear-mcmmo-skillreward-chance", category = "mcmmo")
	public double polarBearMcMMOSkillRewardChance = 0.05;
	// Hostile mob, normal

	@ConfigField(name = "priest-mcmmo-skillreward-amount", category = "mcmmo")
	public String priestMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "priest-mcmmo-skillreward-chance", category = "mcmmo")
	public double priestMcMMOSkillRewardChance = 0.025;
	// Passive mob, risk free

	@ConfigField(name = "pufferfish-mcmmo-skillreward-amount", category = "mcmmo")
	public String pufferfishMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "pufferfish-mcmmo-skillreward-chance", category = "mcmmo")
	public double pufferfishMcMMOSkillRewardChance = 0.06;
	// Fishing Hard

	@ConfigField(name = "pvpplayer-mcmmo-skillreward-amount", category = "mcmmo")
	public String pvpPlayerMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "pvpplayer-mcmmo-skillreward-chance", category = "mcmmo")
	public double pvpPlayerMcMMOSkillRewardChance = 0.025;
	// Easy to abuse

	@ConfigField(name = "rabbit-mcmmo-skillreward-amount", category = "mcmmo")
	public String rabbitMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "rabbit-mcmmo-skillreward-chance", category = "mcmmo")
	public double rabbitMcMMOSkillRewardChance = 0.025;
	// Passive mob, risk free

	@ConfigField(name = "rawfish-mcmmo-skillreward-amount", category = "mcmmo")
	public String rawfishMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "rawfish-mcmmo-skillreward-chance", category = "mcmmo")
	public double rawfishMcMMOSkillRewardChance = 0.05;
	// Fishing Easy

	@ConfigField(name = "rawsalmon-mcmmo-skillreward-amount", category = "mcmmo")
	public String rawsalmonMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "rawsalmon-mcmmo-skillreward-chance", category = "mcmmo")
	public double rawsalmonMcMMOSkillRewardChance = 0.06;
	// Fishing normal

	@ConfigField(name = "sheep-mcmmo-skillreward-amount", category = "mcmmo")
	public String sheepMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "sheep-mcmmo-skillreward-chance", category = "mcmmo")
	public double sheepMcMMOSkillRewardChance = 0.025;
	// Passive mob, risk free

	@ConfigField(name = "shulker-mcmmo-skillreward-amount", category = "mcmmo")
	public String shulkerMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "shulker-mcmmo-skillreward-chance", category = "mcmmo")
	public double shulkerMcMMOSkillRewardChance = 0.05;
	// Hostile mob, normal

	@ConfigField(name = "silverfish-mcmmo-skillreward-amount", category = "mcmmo")
	public String silverfishMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "silverfish-mcmmo-skillreward-chance", category = "mcmmo")
	public double silverfishMcMMOSkillRewardChance = 0.04;
	// Hostile mob, easy

	@ConfigField(name = "skeleton-mcmmo-skillreward-amount", category = "mcmmo")
	public String skeletonMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "skeleton-mcmmo-skillreward-chance", category = "mcmmo")
	public double skeletonMcMMOSkillRewardChance = 0.04;
	// Hostile mob, easy

	@ConfigField(name = "skeletonhorse-mcmmo-skillreward-amount", category = "mcmmo")
	public String skeletonHorseMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "skeletonhorse-mcmmo-skillreward-chance", category = "mcmmo")
	public double skeletonHorseMcMMOSkillRewardChance = 0.025;
	// Passive mob, risk free

	@ConfigField(name = "slime-base-mcmmo-skillreward-amount", category = "mcmmo")
	public String slimeMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "slime-base-mcmmo-skillreward-chance", category = "mcmmo")
	public double slimeMcMMOSkillRewardChance = 0.04;
	// Hostile mob, easy

	@ConfigField(name = "snowman-mcmmo-skillreward-amount", category = "mcmmo")
	public String snowmanMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "snowman-mcmmo-skillreward-chance", category = "mcmmo")
	public double snowmanMcMMOSkillRewardChance = 0.025;
	// Passive mob, risk free

	@ConfigField(name = "spider-mcmmo-skillreward-amount", category = "mcmmo")
	public String spiderMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "spider-mcmmo-skillreward-chance", category = "mcmmo")
	public double spiderMcMMOSkillRewardChance = 0.04;
	// Hostile mob, easy

	@ConfigField(name = "squid-mcmmo-skillreward-amount", category = "mcmmo")
	public String squidMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "squid-mcmmo-skillreward-chance", category = "mcmmo")
	public double squidMcMMOSkillRewardChance = 0.025;
	// Passive mob, risk free

	@ConfigField(name = "stray-mcmmo-skillreward-amount", category = "mcmmo")
	public String strayMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "stray-mcmmo-skillreward-chance", category = "mcmmo")
	public double strayMcMMOSkillRewardChance = 0.04;
	// Hostile mob, easy

	@ConfigField(name = "vex-mcmmo-skillreward-amount", category = "mcmmo")
	public String vexMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "vex-mcmmo-skillreward-chance", category = "mcmmo")
	public double vexMcMMOSkillRewardChance = 0.04;
	// Hostile mob, easy

	@ConfigField(name = "villager-mcmmo-skillreward-amount", category = "mcmmo")
	public String villagerMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "villager-mcmmo-skillreward-chance", category = "mcmmo")
	public double villagerMcMMOSkillRewardChance = 0.025;
	// Passive mob, risk free

	@ConfigField(name = "vindicator-mcmmo-skillreward-amount", category = "mcmmo")
	public String vindicatorMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "vindicator-mcmmo-skillreward-chance", category = "mcmmo")
	public double vindicatorMcMMOSkillRewardChance = 0.05;
	// Hostile mob, normal

	@ConfigField(name = "witch-mcmmo-skillreward-amount", category = "mcmmo")
	public String witchMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "witch-mcmmo-skillreward-chance", category = "mcmmo")
	public double witchMcMMOSkillRewardChance = 0.05;
	// Hostile mob, normal

	@ConfigField(name = "wither-mcmmo-skillreward-amount", category = "mcmmo")
	public String witherMcMMOSkillRewardAmount = "5";
	@ConfigField(name = "wither-mcmmo-skillreward-chance", category = "mcmmo")
	public double witherMcMMOSkillRewardChance = 0.33;
	// Hostile mob, hard (and rare considering the summoning requirements)

	@ConfigField(name = "wither-skeleton-mcmmo-skillreward-amount", category = "mcmmo")
	public String witherSkeletonMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "wither-skeleton-mcmmo-skillreward-chance", category = "mcmmo")
	public double witherSkeletonMcMMOSkillRewardChance = 0.05;
	// Hostile mob, normal

	@ConfigField(name = "wolf-mcmmo-skillreward-amount", category = "mcmmo")
	public String wolfMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "wolf-mcmmo-skillreward-chance", category = "mcmmo")
	public double wolfMcMMOSkillRewardChance = 0.04;
	// Hostile mob (kind of, needs to be hit first), easy

	@ConfigField(name = "zombie-mcmmo-skillreward-amount", category = "mcmmo")
	public String zombieMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "zombie-mcmmo-skillreward-chance", category = "mcmmo")
	public double zombieMcMMOSkillRewardChance = 0.4;
	// Hostile mob, easy

	@ConfigField(name = "zombiehorse-mcmmo-skillreward-amount", category = "mcmmo")
	public String zombieHorseMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "zombiehorse-mcmmo-skillreward-chance", category = "mcmmo")
	public double zombieHorseMcMMOSkillRewardChance = 0.025;
	// Passive mob, risk free

	@ConfigField(name = "zombie-pigman-mcmmo-skillreward-amount", category = "mcmmo")
	public String zombiePigManMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "zombie-pigman-mcmmo-skillreward-chance", category = "mcmmo")
	public double zombiePigManMcMMOSkillRewardChance = 0.05;
	// Hostile mob, normal

	@ConfigField(name = "zombie_villager-mcmmo-skillreward-amount", category = "mcmmo")
	public String zombieVillagerMcMMOSkillRewardAmount = "1";
	@ConfigField(name = "zombie_villager-mcmmo-skillreward-chance", category = "mcmmo")
	public double zombieVillagerMcMMOSkillRewardChance = 0.04;

	// #####################################################################################
	// CrackShot integration
	// #####################################################################################
	@ConfigField(name = "disable-integration-crackshot", category = "crackshot", comment = "Disable integration with CrackShot."
			+ "\nhttps://dev.bukkit.org/projects/crackshot")
	public boolean disableIntegrationCrackShot = false;

	@ConfigField(name = "crackshot-multiplier", category = "crackshot")
	public double crackShot = 0.7;

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

	@ConfigField(name = "disable-integration-smartgiants", category = "plugins", comment = "Disable integration with SmartGiants."
			+ "\nhttps://www.spigotmc.org/threads/smartgiants.55208/")
	public boolean disableIntegrationSmartGiants = false;

	@ConfigField(name = "disable-integration-placeholderapi", category = "plugins", comment = "Disable integration with PlaceholderAPI."
			+ "\nhttps://www.spigotmc.org/resources/placeholderapi.6245/")
	public boolean disableIntegrationPlaceholderAPI = false;

	@ConfigField(name = "disable-integration-bossshop", category = "plugins", comment = "Disable integration with BossShop."
			+ "\nhttps://www.spigotmc.org/resources/bossshop-powerful-and-playerfriendly-chest-gui-shop-menu-plugin.222/")
	public boolean disableIntegrationBossShop = false;

	@ConfigField(name = "disable-integration-extra-hard-mode", category = "plugins", comment = "Disable integration with ExtraHardmode."
			+ "\nhttps://www.spigotmc.org/resources/extra-hard-mode.19673/")
	public boolean disableIntegrationExtraHardMode = false;

	@ConfigField(name = "disable-integration-herobrine", category = "plugins", comment = "Disable integration with Herobrine."
			+ "\nhttps://www.theprogrammersworld.net/Herobrine/")
	public boolean disableIntegrationHerobrine = false;

	@ConfigField(name = "disable-integration-holograms", category = "plugins", comment = "Disable integration with Holograms."
			+ "\nhttps://www.spigotmc.org/resources/holograms.4924/")
	public boolean disableIntegrationHolograms = false;

	@ConfigField(name = "disable-integration-holographic-displays", category = "plugins", comment = "Disable integration with Holograms."
			+ "\nhttps://dev.bukkit.org/projects/holographic-displays")
	public boolean disableIntegrationHolographicDisplays = false;

	@ConfigField(name = "disable-integration-preciousstones", category = "plugins", comment = "Disable integration with PreciousStones."
			+ "\nhttps://www.spigotmc.org/resources/preciousstones.5270/")
	public boolean disableIntegrationPreciousStones = false;

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
	// Economy settings
	// #####################################################################################
	@ConfigField(name = "starting_balance", category = "economy", comment = "Set initial balance for new players.")
	public double startingBalance = 3000;

	// #####################################################################################
	// Generel settings
	// #####################################################################################
	@ConfigField(name = "disabled-in-worlds", category = "general", comment = "Put the names of the worlds here that you do not wish for mobhunting to be enabled in.")
	public String[] disabledInWorlds = { "worldname" };

	@ConfigField(name = "language", category = "general", comment = "The language (file) to use. You can put the name of the language file as the language code "
			+ "\n(eg. en_US, de_DE, fr_FR, ect.) or you can specify the name of a custom file without the .lang\nPlease check the lang/ folder for a list of all available translations.")
	public String language = "en_US";

	@ConfigField(name = "disable_money_rewards_from_mobspawners_and_eggs", category = "general", comment = "Can the players earn money on mobs spawned from mobspawners, eggs and from eggs from Dispensers?"
			+ "\nIf you disable this you are still able to get rewards from specific Spawners, if you white list the area "
			+ "\nusing '/mh whitelistarea'.")
	public boolean disableMoneyRewardsFromMobSpawnersEggsAndDispensers = true;
	@ConfigField(name = "disable_naturally_dropped_items_from_mobspawners_and_eggs", category = "general", comment = "Let the players get the naturally dropped items from mobs spawned from mobspawners, eggs and from eggs from Dispensers ?")
	public boolean disableNaturallyDroppedItemsFromMobSpawnersEggsAndDispensers = false;
	@ConfigField(name = "disable_naturally_dropped_xp_from_mobspawners_and_eggs", category = "general", comment = "Let the players get the naturally dropped XP from mobs spawned from mobspawners, eggs and from eggs from Dispensers ?")
	public boolean disableNaturallyDroppedXPFromMobSpawnersEggsAndDispensers = false;

	@ConfigField(name = "disable_mobhunting_advancements", category = "general", comment = "As of V 5.0.0 MobHunting utilizises the Advancement system (L key) to to show which"
			+ "\nAchievements the players has made. This is still BETA feature and it is only Supported on Spigot Servers and if you have any problems, you can set 'disable_mobhunting_advancements: true and the reload the plugin.")
	public boolean disableMobHuntingAdvancements = true;

	@ConfigField(name = "use-actionbar-for-broadcasts", category = "general", comment = "Broadcast messages will be send in the ActionBar if MobHunting finds a supported ActionBar plugin.")
	public boolean useActionBarforBroadcasts = true;

	@ConfigField(name = "broadcast-achievement", category = "general", comment = "Should achievements be broadcasted?")
	public boolean broadcastAchievement = true;

	@ConfigField(name = "broadcast-first-achievement", category = "general", comment = "Should the hunt begins achievement be broadcasted?")
	public boolean broadcastFirstAchievement = true;

	@ConfigField(name = "save-period", category = "general", comment = "Time between saves in ticks (20 ticks ~ 1 sec) This number must be higher that 1200 ticks = 2 minutes,"
			+ "\nbut I recommend to save every 5th minute = 6000 ticks")
	public int savePeriod = 6000;

	@ConfigField(name = "leaderboard-update-period", category = "general", comment = "Time between leaderboard updates in ticks (20 ticks ~ 1 sec) This number must be higher that 1200 ticks = 2 minutes,"
			+ "\nbut I recommend to update leaderboards max every 5 min = 6000 ticks")
	public int leaderboardUpdatePeriod = 6000;

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

	@ConfigField(name = "disable-natural-item-drops", category = "general", comment = "Disable natural drops when a mob is killed "
			+ "\n(because player is grinding or protected by Worldguard or in God mode or similar)"
			+ "\nIf you want the mobs to drops normal rewards set " + "\n\"disable-natural-item-drops\"=false")
	public boolean disableNaturalItemDrops = true;

	@ConfigField(name = "disable-natural-xp-drops", category = "general", comment = "Disable natural xp drops when a mob is killed"
			+ "\n(because player is grinding or protected by Worldguard or in God mode or similar)"
			+ "\nIf you want the mobs to drop normal XP set " + "\n\"disable-natural-xp-drops\"=false")
	public boolean disableNatualXPDrops = true;

	@ConfigField(name = "try-to-cancel-natural-drops-when-in-creative", category = "general", comment = "Try to cancel natural drops when a mob is killed the player is in creative mode."
			+ "\nIf you want the mobs to drops normal rewards set "
			+ "\n\"try-to-cancel-natural-drops-when-in-creative\"=false")
	public boolean tryToCancelNaturalDropsWhenInCreative = true;

	@ConfigField(name = "try-to-cancel-xp-drops-when-in-creative", category = "general", comment = "Try to cancel XP drops when a mob is killed while the player is in creative mode."
			+ "\nIf you want the mobs to drop normal XP set " + "\n\"try-to-cancel-xp-drops-when-in-creative\"=false")
	public boolean tryToCancelXPDropsWhenInCreative = true;

	@Override
	protected void onPostLoad() throws InvalidConfigurationException {
		Messages.setLanguage(language + ".lang");
	}

}
