package com.example.drive_kit.Data.Repository;

import android.net.Uri;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class ProfileRepository {

    public interface DriverCallback {
        void onSuccess(com.example.drive_kit.Model.Driver driver);
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
                .addOnSuccessListener(doc -> cb.onSuccess(doc.toObject(com.example.drive_kit.Model.Driver.class)))
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
        if (isBlank(uid)) {
            cb.onError(new IllegalArgumentException("uid is null/empty"));
            return;
        }

        Map<String, Object> updates = new HashMap<>();

        updates.put("firstName", firstName);
        updates.put("lastName", lastName);
        updates.put("phone", phone);

        updates.put("car.carNum", carNumber);
        updates.put("car.insuranceDateMillis", insuranceDateMillis);
        updates.put("car.testDateMillis", testDateMillis);
        updates.put("car.treatmentDateMillis", treatmentDateMillis);

        FirebaseFirestore.getInstance()
                .collection("drivers")
                .document(uid)
                .update(updates)
                .addOnSuccessListener(v -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }

    // =========================
    // Existing overload: manufacturer/model/year (kept)
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
            String manufacturer,
            String carSpecificModel,
            int year,
            SimpleCallback cb
    ) {
        if (isBlank(uid)) {
            cb.onError(new IllegalArgumentException("uid is null/empty"));
            return;
        }

        Map<String, Object> updates = new HashMap<>();

        updates.put("firstName", firstName);
        updates.put("lastName", lastName);
        updates.put("phone", phone);

        updates.put("car.carNum", carNumber);
        updates.put("car.insuranceDateMillis", insuranceDateMillis);
        updates.put("car.testDateMillis", testDateMillis);
        updates.put("car.treatmentDateMillis", treatmentDateMillis);

        updates.put("car.carModel", manufacturer);
        updates.put("car.carSpecificModel", carSpecificModel);
        updates.put("car.year", year);

        FirebaseFirestore.getInstance()
                .collection("drivers")
                .document(uid)
                .update(updates)
                .addOnSuccessListener(v -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }

    // =========================
    // NEW: update profile + car + image
    // imageUriOrUrl can be:
    // - "" / null  -> do not update image field
    // - "http..."  -> store as is
    // - "content://..." or "file://..." -> upload to Storage, store downloadUrl
    // =========================
    public void updateProfileFieldsWithImage(
            String uid,
            String firstName,
            String lastName,
            String phone,
            String carNumber,
            long insuranceDateMillis,
            long testDateMillis,
            long treatmentDateMillis,
            String manufacturer,
            String carSpecificModel,
            int year,
            String imageUriOrUrl,
            SimpleCallback cb
    ) {
        if (isBlank(uid)) {
            cb.onError(new IllegalArgumentException("uid is null/empty"));
            return;
        }

        // If no image provided -> just update fields (without touching carImageUri)
        if (isBlank(imageUriOrUrl)) {
            updateProfileFields(
                    uid,
                    firstName,
                    lastName,
                    phone,
                    carNumber,
                    insuranceDateMillis,
                    testDateMillis,
                    treatmentDateMillis,
                    manufacturer,
                    carSpecificModel,
                    year,
                    cb
            );
            return;
        }

        // If already a web URL -> update directly
        if (isHttpUrl(imageUriOrUrl)) {
            Map<String, Object> updates = baseUpdates(
                    firstName, lastName, phone, carNumber,
                    insuranceDateMillis, testDateMillis, treatmentDateMillis,
                    manufacturer, carSpecificModel, year
            );
            updates.put("car.carImageUri", imageUriOrUrl);

            FirebaseFirestore.getInstance()
                    .collection("drivers")
                    .document(uid)
                    .update(updates)
                    .addOnSuccessListener(v -> cb.onSuccess())
                    .addOnFailureListener(cb::onError);
            return;
        }

        // Otherwise assume it's local Uri (content:// or file://) -> upload to Storage first
        Uri localUri = Uri.parse(imageUriOrUrl);

        StorageReference ref = FirebaseStorage.getInstance()
                .getReference()
                .child("drivers")
                .child(uid)
                .child("car_profile_" + System.currentTimeMillis() + ".jpg");

        ref.putFile(localUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException() != null ? task.getException() : new Exception("Upload failed");
                    }
                    return ref.getDownloadUrl();
                })
                .addOnSuccessListener(downloadUri -> {
                    String downloadUrl = downloadUri.toString();

                    Map<String, Object> updates = baseUpdates(
                            firstName, lastName, phone, carNumber,
                            insuranceDateMillis, testDateMillis, treatmentDateMillis,
                            manufacturer, carSpecificModel, year
                    );
                    updates.put("car.carImageUri", downloadUrl);

                    FirebaseFirestore.getInstance()
                            .collection("drivers")
                            .document(uid)
                            .update(updates)
                            .addOnSuccessListener(v -> cb.onSuccess())
                            .addOnFailureListener(cb::onError);
                })
                .addOnFailureListener(cb::onError);
    }

    // =========================
    // Helpers
    // =========================

    private Map<String, Object> baseUpdates(
            String firstName,
            String lastName,
            String phone,
            String carNumber,
            long insuranceDateMillis,
            long testDateMillis,
            long treatmentDateMillis,
            String manufacturer,
            String carSpecificModel,
            int year
    ) {
        Map<String, Object> updates = new HashMap<>();

        updates.put("firstName", firstName);
        updates.put("lastName", lastName);
        updates.put("phone", phone);

        updates.put("car.carNum", carNumber);
        updates.put("car.insuranceDateMillis", insuranceDateMillis);
        updates.put("car.testDateMillis", testDateMillis);
        updates.put("car.treatmentDateMillis", treatmentDateMillis);

        updates.put("car.carModel", manufacturer);
        updates.put("car.carSpecificModel", carSpecificModel);
        updates.put("car.year", year);

        return updates;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private boolean isHttpUrl(String s) {
        if (s == null) return false;
        String t = s.trim().toLowerCase();
        return t.startsWith("http://") || t.startsWith("https://");
    }
}
