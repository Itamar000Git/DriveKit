package com.example.drive_kit.View;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import com.example.drive_kit.Data.ManualQaEngine;
import com.example.drive_kit.Data.ManualTextExtractor;
import com.example.drive_kit.R;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PdfViewerActivity extends AppCompatActivity {

    public static final String EXTRA_PDF_URL = "extra_pdf_url";
    public static final String EXTRA_TITLE   = "extra_title";

    private static final String TAG = "PDF_VIEWER";
    private static final String TAG_QA = "MANUAL_QA";

    private PDFView pdfView;
    private View loading;
    private TextView titleTv;
    private MaterialButton btnBack;

    // QA UI
    private TextInputEditText etQuestion;
    private MaterialButton btnAsk;
    private TextView tvAnswer;
    private NestedScrollView answerScroll;

    // Manual text cache (in-memory)
    private volatile String manualText = "";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);

        // PDF bindings
        pdfView = findViewById(R.id.pdfView);
        loading = findViewById(R.id.pdfLoading);
        titleTv = findViewById(R.id.pdfTitle);
        btnBack = findViewById(R.id.btnBack);

        // QA bindings
        etQuestion = findViewById(R.id.etQuestion);
        btnAsk = findViewById(R.id.btnAsk);
        tvAnswer = findViewById(R.id.tvAnswer);
        answerScroll = findViewById(R.id.answerScroll);

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // Disable QA until text is extracted
        setQaEnabled(false, "טוען ספר כדי לאפשר חיפוש…");

        if (btnAsk != null) {
            btnAsk.setOnClickListener(v -> onAskClicked());
        }

        String url = getIntent().getStringExtra(EXTRA_PDF_URL);
        String title = getIntent().getStringExtra(EXTRA_TITLE);

        if (titleTv != null) titleTv.setText(isBlank(title) ? "ספר רכב" : title.trim());

        if (isBlank(url)) {
            Toast.makeText(this, "Missing PDF url", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        showLoading(true);
        downloadAndOpenPdf(url.trim());
    }

    private void onAskClicked() {
        String q = (etQuestion == null || etQuestion.getText() == null)
                ? ""
                : etQuestion.getText().toString().trim();

        ManualQaEngine.QaResult res = ManualQaEngine.answer(q, manualText);

        if (tvAnswer != null) tvAnswer.setText(res.answer);

        // ✅ Scroll answer box to bottom so user sees the end if long
        if (answerScroll != null) {
            answerScroll.post(() -> answerScroll.fullScroll(View.FOCUS_DOWN));
        }

        Log.d(TAG_QA, "ask='" + q + "' textLen=" + (manualText == null ? 0 : manualText.length()));
    }

    private void downloadAndOpenPdf(String pdfUrl) {
        executor.execute(() -> {
            try {
                File file = downloadToCache(pdfUrl);

                runOnUiThread(() -> {
                    showLoading(false);

                    if (file == null || !file.exists()) {
                        Toast.makeText(this, "לא ניתן לפתוח את ה-PDF", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    pdfView.fromFile(file)
                            .enableSwipe(true)
                            .swipeHorizontal(false)
                            .enableDoubletap(true)
                            .onError(t -> {
                                Log.e(TAG, "pdf render error", t);
                                Toast.makeText(this, "שגיאה בהצגת ה-PDF", Toast.LENGTH_SHORT).show();
                            })
                            .load();

                    // Start extracting text for QA (offline agent)
                    extractTextForQa(file);
                });

            } catch (Exception e) {
                Log.e(TAG, "download/open failed", e);
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(this, "שגיאה בטעינת ספר הרכב", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private void extractTextForQa(File pdfFile) {
        setQaEnabled(false, "מכין חיפוש בתוך הספר…");

        ManualTextExtractor.extractTextAsync(this, pdfFile, new ManualTextExtractor.Callback() {
            @Override
            public void onSuccess(String text) {
                manualText = (text == null) ? "" : text;
                Log.d(TAG_QA, "extracted text len=" + manualText.length());

                runOnUiThread(() -> {
                    if (manualText.trim().isEmpty()) {
                        setQaEnabled(false, "הספר נראה ללא טקסט (אולי סריקה/תמונה).");
                    } else {
                        setQaEnabled(true, "אפשר לשאול שאלה ולחפש בספר 👇");
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG_QA, "extract failed", e);
                manualText = "";
                runOnUiThread(() -> setQaEnabled(false, "לא הצלחתי לחלץ טקסט מהספר."));
            }
        });
    }

    private void setQaEnabled(boolean enabled, String statusText) {
        if (btnAsk != null) {
            btnAsk.setEnabled(enabled);
            btnAsk.setAlpha(enabled ? 1f : 0.5f);
        }
        if (tvAnswer != null) {
            tvAnswer.setText(statusText == null ? "" : statusText);
        }

        // Optional: scroll to top when status changes
        if (answerScroll != null) {
            answerScroll.post(() -> answerScroll.fullScroll(View.FOCUS_UP));
        }
    }

    private File downloadToCache(String pdfUrl) throws Exception {
        HttpURLConnection connection = null;
        InputStream input = null;
        FileOutputStream output = null;

        try {
            URL url = new URL(pdfUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(20000);
            connection.connect();

            int code = connection.getResponseCode();
            if (code < 200 || code >= 300) {
                throw new RuntimeException("HTTP " + code);
            }

            input = new BufferedInputStream(connection.getInputStream());

            File file = new File(getCacheDir(), "car_manual.pdf");
            output = new FileOutputStream(file);

            byte[] buffer = new byte[8 * 1024];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            output.flush();

            return file;
        } finally {
            try { if (output != null) output.close(); } catch (Exception ignored) {}
            try { if (input != null) input.close(); } catch (Exception ignored) {}
            if (connection != null) connection.disconnect();
        }
    }

    private void showLoading(boolean show) {
        if (loading != null) loading.setVisibility(show ? View.VISIBLE : View.GONE);
        if (pdfView != null) pdfView.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
