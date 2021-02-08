package org.c0g.nitunes.models;

public class Track {
    private int trackId;
    private String name;
    private String artistName;
    private String albumName;
    private String genre;

    public Track(int trackId, String name, String artistName, String albumName) {
        this.trackId = trackId;
        this.name = name;
        this.artistName = artistName;
        this.albumName = albumName;
    }

    public Track(int trackId, String name, String artistName, String albumName, String genre) {
        this.trackId = trackId;
        this.name = name;
        this.artistName = artistName;
        this.albumName = albumName;
        this.genre = genre;
    }

    public int getTrackId() {
        return trackId;
    }

    public void setTrackId(int trackId) {
        this.trackId = trackId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }
}
