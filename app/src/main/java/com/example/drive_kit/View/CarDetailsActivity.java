
package com.example.drive_kit.View;

import android.app.DownloadManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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

    // Insurance rows
    private TextView tvInsuranceCompanyValue;
    private TextView tvInsuranceCompanyIdValue;

    // Legacy view (hidden in XML)
    private TextView info;

    // Actions
    private MaterialButton btnDownloadManual;   // ספר רכב (PDF) - הורדה
    private MaterialButton btnDownloadCarBook;  // חפש בספר הרכב - פתיחה באפליקציה
    private MaterialButton btnOpenYad2;

    private CarDetailsViewModel vm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bindViews();
        setupDefaultUi();

        vm = new ViewModelProvider(this).get(CarDetailsViewModel.class);

        // Buttons start disabled until URLs arrive
        setButtonEnabled(btnDownloadManual, false);
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

    private void bindViews() {
        tvCarNumberValue = findViewById(R.id.tvCarNumberValue);
        tvManufacturerValue = findViewById(R.id.tvManufacturerValue);
        tvModelValue = findViewById(R.id.tvModelValue);
        tvYearValue = findViewById(R.id.tvYearValue);

        info = findViewById(R.id.carInfo);

        btnDownloadManual = findViewById(R.id.btnDownloadManual);
        btnDownloadCarBook = findViewById(R.id.btnDownloadCarBook);
        btnOpenYad2 = findViewById(R.id.btnOpenYad2);
    }

    private void setupDefaultUi() {
        if (title != null) title.setText("פרטי הרכב שלי");

        setTextOrDash(tvCarSubtitle, "-");
        setTextOrDash(tvCarNumberValue, "-");
        setTextOrDash(tvManufacturerValue, "-");
        setTextOrDash(tvModelValue, "-");
        setTextOrDash(tvYearValue, "-");

        setTextOrDash(tvInsuranceCompanyValue, "-");
        setTextOrDash(tvInsuranceCompanyIdValue, "-");

        if (ivCarImage != null) {
            ivCarImage.setImageResource(R.drawable.ic_profile_placeholder);
        }

        if (btnDownloadManual != null) btnDownloadManual.setText("ספר רכב (PDF)");
        if (btnDownloadCarBook != null) btnDownloadCarBook.setText("חיפוש בספר הרכב");
    }

    private void observeViewModel() {
        // Details
        vm.getCarSubtitle().observe(this, v -> setTextOrDash(tvCarSubtitle, v));
        vm.getCarNumber().observe(this, v -> setTextOrDash(tvCarNumberValue, v));
        vm.getManufacturer().observe(this, v -> setTextOrDash(tvManufacturerValue, v));
        vm.getModel().observe(this, v -> setTextOrDash(tvModelValue, v));
        vm.getYear().observe(this, v -> setTextOrDash(tvYearValue, v));

        // Insurance
        vm.getInsuranceCompanyName().observe(this, v -> setTextOrDash(tvInsuranceCompanyValue, v));
        vm.getInsuranceCompanyId().observe(this, v -> setTextOrDash(tvInsuranceCompanyIdValue, v));

        // Legacy info
        vm.getInfoText().observe(this, text -> {
            if (info != null) info.setText(text == null ? "" : text);
        });

        vm.getScreenError().observe(this, err -> {
            if (err != null && !err.trim().isEmpty()) {
                Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
            }
        });

        // Image (base64 first, uri fallback)
        vm.getCarImageBase64().observe(this, base64 -> {
            boolean loaded = tryLoadCarBase64Into(ivCarImage, base64);
            if (!loaded) {
                String uri = vm.getCarImageUri().getValue();
                tryLoadCarUriInto(ivCarImage, uri);
            }
        });

        vm.getCarImageUri().observe(this, uri -> {
            String base64 = vm.getCarImageBase64().getValue();
            if (base64 != null && !base64.trim().isEmpty()) return;
            tryLoadCarUriInto(ivCarImage, uri);
        });

        // ========= ספר רכב =========

        vm.getManualLoading().observe(this, loading -> {
            boolean isLoading = (loading != null && loading);

            // UX: בזמן טעינה ננטרל את שני הכפתורים (PDF + חיפוש)
            if (btnDownloadManual != null) {
                btnDownloadManual.setText(isLoading ? "טוען ספר רכב..." : "ספר רכב (PDF)");
                setButtonEnabled(btnDownloadManual, false);
            }
            if (btnDownloadCarBook != null) {
                btnDownloadCarBook.setText(isLoading ? "טוען ספר רכב..." : "חיפוש בספר הרכב");
                setButtonEnabled(btnDownloadCarBook, false);
            }
        });

        // ✅ אותו URL מפעיל שני כפתורים:
        // btnDownloadManual -> הורדה
        // btnDownloadCarBook -> פתיחה באפליקציה (כמו קודם)
        vm.getManualPdfUrl().observe(this, url -> {
            Log.d(TAG, "manual url=" + url);

            if (url == null || url.trim().isEmpty()) {
                setButtonEnabled(btnDownloadManual, false);
                if (btnDownloadManual != null) btnDownloadManual.setOnClickListener(null);

                setButtonEnabled(btnDownloadCarBook, false);
                if (btnDownloadCarBook != null) btnDownloadCarBook.setOnClickListener(null);
                return;
            }

            // PDF download button
            setButtonEnabled(btnDownloadManual, true);
            if (btnDownloadManual != null) {
                btnDownloadManual.setText("ספר רכב (PDF)");
                btnDownloadManual.setOnClickListener(v -> downloadPdf(url));
            }

            // Search/open-in-app button (restore old behavior)
            setButtonEnabled(btnDownloadCarBook, true);
            if (btnDownloadCarBook != null) {
                btnDownloadCarBook.setText("חיפוש בספר הרכב");
                btnDownloadCarBook.setOnClickListener(v -> openManualInApp(url));
            }
        });

        vm.getManualError().observe(this, err -> {
            if (err != null && !err.trim().isEmpty()) {
                Log.e(TAG, "manual error=" + err);
                Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
            }
        });

        // Yad2 stays external
        vm.getYad2Url().observe(this, url -> {
            Log.d(TAG, "yad2 url=" + url);

            if (url == null || url.trim().isEmpty()) {
                setButtonEnabled(btnOpenYad2, false);
                if (btnOpenYad2 != null) btnOpenYad2.setOnClickListener(null);
                return;
            }

            setButtonEnabled(btnOpenYad2, true);
            if (btnOpenYad2 != null) {
                btnOpenYad2.setOnClickListener(v -> openUrl(url, "לא ניתן לפתוח את יד2"));
            }
        });
    }

    // ✅ כמו בקוד הישן שעבד: פתיחה בתוך האפליקציה (PdfViewerActivity)
    private void openManualInApp(String url) {
        try {
            Intent i = new Intent(this, PdfViewerActivity.class);
            i.putExtra(PdfViewerActivity.EXTRA_PDF_URL, url);
            i.putExtra(PdfViewerActivity.EXTRA_TITLE, "ספר רכב");
            startActivity(i);
        } catch (Exception e) {
            Toast.makeText(this, "לא ניתן לפתוח את ספר הרכב", Toast.LENGTH_SHORT).show();
        }
    }

    // ✅ Download PDF
    private void downloadPdf(String url) {
        try {
            Uri uri = Uri.parse(url);

            DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            if (dm == null) {
                openUrl(url, "לא ניתן להוריד את ספר הרכב");
                return;
            }

            DownloadManager.Request req = new DownloadManager.Request(uri);
            req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            req.setAllowedOverRoaming(true);
            req.setAllowedOverMetered(true);

            String fileName = "DriveKit_Car_Manual.pdf";
            req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

            dm.enqueue(req);
            Toast.makeText(this, "ההורדה החלה…", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "downloadPdf failed", e);
            openUrl(url, "לא ניתן להוריד את ספר הרכב");
        }
    }

    // -------------------- Helpers --------------------

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

    private boolean tryLoadCarBase64Into(ShapeableImageView target, String base64) {
        if (target == null) return false;
        if (base64 == null || base64.trim().isEmpty()) return false;

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