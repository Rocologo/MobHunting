package one.lindegaard.MobHunting.compatibility;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import net.theprogrammersworld.herobrine.Herobrine;
import net.theprogrammersworld.herobrine.nms.entity.EntityManager;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.mobs.MinecraftMob;

public class HerobrineCompat implements Listener {

	// https://www.theprogrammersworld.net/Herobrine/

	private static boolean supported = false;
	private static Plugin mPlugin;
	private static HashMap<String, Double> mMobRewardData = new HashMap<String, Double>();
	private static Herobrine api;
	private static EntityManager entityManager = new EntityManager();
	public static final String MH_HEROBRINEMOBS = "MH:Herobrine";

	public HerobrineCompat() {
		if (isDisabledInConfig()) {
			Bukkit.getConsoleSender()
					.sendMessage("[MobHunting] Compatibility with Herobrine is disabled in config.yml");
		} else {
			mPlugin = Bukkit.getPluginManager().getPlugin("Herobrine");

			Bukkit.getPluginManager().registerEvents(this, MobHunting.getInstance());

			Bukkit.getConsoleSender().sendMessage("[MobHunting] Enabling Compatibility with Herobrine ("
					+ mPlugin.getDescription().getVersion() + ")");

			api = (Herobrine) mPlugin;

			// loadInfernalMobsData();

			// MobHunting.getStoreManager().insertInfernalMobs();
			// Messages.injectMissingMobNamesToLangFiles();

			// Messages.debug("Herobrine AICore%s ", api.getAICore());
			// Messages.debug("Herobrine Deamon=%s ",
			// api.getConfigDB().UseNPC_Demon);
			// Messages.debug("Herobrine Guardian=%s ",
			// api.getConfigDB().UseNPC_Guardian);
			// Messages.debug("Herobrine Warrior=%s ",
			// api.getConfigDB().UseNPC_Warrior);
			//Messages.debug("Herobrine EntityManager=%s ", api.getEntityManager().isCustomMob(0));
			//Messages.debug("Herobrine NPCCore=%s ", api.getNPCCore());
			// Messages.debug("Herobrine Support=%s ", api.getSupport());
			//Messages.debug("Herobrine Herobrine = %s", Herobrine.herobrineNPC.getBukkitEntity());
			supported = true;
		}
	}

	// **************************************************************************
	// OTHER FUNCTIONS
	// **************************************************************************
	// public static Herobrine getHerobrine() {
	// return (Herobrine) mPlugin;
	// }

	public static boolean isSupported() {
		return supported;
	}

	public static boolean isHerobrineMob(Entity entity) {
		if (isSupported()) {
			return entity.hasMetadata(MH_HEROBRINEMOBS) ||
					entityManager.isCustomMob(entity.getEntityId())
					|| entity.getEntityId()== Herobrine.herobrineNPC.getBukkitEntity().getEntityId()
					|| api.getEntityManager().isCustomMob(entity.getEntityId());
		}
		return false;
	}

	public static HashMap<String, Double> getMobRewardData() {
		return mMobRewardData;
	}

	public static boolean isDisabledInConfig() {
		return MobHunting.getConfigManager().disableIntegrationHerobrine;
	}

	public static boolean isEnabledInConfig() {
		return !MobHunting.getConfigManager().disableIntegrationHerobrine;
	}

	// **************************************************************************
	// LOAD & SAVE
	// **************************************************************************
	public static void loadHerobrineMobsData() {
		// read from npc.yml
		Messages.debug("Loaded %s Herobrine Mobs", mMobRewardData.size());
	}

	// **************************************************************************
	// EVENTS
	// **************************************************************************
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	private void onHerobrineMobDeathEvent(EntityDeathEvent event) {
		Entity entity = event.getEntity();
		if (isHerobrineMob(entity)) {
			// if (api.findMobAbilities(entity.getUniqueId()) != null)
			entity.setMetadata(MH_HEROBRINEMOBS, new FixedMetadataValue(MobHunting.getInstance(), true));
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	private void onHerobrineMobSpawnEvent(EntitySpawnEvent event) {

		if (isSupported()) {
			Entity entity = event.getEntity();

			if (isHerobrineMob(entity)) {
				Messages.debug("A Herobrine Mob was spawned at %s,%s,%s in %s",
						event.getEntity().getLocation().getBlockX(), event.getEntity().getLocation().getBlockY(),
						event.getEntity().getLocation().getBlockZ(),
						event.getEntity().getLocation().getWorld().getName());
				Messages.debug("Herobrine MobType=%s",api.getEntityManager().getMobType(entity.getEntityId()));
				// String mobtype = MONSTER_NAME;
				// if (mMobRewardData != null &&
				// !mMobRewardData.containsKey(mobtype)) {
				// Messages.debug("New SmartGiants mob found=%s (%s)", mobtype,
				// mobtype.toString());
				// mMobRewardData.put(mobtype, new
				// RewardData(MobPlugin.SmartGiants, mobtype, mobtype,
				// "100:200",
				// "minecraft:give {player} iron_sword 1", "You got an Iron
				// sword.", 1, 1, 0.02));
				// saveSmartGiantsData(mobtype);
				// MobHunting.getStoreManager().insertSmartGiants(mobtype);
				/// // Update mob loaded into memory
				// MobHunting.getExtendedMobManager().updateExtendedMobs();
				// Messages.injectMissingMobNamesToLangFiles();
				// }
				event.getEntity().setMetadata(MH_HEROBRINEMOBS, new FixedMetadataValue(mPlugin, true));
			} 
		}
	}

	public static int getProgressAchievementLevel1(String mobtype) {
		MinecraftMob mob = MinecraftMob.valueOf(mobtype);
		if (mob != null)
			return mob.getProgressAchievementLevel1();
		else
			return 100;
	}

}
