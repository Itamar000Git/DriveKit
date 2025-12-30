package com.example.drive_kit.View;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.drive_kit.R;
import com.example.drive_kit.ViewModel.LoadingViewModel;

/**
 * LoadingActivity is a temporary screen shown during the login process.
 *
 * This Activity is responsible ONLY for:
 * 1) Receiving the email and password from MainActivity.
 * 2) Starting the login process via the ViewModel.
 * 3) Observing login results (success / error).
 * 4) Navigating to the next screen based on the result.
 *
 * IMPORTANT:
 * - This Activity does NOT perform login logic itself.
 * - All authentication logic lives inside LoadingViewModel.
 */
public class LoadingActivity extends AppCompatActivity {

    // Always call the parent implementation first.
    // This is required for proper Activity initialization.
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Always call the parent implementation first.
        // This is required for proper Activity initialization.
        super.onCreate(savedInstanceState);

        // Attach the XML layout file to this Activity.
        // After this line, all views inside loading.xml exist in memory.
        setContentView(R.layout.loading);

        // Retrieve the Intent that started this Activity.
        // This Intent was sent from MainActivity.
        Intent intent = getIntent();

        // Extract the email passed from the previous screen.
        // If the key does not exist, this will return null.
        String email = intent.getStringExtra("email");

        // Extract the password passed from the previous screen.
        String password = intent.getStringExtra("password");

        // Create (or retrieve) the ViewModel associated with this Activity.
        // The ViewModel contains all login-related logic and LiveData.
        LoadingViewModel viewModel = new ViewModelProvider(this).get(LoadingViewModel.class);



        // Observe the login success LiveData.
        // This observer is triggered when the ViewModel updates the login result to "true".
        viewModel.getLoginSuccess().observe(this, success -> {

            // Start background notifications after a successful login.
            // Application context is used because WorkManager must not use Activity context.
            if (Boolean.TRUE.equals(success)) {

                // Start background notifications after a successful login.
                // Application context is used because WorkManager must not use Activity context.
                viewModel.startNotifications(getApplicationContext());////

                // Show a success message to the user.
                Toast.makeText(this, "התחברת בהצלחה", Toast.LENGTH_SHORT).show();

                // Navigate to the HomeActivity (main screen of the app).
                startActivity(new Intent(this, HomeActivity.class));
                // Close LoadingActivity so the user cannot return to it using the Back button.
                finish();
            }
        });

        // Observe login error messages from the ViewModel.
        // This observer is triggered when login fails.
        viewModel.getLoginError().observe(this, msg -> {

            // If an error message exists, show it to the user.
            if (msg != null) {

                // Display the error message using a Toast.
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

                // Navigate back to the login screen (MainActivity).
                startActivity(new Intent(this, MainActivity.class));

                // Close LoadingActivity so it is removed from the back stack.
                finish();
            }
        });
        // Start the login process.
        // This triggers authentication logic inside the ViewModel.
        // The result (success or error) will be delivered via LiveData observers above.
        viewModel.login(email, password);
    }
}
