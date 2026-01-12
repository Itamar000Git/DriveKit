package com.example.drive_kit.Data.Repository;



import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.drive_kit.Model.Car;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class CarRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public LiveData<List<Car>> getCars(String uid) {
        MutableLiveData<List<Car>> live = new MutableLiveData<>(new ArrayList<>());

        db.collection("users")
                .document(uid)
                .collection("cars")
                .addSnapshotListener((snap, err) -> {
                    if (err != null || snap == null) return;

                    List<Car> out = new ArrayList<>();
                    snap.getDocuments().forEach(doc -> {
                        Car c = doc.toObject(Car.class);
                        if (c != null) {
                            c.setId(doc.getId());
                            out.add(c);
                        }
                    });
                    live.setValue(out);
                });

        return live;
    }
}
