package com.example.spotifywrapped;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.spotifywrapped.databinding.SummaryEndBinding;

public class SummaryEndFragment extends Fragment {
    private SummaryEndBinding binding;

    private String[] artists;
    private String[] songs;
    private String[] genres;

    public SummaryEndFragment() {

    }

    public static SummaryEndFragment newInstance(SpotifyDataModel data) {
        SummaryEndFragment fragment = new SummaryEndFragment();
        Bundle args = new Bundle();
        args.putStringArray("artists", (String[]) data.topArtists.toArray());
        args.putStringArray("songs", (String[]) data.topSongs.toArray());
        args.putStringArray("genres", (String[]) data.topGenres.toArray());
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
        binding = SummaryEndBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.summaryArtist1TextView.setText("1. " + artists[0]);
        binding.summaryArtist2TextView.setText("2. " + artists[1]);
        binding.summaryArtist3TextView.setText("3. " + artists[2]);
        binding.summaryArtist4TextView.setText("4. " + artists[3]);
        binding.summaryArtist5TextView.setText("5. " + artists[4]);

        binding.summarySong1TextView.setText("1. " + songs[0]);
        binding.summarySong2TextView.setText("2. " + songs[1]);
        binding.summarySong3TextView.setText("3. " + songs[2]);
        binding.summarySong4TextView.setText("4. " + songs[3]);
        binding.summarySong5TextView.setText("5. " + songs[4]);

        String genre = "";
        for (int i = 0; i < genres.length; i++) {
            if (i != genres.length - 1) {
                genre.concat(genres[i] + ", ");
            } else {
                genre.concat(genres[i]);
            }
        }

        binding.summaryGenresTextView.setText(genre);
    }
}
