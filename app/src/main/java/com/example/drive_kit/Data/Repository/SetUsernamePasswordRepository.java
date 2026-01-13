package com.example.drive_kit.Data.Repository;

import android.util.Log;

import com.example.drive_kit.Model.Driver;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

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
                    String uid = authResult.getUser().getUid();
                    Log.d("SIGNUP", "treatMillis=" + driver.getCar().getTreatmentDateMillis());
                    //setting the driver object to the database
                    FirebaseFirestore.getInstance()
                            .collection("drivers")
                            .document(uid)
                            .set(driver)
                            .addOnSuccessListener(aVoid -> cb.onSuccess())
                            .addOnFailureListener(cb::onError);
                })
                .addOnFailureListener(cb::onError);
    }
}
