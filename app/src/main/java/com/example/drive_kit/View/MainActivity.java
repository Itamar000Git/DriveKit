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
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
/// ////////////////////////////////////////////////////////////////
import com.example.drive_kit.R;
import com.example.drive_kit.ViewModel.MainViewModel;
import com.example.drive_kit.ViewModel.VideosViewModel;

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

        // Always call the parent implementation first.
        // Android needs this to create the Activity properly.
        super.onCreate(savedInstanceState);


        // Attach the XML layout file to this Activity.
        // After this line, all views inside activity_main.xml exist in memory.
        setContentView(R.layout.activity_main);

        // Connect Java variables to the actual views from the XML using their IDs.
        // This allows us to read input and set click listeners.
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signinButton = findViewById(R.id.loginButton);
        signupButton = findViewById(R.id.signupButton);

        // Create (or reuse) the ViewModel associated with this Activity.
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Observe error messages from the ViewModel.
        // Whenever the ViewModel publishes a new error message (non-null),
        // this observer runs and shows a Toast on the screen.
        viewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        });

        // Login button click handler.
        // When the user clicks "Login", we read the email/password from the UI,
        // validate them using the ViewModel, and if valid - navigate to LoadingActivity.
        signinButton.setOnClickListener(v -> {

            // Read current user input from the EditTexts.
            // toString() converts the text to a Java String.
            // trim() removes spaces at the beginning/end.
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            // Validate user input using the ViewModel.
            // If invalid, validateLoginInputs will typically trigger an error message
            // (via LiveData) and this Activity will show it through the observer.
            // return; stops the click handler so we do NOT continue to the next screen.
            if (!viewModel.validateLoginInputs(email, password)) {
                return;
            }

            // Create an Intent to move from MainActivity to LoadingActivity.
            // LoadingActivity will perform the actual login operation (Firebase, etc.).
            Intent intent = new Intent(MainActivity.this, LoadingActivity.class);

            // Pass the email and password to the next screen using Intent extras.
            // The keys ("email", "password") must match what LoadingActivity expects.
            intent.putExtra("email", email);
            intent.putExtra("password", password);

            // Start the new Activity.
            startActivity(intent);
        });
        // Sign-up button click handler.
        // When clicked, we navigate to SignUpActivity where the user can register.
        signupButton.setOnClickListener(v -> {

            // Create an Intent to open the SignUpActivity screen.
            Intent intent = new Intent(MainActivity.this, SignUpActivity.class);

            // Start the sign-up screen.
            startActivity(intent);
        });



    }
}
