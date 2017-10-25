package one.lindegaard.MobHunting.storage;

import org.bukkit.OfflinePlayer;

import one.lindegaard.MobHunting.MobHunting;

public class PlayerSettings {
	private OfflinePlayer player;
	private int playerId;
	private boolean learning_mode = false;
	private boolean mute = false;
	private double balance = 0;
	private double balanceChanges = 0;
	private double bankBalance = 0;
	private double bankBalanceChanges = 0;

	public PlayerSettings(OfflinePlayer player, boolean learning_mode, boolean mute, double balance,
			double balanceChanges, double bankBalance, double bankBalanceChanges) {
		this.player = player;
		this.setLearningMode(learning_mode);
		this.setMuteMode(mute);
		this.setBalance(balance);
		this.setBalanceChanges(balanceChanges);
		this.setBankBalance(bankBalance);
		this.setBankBalanceChanges(bankBalanceChanges);

	}

	public PlayerSettings(OfflinePlayer player) {
		this.player = player;
		this.setLearningMode(MobHunting.getConfigManager().learningMode);
		this.setMuteMode(false);
		this.setBalance(MobHunting.getConfigManager().startingBalance);
		this.setBalanceChanges(0);
		this.setBankBalance(0);
		this.setBankBalanceChanges(0);
	}

	/**
	 * @return the learning mode (0:false, 1: true (in learning mode))
	 */
	public boolean isLearningMode() {
		return learning_mode;
	}

	/**
	 * @param learning_mode
	 *            learning mode for player (0:false, 1: true (in learning mode))
	 */
	public void setLearningMode(boolean learning_mode) {
		this.learning_mode = learning_mode;
	}

	/**
	 * @return the mute status (0:false (unmuted), 1: true (muted))
	 */
	public boolean isMuted() {
		return mute;
	}

	/**
	 * @param mute
	 *            the mute to set (0:false (unmuted), 1: true (muted))
	 */
	public void setMuteMode(boolean mute) {
		this.mute = mute;
	}

	/**
	 * @return the player
	 */
	public OfflinePlayer getPlayer() {
		return player;
	}

	/**
	 * @param player
	 *            the player to set
	 */
	public void setPlayer(OfflinePlayer player) {
		this.player = player;
	}

	/**
	 * convert data to a readable format.
	 */
	@Override
	public String toString() {
		return String.format(
				"PlayerStore: {player: Id:%s Name:%s, Learning: %s, Muted: %s, Balance: %s(-%s), Bank balance: %s(-%s)}",
				playerId, player.getName(), learning_mode, mute, balance, balanceChanges, bankBalance,
				bankBalanceChanges);
	}

	public int getPlayerId() {
		return playerId;
	}

	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}

	/**
	 * Get the players balance
	 * 
	 * @return
	 */
	public double getBalance() {
		return balance;
	}

	/**
	 * Set the players balance
	 * 
	 * @param balance
	 */
	public void setBalance(double balance) {
		this.balance = balance;
	}

	public double getBalanceChanges() {
		return balanceChanges;
	}

	public void setBalanceChanges(double balanceChanges) {
		this.balanceChanges = balanceChanges;
	}

	/**
	 * Get the players bank balance
	 * 
	 * @return
	 */
	public double getBankBalance() {
		return bankBalance;
	}

	/**
	 * Set the players bank balance
	 * 
	 * @param bankBalance
	 */
	public void setBankBalance(double bankBalance) {
		this.bankBalance = bankBalance;
	}

	public double getBankBalanceChanges() {
		return bankBalanceChanges;
	}

	public void setBankBalanceChanges(double bankBalanceChanges) {
		this.bankBalanceChanges = bankBalanceChanges;
	}

}
