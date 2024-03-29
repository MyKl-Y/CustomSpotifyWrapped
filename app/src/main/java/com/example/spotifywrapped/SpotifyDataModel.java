package com.example.spotifywrapped;

import java.util.List;

public class SpotifyDataModel {
    public String documentId;
    public String dateTime;
    public String type;
    public String timeRange;
    public List<String> topArtists;
    public List<String> topSongs;
    public List<String> topGenres;
    public List<String> artistImages;
    public List<String> songImages;
    public List<String> artistIds;
    public List<String> songIds;

    // Default constructor required for Firestore data mapping
    public SpotifyDataModel() {}

    // Getters and setters (if needed)
}

