package com.example.drive_kit.View;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.drive_kit.Model.VideoItem;
import com.example.drive_kit.R;
import com.example.drive_kit.View.Adapter.IssueButtonsAdapter;
import com.example.drive_kit.ViewModel.VideosViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.List;
/**
 * DIYIssuesActivity
 *
 * Displays a list of common car issues (DIY troubleshooting).
 *
 * Responsibilities:
 * - Show issues list (RecyclerView)
 * - Allow user to select issue → opens video
 * - Handle car manual (PDF)
 * - Display selected filters (manufacturer, model, year range)
 *
 * Architecture (MVVM):
 * - Activity: UI + user interaction
 * - ViewModel (VideosViewModel):
 *     - Loads issues
 *     - Loads video links
 *     - Loads manual PDF
 */
public class DIYIssuesActivity extends BaseLoggedInActivity {
    private static final String TAG = "DIY_ISSUES"; // Log tag for debugging
    private VideosViewModel vm; // ViewModel handling data
    private String manufacturer; // Selected manufacturer
    private String model; // Selected model
    private String yearRange; // Selected year range
    private IssueButtonsAdapter adapter; // Adapter for issues list
    private MaterialButton btnDownloadManual; // Button for downloading/viewing car manual

    /**
     * Initializes UI, ViewModel, observers and loads data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ===== Get filter data from Intent =====
        Intent i = getIntent();
        manufacturer = safe(i.getStringExtra("manufacturer"));
        model = safe(i.getStringExtra("model"));
        yearRange = safe(i.getStringExtra("yearRange"));

        // ===== Show selected filters =====
        TextView subtitle = findViewById(R.id.subtitleFilter);
        subtitle.setText(manufacturer + "  |  " + model + "  |  " + yearRange);

        // ===== Manual button =====
        btnDownloadManual = findViewById(R.id.btnDownloadManual);
        setManualButtonEnabled(false);

        // ===== RecyclerView setup =====
        RecyclerView rv = findViewById(R.id.issuesRecycler);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new IssueButtonsAdapter(this::onIssueClicked);
        rv.setAdapter(adapter);

        // ===== ViewModel =====
        vm = new ViewModelProvider(this).get(VideosViewModel.class);
        vm.loadManual(manufacturer, model, yearRange); // Initial manual load (can be redundant but safe)

        // ===== Issues list observer =====
        vm.getIssues().observe(this, this::renderIssues);

        /**
         * Selected video observer
         * Opens video using external app/browser
         */
        vm.getSelectedVideo().observe(this, video -> {
            if (video == null) {
                Toast.makeText(this, "לא נמצא סרטון לתקלה הזו", Toast.LENGTH_SHORT).show();
                return;
            }
            String url = video.getUrl();
            if (url == null || url.trim().isEmpty()) {
                Toast.makeText(this, "אין לינק לסרטון", Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        });

        // ===== Error handling =====
        vm.getError().observe(this, err -> {
            if (err != null && !err.trim().isEmpty()) {
                Log.e(TAG, "VM error: " + err);
                Toast.makeText(this, err, Toast.LENGTH_LONG).show();
            }
        });

        // ===== Manual loading state =====
        vm.getManualLoading().observe(this, loading -> {
            if (loading != null && loading) {
                btnDownloadManual.setText("טוען ספר רכב...");
                setManualButtonEnabled(false);
            } else {
                btnDownloadManual.setText("ספר רכב (PDF)");
            }
        });

        /**
         * Manual PDF URL observer
         *
         * Behavior:
         * - If URL exists → enable button
         * - If not → disable button
         */
        vm.getManualPdfUrl().observe(this, url -> {
            Log.d("DIY_MANUAL", "manual url = " + url);

            if (url == null || url.trim().isEmpty()) {
                btnDownloadManual.setEnabled(false);
                btnDownloadManual.setOnClickListener(null);
                return;
            }

            btnDownloadManual.setEnabled(true);
            btnDownloadManual.setOnClickListener(v -> {
                Intent open = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(open);
            });
        });

        // ===== Manual error =====
        vm.getManualError().observe(this, err -> {
            if (err != null && !err.trim().isEmpty()) {
                Log.e("DIY_MANUAL", "manual error = " + err);
            }
        });

        // ===== Initial data loading =====
        vm.loadIssues(manufacturer, model, yearRange);
        vm.loadManual(manufacturer, model, yearRange);
    }

    /**
     * Provides layout for BaseLoggedInActivity.
     */
    @Override
    protected int getContentLayoutId() {
        return R.layout.diy_issues;
    }

    /**
     * Renders issues list into RecyclerView.
     *
     * @param issues list of issues (videos)
     */
    private void renderIssues(List<VideoItem> issues) {
        adapter.setItems(issues);
        if (issues == null || issues.isEmpty()) {
            Toast.makeText(this, "לא נמצאו תקלות למסנן הזה", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handles click on a specific issue.
     *
     * Flow:
     * - Extract issueKey
     * - Validate
     * - Ask ViewModel to load video
     */
    private void onIssueClicked(VideoItem item) {
        if (item == null) return;

        String issueKey = safe(item.getIssueKey());
        if (issueKey.isEmpty()) {
            Toast.makeText(this, "תקלה לא תקינה (אין issueKey)", Toast.LENGTH_SHORT).show();
            return;
        }

        vm.loadVideo(manufacturer, model, yearRange, issueKey);
    }

    /**
     * Enables/disables manual button visually.
     */
    private void setManualButtonEnabled(boolean enabled) {
        btnDownloadManual.setEnabled(enabled);
        btnDownloadManual.setAlpha(enabled ? 1f : 0.5f);
    }

    /**
     * Safely trims string (null-safe).
     */
    private String safe(String s) {
        return (s == null) ? "" : s.trim();
    }
}
