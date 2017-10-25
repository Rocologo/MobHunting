package one.lindegaard.MobHunting.grinding;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.mobs.ExtendedMob;
import one.lindegaard.MobHunting.mobs.MinecraftMob;
import one.lindegaard.MobHunting.util.Misc;

public class GrindingManager implements Listener {

	private static HashMap<UUID, LinkedList<Area>> mKnownGrindingAreas = new HashMap<>();
	private static HashMap<UUID, LinkedList<Area>> mWhitelistedAreas = new HashMap<>();
	private static HashMap<Integer, GrindingInformation> killed_mobs = new HashMap<>();

	public GrindingManager(MobHunting instance) {
		if (!loadWhitelist(instance))
			throw new RuntimeException();
		if (!loadBlacklist(instance))
			throw new RuntimeException();
		Bukkit.getPluginManager().registerEvents(this, instance);
	}

	public void saveData() {
		saveWhitelist();
		saveBlacklist();
	}

	/**
	 * Register a kill for later inspection. Farming can be caught because most
	 * farms kills the mobs by letting them fall down from a high place.
	 * 
	 * @param killed
	 */
	public void registerDeath(LivingEntity killed) {
		GrindingInformation grindingInformation = new GrindingInformation(killed);
		if (!isGrindingArea(killed.getLocation()) && !isWhitelisted(killed.getLocation())) {
			killed_mobs.put(killed.getEntityId(), grindingInformation);
		}
	}

	/**
	 * Test if the killed mob is killed in a NetherGoldXPFarm
	 * 
	 * @param killed
	 * @return true if the location is detected as a NetherGoldXPFarm, or if the
	 *         area is detected as a Grinding Area
	 */
	public boolean isNetherGoldXPFarm(LivingEntity killed) {
		ExtendedMob mob = MobHunting.getExtendedMobManager().getExtendedMobFromEntity(killed);
		int n = 0;
		long now = System.currentTimeMillis();
		final long seconds = MobHunting.getConfigManager().secondsToSearchForGrinding;
		final double killRadius = MobHunting.getConfigManager().rangeToSearchForGrinding;
		final int numberOfDeaths = MobHunting.getConfigManager().numberOfDeathsWhenSearchingForGringding;
		if (MinecraftMob.getMinecraftMobType(killed) == MinecraftMob.ZombiePigman) {
			if (killed.getLastDamageCause().getCause() == DamageCause.FALL) {
				Area detectedGrindingArea = getGrindingArea(killed.getLocation());
				if (detectedGrindingArea == null) {
					Iterator<Entry<Integer, GrindingInformation>> itr = killed_mobs.entrySet().iterator();
					while (itr.hasNext()) {
						GrindingInformation gi = itr.next().getValue();
						if (killed.getType() == EntityType.PIG_ZOMBIE && gi.getKilled().getType() == killed.getType()
								&& gi.getKilled().getEntityId() != killed.getEntityId()) {
							if (n < numberOfDeaths) {
								if (now < gi.getTimeOfDeath() + seconds * 1000L) {
									if (killed.getLocation().distance(gi.getKilled().getLocation()) < killRadius) {
										n++;
										// Messages.debug("This was not a Nether
										// Gold XP Farm (%s sec.)",
										// new Date(now -
										// gi.getTimeOfDeath()).getSeconds());
									}
								} else {
									// Messages.debug("Removing old kill.
									// (Killed %s seconds ago).",
									// Math.round((now - gi.getTimeOfDeath()) /
									// 1000L));
									itr.remove();
								}
							} else {
								Area area = new Area(killed.getLocation(), killRadius, numberOfDeaths);
								Messages.debug("Nether Gold XP Farm detected at (%s,%s,%s,%s)",
										area.getCenter().getWorld().getName(), area.getCenter().getBlockX(),
										area.getCenter().getBlockY(), area.getCenter().getBlockZ());
								registerKnownGrindingSpot(area);
								return true;
							}
						}
					}
				} else {
					Messages.debug("This is a known grinding area: (%s,%s,%s,%s)",
							detectedGrindingArea.getCenter().getWorld().getName(),
							detectedGrindingArea.getCenter().getBlockX(), detectedGrindingArea.getCenter().getBlockY(),
							detectedGrindingArea.getCenter().getBlockZ());
					return true;
				}
			}
		}
		Messages.debug("Farm detection: This was not a Nether Gold XP Farm (%s of %s mobs with last %s sec.)", n,
				numberOfDeaths, seconds);
		return false;
	}

