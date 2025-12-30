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
 * HomeActivity is the main screen shown after a successful login.
 *
 * This screen:
 * - Displays a welcome message to the user
 * - Allows navigation to notifications and profile screens
 * - Requests notification permission (Android 13+)
 * - Triggers a notification worker
 */
public class HomeActivity extends AppCompatActivity {

    // TextView used to display the welcome message
    private TextView welcomeText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Standard Activity initialization
        super.onCreate(savedInstanceState);

        // Attach the home_activity.xml layout to this Activity
        // After this line, all views inside the layout exist in memory
        setContentView(R.layout.home_activity);


        // Find and connect the TextView for the welcome message
        welcomeText = findViewById(R.id.welcomeText);

        // Find and connect the notification icon (bell icon)
        ImageView notyIcon = findViewById(R.id.noty_icon);

        // Find and connect the profile icon
        ImageView profileIcon = findViewById(R.id.profile_icon);


        /// ///////////////////////////
        // Request notification permission for Android 13 (API 33) and above
        // Without this permission, notifications will not be shown
        if (android.os.Build.VERSION.SDK_INT >= 33) {

            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                // Ask the user for notification permission
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
        }



        // When the notification icon is clicked, open NotificationsActivity
        notyIcon.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, NotificationsActivity.class);
            startActivity(intent);
        });

        // When the profile icon is clicked, open ProfileActivity
        profileIcon.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
        });


        // Schedule a one-time execution of the notification worker
        // This is usually used for immediate background checks
        WorkManager.getInstance(getApplicationContext())
                .enqueue(new OneTimeWorkRequest.Builder(NotyWorker.class).build());


        // Create (or retrieve) the ViewModel associated with this Activity
        HomeViewModel viewModel = new ViewModelProvider(this).get(HomeViewModel.class);


        // Observe the welcome text LiveData
        // Whenever the value changes, the TextView will update automatically
        viewModel.getWelcomeText().observe(this, newText -> {
            welcomeText.setText(newText);
        });



        // Get the currently logged-in Firebase user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


        // Extract the user ID (UID) if the user exists
        String uid = (user == null) ? null : user.getUid();

        // Ask the ViewModel to load the welcome text for this user
        viewModel.loadWelcomeText(uid);
    }
}
