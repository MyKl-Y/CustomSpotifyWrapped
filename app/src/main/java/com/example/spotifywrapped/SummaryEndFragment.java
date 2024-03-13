package com.example.spotifywrapped;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.spotifywrapped.databinding.SummaryEndBinding;

import java.util.ArrayList;

public class SummaryEndFragment extends Fragment {
    private SummaryEndBinding binding;

    private ArrayList<String> artists;
    private ArrayList<String> songs;
    private ArrayList<String> genres;
    private ArrayList<String> images;

    public SummaryEndFragment() {

    }

    public static SummaryEndFragment newInstance(SpotifyDataModel data) {
        SummaryEndFragment fragment = new SummaryEndFragment();
        Bundle args = new Bundle();
        args.putStringArrayList("artists", (ArrayList<String>) data.topArtists);
        args.putStringArrayList("songs", (ArrayList<String>) data.topSongs);
        args.putStringArrayList("genres", (ArrayList<String>) data.topGenres);
        args.putStringArrayList("images", (ArrayList<String>) data.songImages);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            artists = getArguments().getStringArrayList("artists");
            songs = getArguments().getStringArrayList("songs");
            genres = getArguments().getStringArrayList("genres");
            images = getArguments().getStringArrayList("images");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = SummaryEndBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.summaryArtist1TextView.setText("1. " + artists.get(0));
        binding.summaryArtist2TextView.setText("2. " + artists.get(1));
        binding.summaryArtist3TextView.setText("3. " + artists.get(2));
        binding.summaryArtist4TextView.setText("4. " + artists.get(3));
        binding.summaryArtist5TextView.setText("5. " + artists.get(4));

        binding.summarySong1TextView.setText("1. " + songs.get(0));
        binding.summarySong2TextView.setText("2. " + songs.get(1));
        binding.summarySong3TextView.setText("3. " + songs.get(2));
        binding.summarySong4TextView.setText("4. " + songs.get(3));
        binding.summarySong5TextView.setText("5. " + songs.get(4));

        String genre = "";
        for (int i = 0; i < genres.size(); i++) {
            if (i != genres.size() - 1) {
                genre = genre.concat(genres.get(i) + ", ");
            } else {
                genre = genre.concat(genres.get(i));
            }
        }

        binding.summaryGenresTextView.setText(capitalizeString(genre));

        if (images != null && images.size() > 0) {
            Glide.with(this)
                    .load(images.get(0)) // Load the first image URL
                    .into(binding.summaryImageView); // Set it to artistImageView
        }
    }

    public static String capitalizeString(String string) {
        char[] chars = string.toLowerCase().toCharArray();
        boolean found = false;
        for (int i = 0; i < chars.length; i++) {
            if (!found && Character.isLetter(chars[i])) {
                chars[i] = Character.toUpperCase(chars[i]);
                found = true;
            } else if (Character.isWhitespace(chars[i]) || chars[i]=='.' || chars[i]=='\'') { // You can add other chars here
                found = false;
            }
        }
        return String.valueOf(chars);
    }
}
