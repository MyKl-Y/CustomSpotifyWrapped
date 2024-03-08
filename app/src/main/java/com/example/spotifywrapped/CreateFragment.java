package com.example.spotifywrapped;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spotifywrapped.databinding.FragmentCreateBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CreateFragment extends Fragment {

    private FragmentCreateBinding mBinding;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user = auth.getCurrentUser();

    private final OkHttpClient mOkHttpClient = new OkHttpClient();
    private String accessToken;
    private Call call;

    private Map<String, Object> spotifyData = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        mBinding = FragmentCreateBinding.inflate(inflater, container, false);
        RecyclerView recyclerView = mBinding.getRoot().findViewById(R.id.pastWrapped_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        CreateAdapter adapter = new CreateAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
        fetchSpotifyDataAndUpdateAdapter();
        db.collection("users")
                .document(user.getUid()) // Use the UID to directly reference the document
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                accessToken = document.getString("token");
                            } else {
                                Log.d("Firestore", "No such document");
                                // Handle the case where the document does not exist
                                // Update your UI or logic accordingly
                            }
                        } else {
                            Log.d("Firestore", "get failed with ", task.getException());
                            // Handle the failure
                        }
                    }
                });
        return mBinding.getRoot();
    }

    public void fetchSpotifyDataAndUpdateAdapter() {
        db.collection("users").document(user.getUid()).collection("data")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<SpotifyDataModel> dataList = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            SpotifyDataModel data = document.toObject(SpotifyDataModel.class);
                            if (data != null) {
                                data.documentId = document.getId();
                                if (document.getId().substring(4,8).equals("0101")) {
                                    // New Years
                                    data.type = "New Years";
                                } else if (document.getId().substring(4,8).equals("1225")) {
                                    // Christmas
                                    data.type = "Christmas";
                                } else if (document.getId().substring(4,8).equals("1031")) {
                                    // Halloween
                                    data.type = "Halloween";
                                } else {
                                    // Base
                                    if (data.timeRange.equals("short_term")) {
                                        data.type = "1 Month";
                                    } else if (data.timeRange.equals("medium_term")) {
                                        data.type = "1 Year";
                                    } else if (data.timeRange.equals("long_term")) {
                                        data.type = "All Time";
                                    } else {
                                        data.type = data.timeRange;
                                    }
                                }
                                dataList.add(data);
                            }
                        }
                        // Assuming you have initialized your adapter and RecyclerView
                        getActivity().runOnUiThread(() -> {
                            ((CreateAdapter) mBinding.pastWrappedRecyclerView.getAdapter()).updateData(dataList);
                        });
                    } else {
                        Log.e("Firestore", "Error getting documents: ", task.getException());
                    }
                });
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBinding.createWrappedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] timeFrames = new String[]{"1 week", "1 month", "1 year", "All time"};
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle("Select Time Span");

                builder.setSingleChoiceItems(timeFrames, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int selectedPosition = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
                        String selectedTimeRange = "short_term"; // Default value
                        switch (selectedPosition) {
                            case 0:
                                selectedTimeRange = null;
                                break;
                            case 1:
                                selectedTimeRange = "short_term";
                                break;
                            case 2:
                                selectedTimeRange = "medium_term";
                                break;
                            case 3:
                                selectedTimeRange = "long_term";
                                break;
                        }
                        // Fetch and update UI after selection
                        getUserTopItems(selectedTimeRange);
                        mBinding.pastWrappedRecyclerView.invalidate();
                    }
                });

                builder.setNegativeButton("Cancel", null);

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    public void getUserTopItems(String timeRange) {
        if (accessToken == null) {
            return;
        }

        // Reset completedCalls for this operation
        completedCalls = 0;

        HashMap<String, Object> parent = new HashMap<>();
        HashMap<String, Object> updates = new HashMap<>();
        HashSet<String> genres = new HashSet<>();
//        updates.put("date", new Date());
        ArrayList<String> artistNames = new ArrayList<>();
        ArrayList<String> songs = new ArrayList<>();

        // Define the callback as a local variable for reuse
        Callback callback = new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Failed to fetch data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                // Consider also incrementing completedCalls here or setting a flag to avoid hanging if one call fails
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseData = response.body().string();
                getActivity().runOnUiThread(() -> {
                    try {
                        JSONObject jsonObject = new JSONObject(responseData);
                        JSONArray items = jsonObject.getJSONArray("items");

                        for (int i = 0; i < items.length(); i++) {
                            JSONObject itemObject = items.getJSONObject(i);
                            String name = itemObject.getString("name");

                            // Check if this was a call for artists or tracks
                            if (call.request().url().toString().contains("/top/artists")) {
                                artistNames.add(name);
                                JSONArray genresArray = itemObject.getJSONArray("genres");
                                for (int j = 0; j < genresArray.length(); j++) {
                                    genres.add(genresArray.getString(j));
                                }
                            } else {
                                songs.add(name);
                                // Assuming tracks don't directly give genres but leaving placeholder logic
                            }
                        }

                        synchronized (CreateFragment.this) {
                            completedCalls++;
                            if (completedCalls == 2) {
                                checkAndUpdateFirestore(updates, artistNames, songs, new ArrayList<>(genres), timeRange);
                                completedCalls = 0;
                            }
                        }
                    } catch (JSONException e) {
                        Toast.makeText(getContext(), "Failed to parse data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };

        // Artist request
        call = mOkHttpClient.newCall(new Request.Builder()
                .url("https://api.spotify.com/v1/me/top/artists?time_range=" + timeRange + "&limit=5")
                .addHeader("Authorization", "Bearer " + accessToken)
                .build());
        call.enqueue(callback);

        // Tracks request
        call = mOkHttpClient.newCall(new Request.Builder()
                .url("https://api.spotify.com/v1/me/top/tracks?time_range=" + timeRange + "&limit=5")
                .addHeader("Authorization", "Bearer " + accessToken)
                .build());
        call.enqueue(callback);
    }

    private void checkAndUpdateFirestore(HashMap<String, Object> updates, ArrayList<String> artistNames, ArrayList<String> songs, ArrayList<String> genres, String timeRange) {
        if (timeRange != null) {
            updates.put("timeRange", timeRange);
        } else {
            updates.put("timeRange", "1 Week");
        }
        updates.put("topArtists", artistNames);
        updates.put("topSongs", songs);
        updates.put("topGenres", genres);
        //parent.put(new Date().toString(), updates);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        String dateDocumentId = sdf.format(new Date());
        db.collection("users").document(user.getUid())
                .collection("data").document(dateDocumentId)
                .set(updates, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Data successfully written!"))
                .addOnFailureListener(e -> Log.w("Firestore", "Error writing document", e));
        fetchSpotifyDataAndUpdateAdapter();
    }

    private int completedCalls = 0;

    private void cancelCall() {
        if (call != null) {
            call.cancel();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
        cancelCall();
    }
}
