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
public class ProfileActivity extends BaseLoggedInActivity {

    // TextViews for displaying profile information
    private TextView firstNameText;
    private TextView lastNameText;
    private TextView emailValue;
    private TextView phoneValue;

    // Car details
    private TextView carNumberValue;

    // ADDED (XML new fields)
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
        //setContentView(R.layout.profile_activity);
        getContentLayoutId();

        // --- Find views ---
        firstNameText = findViewById(R.id.firstNameText);
        lastNameText = findViewById(R.id.lastNameText);
        emailValue = findViewById(R.id.emailValue);
        phoneValue = findViewById(R.id.phoneValue);

        carNumberValue = findViewById(R.id.carNumberValue);

        // ADDED: new TextViews from updated XML
        manufacturerValue = findViewById(R.id.manufacturerValue);
        modelValue = findViewById(R.id.modelValue);
        yearValue = findViewById(R.id.yearValue);

        insuranceDateValue = findViewById(R.id.insuranceDateValue);
        testDateValue = findViewById(R.id.testDateValue);
        treatmentDateValue = findViewById(R.id.treatmentDateValue);

        editProfileButton = findViewById(R.id.editProfileButton);

        // --- ViewModel ---
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        // Observe the Driver LiveData
        viewModel.getDriver().observe(this, d -> {
            if (d == null) return;

            firstNameText.setText(safe(d.getFirstName()));
            lastNameText.setText(safe(d.getLastName()));
            emailValue.setText(safe(d.getEmail()));
            phoneValue.setText(safe(d.getPhone()));

            // Car might be null from Firestore -> use Driver.getCar() which ensures non-null
            carNumberValue.setText(safe(d.getCar().getCarNum()));

            // ADDED: manufacturer, model, year
            // Manufacturer = carModel enum name (TOYOTA / HYUNDAI / ...)
            String manufacturerText = (d.getCar().getCarModel() == null)
                    ? "-"
                    : d.getCar().getCarModel().name();
            manufacturerValue.setText(safe(manufacturerText));

            // Specific model = carSpecificModel (I10 / TUCSON / ...)
            modelValue.setText(safe(d.getCar().getCarSpecificModel()));

            // Year = int -> show "-" if 0
            int y = d.getCar().getYear();
            yearValue.setText(y > 0 ? String.valueOf(y) : "-");

            // Dates
            insuranceDateValue.setText(safe(d.getFormattedInsuranceDate()));
            testDateValue.setText(safe(d.getFormattedTestDate()));
            treatmentDateValue.setText(safe(d.getFormattedTreatDate()));
        });

        // Observe error messages from the ViewModel
        viewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        // Check logged-in user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "משתמש לא מחובר", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load profile
        viewModel.loadProfile(user.getUid());

        // Edit profile button
        editProfileButton.setOnClickListener(v -> {
            Intent i = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(i);
        });
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
