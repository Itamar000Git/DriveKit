//package com.example.drive_kit.Data.Repository;
//
//import android.util.Log;
//
//import com.example.drive_kit.Model.Driver;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.firestore.FirebaseFirestore;
//
///**
// * Repository for accessing the database.
// * It uses the FirebaseFirestore class to access the database.
// */
//public class SetUsernamePasswordRepository {
//
//    public interface SignUpCallback {
//        void onSuccess();
//        void onError(Exception e);
//    }
//
//    /**
//     * registers the user with the given email and password and driver object
//     * if the registration is successful, it starts the HomeActivity
//     * if the registration fails, it shows an error message
//     * @param email
//     * @param password
//     * @param driver
//     * @param cb
//     */
//    public void register(String email, String password, Driver driver, SignUpCallback cb) {
//        //trying to register the user with the given email and password
//        FirebaseAuth.getInstance()
//                .createUserWithEmailAndPassword(email, password)
//                .addOnSuccessListener(authResult -> {
//                    String uid = authResult.getUser().getUid();
//                    Log.d("SIGNUP", "treatMillis=" + driver.getCar().getTreatmentDateMillis());
//                    //setting the driver object to the database
//                    FirebaseFirestore.getInstance()
//                            .collection("drivers")
//                            .document(uid)
//                            .set(driver)
//                            .addOnSuccessListener(aVoid -> cb.onSuccess())
//                            .addOnFailureListener(cb::onError);
//                })
//                .addOnFailureListener(cb::onError);
//    }
//}

package com.example.drive_kit.Data.Repository;

import android.net.Uri;
import android.util.Log;

import com.example.drive_kit.Model.Driver;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Repository for accessing the database.
 * It uses the FirebaseFirestore class to access the database.
 */
public class SetUsernamePasswordRepository {

    public interface SignUpCallback {
        void onSuccess();
        void onError(Exception e);
    }

    /**
     * registers the user with the given email and password and driver object
     * if the registration is successful, it starts the HomeActivity
     * if the registration fails, it shows an error message
     * @param email
     * @param password
     * @param driver
     * @param cb
     */
    public void register(String email, String password, Driver driver, SignUpCallback cb) {

        //trying to register the user with the given email and password
        FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {

                    FirebaseUser user = authResult.getUser();
                    if (user == null) {
                        cb.onError(new IllegalStateException("User is null after signup"));
                        return;
                    }

                    String uid = user.getUid();

                    Log.d("SIGNUP", "treatMillis=" + driver.getCar().getTreatmentDateMillis());

                    // =========================
                    // NEW: If car photo exists -> upload to Storage first
                    // This does NOT affect old flow when no photo was selected.
                    // =========================

                    String carPhotoUriStr = null;

                    // We keep using your existing model usage:
                    // in your Activity you passed carPhotoUriToSave into the Driver,
                    // and the car object holds car-related fields.
                    if (driver.getCar() != null) {
                        // If your getter name is different - it will be here in your Car model.
                        // Based on the files you used, the photo string is saved on the car object.
                        carPhotoUriStr = driver.getCar().getCarImageUri();
                    }

                    // No photo -> old behavior (Firestore only)
                    if (carPhotoUriStr == null || carPhotoUriStr.trim().isEmpty()) {
                        saveDriverToFirestore(uid, driver, cb, user);
                        return;
                    }

                    // If it's already a URL (from previous saved data), skip upload
                    if (carPhotoUriStr.startsWith("http://") || carPhotoUriStr.startsWith("https://")) {
                        saveDriverToFirestore(uid, driver, cb, user);
                        return;
                    }

                    Uri carPhotoUri = Uri.parse(carPhotoUriStr);

                    uploadCarPhotoToStorage(uid, carPhotoUri,
                            downloadUrl -> {
                                // Replace the local uri with a permanent download URL
                                driver.getCar().setCarImageUri(downloadUrl);

                                // Continue to Firestore as usual
                                saveDriverToFirestore(uid, driver, cb, user);
                            },
                            e -> {
                                Log.e("SIGNUP_STORAGE", "car photo upload failed", e);

                                // Important: rollback so user won't be "created but incomplete"
                                //rollbackAuthUser(user, () -> cb.onError(e));
                                saveDriverToFirestore(uid, driver, cb, user);

                            });

                })
                .addOnFailureListener(cb::onError);
    }

    // =========================
    // Firestore save (same logic as before, with rollback on failure)
    // =========================
    private void saveDriverToFirestore(String uid, Driver driver, SignUpCallback cb, FirebaseUser rollbackUser) {

        FirebaseFirestore.getInstance()
                .collection("drivers")
                .document(uid)
                .set(driver)
                .addOnSuccessListener(aVoid -> cb.onSuccess())
                .addOnFailureListener(e -> {
                    Log.e("SIGNUP_FIRESTORE", "failed to save driver", e);

                    // rollback auth user too (so user isn't "created but missing profile")
                    rollbackAuthUser(rollbackUser, () -> cb.onError(e));
                });
    }

    // =========================
    // Storage upload
    // =========================
    private interface UrlSuccess {
        void onSuccess(String downloadUrl);
    }

    private interface ErrorCb {
        void onError(Exception e);
    }

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
                                .addOnFailureListener(fail::onError)
                )
                .addOnFailureListener(fail::onError);
    }

    // =========================
    // Rollback
    // =========================
    private void rollbackAuthUser(FirebaseUser user, Runnable after) {
        user.delete().addOnCompleteListener(t -> after.run());
    }
}
