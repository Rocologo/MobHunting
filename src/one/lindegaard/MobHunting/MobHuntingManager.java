package one.lindegaard.MobHunting;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.sk89q.worldguard.protection.flags.DefaultFlag;

import one.lindegaard.MobHunting.bounty.Bounty;
import one.lindegaard.MobHunting.bounty.BountyManager;
import one.lindegaard.MobHunting.bounty.BountyStatus;
import one.lindegaard.MobHunting.compatibility.BattleArenaCompat;
import one.lindegaard.MobHunting.compatibility.CitizensCompat;
import one.lindegaard.MobHunting.compatibility.ConquestiaMobsCompat;
import one.lindegaard.MobHunting.compatibility.CustomMobsCompat;
import one.lindegaard.MobHunting.compatibility.DisguisesHelper;
import one.lindegaard.MobHunting.compatibility.EssentialsCompat;
import one.lindegaard.MobHunting.compatibility.MinigamesLibCompat;
import one.lindegaard.MobHunting.compatibility.MobArenaCompat;
import one.lindegaard.MobHunting.compatibility.MobStackerCompat;
import one.lindegaard.MobHunting.compatibility.MyPetCompat;
import one.lindegaard.MobHunting.compatibility.MysteriousHalloweenCompat;
import one.lindegaard.MobHunting.compatibility.MythicMobsCompat;
import one.lindegaard.MobHunting.compatibility.PVPArenaCompat;
import one.lindegaard.MobHunting.compatibility.StackMobCompat;
import one.lindegaard.MobHunting.compatibility.TARDISWeepingAngelsCompat;
import one.lindegaard.MobHunting.compatibility.VanishNoPacketCompat;
import one.lindegaard.MobHunting.compatibility.WorldGuardCompat;
import one.lindegaard.MobHunting.compatibility.WorldGuardHelper;
import one.lindegaard.MobHunting.events.MobHuntEnableCheckEvent;
import one.lindegaard.MobHunting.events.MobHuntKillEvent;
import one.lindegaard.MobHunting.mobs.ExtendedMob;
import one.lindegaard.MobHunting.modifier.BonusMobBonus;
import one.lindegaard.MobHunting.modifier.BrawlerBonus;
import one.lindegaard.MobHunting.modifier.ConquestiaBonus;
import one.lindegaard.MobHunting.modifier.CoverBlown;
import one.lindegaard.MobHunting.modifier.CriticalModifier;
import one.lindegaard.MobHunting.modifier.DifficultyBonus;
import one.lindegaard.MobHunting.modifier.FlyingPenalty;
import one.lindegaard.MobHunting.modifier.FriendleFireBonus;
import one.lindegaard.MobHunting.modifier.GrindingPenalty;
import one.lindegaard.MobHunting.modifier.HappyHourBonus;
import one.lindegaard.MobHunting.modifier.IModifier;
import one.lindegaard.MobHunting.modifier.MountedBonus;
import one.lindegaard.MobHunting.modifier.ProSniperBonus;
import one.lindegaard.MobHunting.modifier.RankBonus;
import one.lindegaard.MobHunting.modifier.ReturnToSenderBonus;
import one.lindegaard.MobHunting.modifier.ShoveBonus;
import one.lindegaard.MobHunting.modifier.SneakyBonus;
import one.lindegaard.MobHunting.modifier.SniperBonus;
import one.lindegaard.MobHunting.modifier.StackedMobBonus;
import one.lindegaard.MobHunting.modifier.Undercover;
import one.lindegaard.MobHunting.npc.MasterMobHunterManager;
import one.lindegaard.MobHunting.rewards.RewardManager;
import one.lindegaard.MobHunting.update.Updater;
import one.lindegaard.MobHunting.util.Misc;

public class MobHuntingManager implements Listener {

	private MobHunting instance;
	public Random mRand = new Random();
	private final String HUNTDATA = "MH:HuntData";

	private static WeakHashMap<LivingEntity, DamageInformation> mDamageHistory = new WeakHashMap<LivingEntity, DamageInformation>();
	private Set<IModifier> mHuntingModifiers = new HashSet<IModifier>();

	/**
	 * Constructor for MobHuntingManager
	 * 
	 * @param instance
	 */
	public MobHuntingManager(MobHunting instance) {
		this.instance = instance;
		registerHuntingModifiers();
		Bukkit.getServer().getPluginManager().registerEvents(this, instance);
	}

