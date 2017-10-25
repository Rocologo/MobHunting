package one.lindegaard.MobHunting.storage;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.bounty.Bounty;
import one.lindegaard.MobHunting.bounty.BountyStatus;
import one.lindegaard.MobHunting.compatibility.CitizensCompat;
import one.lindegaard.MobHunting.compatibility.CustomMobsCompat;
import one.lindegaard.MobHunting.compatibility.HerobrineCompat;
import one.lindegaard.MobHunting.compatibility.MysteriousHalloweenCompat;
import one.lindegaard.MobHunting.compatibility.MythicMobsCompat;
import one.lindegaard.MobHunting.compatibility.SmartGiantsCompat;
import one.lindegaard.MobHunting.compatibility.TARDISWeepingAngelsCompat;
import one.lindegaard.MobHunting.mobs.PluginManager;
import one.lindegaard.MobHunting.mobs.MobPlugin;
import one.lindegaard.MobHunting.mobs.ExtendedMob;
import one.lindegaard.MobHunting.mobs.MinecraftMob;

public abstract class DatabaseDataStore implements IDataStore {

	private MobHunting plugin;

	public DatabaseDataStore(MobHunting plugin) {
		this.plugin = plugin;
	}

	/**
	 * Connection to the Database
	 */
	// protected Connection mConnection;

	/**
	 * Args: player id
	 */
	protected PreparedStatement mSavePlayerStats;

	/**
	 * Args: player uuid
	 */
	protected PreparedStatement mGetPlayerData;

	/**
	 * Args: player name
	 */
	protected PreparedStatement mGetPlayerUUID;

	/**
	 * Args: player name, player uuid
	 */
	protected PreparedStatement mUpdatePlayerName;

	/**
	 * Args: player uuid
	 */
	protected PreparedStatement mUpdatePlayerSettings;

	/**
	 * Args: player uuid
	 */
	protected PreparedStatement mInsertPlayerData;

	/**
	 * Args: Player OfflinePLayer
	 */
	protected PreparedStatement mGetBounties;

	/**
	 * Args: Bounty
	 */
	protected PreparedStatement mInsertBounty;

	/**
	 * Args: Bounty
	 */
	protected PreparedStatement mUpdateBounty;

	/**
	 * Args: Bounty ID
	 */
	protected PreparedStatement mDeleteBounty;

	/**
	 * Args: player player_id
	 */
	protected PreparedStatement mGetPlayerByPlayerId;

	/**
	 * Establish initial connection to Database
	 */
	protected abstract Connection setupConnection() throws SQLException, DataStoreException;

	/**
	 * Setup / Create database version 2 tables for MobHunting
	 */
	protected abstract void setupV2Tables(Connection connection) throws SQLException;

	/**
	 * Setup / Create database version 3 tables for MobHunting
	 */
	protected abstract void setupV3Tables(Connection connection) throws SQLException;

	/**
	 * Setup / Setup Triggers for V3 Database Layout
	 */
	protected abstract void setupTriggerV3(Connection connection) throws SQLException;

	/**
	 * Setup / Create database version 4 tables for MobHunting
	 */
	protected abstract void setupV4Tables(Connection connection) throws SQLException;

	/**
	 * Setup / Setup Triggers for V4 Database Layout
	 */
	protected abstract void setupTriggerV4andV5(Connection connection) throws SQLException;

	/**
	 * Setup / Create database version 5 tables for MobHunting
	 */
	protected abstract void setupV5Tables(Connection connection) throws SQLException;

	/**
	 * Open a connection to the Database and prepare a statement for executing.
	 * 
	 * @param connection
	 * @param preparedConnectionType
	 * @throws SQLException
	 */
	protected abstract void openPreparedStatements(Connection connection, PreparedConnectionType preparedConnectionType)
			throws SQLException;

	public enum PreparedConnectionType {
		LOAD_ARCHIEVEMENTS, SAVE_ACHIEVEMENTS, UPDATE_PLAYER_NAME, GET_PLAYER_DATA, GET_PLAYER_UUID, INSERT_PLAYER_DATA, UPDATE_PLAYER_SETTINGS, GET_BOUNTIES, INSERT_BOUNTY, UPDATE_BOUNTY, DELETE_BOUNTY, GET_PLAYER_BY_PLAYER_ID, LOAD_MOBS, INSERT_MOBS, UPDATE_MOBS, SAVE_PLAYER_STATS
	};

	/**
	 * Initialize the connection. Must be called after Opening of initial
	 * connection. Open Prepared statements for batch processing large
	 * selections of players. Batches will be performed in batches of 10,5,2,1
	 */
	@Override
	public void initialize() throws DataStoreException {
		try {

			Connection mConnection = setupConnection();

			// Find current database version
			if (MobHunting.getConfigManager().databaseVersion == 0) {
				Statement statement = mConnection.createStatement();
				try {
					ResultSet rs = statement.executeQuery("SELECT BALANCE FROM mh_Players LIMIT 0");
					rs.close();
					// The TABLE Coloumn BALANCE only exists in Database
					// layout V5
					MobHunting.getConfigManager().databaseVersion = 5;
					MobHunting.getConfigManager().saveConfig();
				} catch (SQLException e5) {
					try {
						ResultSet rs = statement.executeQuery("SELECT TOTAL_CASH FROM mh_Daily LIMIT 0");
						rs.close();
						// The TABLE Coloumn TOTAL_CASH only exists in Database
						// layout V4
						MobHunting.getConfigManager().databaseVersion = 4;
						MobHunting.getConfigManager().saveConfig();
					} catch (SQLException e4) {
						try {
							ResultSet rs = statement.executeQuery("SELECT MOB_ID FROM mh_Mobs LIMIT 0");
							rs.close();
							// The TABLE mh_Mobs created for V3 and does only
							// contain
							// data after migration
							MobHunting.getConfigManager().databaseVersion = 3;
							MobHunting.getConfigManager().saveConfig();
						} catch (SQLException e3) {
							try {
								ResultSet rs = statement.executeQuery("SELECT UUID from mh_Players LIMIT 0");
								rs.close();
								// Player UUID is migrated in V2
								MobHunting.getConfigManager().databaseVersion = 2;
								MobHunting.getConfigManager().saveConfig();
							} catch (SQLException e2) {
								// database if from before Minecraft 1.7.9 R1
								// (No
								// UUID)
								// = V1
								try {
									ResultSet rs = statement.executeQuery("SELECT PLAYER_ID from mh_Players LIMIT 0");
									rs.close();
									MobHunting.getConfigManager().databaseVersion = 1;
									MobHunting.getConfigManager().saveConfig();
								} catch (SQLException e1) {
									// DATABASE DOES NOT EXIST AT ALL, CREATE
									// NEW
									// EMPTY
									// V3 DATABASE
									MobHunting.getConfigManager().databaseVersion = 4;
									MobHunting.getConfigManager().saveConfig();
								}
							}
						}
					}
				}
				statement.close();
			}

			switch (MobHunting.getConfigManager().databaseVersion) {
			case 1:
				// create new V2 tables and migrate data.
				Bukkit.getLogger().info("[MobHunting] Database version " + MobHunting.getConfigManager().databaseVersion
						+ " detected. Migrating to V2");
				setupV2Tables(mConnection);
				MobHunting.getConfigManager().databaseVersion = 2;
				MobHunting.getConfigManager().saveConfig();
			case 2:
				// Create new V3 tables and migrate data;
				Bukkit.getLogger().info("[MobHunting] Database version " + MobHunting.getConfigManager().databaseVersion
						+ " detected. Migrating to V3");
				migrateDatabaseLayoutFromV2toV3(mConnection);
				migrate_mh_PlayersFromV2ToV3(mConnection);
				createRandomBountyPlayer(mConnection);
				setupTriggerV3(mConnection);
				MobHunting.getConfigManager().databaseVersion = 3;
				MobHunting.getConfigManager().saveConfig();
			case 3:
				Bukkit.getLogger().info("[MobHunting] Database version " + MobHunting.getConfigManager().databaseVersion
						+ " detected.");
				// DATABASE IS UPTODATE or NOT created => create new database
				setupV3Tables(mConnection);
				migrate_mh_PlayersFromV2ToV3(mConnection);
				createRandomBountyPlayer(mConnection);
				setupTriggerV3(mConnection);
				migrateDatabaseLayoutFromV3ToV4(mConnection);
				MobHunting.getConfigManager().databaseVersion = 4;
				MobHunting.getConfigManager().saveConfig();
			case 4:
				Bukkit.getLogger().info("[MobHunting] Database version " + MobHunting.getConfigManager().databaseVersion
						+ " detected.");
				setupV4Tables(mConnection);
				migrateDatabaseLayoutFromV3ToV4(mConnection);
				setupTriggerV4andV5(mConnection);
				addPlayerBalancesToV4(mConnection);
				MobHunting.getConfigManager().databaseVersion = 5;
				MobHunting.getConfigManager().saveConfig();
			case 5:
				Bukkit.getLogger().info("[MobHunting] Database version " + MobHunting.getConfigManager().databaseVersion
						+ " detected.");
				setupV5Tables(mConnection);
				setupTriggerV4andV5(mConnection);
			}

			insertMissingVanillaMobs();

			// Enable FOREIGN KEY for Sqlite database
			if (!MobHunting.getConfigManager().databaseType.equalsIgnoreCase("MySQL")) {
				Statement statement = mConnection.createStatement();
				statement.execute("PRAGMA foreign_keys = ON");
				statement.close();
			}
			mConnection.close();

		} catch (SQLException e) {
			throw new DataStoreException(e);
		}
	}

