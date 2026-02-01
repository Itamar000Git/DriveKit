package com.example.drive_kit.View;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.drive_kit.R;
import com.example.drive_kit.ViewModel.ProfileViewModel;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * ProfileActivity displays the user's profile information.
 *
 * This screen:
 * - Shows personal details and car information
 * - Observes profile data from ProfileViewModel
 * - Handles the case where the user is not logged in
 *
 * IMPORTANT:
 * This Activity only handles UI logic.
 * All data loading is done inside the ViewModel.
 */
public class ProfileActivity extends BaseLoggedInActivity {

    // Header avatar
    private ShapeableImageView profileAvatar;

    // TextViews for displaying profile information
    private TextView firstNameText;
    private TextView lastNameText;
    private TextView emailValue;
    private TextView phoneValue;

    // Car details
    private TextView carNumberValue;
    private TextView manufacturerValue;
    private TextView modelValue;
    private TextView yearValue;

    // Dates
    private TextView insuranceDateValue;
    private TextView testDateValue;
    private TextView treatmentDateValue;

    // Button for navigating to edit profile screen
    private Button editProfileButton;

    // ViewModel that provides the profile data
    private ProfileViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // NOTE:
        // If BaseLoggedInActivity doesn't set the content view by itself,
        // uncomment the following line:
        // setContentView(getContentLayoutId());

        bindViews();
        setupViewModel();
        setupEditButton();
        loadProfileOrExit();
    }

    private void bindViews() {
        // Header
        profileAvatar = findViewById(R.id.profileAvatar);

        // Personal
        firstNameText = findViewById(R.id.firstNameText);
        lastNameText = findViewById(R.id.lastNameText);
        emailValue = findViewById(R.id.emailValue);
        phoneValue = findViewById(R.id.phoneValue);

        // Car
        carNumberValue = findViewById(R.id.carNumberValue);
        manufacturerValue = findViewById(R.id.manufacturerValue);
        modelValue = findViewById(R.id.modelValue);
        yearValue = findViewById(R.id.yearValue);

        // Dates
        insuranceDateValue = findViewById(R.id.insuranceDateValue);
        testDateValue = findViewById(R.id.testDateValue);
        treatmentDateValue = findViewById(R.id.treatmentDateValue);

        // Action
        editProfileButton = findViewById(R.id.editProfileButton);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        // Observe the Driver LiveData
        viewModel.getDriver().observe(this, d -> {
            if (d == null) return;

            // Personal details
            firstNameText.setText(safe(d.getFirstName()));
            lastNameText.setText(safe(d.getLastName()));
            emailValue.setText(safe(d.getEmail()));
            phoneValue.setText(safe(d.getPhone()));

            // Car (getCar() should be non-null in your model)
            carNumberValue.setText(safe(d.getCar().getCarNum()));

            String manufacturerText = (d.getCar().getCarModel() == null)
                    ? "-"
                    : d.getCar().getCarModel().name();
            manufacturerValue.setText(safe(manufacturerText));

            modelValue.setText(safe(d.getCar().getCarSpecificModel()));

            int y = d.getCar().getYear();
            yearValue.setText(y > 0 ? String.valueOf(y) : "-");

            // Dates
            insuranceDateValue.setText(safe(d.getFormattedInsuranceDate()));
            testDateValue.setText(safe(d.getFormattedTestDate()));
            treatmentDateValue.setText(safe(d.getFormattedTreatDate()));

            // Avatar: load from Firestore field "carImageUri"
            loadAvatar(d.getCarImageUri());
        });

        // Observe error messages from the ViewModel
        viewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null && !msg.trim().isEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupEditButton() {
        editProfileButton.setOnClickListener(v -> {
            Intent i = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(i);
        });
    }

    private void loadProfileOrExit() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "משתמש לא מחובר", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        viewModel.loadProfile(user.getUid());
    }

    private void loadAvatar(String carImageUri) {
        // If empty -> show placeholder
        if (carImageUri == null || carImageUri.trim().isEmpty()) {
            profileAvatar.setImageResource(R.drawable.ic_profile_placeholder);
            return;
        }

        Glide.with(this)
                .load(carImageUri)
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_profile_placeholder)
                .centerCrop()
                .into(profileAvatar);
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.profile_activity;
    }

    private String safe(String s) {
        if (s == null || s.trim().isEmpty()) return "-";
        return s;
    }
}
