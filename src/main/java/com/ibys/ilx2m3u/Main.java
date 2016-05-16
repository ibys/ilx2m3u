package com.ibys.ilx2m3u;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
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
			Playlist playlist = new Playlist(loggerContext, playlistName, outputPath + "/" + playlistName + ".m3u");

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

			playlists.add(playlist);
		}

		// Generate m3u playlists and copy track files
		for (Playlist playlist : playlists) {
			logger.info("Playlist \"{}\" include {} track(s). Path=\"{}\"", playlist.getName(), playlist.getLength(),
					playlist.getPath());
			FileWriterWithEncoding m3uWriter = new FileWriterWithEncoding(playlist.getFile(),
					StandardCharsets.UTF_8.toString());
			m3uWriter.write("#EXTM3U\n");
			Iterator<Track> it = playlist.iterator();

			int i = 0;

			while (it.hasNext()) {
				Track track = it.next();
				String illegalFilenameCharacter = "[\\/:*?\"<>|]";
				String artist = track.getArtist().replaceAll(illegalFilenameCharacter, "");
				String album = track.getAlbum().replaceAll(illegalFilenameCharacter, "");
				String filename = track.getFilename().replaceAll(illegalFilenameCharacter, "");
				Path target = Paths.get(outputPath.toString(), artist, album, filename);

				logger.info("Track [{}] \"{}\" path=\"{}", ++i, track.getName(), track.getPath());

				if (!track.getFile().exists()) {
					logger.warn("File not found, track removed from playlist.");
					continue;
				} else if (target.toFile().exists()) {
					logger.debug("File already exists, skip copy to \"" + target + "\"");
				} else {
					FileUtils.copyFile(track.getFile(), target.toFile());
					logger.debug("File copied to \"" + target + "\"");
				}

				m3uWriter.write(artist + "/" + album + "/" + filename + "\n");
			}
			m3uWriter.close();
		}

		logger.info(Main.class.getSimpleName() + " Stop.");

		System.exit(0);
	}
}
