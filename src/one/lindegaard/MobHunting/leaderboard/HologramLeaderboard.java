package one.lindegaard.MobHunting.leaderboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.util.Vector;

import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.sainttx.holograms.api.Hologram;
import com.sainttx.holograms.api.line.HologramLine;
import com.sainttx.holograms.api.line.TextualHologramLine;

import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.StatType;
import one.lindegaard.MobHunting.compatibility.HologramsCompat;
import one.lindegaard.MobHunting.compatibility.HologramsHelper;
import one.lindegaard.MobHunting.compatibility.HolographicDisplaysCompat;
import one.lindegaard.MobHunting.compatibility.HolographicDisplaysHelper;
import one.lindegaard.MobHunting.compatibility.PlaceholderAPICompat;
import one.lindegaard.MobHunting.storage.IDataCallback;
import one.lindegaard.MobHunting.storage.StatStore;
import one.lindegaard.MobHunting.storage.TimePeriod;

public class HologramLeaderboard implements IDataCallback<List<StatStore>> {

	private MobHunting plugin;

	private String mHologramName;
	private Location mLocation;
	private int mHeight;
	private TimePeriod[] mPeriod = new TimePeriod[] {};
	private int mPeriodIndex = 0;
	private StatType[] mType = new StatType[] {};
	private int mTypeIndex = 0;
	private String mFormat_title;
	private String mRow_format_integer;
	private String mRow_format_money;

	private List<StatStore> mData;

	private final String FORMAT_TITLE = ChatColor.GOLD + "" + ChatColor.BOLD + "[StatType] - [Period]";
	private final String FORMAT_MONEY = ChatColor.WHITE + "%2d " + ChatColor.GREEN + "%-15s " + ChatColor.WHITE
			+ ": %12s";
	private final String FORMAT_INTEGER = ChatColor.WHITE + "%2d " + ChatColor.GREEN + "%-15s " + ChatColor.WHITE
			+ ": %6d";

	public HologramLeaderboard(MobHunting plugin, String hologramName, StatType[] stat, TimePeriod[] period, int height,
			Location location) {
		this.plugin = plugin;
		mHologramName = hologramName;
		mLocation = location;
		mHeight = height;
		mType = stat;
		mPeriod = period;
		mPeriodIndex = 0;
		mTypeIndex = 0;
		mFormat_title = FORMAT_TITLE;
		mRow_format_money = FORMAT_MONEY;
		mRow_format_integer = FORMAT_INTEGER;
	}

	public HologramLeaderboard(MobHunting plugin) {
		this.plugin = plugin;
		mFormat_title = FORMAT_TITLE;
		mRow_format_money = FORMAT_MONEY;
		mRow_format_integer = FORMAT_INTEGER;
	}

	public HologramLeaderboard(MobHunting plugin, String hologramName, StatType statType, TimePeriod period, int height,
			Location location) {
		this.plugin = plugin;
		mHologramName = hologramName;
		mLocation = location;
		mHeight = height;
		if (mType == null || mType.length == 0)
			mType = new StatType[] { statType };
		else
			mType[mType.length] = statType;
		if (mPeriod == null || mPeriod.length == 0)
			mPeriod = new TimePeriod[] { period };
		else
			mPeriod[mPeriod.length] = period;
		mPeriodIndex++;
		mTypeIndex++;
		mFormat_title = FORMAT_TITLE;
		mRow_format_money = FORMAT_MONEY;
		mRow_format_integer = FORMAT_INTEGER;
	}

	public List<StatStore> getCurrentStats() {
		if (mData == null)
			return Collections.emptyList();
		return mData;
	}

