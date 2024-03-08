package com.example.spotifywrapped;

import java.util.List;

public class SpotifyDataModel {
    public String documentId;
    public String type;
    public String timeRange;
    public List<String> topArtists;
    public List<String> topSongs;
    public List<String> topGenres;

    // Default constructor required for Firestore data mapping
    public SpotifyDataModel() {}

    // Getters and setters (if needed)
}

