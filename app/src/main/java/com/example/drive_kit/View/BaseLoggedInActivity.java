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

public abstract class BaseLoggedInActivity extends AppCompatActivity {

    // Drawer
    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;

    // Bottom bar
    private View bottomNotyBtn;
    private View bottomProfileBtn;
    private View bottomMenuBtn;
    private TextView bottomNotyBadge;

    // Badge VM
    protected NotificationsViewModel notyVm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1) Base layout with Drawer + container + bottom bar
        setContentView(R.layout.activity_base_logged_in);

        // 2) Inject screen-specific content
        FrameLayout container = findViewById(R.id.contentContainer);
        LayoutInflater.from(this).inflate(getContentLayoutId(), container, true);

        // 3) Drawer views
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        // 4) Bottom bar views
        bottomNotyBtn = findViewById(R.id.bottomNotyBtn);
        bottomProfileBtn = findViewById(R.id.bottomProfileBtn);
        bottomMenuBtn = findViewById(R.id.bottomMenuBtn);
        bottomNotyBadge = findViewById(R.id.bottomNotyBadge);

        setupBottomBar();
        setupDrawerMenu();

        // צבעי התפריט שנפתח מההמבורגר
        applyDrawerColors();

        // Badge notifications
        setupNotificationsBadge();
    }

    /** Each inheriting Activity must return its own content layout id */
    @LayoutRes
    protected abstract int getContentLayoutId();

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

    private void setupDrawerMenu() {
        if (navigationView == null || drawerLayout == null) return;

        // סימון הפריט הפעיל לפי המסך הנוכחי
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

            // סמן מיד את הפריט שנבחר (חוויית UI טובה יותר)
            navigationView.setCheckedItem(id);

            drawerLayout.closeDrawer(GravityCompat.END);

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
     * Drawer menu colors:
     * - checked item: Royal Blue
     * - default item: DriveKit dark blue
     */
    private void applyDrawerColors() {
        if (navigationView == null) return;

        int[][] states = new int[][]{
                new int[]{android.R.attr.state_checked}, // selected
                new int[]{}                              // default
        };

        int[] colors = new int[]{
                0xFF8BC34A, // selected (הסימון)
                0xFF001F3F  // default
        };

        ColorStateList csl = new ColorStateList(states, colors);
        navigationView.setItemTextColor(csl);
        navigationView.setItemIconTintList(csl);
        navigationView.setItemBackgroundResource(R.color.nav_item_bg);
    }

    private void setupNotificationsBadge() {
        notyVm = new ViewModelProvider(this).get(NotificationsViewModel.class);

        notyVm.getNoty().observe(this, list -> {
            int count = (list == null) ? 0 : list.size();
            updateBadge(count);
        });
    }

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
