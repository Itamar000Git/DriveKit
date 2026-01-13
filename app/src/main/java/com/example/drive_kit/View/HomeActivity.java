//package com.example.drive_kit.View;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.lifecycle.ViewModelProvider;
//import androidx.work.OneTimeWorkRequest;
//import androidx.work.WorkManager;
//
//import com.example.drive_kit.Data.Workers.NotyWorker;
//import com.example.drive_kit.R;
//import com.example.drive_kit.ViewModel.HomeViewModel;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import android.view.View;
//import androidx.work.WorkInfo;
//import com.example.drive_kit.ViewModel.NotificationsViewModel;
//
//
///**
// * HomeActivity is the main screen shown after a successful login.
// *
// * This screen:
// * - Displays a welcome message to the user
// * - Allows navigation to notifications and profile screens
// * - Requests notification permission (Android 13+)
// * - Triggers a notification worker
// */
//public class HomeActivity extends AppCompatActivity {
//
//    // TextView used to display the welcome message
//    private TextView welcomeText;
//    private TextView badgeTv;
//    private NotificationsViewModel notyVm;
//    private String uid;
//
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        // Standard Activity initialization
//        super.onCreate(savedInstanceState);
//
//        // Attach the home_activity.xml layout to this Activity
//        // After this line, all views inside the layout exist in memory
//        setContentView(R.layout.home_activity);
//
//
//        // Find and connect the TextView for the welcome message
//        welcomeText = findViewById(R.id.welcomeText);
//
//        // Find and connect the notification icon (bell icon)
//        ImageView notyIcon = findViewById(R.id.noty_icon);
//
//        // Find and connect the profile icon
//        ImageView profileIcon = findViewById(R.id.profile_icon);
//
//
//        /// ///////////////////////////
//        // Request notification permission for Android 13 (API 33) and above
//        // Without this permission, notifications will not be shown
//        if (android.os.Build.VERSION.SDK_INT >= 33) {
//
//            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
//                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
//                // Ask the user for notification permission
//                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1001);
//            }
//        }
//
//
//
//        // When the notification icon is clicked, open NotificationsActivity
//        notyIcon.setOnClickListener(v -> {
//            Intent intent = new Intent(HomeActivity.this, NotificationsActivity.class);
//            startActivity(intent);
//        });
//
//        // When the profile icon is clicked, open ProfileActivity
//        profileIcon.setOnClickListener(v -> {
//            startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
//        });
//
//
//        // Schedule a one-time execution of the notification worker
//        // This is usually used for immediate background checks
//        WorkManager.getInstance(getApplicationContext())
//                .enqueue(new OneTimeWorkRequest.Builder(NotyWorker.class).build());
//
//
//        // Create (or retrieve) the ViewModel associated with this Activity
//        HomeViewModel viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
//
//
//        // Observe the welcome text LiveData
//        // Whenever the value changes, the TextView will update automatically
//        viewModel.getWelcomeText().observe(this, newText -> {
//            welcomeText.setText(newText);
//        });
//
//
//
//        // Get the currently logged-in Firebase user
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//
//
//        // Extract the user ID (UID) if the user exists
//        String uid = (user == null) ? null : user.getUid();
//
//        // Ask the ViewModel to load the welcome text for this user
//        viewModel.loadWelcomeText(uid);
//    }
//}
package com.example.drive_kit.View;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.drive_kit.Data.Workers.NotyWorker;
import com.example.drive_kit.R;
import com.example.drive_kit.ViewModel.HomeViewModel;
import com.example.drive_kit.ViewModel.NotificationsViewModel;
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
 * - Shows a badge count above the bell icon
 */
public class HomeActivity extends AppCompatActivity {

    // Welcome message
    private TextView welcomeText;

    // Badge text on top of the bell
    private TextView badgeTv;

    // Notifications ViewModel for badge count
    private NotificationsViewModel notyVm;

    // Cached uid for reloads
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        // Welcome text
        welcomeText = findViewById(R.id.welcomeText);

        // Profile icon
        ImageView profileIcon = findViewById(R.id.profile_icon);

        findViewById(R.id.circleMyCar).setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, MyCarsActivity.class))
        );
        // Bell container + badge (requires XML change: FrameLayout id notyContainer + TextView id noty_badge)
        View notyContainer = findViewById(R.id.notyContainer);
        badgeTv = findViewById(R.id.noty_badge);

        // Request notification permission for Android 13+ (API 33+)
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
        }

        // Open NotificationsActivity when bell is clicked
        notyContainer.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, NotificationsActivity.class);
            startActivity(intent);
        });



        // Open ProfileActivity when profile icon is clicked
        profileIcon.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
        });

        // Trigger a one-time background check
        WorkManager.getInstance(getApplicationContext())
                .enqueue(new OneTimeWorkRequest.Builder(NotyWorker.class).build());

        // Home ViewModel (welcome text)
        HomeViewModel homeVm = new ViewModelProvider(this).get(HomeViewModel.class);
        homeVm.getWelcomeText().observe(this, welcomeText::setText);

        // Get Firebase user + uid
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        uid = (user == null) ? null : user.getUid();

        // Load welcome text
        homeVm.loadWelcomeText(uid);

        // Notifications ViewModel (badge count)
        notyVm = new ViewModelProvider(this).get(NotificationsViewModel.class);
        notyVm.getNoty().observe(this, list -> {
            int count = (list == null) ? 0 : list.size();
            updateBadge(count);
        });

        // Initial badge load
        if (uid != null) {
            notyVm.loadNoty(uid);
        } else {
            updateBadge(0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh badge each time we return to Home (e.g., after dismissing notifications)
        if (uid != null && notyVm != null) {
            notyVm.loadNoty(uid);
        }
    }

    private void updateBadge(int count) {
        if (badgeTv == null) return;

        if (count <= 0) {
            badgeTv.setVisibility(View.GONE);
        } else {
            badgeTv.setVisibility(View.VISIBLE);
            badgeTv.setText(count > 99 ? "99+" : String.valueOf(count));
        }
    }
}

