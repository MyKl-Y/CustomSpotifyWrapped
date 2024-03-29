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

    private ArrayList<String> artists;

    public TopGenresFragment() {

    }

    public static TopGenresFragment newInstance(SpotifyDataModel data) {
        TopGenresFragment fragment = new TopGenresFragment();
        Bundle args = new Bundle();
        args.putStringArrayList("genres", (ArrayList<String>) data.topGenres);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            artists = getArguments().getStringArrayList("genres");
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

       binding.genre1TextView.setText("1. " + artists.get(0));
       binding.genre2TextView.setText("2. " + artists.get(1));
       binding.genre3TextView.setText("3. " + artists.get(2));
       binding.genre4TextView.setText("4. " + artists.get(3));
       binding.genre5TextView.setText("5. " + artists.get(4));
    }

}
