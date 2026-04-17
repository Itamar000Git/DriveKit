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


public class InsuranceInquiriesActivity extends BaseInsuranceActivity {

    private RecyclerView recyclerView;
    private InsuranceInquiriesAdapter adapter;
    private InsuranceInquiriesViewModel vm;

    private TextView titleText;
    private Button btnOldInquiries;

    private final List<Map<String, Object>> data = new ArrayList<>();


    private String companyDocId;

    private String listStatus = "new";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        recyclerView = findViewById(R.id.inquiriesRecycler);
        titleText = findViewById(R.id.titleText);
        btnOldInquiries = findViewById(R.id.btnOldInquiries);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        vm = new ViewModelProvider(this).get(InsuranceInquiriesViewModel.class);


        companyDocId = safe(getIntent().getStringExtra("insuranceCompanyId"));

        if (companyDocId.isEmpty()) {
            Toast.makeText(this, "חסר insuranceCompanyId", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setupAdapterForCurrentStatus();

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

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

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

    private void setupAdapterForCurrentStatus() {
        boolean showMarkButton = "new".equals(listStatus);

        adapter = new InsuranceInquiriesAdapter(
                data,
                docId -> vm.markContacted(docId),
                showMarkButton
        );
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_insurance_inquiries;
    }
}