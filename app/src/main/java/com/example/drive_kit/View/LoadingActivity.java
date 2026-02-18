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
 * 4) Route user:
 *    - Insurance partner -> InsuranceHomeActivity
 *    - Driver with profile doc -> HomeActivity
 *    - Driver without profile doc -> Complete/Edit profile screen
 */
public class LoadingActivity extends AppCompatActivity {

    private com.example.drive_kit.Data.Repository.NotificationSchedulerRepository scheduler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading);

        scheduler = new com.example.drive_kit.Data.Repository.NotificationSchedulerRepository();

        Intent intent = getIntent();
        String email = intent.getStringExtra("email");
        String password = intent.getStringExtra("password");
        String googleIdToken = intent.getStringExtra("googleIdToken");

        LoadingViewModel viewModel = new ViewModelProvider(this).get(LoadingViewModel.class);

        viewModel.getLoginSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(this, "התחברת בהצלחה", Toast.LENGTH_SHORT).show();
                routeAfterLogin();
            }
        });

        viewModel.getLoginError().observe(this, msg -> {
            if (msg != null) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        });

        if (googleIdToken != null && !googleIdToken.trim().isEmpty()) {
            viewModel.loginWithGoogle(googleIdToken);
        } else {
            viewModel.login(email, password);
        }
    }

    /**
     * Route order:
     * 1) Insurance partner?
     * 2) If not insurance -> check drivers/{uid} exists?
     *    - yes -> HomeActivity
     *    - no  -> Complete/Edit profile
     */
    private void routeAfterLogin() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        String uid = user.getUid();

        // 1) Check insurance role first
        FirebaseFirestore.getInstance()
                .collection("insurance_companies")
                .whereEqualTo("partnerUid", uid)
                .whereEqualTo("isPartner", true)
                .limit(1)
                .get()
                .addOnSuccessListener(qs -> {
                    if (!qs.isEmpty()) {
                        // Insurance partner
                        String companyId = qs.getDocuments().get(0).getId();

                        scheduler.cancelDriver(getApplicationContext());
                        scheduler.scheduleInsuranceDaily(getApplicationContext());

                        Intent nextIntent = new Intent(this, InsuranceHomeActivity.class);
                        nextIntent.putExtra("insuranceCompanyId", companyId);
                        nextIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(nextIntent);
                        finish();
                        return;
                    }

                    // 2) Not insurance -> check drivers doc by UID
                    checkDriverProfileAndRoute(user);
                })
                .addOnFailureListener(e -> {
                    // אם נכשלת בדיקת insurance, עדיין מנסים מסלול driver
                    checkDriverProfileAndRoute(user);
                });
    }

    /**
     * Check if drivers/{uid} exists.
     * exists -> Home
     * not exists -> Complete/Edit profile
     */
    private void checkDriverProfileAndRoute(FirebaseUser user) {
        String uid = user.getUid();

        FirebaseFirestore.getInstance()
                .collection("drivers")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        // Driver with profile
                        scheduler.cancelInsurance(getApplicationContext());
                        scheduler.scheduleDriverDaily(getApplicationContext());

                        Intent nextIntent = new Intent(this, HomeActivity.class);
                        nextIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(nextIntent);
                        finish();
                    } else {
                        // Auth קיים אבל אין פרופיל נהג ב-Firestore -> מעבר להרשמה
                        scheduler.cancelInsurance(getApplicationContext());
                        scheduler.cancelDriver(getApplicationContext());

                        Intent nextIntent = new Intent(this, SignUpActivity.class);
                        nextIntent.putExtra("fromAuthNoDriverDoc", true);
                        nextIntent.putExtra("prefillEmail", user.getEmail()); // אופציונלי
                        nextIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(nextIntent);
                        finish();
                    }

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "שגיאה בבדיקת פרופיל נהג", Toast.LENGTH_SHORT).show();

                    // fallback בטוח: חזרה למסך כניסה
                    Intent back = new Intent(this, MainActivity.class);
                    back.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(back);
                    finish();
                });
    }
}
