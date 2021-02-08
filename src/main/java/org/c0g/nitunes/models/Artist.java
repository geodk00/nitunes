package org.c0g.nitunes.models;

public class Artist {
    private int artistId;
    private String Name;

    public Artist(int artistId, String name) {
        this.artistId = artistId;
        Name = name;
    }

    public int getArtistId() {
        return artistId;
    }

    public void setArtistId(int artistId) {
        this.artistId = artistId;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }
}
