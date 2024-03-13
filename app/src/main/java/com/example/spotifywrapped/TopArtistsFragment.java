package com.example.spotifywrapped;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.spotifywrapped.databinding.SummaryStartBinding;
import com.example.spotifywrapped.databinding.TopArtistsBinding;

import java.util.ArrayList;

public class TopArtistsFragment extends Fragment {
    private TopArtistsBinding binding;

    private ArrayList<String> artists;
    private ArrayList<String> artistImages;

    public TopArtistsFragment() {

    }

    public static TopArtistsFragment newInstance(SpotifyDataModel data) {
        TopArtistsFragment fragment = new TopArtistsFragment();
        Bundle args = new Bundle();
        args.putStringArrayList("artists", (ArrayList<String>) data.topArtists);
        args.putStringArrayList("artistImages", (ArrayList<String>) data.artistImages);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            artists = getArguments().getStringArrayList("artists");
            artistImages = getArguments().getStringArrayList("artistImages");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = TopArtistsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.artist1TextView.setText("1. " + artists.get(0));
        binding.artist2TextView.setText("2. " + artists.get(1));
        binding.artist3TextView.setText("3. " + artists.get(2));
        binding.artist4TextView.setText("4. " + artists.get(3));
        binding.artist5TextView.setText("5. " + artists.get(4));
        // Check if artistImages is not null and has at least one URL
        if (artistImages != null && artistImages.size() > 0) {
            Glide.with(this)
                    .load(artistImages.get(0)) // Load the first image URL
                    .into(binding.artistImageView); // Set it to artistImageView
        }
    }
}
