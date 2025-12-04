package com.example.drive_kit;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoadingActivity extends AppCompatActivity {
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading);
        Intent intent = getIntent();
        String email = intent.getStringExtra("email");
        String password = intent.getStringExtra("password");
        //  FirebaseAuth
        auth = FirebaseAuth.getInstance();

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    //successful login
                    Log.d("Auth", "Login successful");
                    Toast.makeText(this, "התחברת בהצלחה", Toast.LENGTH_SHORT).show();
//                    Intent goToHome = new Intent(LoadingActivity.this, HomeActivity.class);
////                    startActivity(goToHome);
////                    finish();
                })
                .addOnFailureListener(e -> {
                    //failed login
                    Log.e("Auth", "Login failed: " + e.getMessage());

                    Toast.makeText(this, "פרטי ההתחברות שגויים", Toast.LENGTH_SHORT).show();

                    //Intent backToLogin = new Intent(LoadingActivity.this, LoginActivity.class);
                    //startActivity(backToLogin);
                    //finish();
                });

    }
}