	public boolean isOtherFarm(LivingEntity killed) {
		ExtendedMob mob = MobHunting.getExtendedMobManager().getExtendedMobFromEntity(killed);
		int n = 0;
		long now = System.currentTimeMillis();
		final long seconds = MobHunting.getConfigManager().secondsToSearchForGrinding;
		final double killRadius = MobHunting.getConfigManager().rangeToSearchForGrinding;
		final int numberOfDeaths = MobHunting.getConfigManager().numberOfDeathsWhenSearchingForGringding;
		if (MinecraftMob.getMinecraftMobType(killed) == MinecraftMob.ZombiePigman) {
			if (killed.getLastDamageCause().getCause() == DamageCause.FALL) {
				Area detectedGrindingArea = getGrindingArea(killed.getLocation());
				if (detectedGrindingArea == null) {
					Iterator<Entry<Integer, GrindingInformation>> itr = killed_mobs.entrySet().iterator();
					while (itr.hasNext()) {
						GrindingInformation gi = itr.next().getValue();
						if (gi.getKilled().getEntityId() != killed.getEntityId()) {
							if (n < numberOfDeaths) {
								if (now < gi.getTimeOfDeath() + seconds * 1000L) {
									if (killed.getLocation().distance(gi.getKilled().getLocation()) < killRadius) {
										n++;
										// Messages.debug("This was not a Nether
										// Gold XP Farm (%s sec.)",
										// new Date(now -
										// gi.getTimeOfDeath()).getSeconds());
									}
								} else {
									// Messages.debug("Removing old kill.
									// (Killed %s seconds ago).",
									// Math.round((now - gi.getTimeOfDeath()) /
									// 1000L));
									itr.remove();
								}
							} else {
								Area area = new Area(killed.getLocation(), killRadius, numberOfDeaths);
								Messages.debug("Other Farm detected at (%s,%s,%s,%s)",
										area.getCenter().getWorld().getName(), area.getCenter().getBlockX(),
										area.getCenter().getBlockY(), area.getCenter().getBlockZ());
								registerKnownGrindingSpot(area);
								return true;
							}
						}
					}
				} else {
					Messages.debug("This is a known grinding area: (%s,%s,%s,%s)",
							detectedGrindingArea.getCenter().getWorld().getName(),
							detectedGrindingArea.getCenter().getBlockX(), detectedGrindingArea.getCenter().getBlockY(),
							detectedGrindingArea.getCenter().getBlockZ());
					return true;
				}
			}
		}
		Messages.debug("Farm detection: This was not a Farm (%s of %s mobs with last %s sec.)", n, numberOfDeaths,
				seconds);
		return false;
	}

	// ****************************************************************
	// Events
	// ****************************************************************
	@EventHandler
	private void onWorldLoad(WorldLoadEvent event) {
		List<Area> areas = getWhitelistedAreas(event.getWorld());
		if (areas != null) {
			for (Area area : areas)
				area.getCenter().setWorld(event.getWorld());
		}
	}

	@EventHandler
	private void onWorldUnLoad(WorldUnloadEvent event) {
		List<Area> areas = getWhitelistedAreas(event.getWorld());
		if (areas != null) {
			for (Area area : areas)
				area.getCenter().setWorld(null);
		}
	}

	// ****************************************************************
	// Blacklist
	// ****************************************************************
	public LinkedList<Area> getKnownGrindingSpots(Location loc) {
		if (mKnownGrindingAreas.containsKey(loc.getWorld().getUID()))
			return mKnownGrindingAreas.get(loc.getWorld().getUID());
		else
			return new LinkedList<Area>();
	}

	public void addKnownGrindingSpot(Area area) {
		LinkedList<Area> list = getKnownGrindingSpots(area.getCenter());
		list.add(area);
		mKnownGrindingAreas.put(area.getCenter().getWorld().getUID(), list);
	}