	/**
	 * Rollback of last transaction on Database.
	 * 
	 * @throws DataStoreException
	 */
	protected void rollback(Connection mConnection) throws DataStoreException {

		try {
			mConnection.rollback();
		} catch (SQLException e) {
			throw new DataStoreException(e);
		}
	}

	/**
	 * Shutdown: Commit and close database connection completely.
	 */
	@Override
	public void shutdown() throws DataStoreException {
		int n = 0;
		do {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			n++;
		} while (MobHunting.getDataStoreManager().isRunning() && n < 40);
		System.out.println("[MobHunting] Closing database connection.");
	}

	/**
	 * databaseFixLeaderboard - tries to fix inconsistens in the database. Will
	 * later be used for cleaning the database; deleting old data or so. This is
	 * not implemented yet.
	 * 
	 * @throws DataStoreException
	 */
	@Override
	public void databaseFixLeaderboard() throws DataStoreException {
		try {
			Connection mConnection = setupConnection();
			Statement statement = mConnection.createStatement();

			Messages.debug("Beginning cleaning of database");
			int result;
			result = statement.executeUpdate("DELETE FROM mh_Achievements WHERE PLAYER_ID NOT IN "
					+ "(SELECT PLAYER_ID FROM mh_Players " + "where mh_Achievements.PLAYER_ID=mh_Players.PLAYER_ID);");
			Messages.debug("%s rows was deleted from Mh_Achievements", result);
			result = statement.executeUpdate("DELETE FROM mh_AllTime WHERE PLAYER_ID NOT IN "
					+ "(SELECT PLAYER_ID FROM mh_Players " + "where mh_AllTime.PLAYER_ID=mh_Players.PLAYER_ID);");
			Messages.debug("%s rows was deleted from Mh_AllTime", result);
			result = statement.executeUpdate("DELETE FROM mh_Daily WHERE PLAYER_ID NOT IN "
					+ "(SELECT PLAYER_ID FROM mh_Players " + "where mh_Daily.PLAYER_ID=mh_Players.PLAYER_ID);");
			Messages.debug("%s rows was deleted from Mh_Daily", result);
			result = statement.executeUpdate("DELETE FROM mh_Monthly WHERE PLAYER_ID NOT IN "
					+ "(SELECT PLAYER_ID FROM mh_Players " + "where mh_Monthly.PLAYER_ID=mh_Players.PLAYER_ID);");
			Messages.debug("%s rows was deleted from Mh_Monthly", result);
			result = statement.executeUpdate("DELETE FROM mh_Weekly WHERE PLAYER_ID NOT IN "
					+ "(SELECT PLAYER_ID FROM mh_Players " + "where mh_Weekly.PLAYER_ID=mh_Players.PLAYER_ID);");
			Messages.debug("%s rows was deleted from Mh_Weekly", result);
			result = statement.executeUpdate("DELETE FROM mh_Yearly WHERE PLAYER_ID NOT IN "
					+ "(SELECT PLAYER_ID FROM mh_Players " + "where mh_Yearly.PLAYER_ID=mh_Players.PLAYER_ID);");
			Messages.debug("%s rows was deleted from Mh_Yearly", result);
			statement.close();
			mConnection.commit();
			mConnection.close();
			Bukkit.getLogger().info("MobHunting Database was cleaned");
		} catch (SQLException | DataStoreException e) {
			throw new DataStoreException(e);
		}
	}

	// ******************************************************************
	// V2 To V3 Database migration
	// ******************************************************************

