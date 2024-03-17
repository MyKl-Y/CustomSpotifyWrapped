package com.example.spotifywrapped;

import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import androidx.activity.ComponentActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.List;

public class SummaryPlayerActivity extends FragmentActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user = auth.getCurrentUser();
    private ViewPager2 viewPager;
    private StoryPagerAdapter storyPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary_player);

        viewPager = findViewById(R.id.viewPager);
        storyPagerAdapter = new StoryPagerAdapter(this);

        String documentId = getIntent().getStringExtra("documentId");
        Log.d("DataCheck", "Document ID: " + documentId);

        fetchDataAndSetupStories(documentId);
    }

    private void fetchDataAndSetupStories(String documentId) {
        db.collection("users").document(user.getUid())
                .collection("data").document(documentId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        SpotifyDataModel data = task.getResult().toObject(SpotifyDataModel.class);
                        storyPagerAdapter.setData(data);
                        viewPager.setAdapter(storyPagerAdapter);
                    } else {
                        Log.d("DataCheck", "Error fetching data", task.getException());
                    }
                });
    }
}
