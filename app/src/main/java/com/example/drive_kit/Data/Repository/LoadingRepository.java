package com.example.drive_kit.Data.Repository;

import com.google.firebase.auth.FirebaseAuth;

/**
 * Repository for accessing the database.
 * It uses the FirebaseFirestore class to access the database.
 */
public class LoadingRepository {

    public interface LoadingCallback {
        void onSuccess();
        void onError(Exception e);
    }

    /**
     * logs in the user with the given email and password
     * if the login is successful, it starts the HomeActivity
     * if the login fails, it starts the MainActivity
     * @param email
     * @param password
     * @param cb
     */
    public void signIn(String email, String password, LoadingCallback cb) {
        //trying to log in with the given email and password
        FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }
}
