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

public class ProfileActivity extends AppCompatActivity {

    private TextView firstNameText;
    private TextView lastNameText;
    private TextView emailValue;
    private TextView phoneValue;
    private TextView carNumberValue;
    private TextView insuranceDateValue;
    private TextView testDateValue;

    private Button editProfileButton;

    private ProfileViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);

        firstNameText = findViewById(R.id.firstNameText);
        lastNameText = findViewById(R.id.lastNameText);
        emailValue = findViewById(R.id.emailValue);
        phoneValue = findViewById(R.id.phoneValue);
        carNumberValue = findViewById(R.id.carNumberValue);
        insuranceDateValue = findViewById(R.id.insuranceDateValue);
        testDateValue = findViewById(R.id.testDateValue);

        editProfileButton = findViewById(R.id.editProfileButton);

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        viewModel.getDriver().observe(this, d -> {
            if (d == null) return;

            firstNameText.setText(safe(d.getFirstName()));
            lastNameText.setText(safe(d.getLastName()));
            emailValue.setText(safe(d.getEmail()));
            phoneValue.setText(safe(d.getPhone()));
            carNumberValue.setText(safe(d.getCarNumber()));

            insuranceDateValue.setText(safe(d.getFormattedInsuranceDate()));
            testDateValue.setText(safe(d.getFormattedTestDate()));
        });

        viewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "משתמש לא מחובר", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        viewModel.loadProfile(user.getUid());

        editProfileButton.setOnClickListener(v -> {

//            Intent i = new Intent(ProfileActivity.this, EditProfileActivity.class);
//            startActivity(i);
        });
    }

    private String safe(String s) {
        if (s == null || s.trim().isEmpty()) return "-";
        return s;
    }
}
