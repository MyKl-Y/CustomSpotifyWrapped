package com.example.spotifywrapped;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.spotifywrapped.databinding.RecommendationsBinding;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.List;

public class RecommendationsFragment extends Fragment {
    private RecommendationsBinding binding;

    private ArrayList<String> artists;

    public RecommendationsFragment() {

    }

    public static RecommendationsFragment newInstance(SpotifyDataModel data) {
        RecommendationsFragment fragment = new RecommendationsFragment();
        Bundle args = new Bundle();
        args.putStringArrayList("artists", (ArrayList<String>) data.topArtists);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            artists = getArguments().getStringArrayList("artists");
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

        binding.textView2.setText("1. " + artists.get(0));
    }
}
