package com.ibys.ilx2m3u;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.xml.XmlConfigurationFactory;

import com.dd.plist.NSArray;
import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.dd.plist.PropertyListParser;

/**
 * Hello world!
 *
 */
public final class Main {
	private static ConfigurationFactory factory;
	private static ConfigurationSource configurationSource;
	private static Configuration configuration;
	private static LoggerContext loggerContext;

	private static boolean isBypassPlaylist(List<String> bypassPlaylistLists, String playlist) {
		for (String bypassPlaylistList : bypassPlaylistLists) {
			if (playlist.equals(bypassPlaylistList))
				return true;
		}
		return false;
	}

	public static void main(String[] args) throws Exception {

		ArrayList<Playlist> playlists = new ArrayList<Playlist>();

		if (args.length != 2) {
			System.out.println("Usage: ilx2m3u iTunesLibraryXmlPath m3uPath");
			System.out.println(args.length);
			System.exit(1);
		}

		System.setProperty("logFilename", Main.class.getSimpleName());

		factory = XmlConfigurationFactory.getInstance();
		configurationSource = new ConfigurationSource(new FileInputStream(new File("conf/log4j2.xml")));
		configuration = factory.getConfiguration(configurationSource);
		loggerContext = new LoggerContext(Main.class.getSimpleName());
		loggerContext.start(configuration);

		Logger logger = loggerContext.getLogger(Main.class.getName());
		logger.info(Main.class.getSimpleName() + " starting..");
		logger.info("iTunes music library xml=" + args[0]);
		logger.info("m3u store location=" + args[1]);

		Scanner sc = new Scanner(new File("conf/bypass_playlist.lst"));
		List<String> bypassPlaylists = new ArrayList<String>();
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			if (!line.startsWith("#")) {
				bypassPlaylists.add(line);
			}
		}
		sc.close();
		logger.debug("Playlist bypasss list: " + bypassPlaylists);

		NSDictionary rootDict = (NSDictionary) PropertyListParser.parse(new File(args[0]));
		NSDictionary nTracks = (NSDictionary) rootDict.objectForKey("Tracks");
		NSObject[] nPlaylists = ((NSArray) rootDict.objectForKey("Playlists")).getArray();

		for (NSObject param : nPlaylists) {
			if (((NSDictionary) param).objectForKey("Distinguished Kind") != null
					|| ((NSDictionary) param).objectForKey("Master") != null
					|| ((NSDictionary) param).objectForKey("Folder") != null) {
				continue;
			}

			String playlistName = ((NSDictionary) param).objectForKey("Name").toString();

			if (isBypassPlaylist(bypassPlaylists, playlistName)) {
				logger.info("Playlist found and bypassed: \"" + playlistName + "\"");
				continue;
			}

			logger.info("Playlist found: \"" + playlistName + "\"");
			Playlist playlist = new Playlist(loggerContext, playlistName, args[1] + "/" + playlistName + ".m3u");

			NSObject[] nPlaylistItems = ((NSArray) ((NSDictionary) param).objectForKey("Playlist Items")).getArray();

			for (NSObject nPlaylistItem : nPlaylistItems) {
				logger.debug("Track ID " + ((NSDictionary) nPlaylistItem).objectForKey("Track ID"));

				String trackID = ((NSDictionary) nPlaylistItem).objectForKey("Track ID").toString();
				NSObject name = ((NSDictionary) nTracks.objectForKey(trackID)).objectForKey("Name");
				NSObject artist = ((NSDictionary) nTracks.objectForKey(trackID)).objectForKey("Artist");
				NSObject album = ((NSDictionary) nTracks.objectForKey(trackID)).objectForKey("Album");
				NSObject location = ((NSDictionary) nTracks.objectForKey(trackID)).objectForKey("Location");

				playlist.add(name == null ? "" : name.toString(), artist == null ? "unknownArtist" : artist.toString(),
						album == null ? "unknownAlbum" : album.toString(), location == null ? "" : location.toString());
			}

			playlists.add(playlist);
		}

		Track track = null;
		File target = null;

		for (Playlist playlist : playlists) {
			logger.info(playlist.getName() + " include " + playlist.getLength() + " track(s).");
			FileWriterWithEncoding m3uWriter = new FileWriterWithEncoding(playlist.getFile(),
					java.nio.charset.StandardCharsets.UTF_8.toString());
			m3uWriter.write("#EXTM3U\n");
			Iterator<Track> it = playlist.iterator();

			while (it.hasNext()) {
				track = it.next();
				String targetPath = args[1] + "/" + track.getArtist().replaceAll("[\\/:*?\"<>|]", "") + "/"
						+ track.getAlbum().replaceAll("[\\/:*?\"<>|]", "") + "/"
						+ track.getFilename().replaceAll("[\\/:*?\"<>|]", "");

				target = new File(targetPath);

				try {
					if (target.exists()) {
						logger.debug(
								"Skip copy from \"" + track.getFile().toPath() + "\" to \"" + target.toPath() + "\"");
					} else {
						FileUtils.copyFile(track.getFile(), target);
						logger.debug("Copy from \"" + track.getFile().toPath() + "\" to \"" + target.toPath() + "\"");
					}
				} catch (FileNotFoundException e) {
					logger.warn("File not found \"" + track.getFile().toPath() + "\"");
					continue;
				}

				m3uWriter.write(track.getArtist() + "/" + track.getAlbum() + "/" + track.getFilename() + "\n");
			}
			m3uWriter.close();
		}

		logger.info(Main.class.getSimpleName() + " Stop.");

		System.exit(0);
	}
}
