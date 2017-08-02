package one.lindegaard.MobHunting.achievements;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.events.MobHuntFishingEvent;
import one.lindegaard.MobHunting.events.MobHuntKillEvent;
import one.lindegaard.MobHunting.mobs.ExtendedMob;
import one.lindegaard.MobHunting.mobs.MobPlugin;

public class EighthHuntAchievement implements ProgressAchievement, Listener {

	private ExtendedMob mExtendedMob;

	public EighthHuntAchievement(ExtendedMob extendedMob) {
		mExtendedMob = extendedMob;
	}

	@Override
	public String getName() {
		return Messages.getString("achievements.hunter.8.name", "mob", mExtendedMob.getFriendlyName());
	}

	@Override
	public String getID() {
		if (mExtendedMob.getMobPlugin() == MobPlugin.Minecraft)
			return "hunting-level8-" + mExtendedMob.getMobName().toLowerCase();
		else
			return mExtendedMob.getMobPlugin().name().toLowerCase() + "-hunting-level8-" + mExtendedMob.getMobtype().toLowerCase();

	}

	@Override
	public String getDescription() {
		return Messages.getString("achievements.hunter.8.description", "count", getNextLevel(), "mob",
				mExtendedMob.getFriendlyName());
	}

	@Override
	public double getPrize() {
		return MobHunting.getConfigManager().specialHunter8;
	}

	@Override
	public int getNextLevel() {
		return mExtendedMob.getProgressAchievementLevel1() * 500;
	}

	@Override
	public String inheritFrom() {
		if (mExtendedMob.getMobPlugin() == MobPlugin.Minecraft)
			return "hunting-level7-" + mExtendedMob.getMobtype().toLowerCase();
		else
			return mExtendedMob.getMobPlugin().name().toLowerCase() + "-hunting-level7-" + mExtendedMob.getMobtype().toLowerCase();
	}

	@Override
	public String nextLevelId() {
		return null;
	}

	@Override
	public String getPrizeCmd() {
		return MobHunting.getConfigManager().specialHunter8Cmd;
	}

	@Override
	public String getPrizeCmdDescription() {
		return MobHunting.getConfigManager().specialHunter8CmdDesc;
	}

	@Override
	public ItemStack getSymbol() {
		return mExtendedMob.getCustomHead(mExtendedMob.getMobName(), 7, 0);
	}

	@Override
	public ExtendedMob getExtendedMob() {
		return mExtendedMob;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onKillCompleted(MobHuntKillEvent event) {
		if (mExtendedMob.matches(event.getKilledEntity()))
			MobHunting.getAchievementManager().awardAchievementProgress(this, event.getPlayer(),
					MobHunting.getExtendedMobManager().getExtendedMobFromEntity(event.getKilledEntity()), 1);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onFishingCompleted(MobHuntFishingEvent event) {
		if (mExtendedMob.matches(event.getFish())) {
			MobHunting.getAchievementManager().awardAchievementProgress(this, event.getPlayer(),
					MobHunting.getExtendedMobManager().getExtendedMobFromEntity(event.getFish()), 1);
		}
	}
}
