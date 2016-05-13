package com.ibys.ilx2m3u;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;

public class Playlist implements Iterable<Track> {
	private Logger logger;
	private String name;
	private File file;

	LinkedList<Track> list = new LinkedList<Track>();

	public Playlist(LoggerContext loggerContext, String name, String path) {
		logger = loggerContext.getLogger(Playlist.class.getName());
		this.name = name;
		this.file = new File(path);
	}

	public boolean add(String name, String artist, String album, String location)
			throws UnsupportedEncodingException, URISyntaxException {
		logger.debug("Add Track - name=\"" + name + "\" artist=\"" + artist + "\" album=\"" + album + "\" location=\""
				+ location + "\"");
		return list.add(new Track(name, artist, album, location));
	}

	public boolean add(Track t) {
		return list.add(t);
	}

	public String toString() {
		String string = new String(this.name + ": ");
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
		// return new InnerIterator();
		return list.iterator();
	}

	public File getFile() {
		return file;
	}

	public int getLength() {
		return list.size();
	}
}
