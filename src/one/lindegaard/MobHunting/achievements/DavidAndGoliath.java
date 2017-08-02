package one.lindegaard.MobHunting.achievements;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.compatibility.SmartGiantsCompat;
import one.lindegaard.MobHunting.events.MobHuntKillEvent;

public class DavidAndGoliath implements Achievement, Listener {

	@Override
	public String getName() {
		return Messages.getString("achievements.davidandgoliath.name");
	}

	@Override
	public String getID() {
		return "davidandgoliath";
	}

	@Override
	public String getDescription() {
		return Messages.getString("achievements.davidandgoliath.description");
	}

	@Override
	public double getPrize() {
		return MobHunting.getConfigManager().davidAndGoliat;
	}

	@EventHandler
	public void onKill(MobHuntKillEvent event) {
		if (SmartGiantsCompat.isSmartGiants(event.getKilledEntity())
				&& event.getDamageInfo().getWeapon().getType() == Material.STONE_BUTTON
				&& !(MobHunting.getConfigManager().getBaseKillPrize(event.getKilledEntity()) == 0
						&& MobHunting.getConfigManager().getKillConsoleCmd(event.getKilledEntity()).isEmpty()))
			MobHunting.getAchievementManager().awardAchievement(this, event.getPlayer(),
					MobHunting.getExtendedMobManager().getExtendedMobFromEntity(event.getKilledEntity()));
	}

	@Override
	public String getPrizeCmd() {
		return MobHunting.getConfigManager().davidAndGoliatCmd;
	}

	@Override
	public String getPrizeCmdDescription() {
		return MobHunting.getConfigManager().davidAndGoliatCmdDesc;
	}

	@Override
	public ItemStack getSymbol() {
		return new ItemStack(Material.SKULL_ITEM, 1, (short) 2);
	}

}
