package com.example.drive_kit;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import Model.Driver;

public class NotificationsActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private ArrayList<String> noty;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notifications);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        noty = new ArrayList<>();

        FirebaseUser user = auth.getCurrentUser();
        String uid = user.getUid();


        db.collection("drivers")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        //converts the document to a driver object
                        Driver driver = documentSnapshot.toObject(Driver.class);

                        if (driver != null) {
//                            String firstName = driver.getFirstName();
//
//                            welcomeText.setText("שלום, " + firstName + "!");
                        }
                    } else {
                       // welcomeText.setText("שלום!");
                    }
                })
                .addOnFailureListener(e -> {
                  //  welcomeText.setText("שגיאה בטעינת הנתונים");
                });
    }
}
