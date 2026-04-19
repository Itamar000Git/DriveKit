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
/**
 * Activity for editing an insurance company's profile.
 *
 * Responsibilities:
 * - Display current company details
 * - Allow editing basic fields (name, phone, email, website)
 * - Handle logo selection and preview
 * - Upload new logo via ViewModel
 * - Save updated company data
 *
 * Uses MVVM architecture:
 * - View (this Activity)
 * - ViewModel: InsuranceCompanyEditProfileViewModel
 */
public class InsuranceCompanyEditProfileActivity extends BaseInsuranceActivity {
    private static final String TAG = "LOGO_FLOW_UI"; //Tag for logging logo-related flow
    private TextInputEditText etId, etName, etPhone, etEmail, etWebsite; // Input fields for company details
    private View loading; //Loading overlay view
    private MaterialButton btnSave; // Save button

    // ===== Logo UI =====
    private ShapeableImageView ivLogo; // ImageView for displaying company logo
    private MaterialButton btnChangeLogo; // Button to trigger logo change
    private Uri pendingLogoUri = null; //Holds the selected image URI for preview before upload
    private InsuranceCompanyEditProfileViewModel vm; // ViewModel handling business logic

    /**
     * Launcher for selecting an image from device storage.
     *
     * Flow:
     * 1. User picks image
     * 2. Preview is shown immediately
     * 3. Image is uploaded via ViewModel
     */
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
    /**
     * Initializes UI, ViewModel, observers, and event handlers.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Bind views
        etId = findViewById(R.id.editCompanyId);
        etName = findViewById(R.id.editCompanyName);
        etPhone = findViewById(R.id.editCompanyPhone);
        etEmail = findViewById(R.id.editCompanyEmail);
        etWebsite = findViewById(R.id.editCompanyWebsite);

        loading = findViewById(R.id.editCompanyLoading);
        btnSave = findViewById(R.id.btnSaveCompany);

        ivLogo = findViewById(R.id.editCompanyLogo);
        btnChangeLogo = findViewById(R.id.btnChangeLogo);
        // Initialize ViewModel
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
        // Observe errors
        vm.getErrorMessage().observe(this, msg -> {
            if (msg != null && !msg.trim().isEmpty()) {
                Log.e(TAG, "error=" + msg);
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        });
        // Observe company data and bind to UI
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
        // Observe save success
        vm.getSaveSuccess().observe(this, ok -> {
            if (Boolean.TRUE.equals(ok)) {
                Toast.makeText(this, "נשמר בהצלחה", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        // Logo change button
        if (btnChangeLogo != null) {
            btnChangeLogo.setOnClickListener(v -> {
                Log.d(TAG, "change logo clicked");
                pickImage.launch("image/*");
            });
        }
        // Save button
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
        // Initial data load
        vm.loadCompany(cid);
    }
    /**
     * Returns layout resource ID for this screen.
     */
    @Override
    protected int getContentLayoutId() {
        return R.layout.insurance_company_edit_profile_activity;
    }

    /**
     * Returns layout resource ID for this screen.
     */
    private void updateLoadingOverlay() {
        boolean isLoading = Boolean.TRUE.equals(vm.getLoading().getValue());
        boolean isSaving = Boolean.TRUE.equals(vm.getSaving().getValue());
        boolean show = isLoading || isSaving;

        Log.d(TAG, "updateLoadingOverlay loading=" + isLoading + " saving=" + isSaving + " show=" + show);
        if (loading != null) loading.setVisibility(show ? View.VISIBLE : View.GONE);
    }
    /**
     * Safely extracts trimmed text from an input field.
     *
     * @param et input field
     * @return trimmed string or empty string
     */
    private String text(TextInputEditText et) {
        return (et == null || et.getText() == null) ? "" : et.getText().toString().trim();
    }

    /**
     * Safely trims a string, avoiding null values.
     *
     * @param s input string
     * @return trimmed string or empty string
     */
    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
