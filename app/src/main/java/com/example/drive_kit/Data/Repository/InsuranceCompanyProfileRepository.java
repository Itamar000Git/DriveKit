package com.example.drive_kit.Data.Repository;

import androidx.annotation.NonNull;

import com.example.drive_kit.Model.InsuranceCompany;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * InsuranceCompanyProfileRepository
 *
 * Data layer for insurance company profile.
 * Responsibilities:
 * - Read insurance company document from Firestore
 * - Update insurance company fields in Firestore
 */
public class InsuranceCompanyProfileRepository {

    public interface CompanyCallback {
        void onSuccess(InsuranceCompany company);
        void onError(Exception e);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onError(Exception e);
    }

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Loads a single insurance company by document id (companyId).
     */
    public void loadCompany(@NonNull String companyId, @NonNull CompanyCallback cb) {
        String id = safe(companyId);
        if (id.isEmpty()) {
            cb.onError(new IllegalArgumentException("companyId is empty"));
            return;
        }

        db.collection("insurance_companies")
                .document(id)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        cb.onError(new IllegalStateException("Company not found: " + id));
                        return;
                    }

                    String name = safe(doc.getString("name"));
                    String phone = safe(doc.getString("phone"));
                    String email = safe(doc.getString("email"));
                    String website = safe(doc.getString("website"));
                    Boolean isPartner = doc.getBoolean("isPartner");

                    String internalId = doc.getString("id");
                    if (internalId == null || internalId.trim().isEmpty()) internalId = doc.getId();

                    InsuranceCompany company = new InsuranceCompany(
                            internalId,
                            name,
                            phone,
                            email,
                            website,
                            isPartner != null && isPartner
                    );

                    cb.onSuccess(company);
                })
                .addOnFailureListener(cb::onError);
    }

    /**
     * Updates editable fields for the insurance company profile.
     */
    public void updateCompany(@NonNull String companyId,
                              @NonNull String name,
                              @NonNull String phone,
                              @NonNull String email,
                              @NonNull String website,
                              @NonNull SimpleCallback cb) {

        String id = safe(companyId);
        if (id.isEmpty()) {
            cb.onError(new IllegalArgumentException("companyId is empty"));
            return;
        }

        db.collection("insurance_companies")
                .document(id)
                .update(
                        "name", safe(name),
                        "phone", safe(phone),
                        "email", safe(email),
                        "website", safe(website)
                )
                .addOnSuccessListener(v -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
