package com.example.drive_kit.View;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.bumptech.glide.Glide;
import com.example.drive_kit.Data.Workers.NotyWorker;
import com.example.drive_kit.Model.Car;
import com.example.drive_kit.Model.Driver;
import com.example.drive_kit.R;
import com.example.drive_kit.ViewModel.HomeViewModel;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * HomeActivity is the main screen shown after a successful login.
 * Uses BaseLoggedInActivity (drawer + bottom bar are handled in the base).
 */
public class HomeActivity extends BaseLoggedInActivity {

    private TextView welcomeText;
    private TextView helloUserText;
    private ShapeableImageView homeAvatar;

    // ViewModel
    private HomeViewModel homeVm;

    // Firebase uid
    private String uid;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // -----------------------------------
        // Prevent back navigation from Home
        // -----------------------------------
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                moveTaskToBack(true);
            }
        });
        //init manual car pdf
            ManualsSeeder.seedIfNeeded(this);

        // -----------------------------------
        // Find Views (from home_activity_content.xml)
        // -----------------------------------
        welcomeText = findViewById(R.id.welcomeText);
        helloUserText = findViewById(R.id.helloUserText);
        homeAvatar = findViewById(R.id.homeAvatar);

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
                    startActivity(new Intent(HomeActivity.this, Driver_InsuranceActivity.class))
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
        loadHomeAvatar(uid);

        // -----------------------------------
        // Home ViewModel (welcome text)
        // -----------------------------------
        homeVm = new ViewModelProvider(this).get(HomeViewModel.class);

        homeVm.getWelcomeText().observe(this, vmText -> {
            String t = (vmText == null) ? "" : vmText.trim();

            String helloLine = "שלום";

            if (t.startsWith("שלום")) {
                String rest = t.substring("שלום".length()).trim();
                rest = rest.replaceAll("^[,!?\\s]+", "");
                rest = rest.replaceAll("[,!?\\s]+$", "");

                if (!rest.isEmpty() && !t.startsWith("שגיאה")) {
                    helloLine = "שלום " + rest;
                }
            }

            if (helloUserText != null) {
                helloUserText.setText(helloLine);
            }

            if (welcomeText != null) {
                welcomeText.setText("ברוך הבא ל-DriveKit");
            }
        });

        homeVm.loadWelcomeText(uid);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHomeAvatar(uid);
        homeVm.getWelcomeText().observe(this, vmText -> {
            String t = (vmText == null) ? "" : vmText.trim();

            String helloLine = "שלום";

            if (t.startsWith("שלום")) {
                String rest = t.substring("שלום".length()).trim();
                rest = rest.replaceAll("^[,!?\\s]+", "");
                rest = rest.replaceAll("[,!?\\s]+$", "");

                if (!rest.isEmpty() && !t.startsWith("שגיאה")) {
                    helloLine = "שלום " + rest;
                }
            }

            if (helloUserText != null) {
                helloUserText.setText(helloLine);
            }

            if (welcomeText != null) {
                welcomeText.setText("ברוך הבא ל-DriveKit");
            }
        });
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.home_activity;
    }

    private void loadHomeAvatar(String uid) {
        if (uid == null || uid.trim().isEmpty()) {
            if (homeAvatar != null) homeAvatar.setImageResource(R.drawable.ic_profile_placeholder);
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("drivers")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    Driver d = doc.toObject(Driver.class);
                    loadAvatarIntoHome(d);
                })
                .addOnFailureListener(e -> {
                    if (homeAvatar != null) homeAvatar.setImageResource(R.drawable.ic_profile_placeholder);
                });
    }

    private void loadAvatarIntoHome(Driver d) {
        if (homeAvatar == null) return;

        if (d == null || d.getCar() == null) {
            homeAvatar.setImageResource(R.drawable.ic_profile_placeholder);
            return;
        }

        Car car = d.getCar();

        // 1) Base64 (new)
        String base64 = null;
        try { base64 = car.getCarImageBase64(); } catch (Exception ignored) {}

        if (base64 != null && !base64.trim().isEmpty()) {
            try {
                String clean = base64;
                int comma = clean.indexOf(',');
                if (comma >= 0) clean = clean.substring(comma + 1);

                byte[] bytes = Base64.decode(clean, Base64.DEFAULT);

                Glide.with(this)
                        .load(bytes)
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .error(R.drawable.ic_profile_placeholder)
                        .centerCrop()
                        .into(homeAvatar);

                return;
            } catch (Exception ignored) {
                // fall back to URI
            }
        }

        // 2) Uri (old)
        String uri = null;
        try { uri = car.getCarImageUri(); } catch (Exception ignored) {}

        if (uri == null || uri.trim().isEmpty()) {
            homeAvatar.setImageResource(R.drawable.ic_profile_placeholder);
            return;
        }

        Glide.with(this)
                .load(uri)
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_profile_placeholder)
                .centerCrop()
                .into(homeAvatar);
    }

}




