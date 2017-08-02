package one.lindegaard.MobHunting.events;

import java.util.Set;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import one.lindegaard.MobHunting.bounty.Bounty;

public class BountyKillEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

	private String mWorldGroupName;
	private Entity mKiller;
	private OfflinePlayer mWantedPlayer;
	private Set<Bounty> mBounties;
	private boolean mIsCancelled = false;

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public BountyKillEvent(String worldGroupName, Entity killer, OfflinePlayer wantedPlayer, Set<Bounty> set) {
		mWorldGroupName = worldGroupName;
		mKiller = killer;
		mWantedPlayer = wantedPlayer;
		mBounties = set;
	}

	/**
	 * @return the WorldGroupName
	 */
	public String getWorldGroupName() {
		return mWorldGroupName;
	}

	/**
	 * @param worldGroupName
	 *            the WorldGroupName to set
	 */
	public void setWorldGroupName(String worldGroupName) {
		this.mWorldGroupName = worldGroupName;
	}

	/**
	 * @return the Killer
	 */
	public Entity getKiller() {
		return mKiller;
	}

	/**
	 * @param killer the Killer to set
	 */
	public void setKiller(Entity killer) {
		this.mKiller = killer;
	}

	/**
	 * @return the mWantedPlayer
	 */
	public OfflinePlayer getWantedPlayer() {
		return mWantedPlayer;
	}

	/**
	 * @param wantedPlayer
	 *            the wantedPlayer to set
	 */
	public void setWantedPlayer(OfflinePlayer wantedPlayer) {
		this.mWantedPlayer = wantedPlayer;
	}

	/**
	 * @return the Bounties
	 */
	public Set<Bounty> getBounties() {
		return mBounties;
	}

	/**
	 * @param bounties
	 *            the mBounties to set
	 */
	public void setBounties(Set<Bounty> bounties) {
		this.mBounties = bounties;
	}

	@Override
	public boolean isCancelled() {
		return mIsCancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		mIsCancelled = cancel;
	}

}
