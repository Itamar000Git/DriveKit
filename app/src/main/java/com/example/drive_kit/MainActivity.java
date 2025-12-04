package com.example.drive_kit;


import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
//import com.example.drive_kit.SignUp;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;

public class MainActivity extends AppCompatActivity {


    private EditText emailEditText;
    private EditText passwordEditText;
    private Button signinButton;
    private Button signupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

         emailEditText = findViewById(R.id.emailEditText);
         passwordEditText = findViewById(R.id.passwordEditText);
         signinButton = findViewById(R.id.loginButton);
         signupButton = findViewById(R.id.signupButton);

        //listening to the signin button
        signinButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoadingActivity.class);
            intent.putExtra("email", emailEditText.getText().toString().trim());
            intent.putExtra("password", passwordEditText.getText().toString().trim());
            startActivity(intent);
        });
        //listening to the signup button
        signupButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignUp.class);
            startActivity(intent);
        });
    }
}