	// Migrate from DatabaseLayout from V2 to V3
	public void migrateDatabaseLayoutFromV2toV3(Connection connection) throws SQLException {
		Bukkit.getLogger().info("[MobHunting] DATAMIGRATION FROM DATABASE LAYOUT V2 TO V3.");

		// rename old tables
		Statement create = connection.createStatement();
		// Check if tables are V3.
		try {
			ResultSet rs = create.executeQuery("SELECT MOB_ID from mh_Mobs LIMIT 0");
			rs.close();
		} catch (SQLException e) {
			// Tables are V2
			create.executeUpdate("ALTER TABLE mh_Daily RENAME TO mh_DailyV2");
			create.executeUpdate("ALTER TABLE mh_Weekly RENAME TO mh_WeeklyV2");
			create.executeUpdate("ALTER TABLE mh_Monthly RENAME TO mh_MonthlyV2");
			create.executeUpdate("ALTER TABLE mh_Yearly RENAME TO mh_YearlyV2");
			create.executeUpdate("ALTER TABLE mh_AllTime RENAME TO mh_AllTimeV2");
			create.executeUpdate("DROP TRIGGER IF EXISTS DailyInsert");
			create.executeUpdate("DROP TRIGGER IF EXISTS DailyUpdate");
		}
		create.close();

		// create new tables
		setupV3Tables(connection);

		// migrate data from old table3s to new tables.
		Statement statement = connection.createStatement();

		HashMap<String, Integer> mobs = new HashMap<>();
		try {
			ResultSet rs = statement.executeQuery("SELECT * FROM mh_Mobs");
			while (rs.next()) {
				mobs.put(rs.getString("MOBTYPE"), rs.getInt("MOB_ID"));
			}
			rs.close();
		} catch (Exception e) {
			Bukkit.getLogger().severe("Error while fetching Vanilla Mobs from mh_Mobs");
			e.printStackTrace();
		}

		try {
			int n = 0;
			ResultSet rs = statement.executeQuery(
					"SELECT * FROM mh_DailyV2" + " inner join mh_Players using (PLAYER_ID) WHERE NAME!=''");
			while (rs.next()) {
				int achievements = rs.getInt("ACHIEVEMENT_COUNT");
				Statement statement2 = connection.createStatement();
				for (MinecraftMob mob : MinecraftMob.values()) {
					String id = rs.getString("ID");
					int mob_id = mobs.get(mob.name());
					int player_id = rs.getInt("PLAYER_ID");
					try {
						int kills = rs.getInt(mob.name() + "_KILL");
						int assists = rs.getInt(mob.name() + "_ASSIST");
						if (kills > 0 || assists > 0 || achievements > 0) {
							String insertStr = "INSERT INTO mh_Daily VALUES (" + id + "," + mob_id + "," + player_id
									+ "," + achievements + "," + kills + "," + assists + ")";
							statement2.executeUpdate(insertStr);
							n++;
							achievements = 0;
						}
					} catch (SQLException e) {
					}
				}
				statement2.close();
				connection.commit();
			}
			rs.close();
			Bukkit.getLogger().info("[MobHunting] Migrated " + n + " records into mh_Daily.");
		} catch (SQLException e) {
			Bukkit.getLogger().severe("[MobHunting] Error while inserting data to new mh_Daily");
			e.printStackTrace();
		}

		try {
			int n = 0;
			ResultSet rs = statement.executeQuery(
					"SELECT * FROM mh_WeeklyV2" + " inner join mh_Players using (PLAYER_ID) WHERE NAME!=''");

			while (rs.next()) {
				int achievements = rs.getInt("ACHIEVEMENT_COUNT");
				Statement statement2 = connection.createStatement();
				for (MinecraftMob mob : MinecraftMob.values()) {
					String id = rs.getString("ID");
					int mob_id = mobs.get(mob.name());
					int player_id = rs.getInt("PLAYER_ID");
					try {
						int kills = rs.getInt(mob.name() + "_KILL");
						int assists = rs.getInt(mob.name() + "_ASSIST");
						if (kills > 0 || assists > 0 || achievements > 0) {
							String insertStr = "INSERT INTO mh_Weekly VALUES (" + id + "," + mob_id + "," + player_id
									+ "," + achievements + "," + kills + "," + assists + ")";
							statement2.executeUpdate(insertStr);
							n++;
							achievements = 0;
						}
					} catch (SQLException e) {
					}
				}
				statement2.close();
				connection.commit();
			}
			rs.close();
			Bukkit.getLogger().info("[MobHunting] Migrated " + n + " records into mh_Weekly.");
		} catch (SQLException e) {
			Bukkit.getLogger().severe("[MobHunting] Error while inserting data to new mh_Weekly");
			e.printStackTrace();
		}

		try {
			int n = 0;
			ResultSet rs = statement.executeQuery(
					"SELECT * FROM mh_MonthlyV2" + " inner join mh_Players using (PLAYER_ID) WHERE NAME!=''");

			while (rs.next()) {
				int achievements = rs.getInt("ACHIEVEMENT_COUNT");
				Statement statement2 = connection.createStatement();
				for (MinecraftMob mob : MinecraftMob.values()) {
					String id = rs.getString("ID");
					int mob_id = mobs.get(mob.name());
					int player_id = rs.getInt("PLAYER_ID");
					try {
						int kills = rs.getInt(mob.name() + "_KILL");
						int assists = rs.getInt(mob.name() + "_ASSIST");
						if (kills > 0 || assists > 0 || achievements > 0) {
							String insertStr = "INSERT INTO mh_Monthly VALUES (" + id + "," + mob_id + "," + player_id
									+ "," + achievements + "," + kills + "," + assists + ")";
							statement2.executeUpdate(insertStr);
							n++;
							achievements = 0;
						}
					} catch (SQLException e) {
					}
				}
				statement2.close();
				connection.commit();
			}
			rs.close();
			Bukkit.getLogger().info("[MobHunting] Migrated " + n + " records into mh_Monthly.");
		} catch (SQLException e) {
			Bukkit.getLogger().severe("[MobHunting] Error while inserting data to new mh_Monthly");
			e.printStackTrace();
		}

		try {
			int n = 0;
			ResultSet rs = statement.executeQuery(
					"SELECT * FROM mh_YearlyV2" + " inner join mh_Players using (PLAYER_ID) WHERE NAME!=''");

			while (rs.next()) {
				int achievements = rs.getInt("ACHIEVEMENT_COUNT");
				Statement statement2 = connection.createStatement();
				for (MinecraftMob mob : MinecraftMob.values()) {
					String id = rs.getString("ID");
					int mob_id = mobs.get(mob.name());
					int player_id = rs.getInt("PLAYER_ID");
					try {
						int kills = rs.getInt(mob.name() + "_KILL");
						int assists = rs.getInt(mob.name() + "_ASSIST");
						if (kills > 0 || assists > 0 || achievements > 0) {
							String insertStr = "INSERT INTO mh_Yearly VALUES (" + id + "," + mob_id + "," + player_id
									+ "," + achievements + "," + kills + "," + assists + ")";
							statement2.executeUpdate(insertStr);
							n++;
							achievements = 0;
						}
					} catch (SQLException e) {
					}
				}
				statement2.close();
				connection.commit();
			}
			rs.close();
			Bukkit.getLogger().info("[MobHunting] Migrated " + n + " records into mh_Yearly.");
		} catch (SQLException e) {
			Bukkit.getLogger().severe("[MobHunting] Error while inserting data to new mh_Yearly");
			e.printStackTrace();
		}

		try {
			int n = 0;
			ResultSet rs = statement.executeQuery(
					"SELECT * FROM mh_AllTimeV2" + " inner join mh_Players using (PLAYER_ID) WHERE NAME!=''");

			while (rs.next()) {
				int achievements = rs.getInt("ACHIEVEMENT_COUNT");
				Statement statement2 = connection.createStatement();
				for (MinecraftMob mob : MinecraftMob.values()) {
					int mob_id = mobs.get(mob.name());
					int player_id = rs.getInt("PLAYER_ID");
					try {
						int kills = rs.getInt(mob.name() + "_KILL");
						int assists = rs.getInt(mob.name() + "_ASSIST");
						if (kills > 0 || assists > 0 || achievements > 0) {
							String insertStr = "INSERT INTO mh_AllTime VALUES (" + mob_id + "," + player_id + ","
									+ achievements + "," + kills + "," + assists + ")";
							statement2.executeUpdate(insertStr);
							n++;
							achievements = 0;
						}
					} catch (SQLException e) {
					}
				}
				statement2.close();
				connection.commit();
			}
			rs.close();
			Bukkit.getLogger().info("[MobHunting] Migrated " + n + " records into mh_AllTime.");
		} catch (SQLException e) {
			Bukkit.getLogger().severe("[MobHunting] Error while inserting data to new mh_AllTime");
			e.printStackTrace();
		}

		statement.close();
		connection.commit();
	}

	private void migrateDatabaseLayoutFromV3ToV4(Connection mConnection) throws DataStoreException {
		Statement statement;
		try {
			statement = mConnection.createStatement();
			try {
				ResultSet rs = statement.executeQuery("SELECT TOTAL_CASH from mh_Daily LIMIT 0");
				rs.close();
			} catch (SQLException e) {
				statement.executeUpdate("alter table `mh_Daily` add column `TOTAL_CASH` REAL NOT NULL DEFAULT 0");
				System.out.println("[MobHunting] TOTAL_CASH added to mh_Daily.");
			}
			try {
				ResultSet rs = statement.executeQuery("SELECT TOTAL_CASH from mh_Weekly LIMIT 0");
				rs.close();
			} catch (SQLException e) {
				statement.executeUpdate("alter table `mh_Weekly` add column `TOTAL_CASH` REAL NOT NULL DEFAULT 0");
				System.out.println("[MobHunting] TOTAL_CASH added to mh_Weekly.");
			}
			try {
				ResultSet rs = statement.executeQuery("SELECT TOTAL_CASH from mh_Monthly LIMIT 0");
				rs.close();
			} catch (SQLException e) {
				statement.executeUpdate("alter table `mh_Monthly` add column `TOTAL_CASH` REAL NOT NULL DEFAULT 0");
				System.out.println("[MobHunting] TOTAL_CASH added to mh_Monthly.");
			}
			try {
				ResultSet rs = statement.executeQuery("SELECT TOTAL_CASH from mh_Yearly LIMIT 0");
				rs.close();
			} catch (SQLException e) {
				statement.executeUpdate("alter table `mh_Yearly` add column `TOTAL_CASH` REAL NOT NULL DEFAULT 0");
				System.out.println("[MobHunting] TOTAL_CASH added to mh_Yearly.");
			}
			try {
				ResultSet rs = statement.executeQuery("SELECT TOTAL_CASH from mh_AllTime LIMIT 0");
				rs.close();
			} catch (SQLException e) {
				statement.executeUpdate("alter table `mh_AllTime` add column `TOTAL_CASH` REAL NOT NULL DEFAULT 0");
				System.out.println("[MobHunting] TOTAL_CASH added to mh_AllTime.");
			}
			statement.close();
			mConnection.commit();
		} catch (SQLException e) {
			throw new DataStoreException(e);
		}
	}

	public int getMobIdFromExtendedMobType(String mobtype, MobPlugin plugin) {
		int res = 0;
		try {
			Connection mConnection = setupConnection();
			Statement statement = mConnection.createStatement();
			ResultSet rs = statement.executeQuery(
					"SELECT MOB_ID from mh_Mobs WHERE PLUGIN_ID=" + plugin.getId() + " AND MOBTYPE='" + mobtype + "'");
			if (rs.next())
				res = rs.getInt("MOB_ID");
			rs.close();
			statement.close();
			mConnection.close();
		} catch (SQLException e) {
			Bukkit.getLogger().severe("[MobHunting] The ExtendedMobType " + mobtype + " was not found");
			e.printStackTrace();
		} catch (DataStoreException e) {
			e.printStackTrace();
		}
		return res;
	}

