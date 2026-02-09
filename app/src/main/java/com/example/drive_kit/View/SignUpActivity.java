package com.example.drive_kit.View;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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

/**
 * SignUpActivity
 *
 * Responsibilities:
 * 1) Collect shared signup fields for both roles (driver / insurance).
 * 2) Show role-specific UI sections:
 *    - Driver: car details, dates, optional car photo.
 *    - Insurance: insurance company selection from Firestore.
 * 3) Auto-fill driver fields from Israeli gov vehicle API using car number.
 * 4) Validate required inputs and navigate to SetUsernamePasswordActivity.
 *
 * Data flow:
 * - This screen gathers profile/base data.
 * - Password is collected in SetUsernamePasswordActivity.
 *
 * IMPORTANT:
 * Requires these IDs in signup.xml:
 * - insuranceCompanyLayout
 * - insuranceCompanyDropdown
 * - carPhotoLayout / carPhotoEditText
 */
public class SignUpActivity extends AppCompatActivity {

    // Next button (navigates to password step)
    private Button next;

    // "Can't remember" quick-fill buttons for 10K service date
    private Button canNotRemember2Month;
    private Button canNotRemember4Month;
    private Button canNotRemember6Month;

    // Role controls
    private android.widget.RadioGroup roleGroup;
    private android.widget.RadioButton radioDriver, radioInsurance;

    // Shared/common inputs
    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText emailEditText;
    private EditText phoneEditText;

    // Driver-specific fields
    private EditText carNumberEditText;
    private CarModel carModel;

    // Driver car year
    private MaterialAutoCompleteTextView yearDropdown;
    private int year;

    // Driver manufacturer + specific model
    private MaterialAutoCompleteTextView manufacturerDropdown;
    private MaterialAutoCompleteTextView modelDropdown;
    private String selectedModelName = null;

    // Driver date fields
    private TextInputEditText insuranceDateEditText;
    private TextInputEditText testDateEditText;
    private TextInputEditText treatmentDateEditText;

    // Driver-only layout wrappers (shown/hidden by role)
    private View carNumberLayout, manufacturerLayout, modelLayout, yearLayout;
    private View insuranceDateLayout, testDateLayout, service10kDateLayout;
    private View notrememberView, service10kRangeButtonsScrollView;

    // Optional car photo picker UI
    private TextInputLayout carPhotoLayout;
    private TextInputEditText carPhotoEditText;

    // Stores selected local URI as String (passed to next screen)
    private String selectedCarPhotoUriString = null;
    private ActivityResultLauncher<String> pickCarPhotoLauncher;

    // Insurance-only company selector
    private View insuranceCompanyLayout;
    private MaterialAutoCompleteTextView insuranceCompanyDropdown;
    private String selectedCompanyId = null;

    // Dropdown mapping arrays:
    // display name list + parallel company doc IDs
    private final ArrayList<String> insuranceCompanyIds = new ArrayList<>();
    private final ArrayList<String> insuranceCompanyNames = new ArrayList<>();

    // ViewModel for date validation/state
    private SignUpViewModel viewModel;

