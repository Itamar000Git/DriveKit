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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * SignUpActivity is responsible for collecting user registration details.
 *
 * This screen allows the user to enter:
 * - Personal information (name, email, phone)
 * - Car information (car number)
 * - Important dates (insurance, test, 10K treatment)
 *
 * This Activity:
 * - Handles ONLY UI interactions and navigation
 * - Delegates validation and data handling to SignUpViewModel
 */
public class SignUpActivity extends AppCompatActivity {
    // Button to move to the next registration step
    private Button next;
    private Button canNotRemember2Month;
    private Button canNotRemember4Month;
    private Button canNotRemember6Month;


    // Input fields for personal details
    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText emailEditText;
    private EditText phoneEditText;
    private EditText carNumberEditText;

    // Date input fields (Material Design text inputs)

    private TextInputEditText insuranceDateEditText;
    private TextInputEditText testDateEditText;
    private SignUpViewModel viewModel;
    private TextInputEditText treatmentDateEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Required call to initialize the Activity properly
        super.onCreate(savedInstanceState);

        // Attach the signup.xml layout to this Activity
        // After this line, all views defined in signup.xml exist in memory
        setContentView(R.layout.signup);

        // Find and connect all UI elements from the XML layout
        next = findViewById(R.id.next);
        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        carNumberEditText = findViewById(R.id.carNumberEditText);
        insuranceDateEditText = findViewById(R.id.insuranceDateEditText);
        testDateEditText = findViewById(R.id.testDateEditText);
        treatmentDateEditText= findViewById(R.id.service10kDateEditText);
        canNotRemember2Month = findViewById(R.id.btnService10kUpTo2Months);
        canNotRemember4Month = findViewById(R.id.btnService10kUpTo4Months);
        canNotRemember6Month = findViewById(R.id.btnService10kUpTo6MonthsPlus);


        // Debug checks to ensure that date fields are correctly linked to the XML
        // If one of these logs appears, it means the ID is missing or incorrect in XML
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

        // Create (or retrieve) the ViewModel associated with this Activity
        // The ViewModel holds the selected dates and validation logic
        viewModel = new ViewModelProvider(this).get(SignUpViewModel.class);


        // Observe insurance date validation errors
        // If an error message is published, it will be shown on the input field
        viewModel.getInsuranceDateError().observe(this, err -> {
            if (err != null) insuranceDateEditText.setError(err);
        });


        // Observe test date validation errors
        viewModel.getTestDateError().observe(this, err -> {
            if (err != null) testDateEditText.setError(err);
        });
        // Observe 10K treatment date validation errors
        viewModel.getTreatDateError().observe(this, err -> {
            if (err != null) treatmentDateEditText.setError(err);
        });


        // Open date picker when insurance date field is clicked
        insuranceDateEditText.setOnClickListener(v -> openDatePickerInsurance());

        // Open date picker when test date field is clicked

        testDateEditText.setOnClickListener(v -> openDatePickerTest());

        // Open date picker when 10K treatment date field is clicked

        treatmentDateEditText.setOnClickListener(v -> openDatePickerTreat());
        long now = System.currentTimeMillis();



        canNotRemember2Month.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, -2);
            long millis = cal.getTimeInMillis();
            viewModel.setSelectedTreatDateMillis(millis);
            SimpleDateFormat sdf =
                    new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            treatmentDateEditText.setText(sdf.format(cal.getTime()));
        });
        canNotRemember4Month.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, -4);
            long millis = cal.getTimeInMillis();
            viewModel.setSelectedTreatDateMillis(millis);
            SimpleDateFormat sdf =
                    new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            treatmentDateEditText.setText(sdf.format(cal.getTime()));
        });
        canNotRemember6Month.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, -6);
            long millis = cal.getTimeInMillis();
            viewModel.setSelectedTreatDateMillis(millis);
            SimpleDateFormat sdf =
                    new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            treatmentDateEditText.setText(sdf.format(cal.getTime()));
        });





        // Handle click on the "Next" button
        next.setOnClickListener(v -> {

            // Validate that all required dates were selected
            // Validation logic is handled inside the ViewModel
            if (!viewModel.validateDates()) {
                return;
            }


            // Extract and trim text input values
            String firstName = firstNameEditText.getText().toString().trim();
            String lastName = lastNameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String carNumber = carNumberEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();


            // Create an Intent to move to the next registration screen
            Intent intent = new Intent(SignUpActivity.this, SetUsernamePasswordActivity.class);
            intent.putExtra("firstName", firstName);
            intent.putExtra("lastName", lastName);
            intent.putExtra("email", email);
            intent.putExtra("phone", phone);
            intent.putExtra("carNumber", carNumber);
            intent.putExtra("insuranceDateMillis", viewModel.getSelectedInsuranceDateMillis());
            intent.putExtra("testDateMillis", viewModel.getSelectedTestDateMillis());
            intent.putExtra("treatmentDateMillis", viewModel.getSelectedTreatDateMillis());

            // Start the next Activity
            startActivity(intent);
        });
    }


    /**
     * Opens a Material Date Picker for selecting the insurance expiration date.
     */
    private void openDatePickerInsurance() {
        MaterialDatePicker<Long> datePicker =
                MaterialDatePicker.Builder.datePicker()
                        .setTitleText("בחר תאריך ביטוח")
                        .build();
        // Show the date picker dialog
        datePicker.show(getSupportFragmentManager(), "DATE_PICKER_INSURANCE");

        // Handle date selection
        datePicker.addOnPositiveButtonClickListener(selection -> {
            viewModel.setSelectedInsuranceDateMillis(selection);
            insuranceDateEditText.setText(datePicker.getHeaderText());
        });
    }


    /**
     * Opens a Material Date Picker for selecting the test date.
     */
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


    /**
     * Opens a Material Date Picker for selecting the 10K treatment date.
     */
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

