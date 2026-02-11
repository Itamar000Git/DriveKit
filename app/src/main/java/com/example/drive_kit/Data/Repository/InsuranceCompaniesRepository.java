//package com.example.drive_kit.Data.Repository;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//
//import com.example.drive_kit.View.InsuranceCompany;
//import com.google.firebase.firestore.FirebaseFirestore;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class InsuranceCompaniesRepository {
//
//    public interface Callback {
//        void onResult(List<InsuranceCompany> companies);
//        void onError(Exception e);
//    }
//
//    public interface CompanyNameCallback {
//        void onSuccess(@Nullable String companyName);
//        void onError(@NonNull Exception e);
//    }
//
//    // NEW: single company object callback
//    public interface CompanyCallback {
//        void onSuccess(@NonNull InsuranceCompany company);
//        void onError(@NonNull Exception e);
//    }
//
//    // NEW: simple success/fail callback for updates
//    public interface SimpleCallback {
//        void onSuccess();
//        void onError(@NonNull Exception e);
//    }
//
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
//                        String internalId = doc.getString("id");
//                        if (internalId == null || internalId.trim().isEmpty()) internalId = doc.getId();
//                        list.add(new InsuranceCompany(
//                                internalId,
//                                name == null ? "" : name,
//                                phone == null ? "" : phone,
//                                email == null ? "" : email,
//                                website == null ? "" : website,
//                                isPartner != null && isPartner
//                        ));
//                    });
//
//                    java.util.Collections.sort(list, (a, b) ->
//                            a.getName().compareToIgnoreCase(b.getName())
//                    );
//
//                    cb.onResult(list);
//                })
//                .addOnFailureListener(cb::onError);
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
//    /**
//     * NEW
//     * Loads full company data for profile/edit screens.
//     */
////    public void getCompanyById(@NonNull String companyId, @NonNull CompanyCallback cb) {
////        String id = safe(companyId);
////        if (id.isEmpty()) {
////            cb.onError(new IllegalArgumentException("companyId is empty"));
////            return;
////        }
////
////        db.collection("insurance_companies")
////                .document(id)
////                .get()
////                .addOnSuccessListener(doc -> {
////                    if (!doc.exists()) {
////                        cb.onError(new IllegalArgumentException("company not found"));
////                        return;
////                    }
////
////                    String name = doc.getString("name");
////                    String phone = doc.getString("phone");
////                    String email = doc.getString("email");
////                    String website = doc.getString("website");
////                    Boolean isPartner = doc.getBoolean("isPartner");
////
////                    String internalId = doc.getString("id");
////                    if (internalId == null || internalId.trim().isEmpty()) internalId = doc.getId();
////
////                    InsuranceCompany company = new InsuranceCompany(
////                            internalId,
////                            name == null ? "" : name,
////                            phone == null ? "" : phone,
////                            email == null ? "" : email,
////                            website == null ? "" : website,
////                            isPartner != null && isPartner
////                    );
////
////
////                    cb.onSuccess(company);
////                })
////                .addOnFailureListener(cb::onError);
////    }
//    public void getCompanyById(@NonNull String companyId, @NonNull CompanyCallback cb) {
//        String id = safe(companyId);
//        if (id.isEmpty()) {
//            cb.onError(new IllegalArgumentException("companyId is empty"));
//            return;
//        }
//
//        db.collection("insurance_companies")
//                .document(id)
//                .get()
//                .addOnSuccessListener(doc -> {
//                    if (!doc.exists()) {
//                        cb.onError(new IllegalArgumentException("company not found"));
//                        return;
//                    }
//
//                    String name = doc.getString("name");
//                    String phone = doc.getString("phone");
//                    String email = doc.getString("email");
//                    String website = doc.getString("website");
//                    Boolean isPartner = doc.getBoolean("isPartner");
//
//                    // IMPORTANT: prefer h.p for display in "company id" field
//                    String hp = "";
//                    Object hpObj = doc.get("h_p"); // literal field name with dot
//                    if (hpObj != null) hp = hpObj.toString().trim();
//
//                    String internalId = hp;
//                    if (internalId.isEmpty()) {
//                        String idField = doc.getString("id");
//                        internalId = (idField == null || idField.trim().isEmpty()) ? doc.getId() : idField.trim();
//                    }
//
//                    InsuranceCompany company = new InsuranceCompany(
//                            internalId,
//                            name == null ? "" : name,
//                            phone == null ? "" : phone,
//                            email == null ? "" : email,
//                            website == null ? "" : website,
//                            isPartner != null && isPartner
//                    );
//
//                    cb.onSuccess(company);
//                })
//                .addOnFailureListener(cb::onError);
//    }
//
//
//    /**
//     * NEW
//     * Updates only editable fields of the company profile.
//     * Keeps everything else unchanged (e.g., category, isPartner).
//     */
//    public void updateCompanyProfile(
//            @NonNull String companyId,
//            @NonNull String name,
//            @NonNull String phone,
//            @NonNull String email,
//            @NonNull String website,
//            @NonNull SimpleCallback cb
//    ) {
//        String id = safe(companyId);
//        if (id.isEmpty()) {
//            cb.onError(new IllegalArgumentException("companyId is empty"));
//            return;
//        }
//
//        Map<String, Object> updates = new HashMap<>();
//        updates.put("name", safe(name));
//        updates.put("phone", safe(phone));
//        updates.put("email", safe(email));
//        updates.put("website", safe(website));
//        updates.put("updatedAt", com.google.firebase.firestore.FieldValue.serverTimestamp());
//
//        db.collection("insurance_companies")
//                .document(id)
//                .update(updates)
//                .addOnSuccessListener(v -> cb.onSuccess())
//                .addOnFailureListener(cb::onError);
//    }
//
//    private String safe(String s) {
//        return s == null ? "" : s.trim();
//    }
//}


