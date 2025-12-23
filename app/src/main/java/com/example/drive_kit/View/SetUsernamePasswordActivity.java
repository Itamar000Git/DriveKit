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

public class SetUsernamePasswordActivity extends AppCompatActivity {

    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private Button signupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_username_password);

        Intent intent = getIntent();
        String firstName = intent.getStringExtra("firstName");
        String lastName = intent.getStringExtra("lastName");
        String email = intent.getStringExtra("email");
        String phone = intent.getStringExtra("phone");
        String carNumber = intent.getStringExtra("carNumber");
        long insuranceDateMillis = intent.getLongExtra("insuranceDateMillis", -1);
        long testDateMillis = intent.getLongExtra("testDateMillis", -1);

        if (insuranceDateMillis == -1 || testDateMillis == -1) {
            Log.e("SetUsernamePassword", "Dates not received");
            Toast.makeText(this, "שגיאה בטעינת נתונים", Toast.LENGTH_SHORT).show();
        }

        Driver driver = new Driver(
                firstName,
                lastName,
                email,
                phone,
                carNumber,
                insuranceDateMillis,
                testDateMillis
        );

        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        signupButton = findViewById(R.id.registerButton);

        SetUsernamePasswordViewModel viewModel = new ViewModelProvider(this).get(SetUsernamePasswordViewModel.class);

        viewModel.getSignUpSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(this, "הרשמה הושלמה בהצלחה", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, HomeActivity.class));
                finish();
            }
        });

        viewModel.getSignUpError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        signupButton.setOnClickListener(v -> {
            String password = passwordEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();

            viewModel.signUp(email, password, confirmPassword, driver);
        });
    }
}