	/**
	 * Gets the DamageInformation for a LivingEntity
	 * 
	 * @param entity
	 * @return
	 */
	public DamageInformation getDamageInformation(LivingEntity entity) {
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
			}.runTaskLater(instance, 20L);
		}
	}

	/**
	 * Set if MobHunting is allowed for the player
	 * 
	 * @param player
	 * @param enabled
	 *            = true : means the MobHunting is allowed
	 */
	public void setHuntEnabled(Player player, boolean enabled) {
		player.setMetadata("MH:enabled", new FixedMetadataValue(instance, enabled));
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
			Messages.debug(ex.getMessage().toString());
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

	/**
	 * get the HuntData() stored on the player.
	 * 
	 * @param player
	 * @return HuntData
	 */
	public HuntData getHuntData(Player player) {
		HuntData data = new HuntData(instance);
		if (!player.hasMetadata(HUNTDATA)) {
			player.setMetadata(HUNTDATA, new FixedMetadataValue(instance, data));
		} else {
			List<MetadataValue> md = player.getMetadata(HUNTDATA);
			for (MetadataValue mdv : md) {
				if (mdv.value() instanceof HuntData) {
					data = (HuntData) mdv.value();
					break;
				}
			}
		}
		return data;
	}

	private void registerHuntingModifiers() {
		mHuntingModifiers.add(new BonusMobBonus());
		mHuntingModifiers.add(new BrawlerBonus());
		mHuntingModifiers.add(new CoverBlown());
		mHuntingModifiers.add(new CriticalModifier());
		mHuntingModifiers.add(new DifficultyBonus());
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
		mHuntingModifiers.add(new Undercover());
		if (MobStackerCompat.isSupported() || StackMobCompat.isSupported())
			mHuntingModifiers.add(new StackedMobBonus());
		if (ConquestiaMobsCompat.isSupported())
			mHuntingModifiers.add(new ConquestiaBonus());
	}

	public double handleKillstreak(Player player) {
		HuntData data = getHuntData(player);

		// Killstreak can be disabled by setting the multiplier to 1

		int lastKillstreakLevel = data.getKillstreakLevel();
		Messages.debug("lastKillstreakLevel=%s", lastKillstreakLevel);

		data.setKillStreak(data.getKillStreak() + 1);
		player.setMetadata(HUNTDATA, new FixedMetadataValue(instance, data));

		Messages.debug("newKillstreakLevel=%s", data.getKillstreakLevel());

		double multiplier = data.getKillstreakMultiplier();
		if (multiplier != 1) {
			// Give a message notifying of killstreak increase
			if (data.getKillstreakLevel() != lastKillstreakLevel) {
				switch (data.getKillstreakLevel()) {
				case 1:
					Messages.playerBossbarMessage(player,
							ChatColor.BLUE + Messages.getString("mobhunting.killstreak.level.1") + " " + ChatColor.GRAY
									+ Messages.getString("mobhunting.killstreak.activated", "multiplier",
											String.format("%.1f", multiplier)));
					break;
				case 2:
					Messages.playerBossbarMessage(player,
							ChatColor.BLUE + Messages.getString("mobhunting.killstreak.level.2") + " " + ChatColor.GRAY
									+ Messages.getString("mobhunting.killstreak.activated", "multiplier",
											String.format("%.1f", multiplier)));
					break;
				case 3:
					Messages.playerBossbarMessage(player,
							ChatColor.BLUE + Messages.getString("mobhunting.killstreak.level.3") + " " + ChatColor.GRAY
									+ Messages.getString("mobhunting.killstreak.activated", "multiplier",
											String.format("%.1f", multiplier)));
					break;
				default:
					Messages.playerBossbarMessage(player,
							ChatColor.BLUE + Messages.getString("mobhunting.killstreak.level.4") + " " + ChatColor.GRAY
									+ Messages.getString("mobhunting.killstreak.activated", "multiplier",
											String.format("%.1f", multiplier)));
					break;
				}

			}
		}

		return multiplier;
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
		} else if (CitizensCompat.isSentryOrSentinel(mob)) {
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

		HuntData data = MobHunting.getMobHuntingManager().getHuntData(event.getEntity());
		if (data.getKillstreakLevel() != 0 && data.getKillstreakMultiplier() != 1)
			Messages.playerActionBarMessage((Player) event.getEntity(),
					ChatColor.RED + "" + ChatColor.ITALIC + Messages.getString("mobhunting.killstreak.ended"));
		data.setKillStreak(0);

		double playerPenalty = 0;
		Player killed = event.getEntity();

		if (CitizensCompat.isNPC(killed))
			return;

		EntityDamageEvent cause = killed.getLastDamageCause();
		if (cause instanceof EntityDamageByEntityEvent) {
			Entity damager = ((EntityDamageByEntityEvent) cause).getDamager();
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
				Messages.debug("%s was killed by a %s shot by %s", killed.getName(), damager.getName(),
						((Projectile) damager).getShooter().toString());
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

				playerPenalty = MobHunting.getConfigManager().getPlayerKilledByMobPenalty(killed);
				if (playerPenalty != 0) {
					boolean killed_muted = false;
					if (MobHunting.getPlayerSettingsmanager().containsKey(killed))
						killed_muted = MobHunting.getPlayerSettingsmanager().getPlayerSettings(killed).isMuted();
					MobHunting.getRewardManager().withdrawPlayer(killed, playerPenalty);
					if (!killed_muted)
						Messages.playerActionBarMessage(killed,
								ChatColor.RED + "" + ChatColor.ITALIC + Messages.getString("mobhunting.moneylost",
										"prize", MobHunting.getRewardManager().format(playerPenalty)));
					Messages.debug("%s lost %s for being killed by a %s", mob.getName(),
							MobHunting.getRewardManager().format(playerPenalty), mob.getName());
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
		HuntData data = MobHunting.getMobHuntingManager().getHuntData(player);
		if (data.getKillstreakLevel() != 0 && data.getKillstreakMultiplier() != 1)
			Messages.playerActionBarMessage(player,
					ChatColor.RED + "" + ChatColor.ITALIC + Messages.getString("mobhunting.killstreak.ended"));
		data.setKillStreak(0);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onSkeletonShoot(ProjectileLaunchEvent event) {
		// TODO: can Skeleton use other weapons than an Arrow?
		if (!(event.getEntity() instanceof Arrow) || !(event.getEntity().getShooter() instanceof Skeleton)
				|| !MobHunting.getMobHuntingManager().isHuntEnabledInWorld(event.getEntity().getWorld()))
			return;

		Skeleton shooter = (Skeleton) event.getEntity().getShooter();

		if (shooter.getTarget() instanceof Player
				&& MobHunting.getMobHuntingManager().isHuntEnabled((Player) shooter.getTarget())
				&& ((Player) shooter.getTarget()).getGameMode() != GameMode.CREATIVE) {
			DamageInformation info = null;
			info = mDamageHistory.get(shooter);

			if (info == null)
				info = new DamageInformation();

			info.time = System.currentTimeMillis();

			info.attacker = (Player) shooter.getTarget();

			info.attackerPosition = shooter.getTarget().getLocation().clone();
			mDamageHistory.put(shooter, info);
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
		if (CitizensCompat.isNPC(damager) && !CitizensCompat.isSentryOrSentinel(damager))
			return;

		if (CitizensCompat.isNPC(damaged) && !CitizensCompat.isSentryOrSentinel(damaged))
			return;

		if (WorldGuardCompat.isSupported()
				&& !WorldGuardHelper.isAllowedByWorldGuard(damager, damaged, DefaultFlag.MOB_DAMAGE, true)) {
			return;
		}

		DamageInformation info = null;
		info = mDamageHistory.get(damaged);
		if (info == null)
			info = new DamageInformation();

		info.time = System.currentTimeMillis();

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

			info.mele = false;
			projectile = true;
		} else
			info.mele = true;

		if (MyPetCompat.isMyPet(damager)) {
			cause = MyPetCompat.getMyPetOwner(damager);
			info.mele = false;
			info.wolfAssist = true;
		} else if (damager instanceof Wolf && ((Wolf) damager).isTamed()
				&& ((Wolf) damager).getOwner() instanceof Player) {
			cause = (Player) ((Wolf) damager).getOwner();
			info.mele = false;
			info.wolfAssist = true;
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
		}

		if (weapon != null)
			info.weapon = weapon;

		// Take note that a weapon has been used at all
		if (info.weapon != null
				&& (Misc.isSword(info.weapon) || Misc.isAxe(info.weapon) || Misc.isPick(info.weapon) || projectile))
			info.usedWeapon = true;

		if (cause != null) {
			if (cause != info.attacker) {
				info.assister = info.attacker;
				info.lastAssistTime = info.lastAttackTime;
			}

			info.lastAttackTime = System.currentTimeMillis();

			info.attacker = cause;
			if (cause.isFlying() && !cause.isInsideVehicle())
				info.wasFlying = true;

			info.attackerPosition = cause.getLocation().clone();

			if (!info.playerUndercover)
				if (DisguisesHelper.isDisguised(cause)) {
					if (DisguisesHelper.isDisguisedAsAgresiveMob(cause)) {
						Messages.debug("[MobHunting] %s was under cover - diguised as an agressive mob",
								cause.getName());
						info.playerUndercover = true;
					} else
						Messages.debug("[MobHunting] %s was under cover - diguised as an passive mob", cause.getName());
					if (MobHunting.getConfigManager().removeDisguiseWhenAttacking) {
						DisguisesHelper.undisguiseEntity(cause);
						// if (cause instanceof Player)
						Messages.playerActionBarMessage(cause, ChatColor.GREEN + "" + ChatColor.ITALIC
								+ Messages.getString("bonus.undercover.message", "cause", cause.getName()));
						if (damaged instanceof Player)
							Messages.playerActionBarMessage((Player) damaged, ChatColor.GREEN + "" + ChatColor.ITALIC
									+ Messages.getString("bonus.undercover.message", "cause", cause.getName()));
					}
				}

			if (!info.mobCoverBlown)
				if (DisguisesHelper.isDisguised(damaged)) {
					if (DisguisesHelper.isDisguisedAsAgresiveMob(damaged)) {
						Messages.debug("[MobHunting] %s Cover blown, diguised as an agressive mob", damaged.getName());
						info.mobCoverBlown = true;
					} else
						Messages.debug("[MobHunting] %s Cover Blown, diguised as an passive mob", damaged.getName());
					if (MobHunting.getConfigManager().removeDisguiseWhenAttacked) {
						DisguisesHelper.undisguiseEntity(damaged);
						if (damaged instanceof Player)
							Messages.playerActionBarMessage((Player) damaged, ChatColor.GREEN + "" + ChatColor.ITALIC
									+ Messages.getString("bonus.coverblown.message", "damaged", damaged.getName()));
						if (cause instanceof Player)
							Messages.playerActionBarMessage(cause, ChatColor.GREEN + "" + ChatColor.ITALIC
									+ Messages.getString("bonus.coverblown.message", "damaged", damaged.getName()));
					}
				}

			mDamageHistory.put((LivingEntity) damaged, info);
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	private void onMobDeath(EntityDeathEvent event) {

		boolean cancelNaturalDrops = false;
		boolean cancelXPDrops = false;

		LivingEntity killed = event.getEntity();

		Player killer = event.getEntity().getKiller();

		// Killer is not a player and not a MyPet.
		if (killer == null && !MyPetCompat.isKilledByMyPet(killed)) {
			Messages.debug("KillBlocked: Killer is null and Killed is not killed by MyPet");
			return;
		}

		// Killer is a NPC
		if (killer != null && CitizensCompat.isNPC(killer)) {
			Messages.debug("KillBlocked: Killer is a Citizen NPC.");
			return;
		}

		ExtendedMob mob = MobHunting.getExtendedMobManager().getExtendedMobFromEntity(killed);
		if (mob.getMob_id() == 0) {
			Bukkit.getLogger().warning("Unknown Mob:" + mob.getName() + " from plugin " + mob.getMobPlugin());
			Bukkit.getLogger().warning("Please report this to developer!");
			return;
		}

		// WorldGuard Compatibility
		if (WorldGuardCompat.isSupported()) {
			if ((killer != null || MyPetCompat.isMyPet(killer)) && !CitizensCompat.isNPC(killer)) {
				if (!WorldGuardHelper.isAllowedByWorldGuard(killer, killed, DefaultFlag.MOB_DAMAGE, true)) {
					Messages.debug("KillBlocked:(2) %s is hiding in WG region with mob-damage=DENY", killer.getName());
					Messages.learn(killer, Messages.getString("mobhunting.learn.mob-damage-flag"));
					if (MobHunting.getConfigManager().tryToCancelNaturalDrops) {
						Messages.debug("Trying to remove natural drops");
						cancelNaturalDrops = true;
						event.getDrops().clear();
					}
					if (MobHunting.getConfigManager().tryToCancelXPDrops) {
						Messages.debug("Trying to remove XP drops");
						cancelXPDrops = true;
						event.setDroppedExp(0);
					}
					return;
					// }
				} else if (!WorldGuardHelper.isAllowedByWorldGuard(killer, killed, WorldGuardHelper.getMobHuntingFlag(),
						true)) {
					Messages.debug("KillBlocked: %s is in a protected region mobhunting=DENY", killer.getName());
					Messages.learn(killer, Messages.getString("mobhunting.learn.mobhunting-deny"));
					if (MobHunting.getConfigManager().tryToCancelNaturalDrops) {
						Messages.debug("Trying to remove natural drops");
						cancelNaturalDrops = true;
						event.getDrops().clear();
					}
					if (MobHunting.getConfigManager().tryToCancelXPDrops) {
						Messages.debug("Trying to remove XP drops");
						cancelXPDrops = true;
						event.setDroppedExp(0);
					}
					return;
				}
			}
		}

		// MobHunting is Disabled in World
		if (!MobHunting.getMobHuntingManager().isHuntEnabledInWorld(event.getEntity().getWorld())) {
			if (WorldGuardCompat.isSupported()) {
				if ((killer != null || MyPetCompat.isMyPet(killer)) && !CitizensCompat.isNPC(killer)) {
					if (WorldGuardHelper.isAllowedByWorldGuard(killer, killed, WorldGuardHelper.getMobHuntingFlag(),
							false)) {
						Messages.debug("KillBlocked %s: Mobhunting disabled in world '%s'", killer.getName(),
								killer.getWorld().getName());
						Messages.learn(killer, Messages.getString("mobhunting.learn.disabled"));
						return;
					} else {
						Messages.debug("KillBlocked %s: Mobhunting disabled in world '%s'", killer.getName(),
								killer.getWorld().getName());
						Messages.learn(killer, Messages.getString("mobhunting.learn.disabled"));
						return;
					}
				} else {
					Messages.debug("KillBlocked: killer is null and killer was not a MyPet or NPC Sentinel Guard.");
					return;
				}
			} else {
				// MobHunting is NOT allowed in this world,
				Messages.debug("KillBlocked %s: Mobhunting disabled in world '%s'", killer.getName(),
						killer.getWorld().getName());
				Messages.learn(killer, Messages.getString("mobhunting.learn.disabled"));
				return;
			}
		}

		// Handle Muted mode
		boolean killer_muted = false;
		boolean killed_muted = false;
		if (MobHunting.getPlayerSettingsmanager().containsKey(killer))
			killer_muted = MobHunting.getPlayerSettingsmanager().getPlayerSettings(killer).isMuted();
		if (MobHunting.getPlayerSettingsmanager().containsKey(killed))
			killed_muted = MobHunting.getPlayerSettingsmanager().getPlayerSettings((Player) killed).isMuted();

		// Player died while playing a Minigame: MobArena, PVPArena,
		// BattleArena, Suiside, PVP, penalty when Mobs kills player
		if (killed instanceof Player) {
			// MobArena
			if (MobArenaCompat.isPlayingMobArena((Player) killed)
					&& !MobHunting.getConfigManager().mobarenaGetRewards) {
				Messages.debug("KillBlocked: %s was killed while playing MobArena.", killed.getName());
				Messages.learn(killer, Messages.getString("mobhunting.learn.mobarena"));
				return;

				// PVPArena
			} else if (PVPArenaCompat.isPlayingPVPArena((Player) killed)
					&& !MobHunting.getConfigManager().pvparenaGetRewards) {
				Messages.debug("KillBlocked: %s was killed while playing PvpArena.", killed.getName());
				Messages.learn(killer, Messages.getString("mobhunting.learn.pvparena"));
				return;

				// BattleArena
			} else if (BattleArenaCompat.isPlayingBattleArena((Player) killed)) {
				Messages.debug("KillBlocked: %s was killed while playing BattleArena.", killed.getName());
				Messages.learn(killer, Messages.getString("mobhunting.learn.battlearena"));
				return;

				// MiniGamesLib
			} else if (MinigamesLibCompat.isPlayingMinigame((Player) killed)) {
				Messages.debug("KillBlocked: %s was killed while playing a MiniGame.", killed.getName());
				Messages.learn(killer, Messages.getString("mobhunting.learn.minigameslib"));
				return;

				//
			} else if (killer != null) {
				if (killed.equals(killer)) {
					// Suicide
					Messages.learn(killer, Messages.getString("mobhunting.learn.suiside"));
					Messages.debug("KillBlocked: Suiside not allowed (Killer=%s, Killed=%s)", killer.getName(),
							killed.getName());
					return;
					// PVP
				} else if (!MobHunting.getConfigManager().pvpAllowed) {
					// PVP
					Messages.learn(killer, Messages.getString("mobhunting.learn.nopvp"));
					Messages.debug("KillBlocked: PVP not allowed. %s killed %s.", killer.getName(), killed.getName());
					return;
				}
			}
		}

		// Player killed a MythicMob
		if (MythicMobsCompat.isMythicMob(killed)) {
			if (killer != null)
				Messages.debug("%s killed a MythicMob (%s)", killer.getName(), mob.getName());
		} else

		// Player killed a TARDISWeepingAngelMob
		if (TARDISWeepingAngelsCompat.isWeepingAngelMonster(killed)) {
			if (killer != null)
				Messages.debug("%s killed a TARDISWeepingAngelMob (%s)", killer.getName(), mob.getName());
		} else

		// Player killed a Stacked Mob
		if (MobStackerCompat.isStackedMob(killed)) {
			if (MobHunting.getConfigManager().getRewardFromStackedMobs) {
				if (killer != null) {
					Messages.debug("%s killed a stacked mob (%s) No=%s", killer.getName(), killed.getType().getName(),
							MobStackerCompat.getStackSize(killed));
					if (MobStackerCompat.killHoleStackOnDeath(killed) && MobStackerCompat.multiplyLoot()) {
						Messages.debug("Pay reward for no x mob");
					} else {
						// pay reward for one mob, if config allows
						Messages.debug("Pay reward for one mob");
					}
				}
			} else {
				Messages.debug("KillBlocked: Rewards from StackedMobs is disabled in Config.yml");
				return;
			}
		} else

		// Player killed a Citizens2 NPC
		if (killer != null && CitizensCompat.isNPC(killed) && CitizensCompat.isSentryOrSentinel(killed)) {
			Messages.debug("%s killed Sentinel or a Sentry npc-%s (name=%s)", killer.getName(),
					CitizensCompat.getNPCId(killed), mob.getName());
			if (MasterMobHunterManager.isMasterMobHunter(CitizensCompat.getNPC(killed))) {
				// ItemStack is = ((Player)
				// CitizensCompat.getNPC(killed).getEntity()).getItemInHand();
				// killer.getWorld().dropItem(killed.getLocation(), is);
			}
		} else

		// Player killed a MysteriousHalloween Mob
		if (MysteriousHalloweenCompat.isMysteriousHalloween(killed)) {
			if (killer != null)
				Messages.debug("%s killed a MysteriousHalloween Mob (%s)", killer.getName(), mob.getName());
		}

		// Player killed a mob while playing a minigame: MobArena, PVPVArena,
		// BattleArena
		// Player is in Godmode or Vanished
		// Player permission to Hunt (and get rewards)
		if (killer != null) {
			if (MobArenaCompat.isPlayingMobArena(killer) && !MobHunting.getConfigManager().mobarenaGetRewards) {
				Messages.debug("KillBlocked: %s is currently playing MobArena.", killer.getName());
				Messages.learn(killer, Messages.getString("mobhunting.learn.mobarena"));
				return;
			} else if (PVPArenaCompat.isPlayingPVPArena(killer) && !MobHunting.getConfigManager().pvparenaGetRewards) {
				Messages.debug("KillBlocked: %s is currently playing PvpArena.", killer.getName());
				Messages.learn(killer, Messages.getString("mobhunting.learn.pvparena"));
				return;
			} else if (BattleArenaCompat.isPlayingBattleArena(killer)) {
				Messages.debug("KillBlocked: %s is currently playing BattleArena.", killer.getName());
				Messages.learn(killer, Messages.getString("mobhunting.learn.battlearena"));
				return;
			} else if (EssentialsCompat.isGodModeEnabled(killer)) {
				Messages.debug("KillBlocked: %s is in God mode", killer.getName());
				Messages.learn(killer, Messages.getString("mobhunting.learn.godmode"));
				return;
			} else if (EssentialsCompat.isVanishedModeEnabled(killer)) {
				Messages.debug("KillBlocked: %s is in Vanished mode", killer.getName());
				Messages.learn(killer, Messages.getString("mobhunting.learn.vanished"));
				return;
			} else if (VanishNoPacketCompat.isVanishedModeEnabled(killer)) {
				Messages.debug("KillBlocked: %s is in Vanished mode", killer.getName());
				Messages.learn(killer, Messages.getString("mobhunting.learn.vanished"));
				return;
			}

			if (!MobHunting.getMobHuntingManager().hasPermissionToKillMob(killer, killed)) {
				Messages.debug("KillBlocked: %s has not permission to kill %s.", killer.getName(),
						killed.getType().getName());
				Messages.learn(killer,
						Messages.getString("mobhunting.learn.no-permission", "killed-mob", killed.getType().getName()));
				return;
			}
		}

		// There is no reward and no penalty for this kill
		if (MobHunting.getConfigManager().getBaseKillPrize(killed) == 0
				&& MobHunting.getConfigManager().getKillConsoleCmd(killed).equals("")) {
			Messages.debug("KillBlocked %s(%d): There is no reward and no penalty for this Mob/Player", mob.getName(),
					killed.getEntityId());
			Messages.learn(killer, Messages.getString("mobhunting.learn.no-reward", "killed", mob.getName()));
			return;
		}

		// The Mob/Player has MH:Blocked
		if (event.getEntity().hasMetadata("MH:blocked")) {
			if (killed != null) {
				Messages.debug("KillBlocked %s(%d): Mob has MH:blocked meta (probably spawned from a mob spawner)",
						event.getEntity().getType(), killed.getEntityId());
				Messages.learn(killer, Messages.getString("mobhunting.learn.mobspawner", "killed", mob.getName()));
			}
			return;
		}

		// MobHunting is disabled for the player
		if (killer != null && !MobHunting.getMobHuntingManager().isHuntEnabled(killer)) {
			Messages.debug("KillBlocked: %s Hunting is disabled for player", killer.getName());
			Messages.learn(killer, Messages.getString("mobhunting.learn.huntdisabled"));
			return;
		}

		// The player is in Creative mode
		if (killer != null && killer.getGameMode() != GameMode.SURVIVAL) {
			Messages.debug("KillBlocked: %s is not in survival mode", killer.getName());
			Messages.learn(killer, Messages.getString("mobhunting.learn.creative"));
			if (MobHunting.getConfigManager().tryToCancelNaturalDropsWhenInCreative) {
				Messages.debug("Trying to remove natural drops");
				cancelNaturalDrops = true;
				event.getDrops().clear();
			}
			if (MobHunting.getConfigManager().tryToCancelXPDropsWhenInCreative) {
				Messages.debug("Trying to remove XP drops");
				cancelXPDrops = true;
				event.setDroppedExp(0);
			}
			return;
		}

		// Update DamageInformation
		DamageInformation info = null;
		info = mDamageHistory.get(killed);

		if (killed instanceof LivingEntity && mDamageHistory.containsKey((LivingEntity) killed)) {
			info = mDamageHistory.get(killed);

			if (System.currentTimeMillis() - info.time > MobHunting.getConfigManager().assistTimeout * 1000)
				info = null;
			else if (killer == null)
				killer = info.attacker;
		}
		EntityDamageByEntityEvent lastDamageCause = null;
		if (killed.getLastDamageCause() instanceof EntityDamageByEntityEvent)
			lastDamageCause = (EntityDamageByEntityEvent) killed.getLastDamageCause();
		if (info == null) {
			info = new DamageInformation();
			info.time = System.currentTimeMillis();
			info.lastAttackTime = info.time;
			if (killer != null) {
				info.attacker = killer;
				info.attackerPosition = killer.getLocation();
			}
			info.usedWeapon = true;
		}

		// Check if the kill was within the time limit on both kills and
		// assisted kills
		if (((System.currentTimeMillis() - info.lastAttackTime) > MobHunting.getConfigManager().killTimeout * 1000)
				&& (info.wolfAssist && ((System.currentTimeMillis()
						- info.lastAttackTime) > MobHunting.getConfigManager().assistTimeout * 1000))) {
			Messages.debug("KillBlocked %s: Last damage was too long ago (%s sec.)", killer.getName(),
					(System.currentTimeMillis() - info.lastAttackTime) / 1000);
			return;
		}

		// MyPet killed a mob - Assister is the Owner
		if (MyPetCompat.isKilledByMyPet(killed) && MobHunting.getConfigManager().enableAssists == true) {
			info.assister = MyPetCompat.getMyPetOwner(killed);
			Messages.debug("MyPetAssitedKill: Pet owned by %s killed a %s", info.assister.getName(), mob.getName());
		}

		if (info.weapon == null)
			info.weapon = new ItemStack(Material.AIR);

		// Player or killed Mob is disguised
		if (!info.playerUndercover)
			if (DisguisesHelper.isDisguised(killer)) {
				if (DisguisesHelper.isDisguisedAsAgresiveMob(killer)) {
					info.playerUndercover = true;
				} else if (MobHunting.getConfigManager().removeDisguiseWhenAttacking) {
					DisguisesHelper.undisguiseEntity(killer);
					if (killer != null && !killer_muted)
						Messages.playerActionBarMessage(killer, ChatColor.GREEN + "" + ChatColor.ITALIC
								+ Messages.getString("bonus.undercover.message", "cause", killer.getName()));
					if (killed instanceof Player && !killed_muted)
						Messages.playerActionBarMessage((Player) killed, ChatColor.GREEN + "" + ChatColor.ITALIC
								+ Messages.getString("bonus.undercover.message", "cause", killer.getName()));
				}
			}
		if (!info.mobCoverBlown)
			if (DisguisesHelper.isDisguised(killed)) {
				if (DisguisesHelper.isDisguisedAsAgresiveMob(killed)) {
					info.mobCoverBlown = true;
				}
				if (MobHunting.getConfigManager().removeDisguiseWhenAttacked) {
					DisguisesHelper.undisguiseEntity(killed);
					if (killed instanceof Player && !killed_muted)
						Messages.playerActionBarMessage((Player) killed, ChatColor.GREEN + "" + ChatColor.ITALIC
								+ Messages.getString("bonus.coverblown.message", "damaged", mob.getName()));
					if (killer != null && !killer_muted)
						Messages.playerActionBarMessage(killer, ChatColor.GREEN + "" + ChatColor.ITALIC
								+ Messages.getString("bonus.coverblown.message", "damaged", mob.getName()));
				}
			}

		HuntData data = new HuntData(instance);
		if (killer != null) {
			data = getHuntData(killer);
			// Killstreak
			handleKillstreak(killer);
		} else if (MyPetCompat.isKilledByMyPet(killed)) {
			data = getHuntData(MyPetCompat.getMyPet(killed).getOwner().getPlayer());
			// Killstreak
			handleKillstreak(MyPetCompat.getMyPet(killed).getOwner().getPlayer());
		} else
			return;

		// Record kills that are still within a small area
		Location loc = killed.getLocation();

		// Grinding detection
		Area detectedGrindingArea = MobHunting.getAreaManager().getGrindingArea(loc);
		if (detectedGrindingArea == null)
			detectedGrindingArea = data.getGrindingArea(loc);
		// Slimes ang magmacubes are except from grinding due to their
		// splitting
		// nature
		if (!(event.getEntity() instanceof Slime || event.getEntity() instanceof MagmaCube)
				&& MobHunting.getConfigManager().penaltyGrindingEnable && !killed.hasMetadata("MH:reinforcement")
				&& !MobHunting.getAreaManager().isWhitelisted(loc)) {
			Messages.debug("Checking if player is grinding mob in the same region within a range of %s blocks",
					data.getcDampnerRange());
			Messages.debug("DampendKills=%s", data.getDampenedKills());

			if (detectedGrindingArea != null) {
				data.lastKillAreaCenter = null;
				data.setDampenedKills(detectedGrindingArea.count++);
				if (data.getDampenedKills() == 20) {
					MobHunting.getAreaManager().registerKnownGrindingSpot(detectedGrindingArea);
					if (MobHunting.getConfigManager().tryToCancelNaturalDrops) {
						Messages.debug("This is a registered grinding spot. Natural drops was removed.");
						Messages.learn(killer, "This is a registered grinding spot. Natural drops was removed.");
						cancelNaturalDrops = true;
					}
					if (MobHunting.getConfigManager().tryToCancelXPDrops) {
						Messages.debug("Trying to remove XP drops");
						cancelXPDrops = true;
						event.setDroppedExp(0);
					}
				}
			} else {
				if (data.lastKillAreaCenter != null) {
					if (loc.getWorld().equals(data.lastKillAreaCenter.getWorld())) {
						if (loc.distance(data.lastKillAreaCenter) < data.getcDampnerRange()) {
							if (!MobStackerCompat.isSupported() || (MobStackerCompat.isStackedMob(killed)
									&& !MobStackerCompat.isGrindingStackedMobsAllowed())) {
								data.setDampenedKills(data.getDampenedKills() + 1);
								if (data.getDampenedKills() == 10) {
									Messages.debug("Detected grinding. Killings too close, adding 1 to DampenedKills.");
									Messages.learn(killer, Messages.getString("mobhunting.learn.grindingnotallowed"));
									Messages.playerActionBarMessage(killer,
											ChatColor.RED + Messages.getString("mobhunting.grinding.detected"));
									data.recordGrindingArea();
									if (MobHunting.getConfigManager().tryToCancelNaturalDrops) {
										Messages.debug("Grinding caused natural drops to be removed.");
										cancelNaturalDrops = true;
									}
									if (MobHunting.getConfigManager().tryToCancelXPDrops) {
										Messages.debug("Trying to remove XP drops");
										cancelXPDrops = true;
										event.setDroppedExp(0);
									}
								}
							}
						} else {
							data.lastKillAreaCenter = loc.clone();
							data.setDampenedKills(0);
						}
					} else {
						data.lastKillAreaCenter = loc.clone();
						data.setDampenedKills(0);
					}
				} else {
					data.lastKillAreaCenter = loc.clone();
					data.setDampenedKills(0);
				}
			}

			if (data.getDampenedKills() > 10 + 4) {
				if (data.getKillstreakLevel() != 0 && data.getKillstreakMultiplier() != 1)
					Messages.playerActionBarMessage(killer,
							ChatColor.RED + Messages.getString("mobhunting.killstreak.lost"));
				data.setKillStreak(0);
			}
		}

		// Calculate basic the reward
		double cash = MobHunting.getConfigManager().getBaseKillPrize(killed);
		double basic_prize = cash;

		Messages.debug("Basic Prize=%s for killing a %s", cash, mob.getName());
		double multipliers = 1.0;

		// Apply the modifiers to Basic reward
		ArrayList<String> modifiers = new ArrayList<String>();
		for (IModifier mod : mHuntingModifiers) {
			if (mod.doesApply(killed, killer, data, info, lastDamageCause)) {
				double amt = mod.getMultiplier(killed, killer, data, info, lastDamageCause);
				if (amt != 1.0) {
					modifiers.add(mod.getName());
					multipliers *= amt;
					data.addModifier(mod.getName(), amt);
					Messages.debug("Multiplier: %s = %s", mod.getName(), amt);
				}
			}
		}
		data.setReward(cash);

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

		// Handle Bounty Kills
		double reward = 0;
		if (killer != null && !MobHunting.getConfigManager().disablePlayerBounties && killed instanceof Player) {
			Messages.debug("This was a Pvp kill (killed=%s), number of bounties=%s", killed.getName(),
					MobHunting.getBountyManager().getAllBounties().size());
			OfflinePlayer wantedPlayer = (OfflinePlayer) killed;
			String worldGroupName = MobHunting.getWorldGroupManager().getCurrentWorldGroup(killer);
			if (BountyManager.hasBounties(worldGroupName, wantedPlayer)) {
				Set<Bounty> bounties = MobHunting.getBountyManager().getBounties(worldGroupName, wantedPlayer);
				for (Bounty b : bounties) {
					reward += b.getPrize();
					OfflinePlayer bountyOwner = b.getBountyOwner();
					MobHunting.getBountyManager().removeBounty(b);
					if (bountyOwner != null && bountyOwner.isOnline())
						Messages.playerActionBarMessage(Misc.getOnlinePlayer(bountyOwner),
								Messages.getString("mobhunting.bounty.bounty-claimed", "killer", killer.getName(),
										"prize", b.getPrize(), "killed", killed.getName()));
					b.setStatus(BountyStatus.completed);
					MobHunting.getDataStoreManager().updateBounty(b);
				}
				Messages.playerActionBarMessage(killer, Messages.getString("mobhunting.moneygain-for-killing", "money",
						MobHunting.getRewardManager().format(reward), "killed", killed.getName()));
				Messages.debug("%s got %s for killing %s", killer.getName(), reward, killed.getName());
				// TODO: call bounty event, and check if canceled.
				MobHunting.getRewardManager().depositPlayer(killer, reward);

			} else {
				Messages.debug("There is no Bounty on %s", killed.getName());
			}
		}

		// Pay the reward to player and assister
		if ((cash >= MobHunting.getConfigManager().minimumReward)
				|| (cash <= -MobHunting.getConfigManager().minimumReward)) {

			// Handle MobHuntKillEvent
			MobHuntKillEvent event2 = new MobHuntKillEvent(data, info, killed, killer);
			Bukkit.getPluginManager().callEvent(event2);
			if (event2.isCancelled()) {
				Messages.debug("KillBlocked %s: MobHuntKillEvent was cancelled", killer.getName());
				return;
			}

			if (cancelNaturalDrops) {
				Messages.debug("Natural drops removed.");
				event.getDrops().clear();
			}
			if (cancelXPDrops) {
				Messages.debug("XP drops removed.");
				event.setDroppedExp(0);
			}

			// Handle reward on PVP kill. (Robbing)
			boolean robbing = killer != null && killed instanceof Player && !CitizensCompat.isNPC(killed)
					&& MobHunting.getConfigManager().robFromVictim;
			if (robbing) {
				MobHunting.getRewardManager().withdrawPlayer((Player) killed, cash);
				if (!killed_muted)
					killed.sendMessage(ChatColor.RED + "" + ChatColor.ITALIC + Messages
							.getString("mobhunting.moneylost", "prize", MobHunting.getRewardManager().format(cash)));
				Messages.debug("%s lost %s", killed.getName(), MobHunting.getRewardManager().format(cash));
			}

			// Reward/Penalty for assisted kill
			if (info.assister == null || MobHunting.getConfigManager().enableAssists == false) {
				if (cash >= MobHunting.getConfigManager().minimumReward) {
					if (MobHunting.getConfigManager().dropMoneyOnGroup) {
						RewardManager.dropMoneyOnGround(killer, killed, cash);
					} else {
						MobHunting.getRewardManager().depositPlayer(killer, cash);
						Messages.debug("%s got a reward (%s)", killer.getName(),
								MobHunting.getRewardManager().format(cash));
					}
				} else if (cash <= -MobHunting.getConfigManager().minimumReward) {
					MobHunting.getRewardManager().withdrawPlayer(killer, -cash);
					Messages.debug("%s got a penalty (%s)", killer.getName(),
							MobHunting.getRewardManager().format(cash));
				}
			} else {
				cash = cash / 2;
				if (cash >= MobHunting.getConfigManager().minimumReward) {
					if (MobHunting.getConfigManager().dropMoneyOnGroup) {
						Messages.debug("Assisted kill. Reward/Penalty is ½");
						RewardManager.dropMoneyOnGround(killer, killed, cash);
					} else {
						MobHunting.getRewardManager().depositPlayer(killer, cash);
						onAssist(info.assister, killer, killed, info.lastAssistTime);
						Messages.debug("Assisted kill. %s got a ½ reward (%s)", killer.getName(),
								MobHunting.getRewardManager().format(cash));
					}
				} else if (cash <= -MobHunting.getConfigManager().minimumReward) {
					MobHunting.getRewardManager().withdrawPlayer(killer, -cash);
					onAssist(info.assister, killer, killed, info.lastAssistTime);
					Messages.debug("Assisted kill. %s got a ½ penalty (%s)", killer.getName(),
							MobHunting.getRewardManager().format(cash));
				}
			}

			// Record the kill in the Database
			if (killer != null) {
				Messages.debug("RecordKill: %s killed a %s (%s)", killer.getName(), mob.getName(),
						mob.getMobPlugin().name());
				MobHunting.getDataStoreManager().recordKill(killer, mob, killed.hasMetadata("MH:hasBonus"));
			}

			// Tell the player that he got the reward/penalty, unless muted
			if (!killer_muted)

				if (extraString.trim().isEmpty()) {
					if (cash >= MobHunting.getConfigManager().minimumReward) {
						if (!MobHunting.getConfigManager().dropMoneyOnGroup)
							Messages.playerActionBarMessage(killer,
									ChatColor.GREEN + "" + ChatColor.ITALIC
											+ Messages.getString("mobhunting.moneygain", "prize",
													MobHunting.getRewardManager().format(cash), "killed",
													mob.getFriendlyName()));
						else
							Messages.playerActionBarMessage(killer,
									ChatColor.GREEN + "" + ChatColor.ITALIC
											+ Messages.getString("mobhunting.moneygain.drop", "prize",
													MobHunting.getRewardManager().format(cash), "killed",
													mob.getFriendlyName()));
					} else if (cash <= -MobHunting.getConfigManager().minimumReward) {
						Messages.playerActionBarMessage(killer,
								ChatColor.RED + "" + ChatColor.ITALIC
										+ Messages.getString("mobhunting.moneylost", "prize",
												MobHunting.getRewardManager().format(cash), "killed",
												mob.getFriendlyName()));
					}

				} else {
					if (cash >= MobHunting.getConfigManager().minimumReward) {
						if (!MobHunting.getConfigManager().dropMoneyOnGroup)
							Messages.playerActionBarMessage(killer, ChatColor.GREEN + "" + ChatColor.ITALIC
									+ Messages.getString("mobhunting.moneygain.bonuses", "basic_prize",
											MobHunting.getRewardManager().format(basic_prize), "prize",
											MobHunting.getRewardManager().format(cash), "bonuses", extraString.trim(),
											"multipliers", MobHunting.getRewardManager().format(multipliers), "killed",
											mob.getFriendlyName()));
						else
							Messages.playerActionBarMessage(killer, ChatColor.GREEN + "" + ChatColor.ITALIC
									+ Messages.getString("mobhunting.moneygain.bonuses.drop", "basic_prize",
											MobHunting.getRewardManager().format(basic_prize), "prize",
											MobHunting.getRewardManager().format(cash), "bonuses", extraString.trim(),
											"multipliers", MobHunting.getRewardManager().format(multipliers), "killed",
											mob.getFriendlyName()));
					} else if (cash <= -MobHunting.getConfigManager().minimumReward) {
						Messages.playerActionBarMessage(killer, ChatColor.RED + "" + ChatColor.ITALIC
								+ Messages.getString("mobhunting.moneylost.bonuses", "basic_prize",
										MobHunting.getRewardManager().format(basic_prize), "prize",
										MobHunting.getRewardManager().format(cash), "bonuses", extraString.trim(),
										"multipliers", multipliers, "killed", mob.getFriendlyName()));
					}
				}
		} else
			Messages.debug("KillBlocked %s: Reward was less than %s  (Bonuses=%s)", killer.getName(),
					MobHunting.getConfigManager().minimumReward, extraString);

		// Run console commands as a reward
		if (data.getDampenedKills() < 10) {
			if (MobHunting.getConfigManager().isCmdGointToBeExcuted(killed)) {
				String worldname = killer.getWorld().getName();
				String killerpos = killer.getLocation().getBlockX() + " " + killer.getLocation().getBlockY() + " "
						+ killer.getLocation().getBlockZ();
				String killedpos = killed.getLocation().getBlockX() + " " + killed.getLocation().getBlockY() + " "
						+ killed.getLocation().getBlockZ();
				String prizeCommand = MobHunting.getConfigManager().getKillConsoleCmd(killed)
						.replaceAll("\\{player\\}", killer.getName()).replaceAll("\\{killer\\}", killer.getName())
						.replaceAll("\\{world\\}", worldname)
						.replace("\\{prize\\}", MobHunting.getRewardManager().format(cash))
						.replaceAll("\\{killerpos\\}", killerpos).replaceAll("\\{killedpos\\}", killedpos);
				if (killed instanceof Player)
					prizeCommand = prizeCommand.replaceAll("\\{killed_player\\}", killed.getName())
							.replaceAll("\\{killed\\}", killed.getName());
				else
					prizeCommand = prizeCommand.replaceAll("\\{killed_player\\}", killed.getType().getName())
							.replaceAll("\\{killed\\}", killed.getType().getName());
				Messages.debug("command to be run is:" + prizeCommand);
				if (!MobHunting.getConfigManager().getKillConsoleCmd(killed).equals("")) {
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
				if (!MobHunting.getConfigManager().getKillRewardDescription(killed).equals("") && !killer_muted) {
					String message = ChatColor.GREEN + "" + ChatColor.ITALIC
							+ MobHunting.getConfigManager().getKillRewardDescription(killed)
									.replaceAll("\\{player\\}", killer.getName())
									.replaceAll("\\{killer\\}", killer.getName())
									.replace("\\{prize\\}", MobHunting.getRewardManager().format(cash))
									.replaceAll("\\{world\\}", worldname).replaceAll("\\{killerpos\\}", killerpos)
									.replaceAll("\\{killedpos\\}", killedpos);
					if (killed instanceof Player)
						message = message.replaceAll("\\{killed_player\\}", killed.getName()).replaceAll("\\{killed\\}",
								killed.getName());
					else
						message = message.replaceAll("\\{killed_player\\}", killed.getType().getName())
								.replaceAll("\\{killed\\}", mob.getName());
					Messages.debug("Description to be send:" + message);
					killer.sendMessage(message);
				}
			}
		}
	}

	private void onAssist(Player player, Player killer, LivingEntity killed, long time) {
		if (!MobHunting.getConfigManager().enableAssists
				|| (System.currentTimeMillis() - time) > MobHunting.getConfigManager().assistTimeout * 1000)
			return;

		double multiplier = MobHunting.getConfigManager().assistMultiplier;
		double ks = 1.0;
		if (MobHunting.getConfigManager().assistAllowKillstreak)
			ks = MobHunting.getMobHuntingManager().handleKillstreak(player);

		multiplier *= ks;
		double cash = 0;
		if (killed instanceof Player)
			cash = MobHunting.getConfigManager().getBaseKillPrize(killed) * multiplier / 2;
		else
			cash = MobHunting.getConfigManager().getBaseKillPrize(killed) * multiplier;

		if ((cash >= MobHunting.getConfigManager().minimumReward)
				|| (cash <= -MobHunting.getConfigManager().minimumReward)) {
			ExtendedMob mob = MobHunting.getExtendedMobManager().getExtendedMobFromEntity(killed);
			if (mob.getMob_id() == 0) {
				Bukkit.getLogger().warning("Unknown Mob:" + mob.getName() + " from plugin " + mob.getMobPlugin());
				Bukkit.getLogger().warning("Please report this to developer!");
				return;
			}
			MobHunting.getDataStoreManager().recordAssist(player, killer, mob, killed.hasMetadata("MH:hasBonus"));
			MobHunting.getRewardManager().depositPlayer(player, cash);
			Messages.debug("%s got a on assist reward (%s)", player.getName(),
					MobHunting.getRewardManager().format(cash));

			if (ks != 1.0)
				Messages.playerActionBarMessage(player, ChatColor.GREEN + "" + ChatColor.ITALIC + Messages
						.getString("mobhunting.moneygain.assist", "prize", MobHunting.getRewardManager().format(cash)));
			else
				Messages.playerActionBarMessage(player,
						ChatColor.GREEN + "" + ChatColor.ITALIC
								+ Messages.getString("mobhunting.moneygain.assist.bonuses", "prize",
										MobHunting.getRewardManager().format(cash), "bonuses",
										String.format("x%.1f", ks)));
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
				|| (MobHunting.getConfigManager().getBaseKillPrize(event.getEntity()) <= 0
						&& MobHunting.getConfigManager().getKillConsoleCmd(event.getEntity()).equals(""))
				|| event.getSpawnReason() != SpawnReason.NATURAL)
			return;

		if (MobHunting.getMobHuntingManager().mRand.nextDouble() * 100 < MobHunting.getConfigManager().bonusMobChance) {
			MobHunting.getParticleManager().attachEffect(event.getEntity(), Effect.MOBSPAWNER_FLAMES);
			if (MobHunting.getMobHuntingManager().mRand.nextBoolean())
				event.getEntity()
						.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 3));
			else
				event.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2));
			event.getEntity().setMetadata("MH:hasBonus", new FixedMetadataValue(MobHunting.getInstance(), true));
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void spawnerMobSpawn(CreatureSpawnEvent event) {

		if (CitizensCompat.isNPC(event.getEntity()))
			return;

		if (!MobHunting.getMobHuntingManager().isHuntEnabledInWorld(event.getLocation().getWorld())
				|| (MobHunting.getConfigManager().getBaseKillPrize(event.getEntity()) == 0)
						&& MobHunting.getConfigManager().getKillConsoleCmd(event.getEntity()).equals(""))
			return;

		if (event.getSpawnReason() == SpawnReason.CUSTOM) {
			if (!MobHunting.getConfigManager().allowCustomMobsSpawners) {
				// used for TARDISweepingAngels / CustomMobs / MythicMobs
				// Messages.debug("%s was spawned with SpawnReason.CUSTOM",
				// event.getEntityType());
				// event.getEntity().setMetadata("MH:blocked", new
				// FixedMetadataValue(MobHunting.getInstance(), true));
			}
		} else if (event.getSpawnReason() == SpawnReason.SPAWNER || event.getSpawnReason() == SpawnReason.SPAWNER_EGG) {
			if (!MobHunting.getConfigManager().allowMobSpawners)
				event.getEntity().setMetadata("MH:blocked", new FixedMetadataValue(MobHunting.getInstance(), true));
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void reinforcementMobSpawn(CreatureSpawnEvent event) {

		if (event.getSpawnReason() != SpawnReason.REINFORCEMENTS)
			return;

		LivingEntity mob = event.getEntity();

		if (CitizensCompat.isNPC(mob) && !CitizensCompat.isSentryOrSentinel(mob))
			return;

		if (!MobHunting.getMobHuntingManager().isHuntEnabledInWorld(event.getLocation().getWorld())
				|| (MobHunting.getConfigManager().getBaseKillPrize(mob) <= 0)
						&& MobHunting.getConfigManager().getKillConsoleCmd(mob).equals(""))
			return;

		event.getEntity().setMetadata("MH:reinforcement", new FixedMetadataValue(MobHunting.getInstance(), true));

	}

	public Set<IModifier> getHuntingModifiers() {
		return mHuntingModifiers;
	}

}
