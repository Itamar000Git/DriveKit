package com.example.drive_kit.View.Insurance_user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.drive_kit.R;
import com.example.drive_kit.ViewModel.Insurance_user_ViewModel.InsuranceHomeViewModel;
import com.google.android.material.imageview.ShapeableImageView;

public class InsuranceHomeActivity extends BaseInsuranceActivity {

    private TextView companyNameText;
    private TextView companyIdValue;
    private Button openInquiriesButton;
    private TextView newInquiriesBadge;
    private ShapeableImageView companyLogoImage;

    private InsuranceHomeViewModel vm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        companyNameText = findViewById(R.id.companyNameText);
        companyIdValue = findViewById(R.id.companyIdValue);
        openInquiriesButton = findViewById(R.id.openInquiriesButton);
        newInquiriesBadge = findViewById(R.id.newInquiriesBadge);
        companyLogoImage = findViewById(R.id.companyLogoImage);

        vm = new ViewModelProvider(this).get(InsuranceHomeViewModel.class);

        String companyDocId = getIntent().getStringExtra("insuranceCompanyId");
        if (companyDocId == null || companyDocId.trim().isEmpty()) {
            Toast.makeText(this, "Missing insuranceCompanyId", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        final String companyDocIdFinal = companyDocId.trim(); // ✅ for lambda

        vm.getWelcomeText().observe(this, text -> {
            if (text != null) companyNameText.setText(text);
        });

        vm.getInternalCompanyId().observe(this, id -> {
            if (id != null) companyIdValue.setText(id);
        });

        vm.getErrorMessage().observe(this, msg -> {
            if (msg != null && !msg.trim().isEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        });

        vm.getCompanyLogoUrl().observe(this, url -> {
            if (companyLogoImage == null) return;

            if (url == null || url.trim().isEmpty()) {
                companyLogoImage.setImageResource(R.drawable.ic_profile_placeholder);
                return;
            }

            Glide.with(this)
                    .load(url.trim())
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .into(companyLogoImage);
        });

        // ✅ NEW: observe badge count from VM (MVVM)
        vm.getNewInquiriesCount().observe(this, count -> {
            setBadgeCount(count == null ? 0 : count);
        });

        // Load company data for home screen (also loads logo url)
        vm.loadCompanyForHome(companyDocIdFinal);

        // default hidden
        setBadgeCount(0);

        openInquiriesButton.setOnClickListener(v -> {
            Intent i = new Intent(InsuranceHomeActivity.this, InsuranceInquiriesActivity.class);
            i.putExtra("insuranceCompanyId", companyDocIdFinal);
            startActivity(i);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        String companyDocId = getIntent().getStringExtra("insuranceCompanyId");
        vm.startNewInquiriesListener(companyDocId); // ✅ moved to VM
    }

    @Override
    protected void onStop() {
        super.onStop();
        vm.stopNewInquiriesListener(); // ✅ moved to VM
    }

    private void setBadgeCount(int count) {
        if (newInquiriesBadge == null) return;

        if (count <= 0) {
            newInquiriesBadge.setVisibility(View.GONE);
        } else {
            newInquiriesBadge.setText(count > 99 ? "99+" : String.valueOf(count));
            newInquiriesBadge.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.ins_home_activity;
    }
}