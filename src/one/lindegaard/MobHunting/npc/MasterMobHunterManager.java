package one.lindegaard.MobHunting.npc;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.speech.SpeechContext;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.compatibility.CitizensCompat;
import one.lindegaard.MobHunting.util.Misc;

public class MasterMobHunterManager implements Listener {

	private static HashMap<Integer, MasterMobHunter> mMasterMobHunter = new HashMap<Integer, MasterMobHunter>();

	private File file = new File(MobHunting.getInstance().getDataFolder(), "citizens-MasterMobHunter.yml");
	private YamlConfiguration config = new YamlConfiguration();

	private BukkitTask mUpdater = null;

	public MasterMobHunterManager() {
	}

	public static HashMap<Integer, MasterMobHunter> getMasterMobHunterManager() {
		return mMasterMobHunter;
	}

	public void initialize() {
		if (CitizensCompat.isSupported()) {
			loadData();
			Bukkit.getPluginManager().registerEvents(new MasterMobHunterTrait(), MobHunting.getInstance());
			Bukkit.getPluginManager().registerEvents(new MasterMobHunterManager(), MobHunting.getInstance());
			Bukkit.getPluginManager().registerEvents(new MasterMobHunterSign(MobHunting.getInstance()),
					MobHunting.getInstance());
			mUpdater = Bukkit.getScheduler().runTaskTimer(MobHunting.getInstance(), new Updater(), 1L,
					MobHunting.getConfigManager().masterMobHuntercheckEvery * 20);

		}
	}

	public void forceUpdate() {
		mUpdater = Bukkit.getScheduler().runTaskAsynchronously(MobHunting.getInstance(), new Updater());
	}

	private class Updater implements Runnable {
		@Override
		public void run() {
			if (CitizensCompat.isSupported()) {
				int n = 0;
				for (Iterator<NPC> npcList = CitizensAPI.getNPCRegistry().iterator(); npcList.hasNext();) {
					NPC npc = npcList.next();
					if (isMasterMobHunter(npc.getEntity())) {
						update(npc);
						n++;
					}
				}
				if (n > 0)
					Messages.debug("Refreshed %s MasterMobHunters", n);
			} else {
				Messages.debug("MasterMobHunterManager: Citizens is disabled.");
			}
		}
	}

	public void update(NPC npc) {
		if (hasMasterMobHunterData(npc)) {
			MasterMobHunter mmh = new MasterMobHunter(npc);
			if (mmh != null) {
				mmh.update();
				mMasterMobHunter.put(npc.getId(), mmh);
			}
		}
	}

	public static boolean hasMasterMobHunterData(NPC npc) {
		return (npc.getTrait(MasterMobHunterTrait.class).stattype != null);
	}

	public MasterMobHunter get(int id) {
		return mMasterMobHunter.get(id);
	}

	public HashMap<Integer, MasterMobHunter> getAll() {
		return mMasterMobHunter;
	}

	public void put(int id, MasterMobHunter mmh) {
		mMasterMobHunter.put(id, mmh);
	}

	public boolean contains(int id) {
		return mMasterMobHunter.containsKey(id);
	}

	public void remove(int id) {
		mMasterMobHunter.remove(id);
	}

	public void shutdown() {
		if (mUpdater != null)
			mUpdater.cancel();
	}

	public boolean isMasterMobHunter(Entity entity) {
		if (CitizensAPI.getNPCRegistry().isNPC(entity)) {
			NPC npc = CitizensCompat.getNPC(entity);
			return (npc.hasTrait(MasterMobHunterTrait.class));
		} else
			return false;
	}

	public static boolean isMasterMobHunter(NPC npc) {
		return (npc.hasTrait(MasterMobHunterTrait.class));
	}

	// ****************************************************************************
	// Save & Load
	// ****************************************************************************

