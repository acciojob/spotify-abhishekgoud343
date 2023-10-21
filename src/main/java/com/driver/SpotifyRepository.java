package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;


@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
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

        return user;
    }

    private User findUser(String mobile) {
        for (User user : users)
            if (user.getMobile().equals(mobile))
                return user;

        return null;
    }

    public Artist createArtist(String name) {
        Artist artist = new Artist(name);
        artists.add(artist);

        return artist;
    }

    private Artist findArtist(String artistName) {
        for (Artist artist : artists)
            if (artist.getName().equals(artistName))
                return artist;

        return null;
    }

    public Album createAlbum(String title, String artistName) {
        Artist artist = findArtist(artistName);

        if (artist == null) {
            artist = createArtist(artistName);
            artists.add(artist);
        }

        Album album = new Album(title);
        albums.add(album);

        List<Album> albumList = artistAlbumMap.getOrDefault(artist, new ArrayList<>());
        albumList.add(album);

        artistAlbumMap.put(artist, albumList);

        return album;
    }

    public Album findALbum(String albumName) {
        for (Album album : albums)
            if (album.getTitle().equals(albumName))
                return album;

        return null;
    }

    public Song createSong(String title, String albumName, int length) throws Exception {
        Album album = findALbum(albumName);
        if (album == null)
            throw new Exception("Album does not exist");

        Song song = new Song(title, length);
        songs.add(song);

        List<Song> songList = albumSongMap.getOrDefault(album, new ArrayList<>());
        songList.add(song);
        albumSongMap.put(album, songList);

        return song;
    }

    private Song findSong(String songTitle) {
        for (Song song : songs)
            if (song.getTitle().equals(songTitle))
                return song;

        return null;
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        User user = findUser(mobile);
        if (user == null)
            throw new Exception("User does not exist");

        Playlist playlist = new Playlist(title);
        playlists.add(playlist);

        List<Song> songList = playlistSongMap.getOrDefault(playlist, new ArrayList<>());
        for (Song song : songs)
            if (song.getLength() == length)
                songList.add(song);

        playlistSongMap.put(playlist, songList);

        creatorPlaylistMap.put(user, playlist);

        List<User> listenerList = new ArrayList<>();
        listenerList.add(user);
        playlistListenerMap.put(playlist, listenerList);

        List<Playlist> playlistList = userPlaylistMap.getOrDefault(user, new ArrayList<>());
        playlistList.add(playlist);
        userPlaylistMap.put(user, playlistList);

        return playlist;
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        User user = findUser(mobile);
        if (user == null)
            throw new Exception("User does not exist");

        Playlist playlist = new Playlist(title);
        playlists.add(playlist);

        List<Song> songList = playlistSongMap.getOrDefault(playlist, new ArrayList<>());
        for (Song song : songs)
            for (String songTitle : songTitles)
                if (song.getTitle().equals(songTitle))
                    songList.add(song);

        playlistSongMap.put(playlist, songList);

        creatorPlaylistMap.put(user, playlist);

        List<User> listenerList = new ArrayList<>();
        listenerList.add(user);
        playlistListenerMap.put(playlist, listenerList);

        List<Playlist> playlistList = userPlaylistMap.getOrDefault(user, new ArrayList<>());
        playlistList.add(playlist);
        userPlaylistMap.put(user, playlistList);

        return playlist;
    }

    private Playlist findPlaylistByTitle(String playlistTitle) {
        for (Playlist playlist : playlists)
            if (playlist.getTitle().equals(playlistTitle))
                return playlist;

        return null;
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        User user = findUser(mobile);
        if (user == null)
            throw new Exception("User does not exist");

        Playlist playlist = findPlaylistByTitle(playlistTitle);
        if (playlist == null)
            throw new Exception("Playlist does not exist");

        if (!playlistListenerMap.containsKey(playlist) || !playlistListenerMap.get(playlist).contains(user)) {
            List<User> listenerList = playlistListenerMap.getOrDefault(playlist, new ArrayList<>());
            listenerList.add(user);
            playlistListenerMap.put(playlist, listenerList);
        }

        return playlist;
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        User user = findUser(mobile);
        if (user == null)
            throw new Exception("User does not exist");

        Song song = findSong(songTitle);
        if (song == null)
            throw new Exception("Song does not exist");

        if (!songLikeMap.containsKey(song) || !songLikeMap.get(song).contains(user)) {
            List<User> userList = songLikeMap.getOrDefault(song, new ArrayList<>());
            userList.add(user);
            songLikeMap.put(song, userList);

            //find the artist corresponding to the song
            Album album = null;
            for (Album a : albumSongMap.keySet())
                if (albumSongMap.get(a).contains(song)) {
                    album = a;
                    break;
                }

            Artist artist = null;
            for (Artist a : artistAlbumMap.keySet())
                if (artistAlbumMap.get(a).contains(album)) {
                    artist = a;
                    break;
                }

            userList = artistLikeMap.getOrDefault(artist, new ArrayList<>());
            userList.add(user);
            artistLikeMap.put(artist, userList);
        }

        return song;
    }

    public String mostPopularArtist() {
        int likes = 0;
        String artistName = "";

        for (Artist artist : artistLikeMap.keySet()) {
            int size = artistLikeMap.get(artist).size();
            if (size > likes) {
                likes = size;
                artistName = artist.getName();
            }
        }

        return artistName;
    }

    public String mostPopularSong() {
        int likes = 0;
        String songName = "";

        for (Song song : songLikeMap.keySet()) {
            int size = songLikeMap.get(song).size();
            if (size > likes) {
                likes = size;
                songName = song.getTitle();
            }
        }

        return songName;
    }
}