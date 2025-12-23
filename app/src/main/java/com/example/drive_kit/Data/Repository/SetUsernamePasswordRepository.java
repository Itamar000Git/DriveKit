package com.example.drive_kit.Data.Repository;

import com.example.drive_kit.Model.Driver;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SetUsernamePasswordRepository {

    public interface SignUpCallback {
        void onSuccess();
        void onError(Exception e);
    }

    public void register(String email, String password, Driver driver, SignUpCallback cb) {
        FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();

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
