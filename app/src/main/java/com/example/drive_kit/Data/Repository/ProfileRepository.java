
package com.example.drive_kit.Data.Repository;

import android.net.Uri;

import com.example.drive_kit.Model.Driver;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

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
    // OLD (kept): core update (no manufacturer/model/year, no image)
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
    // Existing overload (kept): manufacturer/model/year (no image)
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

        Map<String, Object> updates = baseUpdates(
                firstName, lastName, phone, carNumber,
                insuranceDateMillis, testDateMillis, treatmentDateMillis,
                manufacturer, carSpecificModel, year
        );

        FirebaseFirestore.getInstance()
                .collection("drivers")
                .document(uid)
                .update(updates)
                .addOnSuccessListener(v -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }

//    // =========================
//    // NEW: update + image
//    // =========================
//    public void updateProfileFieldsWithImage(
//            String uid,
//            String firstName,
//            String lastName,
//            String phone,
//            String carNumber,
//            long insuranceDateMillis,
//            long testDateMillis,
//            long treatmentDateMillis,
//            String manufacturer,
//            String carSpecificModel,
//            int year,
//            String imageUriOrUrl,
//            SimpleCallback cb
//    ) {
//        if (isBlank(uid)) {
//            cb.onError(new IllegalArgumentException("uid is null/empty"));
//            return;
//        }
//
//        // אין תמונה חדשה -> רק עדכון שדות (לא נוגעים ב-carImageUri)
//        if (isBlank(imageUriOrUrl)) {
//            updateProfileFields(
//                    uid, firstName, lastName, phone, carNumber,
//                    insuranceDateMillis, testDateMillis, treatmentDateMillis,
//                    manufacturer, carSpecificModel, year,
//                    cb
//            );
//            return;
//        }
//
//        // כבר URL -> נשמור כמו שהוא
//        if (isHttpUrl(imageUriOrUrl)) {
//            Map<String, Object> updates = baseUpdates(
//                    firstName, lastName, phone, carNumber,
//                    insuranceDateMillis, testDateMillis, treatmentDateMillis,
//                    manufacturer, carSpecificModel, year
//            );
//            updates.put("car.carImageUri", imageUriOrUrl);
//
//            FirebaseFirestore.getInstance()
//                    .collection("drivers")
//                    .document(uid)
//                    .update(updates)
//                    .addOnSuccessListener(v -> cb.onSuccess())
//                    .addOnFailureListener(cb::onError);
//            return;
//        }
//
//        // אחרת: local Uri (content:// / file://) -> מעלים ל-Storage כמו SignUp
//        Uri localUri = Uri.parse(imageUriOrUrl);
//
//        StorageReference ref = FirebaseStorage.getInstance()
//                .getReference()
//                .child("car_photos")
//                .child(uid)
//                .child("car.jpg"); // בדיוק כמו SignUp
//
//        ref.putFile(localUri)
//                .addOnSuccessListener(taskSnapshot ->
//                        ref.getDownloadUrl()
//                                .addOnSuccessListener(downloadUri -> {
//                                    String downloadUrl = downloadUri.toString();
//
//                                    Map<String, Object> updates = baseUpdates(
//                                            firstName, lastName, phone, carNumber,
//                                            insuranceDateMillis, testDateMillis, treatmentDateMillis,
//                                            manufacturer, carSpecificModel, year
//                                    );
//                                    updates.put("car.carImageUri", downloadUrl);
//
//                                    FirebaseFirestore.getInstance()
//                                            .collection("drivers")
//                                            .document(uid)
//                                            .update(updates)
//                                            .addOnSuccessListener(v -> cb.onSuccess())
//                                            .addOnFailureListener(cb::onError);
//                                })
//                                .addOnFailureListener(cb::onError)
//                )
//                .addOnFailureListener(cb::onError);
//    }

    public void updateProfileFieldsWithBase64(
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
            String imageBase64OrNull,
            SimpleCallback cb
    ) {
        if (uid == null || uid.trim().isEmpty()) {
            cb.onError(new IllegalArgumentException("uid is null/empty"));
            return;
        }

        Map<String, Object> updates = new HashMap<>();

        updates.put("firstName", firstName);
        updates.put("lastName", lastName);
        updates.put("phone", phone);

        // ✅ אחידות: רק carNum
        updates.put("car.carNum", carNumber);
        updates.put("car.carNumber", com.google.firebase.firestore.FieldValue.delete());

        updates.put("car.insuranceDateMillis", insuranceDateMillis);
        updates.put("car.testDateMillis", testDateMillis);
        updates.put("car.treatmentDateMillis", treatmentDateMillis);

        updates.put("car.carModel", manufacturer);
        updates.put("car.carSpecificModel", carSpecificModel);
        updates.put("car.year", year);

        if (imageBase64OrNull != null && !imageBase64OrNull.trim().isEmpty()) {
            updates.put("car.carImageBase64", imageBase64OrNull);
        }

        FirebaseFirestore.getInstance()
                .collection("drivers")
                .document(uid)
                .update(updates)
                .addOnSuccessListener(v -> cb.onSuccess())
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
