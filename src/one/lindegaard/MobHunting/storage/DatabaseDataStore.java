package one.lindegaard.MobHunting.storage;

import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import one.lindegaard.CustomItemsLib.Core;
import one.lindegaard.CustomItemsLib.mobs.MobType;
import one.lindegaard.CustomItemsLib.storage.DataStoreException;
import one.lindegaard.CustomItemsLib.storage.UserNotFoundException;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.bounty.Bounty;
import one.lindegaard.MobHunting.bounty.BountyStatus;
import one.lindegaard.MobHunting.compatibility.BossCompat;
import one.lindegaard.MobHunting.compatibility.CitizensCompat;
import one.lindegaard.MobHunting.compatibility.CustomMobsCompat;
import one.lindegaard.MobHunting.compatibility.EliteMobsCompat;
import one.lindegaard.MobHunting.compatibility.HerobrineCompat;
import one.lindegaard.MobHunting.compatibility.MysteriousHalloweenCompat;
import one.lindegaard.MobHunting.compatibility.MythicMobsCompat;
import one.lindegaard.MobHunting.compatibility.SmartGiantsCompat;
import one.lindegaard.MobHunting.compatibility.TARDISWeepingAngelsCompat;
import one.lindegaard.MobHunting.mobs.MobPluginManager;
import one.lindegaard.MobHunting.mobs.MobPlugin;
import one.lindegaard.MobHunting.mobs.ExtendedMob;

public abstract class DatabaseDataStore implements IDataStore {

	private MobHunting plugin;

	public DatabaseDataStore(MobHunting plugin) {
		this.plugin = plugin;
	}

	/**
	 * Args: player id
	 */
	protected PreparedStatement mSavePlayerStats;

	/**
	 * Args: UUID
	 */
	protected PreparedStatement mGetOldPlayerData;

	/**
	 * Args: Player OfflinePLayer
	 */
	protected PreparedStatement mGetBounties;

	/**
	 * Args: Bounty
	 */
	protected PreparedStatement mInsertBounty;

	/**
	 * Args: Bounty ID
	 */
	protected PreparedStatement mDeleteBounty;

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
	 * Setup / Create database version 6 tables for MobHunting
	 */
	protected abstract void setupV6Tables(Connection connection) throws SQLException;

	/**
	 * Setup / Create database version 7 tables for MobHunting
	 */
	protected abstract void setupV8Tables(Connection connection) throws SQLException;

	/**
	 * Setup / Migrate from database version 5 to version 6 tables for MobHunting
	 * 
	 * @throws DataStoreException
	 */
	protected abstract void migrateDatabaseLayoutFromV5ToV6(Connection connection) throws DataStoreException;

	/**
	 * Setup / Migrate from database version 6 to version 7 tables for MobHunting
	 * 
	 * @throws DataStoreException
	 */
	protected abstract void migrateDatabaseLayoutFromV6ToV7(Connection connection) throws DataStoreException;

