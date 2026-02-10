package com.example.drive_kit.Data.Repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.drive_kit.Model.Driver;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * DriverRepository
 *
 * Data layer for loading a Driver object from Firestore.
 *
 * This repository exists because InsuranceInquiryRepository is responsible only for "inquiries".
 * Fetching the driver document is a separate responsibility.
 *
 * Behavior is identical to the old BottomSheet code:
 * - Reads from: collection("drivers").document(userId).get()
 * - Converts the document to Driver.class
 */
public class DriverRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface DriverCallback {
        void onSuccess(@Nullable Driver driver);
        void onError(@NonNull Exception e);
    }

    /**
     * Loads the latest driver data from Firestore.
     *
     * @param userId Firebase user id (document id in "drivers" collection)
     * @param cb callback with Driver (can be null if document is missing)
     */
    public void getDriverById(@NonNull String userId, @NonNull DriverCallback cb) {
        String uid = safe(userId);
        if (uid.isEmpty()) {
            cb.onError(new IllegalArgumentException("userId is empty"));
            return;
        }

        db.collection("drivers")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> cb.onSuccess(doc.toObject(Driver.class)))
                .addOnFailureListener(cb::onError);
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
