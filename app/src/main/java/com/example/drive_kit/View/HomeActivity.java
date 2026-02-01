package com.example.drive_kit.View;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.drive_kit.Data.Workers.NotyWorker;
import com.example.drive_kit.R;
import com.example.drive_kit.ViewModel.HomeViewModel;
import com.example.drive_kit.ViewModel.NotificationsViewModel;
import com.google.android.material.navigation.NavigationView;
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
 * - Opens a hamburger drawer menu for navigation between screens
 * - Prevents back button from returning to previous screen (from Home)
 */
public class HomeActivity extends AppCompatActivity {

    // Welcome message
    private TextView welcomeText;

    // Bottom bar badge
    private TextView badgeTv;

    // ViewModels
    private HomeViewModel homeVm;
    private NotificationsViewModel notyVm;

    // Firebase uid
    private String uid;

    // Drawer
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    // Bottom bar buttons (IDs from bottom_bar.xml)
    private View bottomMenuBtn;     // hamburger
    private View bottomProfileBtn;  // profile
    private View bottomNotyBtn;     // notifications

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        // -----------------------------------
        // Prevent back navigation from Home
        // -----------------------------------
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                moveTaskToBack(true);
            }
        });

        // -----------------------------------
        // Find Views
        // -----------------------------------
        welcomeText = findViewById(R.id.welcomeText);

        // Drawer
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        // Bottom bar (from include)
        bottomMenuBtn = findViewById(R.id.bottomMenuBtn);
        bottomProfileBtn = findViewById(R.id.bottomProfileBtn);
        bottomNotyBtn = findViewById(R.id.bottomNotyBtn);
        badgeTv = findViewById(R.id.bottomNotyBadge);

        // -----------------------------------
        // Request notification permission (Android 13+)
        // -----------------------------------
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
        }

        // -----------------------------------
        // Bottom bar click listeners
        // -----------------------------------
        if (bottomNotyBtn != null) {
            bottomNotyBtn.setOnClickListener(v ->
                    startActivity(new Intent(HomeActivity.this, NotificationsActivity.class))
            );
        }

        if (bottomProfileBtn != null) {
            bottomProfileBtn.setOnClickListener(v ->
                    startActivity(new Intent(HomeActivity.this, ProfileActivity.class))
            );
        }

        if (bottomMenuBtn != null) {
            bottomMenuBtn.setOnClickListener(v -> toggleDrawer());
        }

        // -----------------------------------
        // Circles (home main buttons)
        // -----------------------------------
        View circleMyCar = findViewById(R.id.circleMyCar);
        if (circleMyCar != null) {
            circleMyCar.setOnClickListener(v ->
                    startActivity(new Intent(HomeActivity.this, CarDetailsActivity.class))
            );
        }

        View circleDIY = findViewById(R.id.circleDIY);
        if (circleDIY != null) {
            circleDIY.setOnClickListener(v ->
                    startActivity(new Intent(HomeActivity.this, DIYFilterActivity.class))
            );
        }

        View circleGarage = findViewById(R.id.circleGarage);
        if (circleGarage != null) {
            circleGarage.setOnClickListener(v ->
                    startActivity(new Intent(HomeActivity.this, activity_nearby_garages.class))
            );
        }

        View circleInsurance = findViewById(R.id.circleInsurance);
        if (circleInsurance != null) {
            circleInsurance.setOnClickListener(v ->
                    Toast.makeText(this, "מסך ביטוח עדיין לא ממומש", Toast.LENGTH_SHORT).show()
            );
        }

        // -----------------------------------
        // Trigger one-time background check (NotyWorker)
        // -----------------------------------
        WorkManager.getInstance(getApplicationContext())
                .enqueue(new OneTimeWorkRequest.Builder(NotyWorker.class).build());

        // -----------------------------------
        // Get Firebase user + uid
        // -----------------------------------
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        uid = (user == null) ? null : user.getUid();

        // -----------------------------------
        // Home ViewModel (welcome text)
        // -----------------------------------
        homeVm = new ViewModelProvider(this).get(HomeViewModel.class);
        homeVm.getWelcomeText().observe(this, welcomeText::setText);
        homeVm.loadWelcomeText(uid);

        // -----------------------------------
        // Notifications ViewModel (badge count)
        // -----------------------------------
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

        // -----------------------------------
        // Drawer menu behavior
        // -----------------------------------
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    // already here

                } else if (id == R.id.nav_profile) {
                    startActivity(new Intent(HomeActivity.this, ProfileActivity.class));

                } else if (id == R.id.nav_notifications) {
                    startActivity(new Intent(HomeActivity.this, NotificationsActivity.class));

                } else if (id == R.id.nav_my_car) {
                    startActivity(new Intent(HomeActivity.this, CarDetailsActivity.class));

                } else if (id == R.id.nav_diy) {
                    startActivity(new Intent(HomeActivity.this, DIYFilterActivity.class));

                } else if (id == R.id.nav_garage) {
                    startActivity(new Intent(HomeActivity.this, activity_nearby_garages.class));

                } else if (id == R.id.nav_insurance) {
                    // TODO when ready
                    Toast.makeText(this, "מסך ביטוח עדיין לא ממומש", Toast.LENGTH_SHORT).show();

                } else if (id == R.id.nav_logout) {
                    FirebaseAuth.getInstance().signOut();

                    // Back to login and clear back stack
                    Intent i = new Intent(HomeActivity.this, MainActivity.class); // change if your login is different
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();
                }

                closeDrawer();
                return true;
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh badge each time we return to Home
        if (uid != null && notyVm != null) {
            notyVm.loadNoty(uid);
        }
    }

    private void toggleDrawer() {
        if (drawerLayout == null) return;
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
        } else {
            drawerLayout.openDrawer(GravityCompat.END);
        }
    }

    private void closeDrawer() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
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
