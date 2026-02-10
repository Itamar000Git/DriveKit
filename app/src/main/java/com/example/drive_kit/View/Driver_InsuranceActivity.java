//package com.example.drive_kit.View;
//
//import android.os.Bundle;
//import android.widget.Toast;
//
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.drive_kit.Data.Repository.InsuranceCompaniesRepository;
//import com.example.drive_kit.Data.Repository.InsuranceInquiryRepository;
//import com.example.drive_kit.R;
//import com.example.drive_kit.View.Adapter.InsuranceListAdapter;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//
//import java.util.List;
//
//public class InsuranceActivity extends BaseLoggedInActivity {
//
//    private RecyclerView recycler;
//    private InsuranceInquiryRepository inquiryRepo;
//    private String uid;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        //setContentView(R.layout.insurance_activity);
//
//        recycler = findViewById(R.id.insuranceRecycler);
//        recycler.setLayoutManager(new LinearLayoutManager(this));
//
//        inquiryRepo = new InsuranceInquiryRepository();
//
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        uid = (user == null) ? null : user.getUid();
//
//        InsuranceCompaniesRepository repo = new InsuranceCompaniesRepository();
//
//        repo.loadCarCompanies(new InsuranceCompaniesRepository.Callback() {
//            @Override
//            public void onResult(List<com.example.drive_kit.View.InsuranceCompany> companies) {
//
//                InsuranceListAdapter adapter = new InsuranceListAdapter(companies, company -> {
//
//                    InsuranceBottomSheet sheet = InsuranceBottomSheet.newInstance(
//                            company.getId(),
//                            company.getName(),
//                            company.getPhone(),
//                            company.getEmail(),
//                            company.getWebsite(),
//                            company.isPartner()
//                    );
//
//
//                    sheet.show(getSupportFragmentManager(), "insurance_sheet");
//
//                    if (company.isPartner()) {
//                        inquiryRepo.logInquiry(uid, company.getId(), company.getName());
//                    }
//                });
//
//                recycler.setAdapter(adapter);
//            }
//
//            @Override
//            public void onError(Exception e) {
//                Toast.makeText(InsuranceActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
//                Toast.makeText(InsuranceActivity.this,
//                        "Firestore error: " + e.getMessage(),
//                        Toast.LENGTH_LONG).show();
//            }
//
//
//
//        });
//    }
//
//    @Override
//    protected int getContentLayoutId() {
//        return R.layout.insurance_activity;
//    }
//}


package com.example.drive_kit.View;

import android.os.Bundle;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.drive_kit.R;
import com.example.drive_kit.View.Adapter.InsuranceListAdapter;
import com.example.drive_kit.ViewModel.DriverInsuranceListViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Driver_InsuranceActivity extends BaseLoggedInActivity {

    /**
     * InsuranceActivity (Driver UI)
     *
     * This Activity shows a list of insurance companies for the driver.
     * It observes the LiveData in DriverInsuranceListViewModel and updates the UI accordingly.
     *
     * UI responsibilities kept here:
     * - RecyclerView setup + adapter binding
     * - Showing the InsuranceBottomSheet when a company is selected
     * - Reading Firebase user uid (simple UI layer dependency)
     * - Showing Toast messages
     *
     * Data/business logic lives in the ViewModel:
     * - Loading companies list
     * - Logging an inquiry for partner companies
     */
    private RecyclerView recycler;
    private InsuranceListAdapter adapter;
    private DriverInsuranceListViewModel vm;

    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // RecyclerView
        recycler = findViewById(R.id.insuranceRecycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        // Get uid (simple, stays in Activity)
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        uid = (user == null) ? null : user.getUid();

        // ViewModel
        vm = new ViewModelProvider(this).get(DriverInsuranceListViewModel.class);

        // Adapter is created once, list updates via LiveData
        adapter = new InsuranceListAdapter(java.util.Collections.emptyList(), company -> {

            // UI action: show bottom sheet
            Driver_InsuranceBottomSheet sheet = Driver_InsuranceBottomSheet.newInstance(
                    company.getId(),
                    company.getName(),
                    company.getPhone(),
                    company.getEmail(),
                    company.getWebsite(),
                    company.isPartner()
            );
            sheet.show(getSupportFragmentManager(), "insurance_sheet");

            // Business logic: log inquiry (only if partner + uid exists)
            vm.onCompanyClicked(uid, company);
        });

        recycler.setAdapter(adapter);

        // Observe companies list and update adapter
        vm.getCompanies().observe(this, companies -> {
            // Keep it simple: recreate adapter with new list (works, minimal change).
            // If your adapter supports updateList/submitList, use that instead.
            recycler.setAdapter(new InsuranceListAdapter(companies, company -> {

                Driver_InsuranceBottomSheet sheet = Driver_InsuranceBottomSheet.newInstance(
                        company.getId(),
                        company.getName(),
                        company.getPhone(),
                        company.getEmail(),
                        company.getWebsite(),
                        company.isPartner()
                );
                sheet.show(getSupportFragmentManager(), "insurance_sheet");

                //vm.onCompanyClicked(uid, company);
            }));
        });

        // Observe errors
        vm.getErrorMessage().observe(this, msg -> {
            if (msg != null && !msg.trim().isEmpty()) {
                Toast.makeText(this, "Firestore error: " + msg, Toast.LENGTH_LONG).show();
            }
        });

        // Load data once
        vm.loadCompanies();
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.insurance_activity;
    }
}
