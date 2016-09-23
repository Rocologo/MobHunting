package one.lindegaard.MobHunting;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;

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
import one.lindegaard.MobHunting.compatibility.CustomMobsCompat;
import one.lindegaard.MobHunting.compatibility.DisguiseCraftCompat;
import one.lindegaard.MobHunting.compatibility.EssentialsCompat;
import one.lindegaard.MobHunting.compatibility.GringottsCompat;
import one.lindegaard.MobHunting.compatibility.IDisguiseCompat;
import one.lindegaard.MobHunting.compatibility.LibsDisguisesCompat;
import one.lindegaard.MobHunting.compatibility.MobArenaCompat;
import one.lindegaard.MobHunting.compatibility.MobStackerCompat;
import one.lindegaard.MobHunting.compatibility.MyPetCompat;
import one.lindegaard.MobHunting.compatibility.MythicMobsCompat;
import one.lindegaard.MobHunting.compatibility.PVPArenaCompat;
import one.lindegaard.MobHunting.compatibility.TARDISWeepingAngelsCompat;
import one.lindegaard.MobHunting.compatibility.TitleAPICompat;
import one.lindegaard.MobHunting.compatibility.TitleManagerCompat;
import one.lindegaard.MobHunting.compatibility.VanishNoPacketCompat;
import one.lindegaard.MobHunting.compatibility.WorldEditCompat;
import one.lindegaard.MobHunting.compatibility.WorldGuardCompat;
import one.lindegaard.MobHunting.npc.MasterMobHunterManager;

public class MetricsManager {

	// Metrics
	private Metrics metrics;
	private Graph automaticUpdatesGraph, databaseGraph, integrationsGraph, titleManagerGraph, usageGraph,
			mobPluginIntegrationsGraph;
	private MobHunting instance;

	public MetricsManager(MobHunting instance) {
		this.instance = instance;
	}

	public void startMetrics() {
		try {
			metrics = new Metrics(instance);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		databaseGraph = metrics.createGraph("Database used for MobHunting");
		if (MobHunting.getConfigManager().databaseType.equalsIgnoreCase("MySQL"))
			databaseGraph.addPlotter(new Metrics.Plotter("MySQL") {
				@Override
				public int getValue() {
					return 1;
				}
			});
		else if (MobHunting.getConfigManager().databaseType.equalsIgnoreCase("SQLite"))
			databaseGraph.addPlotter(new Metrics.Plotter("SQLite") {
				@Override
				public int getValue() {
					return 1;
				}
			});
		else {
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
		integrationsGraph.addPlotter(new Metrics.Plotter("DisguisesCraft") {
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
		integrationsGraph.addPlotter(new Metrics.Plotter("iDisguises") {
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
		integrationsGraph.addPlotter(new Metrics.Plotter("LibsDisguises") {
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
		integrationsGraph.addPlotter(new Metrics.Plotter("MobArena") {
			@Override
			public int getValue() {
				return MobArenaCompat.isSupported() ? 1 : 0;
			}
		});
		integrationsGraph.addPlotter(new Metrics.Plotter("PvpArena") {
			@Override
			public int getValue() {
				return PVPArenaCompat.isSupported() ? 1 : 0;
			}
		});
		integrationsGraph.addPlotter(new Metrics.Plotter("BattleArena") {
			@Override
			public int getValue() {
				return BattleArenaCompat.isSupported() ? 1 : 0;
			}
		});
		integrationsGraph.addPlotter(new Metrics.Plotter("WorldGuard") {
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
		metrics.addGraph(integrationsGraph);

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
		metrics.addGraph(mobPluginIntegrationsGraph);

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
	}

	public static boolean isMCStatsReachable() {
		try {
			// make a URL to MCStats.org
			URL url = new URL("http://mcstats.org");

			// open a connection to that source
			HttpURLConnection urlConnect = (HttpURLConnection) url.openConnection();

			// trying to retrieve data from the source. If there
			// is no connection, this line will fail
			urlConnect.setConnectTimeout(5000);
			@SuppressWarnings("unused")
			Object objData = urlConnect.getContent();

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			return false;
		}
		return true;
	}
}
