package one.lindegaard.MobHunting.storage;

import org.bukkit.OfflinePlayer;

import one.lindegaard.MobHunting.MobHunting;

public class PlayerSettings {
	private OfflinePlayer player;
	private int playerId;
	private boolean learning_mode=false;
	private boolean mute=false;

	public PlayerSettings(OfflinePlayer player, boolean learning_mode, boolean mute) {
		this.player = player;
		this.learning_mode = learning_mode;
		this.mute = mute;
	}

	public PlayerSettings(OfflinePlayer player) {
		this.player = player;
		this.learning_mode = MobHunting.getConfigManager().learningMode;
		this.mute = false;
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
				"PlayerStore: {player: Id:%s Name:%s, Learning Mode: %s, Mute Mode: %s}",
				playerId, player.getName(), learning_mode, mute);
	}

	public int getPlayerId() {
		return playerId;
	}

	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}

}
