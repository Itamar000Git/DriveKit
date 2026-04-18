package com.example.drive_kit.View;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;

import com.example.drive_kit.R;
import com.example.drive_kit.ViewModel.NotificationsViewModel;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

/**
 * BaseLoggedInActivity
 *
 * Abstract base class for all logged-in user screens.
 *
 * Responsibilities:
 * 1. Provides a shared layout:
 *    - DrawerLayout (navigation menu)
 *    - NavigationView (menu items)
 *    - Bottom bar (profile / notifications / menu)
 *    - Content container (child layout injected)
 *
 * 2. Handles navigation:
 *    - Drawer menu navigation between screens
 *    - Bottom bar navigation
 *    - Logout flow
 *
 * 3. Handles notifications badge:
 *    - Observes NotificationsViewModel
 *    - Updates badge count dynamically
 *
 * Usage:
 * - All logged-in activities extend this class
 * - Must implement getContentLayoutId()
 */
public abstract class BaseLoggedInActivity extends AppCompatActivity {

    // ===== Drawer =====
    protected DrawerLayout drawerLayout; // Root DrawerLayout
    protected NavigationView navigationView; // Navigation menu

    // ===== Bottom bar =====
    private View bottomNotyBtn; // Notifications button
    private View bottomProfileBtn; // Profile button
    private View bottomMenuBtn; // Menu (hamburger) button
    private TextView bottomNotyBadge; // Badge showing number of notifications

    // ===== ViewModel =====
    protected NotificationsViewModel notyVm; // ViewModel responsible for notifications

    /**
     * Initializes base layout, drawer, bottom bar and notifications.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Set base layout (drawer + container + bottom bar)
        setContentView(R.layout.activity_base_logged_in);

        // 2. Inject child layout
        FrameLayout container = findViewById(R.id.contentContainer);
        LayoutInflater.from(this).inflate(getContentLayoutId(), container, true);

        // 3. Bind drawer views
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        // 4. Bind bottom bar views
        bottomNotyBtn = findViewById(R.id.bottomNotyBtn);
        bottomProfileBtn = findViewById(R.id.bottomProfileBtn);
        bottomMenuBtn = findViewById(R.id.bottomMenuBtn);
        bottomNotyBadge = findViewById(R.id.bottomNotyBadge);

        setupBottomBar();
        setupDrawerMenu();
        applyDrawerColors();

        // Badge notifications
        setupNotificationsBadge();
    }

    /**
     * Each inheriting Activity must provide its layout.
     *
     * @return layout resource ID
     */
    @LayoutRes
    protected abstract int getContentLayoutId();

