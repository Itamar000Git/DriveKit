//package com.example.drive_kit.View;
//
//import android.content.Intent;
//import android.net.Uri;
//import android.os.Bundle;
//import android.util.Log;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.lifecycle.ViewModelProvider;
//
//import com.example.drive_kit.R;
//import com.example.drive_kit.ViewModel.CarDetailsViewModel;
//import com.google.android.material.button.MaterialButton;
//import com.google.firebase.auth.FirebaseAuth;
//
//public class CarDetailsActivity extends BaseLoggedInActivity {
//
//    private static final String TAG = "CAR_DETAILS";
//
//    private TextView title;
//    private TextView info;
//
//    private MaterialButton btnDownloadCarBook;
//    private MaterialButton btnOpenYad2;
//
//    private CarDetailsViewModel vm;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        // UI
//        title = findViewById(R.id.carTitle);
//        info  = findViewById(R.id.carInfo);
//
//        btnDownloadCarBook = findViewById(R.id.btnDownloadCarBook);
//        btnOpenYad2        = findViewById(R.id.btnOpenYad2);
//
//        title.setText("פרטי הרכב שלי");
//
//        // ViewModel
//        vm = new ViewModelProvider(this).get(CarDetailsViewModel.class);
//
//        // Manual button starts disabled until URL arrives
//        setButtonEnabled(btnDownloadCarBook, false);
//
//        // Yad2 button starts disabled until URL arrives
//        setButtonEnabled(btnOpenYad2, false);
//
//        // --- Car info text ---
//        vm.getInfoText().observe(this, info::setText);
//
//        vm.getScreenError().observe(this, err -> {
//            if (err != null && !err.trim().isEmpty()) {
//                info.setText(err);
//            }
//        });
//
//        // --- Manual loading UX (same as DIY) ---
//        vm.getManualLoading().observe(this, loading -> {
//            if (loading != null && loading) {
//                btnDownloadCarBook.setText("טוען ספר רכב...");
//                setButtonEnabled(btnDownloadCarBook, false);
//            } else {
//                btnDownloadCarBook.setText("הורדת ספר רכב");
//            }
//        });
//
//        // Manual URL -> enable + click opens browser
//        vm.getManualPdfUrl().observe(this, url -> {
//            Log.d(TAG, "manual url=" + url);
//
//            if (url == null || url.trim().isEmpty()) {
//                setButtonEnabled(btnDownloadCarBook, false);
//                btnDownloadCarBook.setOnClickListener(null);
//                return;
//            }
//
//            setButtonEnabled(btnDownloadCarBook, true);
//            btnDownloadCarBook.setOnClickListener(v -> {
//                try {
//                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
//                } catch (Exception e) {
//                    Toast.makeText(this, "לא ניתן לפתוח את ספר הרכב", Toast.LENGTH_SHORT).show();
//                }
//            });
//        });
//
//        vm.getManualError().observe(this, err -> {
//            if (err != null && !err.trim().isEmpty()) {
//                Log.e(TAG, "manual error=" + err);
//                Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        // --- Yad2 URL -> enable + click opens browser ---
//        vm.getYad2Url().observe(this, url -> {
//            Log.d(TAG, "yad2 url=" + url);
//
//            if (url == null || url.trim().isEmpty()) {
//                setButtonEnabled(btnOpenYad2, false);
//                btnOpenYad2.setOnClickListener(null);
//                return;
//            }
//
//            setButtonEnabled(btnOpenYad2, true);
//            btnOpenYad2.setOnClickListener(v -> {
//                try {
//                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
//                } catch (Exception e) {
//                    Toast.makeText(this, "לא ניתן לפתוח את יד2", Toast.LENGTH_SHORT).show();
//                }
//            });
//        });
//
//        // Load everything
//        String uid = FirebaseAuth.getInstance().getUid();
//        vm.loadDriverAndManual(uid);
//    }
//
//    @Override
//    protected int getContentLayoutId() {
//        return R.layout.activity_car_details;
//    }
//
//    // Simple helper to enable/disable a button with opacity (like DIY style)
//    private void setButtonEnabled(MaterialButton btn, boolean enabled) {
//        if (btn == null) return;
//        btn.setEnabled(enabled);
//        btn.setAlpha(enabled ? 1f : 0.5f);
//    }
//}


