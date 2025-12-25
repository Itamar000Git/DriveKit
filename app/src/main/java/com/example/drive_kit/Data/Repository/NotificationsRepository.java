package com.example.drive_kit.Data.Repository;

import com.example.drive_kit.Model.Driver;
import com.example.drive_kit.Model.NotificationItem;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Repository for accessing the database.
 * It uses the FirebaseFirestore class to access the database.
 */
public class NotificationsRepository {

    /**
     * Callback interface for getting the driver.
     * It has two methods: onSuccess and onError.
     */
    public interface DriverCallback {
        void onSuccess(Driver driver);
        void onError(Exception e);
    }
    public interface SimpleCallback {
        void onSuccess();
        void onError(Exception e);
    }


    /**
     * Gets the driver from the database.
     * It uses the FirebaseFirestore class to access the database.
     * If the driver is found, it calls the onSuccess method of the callback with the driver object.
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



    public void deferNotification(String uid,NotificationItem item, SimpleCallback cb) {
        if (uid == null || item == null) {
            cb.onError(new IllegalArgumentException("uid or item is null"));
            return;
        }
        String fieldName;
        if (item.getType() == NotificationItem.Type.INSURANCE) {
            fieldName = "dismissedInsuranceStage";
        } else {
            fieldName = "dismissedTestStage";
        }

        Map<String, Object> update = new HashMap<>();
        update.put(fieldName, item.getStage().name());
        FirebaseFirestore.getInstance()
                .collection("drivers")
                .document(uid)
                .update(update)
                .addOnSuccessListener(v -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }
}
