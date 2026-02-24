package com.example.drive_kit.View;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.drive_kit.Model.Car;
import com.example.drive_kit.Model.Driver;
import com.example.drive_kit.R;
import com.example.drive_kit.ViewModel.ProfileViewModel;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * ProfileActivity displays the user's profile information.
 * Shows personal details + car info + car image.
 * <p>
 * Image priority:
 * 1) car.carImageBase64 (new)
 * 2) car.carImageUri (old - backward compatibility)
 */
public class ProfileActivity extends BaseLoggedInActivity {

    private ShapeableImageView profileAvatar;

    private TextView firstNameText;
    private TextView lastNameText;
    private TextView emailValue;
    private TextView phoneValue;

    private TextView carNumberValue;
    private TextView manufacturerValue;
    private TextView modelValue;
    private TextView yearValue;

    private TextView insuranceDateValue;
    private TextView testDateValue;
    private TextView treatmentDateValue;

    private Button editProfileButton;

    private ProfileViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bindViews();
        setupViewModel();
        setupEditButton();
        loadProfileOrExit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh after returning from EditProfileActivity so new image shows immediately
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && viewModel != null) {
            viewModel.loadProfile(user.getUid());
        }
    }

    private void bindViews() {
        profileAvatar = findViewById(R.id.profileAvatar);

        firstNameText = findViewById(R.id.firstNameText);
        //lastNameText = findViewById(R.id.lastNameText);
        emailValue = findViewById(R.id.emailValue);
        phoneValue = findViewById(R.id.phoneValue);

        carNumberValue = findViewById(R.id.carNumberValue);
        manufacturerValue = findViewById(R.id.manufacturerValue);
        modelValue = findViewById(R.id.modelValue);
        yearValue = findViewById(R.id.yearValue);

        insuranceDateValue = findViewById(R.id.insuranceDateValue);
        testDateValue = findViewById(R.id.testDateValue);
        treatmentDateValue = findViewById(R.id.treatmentDateValue);

        editProfileButton = findViewById(R.id.editProfileButton);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        viewModel.getDriver().observe(this, d -> {
            if (d == null) return;

            firstNameText.setText(safe(d.getFirstName()));
            //lastNameText.setText(safe(d.getLastName()));
            emailValue.setText(safe(d.getEmail()));
            phoneValue.setText(safe(d.getPhone()));

            Car car = d.getCar();
            if (car != null) {
                carNumberValue.setText(safe(car.getCarNum()));

                String manufacturerText = (car.getCarModel() == null) ? "-" : car.getCarModel().name();
                manufacturerValue.setText(safe(manufacturerText));

                modelValue.setText(safe(car.getCarSpecificModel()));

                int y = car.getYear();
                yearValue.setText(y > 0 ? String.valueOf(y) : "-");
            } else {
                carNumberValue.setText("-");
                manufacturerValue.setText("-");
                modelValue.setText("-");
                yearValue.setText("-");
            }

            insuranceDateValue.setText(safe(d.getFormattedInsuranceDate()));
            testDateValue.setText(safe(d.getFormattedTestDate()));
            treatmentDateValue.setText(safe(d.getFormattedTreatDate()));

            loadAvatarFromDriver(d);
        });

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

    /**
     * Loads avatar with priority:
     * 1) car.carImageBase64 (new)
     * 2) car.carImageUri (old fallback)
     */
    private void loadAvatarFromDriver(Driver d) {
        if (d == null || d.getCar() == null) {
            profileAvatar.setImageResource(R.drawable.ic_profile_placeholder);
            return;
        }

        Car car = d.getCar();

        // 1) NEW: Base64
        String base64 = null;
        try {
            // IMPORTANT: this method must exist in your Car model
            base64 = car.getCarImageBase64();
        } catch (Exception ignored) {
        }

        if (base64 != null && !base64.trim().isEmpty()) {
            try {
                // support "data:image/jpeg;base64,...."
                String clean = base64;
                int comma = clean.indexOf(',');
                if (comma >= 0) clean = clean.substring(comma + 1);

                byte[] bytes = Base64.decode(clean, Base64.DEFAULT);

                Glide.with(this)
                        .load(bytes)
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .error(R.drawable.ic_profile_placeholder)
                        .centerCrop()
                        .into(profileAvatar);

                return;
            } catch (Exception ignored) {
                // fall back to URI
            }
        }

        // 2) OLD: Uri
        String uri = null;
        try {
            uri = car.getCarImageUri();
        } catch (Exception ignored) {
        }

        if (uri == null || uri.trim().isEmpty()) {
            profileAvatar.setImageResource(R.drawable.ic_profile_placeholder);
            return;
        }

        Glide.with(this)
                .load(uri)
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
