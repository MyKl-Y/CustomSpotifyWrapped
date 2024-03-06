package com.example.spotifywrapped;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.spotifywrapped.databinding.FragmentAccountBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AccountFragment extends Fragment {

    private FragmentAccountBinding mBinding;

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user = auth.getCurrentUser();

    private String oldEmail, oldPass;

    public static final String CLIENT_ID = MainActivity.tokens.getValue("Spotify Client ID");

    public static final String CLIENT_SECRET = MainActivity.tokens.getValue("Spotify Client Secret");

    public static final String REDIRECT_URI = MainActivity.tokens.getValue("Spotify Redirect URI");

    public static final int AUTH_TOKEN_REQUEST_CODE = 0;
    public static final int AUTH_CODE_REQUEST_CODE = 1;

    private final OkHttpClient mOkHttpClient = new OkHttpClient();
    private String mAccessToken, mAccessCode, mRefreshToken;
    private Call mCall;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ScheduledExecutorService scheduler;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        mBinding = FragmentAccountBinding.inflate(inflater, container, false);
        getUserProfile();
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getUserProfile();

        String userId = user.getUid(); // Get the current user's UID

        db.collection("users")
                .document(userId) // Use the UID to directly reference the document
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                Boolean isLinked = document.getBoolean("isLinked"); // Assuming "isLinked" is your field
                                if (Boolean.TRUE.equals(isLinked)) {
                                    // The account is linked
                                    // Update your UI or logic accordingly
                                    mBinding.linkSpotify.setVisibility(View.GONE);
                                    mBinding.unlinkSpotify.setVisibility(View.VISIBLE);
                                    mAccessToken = document.getString("token");
                                    getUserProfile();
                                } else {
                                    // The account is not linked or the field is missing/false
                                    // Update your UI or logic accordingly
                                    mBinding.linkSpotify.setVisibility(View.VISIBLE);
                                    mBinding.unlinkSpotify.setVisibility(View.GONE);
                                    mAccessToken = document.getString("token");
                                    getUserProfile();
                                }
                            } else {
                                Log.d("Firestore", "No such document");
                                // Handle the case where the document does not exist
                                // Update your UI or logic accordingly
                                mBinding.linkSpotify.setVisibility(View.VISIBLE);
                                mBinding.unlinkSpotify.setVisibility(View.GONE);
                                mAccessToken = document.getString("token");
                                getUserProfile();
                            }
                        } else {
                            Log.d("Firestore", "get failed with ", task.getException());
                            // Handle the failure
                        }
                    }
                });

        ActivityResultLauncher<Intent> authActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Handle the response
                        final AuthorizationResponse response = AuthorizationClient.getResponse(result.getResultCode(), result.getData());
                        mAccessToken = response.getAccessToken();
                        // Update UI or perform further actions with the token

                        Map<String, Object> userObj = new HashMap<>();
                        userObj.put("isLinked", Boolean.TRUE);
                        userObj.put("token", mAccessToken);
                        db.collection("users").document(user.getUid())
                                .set(userObj)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void avoid) {
                                        Log.d("Firestore", "Account linked successfully.");
                                        mBinding.linkSpotify.setVisibility(View.GONE);
                                        mBinding.unlinkSpotify.setVisibility(View.VISIBLE);

                                        // Check if the scheduler is null or has been shut down, and then start it
                                        if (scheduler == null || scheduler.isShutdown()) {
                                            scheduler = Executors.newSingleThreadScheduledExecutor();
                                            scheduler.scheduleAtFixedRate(() -> refreshSpotifyToken(), 3500, 3500, TimeUnit.SECONDS);
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w("Firestore", "Error adding document", e);

                                    }
                                });
                    }
                    getUserProfile();
                });

        /*
        mBinding.linkSpotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection("users")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        if (document.getData().get(user.getUid()).equals(Boolean.TRUE)) {
                                            Map<String, Boolean> userObj = new HashMap<>();
                                            userObj.put(user.getUid(), Boolean.FALSE);
                                            db.collection("users")
                                                    .add(userObj)
                                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                        @Override
                                                        public void onSuccess(DocumentReference documentReference) {
                                                            Log.d("Firestore", "DocumentSnapshot added with ID: " + documentReference.getId());
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.w("Firestore", "Error adding document", e);

                                                        }
                                                    });
                                        } else {
                                            getToken(authActivityResultLauncher);
                                        }
                                    }
                                } else {
                                    Map<String, Boolean> userObj = new HashMap<>();
                                    userObj.put(user.getUid(), Boolean.FALSE);
                                    db.collection("users")
                                            .add(userObj)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    Log.d("Firestore", "DocumentSnapshot added with ID: " + documentReference.getId());
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.w("Firestore", "Error adding document", e);

                                                }
                                            });
                                }
                            }
                        });
            }
        });
         */
        mBinding.linkSpotify.setOnClickListener(v -> getToken(authActivityResultLauncher));
        mBinding.unlinkSpotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unlinkAccount();
            }
        });

        mBinding.signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle("Sign out?");

                builder.setPositiveButton("Yes", (dialog, which) -> {
                    auth.signOut();
                    NavHostFragment.findNavController(AccountFragment.this)
                            .navigate(R.id.action_AccountNavigation_to_EmailPasswordFragment);
                });
                builder.setNegativeButton("No", (dialog, which) -> dialog.cancel());

                builder.show();
            }
        });

        mBinding.updatePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle("Enter Old Email");

                final EditText emailInput = new EditText(requireContext());
                final EditText oldPassInput = new EditText(requireContext());
                final EditText newPassInput = new EditText(requireContext());
                emailInput.setInputType(InputType.TYPE_CLASS_TEXT);
                oldPassInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                newPassInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                builder.setView(emailInput);

                builder.setPositiveButton("Next", (dialog, which) -> {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(requireContext());
                    builder1.setTitle("Enter Old Password");
                    oldEmail = emailInput.getText().toString();
                    builder1.setView(oldPassInput);
                    builder1.setPositiveButton("Next", (dialog1, which1) -> {
                        AlertDialog.Builder builder2 = new AlertDialog.Builder(requireContext());
                        builder2.setTitle("New Password");
                        oldPass = oldPassInput.getText().toString();
                        builder2.setView(newPassInput);
                        builder2.setPositiveButton("Complete", (dialog2, which2) -> {
                            updateAccount("password", oldEmail, oldPass, newPassInput.getText().toString());
                            oldPass = newPassInput.getText().toString();
                        });
                        builder2.setNegativeButton("Cancel", (dialog2, which2) -> dialog2.cancel());

                        builder2.show();
                    });
                    builder1.setNegativeButton("Cancel", (dialog1, which1) -> dialog1.cancel());

                    builder1.show();
                });
                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

                builder.show();
            }
        });

        mBinding.updateEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle("Enter Old Email");

                final EditText emailInput = new EditText(requireContext());
                final EditText oldPassInput = new EditText(requireContext());
                final EditText newPassInput = new EditText(requireContext());
                emailInput.setInputType(InputType.TYPE_CLASS_TEXT);
                oldPassInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                newPassInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                builder.setView(emailInput);

                builder.setPositiveButton("Next", (dialog, which) -> {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(requireContext());
                    builder1.setTitle("Enter Old Password");
                    oldEmail = emailInput.getText().toString();
                    builder1.setView(oldPassInput);
                    builder1.setPositiveButton("Next", (dialog1, which1) -> {
                        AlertDialog.Builder builder2 = new AlertDialog.Builder(requireContext());
                        builder2.setTitle("New Email");
                        oldPass = oldPassInput.getText().toString();
                        builder2.setView(newPassInput);
                        builder2.setPositiveButton("Complete", (dialog2, which2) -> {
                            updateAccount("email", oldEmail, oldPass, newPassInput.getText().toString());
                            oldEmail = newPassInput.getText().toString();
                        });
                        builder2.setNegativeButton("Cancel", (dialog2, which2) -> dialog2.cancel());

                        builder2.show();
                    });
                    builder1.setNegativeButton("Cancel", (dialog1, which1) -> dialog1.cancel());

                    builder1.show();
                });
                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

                builder.show();
            }
        });

        mBinding.verifyEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBinding.verifyEmailButton.setEnabled(false);

                user.sendEmailVerification()
                        .addOnCompleteListener(requireActivity(), new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                mBinding.verifyEmailButton.setEnabled(true);

                                if (task.isSuccessful()) {
                                    Toast.makeText(getContext(),
                                            "Verification email sent to " + user.getEmail(),
                                            Toast.LENGTH_SHORT).show();
                                    reload();
                                } else {
                                    Log.e("UpdateAccount", "sendEmailVerification", task.getException());
                                    Toast.makeText(getContext(),
                                            "Failed to send verification email.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        mBinding.resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBinding.resetPasswordButton.setEnabled(false);

                auth.sendPasswordResetEmail(user.getEmail())
                        .addOnCompleteListener(requireActivity(), new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                mBinding.verifyEmailButton.setEnabled(true);

                                if (task.isSuccessful()) {
                                    Toast.makeText(getContext(),
                                            "Password reset email sent to " + user.getEmail(),
                                            Toast.LENGTH_SHORT).show();
                                    reload();
                                } else {
                                    Log.e("UpdateAccount", "sendPasswordResetEmailVerification", task.getException());
                                    Toast.makeText(getContext(),
                                            "Failed to send password reset email.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        mBinding.deleteAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle("Are you sure you want to delete your account?");

                builder.setPositiveButton("Yes", (dialog, which) -> {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(requireContext());
                    builder1.setTitle("Are you sure you are sure?");

                    builder1.setPositiveButton("Yes", (dialog1, which1) -> {
                        user.delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d("DeleteAccount", "User account deleted.");
                                            Toast.makeText(getContext(),
                                                    "Account deleted, you will be missed :(.",
                                                    Toast.LENGTH_LONG).show();
                                            NavHostFragment.findNavController(AccountFragment.this)
                                                    .navigate(R.id.action_AccountNavigation_to_EmailPasswordFragment);
                                        }
                                    }
                                });
                    });
                    builder1.setNegativeButton("No", (dialog1, which1) -> dialog1.cancel());

                    builder1.show();
                });
                builder.setNegativeButton("No", (dialog, which) -> dialog.cancel());

                builder.show();
            }
        });

        mBinding.displayNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle("Enter New Display Name");

                final EditText displayNameInput = new EditText(requireContext());
                displayNameInput.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(displayNameInput);

                builder.setPositiveButton("Done", (dialog, which) -> {
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(displayNameInput.getText().toString())
                            .build();

                    user.updateProfile(profileUpdates)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        reload();
                                    }
                                }
                            });
                });
                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

                builder.show();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (user != null) {
            reload();
        }
    }

    private void updateAccount(String type, String email, String oldPass, String newVal) {
        AuthCredential credential = EmailAuthProvider
                .getCredential(email, oldPass);

        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            if (type.equals("password")) {
                                user.updatePassword(newVal).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d("UpdateAccount", "Password Updated");
                                        } else {
                                            Log.e("UpdateAccount", "Password not updated");
                                        }
                                    }
                                });
                            } else {
                                user.verifyBeforeUpdateEmail(newVal).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d("UpdateAccount", "Email Updated");
                                        } else {
                                            Log.e("UpdateAccount", "Email not updated");
                                        }
                                    }
                                });
                            }
                        } else {
                            Log.e("UpdateAccount", "Re-authentication failed");
                        }
                    }
                });
        reload();
    }

    private void getToken(ActivityResultLauncher<Intent> authActivityResultLauncher) {
        AuthorizationRequest request = getAuthenticationRequest(AuthorizationResponse.Type.TOKEN);
        Intent intent = AuthorizationClient.createLoginActivityIntent(getActivity(), request);
        authActivityResultLauncher.launch(intent);
    }

    private AuthorizationRequest getAuthenticationRequest(AuthorizationResponse.Type type) {
        return new AuthorizationRequest.Builder(CLIENT_ID, type, getRedirectUri().toString())
                .setShowDialog(false)
                .setScopes(new String[] { "user-read-email", "user-read-private", "user-top-read" }) // <--- Change the scope of your requested token here
                .setCampaign("your-campaign-token")
                .build();
    }

    private Uri getRedirectUri() {
        return Uri.parse(REDIRECT_URI);
    }

    private void refreshSpotifyToken() {
        OkHttpClient client = new OkHttpClient();

        // Spotify's token endpoint
        String tokenEndpoint = "https://accounts.spotify.com/api/token";

        // Prepare the request body with the refresh token and grant type
        RequestBody requestBody = new FormBody.Builder()
                .add("grant_type", "refresh_token")
                .add("refresh_token", mRefreshToken)
                .build();

        // Encode CLIENT_ID and CLIENT_SECRET
        String authValue = CLIENT_ID + ":" + CLIENT_SECRET;
        String base64AuthValue = Base64.encodeToString(authValue.getBytes(), Base64.NO_WRAP);

        // Prepare the request
        Request request = new Request.Builder()
                .url(tokenEndpoint)
                .post(requestBody)
                .addHeader("Authorization", "Basic " + base64AuthValue)
                .build();

        // Execute the request
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                // Log failure
                Log.e("SpotifyRefreshToken", "Failed to refresh token", e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = Objects.requireNonNull(response.body()).string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseBody);
                        mAccessToken = jsonObject.getString("access_token");
                        Map<String, Object> userObj = new HashMap<>();
                        userObj.put("token", mAccessToken);
                        db.collection("users").document(user.getUid())
                                .set(userObj)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void avoid) {
                                        Log.d("Firestore", "Access Token Updated.");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w("Firestore", "Error adding document", e);

                                    }
                                });
                        // Optionally, update the refresh token if provided
                        if (jsonObject.has("refresh_token")) {
                            mRefreshToken = jsonObject.getString("refresh_token");
                        }
                        // You might want to update the UI or retry the failed Spotify API call here
                    } catch (JSONException e) {
                        Log.e("SpotifyRefreshToken", "Failed to parse token refresh response", e);
                    }
                } else {
                    Log.e("SpotifyRefreshToken", "Token refresh was not successful. Response code: " + response.code());
                }
            }
        });
    }

    public void getUserProfile() {
        if (mAccessToken == null) {
            mBinding.spotifyAccountTextView.setText("Show linked Spotify Account!");
            return;
        }

        final Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me")
                .addHeader("Authorization", "Bearer " + mAccessToken)
                .build();

        cancelCall();
        mCall = mOkHttpClient.newCall(request);

        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Failed to fetch data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseData = response.body().string(); // Ensures only one call to .string()
                getActivity().runOnUiThread(() -> {
                    try {
                        JSONObject jsonObject = new JSONObject(responseData);
                        String displayName = jsonObject.optString("display_name", "N/A");
                        String email = jsonObject.optString("email", "N/A");
                        // Update UI to display username and email in a formatted way
                        String accountDetails = "Username: " + displayName + "\nEmail: " + email;
                        mBinding.spotifyAccountTextView.setText(accountDetails); // Make sure you have a TextView with this ID in your layout
                    } catch (JSONException e) {
                        Toast.makeText(getContext(), "Failed to parse data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    public void unlinkAccount() {
        Map<String, Object> userObj = new HashMap<>();
        userObj.put("isLinked", Boolean.FALSE);
        db.collection("users").document(user.getUid())
                .set(userObj)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firestore", "Account unlinked successfully.");
                        mBinding.linkSpotify.setVisibility(View.VISIBLE);
                        mBinding.unlinkSpotify.setVisibility(View.GONE);

                        // Optionally stop and shutdown the scheduler here if it's no longer needed
                        if (scheduler != null && !scheduler.isShutdown()) {
                            scheduler.shutdownNow();
                        }                            }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Firestore", "Error adding document", e);

                    }
                });
        getUserProfile();
    }

    private void cancelCall() {
        if (mCall != null) {
            mCall.cancel();
        }
    }

    private void reload() {
        auth.getCurrentUser().reload().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    updateUI(auth.getCurrentUser());
                    /*Toast.makeText(getContext(),
                            "Reload successful!",
                            Toast.LENGTH_SHORT).show();*/
                } else {
                    Log.e("AccountReload", "reload", task.getException());
                    /*Toast.makeText(getContext(),
                            "Failed to reload user.",
                            Toast.LENGTH_SHORT).show();*/
                }
            }
        });
        getUserProfile();
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            if (user.getDisplayName() != null) {
                mBinding.accountWelcomeHeader.setText("Welcome " + user.getDisplayName() + "!");
                mBinding.userName.setText(user.getDisplayName());
            } else {
                mBinding.accountWelcomeHeader.setText("Welcome User " + user.getUid());
                mBinding.userName.setText("Enter a Display Name!");
            }
            mBinding.userEmailTextView.setText(user.getEmail());

            if (user.isEmailVerified()) {
                mBinding.verifyEmailButton.setVisibility(View.GONE);
            } else {
                mBinding.verifyEmailButton.setVisibility(View.VISIBLE);
            }
        }
        getUserProfile();
    }

    @Override
    public void onDestroyView() {
        Map<String, Object> userObj = new HashMap<>();
        userObj.put("isLinked", Boolean.FALSE);
        db.collection("users").document(user.getUid())
                .set(userObj)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firestore", "Account unlinked successfully.");
                        // Optionally stop and shutdown the scheduler here if it's no longer needed
                        if (scheduler != null && !scheduler.isShutdown()) {
                            scheduler.shutdownNow();
                        }                            }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Firestore", "Error adding document", e);

                    }
                });
        super.onDestroyView();
        mBinding = null;
    }
}