	public void loadData() {
		try {
			if (!file.exists())
				return;
			config.load(file);
			int n = 0;
			for (String key : config.getKeys(false)) {
				ConfigurationSection section = config.getConfigurationSection(key);
				NPC npc = CitizensAPI.getNPCRegistry().getById(Integer.parseInt(key));
				if (npc != null && npc.hasTrait(MasterMobHunterTrait.class)) {
					MasterMobHunter mmh = new MasterMobHunter(npc);
					if (npc.getTrait(MasterMobHunterTrait.class).stattype == null) {
						mmh.read(section);
						n++;
						section.set(key, null);
						config.save(file);
					}
					mMasterMobHunter.put(Integer.valueOf(key), mmh);
					mmh.getHome();
				}
			}
			Messages.debug("The file citizens-MasterMobHunter.yml is not used anymore and can be deleted.");
			if (n > 0)
				Messages.debug("Loaded %s MasterMobHunter Traits's from file.", n);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}

	// ****************************************************************************
	// Events
	// ****************************************************************************

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onClick(NPCLeftClickEvent event) {
		Messages.debug("NPCLeftClickEvent");
		NPC npc = event.getNPC();
		if (isMasterMobHunter(npc)) {
			@SuppressWarnings("deprecation")
			ItemStack is = event.getClicker().getItemInHand();
			// Messages.debug("ItemStack=%s", is);
			if (!is.getType().equals(Material.STICK)) {
				if (Misc.isMC110OrNewer()) {
					// ((Player) npc).getInventory().setItemInMainHand(is);
					// ((Player)
					// event.getClicker()).getInventory().setItemInMainHand(new
					// ItemStack(Material.AIR));
				} else {
					// ((Player) npc).getInventory().setItemInHand(is);
					// ((Player)
					// event.getClicker()).getInventory().setItemInHand(new
					// ItemStack(Material.AIR));
				}
				Trait trait = getSentinelOrSentryTrait(npc);
				if (trait != null) {
					trait.getNPC().faceLocation(event.getClicker().getLocation());
					trait.getNPC().getDefaultSpeechController()
							.speak(new SpeechContext("Don't hit me!!!", event.getClicker()));
					trait.getNPC().getNavigator().setTarget(event.getClicker(), true);
				}
			} else {
				npc.setName("UPDATING SKIN");
				update(npc);
				MasterMobHunter mmh = mMasterMobHunter.get(npc.getId());
				mmh.update();
				Messages.playerActionBarMessage(event.getClicker(),
						Messages.getString("mobhunting.npc.clickednpc", "killer",
								CitizensAPI.getNPCRegistry().getById(npc.getId()).getName(), "rank", mmh.getRank(),
								"numberofkills", mmh.getNumberOfKills(), "stattype", mmh.getStatType().translateName(),
								"period", mmh.getPeriod().translateNameFriendly(), "npcid", npc.getId()));
				mMasterMobHunter.put(event.getNPC().getId(), mmh);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onKilledTarget(EntityDeathEvent event) {
		if (isMasterMobHunter(event.getEntity().getKiller()) && event.getEntity() instanceof Player) {
			NPC npc = CitizensCompat.getNPC(event.getEntity().getKiller());
			final Player player = (Player) event.getEntity();
			final NPC npc1 = npc;
			Bukkit.getScheduler().runTaskLaterAsynchronously(MobHunting.getInstance(), new Runnable() {
				public void run() {
					Messages.debug("NPC %s (ID=%s) killed %s - return to home", npc1.getName(), npc1.getId(),
							player.getName());
					npc1.teleport(mMasterMobHunter.get(npc1.getId()).getHome(), TeleportCause.PLUGIN);
				}
			}, 20 * 10); // 20ticks/sec * 10 sec
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onSpawnNPC(NPCSpawnEvent event) {
		NPC npc = event.getNPC();
		if (isMasterMobHunter(npc)) {
			if (npc.getStoredLocation() != null && mMasterMobHunter.containsKey(npc.getId())
					&& npc.getStoredLocation().distance(mMasterMobHunter.get(npc.getId()).getHome()) > 0.2) {
				Messages.debug("NPC %s (ID=%s) return to home", npc.getName(), npc.getId());
				final NPC npc1 = npc;
				Bukkit.getScheduler().runTaskLaterAsynchronously(MobHunting.getInstance(), new Runnable() {
					public void run() {
						npc1.teleport(mMasterMobHunter.get(npc1.getId()).getHome(), TeleportCause.PLUGIN);
					}
				}, 20 * 10); // 20ticks/sec * 10 sec
			}
		}
	}

	private Trait getSentinelOrSentryTrait(NPC npc) {
		Trait trait = null;
		if (CitizensCompat.isSentryOrSentinelOrSentries(npc.getEntity())) {
			if (npc.hasTrait(CitizensAPI.getTraitFactory().getTraitClass("Sentinel")))
				trait = npc.getTrait(CitizensAPI.getTraitFactory().getTraitClass("Sentinel"));
			else if (npc.hasTrait(CitizensAPI.getTraitFactory().getTraitClass("Sentry")))
				trait = npc.getTrait(CitizensAPI.getTraitFactory().getTraitClass("Sentry"));

		} else {// how to handle/add Trait ???
			// npc.addTrait(CitizensAPI.getTraitFactory().getTraitClass("Sentinel"));
			// trait =
			// npc.getTrait(CitizensAPI.getTraitFactory().getTraitClass("Sentinel"));
			// Messages.debug("Sentinel trait added to %s (id=%s)",
			// npc.getName(), npc.getId());
		}
		return trait;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onClick(NPCRightClickEvent event) {
		NPC npc = event.getNPC();
		if (isMasterMobHunter(npc)) {
			update(npc);
			MasterMobHunter mmh = mMasterMobHunter.get(npc.getId());
			mmh.update();
			Messages.playerActionBarMessage(event.getClicker(),
					Messages.getString("mobhunting.npc.clickednpc", "killer",
							CitizensAPI.getNPCRegistry().getById(npc.getId()).getName(), "rank", mmh.getRank(),
							"numberofkills", mmh.getNumberOfKills(), "stattype", mmh.getStatType().translateName(),
							"period", mmh.getPeriod().translateNameFriendly(), "npcid", npc.getId()));
			mMasterMobHunter.put(event.getNPC().getId(), mmh);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onPlayerJoin(PlayerJoinEvent event) {
		Iterator<NPC> itr = CitizensAPI.getNPCRegistry().iterator();
		while (itr.hasNext()) {
			NPC npc = itr.next();
			if (event.getPlayer().getName().equals(npc.getName()) && isMasterMobHunter(npc))
				update(npc);
		}
	}

	// @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	// public void onSpawnNPC(NPCTraitEvent event) {
	// Messages.debug("NPCTraitEvent NPC=%s, Trait=%s", event.getNPC().getId(),
	// event.getTrait().getName());
	// }
}
