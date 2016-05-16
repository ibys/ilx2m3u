package com.ibys.ilx2m3u;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;

public class Playlist implements Iterable<Track> {
	private Logger logger;
	private String name;
	private Path path;
	private File file;

	LinkedList<Track> list = new LinkedList<Track>();

	public Playlist(LoggerContext loggerContext, String name, String location) {
		logger = loggerContext.getLogger(Playlist.class.getName());
		this.name = name;
		this.path = Paths.get(location);
		this.file = this.path.toFile();
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

	public Path getPath() {
		return path;
	}

	public File getFile() {
		return file;
	}

	public int getLength() {
		return list.size();
	}
}
