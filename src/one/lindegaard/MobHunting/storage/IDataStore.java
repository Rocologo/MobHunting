package one.lindegaard.MobHunting.storage;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import org.bukkit.OfflinePlayer;

import one.lindegaard.MobHunting.StatType;
import one.lindegaard.MobHunting.bounty.Bounty;
import one.lindegaard.MobHunting.mobs.ExtendedMob;

public interface IDataStore {
	/**
	 * Initialize - opening a connection to the Database and initialize the
	 * connection.
	 * 
	 * @throws DataStoreException
	 */
	void initialize() throws DataStoreException;

	/**
	 * Closing all connections to the Database
	 * 
	 * @throws DataStoreException
	 */
	void shutdown() throws DataStoreException;

	/**
	 * loadPlayerStats - Loading <count> records of Player Stats from the
	 * Database
	 * 
	 * @param type
	 * @param period
	 * @param count
	 * @return List<StatStore>
	 * @throws DataStoreException
	 */
	List<StatStore> loadPlayerStats(StatType type, TimePeriod period, int count) throws DataStoreException;

	/**
	 * Save a Set of Player Stats to the Database
	 * 
	 * @param stats
	 * @throws DataStoreException
	 */
	void savePlayerStats(Set<StatStore> stats) throws DataStoreException;

	/**
	 * Load a Players Archievements
	 * 
	 * @param player
	 * @return
	 * @throws DataStoreException
	 */
	Set<AchievementStore> loadAchievements(OfflinePlayer player) throws DataStoreException;

	/**
	 * Save a Set of players archievements
	 * 
	 * @param achievements
	 * @throws DataStoreException
	 */
	void saveAchievements(Set<AchievementStore> achievements) throws DataStoreException;

	/**
	 * Get the player by his name from the Database. ings @param name
	 * 
	 * @return
	 * @throws DataStoreException
	 */
	OfflinePlayer getPlayerByName(String name) throws DataStoreException;

	/**
	 * Get the players Settings from the Database
	 * 
	 * @param player
	 * @return
	 * @throws DataStoreException
	 * @throws SQLException
	 */
	PlayerSettings loadPlayerSettings(OfflinePlayer player) throws DataStoreException, SQLException;

	/**
	 * Update the players Settings in the Database
	 * 
	 * @param playerDataSet
	 * @throws DataStoreException
	 */
	void savePlayerSettings(Set<PlayerSettings> ps) throws DataStoreException;

	/**
	 * Insert all PlayerData for one player into the Database
	 * 
	 * @param ps
	 * @throws DataStoreException
	 */
	void insertPlayerSettings(PlayerSettings ps) throws DataStoreException;

	/**
	 * Load all bounties for the given player directly from the Sql Database
	 * @param mPlayer
	 * @return Set<Bounty>
	 * @throws DataStoreException
	 */
	Set<Bounty> loadBounties(OfflinePlayer mPlayer) throws DataStoreException;

	/**
	 * Save the Bounty Sets direktly to the Database
	 * @param bountyDataSet
	 * @throws DataStoreException
	 */
	void saveBounties(Set<Bounty> bountyDataSet) throws DataStoreException;

	/**
	 * Fixes error in the database
	 * 
	 * @throws SQLException
	 * @throws DataStoreException 
	 */
	void databaseFixLeaderboard() throws DataStoreException;
	
	/**
	 * Convert all tables to use UTF-8 character set.
	 * @param database_name
	 * @throws DataStoreException
	 */
	void databaseConvertToUtf8(String database_name) throws DataStoreException;

	OfflinePlayer getPlayerByPlayerId(int playerId) throws DataStoreException;

	/**
	 * Get the player ID directly from the database
	 * @param player
	 * @return
	 * @throws DataStoreException
	 * @throws UserNotFoundException
	 */
	int getPlayerId(OfflinePlayer player) throws DataStoreException;

	
	Set<ExtendedMob> loadMobs() throws DataStoreException;

	void insertMissingVanillaMobs();
	
	void insertMissingMythicMobs();

	void insertMissingCitizensMobs();

	void insertTARDISWeepingAngelsMobs();

	void insertMysteriousHalloweenMobs();
	
	void insertSmartGiants();

	void insertCustomMobs();
	
	void insertInfernalMobs();

	void insertMobs(Set<ExtendedMob> mobs) throws DataStoreException;

	void updateMobs(Set<ExtendedMob> mobs) throws DataStoreException;

	void insertMissingMythicMobs(String mob);

	void insertCitizensMobs(String mob);

	void insertTARDISWeepingAngelsMobs(String mob);

	void insertMysteriousHalloweenMobs(String mob);
	
	void insertCustomMobs(String mob);
	
	void insertSmartGiants(String mob);

}
