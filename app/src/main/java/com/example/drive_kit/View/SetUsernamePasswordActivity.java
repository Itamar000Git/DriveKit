//package com.example.drive_kit.View;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.text.TextUtils;
//import android.util.Log;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.lifecycle.ViewModelProvider;
//
//import com.example.drive_kit.Model.CarModel;
//import com.example.drive_kit.Model.Driver;
//import com.example.drive_kit.R;
//import com.example.drive_kit.View.Insurance_user.InsuranceHomeActivity;
//import com.example.drive_kit.ViewModel.SetUsernamePasswordViewModel;
//
///**
// * Final signup step screen:
// * - Receives payload from SignUpActivity.
// * - Collects password + confirm password.
// * - Triggers either driver registration or insurance registration.
// * - Navigates to role-specific home screen on success:
// *      driver    -> HomeActivity
// *      insurance -> InsuranceHomeActivity
// */
//public class SetUsernamePasswordActivity extends AppCompatActivity {
//
//    // Password UI inputs
//    private EditText passwordEditText;
//    private EditText confirmPasswordEditText;
//    private Button signupButton;
//
//    // Insurance company id passed from previous screen (insurance flow only)
//    private String insuranceCompanyId;
//
//    // Role string for debug/readability (default driver)
//    private String currentRole = "driver";
//
//    // Robust role flag used for navigation decision
//    private boolean isInsurance = false;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.set_username_password);
//
//        Intent intent = getIntent();
//
//        // ---------- Common payload ----------
//        // role is sent as "driver" / "insurance"
//        String role = safe(intent.getStringExtra("role"));
//        if (role.isEmpty()) role = "driver";
//        currentRole = role.toLowerCase().trim();
//
//        // Prefer explicit boolean role flag when available.
//        // Fallback to role string for backward compatibility.
//        isInsurance = intent.getBooleanExtra("isInsurance", "insurance".equals(currentRole));
//
//        String firstName = safe(intent.getStringExtra("firstName"));
//        String lastName  = safe(intent.getStringExtra("lastName"));
//        String email     = safe(intent.getStringExtra("email"));
//        String phone     = safe(intent.getStringExtra("phone"));
//
//        // ---------- Insurance-only payload ----------
//        insuranceCompanyId = safe(intent.getStringExtra("insuranceCompanyId"));
//
//        // ---------- Driver-only payload ----------
//        String carNumber = safe(intent.getStringExtra("carNumber"));
//
//        CarModel carModel = (CarModel) intent.getSerializableExtra("carModel");
//        if (carModel == null) carModel = CarModel.UNKNOWN;
//
//        int year = intent.getIntExtra("year", 0);
//        String carSpecificModel = safe(intent.getStringExtra("carSpecificModel"));
//
//        long insuranceDateMillis = intent.getLongExtra("insuranceDateMillis", -1);
//        long testDateMillis      = intent.getLongExtra("testDateMillis", -1);
//        long treatmentDateMillis = intent.getLongExtra("treatmentDateMillis", -1);
//
//        String carPhotoUriStr = safe(intent.getStringExtra("carPhotoUri"));
//        String carPhotoUriLocal = carPhotoUriStr.isEmpty() ? null : carPhotoUriStr;
//
//        // ---------- Debug role state ----------
//        Log.d("ROLE_DEBUG", "role extra = " + intent.getStringExtra("role"));
//        Log.d("ROLE_DEBUG", "currentRole = " + currentRole);
//        Log.d("ROLE_DEBUG", "isInsurance = " + isInsurance);
//
//        // ---------- UI bind ----------
//        passwordEditText = findViewById(R.id.passwordEditText);
//        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
//        signupButton = findViewById(R.id.registerButton);
//
//        SetUsernamePasswordViewModel viewModel =
//                new ViewModelProvider(this).get(SetUsernamePasswordViewModel.class);
//
//        // Observe success state and navigate to role-specific destination.
//        viewModel.getSignUpSuccess().observe(this, success -> {
//            if (Boolean.TRUE.equals(success)) {
//                Toast.makeText(this, "הרשמה הושלמה בהצלחה", Toast.LENGTH_SHORT).show();
//
//                Log.d("ROLE_DEBUG", "onSuccess -> isInsurance=" + isInsurance + ", currentRole=" + currentRole);
//
//                Intent nextIntent;
//                if (isInsurance) {
//                    nextIntent = new Intent(this, InsuranceHomeActivity.class);
//                    // Optional: pass company id so insurance home can load company details/name
//                    nextIntent.putExtra("insuranceCompanyId", insuranceCompanyId);
//                } else {
//                    nextIntent = new Intent(this, HomeActivity.class);
//                }
//
//                // Clear back stack so user cannot return to signup by pressing Back.
//                nextIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                Log.d("ROLE_DEBUG", "Navigating to: " + (isInsurance ? "InsuranceHomeActivity" : "HomeActivity"));
//
//                startActivity(nextIntent);
//                finish();
//            }
//        });
//
//        // Observe error state for user feedback and duplicate-email redirect behavior.
//        viewModel.getSignUpError().observe(this, error -> {
//            if (error == null || error.trim().isEmpty()) return;
//
//            String err = error.trim();
//            Toast.makeText(this, err, Toast.LENGTH_LONG).show();
//
//            // If company email is already registered, redirect to login screen.
//            if ("EMAIL_ALREADY_EXISTS".equals(err) || err.contains("כבר רשומה")) {
//                Intent loginIntent = new Intent(this, MainActivity.class);
//                loginIntent.putExtra("prefill_email", email);
//                loginIntent.putExtra("insuranceCompanyId", insuranceCompanyId);
//                loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                startActivity(loginIntent);
//                finish();
//            }
//        });
//
//        // Final local constants for click flow
//        final String finalRole = currentRole;
//        final CarModel finalCarModel = carModel;
//
//        signupButton.setOnClickListener(v -> {
//            // Read password inputs safely
//            String password = safe(passwordEditText.getText() != null ? passwordEditText.getText().toString() : "");
//            String confirmPassword = safe(confirmPasswordEditText.getText() != null ? confirmPasswordEditText.getText().toString() : "");
//
//            Log.d("ROLE_DEBUG", "signup click -> finalRole=" + finalRole + ", isInsurance=" + isInsurance);
//
//            // ===== Insurance flow =====
//            if (isInsurance || "insurance".equals(finalRole)) {
//                viewModel.signUpInsurance(
//                        email,
//                        password,
//                        confirmPassword,
//                        firstName,
//                        "", // lastName intentionally empty in insurance flow (as per your current logic)
//                        phone,
//                        insuranceCompanyId
//                );
//                return; // Important: do not continue into driver flow
//            }
//
//            // ===== Driver flow =====
//            if (insuranceDateMillis == -1 || testDateMillis == -1 || treatmentDateMillis == -1) {
//                Log.e("SetUsernamePassword", "Driver dates not received");
//                Toast.makeText(this, "שגיאה בטעינת נתוני נהג", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            Driver driver = new Driver(
//                    firstName,
//                    lastName,
//                    email,
//                    phone,
//                    carNumber,
//                    finalCarModel,
//                    year,
//                    insuranceDateMillis,
//                    testDateMillis,
//                    treatmentDateMillis,
//                    carPhotoUriLocal
//            );
//
//            if (!TextUtils.isEmpty(carSpecificModel) && driver.getCar() != null) {
//                driver.getCar().setCarSpecificModel(carSpecificModel.trim());
//            }
//
//            Log.d("SetUsernamePassword", "driver=" + driver);
//
//            viewModel.signUp(email, password, confirmPassword, "driver", driver);
//        });
//    }
//
//    /**
//     * Null-safe trim helper:
//     * - null -> ""
//     * - otherwise -> trimmed string
//     */
//    private String safe(String s) {
//        return s == null ? "" : s.trim();
//    }
//}


