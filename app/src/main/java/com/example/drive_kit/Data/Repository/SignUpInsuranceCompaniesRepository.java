package com.example.drive_kit.Data.Repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SignUpInsuranceCompaniesRepository {

    // =========================
    // DTO for dropdown list
    // =========================
    public static class CompanyItem {
        public final String docId;  // e.g. "libra"
        public final String name;   // e.g. "ליברה"

        public CompanyItem(@NonNull String docId, @NonNull String name) {
            this.docId = docId;
            this.name = name;
        }

        // ✅ The Activity expects this method
        public String display() {
            String n = (name == null || name.trim().isEmpty()) ? docId : name.trim();
            return n + " (" + docId + ")";
        }

        // still useful if you ever bind the object directly to the adapter
        @NonNull
        @Override
        public String toString() {
            return (name == null) ? "" : name.trim();
        }
    }

    // =========================
    // DTO for details fill
    // =========================
    public static class CompanyDetails {
        public final String docId;
        public final String name;
        public final String phone;
        public final String email;
        public final String website;

        // ✅ The Activity expects details.hp
        public final String hp; // maps Firestore field: h_p

        public CompanyDetails(
                @NonNull String docId,
                @Nullable String name,
                @Nullable String phone,
                @Nullable String email,
                @Nullable String website,
                @Nullable String hp
        ) {
            this.docId = docId;
            this.name = safe(name);
            this.phone = safe(phone);
            this.email = safe(email);
            this.website = safe(website);
            this.hp = safe(hp);
        }

        private static String safe(String s) {
            return s == null ? "" : s.trim();
        }
    }

    // =========================
    // Callbacks
    // =========================
    public interface CompaniesCallback {
        void onSuccess(@NonNull List<CompanyItem> items);
        void onError(@NonNull Exception e);
    }

    public interface CompanyDetailsCallback {
        void onSuccess(@NonNull CompanyDetails details);
        void onError(@NonNull Exception e);
    }

    // =========================
    // Firestore API
    // =========================
    public void loadCompanies(@NonNull CompaniesCallback cb) {
        FirebaseFirestore.getInstance()
                .collection("insurance_companies")
                .get()
                .addOnSuccessListener(qs -> {
                    List<CompanyItem> out = new ArrayList<>();

                    qs.getDocuments().forEach(doc -> {
                        String docId = doc.getId();
                        String name = doc.getString("name");
                        if (name == null) name = "";
                        out.add(new CompanyItem(docId, name.trim()));
                    });

                    // optional: sort by name
                    out.sort((a, b) -> a.display().compareToIgnoreCase(b.display()));

                    cb.onSuccess(out);
                })
                .addOnFailureListener(cb::onError);
    }

    public void loadCompanyDetails(@NonNull String companyDocId, @NonNull CompanyDetailsCallback cb) {
        String id = companyDocId == null ? "" : companyDocId.trim();
        if (id.isEmpty()) {
            cb.onError(new IllegalArgumentException("companyDocId is empty"));
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("insurance_companies")
                .document(id)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        cb.onError(new IllegalArgumentException("company not found"));
                        return;
                    }

                    String name = doc.getString("name");
                    String phone = doc.getString("phone");
                    String email = doc.getString("email");
                    String website = doc.getString("website");

                    // ✅ This is the one you wanted in details.hp
                    String hp = doc.getString("h_p");

                    cb.onSuccess(new CompanyDetails(id, name, phone, email, website, hp));
                })
                .addOnFailureListener(cb::onError);
    }
}