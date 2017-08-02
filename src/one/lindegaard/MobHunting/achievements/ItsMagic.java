package one.lindegaard.MobHunting.achievements;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.events.MobHuntKillEvent;

public class ItsMagic implements Achievement, Listener {

	@Override
	public String getName() {
		return Messages.getString("achievements.itsmagic.name");
	}

	@Override
	public String getID() {
		return "itsmagic";
	}

	@Override
	public String getDescription() {
		return Messages.getString("achievements.itsmagic.description");
	}

	@Override
	public double getPrize() {
		return MobHunting.getConfigManager().specialItsMagic;
	}

	@EventHandler
	public void onKill(MobHuntKillEvent event) {
		if (event.getDamageInfo().getWeapon().getType() == Material.POTION
				&& MobHunting.getConfigManager().getBaseKillPrize(event.getKilledEntity()) > 0)
			MobHunting.getAchievementManager().awardAchievement(this, event.getPlayer(),
					MobHunting.getExtendedMobManager().getExtendedMobFromEntity(event.getKilledEntity()));
	}

	@Override
	public String getPrizeCmd() {
		return MobHunting.getConfigManager().specialItsMagicCmd;
	}

	@Override
	public String getPrizeCmdDescription() {
		return MobHunting.getConfigManager().specialItsMagicCmdDesc;
	}

	@Override
	public ItemStack getSymbol() {
		return new ItemStack(Material.POTION);
	}
}
