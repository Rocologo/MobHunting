package one.lindegaard.MobHunting.compatibility;

import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.api.ResidenceApi;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class ResidenceHelper {

	public static boolean isProtected(Player player) {
		if (ResidenceCompat.isSupported()) {

			ResidencePlayer residencePlayer = ResidenceApi.getPlayerManager().getResidencePlayer(player.getName());

			List<ClaimedResidence> residenseList = residencePlayer.getResList();

			for (ClaimedResidence res : residenseList) {
				if (res.containsLoc(player.getLocation())) {
					Map<String, Boolean> flags = res.getPermissions().getFlags();
					for (String flag: flags.keySet()){
						if (flag.equalsIgnoreCase("damage") && !flags.get(flag))
							return true;
					}

				}
			}
		}
		return false;
	}
}
