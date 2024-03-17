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
import java.util.Collections;
import java.util.Comparator;
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
                                } else if (document.getId().substring(4,6).equals("12")) {
                                    // Christmas
                                    data.type = "Holiday";
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

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM", Locale.getDefault());
        String date = sdf.format(new Date());

        if (date.substring(4,6).equals("12")) {
            db.collection("users").document(user.getUid()).collection("data")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                if (document != null && document.exists()) {
                                    if (document.getId().substring(0, 5).equals(date.substring(0,5)) && !(document.getString("timeRange").equalsIgnoreCase("Holiday"))) {
                                        mBinding.createHolidayButton.setVisibility(View.GONE);
                                        break;
                                    } else {
                                        mBinding.createHolidayButton.setVisibility(View.VISIBLE);
                                    }
                                }
                            }
                        } else {
                            Log.e("Firestore", "Error getting documents: ", task.getException());
                        }
                    });
        } else {
            mBinding.createHolidayButton.setVisibility(View.GONE);
        }

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
                        String selectedTimeRange = null; // Default value
                        switch (selectedPosition) {
                            case 0:
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

        mBinding.createHolidayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Fetch and update UI after selection
                getUserHoliday("Holiday");
                mBinding.pastWrappedRecyclerView.invalidate();
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
        HashMap<String, Integer> genreOccurrences = new HashMap<>();
        HashSet<String> trackGenres = new HashSet<>();
        ArrayList<String> finalGenres = new ArrayList<>();
//        updates.put("date", new Date());
        ArrayList<String> artistNames = new ArrayList<>();
        ArrayList<String> songs = new ArrayList<>();
        ArrayList<String> artistImages = new ArrayList<>();
        ArrayList<String> songImages = new ArrayList<>();
        ArrayList<String> artistIds = new ArrayList<>();
        ArrayList<String> songIds = new ArrayList<>();
        HashSet<String> songIdsHashSet = new HashSet<>();
        HashSet<String> artistIdsHashSet = new HashSet<>();
        HashSet<String> songsHashSet = new HashSet<>();
        HashSet<String> artistsHashSet = new HashSet<>();
        HashSet<String> artistImagesHashSet = new HashSet<>();
        HashSet<String> songImagesHashSet = new HashSet<>();
        Map<String, TrackDetails> trackDetailsMap = new HashMap<>();

        if (timeRange == null) {
            // 1 week, get based on recently played
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

                                // Check if this was a call for artists or tracks
                                if (call.request().url().toString().contains("/top/artists")) {
                                    JSONArray genresArray = itemObject.getJSONArray("genres");
                                    for (int j = 0; j < genresArray.length(); j++) {
                                        String genre = genresArray.getString(j);
                                        genreOccurrences.put(genre, genreOccurrences.getOrDefault(genre, 0) + 1);
                                    }
                                } else {
                                    JSONObject trackObject = itemObject.getJSONObject("track");
                                    JSONObject album = trackObject.getJSONObject("album");
                                    String trackName = trackObject.getString("name");
                                    JSONArray artistArrayOfObjects = trackObject.getJSONArray("artists");
                                    JSONObject artistObject = artistArrayOfObjects.getJSONObject(0);
                                    //JSONArray images = artistObject.getJSONArray("images");
                                    String artistName = artistObject.getString("name");
                                    //String artistImageUrl = images.getJSONObject(0).getString("url");
                                    String artistId = artistObject.getString("id");
                                    String trackId = trackObject.getString("id");
                                    JSONArray trackImages = album.getJSONArray("images");
                                    String trackImageUrl = trackImages.getJSONObject(0).getString("url");
                                    artistNames.add(artistName);
                                    artistImages.add(trackImageUrl);
                                    artistIds.add(artistId);
                                    songs.add(trackName);
                                    songImages.add(trackImageUrl);
                                    songIds.add(trackId);
                                    TrackDetails details = trackDetailsMap.getOrDefault(trackId, new TrackDetails(
                                            trackName,
                                            trackId,
                                            trackImageUrl,
                                            artistName,
                                            artistId,
                                            trackImageUrl
                                    ));
                                    details.increaseOccurrences();
                                    trackDetailsMap.put(trackId, details);
                                }
                            }

                            synchronized (CreateFragment.this) {
                                completedCalls++;
                                if (completedCalls == 2) {
                                    // Sort genres by occurrences
                                    List<Map.Entry<String, Integer>> list = new ArrayList<>(genreOccurrences.entrySet());
                                    list.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

                                    // Collect top genres based on occurrence, prioritizing tracks if needed
                                    int count = 0;
                                    for (Map.Entry<String, Integer> entry : list) {
                                        if (count < 5) {
                                            finalGenres.add(entry.getKey());
                                            count++;
                                        } else break;
                                    }

                                    // If less than 5 genres have more than one occurrence, add from tracks
                                    if (finalGenres.size() < 5) {
                                        for (String genre : trackGenres) {
                                            if (!finalGenres.contains(genre)) {
                                                finalGenres.add(genre);
                                                if (finalGenres.size() == 5) break;
                                            }
                                        }
                                    }

                                    // Now sort and select the top 5 tracks based on occurrences
                                    List<TrackDetails> sortedTracks = new ArrayList<>(trackDetailsMap.values());
                                    Collections.sort(sortedTracks, (t1, t2) -> Integer.compare(t2.getOccurrences(), t1.getOccurrences()));

                                    // Trim the list or fill with the most recent if less than 5
                                    List<TrackDetails> finalTrackList = sortedTracks.size() > 5 ? sortedTracks.subList(0, 5) : sortedTracks;
                                    // If less than 5, you might need to fetch more recent tracks or handle accordingly
                                    if (finalTrackList.size() < 5) {
                                        for (int i = 0; i < songs.size(); i++) {
                                            if (!finalTrackList.contains(songs.get(i))) {
                                                finalTrackList.add(new TrackDetails(
                                                        songs.get(i),
                                                        songIds.get(i),
                                                        songImages.get(i),
                                                        artistNames.get(i),
                                                        artistIds.get(i),
                                                        artistImages.get(i)
                                                ));
                                                if (finalTrackList.size() == 5) break;
                                            }
                                        }
                                        for (String genre : trackGenres) {
                                            if (!finalGenres.contains(genre)) {
                                                finalGenres.add(genre);
                                                if (finalGenres.size() == 5) break;
                                            }
                                        }
                                    }
                                    // Now update your genres, tracks, images, and artists lists based on finalTrackList
                                    songs.clear();
                                    songIds.clear();
                                    songImages.clear();
                                    artistNames.clear();
                                    artistIds.clear();
                                    artistImages.clear();
                                    for (TrackDetails detail : finalTrackList) {
                                        songs.add(detail.getTrackName());
                                        songIds.add(detail.getTrackId());
                                        songImages.add(detail.getTrackImage());
                                        artistsHashSet.add(detail.getArtistName());
                                        artistIds.add(detail.getArtistId());
                                        artistImages.add(detail.getArtistImage());
                                    }
                                    artistNames.addAll(artistsHashSet);

                                    checkAndUpdateFirestore(updates, artistNames, songs, finalGenres, artistImages, songImages, artistIds, songIds, timeRange);
                                    completedCalls = 0;
                                }
                            }
                        } catch (JSONException e) {
                            Toast.makeText(getContext(), "Failed to parse data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e("DataCheck", "Failed to parse data: " + e.getMessage());
                        }
                    });
                }
            };

            // Recently played request
            call = mOkHttpClient.newCall(new Request.Builder()
                    .url("https://api.spotify.com/v1/me/player/recently-played?limit=50")
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .build());
            call.enqueue(callback);

            // Artist request
            call = mOkHttpClient.newCall(new Request.Builder()
                    .url("https://api.spotify.com/v1/me/top/artists?time_range=short_term&limit=5")
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .build());
            call.enqueue(callback);
        } else {

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
                                String id = itemObject.getString("id");

                                // Check if this was a call for artists or tracks
                                if (call.request().url().toString().contains("/top/artists")) {
                                    JSONArray images = itemObject.getJSONArray("images");
                                    String imageUrl = images.getJSONObject(0).getString("url");
                                    artistNames.add(name);
                                    artistImages.add(imageUrl);
                                    artistIds.add(id);
                                    JSONArray genresArray = itemObject.getJSONArray("genres");
                                    for (int j = 0; j < genresArray.length(); j++) {
                                        String genre = genresArray.getString(j);
                                        genreOccurrences.put(genre, genreOccurrences.getOrDefault(genre, 0) + 1);
                                    }
                                } else {
                                    JSONObject album = itemObject.getJSONObject("album");
                                    JSONArray trackImages = album.getJSONArray("images");
                                    String trackImageUrl = trackImages.getJSONObject(0).getString("url");
                                    songs.add(name);
                                    songImages.add(trackImageUrl);
                                    songIds.add(id);
                                    // Assuming tracks don't directly give genres but leaving placeholder logic
                                }
                            }

                            synchronized (CreateFragment.this) {
                                completedCalls++;
                                if (completedCalls == 2) {
                                    // Sort genres by occurrences
                                    List<Map.Entry<String, Integer>> list = new ArrayList<>(genreOccurrences.entrySet());
                                    list.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

                                    // Collect top genres based on occurrence, prioritizing tracks if needed
                                    int count = 0;
                                    for (Map.Entry<String, Integer> entry : list) {
                                        if (count < 5) {
                                            finalGenres.add(entry.getKey());
                                            count++;
                                        } else break;
                                    }

                                    // If less than 5 genres have more than one occurrence, add from tracks
                                    if (finalGenres.size() < 5) {
                                        for (String genre : trackGenres) {
                                            if (!finalGenres.contains(genre)) {
                                                finalGenres.add(genre);
                                                if (finalGenres.size() == 5) break;
                                            }
                                        }
                                    }
                                    checkAndUpdateFirestore(updates, artistNames, songs, finalGenres, artistImages, songImages, artistIds, songIds, timeRange);
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
    }

    public void getUserHoliday(String type) {
        if (accessToken == null) {
            return;
        }

        // Reset completedCalls for this operation
        completedCalls = 0;

        HashMap<String, Object> updates = new HashMap<>();
        ArrayList<String> popularities = new ArrayList<>();
        ArrayList<String> artistNames = new ArrayList<>();
        ArrayList<String> songs = new ArrayList<>();
        ArrayList<String> artistImages = new ArrayList<>();
        ArrayList<String> songImages = new ArrayList<>();
        ArrayList<String> artistIds = new ArrayList<>();
        ArrayList<String> songIds = new ArrayList<>();
        ArrayList<String> artistTemp = new ArrayList<>();
        ArrayList<String> songTemp = new ArrayList<>();
        ArrayList<String> artistImageTemp = new ArrayList<>();
        ArrayList<String> songImageTemp = new ArrayList<>();
        ArrayList<String> artistIdTemp = new ArrayList<>();
        ArrayList<String> songIdTemp = new ArrayList<>();
        HashMap<Integer, Integer> popularityRatings = new HashMap<>();

        if (type.equals("Holiday")) {
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
                            JSONArray tracks = jsonObject.getJSONArray("tracks");

                            for (int i = 0; i < tracks.length(); i++) {
                                JSONObject trackObject = tracks.getJSONObject(i);
                                JSONObject album = trackObject.getJSONObject("album");
                                String trackName = trackObject.getString("name");
                                Integer popularity = (Integer) trackObject.getInt("popularity");
                                JSONObject artistObject = album.getJSONArray("artists").getJSONObject(0);
                                String artistName = artistObject.getString("name");
                                String artistId = artistObject.getString("id");
                                String trackId = trackObject.getString("id");
                                JSONArray trackImages = album.getJSONArray("images");
                                String trackImageUrl = trackImages.getJSONObject(0).getString("url");

                                songTemp.add(trackName);
                                songIdTemp.add(trackId);
                                songImageTemp.add(trackImageUrl);
                                popularityRatings.put(i, popularity);
                                artistTemp.add(artistName);
                                artistIdTemp.add(artistId);
                                artistImageTemp.add(trackImageUrl);
                            }

                            synchronized (CreateFragment.this) {
                                completedCalls++;
                                if (completedCalls == 1) {
                                    entriesSortedByValues(popularityRatings);
                                    for (int i = 0; i < 5; i++) {
                                        Map.Entry<Integer, Integer> entry = popularityRatings.entrySet().iterator().next();
                                        int key = entry.getKey();
                                        artistNames.add(artistTemp.get(key));
                                        artistIds.add(artistIdTemp.get(key));
                                        artistImages.add(artistImageTemp.get(key));
                                        popularities.add(entry.getValue().toString());
                                        songs.add(songTemp.get(key));
                                        songIds.add(songIdTemp.get(key));
                                        songImages.add(songImageTemp.get(key));
                                        popularityRatings.remove(key);
                                    }
                                    checkAndUpdateFirestore(updates, artistNames, songs, popularities, artistImages, songImages, artistIds, songIds, type);
                                    completedCalls = 0;
                                }
                            }
                        } catch (JSONException e) {
                            Toast.makeText(getContext(), "Failed to parse data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e("DataCheck", "Failed to parse holiday data: " + e.getMessage());
                        }
                    });
                }
            };

            // Recommendations request
            call = mOkHttpClient.newCall(new Request.Builder()
                    .url("https://api.spotify.com/v1/recommendations?seed_genres=" + type.toLowerCase() + "s&target_popularity=100")
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .build());
            call.enqueue(callback);
        }
    }

    static <K,V extends Comparable<? super V>>
    List<Map.Entry<K, V>> entriesSortedByValues(Map<K,V> map) {

        List<Map.Entry<K,V>> sortedEntries = new ArrayList<Map.Entry<K,V>>(map.entrySet());

        Collections.sort(sortedEntries,
                new Comparator<Map.Entry<K,V>>() {
                    @Override
                    public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {
                        return e2.getValue().compareTo(e1.getValue());
                    }
                }
        );

        return sortedEntries;
    }


    private void checkAndUpdateFirestore(
            HashMap<String, Object> updates,
            ArrayList<String> artistNames,
            ArrayList<String> songs,
            ArrayList<String> genres,
            ArrayList<String> artistImages,
            ArrayList<String> songImages,
            ArrayList<String> artistIds,
            ArrayList<String> songIds,
            String timeRange
    ) {
        if (timeRange != null) {
            updates.put("timeRange", timeRange);
        } else {
            updates.put("timeRange", "1 Week");
        }
        updates.put("topArtists", artistNames);
        updates.put("topSongs", songs);
        updates.put("topGenres", genres);
        updates.put("artistImages", artistImages);
        updates.put("songImages", songImages);
        updates.put("artistIds", artistIds);
        updates.put("songIds", songIds);
        //parent.put(new Date().toString(), updates);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        String dateDocumentId = sdf.format(new Date());
        if (timeRange.equals("Holiday")) {
            updates.put("dateTime", timeRange);
        } else {
            updates.put("dateTime", dateDocumentId);
        }
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

class TrackDetails {
    private String trackName;
    private String trackId;
    private String trackImage;
    private String artistName;
    private String artistId;
    private String artistImage;
    private int occurrences = 0;
    // Add other fields as needed, e.g., artistName, imageUrl

    public TrackDetails(String trackName, String trackId, String trackImage, String artistName, String artistId, String artistImage) {
        this.trackName = trackName;
        this.trackId = trackId;
        this.trackImage = trackImage;
        this.artistName = artistName;
        this.artistId = artistId;
        this.artistImage = artistImage;
    }

    public void increaseOccurrences() {
        this.occurrences++;
    }

    public int getOccurrences() {
        return occurrences;
    }

    // Add getters for other details
    public String getTrackName() {
        return trackName;
    }
    public String getTrackId() {
        return trackId;
    }
    public String getTrackImage() {
        return trackImage;
    }
    public String getArtistName() {
        return artistName;
    }
    public String getArtistId() {
        return artistId;
    }
    public String getArtistImage() {
        return artistImage;
    }
}
