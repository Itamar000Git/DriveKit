package com.example.drive_kit.View;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.drive_kit.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import com.example.drive_kit.Model.Driver;

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
        ImageView notyIcon = findViewById(R.id.noty_icon);

        notyIcon.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, NotificationsActivity.class);
            startActivity(intent);
        });

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