package com.example.drive_kit.View;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.drive_kit.Model.CarModel;
import com.example.drive_kit.Model.Driver;
import com.example.drive_kit.R;
import com.example.drive_kit.View.Insurance_user.InsuranceHomeActivity;
import com.example.drive_kit.ViewModel.SetUsernamePasswordViewModel;

/**
 * Final signup step screen:
 * - Receives payload from SignUpActivity.
 * - Collects password + confirm password.
 * - Triggers either driver registration or insurance registration.
 * - Navigates to role-specific home screen on success:
 *      driver    -> HomeActivity
 *      insurance -> InsuranceHomeActivity
 */
public class SetUsernamePasswordActivity extends AppCompatActivity {

    // Password UI inputs
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private Button signupButton;

    // Insurance company id passed from previous screen (insurance flow only)
    private String insuranceCompanyId;

    // Role string for debug/readability (default driver)
    private String currentRole = "driver";

    // Robust role flag used for navigation decision
    private boolean isInsurance = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_username_password);

        Intent intent = getIntent();

        // ---------- Common payload ----------
        // role is sent as "driver" / "insurance"
        String role = safe(intent.getStringExtra("role"));
        if (role.isEmpty()) role = "driver";
        currentRole = role.toLowerCase().trim();

        // Prefer explicit boolean role flag when available.
        // Fallback to role string for backward compatibility.
        isInsurance = intent.getBooleanExtra("isInsurance", "insurance".equals(currentRole));

        String firstName = safe(intent.getStringExtra("firstName"));
        String lastName  = safe(intent.getStringExtra("lastName"));
        String email     = safe(intent.getStringExtra("email"));
        String phone     = safe(intent.getStringExtra("phone"));

        // ---------- Insurance-only payload ----------
        insuranceCompanyId = safe(intent.getStringExtra("insuranceCompanyId"));

        // ✅ NEW: Insurance logo local URI (optional)
        String insuranceLogoUriStr = safe(intent.getStringExtra("insuranceLogoUri"));
        String insuranceLogoUriLocal = insuranceLogoUriStr.isEmpty() ? null : insuranceLogoUriStr;

        // ---------- Driver-only payload ----------
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

        // ---------- Debug role state ----------
        Log.d("ROLE_DEBUG", "role extra = " + intent.getStringExtra("role"));
        Log.d("ROLE_DEBUG", "currentRole = " + currentRole);
        Log.d("ROLE_DEBUG", "isInsurance = " + isInsurance);

        // ---------- UI bind ----------
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        signupButton = findViewById(R.id.registerButton);

        SetUsernamePasswordViewModel viewModel =
                new ViewModelProvider(this).get(SetUsernamePasswordViewModel.class);

        // Observe success state and navigate to role-specific destination.
        viewModel.getSignUpSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(this, "הרשמה הושלמה בהצלחה", Toast.LENGTH_SHORT).show();

                Log.d("ROLE_DEBUG", "onSuccess -> isInsurance=" + isInsurance + ", currentRole=" + currentRole);

                Intent nextIntent;
                if (isInsurance) {
                    nextIntent = new Intent(this, InsuranceHomeActivity.class);
                    nextIntent.putExtra("insuranceCompanyId", insuranceCompanyId);
                } else {
                    nextIntent = new Intent(this, HomeActivity.class);
                }

                nextIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                Log.d("ROLE_DEBUG", "Navigating to: " + (isInsurance ? "InsuranceHomeActivity" : "HomeActivity"));

                startActivity(nextIntent);
                finish();
            }
        });

        // Observe error state for user feedback and duplicate-email redirect behavior.
        viewModel.getSignUpError().observe(this, error -> {
            if (error == null || error.trim().isEmpty()) return;

            String err = error.trim();
            Toast.makeText(this, err, Toast.LENGTH_LONG).show();

            // If company email is already registered, redirect to login screen.
            if ("EMAIL_ALREADY_EXISTS".equals(err) || err.contains("כבר רשומה")) {
                Intent loginIntent = new Intent(this, MainActivity.class);
                loginIntent.putExtra("prefill_email", email);
                loginIntent.putExtra("insuranceCompanyId", insuranceCompanyId);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(loginIntent);
                finish();
            }
        });

        // Final local constants for click flow
        final String finalRole = currentRole;
        final CarModel finalCarModel = carModel;

        signupButton.setOnClickListener(v -> {
            String password = safe(passwordEditText.getText() != null ? passwordEditText.getText().toString() : "");
            String confirmPassword = safe(confirmPasswordEditText.getText() != null ? confirmPasswordEditText.getText().toString() : "");

            Log.d("ROLE_DEBUG", "signup click -> finalRole=" + finalRole + ", isInsurance=" + isInsurance);

            // ===== Insurance flow =====
            if (isInsurance || "insurance".equals(finalRole)) {
                viewModel.signUpInsurance(
                        email,
                        password,
                        confirmPassword,
                        firstName,
                        "", // lastName intentionally empty in insurance flow
                        phone,
                        insuranceCompanyId,
                        insuranceLogoUriLocal // ✅ NEW
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

            Log.d("SetUsernamePassword", "driver=" + driver);

            viewModel.signUp(email, password, confirmPassword, "driver", driver);
        });
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
