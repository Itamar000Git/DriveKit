package com.example.drive_kit.View.Insurance_user;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.drive_kit.R;
import com.example.drive_kit.View.Adapter.InsuranceInquiriesAdapter;
import com.example.drive_kit.ViewModel.Insurance_user_ViewModel.InsuranceInquiriesViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * InsuranceInquiriesActivity
 *
 * Displays a list of inquiries for an insurance company.
 *
 * Responsibilities:
 * - Show list of inquiries (RecyclerView)
 * - Support switching between:
 *   - "new" inquiries (default)
 *   - "contacted" inquiries (old/handled)
 * - Allow marking inquiries as "contacted"
 *
 * Architecture (MVVM):
 * - Activity: UI + user interaction
 * - ViewModel: loads data and handles updates
 *
 * Data:
 * - Each inquiry is represented as Map<String, Object> (Firestore-like structure)
 */
public class InsuranceInquiriesActivity extends BaseInsuranceActivity {
    private RecyclerView recyclerView; // RecyclerView for displaying inquiries
    private InsuranceInquiriesAdapter adapter; // Adapter for inquiries list
    private InsuranceInquiriesViewModel vm; // ViewModel handling inquiries logic
    private TextView titleText; // Title text showing current list type
    private Button btnOldInquiries; // Button to switch to old (contacted) inquiries
    private final List<Map<String, Object>> data = new ArrayList<>(); //  Data source for RecyclerView
    private String companyDocId; // Firestore document ID of the company
    /**
     * Current list status:
     * - "new" (default)
     * - "contacted"
     */
    private String listStatus = "new";

    /**
     * Initializes UI, ViewModel, observers, and list behavior.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Bind views
        recyclerView = findViewById(R.id.inquiriesRecycler);
        titleText = findViewById(R.id.titleText);
        btnOldInquiries = findViewById(R.id.btnOldInquiries);

        // Set RecyclerView layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize ViewModel
        vm = new ViewModelProvider(this).get(InsuranceInquiriesViewModel.class);

        // Get companyDocId safely
        companyDocId = safe(getIntent().getStringExtra("insuranceCompanyId"));
        if (companyDocId.isEmpty()) {
            Toast.makeText(this, "חסר insuranceCompanyId", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Setup adapter based on current list status
        setupAdapterForCurrentStatus();

        // Observe inquiries data
        vm.getInquiries().observe(this, inquiries -> {
            data.clear();
            if (inquiries != null) data.addAll(inquiries);
            adapter.notifyDataSetChanged();

            if (data.isEmpty()) {
                if ("new".equals(listStatus)) {
                    Toast.makeText(this, "אין פניות חדשות", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "אין פניות שטופלו", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Observe toast messages from ViewModel
        vm.getToastMessage().observe(this, msg -> {
            if (msg != null && !msg.trim().isEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        });

        // initial load
        switchList("new");

        if (btnOldInquiries != null) {
            btnOldInquiries.setOnClickListener(v -> switchList("contacted"));

            btnOldInquiries.setOnLongClickListener(v -> {
                switchList("new");
                return true;
            });
        }
    }

    /**
     * Safely trims a string and avoids null values.
     *
     * @param s input string
     * @return trimmed string or empty string if null
     */
    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    /**
     * Switches between inquiry lists ("new" / "contacted").
     *
     * Updates:
     * - Current status
     * - UI title
     * - Adapter configuration
     * - Reloads data from ViewModel
     *
     * @param status desired list status
     */
    private void switchList(String status) {
        listStatus = (status == null || status.trim().isEmpty())
                ? "new"
                : status.trim().toLowerCase();

        if ("contacted".equals(listStatus)) {
            titleText.setText("פניות שטופלו");
        } else {
            titleText.setText("פניות חדשות");
        }

        setupAdapterForCurrentStatus();

        vm.load(companyDocId, listStatus);
    }

    /**
     * Creates and sets adapter based on current list status.
     *
     * Behavior:
     * - If "new" → show "mark as contacted" button
     * - If "contacted" → hide button
     */
    private void setupAdapterForCurrentStatus() {
        boolean showMarkButton = "new".equals(listStatus);

        adapter = new InsuranceInquiriesAdapter(
                data,
                docId -> vm.markContacted(docId),
                showMarkButton
        );
        recyclerView.setAdapter(adapter);
    }

    /**
     * Returns layout resource for this screen.
     */
    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_insurance_inquiries;
    }
}