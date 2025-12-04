package com.example.drive_kit;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import Model.Driver;

public class HomeActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private TextView welcomeText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);
        //find the welcomeText and insert it into the variable
        welcomeText = findViewById(R.id.welcomeText);

        //we need the auth for the uid
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            welcomeText.setText("שלום אורח");
            return;
        }
        //gets the unique id of the user
        String uid = user.getUid();

        db.collection("drivers")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        //converts the document to a driver object
                        Driver driver = documentSnapshot.toObject(Driver.class);

                        if (driver != null) {
                            String firstName = driver.getFirstName();

                            welcomeText.setText("שלום, " + firstName + "!");
                        }
                    } else {
                        welcomeText.setText("שלום!");
                    }
                })
                .addOnFailureListener(e -> {
                    welcomeText.setText("שגיאה בטעינת הנתונים");
                });
    }


}
