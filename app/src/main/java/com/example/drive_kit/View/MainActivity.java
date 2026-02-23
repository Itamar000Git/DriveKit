package com.example.drive_kit.View;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.drive_kit.R;
import com.example.drive_kit.ViewModel.ForgotPasswordViewModel;
import com.example.drive_kit.ViewModel.MainViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import androidx.core.splashscreen.SplashScreen;

public class MainActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button signinButton;
    private Button signupButton;
    private Button googleLoginButton;

    private MainViewModel viewModel;
    private GoogleSignInClient googleSignInClient;

    private final ActivityResultLauncher<Intent> googleLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                Intent data = result.getData();
                if (data == null) {
                    Toast.makeText(this, "התחברות עם Google בוטלה", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data)
                            .getResult(ApiException.class);

                    if (account == null) {
                        Toast.makeText(this, "לא התקבל חשבון Google", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String idToken = account.getIdToken();
                    if (idToken == null || idToken.trim().isEmpty()) {
                        Toast.makeText(this, "חסר idToken (בדוק default_web_client_id)", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // ממשיכים ל-LoadingActivity לביצוע Firebase login + routing
                    Intent intent = new Intent(MainActivity.this, LoadingActivity.class);
                    intent.putExtra("googleIdToken", idToken);
                    intent.putExtra("authFlow", true); // NEW
                    startActivity(intent);

                } catch (ApiException e) {
                    Toast.makeText(this, "שגיאה בהתחברות עם Google: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signinButton = findViewById(R.id.loginButton);
        signupButton = findViewById(R.id.signupButton);
        googleLoginButton = findViewById(R.id.googleLoginButton);

        ForgotPasswordViewModel forgotVm =
                new ViewModelProvider(this).get(ForgotPasswordViewModel.class);

        TextView tvForgot = findViewById(R.id.tvForgotPassword);
        tvForgot.setOnClickListener(v -> showForgotDialog(forgotVm));

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        viewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null && !msg.trim().isEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        });

        // GoogleSignInClient
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(getString(R.string.default_web_client_id))
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Email/Password -> LoadingActivity
        signinButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (!viewModel.validateLoginInputs(email, password)) return;

            Intent intent = new Intent(MainActivity.this, LoadingActivity.class);
            intent.putExtra("email", email);
            intent.putExtra("password", password);
            intent.putExtra("authFlow", true); // NEW
            startActivity(intent);
        });

        signupButton.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, SignUpActivity.class))
        );

        // Google button -> chooser
        googleLoginButton.setOnClickListener(v -> {
            googleSignInClient.signOut().addOnCompleteListener(task -> {
                Intent signInIntent = googleSignInClient.getSignInIntent();
                googleLauncher.launch(signInIntent);
            });
        });
    }

    private void showForgotDialog(ForgotPasswordViewModel vm) {
        View view = getLayoutInflater().inflate(R.layout.dialog_forgot_password, null);
        TextInputEditText etEmail = view.findViewById(R.id.etEmail);
        ProgressBar pb = view.findViewById(R.id.pbLoading);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setTitle("איפוס סיסמה")
                .setView(view)
                .setNegativeButton("ביטול", (d, w) -> d.dismiss())
                .setPositiveButton("שלח", null);

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(d -> {
            Button btnSend = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btnSend.setOnClickListener(x -> vm.sendReset(String.valueOf(etEmail.getText())));
        });

        vm.getState().observe(this, s -> {
            pb.setVisibility(s.loading ? View.VISIBLE : View.GONE);
            if (dialog.isShowing()) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(!s.loading);
            }
            if (s.message != null) {
                Toast.makeText(this, s.message, Toast.LENGTH_LONG).show();
                if (!s.loading) dialog.dismiss();
            }
        });

        dialog.show();
    }
}
