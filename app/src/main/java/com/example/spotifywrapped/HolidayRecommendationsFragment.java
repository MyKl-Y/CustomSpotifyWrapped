package com.example.spotifywrapped;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.spotifywrapped.databinding.HolidayRecommendationsBinding;

import java.util.ArrayList;

public class HolidayRecommendationsFragment extends Fragment {
    private HolidayRecommendationsBinding binding;

    private ArrayList<String> artists;
    private ArrayList<String> songs;
    private ArrayList<String> genres;
    private ArrayList<String> images;
    int index;

    public HolidayRecommendationsFragment() {

    }

    public static HolidayRecommendationsFragment newInstance(SpotifyDataModel data, int index) {
        HolidayRecommendationsFragment fragment = new HolidayRecommendationsFragment();
        Bundle args = new Bundle();
        args.putStringArrayList("artists", (ArrayList<String>) data.topArtists);
        args.putStringArrayList("songs", (ArrayList<String>) data.topSongs);
        args.putStringArrayList("genres", (ArrayList<String>) data.topGenres);
        args.putStringArrayList("images", (ArrayList<String>) data.songImages);
        args.putInt("index", index);
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
            index = getArguments().getInt("index");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = HolidayRecommendationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.albumTitleTextView.setText("Holiday Harmonies");
        binding.artistTextView.setText(artists.get(index));
        binding.popularityTextView.setText(genres.get(index));
        binding.songTitleTextView.setText(songs.get(index));

        if (images != null && images.size() > 0) {
            Glide.with(this)
                    .load(images.get(index)) // Load the first image URL
                    .into(binding.albumCoverImageView); // Set it to artistImageView
        }
    }
}
