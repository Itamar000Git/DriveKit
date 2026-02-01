package com.example.drive_kit.Data.Repository;

import com.google.firebase.auth.FirebaseAuth;

// NEW (Google)
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;

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

    /**
     * NEW:
     * logs in the user with Google using idToken (FirebaseAuth Google credential)
     *
     * Flow:
     * 1) Create Google credential from idToken
     * 2) FirebaseAuth.signInWithCredential(...)
     * 3) onSuccess -> cb.onSuccess()
     * 4) onError   -> cb.onError(e)
     *
     * @param idToken Google ID token from GoogleSignInAccount
     * @param cb callback to return success/error
     */
    public void signInWithGoogle(String idToken, LoadingCallback cb) {
        if (idToken == null || idToken.trim().isEmpty()) {
            cb.onError(new IllegalArgumentException("idToken is null/empty"));
            return;
        }

        // Create Firebase credential from Google idToken
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        // Sign in to Firebase using the Google credential
        FirebaseAuth.getInstance()
                .signInWithCredential(credential)
                .addOnSuccessListener(authResult -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }
}
