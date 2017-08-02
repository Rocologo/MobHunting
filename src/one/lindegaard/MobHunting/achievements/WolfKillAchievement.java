package one.lindegaard.MobHunting.achievements;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.compatibility.MobArenaCompat;
import one.lindegaard.MobHunting.events.MobHuntKillEvent;
import one.lindegaard.MobHunting.mobs.ExtendedMob;
import one.lindegaard.MobHunting.mobs.MinecraftMob;
import one.lindegaard.MobHunting.mobs.MobPlugin;

public class WolfKillAchievement implements ProgressAchievement, Listener {

	@Override
	public String getName() {
		return Messages.getString("achievements.fangmaster.name");
	}

	@Override
	public String getID() {
		return "fangmaster";
	}

	@Override
	public String getDescription() {
		return Messages.getString("achievements.fangmaster.description");
	}

	@Override
	public double getPrize() {
		return MobHunting.getConfigManager().specialFangMaster;
	}

	@Override
	public int getNextLevel() {
		return 500;
	}

	@Override
	public String inheritFrom() {
		return null;
	}

	@Override
	public String nextLevelId() {
		return null;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onWolfKillMob(MobHuntKillEvent event) {
		if (!MobHunting.getMobHuntingManager().isHuntEnabledInWorld(event.getKilledEntity().getWorld())
				|| !(event.getKilledEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent)
				|| (MobHunting.getConfigManager().getBaseKillPrize(event.getKilledEntity()) <= 0))
			return;

		EntityDamageByEntityEvent dmg = (EntityDamageByEntityEvent) event.getKilledEntity().getLastDamageCause();

		if (!(dmg.getDamager() instanceof Wolf))
			return;

		Wolf killer = (Wolf) dmg.getDamager();

		if (killer.isTamed() && killer.getOwner() instanceof OfflinePlayer) {
			Player owner = ((OfflinePlayer) killer.getOwner()).getPlayer();

			if (owner != null && MobHunting.getMobHuntingManager().isHuntEnabled(owner)) {
				if (MobArenaCompat.isPlayingMobArena((Player) owner)
						&& !MobHunting.getConfigManager().mobarenaGetRewards) {
					Messages.debug("AchiveBlocked: FangMaster was achieved while %s was playing MobArena.",
							owner.getName());
					Messages.learn(owner, Messages.getString("mobhunting.learn.mobarena"));
				} else
					MobHunting.getAchievementManager().awardAchievementProgress(this, owner,
							MobHunting.getExtendedMobManager().getExtendedMobFromEntity(event.getKilledEntity()), 1);
			}
		}

	}

	@Override
	public String getPrizeCmd() {
		return MobHunting.getConfigManager().specialFangMasterCmd;
	}

	@Override
	public String getPrizeCmdDescription() {
		return MobHunting.getConfigManager().specialFangMasterCmdDesc;
	}

	@Override
	public ItemStack getSymbol() {
		return new ItemStack(Material.STRING);
	}

	@Override
	public ExtendedMob getExtendedMob() {
		return new ExtendedMob(MobPlugin.Minecraft, MinecraftMob.Wolf.name());
	}
}
