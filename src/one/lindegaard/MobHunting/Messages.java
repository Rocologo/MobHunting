package one.lindegaard.MobHunting;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.clip.placeholderapi.PlaceholderAPI;
import one.lindegaard.MobHunting.compatibility.ActionAnnouncerCompat;
import one.lindegaard.MobHunting.compatibility.ActionBarAPICompat;
import one.lindegaard.MobHunting.compatibility.ActionbarCompat;
import one.lindegaard.MobHunting.compatibility.BarAPICompat;
import one.lindegaard.MobHunting.compatibility.BossBarAPICompat;
import one.lindegaard.MobHunting.compatibility.CitizensCompat;
import one.lindegaard.MobHunting.compatibility.PlaceholderAPICompat;
import one.lindegaard.MobHunting.compatibility.TitleAPICompat;
import one.lindegaard.MobHunting.compatibility.TitleManagerCompat;
import one.lindegaard.MobHunting.mobs.ExtendedMob;
import one.lindegaard.MobHunting.mobs.MobPlugin;

public class Messages {

	private MobHunting plugin;

	public Messages(MobHunting plugin) {
		this.plugin = plugin;
	}

	private static Map<String, String> mTranslationTable;
	private static String[] mValidEncodings = new String[] { "UTF-16", "UTF-16BE", "UTF-16LE", "UTF-8", "ISO646-US" };
	private static final String PREFIX = "[MobHunting]";
	private static String[] sources = new String[] { "en_US.lang", "zh_CN.lang" };
	private static MobHunting mPlugin;

	public void exportDefaultLanguages(MobHunting plugin) {
		mPlugin = plugin;
		File folder = new File(plugin.getDataFolder(), "lang");
		if (!folder.exists())
			folder.mkdirs();

		for (String source : sources) {
			File dest = new File(folder, source);
			if (!dest.exists()) {
				// if (plugin.getResource("lang/" + source) != null) {
				Bukkit.getLogger().info(PREFIX + " Creating language file " + source + " from JAR.");
				plugin.saveResource("lang/" + source, false);
			} else {
				if (!injectChanges(plugin.getResource("lang/" + source),
						new File(plugin.getDataFolder(), "lang/" + source))) {
					plugin.saveResource("lang/" + source, true);
				}
			}
			mTranslationTable = loadLang(dest);
		}
	}

