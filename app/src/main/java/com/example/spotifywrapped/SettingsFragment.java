package com.example.spotifywrapped;

import android.app.ActionBar;
import android.app.UiModeManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        SwitchPreferenceCompat holidayNotificationPreference = findPreference("holiday_notification_enabled");
        if (holidayNotificationPreference != null) {
            holidayNotificationPreference.setOnPreferenceChangeListener(((preference, newValue) -> {
                boolean isEnabled = (Boolean) newValue;

                updateUserHolidayNotificationPreference(isEnabled);

                AlarmScheduler.cancelHolidayNotifications(getContext());

                return true;
            }));
        }

        ListPreference listPreference = findPreference("theme_switch");
        if (listPreference != null) {
            listPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                // Handle the preference change
                String newValueString = (String) newValue;
                UiModeManager uiModeManager = (UiModeManager) getContext().getSystemService(Context.UI_MODE_SERVICE);
                // Perform action based on new value
                switch (newValueString) {
                    case "1":
                        uiModeManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_AUTO);
                        break;
                    case "2":
                        uiModeManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_NO);
                        break;
                    case "3":
                        uiModeManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_YES);
                        break;
                }
                return true; // True to update the state of the preference with the new value
            });
        }
    }

    private void updateUserHolidayNotificationPreference(boolean isEnabled) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid())
                    .update("holidayNotificationsEnabled", isEnabled)
                    .addOnSuccessListener(aVoid -> Log.d("SettingsFragment", "User preference updated successfully"))
                    .addOnFailureListener(e -> Log.e("SettingsFragment", "Error updating user preference", e));
        }
    }
}