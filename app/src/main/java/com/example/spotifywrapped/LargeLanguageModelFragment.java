package com.example.spotifywrapped;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.spotifywrapped.databinding.LargeLanguageModelBinding;

import java.util.ArrayList;

public class LargeLanguageModelFragment extends Fragment {
    private LargeLanguageModelBinding binding;

    private String llm;

    private ArrayList<String> artists;
    private ArrayList<String> songs;
    private ArrayList<String> genres;

    public LargeLanguageModelFragment() {

    }

    public static LargeLanguageModelFragment newInstance(SpotifyDataModel data) {
        LargeLanguageModelFragment fragment = new LargeLanguageModelFragment();
        Bundle args = new Bundle();
        args.putStringArrayList("artists", (ArrayList<String>) data.topArtists);
        args.putStringArrayList("songs", (ArrayList<String>) data.topSongs);
        args.putStringArrayList("genres", (ArrayList<String>) data.topGenres);
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
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = LargeLanguageModelBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.textView3.setText(llm);
    }
}
