package one.lindegaard.MobHunting;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DamageInformation {
	private long time;

	private ItemStack weapon;
	private boolean usedWeapon;
	private boolean mele;
	private Player attacker;
	private Player assister;
	private long lastAttackTime = System.currentTimeMillis();
	private long lastAssistTime = System.currentTimeMillis();
	private Location attackerPosition; // TODO: Is this really needed???
	private boolean wolfAssist;
	private boolean wasFlying;
	private String crackShotWeapon = "";
	private Player crackShotPlayer;

	// Disguises
	private boolean playerUndercover; // Player attacking undercover (disguise)
	private boolean mobCoverBlown; // Player attacked a disguised Mob/Player

	/**
	 * The time where the attack happened.
	 * 
	 * @return time as a long
	 */
	public long getTime() {
		return time;
	}

	/**
	 * Set the time where the attack happened
	 * 
	 * @param time
	 */
	public void setTime(long time) {
		this.time = time;
	}

	/**
	 * @return the weapon
	 */
	public ItemStack getWeapon() {
		return weapon;
	}

	/**
	 * @param weapon
	 *            Set the weapon which was used for killing a mob
	 */
	public void setWeapon(ItemStack weapon) {
		this.weapon = weapon;
	}

	/**
	 * @return the usedWeapon
	 */
	public boolean hasUsedWeapon() {
		return usedWeapon;
	}

	/**
	 * @param usedWeapon
	 *            the usedWeapon to set
	 */
	public void setHasUsedWeapon(boolean usedWeapon) {
		this.usedWeapon = usedWeapon;
	}

	/**
	 * @return the mele
	 */
	public boolean isMeleWeapenUsed() {
		return mele;
	}

	/**
	 * @param mele
	 *            the mele to set
	 */
	public void setIsMeleWeaponUsed(boolean mele) {
		this.mele = mele;
	}

	/**
	 * @return the attacker
	 */
	public Player getAttacker() {
		return attacker;
	}

	/**
	 * @param attacker
	 *            the attacker to set
	 */
	public void setAttacker(Player attacker) {
		this.attacker = attacker;
	}

	/**
	 * @return the attackerPosition
	 */
	public Location getAttackerPosition() {
		return attackerPosition;
	}

	/**
	 * @param attackerPosition
	 *            the attackerPosition to set
	 */
	public void setAttackerPosition(Location attackerPosition) {
		this.attackerPosition = attackerPosition;
	}

	/**
	 * @return the assister
	 */
	public Player getAssister() {
		return assister;
	}

	/**
	 * @param assister
	 *            the assister to set
	 */
	public void setAssister(Player assister) {
		this.assister = assister;
	}

	/**
	 * @return the lastAttackTime
	 */
	public long getLastAttackTime() {
		return lastAttackTime;
	}

	/**
	 * @param lastAttackTime
	 *            the lastAttackTime to set
	 */
	public void setLastAttackTime(long lastAttackTime) {
		this.lastAttackTime = lastAttackTime;
	}

	/**
	 * @return the lastAssistTime
	 */
	public long getLastAssistTime() {
		return lastAssistTime;
	}

	/**
	 * @param lastAssistTime
	 *            the lastAssistTime to set
	 */
	public void setLastAssistTime(long lastAssistTime) {
		this.lastAssistTime = lastAssistTime;
	}

	/**
	 * @return the wolfAssist
	 */
	public boolean isWolfAssist() {
		return wolfAssist;
	}

	/**
	 * @param wolfAssist
	 *            the wolfAssist to set
	 */
	public void setIsMyPetAssist(boolean wolfAssist) {
		this.wolfAssist = wolfAssist;
	}

	/**
	 * @return the wasFlying
	 */
	public boolean wasFlying() {
		return wasFlying;
	}

	/**
	 * @param wasFlying
	 *            the wasFlying to set
	 */
	public void setWasFlying(boolean wasFlying) {
		this.wasFlying = wasFlying;
	}

	/**
	 * @return the playerUndercover
	 */
	public boolean isPlayerUndercover() {
		return playerUndercover;
	}

	/**
	 * @param playerUndercover
	 *            the playerUndercover to set
	 */
	public void setPlayerUndercover(boolean playerUndercover) {
		this.playerUndercover = playerUndercover;
	}

	/**
	 * @return the mobCoverBlown
	 */
	public boolean isMobCoverBlown() {
		return mobCoverBlown;
	}

	/**
	 * @param mobCoverBlown
	 *            the mobCoverBlown to set
	 */
	public void setMobCoverBlown(boolean mobCoverBlown) {
		this.mobCoverBlown = mobCoverBlown;
	}

	// CRACKSHOT Integration
	public boolean isCrackShotWeaponUsed() {
		return crackShotWeapon!=null && !crackShotWeapon.isEmpty();
	}

	public String getCrackShotWeaponUsed() {
		return crackShotWeapon;
	}

	public void setCrackShotWeapon(String weapon) {
		crackShotWeapon = weapon;
	}

	public Player getCrackShotPlayer() {
		return crackShotPlayer;
	}

	public void setCrackShotPlayer(Player crackShotPlayer) {
		this.crackShotPlayer = crackShotPlayer;
	}

}
