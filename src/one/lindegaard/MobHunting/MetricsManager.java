package one.lindegaard.MobHunting;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.mcstats_mh.Metrics;
import org.mcstats_mh.Metrics.Graph;

import one.lindegaard.MobHunting.compatibility.ActionAnnouncerCompat;
import one.lindegaard.MobHunting.compatibility.ActionBarAPICompat;
import one.lindegaard.MobHunting.compatibility.ActionbarCompat;
import one.lindegaard.MobHunting.compatibility.BarAPICompat;
import one.lindegaard.MobHunting.compatibility.BattleArenaCompat;
import one.lindegaard.MobHunting.compatibility.BossBarAPICompat;
import one.lindegaard.MobHunting.compatibility.CitizensCompat;
import one.lindegaard.MobHunting.compatibility.ConquestiaMobsCompat;
import one.lindegaard.MobHunting.compatibility.CrackShotCompat;
import one.lindegaard.MobHunting.compatibility.CustomMobsCompat;
import one.lindegaard.MobHunting.compatibility.DisguiseCraftCompat;
import one.lindegaard.MobHunting.compatibility.EssentialsCompat;
import one.lindegaard.MobHunting.compatibility.ExtraHardModeCompat;
import one.lindegaard.MobHunting.compatibility.FactionsCompat;
import one.lindegaard.MobHunting.compatibility.GringottsCompat;
import one.lindegaard.MobHunting.compatibility.IDisguiseCompat;
import one.lindegaard.MobHunting.compatibility.InfernalMobsCompat;
import one.lindegaard.MobHunting.compatibility.LibsDisguisesCompat;
import one.lindegaard.MobHunting.compatibility.MinigamesCompat;
import one.lindegaard.MobHunting.compatibility.MinigamesLibCompat;
import one.lindegaard.MobHunting.compatibility.MobArenaCompat;
import one.lindegaard.MobHunting.compatibility.MobStackerCompat;
import one.lindegaard.MobHunting.compatibility.MyPetCompat;
import one.lindegaard.MobHunting.compatibility.MysteriousHalloweenCompat;
import one.lindegaard.MobHunting.compatibility.MythicMobsCompat;
import one.lindegaard.MobHunting.compatibility.PVPArenaCompat;
import one.lindegaard.MobHunting.compatibility.ProtocolLibCompat;
import one.lindegaard.MobHunting.compatibility.ResidenceCompat;
import one.lindegaard.MobHunting.compatibility.SmartGiantsCompat;
import one.lindegaard.MobHunting.compatibility.StackMobCompat;
import one.lindegaard.MobHunting.compatibility.TARDISWeepingAngelsCompat;
import one.lindegaard.MobHunting.compatibility.TitleAPICompat;
import one.lindegaard.MobHunting.compatibility.TitleManagerCompat;
import one.lindegaard.MobHunting.compatibility.TownyCompat;
import one.lindegaard.MobHunting.compatibility.VanishNoPacketCompat;
import one.lindegaard.MobHunting.compatibility.WorldEditCompat;
import one.lindegaard.MobHunting.compatibility.WorldGuardCompat;
import one.lindegaard.MobHunting.npc.MasterMobHunterManager;

public class MetricsManager {

	// Metrics
	private Metrics metrics;
	private Graph automaticUpdatesGraph, databaseGraph, integrationsGraph, titleManagerGraph, usageGraph,
			mobPluginIntegrationsGraph, protectionPluginsGraph, minigamesGraph, disguiseGraph;
	private MobHunting instance;

	private org.bstats.Metrics bStatsMetrics;

	private ProtocolLibCompat protocolLibCompat;

	public MetricsManager(MobHunting instance, ProtocolLibCompat protocolLibCompat) {
		this.instance = instance;
		this.protocolLibCompat = protocolLibCompat;
	}

