package com.ibys.ilx2m3u;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;

public class Playlist implements Iterable<Track> {
	private Logger logger;
	private String name;
	ArrayList<Track> list;

	public Playlist(LoggerContext loggerContext, String name) {
		logger = loggerContext.getLogger(Playlist.class.getName());
		this.name = name;
		list = new ArrayList<Track>();
	}

	public boolean add(String name, String artist, String album, String location)
			throws UnsupportedEncodingException, URISyntaxException {
		logger.debug("Add Track - name=\"{}\" artist=\"{}\" album=\"{}\" location=\"{}\"", name, artist, album,
				location);
		return list.add(new Track(name, artist, album, location));
	}

	public boolean add(Track t) {
		logger.debug("Add Track - name=\"{}\" artist=\"{}\" album=\"{}\" location=\"{}\"", t.getName(), t.getArtist(),
				t.getAlbum(), t.getPath());
		return list.add(t);
	}

	public String toString() {
		String string = this.name + ": ";
		Iterator<Track> it = list.iterator();
		while (it.hasNext()) {
			string += it.next() + (it.hasNext() ? ", " : "");
		}
		return string;
	}

	public String getName() {
		return name;
	}

	public Iterator<Track> iterator() {
		return list.iterator();
	}

	public void genM3u(String outputPath) throws IOException {
		Path m3uPath = Paths.get(outputPath, this.name + ".m3u");
		FileWriterWithEncoding m3uWriter = new FileWriterWithEncoding(m3uPath.toFile(),
				StandardCharsets.UTF_8.toString());
		String illegalFilenameCharacter = "[\\/:*?\"<>|]";
		long begin = new Date().getTime();

		m3uWriter.write("#EXTM3U\n");

		for (int i = 0; i < list.size(); i++) {
			Track track = list.get(i);
			String artist = track.getArtist().replaceAll(illegalFilenameCharacter, "");
			String album = track.getAlbum().replaceAll(illegalFilenameCharacter, "");
			String filename = track.getFilename().replaceAll(illegalFilenameCharacter, "");
			Path copyTo = Paths.get(outputPath, artist, album, filename);

			logger.info("Track [{}] \"{}\" path=\"{}", i + 1, track.getName(), track.getPath());

			if (!track.getFile().exists()) {
				logger.warn("File not found, track removed from playlist.");
				continue;
			} else if (copyTo.toFile().exists()) {
				logger.debug("File already exists, skip copy to \"" + copyTo + "\"");
			} else {
				FileUtils.copyFile(track.getFile(), copyTo.toFile());
				logger.debug("File copied to \"" + copyTo + "\"");
			}

			m3uWriter.write(artist + "/" + album + "/" + filename + "\n");
		}

		m3uWriter.close();
		long end = new Date().getTime();
		logger.info("m3u playlist generated. Path=\"" + m3uPath + "\"");
		logger.debug("Time used=" + (end - begin) + " millisecond(s).");
	}
}
