package com.example.drive_kit.Data.Repository;

import com.example.drive_kit.Model.Driver;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeRepository {

    public interface DriverCallback {
        void onSuccess(Driver driver);
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
}
