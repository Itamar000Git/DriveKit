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
// * A base Activity for the insurance-company interface screens.
// *
// * What it does:
// * 1) Sets a base layout that contains:
// *    - contentContainer (where each screen content is injected)
// *    - bottom_bar (same exact design as the driver UI)
// * 2) Inflates the child layout (getContentLayoutId()) into the container.
// *
// * What it does NOT do (for now):
// * - No navigation logic (you said we will handle actions later)
// * - No drawer
// * - No data loading
// *
// * Usage:
// * Any insurance Activity should extend this class and implement getContentLayoutId().
// */
//public abstract class BaseInsuranceActivity extends AppCompatActivity {
//
//    // Bottom bar views (same IDs from bottom_bar.xml)
//    protected View bottomNotyBtn;
//    protected View bottomProfileBtn;
//    protected View bottomMenuBtn;
//    protected TextView bottomNotyBadge;
//
//    protected String companyId = "";
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        // 1) Wrap everything with the insurance base layout (container + bottom bar)
//        setContentView(R.layout.activity_base_insurance);
//
//        companyId = safe(getIntent().getStringExtra("insuranceCompanyId"));
//
//
//        // 2) Inject the screen-specific layout into the container
//        FrameLayout container = findViewById(R.id.contentContainer);
//        LayoutInflater.from(this).inflate(getContentLayoutId(), container, true);
//
//        // 3) Bind bottom bar views (from the included bottom_bar.xml)
//        bottomNotyBtn = findViewById(R.id.bottomNotyBtn);
//        bottomProfileBtn = findViewById(R.id.bottomProfileBtn);
//        bottomMenuBtn = findViewById(R.id.bottomMenuBtn);
//        bottomNotyBadge = findViewById(R.id.bottomNotyBadge);
//
//        // 4) For now we do not attach navigation actions.
//        //    The only thing we ensure is that the bar exists and is clickable.
//        setupBottomBarBase();
//        bottomProfileBtn.setOnClickListener(v -> {
//            Intent i = new Intent(this, com.example.drive_kit.View.Insurance_user.InsuranceCompanyProfileActivity.class);
//            i.putExtra("insuranceCompanyId", companyId); // make sure companyId is stored in BaseInsuranceActivity
//            startActivity(i);
//        });
//
//    }
//
//    /**
//     * Each insurance Activity must return its own content layout id.
//     * Example: return R.layout.activity_insurance_home;
//     */
//    @LayoutRes
//    protected abstract int getContentLayoutId();
//
//    /**
//     * Minimal setup to keep UI stable.
//     * We DO NOT navigate anywhere yet (as requested).
//     * You can later add actions here, or override and call super.
//     */
//    protected void setupBottomBarBase() {
//        // No-op for now (we will implement actions later)
//        // Keeping the views bound is enough for the UI to appear.
//    }
//
//    /**
//     * Helper: update badge text/visibility.
//     * You can call this later when you implement "unhandled inquiries count".
//     */
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
//    protected String safe(String s) {
//        return s == null ? "" : s.trim();
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
import androidx.appcompat.app.AppCompatActivity;

import com.example.drive_kit.R;

/**
 * BaseInsuranceActivity
 *
 * Base screen for the insurance interface.
 * It injects the child layout into a container and shows the bottom bar.
 *
 * Important:
 * - We keep the insuranceCompanyId in a field so all screens can reuse it.
 */
public abstract class BaseInsuranceActivity extends AppCompatActivity {

    // NEW: stored company id for navigation across insurance screens
    protected String companyId;

    // Bottom bar views (same IDs from bottom_bar.xml)
    protected View bottomNotyBtn;
    protected View bottomProfileBtn;
    protected View bottomMenuBtn;
    protected TextView bottomNotyBadge;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_base_insurance);

        // Inject the screen-specific layout into the container
        FrameLayout container = findViewById(R.id.contentContainer);
        LayoutInflater.from(this).inflate(getContentLayoutId(), container, true);

        // Read companyId once (same key you already use everywhere)
        companyId = getIntent().getStringExtra("insuranceCompanyId");

        // Bind bottom bar views
        bottomNotyBtn = findViewById(R.id.bottomNotyBtn);
        bottomProfileBtn = findViewById(R.id.bottomProfileBtn);
        bottomMenuBtn = findViewById(R.id.bottomMenuBtn);
        bottomNotyBadge = findViewById(R.id.bottomNotyBadge);

        setupBottomBarBase();
    }

    @LayoutRes
    protected abstract int getContentLayoutId();

    /**
     * Minimal bottom bar behavior for now.
     * We implement only Profile navigation (as you asked).
     * Other buttons can be added later.
     */
    protected void setupBottomBarBase() {

        // Profile button -> open company profile screen
        if (bottomProfileBtn != null) {
            bottomProfileBtn.setOnClickListener(v -> {
                Intent i = new Intent(this, InsuranceCompanyProfileActivity.class);
                i.putExtra("insuranceCompanyId", companyId);
                startActivity(i);
            });
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
