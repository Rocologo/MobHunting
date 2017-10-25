package one.lindegaard.MobHunting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;

import com.gmail.nossr50.datatypes.skills.SkillType;

import one.lindegaard.MobHunting.compatibility.FactionsCompat;
import one.lindegaard.MobHunting.compatibility.McMMOCompat;
import one.lindegaard.MobHunting.events.MobHuntFishingEvent;
import one.lindegaard.MobHunting.mobs.ExtendedMob;
import one.lindegaard.MobHunting.modifier.DifficultyBonus;
import one.lindegaard.MobHunting.modifier.FactionWarZoneBonus;
import one.lindegaard.MobHunting.modifier.HappyHourBonus;
import one.lindegaard.MobHunting.modifier.IModifier;
import one.lindegaard.MobHunting.modifier.RankBonus;
import one.lindegaard.MobHunting.util.Misc;

public class FishingManager implements Listener {

	private MobHunting plugin;
	private Set<IModifier> mFishingModifiers = new HashSet<IModifier>();

	public FishingManager(MobHunting plugin) {
		this.plugin = plugin;
		if (!MobHunting.getConfigManager().disableFishingRewards) {
			registerFishingModifiers();
			Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
		}
	}

	private void registerFishingModifiers() {
		mFishingModifiers.add(new DifficultyBonus());
		mFishingModifiers.add(new HappyHourBonus());
		mFishingModifiers.add(new RankBonus());
		if (FactionsCompat.isSupported())
			mFishingModifiers.add(new FactionWarZoneBonus());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void Fish(PlayerFishEvent event) {

		if (event.isCancelled()) {
			Messages.debug("FishingEvent: event was cancelled");
			return;
		}

		Player player = event.getPlayer();
		if (player == null) {
			Messages.debug("FishingEvent: player was null");
			return;
		}

		if (!MobHunting.getMobHuntingManager().isHuntEnabled(player)) {
			Messages.debug("FishingEvent %s: Player doesnt have permission mobhunting.enable", player.getName());
			return;
		}

		State state = event.getState();
		Entity fish = event.getCaught();

		if (fish == null || (fish != null && !(fish instanceof Item)))
			Messages.debug("FishingEvent: State=%s", state);
		else
			Messages.debug("FishingEvent: State=%s, %s caught a %s", state, player.getName(),
					((Item) fish).getItemStack().getData());

		switch (state) {
		case CAUGHT_FISH:
			// When a player has successfully caught a fish and is reeling it
			// in.
			// break;
		case CAUGHT_ENTITY:
			// When a player has successfully caught an entity
			if (player.getGameMode() != GameMode.SURVIVAL) {
				Messages.debug("FishingBlocked: %s is not in survival mode", player.getName());
				plugin.getMessages().learn(player, Messages.getString("mobhunting.learn.survival"));
				return;
			}

			if (fish == null || !(fish instanceof Item)
					|| ((Item) fish).getItemStack().getType() != Material.RAW_FISH) {
				Messages.debug("FishingBlocked: %s only get rewards for fish", player.getName());
				return;
			}

			Material material_under_hook = fish.getLocation().getBlock().getType();
			if (!(material_under_hook == Material.WATER || material_under_hook == Material.STATIONARY_WATER)) {
				Messages.debug("FishingBlocked: %s was fishing on %s", player.getName(), material_under_hook);
				return;
			}

			// Calculate basic the reward
			ExtendedMob eMob = MobHunting.getExtendedMobManager().getExtendedMobFromEntity(fish);
			if (eMob.getMob_id() == 0) {
				Bukkit.getLogger().warning("Unknown Mob:" + eMob.getMobName() + " from plugin " + eMob.getMobPlugin());
				Bukkit.getLogger().warning("Please report this to developer!");
				return;
			}
			double cash = plugin.getRewardManager().getBaseKillPrize(fish);

			Messages.debug("Basic Prize=%s for catching a %s", plugin.getRewardManager().format(cash),
					eMob.getMobName());

			// Apply the modifiers to Basic reward
			double multipliers = 1.0;
			HashMap<String, Double> multiplierList = new HashMap<String, Double>();
			ArrayList<String> modifiers = new ArrayList<String>();
			for (IModifier mod : mFishingModifiers) {
				if (mod.doesApply(fish, player, null, null, null)) {
					double amt = mod.getMultiplier(fish, player, null, null, null);
					if (amt != 1.0) {
						Messages.debug("Multiplier: %s = %s", mod.getName(), amt);
						modifiers.add(mod.getName());
						multiplierList.put(mod.getName(), amt);
						multipliers *= amt;
					}
				}
			}

			String extraString = "";

			// Only display the multiplier if its not 1
			if (Math.abs(multipliers - 1) > 0.05)
				extraString += String.format("x%.1f", multipliers);

			// Add on modifiers
			int i = 0;
			for (String modifier : modifiers) {
				if (i == 0)
					extraString += ChatColor.WHITE + " ( " + modifier;
				else
					extraString += ChatColor.WHITE + " * " + modifier;
				i++;
			}
			if (i != 0)
				extraString += ChatColor.WHITE + " ) ";

			cash *= multipliers;

			cash = Misc.ceil(cash);

			// Pay the reward to player and assister
			if (cash >= MobHunting.getConfigManager().minimumReward
					|| cash <= -MobHunting.getConfigManager().minimumReward
					|| !plugin.getRewardManager().getKillConsoleCmd(fish).isEmpty()) {

				// Handle MobHuntFishingEvent
				MobHuntFishingEvent event2 = new MobHuntFishingEvent(player, fish, cash, multiplierList);
				Bukkit.getPluginManager().callEvent(event2);
				if (event2.isCancelled()) {
					Messages.debug("FishingBlocked %s: MobHuntFishingEvent was cancelled by another plugin",
							player.getName());
					return;
				}

				if (cash >= MobHunting.getConfigManager().minimumReward) {
					plugin.getRewardManager().depositPlayer(player, cash);
					Messages.debug("%s got a reward (%s)", player.getName(), plugin.getRewardManager().format(cash));
				} else if (cash <= -MobHunting.getConfigManager().minimumReward) {
					plugin.getRewardManager().withdrawPlayer(player, -cash);
					Messages.debug("%s got a penalty (%s)", player.getName(), plugin.getRewardManager().format(cash));
				}

				// Record Fishing Achievement is done using
				// SeventhHuntAchievement.java (onFishingCompleted)

				// Record the kill in the Database
				if (player != null) {
					Messages.debug("RecordFishing: %s caught a %s (%s)", player.getName(), eMob.getMobName(),
							eMob.getMobPlugin().name());
					MobHunting.getDataStoreManager().recordKill(player, eMob, player.hasMetadata("MH:hasBonus"), cash);
				}

				// Handle Muted mode
				boolean fisherman_muted = false;
				if (plugin.getPlayerSettingsmanager().containsKey(player))
					fisherman_muted = plugin.getPlayerSettingsmanager().getPlayerSettings(player).isMuted();

				// Tell the player that he got the reward/penalty,
				// unless
				// muted
				if (!fisherman_muted)
					if (extraString.trim().isEmpty()) {
						if (cash >= MobHunting.getConfigManager().minimumReward) {
							plugin.getMessages().playerActionBarMessage(player,
									ChatColor.GREEN + "" + ChatColor.ITALIC
											+ Messages.getString("mobhunting.fishcaught.reward", "prize",
													plugin.getRewardManager().format(cash)));
						} else if (cash <= -MobHunting.getConfigManager().minimumReward) {
							plugin.getMessages().playerActionBarMessage(player,
									ChatColor.RED + "" + ChatColor.ITALIC
											+ Messages.getString("mobhunting.fishcaught.penalty", "prize",
													plugin.getRewardManager().format(cash)));
						}

					} else {
						if (cash >= MobHunting.getConfigManager().minimumReward) {
							Messages.debug("Message to send to ActionBar=%s", ChatColor.GREEN + "" + ChatColor.ITALIC
									+ Messages.getString("mobhunting.fishcaught.reward.bonuses", "prize",
											plugin.getRewardManager().format(cash), "bonuses", extraString.trim(),
											"multipliers", plugin.getRewardManager().format(multipliers)));
							plugin.getMessages().playerActionBarMessage(player, ChatColor.GREEN + "" + ChatColor.ITALIC
									+ Messages.getString("mobhunting.fishcaught.reward.bonuses", "prize",
											plugin.getRewardManager().format(cash), "bonuses", extraString.trim(),
											"multipliers", plugin.getRewardManager().format(multipliers)));
						} else if (cash <= -MobHunting.getConfigManager().minimumReward) {
							plugin.getMessages().playerActionBarMessage(player, ChatColor.RED + "" + ChatColor.ITALIC
									+ Messages.getString("mobhunting.fishcaught.penalty.bonuses", "prize",
											plugin.getRewardManager().format(cash), "bonuses", extraString.trim(),
											"multipliers", plugin.getRewardManager().format(multipliers)));
						} else
							Messages.debug("FishingBlocked %s: Reward was less than %s", player.getName(),
									MobHunting.getConfigManager().minimumReward);
					}

				// McMMO Experience rewards
				if (McMMOCompat.isSupported() && MobHunting.getConfigManager().enableMcMMOLevelRewards) {
					double chance = MobHunting.getMobHuntingManager().mRand.nextDouble();
					int level = plugin.getRewardManager().getMcMMOLevel(fish);
					Messages.debug("If %s<%s %s will get a McMMO Level for fishing", chance,
							plugin.getRewardManager().getMcMMOChance(fish), player.getName());
					if (chance < plugin.getRewardManager().getMcMMOChance(fish)) {
						McMMOCompat.addLevel(player, SkillType.FISHING.getName(), level);
						Messages.debug("%s was rewarded with %s McMMO level for Fishing", player.getName(), level);
						plugin.getMessages().playerSendMessage(player, Messages.getString("mobhunting.mcmmo.fishing_level", "mcmmo_level", level));
					}
				}

				String fishermanPos = player.getLocation().getBlockX() + " " + player.getLocation().getBlockY() + " "
						+ player.getLocation().getBlockZ();
				if (plugin.getRewardManager().isCmdGointToBeExcuted(fish)) {
					String worldname = player.getWorld().getName();
					String prizeCommand = plugin.getRewardManager().getKillConsoleCmd(fish)
							.replaceAll("\\{player\\}", player.getName()).replaceAll("\\{killer\\}", player.getName())
							.replaceAll("\\{world\\}", worldname)
							.replace("\\{prize\\}", plugin.getRewardManager().format(cash))
							.replaceAll("\\{killerpos\\}", fishermanPos);
					Messages.debug("command to be run is:" + prizeCommand);
					if (!plugin.getRewardManager().getKillConsoleCmd(fish).equals("")) {
						String str = prizeCommand;
						do {
							if (str.contains("|")) {
								int n = str.indexOf("|");
								Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),
										str.substring(0, n));
								str = str.substring(n + 1, str.length()).toString();
							}
						} while (str.contains("|"));
						Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), str);
					}

					// send a message to the player
					if (!plugin.getRewardManager().getKillRewardDescription(fish).equals("") && !fisherman_muted) {
						String message = ChatColor.GREEN + "" + ChatColor.ITALIC + plugin.getRewardManager()
								.getKillRewardDescription(fish).replaceAll("\\{player\\}", player.getName())
								.replaceAll("\\{killer\\}", player.getName())
								.replace("\\{prize\\}", plugin.getRewardManager().format(cash))
								.replaceAll("\\{world\\}", worldname).replaceAll("\\{killerpos\\}", fishermanPos);

						Messages.debug("Description to be send:" + message);
						plugin.getMessages().playerSendMessage(player, message);
					}
				}
			}
			break;
		case BITE:
			// Called when there is a bite on the hook and it is ready to be
			// reeled in.
			break;
		case FAILED_ATTEMPT:
			// When a player fails to catch anything while fishing usually due
			// to poor aiming or timing
			break;
		case FISHING:
			// When a player is fishing, ie casting the line out.
			break;
		case IN_GROUND:
			// When a bobber is stuck in the ground
			Messages.debug("State is IN_GROUND");
			break;
		// default:
		// break;

		}

	}

	public Set<IModifier> getFishingModifiers() {
		return mFishingModifiers;
	}

}
