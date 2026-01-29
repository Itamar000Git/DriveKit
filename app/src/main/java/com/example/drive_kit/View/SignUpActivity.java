package com.example.drive_kit.View;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.drive_kit.Model.CarModel;
import com.example.drive_kit.R;
import com.example.drive_kit.ViewModel.SignUpViewModel;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.widget.ArrayAdapter;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import java.util.ArrayList;
import java.util.LinkedHashSet;

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
    private CarModel carModel;
    // private EditText yearEditText;
    private MaterialAutoCompleteTextView yearDropdown ;

    private int year;

    // Dropdown for car manufacturer
    private MaterialAutoCompleteTextView manufacturerDropdown;

    // Date input fields (Material Design text inputs)

    private TextInputEditText insuranceDateEditText;
    private TextInputEditText testDateEditText;
    private SignUpViewModel viewModel;
    private TextInputEditText treatmentDateEditText;

    private boolean userChangedTestDate = false;
    private boolean userChangedModel = false;
    private boolean userChangedYear = false;
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
        manufacturerDropdown = findViewById(R.id.manufacturerDropdown);
        yearDropdown = findViewById(R.id.yearDropdown);



        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        ArrayList<String> years = new ArrayList<>();
        for (int y = currentYear; y >= 1980; y--) {
            years.add(String.valueOf(y));
        }
        ArrayAdapter<String> yearAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, years);
        yearDropdown.setAdapter(yearAdapter);

        year = currentYear; // default
        yearDropdown.setText(String.valueOf(currentYear), false);

        yearDropdown.setOnClickListener(v -> yearDropdown.showDropDown());


        LinkedHashSet<String> unique = new LinkedHashSet<>(); //for the dropdown manufactures
        for (CarModel c : CarModel.values()) {
            unique.add(c.name());
        }
        ArrayList<String> items = new ArrayList<>(unique);
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        manufacturerDropdown.setAdapter(adapter);
        carModel = CarModel.UNKNOWN;

        manufacturerDropdown.setOnClickListener(v -> manufacturerDropdown.showDropDown());

        manufacturerDropdown.setOnItemClickListener((parent, view, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);
            carModel = CarModel.valueOf(selected);
            userChangedModel = true;
        });
        yearDropdown.setOnItemClickListener((parent, view, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);
            year = Integer.parseInt(selected);
            userChangedYear = true;
        });




//        manufacturerDropdown.setOnFocusChangeListener((v, hasFocus) -> {
//            if (!hasFocus) {
//                updateCarModelFromInput(manufacturerDropdown);
//            }
//        });




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

        //Listener to the focus change on the car number field
