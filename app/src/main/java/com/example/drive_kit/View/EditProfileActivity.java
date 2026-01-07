    package com.example.drive_kit.View;

    import android.os.Bundle;
    import android.widget.Button;
    import android.widget.Toast;

    import androidx.appcompat.app.AppCompatActivity;
    import androidx.lifecycle.ViewModelProvider;

    import com.example.drive_kit.R;
    import com.example.drive_kit.Model.Driver;
    import com.example.drive_kit.ViewModel.EditProfileViewModel;
    import com.google.android.material.datepicker.MaterialDatePicker;
    import com.google.android.material.textfield.TextInputEditText;
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.auth.FirebaseUser;

    public class EditProfileActivity extends AppCompatActivity {

        private TextInputEditText firstNameEditText;
        private TextInputEditText lastNameEditText;
        private TextInputEditText emailEditText;
        private TextInputEditText phoneEditText;
        private TextInputEditText carNumberEditText;
        private TextInputEditText insuranceDateEditText;
        private TextInputEditText testDateEditText;
        private TextInputEditText treatmentDateEditText;

        private Button saveButton;
        private Button cancelButton;

        private EditProfileViewModel viewModel;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.edit_profile_activity);

            firstNameEditText = findViewById(R.id.firstNameEditText);
            lastNameEditText = findViewById(R.id.lastNameEditText);
            phoneEditText = findViewById(R.id.phoneEditText);
            carNumberEditText = findViewById(R.id.carNumberEditText);
            insuranceDateEditText = findViewById(R.id.insuranceDateEditText);
            testDateEditText = findViewById(R.id.testDateEditText);
            treatmentDateEditText = findViewById(R.id.treatmentDateEditText);

            saveButton = findViewById(R.id.saveButton);
            cancelButton = findViewById(R.id.cancelButton);

            viewModel = new ViewModelProvider(this).get(EditProfileViewModel.class);

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "משתמש לא מחובר", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            String uid = user.getUid();

            // When driver loads -> fill UI
            viewModel.getDriver().observe(this, d -> {
                if (d == null) return;

                firstNameEditText.setText(nullToEmpty(d.getFirstName()));
                lastNameEditText.setText(nullToEmpty(d.getLastName()));
                phoneEditText.setText(nullToEmpty(d.getPhone()));
                carNumberEditText.setText(nullToEmpty(d.getCarNumber()));

                // show formatted text
                insuranceDateEditText.setText(nullToEmpty(d.getFormattedInsuranceDate()));
                testDateEditText.setText(nullToEmpty(d.getFormattedTestDate()));
                treatmentDateEditText.setText(nullToEmpty(d.getFormattedTreatDate()));

                // keep millis in ViewModel
                viewModel.setSelectedInsuranceDateMillis(d.getInsuranceDateMillis());
                viewModel.setSelectedTestDateMillis(d.getTestDateMillis());
                viewModel.setSelectedTreatDateMillis(d.getTreatmentDateMillis());
            });

            viewModel.getToastMessage().observe(this, msg -> {
                if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            });

            viewModel.getFinishScreen().observe(this, finish -> {
                if (Boolean.TRUE.equals(finish)) {
                    finish();
                }
            });

            // Load data
            viewModel.loadProfile(uid);

            // Date pickers
            insuranceDateEditText.setOnClickListener(v -> openDatePicker("בחר תאריך ביטוח", selection -> {
                viewModel.setSelectedInsuranceDateMillis(selection);
                insuranceDateEditText.setText(viewModel.formatDate(selection));
            }));

            testDateEditText.setOnClickListener(v -> openDatePicker("בחר תאריך טסט", selection -> {
                viewModel.setSelectedTestDateMillis(selection);
                testDateEditText.setText(viewModel.formatDate(selection));
            }));

            treatmentDateEditText.setOnClickListener(v -> openDatePicker("בחר תאריך טיפול 10K", selection -> {
                viewModel.setSelectedTreatDateMillis(selection);
                treatmentDateEditText.setText(viewModel.formatDate(selection));
            }));

            cancelButton.setOnClickListener(v -> finish());

            saveButton.setOnClickListener(v -> {
                String firstName = text(firstNameEditText);
                String lastName = text(lastNameEditText);
                String phone = text(phoneEditText);
                String carNumber = text(carNumberEditText);

                viewModel.saveProfile(
                        uid,
                        firstName,
                        lastName,
                        phone,
                        carNumber
                );
            });
        }

        private interface DatePicked {
            void onPicked(long millis);
        }

        private void openDatePicker(String title, DatePicked cb) {
            MaterialDatePicker<Long> picker =
                    MaterialDatePicker.Builder.datePicker()
                            .setTitleText(title)
                            .build();

            picker.show(getSupportFragmentManager(), "DATE_PICKER_EDIT_PROFILE");
            picker.addOnPositiveButtonClickListener(cb::onPicked);
        }

        private String text(TextInputEditText et) {
            return et.getText() == null ? "" : et.getText().toString().trim();
        }

        private String nullToEmpty(String s) {
            return s == null ? "" : s;
        }
    }
