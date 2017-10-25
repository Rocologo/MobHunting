package one.lindegaard.MobHunting.storage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.StatType;
import one.lindegaard.MobHunting.achievements.Achievement;
import one.lindegaard.MobHunting.achievements.ProgressAchievement;
import one.lindegaard.MobHunting.bounty.Bounty;
import one.lindegaard.MobHunting.bounty.BountyStatus;
import one.lindegaard.MobHunting.mobs.ExtendedMob;
import one.lindegaard.MobHunting.mobs.MinecraftMob;
import one.lindegaard.MobHunting.mobs.MobPlugin;
import one.lindegaard.MobHunting.storage.asynch.AchievementRetrieverTask;
import one.lindegaard.MobHunting.storage.asynch.IDataStoreTask;
import one.lindegaard.MobHunting.storage.asynch.PlayerSettingsRetrieverTask;
import one.lindegaard.MobHunting.storage.asynch.StatRetrieverTask;
import one.lindegaard.MobHunting.storage.asynch.StoreTask;
import one.lindegaard.MobHunting.storage.asynch.AchievementRetrieverTask.Mode;
import one.lindegaard.MobHunting.storage.asynch.BountyRetrieverTask;

public class DataStoreManager {

	private MobHunting plugin;

	// Accessed on multiple threads
	private final HashSet<Object> mWaiting = new HashSet<Object>();

	// Accessed only from these threads
	private IDataStore mStore;
	private boolean mExit = false;

	// Accessed only from store thread
	private StoreThread mStoreThread;

	// Accessed only from retrieve thread
	private TaskThread mTaskThread;

