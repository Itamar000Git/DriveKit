//
//
//package com.example.drive_kit.View;
//
//import android.content.Intent;
//import android.net.Uri;
//import android.os.Bundle;
//import android.util.Log;
//import android.widget.ArrayAdapter;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ImageView;
//
//import androidx.activity.result.ActivityResultLauncher;
//import androidx.activity.result.contract.ActivityResultContracts;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.lifecycle.ViewModelProvider;
//
//import com.example.drive_kit.Model.CarModel;
//import com.example.drive_kit.R;
//import com.example.drive_kit.ViewModel.SignUpViewModel;
//import com.google.android.material.datepicker.MaterialDatePicker;
//import com.google.android.material.textfield.MaterialAutoCompleteTextView;
//import com.google.android.material.textfield.TextInputEditText;
//import com.google.android.material.textfield.TextInputLayout;
//
//import org.json.JSONArray;
//import org.json.JSONObject;
//
//import java.io.BufferedReader;
//import java.io.InputStreamReader;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.LinkedHashSet;
//import java.util.Locale;
//
//public class SignUpActivity extends AppCompatActivity {
//
//    // Buttons
//    private Button next;
//    private Button canNotRemember2Month;
//    private Button canNotRemember4Month;
//    private Button canNotRemember6Month;
//
//    // Input fields
//    private EditText firstNameEditText;
//    private EditText lastNameEditText;
//    private EditText emailEditText;
//    private EditText phoneEditText;
//    private EditText carNumberEditText;
//
//    private CarModel carModel;
//
//    // Dropdowns
//    private MaterialAutoCompleteTextView yearDropdown;
//    private int year;
//
//    private MaterialAutoCompleteTextView manufacturerDropdown;
//    private MaterialAutoCompleteTextView modelDropdown;
//    private String selectedModelName = null;
//
//    // Dates
//    private TextInputEditText insuranceDateEditText;
//    private TextInputEditText testDateEditText;
//    private TextInputEditText treatmentDateEditText;
//
//    private SignUpViewModel viewModel;
//
//    private boolean userChangedTestDate = false;
//    private boolean userChangedModel = false; // manufacturer changed
//    private boolean userChangedYear = false;
//    private boolean userChangedCarSpecificModel = false;
//
//    // =========================
//    // Car photo UI + data
//    // =========================
//    private TextInputLayout carPhotoLayout;
//    private TextInputEditText carPhotoEditText;
//    //private ImageView carPhotoPreview; // optional in XML
//
//    private Uri selectedCarPhotoUri = null;
//
//    // Gallery picker launcher
//    private ActivityResultLauncher<String> pickCarPhotoLauncher;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.signup);
//
//        // =========================
//        // findViewById
//        // =========================
//        next = findViewById(R.id.next);
//
//        firstNameEditText = findViewById(R.id.firstNameEditText);
//        lastNameEditText = findViewById(R.id.lastNameEditText);
//        emailEditText = findViewById(R.id.emailEditText);
//        phoneEditText = findViewById(R.id.phoneEditText);
//        carNumberEditText = findViewById(R.id.carNumberEditText);
//
//        insuranceDateEditText = findViewById(R.id.insuranceDateEditText);
//        testDateEditText = findViewById(R.id.testDateEditText);
//        treatmentDateEditText = findViewById(R.id.service10kDateEditText);
//
//        canNotRemember2Month = findViewById(R.id.btnService10kUpTo2Months);
//        canNotRemember4Month = findViewById(R.id.btnService10kUpTo4Months);
//        canNotRemember6Month = findViewById(R.id.btnService10kUpTo6MonthsPlus);
//
//        manufacturerDropdown = findViewById(R.id.manufacturerDropdown);
//        yearDropdown = findViewById(R.id.yearDropdown);
//        modelDropdown = findViewById(R.id.modelDropdown);
//
//        // Car photo (IMPORTANT: ids must exist in XML)
//        carPhotoLayout = findViewById(R.id.carPhotoLayout);
//        carPhotoEditText = findViewById(R.id.carPhotoEditText);
////        // optional preview (אם לא קיים ב-XML זה יחזור null - זה בסדר)
////        carPhotoPreview = findViewById(R.id.carPhotoPreview);
//
//        // =========================
//        // Register Gallery Picker
//        // =========================
//        pickCarPhotoLauncher = registerForActivityResult(
//                new ActivityResultContracts.GetContent(),
//                uri -> {
//                    if (uri == null) return;
//
//                    selectedCarPhotoUri = uri;
//
//                    if (carPhotoEditText != null) {
//                        carPhotoEditText.setText("נבחרה תמונה");
//                        carPhotoEditText.setError(null);
//                    }
//                    if (carPhotoLayout != null) {
//                        carPhotoLayout.setError(null);
//                    }
//
////                    // Show preview only if ImageView exists in XML
////                    if (carPhotoPreview != null) {
////                        carPhotoPreview.setImageURI(uri);
////                        carPhotoPreview.setVisibility(android.view.View.VISIBLE);
////                    }
//                }
//        );
//
//        // Open picker on click (field)
//        if (carPhotoEditText != null) {
//            carPhotoEditText.setOnClickListener(v -> openCarPhotoPicker());
//        }
//
//        // Open picker on end icon click (camera icon)
//        if (carPhotoLayout != null) {
//            carPhotoLayout.setEndIconOnClickListener(v -> openCarPhotoPicker());
//        }
//
//        // =========================
//        // Year dropdown
//        // =========================
//        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
//        ArrayList<String> years = new ArrayList<>();
//        for (int y = currentYear; y >= 1980; y--) years.add(String.valueOf(y));
//
//        ArrayAdapter<String> yearAdapter =
//                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, years);
//        yearDropdown.setAdapter(yearAdapter);
//
//        year = currentYear;
//        yearDropdown.setText(String.valueOf(currentYear), false);
//        yearDropdown.setOnClickListener(v -> yearDropdown.showDropDown());
//
//        yearDropdown.setOnItemClickListener((parent, view, position, id) -> {
//            String selected = (String) parent.getItemAtPosition(position);
//            year = Integer.parseInt(selected);
//            userChangedYear = true;
//        });
//
//        // =========================
//        // Manufacturer dropdown
//        // =========================
//        LinkedHashSet<String> unique = new LinkedHashSet<>();
//        for (CarModel c : CarModel.values()) unique.add(c.name());
//
//        ArrayList<String> manufacturerItems = new ArrayList<>(unique);
//        ArrayAdapter<String> manufacturerAdapter =
//                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, manufacturerItems);
//        manufacturerDropdown.setAdapter(manufacturerAdapter);
//
//        carModel = CarModel.UNKNOWN;
//
//        manufacturerDropdown.setOnClickListener(v -> manufacturerDropdown.showDropDown());
//
//        manufacturerDropdown.setOnItemClickListener((parent, view, position, id) -> {
//            String selected = (String) parent.getItemAtPosition(position);
//            carModel = CarModel.valueOf(selected);
//            userChangedModel = true;
//
//            updateModelDropdownAdapter(carModel);
//
//            selectedModelName = null;
//            userChangedCarSpecificModel = false;
//            if (modelDropdown != null) modelDropdown.setText("", false);
//        });
//
//        // =========================
//        // Specific model dropdown
//        // =========================
//        updateModelDropdownAdapter(carModel);
//        if (modelDropdown != null) {
//            modelDropdown.setOnClickListener(v -> modelDropdown.showDropDown());
//            modelDropdown.setOnItemClickListener((parent, view, position, id) -> {
//                String selected = (String) parent.getItemAtPosition(position);
//                selectedModelName = selected;
//                userChangedCarSpecificModel = true;
//            });
//        }
//
//        // =========================
//        // Debug nulls
//        // =========================
//        if (insuranceDateEditText == null) Log.e("SignUp", "insuranceDateEditText is NULL (check XML id!)");
//        if (testDateEditText == null) Log.e("SignUp", "testDateEditText is NULL (check XML id!)");
//        if (treatmentDateEditText == null) Log.e("SignUp", "treatmentEditText is NULL (check XML id!)");
//        if (modelDropdown == null) Log.e("SignUp", "modelDropdown is NULL (check XML id modelDropdown!)");
//        if (carPhotoLayout == null) Log.e("SignUp", "carPhotoLayout is NULL (check XML id carPhotoLayout!)");
//        if (carPhotoEditText == null) Log.e("SignUp", "carPhotoEditText is NULL (check XML id carPhotoEditText!)");
//
//        // =========================
//        // ViewModel
//        // =========================
//        viewModel = new ViewModelProvider(this).get(SignUpViewModel.class);
//
//        viewModel.getInsuranceDateError().observe(this, err -> {
//            if (err != null) insuranceDateEditText.setError(err);
//        });
//        viewModel.getTestDateError().observe(this, err -> {
//            if (err != null) testDateEditText.setError(err);
//        });
//        viewModel.getTreatDateError().observe(this, err -> {
//            if (err != null) treatmentDateEditText.setError(err);
//        });
//
//        // =========================
//        // Car number focus -> fetch gov data if needed
//        // =========================
//        carNumberEditText.setOnFocusChangeListener((v, hasFocus) -> {
//            if (!hasFocus) {
//                String carNumber = carNumberEditText.getText().toString().trim();
//                if (carNumber.isEmpty()) return;
//
//                if (!userChangedTestDate || !userChangedModel || !userChangedYear || !userChangedCarSpecificModel) {
//                    fetchCarInfoFromGov(carNumber);
//                }
//            }
//        });
//
//        // =========================
//        // Date pickers
//        // =========================
//        insuranceDateEditText.setOnClickListener(v -> {
//            carNumberEditText.clearFocus();
//            triggerCarLookupIfNeeded();
//            openDatePickerInsurance();
//        });
//
//        testDateEditText.setOnClickListener(v -> {
//            carNumberEditText.clearFocus();
//            triggerCarLookupIfNeeded();
//            openDatePickerTest();
//        });
//
//        treatmentDateEditText.setOnClickListener(v -> {
//            carNumberEditText.clearFocus();
//            triggerCarLookupIfNeeded();
//            openDatePickerTreat();
//        });
//
//        // =========================
//        // "Can't remember" buttons
//        // =========================
//        canNotRemember2Month.setOnClickListener(v -> setTreatByMonthsBack(2));
//        canNotRemember4Month.setOnClickListener(v -> setTreatByMonthsBack(4));
//        canNotRemember6Month.setOnClickListener(v -> setTreatByMonthsBack(6));
//
//        // =========================
//        // Next
//        // =========================
//        next.setOnClickListener(v -> {
//
//            // 1) Validate dates first
//            if (!viewModel.validateDates()) return;
//
//            // 2) Read inputs
//            String firstName = firstNameEditText.getText().toString().trim();
//            String lastName  = lastNameEditText.getText().toString().trim();
//            String email     = emailEditText.getText().toString().trim();
//            String carNumber = carNumberEditText.getText().toString().trim();
//            String phone     = phoneEditText.getText().toString().trim();
//
//            // 3) Validate required fields
//            boolean missingText =
//                    firstName.isEmpty() ||
//                            lastName.isEmpty()  ||
//                            email.isEmpty()     ||
//                            phone.isEmpty()     ||
//                            carNumber.isEmpty();
//
//            boolean missingDropdowns =
//                    carModel == null || carModel == CarModel.UNKNOWN ||
//                            year <= 0 ||
//                            selectedModelName == null || selectedModelName.trim().isEmpty();
//
//            if (missingText || missingDropdowns) {
//                android.widget.Toast.makeText(
//                        SignUpActivity.this,
//                        "נא למלא את כל השדות",
//                        android.widget.Toast.LENGTH_SHORT
//                ).show();
//                return;
//            }
//
//            // OPTIONAL: אם אתה רוצה להפוך תמונה לחובה - תבטל הערות:
//            /*
//            if (selectedCarPhotoUri == null) {
//                if (carPhotoLayout != null) carPhotoLayout.setError("נא לבחור תמונת רכב");
//                return;
//            }
//            */
//
//            // 4) Continue
//            Intent intent = new Intent(SignUpActivity.this, SetUsernamePasswordActivity.class);
//            intent.putExtra("firstName", firstName);
//            intent.putExtra("lastName", lastName);
//            intent.putExtra("email", email);
//            intent.putExtra("phone", phone);
//            intent.putExtra("carNumber", carNumber);
//
//            intent.putExtra("insuranceDateMillis", viewModel.getSelectedInsuranceDateMillis());
//            intent.putExtra("testDateMillis", viewModel.getSelectedTestDateMillis());
//            intent.putExtra("treatmentDateMillis", viewModel.getSelectedTreatDateMillis());
//
//            intent.putExtra("carModel", carModel);
//            intent.putExtra("year", year);
//            intent.putExtra("carSpecificModel", selectedModelName);
//
//            // PASS PHOTO URI (if selected)
//            if (selectedCarPhotoUri != null) {
//                intent.putExtra("carPhotoUri", selectedCarPhotoUri.toString());
//            }
//
//            startActivity(intent);
//        });
//    }
//
//    // =========================
//    // Car photo picker
//    // =========================
//    private void openCarPhotoPicker() {
//        // opens gallery
//        pickCarPhotoLauncher.launch("image/*");
//    }
//
//    private void setTreatByMonthsBack(int months) {
//        Calendar cal = Calendar.getInstance();
//        cal.add(Calendar.MONTH, -months);
//        long millis = cal.getTimeInMillis();
//
//        viewModel.setSelectedTreatDateMillis(millis);
//
//        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
//        treatmentDateEditText.setText(sdf.format(cal.getTime()));
//    }
//
//    private void openDatePickerInsurance() {
//        MaterialDatePicker<Long> datePicker =
//                MaterialDatePicker.Builder.datePicker()
//                        .setTitleText("בחר תאריך ביטוח")
//                        .build();
//
//        datePicker.show(getSupportFragmentManager(), "DATE_PICKER_INSURANCE");
//
//        datePicker.addOnPositiveButtonClickListener(selection -> {
//            viewModel.setSelectedInsuranceDateMillis(selection);
//            insuranceDateEditText.setText(datePicker.getHeaderText());
//        });
//    }
//
//    private void openDatePickerTest() {
//        MaterialDatePicker<Long> datePicker =
//                MaterialDatePicker.Builder.datePicker()
//                        .setTitleText("בחר תאריך טסט")
//                        .build();
//
//        datePicker.show(getSupportFragmentManager(), "DATE_PICKER_TEST");
//
//        datePicker.addOnPositiveButtonClickListener(selection -> {
//            userChangedTestDate = true;
//            viewModel.setSelectedTestDateMillis(selection);
//            testDateEditText.setText(datePicker.getHeaderText());
//        });
//    }
//
//    private void openDatePickerTreat() {
//        MaterialDatePicker<Long> datePicker =
//                MaterialDatePicker.Builder.datePicker()
//                        .setTitleText("בחר תאריך טיפול 10K")
//                        .build();
//
//        datePicker.show(getSupportFragmentManager(), "DATE_PICKER_TREATMENT");
//
//        datePicker.addOnPositiveButtonClickListener(selection -> {
//            viewModel.setSelectedTreatDateMillis(selection);
//            treatmentDateEditText.setText(datePicker.getHeaderText());
//        });
//    }
//
//    private void triggerCarLookupIfNeeded() {
//        String carNumber = carNumberEditText.getText().toString().trim();
//        if (carNumber.isEmpty()) return;
//
//        if (!userChangedTestDate || !userChangedModel || !userChangedYear || !userChangedCarSpecificModel) {
//            fetchCarInfoFromGov(carNumber);
//        }
//    }
//
//    private void fetchCarInfoFromGov(String carNumber) {
//        final String urlStr =
//                "https://data.gov.il/api/3/action/datastore_search" +
//                        "?resource_id=053cea08-09bc-40ec-8f7a-156f0677aff3" +
//                        "&q=" + carNumber +
//                        "&limit=1";
//
//        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
//            HttpURLConnection conn = null;
//
//            try {
//                URL url = new URL(urlStr);
//                conn = (HttpURLConnection) url.openConnection();
//                conn.setRequestMethod("GET");
//                conn.setConnectTimeout(10000);
//                conn.setReadTimeout(10000);
//
//                int code = conn.getResponseCode();
//                if (code != 200) {
//                    Log.e("SignUp", "HTTP error: " + code);
//                    return;
//                }
//
//                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//                StringBuilder sb = new StringBuilder();
//                String line;
//                while ((line = reader.readLine()) != null) sb.append(line);
//                reader.close();
//
//                JSONObject root = new JSONObject(sb.toString());
//                if (!root.optBoolean("success", false)) return;
//
//                JSONObject result = root.optJSONObject("result");
//                if (result == null) return;
//
//                JSONArray records = result.optJSONArray("records");
//                if (records == null || records.length() == 0) return;
//
//                JSONObject rec0 = records.getJSONObject(0);
//
//                // 1) Test date from tokef_dt minus 1 year
//                Long testMillisFromApi = null;
//                String testDisplayFromApi = null;
//
//                String tokefDt = rec0.optString("tokef_dt", "");
//                if (!tokefDt.isEmpty()) {
//                    SimpleDateFormat apiFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
//                    java.util.Date tokefDate = apiFmt.parse(tokefDt);
//                    if (tokefDate != null) {
//                        Calendar cal = Calendar.getInstance();
//                        cal.setTime(tokefDate);
//                        cal.add(Calendar.YEAR, -1);
//
//                        testMillisFromApi = cal.getTimeInMillis();
//                        SimpleDateFormat outFmt = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
//                        testDisplayFromApi = outFmt.format(cal.getTime());
//                    }
//                }
//
//                // 2) Production year
//                Integer yearFromApi = null;
//                if (rec0.has("shnat_yitzur")) {
//                    int y = rec0.optInt("shnat_yitzur", -1);
//                    if (y > 0) yearFromApi = y;
//                }
//
//                // 3) Manufacturer
//                String tozeretNm = rec0.optString("tozeret_nm", "");
//                CarModel manufacturerFromApi = CarModel.fromGovValue(tozeretNm);
//
//                // 4) Specific model inference
//                String kinuyMishari = rec0.optString("kinuy_mishari", "");
//                String degemNm = rec0.optString("degem_nm", "");
//
//                CarModel manufacturerContext =
//                        (manufacturerFromApi != null && manufacturerFromApi != CarModel.UNKNOWN)
//                                ? manufacturerFromApi
//                                : carModel;
//
//                String specificModelNameFromApi =
//                        inferSpecificModelName(manufacturerContext, kinuyMishari, degemNm);
//
//                Long finalTestMillisFromApi = testMillisFromApi;
//                String finalTestDisplayFromApi = testDisplayFromApi;
//                Integer finalYearFromApi = yearFromApi;
//                CarModel finalManufacturerFromApi = manufacturerFromApi;
//                String finalSpecificModelNameFromApi = specificModelNameFromApi;
//                CarModel finalManufacturerContext = manufacturerContext;
//
//                runOnUiThread(() -> {
//
//                    if (!userChangedTestDate && finalTestMillisFromApi != null && finalTestDisplayFromApi != null) {
//                        viewModel.setSelectedTestDateMillis(finalTestMillisFromApi);
//                        testDateEditText.setText(finalTestDisplayFromApi);
//                    }
//
//                    if (!userChangedYear && finalYearFromApi != null) {
//                        year = finalYearFromApi;
//                        yearDropdown.setText(String.valueOf(year), false);
//                    }
//
//                    if (!userChangedModel && finalManufacturerFromApi != null && finalManufacturerFromApi != CarModel.UNKNOWN) {
//                        carModel = finalManufacturerFromApi;
//                        manufacturerDropdown.setText(carModel.name(), false);
//                        updateModelDropdownAdapter(carModel);
//                    }
//
//                    if (!userChangedCarSpecificModel && modelDropdown != null
//                            && finalSpecificModelNameFromApi != null && !finalSpecificModelNameFromApi.isEmpty()) {
//
//                        CarModel effectiveManufacturer =
//                                (!userChangedModel && finalManufacturerFromApi != null && finalManufacturerFromApi != CarModel.UNKNOWN)
//                                        ? finalManufacturerFromApi
//                                        : finalManufacturerContext;
//
//                        updateModelDropdownAdapter(effectiveManufacturer);
//
//                        selectedModelName = finalSpecificModelNameFromApi;
//                        modelDropdown.setText(selectedModelName, false);
//                    }
//                });
//
//            } catch (Exception e) {
//                Log.e("SignUp", "fetchCarInfoFromGov failed", e);
//            } finally {
//                if (conn != null) conn.disconnect();
//            }
//        });
//    }
//
//    // =========================
//    // Model dropdown helpers
//    // =========================
//    private void updateModelDropdownAdapter(CarModel manufacturer) {
//        if (modelDropdown == null) return;
//
//        Enum<?>[] models = CarModel.getModelsFor(manufacturer);
//
//        ArrayList<String> modelNames = new ArrayList<>();
//        if (models != null) {
//            for (Enum<?> m : models) {
//                if (m == null) continue;
//                if ("UNKNOWN".equalsIgnoreCase(m.name())) continue;
//                modelNames.add(m.name());
//            }
//        }
//
//        ArrayAdapter<String> modelAdapter =
//                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, modelNames);
//        modelDropdown.setAdapter(modelAdapter);
//    }
//
//    private String inferSpecificModelName(CarModel manufacturer, String... candidates) {
//        Enum<?>[] models = CarModel.getModelsFor(manufacturer);
//        if (models == null || models.length == 0) return null;
//
//        for (String cand : candidates) {
//            if (cand == null) continue;
//            String key = normalize(cand);
//            if (key.isEmpty()) continue;
//
//            for (Enum<?> m : models) {
//                if (m == null) continue;
//                if ("UNKNOWN".equalsIgnoreCase(m.name())) continue;
//
//                String enumKey = normalize(m.name());
//                if (enumKey.equals(key) || key.contains(enumKey) || enumKey.contains(key)) {
//                    return m.name();
//                }
//            }
//        }
//        return null;
//    }
//
//    private String normalize(String s) {
//        return s.trim().toUpperCase(Locale.ROOT).replaceAll("[^0-9A-Zא-ת]+", "");
//    }
//}
package com.example.drive_kit.View;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.drive_kit.Model.CarModel;
import com.example.drive_kit.R;
import com.example.drive_kit.ViewModel.SignUpViewModel;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.Locale;

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

    private MaterialAutoCompleteTextView yearDropdown;
    private int year;

    private MaterialAutoCompleteTextView manufacturerDropdown;
    private MaterialAutoCompleteTextView modelDropdown;
    private String selectedModelName = null;

    // Date input fields
    private TextInputEditText insuranceDateEditText;
    private TextInputEditText testDateEditText;
    private TextInputEditText treatmentDateEditText;

    private SignUpViewModel viewModel;

    private boolean userChangedTestDate = false;
    private boolean userChangedModel = false; // manufacturer
    private boolean userChangedYear = false;
    private boolean userChangedCarSpecificModel = false; // specific model dropdown

    // =========================
    // Car photo (Uri בלבד)
    // =========================
    private TextInputLayout carPhotoLayout;
    private TextInputEditText carPhotoEditText;

    private String selectedCarPhotoUriString = null;
    private ActivityResultLauncher<String> pickCarPhotoLauncher;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        next = findViewById(R.id.next);
        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        carNumberEditText = findViewById(R.id.carNumberEditText);

        insuranceDateEditText = findViewById(R.id.insuranceDateEditText);
        testDateEditText = findViewById(R.id.testDateEditText);
        treatmentDateEditText = findViewById(R.id.service10kDateEditText);

        canNotRemember2Month = findViewById(R.id.btnService10kUpTo2Months);
        canNotRemember4Month = findViewById(R.id.btnService10kUpTo4Months);
        canNotRemember6Month = findViewById(R.id.btnService10kUpTo6MonthsPlus);

        manufacturerDropdown = findViewById(R.id.manufacturerDropdown);
        yearDropdown = findViewById(R.id.yearDropdown);

        // IMPORTANT: this id must exist in your updated XML
        modelDropdown = findViewById(R.id.modelDropdown);

        // Car photo (ids must exist in XML)
        carPhotoLayout = findViewById(R.id.carPhotoLayout);
        carPhotoEditText = findViewById(R.id.carPhotoEditText);

        // =========================
        // Register Gallery Picker (Uri בלבד)
        // =========================
        pickCarPhotoLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri == null) return;

                    selectedCarPhotoUriString = uri.toString();

                    if (carPhotoEditText != null) {
                        carPhotoEditText.setText("נבחרה תמונה");
                        carPhotoEditText.setError(null);
                    }
                    if (carPhotoLayout != null) {
                        carPhotoLayout.setError(null);
                    }
                }
        );

        if (carPhotoEditText != null) {
            carPhotoEditText.setOnClickListener(v -> pickCarPhotoLauncher.launch("image/*"));
        }
        if (carPhotoLayout != null) {
            carPhotoLayout.setEndIconOnClickListener(v -> pickCarPhotoLauncher.launch("image/*"));
        }

        // ===== Year dropdown =====
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        ArrayList<String> years = new ArrayList<>();
        for (int y = currentYear; y >= 1980; y--) years.add(String.valueOf(y));

        ArrayAdapter<String> yearAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, years);
        yearDropdown.setAdapter(yearAdapter);

        year = currentYear;
        yearDropdown.setText(String.valueOf(currentYear), false);
        yearDropdown.setOnClickListener(v -> yearDropdown.showDropDown());

        yearDropdown.setOnItemClickListener((parent, view, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);
            year = Integer.parseInt(selected);
            userChangedYear = true;
        });

        // ===== Manufacturer dropdown =====
        LinkedHashSet<String> unique = new LinkedHashSet<>();
        for (CarModel c : CarModel.values()) unique.add(c.name());

        ArrayList<String> manufacturerItems = new ArrayList<>(unique);
        ArrayAdapter<String> manufacturerAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, manufacturerItems);
        manufacturerDropdown.setAdapter(manufacturerAdapter);

        carModel = CarModel.UNKNOWN;

        manufacturerDropdown.setOnClickListener(v -> manufacturerDropdown.showDropDown());

        manufacturerDropdown.setOnItemClickListener((parent, view, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);
            carModel = CarModel.valueOf(selected);
            userChangedModel = true;

            // update model dropdown list
            updateModelDropdownAdapter(carModel);

            // reset specific model selection (manufacturer changed)
            selectedModelName = null;
            userChangedCarSpecificModel = false;
            if (modelDropdown != null) modelDropdown.setText("", false);
        });

        // ===== Specific model dropdown =====
        updateModelDropdownAdapter(carModel);
        if (modelDropdown != null) {
            modelDropdown.setOnClickListener(v -> modelDropdown.showDropDown());
            modelDropdown.setOnItemClickListener((parent, view, position, id) -> {
                String selected = (String) parent.getItemAtPosition(position);
                selectedModelName = selected;
                userChangedCarSpecificModel = true;
            });
        }

        // ===== Debug =====
        if (insuranceDateEditText == null) Log.e("SignUp", "insuranceDateEditText is NULL (check XML id!)");
        if (testDateEditText == null) Log.e("SignUp", "testDateEditText is NULL (check XML id!)");
        if (treatmentDateEditText == null) Log.e("SignUp", "treatmentEditText is NULL (check XML id!)");
        if (modelDropdown == null) Log.e("SignUp", "modelDropdown is NULL (check XML id modelDropdown!)");

        // ===== ViewModel =====
        viewModel = new ViewModelProvider(this).get(SignUpViewModel.class);

        viewModel.getInsuranceDateError().observe(this, err -> {
            if (err != null) insuranceDateEditText.setError(err);
        });

        viewModel.getTestDateError().observe(this, err -> {
            if (err != null) testDateEditText.setError(err);
        });

        viewModel.getTreatDateError().observe(this, err -> {
            if (err != null) treatmentDateEditText.setError(err);
        });

        // ===== Car number -> fetch gov data if needed =====
        carNumberEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String carNumber = carNumberEditText.getText().toString().trim();
                if (carNumber.isEmpty()) return;

                if (!userChangedTestDate || !userChangedModel || !userChangedYear || !userChangedCarSpecificModel) {
                    fetchCarInfoFromGov(carNumber);
                }
            }
        });

        // ===== Date pickers =====
        insuranceDateEditText.setOnClickListener(v -> {
            carNumberEditText.clearFocus();
            triggerCarLookupIfNeeded();
            openDatePickerInsurance();
        });

        testDateEditText.setOnClickListener(v -> {
            carNumberEditText.clearFocus();
            triggerCarLookupIfNeeded();
            openDatePickerTest();
        });

        treatmentDateEditText.setOnClickListener(v -> {
            carNumberEditText.clearFocus();
            triggerCarLookupIfNeeded();
            openDatePickerTreat();
        });

        // ===== "Can't remember" buttons =====
        canNotRemember2Month.setOnClickListener(v -> setTreatByMonthsBack(2));
        canNotRemember4Month.setOnClickListener(v -> setTreatByMonthsBack(4));
        canNotRemember6Month.setOnClickListener(v -> setTreatByMonthsBack(6));

        // ===== Next =====
        next.setOnClickListener(v -> {

            // 1) Validate dates first (your existing logic)
            if (!viewModel.validateDates()) return;

            // 2) Read inputs
            String firstName = firstNameEditText.getText().toString().trim();
            String lastName  = lastNameEditText.getText().toString().trim();
            String email     = emailEditText.getText().toString().trim();
            String carNumber = carNumberEditText.getText().toString().trim();
            String phone     = phoneEditText.getText().toString().trim();

            // 3) Validate all required fields
            boolean missingText =
                    firstName.isEmpty() ||
                            lastName.isEmpty()  ||
                            email.isEmpty()     ||
                            phone.isEmpty()     ||
                            carNumber.isEmpty();

            boolean missingDropdowns =
                    carModel == null || carModel == CarModel.UNKNOWN ||
                            year <= 0 ||
                            selectedModelName == null || selectedModelName.trim().isEmpty();

            if (missingText || missingDropdowns) {
                android.widget.Toast.makeText(
                        SignUpActivity.this,
                        "נא למלא את כל השדות",
                        android.widget.Toast.LENGTH_SHORT
                ).show();
                return;
            }

            // 4) Continue as usual
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
            intent.putExtra("carSpecificModel", selectedModelName);

            // ✅ שינוי יחיד: מעבירים Uri string (לא Base64)
            if (selectedCarPhotoUriString != null && !selectedCarPhotoUriString.trim().isEmpty()) {
                intent.putExtra("carPhotoUri", selectedCarPhotoUriString.trim());
            }

            startActivity(intent);
        });
    }

    private void setTreatByMonthsBack(int months) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -months);
        long millis = cal.getTimeInMillis();

        viewModel.setSelectedTreatDateMillis(millis);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        treatmentDateEditText.setText(sdf.format(cal.getTime()));
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
            userChangedTestDate = true;
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

    private void triggerCarLookupIfNeeded() {
        String carNumber = carNumberEditText.getText().toString().trim();
        if (carNumber.isEmpty()) return;

        if (!userChangedTestDate || !userChangedModel || !userChangedYear || !userChangedCarSpecificModel) {
            fetchCarInfoFromGov(carNumber);
        }
    }

    private void fetchCarInfoFromGov(String carNumber) {
        final String urlStr =
                "https://data.gov.il/api/3/action/datastore_search" +
                        "?resource_id=053cea08-09bc-40ec-8f7a-156f0677aff3" +
                        "&q=" + carNumber +
                        "&limit=1";

        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            HttpURLConnection conn = null;

            try {
                URL url = new URL(urlStr);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                int code = conn.getResponseCode();
                if (code != 200) {
                    Log.e("SignUp", "HTTP error: " + code);
                    return;
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                JSONObject root = new JSONObject(sb.toString());
                if (!root.optBoolean("success", false)) return;

                JSONObject result = root.optJSONObject("result");
                if (result == null) return;

                JSONArray records = result.optJSONArray("records");
                if (records == null || records.length() == 0) return;

                JSONObject rec0 = records.getJSONObject(0);

                Long testMillisFromApi = null;
                String testDisplayFromApi = null;

                String tokefDt = rec0.optString("tokef_dt", "");
                if (!tokefDt.isEmpty()) {
                    SimpleDateFormat apiFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    java.util.Date tokefDate = apiFmt.parse(tokefDt);
                    if (tokefDate != null) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(tokefDate);
                        cal.add(Calendar.YEAR, -1);

                        testMillisFromApi = cal.getTimeInMillis();
                        SimpleDateFormat outFmt = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        testDisplayFromApi = outFmt.format(cal.getTime());
                    }
                }

                Integer yearFromApi = null;
                if (rec0.has("shnat_yitzur")) {
                    int y = rec0.optInt("shnat_yitzur", -1);
                    if (y > 0) yearFromApi = y;
                }

                String tozeretNm = rec0.optString("tozeret_nm", "");
                CarModel manufacturerFromApi = CarModel.fromGovValue(tozeretNm);

                String kinuyMishari = rec0.optString("kinuy_mishari", "");
                String degemNm = rec0.optString("degem_nm", "");

                CarModel manufacturerContext =
                        (manufacturerFromApi != null && manufacturerFromApi != CarModel.UNKNOWN)
                                ? manufacturerFromApi
                                : carModel;

                String specificModelNameFromApi =
                        inferSpecificModelName(manufacturerContext, kinuyMishari, degemNm);

                Long finalTestMillisFromApi = testMillisFromApi;
                String finalTestDisplayFromApi = testDisplayFromApi;
                Integer finalYearFromApi = yearFromApi;
                CarModel finalManufacturerFromApi = manufacturerFromApi;
                String finalSpecificModelNameFromApi = specificModelNameFromApi;
                CarModel finalManufacturerContext = manufacturerContext;

                runOnUiThread(() -> {

                    if (!userChangedTestDate && finalTestMillisFromApi != null && finalTestDisplayFromApi != null) {
                        viewModel.setSelectedTestDateMillis(finalTestMillisFromApi);
                        testDateEditText.setText(finalTestDisplayFromApi);
                    }

                    if (!userChangedYear && finalYearFromApi != null) {
                        year = finalYearFromApi;
                        yearDropdown.setText(String.valueOf(year), false);
                    }

                    if (!userChangedModel && finalManufacturerFromApi != null && finalManufacturerFromApi != CarModel.UNKNOWN) {
                        carModel = finalManufacturerFromApi;
                        manufacturerDropdown.setText(carModel.name(), false);

                        updateModelDropdownAdapter(carModel);
                    }

                    if (!userChangedCarSpecificModel && modelDropdown != null
                            && finalSpecificModelNameFromApi != null && !finalSpecificModelNameFromApi.isEmpty()) {

                        CarModel effectiveManufacturer =
                                (!userChangedModel && finalManufacturerFromApi != null && finalManufacturerFromApi != CarModel.UNKNOWN)
                                        ? finalManufacturerFromApi
                                        : finalManufacturerContext;

                        updateModelDropdownAdapter(effectiveManufacturer);

                        selectedModelName = finalSpecificModelNameFromApi;
                        modelDropdown.setText(selectedModelName, false);
                    }
                });

            } catch (Exception e) {
                Log.e("SignUp", "fetchCarInfoFromGov failed", e);
            } finally {
                if (conn != null) conn.disconnect();
            }
        });
    }

    private void updateModelDropdownAdapter(CarModel manufacturer) {
        if (modelDropdown == null) return;

        Enum<?>[] models = CarModel.getModelsFor(manufacturer);

        ArrayList<String> modelNames = new ArrayList<>();
        if (models != null) {
            for (Enum<?> m : models) {
                if (m == null) continue;
                if ("UNKNOWN".equalsIgnoreCase(m.name())) continue;
                modelNames.add(m.name());
            }
        }

        ArrayAdapter<String> modelAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, modelNames);
        modelDropdown.setAdapter(modelAdapter);
    }

    private String inferSpecificModelName(CarModel manufacturer, String... candidates) {
        Enum<?>[] models = CarModel.getModelsFor(manufacturer);
        if (models == null || models.length == 0) return null;

        for (String cand : candidates) {
            if (cand == null) continue;
            String key = normalize(cand);
            if (key.isEmpty()) continue;

            for (Enum<?> m : models) {
                if (m == null) continue;
                if ("UNKNOWN".equalsIgnoreCase(m.name())) continue;

                String enumKey = normalize(m.name());
                if (enumKey.equals(key) || key.contains(enumKey) || enumKey.contains(key)) {
                    return m.name();
                }
            }
        }
        return null;
    }

    private String normalize(String s) {
        return s.trim().toUpperCase(Locale.ROOT).replaceAll("[^0-9A-Zא-ת]+", "");
    }
}
