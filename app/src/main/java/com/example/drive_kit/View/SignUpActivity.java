package com.example.drive_kit.View;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.drive_kit.R;
import com.example.drive_kit.ViewModel.SignUpViewModel;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
public class SignUpActivity extends AppCompatActivity {

    private Button next;
    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText emailEditText;
    private EditText phoneEditText;
    private EditText carNumberEditText;
    private TextInputEditText insuranceDateEditText;
    private TextInputEditText testDateEditText;
    private SignUpViewModel viewModel;
    private TextInputEditText treatmentDateEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        // find views
        next = findViewById(R.id.next);
        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        carNumberEditText = findViewById(R.id.carNumberEditText);
        insuranceDateEditText = findViewById(R.id.insuranceDateEditText);
        testDateEditText = findViewById(R.id.testDateEditText);
        treatmentDateEditText= findViewById(R.id.service10kDateEditText);

        if (insuranceDateEditText == null) {
            Log.e("SignUp", "insuranceDateEditText is NULL (check XML id!)");
        }
        if (testDateEditText == null) {
            Log.e("SignUp", "testDateEditText is NULL (check XML id!)");
        }
        // Need to take care of bottoms
        if(treatmentDateEditText== null){
            Log.e("SignUp", "treatmentEditText is NULL (check XML id!)");
        }
        viewModel = new ViewModelProvider(this).get(SignUpViewModel.class);

        // Observe the LiveData and update the UI when the dates are invalid
        viewModel.getInsuranceDateError().observe(this, err -> {
            if (err != null) insuranceDateEditText.setError(err);
        });

        viewModel.getTestDateError().observe(this, err -> {
            if (err != null) testDateEditText.setError(err);
        });
        viewModel.getTreatDateError().observe(this, err -> {
            if (err != null) treatmentDateEditText.setError(err);
        });
        // Set the click listener for the insurance date edit text
        insuranceDateEditText.setOnClickListener(v -> openDatePickerInsurance());
        testDateEditText.setOnClickListener(v -> openDatePickerTest());
        treatmentDateEditText.setOnClickListener(v -> openDatePickerTreat());

        next.setOnClickListener(v -> {

            if (!viewModel.validateDates()) {
                return;
            }
            // extract text fields
            String firstName = firstNameEditText.getText().toString().trim();
            String lastName = lastNameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String carNumber = carNumberEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();

            //goes to the next page
            Intent intent = new Intent(SignUpActivity.this, SetUsernamePasswordActivity.class);
            intent.putExtra("firstName", firstName);
            intent.putExtra("lastName", lastName);
            intent.putExtra("email", email);
            intent.putExtra("phone", phone);
            intent.putExtra("carNumber", carNumber);
            intent.putExtra("insuranceDateMillis", viewModel.getSelectedInsuranceDateMillis());
            intent.putExtra("testDateMillis", viewModel.getSelectedTestDateMillis());
            intent.putExtra("treatmentDateMillis", viewModel.getSelectedTreatDateMillis());

            startActivity(intent);
        });
    }

    private void openDatePickerInsurance() {
        MaterialDatePicker<Long> datePicker =
                MaterialDatePicker.Builder.datePicker()
                        .setTitleText("בחר תאריך ביטוח")
                        .build();

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER_INSURANCE");

        datePicker.addOnPositiveButtonClickListener(selection -> {
            viewModel.setSelectedInsuranceDateMillis(selection);
            insuranceDateEditText.setText(datePicker.getHeaderText());
        });
    }

    private void openDatePickerTest() {
        MaterialDatePicker<Long> datePicker =
                MaterialDatePicker.Builder.datePicker()
                        .setTitleText("בחר תאריך טסט")
                        .build();

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER_TEST");

        datePicker.addOnPositiveButtonClickListener(selection -> {
            viewModel.setSelectedTestDateMillis(selection);
            testDateEditText.setText(datePicker.getHeaderText());
        });
    }
    private void openDatePickerTreat() {
        MaterialDatePicker<Long> datePicker =
                MaterialDatePicker.Builder.datePicker()
                        .setTitleText("בחר תאריך טיפול 10K")
                        .build();

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER_TREATMENT");

        datePicker.addOnPositiveButtonClickListener(selection -> {
            viewModel.setSelectedTreatDateMillis(selection);
            treatmentDateEditText.setText(datePicker.getHeaderText());
        });
    }
}