	public void startBStatsMetrics() {
		bStatsMetrics = new org.bstats.Metrics(instance);

		bStatsMetrics.addCustomChart(new org.bstats.Metrics.SimplePie("database_used_for_mobhunting") {
			@Override
			public String getValue() {
				return MobHunting.getConfigManager().databaseType;
			}
		});

		bStatsMetrics.addCustomChart(new org.bstats.Metrics.SimplePie("language") {
			@Override
			public String getValue() {
				return MobHunting.getConfigManager().language;
			}
		});

		bStatsMetrics.addCustomChart(new org.bstats.Metrics.SimpleBarChart("protection_plugin_integrations") {
			@Override
			public HashMap<String, Integer> getValues(HashMap<String, Integer> valueMap) {
				valueMap.put("WorldGuard", WorldGuardCompat.isSupported() ? 1 : 0);
				valueMap.put("Factions", FactionsCompat.isSupported() ? 1 : 0);
				valueMap.put("Towny", TownyCompat.isSupported() ? 1 : 0);
				valueMap.put("Residence", ResidenceCompat.isSupported() ? 1 : 0);
				return valueMap;
			}
		});

		bStatsMetrics.addCustomChart(new org.bstats.Metrics.SimpleBarChart("minigame_integrations") {
			@Override
			public HashMap<String, Integer> getValues(HashMap<String, Integer> valueMap) {
				valueMap.put("MobArena", MobArenaCompat.isSupported() ? 1 : 0);
				valueMap.put("Minigames", MinigamesCompat.isSupported() ? 1 : 0);
				valueMap.put("MinigamesLib", MinigamesLibCompat.isSupported() ? 1 : 0);
				valueMap.put("PVPArena", PVPArenaCompat.isSupported() ? 1 : 0);
				valueMap.put("BattleArena", BattleArenaCompat.isSupported() ? 1 : 0);
				return valueMap;
			}
		});

		bStatsMetrics.addCustomChart(new org.bstats.Metrics.SimpleBarChart("disguise_plugin_integrations") {
			@Override
			public HashMap<String, Integer> getValues(HashMap<String, Integer> valueMap) {
				try {
					@SuppressWarnings({ "rawtypes", "unused" })
					Class cls = Class.forName("pgDev.bukkit.DisguiseCraft.disguise.DisguiseType");
					valueMap.put("DisguiseCraft", DisguiseCraftCompat.isSupported() ? 1 : 0);
				} catch (ClassNotFoundException e) {
				}
				try {
					@SuppressWarnings({ "rawtypes", "unused" })
					Class cls = Class.forName("de.robingrether.idisguise.disguise.DisguiseType");
					valueMap.put("iDisguise", IDisguiseCompat.isSupported() ? 1 : 0);
				} catch (ClassNotFoundException e) {
				}
				try {
					@SuppressWarnings({ "rawtypes", "unused" })
					Class cls = Class.forName("me.libraryaddict.disguise.disguisetypes.DisguiseType");
					valueMap.put("LibsDisguises", LibsDisguisesCompat.isSupported() ? 1 : 0);
				} catch (ClassNotFoundException e) {
				}
				valueMap.put("VanishNoPacket", VanishNoPacketCompat.isSupported() ? 1 : 0);
				valueMap.put("Essentials", EssentialsCompat.isSupported() ? 1 : 0);
				return valueMap;
			}
		});

		bStatsMetrics.addCustomChart(new org.bstats.Metrics.SimpleBarChart("other_integrations") {
			@Override
			public HashMap<String, Integer> getValues(HashMap<String, Integer> valueMap) {
				valueMap.put("Citizens", CitizensCompat.isSupported() ? 1 : 0);
				valueMap.put("Gringotts", GringottsCompat.isSupported() ? 1 : 0);
				valueMap.put("MyPet", MyPetCompat.isSupported() ? 1 : 0);
				valueMap.put("WorldEdit", WorldEditCompat.isSupported() ? 1 : 0);
				valueMap.put("ProtocolLib", protocolLibCompat.isSupported() ? 1 : 0);
				valueMap.put("ExtraHardMode", ExtraHardModeCompat.isSupported() ? 1 : 0);
				valueMap.put("CrackShot", CrackShotCompat.isSupported() ? 1 : 0);
				return valueMap;
			}
		});

		bStatsMetrics.addCustomChart(new org.bstats.Metrics.SimpleBarChart("special_mobs") {
			@Override
			public HashMap<String, Integer> getValues(HashMap<String, Integer> valueMap) {
				valueMap.put("MythicMobs", MythicMobsCompat.isSupported() ? 1 : 0);
				valueMap.put("TARDISWeepingAngels", TARDISWeepingAngelsCompat.isSupported() ? 1 : 0);
				valueMap.put("MobStacker", MobStackerCompat.isSupported() ? 1 : 0);
				valueMap.put("CustomMobs", CustomMobsCompat.isSupported() ? 1 : 0);
				valueMap.put("ConquestiaMobs", ConquestiaMobsCompat.isSupported() ? 1 : 0);
				valueMap.put("StackMob", StackMobCompat.isSupported() ? 1 : 0);
				valueMap.put("MysteriousHalloween", MysteriousHalloweenCompat.isSupported() ? 1 : 0);
				valueMap.put("SmartGiants", SmartGiantsCompat.isSupported() ? 1 : 0);
				valueMap.put("InfernalMobs", InfernalMobsCompat.isSupported() ? 1 : 0);
				return valueMap;
			}
		});

		bStatsMetrics.addCustomChart(new org.bstats.Metrics.SimpleBarChart("titlemanagers") {
			@Override
			public HashMap<String, Integer> getValues(HashMap<String, Integer> valueMap) {
				valueMap.put("BossBarAPI", BossBarAPICompat.isSupported() ? 1 : 0);
				valueMap.put("TitleAPI", TitleAPICompat.isSupported() ? 1 : 0);
				valueMap.put("BarAPI", BarAPICompat.isSupported() ? 1 : 0);
				valueMap.put("TitleManager", TitleManagerCompat.isSupported() ? 1 : 0);
				valueMap.put("ActionBar", ActionbarCompat.isSupported() ? 1 : 0);
				valueMap.put("ActionBarAPI", ActionBarAPICompat.isSupported() ? 1 : 0);
				valueMap.put("ActionAnnouncer", ActionAnnouncerCompat.isSupported() ? 1 : 0);
				return valueMap;
			}
		});

		bStatsMetrics.addCustomChart(new org.bstats.Metrics.SimpleBarChart("mobhunting_usage") {
			@Override
			public HashMap<String, Integer> getValues(HashMap<String, Integer> valueMap) {
				valueMap.put("Leaderboards", MobHunting.getLeaderboardManager().getWorldLeaderBoards().size());
				valueMap.put("MasterMobHunters", MasterMobHunterManager.getMasterMobHunterManager().size());
				valueMap.put("PlayerBounties", MobHunting.getConfigManager().disablePlayerBounties ? 0
						: MobHunting.getBountyManager().getAllBounties().size());
				return valueMap;
			}
		});
	}

