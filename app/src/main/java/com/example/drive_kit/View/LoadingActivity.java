package com.example.drive_kit.View;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.drive_kit.R;
import com.example.drive_kit.View.Insurance_user.InsuranceHomeActivity;
import com.example.drive_kit.ViewModel.LoadingViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * LoadingActivity is a temporary screen shown during login.
 *
 * Responsibilities:
 * 1) Receive login payload from MainActivity (email/password or Google token).
 * 2) Trigger login via LoadingViewModel.
 * 3) Observe login result.
 * 4) Route user to the correct home screen based on role:
 *    - Insurance partner -> InsuranceHomeActivity
 *    - Regular user      -> HomeActivity
 */
public class LoadingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading);

        Intent intent = getIntent();
        String email = intent.getStringExtra("email");
        String password = intent.getStringExtra("password");
        String googleIdToken = intent.getStringExtra("googleIdToken");

        LoadingViewModel viewModel = new ViewModelProvider(this).get(LoadingViewModel.class);

        // Observe successful login.
        // Do NOT navigate directly to HomeActivity.
        // First route by role from Firestore.
        viewModel.getLoginSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                //viewModel.startNotifications(getApplicationContext());
                Toast.makeText(this, "התחברת בהצלחה", Toast.LENGTH_SHORT).show();
                routeAfterLoginByRole();
            }
        });

        // Observe login failure.
        viewModel.getLoginError().observe(this, msg -> {
            if (msg != null) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        });

        // Start login flow.
        if (googleIdToken != null && !googleIdToken.trim().isEmpty()) {
            viewModel.loginWithGoogle(googleIdToken);
        } else {
            viewModel.login(email, password);
        }
    }

    /**
     * Routes user to the correct home screen based on insurance partnership.
     *
     * Logic:
     * - If current auth user is linked to an insurance company doc
     *   where partnerUid == uid and isPartner == true:
     *      -> InsuranceHomeActivity
     * - Otherwise:
     *      -> HomeActivity
     */
    private void routeAfterLoginByRole() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            // Safety fallback: return to login
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        String uid = user.getUid();

        FirebaseFirestore.getInstance()
                .collection("insurance_companies")
                .whereEqualTo("partnerUid", uid)
                .whereEqualTo("isPartner", true)
                .limit(1)
                .get()
                .addOnSuccessListener(qs -> {
                    Intent nextIntent;
                    com.example.drive_kit.Data.Repository.NotificationSchedulerRepository scheduler =
                            new com.example.drive_kit.Data.Repository.NotificationSchedulerRepository();

                    if (!qs.isEmpty()) {
                        // Insurance partner user
                        String companyId = qs.getDocuments().get(0).getId();

                        // schedule insurance worker only
                        scheduler.cancelDriver(getApplicationContext());
                        scheduler.scheduleInsuranceDaily(getApplicationContext());

                        nextIntent = new Intent(this, InsuranceHomeActivity.class);
                        nextIntent.putExtra("insuranceCompanyId", companyId);
                    } else {
                        // Regular driver user

                        // schedule driver worker only
                        scheduler.cancelInsurance(getApplicationContext());
                        scheduler.scheduleDriverDaily(getApplicationContext());

                        nextIntent = new Intent(this, HomeActivity.class);
                    }

                    nextIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(nextIntent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    com.example.drive_kit.Data.Repository.NotificationSchedulerRepository scheduler =
                            new com.example.drive_kit.Data.Repository.NotificationSchedulerRepository();
                    scheduler.cancelInsurance(getApplicationContext());
                    scheduler.scheduleDriverDaily(getApplicationContext());

                    Intent nextIntent = new Intent(this, HomeActivity.class);
                    nextIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(nextIntent);
                    finish();
                });
    }
}
