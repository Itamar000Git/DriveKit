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
 * Screen for sending an insurance inquiry by a driver.
 *
 * Responsibilities:
 * - Display dropdown of insurance companies
 * - Collect user message
 * - Send inquiry via ViewModel
 *
 * Architecture (MVVM):
 * - Activity: UI + input handling
 * - ViewModel: business logic (Firestore interaction)
 *
 * Data Flow:
 * ViewModel → LiveData (companies list) → Dropdown
 * User input → Activity → ViewModel.sendInquiry()
 */
public class DriverInsuranceInquiryActivity extends AppCompatActivity {
    private MaterialAutoCompleteTextView companyDropdown; // Dropdown for selecting insurance company
    private EditText messageEditText; // Text input for inquiry message
    private Button sendButton; // Send button
    private DriverInsuranceInquiryViewModel vm; // ViewModel handling logic

    /**
     * Holds selected company display string.
     * Important:
     * This is NOT an ID — it is the visible string shown in dropdown.
     */
    private String selectedCompanyDisplay = "";

    /**
     * Initializes UI, ViewModel and observers.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_insurance_inquiry);

        // ===== Bind views =====
        companyDropdown = findViewById(R.id.insuranceCompanyDropdown);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendInquiryButton);

        // ===== ViewModel =====
        vm = new ViewModelProvider(this).get(DriverInsuranceInquiryViewModel.class);

        /**
         * Always show dropdown when clicked.
         * Prevents confusion if user expects dropdown to open immediately.
         */
        companyDropdown.setOnClickListener(v -> companyDropdown.showDropDown());

        /**
         * Observe companies list and bind dropdown adapter.
         */
        vm.getCompanyDisplayList().observe(this, this::bindDropdown);

        /**
         * Observe toast messages from ViewModel.
         */
        vm.getToastMessage().observe(this, msg -> {
            if (msg != null && !msg.trim().isEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        });

        /**
         * Close screen after successful send.
         */
        vm.getSent().observe(this, ok -> {
            if (Boolean.TRUE.equals(ok)) {
                finish();
            }
        });

        // Load companies list from ViewModel
        vm.loadCompanies();

        /**
         * Send button logic.
         *
         * Flow:
         * - Validate user is logged in
         * - Read selected company
         * - Read message
         * - Read optional driver details from Intent
         * - Send to ViewModel
         */
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

    /**
     * Binds dropdown adapter with company list.
     *
     * @param items list of display strings for companies
     */
    private void bindDropdown(List<String> items) {
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        companyDropdown.setAdapter(adapter);

        /**
         * Save selected item when user clicks from dropdown.
         */
        companyDropdown.setOnItemClickListener((parent, view, position, id) -> {
            // Save the display string user selected
            selectedCompanyDisplay = items.get(position);
        });
    }

    /**
     * Null-safe string trimming helper.
     *
     * @param s input string
     * @return trimmed string or empty string if null
     */
    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
