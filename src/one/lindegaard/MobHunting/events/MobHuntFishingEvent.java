package one.lindegaard.MobHunting.events;

import java.util.HashMap;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MobHuntFishingEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

	private Player mPlayer;
	private Entity mEntity;
	private double mReward;
	HashMap<String,Double> mMultiplierList = new HashMap<String,Double>();
	private boolean mIsCancelled = false;

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public MobHuntFishingEvent(Player player, Entity fish, double cash, HashMap<String,Double> multiplierList) {
		mPlayer = player;
		mEntity = fish;
		mReward = cash;
		mMultiplierList = multiplierList;
	}

	public double getBasicReward() {
		return mReward;
	}

	public HashMap<String, Double> getMultipliers() {
		return mMultiplierList;
	}

	public Entity getFish() {
		return mEntity;
	}

	public Player getPlayer() {
		return mPlayer;
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
