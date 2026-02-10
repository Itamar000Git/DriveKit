//package com.example.drive_kit.Data.Repository;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//
//import com.example.drive_kit.View.InsuranceCompany;
//import com.google.firebase.firestore.FirebaseFirestore;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class InsuranceCompaniesRepository {
//
//    public interface Callback {
//        void onResult(List<InsuranceCompany> companies);
//        void onError(Exception e);
//    }
//    public interface CompanyNameCallback {
//        void onSuccess(@Nullable String companyName);
//        void onError(@NonNull Exception e);
//    }
//    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
//
//    public void loadCarCompanies(Callback cb) {
//        db.collection("insurance_companies")
//                .whereEqualTo("category", "car")
//                .get()
//                .addOnSuccessListener(snap -> {
//                    List<InsuranceCompany> list = new ArrayList<>();
//                    snap.getDocuments().forEach(doc -> {
//                        String id = doc.getId();
//                        String name = doc.getString("name");
//                        String phone = doc.getString("phone");
//                        String email = doc.getString("email");
//                        String website = doc.getString("website");
//                        Boolean isPartner = doc.getBoolean("isPartner");
//
//                        list.add(new InsuranceCompany(
//                                id,
//                                name == null ? "" : name,
//                                phone == null ? "" : phone,
//                                email == null ? "" : email,
//                                website == null ? "" : website,
//                                isPartner != null && isPartner
//                        ));
//                    });
//
//                    // ✅ מיון מקומי במקום orderBy
//                    java.util.Collections.sort(list, (a, b) ->
//                            a.getName().compareToIgnoreCase(b.getName())
//                    );
//
//                    cb.onResult(list);
//                })
//                .addOnFailureListener(cb::onError);
//
//    }
//
//    /**
//     * Loads the company name field from Firestore.
//     * If the document doesn't exist or "name" is empty, returns null.
//     */
//    public void getCompanyName(@NonNull String companyId, @NonNull CompanyNameCallback cb) {
//        String id = safe(companyId);
//        if (id.isEmpty()) {
//            cb.onSuccess(null);
//            return;
//        }
//
//        db.collection("insurance_companies")
//                .document(id)
//                .get()
//                .addOnSuccessListener(doc -> {
//                    if (!doc.exists()) {
//                        cb.onSuccess(null);
//                        return;
//                    }
//                    String name = doc.getString("name");
//                    if (name == null || name.trim().isEmpty()) {
//                        cb.onSuccess(null);
//                    } else {
//                        cb.onSuccess(name.trim());
//                    }
//                })
//                .addOnFailureListener(cb::onError);
//    }
//
//
//
//
//    private String safe(String s) {
//        return s == null ? "" : s.trim();
//    }
//}


package com.example.drive_kit.Data.Repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.drive_kit.View.InsuranceCompany;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InsuranceCompaniesRepository {

    public interface Callback {
        void onResult(List<InsuranceCompany> companies);
        void onError(Exception e);
    }

    public interface CompanyNameCallback {
        void onSuccess(@Nullable String companyName);
        void onError(@NonNull Exception e);
    }

    // NEW: single company object callback
    public interface CompanyCallback {
        void onSuccess(@NonNull InsuranceCompany company);
        void onError(@NonNull Exception e);
    }

    // NEW: simple success/fail callback for updates
    public interface SimpleCallback {
        void onSuccess();
        void onError(@NonNull Exception e);
    }

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void loadCarCompanies(Callback cb) {
        db.collection("insurance_companies")
                .whereEqualTo("category", "car")
                .get()
                .addOnSuccessListener(snap -> {
                    List<InsuranceCompany> list = new ArrayList<>();
                    snap.getDocuments().forEach(doc -> {
                        String id = doc.getId();
                        String name = doc.getString("name");
                        String phone = doc.getString("phone");
                        String email = doc.getString("email");
                        String website = doc.getString("website");
                        Boolean isPartner = doc.getBoolean("isPartner");

                        String internalId = doc.getString("id");
                        if (internalId == null || internalId.trim().isEmpty()) internalId = doc.getId();
                        list.add(new InsuranceCompany(
                                internalId,
                                name == null ? "" : name,
                                phone == null ? "" : phone,
                                email == null ? "" : email,
                                website == null ? "" : website,
                                isPartner != null && isPartner
                        ));
                    });

                    java.util.Collections.sort(list, (a, b) ->
                            a.getName().compareToIgnoreCase(b.getName())
                    );

                    cb.onResult(list);
                })
                .addOnFailureListener(cb::onError);
    }

    /**
     * Loads the company name field from Firestore.
     * If the document doesn't exist or "name" is empty, returns null.
     */
    public void getCompanyName(@NonNull String companyId, @NonNull CompanyNameCallback cb) {
        String id = safe(companyId);
        if (id.isEmpty()) {
            cb.onSuccess(null);
            return;
        }

        db.collection("insurance_companies")
                .document(id)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        cb.onSuccess(null);
                        return;
                    }
                    String name = doc.getString("name");
                    if (name == null || name.trim().isEmpty()) {
                        cb.onSuccess(null);
                    } else {
                        cb.onSuccess(name.trim());
                    }
                })
                .addOnFailureListener(cb::onError);
    }

    /**
     * NEW
     * Loads full company data for profile/edit screens.
     */
    public void getCompanyById(@NonNull String companyId, @NonNull CompanyCallback cb) {
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
                        cb.onError(new IllegalArgumentException("company not found"));
                        return;
                    }

                    String name = doc.getString("name");
                    String phone = doc.getString("phone");
                    String email = doc.getString("email");
                    String website = doc.getString("website");
                    Boolean isPartner = doc.getBoolean("isPartner");

                    String internalId = doc.getString("id");
                    if (internalId == null || internalId.trim().isEmpty()) internalId = doc.getId();

                    InsuranceCompany company = new InsuranceCompany(
                            internalId,
                            name == null ? "" : name,
                            phone == null ? "" : phone,
                            email == null ? "" : email,
                            website == null ? "" : website,
                            isPartner != null && isPartner
                    );


                    cb.onSuccess(company);
                })
                .addOnFailureListener(cb::onError);
    }

    /**
     * NEW
     * Updates only editable fields of the company profile.
     * Keeps everything else unchanged (e.g., category, isPartner).
     */
    public void updateCompanyProfile(
            @NonNull String companyId,
            @NonNull String name,
            @NonNull String phone,
            @NonNull String email,
            @NonNull String website,
            @NonNull SimpleCallback cb
    ) {
        String id = safe(companyId);
        if (id.isEmpty()) {
            cb.onError(new IllegalArgumentException("companyId is empty"));
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", safe(name));
        updates.put("phone", safe(phone));
        updates.put("email", safe(email));
        updates.put("website", safe(website));
        updates.put("updatedAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

        db.collection("insurance_companies")
                .document(id)
                .update(updates)
                .addOnSuccessListener(v -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
