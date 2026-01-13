package com.example.drive_kit.View;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.drive_kit.R;
import com.example.drive_kit.ViewModel.ProfileViewModel;
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

public class ProfileActivity extends AppCompatActivity {
    // TextViews for displaying profile information
    private TextView firstNameText;
    private TextView lastNameText;
    private TextView emailValue;
    private TextView phoneValue;
    private TextView carNumberValue;
    private TextView insuranceDateValue;
    private TextView testDateValue;
    private TextView treatmentDateValue;

    // Button for navigating to edit profile screen (currently unused)
    private Button editProfileButton;
    // ViewModel that provides the profile data
    private ProfileViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Standard Activity initialization
        super.onCreate(savedInstanceState);

        // Attach the profile_activity.xml layout to this Activity
        // After this line, all views defined in the layout exist in memory
        setContentView(R.layout.profile_activity);

        // Find and connect all TextViews from the XML layout
        firstNameText = findViewById(R.id.firstNameText);
        lastNameText = findViewById(R.id.lastNameText);
        emailValue = findViewById(R.id.emailValue);
        phoneValue = findViewById(R.id.phoneValue);
        carNumberValue = findViewById(R.id.carNumberValue);
        insuranceDateValue = findViewById(R.id.insuranceDateValue);
        testDateValue = findViewById(R.id.testDateValue);
        treatmentDateValue = findViewById(R.id.treatmentDateValue);

        // Find and connect the Edit Profile button
        editProfileButton = findViewById(R.id.editProfileButton);

        // Create (or retrieve) the ViewModel associated with this Activity
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        // Observe the Driver LiveData
        // When the profile data changes, update the UI fields
        viewModel.getDriver().observe(this, d -> {
            if (d == null) return;

            // Safely display user and car information
            firstNameText.setText(safe(d.getFirstName()));
            lastNameText.setText(safe(d.getLastName()));
            emailValue.setText(safe(d.getEmail()));
            phoneValue.setText(safe(d.getPhone()));
            carNumberValue.setText(safe(d.getCar().getCarNum()));

            // Display formatted dates
            insuranceDateValue.setText(safe(d.getFormattedInsuranceDate()));
            testDateValue.setText(safe(d.getFormattedTestDate()));
            treatmentDateValue.setText(safe(d.getFormattedTreatDate()));
        });

        // Observe error messages from the ViewModel
        // If an error occurs, show a Toast message
        viewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        // Get the currently logged-in Firebase user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // If there is no logged-in user, show an error and close this screen
        if (user == null) {
            Toast.makeText(this, "משתמש לא מחובר", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load the profile data for the current user using their UID
        viewModel.loadProfile(user.getUid());
//        Button click listener for editing the profile (currently commented out)
        editProfileButton.setOnClickListener(v -> {

           Intent i = new Intent(ProfileActivity.this, EditProfileActivity.class);
           startActivity(i);
        });
    }

    /**
     * Returns a safe string for display.
     * If the string is null or empty, returns "-".
     *
     * @param s input string
     * @return safe string for UI display
     */
    private String safe(String s) {
        if (s == null || s.trim().isEmpty()) return "-";
        return s;
    }
}