//        carNumberEditText.setOnFocusChangeListener((v, hasFocus) -> {
//            if (!hasFocus && !userChangedTestDate) {
//                userChangedTestDate = true;
//                String carNumber = carNumberEditText.getText().toString().trim();
//                if (!carNumber.isEmpty()) {
//                    fetchTestDateFromGov(carNumber);
//                }
//            }
//        });

        carNumberEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String carNumber = carNumberEditText.getText().toString().trim();

                // אם אין מספר רכב - אין מה למשוך
                if (carNumber.isEmpty()) return;

                // נבצע קריאה רק אם יש לפחות שדה אחד שהמשתמש לא שינה ידנית
                if (!userChangedTestDate || !userChangedModel || !userChangedYear) {
                    fetchCarInfoFromGov(carNumber);
                }
            }
        });




        // Open date picker when insurance date field is clicked
        insuranceDateEditText.setOnClickListener(
                v ->{
                    carNumberEditText.clearFocus();
                    triggerCarLookupIfNeeded();
                    openDatePickerInsurance();
                });

        // Open date picker when test date field is clicked

        testDateEditText.setOnClickListener(v ->
                {
                    carNumberEditText.clearFocus();
                    triggerCarLookupIfNeeded();
                    openDatePickerTest();
                });

        // Open date picker when 10K treatment date field is clicked

        treatmentDateEditText.setOnClickListener(v ->
                {
                    carNumberEditText.clearFocus();
                    triggerCarLookupIfNeeded();
                    openDatePickerTreat();
                });
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

            //updateCarModelFromInput(manufacturerDropdown);
            //String manufacturerText = manufacturerDropdown.getText().toString().trim();


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
            intent.putExtra("carModel", carModel);
            intent.putExtra("year", year);


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
            userChangedTestDate = true;
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

    private void triggerCarLookupIfNeeded() {
        String carNumber = carNumberEditText.getText().toString().trim();

        // No car number -> nothing to fetch
        if (carNumber.isEmpty()) return;

        // Fetch only if at least ONE of the fields was NOT changed manually
        if (!userChangedTestDate || !userChangedModel || !userChangedYear) {
            fetchCarInfoFromGov(carNumber);
        }
    }



    private void fetchCarInfoFromGov(String carNumber) {
        // Build the gov.il API URL (we search by car number and take the first result)
        final String urlStr =
                "https://data.gov.il/api/3/action/datastore_search" +
                        "?resource_id=053cea08-09bc-40ec-8f7a-156f0677aff3" +
                        "&q=" + carNumber +
                        "&limit=1";

        // Run network call in background thread (not on UI thread)
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            java.net.HttpURLConnection conn = null;

            try {
                // Open HTTP connection
                java.net.URL url = new java.net.URL(urlStr);
                conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                // Check HTTP status
                int code = conn.getResponseCode();
                if (code != 200) {
                    Log.e("SignUp", "HTTP error: " + code);
                    return;
                }

                // Read response into a String
                java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(conn.getInputStream())
                );
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                // Parse JSON root object
                org.json.JSONObject root = new org.json.JSONObject(sb.toString());

                // If API says "success": false -> stop
                if (!root.optBoolean("success", false)) return;

                // Get "result" object
                org.json.JSONObject result = root.optJSONObject("result");
                if (result == null) return;

                // Get records array
                org.json.JSONArray records = result.optJSONArray("records");
                if (records == null || records.length() == 0) return;

                // Take the first record (limit=1)
                org.json.JSONObject rec0 = records.getJSONObject(0);

                // -------------------------
                // 1) Test date: "tokef_dt"
                // -------------------------
                Long testMillisFromApi = null;        // we store as millis for ViewModel
                String testDisplayFromApi = null;     // we store as dd/MM/yyyy for UI

                // "tokef_dt" usually looks like: YYYY-MM-DD
                String tokefDt = rec0.optString("tokef_dt", "");
                if (!tokefDt.isEmpty()) {
                    java.text.SimpleDateFormat apiFmt =
                            new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);

                    java.util.Date tokefDate = apiFmt.parse(tokefDt);
                    if (tokefDate != null) {
                        java.util.Calendar cal = java.util.Calendar.getInstance();
                        cal.setTime(tokefDate);

                        // Your logic: test date = tokef_dt minus 1 year
                        cal.add(java.util.Calendar.YEAR, -1);

                        testMillisFromApi = cal.getTimeInMillis();

                        java.text.SimpleDateFormat outFmt =
                                new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
                        testDisplayFromApi = outFmt.format(cal.getTime());
                    }
                }

                // -------------------------
                // 2) Production year: "shnat_yitzur"
                // -------------------------
                Integer yearFromApi = null;

                // "shnat_yitzur" is the car production year (example: 2018)
                if (rec0.has("shnat_yitzur")) {
                    int y = rec0.optInt("shnat_yitzur", -1);
                    if (y > 0) yearFromApi = y;
                }

                // -------------------------
                // 3) Manufacturer/model: Hebrew values -> map to enum
                // -------------------------
                // Important:
                // The API returns Hebrew names in fields like "tozeret_nm".
                // We will map that text to our enum using CarModel.fromGovValue(...)
                String tozeretNm = rec0.optString("tozeret_nm", "");       // Manufacturer name (often Hebrew)

                // First try the main manufacturer field:
                CarModel modelFromApi = CarModel.fromGovValue(tozeretNm);



                // Copy values to final variables (needed because we go into runOnUiThread)
                Long finalTestMillisFromApi = testMillisFromApi;
                String finalTestDisplayFromApi = testDisplayFromApi;
                Integer finalYearFromApi = yearFromApi;
                CarModel finalModelFromApi = modelFromApi;

                // Update UI on main thread
                runOnUiThread(() -> {

                    // Update test date only if user did NOT change it manually
                    if (!userChangedTestDate && finalTestMillisFromApi != null && finalTestDisplayFromApi != null) {
                        viewModel.setSelectedTestDateMillis(finalTestMillisFromApi);
                        testDateEditText.setText(finalTestDisplayFromApi);
                    }

                    // Update year only if user did NOT change it manually
                    if (!userChangedYear && finalYearFromApi != null) {
                        year = finalYearFromApi;
                        yearDropdown.setText(String.valueOf(year), false);
                    }

                    // Update model only if user did NOT change it manually
                    if (!userChangedModel && finalModelFromApi != null && finalModelFromApi != CarModel.UNKNOWN) {
                        carModel = finalModelFromApi;

                        // Show the enum value in the dropdown (TOYOTA / HYUNDAY / ...)
                        manufacturerDropdown.setText(carModel.name(), false);
                    }
                });

            } catch (Exception e) {
                Log.e("SignUp", "fetchCarInfoFromGov failed", e);
            } finally {
                if (conn != null) conn.disconnect();
            }
        });
    }



    private void updateCarModelFromInput(MaterialAutoCompleteTextView manufacturerDropdown) {
        String text = manufacturerDropdown.getText().toString().trim();
        if (text.isEmpty()) {
            carModel = CarModel.UNKNOWN;
            return;
        }
        try {
            carModel = CarModel.valueOf(text);
            return;
        } catch (Exception ignored) { }

        try {
            carModel = CarModel.valueOf(text.toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            carModel = CarModel.UNKNOWN;
        }
    }

    private CarModel mapGovToCarModel(String tozeretNm, String kinuy, String degemNm) {
        String[] candidates = new String[]{tozeretNm, kinuy, degemNm};

        for (String cand : candidates) {
            if (cand == null) continue;
            String c = normalize(cand);
            if (c.isEmpty()) continue;

            for (CarModel m : CarModel.values()) {
                if (m == CarModel.UNKNOWN) continue;

                String enumNorm = normalize(m.name());

                // התאמות "רכות" כדי לתפוס גם מצבים כמו "יונדאי טורקיה"
                if (enumNorm.equals(c) || enumNorm.contains(c) || c.contains(enumNorm)) {
                    return m;
                }
            }
        }
        return CarModel.UNKNOWN;
    }

    private String normalize(String s) {
        // משאיר רק אותיות/מספרים (כולל עברית), ומסיר רווחים/סימנים
        return s.trim().toUpperCase(Locale.ROOT).replaceAll("[^0-9A-Zא-ת]+", "");
    }



}

