package com.driver;


import java.util.*;

import org.springframework.stereotype.Repository;


@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Artist, List<Song>> artistSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap;
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap;
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;
    public HashMap<Artist, List<User>> artistLikeMap;

    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

    public SpotifyRepository() {
        //To avoid hitting apis multiple times, initialize all the hashmaps here with some dummy data
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        artistSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();
        artistLikeMap = new HashMap<>();

        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();
    }

    public User createUser(String name, String mobile) {
        User user = new User(name, mobile);
        users.add(user);

        userPlaylistMap.put(user, new ArrayList<>());

        return user;
    }

    private Optional<User> findUser(String mobile) {
        for (User user : users)
            if (user.getMobile().equals(mobile))
                return Optional.of(user);

        return Optional.empty();
    }

    public Artist createArtist(String name) {
        Artist artist = new Artist(name);
        artists.add(artist);
        artistAlbumMap.put(artist, new ArrayList<>());
        artistSongMap.put(artist, new ArrayList<>());
        artistLikeMap.put(artist, new ArrayList<>());

        return artist;
    }

    private Optional<Artist> findArtist(String artistName) {
        for (Artist artist : artists)
            if (artist.getName().equals(artistName))
                return Optional.of(artist);

        return Optional.empty();
    }

    public Album createAlbum(String title, String artistName) {
        Optional<Artist> optionalArtist = findArtist(artistName);
        if (optionalArtist.isEmpty())
            optionalArtist = Optional.of(createArtist(artistName));
        Artist artist = optionalArtist.get();

        Album album = new Album(title);
        albums.add(album);

        artistAlbumMap.get(artist).add(album);

        albumSongMap.put(album, new ArrayList<>());

        return album;
    }

    public Optional<Album> findAlbum(String albumName) {
        for (Album album : albums)
            if (album.getTitle().equals(albumName))
                return Optional.of(album);

        return Optional.empty();
    }

    public Song createSong(String title, String albumName, int length) throws Exception {
        Album album = findAlbum(albumName)
                .orElseThrow(() -> new Exception("Album does not exist"));

        Song song = new Song(title, length);
        songs.add(song);

        albumSongMap.get(album).add(song);

        for (Artist artist : artistAlbumMap.keySet())
            if (artistAlbumMap.get(artist).contains(album)) {
                artistSongMap.get(artist).add(song);
                break;
            }

        songLikeMap.put(song, new ArrayList<>());

        return song;
    }

    private Optional<Song> findSong(String songTitle) {
        for (Song song : songs)
            if (song.getTitle().equals(songTitle))
                return Optional.of(song);

        return Optional.empty();
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        User user = findUser(mobile)
                .orElseThrow(() -> new Exception("User does not exist"));

        Playlist playlist = new Playlist(title);
        playlists.add(playlist);

        List<Song> songList = new ArrayList<>();
        for (Song song : songs)
            if (song.getLength() == length)
                songList.add(song);
        playlistSongMap.put(playlist, songList);

        creatorPlaylistMap.put(user, playlist);

        List<User> listenerList = new ArrayList<>();
        listenerList.add(user);
        playlistListenerMap.put(playlist, listenerList);

        userPlaylistMap.get(user).add(playlist);

        return playlist;
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        User user = findUser(mobile)
                .orElseThrow(() -> new Exception("User does not exist"));

        Playlist playlist = new Playlist(title);
        playlists.add(playlist);

        List<Song> songList = new ArrayList<>();
        for (Song song : songs)
            if (songTitles.contains(song.getTitle()))
                    songList.add(song);
        playlistSongMap.put(playlist, songList);

        creatorPlaylistMap.put(user, playlist);

        List<User> listenerList = new ArrayList<>();
        listenerList.add(user);
        playlistListenerMap.put(playlist, listenerList);

        userPlaylistMap.get(user).add(playlist);

        return playlist;
    }

    private Optional<Playlist> findPlaylistByTitle(String playlistTitle) {
        for (Playlist playlist : playlists)
            if (playlist.getTitle().equals(playlistTitle))
                return Optional.of(playlist);

        return Optional.empty();
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        User user = findUser(mobile)
            .orElseThrow(() -> new Exception("User does not exist"));

        Playlist playlist = findPlaylistByTitle(playlistTitle)
            .orElseThrow(() -> new Exception("Playlist does not exist"));

        if ((creatorPlaylistMap.containsKey(user) && creatorPlaylistMap.get(user) == playlist) || playlistListenerMap.get(playlist).contains(user))
            return playlist;

        playlistListenerMap.get(playlist).add(user);

        if (!userPlaylistMap.get(user).contains(playlist))
            userPlaylistMap.get(user).add(playlist);

        return playlist;
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        User user = findUser(mobile)
                .orElseThrow(() -> new Exception("User does not exist"));

        Song song = findSong(songTitle)
                .orElseThrow(() -> new Exception("Song does not exist"));

        if (songLikeMap.get(song).contains(user))
            return song;

        song.setLikes(song.getLikes() + 1);
        songLikeMap.get(song).add(user);

        for (Artist artist : artistSongMap.keySet())
            if (artistSongMap.get(artist).contains(song)) {
                artist.setLikes(artist.getLikes() + 1);
                artistLikeMap.get(artist).add(user);
                break;
            }

        return song;
    }

    public String mostPopularArtist() {
        int likes = 0;
        String artistName = "";

        for (Artist artist : artists) {
            if (artist.getLikes() > likes) {
                likes = artist.getLikes();
                artistName = artist.getName();
            }
        }

        return artistName;
    }

    public String mostPopularSong() {
        int likes = 0;
        String songName = "";

        for (Song song : songs) {
            if (song.getLikes() > likes) {
                likes = song.getLikes();
                songName = song.getTitle();
            }
        }

        return songName;
    }
}