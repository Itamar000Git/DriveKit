package com.example.drive_kit.Data.Repository;

import com.example.drive_kit.Model.CarModel;
import com.example.drive_kit.Model.Driver;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class ProfileRepository {

    public interface DriverCallback {
        void onSuccess(Driver driver);
        void onError(Exception e);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onError(Exception e);
    }

    public void getDriver(String uid, DriverCallback cb) {
        FirebaseFirestore.getInstance()
                .collection("drivers")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> cb.onSuccess(doc.toObject(Driver.class)))
                .addOnFailureListener(cb::onError);
    }

    // =========================
    // OLD (kept): update only core car fields
    // =========================
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
        if (uid == null || uid.trim().isEmpty()) {
            cb.onError(new IllegalArgumentException("uid is null/empty"));
            return;
        }

        Map<String, Object> updates = new HashMap<>();

        // Driver root
        updates.put("firstName", firstName);
        updates.put("lastName", lastName);
        updates.put("phone", phone);

        // CHANGED: Car nested fields MUST match Car.java field names
        updates.put("car.carNum", carNumber); // CHANGED (was car.carNumber)
        updates.put("car.insuranceDateMillis", insuranceDateMillis);
        updates.put("car.testDateMillis", testDateMillis);
        updates.put("car.treatmentDateMillis", treatmentDateMillis);

        // OPTIONAL: keep if you really want them in Firestore (not required by your model)
        // updates.put("formattedInsuranceDate", formatDate(insuranceDateMillis));
        // updates.put("formattedTestDate", formatDate(testDateMillis));
        // updates.put("formattedTreatDate", formatDate(treatmentDateMillis));

        FirebaseFirestore.getInstance()
                .collection("drivers")
                .document(uid)
                .update(updates)
                .addOnSuccessListener(v -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }

    // =========================
    // NEW: update including manufacturer / specificModel / year
    // (this matches the ViewModel change you asked for)
    // =========================
    public void updateProfileFields(
            String uid,
            String firstName,
            String lastName,
            String phone,
            String carNumber,
            long insuranceDateMillis,
            long testDateMillis,
            long treatmentDateMillis,
            String manufacturer,         // NEW (store enum name string)
            String carSpecificModel,     // NEW
            int year,                    // NEW
            SimpleCallback cb
    ) {
        if (uid == null || uid.trim().isEmpty()) {
            cb.onError(new IllegalArgumentException("uid is null/empty"));
            return;
        }

        Map<String, Object> updates = new HashMap<>();

        // Driver root
        updates.put("firstName", firstName);
        updates.put("lastName", lastName);
        updates.put("phone", phone);

        // Car nested fields (match Car.java)
        updates.put("car.carNum", carNumber);
        updates.put("car.insuranceDateMillis", insuranceDateMillis);
        updates.put("car.testDateMillis", testDateMillis);
        updates.put("car.treatmentDateMillis", treatmentDateMillis);

        // NEW: extra car data
        updates.put("car.carModel", manufacturer);          // NEW  (e.g., "TOYOTA")
        updates.put("car.carSpecificModel", carSpecificModel); // NEW (e.g., "COROLLA")
        updates.put("car.year", year);                      // NEW

        // OPTIONAL: keep if you want extra cached formatted fields in Firestore
        // updates.put("formattedInsuranceDate", formatDate(insuranceDateMillis));
        // updates.put("formattedTestDate", formatDate(testDateMillis));
        // updates.put("formattedTreatDate", formatDate(treatmentDateMillis));

        FirebaseFirestore.getInstance()
                .collection("drivers")
                .document(uid)
                .update(updates)
                .addOnSuccessListener(v -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }

    private String formatDate(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(new Date(millis));
    }
}

