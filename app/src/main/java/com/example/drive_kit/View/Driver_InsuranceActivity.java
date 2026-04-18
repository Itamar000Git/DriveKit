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

/**
 * Driver_InsuranceActivity
 *
 * Displays a list of insurance companies available to the driver.
 *
 * Responsibilities:
 * - Show list of insurance companies (RecyclerView)
 * - Allow user to click a company → open BottomSheet with details
 * - Observe data from ViewModel (Firestore)
 *
 * Architecture (MVVM):
 * - Activity: UI + click handling
 * - ViewModel: loads companies from Firestore
 */
public class Driver_InsuranceActivity extends BaseLoggedInActivity {

    private RecyclerView recycler; // RecyclerView for displaying insurance companies
    private DriverInsuranceListViewModel vm; //  ViewModel handling data
    private String uid; // Current user ID (Firebase)

    /**
     * Initializes UI, ViewModel, observers and loads companies list.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ===== RecyclerView setup =====
        recycler = findViewById(R.id.insuranceRecycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        // ===== Get current user =====
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Extract UID safely
        uid = (user == null) ? null : user.getUid();

        // ===== ViewModel =====
        vm = new ViewModelProvider(this).get(DriverInsuranceListViewModel.class);

        /**
         * Observe insurance companies list.
         *
         * Flow:
         * Firestore → ViewModel → LiveData → RecyclerView Adapter
         */
        vm.getCompanies().observe(this, companies -> {
            // Set adapter (re-created each update)
            recycler.setAdapter(new InsuranceListAdapter(companies, company -> {

                /**
                 * Open BottomSheet with company details.
                 *
                 * Parameters passed:
                 * - hp (business number)
                 * - docId (Firestore ID)
                 * - contact details
                 * - partner status
                 */
                Driver_InsuranceBottomSheet sheet = Driver_InsuranceBottomSheet.newInstance(
                        company.getHp(),       // h_p (515761625)
                        company.getDocId(),    // docId (libra)
                        company.getName(),
                        company.getPhone(),
                        company.getEmail(),
                        company.getWebsite(),
                        company.isPartner()
                );
                sheet.show(getSupportFragmentManager(), "insurance_sheet");
            }));
        });

        /**
         * Observe error messages from ViewModel.
         */
        vm.getErrorMessage().observe(this, msg -> {
            if (msg != null && !msg.trim().isEmpty()) {
                Toast.makeText(this, "Firestore error: " + msg, Toast.LENGTH_LONG).show();
            }
        });
        // ===== Initial load =====
        vm.loadCompanies();
    }
    /**
     * Provides layout for BaseLoggedInActivity.
     */
    @Override
    protected int getContentLayoutId() {
        return R.layout.insurance_activity;
    }
}
