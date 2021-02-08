package org.c0g.nitunes.dao;

import org.c0g.nitunes.models.Artist;
import org.c0g.nitunes.models.Genre;
import org.c0g.nitunes.models.Track;

import java.util.List;

public interface IMusicRepository {
    List<Artist> getRandomArtists();
    List<Genre> getRandomGenres();
    List<Track> getRandomTracks();
    List<Track> searchTracks(String term);
}