    /**
     * Initializes bottom bar actions.
     *
     * Behavior:
     * - Notifications → open NotificationsActivity
     * - Profile → open ProfileActivity
     * - Menu → open drawer (RTL support)
     */
    private void setupBottomBar() {
        bottomNotyBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationsActivity.class));
        });

        bottomProfileBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

        bottomMenuBtn.setOnClickListener(v -> {
            if (drawerLayout != null) {
                // RTL -> open drawer from right side
                drawerLayout.openDrawer(GravityCompat.END);
            }
        });
    }

    /**
     * Initializes drawer navigation logic.
     *
     * Handles:
     * - Highlighting current screen
     * - Navigation between screens
     * - Logout action
     */
    private void setupDrawerMenu() {
        if (navigationView == null || drawerLayout == null) return;

        // Highlight current screen
        if (this instanceof HomeActivity) {
            navigationView.setCheckedItem(R.id.nav_home);
        } else if (this instanceof ProfileActivity) {
            navigationView.setCheckedItem(R.id.nav_profile);
        } else if (this instanceof NotificationsActivity) {
            navigationView.setCheckedItem(R.id.nav_notifications);
        } else if (this instanceof CarDetailsActivity) {
            navigationView.setCheckedItem(R.id.nav_my_car);
        } else if (this instanceof DIYFilterActivity) {
            navigationView.setCheckedItem(R.id.nav_diy);
        } else if (this instanceof activity_nearby_garages) {
            navigationView.setCheckedItem(R.id.nav_garage);
        } else if (this instanceof Driver_InsuranceActivity) {
            navigationView.setCheckedItem(R.id.nav_insurance);
        }

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            // Mark selected item
            navigationView.setCheckedItem(id);

            // Close drawer after selection
            drawerLayout.closeDrawer(GravityCompat.END);

            // Navigation handling
            if (id == R.id.nav_home) {
                if (!(this instanceof HomeActivity)) {
                    startActivity(new Intent(this, HomeActivity.class));
                }

            } else if (id == R.id.nav_profile) {
                if (!(this instanceof ProfileActivity)) {
                    startActivity(new Intent(this, ProfileActivity.class));
                }

            } else if (id == R.id.nav_notifications) {
                if (!(this instanceof NotificationsActivity)) {
                    startActivity(new Intent(this, NotificationsActivity.class));
                }

            } else if (id == R.id.nav_my_car) {
                if (!(this instanceof CarDetailsActivity)) {
                    startActivity(new Intent(this, CarDetailsActivity.class));
                }

            } else if (id == R.id.nav_diy) {
                if (!(this instanceof DIYFilterActivity)) {
                    startActivity(new Intent(this, DIYFilterActivity.class));
                }

            } else if (id == R.id.nav_garage) {
                if (!(this instanceof activity_nearby_garages)) {
                    startActivity(new Intent(this, activity_nearby_garages.class));
                }

            } else if (id == R.id.nav_insurance) {
                if (!(this instanceof Driver_InsuranceActivity)) {
                    startActivity(new Intent(this, Driver_InsuranceActivity.class));
                }

            } else if (id == R.id.nav_logout) {
                // Logout flow
                FirebaseAuth.getInstance().signOut();
                Intent i = new Intent(this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
            }

            return true;
        });
    }

    /**
     * Applies color styling to drawer items.
     *
     * - Selected item → green (#8BC34A)
     * - Default item → dark blue (#001F3F)
     */
    private void applyDrawerColors() {
        if (navigationView == null) return;

        int[][] states = new int[][]{
                new int[]{android.R.attr.state_checked}, // selected
                new int[]{}                              // default
        };

        int[] colors = new int[]{
                0xFF8BC34A, // selected
                0xFF001F3F  // default
        };

        ColorStateList csl = new ColorStateList(states, colors);
        navigationView.setItemTextColor(csl);
        navigationView.setItemIconTintList(csl);
        navigationView.setItemBackgroundResource(R.color.nav_item_bg);
    }

    /**
     * Initializes the notifications badge logic.
     *
     * Responsibilities:
     * - Create NotificationsViewModel instance
     * - Observe notifications list (LiveData)
     * - Update badge count whenever data changes
     *
     * Flow:
     * ViewModel → LiveData<List<NotificationItem>> → UI (badge)
     */
    private void setupNotificationsBadge() {
        // Initialize ViewModel
        notyVm = new ViewModelProvider(this).get(NotificationsViewModel.class);

        // Observe notifications list
        notyVm.getNoty().observe(this, list -> {
            // Convert list to count safely
            int count = (list == null) ? 0 : list.size();
            // Update badge UI
            updateBadge(count);
        });
    }

    /**
     * Called when Activity becomes visible again.
     *
     * Responsibilities:
     * - Reload notifications from backend (Firestore)
     * - Update badge accordingly
     *
     * Important:
     * - Ensures badge stays up-to-date after returning to screen
     * - Handles case where user is not logged in
     */
    @Override
    protected void onResume() {
        super.onResume();

        if (notyVm != null && FirebaseAuth.getInstance().getCurrentUser() != null) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            notyVm.loadNoty(uid);
        } else {
            updateBadge(0);
        }
    }


    /**
     * Updates the notifications badge UI.
     *
     * Behavior:
     * - count <= 0 → hide badge
     * - count > 0 → show badge
     * - count > 99 → display "99+"
     *
     * @param count number of notifications
     */
    protected void updateBadge(int count) {
        if (bottomNotyBadge == null) return;

        if (count <= 0) {
            bottomNotyBadge.setVisibility(View.GONE);
        } else {
            bottomNotyBadge.setVisibility(View.VISIBLE);
            bottomNotyBadge.setText(count > 99 ? "99+" : String.valueOf(count));
        }
    }
}
