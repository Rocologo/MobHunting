package one.lindegaard.MobHunting.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.ConsoleCommandSender;

import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.StatType;
import one.lindegaard.MobHunting.bounty.Bounty;
import one.lindegaard.MobHunting.mobs.MobPlugin;
import one.lindegaard.MobHunting.util.Misc;
import one.lindegaard.MobHunting.util.UUIDHelper;

public class SQLiteDataStore extends DatabaseDataStore {

	private MobHunting plugin;

	public SQLiteDataStore(MobHunting plugin) {
		super(plugin);
		this.plugin = plugin;
	}

	// *******************************************************************************
	// SETUP / INITIALIZE
	// *******************************************************************************

	@Override
	protected Connection setupConnection() throws DataStoreException {
		try {
			Class.forName("org.sqlite.JDBC");
			Connection c = DriverManager
					.getConnection("jdbc:sqlite:" + MobHunting.getInstance().getDataFolder().getPath() + "/"
							+ MobHunting.getConfigManager().databaseName + ".db");
			c.setAutoCommit(false);
			return c;
		} catch (ClassNotFoundException classNotFoundEx) {
			throw new DataStoreException("SQLite not present on the classpath", classNotFoundEx);
		} catch (SQLException sqlEx) {
			throw new DataStoreException("Error creating sql connection", sqlEx);
		}
	}

	@Override
	protected void openPreparedStatements(Connection connection, PreparedConnectionType preparedConnectionType)
			throws SQLException {
		switch (preparedConnectionType) {
		case GET_PLAYER_DATA:
			mGetPlayerData = connection.prepareStatement("SELECT * FROM mh_Players WHERE UUID=?;");
			break;
		case SAVE_ACHIEVEMENTS:
			mSaveAchievement = connection.prepareStatement("INSERT OR REPLACE INTO mh_Achievements VALUES(?,?,?,?);");
			break;
		case SAVE_PLAYER_STATS:
			mSavePlayerStats = connection.prepareStatement(
					"INSERT OR IGNORE INTO mh_Daily(ID, MOB_ID, PLAYER_ID) VALUES(strftime(\"%Y%j\",\"now\"),?,?);");
			break;
		case LOAD_ARCHIEVEMENTS:
			mLoadAchievements = connection
					.prepareStatement("SELECT ACHIEVEMENT, DATE, PROGRESS FROM mh_Achievements WHERE PLAYER_ID = ?;");
			break;
		case GET_PLAYER_UUID:
			mGetPlayerUUID = connection.prepareStatement("SELECT UUID FROM mh_Players WHERE NAME=?;");
			break;
		case UPDATE_PLAYER_NAME:
			mUpdatePlayerName = connection.prepareStatement("UPDATE mh_Players SET NAME=? WHERE UUID=?;");
			break;
		case INSERT_PLAYER_DATA:
			mInsertPlayerData = connection.prepareStatement(
					"INSERT INTO mh_Players (UUID,NAME,PLAYER_ID,LEARNING_MODE,MUTE_MODE,BALANCE,BALANCE_CHANGES,BANK_BALANCE,BANK_BALANCE_CHNAGES) "
							+ "VALUES(?,?,(SELECT IFNULL(MAX(PLAYER_ID),0)+1 FROM mh_Players),?,?,?,?,?,?);");
			break;
		case UPDATE_PLAYER_SETTINGS:
			mUpdatePlayerSettings = connection
					.prepareStatement("UPDATE mh_Players SET LEARNING_MODE=?,MUTE_MODE=?,BALANCE=?,BALANCE_CHANGES=?,BANK_BALANCE=?,BANK_BALANCE_CHANGES=? WHERE UUID=?;");
			break;
		case GET_BOUNTIES:
			mGetBounties = connection.prepareStatement(
					"SELECT * FROM mh_Bounties where STATUS=0 AND (BOUNTYOWNER_ID=? OR WANTEDPLAYER_ID=? OR NOT NPC_ID=0);");
			break;
		case INSERT_BOUNTY:
			mInsertBounty = connection.prepareStatement("REPLACE INTO mh_Bounties "
					+ "(MOBTYPE, BOUNTYOWNER_ID, WANTEDPLAYER_ID, NPC_ID, MOB_ID, WORLDGROUP, "
					+ "CREATED_DATE, END_DATE, PRIZE, MESSAGE, STATUS) " + " VALUES (?,?,?,?,?,?,?,?,?,?,?);");
			break;
		case UPDATE_BOUNTY:
			mUpdateBounty = connection.prepareStatement("UPDATE mh_Bounties SET PRIZE=?,MESSAGE=?,END_DATE=?,STATUS=?"
					+ " WHERE WANTEDPLAYER_ID=? AND BOUNTYOWNER_ID=? AND WORLDGROUP=?;");
			break;
		case GET_PLAYER_BY_PLAYER_ID:
			mGetPlayerByPlayerId = connection.prepareStatement("SELECT UUID FROM mh_Players WHERE PLAYER_ID=?;");
			break;
		case DELETE_BOUNTY:
			mDeleteBounty = connection.prepareStatement(
					"DELETE FROM mh_Bounties WHERE WANTEDPLAYER_ID=? AND BOUNTYOWNER_ID=? AND WORLDGROUP=?;");
			break;
		case LOAD_MOBS:
			mLoadMobs = connection.prepareStatement("SELECT * FROM mh_Mobs;");
			break;
		case INSERT_MOBS:
			mInsertMobs = connection.prepareStatement("INSERT INTO mh_Mobs (PLUGIN_ID, MOBTYPE) VALUES (?,?);");
			break;
		case UPDATE_MOBS:
			mUpdateMobs = connection
					.prepareStatement("UPDATE mh_Mobs (PLUGIN_ID,MOBTYPE) VALUES (?,?) WHERE MOB_ID=?;");
			break;
		default:
			break;
		}
	}

