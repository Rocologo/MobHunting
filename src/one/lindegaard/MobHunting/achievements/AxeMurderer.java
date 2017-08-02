package one.lindegaard.MobHunting.achievements;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.events.MobHuntKillEvent;
import one.lindegaard.MobHunting.util.Misc;

public class AxeMurderer implements Achievement, Listener {

	@Override
	public String getName() {
		return Messages.getString("achievements.axemurderer.name");
	}

	@Override
	public String getID() {
		return "axemurderer";
	}

	@Override
	public String getDescription() {
		return Messages.getString("achievements.axemurderer.description");
	}

	@Override
	public double getPrize() {
		return MobHunting.getConfigManager().specialAxeMurderer;
	}

	@EventHandler
	public void onKill(MobHuntKillEvent event) {
		if (Misc.isAxe(event.getDamageInfo().getWeapon())
				&& MobHunting.getConfigManager().getBaseKillPrize(event.getKilledEntity()) > 0)
			MobHunting.getAchievementManager().awardAchievement(this, event.getPlayer(),
					MobHunting.getExtendedMobManager().getExtendedMobFromEntity(event.getKilledEntity()));
	}

	@Override
	public String getPrizeCmd() {
		return MobHunting.getConfigManager().specialAxeMurdererCmd;
	}

	@Override
	public String getPrizeCmdDescription() {
		return MobHunting.getConfigManager().specialAxeMurdererCmdDesc;
	}

	@Override
	public ItemStack getSymbol() {
		return new ItemStack(Material.WOOD_AXE);
	}

}