	public void update() {
		++mTypeIndex;
		if (mType != null) {
			if (mTypeIndex >= mType.length) {
				mTypeIndex = 0;
				++mPeriodIndex;
				if (mPeriodIndex >= mPeriod.length)
					mPeriodIndex = 0;
			}
			MobHunting.getDataStoreManager().requestStats(getStatType(), getPeriod(), mHeight * 2, this);
		} else {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[MobHunting][WARNING] The leaderboard at "
					+ mLocation.toString() + " has no StatType");
		}
	}

	public void refresh() {
		if (HologramsCompat.isSupported()) {
			Hologram hologram = HologramsCompat.getHologramManager().getHologram(mHologramName);
			if (hologram.getLines().size() == 0)
				HologramsHelper.editTextLine(hologram,
						mFormat_title.replace("[StatType]", mType[mTypeIndex].longTranslateName()).replace("[Period]",
								mPeriod[mPeriodIndex].translateNameFriendly()),
						0);
			for (int n = 0; n < mHeight && n < mData.size(); n++) {
				if (getStatType().getDBColumn().endsWith("_cash")) {
					HologramLine line = hologram.getLine(n + 1);
					if (line != null)
						((TextualHologramLine) line)
								.setText(String.format(PlaceholderAPICompat.setPlaceholders(null, mRow_format_money), n + 1, mData.get(n).getPlayer().getName(),
										plugin.getRewardManager().format(mData.get(n).getCash())));
					else
						HologramsHelper.editTextLine(hologram,
								String.format(mRow_format_money, n + 1, mData.get(n).getPlayer().getName(),
										plugin.getRewardManager().format(mData.get(n).getCash())),
								n + 1);
				} else {
					HologramLine line = hologram.getLine(n + 1);
					if (line != null)
						((TextualHologramLine) line).setText(String.format(mRow_format_integer, n + 1,
								mData.get(n).getPlayer().getName(), mData.get(n).getAmount()));
					else
						HologramsHelper.editTextLine(hologram, String.format(mRow_format_integer, n + 1,
								mData.get(n).getPlayer().getName(), mData.get(n).getAmount()), n + 1);
				}
				hologram.setDirty(true);
			}
		} else if (HolographicDisplaysCompat.isSupported()) {
			for (com.gmail.filoghost.holographicdisplays.api.Hologram hologram : HologramsAPI.getHolograms(plugin)) {
				if (hologram.getLocation().equals(MobHunting.getLeaderboardManager().getHologramManager().getHolograms()
						.get(mHologramName).getLocation())) {
					hologram.clearLines();
					if (hologram.getHeight() == 0)
						hologram.insertTextLine(0,
								mFormat_title.replace("[StatType]", mType[mTypeIndex].longTranslateName())
										.replace("[Period]", mPeriod[mPeriodIndex].translateNameFriendly()));
					for (int n = 0; n < mHeight && n < mData.size(); n++) {
						if (getStatType().getDBColumn().endsWith("_cash"))
							HolographicDisplaysHelper.editTextLine(hologram,
									String.format(mRow_format_money, n + 1, mData.get(n).getPlayer().getName(),
											plugin.getRewardManager().format(mData.get(n).getCash())),
									n + 1);
						else
							HolographicDisplaysHelper.editTextLine(hologram, String.format(mRow_format_integer, n + 1,
									mData.get(n).getPlayer().getName(), mData.get(n).getAmount()), n + 1);

					}
					break;
				} else {

				}
			}
		}

	}

	public String getHologramName() {
		return mHologramName;
	}

	public void setType(StatType[] type) {
		mType = type;
		mTypeIndex = 0;
	}

	public void setPeriod(TimePeriod[] period) {
		mPeriod = period;
		mPeriodIndex = 0;
	}

	public StatType getStatType() {
		return mType[mTypeIndex];
	}

	public StatType[] getTypes() {
		return mType;
	}

	public TimePeriod getPeriod() {
		return mPeriod[mPeriodIndex];
	}

	public TimePeriod[] getPeriods() {
		return mPeriod;
	}

	public Location getLocation() {
		return mLocation.clone();
	}

	public int getHeight() {
		return mHeight;
	}

	@Override
	public void onCompleted(List<StatStore> data) {
		ArrayList<StatStore> altData = new ArrayList<StatStore>(data.size());
		for (StatStore stat : data) {
			if (stat.getPlayer() != null && stat.getAmount() != 0) {
				altData.add(stat);
			}
		}
		mData = altData;
		refresh();
	}

	@Override
	public void onError(Throwable error) {
		error.printStackTrace();
	}

	// ****************************************************************************************************
	// LOAD & SAVE HOLOGRAM LEADERBOARD
	// ****************************************************************************************************

	public void write(ConfigurationSection section) {
		section.set("hologramName", mHologramName);
		section.set("world", mLocation.getWorld().getUID().toString());
		section.set("position", mLocation.toVector());

		ArrayList<String> periods = new ArrayList<String>(mPeriod.length);
		for (TimePeriod period : mPeriod)
			periods.add(period.name());

		section.set("periods", periods);

		ArrayList<String> stats = new ArrayList<String>(mPeriod.length);
		for (StatType type : mType)
			stats.add(type.getDBColumn());

		section.set("stats", stats);
		section.set("height", mHeight);
		section.set("format_title", mFormat_title);
		section.set("format_integer", mRow_format_integer);
		section.set("format_money", mRow_format_money);
	}

	public void read(ConfigurationSection section) throws InvalidConfigurationException {
		mHologramName = section.getString("hologramName");
		World world = Bukkit.getWorld(UUID.fromString(section.getString("world")));

		if (world == null)
			throw new InvalidConfigurationException("Unknown world:" + section.getString("world"));

		Vector pos = section.getVector("position");

		List<String> periods = section.getStringList("periods");
		List<String> stats = section.getStringList("stats");
		mHeight = section.getInt("height");

		if (periods == null)
			throw new InvalidConfigurationException(
					"Error on Hologram Leaderboard " + section.getName() + ":Error in time period list");
		if (stats == null)
			throw new InvalidConfigurationException(
					"Error on Hologram Leaderboard " + section.getName() + ":Error in stat type list");
		if (pos == null)
			throw new InvalidConfigurationException(
					"Error on Hologram Leaderboard " + section.getName() + ":Error in position");

		if (mHeight < 1)
			throw new InvalidConfigurationException(
					"Error on Hologram Leaderboard " + section.getName() + ":Invalid height");

		mPeriod = new TimePeriod[periods.size()];
		for (int i = 0; i < periods.size(); ++i) {
			mPeriod[i] = TimePeriod.valueOf(periods.get(i));
			if (mPeriod[i] == null)
				throw new InvalidConfigurationException("Error on Hologram Leaderboard " + section.getName()
						+ ":Invalid time period " + periods.get(i));
		}

		mPeriodIndex = 0;
		mTypeIndex = 0;
		mLocation = pos.toLocation(world);
		mType = new StatType[stats.size()];
		for (int i = 0; i < stats.size(); ++i) {
			mType[i] = StatType.fromColumnName(stats.get(i));
			if (mType[i] == null)
				throw new InvalidConfigurationException(
						"Error on Hologram Leaderboard " + section.getName() + ":Invalid stat type " + stats.get(i));
		}
		mFormat_title = section.getString("format_title", FORMAT_TITLE);
		mRow_format_integer = section.getString("format_integer", FORMAT_INTEGER);
		mRow_format_money = section.getString("format_money", FORMAT_MONEY);
	}

}
