package com.example.drive_kit.View;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.drive_kit.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import com.example.drive_kit.Model.Driver;

public class setUsernamePasswordActivity extends AppCompatActivity {

    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private Button signupButton;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_username_password);

        // Firebase init
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Get the data from the previous activity
        Intent intent = getIntent();
        String firstName = intent.getStringExtra("firstName");
        String lastName = intent.getStringExtra("lastName");
        String email = intent.getStringExtra("email");
        String carNumber = intent.getStringExtra("carNumber");
        //long insuranceDateMillis = intent.getLongExtra("insuranceDate", 0);
        long insuranceDateMillis = intent.getLongExtra("insuranceDateMillis", -1);
        if (insuranceDateMillis == -1) {
            Log.e("SetUsernamePassword", "insuranceDateMillis not received from intent");
            Toast.makeText(this, "שגיאה בטעינת תאריך הביטוח", Toast.LENGTH_SHORT).show();
        }
        // Initialize views
        signupButton = findViewById(R.id.registerButton);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);

        signupButton.setOnClickListener(v -> {

            String password = passwordEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();

            // Check if fields are empty
            if (password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "הסיסמאות אינן תואמות", Toast.LENGTH_SHORT).show();
                return;
            }

            // create user with email and password
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {

                        String uid = authResult.getUser().getUid();
                        Log.d("Auth", "User created with uid: " + uid);

                        // creates driver object
                        Driver driver = new Driver(
                                firstName,
                                lastName,
                                email,
                                carNumber,
                                insuranceDateMillis
                        );

                        // save driver to firebase
                        db.collection("drivers")
                                .document(uid)
                                .set(driver)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Firebase", "Driver saved successfully");
                                    Toast.makeText(this, "הרשמה הושלמה בהצלחה", Toast.LENGTH_SHORT).show();


                                     Intent home = new Intent(this, HomeActivity.class);
                                     startActivity(home);
                                     finish();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firebase", "Error saving driver", e);
                                });

                    })
                    .addOnFailureListener(e -> {
                        Log.e("Auth", "Error creating user", e);
                        Toast.makeText(this, "שגיאה בהרשמה: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });
    }
}
