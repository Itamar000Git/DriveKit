package com.example.drive_kit.View;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.drive_kit.utils.PasswordValidator;


import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.drive_kit.Model.CarModel;
import com.example.drive_kit.Model.Driver;
import com.example.drive_kit.R;
import com.example.drive_kit.View.Insurance_user.InsuranceHomeActivity;
import com.example.drive_kit.ViewModel.SetUsernamePasswordViewModel;

public class SetUsernamePasswordActivity extends AppCompatActivity {

    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private Button signupButton;

    private String insuranceCompanyId;
    private String currentRole = "driver";
    private boolean isInsurance = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_username_password);

        Intent intent = getIntent();

        String role = safe(intent.getStringExtra("role"));
        if (role.isEmpty()) role = "driver";
        currentRole = role.toLowerCase().trim();

        isInsurance = intent.getBooleanExtra("isInsurance", "insurance".equals(currentRole));

        String firstName = safe(intent.getStringExtra("firstName"));
        String lastName  = safe(intent.getStringExtra("lastName"));
        String email     = safe(intent.getStringExtra("email"));
        String phone     = safe(intent.getStringExtra("phone"));

        insuranceCompanyId = safe(intent.getStringExtra("insuranceCompanyId"));

        String insuranceLogoUriStr = safe(intent.getStringExtra("insuranceLogoUri"));
        String insuranceLogoUriLocal = insuranceLogoUriStr.isEmpty() ? null : insuranceLogoUriStr;

        String carNumber = safe(intent.getStringExtra("carNumber"));

        CarModel carModel = (CarModel) intent.getSerializableExtra("carModel");
        if (carModel == null) carModel = CarModel.UNKNOWN;

        int year = intent.getIntExtra("year", 0);
        String carSpecificModel = safe(intent.getStringExtra("carSpecificModel"));

        long insuranceDateMillis = intent.getLongExtra("insuranceDateMillis", -1);
        long testDateMillis      = intent.getLongExtra("testDateMillis", -1);
        long treatmentDateMillis = intent.getLongExtra("treatmentDateMillis", -1);

        String carPhotoUriStr = safe(intent.getStringExtra("carPhotoUri"));
        String carPhotoUriLocal = carPhotoUriStr.isEmpty() ? null : carPhotoUriStr;

        Log.d("ROLE_DEBUG", "role extra = " + intent.getStringExtra("role"));
        Log.d("ROLE_DEBUG", "currentRole = " + currentRole);
        Log.d("ROLE_DEBUG", "isInsurance = " + isInsurance);
        Log.d("ROLE_DEBUG", "insuranceCompanyId = " + insuranceCompanyId);

        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        signupButton = findViewById(R.id.registerButton);

        SetUsernamePasswordViewModel viewModel =
                new ViewModelProvider(this).get(SetUsernamePasswordViewModel.class);

        // חשוב: ניקוי שגיאות/הצלחות ישנות
        viewModel.resetState();

        viewModel.getSignUpSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(this, "הרשמה הושלמה בהצלחה", Toast.LENGTH_SHORT).show();

                Intent nextIntent;
                if (isInsurance) {
                    nextIntent = new Intent(this, InsuranceHomeActivity.class);
                    nextIntent.putExtra("insuranceCompanyId", insuranceCompanyId);
                } else {
                    nextIntent = new Intent(this, HomeActivity.class);
                }

                nextIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(nextIntent);
                finish();
            }
        });

        viewModel.getSignUpError().observe(this, error -> {
            if (error == null || error.trim().isEmpty()) return;

            String err = error.trim();
            Log.e("SIGNUP_ERROR", err);

            // הודעה ברורה יותר לשגיאת Rules
            String lower = err.toLowerCase();
            if (lower.contains("permission_denied") || lower.contains("insufficient permissions")) {
                Toast.makeText(this,
                        "אין הרשאה לעדכן את חברת הביטוח. בדקי Firestore Rules ושיוך partnerUid.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            Toast.makeText(this, err, Toast.LENGTH_LONG).show();

            if ("EMAIL_ALREADY_EXISTS".equals(err) || err.contains("כבר רשומה")) {
                Intent loginIntent = new Intent(this, MainActivity.class);
                loginIntent.putExtra("prefill_email", email);
                loginIntent.putExtra("insuranceCompanyId", insuranceCompanyId);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(loginIntent);
                finish();
            }
        });

        final String finalRole = currentRole;
        final CarModel finalCarModel = carModel;

        signupButton.setOnClickListener(v -> {
            String password = safe(passwordEditText.getText() != null ? passwordEditText.getText().toString() : "");
            String confirmPassword = safe(confirmPasswordEditText.getText() != null ? confirmPasswordEditText.getText().toString() : "");

            // ===== Password strength =====
            PasswordValidator.Result r = PasswordValidator.validate(password);
            if (!r.ok) {
                passwordEditText.setError(r.error);
                passwordEditText.requestFocus();
                return;
            }

            // ===== Confirm password =====
            if (!password.equals(confirmPassword)) {
                confirmPasswordEditText.setError("הסיסמאות לא תואמות");
                confirmPasswordEditText.requestFocus();
                return;
            }
            // ===== Insurance flow =====
            if (isInsurance || "insurance".equals(finalRole)) {
                if (insuranceCompanyId.isEmpty()) {
                    Toast.makeText(this, "נא לבחור חברת ביטוח", Toast.LENGTH_SHORT).show();
                    return;
                }

                viewModel.signUpInsurance(
                        email,
                        password,
                        confirmPassword,
                        firstName,
                        "", // לפי הלוגיקה הנוכחית שלך
                        phone,
                        insuranceCompanyId,
                        insuranceLogoUriLocal
                );
                return;
            }

            // ===== Driver flow =====
            if (insuranceDateMillis == -1 || testDateMillis == -1 || treatmentDateMillis == -1) {
                Log.e("SetUsernamePassword", "Driver dates not received");
                Toast.makeText(this, "שגיאה בטעינת נתוני נהג", Toast.LENGTH_SHORT).show();
                return;
            }

            Driver driver = new Driver(
                    firstName,
                    lastName,
                    email,
                    phone,
                    carNumber,
                    finalCarModel,
                    year,
                    insuranceDateMillis,
                    testDateMillis,
                    treatmentDateMillis,
                    carPhotoUriLocal
            );

            if (!TextUtils.isEmpty(carSpecificModel) && driver.getCar() != null) {
                driver.getCar().setCarSpecificModel(carSpecificModel.trim());
            }

            viewModel.signUp(email, password, confirmPassword, "driver", driver);
        });
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
