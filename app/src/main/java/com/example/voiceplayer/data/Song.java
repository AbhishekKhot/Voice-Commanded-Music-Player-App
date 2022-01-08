package com.example.voiceplayer.data;

import android.net.Uri;

public class Song {
    long id;
    Uri uri;
    String name;
    int duration;
    int size;
    long albumId;
    Uri albumImageUri;

    public Song(long id, Uri uri, String name, int duration, int size, long albumId, Uri albumImageUri) {
        this.id = id;
        this.uri = uri;
        this.name = name;
        this.duration = duration;
        this.size = size;
        this.albumId = albumId;
        this.albumImageUri = albumImageUri;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }

    public Uri getAlbumImageUri() {
        return albumImageUri;
    }

    public void setAlbumImageUri(Uri albumImageUri) {
        this.albumImageUri = albumImageUri;
    }
}
