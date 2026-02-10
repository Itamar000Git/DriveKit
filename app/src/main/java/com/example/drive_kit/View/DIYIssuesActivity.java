//package com.example.drive_kit.View;
//
//import android.content.Intent;
//import android.net.Uri;
//import android.os.Bundle;
//import android.util.Log;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.lifecycle.ViewModelProvider;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.drive_kit.Model.VideoItem;
//import com.example.drive_kit.R;
//import com.example.drive_kit.View.Adapter.IssueButtonsAdapter;
//import com.example.drive_kit.ViewModel.VideosViewModel;
//
//import java.util.List;
//
///**
// * DIYIssuesActivity shows a list of issues (buttons) for the chosen filter:
// * manufacturer + model + yearRange
// *
// * MVVM:
// * Activity -> VideosViewModel -> VideosRepository -> Firestore
// */
//public class DIYIssuesActivity extends BaseLoggedInActivity {
//
//    private VideosViewModel vm;
//
//    // Current filter (comes from previous screen)
//    private String manufacturer;
//    private String model;
//    private String yearRange;
//
//    private IssueButtonsAdapter adapter;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        //setContentView(R.layout.diy_issues);
//        //getContentLayoutId();
//
//
//        // --- 1) Read filter from Intent ---
//        Intent i = getIntent();
//        manufacturer = safe(i.getStringExtra("manufacturer"));
//        model = safe(i.getStringExtra("model"));
//        yearRange = safe(i.getStringExtra("yearRange"));
//
//        // --- 2) UI setup ---
//        TextView subtitle = findViewById(R.id.subtitleFilter);
//        subtitle.setText(manufacturer + "  |  " + model + "  |  " + yearRange);
//
//        RecyclerView rv = findViewById(R.id.issuesRecycler);
//        rv.setLayoutManager(new LinearLayoutManager(this));
//
//        adapter = new IssueButtonsAdapter(this::onIssueClicked);
//        rv.setAdapter(adapter);
//
//        // --- 3) ViewModel ---
//        vm = new ViewModelProvider(this).get(VideosViewModel.class);
//
//        // Observe issues list from DB
//        vm.getIssues().observe(this, this::renderIssues);
//
//        // Observe selected video (single doc) -> open link
//        vm.getSelectedVideo().observe(this, video -> {
//            if (video == null) {
//                Toast.makeText(this, "לא נמצא סרטון לתקלה הזו", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            String url = video.getUrl();
//            if (url == null || url.trim().isEmpty()) {
//                Toast.makeText(this, "אין לינק לסרטון", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            // Open the URL in browser
//            Intent open = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//            startActivity(open);
//        });
//
//        // Observe errors from ViewModel
//        vm.getError().observe(this, err -> {
//            if (err != null && !err.trim().isEmpty()) {
//                Log.e("DIY_ISSUES", "VM error: " + err);
//                Toast.makeText(this, err, Toast.LENGTH_LONG).show();
//            }
//        });
//
//        // --- 4) Load issues for this filter ---
//        vm.loadIssues(manufacturer, model, yearRange);
//    }
//
//    @Override
//    protected int getContentLayoutId() {
//        return R.layout.diy_issues;
//    }
//
//    /**
//     * Show issues as buttons.
//     */
//    private void renderIssues(List<VideoItem> issues) {
//        adapter.setItems(issues);
//
//        if (issues == null || issues.isEmpty()) {
//            Toast.makeText(this, "לא נמצאו תקלות למסנן הזה", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    /**
//     * User clicked an issue button.
//     * We load the exact video doc from Firestore by full filter + issueKey.
//     */
//    private void onIssueClicked(VideoItem item) {
//        if (item == null) return;
//
//        String issueKey = safe(item.getIssueKey());
//
//        Log.d("DIY_ISSUES", "Issue clicked: issueKey=" + issueKey
//                + ", nameHe=" + item.getIssueNameHe()
//                + ", url(from list)=" + item.getUrl());
//
//        if (issueKey.isEmpty()) {
//            Toast.makeText(this, "תקלה לא תקינה (אין issueKey)", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // Ask ViewModel to fetch the single video doc (with the URL)
//        vm.loadVideo(manufacturer, model, yearRange, issueKey);
//    }
//
//    /**
//     * Null-safe string helper.
//     */
//    private String safe(String s) {
//        return (s == null) ? "" : s.trim();
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.drive_kit.Model.VideoItem;
import com.example.drive_kit.R;
import com.example.drive_kit.View.Adapter.IssueButtonsAdapter;
import com.example.drive_kit.ViewModel.VideosViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class DIYIssuesActivity extends BaseLoggedInActivity {

    private static final String TAG = "DIY_ISSUES";

    private VideosViewModel vm;

    private String manufacturer;
    private String model;
    private String yearRange;

    private IssueButtonsAdapter adapter;

    private MaterialButton btnDownloadManual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = getIntent();
        manufacturer = safe(i.getStringExtra("manufacturer"));
        model = safe(i.getStringExtra("model"));
        yearRange = safe(i.getStringExtra("yearRange"));

        TextView subtitle = findViewById(R.id.subtitleFilter);
        subtitle.setText(manufacturer + "  |  " + model + "  |  " + yearRange);

        btnDownloadManual = findViewById(R.id.btnDownloadManual);
        setManualButtonEnabled(false);

        RecyclerView rv = findViewById(R.id.issuesRecycler);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new IssueButtonsAdapter(this::onIssueClicked);
        rv.setAdapter(adapter);

        vm = new ViewModelProvider(this).get(VideosViewModel.class);
        vm.loadManual(manufacturer, model, yearRange);


        // Issues list
        vm.getIssues().observe(this, this::renderIssues);

        // Selected video -> open
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

        // General errors
        vm.getError().observe(this, err -> {
            if (err != null && !err.trim().isEmpty()) {
                Log.e(TAG, "VM error: " + err);
                Toast.makeText(this, err, Toast.LENGTH_LONG).show();
            }
        });

        // Manual observers
        vm.getManualLoading().observe(this, loading -> {
            // אופציונלי: לשנות טקסט/להראות loader
            if (loading != null && loading) {
                btnDownloadManual.setText("טוען ספר רכב...");
                setManualButtonEnabled(false);
            } else {
                btnDownloadManual.setText("ספר רכב (PDF)");
            }
        });

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

        vm.getManualError().observe(this, err -> {
            if (err != null && !err.trim().isEmpty()) {
                Log.e("DIY_MANUAL", "manual error = " + err);
            }
        });

        // Load data
        vm.loadIssues(manufacturer, model, yearRange);
        vm.loadManual(manufacturer, model, yearRange);
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.diy_issues;
    }

    private void renderIssues(List<VideoItem> issues) {
        adapter.setItems(issues);
        if (issues == null || issues.isEmpty()) {
            Toast.makeText(this, "לא נמצאו תקלות למסנן הזה", Toast.LENGTH_SHORT).show();
        }
    }

    private void onIssueClicked(VideoItem item) {
        if (item == null) return;

        String issueKey = safe(item.getIssueKey());
        if (issueKey.isEmpty()) {
            Toast.makeText(this, "תקלה לא תקינה (אין issueKey)", Toast.LENGTH_SHORT).show();
            return;
        }

        vm.loadVideo(manufacturer, model, yearRange, issueKey);
    }

    private void setManualButtonEnabled(boolean enabled) {
        btnDownloadManual.setEnabled(enabled);
        btnDownloadManual.setAlpha(enabled ? 1f : 0.5f);
    }

    private String safe(String s) {
        return (s == null) ? "" : s.trim();
    }
}
