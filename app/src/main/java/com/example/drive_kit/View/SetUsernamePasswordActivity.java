package com.example.drive_kit.View;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.drive_kit.R;
import com.example.drive_kit.Model.Driver;
import com.example.drive_kit.ViewModel.SetUsernamePasswordViewModel;

/**
 * SetUsernamePasswordActivity is the final step of the signup process.
 *
 * In this screen, the user:
 * - Chooses a password
 * - Confirms the password
 *
 * This Activity:
 * - Receives all previously collected signup data using Intent
 * - Creates a Driver object
 * - Delegates registration logic to the ViewModel
 * - Observes LiveData to react to success or failure
 */
public class SetUsernamePasswordActivity extends AppCompatActivity {


    // Input field for the password
    private EditText passwordEditText;

    // Input field for confirming the password
    private EditText confirmPasswordEditText;



    // Button used to complete the signup process
    private Button signupButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Standard Activity initialization
        super.onCreate(savedInstanceState);

        // Attach the XML layout file to this Activity
        // After this line, all views defined in set_username_password.xml exist in memory
        setContentView(R.layout.set_username_password);

        // Retrieve the Intent that started this Activity
        // This Intent was sent from SignUpActivity
        Intent intent = getIntent();

        // Extract user personal and car data from the Intent
        String firstName = intent.getStringExtra("firstName");
        String lastName = intent.getStringExtra("lastName");
        String email = intent.getStringExtra("email");
        String phone = intent.getStringExtra("phone");
        String carNumber = intent.getStringExtra("carNumber");

        // Extract important dates (stored as milliseconds)
        long insuranceDateMillis = intent.getLongExtra("insuranceDateMillis", -1);
        long testDateMillis = intent.getLongExtra("testDateMillis", -1);
        long treatmentDateMillis = intent.getLongExtra("treatmentDateMillis", -1);

        // Create a Driver object using all collected registration data
        // This object represents the user and will be saved in the database
        if (insuranceDateMillis == -1 || testDateMillis == -1|| treatmentDateMillis==-1) {
            Log.e("SetUsernamePassword", "Dates not received");
            Toast.makeText(this, "שגיאה בטעינת נתונים", Toast.LENGTH_SHORT).show();
        }

        // Create a Driver object using all collected registration data
        // This object represents the user and will be saved in the database
        Driver driver = new Driver(
                firstName,
                lastName,
                email,
                phone,
                carNumber,
                insuranceDateMillis,
                testDateMillis,
                treatmentDateMillis
        );

        // Find and connect UI elements from the XML layout
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        signupButton = findViewById(R.id.registerButton);

        // Create (or retrieve) the ViewModel associated with this Activity
        // The ViewModel contains all signup-related logic
        SetUsernamePasswordViewModel viewModel = new ViewModelProvider(this).get(SetUsernamePasswordViewModel.class);

        // Observe signup success LiveData
        // If signup succeeds, show a success message and move to HomeActivity
        viewModel.getSignUpSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(this, "הרשמה הושלמה בהצלחה", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, HomeActivity.class));
                finish();
            }
        });


        // Observe signup error LiveData
        // If an error occurs, show the error message to the user
        viewModel.getSignUpError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });



        // Handle click on the signup button
        signupButton.setOnClickListener(v -> {

            // Extract and trim the password inputs
            String password = passwordEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();

            // Delegate the signup process to the ViewModel
            // The ViewModel will validate the passwords and perform registration
            viewModel.signUp(email, password, confirmPassword, driver);
        });
    }
}
