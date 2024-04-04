package com.example.spotifywrapped;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.spotifywrapped.databinding.RecommendationsBinding;

import org.checkerframework.checker.units.qual.A;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecommendationsFragment extends Fragment {
    private RecommendationsBinding binding;

    private ArrayList<String> artists;
    private ArrayList<String> tracks;
    private ArrayList<String> images;

    public RecommendationsFragment() {

    }

    public static RecommendationsFragment newInstance(SpotifyDataModel data) {
        RecommendationsFragment fragment = new RecommendationsFragment();
        Bundle args = new Bundle();
        args.putStringArrayList("recommendedArtists", (ArrayList<String>) data.recommendations.get("artistNames"));
        args.putStringArrayList("recommendedTracks", (ArrayList<String>) data.recommendations.get("trackNames"));
        args.putStringArrayList("recommendedImages", (ArrayList<String>) data.recommendations.get("trackImages"));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            artists = getArguments().getStringArrayList("recommendedArtists");
            tracks = getArguments().getStringArrayList("recommendedTracks");
            images = getArguments().getStringArrayList("recommendedImages");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = RecommendationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.recSong1TextView.setText(tracks.get(0));
        binding.recSong2TextView.setText(tracks.get(1));
        binding.recSong3TextView.setText(tracks.get(2));
        binding.recSong4TextView.setText(tracks.get(3));
        binding.recSong5TextView.setText(tracks.get(4));
        binding.recArtist1TextView.setText(artists.get(0));
        binding.recArtist2TextView.setText(artists.get(1));
        binding.recArtist3TextView.setText(artists.get(2));
        binding.recArtist4TextView.setText(artists.get(3));
        binding.recArtist5TextView.setText(artists.get(4));
        if (images != null && images.size() > 0) {
            Glide.with(this)
                    .load(images.get(0)) // Load the first image URL
                    .into(binding.recSong1ImageView); // Set it to artistImageView
            Glide.with(this)
                    .load(images.get(1)) // Load the first image URL
                    .into(binding.recSong2ImageView); // Set it to artistImageView
            Glide.with(this)
                    .load(images.get(2)) // Load the first image URL
                    .into(binding.recSong3ImageView); // Set it to artistImageView
            Glide.with(this)
                    .load(images.get(3)) // Load the first image URL
                    .into(binding.recSong4ImageView); // Set it to artistImageView
            Glide.with(this)
                    .load(images.get(4)) // Load the first image URL
                    .into(binding.recSong5ImageView); // Set it to artistImageView
        }
    }
}
