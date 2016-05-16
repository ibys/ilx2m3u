package com.ibys.ilx2m3u;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Track {
	private String name;
	private String artist;
	private String album;
	private Path path;
	private String filename;
	private File file = null;

	public Track(String name, String artist, String album, String path)
			throws UnsupportedEncodingException, URISyntaxException {
		this.name = name;
		this.artist = artist;
		this.album = album;
		this.path = Paths.get(new URI(path.replaceFirst("localhost", "")));
		this.file = this.path.toFile();
		this.filename = this.file.getName();
	}

	public String getArtist() {
		return artist;
	}

	public String getName() {
		return name;
	}

	public String getAlbum() {
		return album;
	}

	public Path getPath() {
		return path;
	}

	public String toString() {
		return name + " - " + artist + " <" + album + ">: \"" + path + "\"";
	}

	public String getFilename() {
		return filename;
	}

	public File getFile() {
		return file;
	}

}
