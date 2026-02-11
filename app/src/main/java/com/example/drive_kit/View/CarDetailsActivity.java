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
//    private MaterialButton btnOpenYad2; // נשאיר קיים ב-XML אבל כרגע לא מחברים
//
//    private CarDetailsViewModel vm;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        title = findViewById(R.id.carTitle);
//        info  = findViewById(R.id.carInfo);
//
//        btnDownloadCarBook = findViewById(R.id.btnDownloadCarBook);
//        btnOpenYad2        = findViewById(R.id.btnOpenYad2);
//
//        title.setText("פרטי הרכב שלי");
//
//        // בינתיים לא משתמשים ביד2
//        if (btnOpenYad2 != null) {
//            btnOpenYad2.setEnabled(false);
//            btnOpenYad2.setAlpha(0.5f);
//        }
//
//        vm = new ViewModelProvider(this).get(CarDetailsViewModel.class);
//
//        // Manual button starts disabled
//        setManualButtonEnabled(false);
//
//        // Observe car details text
//        vm.getInfoText().observe(this, info::setText);
//
//        vm.getScreenError().observe(this, err -> {
//            if (err != null && !err.trim().isEmpty()) {
//                info.setText(err);
//            }
//        });
//
//        // Manual loading UX (like DIY)
//        vm.getManualLoading().observe(this, loading -> {
//            if (loading != null && loading) {
//                btnDownloadCarBook.setText("טוען ספר רכב...");
//                setManualButtonEnabled(false);
//            } else {
//                btnDownloadCarBook.setText("הורדת ספר רכב");
//            }
//        });
//
//        // Manual URL -> enable & set click
//        vm.getManualPdfUrl().observe(this, url -> {
//            Log.d(TAG, "manual url=" + url);
//
//            if (url == null || url.trim().isEmpty()) {
//                setManualButtonEnabled(false);
//                btnDownloadCarBook.setOnClickListener(null);
//                return;
//            }
//
//            setManualButtonEnabled(true);
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
//                // אופציונלי:
//                Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
//            }
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
//    private void setManualButtonEnabled(boolean enabled) {
//        btnDownloadCarBook.setEnabled(enabled);
//        btnDownloadCarBook.setAlpha(enabled ? 1f : 0.5f);
//    }
//}


package com.example.drive_kit.View;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.example.drive_kit.R;
import com.example.drive_kit.ViewModel.CarDetailsViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class CarDetailsActivity extends BaseLoggedInActivity {

    private static final String TAG = "CAR_DETAILS";

    private TextView title;
    private TextView info;

    private MaterialButton btnDownloadCarBook;
    private MaterialButton btnOpenYad2;

    private CarDetailsViewModel vm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // UI
        title = findViewById(R.id.carTitle);
        info  = findViewById(R.id.carInfo);

        btnDownloadCarBook = findViewById(R.id.btnDownloadCarBook);
        btnOpenYad2        = findViewById(R.id.btnOpenYad2);

        title.setText("פרטי הרכב שלי");

        // ViewModel
        vm = new ViewModelProvider(this).get(CarDetailsViewModel.class);

        // Manual button starts disabled until URL arrives
        setButtonEnabled(btnDownloadCarBook, false);

        // Yad2 button starts disabled until URL arrives
        setButtonEnabled(btnOpenYad2, false);

        // --- Car info text ---
        vm.getInfoText().observe(this, info::setText);

        vm.getScreenError().observe(this, err -> {
            if (err != null && !err.trim().isEmpty()) {
                info.setText(err);
            }
        });

        // --- Manual loading UX (same as DIY) ---
        vm.getManualLoading().observe(this, loading -> {
            if (loading != null && loading) {
                btnDownloadCarBook.setText("טוען ספר רכב...");
                setButtonEnabled(btnDownloadCarBook, false);
            } else {
                btnDownloadCarBook.setText("הורדת ספר רכב");
            }
        });

        // Manual URL -> enable + click opens browser
        vm.getManualPdfUrl().observe(this, url -> {
            Log.d(TAG, "manual url=" + url);

            if (url == null || url.trim().isEmpty()) {
                setButtonEnabled(btnDownloadCarBook, false);
                btnDownloadCarBook.setOnClickListener(null);
                return;
            }

            setButtonEnabled(btnDownloadCarBook, true);
            btnDownloadCarBook.setOnClickListener(v -> {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                } catch (Exception e) {
                    Toast.makeText(this, "לא ניתן לפתוח את ספר הרכב", Toast.LENGTH_SHORT).show();
                }
            });
        });

        vm.getManualError().observe(this, err -> {
            if (err != null && !err.trim().isEmpty()) {
                Log.e(TAG, "manual error=" + err);
                Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
            }
        });

        // --- Yad2 URL -> enable + click opens browser ---
        vm.getYad2Url().observe(this, url -> {
            Log.d(TAG, "yad2 url=" + url);

            if (url == null || url.trim().isEmpty()) {
                setButtonEnabled(btnOpenYad2, false);
                btnOpenYad2.setOnClickListener(null);
                return;
            }

            setButtonEnabled(btnOpenYad2, true);
            btnOpenYad2.setOnClickListener(v -> {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                } catch (Exception e) {
                    Toast.makeText(this, "לא ניתן לפתוח את יד2", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Load everything
        String uid = FirebaseAuth.getInstance().getUid();
        vm.loadDriverAndManual(uid);
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_car_details;
    }

    // Simple helper to enable/disable a button with opacity (like DIY style)
    private void setButtonEnabled(MaterialButton btn, boolean enabled) {
        if (btn == null) return;
        btn.setEnabled(enabled);
        btn.setAlpha(enabled ? 1f : 0.5f);
    }
}
