package com.example.drive_kit.View;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.drive_kit.Data.Workers.NotyWorker;
import com.example.drive_kit.R;
import com.example.drive_kit.ViewModel.HomeViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Activity for displaying the welcome text.
 * It observes the LiveData in the ViewModel and updates the UI accordingly.
 */
public class HomeActivity extends AppCompatActivity {

    private TextView welcomeText; // text view for displaying the welcome text


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        // Initialize the text view for displaying the welcome text
        welcomeText = findViewById(R.id.welcomeText);
        // Initialize the notification icon
        ImageView notyIcon = findViewById(R.id.noty_icon);

        if (android.os.Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
        }
        // Set the click listener for the notification icon
        notyIcon.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, NotificationsActivity.class);
            startActivity(intent);
        });
        // Schedule the daily notification worker
        WorkManager.getInstance(getApplicationContext())
                .enqueue(new OneTimeWorkRequest.Builder(NotyWorker.class).build());

        // Initialize the ViewModel
        HomeViewModel viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // Observe the LiveData and update the UI when the welcome text changes
        viewModel.getWelcomeText().observe(this, welcomeText::setText);

        // Get the current user and load the welcome text
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = (user == null) ? null : user.getUid();

        viewModel.loadWelcomeText(uid);// Load the welcome text for the current user
    }
}