// InsuranceCompaniesRepository.java
package com.example.drive_kit.Data.Repository;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.drive_kit.Model.InsuranceCompany;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InsuranceCompaniesRepository {

    private static final String TAG_UPLOAD = "LOGO_UPLOAD";
    private static final String TAG_FS = "LOGO_FIRESTORE";

    public interface Callback {
        void onResult(List<InsuranceCompany> companies);
        void onError(Exception e);
    }

    public interface CompanyNameCallback {
        void onSuccess(@Nullable String companyName);
        void onError(@NonNull Exception e);
    }

    public interface CompanyCallback {
        void onSuccess(@NonNull InsuranceCompany company);
        void onError(@NonNull Exception e);
    }

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
                        String name = safe(doc.getString("name"));
                        String phone = safe(doc.getString("phone"));
                        String email = safe(doc.getString("email"));
                        String website = safe(doc.getString("website"));
                        Boolean isPartner = doc.getBoolean("isPartner");

                        String internalId = extractDisplayId(doc);

                        InsuranceCompany company = new InsuranceCompany(
                                internalId,
                                name,
                                phone,
                                email,
                                website,
                                isPartner != null && isPartner
                        );

                        company.setLogoUrl(safe(doc.getString("logoUrl")));
                        list.add(company);
                    });

                    java.util.Collections.sort(list, (a, b) ->
                            safe(a.getName()).compareToIgnoreCase(safe(b.getName()))
                    );

                    cb.onResult(list);
                })
                .addOnFailureListener(cb::onError);
    }

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
                    String name = safe(doc.getString("name"));
                    cb.onSuccess(name.isEmpty() ? null : name);
                })
                .addOnFailureListener(cb::onError);
    }

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

                    String name = safe(doc.getString("name"));
                    String phone = safe(doc.getString("phone"));
                    String email = safe(doc.getString("email"));
                    String website = safe(doc.getString("website"));
                    Boolean isPartner = doc.getBoolean("isPartner");

                    String internalId = extractDisplayId(doc);

                    InsuranceCompany company = new InsuranceCompany(
                            internalId,
                            name,
                            phone,
                            email,
                            website,
                            isPartner != null && isPartner
                    );

                    company.setLogoUrl(safe(doc.getString("logoUrl")));
                    cb.onSuccess(company);
                })
                .addOnFailureListener(cb::onError);
    }

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

    // ✅ NEW: update ONLY logoUrl field
    public void updateCompanyLogoUrl(
            @NonNull String companyDocId,
            @NonNull String logoUrl,
            @NonNull SimpleCallback cb
    ) {
        String id = safe(companyDocId);
        if (id.isEmpty()) {
            cb.onError(new IllegalArgumentException("companyId is empty"));
            return;
        }

        String url = safe(logoUrl);
        if (url.isEmpty()) {
            cb.onError(new IllegalArgumentException("logoUrl is empty"));
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("logoUrl", url);
        updates.put("updatedAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

        Log.d(TAG_FS, "updateCompanyLogoUrl docId=" + id + " url=" + url);

        db.collection("insurance_companies")
                .document(id)
                .update(updates)
                .addOnSuccessListener(v -> {
                    Log.d(TAG_FS, "update logoUrl success");
                    cb.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG_FS, "update logoUrl failed", e);
                    cb.onError(e); // ✅ critical: must callback so UI stops loading
                });
    }

    // ✅ NEW: upload image to Storage and save URL into Firestore.logoUrl
    public void uploadCompanyLogoAndSave(
            @NonNull String companyDocId,
            @NonNull Uri localUri,
            @NonNull SimpleCallback cb
    ) {
        String id = safe(companyDocId);
        if (id.isEmpty()) {
            cb.onError(new IllegalArgumentException("companyId is empty"));
            return;
        }
        if (localUri == null) {
            cb.onError(new IllegalArgumentException("localUri is null"));
            return;
        }

        Log.d(TAG_UPLOAD, "upload start docId=" + id + " uri=" + localUri);

        com.google.firebase.storage.StorageReference ref =
                com.google.firebase.storage.FirebaseStorage.getInstance()
                        .getReference()
                        .child("insurance_logos")
                        .child(id)
                        .child("logo.jpg");

        ref.putFile(localUri)
                .addOnSuccessListener(t -> {
                    Log.d(TAG_UPLOAD, "putFile success");
                    ref.getDownloadUrl()
                            .addOnSuccessListener(downloadUri -> {
                                Log.d(TAG_UPLOAD, "downloadUrl=" + downloadUri);
                                updateCompanyLogoUrl(id, downloadUri.toString(), cb);
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG_UPLOAD, "getDownloadUrl failed", e);
                                cb.onError(e); // ✅ critical
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG_UPLOAD, "putFile failed", e);
                    cb.onError(e); // ✅ critical
                });
    }

    // =========================================================
    // Helpers
    // =========================================================

    private String extractDisplayId(@NonNull DocumentSnapshot doc) {
        String hp = "";
        Object hpObj = doc.get("h_p");
        if (hpObj != null) hp = hpObj.toString().trim();
        if (!hp.isEmpty()) return hp;

        String idField = safe(doc.getString("id"));
        if (!idField.isEmpty()) return idField;

        return doc.getId();
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
