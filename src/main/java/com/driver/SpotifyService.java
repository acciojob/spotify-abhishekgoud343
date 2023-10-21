package com.driver;

import java.util.*;

import org.springframework.stereotype.Service;


@Service
public class SpotifyService {

    //Auto-wire will not work in this case, no need to change this and add autowire

    SpotifyRepository spotifyRepository = new SpotifyRepository();

    public User createUser(String name, String mobile) {
        return spotifyRepository.createUser(name, mobile);
    }

    public Artist createArtist(String name) {
        return spotifyRepository.createArtist(name);
    }

    public Album createAlbum(String title, String artistName) {
        return spotifyRepository.createAlbum(title, artistName);
    }

    public Song createSong(String title, String albumName, int length) throws Exception {
        Album album = spotifyRepository.findALbum(albumName);
        if (album == null)
            throw new Exception("Album does not exist");

        return spotifyRepository.createSong(title, album, length);
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        User user = spotifyRepository.findUser(mobile);
        if (user == null)
            throw new Exception("User does not exist");

        return spotifyRepository.createPlaylistOnLength(user, title, length);
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        User user = spotifyRepository.findUser(mobile);
        if (user == null)
            throw new Exception("User does not exist");

        return spotifyRepository.createPlaylistOnName(user, title, songTitles);
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        User user = spotifyRepository.findUser(mobile);
        if (user == null)
            throw new Exception("User does not exist");

        Playlist playlist = spotifyRepository.findPlaylistByTitle(playlistTitle);
        if (playlist == null)
            throw new Exception("Playlist does not exist");

        return spotifyRepository.findPlaylist(user, playlist);
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        User user = spotifyRepository.findUser(mobile);
        if (user == null)
            throw new Exception("User does not exist");

        Song song = spotifyRepository.findSong(songTitle);
        if (song == null)
            throw new Exception("Song does not exist");

        return spotifyRepository.likeSong(user, song);
    }

    public String mostPopularArtist() {
        String artistName = spotifyRepository.mostPopularArtist();
        if (artistName.isEmpty())
            return "Artist db empty";

        return artistName;
    }

    public String mostPopularSong() {
        String songName = spotifyRepository.mostPopularSong();
        if (songName.isEmpty())
            return "Song db empty";

        return songName;
    }
}