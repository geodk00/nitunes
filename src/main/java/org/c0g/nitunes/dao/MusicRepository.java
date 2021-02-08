package org.c0g.nitunes.dao;

import org.c0g.nitunes.dao.exceptions.DataRepositoryError;
import org.c0g.nitunes.models.Artist;
import org.c0g.nitunes.models.Genre;
import org.c0g.nitunes.models.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/*
    Everything to do with artists/songs/genres goes here
    instead of making a very small one for each individual class

    Number of rows returned from the db and stuff like that
    is generally not checked and empty lists/nulls
    are returned instead and handled by the client classes.

    All methods throw DataRepositoryError on SQL errors
    (hiding the implementation detail of what kind of DB we're using?).
 */
@Component
public class MusicRepository implements IMusicRepository{
    Logger logger = LoggerFactory.getLogger(MusicRepository.class);

    public List<Artist> getRandomArtists() {
        final String sql =  "SELECT * FROM Artist ORDER BY RANDOM() LIMIT 5";
        List<Artist> artistList = new ArrayList<>();

        //using try-with-resources instead of closing manually.
        try (Connection connection = DriverManager.getConnection(ConnectionHelper.URL);
            PreparedStatement statement = connection.prepareStatement(sql)) {
            logger.info("Connected to DB");

            //using try-with-resources instead of closing manually.
            try (ResultSet result = statement.executeQuery()) {
                logger.info("Executed getRandomArtists-query");
                while(result.next()) {
                    artistList.add(new Artist(result.getInt("ArtistId"), result.getString("Name")));
                }
            }
        } catch (SQLException ex) {
            logger.error(ex.getLocalizedMessage());
            throw new DataRepositoryError();
        }
        return artistList;
    }

    public List<Genre> getRandomGenres() {
        final String sql =  "SELECT * FROM Genre ORDER BY RANDOM() LIMIT 5";
        List<Genre> artistList = new ArrayList<>();

        //using try-with-resources instead of closing manually.
        try (Connection connection = DriverManager.getConnection(ConnectionHelper.URL);
            PreparedStatement statement = connection.prepareStatement(sql)) {
            logger.info("Connected to DB");

            //using try-with-resources instead of closing manually.
            try (ResultSet result = statement.executeQuery()) {
                logger.info("Executed getRandomArtists-query");
                while (result.next()) {
                    artistList.add(new Genre(result.getInt("GenreId"), result.getString("Name")));
                }
            }
        } catch (SQLException ex) {
            logger.error(ex.getLocalizedMessage());
            throw new DataRepositoryError();
        }
        return artistList;
    }

    public List<Track> getRandomTracks() {
        final String sql =  "SELECT Track.TrackId, Track.Name, Album.Title, Artist.Name FROM Track " +
                            "JOIN Album ON Track.AlbumId = Album.AlbumId " +
                            "JOIN Artist on Album.ArtistId = Artist.ArtistId " +
                            "ORDER BY RANDOM() LIMIT 5";
        List<Track> trackList = new ArrayList<>();

        //using try-with-resources instead of closing manually.
        try (Connection connection = DriverManager.getConnection(ConnectionHelper.URL);
            PreparedStatement statement = connection.prepareStatement(sql)) {
            logger.info("Connected to DB");

            //using try-with-resources instead of closing manually.
            try (ResultSet result = statement.executeQuery()) {
                logger.info("Executed getRandomTracks-query");
                while (result.next()) {
                    logger.info("results: " + result.getInt(1) + " " + result.getString(2) + " " +
                            result.getString(4) + " " +result.getString(3));

                    trackList.add(new Track(result.getInt(1), result.getString(2),
                            result.getString(4), result.getString(3)));
                }
            }
        } catch (SQLException ex) {
            logger.error(ex.getLocalizedMessage());
            throw new DataRepositoryError();
        }
        return trackList;
    }

    public List<Track> searchTracks(String term) {
        final String sql =  "SELECT Track.TrackId, Track.Name, Album.Title, Artist.Name, Genre.Name FROM Track " +
                "JOIN Album ON Track.AlbumId = Album.AlbumId " +
                "JOIN Artist on Album.ArtistId = Artist.ArtistId " +
                "JOIN Genre ON Track.GenreId = Genre.GenreId " +
                "WHERE Track.Name LIKE ?";
        List<Track> trackList = new ArrayList<>();

        //Return empty list on empty/missing search term
        if (term == null || term.isEmpty())
            return trackList;

        //The prepared statement will not let us replace "%?%" in the statement, so add '%' to beginning and end of the
        //search term before adding it to the statement
        term = "%" + term + "%";

        //using try-with-resources instead of closing manually.
        try (Connection connection = DriverManager.getConnection(ConnectionHelper.URL);
            PreparedStatement statement = connection.prepareStatement(sql)) {
            logger.info("Connected to DB");
            statement.setString(1, term);

            //using try-with-resources instead of closing manually.
            try (ResultSet result = statement.executeQuery()) {
                logger.info("Executed SearchTracks-query");
                while (result.next()) {
                    logger.info("results: " + result.getInt(1) + " " + result.getString(2) + " " +
                            result.getString(4) + " " +result.getString(3));
                    trackList.add(new Track(result.getInt(1), result.getString(2),
                            result.getString(4), result.getString(3), result.getString(5)));
                }
            }
        } catch (SQLException ex) {
            logger.error(ex.getLocalizedMessage());
            throw new DataRepositoryError();
        }
        return trackList;
    }
}
