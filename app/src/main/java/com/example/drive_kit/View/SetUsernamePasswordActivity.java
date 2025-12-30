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
 * Activity for setting the username and password.
 * It observes the LiveData in the ViewModel and updates the UI accordingly.
 * If the registration is successful, it starts the HomeActivity.
 * If the registration fails, it shows an error message.
 */
public class SetUsernamePasswordActivity extends AppCompatActivity {

    private EditText passwordEditText; // edit text for the password
    private EditText confirmPasswordEditText; // edit text for the confirm password
    private Button signupButton; // button for the signup

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_username_password);

        // Get the data from the intent
        Intent intent = getIntent();
        String firstName = intent.getStringExtra("firstName");
        String lastName = intent.getStringExtra("lastName");
        String email = intent.getStringExtra("email");
        String phone = intent.getStringExtra("phone");
        String carNumber = intent.getStringExtra("carNumber");
        long insuranceDateMillis = intent.getLongExtra("insuranceDateMillis", -1);
        long testDateMillis = intent.getLongExtra("testDateMillis", -1);
        long treatmentDateMillis = intent.getLongExtra("treatmentDateMillis", -1);

        // Validate the received data
        if (insuranceDateMillis == -1 || testDateMillis == -1|| treatmentDateMillis==-1) {
            Log.e("SetUsernamePassword", "Dates not received");
            Toast.makeText(this, "שגיאה בטעינת נתונים", Toast.LENGTH_SHORT).show();
        }

        // Create the driver object
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

        // Initialize the edit text and button
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        signupButton = findViewById(R.id.registerButton);

        // Initialize the ViewModel
        SetUsernamePasswordViewModel viewModel = new ViewModelProvider(this).get(SetUsernamePasswordViewModel.class);

        // Observe the LiveData and update the UI when the registration is successful
        viewModel.getSignUpSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(this, "הרשמה הושלמה בהצלחה", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, HomeActivity.class));
                finish();
            }
        });

        // Observe the LiveData and update the UI when the registration fails and Show an error message
        viewModel.getSignUpError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        // Set the click listener for the signup button
        signupButton.setOnClickListener(v -> {
            String password = passwordEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();

            viewModel.signUp(email, password, confirmPassword, driver);
        });
    }
}
