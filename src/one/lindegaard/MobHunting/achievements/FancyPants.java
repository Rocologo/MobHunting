package one.lindegaard.MobHunting.achievements;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.events.MobHuntKillEvent;

public class FancyPants implements Achievement, Listener {

	@Override
	public String getName() {
		return Messages.getString("achievements.fancypants.name");
	}

	@Override
	public String getID() {
		return "fancypants";
	}

	@Override
	public String getDescription() {
		return Messages.getString("achievements.fancypants.description");
	}

	@Override
	public double getPrize() {
		return MobHunting.getConfigManager().specialFancyPants;
	}

	@EventHandler
	public void onKill(MobHuntKillEvent event) {
		if (event.getDamageInfo().getWeapon().getType() == Material.DIAMOND_SWORD
				&& !event.getDamageInfo().getWeapon().getEnchantments().isEmpty()
				&& event.getPlayer().getInventory().getHelmet() != null
				&& event.getPlayer().getInventory().getHelmet().getType() == Material.DIAMOND_HELMET
				&& !event.getPlayer().getInventory().getHelmet().getEnchantments().isEmpty()
				&& event.getPlayer().getInventory().getChestplate() != null
				&& event.getPlayer().getInventory().getChestplate().getType() == Material.DIAMOND_CHESTPLATE
				&& !event.getPlayer().getInventory().getChestplate().getEnchantments().isEmpty()
				&& event.getPlayer().getInventory().getLeggings() != null
				&& event.getPlayer().getInventory().getLeggings().getType() == Material.DIAMOND_LEGGINGS
				&& !event.getPlayer().getInventory().getLeggings().getEnchantments().isEmpty()
				&& event.getPlayer().getInventory().getBoots() != null
				&& event.getPlayer().getInventory().getBoots().getType() == Material.DIAMOND_BOOTS
				&& !event.getPlayer().getInventory().getBoots().getEnchantments().isEmpty()
				&& MobHunting.getConfigManager().getBaseKillPrize(event.getKilledEntity()) > 0)
			MobHunting.getAchievementManager().awardAchievement(this, event.getPlayer(),
					MobHunting.getExtendedMobManager().getExtendedMobFromEntity(event.getKilledEntity()));
	}

	@Override
	public String getPrizeCmd() {
		return MobHunting.getConfigManager().specialFancyPantsCmd;
	}

	@Override
	public String getPrizeCmdDescription() {
		return MobHunting.getConfigManager().specialFancyPantsCmdDesc;
	}

	@Override
	public ItemStack getSymbol() {
		return new ItemStack(Material.DIAMOND_LEGGINGS);
	}
}