	private static boolean injectChanges(InputStream inJar, File onDisk) {
		try {
			Map<String, String> source = loadLang(inJar, "UTF-8");
			Map<String, String> dest = loadLang(onDisk);

			if (dest == null)
				return false;

			HashMap<String, String> newEntries = new HashMap<String, String>();
			for (String key : source.keySet()) {
				if (!dest.containsKey(key)) {
					newEntries.put(key, source.get(key));
				}
			}

			if (!newEntries.isEmpty()) {
				BufferedWriter writer = new BufferedWriter(
						new OutputStreamWriter(new FileOutputStream(onDisk, true), StandardCharsets.UTF_8));
				for (Entry<String, String> entry : newEntries.entrySet())
					writer.append("\n" + entry.getKey() + "=" + entry.getValue());
				writer.close();
				Bukkit.getLogger().info(PREFIX + " Updated " + onDisk.getName() + " language file with missing keys");
			}

			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	private static boolean sortFileOnDisk(File onDisk) {
		try {
			Map<String, String> source = loadLang(onDisk);
			source = sortByKeys(source);
			BufferedWriter writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(onDisk, false), StandardCharsets.UTF_8));
			for (Entry<String, String> entry : source.entrySet()) {
				writer.append("\n" + entry.getKey() + "=" + entry.getValue());
			}
			writer.close();
			// Bukkit.getLogger().info(PREFIX + " Sorted " + onDisk.getName() +
			// " translation");

			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static void injectMissingMobNamesToLangFiles() {
		File folder = new File(MobHunting.getInstance().getDataFolder(), "lang");
		if (!folder.exists())
			folder.mkdirs();

		boolean customLanguage = true;
		for (String source : sources) {
			if (source.equalsIgnoreCase(MobHunting.getConfigManager().language))
				customLanguage = false;
			File dest = new File(folder, source);
			injectMissingMobNamesToLangFile(dest);
		}

		if (customLanguage) {
			File dest = new File(folder, MobHunting.getConfigManager().language + ".lang");
			injectMissingMobNamesToLangFile(dest);
			sortFileOnDisk(dest);
		}

	}

	private static boolean injectMissingMobNamesToLangFile(File onDisk) {
		try {
			Map<String, String> dest = loadLang(onDisk);

			if (dest == null)
				return false;

			HashMap<String, String> newEntries = new HashMap<String, String>();
			if (MobHunting.getExtendedMobManager() != null)
				for (Entry<Integer, ExtendedMob> key : MobHunting.getExtendedMobManager().getAllMobs().entrySet()) {
					String k;
					if (key.getValue().getMobPlugin() == MobPlugin.Minecraft)
						k = "mobs." + key.getValue().getMobtype() + ".name";
					else
						k = "mobs." + key.getValue().getMobPlugin().name() + "_" + key.getValue().getMobtype()
								+ ".name";
					if (!dest.containsKey(k)) {
						Bukkit.getLogger().info(
								PREFIX + " Creating missing key (" + k + ") in language file " + onDisk.getName());
						newEntries.put(k, key.getValue().getMobName());
					}
				}

			if (!newEntries.isEmpty()) {
				BufferedWriter writer = new BufferedWriter(
						new OutputStreamWriter(new FileOutputStream(onDisk, true), StandardCharsets.UTF_8));
				for (Entry<String, String> entry : newEntries.entrySet()) {
					writer.append("\n" + entry.getKey() + "=" + entry.getValue());
				}
				writer.close();

				// add new mobs to the TranslationTable
				mTranslationTable.putAll(newEntries);

				Bukkit.getLogger().info(PREFIX + " Updated " + onDisk.getName() + " language file");
			}

			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	private static boolean injectMissingPluginNamesToLangFile(File onDisk) {
		try {
			Map<String, String> dest = loadLang(onDisk);

			if (dest == null)
				return false;

			HashMap<String, String> newEntries = new HashMap<String, String>();

			for (MobPlugin p : MobPlugin.values()) {
				String k = "stats." + p.name() + ".kills";
				if (!dest.containsKey(k)) {
					Bukkit.getLogger()
							.info(PREFIX + " Creating missing key (" + k + ") in language file" + onDisk.getName());
					newEntries.put(k, p.name() + " kills");
				}
				k = "stats." + p.name() + ".assists";
				if (!dest.containsKey(k)) {
					Bukkit.getLogger()
							.info(PREFIX + " Creating missing key (" + k + ") in language file " + onDisk.getName());
					newEntries.put(k, p.name() + " assists");
				}
				k = "stats." + p.name() + ".cashs";
				if (!dest.containsKey(k)) {
					Bukkit.getLogger()
							.info(PREFIX + " Creating missing key (" + k + ") in language file " + onDisk.getName());
					newEntries.put(k, p.name() + " cash");
				}
			}

			if (!newEntries.isEmpty()) {
				BufferedWriter writer = new BufferedWriter(
						new OutputStreamWriter(new FileOutputStream(onDisk, true), StandardCharsets.UTF_8));
				for (Entry<String, String> entry : newEntries.entrySet()) {
					writer.append("\n" + entry.getKey() + "=" + entry.getValue());
				}
				writer.close();

				// add new mobs to the TranslationTable
				mTranslationTable.putAll(newEntries);
				Bukkit.getLogger().info(PREFIX + " Updated " + onDisk.getName() + " language file");
			}

			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	private static Map<String, String> loadLang(InputStream stream, String encoding) throws IOException {
		Map<String, String> map = new HashMap<String, String>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream, encoding));

		while (reader.ready()) {
			String line = reader.readLine();
			if (line == null)
				continue;
			int index = line.indexOf('=');
			if (index == -1)
				continue;

			String key = line.substring(0, index).trim();
			String value = line.substring(index + 1).trim();

			map.put(key, value);
		}
		reader.close();

		return map;
	}

	private static Pattern mDetectEncodingPattern = Pattern.compile("^[a-zA-Z\\.\\-0-9_]+=.+$");

	private static String detectEncoding(File file) throws IOException {
		for (String charset : mValidEncodings) {
			FileInputStream input = new FileInputStream(file);
			BufferedReader reader = new BufferedReader(new InputStreamReader(input, charset));
			String line = null;
			boolean ok = true;

			while (reader.ready()) {
				line = reader.readLine();
				if (line == null || line.trim().isEmpty())
					continue;

				if (!mDetectEncodingPattern.matcher(line.trim()).matches())
					ok = false;
			}

			reader.close();

			if (ok)
				return charset;
		}

		return "UTF-8";
	}

	private static Map<String, String> loadLang(File file) {
		Map<String, String> map;

		try {
			String encoding = detectEncoding(file);
			if (encoding == null) {
				FileInputStream input = new FileInputStream(file);
				Bukkit.getLogger().warning(PREFIX + " Could not detect encoding of lang file. Defaulting to UTF-8");
				map = loadLang(input, "UTF-8");
				input.close();
			}

			FileInputStream input = new FileInputStream(file);
			map = loadLang(input, encoding);
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		return map;
	}

	public static void setLanguage(String lang) {
		File file = new File(MobHunting.getInstance().getDataFolder(), "lang/" + lang);
		if (!file.exists()) {
			Bukkit.getLogger().severe(PREFIX
					+ " Language file does not exist. Creating a new file based on en_US. You need to translate the file yourself.");
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		if (file.exists()) {
			InputStream resource = mPlugin.getResource("lang/en_US.lang");
			injectChanges(resource, file);
			mTranslationTable = loadLang(file);
			injectMissingPluginNamesToLangFile(file);
			injectMissingMobNamesToLangFile(file);
			sortFileOnDisk(file);
		} else {
			Bukkit.getLogger().warning(PREFIX + " Could not read the language file:" + file.getName());
		}

		if (mTranslationTable == null) {
			mTranslationTable = new HashMap<String, String>();
			Bukkit.getLogger().warning(PREFIX + " Creating new translation table.");
		}
	}

	private static String getStringInternal(String key) {
		String value = mTranslationTable.get(key);

		if (value == null) {
			Bukkit.getLogger().warning(PREFIX + " mTranslationTable has not key: " + key.toString());
			throw new MissingResourceException("", "", key);
		}

		return value;
	}

	private static Pattern mPattern;

	/**
	 * Gets the message and replaces specified values
	 * 
	 * @param key
	 *            The message key to find
	 * @param values
	 *            these are key-value pairs, they should be like: {key1, value1,
	 *            key2, value2,..., keyN,valueN}. keys must be strings
	 */
	public static String getString(String key, Object... values) {
		try {
			if (mPattern == null)
				mPattern = Pattern.compile("\\$\\{([\\w\\.\\-]+)\\}");

			HashMap<String, Object> map = new HashMap<String, Object>();

			String name = null;
			for (Object value : values) {
				if (name == null)
					name = (String) value; // This must be a string
				else {
					map.put(name, value);
					name = null;
				}
			}

			String str = getStringInternal(key);
			Matcher m = mPattern.matcher(str);

			String output = str;

			while (m.find()) {
				name = m.group(1);
				Object replace = map.get(name);
				if (replace != null)
					output = output.replaceAll("\\$\\{" + name + "\\}", Matcher.quoteReplacement(replace.toString()));
			}

			return ChatColor.translateAlternateColorCodes('&', output);
		} catch (MissingResourceException e) {
			Bukkit.getLogger().warning(PREFIX + " MobHunting could not find key: " + key.toString());
			return key;
		}
	}

	public static String getString(String key) {
		try {
			return ChatColor.translateAlternateColorCodes('&', getStringInternal(key));
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Broadcast message to all players except Player using the ActionBar. if
	 * the no plugins for the actionbar is available the chat will be used.
	 * 
	 * @param message
	 * @param except
	 */
	public void broadcast(String message, Player except) {
		if (isEmpty(message))
			return;
		Iterator<Player> players = MobHunting.getMobHuntingManager().getOnlinePlayers().iterator();
		while (players.hasNext()) {
			Player player = players.next();
			if (player.equals(except) || plugin.getPlayerSettingsmanager().getPlayerSettings(player).isMuted())
				continue;

			if (MobHunting.getConfigManager().useActionBarforBroadcasts)
				playerActionBarMessage(player, message);
			else if (isEmpty(message)) {
				player.sendMessage(PlaceholderAPICompat.setPlaceholders(player, message));
			}
		}
	}

	/**
	 * Show debug information in the Server console log
	 * 
	 * @param message
	 * @param args
	 */
	public static void debug(String message, Object... args) {
		if (MobHunting.getConfigManager().killDebug) {
			if (PlaceholderAPICompat.isSupported())
				Bukkit.getServer().getConsoleSender().sendMessage(
						PREFIX + "[Debug] " + PlaceholderAPI.setPlaceholders(null, String.format(message, args)));
			else
				Bukkit.getServer().getConsoleSender().sendMessage(PREFIX + "[Debug] " + String.format(message, args));
		}
	}

	/**
	 * Show learning messages to the player
	 * 
	 * @param player
	 * @param text
	 * @param args
	 */
	public void learn(Player player, String text, Object... args) {
		if (player != null && !CitizensCompat.isNPC(player)
				&& plugin.getPlayerSettingsmanager().getPlayerSettings(player).isLearningMode() && !isEmpty(text))
			playerBossbarMessage(player, text, args);
	}

	/**
	 * Show message to the player using the BossBar. If no BossBar plugin is
	 * available the player chat will be used.
	 * 
	 * @param player
	 * @param message
	 * @param args
	 */
	public void playerBossbarMessage(Player player, String message, Object... args) {
		if (isEmpty(message))
			return;

		message = PlaceholderAPICompat.setPlaceholders(player, message);

		if (BossBarAPICompat.isSupported()) {
			BossBarAPICompat.addBar(player, String.format(message, args));
		} else if (BarAPICompat.isSupported()) {
			BarAPICompat.setMessageTime(player, String.format(message, args), 5);
		} else {
			player.sendMessage(ChatColor.AQUA + Messages.getString("mobhunting.learn.prefix") + " "
					+ String.format(message, args));
		}
	}

	/**
	 * Show message to the player using the ActionBar
	 * 
	 * @param player
	 * @param message
	 */
	public void playerActionBarMessage(final Player player, String message) {
		if (isEmpty(message))
			return;

		message = PlaceholderAPICompat.setPlaceholders(player, message);

		if (TitleManagerCompat.isSupported()) {
			TitleManagerCompat.setActionBar(player, message);
		} else if (ActionbarCompat.isSupported()) {
			ActionbarCompat.setMessage(player, message);
		} else if (ActionAnnouncerCompat.isSupported()) {
			ActionAnnouncerCompat.setMessage(player, message);
		} else if (ActionBarAPICompat.isSupported()) {
			ActionBarAPICompat.setMessage(player, message);
		} else {
			if (!isEmpty(message))
				player.sendMessage(message);
		}
	}

	public void playerSendMessage(final Player player, String message) {
		if (isEmpty(message))
			return;
		player.sendMessage(PlaceholderAPICompat.setPlaceholders(player, message));
	}

	public void senderSendMessage(final CommandSender sender, String message) {
		if (isEmpty(message))
			return;
		if (sender instanceof Player)
			((Player) sender).sendMessage(PlaceholderAPICompat.setPlaceholders((Player) sender, message));
		else
			sender.sendMessage(message);
	}

	public void playerSendTitlesMessage(Player player, String title, String subtitle, int fadein, int stay,
			int fadeout) {

		title = PlaceholderAPICompat.setPlaceholders(player, title);
		subtitle = PlaceholderAPICompat.setPlaceholders(player, subtitle);

		if (TitleManagerCompat.isSupported()) {
			TitleManagerCompat.sendTitles(player, title, subtitle, fadein, stay, fadeout);
		} else if (TitleAPICompat.isSupported()) {
			TitleAPICompat.sendTitles(player, title, subtitle, fadein, stay, fadeout);
		} else {
			if (!(isEmpty(title) && isEmpty(subtitle)))
				player.sendMessage(new String[] { title, subtitle });
		}
	}

	private static Map<String, String> sortByKeys(Map<String, String> map) {
		SortedSet<String> keys = new TreeSet<String>(map.keySet());
		Map<String, String> sortedHashMap = new LinkedHashMap<String, String>();
		for (String it : keys) {
			sortedHashMap.put(it, map.get(it));
		}
		return sortedHashMap;
	}

	private static boolean isEmpty(String message) {
		message = ChatColor.stripColor(message);
		return message.isEmpty();
	}

}