    // Flags that track whether user manually changed auto-fillable fields
    private boolean userChangedTestDate = false;
    private boolean userChangedModel = false;          // manufacturer
    private boolean userChangedYear = false;
    private boolean userChangedCarSpecificModel = false;
    private TextInputEditText insuranceCompanyIdEditText;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);
        //Log.e("SIGNUP_ENTER", "=== SignUpActivity onCreate ENTERED ===");





        // ---------- Views ----------
        // Main action button
        next = findViewById(R.id.next);

        // Shared/common fields
        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText  = findViewById(R.id.lastNameEditText);
        emailEditText     = findViewById(R.id.emailEditText);
        phoneEditText     = findViewById(R.id.phoneEditText);

        // Role toggle
        roleGroup     = findViewById(R.id.roleGroup);
        radioDriver   = findViewById(R.id.radioDriver);
        radioInsurance= findViewById(R.id.radioInsurance);

        // Driver fields
        carNumberEditText     = findViewById(R.id.carNumberEditText);
        insuranceDateEditText = findViewById(R.id.insuranceDateEditText);
        testDateEditText      = findViewById(R.id.testDateEditText);
        treatmentDateEditText = findViewById(R.id.service10kDateEditText);

        // Quick buttons for unknown 10K date
        canNotRemember2Month = findViewById(R.id.btnService10kUpTo2Months);
        canNotRemember4Month = findViewById(R.id.btnService10kUpTo4Months);
        canNotRemember6Month = findViewById(R.id.btnService10kUpTo6MonthsPlus);
        insuranceCompanyIdEditText = findViewById(R.id.insuranceCompanyIdEditText);


        // Driver dropdowns
        manufacturerDropdown = findViewById(R.id.manufacturerDropdown);
        yearDropdown         = findViewById(R.id.yearDropdown);
        modelDropdown        = findViewById(R.id.modelDropdown);

        // Driver wrappers for show/hide by role
        carNumberLayout = findViewById(R.id.carNumberLayout);
        manufacturerLayout = findViewById(R.id.manufacturerLayout);
        modelLayout = findViewById(R.id.modelLayout);
        yearLayout = findViewById(R.id.yearLayout);
        insuranceDateLayout = findViewById(R.id.insuranceDateLayout);
        testDateLayout = findViewById(R.id.testDateLayout);
        service10kDateLayout = findViewById(R.id.service10kDateLayout);
        notrememberView = findViewById(R.id.notremember);
        service10kRangeButtonsScrollView = findViewById(R.id.service10kRangeButtonsScroll);

        // Car photo inputs
        carPhotoLayout = findViewById(R.id.carPhotoLayout);
        carPhotoEditText = findViewById(R.id.carPhotoEditText);

        // Insurance dropdown (must exist in XML)
        insuranceCompanyLayout = findViewById(R.id.insuranceCompanyLayout);
        insuranceCompanyDropdown = findViewById(R.id.insuranceCompanyDropdown);

        // Sanity logs for null-checking key views
        Log.d("SIGNUP", "manufacturerDropdown=" + (manufacturerDropdown != null));
        Log.d("SIGNUP", "modelDropdown=" + (modelDropdown != null));
        Log.d("SIGNUP", "yearDropdown=" + (yearDropdown != null));
        Log.d("SIGNUP", "insuranceCompanyDropdown=" + (insuranceCompanyDropdown != null));


        // ---------- Gallery picker ----------
        // Registers content picker for image selection ("image/*")
        pickCarPhotoLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri == null) return;

                    selectedCarPhotoUriString = uri.toString();

                    // Update UI state after image selection
                    if (carPhotoEditText != null) {
                        carPhotoEditText.setText("נבחרה תמונה");
                        carPhotoEditText.setError(null);
                    }
                    if (carPhotoLayout != null) {
                        carPhotoLayout.setError(null);
                    }
                }
        );

        // Open gallery when user clicks text field
        if (carPhotoEditText != null) {
            carPhotoEditText.setOnClickListener(v -> pickCarPhotoLauncher.launch("image/*"));
        }
        if (carPhotoLayout != null) {
            // Open gallery when end icon is clicked
            carPhotoLayout.setEndIconOnClickListener(v -> pickCarPhotoLauncher.launch("image/*"));
        }

        // ---------- ViewModel ----------
        // ViewModel manages date validation and selected date millis
        viewModel = new ViewModelProvider(this).get(SignUpViewModel.class);

        // Observe date validation errors and show them inline
        viewModel.getInsuranceDateError().observe(this, err -> {
            if (err != null && insuranceDateEditText != null) insuranceDateEditText.setError(err);
        });
        viewModel.getTestDateError().observe(this, err -> {
            if (err != null && testDateEditText != null) testDateEditText.setError(err);
        });
        viewModel.getTreatDateError().observe(this, err -> {
            if (err != null && treatmentDateEditText != null) treatmentDateEditText.setError(err);
        });

        // ---------- Role behavior ----------
        // Default role = driver
        if (radioDriver != null) radioDriver.setChecked(true);
        applyRoleUi(true);

        // Toggle visible sections when role changes
        if (roleGroup != null) {
            roleGroup.setOnCheckedChangeListener((group, checkedId) -> {
                boolean isDriver = (checkedId == R.id.radioDriver);
                applyRoleUi(isDriver);
            });
        }

        // ---------- Insurance companies dropdown ----------
        // Load insurance company list from Firestore
        setupInsuranceCompaniesDropdown();

        // ---------- Year dropdown ----------
        // Initialize year dropdown from current year back to 1980
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        ArrayList<String> years = new ArrayList<>();
        for (int y = currentYear; y >= 1980; y--) years.add(String.valueOf(y));

        ArrayAdapter<String> yearAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, years);
        yearDropdown.setAdapter(yearAdapter);

        // Default selected year = current year
        year = currentYear;
        yearDropdown.setText(String.valueOf(currentYear), false);
        yearDropdown.setOnClickListener(v -> yearDropdown.showDropDown());
        yearDropdown.setOnItemClickListener((parent, view, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);
            year = Integer.parseInt(selected);
            userChangedYear = true;
        });

        // ---------- Manufacturer dropdown ----------
        // Build manufacturer list from CarModel enum values
        LinkedHashSet<String> unique = new LinkedHashSet<>();
        for (CarModel c : CarModel.values()) unique.add(c.name());

        ArrayList<String> manufacturerItems = new ArrayList<>(unique);
        ArrayAdapter<String> manufacturerAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, manufacturerItems);
        manufacturerDropdown.setAdapter(manufacturerAdapter);

        // Default manufacturer
        carModel = CarModel.UNKNOWN;

        manufacturerDropdown.setOnClickListener(v -> manufacturerDropdown.showDropDown());
        manufacturerDropdown.setOnItemClickListener((parent, view, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);
            carModel = CarModel.valueOf(selected);
            userChangedModel = true;

            // Refresh specific-model dropdown when manufacturer changes
            updateModelDropdownAdapter(carModel);

            // Reset specific model selection after manufacturer change
            selectedModelName = null;
            userChangedCarSpecificModel = false;
            if (modelDropdown != null) modelDropdown.setText("", false);
        });

        // ---------- Specific model dropdown ----------
        // Initialize model dropdown for default manufacturer
        updateModelDropdownAdapter(carModel);
        if (modelDropdown != null) {
            modelDropdown.setOnClickListener(v -> modelDropdown.showDropDown());
            modelDropdown.setOnItemClickListener((parent, view, position, id) -> {
                String selected = (String) parent.getItemAtPosition(position);
                selectedModelName = selected;
                userChangedCarSpecificModel = true;
            });
        }

        // ---------- Debug ----------
        // Helpful null check log for XML binding issues
        if (insuranceCompanyDropdown == null) Log.e("SignUp", "insuranceCompanyDropdown is NULL (check XML ids!)");

        // ---------- Car number focus (driver only) ----------
        // When car number loses focus, optionally trigger API lookup for auto-fill
        carNumberEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                if (!isDriverSelected()) return;

                String carNumber = safeText(carNumberEditText);
                if (carNumber.isEmpty()) return;

                // Only auto-fill fields that user didn't manually override
                if (!userChangedTestDate || !userChangedModel || !userChangedYear || !userChangedCarSpecificModel) {
                    fetchCarInfoFromGov(carNumber);
                }
            }
        });

        // ---------- Date pickers (driver only) ----------
        // Insurance date picker
        insuranceDateEditText.setOnClickListener(v -> {
            if (!isDriverSelected()) return;
            carNumberEditText.clearFocus();
            triggerCarLookupIfNeeded();
            openDatePickerInsurance();
        });

        // Test date picker
        testDateEditText.setOnClickListener(v -> {
            if (!isDriverSelected()) return;
            carNumberEditText.clearFocus();
            triggerCarLookupIfNeeded();
            openDatePickerTest();
        });

        // 10K treatment date picker
        treatmentDateEditText.setOnClickListener(v -> {
            if (!isDriverSelected()) return;
            carNumberEditText.clearFocus();
            triggerCarLookupIfNeeded();
            openDatePickerTreat();
        });

        // ---------- Can't remember buttons ----------
        // Quick-set treatment date relative to today
        canNotRemember2Month.setOnClickListener(v -> {
            if (!isDriverSelected()) return;
            setTreatByMonthsBack(2);
        });

        canNotRemember4Month.setOnClickListener(v -> {
            if (!isDriverSelected()) return;
            setTreatByMonthsBack(4);
        });

        canNotRemember6Month.setOnClickListener(v -> {
            if (!isDriverSelected()) return;
            setTreatByMonthsBack(6);
        });

        // ---------- Next ----------
        next.setOnClickListener(v -> {
            boolean isDriver = isDriverSelected();

            // 1) Common inputs
            String firstName = safeText(firstNameEditText);
            String lastName  = safeText(lastNameEditText);
            String email     = safeText(emailEditText);
            String phone     = safeText(phoneEditText);

            // 2) Validate common
            if (  email.isEmpty() || phone.isEmpty()) {
                android.widget.Toast.makeText(
                        SignUpActivity.this,
                        "נא למלא את כל השדות הבסיסיים",
                        android.widget.Toast.LENGTH_SHORT
                ).show();
                return;
            }

            // 3) Insurance extra validation
            // Resolve selectedCompanyId from displayed dropdown text if needed
            if (!isDriver) {
                String chosen = (insuranceCompanyDropdown == null) ? "" :
                        insuranceCompanyDropdown.getText().toString().trim();

                if ((selectedCompanyId == null || selectedCompanyId.trim().isEmpty()) && !chosen.isEmpty()) {
                    int idx = insuranceCompanyNames.indexOf(chosen);
                    if (idx >= 0 && idx < insuranceCompanyIds.size()) {
                        selectedCompanyId = insuranceCompanyIds.get(idx);
                    }
                }
            }


            // 4) Build intent
            // Navigate to password step and pass collected payload
            Intent intent = new Intent(SignUpActivity.this, SetUsernamePasswordActivity.class);
            intent.putExtra("role", isDriver ? "driver" : "insurance");
            intent.putExtra("isInsurance", !isDriver);

            intent.putExtra("firstName", firstName);
            intent.putExtra("lastName", lastName);
            intent.putExtra("email", email);
            intent.putExtra("phone", phone);

            if (!isDriver) {
                intent.putExtra("insuranceCompanyId", selectedCompanyId);
            }

            // 5) Driver extras
            if (isDriver) {
                // Validate date fields in ViewModel
                if (!viewModel.validateDates()) return;

                String carNumber = safeText(carNumberEditText);

                // Validate required driver fields
                boolean missingDriverText = carNumber.isEmpty();
                boolean missingDriverDropdowns =
                        carModel == null || carModel == CarModel.UNKNOWN ||
                                year <= 0 ||
                                selectedModelName == null || selectedModelName.trim().isEmpty();

                if (missingDriverText || missingDriverDropdowns) {
                    android.widget.Toast.makeText(
                            SignUpActivity.this,
                            "לנהג חובה למלא פרטי רכב",
                            android.widget.Toast.LENGTH_SHORT
                    ).show();
                    return;
                }

                // Attach driver payload
                intent.putExtra("carNumber", carNumber);
                intent.putExtra("insuranceDateMillis", viewModel.getSelectedInsuranceDateMillis());
                intent.putExtra("testDateMillis", viewModel.getSelectedTestDateMillis());
                intent.putExtra("treatmentDateMillis", viewModel.getSelectedTreatDateMillis());
                intent.putExtra("carModel", carModel);
                intent.putExtra("year", year);
                intent.putExtra("carSpecificModel", selectedModelName);

                // Attach optional local photo URI
                if (selectedCarPhotoUriString != null && !selectedCarPhotoUriString.trim().isEmpty()) {
                    intent.putExtra("carPhotoUri", selectedCarPhotoUriString.trim());
                }
            }

            // Continue to next step
            startActivity(intent);
        });
    }

    // =========================
    // Insurance companies dropdown from Firestore
    // =========================
    private void setupInsuranceCompaniesDropdown() {
        if (insuranceCompanyDropdown == null) return;

        // Open dropdown on click
        insuranceCompanyDropdown.setOnClickListener(v -> insuranceCompanyDropdown.showDropDown());

        FirebaseFirestore.getInstance()
                .collection("insurance_companies")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    insuranceCompanyIds.clear();
                    insuranceCompanyNames.clear();

                    // Build display list and id mapping
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String docId = doc.getId();
                        String name = doc.getString("name");

                        if (name == null || name.trim().isEmpty()) name = docId;


                        String display = name + " (" + docId + ")";

                        insuranceCompanyIds.add(docId);
                        insuranceCompanyNames.add(display);
                    }

                    ArrayAdapter<String> adapter =
                            new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, insuranceCompanyNames);
                    insuranceCompanyDropdown.setAdapter(adapter);

                    // Save selected company doc ID
                    insuranceCompanyDropdown.setOnItemClickListener((parent, view, position, id) -> {
                        if (position >= 0 && position < insuranceCompanyIds.size()) {
                            selectedCompanyId = insuranceCompanyIds.get(position);
                            if (insuranceCompanyIdEditText != null) {
                                insuranceCompanyIdEditText.setText(selectedCompanyId); // docId מהדאטה בייס
                            }
                            // Auto-load company profile fields into common inputs
                            loadInsuranceCompanyDetails(selectedCompanyId);
                        }
                    });
                })
                .addOnFailureListener(e -> Log.e("SignUp", "Failed to load insurance_companies", e));
    }

    // Returns true if current selected role is driver
    private boolean isDriverSelected() {
        return radioDriver != null && radioDriver.isChecked();
    }

    // Shows/hides role-specific UI groups
    private void applyRoleUi(boolean isDriver) {
        int vis = isDriver ? View.VISIBLE : View.GONE;

        if (carNumberLayout != null) carNumberLayout.setVisibility(vis);
        if (manufacturerLayout != null) manufacturerLayout.setVisibility(vis);
        if (modelLayout != null) modelLayout.setVisibility(vis);
        if (yearLayout != null) yearLayout.setVisibility(vis);
        if (carPhotoLayout != null) carPhotoLayout.setVisibility(vis);
        if (insuranceDateLayout != null) insuranceDateLayout.setVisibility(vis);
        if (testDateLayout != null) testDateLayout.setVisibility(vis);
        if (service10kDateLayout != null) service10kDateLayout.setVisibility(vis);
        if (notrememberView != null) notrememberView.setVisibility(vis);
        if (service10kRangeButtonsScrollView != null) service10kRangeButtonsScrollView.setVisibility(vis);


        if (insuranceCompanyLayout != null)
            insuranceCompanyLayout.setVisibility(isDriver ? View.GONE : View.VISIBLE);
    }

    // Helper: sets treatment date to "today - N months"
    private void setTreatByMonthsBack(int months) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -months);
        long millis = cal.getTimeInMillis();

        viewModel.setSelectedTreatDateMillis(millis);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        treatmentDateEditText.setText(sdf.format(cal.getTime()));
    }

    // Opens insurance date picker dialog
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

    // Opens test date picker dialog
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

    // Opens 10K treatment date picker dialog
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

    // Triggers gov API lookup if user has not manually overridden all relevant fields
    private void triggerCarLookupIfNeeded() {
        if (!isDriverSelected()) return;

        String carNumber = safeText(carNumberEditText);
        if (carNumber.isEmpty()) return;

        if (!userChangedTestDate || !userChangedModel || !userChangedYear || !userChangedCarSpecificModel) {
            fetchCarInfoFromGov(carNumber);
        }
    }

    // Calls data.gov.il API and attempts to auto-fill:
    // - test date (derived from tokef_dt - 1 year)
    // - year
    // - manufacturer
    // - specific model
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

                // Government field tokef_dt seems to represent validity date.
                // App logic sets test date to one year before tokef_dt.
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

                // Manufacturer + specific model inference candidates from API fields
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

                // Capture final values for UI thread
                Long finalTestMillisFromApi = testMillisFromApi;
                String finalTestDisplayFromApi = testDisplayFromApi;
                Integer finalYearFromApi = yearFromApi;
                CarModel finalManufacturerFromApi = manufacturerFromApi;
                String finalSpecificModelNameFromApi = specificModelNameFromApi;
                CarModel finalManufacturerContext = manufacturerContext;

                // Apply updates on main thread (and only for fields user didn't manually change)
                runOnUiThread(() -> {
                    if (!isDriverSelected()) return;

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

    // Loads selected insurance company details and pre-fills common fields
    // (email, phone, name) for insurance role.
    private void loadInsuranceCompanyDetails(String companyId) {
        if (companyId == null || companyId.trim().isEmpty()) return;
        if (isDriverSelected()) return; // רק לחברת ביטוח

        FirebaseFirestore.getInstance()
                .collection("insurance_companies")
                .document(companyId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    String email = doc.getString("email"); // בדיוק כמו בתמונה
                    String phone = doc.getString("phone");
                    String name  = doc.getString("name");


                    if (email != null && !email.trim().isEmpty()) emailEditText.setText(email.trim());
                    else emailEditText.setText("");


                    if (phone != null && !phone.trim().isEmpty()) phoneEditText.setText(phone.trim());
                    else phoneEditText.setText("");


                    if (name != null && !name.trim().isEmpty()) firstNameEditText.setText(name.trim());
                    else firstNameEditText.setText("");
                })
                .addOnFailureListener(e -> Log.e("SignUp", "Failed to load company details", e));
    }


    // Refreshes model dropdown according to selected manufacturer enum
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

    // Best-effort mapping from free-text API model strings to enum model names
    // using normalized string equality / contains matching.
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

    // Normalizes strings for model matching:
    // - trim
    // - uppercase
    // - keep only digits/latin/hebrew letters
    private String normalize(String s) {
        return s.trim().toUpperCase(Locale.ROOT).replaceAll("[^0-9A-Zא-ת]+", "");
    }

    // Null-safe text extraction from EditText
    private String safeText(EditText et) {
        if (et == null || et.getText() == null) return "";
        return et.getText().toString().trim();
    }
}