	@Override
	public void insertMissingVanillaMobs() {
		Connection connection;
		try {
			connection = setupConnection();
			int n = 0;
			Statement statement = connection.createStatement();
			for (MinecraftMob mob : MinecraftMob.values())
				if (getMobIdFromExtendedMobType(mob.name(), MobPlugin.Minecraft) == 0) {
					statement
							.executeUpdate("INSERT INTO mh_Mobs (PLUGIN_ID, MOBTYPE) VALUES ( 0,'" + mob.name() + "')");
					n++;
				}
			if (n > 0)
				Bukkit.getLogger().info("[MobHunting] " + n + " Minecraft Vanilla Mobs was inserted to mh_Mobs");
			statement.close();
			connection.commit();
			connection.close();
		} catch (SQLException | DataStoreException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void insertMissingMythicMobs() {
		int n = 0;
		try {
			Connection mConnection = setupConnection();
			Statement statement = mConnection.createStatement();
			for (String mob : MythicMobsCompat.getMobRewardData().keySet())
				if (MythicMobsCompat.isMythicMob(mob) && getMobIdFromExtendedMobType(mob, MobPlugin.MythicMobs) == 0) {
					statement.executeUpdate("INSERT INTO mh_Mobs (PLUGIN_ID, MOBTYPE) VALUES (1,'" + mob + "')");
					n++;
				}
			if (n > 0)
				Bukkit.getLogger().info("[MobHunting] " + n + " MythicMobs was inserted to mh_Mobs");
			statement.close();
			mConnection.commit();
			mConnection.close();
		} catch (SQLException | DataStoreException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void insertMissingMythicMobs(String mob) {
		if (MythicMobsCompat.isMythicMob(mob) && getMobIdFromExtendedMobType(mob, MobPlugin.MythicMobs) == 0)
			try {
				Connection mConnection = setupConnection();
				Statement statement = mConnection.createStatement();
				statement.executeUpdate("INSERT INTO mh_Mobs (PLUGIN_ID, MOBTYPE) VALUES (1,'" + mob + "')");
				Bukkit.getLogger().info("[MobHunting] MythicMobs MobType " + mob + " was inserted to mh_Mobs");
				statement.close();
				mConnection.commit();
				mConnection.close();
			} catch (SQLException | DataStoreException e) {
				e.printStackTrace();
			}
	}

	@Override
	public void insertMissingCitizensMobs() {
		int n = 0;
		try {
			Connection mConnection = setupConnection();
			Statement statement = mConnection.createStatement();
			for (String mob : CitizensCompat.getMobRewardData().keySet())
				if (CitizensCompat.isNPC(Integer.valueOf(mob))
						&& getMobIdFromExtendedMobType(mob, MobPlugin.Citizens) == 0) {
					statement.executeUpdate("INSERT INTO mh_Mobs (PLUGIN_ID, MOBTYPE) VALUES (2,'" + mob + "')");
					n++;
				}
			if (n > 0)
				Bukkit.getLogger().info("[MobHunting] " + n + " Citizens NPC's was inserted to mh_Mobs");
			statement.close();
			mConnection.commit();
			mConnection.close();
		} catch (SQLException | DataStoreException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void insertCitizensMobs(String mob) {
		if (CitizensCompat.isNPC(Integer.valueOf(mob)) && getMobIdFromExtendedMobType(mob, MobPlugin.Citizens) == 0)
			try {
				Connection mConnection = setupConnection();
				Statement statement = mConnection.createStatement();
				statement.executeUpdate("INSERT INTO mh_Mobs (PLUGIN_ID, MOBTYPE) VALUES (2,'" + mob + "')");
				Bukkit.getLogger().info("[MobHunting] Citizens MobType " + mob + " was inserted to mh_Mobs");
				statement.close();
				mConnection.commit();
				mConnection.close();
			} catch (SQLException | DataStoreException e) {
				e.printStackTrace();
			}
	}

	@Override
	public void insertTARDISWeepingAngelsMobs() {
		int n = 0;
		try {
			Connection mConnection = setupConnection();
			Statement statement = mConnection.createStatement();
			for (String mob : TARDISWeepingAngelsCompat.getMobRewardData().keySet())
				if (getMobIdFromExtendedMobType(mob, MobPlugin.TARDISWeepingAngels) == 0) {
					statement.executeUpdate("INSERT INTO mh_Mobs (PLUGIN_ID, MOBTYPE) VALUES (3,'" + mob + "')");
					n++;
				}
			if (n > 0)
				Bukkit.getLogger().info("[MobHunting] " + n + " TARDISWeepingAngel mobs was inserted to mh_Mobs");
			statement.close();
			mConnection.commit();
			mConnection.close();
		} catch (SQLException | DataStoreException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void insertTARDISWeepingAngelsMobs(String mob) {
		if (getMobIdFromExtendedMobType(mob, MobPlugin.TARDISWeepingAngels) == 0)
			try {
				Connection mConnection = setupConnection();
				Statement statement = mConnection.createStatement();
				statement.executeUpdate("INSERT INTO mh_Mobs (PLUGIN_ID, MOBTYPE) VALUES (3,'" + mob + "')");
				Bukkit.getLogger().info("[MobHunting] TARDISWeepingAngel MobType " + mob + " was inserted to mh_Mobs");
				statement.close();
				mConnection.commit();
				mConnection.close();
			} catch (SQLException | DataStoreException e) {
				e.printStackTrace();
			}
	}

	@Override
	public void insertCustomMobs() {
		int n = 0;
		try {
			Connection mConnection = setupConnection();
			Statement statement = mConnection.createStatement();
			for (String mob : CustomMobsCompat.getMobRewardData().keySet())
				if (MobHunting.getExtendedMobManager().getMobIdFromMobTypeAndPluginID(mob, MobPlugin.CustomMobs) == 0) {
					statement.executeUpdate("INSERT INTO mh_Mobs (PLUGIN_ID, MOBTYPE) VALUES (4,'" + mob + "')");
					n++;
				}
			if (n > 0)
				Bukkit.getLogger().info("[MobHunting] " + n + " CustomMobs was inserted to mh_Mobs");
			statement.close();
			mConnection.commit();
			mConnection.close();
		} catch (SQLException | DataStoreException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void insertCustomMobs(String mob) {
		if (getMobIdFromExtendedMobType(mob, MobPlugin.CustomMobs) == 0)
			try {
				Connection mConnection = setupConnection();
				Statement statement = mConnection.createStatement();
				statement.executeUpdate("INSERT INTO mh_Mobs (PLUGIN_ID, MOBTYPE) VALUES (4,'" + mob + "')");
				Bukkit.getLogger().info("[MobHunting] CustomMobs MobType " + mob + " was inserted to mh_Mobs");
				statement.close();
				mConnection.commit();
				mConnection.close();
			} catch (SQLException | DataStoreException e) {
				e.printStackTrace();
			}
	}

	@Override
	public void insertMysteriousHalloweenMobs() {
		int n = 0;
		try {
			Connection mConnection = setupConnection();
			Statement statement = mConnection.createStatement();
			for (String mob : MysteriousHalloweenCompat.getMobRewardData().keySet())
				if (getMobIdFromExtendedMobType(mob, MobPlugin.MysteriousHalloween) == 0) {
					statement.executeUpdate("INSERT INTO mh_Mobs (PLUGIN_ID, MOBTYPE) VALUES (5,'" + mob + "')");
					n++;
				}
			if (n > 0)
				Bukkit.getLogger().info("[MobHunting] " + n + " MysteriousHalloween mobs was inserted to mh_Mobs");
			statement.close();
			mConnection.commit();
			mConnection.close();
		} catch (SQLException | DataStoreException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void insertMysteriousHalloweenMobs(String mob) {
		if (getMobIdFromExtendedMobType(mob, MobPlugin.MysteriousHalloween) == 0)
			try {
				Connection mConnection = setupConnection();
				Statement statement = mConnection.createStatement();
				statement.executeUpdate("INSERT INTO mh_Mobs (PLUGIN_ID, MOBTYPE) VALUES (5,'" + mob + "')");
				Bukkit.getLogger().info("[MobHunting] MysteriousHalloween MobType " + mob + " was inserted to mh_Mobs");
				statement.close();
				mConnection.commit();
				mConnection.close();
			} catch (SQLException | DataStoreException e) {
				e.printStackTrace();
			}
	}

	@Override
	public void insertSmartGiants() {
		int n = 0;
		try {
			Connection mConnection = setupConnection();
			Statement statement = mConnection.createStatement();
			for (String mob : SmartGiantsCompat.getMobRewardData().keySet())
				if (SmartGiantsCompat.isSmartGiants(mob)
						&& getMobIdFromExtendedMobType(mob, MobPlugin.SmartGiants) == 0) {
					statement.executeUpdate("INSERT INTO mh_Mobs (PLUGIN_ID, MOBTYPE) VALUES (6,'" + mob + "')");
					n++;
				}
			if (n > 0)
				Bukkit.getLogger().info("[MobHunting] " + n + " SmartGiants was inserted to mh_Mobs");
			statement.close();
			mConnection.commit();
			mConnection.close();
		} catch (SQLException | DataStoreException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void insertSmartGiants(String mob) {
		if (getMobIdFromExtendedMobType(mob, MobPlugin.SmartGiants) == 0)
			try {
				Connection mConnection = setupConnection();
				Statement statement = mConnection.createStatement();
				statement.executeUpdate("INSERT INTO mh_Mobs (PLUGIN_ID, MOBTYPE) VALUES (6,'" + mob + "')");
				Bukkit.getLogger().info("[MobHunting] SmartGiants MobType " + mob + " was inserted to mh_Mobs");
				statement.close();
				mConnection.commit();
				mConnection.close();
			} catch (SQLException | DataStoreException e) {
				e.printStackTrace();
			}
	}

	@Override
	public void insertInfernalMobs() {
		Connection connection;
		try {
			connection = setupConnection();
			int n = 0;
			Statement statement = connection.createStatement();
			for (MinecraftMob mob : MinecraftMob.values())
				if (getMobIdFromExtendedMobType(mob.name(), MobPlugin.InfernalMobs) == 0) {
					statement
							.executeUpdate("INSERT INTO mh_Mobs (PLUGIN_ID, MOBTYPE) VALUES ( 7,'" + mob.name() + "')");
					n++;
				}
			if (n > 0)
				Bukkit.getLogger().info("[MobHunting] " + n + " InfernalMobs was inserted to mh_Mobs");
			statement.close();
			connection.commit();
			connection.close();
		} catch (SQLException | DataStoreException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void insertHerobrineMobs() {
		int n = 0;
		try {
			Connection mConnection = setupConnection();
			Statement statement = mConnection.createStatement();
			for (String mob : HerobrineCompat.getMobRewardData().keySet())
				if (HerobrineCompat.isHerobrineMob(mob) && getMobIdFromExtendedMobType(mob, MobPlugin.Herobrine) == 0) {
					statement.executeUpdate("INSERT INTO mh_Mobs (PLUGIN_ID, MOBTYPE) VALUES (8,'" + mob + "')");
					n++;
				}
			if (n > 0)
				Bukkit.getLogger().info("[MobHunting] " + n + " Herobrine Mobs was inserted to mh_Mobs");
			statement.close();
			mConnection.commit();
			mConnection.close();
		} catch (SQLException | DataStoreException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void insertMissingHerobrineMobs(String mob) {
		if (HerobrineCompat.isHerobrineMob(mob) && getMobIdFromExtendedMobType(mob, MobPlugin.Herobrine) == 0)
			try {
				Connection mConnection = setupConnection();
				Statement statement = mConnection.createStatement();
				statement.executeUpdate("INSERT INTO mh_Mobs (PLUGIN_ID, MOBTYPE) VALUES (8,'" + mob + "')");
				Bukkit.getLogger().info("[MobHunting] Herobrine Mobs MobType " + mob + " was inserted to mh_Mobs");
				statement.close();
				mConnection.commit();
				mConnection.close();
			} catch (SQLException | DataStoreException e) {
				e.printStackTrace();
			}
	}

	// ******************************************************************
	// Bounties
	// ******************************************************************
	@Override
	public Set<Bounty> loadBounties(OfflinePlayer offlinePlayer) throws DataStoreException {
		Set<Bounty> bounties = new HashSet<Bounty>();
		try {
			Connection mConnection = setupConnection();
			int playerId = getPlayerId(offlinePlayer);
			openPreparedStatements(mConnection, PreparedConnectionType.GET_BOUNTIES);
			mGetBounties.setInt(1, playerId);
			mGetBounties.setInt(2, playerId);

			ResultSet set = mGetBounties.executeQuery();

			while (set.next()) {
				Bounty b = new Bounty(plugin);
				b.setBountyOwnerId(set.getInt(1));
				b.setBountyOwner(getPlayerByPlayerId(set.getInt(1)));
				b.setMobtype(set.getString(2));
				b.setWantedPlayerId(set.getInt(3));
				b.setWantedPlayer(getPlayerByPlayerId(set.getInt(3)));
				b.setNpcId(set.getInt(4));
				b.setMobId(set.getString(5));
				b.setWorldGroup(set.getString(6));
				b.setCreatedDate(set.getLong(7));
				b.setEndDate(set.getLong(8));
				b.setPrize(set.getDouble(9));
				b.setMessage(set.getString(10));
				b.setStatus(BountyStatus.valueOf(set.getInt(11)));
				if (b.isOpen())
					bounties.add(b);
			}
			set.close();
			mGetBounties.close();
			mConnection.close();
			return (Set<Bounty>) bounties;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DataStoreException(e);
		}
	};

	/**
	 * create a RandomBountyPlayer if not exist in mh_Players
	 * 
	 * @param connection
	 */
	private void createRandomBountyPlayer(Connection connection) {
		// added because BOUNTYOWNER_ID is null for Random bounties.
		try {
			Statement create = connection.createStatement();
			ResultSet rs = create.executeQuery("SELECT PLAYER_ID from mh_Players WHERE NAME='RandomBounty' LIMIT 0");
			if (!rs.next()) {
				System.out.println("[MobHunting] Adding RandomBounty Player to MobHunting Database.");
				create.executeUpdate(
						"insert into mh_Players (UUID,NAME,PLAYER_ID,LEARNING_MODE,MUTE_MODE) values (null,'RandomBounty',0,0,0)");
				create.executeUpdate("update mh_Players set Player_id=0 where name='RandomBounty'");
			}
			rs.close();
			create.close();
		} catch (SQLException e) {
			// e.printStackTrace();
		}
	}

	// ******************************************************************
	// Player Settings
	// ******************************************************************

	/**
	 * getPlayerSettings
	 * 
	 * @param offlinePlayer
	 *            :OfflinePlayer
	 * @return PlayerData
	 * @throws DataStoreException
	 * @throws SQLException
	 * 
	 */
	@Override
	public PlayerSettings loadPlayerSettings(OfflinePlayer offlinePlayer) throws DataStoreException, SQLException {
		Connection mConnection = setupConnection();
		openPreparedStatements(mConnection, PreparedConnectionType.GET_PLAYER_DATA);
		mGetPlayerData.setString(1, offlinePlayer.getUniqueId().toString());
		ResultSet result = mGetPlayerData.executeQuery();
		if (result.next()) {
			PlayerSettings ps = new PlayerSettings(offlinePlayer, result.getBoolean("LEARNING_MODE"),
					result.getBoolean("MUTE_MODE"), result.getDouble("BALANCE"), result.getDouble("BALANCE_CHANGES"),
					result.getDouble("BANK_BALANCE"), result.getDouble("BANK_BALANCE_CHANGES"));
			int id = result.getInt("PLAYER_ID");
			if (id != 0)
				ps.setPlayerId(id);
			result.close();
			Messages.debug("Reading Playersettings from Database: %s", ps.toString());
			mGetPlayerData.close();
			mConnection.close();
			return ps;
		}
		mGetPlayerData.close();
		mConnection.close();
		throw new UserNotFoundException("User " + offlinePlayer.toString() + " is not present in database");
	}

	/**
	 * insertPlayerSettings to database
	 */
	@Override
	public void insertPlayerSettings(PlayerSettings playerSettings) throws DataStoreException {
		Connection mConnection;
		try {
			mConnection = setupConnection();
			try {
				openPreparedStatements(mConnection, PreparedConnectionType.INSERT_PLAYER_DATA);
				mInsertPlayerData.setString(1, playerSettings.getPlayer().getUniqueId().toString());
				mInsertPlayerData.setString(2, playerSettings.getPlayer().getName());
				mInsertPlayerData.setInt(3, playerSettings.isLearningMode() ? 1 : 0);
				mInsertPlayerData.setInt(4, playerSettings.isMuted() ? 1 : 0);
				mInsertPlayerData.setDouble(5, playerSettings.getBalance());
				mInsertPlayerData.setDouble(6, playerSettings.getBalanceChanges());
				mInsertPlayerData.setDouble(7, playerSettings.getBankBalance());
				mInsertPlayerData.setDouble(8, playerSettings.getBankBalanceChanges());
				mInsertPlayerData.addBatch();
				mInsertPlayerData.executeBatch();
				mInsertPlayerData.close();
				mConnection.commit();
				mConnection.close();
			} catch (SQLException e) {
				rollback(mConnection);
				mConnection.close();
				throw new DataStoreException(e);
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

	}

	@Override
	public void savePlayerSettings(Set<PlayerSettings> playerDataSet) throws DataStoreException {
		Connection mConnection;
		try {
			mConnection = setupConnection();
			try {
				openPreparedStatements(mConnection, PreparedConnectionType.UPDATE_PLAYER_SETTINGS);
				for (PlayerSettings playerData : playerDataSet) {
					mUpdatePlayerSettings.setInt(1, playerData.isLearningMode() ? 1 : 0);
					mUpdatePlayerSettings.setInt(2, playerData.isMuted() ? 1 : 0);
					mUpdatePlayerSettings.setDouble(3, playerData.getBalance());
					mUpdatePlayerSettings.setDouble(4, playerData.getBalanceChanges());
					mUpdatePlayerSettings.setDouble(5, playerData.getBankBalance());
					mUpdatePlayerSettings.setDouble(6, playerData.getBankBalanceChanges());
					mUpdatePlayerSettings.setString(7, playerData.getPlayer().getUniqueId().toString());
					mUpdatePlayerSettings.addBatch();
				}
				mUpdatePlayerSettings.executeBatch();
				mUpdatePlayerSettings.close();
				mConnection.commit();
				mConnection.close();

				for (PlayerSettings playerData : playerDataSet) {
					if (plugin.getPlayerSettingsmanager().containsKey(playerData.getPlayer())
							&& !playerData.getPlayer().isOnline())
						plugin.getPlayerSettingsmanager().removePlayerSettings((Player) playerData.getPlayer());
				}

				Messages.debug("PlayerSettings saved.");

			} catch (SQLException e) {
				rollback(mConnection);
				mConnection.close();
				throw new DataStoreException(e);
			}
		} catch (SQLException e1) {
			throw new DataStoreException(e1);
		}
	}

	/**
	 * getPlayerID. get the player ID and check if the player has change name
	 * 
	 * @param offlinePlayer
	 * @return PlayerID: int
	 * @throws SQLException
	 * @throws DataStoreException
	 */
	public int getPlayerId(OfflinePlayer offlinePlayer) throws DataStoreException {
		if (offlinePlayer == null)
			return 0;
		int playerId = 0;
		PlayerSettings ps = plugin.getPlayerSettingsmanager().getPlayerSettings(offlinePlayer);
		if (ps != null)
			playerId = ps.getPlayerId();
		if (playerId == 0) {
			Connection mConnection;
			try {
				ArrayList<OfflinePlayer> changedNames = new ArrayList<OfflinePlayer>();

				mConnection = setupConnection();
				openPreparedStatements(mConnection, PreparedConnectionType.GET_PLAYER_DATA);
				mGetPlayerData.setString(1, offlinePlayer.getUniqueId().toString());
				ResultSet result = mGetPlayerData.executeQuery();
				if (result.next()) {
					String name = result.getString(2);
					UUID uuid = UUID.fromString(result.getString(1));
					if (name != null && uuid != null)
						if (offlinePlayer.getUniqueId().equals(uuid) && !offlinePlayer.getName().equals(name)) {
							MobHunting.getInstance().getLogger()
									.warning("[MobHunting] Name change detected(2): " + name + " -> "
											+ offlinePlayer.getName() + " UUID="
											+ offlinePlayer.getUniqueId().toString());
							changedNames.add(offlinePlayer);
						}
					playerId = result.getInt(3);
					result.close();

				}
				result.close();
				mGetPlayerData.close();
				mConnection.close();

				Iterator<OfflinePlayer> itr = changedNames.iterator();
				while (itr.hasNext()) {
					OfflinePlayer p = itr.next();
					Messages.debug("Updating playername in database and in memory (%s)", p.getName());
					updatePlayerName(p.getPlayer());
				}
			} catch (SQLException e) {
				throw new DataStoreException(e);
			}
		}
		return playerId;
	}

	/**
	 * updatePlayerName - update the players name in the Database
	 * 
	 * @param offlinePlayer
	 *            : OfflinePlayer
	 * @throws SQLException
	 * @throws DataStoreException
	 */
	protected void updatePlayerName(OfflinePlayer offlinePlayer) throws DataStoreException {
		Connection mConnection;
		try {
			mConnection = setupConnection();
			try {
				openPreparedStatements(mConnection, PreparedConnectionType.UPDATE_PLAYER_NAME);
				mUpdatePlayerName.setString(1, offlinePlayer.getName());
				mUpdatePlayerName.setString(2, offlinePlayer.getUniqueId().toString());
				mUpdatePlayerName.executeUpdate();
				mUpdatePlayerName.close();
				mConnection.commit();
				mConnection.close();
			} catch (SQLException e) {
				mConnection.close();
				throw new DataStoreException(e);
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

	}

	/**
	 * getPlayerByName - get the player
	 * 
	 * @param name
	 *            : String
	 * @return player
	 */
	@Override
	public OfflinePlayer getPlayerByName(String name) throws DataStoreException {
		if (name.equals("Random Bounty"))
			return null; // used for Random Bounties
		try {
			Connection mConnection = setupConnection();

			openPreparedStatements(mConnection, PreparedConnectionType.GET_PLAYER_UUID);
			mGetPlayerUUID.setString(1, name);
			ResultSet set = mGetPlayerUUID.executeQuery();

			if (set.next()) {
				UUID uid = UUID.fromString(set.getString(1));
				set.close();
				mGetPlayerUUID.close();
				mConnection.close();
				return Bukkit.getOfflinePlayer(uid);
			}
			mGetPlayerUUID.close();
			mConnection.close();
			throw new UserNotFoundException("[MobHunting] User " + name + " is not present in database");
		} catch (SQLException e) {
			throw new DataStoreException(e);
		}
	}

	/**
	 * getPlayerByName - get the player
	 * 
	 * @param name
	 *            : String
	 * @return player
	 */
	@Override
	public OfflinePlayer getPlayerByPlayerId(int playerId) throws DataStoreException {
		if (playerId == 0)
			return null; // Used for Random Bounty
		try {
			Connection mConnection = setupConnection();
			openPreparedStatements(mConnection, PreparedConnectionType.GET_PLAYER_BY_PLAYER_ID);
			mGetPlayerByPlayerId.setInt(1, playerId);
			ResultSet set = mGetPlayerByPlayerId.executeQuery();

			if (set.next()) {
				UUID uid = UUID.fromString(set.getString(1));
				set.close();
				mGetPlayerByPlayerId.close();
				mConnection.close();
				return Bukkit.getOfflinePlayer(uid);
			}
			mGetPlayerByPlayerId.close();
			mConnection.close();
			throw new UserNotFoundException("[MobHunting] PlayerId " + playerId + " is not present in database");
		} catch (SQLException e) {
			throw new DataStoreException(e);
		}
	}

	// ******************************************************************
	// ACHIEVEMENTS
	// ******************************************************************

	/**
	 * Args: player id
	 */
	protected PreparedStatement mLoadAchievements;

	/**
	 * Args: player id, achievement, date, progress
	 */
	protected PreparedStatement mSaveAchievement;

	/**
	 * loadAchievements - loading the achievements for one player into memory
	 * 
	 * @param OfflinePlayer
	 *            :
	 * @throws DataStoreException
	 */
	@Override
	public Set<AchievementStore> loadAchievements(OfflinePlayer player) throws DataStoreException {
		HashSet<AchievementStore> achievements = new HashSet<AchievementStore>();
		try {
			Connection mConnection = setupConnection();
			openPreparedStatements(mConnection, PreparedConnectionType.LOAD_ARCHIEVEMENTS);
			int playerId = getPlayerId(player);
			if (playerId != 0) {
				mLoadAchievements.setInt(1, playerId);
				ResultSet set = mLoadAchievements.executeQuery();
				while (set.next()) {
					achievements.add(new AchievementStore(set.getString(1), player, set.getInt(3)));
				}
				set.close();
			}
			mLoadAchievements.close();
			mConnection.close();
			return achievements;
		} catch (SQLException e) {
			throw new DataStoreException(e);
		}
	}

	/**
	 * saveAchievements - save all achievements to the Database
	 */
	@Override
	public void saveAchievements(Set<AchievementStore> achievements) throws DataStoreException {
		Connection mConnection;
		try {
			mConnection = setupConnection();
			try {
				openPreparedStatements(mConnection, PreparedConnectionType.SAVE_ACHIEVEMENTS);
				for (AchievementStore achievement : achievements) {
					mSaveAchievement.setInt(1, getPlayerId(achievement.player));
					mSaveAchievement.setString(2, achievement.id);
					mSaveAchievement.setDate(3, new Date(System.currentTimeMillis()));
					mSaveAchievement.setInt(4, achievement.progress);
					mSaveAchievement.addBatch();
				}
				mSaveAchievement.executeBatch();
				mSaveAchievement.close();
				mConnection.commit();
				mConnection.close();
			} catch (SQLException e) {
				rollback(mConnection);
				mConnection.close();
				throw new DataStoreException(e);
			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	// ******************************************************************
	// MOBS
	// ******************************************************************

	/**
	 * Args: player id
	 */
	protected PreparedStatement mLoadMobs;

	/**
	 * Args: player id, achievement, date, progress
	 */
	protected PreparedStatement mInsertMobs;

	/**
	 * Args: player id, achievement, date, progress
	 */
	protected PreparedStatement mUpdateMobs;

	/**
	 * loadMobs - load all mobs from database into memory
	 */
	@Override
	public Set<ExtendedMob> loadMobs() throws DataStoreException {
		HashSet<ExtendedMob> mobs = new HashSet<ExtendedMob>();
		try {
			Connection mConnection = setupConnection();
			openPreparedStatements(mConnection, PreparedConnectionType.LOAD_MOBS);
			ResultSet set = mLoadMobs.executeQuery();
			while (set.next()) {
				MobPlugin mp = PluginManager.valueOf(set.getInt("PLUGIN_ID"));
				switch (mp) {
				case Citizens:
					if (!CitizensCompat.isSupported() || CitizensCompat.isDisabledInConfig())
						continue;
					break;
				case CustomMobs:
					if (!CustomMobsCompat.isSupported() || CustomMobsCompat.isDisabledInConfig())
						continue;
					break;
				case MythicMobs:
					if (!MythicMobsCompat.isSupported() || MythicMobsCompat.isDisabledInConfig())
						continue;
					break;
				case TARDISWeepingAngels:
					if (!TARDISWeepingAngelsCompat.isSupported() || TARDISWeepingAngelsCompat.isDisabledInConfig())
						continue;
					break;
				case MysteriousHalloween:
					if (!MysteriousHalloweenCompat.isSupported() || MysteriousHalloweenCompat.isDisabledInConfig())
						continue;
					break;
				case SmartGiants:
					if (!SmartGiantsCompat.isSupported() || SmartGiantsCompat.isDisabledInConfig())
						continue;
					break;
				case Herobrine:
					if (!HerobrineCompat.isSupported() || HerobrineCompat.isDisabledInConfig())
						continue;
					break;
				case Minecraft:
					break;
				default:
					break;
				}
				mobs.add(new ExtendedMob(set.getInt("MOB_ID"), PluginManager.valueOf(set.getInt("PLUGIN_ID")),
						set.getString("MOBTYPE")));
			}
			set.close();
			mLoadMobs.close();
			mConnection.close();
			return mobs;
		} catch (SQLException e) {
			throw new DataStoreException(e);
		}
	}

	/**
	 * saveMobs - save all NEW mobs to the Database
	 */
	@Override
	public void insertMobs(Set<ExtendedMob> mobs) throws DataStoreException {
		Connection mConnection;
		try {
			mConnection = setupConnection();
			try {
				openPreparedStatements(mConnection, PreparedConnectionType.INSERT_MOBS);
				for (ExtendedMob mob : mobs) {
					mInsertMobs.setInt(1, mob.getMobPlugin().getId());
					mInsertMobs.setString(2, mob.getMobtype());

					mInsertMobs.addBatch();
				}
				mInsertMobs.executeBatch();
				mInsertMobs.close();
				mConnection.commit();
				mConnection.close();
			} catch (SQLException e) {
				rollback(mConnection);
				mConnection.close();
				throw new DataStoreException(e);
			}
		} catch (SQLException e1) {
			throw new DataStoreException(e1);
		}
	}

	/**
	 * updateMobs - update all EXSISTING mobs in the Database
	 */
	@Override
	public void updateMobs(Set<ExtendedMob> mobs) throws DataStoreException {
		Connection mConnection;
		try {
			mConnection = setupConnection();
			try {
				openPreparedStatements(mConnection, PreparedConnectionType.UPDATE_MOBS);
				for (ExtendedMob mob : mobs) {
					mUpdateMobs.setInt(1, mob.getMobPlugin().getId());
					mUpdateMobs.setString(2, mob.getMobtype());
					mUpdateMobs.setInt(3, mob.getMob_id());

					mUpdateMobs.addBatch();
				}
				mUpdateMobs.executeBatch();
				mUpdateMobs.close();
				mConnection.commit();
				mConnection.close();
			} catch (SQLException e) {
				rollback(mConnection);
				mConnection.close();
				throw new DataStoreException(e);
			}
		} catch (SQLException e1) {
			throw new DataStoreException(e1);
		}
	}

	// Migrate from DatabaseLayout from V2 to V3
	public void migrate_mh_PlayersFromV2ToV3(Connection connection) throws SQLException {
		boolean migrateData = false;
		Statement statement = connection.createStatement();
		// Check if tables are V3.
		try {
			ResultSet rs = statement.executeQuery("SELECT * from mh_PlayersV2 LIMIT 0");
			rs.close();
		} catch (SQLException e) {
			// Tables are V2 => rename table and migrate data
			migrateData = true;
			Bukkit.getLogger().info("[MobHunting] Rename mh_Players to mh_PlayersV2.");
			statement.executeUpdate("ALTER TABLE mh_Players RENAME TO mh_PlayersV2");
		}

		if (migrateData) {
			// create new tables
			setupV3Tables(connection);

			// migrate data from old table3s to new tables.
			try {
				String insertStr = "INSERT INTO mh_Players(UUID, NAME, PLAYER_ID, LEARNING_MODE, MUTE_MODE)"
						+ "SELECT UUID,NAME,PLAYER_ID,LEARNING_MODE,MUTE_MODE FROM mh_PlayersV2";
				int n = statement.executeUpdate(insertStr);
				if (n > 0)
					Bukkit.getLogger().info("[MobHunting] Migrated " + n + " players into the new mh_Players.");
			} catch (SQLException e) {
				Bukkit.getLogger().severe("[MobHunting] Error while inserting data to new mh_Players");
				e.printStackTrace();
			}
		}

		if (MobHunting.getConfigManager().databaseType.equalsIgnoreCase("mysql")) {
			try {
				statement.executeUpdate("ALTER TABLE mh_Daily DROP FOREIGN KEY mh_Daily_ibfk_1;");
				statement.executeUpdate("ALTER TABLE mh_Weekly DROP FOREIGN KEY mh_Weekly_ibfk_1;");
				statement.executeUpdate("ALTER TABLE mh_Monthly DROP FOREIGN KEY mh_Monthly_ibfk_1;");
				statement.executeUpdate("ALTER TABLE mh_Yearly DROP FOREIGN KEY mh_Yearly_ibfk_1;");
				statement.executeUpdate("ALTER TABLE mh_AllTime DROP FOREIGN KEY mh_AllTime_ibfk_1;");

				Bukkit.getLogger().info("[MobHunting] Dropped foreign keys on mh_Players.PLAYER_ID");
				statement.executeUpdate(
						"ALTER TABLE mh_Daily ADD CONSTRAINT mh_Daily_Player_Id FOREIGN KEY(PLAYER_ID) REFERENCES mh_Players(PLAYER_ID) ON DELETE CASCADE;");
				statement.executeUpdate(
						"ALTER TABLE mh_Weekly ADD CONSTRAINT mh_Weekly_Player_Id FOREIGN KEY(PLAYER_ID) REFERENCES mh_Players(PLAYER_ID) ON DELETE CASCADE;");
				statement.executeUpdate(
						"ALTER TABLE mh_Monthly ADD CONSTRAINT mh_Monthly_Player_Id FOREIGN KEY(PLAYER_ID) REFERENCES mh_Players(PLAYER_ID) ON DELETE CASCADE;");
				statement.executeUpdate(
						"ALTER TABLE mh_Yearly ADD CONSTRAINT mh_Yearly_Player_Id FOREIGN KEY(PLAYER_ID) REFERENCES mh_Players(PLAYER_ID) ON DELETE CASCADE;");
				statement.executeUpdate(
						"ALTER TABLE mh_AllTime ADD CONSTRAINT mh_AllTime_Player_Id FOREIGN KEY(PLAYER_ID) REFERENCES mh_Players(PLAYER_ID) ON DELETE CASCADE;");
				Bukkit.getLogger().info("[MobHunting] Added constraints on mh_Players.PLAYER_ID");
			} catch (SQLException e) {
				Bukkit.getLogger().info("[MobHunting] Moving constraints is already done.");
			}

			try {
				statement.executeUpdate("ALTER TABLE mh_Achievements DROP FOREIGN KEY mh_Achievements_ibfk_1;");
				Bukkit.getLogger().info("[MobHunting] Dropped constraints on mh_Achievements");
			} catch (SQLException e) {
			}

			try {
				statement.executeUpdate(
						"ALTER TABLE mh_Achievements ADD CONSTRAINT mh_Achievements_Player_Id FOREIGN KEY(PLAYER_ID) REFERENCES mh_Players(PLAYER_ID) ON DELETE CASCADE;");

				Bukkit.getLogger().info("[MobHunting] Added constraints on mh_Achievements");
			} catch (SQLException e) {
			}

			try {
				statement.executeUpdate("ALTER TABLE mh_Bounties DROP FOREIGN KEY mh_Bounties_ibfk_1;");
				statement.executeUpdate("ALTER TABLE mh_Bounties DROP FOREIGN KEY mh_Bounties_ibfk_2;");

				Bukkit.getLogger().info("[MobHunting] Dropped constraints on mh_Bounties");
			} catch (SQLException e) {
			}

			try {
				statement.executeUpdate(
						"ALTER TABLE mh_Bounties ADD CONSTRAINT mh_Bounties_Player_Id_1 FOREIGN KEY(BOUNTYOWNER_ID) REFERENCES mh_Players(PLAYER_ID) ON DELETE CASCADE;");
				statement.executeUpdate(
						"ALTER TABLE mh_Bounties ADD CONSTRAINT mh_Bounties_Player_Id_2 FOREIGN KEY(WANTEDPLAYER_ID) REFERENCES mh_Players(PLAYER_ID) ON DELETE CASCADE;");

				Bukkit.getLogger().info("[MobHunting] Added constraints on mh_Bounties");
			} catch (SQLException e) {
			}

			try {
				statement.executeUpdate("alter table mh_Daily drop FOREIGN KEY `mh_Daily_Player_Id`;");
				statement.executeUpdate("alter table mh_Weekly drop FOREIGN KEY `mh_Weekly_Player_Id`;");
				statement.executeUpdate("alter table mh_Monthly drop FOREIGN KEY `mh_Monthly_Player_Id`;");
				statement.executeUpdate("alter table mh_Yearly drop FOREIGN KEY `mh_Yearly_Player_Id`;");
				statement.executeUpdate("alter table mh_AllTime drop FOREIGN KEY `mh_AllTime_Player_Id`;");
				statement.executeUpdate("alter table mh_Achievements drop FOREIGN KEY `mh_Achievements_Player_Id`;");
				statement.executeUpdate("alter table mh_Bounties drop FOREIGN KEY `mh_Bounties_Player_Id_1`;");
				statement.executeUpdate("alter table mh_Bounties drop FOREIGN KEY `mh_Bounties_Player_Id_2`;");
			} catch (SQLException e) {
			}

			try {
				statement.executeUpdate(
						"alter table mh_Daily add CONSTRAINT `mh_Daily_Player_Id` FOREIGN KEY (`PLAYER_ID`) REFERENCES `mh_Players` (`PLAYER_ID`) ON DELETE CASCADE;");
				statement.executeUpdate(
						"alter table mh_Weekly add CONSTRAINT `mh_Weekly_Player_Id` FOREIGN KEY (`PLAYER_ID`) REFERENCES `mh_Players` (`PLAYER_ID`) ON DELETE CASCADE;");
				statement.executeUpdate(
						"alter table mh_Monthly add CONSTRAINT `mh_Monthly_Player_Id` FOREIGN KEY (`PLAYER_ID`) REFERENCES `mh_Players` (`PLAYER_ID`) ON DELETE CASCADE;");
				statement.executeUpdate(
						"alter table mh_Yearly add CONSTRAINT `mh_Yearly_Player_Id` FOREIGN KEY (`PLAYER_ID`) REFERENCES `mh_Players` (`PLAYER_ID`) ON DELETE CASCADE;");
				statement.executeUpdate(
						"alter table mh_AllTime add CONSTRAINT `mh_AllTime_Player_Id` FOREIGN KEY (`PLAYER_ID`) REFERENCES `mh_Players` (`PLAYER_ID`) ON DELETE CASCADE;");
				statement.executeUpdate(
						"alter table mh_Achievements add CONSTRAINT `mh_Achievements_Player_Id` FOREIGN KEY (`PLAYER_ID`) REFERENCES `mh_Players` (`PLAYER_ID`) ON DELETE CASCADE;");
				statement.executeUpdate(
						"alter table mh_Bounties add CONSTRAINT `mh_Bounties_Player_Id_1` FOREIGN KEY (`BOUNTYOWNER_ID`) REFERENCES `mh_Players` (`PLAYER_ID`) ON DELETE CASCADE;");
				statement.executeUpdate(
						"alter table mh_Bounties add CONSTRAINT `mh_Bounties_Player_Id_2` FOREIGN KEY (`WANTEDPLAYER_ID`) REFERENCES `mh_Players` (`PLAYER_ID`) ON DELETE CASCADE;");
			} catch (SQLException e) {
				e.printStackTrace();
			}

		}

		statement.close();
		connection.commit();
	}

	private void addPlayerBalancesToV4(Connection connection) throws SQLException {
		Statement statement = connection.createStatement();

		try {
			ResultSet rs = statement.executeQuery("SELECT BALANCE from `mh_Players` LIMIT 0");
			rs.close();
		} catch (SQLException e) {
			try {
				System.out.println("[MobHunting] Adding Player balances to MobHunting Database V4.");
				statement.executeUpdate("alter table `mh_Players` add column `BALANCE` REAL NOT NULL DEFAULT 0");
				statement.executeUpdate("alter table `mh_Players` add column `BALANCE_CHANGES` REAL NOT NULL DEFAULT 0");
				statement.executeUpdate("alter table `mh_Players` add column `BANK_BALANCE` REAL NOT NULL DEFAULT 0");
				statement.executeUpdate("alter table `mh_Players` add column `BANK_BALANCE_CHANGES` REAL NOT NULL DEFAULT 0");
				System.out.println("[MobHunting] MobHunting Database upgraded to V5.");
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		statement.close();
		connection.commit();
	}

}
