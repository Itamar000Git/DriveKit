package com.example.drive_kit.View;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.drive_kit.R;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * InsuranceHomeActivity
 *
 * Purpose:
 * - Display the insurance company home screen.
 * - Show a welcome label with the company name (or fallback text).
 * - Navigate to InsuranceInquiriesActivity when the user clicks the inquiries button.
 *
 * Notes:
 * - companyId is expected from Intent extras ("insuranceCompanyId").
 * - The current implementation performs two Firestore reads for the same company document:
 *   one for "שלום, <name>" format and another for raw/fallback name text.
 */
public class InsuranceHomeActivity extends AppCompatActivity {

    // TextView used to show company display text
    private TextView companyNameTextView;

    // Button that navigates to the inquiries list screen
    private Button openInquiriesButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Attach layout
        setContentView(R.layout.ins_home_activity);

        // Bind inquiries button from XML
        openInquiriesButton = findViewById(R.id.openInquiriesButton);

        // Local TextView reference for welcome text
        TextView companyNameText = findViewById(R.id.companyNameText);

        // Read company id passed from previous screen
        String companyId = getIntent().getStringExtra("insuranceCompanyId");

        // Fallback if company id is missing
        if (companyId == null || companyId.trim().isEmpty()) {
            companyNameText.setText("שלום, חברה");
            return;
        }

        // Navigate to inquiries screen and pass company id
        openInquiriesButton.setOnClickListener(v -> {
            Intent i = new Intent(InsuranceHomeActivity.this, InsuranceInquiriesActivity.class);
            i.putExtra("insuranceCompanyId", companyId);
            startActivity(i);
        });

        // Firestore read #1:
        // Populate welcome text in format: "שלום, <company name>"
        FirebaseFirestore.getInstance()
                .collection("insurance_companies")
                .document(companyId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        // Fallback to companyId if doc missing
                        companyNameText.setText("שלום, " + companyId);
                        return;
                    }

                    String companyName = doc.getString("name");
                    if (companyName != null && !companyName.trim().isEmpty()) {
                        companyNameText.setText("שלום, " + companyName.trim());
                    } else {
                        // Fallback to companyId when "name" is empty
                        companyNameText.setText("שלום, " + companyId);
                    }
                })
                .addOnFailureListener(e -> companyNameText.setText("שלום, " + companyId));

        // Bind class-level TextView reference (same view id as above)
        companyNameTextView = findViewById(R.id.companyNameText);

        // Additional fallback check for missing company id
        if (companyId == null || companyId.trim().isEmpty()) {
            companyNameTextView.setText("לא נמצאה חברה");
            return;
        }

        // Firestore read #2:
        // Populate companyNameTextView with plain name/companyId fallback (without "שלום, ")
        FirebaseFirestore.getInstance()
                .collection("insurance_companies")
                .document(companyId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        companyNameTextView.setText(companyId); // fallback
                        return;
                    }

                    String name = doc.getString("name");
                    if (name == null || name.trim().isEmpty()) {
                        companyNameTextView.setText(companyId); // fallback
                    } else {
                        companyNameTextView.setText(name.trim());
                    }
                })
                .addOnFailureListener(e -> companyNameTextView.setText(companyId)); // fallback
    }
}
