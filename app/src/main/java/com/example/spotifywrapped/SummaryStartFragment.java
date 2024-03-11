package com.example.spotifywrapped;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.spotifywrapped.databinding.SummaryStartBinding;

public class SummaryStartFragment extends Fragment {
    private SummaryStartBinding binding;

    private String date;

    public SummaryStartFragment() {

    }

    public static SummaryStartFragment newInstance(SpotifyDataModel data) {
        SummaryStartFragment fragment = new SummaryStartFragment();
        Bundle args = new Bundle();
        args.putString("date", data.documentId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String temp = getArguments().getString("date");
            date = String
                    .format("%s/%s/%s",
                            temp.substring(4,6),
                            temp.substring(6, 8),
                            temp.substring(0,4));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = SummaryStartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.startTitleTextView.setText(date + "Wrapped");
    }
}
