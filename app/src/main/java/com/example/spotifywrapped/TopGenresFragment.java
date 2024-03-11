package com.example.spotifywrapped;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.spotifywrapped.databinding.TopGenresBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class TopGenresFragment extends Fragment {
    private TopGenresBinding binding;

    private String[] artists;

    public TopGenresFragment() {

    }

    public static TopGenresFragment newInstance(SpotifyDataModel data) {
        TopGenresFragment fragment = new TopGenresFragment();
        Bundle args = new Bundle();
        args.putStringArray("genres", (String[]) data.topGenres.toArray());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            artists = getArguments().getStringArray("genres");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = TopGenresBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Check if artists array is not null or empty to avoid ArrayIndexOutOfBoundsException
        if (artists == null || artists.length == 0) {
            // Handle case where there are no genres
            return;
        }

        // Create a list of indices from the artists array
        ArrayList<Integer> indices = new ArrayList<>();
        for (int i = 0; i < artists.length; i++) {
            indices.add(i);
        }

        // Shuffle the list to randomize the order
        Collections.shuffle(indices);

        // Now, you can safely pick the first five unique indices from the shuffled list
        // Make sure your artists array has at least 5 elements to avoid IndexOutOfBoundsException
        int numberOfGenresToShow = Math.min(artists.length, 5); // Ensure you don't go out of bounds
        if (numberOfGenresToShow >= 1) binding.genre1TextView.setText("1. " + artists[indices.get(0)]);
        if (numberOfGenresToShow >= 2) binding.genre2TextView.setText("2. " + artists[indices.get(1)]);
        if (numberOfGenresToShow >= 3) binding.genre3TextView.setText("3. " + artists[indices.get(2)]);
        if (numberOfGenresToShow >= 4) binding.genre4TextView.setText("4. " + artists[indices.get(3)]);
        if (numberOfGenresToShow >= 5) binding.genre5TextView.setText("5. " + artists[indices.get(4)]);
    }

}
