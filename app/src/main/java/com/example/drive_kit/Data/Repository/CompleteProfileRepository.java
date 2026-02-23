package com.example.drive_kit.Data.Repository;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.drive_kit.Model.Driver;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

/**
 * Completes profile for users that ALREADY exist in FirebaseAuth
 * but are missing profile docs in Firestore.
 */
public class CompleteProfileRepository {

    public interface CompleteCallback {
        void onSuccess();
        void onError(Exception e);
    }

    private interface UrlSuccess {
        void onSuccess(String downloadUrl);
    }

    private interface ErrorCb {
        void onError(Exception e);
    }

    // =========================================================
    // DRIVER COMPLETE FLOW (existing auth user)
    // =========================================================
    public void completeDriverProfile(@NonNull String uid,
                                      Driver driver,
                                      CompleteCallback cb) {

        String safeUid = uid.trim();
        if (safeUid.isEmpty()) {
            cb.onError(new IllegalArgumentException("UID חסר"));
            return;
        }
        if (driver == null) {
            cb.onError(new IllegalArgumentException("נתוני נהג חסרים"));
            return;
        }

        String carPhotoUriStr = null;
        if (driver.getCar() != null) {
            carPhotoUriStr = driver.getCar().getCarImageUri();
        }

        // אם אין תמונה או שכבר URL מרוחק -> שמירה ישירה
        if (carPhotoUriStr == null || carPhotoUriStr.trim().isEmpty()
                || carPhotoUriStr.startsWith("http://")
                || carPhotoUriStr.startsWith("https://")) {
            saveDriverDoc(safeUid, driver, cb);
            return;
        }

        // URI מקומי -> להעלות קודם ל-Storage
        Uri localUri = Uri.parse(carPhotoUriStr);
        uploadCarPhotoToStorage(safeUid, localUri,
                downloadUrl -> {
                    if (driver.getCar() != null) {
                        driver.getCar().setCarImageUri(downloadUrl);
                    }
                    saveDriverDoc(safeUid, driver, cb);
                },
                cb::onError
        );
    }

    private void saveDriverDoc(String uid, Driver driver, CompleteCallback cb) {
        FirebaseFirestore.getInstance()
                .collection("drivers")
                .document(uid)
                .set(driver, SetOptions.merge())
                .addOnSuccessListener(v -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }

//    // =========================================================
//    // INSURANCE COMPLETE FLOW (existing auth user)
//    // =========================================================
//    public void completeInsuranceProfile(@NonNull String uid,
//                                         String email,
//                                         String firstName,
//                                         String lastName,
//                                         String phone,
//                                         @NonNull String companyId,
//                                         String insuranceLogoUriLocal,
//                                         CompleteCallback cb) {
//
//        String safeUid = uid.trim();
//        String safeCompanyId = companyId.trim().toLowerCase();
//
//        if (safeUid.isEmpty()) {
//            cb.onError(new IllegalArgumentException("UID חסר"));
//            return;
//        }
//
//        if (safeCompanyId.isEmpty()) {
//            cb.onError(new IllegalArgumentException("נא לבחור חברת ביטוח"));
//            return;
//        }
//
//        DocumentReference ref = FirebaseFirestore.getInstance()
//                .collection("insurance_companies")
//                .document(safeCompanyId);
//
//        ref.get()
//                .addOnSuccessListener(doc -> {
//                    if (!doc.exists()) {
//                        cb.onError(new IllegalStateException("Company not found: " + safeCompanyId));
//                        return;
//                    }
//
//                    Map<String, Object> updates = new HashMap<>();
//                    updates.put("isPartner", true);
//                    updates.put("partnerUid", safeUid);
//                    updates.put("updatedAt", Timestamp.now());
//
//                    if (email != null) updates.put("email", email.trim());
//                    if (phone != null) updates.put("phone", phone.trim());
//                    if (firstName != null) updates.put("contactFirstName", firstName.trim());
//                    if (lastName != null) updates.put("contactLastName", lastName.trim());
//
//                    ref.set(updates, SetOptions.merge())
//                            .addOnSuccessListener(v -> {
//                                // אופציונלי: העלאת לוגו
//                                maybeUploadInsuranceLogo(safeCompanyId, insuranceLogoUriLocal);
//                                cb.onSuccess();
//                            })
//                            .addOnFailureListener(cb::onError);
//                })
//                .addOnFailureListener(cb::onError);
//    }

//    private void maybeUploadInsuranceLogo(String companyId, String logoUriLocal) {
//        if (companyId == null || companyId.trim().isEmpty()) return;
//        if (logoUriLocal == null || logoUriLocal.trim().isEmpty()) return;
//
//        String s = logoUriLocal.trim();
//        if (s.startsWith("http://") || s.startsWith("https://")) {
//            FirebaseFirestore.getInstance()
//                    .collection("insurance_companies")
//                    .document(companyId)
//                    .update("logoUrl", s);
//            return;
//        }
//
//        Uri uri = Uri.parse(s);
//
//        StorageReference ref = FirebaseStorage.getInstance()
//                .getReference()
//                .child("insurance_logos")
//                .child(companyId)
//                .child("logo.jpg");
//
//        ref.putFile(uri)
//                .addOnSuccessListener(task ->
//                        ref.getDownloadUrl()
//                                .addOnSuccessListener(downloadUri ->
//                                        FirebaseFirestore.getInstance()
//                                                .collection("insurance_companies")
//                                                .document(companyId)
//                                                .update("logoUrl", downloadUri.toString()))
//                                .addOnFailureListener(e ->
//                                        Log.e("CompleteProfile", "getDownloadUrl failed", e)))
//                .addOnFailureListener(e ->
//                        Log.e("CompleteProfile", "logo upload failed", e));
//    }

    private void uploadCarPhotoToStorage(String uid, Uri uri, UrlSuccess ok, ErrorCb fail) {
        StorageReference ref = FirebaseStorage.getInstance()
                .getReference()
                .child("car_photos")
                .child(uid)
                .child("car.jpg");

        ref.putFile(uri)
                .addOnSuccessListener(taskSnapshot ->
                        ref.getDownloadUrl()
                                .addOnSuccessListener(downloadUri -> ok.onSuccess(downloadUri.toString()))
                                .addOnFailureListener(fail::onError))
                .addOnFailureListener(fail::onError);
    }


}
