package com.example.drive_kit.View;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.drive_kit.Data.Repository.CompleteProfileRepository;
import com.example.drive_kit.Model.CarModel;
import com.example.drive_kit.Model.Driver;
import com.example.drive_kit.R;
import com.example.drive_kit.ViewModel.SignUpViewModel;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
 * 1) Collect common signup fields (name, email, phone) for both roles.
 * 2) Show/hide role-specific sections:
 *    - Driver: car details, maintenance/test/insurance dates, optional car photo.
 *    - Insurance: insurance company selection and optional company logo.
 * 3) Auto-fill car data from gov API based on car number (best effort).
 * 4) Validate mandatory inputs.
 * 5) Support "existing-auth user missing profile" completion flow (driver-only).
 *
 * NOTE:
 * - No email pre-check is performed here.
 * - Email existence is handled during actual sign-up in the password screen.
 */
public class SignUpActivity extends AppCompatActivity {

    private Button next;

    private TextView tvService10kDontRemember;

    private android.widget.RadioGroup roleGroup;
    private android.widget.RadioButton radioDriver, radioInsurance;

    // Flow flags: existing Auth user but missing drivers doc
    private boolean fromAuthNoDriverDoc = false;
    private String prefillEmail = "";

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

    private TextInputEditText insuranceDateEditText;
    private TextInputEditText testDateEditText;
    private TextInputEditText treatmentDateEditText;

    private View carNumberLayout, manufacturerLayout, modelLayout, yearLayout;
    private View insuranceDateLayout, testDateLayout, service10kDateLayout;

    private TextInputLayout carPhotoLayout;
    private TextInputEditText carPhotoEditText;

    private String selectedCarPhotoUriString = null;
    private ActivityResultLauncher<String> pickCarPhotoLauncher;

    private View insuranceCompanyLayout;
    private MaterialAutoCompleteTextView insuranceCompanyDropdown;
    private String selectedCompanyId = null;

    private final ArrayList<String> insuranceCompanyIds = new ArrayList<>();
    private final ArrayList<String> insuranceCompanyNames = new ArrayList<>();

    private SignUpViewModel viewModel;

    private boolean userChangedTestDate = false;
    private boolean userChangedModel = false;
    private boolean userChangedYear = false;
    private boolean userChangedCarSpecificModel = false;

    private TextInputEditText insuranceCompanyIdEditText;

    private TextInputLayout insuranceLogoLayout;
    private TextInputEditText insuranceLogoEditText;

    private String selectedInsuranceLogoUriString = null;
    private ActivityResultLauncher<String> pickInsuranceLogoLauncher;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        // Read flow extras from LoadingActivity
        fromAuthNoDriverDoc = getIntent().getBooleanExtra("fromAuthNoDriverDoc", false);
        prefillEmail = getIntent().getStringExtra("prefillEmail");
        if (prefillEmail == null) prefillEmail = "";

        insuranceLogoLayout = findViewById(R.id.insuranceLogoLayout);
        insuranceLogoEditText = findViewById(R.id.insuranceLogoEditText);

        pickInsuranceLogoLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri == null) return;

                    selectedInsuranceLogoUriString = uri.toString();

                    if (insuranceLogoEditText != null) {
                        insuranceLogoEditText.setText("נבחר לוגו");
                        insuranceLogoEditText.setError(null);
                    }
                    if (insuranceLogoLayout != null) {
                        insuranceLogoLayout.setError(null);
                    }
                }
        );

        if (insuranceLogoEditText != null) {
            insuranceLogoEditText.setOnClickListener(v -> pickInsuranceLogoLauncher.launch("image/*"));
        }
        if (insuranceLogoLayout != null) {
            insuranceLogoLayout.setEndIconOnClickListener(v -> pickInsuranceLogoLauncher.launch("image/*"));
        }

        next = findViewById(R.id.next);

        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        phoneEditText = findViewById(R.id.phoneEditText);

        roleGroup = findViewById(R.id.roleGroup);
        radioDriver = findViewById(R.id.radioDriver);
        radioInsurance = findViewById(R.id.radioInsurance);

        carNumberEditText = findViewById(R.id.carNumberEditText);
        insuranceDateEditText = findViewById(R.id.insuranceDateEditText);
        testDateEditText = findViewById(R.id.testDateEditText);
        treatmentDateEditText = findViewById(R.id.service10kDateEditText);

        tvService10kDontRemember = findViewById(R.id.tvService10kDontRemember);

        insuranceCompanyIdEditText = findViewById(R.id.insuranceCompanyIdEditText);
        if (tvService10kDontRemember != null) {
            tvService10kDontRemember.setOnClickListener(v -> showTreatRangePopup());
        }

        manufacturerDropdown = findViewById(R.id.manufacturerDropdown);
        yearDropdown = findViewById(R.id.yearDropdown);
        modelDropdown = findViewById(R.id.modelDropdown);

        carNumberLayout = findViewById(R.id.carNumberLayout);
        manufacturerLayout = findViewById(R.id.manufacturerLayout);
        modelLayout = findViewById(R.id.modelLayout);
        yearLayout = findViewById(R.id.yearLayout);
        insuranceDateLayout = findViewById(R.id.insuranceDateLayout);
        testDateLayout = findViewById(R.id.testDateLayout);
        service10kDateLayout = findViewById(R.id.service10kDateLayout);

        carPhotoLayout = findViewById(R.id.carPhotoLayout);
        carPhotoEditText = findViewById(R.id.carPhotoEditText);

        insuranceCompanyLayout = findViewById(R.id.insuranceCompanyLayout);
        insuranceCompanyDropdown = findViewById(R.id.insuranceCompanyDropdown);

        if (fromAuthNoDriverDoc) {
            if (!prefillEmail.trim().isEmpty()) {
                emailEditText.setText(prefillEmail.trim());
            }
            // Prevent changing email when user already authenticated
            emailEditText.setEnabled(false);
            emailEditText.setFocusable(false);
            emailEditText.setClickable(false);
        }

        if (tvService10kDontRemember != null) {
            tvService10kDontRemember.setOnClickListener(v -> {
                if (!isDriverSelected()) return;
                showTreatRangePopup();
            });
        }

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

        viewModel = new ViewModelProvider(this).get(SignUpViewModel.class);

        viewModel.getInsuranceDateError().observe(this, err -> {
            if (err != null && insuranceDateEditText != null) insuranceDateEditText.setError(err);
        });
        viewModel.getTestDateError().observe(this, err -> {
            if (err != null && testDateEditText != null) testDateEditText.setError(err);
        });
        viewModel.getTreatDateError().observe(this, err -> {
            if (err != null && treatmentDateEditText != null) treatmentDateEditText.setError(err);
        });

        if (radioDriver != null) radioDriver.setChecked(true);
        applyRoleUi(true);

        if (roleGroup != null) {
            roleGroup.setOnCheckedChangeListener((group, checkedId) -> {
                boolean isDriver = (checkedId == R.id.radioDriver);
                applyRoleUi(isDriver);
            });
        }

        setupInsuranceCompaniesDropdown();

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

            updateModelDropdownAdapter(carModel);

            selectedModelName = null;
            userChangedCarSpecificModel = false;
            if (modelDropdown != null) modelDropdown.setText("", false);
        });

        updateModelDropdownAdapter(carModel);
        if (modelDropdown != null) {
            modelDropdown.setOnClickListener(v -> modelDropdown.showDropDown());
            modelDropdown.setOnItemClickListener((parent, view, position, id) -> {
                String selected = (String) parent.getItemAtPosition(position);
                selectedModelName = selected;
                userChangedCarSpecificModel = true;
            });
        }

        if (insuranceCompanyDropdown == null) {
            Log.e("SignUp", "insuranceCompanyDropdown is NULL (check XML ids!)");
        }

        carNumberEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                if (!isDriverSelected()) return;

                String carNumber = safeText(carNumberEditText);
                if (carNumber.isEmpty()) return;

                if (!userChangedTestDate || !userChangedModel || !userChangedYear || !userChangedCarSpecificModel) {
                    fetchCarInfoFromGov(carNumber);
                }
            }
        });

        insuranceDateEditText.setOnClickListener(v -> {
            if (!isDriverSelected()) return;
            carNumberEditText.clearFocus();
            triggerCarLookupIfNeeded();
            openDatePickerInsurance();
        });

        testDateEditText.setOnClickListener(v -> {
            if (!isDriverSelected()) return;
            carNumberEditText.clearFocus();
            triggerCarLookupIfNeeded();
            openDatePickerTest();
        });

        treatmentDateEditText.setOnClickListener(v -> {
            if (!isDriverSelected()) return;
            carNumberEditText.clearFocus();
            triggerCarLookupIfNeeded();
            openDatePickerTreat();
        });

        next.setOnClickListener(v -> {
            boolean isDriver = isDriverSelected();

            String firstName = safeText(firstNameEditText);
            String lastName = safeText(lastNameEditText);
            String email = safeText(emailEditText);
            String phone = safeText(phoneEditText);

            if (email.isEmpty() || phone.isEmpty()) {
                Toast.makeText(
                        SignUpActivity.this,
                        "נא למלא את כל השדות הבסיסיים",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

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

            String carNumber = safeText(carNumberEditText);

            if (isDriver) {
                if (!viewModel.validateDates()) return;

                boolean missingDriverText = carNumber.isEmpty();
                boolean missingDriverDropdowns =
                        carModel == null || carModel == CarModel.UNKNOWN ||
                                year <= 0 ||
                                selectedModelName == null || selectedModelName.trim().isEmpty();

                if (missingDriverText || missingDriverDropdowns) {
                    Toast.makeText(
                            SignUpActivity.this,
                            "לנהג חובה למלא פרטי רכב",
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }
            }

            // Existing-auth completion flow (driver only)
            if (fromAuthNoDriverDoc && isDriver) {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser == null) {
                    Toast.makeText(this, "שגיאה: המשתמש לא מחובר", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                    return;
                }

                String uid = currentUser.getUid();
                CompleteProfileRepository repo = new CompleteProfileRepository();

                Driver driver = new Driver(
                        firstName,
                        lastName,
                        email,
                        phone,
                        carNumber,
                        carModel,
                        year,
                        viewModel.getSelectedInsuranceDateMillis(),
                        viewModel.getSelectedTestDateMillis(),
                        viewModel.getSelectedTreatDateMillis(),
                        selectedCarPhotoUriString
                );

                if (selectedModelName != null && !selectedModelName.trim().isEmpty() && driver.getCar() != null) {
                    driver.getCar().setCarSpecificModel(selectedModelName.trim());
                }

                repo.completeDriverProfile(uid, driver, new CompleteProfileRepository.CompleteCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(SignUpActivity.this, "השלמת פרופיל הושלמה", Toast.LENGTH_SHORT).show();

                        Intent nextIntent = new Intent(SignUpActivity.this, HomeActivity.class);
                        nextIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(nextIntent);
                        finish();
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(
                                SignUpActivity.this,
                                "שגיאה בהשלמת פרופיל נהג: " + (e != null ? e.getMessage() : ""),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
                return;
            }

            // Build intent for password screen (no email pre-check here)
            Intent intent = new Intent(SignUpActivity.this, SetUsernamePasswordActivity.class);
            intent.putExtra("role", isDriver ? "driver" : "insurance");
            intent.putExtra("isInsurance", !isDriver);

            intent.putExtra("firstName", firstName);
            intent.putExtra("lastName", lastName);
            intent.putExtra("email", email);
            intent.putExtra("phone", phone);

            if (!isDriver) {
                intent.putExtra("insuranceCompanyId", selectedCompanyId);
                if (selectedInsuranceLogoUriString != null && !selectedInsuranceLogoUriString.trim().isEmpty()) {
                    intent.putExtra("insuranceLogoUri", selectedInsuranceLogoUriString.trim());
                }
            }

            if (isDriver) {
                intent.putExtra("carNumber", carNumber);
                intent.putExtra("insuranceDateMillis", viewModel.getSelectedInsuranceDateMillis());
                intent.putExtra("testDateMillis", viewModel.getSelectedTestDateMillis());
                intent.putExtra("treatmentDateMillis", viewModel.getSelectedTreatDateMillis());
                intent.putExtra("carModel", carModel);
                intent.putExtra("year", year);
                intent.putExtra("carSpecificModel", selectedModelName);

                if (selectedCarPhotoUriString != null && !selectedCarPhotoUriString.trim().isEmpty()) {
                    intent.putExtra("carPhotoUri", selectedCarPhotoUriString.trim());
                }
            }

            startActivity(intent);
        });
    }

    private void showTreatRangePopup() {
        if (!isDriverSelected()) return;

        new MaterialAlertDialogBuilder(this)
                .setTitle("לא זוכר תאריך טיפול?")
                .setMessage("בחר טווח זמן, ואנו נציב תאריך משוער בתיבה")
                .setPositiveButton("עד חודשיים", (dialog, which) -> setTreatByMonthsBack(2))
                .setNegativeButton("עד 4 חודשים", (dialog, which) -> setTreatByMonthsBack(4))
                .setNeutralButton("עד חצי שנה", (dialog, which) -> setTreatByMonthsBack(6))
                .show();
    }

    private void setupInsuranceCompaniesDropdown() {
        if (insuranceCompanyDropdown == null) return;

        insuranceCompanyDropdown.setOnClickListener(v -> insuranceCompanyDropdown.showDropDown());

        FirebaseFirestore.getInstance()
                .collection("insurance_companies")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    insuranceCompanyIds.clear();
                    insuranceCompanyNames.clear();

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

                    insuranceCompanyDropdown.setOnItemClickListener((parent, view, position, id) -> {
                        if (position >= 0 && position < insuranceCompanyIds.size()) {
                            selectedCompanyId = insuranceCompanyIds.get(position);
                            if (insuranceCompanyIdEditText != null) {
                                insuranceCompanyIdEditText.setText(selectedCompanyId);
                            }
                            loadInsuranceCompanyDetails(selectedCompanyId);
                        }
                    });
                })
                .addOnFailureListener(e -> Log.e("SignUp", "Failed to load insurance_companies", e));
    }

    private boolean isDriverSelected() {
        return radioDriver != null && radioDriver.isChecked();
    }

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

        if (tvService10kDontRemember != null) tvService10kDontRemember.setVisibility(vis);

        if (insuranceCompanyLayout != null) {
            insuranceCompanyLayout.setVisibility(isDriver ? View.GONE : View.VISIBLE);
        }
        if (insuranceCompanyDropdown != null) {
            insuranceCompanyDropdown.setVisibility(isDriver ? View.GONE : View.VISIBLE);
        }
        if (insuranceCompanyIdEditText != null) {
            insuranceCompanyIdEditText.setVisibility(isDriver ? View.GONE : View.VISIBLE);
        }
        if (insuranceLogoLayout != null) {
            insuranceLogoLayout.setVisibility(isDriver ? View.GONE : View.VISIBLE);
        }
        if (insuranceLogoEditText != null) {
            insuranceLogoEditText.setVisibility(isDriver ? View.GONE : View.VISIBLE);
        }
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
        if (!isDriverSelected()) return;

        String carNumber = safeText(carNumberEditText);
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

    private void loadInsuranceCompanyDetails(String companyId) {
        if (companyId == null || companyId.trim().isEmpty()) return;
        if (isDriverSelected()) return;

        FirebaseFirestore.getInstance()
                .collection("insurance_companies")
                .document(companyId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    String email = doc.getString("email");
                    String phone = doc.getString("phone");
                    String name = doc.getString("name");

                    if (email != null && !email.trim().isEmpty()) emailEditText.setText(email.trim());
                    else emailEditText.setText("");

                    if (phone != null && !phone.trim().isEmpty()) phoneEditText.setText(phone.trim());
                    else phoneEditText.setText("");

                    if (name != null && !name.trim().isEmpty()) firstNameEditText.setText(name.trim());
                    else firstNameEditText.setText("");
                })
                .addOnFailureListener(e -> Log.e("SignUp", "Failed to load company details", e));
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

    private String safeText(EditText et) {
        if (et == null || et.getText() == null) return "";
        return et.getText().toString().trim();
    }
}
