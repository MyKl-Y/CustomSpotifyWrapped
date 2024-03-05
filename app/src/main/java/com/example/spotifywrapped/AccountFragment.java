package com.example.spotifywrapped;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.spotifywrapped.databinding.FragmentAccountBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class AccountFragment extends Fragment {

    private FragmentAccountBinding mBinding;

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user = auth.getCurrentUser();

    private String oldEmail, oldPass;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        mBinding = FragmentAccountBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}
