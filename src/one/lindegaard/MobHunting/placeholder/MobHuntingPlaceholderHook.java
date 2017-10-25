package one.lindegaard.MobHunting.placeholder;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import me.clip.placeholderapi.external.EZPlaceholderHook;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.compatibility.PlaceholderAPICompat;

public class MobHuntingPlaceholderHook extends EZPlaceholderHook implements Listener {

	public MobHuntingPlaceholderHook(Plugin plugin) {
		super(plugin, "mobhunting");
		Messages.debug("PlaceHolderHook started");
	}

	@Override
	public String onPlaceholderRequest(Player player, String identifier) {

		// Remember to update the documentation when adding new placeholders
		// https://www.spigotmc.org/wiki/mobhunting-placeholders/
		
		// placeholder: %mobhunting_ping%
		if (identifier.equals("ping")) {
			return "pong";
		}

		// placeholder: %mobhunting_dropped_rewards%
		if (identifier.equals("dropped_rewards")) {
			return String.valueOf(MobHunting.getInstance().getRewardManager().getDroppedMoney().size());
		}

		// placeholder: %mobhunting_dropped_rewards%
		if (identifier.equals("dropped_money")) {
			double amt = 0;
			for (double d : MobHunting.getInstance().getRewardManager().getDroppedMoney().values())
				amt = amt + d;
			return MobHunting.getInstance().getRewardManager().format(amt);
		}

		// always check if the player is null for placeholders related to the
		// player!
		if (player == null) {
			return "";
		}

		// placeholder: %mobhunting_total_kills%
		if (identifier.equals("total_kills")) {
			return String.valueOf(PlaceholderAPICompat.getPlaceHolders().get(player.getUniqueId()).getTotal_kills());
		}

		// placeholder: %mobhunting_total_cash%
		if (identifier.equals("total_cash")) {
			return MobHunting.getInstance().getRewardManager()
					.format(PlaceholderAPICompat.getPlaceHolders().get(player.getUniqueId()).getTotal_cash());
		}

		// placeholder: %mobhunting_rank%
		if (identifier.equals("rank")) {
			return String.valueOf(PlaceholderAPICompat.getPlaceHolders().get(player.getUniqueId()).getRank());
		}

		// placeholder: %mobhunting_balance%
		if (identifier.equals("balance")) {
			return String.valueOf(MobHunting.getInstance().getPlayerSettingsmanager().getBalance(player));
		}

		// anything else someone types is invalid because we never defined
		// %customplaceholder_<what they want a value for>%
		// we can just return null so the placeholder they specified is not
		// replaced.
		return null;
	}

}
