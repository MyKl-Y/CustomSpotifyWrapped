package com.example.spotifywrapped;

import android.os.Bundle;
import android.util.Log;
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
        Log.d("DataCheck", "Summary Start Args: " + data.dateTime);
        args.putString("date", data.dateTime);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("DataCheck", "Summary Start Create: " + getArguments().getString("date"));
        if (getArguments() != null) {
            if (getArguments().getString("date").equalsIgnoreCase("holiday")) {
                date = getArguments().getString("date");
            } else {
                //date = getArguments().getString("date");
                String temp = getArguments().getString("date");
                date = String
                        .format("%s/%s/%s",
                                temp.substring(4, 6),
                                temp.substring(6, 8),
                                temp.substring(0, 4));
            }
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

        binding.startTitleTextView.setText(date + " Wrapped");
    }
}
