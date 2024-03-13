package com.example.spotifywrapped;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.spotifywrapped.databinding.TopSongsBinding;

import java.util.ArrayList;


public class TopSongsFragment extends Fragment {
    private TopSongsBinding binding;

    private ArrayList<String> artists;
    private ArrayList<String> songImages;

    public TopSongsFragment() {

    }

    public static TopSongsFragment newInstance(SpotifyDataModel data) {
        TopSongsFragment fragment = new TopSongsFragment();
        Bundle args = new Bundle();
        args.putStringArrayList("songs", (ArrayList<String>) data.topSongs);
        args.putStringArrayList("songImages", (ArrayList<String>) data.songImages);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            artists = getArguments().getStringArrayList("songs");
            songImages = getArguments().getStringArrayList("songImages");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = TopSongsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.song1TextView.setText("1. " + artists.get(0));
        binding.song2TextView.setText("2. " + artists.get(1));
        binding.song3TextView.setText("3. " + artists.get(2));
        binding.song4TextView.setText("4. " + artists.get(3));
        binding.song5TextView.setText("5. " + artists.get(4));
        if (songImages != null && songImages.size() > 0) {
            Glide.with(this)
                    .load(songImages.get(0)) // Load the first image URL
                    .into(binding.song1ImageView); // Set it to artistImageView
            Glide.with(this)
                    .load(songImages.get(1)) // Load the first image URL
                    .into(binding.song2ImageView); // Set it to artistImageView
            Glide.with(this)
                    .load(songImages.get(2)) // Load the first image URL
                    .into(binding.song3ImageView); // Set it to artistImageView
            Glide.with(this)
                    .load(songImages.get(3)) // Load the first image URL
                    .into(binding.song4ImageView); // Set it to artistImageView
            Glide.with(this)
                    .load(songImages.get(4)) // Load the first image URL
                    .into(binding.song5ImageView); // Set it to artistImageView
        }
    }
}
