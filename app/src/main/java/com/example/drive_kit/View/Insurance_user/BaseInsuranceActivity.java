//package com.example.drive_kit.View.Insurance_user;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.widget.FrameLayout;
//import android.widget.TextView;
//
//import androidx.annotation.LayoutRes;
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.example.drive_kit.R;
//
///**
// * BaseInsuranceActivity
// *
// * Base screen for the insurance interface.
// * It injects the child layout into a container and shows the bottom bar.
// *
// * Important:
// * - We keep the insuranceCompanyId in a field so all screens can reuse it.
// */
//public abstract class BaseInsuranceActivity extends AppCompatActivity {
//
//    // NEW: stored company id for navigation across insurance screens
//    protected String companyId;
//
//    // Bottom bar views (same IDs from bottom_bar.xml)
//    protected View bottomNotyBtn;
//    protected View bottomProfileBtn;
//    protected View bottomMenuBtn;
//    protected TextView bottomNotyBadge;
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        setContentView(R.layout.activity_base_insurance);
//
//        // Inject the screen-specific layout into the container
//        FrameLayout container = findViewById(R.id.contentContainer);
//        LayoutInflater.from(this).inflate(getContentLayoutId(), container, true);
//
//        // Read companyId once (same key you already use everywhere)
//        companyId = getIntent().getStringExtra("insuranceCompanyId");
//
//        // Bind bottom bar views
//        bottomNotyBtn = findViewById(R.id.bottomNotyBtn);
//        bottomProfileBtn = findViewById(R.id.bottomProfileBtn);
//        bottomMenuBtn = findViewById(R.id.bottomMenuBtn);
//        bottomNotyBadge = findViewById(R.id.bottomNotyBadge);
//
//        setupBottomBarBase();
//    }
//
//    @LayoutRes
//    protected abstract int getContentLayoutId();
//
//    /**
//     * Minimal bottom bar behavior for now.
//     * We implement only Profile navigation (as you asked).
//     * Other buttons can be added later.
//     */
//    protected void setupBottomBarBase() {
//
//        // Profile button -> open company profile screen
//        if (bottomProfileBtn != null) {
//            bottomProfileBtn.setOnClickListener(v -> {
//                Intent i = new Intent(this, InsuranceCompanyProfileActivity.class);
//                i.putExtra("insuranceCompanyId", companyId);
//                startActivity(i);
//            });
//        }
//    }
//
//    protected void updateBadge(int count) {
//        if (bottomNotyBadge == null) return;
//
//        if (count <= 0) {
//            bottomNotyBadge.setVisibility(View.GONE);
//        } else {
//            bottomNotyBadge.setVisibility(View.VISIBLE);
//            bottomNotyBadge.setText(count > 99 ? "99+" : String.valueOf(count));
//        }
//    }
//}


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
 * Base class for all insurance screens.
 *
 * Responsibilities:
 * 1) Wrap the screen with a base layout:
 *    - contentContainer (child layout injected here)
 *    - bottom_bar (same design as driver)
 *    - DrawerLayout + NavigationView (hamburger menu)
 * 2) Handle hamburger click -> open drawer
 * 3) Handle drawer item selection -> navigate between insurance screens
 *
 * Notes:
 * - We keep using the same Intent extra key: "insuranceCompanyId" (Firestore docId).
 */
public abstract class BaseInsuranceActivity extends AppCompatActivity {

    // Drawer
    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;

    // Bottom bar
    protected View bottomNotyBtn;
    protected View bottomProfileBtn;
    protected View bottomMenuBtn;
    protected TextView bottomNotyBadge;

    // Company doc id (the Firestore document id)
    protected String companyDocId;

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

    /** Each insurance Activity must return its own content layout id */
    @LayoutRes
    protected abstract int getContentLayoutId();

    /**
     * Bottom bar actions (insurance version).
     * For now:
     * - Profile -> open company profile
     * - Hamburger -> open drawer
     * - Notifications button: we will connect later (you said later)
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

        // Noty button will be connected later.
        // Keep it clickable but do nothing for now (or route to inquiries if you want later).
    }

    /**
     * Drawer menu actions (insurance screens).
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

    // ---------------------------------------------------------
    // Navigation helpers
    // ---------------------------------------------------------

    protected void openInsuranceInquiries() {
        if (companyDocId == null || companyDocId.trim().isEmpty()) return;

        if (!(this instanceof InsuranceInquiriesActivity)) {
            Intent i = new Intent(this, InsuranceInquiriesActivity.class);
            i.putExtra("insuranceCompanyId", companyDocId);
            startActivity(i);
        }
    }

    protected void openInsuranceProfile() {
        if (companyDocId == null || companyDocId.trim().isEmpty()) return;

        if (!(this instanceof InsuranceCompanyProfileActivity)) {
            Intent i = new Intent(this, InsuranceCompanyProfileActivity.class);
            i.putExtra("insuranceCompanyId", companyDocId);
            startActivity(i);
        }
    }

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
     * Badge helper (same as driver base).
     * We will use it later for "unhandled inquiries count".
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
