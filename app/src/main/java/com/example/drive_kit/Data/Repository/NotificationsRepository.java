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



    public void updateServiceDate(String uid,NotificationItem.Type type,long newDateMillis,SimpleCallback cb) {

        if (uid == null || uid.trim().isEmpty()) {
            cb.onError(new IllegalArgumentException("uid is null/empty"));
            return;
        }
        if (newDateMillis <= 0) {
            cb.onError(new IllegalArgumentException("newDateMillis invalid"));
            return;
        }

        String dateField;
        String dismissedField;

        if (type == NotificationItem.Type.INSURANCE) {
            dateField = "insuranceDateMillis";
            dismissedField = "dismissedInsuranceStage";
        } else {
            dateField = "testDateMillis";
            dismissedField = "dismissedTestStage";
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put(dateField, newDateMillis);

        updates.put(dismissedField, null);

        FirebaseFirestore.getInstance()
                .collection("drivers")
                .document(uid)
                .update(updates)
                .addOnSuccessListener(v -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }
}
