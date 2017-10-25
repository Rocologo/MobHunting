package one.lindegaard.MobHunting.vault;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.storage.PlayerSettings;
import one.lindegaard.MobHunting.util.Misc;

public class BackOfGoldEconomy implements Economy {

	private MobHunting plugin;

	public BackOfGoldEconomy(MobHunting plugin) {
		this.plugin = plugin;
	}

	/**
	 * Returns the amount the bank has
	 * 
	 * @param name
	 *            of the account
	 * @return EconomyResponse Object
	 */
	@Override
	public EconomyResponse bankBalance(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Deposit an amount into a bank account - DO NOT USE NEGATIVE AMOUNTS
	 * 
	 * @param name
	 *            of the account
	 * @param amount
	 *            to deposit
	 * @return EconomyResponse Object
	 */
	@Override
	public EconomyResponse bankDeposit(String arg0, double arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Returns true or false whether the bank has the amount specified - DO NOT
	 * USE NEGATIVE AMOUNTS
	 * 
	 * @param name
	 *            of the account
	 * @param amount
	 *            to check for
	 * @return EconomyResponse Object
	 */
	@Override
	public EconomyResponse bankHas(String name, double amount) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Withdraw an amount from a bank account - DO NOT USE NEGATIVE AMOUNTS
	 * 
	 * @param name
	 *            of the account
	 * @param amount
	 *            to withdraw
	 * @return EconomyResponse Object
	 */
	@Override
	public EconomyResponse bankWithdraw(String name, double amount) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @deprecated As of VaultAPI 1.4 use
	 *             {{@link #createBank(String, OfflinePlayer)} instead.
	 */
	@Override
	public EconomyResponse createBank(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Creates a bank account with the specified name and the player as the
	 * owner
	 * 
	 * @param name
	 *            of account
	 * @param player
	 *            the account should be linked to
	 * @return EconomyResponse Object
	 */
	@Override
	public EconomyResponse createBank(String name, OfflinePlayer player) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @deprecated As of VaultAPI 1.4 use
	 *             {{@link #createPlayerAccount(OfflinePlayer)} instead.
	 */
	@Override
	public boolean createPlayerAccount(String name) {
		createPlayerAccount(Bukkit.getServer().getOfflinePlayer(name));
		return true;
	}

	/**
	 * Attempts to create a player account for the given player
	 * 
	 * @param player
	 *            OfflinePlayer
	 * @return if the account creation was successful
	 */
	@Override
	public boolean createPlayerAccount(OfflinePlayer offlinePlayer) {
		return true;
	}

	/**
	 * @deprecated As of VaultAPI 1.4 use
	 *             {{@link #createPlayerAccount(OfflinePlayer, String)} instead.
	 */
	@Override
	public boolean createPlayerAccount(String name, String playername) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Attempts to create a player account for the given player on the specified
	 * world IMPLEMENTATION SPECIFIC - if an economy plugin does not support
	 * this the global balance will be returned.
	 * 
	 * @param player
	 *            OfflinePlayer
	 * @param worldName
	 *            String name of the world
	 * @return if the account creation was successful
	 */
	@Override
	public boolean createPlayerAccount(OfflinePlayer arg0, String arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Returns the name of the currency in plural form. If the economy being
	 * used does not support currency names then an empty string will be
	 * returned.
	 * 
	 * @return name of the currency (plural)
	 */
	@Override
	public String currencyNamePlural() {
		return MobHunting.getConfigManager().dropMoneyOnGroundSkullRewardNamePlural;
	}

	/**
	 * Returns the name of the currency in singular form. If the economy being
	 * used does not support currency names then an empty string will be
	 * returned.
	 * 
	 * @return name of the currency (singular)
	 */
	@Override
	public String currencyNameSingular() {
		return MobHunting.getConfigManager().dropMoneyOnGroundSkullRewardName;
	}

	/**
	 * Deletes a bank account with the specified name.
	 * 
	 * @param name
	 *            of the back to delete
	 * @return if the operation completed successfully
	 */
	@Override
	public EconomyResponse deleteBank(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @deprecated As of VaultAPI 1.4 use
	 *             {@link #depositPlayer(OfflinePlayer, double)} instead.
	 */
	@Override
	public EconomyResponse depositPlayer(String name, double amount) {
		return depositPlayer(Bukkit.getOfflinePlayer(name), amount);
	}

	/**
	 * Deposit an amount to a player - DO NOT USE NEGATIVE AMOUNTS
	 * 
	 * @param player
	 *            to deposit to
	 * @param amount
	 *            Amount to deposit
	 * @return Detailed response of transaction
	 */
	@Override
	public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, double amount) {
		if (offlinePlayer != null) {
			PlayerSettings ps = plugin.getPlayerSettingsmanager().getPlayerSettings(offlinePlayer);
			if (offlinePlayer.isOnline()) {
				ps.setBalance(Misc.round(ps.getBalance() + amount));
				plugin.getRewardManager().adjustBagOfGoldInPlayerInventory((Player) offlinePlayer, amount);
			} else {
				ps.setBalanceChanges(Misc.round(ps.getBalanceChanges() + amount));
			}
			plugin.getPlayerSettingsmanager().save(offlinePlayer);
		}
		return null;
	}

	/**
	 * @deprecated As of VaultAPI 1.4 use
	 *             {@link #depositPlayer(OfflinePlayer, String, double)}
	 *             instead.
	 */
	@Override
	public EconomyResponse depositPlayer(String arg0, String arg1, double arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Deposit an amount to a player - DO NOT USE NEGATIVE AMOUNTS
	 * IMPLEMENTATION SPECIFIC - if an economy plugin does not support this the
	 * global balance will be returned.
	 * 
	 * @param player
	 *            to deposit to
	 * @param worldName
	 *            name of the world
	 * @param amount
	 *            Amount to deposit
	 * @return Detailed response of transaction
	 */
	@Override
	public EconomyResponse depositPlayer(OfflinePlayer arg0, String arg1, double arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Format amount into a human readable String This provides translation into
	 * economy specific formatting to improve consistency between plugins.
	 *
	 * @param amount
	 *            to format
	 * @return Human readable string describing amount
	 */
	@Override
	public String format(double money) {
		Locale locale = new Locale("en", "UK");
		String pattern = "#.#####";
		DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getNumberInstance(locale);
		decimalFormat.applyPattern(pattern);
		return ChatColor.valueOf(MobHunting.getConfigManager().dropMoneyOnGroundTextColor)
				+ (MobHunting.getConfigManager().dropMoneyOnGroundItemtype.equalsIgnoreCase("ITEM")
						? decimalFormat.format(money)
						: MobHunting.getConfigManager().dropMoneyOnGroundSkullRewardName + " ("
								+ decimalFormat.format(money) + ")");
	}

	/**
	 * Some economy plugins round off after a certain number of digits. This
	 * function returns the number of digits the plugin keeps or -1 if no
	 * rounding occurs.
	 * 
	 * @return number of digits after the decimal point kept
	 */
	@Override
	public int fractionalDigits() {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @deprecated As of VaultAPI 1.4 use {@link #getBalance(OfflinePlayer)}
	 *             instead.
	 */
	@Override
	public double getBalance(String playername) {
		return getBalance(Bukkit.getOfflinePlayer(playername));
	}

	/**
	 * Gets balance of a player
	 * 
	 * @param player
	 *            of the player
	 * @return Amount currently held in players account
	 */
	@Override
	public double getBalance(OfflinePlayer offlinePlayer) {
		if (offlinePlayer != null)
			return plugin.getPlayerSettingsmanager().getBalance(offlinePlayer);
		return 0;
	}

	/**
	 * @deprecated As of VaultAPI 1.4 use
	 *             {@link #getBalance(OfflinePlayer, String)} instead.
	 */
	@Override
	public double getBalance(String playername, String world) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * Gets balance of a player on the specified world. IMPLEMENTATION SPECIFIC
	 * - if an economy plugin does not support this the global balance will be
	 * returned.
	 * 
	 * @param player
	 *            to check
	 * @param world
	 *            name of the world
	 * @return Amount currently held in players account
	 */
	@Override
	public double getBalance(OfflinePlayer arg0, String world) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * Gets the list of banks
	 * 
	 * @return the List of Banks
	 */
	@Override
	public List<String> getBanks() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Gets name of economy method
	 * 
	 * @return Name of Economy Method
	 */
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @deprecated As of VaultAPI 1.4 use {@link #has(OfflinePlayer, double)}
	 *             instead.
	 */
	@Override
	public boolean has(String name, double amount) {
		return has(Bukkit.getOfflinePlayer(name), amount);
	}

	/**
	 * Checks if the player account has the amount - DO NOT USE NEGATIVE AMOUNTS
	 * 
	 * @param player
	 *            to check
	 * @param amount
	 *            to check for
	 * @return True if <b>player</b> has <b>amount</b>, False else wise
	 */
	@Override
	public boolean has(OfflinePlayer offlinePlayer, double amount) {
		if (offlinePlayer == null)
			return false;
		return plugin.getPlayerSettingsmanager().getBalance(offlinePlayer) >= amount;
	}

	/**
	 * @deprecated As of VaultAPI 1.4 use @{link
	 *             {@link #has(OfflinePlayer, String, double)} instead.
	 */
	@Override
	public boolean has(String playername, String world, double amount) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Checks if the player account has the amount in a given world - DO NOT USE
	 * NEGATIVE AMOUNTS IMPLEMENTATION SPECIFIC - if an economy plugin does not
	 * support this the global balance will be returned.
	 * 
	 * @param player
	 *            to check
	 * @param worldName
	 *            to check with
	 * @param amount
	 *            to check for
	 * @return True if <b>player</b> has <b>amount</b>, False else wise
	 */
	@Override
	public boolean has(OfflinePlayer player, String world, double amount) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * 
	 * @deprecated As of VaultAPI 1.4 use {@link #hasAccount(OfflinePlayer)}
	 *             instead.
	 */
	@Override
	public boolean hasAccount(String playername) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
     * Checks if this player has an account on the server yet
     * This will always return true if the player has joined the server at least once
     * as all major economy plugins auto-generate a player account when the player joins the server
     * 
     * @param player to check
     * @return if the player has an account
     */
	@Override
	public boolean hasAccount(OfflinePlayer player) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Checks if this player has an account on the server yet on the given world
	 * This will always return true if the player has joined the server at least
	 * once as all major economy plugins auto-generate a player account when the
	 * player joins the server
	 * 
	 * @param player
	 *            to check in the world
	 * @param worldName
	 *            world-specific account
	 * @return if the player has an account
	 */
	@Override
	public boolean hasAccount(String playername, String world) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasAccount(OfflinePlayer arg0, String arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Returns true if the given implementation supports banks.
	 * 
	 * @return true if the implementation supports banks
	 */
	@Override
	public boolean hasBankSupport() {
		return false;
	}

	/**
	 * @deprecated As of VaultAPI 1.4 use
	 *             {{@link #isBankMember(String, OfflinePlayer)} instead.
	 */
	@Override
	public EconomyResponse isBankMember(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Check if the player is a member of the bank account
	 * 
	 * @param name
	 *            of the account
	 * @param player
	 *            to check membership
	 * @return EconomyResponse Object
	 */
	@Override
	public EconomyResponse isBankMember(String name, OfflinePlayer player) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @deprecated As of VaultAPI 1.4 use
	 *             {{@link #isBankOwner(String, OfflinePlayer)} instead.
	 */
	@Override
	public EconomyResponse isBankOwner(String name, String playername) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Check if a player is the owner of a bank account
	 * 
	 * @param name
	 *            of the account
	 * @param player
	 *            to check for ownership
	 * @return EconomyResponse Object
	 */
	@Override
	public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Checks if economy method is enabled.
	 * 
	 * @return Success or Failure
	 */
	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @deprecated As of VaultAPI 1.4 use
	 *             {@link #withdrawPlayer(OfflinePlayer, double)} instead.
	 */
	@Override
	public EconomyResponse withdrawPlayer(String name, double amount) {
		return withdrawPlayer(Bukkit.getOfflinePlayer(name), amount);
	}

	/**
	 * Withdraw an amount from a player - DO NOT USE NEGATIVE AMOUNTS
	 * 
	 * @param player
	 *            to withdraw from
	 * @param amount
	 *            Amount to withdraw
	 * @return Detailed response of transaction
	 */
	@Override
	public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, double amount) {
		if (offlinePlayer != null) {
			PlayerSettings ps = plugin.getPlayerSettingsmanager().getPlayerSettings(offlinePlayer);
			if (offlinePlayer.isOnline()) {
				double balance = plugin.getPlayerSettingsmanager().getBalance(offlinePlayer);
				if (balance >= amount) {
					ps.setBalance(ps.getBalance() - amount);
					plugin.getRewardManager().adjustBagOfGoldInPlayerInventory((Player) offlinePlayer, -amount);
				} else {
					ps.setBalance(0);
					plugin.getRewardManager().adjustBagOfGoldInPlayerInventory((Player) offlinePlayer, -balance);
				}
			} else {
				ps.setBalanceChanges(ps.getBalanceChanges() - amount);
			}
			plugin.getPlayerSettingsmanager().save(offlinePlayer);
		}
		return null;
	}

	/**
	 * @deprecated As of VaultAPI 1.4 use
	 *             {@link #withdrawPlayer(OfflinePlayer, String, double)}
	 *             instead.
	 */
	@Override
	public EconomyResponse withdrawPlayer(String arg0, String arg1, double arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Withdraw an amount from a player on a given world - DO NOT USE NEGATIVE
	 * AMOUNTS IMPLEMENTATION SPECIFIC - if an economy plugin does not support
	 * this the global balance will be returned.
	 * 
	 * @param player
	 *            to withdraw from
	 * @param worldName
	 *            - name of the world
	 * @param amount
	 *            Amount to withdraw
	 * @return Detailed response of transaction
	 */
	@Override
	public EconomyResponse withdrawPlayer(OfflinePlayer arg0, String arg1, double arg2) {
		// TODO Auto-generated method stub
		return null;
	}

}
