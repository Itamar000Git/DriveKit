//
//package com.example.drive_kit.View.Insurance_user;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.widget.Button;
//import android.widget.TextView;
//
//import androidx.lifecycle.ViewModelProvider;
//
//import com.example.drive_kit.R;
//import com.example.drive_kit.ViewModel.Insurance_user_ViewModel.InsuranceHomeViewModel;
//
///**
// * InsuranceHomeActivity
// *
// * Purpose:
// * - Display the insurance company home screen.
// * - Show a welcome label with the company name (or fallback text).
// * - Navigate to InsuranceInquiriesActivity when the user clicks the inquiries button.
// *
// * MVVM:
// * - Activity is UI only (bind views, observe LiveData, navigation).
// * - ViewModel loads the data via repository.
// */
//public class InsuranceHomeActivity extends BaseInsuranceActivity {
//
//    private TextView companyNameText;
//    private TextView companyIdValue;
//    private Button openInquiriesButton;
//
//    private InsuranceHomeViewModel vm;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        //setContentView(R.layout.ins_home_activity);
//        getContentLayoutId();
//
//        // Bind views
//
//        companyNameText = findViewById(R.id.companyNameText);
//        companyIdValue = findViewById(R.id.companyIdValue);
//
//
//        openInquiriesButton = findViewById(R.id.openInquiriesButton);
//
//        vm = new ViewModelProvider(this).get(InsuranceHomeViewModel.class);
//
//        // Read company id passed from previous screen
//        String companyId = getIntent().getStringExtra("insuranceCompanyId");
//
//        // Observe welcome text and update UI
//        vm.getWelcomeText().observe(this, text -> companyNameText.setText(text));
//
//        // Load welcome text (same logic as before, now in VM)
//        vm.loadWelcomeText(companyId == null ? "" : companyId);
//
//        // Navigate to inquiries screen and pass company id
//        openInquiriesButton.setOnClickListener(v -> {
//            Intent i = new Intent(InsuranceHomeActivity.this, InsuranceInquiriesActivity.class);
//            i.putExtra("insuranceCompanyId", companyId);
//            startActivity(i);
//        });
//    }
//
//    @Override
//    protected int getContentLayoutId() {
//        return R.layout.ins_home_activity;
//    }
//}


package com.example.drive_kit.View.Insurance_user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.example.drive_kit.R;
import com.example.drive_kit.ViewModel.Insurance_user_ViewModel.InsuranceHomeViewModel;

/**
 * InsuranceHomeActivity
 *
 * Home screen for insurance company.
 * UI only:
 * - bind views
 * - observe LiveData
 * - navigation
 */
public class InsuranceHomeActivity extends BaseInsuranceActivity {

    private TextView companyNameText;
    private TextView companyIdValue;
    private TextView companyId;
    private Button openInquiriesButton;

    //for the red badge
    private TextView newInquiriesBadge;


    private InsuranceHomeViewModel vm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // BaseInsuranceActivity already inflates the content layout into contentContainer.
        // Do NOT call setContentView here and do NOT call getContentLayoutId() manually.

        companyNameText = findViewById(R.id.companyNameText);
        companyIdValue = findViewById(R.id.companyIdValue);
        openInquiriesButton = findViewById(R.id.openInquiriesButton);

        newInquiriesBadge = findViewById(R.id.newInquiriesBadge);


        vm = new ViewModelProvider(this).get(InsuranceHomeViewModel.class);

        String companyDocId = getIntent().getStringExtra("insuranceCompanyId");
        if (companyDocId == null || companyDocId.trim().isEmpty()) {
            Toast.makeText(this, "Missing insuranceCompanyId", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Welcome label
        vm.getWelcomeText().observe(this, text -> {
            if (text != null) companyNameText.setText(text);
        });

        // Internal id label (field "id" from Firestore, fallback to doc id)
        vm.getInternalCompanyId().observe(this, id -> {
            if (id != null) companyIdValue.setText(id);
        });

        // Optional error toast (doesn't break UI)
        vm.getErrorMessage().observe(this, msg -> {
            if (msg != null && !msg.trim().isEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        });

        loadNewInquiriesCount(companyDocId);
        // Load company data for home screen
        vm.loadCompanyForHome(companyDocId);



        openInquiriesButton.setOnClickListener(v -> {
            Intent i = new Intent(InsuranceHomeActivity.this, InsuranceInquiriesActivity.class);
            i.putExtra("insuranceCompanyId", companyDocId);
            startActivity(i);
        });
    }
    private void loadNewInquiriesCount(String companyId) {
        if (companyId == null || companyId.trim().isEmpty()) {
            if (newInquiriesBadge != null) newInquiriesBadge.setVisibility(View.GONE);
            return;
        }

        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("insurance_inquiries")
                .whereEqualTo("companyId", companyId.trim().toLowerCase())
                .whereEqualTo("status", "new")
                .get()
                .addOnSuccessListener(qs -> {
                    int count = qs.size();
                    if (newInquiriesBadge == null) return;

                    if (count <= 0) {
                        newInquiriesBadge.setVisibility(View.GONE);
                    } else {
                        newInquiriesBadge.setText(count > 99 ? "99+" : String.valueOf(count));
                        newInquiriesBadge.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    if (newInquiriesBadge != null) newInquiriesBadge.setVisibility(View.GONE);
                });
    }
    @Override
    protected void onResume() {
        super.onResume();
        String companyId = getIntent().getStringExtra("insuranceCompanyId");
        loadNewInquiriesCount(companyId);
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.ins_home_activity;
    }
}