	public DataStoreManager(MobHunting plugin, IDataStore store) {
		this.plugin = plugin;
		mStore = store;
		mTaskThread = new TaskThread();
		int savePeriod = MobHunting.getConfigManager().savePeriod;
		if (savePeriod < 1200) {
			savePeriod = 1200;
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED
					+ "[MobHunting][Warning] save-period in your config.yml is too low. Please raise it to 1200 or higher");
		}
		mStoreThread = new StoreThread(savePeriod);
	}

	public boolean isRunning() {
		return mTaskThread.getState() != Thread.State.WAITING && mTaskThread.getState() != Thread.State.TERMINATED
				&& mStoreThread.getState() != Thread.State.WAITING
				&& mStoreThread.getState() != Thread.State.TERMINATED;
	}

	// **************************************************************************************
	// PlayerStats
	// **************************************************************************************
	public void recordKill(OfflinePlayer player, ExtendedMob mob, boolean bonusMob, double cash) {
		synchronized (mWaiting) {
			mWaiting.add(new StatStore(StatType.fromMobType(mob, true), mob, player, 1, cash));

			if (bonusMob)
				mWaiting.add(new StatStore(StatType.fromMobType(
						new ExtendedMob(MinecraftMob.BonusMob.ordinal(), MobPlugin.Minecraft, "BonusMob"), true), mob,
						player, 1, cash));
		}
	}

	public void recordAssist(OfflinePlayer player, OfflinePlayer killer, ExtendedMob mob, boolean bonusMob,
			double cash) {
		synchronized (mWaiting) {
			mWaiting.add(new StatStore(StatType.fromMobType(mob, false), mob, player, 1, cash));

			if (bonusMob)
				mWaiting.add(new StatStore(StatType.fromMobType(
						new ExtendedMob(MinecraftMob.BonusMob.ordinal(), MobPlugin.Minecraft, "BonusMob"), false), mob,
						player, 1, cash));
		}
	}

	public void recordCash(OfflinePlayer player, ExtendedMob mob, boolean bonusMob, double cash) {
		synchronized (mWaiting) {
			mWaiting.add(new StatStore(StatType.fromMobType(mob, true), mob, player, 0, cash));

			if (bonusMob)
				mWaiting.add(new StatStore(StatType.fromMobType(
						new ExtendedMob(MinecraftMob.BonusMob.ordinal(), MobPlugin.Minecraft, "BonusMob"), true), mob,
						player, 0, cash));
		}
	}

	public void requestStats(StatType type, TimePeriod period, int count, IDataCallback<List<StatStore>> callback) {
		mTaskThread.addTask(new StatRetrieverTask(type, period, count, mWaiting), callback);
	}

	// **************************************************************************************
	// Achievements
	// **************************************************************************************
	public void recordAchievement(OfflinePlayer player, Achievement achievement, ExtendedMob mob) {
		synchronized (mWaiting) {
			mWaiting.add(new AchievementStore(achievement.getID(), player, -1));
			mWaiting.add(new StatStore(StatType.AchievementCount, mob, player));
		}
	}

	public void recordAchievementProgress(OfflinePlayer player, ProgressAchievement achievement, int progress) {
		synchronized (mWaiting) {
			mWaiting.add(new AchievementStore(achievement.getID(), player, progress));
		}
	}

	public void requestAllAchievements(OfflinePlayer player, IDataCallback<Set<AchievementStore>> callback) {
		mTaskThread.addTask(new AchievementRetrieverTask(Mode.All, player, mWaiting), callback);
	}

	public void requestCompletedAchievements(OfflinePlayer player, IDataCallback<Set<AchievementStore>> callback) {
		mTaskThread.addTask(new AchievementRetrieverTask(Mode.Completed, player, mWaiting), callback);
	}

	public void requestInProgressAchievements(OfflinePlayer player, IDataCallback<Set<AchievementStore>> callback) {
		mTaskThread.addTask(new AchievementRetrieverTask(Mode.InProgress, player, mWaiting), callback);
	}

	// *****************************************************************************
	// Bounties
	// *****************************************************************************
	public void updateBounty(Bounty bounty) {
		synchronized (mWaiting) {
			mWaiting.add(new Bounty(plugin, bounty));
		}
	}

	public void requestBounties(BountyStatus mode, OfflinePlayer player, IDataCallback<Set<Bounty>> callback) {
		mTaskThread.addTask(new BountyRetrieverTask(plugin, mode, player, mWaiting), callback);
	}

	// *****************************************************************************
	// PlayerSettings
	// *****************************************************************************
	public void requestPlayerSettings(OfflinePlayer player, IDataCallback<PlayerSettings> callback) {
		mTaskThread.addTask(new PlayerSettingsRetrieverTask(player, mWaiting), callback);
	}

	/**
	 * Update the playerSettings in the Database
	 * 
	 * @param offlinePlayer
	 * @param learning_mode
	 * @param muted
	 */
	public void updatePlayerSettings(OfflinePlayer offlinePlayer, boolean learning_mode, boolean muted, double balance,
			double balanceChanges, double bankBalance, double bankBalanceChanges) {
		synchronized (mWaiting) {
			mWaiting.add(new PlayerSettings(offlinePlayer, learning_mode, muted, balance, balanceChanges, bankBalance,
					bankBalanceChanges));
		}
	}

	/**
	 * Gets an offline player using the last known name. WARNING: This does a
	 * database lookup directly. This will block waiting for a reply
	 */
	public OfflinePlayer getPlayerByName(String name) {
		try {
			return mStore.getPlayerByName(name);
		} catch (UserNotFoundException e) {
			return null;
		} catch (DataStoreException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Get the playerId from the database
	 * 
	 * @param offlinePlayer
	 * @return
	 * @throws UserNotFoundException
	 */
	public int getPlayerId(OfflinePlayer offlinePlayer) throws UserNotFoundException {
		try {
			return mStore.getPlayerId(offlinePlayer);
		} catch (DataStoreException e) {
			if (MobHunting.getConfigManager().killDebug)
				e.printStackTrace();
		}
		throw new UserNotFoundException(
				"[MobHunting] User " + offlinePlayer.getName() + " is not present in MobHunting database");
	}

	// *****************************************************************************
	// Common
	// *****************************************************************************
	/**
	 * Flush all waiting data to the database
	 */
	public void flush() {
		if (mWaiting.size() != 0) {
			Messages.debug("Flushing waiting %s data to database...", mWaiting.size());
			mTaskThread.addTask(new StoreTask(mWaiting), null);
		}
	}

	/**
	 * Shutdown the DataStoreManager
	 */
	public void shutdown() {
		mExit = true;
		flush();
		mTaskThread.setWriteOnlyMode(true);
		int n = 0;
		try {
			while (mTaskThread.getState() != Thread.State.WAITING && mTaskThread.getState() != Thread.State.TERMINATED
					&& n < 40) {
				Thread.sleep(500);
				n++;
			}
			Messages.debug("mTaskThread.state=%s", mTaskThread.getState());
			if (mTaskThread.getState() == Thread.State.RUNNABLE) {
				Messages.debug("Interupting mTaskThread");
				mTaskThread.interrupt();
			}
			Messages.debug("mStoreThread.state=%s", mStoreThread.getState());
			Messages.debug("Interupting mStoreThread");
			mStoreThread.interrupt();
			Messages.debug("mTaskThread.state=%s", mTaskThread.getState());
			if (mTaskThread.getState() != Thread.State.WAITING) {
				mTaskThread.waitForEmptyQueue();
			} else {
				Messages.debug("Interupting mTaskThread");
				mTaskThread.interrupt();
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Wait until all data has been updated
	 */
	public void waitForUpdates() {
		flush();
		try {
			mTaskThread.waitForEmptyQueue();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Constructor for the StoreThread
	 * 
	 * @author Rocologo
	 *
	 */
	private class StoreThread extends Thread {
		private int mSaveInterval;

		public StoreThread(int interval) {
			super("MH StoreThread");
			start();
			mSaveInterval = interval;
		}

		@Override
		public void run() {
			try {
				while (true) {
					synchronized (this) {
						if (mExit && mWaiting.size() == 0) {
							break;
						}
					}
					mTaskThread.addTask(new StoreTask(mWaiting), null);

					Bukkit.getScheduler().runTask(MobHunting.getInstance(), new Runnable() {
						@Override
						public void run() {
							MobHunting.getGrindingManager().saveData();
						}
					});

					Thread.sleep(mSaveInterval * 50);
				}
			} catch (InterruptedException e) {
				Messages.debug("StoreThread was interrupted");
			}
		}
	}

	private class Task {
		public Task(IDataStoreTask<?> task, IDataCallback<?> callback) {
			this.task = task;
			this.callback = callback;
		}

		public IDataStoreTask<?> task;

		public IDataCallback<?> callback;
	}

	private class CallbackCaller implements Runnable {
		private IDataCallback<Object> mCallback;
		private Object mObj;
		private boolean mSuccess;

		public CallbackCaller(IDataCallback<Object> callback, Object obj, boolean success) {
			mCallback = callback;
			mObj = obj;
			mSuccess = success;
		}

		@Override
		public void run() {
			if (mSuccess)
				mCallback.onCompleted(mObj);
			else
				mCallback.onError((Throwable) mObj);
		}

	}

	private class TaskThread extends Thread {
		private BlockingQueue<Task> mQueue;
		private boolean mWritesOnly = false;

		private Object mSignal = new Object();

		public TaskThread() {
			super("MH TaskThread");

			mQueue = new LinkedBlockingQueue<Task>();

			start();
		}

		public void waitForEmptyQueue() throws InterruptedException {
			if (mQueue.isEmpty())
				return;

			synchronized (mSignal) {
				Messages.debug("waitForEmptyQueue: Waiting for %s+%s tasks to finish before closing connections.",
						mQueue.size(), mWaiting.size());
				while (!mQueue.isEmpty())
					mSignal.wait();
			}
		}

		public void setWriteOnlyMode(boolean writes) {
			mWritesOnly = writes;
		}

		public <T> void addTask(IDataStoreTask<T> storeTask, IDataCallback<T> callback) {
			try {
				mQueue.put(new Task(storeTask, callback));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			try {
				while (true) {
					if (mQueue.isEmpty())
						synchronized (mSignal) {
							mSignal.notifyAll();
						}
					// } else { //DONT ENABLE THIS CAUSES 100 CPU USAGE

					Task task = mQueue.take();

					if (mWritesOnly && task.task.readOnly())
						continue;

					try {

						Object result = task.task.run(mStore);

						if (task.callback != null && !mExit)
							Bukkit.getScheduler().runTask(MobHunting.getInstance(),
									new CallbackCaller((IDataCallback<Object>) task.callback, result, true));

					} catch (DataStoreException e) {
						Messages.debug("DataStoreManager: TaskThread.run() failed!!!!!!!");
						if (task.callback != null)
							Bukkit.getScheduler().runTask(MobHunting.getInstance(),
									new CallbackCaller((IDataCallback<Object>) task.callback, e, false));
						else
							e.printStackTrace();
					}
				}

			} catch (InterruptedException e) {
				Messages.debug(" TaskThread was interrupted");
			}
		}
	}

}
