package com.example.drive_kit.View;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

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

    // Badge VM (אם תרצה לספור התראות)
    protected NotificationsViewModel notyVm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1) עוטפים הכל ב-layout בסיסי שיש בו Drawer + container + bottom bar
        setContentView(R.layout.activity_base_logged_in);

        // 2) מזריקים את ה-layout של המסך הספציפי לתוך container
        FrameLayout container = findViewById(R.id.contentContainer);
        LayoutInflater.from(this).inflate(getContentLayoutId(), container, true);

        // 3) find drawer views
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        // 4) find bottom bar views (מה-include)
        bottomNotyBtn = findViewById(R.id.bottomNotyBtn);
        bottomProfileBtn = findViewById(R.id.bottomProfileBtn);
        bottomMenuBtn = findViewById(R.id.bottomMenuBtn);
        bottomNotyBadge = findViewById(R.id.bottomNotyBadge);

        setupBottomBar();
        setupDrawerMenu();

        // אופציונלי: באדג' התראות
        setupNotificationsBadge();
    }

    /** כל Activity יורש חייב להחזיר כאן את ה-layout שלו (למשל R.layout.home_activity_content) */
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
                // RTL -> תפריט מהצד הימני
                drawerLayout.openDrawer(GravityCompat.END);
            }
        });
    }

    private void setupDrawerMenu() {
        if (navigationView == null || drawerLayout == null) return;

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            drawerLayout.closeDrawer(GravityCompat.END);

            if (id == R.id.nav_home) {
//                if (!(this instanceof HomeActivity)) {
//                    startActivity(new Intent(this, HomeActivity.class));
//                }

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

                if (!(this instanceof InsuranceActivity)) {
                    startActivity(new Intent(this, InsuranceActivity.class));
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
        // רענון באדג׳
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
