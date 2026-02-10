package com.example.drive_kit.View.Insurance_user;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.example.drive_kit.R;
import com.example.drive_kit.ViewModel.Insurance_user_ViewModel.InsuranceCompanyEditProfileViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * InsuranceCompanyEditProfileActivity
 *
 * Edit screen for insurance company profile.
 *
 * UI responsibilities:
 * - Bind views
 * - Fill fields from ViewModel
 * - Call ViewModel.save(...)
 *
 * Data responsibilities:
 * - ViewModel + Repository
 */
public class InsuranceCompanyEditProfileActivity extends BaseInsuranceActivity {

    private TextInputEditText etId, etName, etPhone, etEmail, etWebsite;
    private View loading;
    private MaterialButton btnSave;

    private InsuranceCompanyEditProfileViewModel vm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        etId = findViewById(R.id.editCompanyId);
        etName = findViewById(R.id.editCompanyName);
        etPhone = findViewById(R.id.editCompanyPhone);
        etEmail = findViewById(R.id.editCompanyEmail);
        etWebsite = findViewById(R.id.editCompanyWebsite);

        loading = findViewById(R.id.editCompanyLoading);
        btnSave = findViewById(R.id.btnSaveCompany);

        vm = new ViewModelProvider(this).get(InsuranceCompanyEditProfileViewModel.class);

        // companyId comes from BaseInsuranceActivity field
        if (companyId == null || companyId.trim().isEmpty()) {
            Toast.makeText(this, "Missing insuranceCompanyId", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        vm.getLoading().observe(this, isLoading -> {
            if (loading != null) loading.setVisibility(Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE);
        });

        vm.getErrorMessage().observe(this, msg -> {
            if (msg != null && !msg.trim().isEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        });

        vm.getCompany().observe(this, c -> {
            if (c == null) return;

            // Fill fields once data arrives
            if (etId != null) etId.setText(c.getId());
            if (etName != null) etName.setText(c.getName());
            if (etPhone != null) etPhone.setText(c.getPhone());
            if (etEmail != null) etEmail.setText(c.getEmail());
            if (etWebsite != null) etWebsite.setText(c.getWebsite());
        });

        vm.getSaveSuccess().observe(this, ok -> {
            if (Boolean.TRUE.equals(ok)) {
                Toast.makeText(this, "נשמר בהצלחה", Toast.LENGTH_SHORT).show();
                finish(); // back to profile
            }
        });

        btnSave.setOnClickListener(v -> {
            String name = text(etName);
            String phone = text(etPhone);
            String email = text(etEmail);
            String website = text(etWebsite);

            vm.saveCompany(companyId, name, phone, email, website);
        });

        // Initial load
        vm.loadCompany(companyId);
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.insurance_company_edit_profile_activity;
    }

    private String text(TextInputEditText et) {
        return (et == null || et.getText() == null) ? "" : et.getText().toString().trim();
    }
}
