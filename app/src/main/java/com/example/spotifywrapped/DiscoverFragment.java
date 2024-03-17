package com.example.spotifywrapped;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.spotifywrapped.databinding.FragmentDiscoverBinding;

public class DiscoverFragment extends Fragment {
    FragmentDiscoverBinding mBinding;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        mBinding = FragmentDiscoverBinding.inflate(inflater, container, false);

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBinding.testNotificationButton.setOnClickListener(v -> {
            NotificationReceiver receiver = new NotificationReceiver();
            receiver.onReceive(getContext(), new Intent());
        });

    }
}
