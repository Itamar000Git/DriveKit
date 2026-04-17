package com.example.drive_kit.View.Insurance_user;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.drive_kit.R;
import com.example.drive_kit.ViewModel.Insurance_user_ViewModel.InsuranceCompanyEditProfileViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;

public class InsuranceCompanyEditProfileActivity extends BaseInsuranceActivity {

    private static final String TAG = "LOGO_FLOW_UI";

    private TextInputEditText etId, etName, etPhone, etEmail, etWebsite;
    private View loading;
    private MaterialButton btnSave;

    // logo UI
    private ShapeableImageView ivLogo;
    private MaterialButton btnChangeLogo;

    // local preview
    private Uri pendingLogoUri = null;

    private InsuranceCompanyEditProfileViewModel vm;

    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null) return;

                Log.d(TAG, "pickImage uri=" + uri);
                pendingLogoUri = uri;

                // immediate preview
                if (ivLogo != null) ivLogo.setImageURI(uri);

                String cid = safe(companyDocId);
                if (!cid.isEmpty()) {
                    vm.uploadLogo(cid, uri);
                }
            });

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

        ivLogo = findViewById(R.id.editCompanyLogo);
        btnChangeLogo = findViewById(R.id.btnChangeLogo);

        vm = new ViewModelProvider(this).get(InsuranceCompanyEditProfileViewModel.class);

        String cid = safe(companyDocId);
        if (cid.isEmpty()) {
            Toast.makeText(this, "Missing insuranceCompanyId", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Show/hide overlay based on BOTH loading + saving
        vm.getLoading().observe(this, v -> updateLoadingOverlay());
        vm.getSaving().observe(this, v -> updateLoadingOverlay());

        vm.getErrorMessage().observe(this, msg -> {
            if (msg != null && !msg.trim().isEmpty()) {
                Log.e(TAG, "error=" + msg);
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        });

        vm.getCompany().observe(this, c -> {
            if (c == null) return;

            if (etId != null) etId.setText(safe(c.getId()));
            if (etName != null) etName.setText(safe(c.getName()));
            if (etPhone != null) etPhone.setText(safe(c.getPhone()));
            if (etEmail != null) etEmail.setText(safe(c.getEmail()));
            if (etWebsite != null) etWebsite.setText(safe(c.getWebsite()));

            // logo: only load from URL if user didn't pick a local image now
            if (pendingLogoUri == null && ivLogo != null) {
                String url = safe(c.getLogoUrl());
                Log.d(TAG, "bind company logoUrl=" + url);

                if (!url.isEmpty()) {
                    Glide.with(this)
                            .load(url)
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .into(ivLogo);
                } else {
                    ivLogo.setImageResource(R.drawable.ic_profile_placeholder);
                }
            } else {
                Log.d(TAG, "skip url bind because pendingLogoUri=" + pendingLogoUri);
            }
        });

        vm.getSaveSuccess().observe(this, ok -> {
            if (Boolean.TRUE.equals(ok)) {
                Toast.makeText(this, "נשמר בהצלחה", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        if (btnChangeLogo != null) {
            btnChangeLogo.setOnClickListener(v -> {
                Log.d(TAG, "change logo clicked");
                pickImage.launch("image/*");
            });
        }

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                String name = text(etName);
                String phone = text(etPhone);
                String email = text(etEmail);
                String website = text(etWebsite);

                Log.d(TAG, "save clicked");
                vm.saveCompany(cid, name, phone, email, website);
            });
        }

        vm.loadCompany(cid);
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.insurance_company_edit_profile_activity;
    }

    private void updateLoadingOverlay() {
        boolean isLoading = Boolean.TRUE.equals(vm.getLoading().getValue());
        boolean isSaving = Boolean.TRUE.equals(vm.getSaving().getValue());
        boolean show = isLoading || isSaving;

        Log.d(TAG, "updateLoadingOverlay loading=" + isLoading + " saving=" + isSaving + " show=" + show);
        if (loading != null) loading.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private String text(TextInputEditText et) {
        return (et == null || et.getText() == null) ? "" : et.getText().toString().trim();
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
