package com.example.drive_kit.Data.Repository;

import com.google.firebase.auth.FirebaseAuth;

public class LoadingRepository {

    public interface LoadingCallback {
        void onSuccess();
        void onError(Exception e);
    }

    public void signIn(String email, String password, LoadingCallback cb) {
        FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }
}
