package one.lindegaard.MobHunting;

import com.gmail.nossr50.datatypes.skills.SkillType;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import one.lindegaard.MobHunting.bounty.Bounty;
import one.lindegaard.MobHunting.bounty.BountyStatus;
import one.lindegaard.MobHunting.compatibility.*;
import one.lindegaard.MobHunting.events.BountyKillEvent;
import one.lindegaard.MobHunting.events.MobHuntEnableCheckEvent;
import one.lindegaard.MobHunting.events.MobHuntKillEvent;
import one.lindegaard.MobHunting.grinding.Area;
import one.lindegaard.MobHunting.mobs.ExtendedMob;
import one.lindegaard.MobHunting.modifier.*;
import one.lindegaard.MobHunting.placeholder.PlaceHolderData;
import one.lindegaard.MobHunting.update.Updater;
import one.lindegaard.MobHunting.util.Misc;
import org.bukkit.*;
import org.bukkit.command.CommandException;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Method;
import java.util.*;

public class MobHuntingManager implements Listener {

	private MobHunting plugin;
	public Random mRand = new Random();
	private final String SPAWNER_BLOCKED = "MH:SpawnerBlocked";

	private static WeakHashMap<LivingEntity, DamageInformation> mDamageHistory = new WeakHashMap<>();
	private Set<IModifier> mHuntingModifiers = new HashSet<>();

	/**
	 * Constructor for MobHuntingManager
	 *
	 * @param instance
	 */
	public MobHuntingManager(MobHunting instance) {
		this.plugin = instance;
		registerHuntingModifiers();
		Bukkit.getServer().getPluginManager().registerEvents(this, instance);
	}