	public void startMetrics() {
		try {
			metrics = new Metrics(instance);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		databaseGraph = metrics.createGraph("Database used for MobHunting");
		if (MobHunting.getConfigManager().databaseType.equalsIgnoreCase("MySQL")) {
			databaseGraph.addPlotter(new Metrics.Plotter("MySQL") {
				@Override
				public int getValue() {
					return 1;
				}
			});

		} else if (MobHunting.getConfigManager().databaseType.equalsIgnoreCase("SQLite")) {
			databaseGraph.addPlotter(new Metrics.Plotter("SQLite") {
				@Override
				public int getValue() {
					return 1;
				}
			});
		} else {
			databaseGraph.addPlotter(new Metrics.Plotter(MobHunting.getConfigManager().databaseType) {
				@Override
				public int getValue() {
					return 1;
				}
			});
		}
		metrics.addGraph(databaseGraph);
		integrationsGraph = metrics.createGraph("MobHunting integrations");
		integrationsGraph.addPlotter(new Metrics.Plotter("Citizens") {
			@Override
			public int getValue() {
				return CitizensCompat.isSupported() ? 1 : 0;
			}
		});
		integrationsGraph.addPlotter(new Metrics.Plotter("Essentials") {
			@Override
			public int getValue() {
				return EssentialsCompat.isSupported() ? 1 : 0;
			}
		});
		integrationsGraph.addPlotter(new Metrics.Plotter("Gringotts") {
			@Override
			public int getValue() {
				return GringottsCompat.isSupported() ? 1 : 0;
			}
		});
		integrationsGraph.addPlotter(new Metrics.Plotter("MyPet") {
			@Override
			public int getValue() {
				return MyPetCompat.isSupported() ? 1 : 0;
			}
		});
		integrationsGraph.addPlotter(new Metrics.Plotter("WorldEdit") {
			@Override
			public int getValue() {
				try {
					@SuppressWarnings({ "rawtypes", "unused" })
					Class cls = Class.forName("com.sk89q.worldedit.bukkit.WorldEditPlugin");
					return WorldEditCompat.isSupported() ? 1 : 0;
				} catch (ClassNotFoundException e) {
					return 0;
				}

			}
		});
		integrationsGraph.addPlotter(new Metrics.Plotter("VanishNoPacket") {
			@Override
			public int getValue() {
				return VanishNoPacketCompat.isSupported() ? 1 : 0;
			}
		});
		integrationsGraph.addPlotter(new Metrics.Plotter("ProtocolLib") {
			@Override
			public int getValue() {
				return protocolLibCompat.isSupported() ? 1 : 0;
			}
		});
		integrationsGraph.addPlotter(new Metrics.Plotter("ExtraHardMode") {
			@Override
			public int getValue() {
				return ExtraHardModeCompat.isSupported() ? 1 : 0;
			}
		});
		metrics.addGraph(integrationsGraph);

		protectionPluginsGraph = metrics.createGraph("Protection plugins");
		protectionPluginsGraph.addPlotter(new Metrics.Plotter("WorldGuard") {
			@Override
			public int getValue() {
				try {
					@SuppressWarnings({ "rawtypes", "unused" })
					Class cls = Class.forName("com.sk89q.worldguard.bukkit.WorldGuardPlugin");
					return WorldGuardCompat.isSupported() ? 1 : 0;
				} catch (ClassNotFoundException e) {
					return 0;
				}

			}
		});
		protectionPluginsGraph.addPlotter(new Metrics.Plotter("Factions") {
			@Override
			public int getValue() {
				return FactionsCompat.isSupported() ? 1 : 0;

			}
		});
		protectionPluginsGraph.addPlotter(new Metrics.Plotter("Towny") {
			@Override
			public int getValue() {
				return TownyCompat.isSupported() ? 1 : 0;

			}
		});
		protectionPluginsGraph.addPlotter(new Metrics.Plotter("Residence") {
			@Override
			public int getValue() {
				return ResidenceCompat.isSupported() ? 1 : 0;

			}
		});
		metrics.addGraph(protectionPluginsGraph);

		minigamesGraph = metrics.createGraph("Minigames");
		minigamesGraph.addPlotter(new Metrics.Plotter("MobArena") {
			@Override
			public int getValue() {
				return MobArenaCompat.isSupported() ? 1 : 0;
			}
		});
		minigamesGraph.addPlotter(new Metrics.Plotter("Minigames") {
			@Override
			public int getValue() {
				return MinigamesCompat.isSupported() ? 1 : 0;
			}
		});
		minigamesGraph.addPlotter(new Metrics.Plotter("MinigamesLib") {
			@Override
			public int getValue() {
				return MinigamesLibCompat.isSupported() ? 1 : 0;
			}
		});
		minigamesGraph.addPlotter(new Metrics.Plotter("PvpArena") {
			@Override
			public int getValue() {
				return PVPArenaCompat.isSupported() ? 1 : 0;
			}
		});
		minigamesGraph.addPlotter(new Metrics.Plotter("BattleArena") {
			@Override
			public int getValue() {
				return BattleArenaCompat.isSupported() ? 1 : 0;
			}
		});
		metrics.addGraph(minigamesGraph);

		mobPluginIntegrationsGraph = metrics.createGraph("Special Mobs");
		mobPluginIntegrationsGraph.addPlotter(new Metrics.Plotter("MythicMobs") {
			@Override
			public int getValue() {
				return MythicMobsCompat.isSupported() ? 1 : 0;
			}
		});
		mobPluginIntegrationsGraph.addPlotter(new Metrics.Plotter("TARDISWeepingAngels") {
			@Override
			public int getValue() {
				return TARDISWeepingAngelsCompat.isSupported() ? 1 : 0;
			}
		});
		mobPluginIntegrationsGraph.addPlotter(new Metrics.Plotter("MobStacker") {
			@Override
			public int getValue() {
				return MobStackerCompat.isSupported() ? 1 : 0;
			}
		});
		mobPluginIntegrationsGraph.addPlotter(new Metrics.Plotter("CustomMobs") {
			@Override
			public int getValue() {
				return CustomMobsCompat.isSupported() ? 1 : 0;
			}
		});
		mobPluginIntegrationsGraph.addPlotter(new Metrics.Plotter("Conquestia Mobs") {
			@Override
			public int getValue() {
				return ConquestiaMobsCompat.isSupported() ? 1 : 0;
			}
		});
		mobPluginIntegrationsGraph.addPlotter(new Metrics.Plotter("StackMob") {
			@Override
			public int getValue() {
				return StackMobCompat.isSupported() ? 1 : 0;
			}
		});
		mobPluginIntegrationsGraph.addPlotter(new Metrics.Plotter("MysteriousHalloween") {
			@Override
			public int getValue() {
				return MysteriousHalloweenCompat.isSupported() ? 1 : 0;
			}
		});
		mobPluginIntegrationsGraph.addPlotter(new Metrics.Plotter("SmartGiants") {
			@Override
			public int getValue() {
				return SmartGiantsCompat.isSupported() ? 1 : 0;
			}
		});
		mobPluginIntegrationsGraph.addPlotter(new Metrics.Plotter("InfernalMobs") {
			@Override
			public int getValue() {
				return InfernalMobsCompat.isSupported() ? 1 : 0;
			}
		});
		metrics.addGraph(mobPluginIntegrationsGraph);

		disguiseGraph = metrics.createGraph("Disguise plugins");
		disguiseGraph.addPlotter(new Metrics.Plotter("DisguisesCraft") {
			@Override
			public int getValue() {
				try {
					@SuppressWarnings({ "rawtypes", "unused" })
					Class cls = Class.forName("pgDev.bukkit.DisguiseCraft.disguise.DisguiseType");
					return DisguiseCraftCompat.isSupported() ? 1 : 0;
				} catch (ClassNotFoundException e) {
					return 0;
				}
			}
		});
		disguiseGraph.addPlotter(new Metrics.Plotter("iDisguises") {
			@Override
			public int getValue() {
				try {
					@SuppressWarnings({ "rawtypes", "unused" })
					Class cls = Class.forName("de.robingrether.idisguise.disguise.DisguiseType");
					return IDisguiseCompat.isSupported() ? 1 : 0;
				} catch (ClassNotFoundException e) {
					return 0;
				}
			}
		});
		disguiseGraph.addPlotter(new Metrics.Plotter("LibsDisguises") {
			@Override
			public int getValue() {
				try {
					@SuppressWarnings({ "rawtypes", "unused" })
					Class cls = Class.forName("me.libraryaddict.disguise.disguisetypes.DisguiseType");
					return LibsDisguisesCompat.isSupported() ? 1 : 0;
				} catch (ClassNotFoundException e) {
					return 0;
				}
			}
		});

		metrics.addGraph(disguiseGraph);

		titleManagerGraph = metrics.createGraph("TitleManagers");
		titleManagerGraph.addPlotter(new Metrics.Plotter("BossBarAPI") {
			@Override
			public int getValue() {
				return BossBarAPICompat.isSupported() ? 1 : 0;
			}
		});
		titleManagerGraph.addPlotter(new Metrics.Plotter("TitleAPI") {
			@Override
			public int getValue() {
				return TitleAPICompat.isSupported() ? 1 : 0;
			}
		});
		titleManagerGraph.addPlotter(new Metrics.Plotter("BarAPI") {
			@Override
			public int getValue() {
				return BarAPICompat.isSupported() ? 1 : 0;
			}
		});
		titleManagerGraph.addPlotter(new Metrics.Plotter("TitleManager") {
			@Override
			public int getValue() {
				return TitleManagerCompat.isSupported() ? 1 : 0;
			}
		});
		titleManagerGraph.addPlotter(new Metrics.Plotter("Actionbar") {
			@Override
			public int getValue() {
				return ActionbarCompat.isSupported() ? 1 : 0;
			}
		});
		titleManagerGraph.addPlotter(new Metrics.Plotter("ActionBarAPI") {
			@Override
			public int getValue() {
				return ActionBarAPICompat.isSupported() ? 1 : 0;
			}
		});
		titleManagerGraph.addPlotter(new Metrics.Plotter("ActionAnnouncer") {
			@Override
			public int getValue() {
				return ActionAnnouncerCompat.isSupported() ? 1 : 0;
			}
		});
		metrics.addGraph(titleManagerGraph);

		automaticUpdatesGraph = metrics.createGraph("# of installations with automatic update");
		automaticUpdatesGraph.addPlotter(new Metrics.Plotter("Amount") {
			@Override
			public int getValue() {
				return MobHunting.getConfigManager().autoupdate ? 1 : 0;
			}
		});
		metrics.addGraph(automaticUpdatesGraph);

		usageGraph = metrics.createGraph("Usage");
		usageGraph.addPlotter(new Metrics.Plotter("# of Leaderboards") {
			@Override
			public int getValue() {
				return MobHunting.getLeaderboardManager().getWorldLeaderBoards().size();
			}
		});
		usageGraph.addPlotter(new Metrics.Plotter("# of MasterMobHunters") {
			@Override
			public int getValue() {
				return MasterMobHunterManager.getMasterMobHunterManager().size();
			}
		});
		usageGraph.addPlotter(new Metrics.Plotter("# of Bounties") {
			@Override
			public int getValue() {
				if (MobHunting.getConfigManager().disablePlayerBounties)
					return 0;
				else
					return MobHunting.getBountyManager().getAllBounties().size();
			}
		});
		metrics.addGraph(usageGraph);
		metrics.start();
		Messages.debug("Metrics started");
		Bukkit.getScheduler().runTaskTimerAsynchronously(MobHunting.getInstance(), new Runnable() {
			public void run() {
				try {
					// make a URL to MCStats.org
					URL url = new URL("http://mcstats.org");
					if (HttpTools.isHomePageReachable(url)) {
						metrics.enable();
					} else {
						metrics.disable();
						Messages.debug("Http://mcstats.org seems to be down");
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}, 100, 72000);

	}
}
