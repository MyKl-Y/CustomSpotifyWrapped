package com.example.spotifywrapped;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.spotifywrapped.databinding.LargeLanguageModelBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.image.CreateImageRequest;
import com.theokanning.openai.service.OpenAiService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LargeLanguageModelFragment extends Fragment {
    private LargeLanguageModelBinding binding;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user = auth.getCurrentUser();

    private String llmText;
    private String llmImage;

    private ArrayList<String> artists;
    private ArrayList<String> songs;
    private ArrayList<String> genres;
    private boolean hasLLM;

    public LargeLanguageModelFragment() {

    }

    public static LargeLanguageModelFragment newInstance(SpotifyDataModel data) {
        LargeLanguageModelFragment fragment = new LargeLanguageModelFragment();
        Bundle args = new Bundle();
        args.putStringArrayList("artists", (ArrayList<String>) data.topArtists);
        args.putStringArrayList("songs", (ArrayList<String>) data.topSongs);
        args.putStringArrayList("genres", (ArrayList<String>) data.topGenres);
        args.putString("llmText", data.llmText);
        args.putString("llmImage", data.llmImage);
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
            if (getArguments().getString("llmText").length() > 1) {
                llmText = getArguments().getString("llmText");
                llmImage = getArguments().getString("llmImage");
                hasLLM = true;
            } else {
                hasLLM = false;
            }
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

        //if (hasLLM) {
            binding.llmTextView.setText(llmText);
            //if (llmImage != null && llmImage.length() > 0) {
            //    Glide.with(this)
            //            .load(llmImage) // Load the first image URL
            //            .into(binding.llmImageView); // Set it to artistImageView
            //}
        //} else {
        //    Log.e("LLM", "No LLM Info Available");
        //}
    }
}
