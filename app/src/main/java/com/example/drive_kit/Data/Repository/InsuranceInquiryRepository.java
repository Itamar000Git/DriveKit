package com.example.drive_kit.Data.Repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class InsuranceInquiryRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface InquiryCallback {
        void onSuccess();
        void onError(Exception e);
    }

    public interface LoadInquiriesCallback {
        void onSuccess(List<Map<String, Object>> inquiries);
        void onError(Exception e);
    }

    /**
     * Creates a full inquiry document that insurance companies can view later.
     */
    public void logInquiry(@NonNull String userId,
                           @NonNull String companyId,
                           @Nullable String companyName,
                           @Nullable String driverName,
                           @Nullable String driverPhone,
                           @Nullable String driverEmail,
                           @Nullable String carNumber,
                           @Nullable String carModel,
                           @Nullable String message,
                           @Nullable InquiryCallback cb) {

        String safeUserId = safe(userId);
        String safeCompanyId = normalizeCompanyId(companyId);

        if (safeUserId.isEmpty() || safeCompanyId.isEmpty()) {
            if (cb != null) cb.onError(new IllegalArgumentException("userId/companyId is empty"));
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("userId", safeUserId);
        data.put("companyId", safeCompanyId);
        data.put("companyName", safe(companyName));

        // Existing log semantics
        data.put("type", "OPEN_DETAILS");
        data.put("source", "DriveKit");

        // Fields needed by insurance-side screen
        data.put("driverName", safe(driverName));
        data.put("driverPhone", safe(driverPhone));
        data.put("driverEmail", safe(driverEmail));
        data.put("carNumber", safe(carNumber));
        data.put("carModel", safe(carModel));
        data.put("message", safe(message));

        // Workflow fields
        data.put("status", "new");
        data.put("createdAt", FieldValue.serverTimestamp());
        data.put("updatedAt", FieldValue.serverTimestamp());

        db.collection("insurance_inquiries")
                .add(data)
                .addOnSuccessListener(r -> {
                    if (cb != null) cb.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (cb != null) cb.onError(e);
                });
    }

    /**
     * Optional helper: mark inquiry as contacted.
     */
    public void markAsContacted(@NonNull String inquiryDocId, @Nullable InquiryCallback cb) {
        String safeDocId = safe(inquiryDocId);
        if (safeDocId.isEmpty()) {
            if (cb != null) cb.onError(new IllegalArgumentException("inquiryDocId is empty"));
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "contacted");
        updates.put("updatedAt", FieldValue.serverTimestamp());

        db.collection("insurance_inquiries")
                .document(safeDocId)
                .update(updates)
                .addOnSuccessListener(v -> {
                    if (cb != null) cb.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (cb != null) cb.onError(e);
                });
    }

    /**
     * Loads all inquiries for one insurance company (latest first).
     */
    public void loadInquiriesForCompany(@NonNull String companyId, @NonNull LoadInquiriesCallback cb) {
        String safeCompanyId = normalizeCompanyId(companyId);
    /// ///////////////////////////
        final String normalizedCompanyId = companyId.trim().toLowerCase();
        android.util.Log.d("INS_INQ_DEBUG", "Repo query companyId = [" + normalizedCompanyId + "]");

        if (safeCompanyId.isEmpty()) {
            cb.onError(new IllegalArgumentException("companyId is empty"));
            return;
        }

        db.collection("insurance_inquiries")
                .whereEqualTo("companyId", companyId.trim().toLowerCase())
                .get()

                .addOnSuccessListener(qs -> {
                    /// ////////////
                    android.util.Log.d("INS_INQ_DEBUG", "Repo query size = " + qs.size());

                    List<Map<String, Object>> out = new ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot d : qs.getDocuments()) {
                        Map<String, Object> m = new HashMap<>();
                        m.put("docId", d.getId());
                        m.put("driverName", d.getString("driverName"));
                        m.put("driverPhone", d.getString("driverPhone"));
                        m.put("driverEmail", d.getString("driverEmail"));
                        m.put("carNumber", d.getString("carNumber"));
                        m.put("carModel", d.getString("carModel"));
                        m.put("message", d.getString("message"));
                        m.put("status", d.getString("status"));
                        m.put("companyId", d.getString("companyId"));
                        m.put("companyName", d.getString("companyName"));
                        m.put("createdAt", d.getTimestamp("createdAt"));
                        m.put("updatedAt", d.getTimestamp("updatedAt"));
                        out.add(m);
                    }
                    cb.onSuccess(out);
                })
                .addOnFailureListener(cb::onError);
    }

    /**
     * Legacy helper (backward compatible) – logs only basic fields.
     */
    public void logInquiry(String userId, String companyId, String companyName) {
        logInquiry(
                userId,
                companyId,
                companyName,
                "",   // driverName
                "",   // driverPhone
                "",   // driverEmail
                "",   // carNumber
                "",   // carModel
                "",   // message
                null  // no callback needed in legacy flow
        );
    }
    public void loadInquiriesForCompanyByStatus(String companyId, String status, LoadInquiriesCallback cb) {
        if (companyId == null || companyId.trim().isEmpty()) {
            cb.onError(new IllegalArgumentException("companyId is empty"));
            return;
        }

        String normalizedCompanyId = companyId.trim().toLowerCase();
        String normalizedStatus = status == null ? "" : status.trim().toLowerCase();

        db.collection("insurance_inquiries")
                .whereEqualTo("companyId", normalizedCompanyId)
                .whereEqualTo("status", normalizedStatus)
                .get()
                .addOnSuccessListener(qs -> {
                    java.util.List<java.util.Map<String, Object>> out = new java.util.ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot d : qs.getDocuments()) {
                        java.util.Map<String, Object> m = new java.util.HashMap<>();
                        m.put("docId", d.getId());
                        m.put("driverName", d.getString("driverName"));
                        m.put("driverPhone", d.getString("driverPhone"));
                        m.put("driverEmail", d.getString("driverEmail"));
                        m.put("carNumber", d.getString("carNumber"));
                        m.put("carModel", d.getString("carModel"));
                        m.put("message", d.getString("message"));
                        m.put("status", d.getString("status"));
                        m.put("companyId", d.getString("companyId"));
                        m.put("companyName", d.getString("companyName"));
                        m.put("createdAt", d.getTimestamp("createdAt"));
                        out.add(m);
                    }
                    cb.onSuccess(out);
                })
                .addOnFailureListener(cb::onError);
    }


    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private String normalizeCompanyId(String companyId) {
        String x = safe(companyId);
        return x.toLowerCase(Locale.ROOT);
    }
}
