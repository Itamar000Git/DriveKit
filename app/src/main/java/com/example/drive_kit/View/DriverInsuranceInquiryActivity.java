package com.example.drive_kit.View;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.drive_kit.R;
import com.example.drive_kit.ViewModel.DriverInsuranceInquiryViewModel;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

/**
 * DriverInsuranceInquiryActivity
 *
 * Screen for a driver to send an insurance inquiry.
 *
 * UI responsibilities:
 * - Bind views (dropdown, message, button)
 * - Observe ViewModel LiveData and update UI
 * - Read intent extras (driver details)
 * - Show Toast and finish() on success
 *
 * Data/business responsibilities are in the ViewModel:
 * - Loading companies list
 * - Sending inquiry to Firestore
 */
public class DriverInsuranceInquiryActivity extends AppCompatActivity {

    private MaterialAutoCompleteTextView companyDropdown;
    private EditText messageEditText;
    private Button sendButton;

    private DriverInsuranceInquiryViewModel vm;

    // Keep the selected text (display string) exactly as shown in the dropdown
    private String selectedCompanyDisplay = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_insurance_inquiry);

        companyDropdown = findViewById(R.id.insuranceCompanyDropdown);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendInquiryButton);

        vm = new ViewModelProvider(this).get(DriverInsuranceInquiryViewModel.class);

        // Always show dropdown when clicked (same UX)
        companyDropdown.setOnClickListener(v -> companyDropdown.showDropDown());

        // Observe companies list and bind adapter
        vm.getCompanyDisplayList().observe(this, this::bindDropdown);

        // Observe toast messages from VM
        vm.getToastMessage().observe(this, msg -> {
            if (msg != null && !msg.trim().isEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        });

        // Close screen on success (same behavior)
        vm.getSent().observe(this, ok -> {
            if (Boolean.TRUE.equals(ok)) {
                finish();
            }
        });

        // Load companies once
        vm.loadCompanies();

        sendButton.setOnClickListener(v -> {
            String userId = FirebaseAuth.getInstance().getUid();
            if (userId == null || userId.trim().isEmpty()) {
                Toast.makeText(this, "משתמש לא מחובר", Toast.LENGTH_SHORT).show();
                return;
            }

            // Use the current text if user typed/chose manually (same idea as original code)
            String chosen = companyDropdown.getText() == null ? "" : companyDropdown.getText().toString().trim();
            if (!chosen.isEmpty()) selectedCompanyDisplay = chosen;

            String msg = messageEditText.getText() == null ? "" : messageEditText.getText().toString().trim();

            // Optional driver details from intent (same behavior)
            String driverName = safe(getIntent().getStringExtra("driverName"));
            String driverPhone = safe(getIntent().getStringExtra("driverPhone"));
            String driverEmail = safe(getIntent().getStringExtra("driverEmail"));
            String carNumber = safe(getIntent().getStringExtra("carNumber"));
            String carModel = safe(getIntent().getStringExtra("carModel"));

            vm.sendInquiry(
                    userId,
                    selectedCompanyDisplay,
                    driverName,
                    driverPhone,
                    driverEmail,
                    carNumber,
                    carModel,
                    msg
            );
        });
    }

    private void bindDropdown(List<String> items) {
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        companyDropdown.setAdapter(adapter);

        companyDropdown.setOnItemClickListener((parent, view, position, id) -> {
            // Save the display string user selected
            selectedCompanyDisplay = items.get(position);
        });
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
