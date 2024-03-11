package com.example.spotifywrapped;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.spotifywrapped.databinding.TopSongsBinding;


public class TopSongsFragment extends Fragment {
    private TopSongsBinding binding;

    private String[] artists;

    public TopSongsFragment() {

    }

    public static TopSongsFragment newInstance(SpotifyDataModel data) {
        TopSongsFragment fragment = new TopSongsFragment();
        Bundle args = new Bundle();
        args.putStringArray("songs", (String[]) data.topSongs.toArray());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            artists = getArguments().getStringArray("songs");
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

        binding.song1TextView.setText("1. " + artists[0]);
        binding.song2TextView.setText("2. " + artists[1]);
        binding.song3TextView.setText("3. " + artists[2]);
        binding.song4TextView.setText("4. " + artists[3]);
        binding.song5TextView.setText("5. " + artists[4]);
    }
}
