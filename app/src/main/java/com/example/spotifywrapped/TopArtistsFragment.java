package com.example.spotifywrapped;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.spotifywrapped.databinding.SummaryStartBinding;
import com.example.spotifywrapped.databinding.TopArtistsBinding;

public class TopArtistsFragment extends Fragment {
    private TopArtistsBinding binding;

    private String[] artists;

    public TopArtistsFragment() {

    }

    public static TopArtistsFragment newInstance(SpotifyDataModel data) {
        TopArtistsFragment fragment = new TopArtistsFragment();
        Bundle args = new Bundle();
        args.putStringArray("artists", (String[]) data.topArtists.toArray());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            artists = getArguments().getStringArray("artists");
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

        binding.artist1TextView.setText("1. " + artists[0]);
        binding.artist2TextView.setText("2. " + artists[1]);
        binding.artist3TextView.setText("3. " + artists[2]);
        binding.artist4TextView.setText("4. " + artists[3]);
        binding.artist5TextView.setText("5. " + artists[4]);
    }
}