	/**
	 * Setup / Migrate from database version 7 to version 8 tables for MobHunting
	 * 
	 * @throws DataStoreException
	 */
	protected abstract boolean migrateDatabaseLayoutFromV7ToV8(Connection connection) throws DataStoreException;

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
		LOAD_ARCHIEVEMENTS, SAVE_ACHIEVEMENTS, GET_BOUNTIES, INSERT_BOUNTY, DELETE_BOUNTY, LOAD_MOBS, INSERT_MOBS,
		UPDATE_MOBS, SAVE_PLAYER_STATS, GET_OLD_PLAYERDATA,
	};

	/**
	 * Initialize the connection. Must be called after Opening of initial
	 * connection. Open Prepared statements for batch processing large selections of
	 * players. Batches will be performed in batches of 10,5,2,1
	 */
	@Override
	public void initialize() throws DataStoreException {
		try {

			Connection mConnection = setupConnection();

			int newest_db_version = 8;

			// Find current database version
			if (plugin.getConfigManager().databaseVersion < newest_db_version) {

				// Find current database version
				if (plugin.getConfigManager().databaseVersion == 0) {
					Statement statement = mConnection.createStatement();
					try {
						ResultSet rs = statement.executeQuery("SELECT TEXTURE FROM mh_Players LIMIT 0");
						rs.close();
						/**
						 * The TABLE Coloumn BALANCE only exists in Database layout V5
						 **/
						plugin.getConfigManager().databaseVersion = 6;
						plugin.getConfigManager().saveConfig();
					} catch (SQLException e6) {
						try {
							ResultSet rs = statement.executeQuery("SELECT BALANCE FROM mh_Players LIMIT 0");
							rs.close();
							/**
							 * The TABLE Coloumn BALANCE only exists in Database layout V5
							 **/
							plugin.getConfigManager().databaseVersion = 5;
							plugin.getConfigManager().saveConfig();
						} catch (SQLException e5) {
							try {
								ResultSet rs = statement.executeQuery("SELECT TOTAL_CASH FROM mh_Daily LIMIT 0");
								rs.close();
								/**
								 * The TABLE Coloumn TOTAL_CASH only exists in Database layout V4
								 **/
								plugin.getConfigManager().databaseVersion = 4;
								plugin.getConfigManager().saveConfig();
							} catch (SQLException e4) {
								try {
									ResultSet rs = statement.executeQuery("SELECT MOB_ID FROM mh_Mobs LIMIT 0");
									rs.close();
									/**
									 * The TABLE mh_Mobs created for V3 and does only contain data after migration
									 **/
									plugin.getConfigManager().databaseVersion = 3;
									plugin.getConfigManager().saveConfig();
								} catch (SQLException e3) {
									try {
										ResultSet rs = statement.executeQuery("SELECT UUID from mh_Players LIMIT 0");
										rs.close();
										// Player UUID is migrated in V2
										plugin.getConfigManager().databaseVersion = 2;
										plugin.getConfigManager().saveConfig();
									} catch (SQLException e2) {
										/**
										 * database if from before Minecraft 1.7.9 R1 (No UUID) = V1
										 **/
										try {
											ResultSet rs = statement
													.executeQuery("SELECT PLAYER_ID from mh_Players LIMIT 0");
											rs.close();
											plugin.getConfigManager().databaseVersion = 1;
											plugin.getConfigManager().saveConfig();
										} catch (SQLException e1) {
											/**
											 * DATABASE DOES NOT EXIST AT ALL, CREATE NEW EMPTY DATABASE
											 **/
											plugin.getConfigManager().databaseVersion = 7;
											plugin.getConfigManager().saveConfig();
										}
									}
								}
							}
						}
					}
					statement.close();
				}
			}

			switch (plugin.getConfigManager().databaseVersion) {
			case 1:
				// create new V2 tables and migrate data.
				Bukkit.getConsoleSender()
						.sendMessage(ChatColor.GOLD + "[MobHunting] " + ChatColor.RESET + "Database version "
								+ plugin.getConfigManager().databaseVersion + " detected. Migrating to V2");
				setupV2Tables(mConnection);
				plugin.getConfigManager().databaseVersion = 2;
				plugin.getConfigManager().saveConfig();
			case 2:
				// Create new V3 tables and migrate data;
				Bukkit.getConsoleSender()
						.sendMessage(ChatColor.GOLD + "[MobHunting] " + ChatColor.RESET + "Database version "
								+ plugin.getConfigManager().databaseVersion + " detected. Migrating to V3");
				migrateDatabaseLayoutFromV2toV3(mConnection);
				migrate_mh_PlayersFromV2ToV3(mConnection);
				plugin.getConfigManager().databaseVersion = 3;
				plugin.getConfigManager().saveConfig();
			case 3:
				Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "[MobHunting] " + ChatColor.RESET
						+ "Database version " + plugin.getConfigManager().databaseVersion + " detected.");
				// DATABASE IS UPTODATE or NOT created => create new database
				setupV3Tables(mConnection);
				migrate_mh_PlayersFromV2ToV3(mConnection);
				migrateDatabaseLayoutFromV3ToV4(mConnection);
				plugin.getConfigManager().databaseVersion = 4;
				plugin.getConfigManager().saveConfig();
			case 4:
				Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "[MobHunting] " + ChatColor.RESET
						+ "Database version " + plugin.getConfigManager().databaseVersion + " detected.");
				setupV4Tables(mConnection);
				migrateDatabaseLayoutFromV3ToV4(mConnection);
				plugin.getConfigManager().databaseVersion = 5;
				plugin.getConfigManager().saveConfig();
			case 5:
				Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "[MobHunting] " + ChatColor.RESET
						+ "Database version " + plugin.getConfigManager().databaseVersion + " detected.");
				setupV5Tables(mConnection);
				migrateDatabaseLayoutFromV5ToV6(mConnection);
				plugin.getConfigManager().databaseVersion = 6;
				plugin.getConfigManager().saveConfig();
			case 6:
				Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "[MobHunting] " + ChatColor.RESET
						+ "Database version " + plugin.getConfigManager().databaseVersion + " detected.");
				setupV6Tables(mConnection);
				migrateDatabaseLayoutFromV6ToV7(mConnection);
				plugin.getConfigManager().databaseVersion = 7;
				plugin.getConfigManager().saveConfig();
			case 7:
				Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "[MobHunting] " + ChatColor.RESET
						+ "Database version " + plugin.getConfigManager().databaseVersion + " detected.");
				setupV6Tables(mConnection);
				setupTriggerV4andV5(mConnection);
				if (migrateDatabaseLayoutFromV7ToV8(mConnection))
					plugin.getConfigManager().databaseVersion = 8;
				else {
					plugin.getConfigManager().databaseVersion = 7;
					break;
				}
				plugin.getConfigManager().saveConfig();
			case 8:
				setupV8Tables(mConnection);
				setupTriggerV4andV5(mConnection);
				plugin.getConfigManager().databaseVersion = 8;
				plugin.getConfigManager().saveConfig();
			}

			insertMissingVanillaMobs();

			// Enable FOREIGN KEY for Sqlite database
			if (!plugin.getConfigManager().databaseType.equalsIgnoreCase("MySQL")) {
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
		} while (plugin.getDataStoreManager().isRunning() && n < 40);
		Bukkit.getConsoleSender()
				.sendMessage(ChatColor.GOLD + "[MobHunting] " + ChatColor.RESET + "Closing database connection.");
	}

	/**
	 * databaseFixLeaderboard - tries to fix inconsistens in the database. Will
	 * later be used for cleaning the database; deleting old data or so. This is not
	 * implemented yet.
	 * 
	 * @throws DataStoreException
	 */
	@Override
	public void databaseFixLeaderboard() throws DataStoreException {
		try {
			Connection mConnection = setupConnection();
			Statement statement = mConnection.createStatement();

			plugin.getMessages().debug("Beginning cleaning of database");
			int result;
			result = statement.executeUpdate("DELETE FROM mh_Achievements WHERE PLAYER_ID NOT IN "
					+ "(SELECT PLAYER_ID FROM mh_Players " + "where mh_Achievements.PLAYER_ID=mh_Players.PLAYER_ID);");
			plugin.getMessages().debug("%s rows was deleted from Mh_Achievements", result);
			result = statement.executeUpdate("DELETE FROM mh_AllTime WHERE PLAYER_ID NOT IN "
					+ "(SELECT PLAYER_ID FROM mh_Players " + "where mh_AllTime.PLAYER_ID=mh_Players.PLAYER_ID);");
			plugin.getMessages().debug("%s rows was deleted from Mh_AllTime", result);
			result = statement.executeUpdate("DELETE FROM mh_Daily WHERE PLAYER_ID NOT IN "
					+ "(SELECT PLAYER_ID FROM mh_Players " + "where mh_Daily.PLAYER_ID=mh_Players.PLAYER_ID);");
			plugin.getMessages().debug("%s rows was deleted from Mh_Daily", result);
			result = statement.executeUpdate("DELETE FROM mh_Monthly WHERE PLAYER_ID NOT IN "
					+ "(SELECT PLAYER_ID FROM mh_Players " + "where mh_Monthly.PLAYER_ID=mh_Players.PLAYER_ID);");
			plugin.getMessages().debug("%s rows was deleted from Mh_Monthly", result);
			result = statement.executeUpdate("DELETE FROM mh_Weekly WHERE PLAYER_ID NOT IN "
					+ "(SELECT PLAYER_ID FROM mh_Players " + "where mh_Weekly.PLAYER_ID=mh_Players.PLAYER_ID);");
			plugin.getMessages().debug("%s rows was deleted from Mh_Weekly", result);
			result = statement.executeUpdate("DELETE FROM mh_Yearly WHERE PLAYER_ID NOT IN "
					+ "(SELECT PLAYER_ID FROM mh_Players " + "where mh_Yearly.PLAYER_ID=mh_Players.PLAYER_ID);");
			plugin.getMessages().debug("%s rows was deleted from Mh_Yearly", result);
			statement.close();
			mConnection.commit();
			mConnection.close();
			Bukkit.getConsoleSender().sendMessage(
					ChatColor.GOLD + "[MobHunting] " + ChatColor.RESET + "MobHunting Database was cleaned");
		} catch (SQLException | DataStoreException e) {
			throw new DataStoreException(e);
		}
	}

	// ********************************************************************************************************
	// Achievements
	// ********************************************************************************************************

	@Override
	public void resetAchievements() throws DataStoreException {
		try {
			Connection mConnection = setupConnection();
			Statement statement = mConnection.createStatement();

			plugin.getMessages().debug("Deleting achievements data from the database");
			int result;
			result = statement.executeUpdate("DELETE FROM mh_Achievements;");
			plugin.getMessages().debug("%s rows was deleted from Mh_Achievements", result);
			statement.close();
			mConnection.commit();
			mConnection.close();
		} catch (SQLException | DataStoreException e) {
			throw new DataStoreException(e);
		}
	}

	@Override
	public void resetStatistics() throws DataStoreException {
		try {
			Connection mConnection = setupConnection();
			Statement statement = mConnection.createStatement();

			plugin.getMessages().debug("Deleting statistics data from the database");
			int result;
			result = statement.executeUpdate("DELETE FROM mh_AllTime;");
			plugin.getMessages().debug("%s rows was deleted from Mh_AllTime", result);
			result = statement.executeUpdate("DELETE FROM mh_Daily;");
			plugin.getMessages().debug("%s rows was deleted from Mh_Daily", result);
			result = statement.executeUpdate("DELETE FROM mh_Monthly;");
			plugin.getMessages().debug("%s rows was deleted from Mh_Monthly", result);
			result = statement.executeUpdate("DELETE FROM mh_Weekly;");
			plugin.getMessages().debug("%s rows was deleted from Mh_Weekly", result);
			result = statement.executeUpdate("DELETE FROM mh_Yearly;");
			plugin.getMessages().debug("%s rows was deleted from Mh_Yearly", result);
			statement.close();
			mConnection.commit();
			mConnection.close();
		} catch (SQLException | DataStoreException e) {
			throw new DataStoreException(e);
		}
	}

	@Override
	public void resetBounties() throws DataStoreException {
		try {
			Connection mConnection = setupConnection();
			Statement statement = mConnection.createStatement();
			plugin.getMessages().debug("Deleting bounties from the database");
			int result;
			result = statement.executeUpdate("DELETE FROM mh_Bounties;");
			plugin.getMessages().debug("%s rows was deleted from mh_Bounties", result);
			statement.close();
			mConnection.commit();
			mConnection.close();
		} catch (SQLException | DataStoreException e) {
			throw new DataStoreException(e);
		}
	}

	@Override
	public void deleteExpiredBounties() {
		try {
			Connection mConnection = setupConnection();
			Statement statement = mConnection.createStatement();
			plugin.getMessages().debug("Deleting bounties from the database");
			int result;
			result = statement.executeUpdate("DELETE FROM mh_Bounties where STATUS!=0;");
			plugin.getMessages().debug("%s rows was deleted from mh_Bounties", result);
			statement.close();
			mConnection.commit();
			mConnection.close();
		} catch (SQLException | DataStoreException e) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "[MobHunting]" + ChatColor.RED
					+ "[Error]: Could not delete expired records from bounty database.");
		}
	}

	// ******************************************************************
	// V2 To V3 Database migration
	// ******************************************************************

	// Migrate from DatabaseLayout from V2 to V3
	public void migrateDatabaseLayoutFromV2toV3(Connection connection) throws SQLException {
		Bukkit.getConsoleSender().sendMessage(MobHunting.PREFIX + "DATAMIGRATION FROM DATABASE LAYOUT V2 TO V3.");

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
			Bukkit.getConsoleSender().sendMessage(MobHunting.PREFIX_ERROR + "while fetching Vanilla Mobs from mh_Mobs");
			e.printStackTrace();
		}

		try {
			int n = 0;
			ResultSet rs = statement.executeQuery(
					"SELECT * FROM mh_DailyV2" + " inner join mh_Players using (PLAYER_ID) WHERE NAME!=''");
			while (rs.next()) {
				int achievements = rs.getInt("ACHIEVEMENT_COUNT");
				Statement statement2 = connection.createStatement();
				for (MobType mob : MobType.values()) {
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
			Bukkit.getConsoleSender().sendMessage(MobHunting.PREFIX + "Migrated " + n + " records into mh_Daily.");
		} catch (SQLException e) {
			Bukkit.getConsoleSender()
					.sendMessage(MobHunting.PREFIX_ERROR + "Error while inserting data to new mh_Daily");
			e.printStackTrace();
		}

		try {
			int n = 0;
			ResultSet rs = statement.executeQuery(
					"SELECT * FROM mh_WeeklyV2" + " inner join mh_Players using (PLAYER_ID) WHERE NAME!=''");

			while (rs.next()) {
				int achievements = rs.getInt("ACHIEVEMENT_COUNT");
				Statement statement2 = connection.createStatement();
				for (MobType mob : MobType.values()) {
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
			Bukkit.getConsoleSender().sendMessage(MobHunting.PREFIX + "Migrated " + n + " records into mh_Weekly.");
		} catch (SQLException e) {
			Bukkit.getConsoleSender()
					.sendMessage(MobHunting.PREFIX_ERROR + "Error while inserting data to new mh_Weekly");
			e.printStackTrace();
		}

		try {
			int n = 0;
			ResultSet rs = statement.executeQuery(
					"SELECT * FROM mh_MonthlyV2" + " inner join mh_Players using (PLAYER_ID) WHERE NAME!=''");

			while (rs.next()) {
				int achievements = rs.getInt("ACHIEVEMENT_COUNT");
				Statement statement2 = connection.createStatement();
				for (MobType mob : MobType.values()) {
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
			Bukkit.getConsoleSender().sendMessage(MobHunting.PREFIX + "Migrated " + n + " records into mh_Monthly.");
		} catch (SQLException e) {
			Bukkit.getConsoleSender().sendMessage(MobHunting.PREFIX + "Error while inserting data to new mh_Monthly");
			e.printStackTrace();
		}

		try {
			int n = 0;
			ResultSet rs = statement.executeQuery(
					"SELECT * FROM mh_YearlyV2" + " inner join mh_Players using (PLAYER_ID) WHERE NAME!=''");

			while (rs.next()) {
				int achievements = rs.getInt("ACHIEVEMENT_COUNT");
				Statement statement2 = connection.createStatement();
				for (MobType mob : MobType.values()) {
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
			Bukkit.getConsoleSender().sendMessage(MobHunting.PREFIX + "Migrated " + n + " records into mh_Yearly.");
		} catch (SQLException e) {
			Bukkit.getConsoleSender()
					.sendMessage(MobHunting.PREFIX_ERROR + "Error while inserting data to new mh_Yearly");
			e.printStackTrace();
		}

		try {
			int n = 0;
			ResultSet rs = statement.executeQuery(
					"SELECT * FROM mh_AllTimeV2" + " inner join mh_Players using (PLAYER_ID) WHERE NAME!=''");

			while (rs.next()) {
				int achievements = rs.getInt("ACHIEVEMENT_COUNT");
				Statement statement2 = connection.createStatement();
				for (MobType mob : MobType.values()) {
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
			Bukkit.getConsoleSender().sendMessage(MobHunting.PREFIX + "Migrated " + n + " records into mh_AllTime.");
		} catch (SQLException e) {
			Bukkit.getConsoleSender()
					.sendMessage(MobHunting.PREFIX_ERROR + "Error while inserting data to new mh_AllTime");
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
			Bukkit.getConsoleSender()
					.sendMessage(MobHunting.PREFIX_WARNING + "The ExtendedMobType " + mobtype + " was not found");
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
			for (MobType mob : MobType.values())
				if (getMobIdFromExtendedMobType(mob.name(), MobPlugin.Minecraft) == 0) {
					statement
							.executeUpdate("INSERT INTO mh_Mobs (PLUGIN_ID, MOBTYPE) VALUES ( 0,'" + mob.name() + "')");
					n++;
				}
			if (n > 0)
				Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "[MobHunting] " + ChatColor.RESET + n
						+ " Minecraft Vanilla Mobs was inserted to mh_Mobs");
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
				Bukkit.getConsoleSender().sendMessage(
						ChatColor.GOLD + "[MobHunting] " + ChatColor.RESET + n + " MythicMobs was inserted to mh_Mobs");
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
				Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "[MobHunting] " + ChatColor.RESET
						+ "MythicMobs MobType " + mob + " was inserted to mh_Mobs");
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
				Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "[MobHunting] " + ChatColor.RESET + n
						+ " Citizens NPC's was inserted to mh_Mobs");
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
				Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "[MobHunting] " + ChatColor.RESET
						+ "Citizens MobType " + mob + " was inserted to mh_Mobs");
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
				Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "[MobHunting] " + ChatColor.RESET + n
						+ " TARDISWeepingAngel mobs was inserted to mh_Mobs");
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
				Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "[MobHunting] " + ChatColor.RESET
						+ "TARDISWeepingAngel MobType " + mob + " was inserted to mh_Mobs");
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
				if (plugin.getExtendedMobManager().getMobIdFromMobTypeAndPluginID(mob, MobPlugin.CustomMobs) == 0) {
					statement.executeUpdate("INSERT INTO mh_Mobs (PLUGIN_ID, MOBTYPE) VALUES (4,'" + mob + "')");
					n++;
				}
			if (n > 0)
				Bukkit.getConsoleSender().sendMessage(
						ChatColor.GOLD + "[MobHunting] " + ChatColor.RESET + n + " CustomMobs was inserted to mh_Mobs");
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
				Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "[MobHunting] " + ChatColor.RESET
						+ "CustomMobs MobType " + mob + " was inserted to mh_Mobs");
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
				Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "[MobHunting] " + ChatColor.RESET + n
						+ " MysteriousHalloween mobs was inserted to mh_Mobs");
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
				Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "[MobHunting] " + ChatColor.RESET
						+ "MysteriousHalloween MobType " + mob + " was inserted to mh_Mobs");
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
				Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "[MobHunting] " + ChatColor.RESET + n
						+ " SmartGiants was inserted to mh_Mobs");
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
				Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "[MobHunting] " + ChatColor.RESET
						+ "SmartGiants MobType " + mob + " was inserted to mh_Mobs");
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
			for (MobType mob : MobType.values())
				if (getMobIdFromExtendedMobType(mob.name(), MobPlugin.InfernalMobs) == 0) {
					statement
							.executeUpdate("INSERT INTO mh_Mobs (PLUGIN_ID, MOBTYPE) VALUES ( 7,'" + mob.name() + "')");
					n++;
				}
			if (n > 0)
				Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "[MobHunting] " + ChatColor.RESET + n
						+ " InfernalMobs was inserted to mh_Mobs");
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
				Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "[MobHunting] " + ChatColor.RESET + n
						+ " Herobrine Mobs was inserted to mh_Mobs");
			statement.close();
			mConnection.commit();
			mConnection.close();
		} catch (SQLException | DataStoreException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void insertHerobrineMob(String mob) {
		if (HerobrineCompat.isHerobrineMob(mob) && getMobIdFromExtendedMobType(mob, MobPlugin.Herobrine) == 0)
			try {
				Connection mConnection = setupConnection();
				Statement statement = mConnection.createStatement();
				statement.executeUpdate("INSERT INTO mh_Mobs (PLUGIN_ID, MOBTYPE) VALUES (8,'" + mob + "')");
				Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "[MobHunting] " + ChatColor.RESET
						+ "Herobrine Mobs MobType " + mob + " was inserted to mh_Mobs");
				statement.close();
				mConnection.commit();
				mConnection.close();
			} catch (SQLException | DataStoreException e) {
				e.printStackTrace();
			}
	}

	@Override
	public void insertEliteMobs() {
		int n = 0;
		try {
			Connection mConnection = setupConnection();
			Statement statement = mConnection.createStatement();
			for (String mob : EliteMobsCompat.getMobRewardData().keySet())
				if (getMobIdFromExtendedMobType(mob, MobPlugin.EliteMobs) == 0) {
					statement.executeUpdate("INSERT INTO mh_Mobs (PLUGIN_ID, MOBTYPE) VALUES (9,'" + mob + "')");
					n++;
				}
			if (n > 0)
				Bukkit.getConsoleSender().sendMessage(
						ChatColor.GOLD + "[MobHunting] " + ChatColor.RESET + n + " EliteMobs was inserted to mh_Mobs");
			statement.close();
			mConnection.commit();
			mConnection.close();
		} catch (SQLException | DataStoreException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void insertEliteMobs(String mob) {
		if (getMobIdFromExtendedMobType(mob, MobPlugin.EliteMobs) == 0)
			try {
				Connection mConnection = setupConnection();
				Statement statement = mConnection.createStatement();
				statement.executeUpdate("INSERT INTO mh_Mobs (PLUGIN_ID, MOBTYPE) VALUES (9,'" + mob + "')");
				Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "[MobHunting] " + ChatColor.RESET
						+ "EliteMob MobType " + mob + " was inserted to mh_Mobs");
				statement.close();
				mConnection.commit();
				mConnection.close();
			} catch (SQLException | DataStoreException e) {
				e.printStackTrace();
			}
	}

	@Override
	public void insertBossMobs() {
		int n = 0;
		try {
			Connection mConnection = setupConnection();
			Statement statement = mConnection.createStatement();
			for (String mob : BossCompat.getMobRewardData().keySet())
				if (getMobIdFromExtendedMobType(mob, MobPlugin.Boss) == 0) {
					statement.executeUpdate("INSERT INTO mh_Mobs (PLUGIN_ID, MOBTYPE) VALUES (10,'" + mob + "')");
					n++;
				}
			if (n > 0)
				Bukkit.getConsoleSender().sendMessage(
						ChatColor.GOLD + "[MobHunting] " + ChatColor.RESET + n + " Boss mobs was inserted to mh_Mobs");
			statement.close();
			mConnection.commit();
			mConnection.close();
		} catch (SQLException | DataStoreException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void insertBossMobs(String mob) {
		if (getMobIdFromExtendedMobType(mob, MobPlugin.Boss) == 0)
			try {
				Connection mConnection = setupConnection();
				Statement statement = mConnection.createStatement();
				statement.executeUpdate("INSERT INTO mh_Mobs (PLUGIN_ID, MOBTYPE) VALUES (10,'" + mob + "')");
				Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "[MobHunting] " + ChatColor.RESET
						+ "Boss MobType " + mob + " was inserted to mh_Mobs");
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
			int playerId = Core.getDataStoreManager().getPlayerId(offlinePlayer);
			openPreparedStatements(mConnection, PreparedConnectionType.GET_BOUNTIES);
			mGetBounties.setInt(1, playerId);
			mGetBounties.setInt(2, playerId);

			ResultSet set = mGetBounties.executeQuery();
			while (set.next()) {
				Bounty b = new Bounty(plugin);
				b.setBountyOwnerId(set.getInt(1));
				OfflinePlayer owner = Core.getDataStoreManager().getPlayerByPlayerId(set.getInt(1));
				if (owner == null)
					continue;
				b.setBountyOwner(owner);
				b.setMobtype(set.getString(2));
				b.setWantedPlayerId(set.getInt(3));
				OfflinePlayer wanted = Core.getDataStoreManager().getPlayerByPlayerId(set.getInt(3));
				if (wanted == null)
					continue;
				b.setWantedPlayer(wanted);
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
	 * @param OfflinePlayer :
	 * @throws DataStoreException
	 */
	@Override
	public Set<AchievementStore> loadAchievements(OfflinePlayer player) throws DataStoreException {
		HashSet<AchievementStore> achievements = new HashSet<AchievementStore>();
		try {
			Connection mConnection = setupConnection();
			openPreparedStatements(mConnection, PreparedConnectionType.LOAD_ARCHIEVEMENTS);
			int playerId = Core.getDataStoreManager().getPlayerId(player);
			if (playerId != 0) {
				mLoadAchievements.setInt(1, playerId);
				ResultSet set = mLoadAchievements.executeQuery();
				while (set.next()) {
					achievements
							.add(new AchievementStore(set.getString("ACHIEVEMENT"), player, set.getInt("PROGRESS")));
				}
				set.close();
			}
			mLoadAchievements.close();
			mConnection.close();
			return achievements;
		} catch (SQLException | UserNotFoundException e) {
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
					int playerId;
					try {
						playerId = Core.getDataStoreManager().getPlayerId(achievement.player);
					} catch (UserNotFoundException e) {
						String playerName = achievement.player != null ? achievement.player.getName() : "null";
						String playerUuid = achievement.player != null ? String.valueOf(achievement.player.getUniqueId())
								: "null";
						Bukkit.getConsoleSender().sendMessage(MobHunting.PREFIX_WARNING
								+ "Skipping Achievement save for missing player " + playerName + " (" + playerUuid + ")");
						continue;
					}
					mSaveAchievement.setInt(1, playerId);
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
				MobPlugin mp = MobPluginManager.valueOf(set.getInt("PLUGIN_ID"));
				switch (mp) {
				case Citizens:
					if (!CitizensCompat.isSupported() || !CitizensCompat.isEnabledInConfig())
						continue;
					break;
				case CustomMobs:
					if (!CustomMobsCompat.isSupported() || !CustomMobsCompat.isEnabledInConfig())
						continue;
					break;
				case MythicMobs:
					if (!MythicMobsCompat.isSupported() || !MythicMobsCompat.isEnabledInConfig())
						continue;
					break;
				case TARDISWeepingAngels:
					if (!TARDISWeepingAngelsCompat.isSupported() || !TARDISWeepingAngelsCompat.isEnabledInConfig())
						continue;
					break;
				case MysteriousHalloween:
					if (!MysteriousHalloweenCompat.isSupported() || !MysteriousHalloweenCompat.isEnabledInConfig())
						continue;
					break;
				case SmartGiants:
					if (!SmartGiantsCompat.isSupported() || !SmartGiantsCompat.isEnabledInConfig())
						continue;
					break;
				case Herobrine:
					if (!HerobrineCompat.isSupported() || !HerobrineCompat.isEnabledInConfig())
						continue;
					break;
				case EliteMobs:
					if (!EliteMobsCompat.isSupported() || !EliteMobsCompat.isEnabledInConfig())
						continue;
				case Boss:
					if (!BossCompat.isSupported() || !BossCompat.isEnabledInConfig())
						continue;
				case Minecraft:
					break;
				default:
					break;
				}
				mobs.add(new ExtendedMob(set.getInt("MOB_ID"), MobPluginManager.valueOf(set.getInt("PLUGIN_ID")),
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
			Bukkit.getConsoleSender().sendMessage(MobHunting.PREFIX + "Rename mh_Players to mh_PlayersV2.");
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
					Bukkit.getConsoleSender()
							.sendMessage(MobHunting.PREFIX + "Migrated " + n + " players into the new mh_Players.");
			} catch (SQLException e) {
				Bukkit.getConsoleSender()
						.sendMessage(MobHunting.PREFIX_ERROR + "Error while inserting data to new mh_Players");
				e.printStackTrace();
			}
		}

		if (plugin.getConfigManager().databaseType.equalsIgnoreCase("mysql")) {
			try {
				statement.executeUpdate("ALTER TABLE mh_Daily DROP FOREIGN KEY mh_Daily_ibfk_1;");
				statement.executeUpdate("ALTER TABLE mh_Weekly DROP FOREIGN KEY mh_Weekly_ibfk_1;");
				statement.executeUpdate("ALTER TABLE mh_Monthly DROP FOREIGN KEY mh_Monthly_ibfk_1;");
				statement.executeUpdate("ALTER TABLE mh_Yearly DROP FOREIGN KEY mh_Yearly_ibfk_1;");
				statement.executeUpdate("ALTER TABLE mh_AllTime DROP FOREIGN KEY mh_AllTime_ibfk_1;");

				Bukkit.getConsoleSender()
						.sendMessage(MobHunting.PREFIX + "Dropped foreign keys on mh_Players.PLAYER_ID");
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
				Bukkit.getConsoleSender().sendMessage(MobHunting.PREFIX + "Added constraints on mh_Players.PLAYER_ID");
			} catch (SQLException e) {
				Bukkit.getConsoleSender()
						.sendMessage(MobHunting.PREFIX_WARNING + "Moving constraints is already done.");
			}

			try {
				statement.executeUpdate("ALTER TABLE mh_Achievements DROP FOREIGN KEY mh_Achievements_ibfk_1;");
				Bukkit.getConsoleSender().sendMessage(MobHunting.PREFIX + "Dropped constraints on mh_Achievements");
			} catch (SQLException e) {
			}

			try {
				statement.executeUpdate(
						"ALTER TABLE mh_Achievements ADD CONSTRAINT mh_Achievements_Player_Id FOREIGN KEY(PLAYER_ID) REFERENCES mh_Players(PLAYER_ID) ON DELETE CASCADE;");

				Bukkit.getConsoleSender().sendMessage(MobHunting.PREFIX + "Added constraints on mh_Achievements");
			} catch (SQLException e) {
			}

			try {
				statement.executeUpdate("ALTER TABLE mh_Bounties DROP FOREIGN KEY mh_Bounties_ibfk_1;");
				statement.executeUpdate("ALTER TABLE mh_Bounties DROP FOREIGN KEY mh_Bounties_ibfk_2;");

				Bukkit.getConsoleSender().sendMessage(MobHunting.PREFIX + "Dropped constraints on mh_Bounties");
			} catch (SQLException e) {
			}

			try {
				statement.executeUpdate(
						"ALTER TABLE mh_Bounties ADD CONSTRAINT mh_Bounties_Player_Id_1 FOREIGN KEY(BOUNTYOWNER_ID) REFERENCES mh_Players(PLAYER_ID) ON DELETE CASCADE;");
				statement.executeUpdate(
						"ALTER TABLE mh_Bounties ADD CONSTRAINT mh_Bounties_Player_Id_2 FOREIGN KEY(WANTEDPLAYER_ID) REFERENCES mh_Players(PLAYER_ID) ON DELETE CASCADE;");

				Bukkit.getConsoleSender().sendMessage(MobHunting.PREFIX + "Added constraints on mh_Bounties");
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

	@Override
	public void databaseDeleteOldPlayers() {
		plugin.getMessages().debug("Deleting players not known on this server.");
		int n = 0;
		try {
			Connection mConnection = setupConnection();
			Statement statement = mConnection.createStatement();
			ResultSet rs = statement.executeQuery("SELECT UUID, PLAYER_ID FROM mh_Players");
			while (rs.next()) {
				String uuid = rs.getString("UUID");
				int playerId = rs.getInt("PLAYER_ID");
				if (!Bukkit.getOfflinePlayer(UUID.fromString(uuid)).hasPlayedBefore()) {
					plugin.getMessages().debug("Deleting ID:%s (%s) from MobHunting tables.", playerId, uuid);
					statement.executeUpdate("DELETE FROM mh_Players WHERE UUID='" + uuid + "'");
					statement.executeUpdate("DELETE FROM mh_Achievements WHERE ID='" + playerId + "'");
					statement.executeUpdate("DELETE FROM mh_Bounties WHERE ID='" + playerId + "'");
					statement.executeUpdate("DELETE FROM mh_Daily WHERE ID='" + playerId + "'");
					statement.executeUpdate("DELETE FROM mh_Weekly WHERE ID='" + playerId + "'");
					statement.executeUpdate("DELETE FROM mh_Monthly WHERE ID='" + playerId + "'");
					statement.executeUpdate("DELETE FROM mh_Yearly WHERE ID='" + playerId + "'");
					statement.executeUpdate("DELETE FROM mh_AllTime WHERE ID='" + playerId + "'");
					n++;
				}
			}
			rs.close();
			statement.close();
			mConnection.close();
			Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "[MobHunting] " + ChatColor.WHITE + n
					+ " players was deleted from the MobHunting database.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
