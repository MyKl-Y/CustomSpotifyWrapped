package com.example.spotifywrapped;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spotifywrapped.databinding.FragmentCreateBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CreateFragment extends Fragment {

    private FragmentCreateBinding mBinding;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user = auth.getCurrentUser();

    private List<List<Object>> spotifyData = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        mBinding = FragmentCreateBinding.inflate(inflater, container, false);
        RecyclerView recyclerView = mBinding.getRoot().findViewById(R.id.pastWrapped_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        db.collection("users")
                .document(user.getUid()) // Use the UID to directly reference the document
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                List<Object> dataTemp = new ArrayList();
                                dataTemp.add(document.getData().get("topArtists"));
                                dataTemp.add(document.getData().get("topGenres"));
                                dataTemp.add(document.getData().get("topSongs"));
                                spotifyData.add(dataTemp);
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
        recyclerView.setAdapter(new CreateAdapter(spotifyData));
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBinding.createWrappedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}
