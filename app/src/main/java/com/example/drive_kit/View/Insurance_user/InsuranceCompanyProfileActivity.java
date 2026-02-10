package com.example.drive_kit.View.Insurance_user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.example.drive_kit.R;
import com.example.drive_kit.ViewModel.Insurance_user_ViewModel.InsuranceCompanyProfileViewModel;
import com.google.android.material.button.MaterialButton;

/**
 * InsuranceCompanyProfileActivity
 *
 * Profile screen for an insurance company.
 *
 * MVVM:
 * - Activity = UI only (bind views, observe LiveData).
 * - ViewModel = loads company data and exposes it as LiveData.
 */
public class InsuranceCompanyProfileActivity extends BaseInsuranceActivity {

    // UI
    private TextView tvCompanyName;
    private TextView tvCompanyIdValue;
    private TextView tvPhoneValue;
    private TextView tvEmailValue;
    private TextView tvWebsiteValue;
    private TextView tvPartnerValue;

    private View loadingView;
    private MaterialButton btnEdit;

    private InsuranceCompanyProfileViewModel vm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getContentLayoutId();
        // Bind views from insurance_company_profile_activity.xml
        tvCompanyName = findViewById(R.id.profileCompanyName);
        tvCompanyIdValue = findViewById(R.id.profileCompanyIdValue);
        tvPhoneValue = findViewById(R.id.profilePhoneValue);
        tvEmailValue = findViewById(R.id.profileEmailValue);
        tvWebsiteValue = findViewById(R.id.profileWebsiteValue);
        tvPartnerValue = findViewById(R.id.profilePartnerValue);

        loadingView = findViewById(R.id.profileLoading);
        btnEdit = findViewById(R.id.profileEditButton);

        // Init ViewModel
        vm = new ViewModelProvider(this).get(InsuranceCompanyProfileViewModel.class);

        // companyId is stored in BaseInsuranceActivity (read from Intent in base)
        if (companyId == null || companyId.trim().isEmpty()) {
            Toast.makeText(this, "Missing insuranceCompanyId", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Observe loading state
        vm.getLoading().observe(this, isLoading -> {
            if (loadingView != null) {
                loadingView.setVisibility(Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE);
            }
        });

        // Observe errors
        vm.getErrorMessage().observe(this, msg -> {
            if (msg != null && !msg.trim().isEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        });

        // Observe company data and update UI
        vm.getCompany().observe(this, company -> {
            if (company == null) return;

            tvCompanyName.setText(company.getName().isEmpty() ? "חברת ביטוח" : company.getName());
            tvCompanyIdValue.setText(company.getId()); // shows companyId as requested
            tvPhoneValue.setText(company.getPhone().isEmpty() ? "-" : company.getPhone());
            tvEmailValue.setText(company.getEmail().isEmpty() ? "-" : company.getEmail());
            tvWebsiteValue.setText(company.getWebsite().isEmpty() ? "-" : company.getWebsite());
            tvPartnerValue.setText(company.isPartner() ? "כן" : "לא");
        });

        btnEdit.setOnClickListener(v -> {
            Intent i = new Intent(this, InsuranceCompanyEditProfileActivity.class);
            i.putExtra("insuranceCompanyId", companyId);
            startActivity(i);
        });


        // Initial load
        vm.loadCompany(companyId);
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.insurance_company_profile_activity;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload after returning from edit screen
        if (companyId != null && !companyId.trim().isEmpty()) {
            vm.loadCompany(companyId);
        }
    }
}
