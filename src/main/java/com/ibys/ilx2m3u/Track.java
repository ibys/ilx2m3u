package com.ibys.ilx2m3u;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.apache.commons.io.FilenameUtils;

public class Track {
	private String name;
	private String artist;
	private String album;
	private String location;
	private String filename;
	private File file = null;

	public Track(String name, String artist, String album, String location) throws UnsupportedEncodingException {
		this.name = name;
		this.artist = artist;
		this.album = album;
		String tmp = location.replaceFirst("file://localhost/", "");
		tmp = URLDecoder.decode(tmp, java.nio.charset.StandardCharsets.UTF_8.toString());
		this.location = URLDecoder.decode(location.replace("+", "%2B").replaceFirst("file://localhost/", ""),
				java.nio.charset.StandardCharsets.UTF_8.toString());
		this.file = new File(this.location);
		this.filename = FilenameUtils.getName(this.location);
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

	public String getLocation() {
		return location;
	}

	public String toString() {
		return name + " - " + artist + " <" + album + ">: \"" + location + "\"";
	}

	public String getFilename() {
		return filename;
	}

	public File getFile() {
		return file;
	}

}