package com.example.drive_kit.View;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.drive_kit.R;
import com.example.drive_kit.ViewModel.CarDetailsViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;

public class CarDetailsActivity extends BaseLoggedInActivity {

    private static final String TAG = "CAR_DETAILS";

    // Header
    private TextView title;
    private TextView tvCarSubtitle;
    private ShapeableImageView ivCarImage;

    // Details rows
    private TextView tvCarNumberValue;
    private TextView tvManufacturerValue;
    private TextView tvModelValue;
    private TextView tvYearValue;
    private TextView tvColorValue;
    private TextView tvNicknameValue;

    // Insurance rows
    private TextView tvInsuranceCompanyValue;
    private TextView tvInsuranceCompanyIdValue;

    // Keep legacy view (hidden in XML but keep binding safe)
    private TextView info;

    // Actions
    private MaterialButton btnDownloadCarBook;
    private MaterialButton btnOpenYad2;

    private CarDetailsViewModel vm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bindViews();
        setupDefaultUi();

        vm = new ViewModelProvider(this).get(CarDetailsViewModel.class);

        // Buttons start disabled until URL arrives
        setButtonEnabled(btnDownloadCarBook, false);
        setButtonEnabled(btnOpenYad2, false);

        observeViewModel();

        String uid = FirebaseAuth.getInstance().getUid();
        vm.loadDriverAndManual(uid);
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_car_details;
    }

    // -------------------- UI --------------------

    private void bindViews() {
        // Header
        title = findViewById(R.id.carTitle);
        tvCarSubtitle = findViewById(R.id.tvCarSubtitle);
        ivCarImage = findViewById(R.id.ivCarImage);

        // Details
        tvCarNumberValue = findViewById(R.id.tvCarNumberValue);
        tvManufacturerValue = findViewById(R.id.tvManufacturerValue);
        tvModelValue = findViewById(R.id.tvModelValue);
        tvYearValue = findViewById(R.id.tvYearValue);
        tvColorValue = findViewById(R.id.tvColorValue);
        tvNicknameValue = findViewById(R.id.tvNicknameValue);

//        // Insurance
//        tvInsuranceCompanyValue = findViewById(R.id.tvInsuranceCompanyValue);
//        tvInsuranceCompanyIdValue = findViewById(R.id.tvInsuranceCompanyIdValue);

        // Legacy (hidden)
        info = findViewById(R.id.carInfo);

        // Buttons
        btnDownloadCarBook = findViewById(R.id.btnDownloadCarBook);
        btnOpenYad2 = findViewById(R.id.btnOpenYad2);
    }

    private void setupDefaultUi() {
        if (title != null) title.setText("פרטי הרכב שלי");

        // Default placeholders
        setTextOrDash(tvCarSubtitle, "-");
        setTextOrDash(tvCarNumberValue, "-");
        setTextOrDash(tvManufacturerValue, "-");
        setTextOrDash(tvModelValue, "-");
        setTextOrDash(tvYearValue, "-");
        setTextOrDash(tvColorValue, "-");
        setTextOrDash(tvNicknameValue, "-");
        setTextOrDash(tvInsuranceCompanyValue, "-");
        setTextOrDash(tvInsuranceCompanyIdValue, "-");

        // Default image
        if (ivCarImage != null) {
            ivCarImage.setImageResource(R.drawable.ic_profile_placeholder);
        }
    }

    private void observeViewModel() {
        // Header + details
        vm.getCarSubtitle().observe(this, v -> setTextOrDash(tvCarSubtitle, v));
        vm.getCarNumber().observe(this, v -> setTextOrDash(tvCarNumberValue, v));
        vm.getManufacturer().observe(this, v -> setTextOrDash(tvManufacturerValue, v));
        vm.getModel().observe(this, v -> setTextOrDash(tvModelValue, v));
        vm.getYear().observe(this, v -> setTextOrDash(tvYearValue, v));
        vm.getColor().observe(this, v -> setTextOrDash(tvColorValue, v));
        vm.getNickname().observe(this, v -> setTextOrDash(tvNicknameValue, v));

        // Insurance
        vm.getInsuranceCompanyName().observe(this, v -> setTextOrDash(tvInsuranceCompanyValue, v));
        vm.getInsuranceCompanyId().observe(this, v -> setTextOrDash(tvInsuranceCompanyIdValue, v));

        // Legacy info text (kept for compatibility)
        vm.getInfoText().observe(this, text -> {
            if (info != null) info.setText(text == null ? "" : text);
        });

        // Screen error
        vm.getScreenError().observe(this, err -> {
            if (err != null && !err.trim().isEmpty()) {
                Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
            }
        });

        // --- Image loading (same logic as Profile: base64 first, uri fallback) ---
        vm.getCarImageBase64().observe(this, base64 -> {
            boolean loaded = tryLoadCarBase64Into(ivCarImage, base64);
            if (!loaded) {
                String uri = vm.getCarImageUri().getValue();
                tryLoadCarUriInto(ivCarImage, uri);
            }
        });

        vm.getCarImageUri().observe(this, uri -> {
            String base64 = vm.getCarImageBase64().getValue();
            if (base64 != null && !base64.trim().isEmpty()) return; // base64 has priority
            tryLoadCarUriInto(ivCarImage, uri);
        });

        // --- Manual loading UX (like DIY) ---
        vm.getManualLoading().observe(this, loading -> {
            if (loading != null && loading) {
                btnDownloadCarBook.setText("טוען ספר רכב...");
                setButtonEnabled(btnDownloadCarBook, false);
            } else {
                btnDownloadCarBook.setText("הורדת ספר רכב");
            }
        });

        vm.getManualPdfUrl().observe(this, url -> {
            Log.d(TAG, "manual url=" + url);

            if (url == null || url.trim().isEmpty()) {
                setButtonEnabled(btnDownloadCarBook, false);
                btnDownloadCarBook.setOnClickListener(null);
                return;
            }

            setButtonEnabled(btnDownloadCarBook, true);
            btnDownloadCarBook.setOnClickListener(v -> openUrl(url, "לא ניתן לפתוח את ספר הרכב"));
        });

        vm.getManualError().observe(this, err -> {
            if (err != null && !err.trim().isEmpty()) {
                Log.e(TAG, "manual error=" + err);
                Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
            }
        });

        // --- Yad2 URL ---
        vm.getYad2Url().observe(this, url -> {
            Log.d(TAG, "yad2 url=" + url);

            if (url == null || url.trim().isEmpty()) {
                setButtonEnabled(btnOpenYad2, false);
                btnOpenYad2.setOnClickListener(null);
                return;
            }

            setButtonEnabled(btnOpenYad2, true);
            btnOpenYad2.setOnClickListener(v -> openUrl(url, "לא ניתן לפתוח את יד2"));
        });
    }

    // -------------------- Helpers --------------------

    // Simple helper to enable/disable a button with opacity
    private void setButtonEnabled(MaterialButton btn, boolean enabled) {
        if (btn == null) return;
        btn.setEnabled(enabled);
        btn.setAlpha(enabled ? 1f : 0.5f);
    }

    private void setTextOrDash(TextView tv, String value) {
        if (tv == null) return;
        if (value == null) {
            tv.setText("-");
            return;
        }
        String t = value.trim();
        tv.setText(t.isEmpty() ? "-" : t);
    }

    private void openUrl(String url, String errorToast) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            Toast.makeText(this, errorToast, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Loads car image with priority:
     * 1) car.carImageBase64 (new)
     * 2) car.carImageUri (old fallback)
     *
     * Returns true if Base64 load succeeded.
     */
    private boolean tryLoadCarBase64Into(ShapeableImageView target, String base64) {
        if (target == null) return false;

        if (base64 == null || base64.trim().isEmpty()) {
            return false;
        }

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
                    .into(target);

            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private void tryLoadCarUriInto(ShapeableImageView target, String uri) {
        if (target == null) return;

        if (uri == null || uri.trim().isEmpty()) {
            target.setImageResource(R.drawable.ic_profile_placeholder);
            return;
        }

        Glide.with(this)
                .load(uri)
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_profile_placeholder)
                .centerCrop()
                .into(target);
    }
}

