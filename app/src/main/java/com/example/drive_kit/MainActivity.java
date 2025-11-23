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


    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button signinButton;
    private Button signupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //usernameEditText = findViewById(R.id.usernameEditText);
        //passwordEditText = findViewById(R.id.passwordEditText);
        //signinButton = findViewById(R.id.loginButton);
        signupButton = findViewById(R.id.signupButton);

        signupButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignUp.class);
            startActivity(intent);
        });
    }
}