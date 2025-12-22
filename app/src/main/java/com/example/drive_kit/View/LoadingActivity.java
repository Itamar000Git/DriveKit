package com.example.drive_kit.View;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.drive_kit.R;
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
                    Intent home = new Intent(LoadingActivity.this, HomeActivity.class);
                    startActivity(home);
                    finish();

                })
                .addOnFailureListener(e -> {
                    //failed login
                    Log.e("Auth", "Login failed: " + e.getMessage());

                    Toast.makeText(this, "פרטי ההתחברות שגויים", Toast.LENGTH_SHORT).show();

                    Intent backToLogin = new Intent(LoadingActivity.this,MainActivity.class);
                    startActivity(backToLogin);
                    finish();
                });

    }
}
