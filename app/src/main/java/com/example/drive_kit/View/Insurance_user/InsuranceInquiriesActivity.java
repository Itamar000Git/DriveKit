//package com.example.drive_kit.View;
//
//import android.os.Bundle;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.drive_kit.Data.Repository.InsuranceInquiryRepository;
//import com.example.drive_kit.R;
//import com.example.drive_kit.View.Adapter.InsuranceInquiriesAdapter;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
///**
// * InsuranceInquiriesActivity
// *
// * Purpose:
// * - Displays all inquiries that belong to a specific insurance company.
// * - Uses a RecyclerView with InsuranceInquiriesAdapter.
// * - Supports marking an inquiry as "contacted" from the list.
// *
// * Data flow:
// * - companyId is received via Intent extra ("insuranceCompanyId").
// * - Repository fetches inquiries for that company.
// * - Adapter renders the inquiry rows.
// * - On row action, repository updates status to "contacted",
// *   then the list is refreshed.
// */
//public class InsuranceInquiriesActivity extends AppCompatActivity {
//
//    // RecyclerView that displays inquiry items
//    private RecyclerView recyclerView;
//
//    // Adapter that binds inquiry data to item views
//    private InsuranceInquiriesAdapter adapter;
//
//    // In-memory list used as adapter data source
//    private final List<Map<String, Object>> data = new ArrayList<>();
//
//    // Repository for loading/updating insurance inquiries
//    private final InsuranceInquiryRepository repo = new InsuranceInquiryRepository();
//
//    // Company id used to filter inquiries
//    private String companyId;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        // Inflate activity layout
//        setContentView(R.layout.activity_insurance_inquiries);
//
//        // Bind RecyclerView from XML
//        recyclerView = findViewById(R.id.inquiriesRecycler);
//
//        // Use a vertical linear list layout
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//
//        // Create adapter and define item action callback:
//        // when user triggers "mark contacted" on an item, update DB and reload list
//        adapter = new InsuranceInquiriesAdapter(data, docId -> {
//            repo.markAsContacted(docId, new InsuranceInquiryRepository.InquiryCallback() {
//                @Override
//                public void onSuccess() {
//                    // Update success feedback
//                    Toast.makeText(InsuranceInquiriesActivity.this, "עודכן ל- contacted", Toast.LENGTH_SHORT).show();
//
//                    // Refresh list after status change
//                    loadData();
//                }
//
//                @Override
//                public void onError(Exception e) {
//                    // Update failure feedback
//                    Toast.makeText(InsuranceInquiriesActivity.this, "שגיאה בעדכון", Toast.LENGTH_SHORT).show();
//                }
//            });
//        });
//
//        // Attach adapter to RecyclerView
//        recyclerView.setAdapter(adapter);
//
//        // Read company id from Intent (required for query)
//        companyId = getIntent().getStringExtra("insuranceCompanyId");
//
//        // Validate required argument
//        if (companyId == null || companyId.trim().isEmpty()) {
//            Toast.makeText(this, "חסר insuranceCompanyId", Toast.LENGTH_LONG).show();
//            finish();
//            return;
//        }
//
//        // Initial data load
//        loadData();
//    }
//
//    /**
//     * Loads all inquiries for the current insurance company
//     * and updates the RecyclerView data source.
//     */
//    private void loadData() {
//        repo.loadInquiriesForCompany(companyId, new InsuranceInquiryRepository.LoadInquiriesCallback() {
//            @Override
//            public void onSuccess(List<Map<String, Object>> inquiries) {
//                // Replace existing list content
//                data.clear();
//                data.addAll(inquiries);
//
//                // Notify adapter that data changed
//                adapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onError(Exception e) {
//                // Build readable error message
//                String m = (e != null && e.getMessage() != null) ? e.getMessage() : "שגיאה בטעינת פניות";
//
//                // Show load error
//                Toast.makeText(InsuranceInquiriesActivity.this, m, Toast.LENGTH_LONG).show();
//            }
//        });
//    }
//}


package com.example.drive_kit.View.Insurance_user;

import android.os.Bundle;
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
 * Purpose:
 * - Displays all inquiries that belong to a specific insurance company.
 * - Uses a RecyclerView with InsuranceInquiriesAdapter.
 * - Supports marking an inquiry as "contacted" from the list.
 *
 * MVVM:
 * - Activity = UI only (RecyclerView, Adapter, Toast, finish)
 * - ViewModel = loads data + runs actions (mark contacted)
 * - Repository = Firestore calls
 */
public class InsuranceInquiriesActivity extends BaseInsuranceActivity {

    private RecyclerView recyclerView;
    private InsuranceInquiriesAdapter adapter;

    // We keep the same list reference the adapter uses (no behavior change)
    private final List<Map<String, Object>> data = new ArrayList<>();

    private InsuranceInquiriesViewModel vm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setContentView(R.layout.activity_insurance_inquiries);
        getContentLayoutId();


        recyclerView = findViewById(R.id.inquiriesRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        vm = new ViewModelProvider(this).get(InsuranceInquiriesViewModel.class);

        // Adapter action: delegate business work to the ViewModel
        adapter = new InsuranceInquiriesAdapter(data, docId -> vm.markContacted(docId));
        recyclerView.setAdapter(adapter);

        // Observe list updates from ViewModel
        vm.getInquiries().observe(this, inquiries -> {
            data.clear();
            if (inquiries != null) data.addAll(inquiries);
            adapter.notifyDataSetChanged();
        });

        // Observe messages from ViewModel
        vm.getToastMessage().observe(this, msg -> {
            if (msg != null && !msg.trim().isEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        });

        String companyId = getIntent().getStringExtra("insuranceCompanyId");

        if (companyId == null || companyId.trim().isEmpty()) {
            Toast.makeText(this, "חסר insuranceCompanyId", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Initial load through ViewModel
        vm.load(companyId);
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_insurance_inquiries;
    }
}

