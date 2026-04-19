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
import com.google.android.material.imageview.ShapeableImageView;

/**
 * InsuranceCompanyProfileActivity
 *
 * Displays the profile of an insurance company.
 *
 * Responsibilities:
 * - Show company details (name, id, phone, email, website, partner status)
 * - Display company logo
 * - Navigate to edit profile screen
 * - Observe data from ViewModel (MVVM pattern)
 *
 * Architecture:
 * - Activity: UI only (bind views, observe LiveData, navigation)
 * - ViewModel: handles data loading and business logic
 *
 * Data source:
 * - companyDocId is passed via Intent and stored in BaseInsuranceActivity
 */
public class InsuranceCompanyProfileActivity extends BaseInsuranceActivity {

    // ===== UI elements =====
    private TextView tvCompanyName; //Company name title
    private TextView tvCompanyIdValue; //Company ID value
    private TextView tvPhoneValue; //Phone number value
    private TextView tvEmailValue; //Email value
    private TextView tvWebsiteValue; //Website value
    private TextView tvPartnerValue; //Partner status
    private View loadingView; //Loading overlay view
    private MaterialButton btnEdit; //Button to navigate to edit profile
    private ShapeableImageView profileAvatar; //ImageView for displaying company logo
    private InsuranceCompanyProfileViewModel vm; // ViewModel for this screen

    /**
     * Initializes UI, ViewModel, observers, and event handlers.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Bind views from insurance_company_profile_activity.xml (already injected by BaseInsuranceActivity)
        tvCompanyName = findViewById(R.id.profileCompanyName);
        tvCompanyIdValue = findViewById(R.id.profileCompanyIdValue);
        tvPhoneValue = findViewById(R.id.profilePhoneValue);
        tvEmailValue = findViewById(R.id.profileEmailValue);
        tvWebsiteValue = findViewById(R.id.profileWebsiteValue);
        tvPartnerValue = findViewById(R.id.profilePartnerValue);

        loadingView = findViewById(R.id.profileLoading);
        btnEdit = findViewById(R.id.profileEditButton);

        profileAvatar = findViewById(R.id.profileAvatar);


        // Init ViewModel
        vm = new ViewModelProvider(this).get(InsuranceCompanyProfileViewModel.class);

        // companyDocId comes from BaseInsuranceActivity
        String cid = safe(companyDocId);
        if (cid.isEmpty()) {
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

            tvCompanyName.setText(safe(company.getName()).isEmpty() ? "חברת ביטוח" : safe(company.getName()));

            // IMPORTANT:
            // company.getId() is your "internal id" field (if you mapped it that way in the repository).
            tvCompanyIdValue.setText(safe(company.getId()));

            tvPhoneValue.setText(safe(company.getPhone()).isEmpty() ? "-" : safe(company.getPhone()));
            tvEmailValue.setText(safe(company.getEmail()).isEmpty() ? "-" : safe(company.getEmail()));
            tvWebsiteValue.setText(safe(company.getWebsite()).isEmpty() ? "-" : safe(company.getWebsite()));
            tvPartnerValue.setText(company.isPartner() ? "כן" : "לא");

            // ===== Logo handling =====
            String logoUrl = safe(company.getLogoUrl());
            if (profileAvatar != null) {
                if (logoUrl.isEmpty()) {
                    profileAvatar.setImageResource(R.drawable.ic_profile_placeholder);
                } else {
                    com.bumptech.glide.Glide.with(this)
                            .load(logoUrl)
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .error(R.drawable.ic_profile_placeholder)
                            .into(profileAvatar);
                }
            }
            android.util.Log.d("PROFILE_LOGO", "logoUrl=" + safe(company.getLogoUrl()));
        });

        // Edit button → navigate to edit screen
        if (btnEdit != null) {
            btnEdit.setOnClickListener(v -> {
                Intent i = new Intent(this, InsuranceCompanyEditProfileActivity.class);
                i.putExtra("insuranceCompanyId", cid);
                startActivity(i);
            });
        }

        // Initial load
        vm.loadCompany(cid);
    }

    /**
     * Returns layout resource ID for this screen.
     */
    @Override
    protected int getContentLayoutId() {
        return R.layout.insurance_company_profile_activity;
    }

    /**
     * Reloads company data when returning to this screen
     * (e.g., after editing profile).
     */
    @Override
    protected void onResume() {
        super.onResume();

        // Reload after returning from edit screen
        String cid = safe(companyDocId);
        if (!cid.isEmpty()) {
            vm.loadCompany(cid);
        }
    }

    /**
     * Utility method to safely trim strings and avoid null values.
     *
     * @param s input string
     * @return trimmed string or empty string if null
     */
    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