	// *******************************************************************************
	// LoadStats / SaveStats
	// *******************************************************************************
	@SuppressWarnings("deprecation")
	@Override
	public List<StatStore> loadPlayerStats(StatType type, TimePeriod period, int count) throws DataStoreException {
		ArrayList<StatStore> list = new ArrayList<StatStore>();
		String id;
		// If The NPC has an invalid period or timeperiod return an empty list
		if (period == null || type == null)
			return list;
		switch (period) {
		case Day:
			id = "strftime('%Y%j','now')";
			break;
		case Week:
			id = "strftime('%Y%W','now')";
			break;
		case Month:
			id = "strftime('%Y%m','now')";
			break;
		case Year:
			id = "strftime('%Y','now')";
			break;
		default:
			id = null;
			break;
		}

		MobPlugin mobPlugin = MobPlugin.Minecraft;
		String mobType = type.getDBColumn().substring(0, type.getDBColumn().lastIndexOf("_"));
		ArrayList<String> plugins_kill = new ArrayList<String>();
		ArrayList<String> plugins_assist = new ArrayList<String>();
		ArrayList<String> plugins_cash = new ArrayList<String>();
		for (MobPlugin p : MobPlugin.values()) {
			plugins_kill.add(p.name() + "_kill");
			plugins_assist.add(p.name() + "_assist");
			plugins_cash.add(p.name() + "_cash");
			if (p.name().equalsIgnoreCase(type.getDBColumn().substring(0, type.getDBColumn().indexOf("_")))) {
				mobPlugin = p;
				if (type.getDBColumn().indexOf("_") != type.getDBColumn().lastIndexOf("_"))
					mobType = type.getDBColumn().substring(type.getDBColumn().indexOf("_") + 1,
							type.getDBColumn().lastIndexOf("_"));
			}
		}

		String column = "";
		if (type.getDBColumn().equalsIgnoreCase("achievement_count"))
			column = "sum(achievement_count) amount ";
		else if (type.getDBColumn().equalsIgnoreCase("total_kill"))
			column = "sum(total_kill) amount ";
		else if (type.getDBColumn().equalsIgnoreCase("total_assist"))
			column = "sum(total_assist) amount ";
		else if (plugins_kill.contains(type.getDBColumn()))
			column = "mh_Mobs.plugin_id, sum(total_kill) amount ";
		else if (plugins_assist.contains(type.getDBColumn()))
			column = "mh_Mobs.plugin_id, sum(total_assist) amount ";
		else if (type.getDBColumn().substring(type.getDBColumn().lastIndexOf("_"), type.getDBColumn().length())
				.equalsIgnoreCase("_kill"))
			column = "mh_Mobs.mob_id, mh_Mobs.MOBTYPE mt, sum(total_kill) amount ";
		else if (type.getDBColumn().substring(type.getDBColumn().lastIndexOf("_"), type.getDBColumn().length())
				.equalsIgnoreCase("_assist"))
			column = "mh_Mobs.mob_id, mh_Mobs.MOBTYPE mt, sum(total_assist) amount ";
		else
			column = "sum(total_kill) amount ";

		column = column + ", sum(total_cash) CASH";

		String wherepart = "";
		if (type.getDBColumn().equalsIgnoreCase("total_kill") || type.getDBColumn().equalsIgnoreCase("total_assist")
				|| type.getDBColumn().equalsIgnoreCase("achievement_count")
				|| type.getDBColumn().equalsIgnoreCase("total_cash")) {
			wherepart = (id != null ? " AND ID=" + id : "");
		} else if (plugins_kill.contains(type.getDBColumn()) || plugins_assist.contains(type.getDBColumn())
				|| plugins_cash.contains(type.getDBColumn())) {
			wherepart = (id != null ? " AND ID=" + id + " AND mh_Mobs.PLUGIN_ID=" + mobPlugin.getId()
					: " AND mh_Mobs.PLUGIN_ID=" + mobPlugin.getId());
		} else {
			wherepart = (id != null
					? " AND ID=" + id + " and mh_Mobs.MOB_ID="
							+ MobHunting.getExtendedMobManager().getMobIdFromMobTypeAndPluginID(mobType, mobPlugin)
					: " AND mh_Mobs.MOB_ID="
							+ MobHunting.getExtendedMobManager().getMobIdFromMobTypeAndPluginID(mobType, mobPlugin));
		}

		try {
			Connection mConnection = setupConnection();

			Statement statement = mConnection.createStatement();
			String exestr = "SELECT " + column + ", PLAYER_ID, mh_Players.UUID uuid, mh_Players.NAME name" + " from mh_"
					+ period.getTable() + " inner join mh_Players using (PLAYER_ID)"
					+ " inner join mh_Mobs using (MOB_ID) WHERE PLAYER_ID!=0 AND NAME IS NOT NULL " + wherepart
					+ " GROUP BY PLAYER_ID ORDER BY "
					+ ((type.getDBColumn().equalsIgnoreCase("total_cash") || plugins_cash.contains(type.getDBColumn()))
							? "CASH" : "AMOUNT")
					+ " DESC LIMIT " + count;
			// Messages.debug("Load str=%s",exestr);
			ResultSet results = statement.executeQuery(exestr);
			while (results.next()) {
				OfflinePlayer offlinePlayer = null;
				try {
					if (results.getString("uuid").equals(""))
						offlinePlayer = Bukkit.getOfflinePlayer(results.getString("name"));
					else
						offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(results.getString("uuid")));
				} catch (Exception e) {
					Bukkit.getLogger()
							.warning("Could not find player name for PLAYER_ID:" + results.getString("PLAYER_ID"));
				}
				if (offlinePlayer == null)
					Messages.debug("getOfflinePlayer(%s) was not in cache.", results.getString("name"));
				else
					list.add(new StatStore(type, offlinePlayer, results.getInt("amount"), results.getDouble("cash")));
			}
			results.close();
			statement.close();
			mConnection.close();
			return list;
		} catch (SQLException e) {
			throw new DataStoreException(e);
		}
	}

	@Override
	public void savePlayerStats(Set<StatStore> stats) throws DataStoreException {
		Connection mConnection = setupConnection();
		try {
			Messages.debug("Saving PlayerStats to Database.");
			openPreparedStatements(mConnection, PreparedConnectionType.SAVE_PLAYER_STATS);
			mSavePlayerStats.clearBatch();
			for (StatStore st : stats) {
				int mob_id = 0;
				if (!st.getType().getDBColumn().substring(0, st.getType().getDBColumn().lastIndexOf("_"))
						.equalsIgnoreCase("achievement"))
					mob_id = st.getMob().getMob_id();
				mSavePlayerStats.setInt(2, getPlayerId(st.getPlayer()));
				mSavePlayerStats.setInt(1, mob_id);
				mSavePlayerStats.addBatch();
			}
			mSavePlayerStats.executeBatch();
			mSavePlayerStats.close();
			mConnection.commit();

			// Now add each of the stats
			Statement statement = mConnection.createStatement();
			for (StatStore stat : stats) {
				String column = "";
				String column2 = "";
				int mob_id = stat.getMob().getMob_id();
				if (stat.getType().getDBColumn().substring(0, stat.getType().getDBColumn().lastIndexOf("_"))
						.equalsIgnoreCase("achievement")) {
					column = "achievement_count";
				} else {
					column = "total" + stat.getType().getDBColumn().substring(
							stat.getType().getDBColumn().lastIndexOf("_"), stat.getType().getDBColumn().length());
				}
				column2 = "total_cash";
				int amount = stat.getAmount();
				double cash = Misc.round(stat.getCash());
				int player_id = getPlayerId(stat.getPlayer());
				String str = String.format(Locale.US,
						"UPDATE mh_Daily SET %1$s = %1$s + %2$d, %5$s = %5$s + %6$f WHERE ID = strftime(\"%%Y%%j\",\"now\")"
								+ " AND MOB_ID=%3$d AND PLAYER_ID = %4$d;",
						column, amount, mob_id, player_id, column2, cash);
				// Messages.debug("Save Str=%s", str);
				statement.addBatch(str);
			}
			statement.executeBatch();
			statement.close();
			mConnection.commit();
			mConnection.close();
			Messages.debug("Saved.");
		} catch (SQLException e) {
			rollback(mConnection);
			throw new DataStoreException(e);
		}
	}

	@Override
	public void saveBounties(Set<Bounty> bountyDataSet) throws DataStoreException {
		Connection mConnection;
		try {
			mConnection = setupConnection();
			try {
				openPreparedStatements(mConnection, PreparedConnectionType.INSERT_BOUNTY);
				for (Bounty bounty : bountyDataSet) {
					if (bounty.getBountyOwner() == null)
						Messages.debug("RandomBounty to be inserted: %s", bounty.toString());
					int bountyOwnerId = getPlayerId(bounty.getBountyOwner());
					int wantedPlayerId = getPlayerId(bounty.getWantedPlayer());
					mInsertBounty.setString(1, bounty.getMobtype());
					mInsertBounty.setInt(2, bountyOwnerId);
					mInsertBounty.setInt(3, wantedPlayerId);
					mInsertBounty.setInt(4, bounty.getNpcId());
					mInsertBounty.setString(5, bounty.getMobId());
					mInsertBounty.setString(6, bounty.getWorldGroup());
					mInsertBounty.setLong(7, bounty.getCreatedDate());
					mInsertBounty.setLong(8, bounty.getEndDate());
					mInsertBounty.setDouble(9, bounty.getPrize());
					mInsertBounty.setString(10, bounty.getMessage());
					mInsertBounty.setInt(11, bounty.getStatus().getValue());

					mInsertBounty.addBatch();
				}
				mInsertBounty.executeBatch();
				mInsertBounty.close();

				mConnection.commit();
				mConnection.close();
			} catch (SQLException e) {
				rollback(mConnection);
				mConnection.close();
				throw new DataStoreException(e);
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
			throw new DataStoreException(e1);
		}

	};

	@Override
	public void databaseConvertToUtf8(String database_name) throws DataStoreException {
		ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
		console.sendMessage(ChatColor.RED + "[MobHunting] this command is only for MySQL");
	}

	// *******************************************************************************
	// V2 DATABASE SETUP / MIGRATION
	// *******************************************************************************

	@Override
	protected void setupV2Tables(Connection connection) throws SQLException {
		Statement create = connection.createStatement();
		// Prefix tables to mh_
		try {
			ResultSet rs = create.executeQuery("SELECT * from Players LIMIT 0");
			rs.close();
			create.executeUpdate("ALTER TABLE Players RENAME TO mh_Players");
			create.executeUpdate("ALTER TABLE Achievements RENAME TO mh_Achievements");
			create.executeUpdate("ALTER TABLE Daily RENAME TO mh_Daily");
			create.executeUpdate("ALTER TABLE Weekly RENAME TO mh_Weekly");
			create.executeUpdate("ALTER TABLE Monthly RENAME TO mh_Monthly");
			create.executeUpdate("ALTER TABLE Yearly RENAME TO mh_Yearly");
			create.executeUpdate("ALTER TABLE AllTime RENAME TO mh_AllTime");

			create.executeUpdate("DROP TRIGGER IF EXISTS DailyInsert");
			create.executeUpdate("DROP TRIGGER IF EXISTS DailyUpdate");

		} catch (SQLException e) {
		}

		// Create new empty tables if they do not exist
		String lm = MobHunting.getConfigManager().learningMode ? "1" : "0";
		create.executeUpdate("CREATE TABLE IF NOT EXISTS mh_Players (UUID TEXT PRIMARY KEY, NAME TEXT, "
				+ "PLAYER_ID INTEGER NOT NULL, LEARNING_MODE INTEGER NOT NULL DEFAULT " + lm
				+ ", MUTE_MODE INTEGER NOT NULL DEFAULT 0 )");
		String dataString = "";
		for (StatType type : StatType.values())
			dataString += ", " + type.getDBColumn() + " INTEGER NOT NULL DEFAULT 0";
		create.executeUpdate(
				"CREATE TABLE IF NOT EXISTS mh_Daily (ID CHAR(6) NOT NULL, PLAYER_ID INTEGER REFERENCES mh_Players(PLAYER_ID)"
						+ dataString + ", PRIMARY KEY(PLAYER_ID, ID))");
		create.executeUpdate(
				"CREATE TABLE IF NOT EXISTS mh_Weekly (ID CHAR(6) NOT NULL, PLAYER_ID INTEGER REFERENCES mh_Players(PLAYER_ID)"
						+ dataString + ", PRIMARY KEY(PLAYER_ID, ID))");
		create.executeUpdate(
				"CREATE TABLE IF NOT EXISTS mh_Monthly (ID CHAR(6) NOT NULL, PLAYER_ID INTEGER REFERENCES mh_Players(PLAYER_ID)"
						+ dataString + ", PRIMARY KEY(PLAYER_ID, ID))");
		create.executeUpdate(
				"CREATE TABLE IF NOT EXISTS mh_Yearly (ID CHAR(6) NOT NULL, PLAYER_ID INTEGER REFERENCES mh_Players(PLAYER_ID)"
						+ dataString + ", PRIMARY KEY(PLAYER_ID, ID))");
		create.executeUpdate("CREATE TABLE IF NOT EXISTS mh_AllTime (PLAYER_ID INTEGER REFERENCES mh_Players(PLAYER_ID)"
				+ dataString + ", PRIMARY KEY(PLAYER_ID))");
		create.executeUpdate("CREATE TABLE IF NOT EXISTS mh_Achievements "
				+ "(PLAYER_ID INTEGER REFERENCES mh_Players(PLAYER_ID) NOT NULL, ACHIEVEMENT TEXT NOT NULL, "
				+ "DATE INTEGER NOT NULL, PROGRESS INTEGER NOT NULL, PRIMARY KEY(PLAYER_ID, ACHIEVEMENT), "
				+ "FOREIGN KEY(PLAYER_ID) REFERENCES mh_Players(PLAYER_ID))");
		if (!MobHunting.getConfigManager().disablePlayerBounties)
			create.executeUpdate("CREATE TABLE IF NOT EXISTS mh_Bounties ("
					+ "BOUNTYOWNER_ID INTEGER REFERENCES mh_Players(PLAYER_ID) NOT NULL, " + "MOBTYPE TEXT, "
					+ "WANTEDPLAYER_ID INTEGER REFERENCES mh_Players(PLAYER_ID), " + "NPC_ID INTEGER, "
					+ "MOB_ID TEXT, " + "WORLDGROUP TEXT NOT NULL, " + "CREATED_DATE INTEGER NOT NULL, "
					+ "END_DATE INTEGER NOT NULL, " + "PRIZE FLOAT NOT NULL, " + "MESSAGE TEXT, "
					+ "STATUS INTEGER NOT NULL DEFAULT 0, "
					+ "PRIMARY KEY(WORLDGROUP, WANTEDPLAYER_ID, BOUNTYOWNER_ID), "
					+ "FOREIGN KEY(BOUNTYOWNER_ID) REFERENCES mh_Players(PLAYER_ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(WANTEDPLAYER_ID) REFERENCES mh_Players(PLAYER_ID) ON DELETE CASCADE" + ")");

		create.close();
		connection.commit();

		setupTriggerV2(connection);

		performUUIDMigrateV2(connection);
		performAddNewMobsIntoV2(connection);
	}

	private void setupTriggerV2(Connection connection) throws SQLException {

		Statement create = connection.createStatement();

		create.executeUpdate(
				"create trigger if not exists mh_DailyInsert after insert on mh_Daily begin insert or ignore into mh_Weekly(ID, PLAYER_ID) values(strftime(\"%Y%W\",\"now\"), NEW.PLAYER_ID); insert or ignore into mh_Monthly(ID, PLAYER_ID) values(strftime(\"%Y%m\",\"now\"), NEW.PLAYER_ID); insert or ignore into mh_Yearly(ID, PLAYER_ID) values(strftime(\"%Y\",\"now\"), NEW.PLAYER_ID); insert or ignore into mh_AllTime(PLAYER_ID) values(NEW.PLAYER_ID); end");

		// Create the cascade update trigger. It will allow us to only modify
		// the Daily table, and the rest will happen automatically
		StringBuilder updateStringBuilder = new StringBuilder();

		for (StatType type : StatType.values()) {
			if (updateStringBuilder.length() != 0)
				updateStringBuilder.append(", ");

			updateStringBuilder
					.append(String.format(Locale.US, "%s = (%1$s + (NEW.%1$s - OLD.%1$s)) ", type.getDBColumn()));
		}

		String updateString = updateStringBuilder.toString();

		StringBuilder updateTrigger = new StringBuilder();
		updateTrigger.append("create trigger if not exists mh_DailyUpdate after update on mh_Daily begin ");

		// Weekly
		updateTrigger.append(" update mh_Weekly set ");
		updateTrigger.append(updateString);
		updateTrigger.append(" where ID=strftime('%Y%W','now') AND PLAYER_ID=New.PLAYER_ID;");

		// Monthly
		updateTrigger.append(" update mh_Monthly set ");
		updateTrigger.append(updateString);
		updateTrigger.append(" where ID=strftime('%Y%m','now') AND PLAYER_ID=New.PLAYER_ID;");

		// Yearly
		updateTrigger.append(" update mh_Yearly set ");
		updateTrigger.append(updateString);
		updateTrigger.append(" where ID=strftime('%Y','now') AND PLAYER_ID=New.PLAYER_ID;");

		// AllTime
		updateTrigger.append(" update mh_AllTime set ");
		updateTrigger.append(updateString);
		updateTrigger.append(" where PLAYER_ID=New.PLAYER_ID;");

		updateTrigger.append("END");

		create.executeUpdate(updateTrigger.toString());
		create.close();
		connection.commit();

	}

	private void performTableMigrateFromV1ToV2(Connection connection) throws SQLException {
		Statement statement = connection.createStatement();
		try {
			ResultSet rs = statement.executeQuery("SELECT UUID from mh_Players LIMIT 0");
			rs.close();
			statement.close();
			return; // Tables will be fine
		} catch (SQLException e) {
		}

		statement.executeUpdate("ALTER TABLE mh_Players RENAME TO mh_PlayersOLD");
		statement.executeUpdate("ALTER TABLE mh_Achievements RENAME TO mh_AchievementsOLD");
		statement.executeUpdate("ALTER TABLE mh_Daily RENAME TO mh_DailyOLD");
		statement.executeUpdate("ALTER TABLE mh_Weekly RENAME TO mh_WeeklyOLD");
		statement.executeUpdate("ALTER TABLE mh_Monthly RENAME TO mh_MonthlyOLD");
		statement.executeUpdate("ALTER TABLE mh_Yearly RENAME TO mh_YearlyOLD");
		statement.executeUpdate("ALTER TABLE mh_AllTime RENAME TO mh_AllTimeOLD");

		// Create new empty tables if they do not exist
		statement.executeUpdate(
				"CREATE TABLE IF NOT EXISTS mh_Players (UUID TEXT PRIMARY KEY, NAME TEXT, PLAYER_ID INTEGER NOT NULL,"
						+ "LEARNIN_MODE INTEGER NOT NULL, MUTE_MODE INTEGER NOT NULL )");
		String dataString = "";
		for (StatType type : StatType.values())
			dataString += ", " + type.getDBColumn() + " INTEGER NOT NULL DEFAULT 0";
		statement.executeUpdate(
				"CREATE TABLE IF NOT EXISTS mh_Daily (ID CHAR(6) NOT NULL, PLAYER_ID INTEGER REFERENCES mh_Players(PLAYER_ID)" //$NON-NLS-1$
						+ dataString + ", PRIMARY KEY(ID, PLAYER_ID))");
		statement.executeUpdate(
				"CREATE TABLE IF NOT EXISTS mh_Weekly (ID CHAR(6) NOT NULL, PLAYER_ID INTEGER REFERENCES mh_Players(PLAYER_ID)" //$NON-NLS-1$
						+ dataString + ", PRIMARY KEY(ID, PLAYER_ID))");
		statement.executeUpdate(
				"CREATE TABLE IF NOT EXISTS mh_Monthly (ID CHAR(6) NOT NULL, PLAYER_ID INTEGER REFERENCES mh_Players(PLAYER_ID)" //$NON-NLS-1$
						+ dataString + ", PRIMARY KEY(ID, PLAYER_ID))");
		statement.executeUpdate(
				"CREATE TABLE IF NOT EXISTS mh_Yearly (ID CHAR(6) NOT NULL, PLAYER_ID INTEGER REFERENCES mh_Players(PLAYER_ID)" //$NON-NLS-1$
						+ dataString + ", PRIMARY KEY(ID, PLAYER_ID))");
		statement.executeUpdate(
				"CREATE TABLE IF NOT EXISTS mh_AllTime (PLAYER_ID INTEGER REFERENCES mh_Players(PLAYER_ID)" + dataString //$NON-NLS-1$
						+ ", PRIMARY KEY(PLAYER_ID))");
		statement.executeUpdate(
				"CREATE TABLE IF NOT EXISTS mh_Achievements (PLAYER_ID INTEGER REFERENCES mh_Players(PLAYER_ID) NOT NULL, ACHIEVEMENT TEXT NOT NULL, DATE INTEGER NOT NULL, PROGRESS INTEGER NOT NULL, PRIMARY KEY(PLAYER_ID, ACHIEVEMENT), FOREIGN KEY(PLAYER_ID) REFERENCES mh_Players(PLAYER_ID))"); //$NON-NLS-1$

		statement.executeUpdate("INSERT INTO mh_Players SELECT * FROM mh_PlayersOLD");
		statement.executeUpdate("INSERT INTO mh_Achievements SELECT * FROM mh_AchievementsOLD");
		statement.executeUpdate("INSERT INTO mh_Daily SELECT * FROM mh_DailyOLD");
		statement.executeUpdate("INSERT INTO mh_Weekly SELECT * FROM mh_WeeklyOLD");
		statement.executeUpdate("INSERT INTO mh_Monthly SELECT * FROM mh_MonthlyOLD");
		statement.executeUpdate("INSERT INTO mh_Yearly SELECT * FROM mh_YearlyOLD");
		statement.executeUpdate("INSERT INTO mh_AllTime SELECT * FROM mh_AllTimeOLD");

		statement.executeUpdate("DROP TABLE mh_Players");
		statement.executeUpdate("DROP TABLE mh_AchievementsOLD");
		statement.executeUpdate("DROP TABLE mh_DailyOLD");
		statement.executeUpdate("DROP TABLE mh_WeeklyOLD");
		statement.executeUpdate("DROP TABLE mh_MonthlyOLD");
		statement.executeUpdate("DROP TABLE mh_YearlyOLD");
		statement.executeUpdate("DROP TABLE mh_AllTimeOLD");
		statement.close();
		connection.commit();
	}

	private void performUUIDMigrateV2(Connection connection) throws SQLException {
		Statement statement = connection.createStatement();
		try {
			ResultSet rs = statement.executeQuery("SELECT UUID from mh_Players LIMIT 0");
			rs.close();
			statement.close();
			return; // UUIDs are in place
		} catch (SQLException e) {
			performTableMigrateFromV1ToV2(connection);
		}

		System.out.println("[MobHunting] Migrating MobHunting Database User ID to User UUID.");

		// Add missing columns
		performTableMigrateFromV1ToV2(connection);

		// Get UUID and update table
		ResultSet rs = statement.executeQuery("select `NAME`,`PLAYER_ID` from `mh_Players`");
		UUIDHelper.initialize();

		PreparedStatement insert = connection.prepareStatement("INSERT INTO mh_Players VALUES(?,?,?)");
		StringBuilder failString = new StringBuilder();
		int failCount = 0;
		while (rs.next()) {
			String player = rs.getString(1);
			int pId = rs.getInt(2);
			UUID id = UUIDHelper.getKnown(player);
			if (id != null) {
				insert.setString(1, id.toString());
				insert.setString(2, player);
				insert.setInt(3, pId);
				insert.addBatch();
			} else {
				if (failString.length() != 0)
					failString.append(", ");
				failString.append(player);
				++failCount;
			}
		}

		rs.close();
		UUIDHelper.clearCache();

		if (failCount > 0) {
			System.err.println("[MobHunting] " + failCount + " accounts failed to convert:");
			System.err.println("[MobHunting] " + failString.toString());
		}

		insert.executeBatch();
		insert.close();

		System.out.println("[MobHunting] Player UUID migration complete.");

		statement.close();
		connection.commit();

	}

	private void performAddNewMobsIntoV2(Connection connection) throws SQLException {
		Statement statement = connection.createStatement();

		try {
			ResultSet rs = statement.executeQuery("SELECT Bat_kill from `mh_Daily` LIMIT 0");
			rs.close();
		} catch (SQLException e) {

			System.out.println("[MobHunting] Adding Passive Mobs to MobHunting Database.");

			statement.executeUpdate("alter table `mh_Daily` add column `Bat_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Daily` add column `Bat_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Weekly` add column `Bat_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Weekly` add column `Bat_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Monthly` add column `Bat_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Monthly` add column `Bat_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Yearly` add column `Bat_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Yearly` add column `Bat_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_AllTime` add column `Bat_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_AllTime` add column `Bat_assist`  INTEGER NOT NULL DEFAULT 0");

			statement.executeUpdate("alter table `mh_Daily` add column `Chicken_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Daily` add column `Chicken_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Weekly` add column `Chicken_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Weekly` add column `Chicken_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Monthly` add column `Chicken_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Monthly` add column `Chicken_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Yearly` add column `Chicken_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Yearly` add column `Chicken_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_AllTime` add column `Chicken_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_AllTime` add column `Chicken_assist`  INTEGER NOT NULL DEFAULT 0");

			statement.executeUpdate("alter table `mh_Daily` add column `Cow_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Daily` add column `Cow_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Weekly` add column `Cow_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Weekly` add column `Cow_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Monthly` add column `Cow_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Monthly` add column `Cow_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Yearly` add column `Cow_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Yearly` add column `Cow_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_AllTime` add column `Cow_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_AllTime` add column `Cow_assist`  INTEGER NOT NULL DEFAULT 0");

			statement.executeUpdate("alter table `mh_Daily` add column `Horse_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Daily` add column `Horse_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Weekly` add column `Horse_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Weekly` add column `Horse_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Monthly` add column `Horse_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Monthly` add column `Horse_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Yearly` add column `Horse_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Yearly` add column `Horse_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_AllTime` add column `Horse_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_AllTime` add column `Horse_assist`  INTEGER NOT NULL DEFAULT 0");

			statement.executeUpdate("alter table `mh_Daily` add column `MushroomCow_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate(
					"alter table `mh_Daily` add column `MushroomCow_assist`  INTEGER NOT NULL DEFAULT 0");
			statement
					.executeUpdate("alter table `mh_Weekly` add column `MushroomCow_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate(
					"alter table `mh_Weekly` add column `MushroomCow_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate(
					"alter table `mh_Monthly` add column `MushroomCow_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate(
					"alter table `mh_Monthly` add column `MushroomCow_assist`  INTEGER NOT NULL DEFAULT 0");
			statement
					.executeUpdate("alter table `mh_Yearly` add column `MushroomCow_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate(
					"alter table `mh_Yearly` add column `MushroomCow_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate(
					"alter table `mh_AllTime` add column `MushroomCow_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate(
					"alter table `mh_AllTime` add column `MushroomCow_assist`  INTEGER NOT NULL DEFAULT 0");

			statement.executeUpdate("alter table `mh_Daily` add column `Ocelot_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Daily` add column `Ocelot_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Weekly` add column `Ocelot_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Weekly` add column `Ocelot_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Monthly` add column `Ocelot_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Monthly` add column `Ocelot_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Yearly` add column `Ocelot_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Yearly` add column `Ocelot_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_AllTime` add column `Ocelot_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_AllTime` add column `Ocelot_assist`  INTEGER NOT NULL DEFAULT 0");

			statement.executeUpdate("alter table `mh_Daily` add column `Pig_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Daily` add column `Pig_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Weekly` add column `Pig_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Weekly` add column `Pig_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Monthly` add column `Pig_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Monthly` add column `Pig_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Yearly` add column `Pig_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Yearly` add column `Pig_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_AllTime` add column `Pig_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_AllTime` add column `Pig_assist`  INTEGER NOT NULL DEFAULT 0");

			statement.executeUpdate(
					"alter table `mh_Daily` add column `PassiveRabbit_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate(
					"alter table `mh_Daily` add column `PassiveRabbit_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate(
					"alter table `mh_Weekly` add column `PassiveRabbit_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate(
					"alter table `mh_Weekly` add column `PassiveRabbit_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate(
					"alter table `mh_Monthly` add column `PassiveRabbit_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate(
					"alter table `mh_Monthly` add column `PassiveRabbit_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate(
					"alter table `mh_Yearly` add column `PassiveRabbit_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate(
					"alter table `mh_Yearly` add column `PassiveRabbit_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate(
					"alter table `mh_AllTime` add column `PassiveRabbit_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate(
					"alter table `mh_AllTime` add column `PassiveRabbit_assist`  INTEGER NOT NULL DEFAULT 0");

			statement.executeUpdate("alter table `mh_Daily` add column `Sheep_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Daily` add column `Sheep_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Weekly` add column `Sheep_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Weekly` add column `Sheep_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Monthly` add column `Sheep_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Monthly` add column `Sheep_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Yearly` add column `Sheep_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Yearly` add column `Sheep_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_AllTime` add column `Sheep_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_AllTime` add column `Sheep_assist`  INTEGER NOT NULL DEFAULT 0");

			statement.executeUpdate("alter table `mh_Daily` add column `Snowman_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Daily` add column `Snowman_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Weekly` add column `Snowman_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Weekly` add column `Snowman_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Monthly` add column `Snowman_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Monthly` add column `Snowman_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Yearly` add column `Snowman_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Yearly` add column `Snowman_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_AllTime` add column `Snowman_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_AllTime` add column `Snowman_assist`  INTEGER NOT NULL DEFAULT 0");

			statement.executeUpdate("alter table `mh_Daily` add column `Squid_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Daily` add column `Squid_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Weekly` add column `Squid_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Weekly` add column `Squid_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Monthly` add column `Squid_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Monthly` add column `Squid_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Yearly` add column `Squid_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Yearly` add column `Squid_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_AllTime` add column `Squid_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_AllTime` add column `Squid_assist`  INTEGER NOT NULL DEFAULT 0");

			statement.executeUpdate("alter table `mh_Daily` add column `Villager_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Daily` add column `Villager_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Weekly` add column `Villager_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Weekly` add column `Villager_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Monthly` add column `Villager_kill`  INTEGER NOT NULL DEFAULT 0");
			statement
					.executeUpdate("alter table `mh_Monthly` add column `Villager_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Yearly` add column `Villager_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Yearly` add column `Villager_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_AllTime` add column `Villager_kill`  INTEGER NOT NULL DEFAULT 0");
			statement
					.executeUpdate("alter table `mh_AllTime` add column `Villager_assist`  INTEGER NOT NULL DEFAULT 0");

			statement.executeUpdate("alter table `mh_Daily` add column `Wolf_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Daily` add column `Wolf_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Weekly` add column `Wolf_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Weekly` add column `Wolf_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Monthly` add column `Wolf_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Monthly` add column `Wolf_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Yearly` add column `Wolf_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Yearly` add column `Wolf_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_AllTime` add column `Wolf_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_AllTime` add column `Wolf_assist`  INTEGER NOT NULL DEFAULT 0");

			System.out.println("[MobHunting] Adding passive mobs complete.");

		}

		try {
			ResultSet rs = statement.executeQuery("SELECT EnderDragon_kill from mh_Daily LIMIT 0");
			rs.close();
		} catch (SQLException e) {

			System.out.println("[MobHunting] Adding EnderDragon to MobHunting Database.");

			statement.executeUpdate("alter table `mh_Daily` add column `EnderDragon_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate(
					"alter table `mh_Daily` add column `EnderDragon_assist`  INTEGER NOT NULL DEFAULT 0");
			statement
					.executeUpdate("alter table `mh_Weekly` add column `EnderDragon_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate(
					"alter table `mh_Weekly` add column `EnderDragon_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate(
					"alter table `mh_Monthly` add column `EnderDragon_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate(
					"alter table `mh_Monthly` add column `EnderDragon_assist`  INTEGER NOT NULL DEFAULT 0");
			statement
					.executeUpdate("alter table `mh_Yearly` add column `EnderDragon_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate(
					"alter table `mh_Yearly` add column `EnderDragon_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate(
					"alter table `mh_AllTime` add column `EnderDragon_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate(
					"alter table `mh_AllTime` add column `EnderDragon_assist`  INTEGER NOT NULL DEFAULT 0");

			System.out.println("[MobHunting] Adding EnderDragon complete.");
		}
		try {
			ResultSet rs = statement.executeQuery("SELECT IronGolem_kill from mh_Daily LIMIT 0");
			rs.close();
		} catch (SQLException e) {

			System.out.println("[MobHunting] Adding IronGolem to MobHunting Database ");

			statement.executeUpdate("alter table `mh_Daily` add column `IronGolem_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Daily` add column `IronGolem_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Weekly` add column `IronGolem_kill`  INTEGER NOT NULL DEFAULT 0");
			statement
					.executeUpdate("alter table `mh_Weekly` add column `IronGolem_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Monthly` add column `IronGolem_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate(
					"alter table `mh_Monthly` add column `IronGolem_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Yearly` add column `IronGolem_kill`  INTEGER NOT NULL DEFAULT 0");
			statement
					.executeUpdate("alter table `mh_Yearly` add column `IronGolem_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_AllTime` add column `IronGolem_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate(
					"alter table `mh_AllTime` add column `IronGolem_assist`  INTEGER NOT NULL DEFAULT 0");

			System.out.println("[MobHunting] Adding IronGolem complete.");
		}
		try {
			ResultSet rs = statement.executeQuery("SELECT PvpPlayer_kill from mh_Daily LIMIT 0");
			rs.close();
		} catch (SQLException e) {

			System.out.println("[MobHunting] Adding new PvpPlayer to MobHunting Database.");

			statement.executeUpdate("alter table `mh_Daily` add column `PvpPlayer_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Daily` add column `PvpPlayer_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Weekly` add column `PvpPlayer_kill`  INTEGER NOT NULL DEFAULT 0");
			statement
					.executeUpdate("alter table `mh_Weekly` add column `PvpPlayer_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Monthly` add column `PvpPlayer_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate(
					"alter table `mh_Monthly` add column `PvpPlayer_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Yearly` add column `PvpPlayer_kill`  INTEGER NOT NULL DEFAULT 0");
			statement
					.executeUpdate("alter table `mh_Yearly` add column `PvpPlayer_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_AllTime` add column `PvpPlayer_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate(
					"alter table `mh_AllTime` add column `PvpPlayer_assist`  INTEGER NOT NULL DEFAULT 0");

			System.out.println("[MobHunting] Adding new PvpPlayer complete.");
		}

		try {
			ResultSet rs = statement.executeQuery("SELECT Giant_kill from mh_Daily LIMIT 0");
			rs.close();
		} catch (SQLException e) {

			System.out.println("[MobHunting] Adding new Mobs to MobHunting Database.");

			statement.executeUpdate("alter table `mh_Daily` add column `Endermite_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Daily` add column `Endermite_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Weekly` add column `Endermite_kill`  INTEGER NOT NULL DEFAULT 0");
			statement
					.executeUpdate("alter table `mh_Weekly` add column `Endermite_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Monthly` add column `Endermite_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate(
					"alter table `mh_Monthly` add column `Endermite_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Yearly` add column `Endermite_kill`  INTEGER NOT NULL DEFAULT 0");
			statement
					.executeUpdate("alter table `mh_Yearly` add column `Endermite_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_AllTime` add column `Endermite_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate(
					"alter table `mh_AllTime` add column `Endermite_assist`  INTEGER NOT NULL DEFAULT 0");

			statement.executeUpdate("alter table `mh_Daily` add column `Giant_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Daily` add column `Giant_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Weekly` add column `Giant_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Weekly` add column `Giant_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Monthly` add column `Giant_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Monthly` add column `Giant_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Yearly` add column `Giant_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Yearly` add column `Giant_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_AllTime` add column `Giant_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_AllTime` add column `Giant_assist`  INTEGER NOT NULL DEFAULT 0");

			statement.executeUpdate("alter table `mh_Daily` add column `Guardian_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Daily` add column `Guardian_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Weekly` add column `Guardian_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Weekly` add column `Guardian_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Monthly` add column `Guardian_kill`  INTEGER NOT NULL DEFAULT 0");
			statement
					.executeUpdate("alter table `mh_Monthly` add column `Guardian_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Yearly` add column `Guardian_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Yearly` add column `Guardian_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_AllTime` add column `Guardian_kill`  INTEGER NOT NULL DEFAULT 0");
			statement
					.executeUpdate("alter table `mh_AllTime` add column `Guardian_assist`  INTEGER NOT NULL DEFAULT 0");

			statement
					.executeUpdate("alter table `mh_Daily` add column `KillerRabbit_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate(
					"alter table `mh_Daily` add column `KillerRabbit_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate(
					"alter table `mh_Weekly` add column `KillerRabbit_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate(
					"alter table `mh_Weekly` add column `KillerRabbit_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate(
					"alter table `mh_Monthly` add column `KillerRabbit_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate(
					"alter table `mh_Monthly` add column `KillerRabbit_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate(
					"alter table `mh_Yearly` add column `KillerRabbit_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate(
					"alter table `mh_Yearly` add column `KillerRabbit_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate(
					"alter table `mh_AllTime` add column `KillerRabbit_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate(
					"alter table `mh_AllTime` add column `KillerRabbit_assist`  INTEGER NOT NULL DEFAULT 0");

			System.out.println("[MobHunting] Adding new Mobs complete.");
		}

		try {
			ResultSet rs = statement.executeQuery("SELECT Shulker_kill from mh_Daily LIMIT 0");
			rs.close();
		} catch (SQLException e) {

			System.out.println("[MobHunting] Adding new 1.9 Mobs (Shulker) to MobHunting Database.");

			statement.executeUpdate("alter table `mh_Daily` add column `Shulker_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Daily` add column `Shulker_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Weekly` add column `Shulker_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Weekly` add column `Shulker_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Monthly` add column `Shulker_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Monthly` add column `Shulker_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Yearly` add column `Shulker_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Yearly` add column `Shulker_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_AllTime` add column `Shulker_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_AllTime` add column `Shulker_assist`  INTEGER NOT NULL DEFAULT 0");

			System.out.println("[MobHunting] Adding new 1.9 Mobs (Shulker) complete.");
		}

		try {
			ResultSet rs = statement.executeQuery("SELECT PolarBear_kill from mh_Daily LIMIT 0");
			rs.close();
		} catch (SQLException e) {

			System.out.println("[MobHunting] Adding new 1.10 Mobs (Polar Bear) to MobHunting Database.");

			statement.executeUpdate("alter table `mh_Daily` add column `PolarBear_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Daily` add column `PolarBear_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Weekly` add column `PolarBear_kill`  INTEGER NOT NULL DEFAULT 0");
			statement
					.executeUpdate("alter table `mh_Weekly` add column `PolarBear_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Monthly` add column `PolarBear_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate(
					"alter table `mh_Monthly` add column `PolarBear_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Yearly` add column `PolarBear_kill`  INTEGER NOT NULL DEFAULT 0");
			statement
					.executeUpdate("alter table `mh_Yearly` add column `PolarBear_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_AllTime` add column `PolarBear_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate(
					"alter table `mh_AllTime` add column `PolarBear_assist`  INTEGER NOT NULL DEFAULT 0");

			System.out.println("[MobHunting] Adding new 1.10 Mobs (Polar Bear) complete.");
		}

		try {
			ResultSet rs = statement.executeQuery("SELECT Stray_kill from mh_Daily LIMIT 0");
			rs.close();
		} catch (SQLException e) {

			System.out.println("[MobHunting] Adding new 1.10 Mobs (Stray + Husk) to MobHunting Database.");

			statement.executeUpdate("alter table `mh_Daily` add column `Stray_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Daily` add column `Stray_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Weekly` add column `Stray_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Weekly` add column `Stray_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Monthly` add column `Stray_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Monthly` add column `Stray_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Yearly` add column `Stray_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Yearly` add column `Stray_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_AllTime` add column `Stray_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_AllTime` add column `Stray_assist`  INTEGER NOT NULL DEFAULT 0");

			statement.executeUpdate("alter table `mh_Daily` add column `Husk_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Daily` add column `Husk_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Weekly` add column `Husk_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Weekly` add column `Husk_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Monthly` add column `Husk_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Monthly` add column `Husk_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Yearly` add column `Husk_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_Yearly` add column `Husk_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_AllTime` add column `Husk_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate("alter table `mh_AllTime` add column `Husk_assist`  INTEGER NOT NULL DEFAULT 0");

			System.out.println("[MobHunting] Adding new 1.10 Mobs (Stray + Husk) complete.");
		}

		try {
			ResultSet rs = statement.executeQuery("SELECT ElderGuardian_kill from mh_Daily LIMIT 0");
			rs.close();
		} catch (SQLException e) {

			System.out.println("[MobHunting] Adding 1.8 Mob (Elder Guardian) to MobHunting Database.");

			statement.executeUpdate(
					"alter table `mh_Daily` add column `ElderGuardian_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate(
					"alter table `mh_Daily` add column `ElderGuardian_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate(
					"alter table `mh_Weekly` add column `ElderGuardian_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate(
					"alter table `mh_Weekly` add column `ElderGuardian_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate(
					"alter table `mh_Monthly` add column `ElderGuardian_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate(
					"alter table `mh_Monthly` add column `ElderGuardian_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate(
					"alter table `mh_Yearly` add column `ElderGuardian_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate(
					"alter table `mh_Yearly` add column `ElderGuardian_assist`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate(
					"alter table `mh_AllTime` add column `ElderGuardian_kill`  INTEGER NOT NULL DEFAULT 0");
			statement.executeUpdate(
					"alter table `mh_AllTime` add column `ElderGuardian_assist`  INTEGER NOT NULL DEFAULT 0");

			System.out.println("[MobHunting] Adding 1.8 Mob (Elder Guardian) complete.");
		}

		try {
			ResultSet rs = statement.executeQuery("SELECT LEARNING_MODE from mh_Players LIMIT 0");
			rs.close();
		} catch (SQLException e) {
			System.out.println("[MobHunting] Adding new Player leaning mode to MobHunting Database.");
			String lm = MobHunting.getConfigManager().learningMode ? "1" : "0";
			statement.executeUpdate(
					"alter table `mh_Players` add column `LEARNING_MODE` INTEGER NOT NULL DEFAULT " + lm);
		}

		try {
			ResultSet rs = statement.executeQuery("SELECT MUTE_MODE from mh_Players LIMIT 0");
			rs.close();
		} catch (SQLException e) {
			System.out.println("[MobHunting] Adding new Player mute mode to MobHunting Database.");
			statement.executeUpdate("alter table `mh_Players` add column `MUTE_MODE` INTEGER NOT NULL DEFAULT 0");
		}

		Messages.debug("Updating database triggers.");
		statement.executeUpdate("DROP TRIGGER IF EXISTS `mh_DailyInsert`");
		statement.executeUpdate("DROP TRIGGER IF EXISTS `mh_DailyUpdate`");

		statement.close();
		connection.commit();

		setupTriggerV2(connection);

	}

	// *******************************************************************************
	// V3 DATABASE SETUP / MIGRATION
	// *******************************************************************************

	@Override
	protected void setupV3Tables(Connection connection) throws SQLException {
		Statement create = connection.createStatement();

		// Create new empty tables if they do not exist
		String lm = MobHunting.getConfigManager().learningMode ? "1" : "0";
		create.executeUpdate("CREATE TABLE IF NOT EXISTS mh_Players" + "(UUID TEXT," + " NAME TEXT, "
				+ " PLAYER_ID INTEGER NOT NULL DEFAULT 1," + " LEARNING_MODE INTEGER NOT NULL DEFAULT " + lm + ","
				+ " MUTE_MODE INTEGER NOT NULL DEFAULT 0," + " PRIMARY KEY(PLAYER_ID))");

		create.executeUpdate(
				"CREATE TABLE IF NOT EXISTS mh_Mobs " + "(MOB_ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL DEFAULT 0,"
						+ " PLUGIN_ID INTEGER NOT NULL," + " MOBTYPE TEXT)");

		create.executeUpdate("CREATE TABLE IF NOT EXISTS mh_Daily" + "(ID CHAR(7) NOT NULL,"//
				+ " MOB_ID INTEGER NOT NULL," //
				+ " PLAYER_ID INTEGER NOT NULL,"//
				+ " ACHIEVEMENT_COUNT INTEGER DEFAULT 0," //
				+ " TOTAL_KILL INTEGER DEFAULT 0,"//
				+ " TOTAL_ASSIST INTEGER DEFAULT 0," //
				+ " PRIMARY KEY(ID, MOB_ID, PLAYER_ID),"
				+ " FOREIGN KEY(MOB_ID) REFERENCES mh_Mobs(MOB_ID) ON DELETE CASCADE,"
				+ " FOREIGN KEY(PLAYER_ID) REFERENCES mh_Players(PLAYER_ID) ON DELETE CASCADE)");

		create.executeUpdate("CREATE TABLE IF NOT EXISTS mh_Weekly" + "(ID CHAR(6) NOT NULL,"
				+ " MOB_ID INTEGER NOT NULL," + " PLAYER_ID INTEGER NOT NULL," + " ACHIEVEMENT_COUNT INTEGER DEFAULT 0,"
				+ " TOTAL_KILL INTEGER DEFAULT 0," + " TOTAL_ASSIST INTEGER DEFAULT 0,"
				+ " PRIMARY KEY(ID, MOB_ID, PLAYER_ID),"
				+ " FOREIGN KEY(MOB_ID) REFERENCES mh_Mobs(MOB_ID) ON DELETE CASCADE,"
				+ " FOREIGN KEY(PLAYER_ID) REFERENCES mh_Players(PLAYER_ID) ON DELETE CASCADE)");

		create.executeUpdate("CREATE TABLE IF NOT EXISTS mh_Monthly" + "(ID CHAR(6) NOT NULL,"
				+ " MOB_ID INTEGER NOT NULL," + " PLAYER_ID INTEGER NOT NULL," + " ACHIEVEMENT_COUNT INTEGER DEFAULT 0,"
				+ " TOTAL_KILL INTEGER DEFAULT 0," + " TOTAL_ASSIST INTEGER DEFAULT 0,"
				+ " PRIMARY KEY(ID, MOB_ID, PLAYER_ID),"
				+ " FOREIGN KEY(MOB_ID) REFERENCES mh_Mobs(MOB_ID) ON DELETE CASCADE,"
				+ " FOREIGN KEY(PLAYER_ID) REFERENCES mh_Players(PLAYER_ID) ON DELETE CASCADE)");

		create.executeUpdate("CREATE TABLE IF NOT EXISTS mh_Yearly" + "(ID CHAR(4) NOT NULL,"
				+ " MOB_ID INTEGER NOT NULL," + " PLAYER_ID INTEGER NOT NULL," + " ACHIEVEMENT_COUNT INTEGER DEFAULT 0,"
				+ " TOTAL_KILL INTEGER DEFAULT 0," + " TOTAL_ASSIST INTEGER DEFAULT 0,"
				+ " PRIMARY KEY(ID, MOB_ID, PLAYER_ID),"
				+ " FOREIGN KEY(MOB_ID) REFERENCES mh_Mobs(MOB_ID) ON DELETE CASCADE,"
				+ " FOREIGN KEY(PLAYER_ID) REFERENCES mh_Players(PLAYER_ID) ON DELETE CASCADE)");

		create.executeUpdate(
				"CREATE TABLE IF NOT EXISTS mh_AllTime" + " (MOB_ID INTEGER NOT NULL," + " PLAYER_ID INTEGER NOT NULL,"
						+ " ACHIEVEMENT_COUNT INTEGER DEFAULT 0," + " TOTAL_KILL INTEGER DEFAULT 0,"
						+ " TOTAL_ASSIST INTEGER DEFAULT 0," + " PRIMARY KEY(MOB_ID, PLAYER_ID),"
						+ " FOREIGN KEY(MOB_ID) REFERENCES mh_Mobs(MOB_ID) ON DELETE CASCADE,"
						+ " FOREIGN KEY(PLAYER_ID) REFERENCES mh_Players(PLAYER_ID) ON DELETE CASCADE)");

		create.executeUpdate("CREATE TABLE IF NOT EXISTS mh_Achievements " + "(PLAYER_ID INTEGER NOT NULL,"
				+ " ACHIEVEMENT TEXT NOT NULL," + " DATE INTEGER NOT NULL," + " PROGRESS INTEGER NOT NULL,"
				+ " PRIMARY KEY(PLAYER_ID, ACHIEVEMENT), "
				+ " FOREIGN KEY(PLAYER_ID) REFERENCES mh_Players(PLAYER_ID))");

		if (!MobHunting.getConfigManager().disablePlayerBounties) {
			create.executeUpdate("CREATE TABLE IF NOT EXISTS mh_Bounties ("
					+ "BOUNTYOWNER_ID INTEGER REFERENCES mh_Players(PLAYER_ID) NOT NULL, " + "MOBTYPE TEXT, "
					+ "WANTEDPLAYER_ID INTEGER REFERENCES mh_Players(PLAYER_ID), " + "NPC_ID INTEGER, "
					+ "MOB_ID TEXT, " + "WORLDGROUP TEXT NOT NULL, " + "CREATED_DATE INTEGER NOT NULL, "
					+ "END_DATE INTEGER NOT NULL, " + "PRIZE FLOAT NOT NULL, " + "MESSAGE TEXT, "
					+ "STATUS INTEGER NOT NULL DEFAULT 0, "
					+ "PRIMARY KEY(WORLDGROUP, WANTEDPLAYER_ID, BOUNTYOWNER_ID), "
					+ "FOREIGN KEY(BOUNTYOWNER_ID) REFERENCES mh_Players(PLAYER_ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(WANTEDPLAYER_ID) REFERENCES mh_Players(PLAYER_ID) ON DELETE CASCADE" + ")");
		}

		// Setup Database triggers
		create.executeUpdate("DROP TRIGGER IF EXISTS `mh_DailyInsert`");
		create.executeUpdate("DROP TRIGGER IF EXISTS `mh_DailyUpdate`");

		create.close();
		connection.commit();

		insertMissingVanillaMobs();

		Messages.debug("MobHunting V3 Database created.");

	}

	@Override
	protected void setupTriggerV3(Connection connection) throws SQLException {

		Statement create = connection.createStatement();

		create.executeUpdate("create trigger if not exists mh_DailyInsert after insert on mh_Daily" + " begin"

				+ " insert or ignore into mh_Weekly(ID, MOB_ID, PLAYER_ID, ACHIEVEMENT_COUNT, TOTAL_KILL, TOTAL_ASSIST)"
				+ " values(strftime(\"%Y%W\",\"now\"), NEW.MOB_ID, NEW.PLAYER_ID, NEW.ACHIEVEMENT_COUNT, NEW.TOTAL_KILL, NEW.TOTAL_ASSIST);"

				+ " insert or ignore into mh_Monthly(ID, MOB_ID, PLAYER_ID, ACHIEVEMENT_COUNT, TOTAL_KILL, TOTAL_ASSIST)"
				+ " values(strftime(\"%Y%m\",\"now\"), NEW.MOB_ID, NEW.PLAYER_ID, NEW.ACHIEVEMENT_COUNT, NEW.TOTAL_KILL, NEW.TOTAL_ASSIST);"

				+ " insert or ignore into mh_Yearly(ID, MOB_ID, PLAYER_ID, ACHIEVEMENT_COUNT, TOTAL_KILL, TOTAL_ASSIST)"
				+ " values(strftime(\"%Y\",\"now\"), NEW.MOB_ID, NEW.PLAYER_ID, NEW.ACHIEVEMENT_COUNT, NEW.TOTAL_KILL, NEW.TOTAL_ASSIST);"

				+ " insert or ignore into mh_AllTime(MOB_ID, PLAYER_ID, ACHIEVEMENT_COUNT, TOTAL_KILL, TOTAL_ASSIST)"
				+ " values(NEW.MOB_ID, NEW.PLAYER_ID, NEW.ACHIEVEMENT_COUNT, NEW.TOTAL_KILL, NEW.TOTAL_ASSIST);"

				+ " end");

		// Create the cascade update trigger. It will allow us to only modify
		// the Daily table, and the rest will happen automatically
		StringBuilder updateStringBuilder = new StringBuilder();

		updateStringBuilder
				.append(String.format(Locale.US, "%s = (%1$s + (NEW.%1$s - OLD.%1$s)), ", "ACHIEVEMENT_COUNT"));
		updateStringBuilder.append(String.format(Locale.US, "%s = (%1$s + (NEW.%1$s - OLD.%1$s)), ", "TOTAL_KILL"));
		updateStringBuilder.append(String.format(Locale.US, "%s = (%1$s + (NEW.%1$s - OLD.%1$s)) ", "TOTAL_ASSIST"));

		String updateString = updateStringBuilder.toString();

		StringBuilder updateTrigger = new StringBuilder();
		updateTrigger.append("create trigger if not exists mh_DailyUpdate after update on mh_Daily BEGIN ");

		// Weekly
		updateTrigger.append(" update mh_Weekly set ");
		updateTrigger.append(updateString);
		updateTrigger.append(" where ID=strftime('%Y%W','now') AND MOB_ID=New.MOB_ID AND PLAYER_ID=New.PLAYER_ID;");

		// Monthly
		updateTrigger.append(" update mh_Monthly set ");
		updateTrigger.append(updateString);
		updateTrigger.append(" where ID=strftime('%Y%m','now') AND MOB_ID=New.MOB_ID AND PLAYER_ID=New.PLAYER_ID;");

		// Yearly
		updateTrigger.append(" update mh_Yearly set ");
		updateTrigger.append(updateString);
		updateTrigger.append(" where ID=strftime('%Y','now') AND MOB_ID=New.MOB_ID AND PLAYER_ID=New.PLAYER_ID;");

		// AllTime
		updateTrigger.append(" update mh_AllTime set ");
		updateTrigger.append(updateString);
		updateTrigger.append(" where MOB_ID=New.MOB_ID AND PLAYER_ID=New.PLAYER_ID;");

		updateTrigger.append("END");

		create.executeUpdate(updateTrigger.toString());

		create.close();
		connection.commit();

		Messages.debug("Database trigger updated.");
	}

	// *******************************************************************************
	// V4 DATABASE SETUP / MIGRATION
	// *******************************************************************************

	@Override
	protected void setupV4Tables(Connection connection) throws SQLException {
		Statement create = connection.createStatement();

		// Create new empty tables if they do not exist
		String lm = MobHunting.getConfigManager().learningMode ? "1" : "0";
		create.executeUpdate("CREATE TABLE IF NOT EXISTS mh_Players" + "(UUID TEXT," + " NAME TEXT, "
				+ " PLAYER_ID INTEGER NOT NULL DEFAULT 1," + " LEARNING_MODE INTEGER NOT NULL DEFAULT " + lm + ","
				+ " MUTE_MODE INTEGER NOT NULL DEFAULT 0," + " PRIMARY KEY(PLAYER_ID))");

		create.executeUpdate("CREATE TABLE IF NOT EXISTS mh_Mobs "//
				+ "(MOB_ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL DEFAULT 0,"//
				+ " PLUGIN_ID INTEGER NOT NULL," + " MOBTYPE TEXT)");

		create.executeUpdate("CREATE TABLE IF NOT EXISTS mh_Daily"//
				+ "(ID CHAR(7) NOT NULL,"//
				+ " MOB_ID INTEGER NOT NULL," //
				+ " PLAYER_ID INTEGER NOT NULL,"//
				+ " ACHIEVEMENT_COUNT INTEGER DEFAULT 0," //
				+ " TOTAL_KILL INTEGER DEFAULT 0,"//
				+ " TOTAL_ASSIST INTEGER DEFAULT 0," //
				+ " TOTAL_CASH REAL DEFAULT 0," //
				+ " PRIMARY KEY(ID, MOB_ID, PLAYER_ID),"
				+ " FOREIGN KEY(MOB_ID) REFERENCES mh_Mobs(MOB_ID) ON DELETE CASCADE,"
				+ " FOREIGN KEY(PLAYER_ID) REFERENCES mh_Players(PLAYER_ID) ON DELETE CASCADE)");

		create.executeUpdate("CREATE TABLE IF NOT EXISTS mh_Weekly"//
				+ "(ID CHAR(6) NOT NULL,"//
				+ " MOB_ID INTEGER NOT NULL,"//
				+ " PLAYER_ID INTEGER NOT NULL,"//
				+ " ACHIEVEMENT_COUNT INTEGER DEFAULT 0,"//
				+ " TOTAL_KILL INTEGER DEFAULT 0,"//
				+ " TOTAL_ASSIST INTEGER DEFAULT 0,"//
				+ " TOTAL_CASH REAL DEFAULT 0," //
				+ " PRIMARY KEY(ID, MOB_ID, PLAYER_ID),"
				+ " FOREIGN KEY(MOB_ID) REFERENCES mh_Mobs(MOB_ID) ON DELETE CASCADE,"
				+ " FOREIGN KEY(PLAYER_ID) REFERENCES mh_Players(PLAYER_ID) ON DELETE CASCADE)");

		create.executeUpdate("CREATE TABLE IF NOT EXISTS mh_Monthly"//
				+ "(ID CHAR(6) NOT NULL,"//
				+ " MOB_ID INTEGER NOT NULL,"//
				+ " PLAYER_ID INTEGER NOT NULL,"//
				+ " ACHIEVEMENT_COUNT INTEGER DEFAULT 0,"//
				+ " TOTAL_KILL INTEGER DEFAULT 0,"//
				+ " TOTAL_ASSIST INTEGER DEFAULT 0,"//
				+ " TOTAL_CASH REAL DEFAULT 0," //
				+ " PRIMARY KEY(ID, MOB_ID, PLAYER_ID),"
				+ " FOREIGN KEY(MOB_ID) REFERENCES mh_Mobs(MOB_ID) ON DELETE CASCADE,"
				+ " FOREIGN KEY(PLAYER_ID) REFERENCES mh_Players(PLAYER_ID) ON DELETE CASCADE)");

		create.executeUpdate("CREATE TABLE IF NOT EXISTS mh_Yearly"//
				+ "(ID CHAR(4) NOT NULL,"//
				+ " MOB_ID INTEGER NOT NULL,"//
				+ " PLAYER_ID INTEGER NOT NULL,"//
				+ " ACHIEVEMENT_COUNT INTEGER DEFAULT 0,"//
				+ " TOTAL_KILL INTEGER DEFAULT 0,"//
				+ " TOTAL_ASSIST INTEGER DEFAULT 0,"//
				+ " TOTAL_CASH REAL DEFAULT 0," //
				+ " PRIMARY KEY(ID, MOB_ID, PLAYER_ID),"
				+ " FOREIGN KEY(MOB_ID) REFERENCES mh_Mobs(MOB_ID) ON DELETE CASCADE,"
				+ " FOREIGN KEY(PLAYER_ID) REFERENCES mh_Players(PLAYER_ID) ON DELETE CASCADE)");

		create.executeUpdate("CREATE TABLE IF NOT EXISTS mh_AllTime"//
				+ " (MOB_ID INTEGER NOT NULL,"//
				+ " PLAYER_ID INTEGER NOT NULL,"//
				+ " ACHIEVEMENT_COUNT INTEGER DEFAULT 0,"//
				+ " TOTAL_KILL INTEGER DEFAULT 0,"//
				+ " TOTAL_ASSIST INTEGER DEFAULT 0,"//
				+ " TOTAL_CASH REAL DEFAULT 0," //
				+ " PRIMARY KEY(MOB_ID, PLAYER_ID),"
				+ " FOREIGN KEY(MOB_ID) REFERENCES mh_Mobs(MOB_ID) ON DELETE CASCADE,"
				+ " FOREIGN KEY(PLAYER_ID) REFERENCES mh_Players(PLAYER_ID) ON DELETE CASCADE)");

		create.executeUpdate("CREATE TABLE IF NOT EXISTS mh_Achievements "//
				+ "(PLAYER_ID INTEGER NOT NULL,"//
				+ " ACHIEVEMENT TEXT NOT NULL,"//
				+ " DATE INTEGER NOT NULL,"//
				+ " PROGRESS INTEGER NOT NULL," + " PRIMARY KEY(PLAYER_ID, ACHIEVEMENT), "
				+ " FOREIGN KEY(PLAYER_ID) REFERENCES mh_Players(PLAYER_ID))");

		if (!MobHunting.getConfigManager().disablePlayerBounties) {
			create.executeUpdate("CREATE TABLE IF NOT EXISTS mh_Bounties ("
					+ "BOUNTYOWNER_ID INTEGER REFERENCES mh_Players(PLAYER_ID) NOT NULL, " + "MOBTYPE TEXT, "
					+ "WANTEDPLAYER_ID INTEGER REFERENCES mh_Players(PLAYER_ID), " + "NPC_ID INTEGER, "
					+ "MOB_ID TEXT, " + "WORLDGROUP TEXT NOT NULL, " + "CREATED_DATE INTEGER NOT NULL, "
					+ "END_DATE INTEGER NOT NULL, " + "PRIZE FLOAT NOT NULL, " + "MESSAGE TEXT, "
					+ "STATUS INTEGER NOT NULL DEFAULT 0, "
					+ "PRIMARY KEY(WORLDGROUP, WANTEDPLAYER_ID, BOUNTYOWNER_ID), "
					+ "FOREIGN KEY(BOUNTYOWNER_ID) REFERENCES mh_Players(PLAYER_ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(WANTEDPLAYER_ID) REFERENCES mh_Players(PLAYER_ID) ON DELETE CASCADE" + ")");
		}

		// Setup Database triggers
		create.executeUpdate("DROP TRIGGER IF EXISTS `mh_DailyInsert`");
		create.executeUpdate("DROP TRIGGER IF EXISTS `mh_DailyUpdate`");

		create.close();
		connection.commit();

	}

	@Override
	protected void setupTriggerV4andV5(Connection connection) throws SQLException {

		Statement create = connection.createStatement();

		create.executeUpdate("create trigger if not exists mh_DailyInsert after insert on mh_Daily" + " begin"

				+ " insert or ignore into mh_Weekly(ID, MOB_ID, PLAYER_ID, ACHIEVEMENT_COUNT, TOTAL_KILL, TOTAL_ASSIST, TOTAL_CASH)"
				+ " values(strftime(\"%Y%W\",\"now\"), NEW.MOB_ID, NEW.PLAYER_ID, NEW.ACHIEVEMENT_COUNT, NEW.TOTAL_KILL, NEW.TOTAL_ASSIST, NEW.TOTAL_CASH);"

				+ " insert or ignore into mh_Monthly(ID, MOB_ID, PLAYER_ID, ACHIEVEMENT_COUNT, TOTAL_KILL, TOTAL_ASSIST, TOTAL_CASH)"
				+ " values(strftime(\"%Y%m\",\"now\"), NEW.MOB_ID, NEW.PLAYER_ID, NEW.ACHIEVEMENT_COUNT, NEW.TOTAL_KILL, NEW.TOTAL_ASSIST, NEW.TOTAL_CASH);"

				+ " insert or ignore into mh_Yearly(ID, MOB_ID, PLAYER_ID, ACHIEVEMENT_COUNT, TOTAL_KILL, TOTAL_ASSIST, TOTAL_CASH)"
				+ " values(strftime(\"%Y\",\"now\"), NEW.MOB_ID, NEW.PLAYER_ID, NEW.ACHIEVEMENT_COUNT, NEW.TOTAL_KILL, NEW.TOTAL_ASSIST, NEW.TOTAL_CASH);"

				+ " insert or ignore into mh_AllTime(MOB_ID, PLAYER_ID, ACHIEVEMENT_COUNT, TOTAL_KILL, TOTAL_ASSIST, TOTAL_CASH)"
				+ " values(NEW.MOB_ID, NEW.PLAYER_ID, NEW.ACHIEVEMENT_COUNT, NEW.TOTAL_KILL, NEW.TOTAL_ASSIST, NEW.TOTAL_CASH);"

				+ " end");

		// Create the cascade update trigger. It will allow us to only modify
		// the Daily table, and the rest will happen automatically
		StringBuilder updateStringBuilder = new StringBuilder();

		updateStringBuilder
				.append(String.format(Locale.US, "%s = (%1$s + (NEW.%1$s - OLD.%1$s)), ", "ACHIEVEMENT_COUNT"));
		updateStringBuilder.append(String.format(Locale.US, "%s = (%1$s + (NEW.%1$s - OLD.%1$s)), ", "TOTAL_KILL"));
		updateStringBuilder.append(String.format(Locale.US, "%s = (%1$s + (NEW.%1$s - OLD.%1$s)), ", "TOTAL_ASSIST"));
		updateStringBuilder.append(String.format(Locale.US, "%s = (%1$s + (NEW.%1$s - OLD.%1$s)) ", "TOTAL_CASH"));

		String updateString = updateStringBuilder.toString();

		StringBuilder updateTrigger = new StringBuilder();
		updateTrigger.append("create trigger if not exists mh_DailyUpdate after update on mh_Daily BEGIN ");

		// Weekly
		updateTrigger.append(" update mh_Weekly set ");
		updateTrigger.append(updateString);
		updateTrigger.append(" where ID=strftime('%Y%W','now') AND MOB_ID=New.MOB_ID AND PLAYER_ID=New.PLAYER_ID;");

		// Monthly
		updateTrigger.append(" update mh_Monthly set ");
		updateTrigger.append(updateString);
		updateTrigger.append(" where ID=strftime('%Y%m','now') AND MOB_ID=New.MOB_ID AND PLAYER_ID=New.PLAYER_ID;");

		// Yearly
		updateTrigger.append(" update mh_Yearly set ");
		updateTrigger.append(updateString);
		updateTrigger.append(" where ID=strftime('%Y','now') AND MOB_ID=New.MOB_ID AND PLAYER_ID=New.PLAYER_ID;");

		// AllTime
		updateTrigger.append(" update mh_AllTime set ");
		updateTrigger.append(updateString);
		updateTrigger.append(" where MOB_ID=New.MOB_ID AND PLAYER_ID=New.PLAYER_ID;");

		updateTrigger.append("END");

		create.executeUpdate(updateTrigger.toString());

		create.close();
		connection.commit();

		Messages.debug("Database trigger updated.");
	}

	// *******************************************************************************
	// V5 DATABASE SETUP / MIGRATION
	// *******************************************************************************

	@Override
	protected void setupV5Tables(Connection connection) throws SQLException {
		Statement create = connection.createStatement();

		// Create new empty tables if they do not exist
		String lm = MobHunting.getConfigManager().learningMode ? "1" : "0";
		create.executeUpdate("CREATE TABLE IF NOT EXISTS mh_Players" //
				+ "(UUID TEXT," //
				+ " NAME TEXT, " //
				+ " PLAYER_ID INTEGER NOT NULL DEFAULT 1," //
				+ " LEARNING_MODE INTEGER NOT NULL DEFAULT " + lm + "," //
				+ " MUTE_MODE INTEGER NOT NULL DEFAULT 0," //
				+ " BALANCE REAL DEFAULT 0," //
				+ " BALANCE_CHANGES REAL DEFAULT 0," //
				+ " BANK_BALANCE REAL DEFAULT 0," //
				+ " BANK_BALANCE_CHANGES REAL DEFAULT 0," //
				+ " PRIMARY KEY(PLAYER_ID))");

		create.executeUpdate("CREATE TABLE IF NOT EXISTS mh_Mobs "//
				+ "(MOB_ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL DEFAULT 0,"//
				+ " PLUGIN_ID INTEGER NOT NULL," + " MOBTYPE TEXT)");

		create.executeUpdate("CREATE TABLE IF NOT EXISTS mh_Daily"//
				+ "(ID CHAR(7) NOT NULL,"//
				+ " MOB_ID INTEGER NOT NULL," //
				+ " PLAYER_ID INTEGER NOT NULL,"//
				+ " ACHIEVEMENT_COUNT INTEGER DEFAULT 0," //
				+ " TOTAL_KILL INTEGER DEFAULT 0,"//
				+ " TOTAL_ASSIST INTEGER DEFAULT 0," //
				+ " TOTAL_CASH REAL DEFAULT 0," //
				+ " PRIMARY KEY(ID, MOB_ID, PLAYER_ID),"
				+ " FOREIGN KEY(MOB_ID) REFERENCES mh_Mobs(MOB_ID) ON DELETE CASCADE,"
				+ " FOREIGN KEY(PLAYER_ID) REFERENCES mh_Players(PLAYER_ID) ON DELETE CASCADE)");

		create.executeUpdate("CREATE TABLE IF NOT EXISTS mh_Weekly"//
				+ "(ID CHAR(6) NOT NULL,"//
				+ " MOB_ID INTEGER NOT NULL,"//
				+ " PLAYER_ID INTEGER NOT NULL,"//
				+ " ACHIEVEMENT_COUNT INTEGER DEFAULT 0,"//
				+ " TOTAL_KILL INTEGER DEFAULT 0,"//
				+ " TOTAL_ASSIST INTEGER DEFAULT 0,"//
				+ " TOTAL_CASH REAL DEFAULT 0," //
				+ " PRIMARY KEY(ID, MOB_ID, PLAYER_ID),"
				+ " FOREIGN KEY(MOB_ID) REFERENCES mh_Mobs(MOB_ID) ON DELETE CASCADE,"
				+ " FOREIGN KEY(PLAYER_ID) REFERENCES mh_Players(PLAYER_ID) ON DELETE CASCADE)");

		create.executeUpdate("CREATE TABLE IF NOT EXISTS mh_Monthly"//
				+ "(ID CHAR(6) NOT NULL,"//
				+ " MOB_ID INTEGER NOT NULL,"//
				+ " PLAYER_ID INTEGER NOT NULL,"//
				+ " ACHIEVEMENT_COUNT INTEGER DEFAULT 0,"//
				+ " TOTAL_KILL INTEGER DEFAULT 0,"//
				+ " TOTAL_ASSIST INTEGER DEFAULT 0,"//
				+ " TOTAL_CASH REAL DEFAULT 0," //
				+ " PRIMARY KEY(ID, MOB_ID, PLAYER_ID),"
				+ " FOREIGN KEY(MOB_ID) REFERENCES mh_Mobs(MOB_ID) ON DELETE CASCADE,"
				+ " FOREIGN KEY(PLAYER_ID) REFERENCES mh_Players(PLAYER_ID) ON DELETE CASCADE)");

		create.executeUpdate("CREATE TABLE IF NOT EXISTS mh_Yearly"//
				+ "(ID CHAR(4) NOT NULL,"//
				+ " MOB_ID INTEGER NOT NULL,"//
				+ " PLAYER_ID INTEGER NOT NULL,"//
				+ " ACHIEVEMENT_COUNT INTEGER DEFAULT 0,"//
				+ " TOTAL_KILL INTEGER DEFAULT 0,"//
				+ " TOTAL_ASSIST INTEGER DEFAULT 0,"//
				+ " TOTAL_CASH REAL DEFAULT 0," //
				+ " PRIMARY KEY(ID, MOB_ID, PLAYER_ID),"
				+ " FOREIGN KEY(MOB_ID) REFERENCES mh_Mobs(MOB_ID) ON DELETE CASCADE,"
				+ " FOREIGN KEY(PLAYER_ID) REFERENCES mh_Players(PLAYER_ID) ON DELETE CASCADE)");

		create.executeUpdate("CREATE TABLE IF NOT EXISTS mh_AllTime"//
				+ " (MOB_ID INTEGER NOT NULL,"//
				+ " PLAYER_ID INTEGER NOT NULL,"//
				+ " ACHIEVEMENT_COUNT INTEGER DEFAULT 0,"//
				+ " TOTAL_KILL INTEGER DEFAULT 0,"//
				+ " TOTAL_ASSIST INTEGER DEFAULT 0,"//
				+ " TOTAL_CASH REAL DEFAULT 0," //
				+ " PRIMARY KEY(MOB_ID, PLAYER_ID),"
				+ " FOREIGN KEY(MOB_ID) REFERENCES mh_Mobs(MOB_ID) ON DELETE CASCADE,"
				+ " FOREIGN KEY(PLAYER_ID) REFERENCES mh_Players(PLAYER_ID) ON DELETE CASCADE)");

		create.executeUpdate("CREATE TABLE IF NOT EXISTS mh_Achievements "//
				+ "(PLAYER_ID INTEGER NOT NULL,"//
				+ " ACHIEVEMENT TEXT NOT NULL,"//
				+ " DATE INTEGER NOT NULL,"//
				+ " PROGRESS INTEGER NOT NULL," + " PRIMARY KEY(PLAYER_ID, ACHIEVEMENT), "
				+ " FOREIGN KEY(PLAYER_ID) REFERENCES mh_Players(PLAYER_ID))");

		if (!MobHunting.getConfigManager().disablePlayerBounties) {
			create.executeUpdate("CREATE TABLE IF NOT EXISTS mh_Bounties ("
					+ "BOUNTYOWNER_ID INTEGER REFERENCES mh_Players(PLAYER_ID) NOT NULL, " + "MOBTYPE TEXT, "
					+ "WANTEDPLAYER_ID INTEGER REFERENCES mh_Players(PLAYER_ID), " + "NPC_ID INTEGER, "
					+ "MOB_ID TEXT, " + "WORLDGROUP TEXT NOT NULL, " + "CREATED_DATE INTEGER NOT NULL, "
					+ "END_DATE INTEGER NOT NULL, " + "PRIZE FLOAT NOT NULL, " + "MESSAGE TEXT, "
					+ "STATUS INTEGER NOT NULL DEFAULT 0, "
					+ "PRIMARY KEY(WORLDGROUP, WANTEDPLAYER_ID, BOUNTYOWNER_ID), "
					+ "FOREIGN KEY(BOUNTYOWNER_ID) REFERENCES mh_Players(PLAYER_ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(WANTEDPLAYER_ID) REFERENCES mh_Players(PLAYER_ID) ON DELETE CASCADE" + ")");
		}

		// Setup Database triggers
		create.executeUpdate("DROP TRIGGER IF EXISTS `mh_DailyInsert`");
		create.executeUpdate("DROP TRIGGER IF EXISTS `mh_DailyUpdate`");

		create.close();
		connection.commit();

	}

}
