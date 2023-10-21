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

    private Optional<User> findUser(String mobile) {
        for (User user : users)
            if (user.getMobile().equals(mobile))
                return Optional.of(user);

        return Optional.empty();
    }

    public Artist createArtist(String name) {
        Artist artist = new Artist(name);
        artists.add(artist);

        return artist;
    }

    private Optional<Artist> findArtist(String artistName) {
        for (Artist artist : artists)
            if (artist.getName().equals(artistName))
                return Optional.of(artist);

        return Optional.empty();
    }

    public Album createAlbum(String title, String artistName) {
        Optional<Artist> artist = findArtist(artistName);

        if (artist.isEmpty()) {
            artist = Optional.of(createArtist(artistName));
            artists.add(artist.get());
        }

        Album album = new Album(title);
        albums.add(album);

        List<Album> albumList = artistAlbumMap.getOrDefault(artist.get(), new ArrayList<>());
        albumList.add(album);

        artistAlbumMap.put(artist.get(), albumList);

        return album;
    }

    public Optional<Album> findAlbum(String albumName) {
        for (Album album : albums)
            if (album.getTitle().equals(albumName))
                return Optional.of(album);

        return Optional.empty();
    }

    public Song createSong(String title, String albumName, int length) throws Exception {
        Optional<Album> album = findAlbum(albumName);
        if (album.isEmpty())
            throw new Exception("Album does not exist");

        Song song = new Song(title, length);
        songs.add(song);

        List<Song> songList = albumSongMap.getOrDefault(album.get(), new ArrayList<>());
        songList.add(song);
        albumSongMap.put(album.get(), songList);

        return song;
    }

    private Optional<Song> findSong(String songTitle) {
        for (Song song : songs)
            if (song.getTitle().equals(songTitle))
                return Optional.of(song);

        return Optional.empty();
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        Optional<User> user = findUser(mobile);
        if (user.isEmpty())
            throw new Exception("User does not exist");

        Playlist playlist = new Playlist(title);
        playlists.add(playlist);

        List<Song> songList = playlistSongMap.getOrDefault(playlist, new ArrayList<>());
        for (Song song : songs)
            if (song.getLength() == length)
                songList.add(song);

        playlistSongMap.put(playlist, songList);

        creatorPlaylistMap.put(user.get(), playlist);

        List<User> listenerList = new ArrayList<>();
        listenerList.add(user.get());
        playlistListenerMap.put(playlist, listenerList);

        List<Playlist> playlistList = userPlaylistMap.getOrDefault(user.get(), new ArrayList<>());
        playlistList.add(playlist);
        userPlaylistMap.put(user.get(), playlistList);

        return playlist;
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        Optional<User> user = findUser(mobile);
        if (user.isEmpty())
            throw new Exception("User does not exist");

        Playlist playlist = new Playlist(title);
        playlists.add(playlist);

        List<Song> songList = playlistSongMap.getOrDefault(playlist, new ArrayList<>());
        for (Song song : songs)
            for (String songTitle : songTitles)
                if (song.getTitle().equals(songTitle))
                    songList.add(song);

        playlistSongMap.put(playlist, songList);

        creatorPlaylistMap.put(user.get(), playlist);

        List<User> listenerList = new ArrayList<>();
        listenerList.add(user.get());
        playlistListenerMap.put(playlist, listenerList);

        List<Playlist> playlistList = userPlaylistMap.getOrDefault(user.get(), new ArrayList<>());
        playlistList.add(playlist);
        userPlaylistMap.put(user.get(), playlistList);

        return playlist;
    }

    private Optional<Playlist> findPlaylistByTitle(String playlistTitle) {
        for (Playlist playlist : playlists)
            if (playlist.getTitle().equals(playlistTitle))
                return Optional.of(playlist);

        return Optional.empty();
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        if (mobile == null || mobile.isEmpty())
            throw new Exception("User does not exist");

        if (playlistTitle == null || playlistTitle.isEmpty())
            throw new Exception("Playlist does not exist");

        Optional<User> user = findUser(mobile);
        if (user.isEmpty())
            throw new Exception("User does not exist");

        Optional<Playlist> playlist = findPlaylistByTitle(playlistTitle);
        if (playlist.isEmpty())
            throw new Exception("Playlist does not exist");

        playlistListenerMap.computeIfAbsent(playlist.get(), k -> new ArrayList<>()).add(user.get());

        return playlist.get();
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        Optional<User> user = findUser(mobile);
        if (user.isEmpty())
            throw new Exception("User does not exist");

        Optional<Song> song = findSong(songTitle);
        if (song.isEmpty())
            throw new Exception("Song does not exist");

        if (!songLikeMap.containsKey(song.get()) || !songLikeMap.get(song.get()).contains(user.get())) {
            List<User> userList = songLikeMap.getOrDefault(song.get(), new ArrayList<>());
            userList.add(user.get());
            songLikeMap.put(song.get(), userList);

            //find the artist corresponding to the song
            Optional<Album> album = Optional.empty();
            for (Album a : albumSongMap.keySet())
                if (albumSongMap.get(a).contains(song.get())) {
                    album = Optional.of(a);
                    break;
                }

            Optional<Artist> artist = Optional.empty();
            for (Artist a : artistAlbumMap.keySet())
                if (artistAlbumMap.get(a).contains(album.get())) {
                    artist = Optional.of(a);
                    break;
                }

            userList = artistLikeMap.getOrDefault(artist.get(), new ArrayList<>());
            userList.add(user.get());
            artistLikeMap.put(artist.get(), userList);
        }

        return song.get();
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