package com.prashant.Bajao;

import java.util.ArrayList;

public class Song {
    long id;
    String title;
    String artist;
    String path;
    String iconpath;

    public Song(long id, String title, String artist, String path, String iconpath) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.path = path;
        this.iconpath=iconpath;
    }
    ArrayList<Song> songs = new ArrayList<>();
    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getPath() {
        return path;
    }

    public String getIconpath() {
        return iconpath;
    }

    public static ArrayList<Song> getSongs (){
        ArrayList<Song> songs = new ArrayList<>();
   return songs;
    }
}

