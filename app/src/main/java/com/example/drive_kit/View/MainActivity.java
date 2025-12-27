//package com.example.drive_kit.View;
//
//
//import android.os.Bundle;
//import android.widget.Button;
//import android.widget.EditText;
////import com.example.drive_kit.SignUp;
//import androidx.appcompat.app.AppCompatActivity;
//
//import android.content.Intent;
//
//import com.example.drive_kit.R;
//
//public class MainActivity extends AppCompatActivity {
//
//
//    private EditText emailEditText;
//    private EditText passwordEditText;
//    private Button signinButton;
//    private Button signupButton;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//         emailEditText = findViewById(R.id.emailEditText);
//         passwordEditText = findViewById(R.id.passwordEditText);
//         signinButton = findViewById(R.id.loginButton);
//         signupButton = findViewById(R.id.signupButton);
//
//        //listening to the signin button
//        signinButton.setOnClickListener(v -> {
//            Intent intent = new Intent(MainActivity.this, LoadingActivity.class);
//            intent.putExtra("email", emailEditText.getText().toString().trim());
//            intent.putExtra("password", passwordEditText.getText().toString().trim());
//            startActivity(intent);
//        });
//        //listening to the signup button
//        signupButton.setOnClickListener(v -> {
//            Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
//            startActivity(intent);
//        });
//    }
//}

package com.example.drive_kit.View;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
/// ////////////////////////////////////////////////////////////////
import com.example.drive_kit.R;
import com.example.drive_kit.ViewModel.MainViewModel;

/**
 * Activity for logging in.
 * It observes the LiveData in the ViewModel and updates the UI accordingly.
 * If the login is successful, it starts the HomeActivity.
 * If the login fails, it starts the MainActivity.
 */
public class MainActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button signinButton;
    private Button signupButton;

    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signinButton = findViewById(R.id.loginButton);
        signupButton = findViewById(R.id.signupButton);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);


        viewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        });


        signinButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();


            if (!viewModel.validateLoginInputs(email, password)) {
                return;
            }


            Intent intent = new Intent(MainActivity.this, LoadingActivity.class);
            intent.putExtra("email", email);
            intent.putExtra("password", password);
            startActivity(intent);
        });

        signupButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

    }
}
