package one.lindegaard.MobHunting.achievements;

import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.events.MobHuntKillEvent;
import org.bukkit.entity.Creeper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class CreeperBoxing extends AbstractSkullAchievement implements Listener {

	@Override
	public String getName() {
		return Messages.getString("achievements.creeperboxing.name");
	}

	@Override
	public String getID() {
		return "creeperboxing";
	}

	@Override
	public String getDescription() {
		return Messages.getString("achievements.creeperboxing.description");
	}

	@Override
	public double getPrize() {
		return MobHunting.getConfigManager().specialCreeperPunch;
	}

	@EventHandler
	public void onKill(MobHuntKillEvent event) {
		if (event.getKilledEntity() instanceof Creeper && !event.getDamageInfo().hasUsedWeapon()
				&& MobHunting.getConfigManager().getBaseKillPrize(event.getKilledEntity()) > 0)
			MobHunting.getAchievementManager().awardAchievement(this, event.getPlayer(),
					MobHunting.getExtendedMobManager().getExtendedMobFromEntity(event.getKilledEntity()));
	}

	@Override
	public String getPrizeCmd() {
		return MobHunting.getConfigManager().specialCreeperPunchCmd;
	}

	@Override
	public String getPrizeCmdDescription() {
		return MobHunting.getConfigManager().specialCreeperPunchCmdDesc;
	}

}
