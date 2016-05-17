package com.ibys.ilx2m3u;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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

		if (args.length != 2) {
			System.out.println("Usage: ilx2m3u <iTunesMusicLibraryXml> <outputPath>");
			System.out.println(args.length);
			System.exit(1);
		}
		Path iTunesMusicLibraryXml = Paths.get(args[0]);
		Path outputPath = Paths.get(args[1]);

		System.setProperty("logFilename", Main.class.getSimpleName());

		factory = XmlConfigurationFactory.getInstance();
		configurationSource = new ConfigurationSource(new FileInputStream(new File("conf/log4j2.xml")));
		configuration = factory.getConfiguration(configurationSource);
		loggerContext = new LoggerContext(Main.class.getSimpleName());
		loggerContext.start(configuration);

		Logger logger = loggerContext.getLogger(Main.class.getName());
		logger.info(Main.class.getSimpleName() + " starting..");
		logger.info("iTunes music library xml=" + iTunesMusicLibraryXml);
		logger.info("Outupt path=" + outputPath);

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

		// Parsing iTunes Music Library XML file
		NSDictionary rootDict = (NSDictionary) PropertyListParser.parse(iTunesMusicLibraryXml.toFile());
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

			Playlist playlist = new Playlist(loggerContext, playlistName);

			NSObject[] nPlaylistItems = ((NSArray) ((NSDictionary) param).objectForKey("Playlist Items")).getArray();

			for (NSObject nPlaylistItem : nPlaylistItems) {
				String trackID = ((NSDictionary) nPlaylistItem).objectForKey("Track ID").toString();
				logger.debug("Track ID" + trackID);

				NSObject name = ((NSDictionary) nTracks.objectForKey(trackID)).objectForKey("Name");
				NSObject artist = ((NSDictionary) nTracks.objectForKey(trackID)).objectForKey("Artist");
				NSObject album = ((NSDictionary) nTracks.objectForKey(trackID)).objectForKey("Album");
				NSObject location = ((NSDictionary) nTracks.objectForKey(trackID)).objectForKey("Location");

				playlist.add(name == null ? "unknownTitle" : name.toString(),
						artist == null ? "unknownArtist" : artist.toString(),
						album == null ? "unknownAlbum" : album.toString(), location == null ? "" : location.toString());
			}

			// Generate m3u playlist and copy track files
			playlist.genM3u(outputPath.toString());
		}

		logger.info(Main.class.getSimpleName() + " Stop.");

		System.exit(0);
	}
}
