package one.lindegaard.MobHunting;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import one.lindegaard.MobHunting.rewards.Reward;
import one.lindegaard.MobHunting.storage.DataStoreException;
import one.lindegaard.MobHunting.storage.IDataCallback;
import one.lindegaard.MobHunting.storage.PlayerSettings;

public class PlayerSettingsManager implements Listener {

	private HashMap<UUID, PlayerSettings> mPlayerSettings = new HashMap<UUID, PlayerSettings>();

	private MobHunting plugin;

	/**
	 * Constructor for the PlayerSettingsmanager
	 */
	PlayerSettingsManager(MobHunting plugin) {
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	/**
	 * Get playerSettings from memory
	 * 
	 * @param offlinePlayer
	 * @return PlayerSettings
	 */
	public PlayerSettings getPlayerSettings(OfflinePlayer offlinePlayer) {
		if (mPlayerSettings.containsKey(offlinePlayer.getUniqueId()))
			return mPlayerSettings.get(offlinePlayer.getUniqueId());
		else {
			try {
				PlayerSettings ps = MobHunting.getStoreManager().loadPlayerSettings(offlinePlayer);
				Messages.debug("%s is offline, fetching PlayerData from database", offlinePlayer.getName());
				return ps;
			} catch (DataStoreException | SQLException e) {
				Messages.debug("%s is not known on this server", offlinePlayer.getName());
				return new PlayerSettings(offlinePlayer);
			}
		}

	}

	/**
	 * Store playerSettings in memory
	 * 
	 * @param playerSettings
	 */
	public void setPlayerSettings(OfflinePlayer player, PlayerSettings playerSettings) {
		mPlayerSettings.put(player.getUniqueId(), playerSettings);
	}

	/**
	 * Remove PlayerSettings from Memory
	 * 
	 * @param player
	 */
	public void removePlayerSettings(OfflinePlayer player) {
		Messages.debug("Removing %s from player settings cache", player.getName());
		mPlayerSettings.remove(player.getUniqueId());
	}

	/**
	 * Read PlayerSettings From database into Memory when player joins
	 * 
	 * @param event
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	private void onPlayerJoin(PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		if (containsKey(player))
			Messages.debug("Using cached playersettings for %s. Balance=%s", player.getName(), getBalance(player));
		else {
			load(player);
		}
	}

	/**
	 * Write PlayerSettings to Database when Player Quit and remove
	 * PlayerSettings from memory
	 * 
	 * @param event
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	private void onPlayerQuit(PlayerQuitEvent event) {
		final Player player = event.getPlayer();
		save(player);
	}

	/**
	 * Load PlayerSettings asynchronously from Database
	 * 
	 * @param player
	 */
	public void load(final OfflinePlayer player) {
		MobHunting.getDataStoreManager().requestPlayerSettings(player, new IDataCallback<PlayerSettings>() {

			@Override
			public void onCompleted(PlayerSettings ps) {
				if (ps.isMuted())
					Messages.debug("%s isMuted()", player.getName());
				if (ps.isLearningMode())
					Messages.debug("%s is in LearningMode()", player.getName());
				mPlayerSettings.put(player.getUniqueId(), ps);
				//get Balance to check if balance in DB is the same as in player inventory
				double balance = getBalance(player);
				Messages.debug("%s balance=%s", player.getName(), balance);
			}

			@Override
			public void onError(Throwable error) {
				Bukkit.getConsoleSender().sendMessage(
						ChatColor.RED + "[MobHunting][ERROR] Could not load playerSettings for " + player.getName());
				mPlayerSettings.put(player.getUniqueId(), new PlayerSettings(player));
			}
		});
	}

	/**
	 * Write PlayerSettings to Database
	 * 
	 * @param player
	 */
	public void save(final OfflinePlayer player) {
		MobHunting.getDataStoreManager().updatePlayerSettings(player, getPlayerSettings(player).isLearningMode(),
				getPlayerSettings(player).isMuted(), getPlayerSettings(player).getBalance(),
				getPlayerSettings(player).getBalanceChanges(), getPlayerSettings(player).getBankBalance(),
				getPlayerSettings(player).getBankBalanceChanges());
	}

	/**
	 * Test if PlayerSettings contains data for Player
	 * 
	 * @param player
	 * @return true if player exists in PlayerSettings in Memory
	 */
	public boolean containsKey(final OfflinePlayer player) {
		return mPlayerSettings.containsKey(player.getUniqueId());
	}

	public double getBalance(OfflinePlayer offlinePlayer) {

		PlayerSettings ps = plugin.getPlayerSettingsmanager().getPlayerSettings(offlinePlayer);

		if (offlinePlayer.isOnline()) {
			Player player = (Player) offlinePlayer;
			double sum = 0;
			for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
				ItemStack is = player.getInventory().getItem(slot);
				if (Reward.isReward(is)) {
					Reward reward = Reward.getReward(is);
					if (reward.isBagOfGoldReward())
						sum = sum + reward.getMoney();
				}
			}
			//Messages.debug("Balance in Inventory=%s", sum);
			if (ps.getBalance() + ps.getBalanceChanges() != sum) {
				if (ps.getBalanceChanges() == 0) {
					Messages.debug("Warning %s has a player balance problem (%s,%s). Adjusting balance to %s",
							offlinePlayer.getName(), ps.getBalance(), sum, sum);
					ps.setBalance(sum);
					ps.setBankBalanceChanges(0);
					plugin.getPlayerSettingsmanager().setPlayerSettings(player, ps);
					MobHunting.getDataStoreManager().updatePlayerSettings(player, ps.isLearningMode(), ps.isMuted(),
							ps.getBalance(), ps.getBalanceChanges(), ps.getBankBalance(), ps.getBankBalanceChanges());
				} else {
					Messages.debug(
							"Warning %s has a player balance changes while offline (%s+%s). Adjusting balance to %s",
							offlinePlayer.getName(), ps.getBalance(), ps.getBalanceChanges(),
							ps.getBalance() + ps.getBalanceChanges());
					double taken = plugin.getRewardManager().adjustBagOfGoldInPlayerInventory(player,
							ps.getBalanceChanges());
					ps.setBalanceChanges(ps.getBalanceChanges() + taken);
					ps.setBalance(ps.getBalance() + ps.getBalanceChanges());
					plugin.getPlayerSettingsmanager().setPlayerSettings(player, ps);
					MobHunting.getDataStoreManager().updatePlayerSettings(player, ps.isLearningMode(), ps.isMuted(),
							ps.getBalance(), ps.getBalanceChanges(), ps.getBankBalance(), ps.getBankBalanceChanges());
				}
			}
		}

		return ps.getBalance() + ps.getBalanceChanges();

	}

}
