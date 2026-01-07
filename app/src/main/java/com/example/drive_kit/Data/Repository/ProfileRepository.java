package com.example.drive_kit.Data.Repository;

import com.example.drive_kit.Model.Driver;
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
 * Access to the driver data for the current user.
 */
public class ProfileRepository {

    public interface DriverCallback {
        void onSuccess(Driver driver);
        void onError(Exception e);
    }

    // Simple callback for update operations (success / error)
    public interface SimpleCallback {
        void onSuccess();
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

    /**
     * Updates selected driver fields in the database.
     * It updates both millis fields and their formatted string fields.
     * It also resets dismissed stages to allow notifications again after changing dates.
     * @param uid
     * @param firstName
     * @param lastName
     * @param phone
     * @param carNumber
     * @param insuranceDateMillis
     * @param testDateMillis
     * @param treatmentDateMillis
     * @param cb
     */
    public void updateProfileFields(
            String uid,
            String firstName,
            String lastName,
            String phone,
            String carNumber,
            long insuranceDateMillis,
            long testDateMillis,
            long treatmentDateMillis,
            SimpleCallback cb
    ) {
        Map<String, Object> updates = new HashMap<>();

        // Basic fields
        updates.put("firstName", firstName);
        updates.put("lastName", lastName);
        updates.put("phone", phone);
        updates.put("carNumber", carNumber);

        // Millis date fields
        updates.put("insuranceDateMillis", insuranceDateMillis);
        updates.put("testDateMillis", testDateMillis);
        updates.put("treatmentDateMillis", treatmentDateMillis);

        // Formatted date fields (match your Firestore names)
        updates.put("formattedInsuranceDate", formatDate(insuranceDateMillis));
        updates.put("formattedTestDate", formatDate(testDateMillis));
        updates.put("formattedTreatDate", formatDate(treatmentDateMillis));

        // Reset dismissed stages so notifications can work again after the date is updated
        updates.put("dismissedInsuranceStage", null);
        updates.put("dismissedTestStage", null);
        updates.put("dismissedTreatment10kStage", null);

        FirebaseFirestore.getInstance()
                .collection("drivers")
                .document(uid)
                .update(updates)
                .addOnSuccessListener(v -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }

    // Converts millis to "dd/MM/yyyy" using the device timezone
    private String formatDate(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(new Date(millis));
    }
}
