package com.example.drive_kit.View;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.drive_kit.Data.Repository.InsuranceInquiryRepository;
import com.example.drive_kit.R;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class DriverInsuranceInquiryActivity extends AppCompatActivity {

    private MaterialAutoCompleteTextView companyDropdown;
    private EditText messageEditText;
    private Button sendButton;

    private final InsuranceInquiryRepository inquiryRepo = new InsuranceInquiryRepository();

    // mapping dropdown position -> company id/name
    private final ArrayList<String> insuranceCompanyIds = new ArrayList<>();
    private final ArrayList<String> insuranceCompanyNames = new ArrayList<>();

    private String selectedCompanyId = null;
    private String selectedCompanyName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_insurance_inquiry);

        companyDropdown = findViewById(R.id.insuranceCompanyDropdown);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendInquiryButton);

        setupCompaniesDropdown();

        sendButton.setOnClickListener(v -> {
            // 1) validate user login
            String userId = FirebaseAuth.getInstance().getUid();
            if (userId == null || userId.trim().isEmpty()) {
                Toast.makeText(this, "משתמש לא מחובר", Toast.LENGTH_SHORT).show();
                return;
            }

            // 2) validate company selection
            if (selectedCompanyId == null || selectedCompanyId.trim().isEmpty()) {
                // try resolving from current text if user typed/chose manually
                String chosen = companyDropdown.getText() == null ? "" : companyDropdown.getText().toString().trim();
                if (!chosen.isEmpty()) {
                    int idx = insuranceCompanyNames.indexOf(chosen);
                    if (idx >= 0 && idx < insuranceCompanyIds.size()) {
                        selectedCompanyId = insuranceCompanyIds.get(idx);
                        selectedCompanyName = insuranceCompanyNames.get(idx);
                    }
                }
            }

            if (selectedCompanyId == null || selectedCompanyId.trim().isEmpty()) {
                Toast.makeText(this, "נא לבחור חברת ביטוח", Toast.LENGTH_SHORT).show();
                return;
            }

            // 3) message (optional)
            String msg = messageEditText.getText() == null ? "" : messageEditText.getText().toString().trim();

            // 4) optional driver details from intent (if sent)
            String driverName = safe(getIntent().getStringExtra("driverName"));
            String driverPhone = safe(getIntent().getStringExtra("driverPhone"));
            String driverEmail = safe(getIntent().getStringExtra("driverEmail"));
            String carNumber = safe(getIntent().getStringExtra("carNumber"));
            String carModel = safe(getIntent().getStringExtra("carModel"));

            // 5) create inquiry
            inquiryRepo.logInquiry(
                    userId,
                    selectedCompanyId,
                    selectedCompanyName,
                    driverName,
                    driverPhone,
                    driverEmail,
                    carNumber,
                    carModel,
                    msg,
                    new InsuranceInquiryRepository.InquiryCallback() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(DriverInsuranceInquiryActivity.this, "הפרטים נשלחו בהצלחה", Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void onError(Exception e) {
                            String err = (e != null && e.getMessage() != null && !e.getMessage().trim().isEmpty())
                                    ? e.getMessage()
                                    : "שגיאה בשליחת הפנייה";
                            Toast.makeText(DriverInsuranceInquiryActivity.this, err, Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        });
    }

    private void setupCompaniesDropdown() {
        if (companyDropdown == null) return;

        companyDropdown.setOnClickListener(v -> companyDropdown.showDropDown());

        FirebaseFirestore.getInstance()
                .collection("insurance_companies")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    insuranceCompanyIds.clear();
                    insuranceCompanyNames.clear();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String docId = doc.getId();
                        String name = doc.getString("name");
                        if (name == null || name.trim().isEmpty()) name = docId;

                        // label shown in dropdown
                        String display = name + " (" + docId + ")";

                        insuranceCompanyIds.add(docId);
                        insuranceCompanyNames.add(display);
                    }

                    ArrayAdapter<String> adapter =
                            new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, insuranceCompanyNames);
                    companyDropdown.setAdapter(adapter);

                    companyDropdown.setOnItemClickListener((parent, view, position, id) -> {
                        if (position >= 0 && position < insuranceCompanyIds.size()) {
                            selectedCompanyId = insuranceCompanyIds.get(position);
                            selectedCompanyName = insuranceCompanyNames.get(position);
                        }
                    });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "שגיאה בטעינת חברות ביטוח", Toast.LENGTH_SHORT).show()
                );
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
