package com.example.drive_kit.Data.Repository;

import com.example.drive_kit.Model.Driver;
import com.example.drive_kit.Model.NotificationItem;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

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


    /**
     * Defer the notification.
     * It uses the FirebaseFirestore class to access the database.
     * @param uid
     * @param item
     * @param cb
     */
    public void deferNotification(String uid,NotificationItem item, SimpleCallback cb) {
        if (uid == null || item == null) {
            cb.onError(new IllegalArgumentException("uid or item is null"));
            return;
        }
        String fieldName;
        switch (item.getType()) {
            case INSURANCE:
                fieldName = "dismissedInsuranceStage";
                break;
            case TEST:
                fieldName = "dismissedTestStage";
                break;
            case TREATMENT_10K:
                fieldName = "dismissedTreatment10kStage";
                break;
            default:
                cb.onError(new IllegalArgumentException("Unknown notification type: " + item.getType()));
                return;
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


    /**
     * Updates the service date.
     * It uses the FirebaseFirestore class to access the database.
     * @param uid
     * @param type
     * @param newDateMillis
     * @param cb
     */
    public void doneButton(
            String uid,
            NotificationItem.Type type,
            long newDateMillis,
            SimpleCallback cb
    ) {
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
        String formattedField;

        switch (type) {
            case INSURANCE:
                dateField = "insuranceDateMillis";
                dismissedField = "dismissedInsuranceStage";
                formattedField = "formattedInsuranceDate";
                break;

            case TEST:
                dateField = "testDateMillis";
                dismissedField = "dismissedTestStage";
                formattedField = "formattedTestDate";
                break;

            case TREATMENT_10K:
                dateField = "treatmentDateMillis";
                dismissedField = "dismissedTreatment10kStage";
                formattedField = "formattedTreatDate";
                break;

            default:
                cb.onError(new IllegalArgumentException("Unknown notification type: " + type));
                return;
        }

        String formattedDate = formatDate(newDateMillis);

        Map<String, Object> updates = new HashMap<>();
        updates.put(dateField, newDateMillis);

        updates.put(formattedField, formattedDate);

        // reset dismissed stage so notifications can appear again for the new date
        updates.put(dismissedField, null);

        FirebaseFirestore.getInstance()
                .collection("drivers")
                .document(uid)
                .update(updates)
                .addOnSuccessListener(v -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }

    /**
     * Formats millis into "dd/MM/yyyy" using the device timezone.
     */
    private String formatDate(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(new Date(millis));
    }
}
