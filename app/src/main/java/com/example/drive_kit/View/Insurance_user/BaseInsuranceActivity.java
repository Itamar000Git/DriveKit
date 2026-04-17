package com.example.drive_kit.View.Insurance_user;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.drive_kit.R;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
/**
 * BaseInsuranceActivity
 *
 * Abstract base activity for all insurance-related screens.
 *
 * Responsibilities:
 * 1. Provides a shared layout structure:
 *    - DrawerLayout (hamburger menu)
 *    - NavigationView (menu items)
 *    - Bottom bar (navigation + badge)
 *    - Content container (child activities inject their layout here)
 *
 * 2. Handles:
 *    - Drawer open/close behavior
 *    - Navigation between insurance screens
 *    - Logout logic
 *    - Bottom bar actions
 *
 * 3. Manages shared state:
 *    - insuranceCompanyId (Firestore document ID)
 *
 * Usage:
 * - Every insurance screen extends this class
 * - Must implement getContentLayoutId()
 */
public abstract class BaseInsuranceActivity extends AppCompatActivity {

    // ===== Drawer =====
    protected DrawerLayout drawerLayout; // Root drawer layout
    protected NavigationView navigationView; // Navigation menu inside drawer

    // ===== Bottom bar =====
    protected View bottomNotyBtn; // Notifications button (bottom bar)
    protected View bottomProfileBtn; // Profile button (bottom bar)
    protected View bottomMenuBtn; // Hamburger/menu button (bottom bar)
    protected TextView bottomNotyBadge; //Badge showing notification count

    // ===== Shared data =====
    protected String companyDocId; //Firestore document ID of the insurance company

    /**
     * Initializes base layout, injects child layout, and binds UI components.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1) Base layout that contains drawer + container + bottom bar
        setContentView(R.layout.activity_base_insurance);

        // 2) Inject the screen-specific layout into the container
        FrameLayout container = findViewById(R.id.contentContainer);
        LayoutInflater.from(this).inflate(getContentLayoutId(), container, true);

        // 3) Read company docId once (used for navigation)
        companyDocId = getIntent().getStringExtra("insuranceCompanyId");

        // 4) Bind drawer views
        drawerLayout = findViewById(R.id.insuranceDrawerLayout);
        navigationView = findViewById(R.id.insuranceNavigationView);

        // 5) Bind bottom bar views (from bottom_bar.xml include)
        bottomNotyBtn = findViewById(R.id.bottomNotyBtn);
        bottomProfileBtn = findViewById(R.id.bottomProfileBtn);
        bottomMenuBtn = findViewById(R.id.bottomMenuBtn);
        bottomNotyBadge = findViewById(R.id.bottomNotyBadge);

        setupBottomBar();
        setupDrawerMenu();
    }

    /**
     * Each child Activity must provide its layout resource ID.
     *
     * @return layout resource ID
     */

    @LayoutRes
    protected abstract int getContentLayoutId();

    /**
     * Initializes bottom bar interactions.
     *
     * Behavior:
     * - Menu button → opens drawer (RTL supported)
     * - Profile button → navigates to profile screen
     * - Notifications → reserved for future implementation
     */
    private void setupBottomBar() {
        if (bottomMenuBtn != null) {
            bottomMenuBtn.setOnClickListener(v -> {
                if (drawerLayout != null) {
                    // RTL -> open from the right
                    drawerLayout.openDrawer(GravityCompat.END);
                }
            });
        }

        if (bottomProfileBtn != null) {
            bottomProfileBtn.setOnClickListener(v -> openInsuranceProfile());
        }
    }

    /**
     * Initializes drawer menu item handling.
     *
     * Handles navigation between:
     * - Inquiries
     * - Profile
     * - Edit Profile
     * - Logout
     */
    private void setupDrawerMenu() {
        if (navigationView == null || drawerLayout == null) return;

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            drawerLayout.closeDrawer(GravityCompat.END);

            if (id == R.id.nav_ins_inquiries) {
                openInsuranceInquiries();
                return true;
            }

            if (id == R.id.nav_ins_profile) {
                openInsuranceProfile();
                return true;
            }

            if (id == R.id.nav_ins_edit_profile) {
                openInsuranceEditProfile();
                return true;
            }

            // NEW: Logout (same behavior as driver)
            if (id == R.id.nav_ins_logout) {
                FirebaseAuth.getInstance().signOut();
                Intent i = new Intent(this, com.example.drive_kit.View.MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
                return true;
            }

            return false;
        });
    }

    // ===== Navigation helpers =====

    /**
     * Opens Insurance Inquiries screen.
     */
    protected void openInsuranceInquiries() {
        if (companyDocId == null || companyDocId.trim().isEmpty()) return;

        if (!(this instanceof InsuranceInquiriesActivity)) {
            Intent i = new Intent(this, InsuranceInquiriesActivity.class);
            i.putExtra("insuranceCompanyId", companyDocId);
            startActivity(i);
        }
    }

    /**
     * Opens Insurance Profile screen.
     */
    protected void openInsuranceProfile() {
        if (companyDocId == null || companyDocId.trim().isEmpty()) return;

        if (!(this instanceof InsuranceCompanyProfileActivity)) {
            Intent i = new Intent(this, InsuranceCompanyProfileActivity.class);
            i.putExtra("insuranceCompanyId", companyDocId);
            startActivity(i);
        }
    }

    /**
     * Opens Insurance Edit Profile screen.
     */
    protected void openInsuranceEditProfile() {
        if (companyDocId == null || companyDocId.trim().isEmpty()) return;

        // Replace class name if your edit screen is named differently
        if (!(this instanceof InsuranceCompanyEditProfileActivity)) {
            Intent i = new Intent(this, InsuranceCompanyEditProfileActivity.class);
            i.putExtra("insuranceCompanyId", companyDocId);
            startActivity(i);
        }
    }

    /**
     * Updates notification badge in bottom bar.
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