	private boolean saveBlacklist() {
		YamlConfiguration blacklist = new YamlConfiguration();
		File file = new File(MobHunting.getInstance().getDataFolder(), "blacklist.yml");

		for (Entry<UUID, LinkedList<Area>> entry : mKnownGrindingAreas.entrySet()) {
			ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
			for (Area area : entry.getValue()) {
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("Center", Misc.toMap(area.getCenter()));
				map.put("Radius", area.getRange());
				map.put("Counter", area.getCounter());
				list.add(map);
			}
			blacklist.set(entry.getKey().toString(), list);
		}

		try {
			blacklist.save(file);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	private boolean loadBlacklist(MobHunting instance) {
		YamlConfiguration blacklist = new YamlConfiguration();
		File file = new File(instance.getDataFolder(), "blacklist.yml");

		if (!file.exists())
			return true;

		try {
			blacklist.load(file);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
			return false;
		}

		mKnownGrindingAreas.clear();

		for (String worldId : blacklist.getKeys(false)) {
			List<Map<String, Object>> list = (List<Map<String, Object>>) blacklist.getList(worldId);
			LinkedList<Area> areas = new LinkedList<Area>();

			if (list == null)
				continue;

			for (Map<String, Object> map : list) {
				Area area = new Area(Misc.fromMap((Map<String, Object>) map.get("Center")), (Double) map.get("Radius"),
						(int) map.getOrDefault("Counter", 0));
				areas.add(area);
			}
			mKnownGrindingAreas.put(UUID.fromString(worldId), areas);
		}

		return true;
	}

	public void registerKnownGrindingSpot(Area newArea) {
		for (Area area : getKnownGrindingSpots(newArea.getCenter())) {
			if (newArea.getCenter().getWorld().equals(area.getCenter().getWorld())) {
				double dist = newArea.getCenter().distance(area.getCenter());

				double remaining = dist;
				remaining -= area.getRange();
				remaining -= newArea.getRange();

				if (remaining < 0) {
					if (dist > area.getRange())
						area.setRange(dist);

					area.setCounter(newArea.getCounter() + 1);

					return;
				}
			}
		}

		addKnownGrindingSpot(newArea);
	}

	public Area getGrindingArea(Location location) {
		LinkedList<Area> areas = getKnownGrindingSpots(location);
		for (Area area : areas) {
			if (area.getCenter().getWorld().equals(location.getWorld())) {
				if (area.getCenter().distance(location) < area.getRange()) {
					Messages.debug("Found a blacklisted grinding area = %s, range=%s", area.getCenter(),
							area.getRange());
					return area;
				}
			}
		}

		return null;
	}

	public boolean isGrindingArea(Location location) {
		if (location != null) {
			LinkedList<Area> areas = getKnownGrindingSpots(location);
			for (Area area : areas) {
				if (area.getCenter().getWorld().equals(location.getWorld())) {
					if (area.getCenter().distance(location) < area.getRange()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public void clearGrindingArea(Location location) {
		Iterator<Area> it = getKnownGrindingSpots(location).iterator();
		while (it.hasNext()) {
			Area area = it.next();

			if (area.getCenter().getWorld().equals(location.getWorld())) {
				if (area.getCenter().distance(location) < area.getRange())
					it.remove();
			}
		}
	}

	public void blacklistArea(Area newArea) {
		LinkedList<Area> areas = mKnownGrindingAreas.get(newArea.getCenter().getWorld().getUID());
		if (areas == null) {
			areas = new LinkedList<Area>();
			mKnownGrindingAreas.put(newArea.getCenter().getWorld().getUID(), areas);
		}

		for (Area area : areas) {
			if (newArea.getCenter().getWorld().equals(area.getCenter().getWorld())) {
				double dist = newArea.getCenter().distance(area.getCenter());

				double remaining = dist;
				remaining -= area.getRange();
				remaining -= newArea.getRange();

				if (remaining < 0) {
					if (dist > area.getRange())
						area.setRange(dist);

					area.setCounter(newArea.getCounter() + 1);

					return;
				}
			}
		}
		areas.add(newArea);
		mKnownGrindingAreas.put(newArea.getCenter().getWorld().getUID(), areas);
		saveBlacklist();
	}

	public void unBlacklistArea(Location location) {
		LinkedList<Area> areas = mKnownGrindingAreas.get(location.getWorld().getUID());

		if (areas == null)
			return;

		Iterator<Area> it = areas.iterator();
		while (it.hasNext()) {
			Area area = it.next();

			if (area.getCenter().getWorld().equals(location.getWorld())) {
				if (area.getCenter().distance(location) < area.getRange())
					it.remove();
			}
		}
		if (areas.isEmpty())
			mKnownGrindingAreas.remove(location.getWorld().getUID());
		else
			mKnownGrindingAreas.put(location.getWorld().getUID(), areas);
		saveBlacklist();
	}

	// ****************************************************************
	// Whitelisted Areas
	// ****************************************************************

	public LinkedList<Area> getWhitelistedAreas(World world) {
		if (mWhitelistedAreas.containsKey(world.getUID()))
			return mWhitelistedAreas.get(world.getUID());
		else
			return new LinkedList<Area>();
	}

	private boolean saveWhitelist() {
		YamlConfiguration whitelist = new YamlConfiguration();
		File file = new File(MobHunting.getInstance().getDataFolder(), "whitelist.yml");

		for (Entry<UUID, LinkedList<Area>> entry : mWhitelistedAreas.entrySet()) {
			ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
			for (Area area : entry.getValue()) {
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("Center", Misc.toMap(area.getCenter()));
				map.put("Radius", area.getRange());
				map.put("Counter", area.getCounter());
				list.add(map);
			}
			whitelist.set(entry.getKey().toString(), list);
		}

		try {
			whitelist.save(file);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	private boolean loadWhitelist(MobHunting instance) {
		YamlConfiguration whitelist = new YamlConfiguration();
		File file = new File(instance.getDataFolder(), "whitelist.yml");

		if (!file.exists())
			return true;

		try {
			whitelist.load(file);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
			return false;
		}

		mWhitelistedAreas.clear();

		for (String worldId : whitelist.getKeys(false)) {
			UUID world = UUID.fromString(worldId);
			List<Map<String, Object>> list = (List<Map<String, Object>>) whitelist.getList(worldId);
			LinkedList<Area> areas = new LinkedList<Area>();

			if (list == null)
				continue;

			for (Map<String, Object> map : list) {
				Area area = new Area(Misc.fromMap((Map<String, Object>) map.get("Center")), (Double) map.get("Radius"),
						(int) map.getOrDefault("Counter", 0));
				areas.add(area);
			}

			mWhitelistedAreas.put(world, areas);
		}

		return true;
	}

	public boolean isWhitelisted(Location location) {
		LinkedList<Area> areas = mWhitelistedAreas.get(location.getWorld().getUID());
		if (areas == null)
			return false;
		for (Area area : areas) {
			if (area.getCenter().distance(location) < area.getRange()) {
				return true;
			}
		}
		return false;
	}

	public Area getWhitelistArea(Location location) {
		LinkedList<Area> areas = getWhitelistedAreas(location.getWorld());
		for (Area area : areas) {
			if (area.getCenter().getWorld().equals(location.getWorld())) {
				if (area.getCenter().distance(location) < area.getRange()) {
					Messages.debug("Found a whitelisted area = %s, range=%s", area.getCenter(), area.getRange());
					return area;
				}
			}
		}
		return null;
	}

	public void whitelistArea(Area newArea) {
		LinkedList<Area> areas = mWhitelistedAreas.get(newArea.getCenter().getWorld().getUID());
		if (areas == null) {
			areas = new LinkedList<Area>();
			mWhitelistedAreas.put(newArea.getCenter().getWorld().getUID(), areas);
		}

		for (Area area : areas) {
			if (newArea.getCenter().getWorld().equals(area.getCenter().getWorld())) {
				double dist = newArea.getCenter().distance(area.getCenter());

				double remaining = dist;
				remaining -= area.getRange();
				remaining -= newArea.getRange();

				if (remaining < 0) {
					if (dist > area.getRange())
						area.setRange(dist);

					area.setCounter(newArea.getCounter() + 1);

					return;
				}
			}
		}
		areas.add(newArea);
		mWhitelistedAreas.put(newArea.getCenter().getWorld().getUID(), areas);
		saveWhitelist();
	}

	public void unWhitelistArea(Location location) {
		LinkedList<Area> areas = mWhitelistedAreas.get(location.getWorld().getUID());

		if (areas == null)
			return;

		Iterator<Area> it = areas.iterator();
		while (it.hasNext()) {
			Area area = it.next();

			if (area.getCenter().getWorld().equals(location.getWorld())) {
				if (area.getCenter().distance(location) < area.getRange())
					it.remove();
			}
		}
		if (areas.isEmpty())
			mWhitelistedAreas.remove(location.getWorld().getUID());
		else
			mWhitelistedAreas.put(location.getWorld().getUID(), areas);
		saveWhitelist();
	}

	/**
	 * Check if Grindind detection is disabled in world
	 * 
	 * @param world
	 * @return true if Grinding is disabled.
	 */
	public boolean isGrindingDisabledInWorld(World world) {
		if (world != null)
			for (String worldName : MobHunting.getConfigManager().disableGrindingDetectionInWorlds) {
				if (world.getName().equalsIgnoreCase(worldName))
					return true;
			}
		return false;
	}
}
