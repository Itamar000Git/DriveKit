package com.example.drive_kit.View;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.drive_kit.R;
import com.example.drive_kit.ViewModel.LoadingViewModel;

/**
 * Activity for logging in.
 * It observes the LiveData in the ViewModel and updates the UI accordingly.
 * If the login is successful, it starts the HomeActivity.
 * If the login fails, it starts the MainActivity.
 */
public class LoadingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading);

        // Get the email and password from the intent
        Intent intent = getIntent();
        String email = intent.getStringExtra("email");
        String password = intent.getStringExtra("password");

        // Initialize the ViewModel
        LoadingViewModel viewModel = new ViewModelProvider(this).get(LoadingViewModel.class);



        // Observe the LiveData and update the UI when the login is successful
        viewModel.getLoginSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                viewModel.startNotifications(getApplicationContext());////
                Toast.makeText(this, "התחברת בהצלחה", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, HomeActivity.class));
                finish();
            }
        });

        // Observe the LiveData and update the UI when the login fails
        viewModel.getLoginError().observe(this, msg -> {
            if (msg != null) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        });

        viewModel.login(email, password); // Start the login process
    }
}
