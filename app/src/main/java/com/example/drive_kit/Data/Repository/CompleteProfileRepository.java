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
 * CompleteProfileRepository
 *
 * Responsible for completing a Firestore profile document
 * for users that already exist in FirebaseAuth but are missing
 * their corresponding Firestore profile document.
 *
 * This repository handles:
 * - Validation of required parameters
 * - Optional upload of car photo to Firebase Storage
 * - Saving driver profile document into Firestore
 *
 * Note:
 * This class does NOT handle authentication.
 * It assumes the user is already authenticated.
 */
public class CompleteProfileRepository {

    /**
     * Callback interface for completion result.
     * Used to notify caller (Activity/ViewModel) about success or failure.
     */
    public interface CompleteCallback {
        void onSuccess();
        void onError(Exception e);
    }

    /**
     * Internal callback used after successful upload
     * to return the generated download URL.
     */
    private interface UrlSuccess {
        void onSuccess(String downloadUrl);
    }

    /**
     * Internal error callback used for Storage failures.
     */
    private interface ErrorCb {
        void onError(Exception e);
    }

    // =========================================================
    // DRIVER COMPLETE FLOW (existing authenticated user)
    // =========================================================

    /**
     * Completes the driver profile in Firestore.
     *
     * Flow:
     * 1. Validate UID and driver object.
     * 2. Check if car photo exists.
     * 3. If local image → upload to Firebase Storage.
     * 4. Replace local URI with download URL.
     * 5. Save final Driver object into Firestore.
     *
     * @param uid FirebaseAuth UID of the user
     * @param driver Driver object containing profile data
     * @param cb Completion callback
     */
    public void completeDriverProfile(@NonNull String uid,
                                      Driver driver,
                                      CompleteCallback cb) {

        // Trim UID to avoid accidental whitespace errors
        String safeUid = uid.trim();

        // Validate UID
        if (safeUid.isEmpty()) {
            cb.onError(new IllegalArgumentException("UID חסר"));
            return;
        }

        // Validate driver object
        if (driver == null) {
            cb.onError(new IllegalArgumentException("נתוני נהג חסרים"));
            return;
        }

        // Extract car image URI if exists
        String carPhotoUriStr = null;
        if (driver.getCar() != null) {
            carPhotoUriStr = driver.getCar().getCarImageUri();
        }

        /*
         * If:
         * - No photo provided
         * - Photo is empty
         * - Photo is already a remote HTTP URL
         *
         * Then we skip upload and save directly.
         */
        if (carPhotoUriStr == null || carPhotoUriStr.trim().isEmpty()
                || carPhotoUriStr.startsWith("http://")
                || carPhotoUriStr.startsWith("https://")) {

            saveDriverDoc(safeUid, driver, cb);
            return;
        }

        /*
         * Otherwise:
         * The URI is local (content:// or file://)
         * → Upload it to Firebase Storage first.
         */
        Uri localUri = Uri.parse(carPhotoUriStr);

        uploadCarPhotoToStorage(safeUid, localUri,
                downloadUrl -> {

                    // Replace local URI with download URL
                    if (driver.getCar() != null) {
                        driver.getCar().setCarImageUri(downloadUrl);
                    }

                    // Save updated driver document
                    saveDriverDoc(safeUid, driver, cb);
                },
                cb::onError
        );
    }

    /**
     * Saves the Driver document into Firestore.
     *
     * Uses SetOptions.merge() to:
     * - Create document if not exists
     * - Merge fields if document already exists
     *
     * @param uid Document ID (same as FirebaseAuth UID)
     * @param driver Driver object to save
     * @param cb Completion callback
     */
    private void saveDriverDoc(String uid, Driver driver, CompleteCallback cb) {

        FirebaseFirestore.getInstance()
                .collection("drivers")
                .document(uid)
                .set(driver, SetOptions.merge())
                .addOnSuccessListener(v -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }

    /**
     * Uploads a local car image file to Firebase Storage.
     *
     * Storage path:
     * car_photos/{uid}/car.jpg
     *
     * After successful upload:
     * - Retrieves the public download URL
     * - Returns it via UrlSuccess callback
     *
     * @param uid User UID
     * @param uri Local file URI
     * @param ok Success callback with download URL
     * @param fail Error callback
     */
    private void uploadCarPhotoToStorage(String uid,
                                         Uri uri,
                                         UrlSuccess ok,
                                         ErrorCb fail) {

        StorageReference ref = FirebaseStorage.getInstance()
                .getReference()
                .child("car_photos")
                .child(uid)
                .child("car.jpg");

        // Upload file to Firebase Storage
        ref.putFile(uri)
                .addOnSuccessListener(taskSnapshot ->
                        ref.getDownloadUrl()
                                .addOnSuccessListener(downloadUri ->
                                        ok.onSuccess(downloadUri.toString()))
                                .addOnFailureListener(fail::onError))
                .addOnFailureListener(fail::onError);
    }

}
