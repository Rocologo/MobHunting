package one.lindegaard.MobHunting.achievements;

import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.events.MobHuntKillEvent;

public class JustInTime implements Achievement, Listener {

	@Override
	public String getName() {
		return Messages.getString("achievements.justintime.name");
	}

	@Override
	public String getID() {
		return "justintime";
	}

	@Override
	public String getDescription() {
		return Messages.getString("achievements.justintime.description");
	}

	@Override
	public double getPrize() {
		return MobHunting.getConfigManager().specialJustInTime;
	}

	@EventHandler
	public void onKill(MobHuntKillEvent event) {
		// getTime() return world time in ticks. 0 ticks = 6:00 500=6:30
		// Zombies begin burning about 5:30 = 23500
		// player get a reward if he kills between 5:30 and 6:00.
		if (event.getKilledEntity().getWorld().getEnvironment().equals(Environment.NORMAL)
				&& MobHunting.getConfigManager().getBaseKillPrize(event.getKilledEntity()) > 0
				&& (event.getKilledEntity().getWorld().getTime() >= 23500
						&& event.getKilledEntity().getWorld().getTime() <= 24000)
				&& event.getKilledEntity().getFireTicks() > 0)
			MobHunting.getAchievementManager().awardAchievement(this, event.getPlayer(),
					MobHunting.getExtendedMobManager().getExtendedMobFromEntity(event.getKilledEntity()));
	}

	@Override
	public String getPrizeCmd() {
		return MobHunting.getConfigManager().specialJustInTimeCmd;
	}

	@Override
	public String getPrizeCmdDescription() {
		return MobHunting.getConfigManager().specialJustInTimeCmdDesc;
	}

	@Override
	public ItemStack getSymbol() {
		return new ItemStack(Material.WATCH);
	}
}
