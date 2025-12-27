package com.example.drive_kit.Data.Repository;

import com.example.drive_kit.Model.Driver;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Repository for accessing the database.
 * It uses the FirebaseFirestore class to access the database.
 * Access to the driver data for the current user.
 */
public class ProfileRepository {

    public interface DriverCallback {
        void onSuccess(Driver driver);
        void onError(Exception e);
    }

    /**
     * Gets the driver from the database.
     * It uses the FirebaseFirestore class to access the database.
     * @param uid
     * @param cb
     */
    public void getDriver(String uid, DriverCallback cb) {
        FirebaseFirestore.getInstance()
                .collection("drivers")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> cb.onSuccess(doc.toObject(Driver.class)))
                .addOnFailureListener(cb::onError);
    }
}