	/**
	 * Gets the DamageInformation for a LivingEntity
	 *
	 * @param entity
	 * @return
	 */
	public DamageInformation getDamageInformation(Entity entity) {
		return mDamageHistory.get(entity);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onPlayerJoin(PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		setHuntEnabled(player, true);
		if (player.hasPermission("mobhunting.update") && MobHunting.getConfigManager().updateCheck) {
			new BukkitRunnable() {
				@Override
				public void run() {
					Updater.pluginUpdateCheck(player, true, true);
				}
			}.runTaskLater(plugin, 20L);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
	private void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
		Player player = event.getPlayer();
		HuntData data = new HuntData(player);
		if (data.getKillstreakLevel() != 0 && data.getKillstreakMultiplier() != 1) {
			plugin.getMessages().playerActionBarMessage(player,
					ChatColor.RED + "" + ChatColor.ITALIC + Messages.getString("mobhunting.killstreak.ended"));
		}
		data.setKillStreak(0);
		data.putHuntDataToPlayer(player);
	}

	/**
	 * Set if MobHunting is allowed for the player
	 *
	 * @param player
	 * @param enabled
	 *            = true : means the MobHunting is allowed
	 */
	public void setHuntEnabled(Player player, boolean enabled) {
		player.setMetadata("MH:enabled", new FixedMetadataValue(plugin, enabled));
	}

	/**
	 * Gets the online player (backwards compatibility)
	 *
	 * @return number of players online
	 */
	public int getOnlinePlayersAmount() {
		try {
			Method method = Server.class.getMethod("getOnlinePlayers");
			if (method.getReturnType().equals(Collection.class)) {
				return ((Collection<?>) method.invoke(Bukkit.getServer())).size();
			} else {
				return ((Player[]) method.invoke(Bukkit.getServer())).length;
			}
		} catch (Exception ex) {
			Messages.debug(ex.getMessage());
		}
		return 0;
	}

	/**
	 * Gets the online player (for backwards compatibility)
	 *
	 * @return all online players as a Java Collection, if return type of
	 *         Bukkit.getOnlinePlayers() is Player[] it will be converted to a
	 *         Collection.
	 */
	@SuppressWarnings({ "unchecked" })
	public Collection<Player> getOnlinePlayers() {
		Method method;
		try {
			method = Bukkit.class.getDeclaredMethod("getOnlinePlayers");
			Object players = method.invoke(null);
			Collection<Player> newPlayers;
			if (players instanceof Player[])
				newPlayers = Arrays.asList((Player[]) players);
			else
				newPlayers = (Collection<Player>) players;
			return newPlayers;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return Collections.emptyList();
	}

	/**
	 * Checks if MobHunting is enabled for the player
	 *
	 * @param player
	 * @return true if MobHunting is enabled for the player, false if not.
	 */
	public boolean isHuntEnabled(Player player) {
		if (CitizensCompat.isNPC(player))
			return false;

		if (!player.hasMetadata("MH:enabled")) {
			Messages.debug("KillBlocked %s: Player doesnt have MH:enabled", player.getName());
			return false;
		}

		List<MetadataValue> values = player.getMetadata("MH:enabled");

		// Use the first value that matches the required type
		boolean enabled = false;
		for (MetadataValue value : values) {
			if (value.value() instanceof Boolean)
				enabled = value.asBoolean();
		}

		if (enabled && !player.hasPermission("mobhunting.enable")) {
			Messages.debug("KillBlocked %s: Player doesnt have permission mobhunting.enable", player.getName());
			return false;
		}

		if (!enabled) {
			Messages.debug("KillBlocked %s: MH:enabled is false", player.getName());
			return false;
		}

		MobHuntEnableCheckEvent event = new MobHuntEnableCheckEvent(player);
		Bukkit.getPluginManager().callEvent(event);

		if (!event.isEnabled())
			Messages.debug("KillBlocked %s: Plugin cancelled check", player.getName());
		return event.isEnabled();
	}

	private void registerHuntingModifiers() {
		mHuntingModifiers.add(new BonusMobBonus());
		mHuntingModifiers.add(new BrawlerBonus());
		if (ConquestiaMobsCompat.isSupported())
			mHuntingModifiers.add(new ConquestiaBonus());
		mHuntingModifiers.add(new CoverBlown());
		mHuntingModifiers.add(new CriticalModifier());
		mHuntingModifiers.add(new DifficultyBonus());
		if (FactionsCompat.isSupported())
			mHuntingModifiers.add(new FactionWarZoneBonus());
		mHuntingModifiers.add(new FlyingPenalty());
		mHuntingModifiers.add(new FriendleFireBonus());
		mHuntingModifiers.add(new GrindingPenalty());
		mHuntingModifiers.add(new HappyHourBonus());
		mHuntingModifiers.add(new MountedBonus());
		mHuntingModifiers.add(new ProSniperBonus());
		mHuntingModifiers.add(new RankBonus());
		mHuntingModifiers.add(new ReturnToSenderBonus());
		mHuntingModifiers.add(new ShoveBonus());
		mHuntingModifiers.add(new SneakyBonus());
		mHuntingModifiers.add(new SniperBonus());
		if (MobStackerCompat.isSupported() || StackMobCompat.isSupported())
			mHuntingModifiers.add(new StackedMobBonus());
		mHuntingModifiers.add(new Undercover());
		if (CrackShotCompat.isSupported())
			mHuntingModifiers.add(new CrackShotPenalty());
		if (InfernalMobsCompat.isSupported())
			mHuntingModifiers.add(new InfernalMobBonus());
	}

	/**
	 * Check if MobHunting is allowed in world
	 *
	 * @param world
	 * @return true if MobHunting is allowed.
	 */
	public boolean isHuntEnabledInWorld(World world) {
		if (world != null)
			for (String worldName : MobHunting.getConfigManager().disabledInWorlds) {
				if (world.getName().equalsIgnoreCase(worldName))
					return false;
			}

		return true;
	}

	/**
	 * Checks if the player has permission to kill the mob
	 *
	 * @param player
	 * @param mob
	 * @return true if the player has permission to kill the mob
	 */
	public boolean hasPermissionToKillMob(Player player, LivingEntity mob) {
		String permission_postfix = "*";
		if (TARDISWeepingAngelsCompat.isWeepingAngelMonster(mob)) {
			permission_postfix = TARDISWeepingAngelsCompat.getWeepingAngelMonsterType(mob).name();
			if (player.isPermissionSet("mobhunting.mobs." + permission_postfix))
				return player.hasPermission("mobhunting.mobs." + permission_postfix);
			else {
				Messages.debug("Permission mobhunting.mobs." + permission_postfix + " not set, defaulting to True.");
				return true;
			}
		} else if (MythicMobsCompat.isMythicMob(mob)) {
			permission_postfix = MythicMobsCompat.getMythicMobType(mob);
			if (player.isPermissionSet("mobhunting.mobs." + permission_postfix))
				return player.hasPermission("mobhunting.mobs." + permission_postfix);
			else {
				Messages.debug("Permission mobhunting.mobs." + permission_postfix + " not set, defaulting to True.");
				return true;
			}
		} else if (CitizensCompat.isSentryOrSentinelOrSentries(mob)) {
			permission_postfix = "npc-" + CitizensCompat.getNPCId(mob);
			if (player.isPermissionSet("mobhunting.mobs." + permission_postfix))
				return player.hasPermission("mobhunting.mobs." + permission_postfix);
			else {
				Messages.debug("Permission mobhunting.mobs.'" + permission_postfix + "' not set, defaulting to True.");
				return true;
			}
		} else if (CustomMobsCompat.isCustomMob(mob)) {
			permission_postfix = CustomMobsCompat.getCustomMobType(mob);
			if (player.isPermissionSet("mobhunting.mobs." + permission_postfix))
				return player.hasPermission("mobhunting.mobs." + permission_postfix);
			else {
				Messages.debug("Permission mobhunting.mobs.'" + permission_postfix + "' not set, defaulting to True.");
				return true;
			}
		} else if (MysteriousHalloweenCompat.isMysteriousHalloween(mob)) {
			permission_postfix = "npc-" + MysteriousHalloweenCompat.getMysteriousHalloweenType(mob);
			if (player.isPermissionSet("mobhunting.mobs." + permission_postfix))
				return player.hasPermission("mobhunting.mobs." + permission_postfix);
			else {
				Messages.debug("Permission mobhunting.mobs.'" + permission_postfix + "' not set, defaulting to True.");
				return true;
			}
		} else {
			permission_postfix = mob.getType().toString();
			if (player.isPermissionSet("mobhunting.mobs." + permission_postfix))
				return player.hasPermission("mobhunting.mobs." + permission_postfix);
			else {
				Messages.debug("Permission 'mobhunting.mobs.*' or 'mobhunting.mobs." + permission_postfix
						+ "' not set, defaulting to True.");
				return true;
			}
		}
	}

	// ************************************************************************************
	// EVENTS
	// ************************************************************************************
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onPlayerDeath(PlayerDeathEvent event) {
		if (!MobHunting.getMobHuntingManager().isHuntEnabledInWorld(event.getEntity().getWorld())
				|| !MobHunting.getMobHuntingManager().isHuntEnabled(event.getEntity()))
			return;

		Player killed = event.getEntity();

		HuntData data = new HuntData(killed);
		if (data.getKillstreakLevel() != 0 && data.getKillstreakMultiplier() != 1)
			plugin.getMessages().playerActionBarMessage((Player) event.getEntity(),
					ChatColor.RED + "" + ChatColor.ITALIC + Messages.getString("mobhunting.killstreak.ended"));
		data.resetKillStreak(killed);

		double playerPenalty = 0;

		if (CitizensCompat.isNPC(killed))
			return;

		EntityDamageEvent lastDamageCause = killed.getLastDamageCause();
		if (lastDamageCause instanceof EntityDamageByEntityEvent) {
			Entity damager = ((EntityDamageByEntityEvent) lastDamageCause).getDamager();
			Entity killer = null;
			LivingEntity mob = null;

			if (damager instanceof Player)
				killer = damager;
			else if (damager instanceof Projectile && ((Projectile) damager).getShooter() instanceof Player)
				killer = (Entity) ((Projectile) damager).getShooter();
			else if (damager instanceof Projectile && ((Projectile) damager).getShooter() instanceof LivingEntity)
				mob = (LivingEntity) ((Projectile) damager).getShooter();
			else if (damager instanceof LivingEntity)
				mob = (LivingEntity) damager;
			else if (damager instanceof Projectile) {
				if (((Projectile) damager).getShooter() != null)
					Messages.debug("%s was killed by a %s shot by %s", killed.getName(), damager.getName(),
							((Projectile) damager).getShooter().toString());
				else
					Messages.debug("%s was killed by a %s", killed.getName(), damager.getName());
			}

			if (mob != null) {

				Messages.debug("%s was killed by a %s", mob.getName(), damager.getName());
				if (damager instanceof Projectile)
					Messages.debug("and shooter was %s", ((Projectile) damager).getShooter().toString());

				// MobArena
				if (MobArenaCompat.isPlayingMobArena((Player) killed)
						&& !MobHunting.getConfigManager().mobarenaGetRewards) {
					Messages.debug("KillBlocked: %s was killed while playing MobArena.", killed.getName());
					return;
					// PVPArena
				} else if (PVPArenaCompat.isPlayingPVPArena((Player) killed)
						&& !MobHunting.getConfigManager().pvparenaGetRewards) {
					Messages.debug("KillBlocked: %s was killed while playing PvpArena.", killed.getName());
					return;
					// BattleArena
				} else if (BattleArenaCompat.isPlayingBattleArena((Player) killed)) {
					Messages.debug("KillBlocked: %s was killed while playing BattleArena.", killed.getName());
					return;
				}

				playerPenalty = plugin.getRewardManager().getPlayerKilledByMobPenalty(killed);
				if (playerPenalty != 0) {
					boolean killed_muted = false;
					if (plugin.getPlayerSettingsmanager().containsKey(killed))
						killed_muted = plugin.getPlayerSettingsmanager().getPlayerSettings(killed).isMuted();
					plugin.getRewardManager().withdrawPlayer(killed, playerPenalty);
					if (!killed_muted)
						plugin.getMessages().playerActionBarMessage(killed,
								ChatColor.RED + "" + ChatColor.ITALIC + Messages.getString("mobhunting.moneylost",
										"prize", plugin.getRewardManager().format(playerPenalty)));
					Messages.debug("%s lost %s for being killed by a %s", mob.getName(),
							plugin.getRewardManager().format(playerPenalty), mob.getName());
				} else {
					Messages.debug("There is NO penalty for being killed by a %s", mob.getName());
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onPlayerDamage(EntityDamageByEntityEvent event) {
		if (!(event.getEntity() instanceof Player))
			return;

		if (!MobHunting.getMobHuntingManager().isHuntEnabledInWorld(event.getEntity().getWorld())
				|| !MobHunting.getMobHuntingManager().isHuntEnabled((Player) event.getEntity()))
			return;

		Player player = (Player) event.getEntity();
		HuntData data = new HuntData(player);
		if (data.getKillstreakLevel() != 0 && data.getKillstreakMultiplier() != 1)
			plugin.getMessages().playerActionBarMessage(player,
					ChatColor.RED + "" + ChatColor.ITALIC + Messages.getString("mobhunting.killstreak.ended"));
		data.resetKillStreak(player);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onSkeletonShoot(ProjectileLaunchEvent event) {
		if (!MobHunting.getMobHuntingManager().isHuntEnabledInWorld(event.getEntity().getWorld()))
			return;

		if (event.getEntity() instanceof Arrow) {
			if (event.getEntity().getShooter() instanceof Skeleton) {
				Skeleton shooter = (Skeleton) event.getEntity().getShooter();
				if (shooter.getTarget() instanceof Player
						&& MobHunting.getMobHuntingManager().isHuntEnabled((Player) shooter.getTarget())
						&& ((Player) shooter.getTarget()).getGameMode() != GameMode.CREATIVE) {
					DamageInformation info = null;
					info = mDamageHistory.get(shooter);
					if (info == null)
						info = new DamageInformation();
					info.setTime(System.currentTimeMillis());
					info.setAttacker((Player) shooter.getTarget());
					info.setAttackerPosition(shooter.getTarget().getLocation().clone());
					mDamageHistory.put(shooter, info);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onFireballShoot(ProjectileLaunchEvent event) {
		if (!MobHunting.getMobHuntingManager().isHuntEnabledInWorld(event.getEntity().getWorld()))
			return;

		if (event.getEntity() instanceof Fireball) {
			if (event.getEntity().getShooter() instanceof Blaze) {
				Blaze blaze = (Blaze) event.getEntity().getShooter();
				if (blaze.getTarget() instanceof Player
						&& MobHunting.getMobHuntingManager().isHuntEnabled((Player) blaze.getTarget())
						&& ((Player) blaze.getTarget()).getGameMode() != GameMode.CREATIVE) {
					DamageInformation info = mDamageHistory.get(blaze);
					if (info == null)
						info = new DamageInformation();
					info.setTime(System.currentTimeMillis());
					info.setAttacker((Player) blaze.getTarget());
					info.setAttackerPosition(blaze.getTarget().getLocation().clone());
					mDamageHistory.put(blaze, info);
				}
			} else if (event.getEntity().getShooter() instanceof Wither) {
				Wither wither = (Wither) event.getEntity().getShooter();
				if (wither.getTarget() instanceof Player
						&& MobHunting.getMobHuntingManager().isHuntEnabled((Player) wither.getTarget())
						&& ((Player) wither.getTarget()).getGameMode() != GameMode.CREATIVE) {
					DamageInformation info = null;
					info = mDamageHistory.get(wither);
					if (info == null)
						info = new DamageInformation();
					info.setTime(System.currentTimeMillis());
					info.setAttacker((Player) wither.getTarget());
					info.setAttackerPosition(wither.getTarget().getLocation().clone());
					mDamageHistory.put(wither, info);
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onMobDamage(EntityDamageByEntityEvent event) {
		if (!(event.getEntity() instanceof LivingEntity)
				|| !MobHunting.getMobHuntingManager().isHuntEnabledInWorld(event.getEntity().getWorld()))
			return;// ok
		Entity damager = event.getDamager();
		Entity damaged = event.getEntity();

		// check if damager or damaged is Sentry / Sentinel. Only Sentry gives a
		// reward.
		if (CitizensCompat.isNPC(damaged) && !CitizensCompat.isSentryOrSentinelOrSentries(damaged))
			return;

		if (WorldGuardCompat.isSupported()
				&& !WorldGuardHelper.isAllowedByWorldGuard(damager, damaged, DefaultFlag.MOB_DAMAGE, true)) {
			return;
		}

		if (damager instanceof Player && (PreciousStonesCompat.isMobDamageProtected((Player) damager)
				|| PreciousStonesCompat.isPVPProtected((Player) damager)))
			return;

		if (CrackShotCompat.isSupported() && CrackShotCompat.isCrackShotUsed(damaged)) {
			return;
		}

		DamageInformation info = null;
		info = mDamageHistory.get(damaged);
		if (info == null)
			info = new DamageInformation();

		info.setTime(System.currentTimeMillis());

		Player cause = null;
		ItemStack weapon = null;

		if (damager instanceof Player) {
			cause = (Player) damager;
		}

		boolean projectile = false;
		if (damager instanceof Projectile) {
			if (((Projectile) damager).getShooter() instanceof Player)
				cause = (Player) ((Projectile) damager).getShooter();

			if (damager instanceof ThrownPotion)
				weapon = ((ThrownPotion) damager).getItem();

			info.setIsMeleWeaponUsed(false);
			projectile = true;

			if (CrackShotCompat.isCrackShotProjectile((Projectile) damager)) {
				info.setCrackShotWeapon(CrackShotCompat.getCrackShotWeapon((Projectile) damager));
			}

		} else
			info.setIsMeleWeaponUsed(true);

		if (MyPetCompat.isMyPet(damager)) {
			cause = MyPetCompat.getMyPetOwner(damaged);
			info.setIsMeleWeaponUsed(false);
			info.setIsMyPetAssist(true);
		} else if (damager instanceof Wolf && ((Wolf) damager).isTamed()
				&& ((Wolf) damager).getOwner() instanceof Player) {
			cause = (Player) ((Wolf) damager).getOwner();
			info.setIsMeleWeaponUsed(false);
			info.setIsMyPetAssist(true);
		}

		if (weapon == null && cause != null) {
			if (Misc.isMC19OrNewer() && projectile) {
				PlayerInventory pi = cause.getInventory();
				if (pi.getItemInMainHand().getType() == Material.BOW)
					weapon = pi.getItemInMainHand();
				else
					weapon = pi.getItemInOffHand();
			} else {
				weapon = cause.getItemInHand();
			}
			if (CrackShotCompat.isCrackShotWeapon(weapon)) {
				info.setCrackShotWeapon(CrackShotCompat.getCrackShotWeapon(weapon));
				Messages.debug("%s used a CrackShot weapon: %s", cause.getName(), info.getCrackShotWeaponUsed());
			}
		}

		if (weapon != null)
			info.setWeapon(weapon);

		// Take note that a weapon has been used at all
		if (info.getWeapon() != null && (Misc.isSword(info.getWeapon()) || Misc.isAxe(info.getWeapon())
				|| Misc.isPick(info.getWeapon()) || info.isCrackShotWeaponUsed() || projectile))
			info.setHasUsedWeapon(true);

		if (cause != null) {
			if (cause != info.getAttacker()) {
				info.setAssister(info.getAttacker());
				info.setLastAssistTime(info.getLastAttackTime());
			}

			info.setLastAttackTime(System.currentTimeMillis());

			info.setAttacker(cause);
			if (cause.isFlying() && !cause.isInsideVehicle())
				info.setWasFlying(true);

			info.setAttackerPosition(cause.getLocation().clone());

			if (!info.isPlayerUndercover())
				if (DisguisesHelper.isDisguised(cause)) {
					if (DisguisesHelper.isDisguisedAsAgresiveMob(cause)) {
						Messages.debug("[MobHunting] %s was under cover - diguised as an agressive mob",
								cause.getName());
						info.setPlayerUndercover(true);
					} else
						Messages.debug("[MobHunting] %s was under cover - diguised as an passive mob", cause.getName());
					if (MobHunting.getConfigManager().removeDisguiseWhenAttacking) {
						DisguisesHelper.undisguiseEntity(cause);
						// if (cause instanceof Player)
						plugin.getMessages().playerActionBarMessage(cause, ChatColor.GREEN + "" + ChatColor.ITALIC
								+ Messages.getString("bonus.undercover.message", "cause", cause.getName()));
						if (damaged instanceof Player)
							plugin.getMessages().playerActionBarMessage((Player) damaged,
									ChatColor.GREEN + "" + ChatColor.ITALIC
											+ Messages.getString("bonus.undercover.message", "cause", cause.getName()));
					}
				}

			if (!info.isMobCoverBlown())
				if (DisguisesHelper.isDisguised(damaged)) {
					if (DisguisesHelper.isDisguisedAsAgresiveMob(damaged)) {
						Messages.debug("[MobHunting] %s Cover blown, diguised as an agressive mob", damaged.getName());
						info.setMobCoverBlown(true);
					} else
						Messages.debug("[MobHunting] %s Cover Blown, diguised as an passive mob", damaged.getName());
					if (MobHunting.getConfigManager().removeDisguiseWhenAttacked) {
						DisguisesHelper.undisguiseEntity(damaged);
						if (damaged instanceof Player)
							plugin.getMessages().playerActionBarMessage((Player) damaged, ChatColor.GREEN + ""
									+ ChatColor.ITALIC
									+ Messages.getString("bonus.coverblown.message", "damaged", damaged.getName()));
						if (cause instanceof Player)
							plugin.getMessages().playerActionBarMessage(cause, ChatColor.GREEN + "" + ChatColor.ITALIC
									+ Messages.getString("bonus.coverblown.message", "damaged", damaged.getName()));
					}
				}

			mDamageHistory.put((LivingEntity) damaged, info);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	private void onMobDeath(EntityDeathEvent event) {
		LivingEntity killed = event.getEntity();

		Player killer = event.getEntity().getKiller();
		ExtendedMob mob = MobHunting.getExtendedMobManager().getExtendedMobFromEntity(killed);
		if (mob.getMob_id() == 0) {
			return;
		}

		// Grinding Farm detections
		if (MobHunting.getConfigManager().detectFarms
				&& !MobHunting.getGrindingManager().isGrindingDisabledInWorld(event.getEntity().getWorld())) {
			if (killed.getLastDamageCause() != null) {
				if (killed.getLastDamageCause().getCause() == DamageCause.FALL
						&& !MobHunting.getGrindingManager().isWhitelisted(killed.getLocation())) {
					Messages.debug("===================== Farm detection =======================");
					MobHunting.getGrindingManager().registerDeath(killed);
					if (MobHunting.getConfigManager().detectNetherGoldFarms
							&& MobHunting.getGrindingManager().isNetherGoldXPFarm(killed)) {
						MobHunting.getMobHuntingManager().cancelDrops(event,
								MobHunting.getConfigManager().disableNaturalItemDropsOnNetherGoldFarms,
								MobHunting.getConfigManager().disableNaturalXPDropsOnNetherGoldFarms);
						if (getPlayer(killer, killed) != null) {
							if ((plugin.getPlayerSettingsmanager().containsKey(getPlayer(killer, killed))
									&& plugin.getPlayerSettingsmanager().getPlayerSettings(getPlayer(killer, killed))
											.isLearningMode())
									|| getPlayer(killer, killed).hasPermission("mobhunting.blacklist")
									|| getPlayer(killer, killed).hasPermission("mobhunting.blacklist.show"))
								ProtocolLibHelper.showGrindingArea(getPlayer(killer, killed),
										new Area(killed.getLocation(),
												MobHunting.getConfigManager().rangeToSearchForGrinding,
												MobHunting.getConfigManager().numberOfDeathsWhenSearchingForGringding),
										killed.getLocation());
							plugin.getMessages().learn(getPlayer(killer, killed),
									Messages.getString("mobhunting.learn.grindingfarm"));
						}
						Messages.debug("================== Farm detection Ended (1)=================");
						return;
					}
					if (MobHunting.getConfigManager().detectOtherFarms
							&& MobHunting.getGrindingManager().isOtherFarm(killed)) {
						MobHunting.getMobHuntingManager().cancelDrops(event,
								MobHunting.getConfigManager().disableNaturalItemDropsOnOtherFarms,
								MobHunting.getConfigManager().disableNaturalXPDropsOnOtherFarms);
						if (getPlayer(killer, killed) != null) {
							if ((plugin.getPlayerSettingsmanager().containsKey(getPlayer(killer, killed))
									&& plugin.getPlayerSettingsmanager().getPlayerSettings(getPlayer(killer, killed))
											.isLearningMode())
									|| getPlayer(killer, killed).hasPermission("mobhunting.blacklist.show")
									|| getPlayer(killer, killed).hasPermission("mobhunting.blacklist"))
								ProtocolLibHelper.showGrindingArea(getPlayer(killer, killed),
										new Area(killed.getLocation(),
												MobHunting.getConfigManager().rangeToSearchForGrinding,
												MobHunting.getConfigManager().numberOfDeathsWhenSearchingForGringding),
										killed.getLocation());
							plugin.getMessages().learn(getPlayer(killer, killed),
									Messages.getString("mobhunting.learn.grindingfarm"));
						}
						return;
					}
					Messages.debug("================== Farm detection Ended (2)=================");
				}
			} else {
				// Messages.debug("The %s (%s) died without a damageCause.",
				// mob.getName(), mob.getMobPlugin().getName());
				return;
			}
		}

		DamageInformation info = mDamageHistory.get(killed);
		if (info == null) {
			info = new DamageInformation();
		}

		// Killer is not a player and not a MyPet and CrackShot not used.
		if (killer == null && !MyPetCompat.isKilledByMyPet(killed) && !info.isCrackShotWeaponUsed()) {
			return;
		}

		if (killed != null && (killed.getType() == EntityType.UNKNOWN || killed.getType() == EntityType.ARMOR_STAND)) {
			return;
		}

		Messages.debug("======================== New kill ==========================");

		// Check if the mob was killed by MyPet and assisted_kill is disabled.
		if (killer == null && MyPetCompat.isKilledByMyPet(killed)
				&& MobHunting.getConfigManager().enableAssists == false) {
			Player owner = MyPetCompat.getMyPetOwner(killed);
			Messages.debug("KillBlocked: %s - Assisted kill is disabled", owner.getName());
			plugin.getMessages().learn(owner, Messages.getString("mobhunting.learn.assisted-kill-is-disabled"));
			Messages.debug("======================= kill ended (1)======================");
			return;
		}

		// Write killer name to Server Log
		if (killer != null)
			Messages.debug("%s killed a %s (%s)@(%s:%s,%s,%s)", killer.getName(), mob.getMobName(),
					mob.getMobPlugin().getName(), killer.getWorld().getName(), (int) killer.getLocation().getBlockX(),
					(int) killer.getLocation().getBlockY(), (int) killer.getLocation().getBlockZ());
		else if (MyPetCompat.isKilledByMyPet(killed))
			Messages.debug("%s owned by %s killed a %s (%s)@(%s:%s,%s,%s)", MyPetCompat.getMyPet(killed).getName(),
					MyPetCompat.getMyPetOwner(killed).getName(), mob.getMobName(), mob.getMobPlugin().getName(),
					MyPetCompat.getMyPetOwner(killed).getWorld().getName(),
					(int) MyPetCompat.getMyPetOwner(killed).getLocation().getBlockX(),
					(int) MyPetCompat.getMyPetOwner(killed).getLocation().getBlockY(),
					(int) MyPetCompat.getMyPetOwner(killed).getLocation().getBlockZ());
		else if (info.isCrackShotWeaponUsed()) {
			if (killer == null) {
				killer = info.getCrackShotPlayer();
				if (killer != null)
					Messages.debug("%s killed a %s (%s) using a %s@(%s:%s,%s,%s)", killer.getName(), mob.getMobName(),
							mob.getMobPlugin().getName(), info.getCrackShotWeaponUsed(), killer.getWorld().getName(),
							(int) killer.getLocation().getBlockX(), (int) killer.getLocation().getBlockY(),
							(int) killer.getLocation().getBlockZ());
				else
					Messages.debug("No killer was stored in the Damageinformation");
			}
		}

		// Killer is a NPC
		if (killer != null && CitizensCompat.isNPC(killer)) {
			Messages.debug("KillBlocked: Killer is a Citizen NPC (ID:%s).", CitizensCompat.getNPCId(killer));
			Messages.debug("======================= kill ended (2)======================");
			return;
		}

		// Player killed a Stacked Mob
		if (MobStackerCompat.isStackedMob(killed)) {
			if (MobHunting.getConfigManager().getRewardFromStackedMobs) {
				if (getPlayer(killer, killed) != null) {
					Messages.debug("%s killed a stacked mob (%s) No=%s", getPlayer(killer, killed).getName(),
							mob.getMobName(), MobStackerCompat.getStackSize(killed));
					if (MobStackerCompat.killHoleStackOnDeath(killed) && MobStackerCompat.multiplyLoot()) {
						Messages.debug("Pay reward for no x mob");
					} else {
						// pay reward for one mob, if config allows
						Messages.debug("Pay reward for one mob");
					}
				}
			} else {
				Messages.debug("KillBlocked: Rewards from StackedMobs is disabled in Config.yml");
				Messages.debug("======================= kill ended (3)======================");
				return;
			}
		} else

		// Player killed a Citizens2 NPC
		if (getPlayer(killer, killed) != null && CitizensCompat.isNPC(killed)
				&& CitizensCompat.isSentryOrSentinelOrSentries(killed)) {
			Messages.debug("%s killed a Sentinel, Sentries or a Sentry npc-%s (name=%s)",
					getPlayer(killer, killed).getName(), CitizensCompat.getNPCId(killed), mob.getMobName());
		}

		// WorldGuard Compatibility
		if (WorldGuardCompat.isSupported()) {
			if ((killer != null || MyPetCompat.isMyPet(killer)) && !CitizensCompat.isNPC(killer)) {
				Player player = getPlayer(killer, killed);
				if (!WorldGuardHelper.isAllowedByWorldGuard(killer, killed, DefaultFlag.MOB_DAMAGE, true)) {
					Messages.debug("KillBlocked: %s is hiding in WG region with mob-damage=DENY", killer.getName());
					plugin.getMessages().learn(player, Messages.getString("mobhunting.learn.mob-damage-flag"));
					cancelDrops(event, MobHunting.getConfigManager().disableNaturalItemDrops,
							MobHunting.getConfigManager().disableNatualXPDrops);
					Messages.debug("======================= kill ended (4)======================");
					return;
				} else if (!WorldGuardHelper.isAllowedByWorldGuard(killer, killed, WorldGuardHelper.getMobHuntingFlag(),
						true)) {
					Messages.debug("KillBlocked: %s is in a protected region mobhunting=DENY", killer.getName());
					plugin.getMessages().learn(player, Messages.getString("mobhunting.learn.mobhunting-deny"));
					cancelDrops(event, MobHunting.getConfigManager().disableNaturalItemDrops,
							MobHunting.getConfigManager().disableNatualXPDrops);
					Messages.debug("======================= kill ended (5)======================");
					return;
				}
			}
		}

		if (PreciousStonesCompat.isMobDamageProtected(getPlayer(killer, killed))) {
			Player player = getPlayer(killer, killed);
			Messages.debug("KillBlocked: %s is hiding in PreciousStone Field with prevent-mob-damage flag",
					player.getName());
			plugin.getMessages().learn(player, Messages.getString("mobhunting.learn.prevent-mob-damage-flag"));
			cancelDrops(event, MobHunting.getConfigManager().disableNaturalItemDrops,
					MobHunting.getConfigManager().disableNatualXPDrops);
			Messages.debug("======================= kill ended (5.5)======================");
			return;
		}

		// Factions Compatibility - no reward when player are in SafeZone
		if (FactionsCompat.isSupported()) {
			if ((killer != null || MyPetCompat.isMyPet(killer)) && !CitizensCompat.isNPC(killer)) {
				Player player = getPlayer(killer, killed);
				if (FactionsCompat.isInSafeZone(player)) {
					Messages.debug("KillBlocked: %s is hiding in Factions SafeZone", player.getName());
					plugin.getMessages().learn(getPlayer(killer, killed),
							Messages.getString("mobhunting.learn.factions-no-rewards-in-safezone"));
					cancelDrops(event, MobHunting.getConfigManager().disableNaturalItemDrops,
							MobHunting.getConfigManager().disableNatualXPDrops);
					Messages.debug("======================= kill ended (6)======================");
					return;
				}
			}
		}

		// Towny Compatibility - no reward when player are in a protected town
		if (TownyCompat.isSupported()) {
			if ((killer != null || MyPetCompat.isMyPet(killer)) && !CitizensCompat.isNPC(killer)
					&& !(killed instanceof Player)) {
				Player player = getPlayer(killer, killed);
				if (MobHunting.getConfigManager().disableRewardsInHomeTown && TownyCompat.isInHomeTome(player)) {
					Messages.debug("KillBlocked: %s is hiding in his home town", player.getName());
					plugin.getMessages().learn(getPlayer(killer, killed),
							Messages.getString("mobhunting.learn.towny-no-rewards-in-home-town"));
					cancelDrops(event, MobHunting.getConfigManager().disableNaturallyRewardsInHomeTown,
							MobHunting.getConfigManager().disableNaturallyRewardsInHomeTown);
					Messages.debug("======================= kill ended (7)======================");
					return;
				}
			}
		}

		// Residence Compatibility - no reward when player are in a protected
		// residence
		if (ResidenceCompat.isSupported()) {
			if ((killer != null || MyPetCompat.isMyPet(killer)) && !CitizensCompat.isNPC(killer)
					&& !(killed instanceof Player)) {
				Player player = getPlayer(killer, killed);
				if (MobHunting.getConfigManager().disableRewardsInHomeResidence
						&& ResidenceCompat.isProtected(player)) {
					Messages.debug("KillBlocked: %s is hiding in a protected residence", player.getName());
					plugin.getMessages().learn(getPlayer(killer, killed),
							Messages.getString("mobhunting.learn.residence-no-rewards-in-protected-area"));
					cancelDrops(event, MobHunting.getConfigManager().disableNaturallyRewardsInProtectedResidence,
							MobHunting.getConfigManager().disableNaturallyRewardsInProtectedResidence);
					Messages.debug("======================= kill ended (8)======================");
					return;
				}
			}
		}

		// MobHunting is Disabled in World
		if (!MobHunting.getMobHuntingManager().isHuntEnabledInWorld(event.getEntity().getWorld())) {
			if (WorldGuardCompat.isSupported()) {
				if (!CitizensCompat.isNPC(killer)) {
					if (WorldGuardHelper.isAllowedByWorldGuard(killer, killed, WorldGuardHelper.getMobHuntingFlag(),
							false)) {
						Messages.debug("KillBlocked %s: Mobhunting disabled in world '%s'",
								getPlayer(killer, killed).getName(), killer.getWorld().getName());
						plugin.getMessages().learn(getPlayer(killer, killed),
								Messages.getString("mobhunting.learn.disabled"));
						Messages.debug("======================= kill ended (9)======================");
						return;
					} else {
						Messages.debug("KillBlocked %s: Mobhunting disabled in world '%s'",
								getPlayer(killer, killed).getName(), killer.getWorld().getName());
						plugin.getMessages().learn(getPlayer(killer, killed),
								Messages.getString("mobhunting.learn.disabled"));
						Messages.debug("======================= kill ended (10)======================");
						return;
					}
				} else {
					Messages.debug("KillBlocked: killer is null and killer was not a MyPet or NPC Sentinel Guard.");
					Messages.debug("======================= kill ended (11)=====================");
					return;
				}
			} else {
				// MobHunting is NOT allowed in this world,
				Messages.debug("KillBlocked %s: Mobhunting disabled in world '%s'", getPlayer(killer, killed).getName(),
						getPlayer(killer, killed).getWorld().getName());
				plugin.getMessages().learn(getPlayer(killer, killed), Messages.getString("mobhunting.learn.disabled"));
				Messages.debug("======================= kill ended (12)=====================");
				return;
			}
		}

		// Handle Muted mode
		boolean killer_muted = false;
		boolean killed_muted = false;
		if (getPlayer(killer, killed) instanceof Player
				&& plugin.getPlayerSettingsmanager().containsKey((Player) getPlayer(killer, killed)))
			killer_muted = plugin.getPlayerSettingsmanager().getPlayerSettings(getPlayer(killer, killed)).isMuted();
		if (killed instanceof Player && plugin.getPlayerSettingsmanager().containsKey((Player) killed))
			killed_muted = plugin.getPlayerSettingsmanager().getPlayerSettings((Player) killed).isMuted();

		// Player died while playing a Minigame: MobArena, PVPArena,
		// BattleArena, Suicide, PVP, penalty when Mobs kills player
		if (killed instanceof Player) {
			// MobArena
			if (MobArenaCompat.isPlayingMobArena((Player) killed)
					&& !MobHunting.getConfigManager().mobarenaGetRewards) {
				Messages.debug("KillBlocked: %s was killed while playing MobArena.", mob.getMobName());
				plugin.getMessages().learn(getPlayer(killer, killed), Messages.getString("mobhunting.learn.mobarena"));
				Messages.debug("======================= kill ended (13)=====================");
				return;

				// PVPArena
			} else if (PVPArenaCompat.isPlayingPVPArena((Player) killed)
					&& !MobHunting.getConfigManager().pvparenaGetRewards) {
				Messages.debug("KillBlocked: %s was killed while playing PvpArena.", mob.getMobName());
				plugin.getMessages().learn(getPlayer(killer, killed), Messages.getString("mobhunting.learn.pvparena"));
				Messages.debug("======================= kill ended (14)=====================");
				return;

				// BattleArena
			} else if (BattleArenaCompat.isPlayingBattleArena((Player) killed)) {
				Messages.debug("KillBlocked: %s was killed while playing BattleArena.", mob.getMobName());
				plugin.getMessages().learn(getPlayer(killer, killed),
						Messages.getString("mobhunting.learn.battlearena"));
				Messages.debug("======================= kill ended (15)=====================");
				return;

				// MiniGamesLib
			} else if (MinigamesLibCompat.isPlayingMinigame((Player) killed)) {
				Messages.debug("KillBlocked: %s was killed while playing a MiniGame.", mob.getMobName());
				plugin.getMessages().learn(getPlayer(killer, killed),
						Messages.getString("mobhunting.learn.minigameslib"));
				Messages.debug("======================= kill ended (16)=====================");
				return;

				//
			} else if (PreciousStonesCompat.isPVPProtected(getPlayer(killer, killed))) {
				Player player = getPlayer(killer, killed);
				Messages.debug("KillBlocked: %s is hiding in PreciousStone Field with prevent-pvp flag",
						player.getName());
				plugin.getMessages().learn(player, Messages.getString("mobhunting.learn.prevent-pvp-flag"));
				Messages.debug("======================= kill ended (16.5)======================");
				return;
			} else if (killer != null) {
				if (killed.equals(killer)) {
					// Suicide
					plugin.getMessages().learn(getPlayer(killer, killed),
							Messages.getString("mobhunting.learn.suiside"));
					Messages.debug("KillBlocked: Suiside not allowed (Killer=%s, Killed=%s)", killer.getName(),
							killed.getName());
					Messages.debug("======================= kill ended (17)======================");
					return;
					// PVP
				} else if (!MobHunting.getConfigManager().pvpAllowed) {
					// PVP
					plugin.getMessages().learn(getPlayer(killer, killed), Messages.getString("mobhunting.learn.nopvp"));
					Messages.debug("KillBlocked: Rewards for PVP kill is not allowed in config.yml. %s killed %s.",
							getPlayer(killer, killed).getName(), mob.getMobName());
					Messages.debug("======================= kill ended (18)=====================");
					return;
				}
			}
		}

		// Player killed a mob while playing a minigame: MobArena, PVPVArena,
		// BattleArena
		// Player is in Godmode or Vanished
		// Player permission to Hunt (and get rewards)
		if (MobArenaCompat.isPlayingMobArena(getPlayer(killer, killed))
				&& !MobHunting.getConfigManager().mobarenaGetRewards) {
			Messages.debug("KillBlocked: %s is currently playing MobArena.", getPlayer(killer, killed).getName());
			plugin.getMessages().learn(getPlayer(killer, killed), Messages.getString("mobhunting.learn.mobarena"));
			Messages.debug("======================= kill ended (19)=====================");
			return;
		} else if (PVPArenaCompat.isPlayingPVPArena(getPlayer(killer, killed))
				&& !MobHunting.getConfigManager().pvparenaGetRewards) {
			Messages.debug("KillBlocked: %s is currently playing PvpArena.", getPlayer(killer, killed).getName());
			plugin.getMessages().learn(getPlayer(killer, killed), Messages.getString("mobhunting.learn.pvparena"));
			Messages.debug("======================= kill ended (20)=====================");
			return;
		} else if (BattleArenaCompat.isPlayingBattleArena(getPlayer(killer, killed))) {
			Messages.debug("KillBlocked: %s is currently playing BattleArena.", getPlayer(killer, killed).getName());
			plugin.getMessages().learn(getPlayer(killer, killed), Messages.getString("mobhunting.learn.battlearena"));
			Messages.debug("======================= kill ended (21)=====================");
			return;
		} else if (EssentialsCompat.isGodModeEnabled(getPlayer(killer, killed))) {
			Messages.debug("KillBlocked: %s is in God mode", getPlayer(killer, killed).getName());
			plugin.getMessages().learn(getPlayer(killer, killed), Messages.getString("mobhunting.learn.godmode"));
			cancelDrops(event, MobHunting.getConfigManager().disableNaturalItemDrops,
					MobHunting.getConfigManager().disableNatualXPDrops);
			Messages.debug("======================= kill ended (22)=====================");
			return;
		} else if (EssentialsCompat.isVanishedModeEnabled(getPlayer(killer, killed))) {
			Messages.debug("KillBlocked: %s is in Vanished mode", getPlayer(killer, killed).getName());
			plugin.getMessages().learn(getPlayer(killer, killed), Messages.getString("mobhunting.learn.vanished"));
			Messages.debug("======================= kill ended (23)=====================");
			return;
		} else if (VanishNoPacketCompat.isVanishedModeEnabled(getPlayer(killer, killed))) {
			Messages.debug("KillBlocked: %s is in Vanished mode", getPlayer(killer, killed).getName());
			plugin.getMessages().learn(getPlayer(killer, killed), Messages.getString("mobhunting.learn.vanished"));
			Messages.debug("======================= kill ended (24)=====================");
			return;
		}

		if (!MobHunting.getMobHuntingManager().hasPermissionToKillMob(getPlayer(killer, killed), killed)) {
			Messages.debug("KillBlocked: %s has not permission to kill %s.", getPlayer(killer, killed).getName(),
					mob.getMobName());
			plugin.getMessages().learn(getPlayer(killer, killed),
					Messages.getString("mobhunting.learn.no-permission", "killed-mob", mob.getMobName()));
			Messages.debug("======================= kill ended (25)=====================");
			return;
		}

		// Mob Spawner / Egg / Egg Dispenser detection
		if (event.getEntity().hasMetadata(SPAWNER_BLOCKED)) {
			if (!MobHunting.getGrindingManager().isWhitelisted(event.getEntity().getLocation())) {
				if (killed != null) {
					Messages.debug(
							"KillBlocked %s(%d): Mob has MH:blocked meta (probably spawned from a mob spawner, an egg or a egg-dispenser )",
							event.getEntity().getType(), killed.getEntityId());
					plugin.getMessages().learn(getPlayer(killer, killed),
							Messages.getString("mobhunting.learn.mobspawner", "killed", mob.getMobName()));
					cancelDrops(event,
							MobHunting.getConfigManager().disableNaturallyDroppedItemsFromMobSpawnersEggsAndDispensers,
							MobHunting.getConfigManager().disableNaturallyDroppedXPFromMobSpawnersEggsAndDispensers);
				}
				Messages.debug("======================= kill ended (26)======================");
				return;
			}
		}

		// MobHunting is disabled for the player
		if (!MobHunting.getMobHuntingManager().isHuntEnabled(getPlayer(killer, killed))) {
			Messages.debug("KillBlocked: %s Hunting is disabled for player", getPlayer(killer, killed).getName());
			plugin.getMessages().learn(getPlayer(killer, killed), Messages.getString("mobhunting.learn.huntdisabled"));
			Messages.debug("======================= kill ended (27)======================");
			return;
		}

		// The player is in Creative mode
		if (getPlayer(killer, killed).getGameMode() == GameMode.CREATIVE) {
			Messages.debug("KillBlocked: %s is in creative mode", getPlayer(killer, killed).getName());
			plugin.getMessages().learn(getPlayer(killer, killed), Messages.getString("mobhunting.learn.creative"));
			cancelDrops(event, MobHunting.getConfigManager().tryToCancelNaturalDropsWhenInCreative,
					MobHunting.getConfigManager().tryToCancelXPDropsWhenInCreative);
			Messages.debug("======================= kill ended (28)======================");
			return;
		}

		// Calculate basic the reward
		double cash = plugin.getRewardManager().getBaseKillPrize(killed);
		double basic_prize = cash;
		Messages.debug("Basic Prize=%s for killing a %s", plugin.getRewardManager().format(cash), mob.getMobName());

		// There is no reward and no penalty for this kill
		if (basic_prize == 0 && plugin.getRewardManager().getKillConsoleCmd(killed).equals("")) {
			Messages.debug(
					"KillBlocked %s(%d): There is no reward and no penalty for this Mob/Player and is not counted as kill/achievement.",
					mob.getMobName(), killed.getEntityId());
			plugin.getMessages().learn(getPlayer(killer, killed),
					Messages.getString("mobhunting.learn.no-reward", "killed", mob.getMobName()));
			Messages.debug("======================= kill ended (29)=====================");
			return;
		}

		// Update DamageInformation
		if (killed instanceof LivingEntity && mDamageHistory.containsKey((LivingEntity) killed)) {
			info = mDamageHistory.get(killed);
			if (System.currentTimeMillis() - info.getTime() > MobHunting.getConfigManager().assistTimeout * 1000)
				info = null;
			// else
			// else if (killer == null)
			// killer = info.getAttacker();
		}
		if (info == null) {
			info = new DamageInformation();
			info.setTime(System.currentTimeMillis());
			info.setLastAttackTime(info.getTime());
			if (killer != null) {
				info.setAttacker(getPlayer(killer, killed));
				info.setAttackerPosition(getPlayer(killer, killed).getLocation());
				ItemStack weapon = killer.getItemInHand();
				if (!weapon.equals(new ItemStack(Material.AIR))) {
					info.setHasUsedWeapon(true);
					if (CrackShotCompat.isCrackShotWeapon(weapon)) {
						info.setCrackShotWeapon(CrackShotCompat.getCrackShotWeapon(weapon));
						Messages.debug("%s used a CrackShot weapon: %s", killer.getName(),
								CrackShotCompat.getCrackShotWeapon(weapon));
					}
				}
			}
		}

		// Check if the kill was within the time limit on both kills and
		// assisted kills
		if (((System.currentTimeMillis() - info.getLastAttackTime()) > MobHunting.getConfigManager().killTimeout * 1000)
				&& (info.isWolfAssist() && ((System.currentTimeMillis()
						- info.getLastAttackTime()) > MobHunting.getConfigManager().assistTimeout * 1000))) {
			Messages.debug("KillBlocked %s: Last damage was too long ago (%s sec.)",
					getPlayer(killer, killed).getName(),
					(System.currentTimeMillis() - info.getLastAttackTime()) / 1000);
			Messages.debug("======================= kill ended (30)=====================");
			return;
		}

		// MyPet killed a mob - Assister is the Owner
		if (MyPetCompat.isKilledByMyPet(killed) && MobHunting.getConfigManager().enableAssists == true) {
			info.setAssister(MyPetCompat.getMyPetOwner(killed));
			Messages.debug("MyPetAssistedKill: Pet owned by %s killed a %s", info.getAssister().getName(),
					mob.getMobName());
		}

		if (info.getWeapon() == null)
			info.setWeapon(new ItemStack(Material.AIR));

		// Player or killed Mob is disguised
		if (!info.isPlayerUndercover())
			if (DisguisesHelper.isDisguised(getPlayer(killer, killed))) {
				if (DisguisesHelper.isDisguisedAsAgresiveMob(getPlayer(killer, killed))) {
					info.setPlayerUndercover(true);
				} else if (MobHunting.getConfigManager().removeDisguiseWhenAttacking) {
					DisguisesHelper.undisguiseEntity(getPlayer(killer, killed));
					if (getPlayer(killer, killed) != null && !killer_muted)
						plugin.getMessages().playerActionBarMessage(getPlayer(killer, killed),
								ChatColor.GREEN + "" + ChatColor.ITALIC + Messages.getString("bonus.undercover.message",
										"cause", getPlayer(killer, killed).getName()));
					if (killed instanceof Player && !killed_muted)
						plugin.getMessages().playerActionBarMessage((Player) killed,
								ChatColor.GREEN + "" + ChatColor.ITALIC + Messages.getString("bonus.undercover.message",
										"cause", getPlayer(killer, killed).getName()));
				}
			}
		if (!info.isMobCoverBlown())
			if (DisguisesHelper.isDisguised(killed)) {
				if (DisguisesHelper.isDisguisedAsAgresiveMob(killed)) {
					info.setMobCoverBlown(true);
				}
				if (MobHunting.getConfigManager().removeDisguiseWhenAttacked) {
					DisguisesHelper.undisguiseEntity(killed);
					if (killed instanceof Player && !killed_muted)
						plugin.getMessages().playerActionBarMessage((Player) killed,
								ChatColor.GREEN + "" + ChatColor.ITALIC
										+ Messages.getString("bonus.coverblown.message", "damaged", mob.getMobName()));
					if (getPlayer(killer, killed) != null && !killer_muted)
						plugin.getMessages()
								.playerActionBarMessage(getPlayer(killer, killed), ChatColor.GREEN + ""
										+ ChatColor.ITALIC + Messages.getString("bonus.coverblown.message", "damaged",
												mob.getMobName()));
				}
			}

		HuntData data = new HuntData(getPlayer(killer, killed));
		if (getPlayer(killer, killed) != null) {
			if (cash != 0 && (!MobHunting.getGrindingManager().isGrindingArea(getPlayer(killer, killed).getLocation())
					|| MobHunting.getGrindingManager().isWhitelisted(getPlayer(killer, killed).getLocation()))) {
				// Killstreak
				data.handleKillstreak(plugin, getPlayer(killer, killed));
			} else {
				// Killstreak ended. Players started to kill 4 chicken and the
				// one mob to gain 4 x prize
				if (data.getKillstreakLevel() != 0 && data.getKillstreakMultiplier() != 1)
					plugin.getMessages().playerActionBarMessage(getPlayer(killer, killed),
							ChatColor.RED + "" + ChatColor.ITALIC + Messages.getString("mobhunting.killstreak.ended"));
				data.resetKillStreak(getPlayer(killer, killed));
			}
		} else {
			Messages.debug("======================= kill ended (31)=====================");
			return;
		}

		// Record kills that are still within a small area
		Location loc = killed.getLocation();

		// Grinding detection
		if (cash != 0 && !plugin.getRewardManager().getKillConsoleCmd(killed).equals("")
				&& MobHunting.getConfigManager().grindingDetectionEnabled) {
			// Check if the location is marked as a Grinding Area. Whitelist
			// overrules blacklist.

			Area detectedGrindingArea = MobHunting.getGrindingManager().getGrindingArea(loc);
			if (detectedGrindingArea == null)
				// Check if Players HuntData contains this Grinding Area.
				detectedGrindingArea = data.getPlayerSpecificGrindingArea(loc);
			else {
				if (!MobHunting.getGrindingManager().isWhitelisted(detectedGrindingArea.getCenter())) {
					if (MobHunting.getGrindingManager().isGrindingArea(detectedGrindingArea.getCenter()))
						if (plugin.getPlayerSettingsmanager().getPlayerSettings(killer).isLearningMode()
								|| getPlayer(killer, killed).hasPermission("mobhunting.blacklist")
								|| getPlayer(killer, killed).hasPermission("mobhunting.blacklist.show"))
							ProtocolLibHelper.showGrindingArea(killer, detectedGrindingArea, killed.getLocation());
					plugin.getMessages().learn(getPlayer(killer, killed),
							Messages.getString("mobhunting.learn.grindingnotallowed"));
					Messages.debug("======================= kill ended (32)=====================");
					return;
				}
			}

			if (!MobHunting.getGrindingManager().isWhitelisted(loc)) {
				// Slimes ang Magmacubes are except from grinding due to their
				// splitting nature
				if (!(event.getEntity() instanceof Slime || event.getEntity() instanceof MagmaCube)
						&& !killed.hasMetadata("MH:reinforcement")) {
					Messages.debug("Checking if player is grinding within a range of %s blocks",
							data.getcDampnerRange());

					if (detectedGrindingArea != null) {
						data.setLastKillAreaCenter(null);
						data.setDampenedKills(data.getDampenedKills() + 1);
						if (data.getDampenedKills() >= MobHunting.getConfigManager().grindingDetectionNumberOfDeath) {
							if (MobHunting.getConfigManager().blacklistPlayerGrindingSpotsServerWorldWide)
								MobHunting.getGrindingManager().registerKnownGrindingSpot(detectedGrindingArea);
							cancelDrops(event, MobHunting.getConfigManager().disableNaturalItemDropsOnPlayerGrinding,
									MobHunting.getConfigManager().disableNaturalXPDropsOnPlayerGrinding);
							Messages.debug(
									"DampenedKills reached the limit %s, no rewards paid. Grinding Spot registered.",
									MobHunting.getConfigManager().grindingDetectionNumberOfDeath);
							if (plugin.getPlayerSettingsmanager().getPlayerSettings(getPlayer(killer, killed))
									.isLearningMode() || getPlayer(killer, killed).hasPermission("mobhunting.blacklist")
									|| getPlayer(killer, killed).hasPermission("mobhunting.blacklist.show"))
								ProtocolLibHelper.showGrindingArea(getPlayer(killer, killed), detectedGrindingArea,
										loc);
							plugin.getMessages().learn(getPlayer(killer, killed),
									Messages.getString("mobhunting.learn.grindingnotallowed"));
							Messages.debug("======================= kill ended (33)======================");
							return;
						} else {
							Messages.debug("DampendKills=%s", data.getDampenedKills());
						}
					} else {
						if (data.getLastKillAreaCenter() != null) {
							if (loc.getWorld().equals(data.getLastKillAreaCenter().getWorld())) {
								if (loc.distance(data.getLastKillAreaCenter()) < data.getcDampnerRange()
										&& !MobHunting.getGrindingManager().isWhitelisted(loc)) {
									if (!MobStackerCompat.isSupported() || (MobStackerCompat.isStackedMob(killed)
											&& !MobStackerCompat.isGrindingStackedMobsAllowed())) {
										data.setDampenedKills(data.getDampenedKills() + 1);
										Messages.debug("DampendKills=%s", data.getDampenedKills());
										if (data.getDampenedKills() >= MobHunting
												.getConfigManager().grindingDetectionNumberOfDeath / 2) {
											Messages.debug(
													"Warning: Grinding detected. Killings too close, adding 1 to DampenedKills.");
											plugin.getMessages().learn(getPlayer(killer, killed),
													Messages.getString("mobhunting.learn.grindingnotallowed"));
											plugin.getMessages().playerActionBarMessage(getPlayer(killer, killed),
													ChatColor.RED + Messages.getString("mobhunting.grinding.detected"));
											data.recordGrindingArea();
											cancelDrops(event, MobHunting.getConfigManager().disableNaturalItemDrops,
													MobHunting.getConfigManager().disableNatualXPDrops);
										}
									}
								} else {
									data.setLastKillAreaCenter(loc.clone());
									Messages.debug(
											"Kill not within %s blocks from previous kill. DampendKills reset to 0",
											data.getcDampnerRange());
									data.setDampenedKills(0);
								}
							} else {
								data.setLastKillAreaCenter(loc.clone());
								Messages.debug("Kill in new world. DampendKills reset to 0");
								data.setDampenedKills(0);
							}
						} else {
							data.setLastKillAreaCenter(loc.clone());
							Messages.debug("Last Kill Area Center was null. DampendKills reset to 0");
							data.setDampenedKills(0);
						}
					}
				}

				if (data.getDampenedKills() > MobHunting.getConfigManager().grindingDetectionNumberOfDeath / 2 + 4
						&& !MobHunting.getGrindingManager().isWhitelisted(loc)) {
					if (data.getKillstreakLevel() != 0 && data.getKillstreakMultiplier() != 1)
						plugin.getMessages().playerActionBarMessage(getPlayer(killer, killed),
								ChatColor.RED + Messages.getString("mobhunting.killstreak.lost"));
					Messages.debug("KillStreak reset to 0");
					data.setKillStreak(0);
				}
				data.putHuntDataToPlayer(getPlayer(killer, killed));
			}
		}

		// Apply the modifiers to Basic reward
		EntityDamageByEntityEvent lastDamageCause = null;
		if (killed.getLastDamageCause() instanceof EntityDamageByEntityEvent)
			lastDamageCause = (EntityDamageByEntityEvent) killed.getLastDamageCause();
		double multipliers = 1.0;
		ArrayList<String> modifiers = new ArrayList<String>();
		// only add modifiers if the killer is the player.
		for (IModifier mod : mHuntingModifiers) {
			if (mod.doesApply(killed, getPlayer(killer, killed), data, info, lastDamageCause)) {
				double amt = mod.getMultiplier(killed, getPlayer(killer, killed), data, info, lastDamageCause);
				if (amt != 1.0) {
					modifiers.add(mod.getName());
					multipliers *= amt;
					data.addModifier(mod.getName(), amt);
					Messages.debug("Multiplier: %s = %s", mod.getName(), amt);
				}
			}
		}
		data.setReward(cash);
		data.putHuntDataToPlayer(getPlayer(killer, killed));

		Messages.debug("Killstreak=%s, level=%s, multiplier=%s ", data.getKillStreak(), data.getKillstreakLevel(),
				data.getKillstreakMultiplier());
		multipliers *= data.getKillstreakMultiplier();

		String extraString = "";

		// Only display the multiplier if its not 1
		if (Math.abs(multipliers - 1) > 0.05)
			extraString += String.format("x%.1f", multipliers);

		// Add on modifiers
		for (String modifier : modifiers)
			extraString += ChatColor.WHITE + " * " + modifier;

		cash *= multipliers;

		cash = Misc.ceil(cash);

		// Handle Bounty Kills
		double reward = 0;
		if (!MobHunting.getConfigManager().disablePlayerBounties && killed instanceof Player) {
			Messages.debug("This was a Pvp kill (killed=%s), number of bounties=%s", killed.getName(),
					plugin.getBountyManager().getAllBounties().size());
			OfflinePlayer wantedPlayer = (OfflinePlayer) killed;
			String worldGroupName = MobHunting.getWorldGroupManager().getCurrentWorldGroup(getPlayer(killer, killed));
			if (plugin.getBountyManager().hasOpenBounties(wantedPlayer)) {
				BountyKillEvent bountyEvent = new BountyKillEvent(worldGroupName, getPlayer(killer, killed),
						wantedPlayer, plugin.getBountyManager().getOpenBounties(worldGroupName, wantedPlayer));
				Bukkit.getPluginManager().callEvent(bountyEvent);
				if (bountyEvent.isCancelled()) {
					Messages.debug("KillBlocked %s: BountyKillEvent was cancelled",
							(killer != null ? killer : info.getAssister()).getName());
					Messages.debug("======================= kill ended (34)=====================");
					return;
				}
				Set<Bounty> bounties = plugin.getBountyManager().getOpenBounties(worldGroupName, wantedPlayer);
				for (Bounty b : bounties) {
					reward += b.getPrize();
					OfflinePlayer bountyOwner = b.getBountyOwner();
					plugin.getBountyManager().delete(b);
					if (bountyOwner != null && bountyOwner.isOnline())
						plugin.getMessages().playerActionBarMessage(Misc.getOnlinePlayer(bountyOwner),
								Messages.getString("mobhunting.bounty.bounty-claimed", "killer",
										getPlayer(killer, killed).getName(), "prize",
										plugin.getRewardManager().format(b.getPrize()), "killed", killed.getName()));
					b.setStatus(BountyStatus.completed);
					MobHunting.getDataStoreManager().updateBounty(b);
				}
				plugin.getMessages().playerActionBarMessage(getPlayer(killer, killed),
						Messages.getString("mobhunting.moneygain-for-killing", "money",
								plugin.getRewardManager().format(reward), "killed", killed.getName()));
				Messages.debug("%s got %s for killing %s", getPlayer(killer, killed).getName(), reward,
						killed.getName());
				plugin.getRewardManager().depositPlayer(getPlayer(killer, killed), reward);
				// Messages.debug("RecordCash: %s killed a %s (%s) Cash=%s",
				// killer.getName(), mob.getName(),
				// mob.getMobPlugin().name(), cash);
				// MobHunting.getDataStoreManager().recordCash(killer, mob,
				// killed.hasMetadata("MH:hasBonus"), cash);

			} else {
				Messages.debug("There is no Bounty on %s", killed.getName());
			}
		}

		// Check if there is a reward for this kill
		if (cash >= MobHunting.getConfigManager().minimumReward || cash <= -MobHunting.getConfigManager().minimumReward
				|| !plugin.getRewardManager().getKillConsoleCmd(killed).isEmpty() || (killer != null
						&& McMMOCompat.isSupported() && MobHunting.getConfigManager().enableMcMMOLevelRewards)) {

			// Handle MobHuntKillEvent and Record Hunt Achievement is done using
			// EighthsHuntAchievement.java (onKillCompleted)
			MobHuntKillEvent event2 = new MobHuntKillEvent(data, info, killed, getPlayer(killer, killed));
			Bukkit.getPluginManager().callEvent(event2);
			// Check if Event is cancelled before paying the reward
			if (event2.isCancelled()) {
				Messages.debug("KillBlocked %s: MobHuntKillEvent was cancelled", getPlayer(killer, killed).getName());
				Messages.debug("======================= kill ended (35)=====================");
				return;
			}

			// Record the kill in the Database
			if (info.getAssister() == null || MobHunting.getConfigManager().enableAssists == false) {
				Messages.debug("RecordKill: %s killed a %s (%s) Cash=%s", getPlayer(killer, killed).getName(),
						mob.getMobName(), mob.getMobPlugin().name(), plugin.getRewardManager().format(cash));
				MobHunting.getDataStoreManager().recordKill(getPlayer(killer, killed), mob,
						killed.hasMetadata("MH:hasBonus"), cash);
			} else {
				Messages.debug("RecordAssistedKill: %s killed a %s (%s) Cash=%s",
						getPlayer(killer, killed).getName() + "/" + info.getAssister().getName(), mob.getMobName(),
						mob.getMobPlugin().name(), plugin.getRewardManager().format(cash));
				MobHunting.getDataStoreManager().recordAssist(getPlayer(killer, killed), killer, mob,
						killed.hasMetadata("MH:hasBonus"), cash);
			}
		} else {
			Messages.debug("KillBlocked %s: There is now reward for killing a %s", getPlayer(killer, killed).getName(),
					mob.getMobName());
			Messages.debug("======================= kill ended (36)=====================");
			return;
		}

		// Pay the money reward to player and assister
		if ((cash >= MobHunting.getConfigManager().minimumReward)
				|| (cash <= -MobHunting.getConfigManager().minimumReward)) {

			// Handle reward on PVP kill. (Robbing)
			boolean robbing = killer != null && killed instanceof Player && !CitizensCompat.isNPC(killed)
					&& MobHunting.getConfigManager().pvpAllowed && MobHunting.getConfigManager().robFromVictim;
			if (robbing) {
				plugin.getRewardManager().withdrawPlayer((Player) killed, cash);
				// Messages.debug("RecordCash: %s killed a %s (%s) Cash=%s",
				// killer.getName(), mob.getName(),
				// mob.getMobPlugin().name(), cash);
				// MobHunting.getDataStoreManager().recordCash(killer, mob,
				// killed.hasMetadata("MH:hasBonus"), -cash);
				if (!killed_muted)
					killed.sendMessage(ChatColor.RED + "" + ChatColor.ITALIC + Messages
							.getString("mobhunting.moneylost", "prize", plugin.getRewardManager().format(cash)));
				Messages.debug("%s lost %s", killed.getName(), plugin.getRewardManager().format(cash));
			}

			// Reward/Penalty for assisted kill
			if (info.getAssister() == null || MobHunting.getConfigManager().enableAssists == false) {
				if (cash >= MobHunting.getConfigManager().minimumReward) {
					if (MobHunting.getConfigManager().dropMoneyOnGroup) {
						plugin.getRewardManager().dropMoneyOnGround(killer, killed, killed.getLocation(), cash);
					} else {
						plugin.getRewardManager().depositPlayer(killer, cash);
						// Messages.debug("RecordCash: %s killed a %s (%s)
						// Cash=%s", killer.getName(), mob.getName(),
						// mob.getMobPlugin().name(), cash);
						// MobHunting.getDataStoreManager().recordCash(killer,
						// mob, killed.hasMetadata("MH:hasBonus"), cash);
						Messages.debug("%s got a reward (%s)", killer.getName(),
								plugin.getRewardManager().format(cash));
					}
				} else if (cash <= -MobHunting.getConfigManager().minimumReward) {
					plugin.getRewardManager().withdrawPlayer(killer, -cash);
					// Messages.debug("RecordCash: %s killed a %s (%s) Cash=%s",
					// killer.getName(), mob.getName(),
					// mob.getMobPlugin().name(), cash);
					// MobHunting.getDataStoreManager().recordCash(killer, mob,
					// killed.hasMetadata("MH:hasBonus"), cash);
					Messages.debug("%s got a penalty (%s)", killer.getName(), plugin.getRewardManager().format(cash));
				}
			} else {
				cash = cash / 2;
				if (cash >= MobHunting.getConfigManager().minimumReward) {
					if (MobHunting.getConfigManager().dropMoneyOnGroup) {
						if (MyPetCompat.isKilledByMyPet(killed))
							Messages.debug("1)%s was assisted by %s. Reward/Penalty is only ½ (%s)",
									getPlayer(killer, killed).getName(), MyPetCompat.getMyPet(killed).getName(),
									plugin.getRewardManager().format(cash));
						else if (CitizensCompat.isNPC(killer))
							Messages.debug("2)%s was assisted by %s. Reward/Penalty is only ½ (%s)",
									getPlayer(killer, killed).getName(), killer.getName(),
									plugin.getRewardManager().format(cash));
						else
							Messages.debug("3)%s was assisted by %s. Reward/Penalty is only ½ (%s)",
									getPlayer(killer, killed).getName(), getKillerName(killer, killed),
									plugin.getRewardManager().format(cash));
						plugin.getRewardManager().dropMoneyOnGround(getPlayer(killer, killed), killed,
								killed.getLocation(), cash);
					} else {
						plugin.getRewardManager().depositPlayer(info.getAssister(), cash);

						if (!MyPetCompat.isKilledByMyPet(killed) && !CitizensCompat.isNPC(killer))
							onAssist(getPlayer(killer, killed), killer, killed, info.getLastAssistTime());
						Messages.debug("%s was assisted by %s. Reward/Penalty is only ½ (%s)",
								getPlayer(killer, killed).getName(), getKillerName(killer, killed),
								plugin.getRewardManager().format(cash));
					}
				} else if (cash <= -MobHunting.getConfigManager().minimumReward) {
					plugin.getRewardManager().withdrawPlayer(getPlayer(killer, killed), -cash);
					if (!MyPetCompat.isKilledByMyPet(killed) && !CitizensCompat.isNPC(killer))
						onAssist(info.getAssister(), killer, killed, info.getLastAssistTime());
					Messages.debug("%s was assisted by %s. Reward/Penalty is only ½ (%s)",
							getPlayer(killer, killed).getName(), getKillerName(killer, killed),
							plugin.getRewardManager().format(cash));
				}
			}

			// Tell the player that he got the reward/penalty, unless muted
			if (!killer_muted)

				if (extraString.trim().isEmpty()) {
					if (cash >= MobHunting.getConfigManager().minimumReward) {
						if (!MobHunting.getConfigManager().dropMoneyOnGroup)
							plugin.getMessages().playerActionBarMessage(getPlayer(killer, killed),
									ChatColor.GREEN + "" + ChatColor.ITALIC
											+ Messages.getString("mobhunting.moneygain", "prize",
													plugin.getRewardManager().format(cash), "killed",
													mob.getFriendlyName()));
						else
							plugin.getMessages().playerActionBarMessage(getPlayer(killer, killed),
									ChatColor.GREEN + "" + ChatColor.ITALIC
											+ Messages.getString("mobhunting.moneygain.drop", "prize",
													plugin.getRewardManager().format(cash), "killed",
													mob.getFriendlyName()));
					} else if (cash <= -MobHunting.getConfigManager().minimumReward) {
						plugin.getMessages().playerActionBarMessage(getPlayer(killer, killed),
								ChatColor.RED + "" + ChatColor.ITALIC
										+ Messages.getString("mobhunting.moneylost", "prize",
												plugin.getRewardManager().format(cash), "killed",
												mob.getFriendlyName()));
					}

				} else {
					if (cash >= MobHunting.getConfigManager().minimumReward) {
						if (!MobHunting.getConfigManager().dropMoneyOnGroup)
							plugin.getMessages().playerActionBarMessage(getPlayer(killer, killed), ChatColor.GREEN + ""
									+ ChatColor.ITALIC
									+ Messages.getString("mobhunting.moneygain.bonuses", "basic_prize",
											plugin.getRewardManager().format(basic_prize), "prize",
											plugin.getRewardManager().format(cash), "bonuses", extraString.trim(),
											"multipliers", plugin.getRewardManager().format(multipliers), "killed",
											mob.getFriendlyName()));
						else
							plugin.getMessages().playerActionBarMessage(getPlayer(killer, killed), ChatColor.GREEN + ""
									+ ChatColor.ITALIC
									+ Messages.getString("mobhunting.moneygain.bonuses.drop", "basic_prize",
											plugin.getRewardManager().format(basic_prize), "prize",
											plugin.getRewardManager().format(cash), "bonuses", extraString.trim(),
											"multipliers", plugin.getRewardManager().format(multipliers), "killed",
											mob.getFriendlyName()));
					} else if (cash <= -MobHunting.getConfigManager().minimumReward) {
						plugin.getMessages().playerActionBarMessage(getPlayer(killer, killed),
								ChatColor.RED + "" + ChatColor.ITALIC
										+ Messages.getString("mobhunting.moneylost.bonuses", "basic_prize",
												plugin.getRewardManager().format(basic_prize), "prize",
												plugin.getRewardManager().format(cash), "bonuses", extraString.trim(),
												"multipliers", multipliers, "killed", mob.getFriendlyName()));
					}
				}
		} else
			Messages.debug("The money reward was 0 or less than %s  (Bonuses=%s)", getPlayer(killer, killed).getName(),
					MobHunting.getConfigManager().minimumReward, extraString);

		// McMMO Level rewards
		if (killer != null && McMMOCompat.isSupported() && MobHunting.getConfigManager().enableMcMMOLevelRewards
				&& data.getDampenedKills() < 10 && !CrackShotCompat.isCrackShotUsed(killed)) {

			SkillType skilltype = null;
			if (Misc.isAxe(info.getWeapon()))
				skilltype = SkillType.AXES;
			else if (Misc.isSword(info.getWeapon()))
				skilltype = SkillType.SWORDS;
			else if (Misc.isBow(info.getWeapon()))
				skilltype = SkillType.ARCHERY;
			else if (Misc.isUnarmed(info.getWeapon()))
				skilltype = SkillType.UNARMED;

			if (skilltype != null) {
				double chance = MobHunting.getMobHuntingManager().mRand.nextDouble();
				Messages.debug("If %s<%s %s will get a McMMO Level for %s", chance,
						plugin.getRewardManager().getMcMMOChance(killed), killer.getName(), skilltype.getName());

				if (chance < plugin.getRewardManager().getMcMMOChance(killed)) {
					int level = plugin.getRewardManager().getMcMMOLevel(killed);
					McMMOCompat.addLevel(killer, skilltype.getName(), level);
					Messages.debug("%s was rewarded with %s McMMO Levels for %s", killer.getName(),
							plugin.getRewardManager().getMcMMOLevel(killed), skilltype.getName());
					killer.sendMessage(Messages.getString("mobhunting.mcmmo.skilltype_level", "mcmmo_level", level,
							"skilltype", skilltype));
				}
			}
		}

		// Run console commands as a reward
		if (plugin.getRewardManager().isCmdGointToBeExcuted(killed) && data.getDampenedKills() < 10) {
			String worldname = getPlayer(killer, killed).getWorld().getName();
			String killerpos = getPlayer(killer, killed).getLocation().getBlockX() + " "
					+ getPlayer(killer, killed).getLocation().getBlockY() + " "
					+ getPlayer(killer, killed).getLocation().getBlockZ();
			String killedpos = killed.getLocation().getBlockX() + " " + killed.getLocation().getBlockY() + " "
					+ killed.getLocation().getBlockZ();
			String prizeCommand = plugin.getRewardManager().getKillConsoleCmd(killed)
					.replaceAll("\\{player\\}", getPlayer(killer, killed).getName())
					.replaceAll("\\{killer\\}", getPlayer(killer, killed).getName())
					.replaceAll("\\{world\\}", worldname).replace("\\{prize\\}", plugin.getRewardManager().format(cash))
					.replaceAll("\\{killerpos\\}", killerpos).replaceAll("\\{killedpos\\}", killedpos);
			if (killed instanceof Player)
				prizeCommand = prizeCommand.replaceAll("\\{killed_player\\}", killed.getName())
						.replaceAll("\\{killed\\}", killed.getName());
			else
				prizeCommand = prizeCommand.replaceAll("\\{killed_player\\}", mob.getMobName())
						.replaceAll("\\{killed\\}", mob.getMobName());
			Messages.debug("Command to be run:" + prizeCommand);
			if (!plugin.getRewardManager().getKillConsoleCmd(killed).equals("")) {
				String str = prizeCommand;
				do {
					if (str.contains("|")) {
						int n = str.indexOf("|");
						try {
							Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),
									str.substring(0, n));
						} catch (CommandException e) {
							Bukkit.getConsoleSender()
									.sendMessage(ChatColor.RED + "[MobHunting][ERROR] Could not run cmd:\""
											+ str.substring(0, n) + "\" when Mob:" + mob.getMobName()
											+ " was killed by " + getPlayer(killer, killed).getName());
							Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Command:" + str.substring(0, n));
						}
						str = str.substring(n + 1, str.length());
					}
				} while (str.contains("|"));
				try {
					Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), str);
				} catch (CommandException e) {
					Bukkit.getConsoleSender()
							.sendMessage(ChatColor.RED + "[MobHunting][ERROR] Could not run cmd:\"" + str
									+ "\" when Mob:" + mob.getMobName() + " was killed by "
									+ getPlayer(killer, killed).getName());
					Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Command:" + str);
				}
			}

			// Update PlaceHolderData
			if (PlaceholderAPICompat.isSupported()) {
				if (info.getAssister() == null) {
					PlaceHolderData p = PlaceholderAPICompat.getPlaceHolders()
							.get(getPlayer(killer, killed).getUniqueId());
					p.setTotal_kills(p.getTotal_kills()+1);
					PlaceholderAPICompat.getPlaceHolders().put(getPlayer(killer, killed).getUniqueId(), p);
				} else {
					PlaceHolderData p = PlaceholderAPICompat.getPlaceHolders()
							.get(getPlayer(killer, killed).getUniqueId());
					p.setTotal_assists(p.getTotal_assists()+1);
					PlaceholderAPICompat.getPlaceHolders().put(getPlayer(killer, killed).getUniqueId(), p);
				}
			}

			// send a message to the player
			if (!plugin.getRewardManager().getKillRewardDescription(killed).equals("") && !killer_muted) {
				String message = ChatColor.GREEN + "" + ChatColor.ITALIC
						+ plugin.getRewardManager().getKillRewardDescription(killed)
								.replaceAll("\\{player\\}", getPlayer(killer, killed).getName())
								.replaceAll("\\{killer\\}", getPlayer(killer, killed).getName())
								.replace("\\{prize\\}", plugin.getRewardManager().format(cash))
								.replaceAll("\\{world\\}", worldname).replaceAll("\\{killerpos\\}", killerpos)
								.replaceAll("\\{killedpos\\}", killedpos);
				if (killed instanceof Player)
					message = message.replaceAll("\\{killed_player\\}", killed.getName()).replaceAll("\\{killed\\}",
							killed.getName());
				else
					message = message.replaceAll("\\{killed_player\\}", mob.getMobName()).replaceAll("\\{killed\\}",
							mob.getMobName());
				Messages.debug("Description to be send:" + message);
				getPlayer(killer, killed).sendMessage(message);
			}
		}
		Messages.debug("======================= kill ended (37)=====================");
	}

	/**
	 * Get the Player or the MyPet owner (Player)
	 *
	 * @param killer
	 *            - the player who killed the mob
	 * @param killed
	 *            - the mob which died
	 * @return the Player or return null when killer is not a player and killed
	 *         not killed by a MyPet.
	 */
	private Player getPlayer(Player killer, Entity killed) {
		if (killer != null)
			return killer;

		Player p = MyPetCompat.getMyPetOwner(killed);
		if (p != null)
			return p;

		DamageInformation damageInformation = mDamageHistory.get(killed);
		if (damageInformation != null && damageInformation.isCrackShotWeaponUsed())
			return damageInformation.getAttacker();

		return null;

	}

	private String getKillerName(Player killer, Entity killed) {
		if (killer != null)
			return killer.getName();
		if (MyPetCompat.isKilledByMyPet(killed))
			return MyPetCompat.getMyPet(killed).getName();
		else
			return "";
	}

	private void cancelDrops(EntityDeathEvent event, boolean items, boolean xp) {
		if (items) {
			Messages.debug("Removing naturally dropped items");
			event.getDrops().clear();
		}
		if (xp) {
			Messages.debug("Removing naturally dropped XP");
			event.setDroppedExp(0);
		}
	}

	private void onAssist(Player player, Player killer, LivingEntity killed, long time) {
		if (!MobHunting.getConfigManager().enableAssists
				|| (System.currentTimeMillis() - time) > MobHunting.getConfigManager().assistTimeout * 1000)
			return;

		double multiplier = MobHunting.getConfigManager().assistMultiplier;
		double ks = 1.0;
		if (MobHunting.getConfigManager().assistAllowKillstreak) {
			HuntData data = new HuntData(player);
			ks = data.handleKillstreak(plugin, player);
		}

		multiplier *= ks;
		double cash = 0;
		if (killed instanceof Player)
			cash = plugin.getRewardManager().getBaseKillPrize(killed) * multiplier / 2;
		else
			cash = plugin.getRewardManager().getBaseKillPrize(killed) * multiplier;

		if ((cash >= MobHunting.getConfigManager().minimumReward)
				|| (cash <= -MobHunting.getConfigManager().minimumReward)) {
			ExtendedMob mob = MobHunting.getExtendedMobManager().getExtendedMobFromEntity(killed);
			if (mob.getMob_id() == 0) {
				Bukkit.getLogger().warning("Unknown Mob:" + mob.getMobName() + " from plugin " + mob.getMobPlugin());
				Bukkit.getLogger().warning("Please report this to developer!");
				return;
			}
			// MobHunting.getDataStoreManager().recordAssist(player, killer,
			// mob, killed.hasMetadata("MH:hasBonus"), cash);
			if (cash >= 0)
				plugin.getRewardManager().depositPlayer(player, cash);
			else
				plugin.getRewardManager().withdrawPlayer(player, -cash);
			// Messages.debug("RecordCash: %s killed a %s (%s) Cash=%s",
			// killer.getName(), mob.getName(),
			// mob.getMobPlugin().name(), cash);
			// MobHunting.getDataStoreManager().recordCash(killer, mob,
			// killed.hasMetadata("MH:hasBonus"), cash);
			Messages.debug("%s got a on assist reward (%s)", player.getName(), plugin.getRewardManager().format(cash));

			if (ks != 1.0)
				plugin.getMessages().playerActionBarMessage(player, ChatColor.GREEN + "" + ChatColor.ITALIC + Messages
						.getString("mobhunting.moneygain.assist", "prize", plugin.getRewardManager().format(cash)));
			else
				plugin.getMessages().playerActionBarMessage(player,
						ChatColor.GREEN + "" + ChatColor.ITALIC
								+ Messages.getString("mobhunting.moneygain.assist.bonuses", "prize",
										plugin.getRewardManager().format(cash), "bonuses", String.format("x%.1f", ks)));
		} else
			Messages.debug("KillBlocked %s: Reward was less than %s.", killer.getName(),
					MobHunting.getConfigManager().minimumReward);
		;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void bonusMobSpawn(CreatureSpawnEvent event) {
		// Bonus Mob can't be Citizens and MyPet
		if (CitizensCompat.isNPC(event.getEntity()) || MyPetCompat.isMyPet(event.getEntity()))
			return;

		if (event.getEntityType() == EntityType.ENDER_DRAGON)
			return;

		if (event.getEntityType() == EntityType.CREEPER)
			return;

		if (!MobHunting.getMobHuntingManager().isHuntEnabledInWorld(event.getLocation().getWorld())
				|| (plugin.getRewardManager().getBaseKillPrize(event.getEntity()) == 0
						&& plugin.getRewardManager().getKillConsoleCmd(event.getEntity()).equals(""))
				|| event.getSpawnReason() != SpawnReason.NATURAL)
			return;

		if (MobHunting.getMobHuntingManager().mRand.nextDouble() * 100 < MobHunting.getConfigManager().bonusMobChance) {
			plugin.getParticleManager().attachEffect(event.getEntity(), Effect.MOBSPAWNER_FLAMES);
			if (MobHunting.getMobHuntingManager().mRand.nextBoolean())
				event.getEntity()
						.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 3));
			else
				event.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2));
			event.getEntity().setMetadata("MH:hasBonus", new FixedMetadataValue(plugin, true));
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void spawnerMobSpawn(CreatureSpawnEvent event) {
		// Citizens and MyPet can't be spawned from Spawners and eggs
		if (CitizensCompat.isNPC(event.getEntity()) || MyPetCompat.isMyPet(event.getEntity()))
			return;

		if (!MobHunting.getMobHuntingManager().isHuntEnabledInWorld(event.getLocation().getWorld())
				|| (plugin.getRewardManager().getBaseKillPrize(event.getEntity()) == 0)
						&& plugin.getRewardManager().getKillConsoleCmd(event.getEntity()).equals(""))
			return;

		if (event.getSpawnReason() == SpawnReason.SPAWNER || event.getSpawnReason() == SpawnReason.SPAWNER_EGG
				|| event.getSpawnReason() == SpawnReason.DISPENSE_EGG) {
			if (MobHunting.getConfigManager().disableMoneyRewardsFromMobSpawnersEggsAndDispensers)
				if (!MobHunting.getGrindingManager().isWhitelisted(event.getEntity().getLocation()))
					event.getEntity().setMetadata(SPAWNER_BLOCKED, new FixedMetadataValue(plugin, true));
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void reinforcementMobSpawn(CreatureSpawnEvent event) {

		if (event.getSpawnReason() != SpawnReason.REINFORCEMENTS)
			return;

		LivingEntity mob = event.getEntity();

		if (CitizensCompat.isNPC(mob) && !CitizensCompat.isSentryOrSentinelOrSentries(mob))
			return;

		if (!MobHunting.getMobHuntingManager().isHuntEnabledInWorld(event.getLocation().getWorld())
				|| (plugin.getRewardManager().getBaseKillPrize(mob) <= 0)
						&& plugin.getRewardManager().getKillConsoleCmd(mob).equals(""))
			return;

		event.getEntity().setMetadata("MH:reinforcement", new FixedMetadataValue(plugin, true));

	}

	public Set<IModifier> getHuntingModifiers() {
		return mHuntingModifiers;
	}

	public WeakHashMap<LivingEntity, DamageInformation> getDamageHistory() {
		return mDamageHistory;
	}
}