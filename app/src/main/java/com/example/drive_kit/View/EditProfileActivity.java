//package com.example.drive_kit.View;
//
//import android.app.AlertDialog;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.os.Bundle;
//import android.text.InputType;
//import android.util.Base64;
//import android.widget.ArrayAdapter;
//import android.widget.Button;
//import android.widget.Toast;
//
//import androidx.activity.result.ActivityResultLauncher;
//import androidx.activity.result.contract.ActivityResultContracts;
//import androidx.lifecycle.ViewModelProvider;
//
//import com.bumptech.glide.Glide;
//import com.example.drive_kit.Model.Car;
//import com.example.drive_kit.Model.CarModel;
//import com.example.drive_kit.R;
//import com.example.drive_kit.ViewModel.EditProfileViewModel;
//import com.google.android.material.datepicker.MaterialDatePicker;
//import com.google.android.material.imageview.ShapeableImageView;
//import com.google.android.material.textfield.MaterialAutoCompleteTextView;
//import com.google.android.material.textfield.TextInputEditText;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//
//import java.io.ByteArrayOutputStream;
//import java.io.InputStream;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.List;
//
//public class EditProfileActivity extends BaseLoggedInActivity {
//
//    private TextInputEditText firstNameEditText;
//    private TextInputEditText lastNameEditText;
//    private TextInputEditText emailEditText;
//    private TextInputEditText phoneEditText;
//    private TextInputEditText carNumberEditText;
//
//    private MaterialAutoCompleteTextView manufacturerDropdown;
//    private MaterialAutoCompleteTextView modelDropdown;
//    private MaterialAutoCompleteTextView yearDropdown;
//
//    private TextInputEditText insuranceDateEditText;
//    private TextInputEditText testDateEditText;
//    private TextInputEditText treatmentDateEditText;
//
//    private Button saveButton;
//    private Button cancelButton;
//
//    private ShapeableImageView editProfileAvatar;
//    private Button changeImageButton;
//
//    private EditProfileViewModel viewModel;
//
//    // Selected values
//    private CarModel selectedManufacturer = CarModel.UNKNOWN;
//    private String selectedModelName = null;
//    private int selectedYear = 0;
//
//    // NEW: Base64 selected image (only if user changed it)
//    private String selectedCarImageBase64 = null;
//
//    // Launchers
//    private ActivityResultLauncher<String> pickImageLauncher;         // gallery -> Uri
//    private ActivityResultLauncher<Void> takePicturePreviewLauncher;  // camera -> Bitmap
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        getContentLayoutId();
//
//        // ---- Find views ----
//        firstNameEditText = findViewById(R.id.firstNameEditText);
//        lastNameEditText = findViewById(R.id.lastNameEditText);
//        emailEditText = findViewById(R.id.emailEditText);
//        phoneEditText = findViewById(R.id.phoneEditText);
//        carNumberEditText = findViewById(R.id.carNumberEditText);
//
//        manufacturerDropdown = findViewById(R.id.manufacturerDropdown);
//        modelDropdown = findViewById(R.id.modelDropdown);
//        yearDropdown = findViewById(R.id.yearDropdown);
//
//        insuranceDateEditText = findViewById(R.id.insuranceDateEditText);
//        testDateEditText = findViewById(R.id.testDateEditText);
//        treatmentDateEditText = findViewById(R.id.treatmentDateEditText);
//
//        saveButton = findViewById(R.id.saveButton);
//        cancelButton = findViewById(R.id.cancelButton);
//
//        editProfileAvatar = findViewById(R.id.editProfileAvatar);
//        changeImageButton = findViewById(R.id.changeImageButton);
//
//        if (emailEditText != null) emailEditText.setEnabled(false);
//
//        // ---- ViewModel ----
//        viewModel = new ViewModelProvider(this).get(EditProfileViewModel.class);
//
//        // ---- User ----
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        if (user == null) {
//            Toast.makeText(this, "משתמש לא מחובר", Toast.LENGTH_SHORT).show();
//            finish();
//            return;
//        }
//        String uid = user.getUid();
//
//        // =========================
//        // Gallery picker -> Uri -> Bitmap -> Base64
//        // =========================
//        pickImageLauncher = registerForActivityResult(
//                new ActivityResultContracts.GetContent(),
//                uri -> {
//                    if (uri == null) return;
//                    try {
//                        Bitmap bmp = decodeBitmapFromUri(uri);
//                        onNewBitmapSelected(bmp);
//                    } catch (Exception e) {
//                        Toast.makeText(this, "שגיאה בבחירת תמונה", Toast.LENGTH_SHORT).show();
//                    }
//                }
//        );
//
//        // =========================
//        // Camera -> Bitmap -> Base64 (simple, no FileProvider)
//        // =========================
//        takePicturePreviewLauncher = registerForActivityResult(
//                new ActivityResultContracts.TakePicturePreview(),
//                bmp -> {
//                    if (bmp == null) return;
//                    onNewBitmapSelected(bmp);
//                }
//        );
//
//        changeImageButton.setOnClickListener(v -> showCameraGalleryDialog());
//
//        // ---- Setup dropdowns ----
//        setupManufacturerDropdown();
//        setupYearDropdown();
//
//        disableTyping(manufacturerDropdown);
//        disableTyping(modelDropdown);
//        disableTyping(yearDropdown);
//
//        manufacturerDropdown.setOnClickListener(v -> manufacturerDropdown.showDropDown());
//        modelDropdown.setOnClickListener(v -> modelDropdown.showDropDown());
//        yearDropdown.setOnClickListener(v -> yearDropdown.showDropDown());
//
//        manufacturerDropdown.setOnItemClickListener((parent, view, position, id) -> {
//            String chosen = (String) parent.getItemAtPosition(position);
//            try {
//                selectedManufacturer = CarModel.valueOf(chosen);
//            } catch (Exception e) {
//                selectedManufacturer = CarModel.UNKNOWN;
//            }
//            setupModelDropdownFor(selectedManufacturer);
//
//            selectedModelName = null;
//            modelDropdown.setText("", false);
//        });
//
//        modelDropdown.setOnItemClickListener((parent, view, position, id) ->
//                selectedModelName = (String) parent.getItemAtPosition(position)
//        );
//
//        yearDropdown.setOnItemClickListener((parent, view, position, id) -> {
//            String chosen = (String) parent.getItemAtPosition(position);
//            try {
//                selectedYear = Integer.parseInt(chosen);
//            } catch (Exception ignored) {
//                selectedYear = 0;
//            }
//        });
//
//        // ---- Observe driver -> fill UI ----
//        viewModel.getDriver().observe(this, d -> {
//            if (d == null) return;
//
//            firstNameEditText.setText(nullToEmpty(d.getFirstName()));
//            lastNameEditText.setText(nullToEmpty(d.getLastName()));
//            if (emailEditText != null) emailEditText.setText(nullToEmpty(d.getEmail()));
//            phoneEditText.setText(nullToEmpty(d.getPhone()));
//
//            Car car = d.getCar();
//            if (car != null) {
//                carNumberEditText.setText(nullToEmpty(car.getCarNum()));
//
//                insuranceDateEditText.setText(nullToEmpty(d.getFormattedInsuranceDate()));
//                testDateEditText.setText(nullToEmpty(d.getFormattedTestDate()));
//                treatmentDateEditText.setText(nullToEmpty(d.getFormattedTreatDate()));
//
//                viewModel.setSelectedInsuranceDateMillis(car.getInsuranceDateMillis());
//                viewModel.setSelectedTestDateMillis(car.getTestDateMillis());
//                viewModel.setSelectedTreatDateMillis(car.getTreatmentDateMillis());
//
//                CarModel cm = (car.getCarModel() == null) ? CarModel.UNKNOWN : car.getCarModel();
//                selectedManufacturer = cm;
//                manufacturerDropdown.setText(selectedManufacturer.name(), false);
//
//                setupModelDropdownFor(selectedManufacturer);
//
//                selectedModelName = isBlank(car.getCarSpecificModel()) ? null : car.getCarSpecificModel();
//                modelDropdown.setText(selectedModelName == null ? "" : selectedModelName, false);
//
//                selectedYear = car.getYear();
//                yearDropdown.setText(selectedYear > 0 ? String.valueOf(selectedYear) : "", false);
//
//                // Load image ONLY if user didn't pick a new one in this session
//                if (isBlank(selectedCarImageBase64)) {
//
//                    // Prefer Base64 if exists
//                    String b64 = car.getCarImageBase64();
//                    if (!isBlank(b64)) {
//                        Bitmap bmp = base64ToBitmapSafe(b64);
//                        if (bmp != null) {
//                            editProfileAvatar.setImageBitmap(bmp);
//                        } else {
//                            editProfileAvatar.setImageResource(R.drawable.ic_profile_placeholder);
//                        }
//                    } else {
//                        // Fallback to existing URL field (old behavior)
//                        String existingUrl = car.getCarImageUri();
//                        if (!isBlank(existingUrl)) {
//                            Glide.with(EditProfileActivity.this)
//                                    .load(existingUrl)
//                                    .placeholder(R.drawable.ic_profile_placeholder)
//                                    .error(R.drawable.ic_profile_placeholder)
//                                    .centerCrop()
//                                    .into(editProfileAvatar);
//                        } else {
//                            editProfileAvatar.setImageResource(R.drawable.ic_profile_placeholder);
//                        }
//                    }
//                }
//            }
//        });
//
//        viewModel.getToastMessage().observe(this, msg -> {
//            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
//        });
//
//        viewModel.getFinishScreen().observe(this, finish -> {
//            if (Boolean.TRUE.equals(finish)) finish();
//        });
//
//        // Load data
//        viewModel.loadProfile(uid);
//
//        // ---- Date pickers ----
//        insuranceDateEditText.setOnClickListener(v -> openDatePicker("בחר תאריך ביטוח", selection -> {
//            viewModel.setSelectedInsuranceDateMillis(selection);
//            insuranceDateEditText.setText(viewModel.formatDate(selection));
//        }));
//
//        testDateEditText.setOnClickListener(v -> openDatePicker("בחר תאריך טסט", selection -> {
//            viewModel.setSelectedTestDateMillis(selection);
//            testDateEditText.setText(viewModel.formatDate(selection));
//        }));
//
//        treatmentDateEditText.setOnClickListener(v -> openDatePicker("בחר תאריך טיפול 10K", selection -> {
//            viewModel.setSelectedTreatDateMillis(selection);
//            treatmentDateEditText.setText(viewModel.formatDate(selection));
//        }));
//
//        cancelButton.setOnClickListener(v -> finish());
//
//        saveButton.setOnClickListener(v -> {
//            String firstName = text(firstNameEditText);
//            String lastName = text(lastNameEditText);
//            String phone = text(phoneEditText);
//            String carNumber = text(carNumberEditText);
//
//            // IMPORTANT: send Base64 (or null if not changed)
//            viewModel.saveProfile(
//                    uid,
//                    firstName,
//                    lastName,
//                    phone,
//                    carNumber,
//                    selectedManufacturer,
//                    selectedModelName,
//                    selectedYear,
//                    selectedCarImageBase64
//            );
//        });
//    }
//
//    @Override
//    protected int getContentLayoutId() {
//        return R.layout.edit_profile_activity;
//    }
//
//    // =========================
//    // Camera / Gallery dialog
//    // =========================
//    private void showCameraGalleryDialog() {
//        AlertDialog.Builder b = new AlertDialog.Builder(this);
//        b.setTitle("תמונת רכב");
//        b.setItems(new CharSequence[]{"צילום מהמצלמה", "בחירה מהגלריה"}, (dialog, which) -> {
//            if (which == 0) {
//                takePicturePreviewLauncher.launch(null);
//            } else {
//                pickImageLauncher.launch("image/*");
//            }
//        });
//        b.show();
//    }
//
//    // =========================
//    // Bitmap -> Base64 + Preview
//    // =========================
//    private void onNewBitmapSelected(Bitmap original) {
//        if (original == null) return;
//
//        Bitmap scaled = scaleDownKeepingRatio(original, 480);
//        selectedCarImageBase64 = bitmapToBase64Jpeg(scaled, 70);
//
//        editProfileAvatar.setImageBitmap(scaled);
//    }
//
//    private Bitmap decodeBitmapFromUri(android.net.Uri uri) throws Exception {
//        InputStream is = getContentResolver().openInputStream(uri);
//        Bitmap bmp = BitmapFactory.decodeStream(is);
//        if (is != null) is.close();
//        return bmp;
//    }
//
//    private Bitmap scaleDownKeepingRatio(Bitmap src, int maxSize) {
//        int w = src.getWidth();
//        int h = src.getHeight();
//        if (w <= maxSize && h <= maxSize) return src;
//
//        float ratio = Math.min((float) maxSize / w, (float) maxSize / h);
//        int nw = Math.round(w * ratio);
//        int nh = Math.round(h * ratio);
//        return Bitmap.createScaledBitmap(src, nw, nh, true);
//    }
//
//    private String bitmapToBase64Jpeg(Bitmap bmp, int quality) {
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        bmp.compress(Bitmap.CompressFormat.JPEG, quality, out);
//        return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP);
//    }
//
//    private Bitmap base64ToBitmapSafe(String b64) {
//        try {
//            byte[] bytes = Base64.decode(b64, Base64.DEFAULT);
//            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//        } catch (Exception ignored) {
//            return null;
//        }
//    }
//
//    // =========================
//    // Dropdown setup
//    // =========================
//    private void setupManufacturerDropdown() {
//        List<String> list = new ArrayList<>();
//        for (CarModel m : CarModel.values()) {
//            if (m == CarModel.UNKNOWN) continue;
//            list.add(m.name());
//        }
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
//        manufacturerDropdown.setAdapter(adapter);
//    }
//
//    private void setupModelDropdownFor(CarModel manufacturer) {
//        if (manufacturer == null) manufacturer = CarModel.UNKNOWN;
//
//        Enum<?>[] models = CarModel.getModelsFor(manufacturer);
//        ArrayList<String> names = new ArrayList<>();
//
//        if (models != null) {
//            for (Enum<?> e : models) {
//                if (e == null) continue;
//                if ("UNKNOWN".equalsIgnoreCase(e.name())) continue;
//                names.add(e.name());
//            }
//        }
//
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, names);
//        modelDropdown.setAdapter(adapter);
//    }
//
//    private void setupYearDropdown() {
//        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
//        ArrayList<String> years = new ArrayList<>();
//        for (int y = currentYear; y >= 1980; y--) years.add(String.valueOf(y));
//
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, years);
//        yearDropdown.setAdapter(adapter);
//    }
//
//    private void disableTyping(MaterialAutoCompleteTextView v) {
//        if (v == null) return;
//        v.setInputType(InputType.TYPE_NULL);
//        v.setCursorVisible(false);
//        v.setKeyListener(null);
//    }
//
//    // =========================
//    // Date picker
//    // =========================
//    private interface DatePicked {
//        void onPicked(long millis);
//    }
//
//    private void openDatePicker(String title, DatePicked cb) {
//        MaterialDatePicker<Long> picker =
//                MaterialDatePicker.Builder.datePicker()
//                        .setTitleText(title)
//                        .build();
//
//        picker.show(getSupportFragmentManager(), "DATE_PICKER_EDIT_PROFILE");
//        picker.addOnPositiveButtonClickListener(cb::onPicked);
//    }
//
//    // =========================
//    // Utils
//    // =========================
//    private String text(TextInputEditText et) {
//        return et.getText() == null ? "" : et.getText().toString().trim();
//    }
//
//    private String nullToEmpty(String s) {
//        return s == null ? "" : s;
//    }
//
//    private boolean isBlank(String s) {
//        return s == null || s.trim().isEmpty();
//    }
//}


package com.example.drive_kit.View;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.drive_kit.Model.Car;
import com.example.drive_kit.Model.CarModel;
import com.example.drive_kit.R;
import com.example.drive_kit.ViewModel.EditProfileViewModel;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class EditProfileActivity extends BaseLoggedInActivity {

    private TextInputEditText firstNameEditText;
    private TextInputEditText lastNameEditText;
    private TextInputEditText emailEditText;
    private TextInputEditText phoneEditText;
    private TextInputEditText carNumberEditText;

    private MaterialAutoCompleteTextView manufacturerDropdown;
    private MaterialAutoCompleteTextView modelDropdown;
    private MaterialAutoCompleteTextView yearDropdown;

    private TextInputEditText insuranceDateEditText;
    private TextInputEditText testDateEditText;
    private TextInputEditText treatmentDateEditText;

    private Button saveButton;
    private Button cancelButton;

    private ShapeableImageView editProfileAvatar;
    private Button changeImageButton;

    private EditProfileViewModel viewModel;

    // Selected values
    private CarModel selectedManufacturer = CarModel.UNKNOWN;
    private String selectedModelName = null;
    private int selectedYear = 0;

    private String selectedCarImageBase64 = null;

    // Launchers
    private ActivityResultLauncher<String> pickImageLauncher;         // gallery -> Uri
    private ActivityResultLauncher<Void> takePicturePreviewLauncher;  // camera -> Bitmap

    // =========================================================
    // NEW: "user changed" flags (so API won't override manual edits)
    // =========================================================
    private boolean userChangedTestDate = false;
    private boolean userChangedYear = false;
    private boolean userChangedModel = false;
    private boolean userChangedCarSpecificModel = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getContentLayoutId();

        // ---- Find views ----
        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        carNumberEditText = findViewById(R.id.carNumberEditText);

        manufacturerDropdown = findViewById(R.id.manufacturerDropdown);
        modelDropdown = findViewById(R.id.modelDropdown);
        yearDropdown = findViewById(R.id.yearDropdown);

        insuranceDateEditText = findViewById(R.id.insuranceDateEditText);
        testDateEditText = findViewById(R.id.testDateEditText);
        treatmentDateEditText = findViewById(R.id.treatmentDateEditText);

        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);

        editProfileAvatar = findViewById(R.id.editProfileAvatar);
        changeImageButton = findViewById(R.id.changeImageButton);

        if (emailEditText != null) emailEditText.setEnabled(false);

        // ---- ViewModel ----
        viewModel = new ViewModelProvider(this).get(EditProfileViewModel.class);

        // ---- User ----
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "משתמש לא מחובר", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        String uid = user.getUid();

        // =========================
        // Gallery picker -> Uri -> Bitmap -> Base64
        // =========================
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri == null) return;
                    try {
                        Bitmap bmp = decodeBitmapFromUri(uri);
                        onNewBitmapSelected(bmp);
                    } catch (Exception e) {
                        Toast.makeText(this, "שגיאה בבחירת תמונה", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // =========================
        // Camera -> Bitmap -> Base64 (simple, no FileProvider)
        // =========================
        takePicturePreviewLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicturePreview(),
                bmp -> {
                    if (bmp == null) return;
                    onNewBitmapSelected(bmp);
                }
        );

        changeImageButton.setOnClickListener(v -> showCameraGalleryDialog());

        // ---- Setup dropdowns ----
        setupManufacturerDropdown();
        setupYearDropdown();

        disableTyping(manufacturerDropdown);
        disableTyping(modelDropdown);
        disableTyping(yearDropdown);

        manufacturerDropdown.setOnClickListener(v -> manufacturerDropdown.showDropDown());
        modelDropdown.setOnClickListener(v -> modelDropdown.showDropDown());
        yearDropdown.setOnClickListener(v -> yearDropdown.showDropDown());

        manufacturerDropdown.setOnItemClickListener((parent, view, position, id) -> {
            userChangedModel = true; // NEW
            String chosen = (String) parent.getItemAtPosition(position);
            try {
                selectedManufacturer = CarModel.valueOf(chosen);
            } catch (Exception e) {
                selectedManufacturer = CarModel.UNKNOWN;
            }
            setupModelDropdownFor(selectedManufacturer);

            // changing manufacturer resets specific model selection
            userChangedCarSpecificModel = true;
            selectedModelName = null;
            modelDropdown.setText("", false);
        });

        modelDropdown.setOnItemClickListener((parent, view, position, id) -> {
            userChangedCarSpecificModel = true;
            selectedModelName = (String) parent.getItemAtPosition(position);
        });

        yearDropdown.setOnItemClickListener((parent, view, position, id) -> {
            userChangedYear = true;
            String chosen = (String) parent.getItemAtPosition(position);
            try {
                selectedYear = Integer.parseInt(chosen);
            } catch (Exception ignored) {
                selectedYear = 0;
            }
        });

        // =========================================================
        // NEW: when car number loses focus -> fetch from gov (like signup)
        // =========================================================
        carNumberEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String carNumber = safeText(carNumberEditText);
                if (carNumber.isEmpty()) return;

                // same idea as signup: only fetch if at least one field hasn't been manually changed
                if (!userChangedTestDate || !userChangedModel || !userChangedYear || !userChangedCarSpecificModel) {
                    fetchCarInfoFromGov(carNumber);
                }
            }
        });

        // ---- Observe driver -> fill UI ----
        viewModel.getDriver().observe(this, d -> {
            if (d == null) return;

            firstNameEditText.setText(nullToEmpty(d.getFirstName()));
            lastNameEditText.setText(nullToEmpty(d.getLastName()));
            if (emailEditText != null) emailEditText.setText(nullToEmpty(d.getEmail()));
            phoneEditText.setText(nullToEmpty(d.getPhone()));

            Car car = d.getCar();
            if (car != null) {
                carNumberEditText.setText(nullToEmpty(car.getCarNum()));

                insuranceDateEditText.setText(nullToEmpty(d.getFormattedInsuranceDate()));
                testDateEditText.setText(nullToEmpty(d.getFormattedTestDate()));
                treatmentDateEditText.setText(nullToEmpty(d.getFormattedTreatDate()));

                viewModel.setSelectedInsuranceDateMillis(car.getInsuranceDateMillis());
                viewModel.setSelectedTestDateMillis(car.getTestDateMillis());
                viewModel.setSelectedTreatDateMillis(car.getTreatmentDateMillis());

                CarModel cm = (car.getCarModel() == null) ? CarModel.UNKNOWN : car.getCarModel();
                selectedManufacturer = cm;
                manufacturerDropdown.setText(selectedManufacturer.name(), false);

                setupModelDropdownFor(selectedManufacturer);

                selectedModelName = isBlank(car.getCarSpecificModel()) ? null : car.getCarSpecificModel();
                modelDropdown.setText(selectedModelName == null ? "" : selectedModelName, false);

                selectedYear = car.getYear();
                yearDropdown.setText(selectedYear > 0 ? String.valueOf(selectedYear) : "", false);

                // Load image ONLY if user didn't pick a new one in this session
                if (isBlank(selectedCarImageBase64)) {
                    // Prefer Base64 if exists
                    String b64 = car.getCarImageBase64();
                    if (!isBlank(b64)) {
                        Bitmap bmp = base64ToBitmapSafe(b64);
                        if (bmp != null) {
                            editProfileAvatar.setImageBitmap(bmp);
                        } else {
                            editProfileAvatar.setImageResource(R.drawable.ic_profile_placeholder);
                        }
                    } else {
                        // Fallback to existing URL field (old behavior)
                        String existingUrl = car.getCarImageUri();
                        if (!isBlank(existingUrl)) {
                            Glide.with(EditProfileActivity.this)
                                    .load(existingUrl)
                                    .placeholder(R.drawable.ic_profile_placeholder)
                                    .error(R.drawable.ic_profile_placeholder)
                                    .centerCrop()
                                    .into(editProfileAvatar);
                        } else {
                            editProfileAvatar.setImageResource(R.drawable.ic_profile_placeholder);
                        }
                    }
                }
            }
        });

        viewModel.getToastMessage().observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        viewModel.getFinishScreen().observe(this, finish -> {
            if (Boolean.TRUE.equals(finish)) finish();
        });

        // Load data
        viewModel.loadProfile(uid);

        // ---- Date pickers ----
        insuranceDateEditText.setOnClickListener(v -> openDatePicker("בחר תאריך ביטוח", selection -> {
            viewModel.setSelectedInsuranceDateMillis(selection);
            insuranceDateEditText.setText(viewModel.formatDate(selection));
        }));

        testDateEditText.setOnClickListener(v -> openDatePicker("בחר תאריך טסט", selection -> {
            userChangedTestDate = true; // NEW
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

            // IMPORTANT: send Base64 (or null if not changed)
            viewModel.saveProfile(
                    uid,
                    firstName,
                    lastName,
                    phone,
                    carNumber,
                    selectedManufacturer,
                    selectedModelName,
                    selectedYear,
                    selectedCarImageBase64
            );
        });
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.edit_profile_activity;
    }

    // =========================================================
    // NEW: same API call logic as signup
    // =========================================================
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
                    Log.e("EditProfile", "HTTP error: " + code);
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

                // ---- Test date (tokef_dt -> minus 1 year) ----
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

                // ---- Year ----
                Integer yearFromApi = null;
                if (rec0.has("shnat_yitzur")) {
                    int y = rec0.optInt("shnat_yitzur", -1);
                    if (y > 0) yearFromApi = y;
                }

                // ---- Manufacturer ----
                String tozeretNm = rec0.optString("tozeret_nm", "");
                CarModel manufacturerFromApi = CarModel.fromGovValue(tozeretNm);

                // ---- Specific model inference ----
                String kinuyMishari = rec0.optString("kinuy_mishari", "");
                String degemNm = rec0.optString("degem_nm", "");

                CarModel manufacturerContext =
                        (manufacturerFromApi != null && manufacturerFromApi != CarModel.UNKNOWN)
                                ? manufacturerFromApi
                                : selectedManufacturer;

                String specificModelNameFromApi =
                        inferSpecificModelName(manufacturerContext, kinuyMishari, degemNm);

                Long finalTestMillisFromApi = testMillisFromApi;
                String finalTestDisplayFromApi = testDisplayFromApi;
                Integer finalYearFromApi = yearFromApi;
                CarModel finalManufacturerFromApi = manufacturerFromApi;
                String finalSpecificModelNameFromApi = specificModelNameFromApi;
                CarModel finalManufacturerContext = manufacturerContext;

                runOnUiThread(() -> {

                    // Only fill if user didn't override manually
                    if (!userChangedTestDate && finalTestMillisFromApi != null && finalTestDisplayFromApi != null) {
                        viewModel.setSelectedTestDateMillis(finalTestMillisFromApi);
                        testDateEditText.setText(finalTestDisplayFromApi);
                    }

                    if (!userChangedYear && finalYearFromApi != null) {
                        selectedYear = finalYearFromApi;
                        yearDropdown.setText(String.valueOf(selectedYear), false);
                    }

                    if (!userChangedModel && finalManufacturerFromApi != null && finalManufacturerFromApi != CarModel.UNKNOWN) {
                        selectedManufacturer = finalManufacturerFromApi;
                        manufacturerDropdown.setText(selectedManufacturer.name(), false);
                        setupModelDropdownFor(selectedManufacturer);
                    }

                    if (!userChangedCarSpecificModel && modelDropdown != null
                            && finalSpecificModelNameFromApi != null && !finalSpecificModelNameFromApi.isEmpty()) {

                        CarModel effectiveManufacturer =
                                (!userChangedModel && finalManufacturerFromApi != null && finalManufacturerFromApi != CarModel.UNKNOWN)
                                        ? finalManufacturerFromApi
                                        : finalManufacturerContext;

                        setupModelDropdownFor(effectiveManufacturer);

                        selectedModelName = finalSpecificModelNameFromApi;
                        modelDropdown.setText(selectedModelName, false);
                    }
                });

            } catch (Exception e) {
                Log.e("EditProfile", "fetchCarInfoFromGov failed", e);
            } finally {
                if (conn != null) conn.disconnect();
            }
        });
    }

    private String inferSpecificModelName(CarModel manufacturerContext, String kinuyMishari, String degemNm) {
        // אותה גישה כמו בהרשמה: אם יש kinuy_mishari – נעדיף אותו, אחרת degem_nm
        // ואם יש מיפוי פנימי/Enum מותאם אצלכם, זה המקום להתאים; בינתיים נשאיר הכי "לא פולשני".
        String a = (kinuyMishari == null) ? "" : kinuyMishari.trim();
        String b = (degemNm == null) ? "" : degemNm.trim();

        if (!a.isEmpty()) return a;
        return b;
    }

    private String safeText(TextInputEditText et) {
        return et == null || et.getText() == null ? "" : et.getText().toString().trim();
    }

    // =========================
    // Camera / Gallery dialog
    // =========================
    private void showCameraGalleryDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("תמונת רכב");
        b.setItems(new CharSequence[]{"צילום מהמצלמה", "בחירה מהגלריה"}, (dialog, which) -> {
            if (which == 0) {
                takePicturePreviewLauncher.launch(null);
            } else {
                pickImageLauncher.launch("image/*");
            }
        });
        b.show();
    }

    // =========================
    // Bitmap -> Base64 + Preview
    // =========================
    private void onNewBitmapSelected(Bitmap original) {
        if (original == null) return;

        Bitmap scaled = scaleDownKeepingRatio(original, 480);
        selectedCarImageBase64 = bitmapToBase64Jpeg(scaled, 70);

        editProfileAvatar.setImageBitmap(scaled);
    }

    private Bitmap decodeBitmapFromUri(android.net.Uri uri) throws Exception {
        InputStream is = getContentResolver().openInputStream(uri);
        Bitmap bmp = BitmapFactory.decodeStream(is);
        if (is != null) is.close();
        return bmp;
    }

    private Bitmap scaleDownKeepingRatio(Bitmap src, int maxSize) {
        int w = src.getWidth();
        int h = src.getHeight();
        if (w <= maxSize && h <= maxSize) return src;

        float ratio = Math.min((float) maxSize / w, (float) maxSize / h);
        int nw = Math.round(w * ratio);
        int nh = Math.round(h * ratio);
        return Bitmap.createScaledBitmap(src, nw, nh, true);
    }

    private String bitmapToBase64Jpeg(Bitmap bmp, int quality) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, quality, out);
        return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP);
    }

    private Bitmap base64ToBitmapSafe(String b64) {
        try {
            byte[] bytes = Base64.decode(b64, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (Exception ignored) {
            return null;
        }
    }

    // =========================
    // Dropdown setup
    // =========================
    private void setupManufacturerDropdown() {
        List<String> list = new ArrayList<>();
        for (CarModel m : CarModel.values()) {
            if (m == CarModel.UNKNOWN) continue;
            list.add(m.name());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
        manufacturerDropdown.setAdapter(adapter);
    }

    private void setupModelDropdownFor(CarModel manufacturer) {
        if (manufacturer == null) manufacturer = CarModel.UNKNOWN;

        Enum<?>[] models = CarModel.getModelsFor(manufacturer);
        ArrayList<String> names = new ArrayList<>();

        if (models != null) {
            for (Enum<?> e : models) {
                if (e == null) continue;
                if ("UNKNOWN".equalsIgnoreCase(e.name())) continue;
                names.add(e.name());
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, names);
        modelDropdown.setAdapter(adapter);
    }

    private void setupYearDropdown() {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        ArrayList<String> years = new ArrayList<>();
        for (int y = currentYear; y >= 1980; y--) years.add(String.valueOf(y));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, years);
        yearDropdown.setAdapter(adapter);
    }

    private void disableTyping(MaterialAutoCompleteTextView v) {
        if (v == null) return;
        v.setInputType(InputType.TYPE_NULL);
        v.setCursorVisible(false);
        v.setKeyListener(null);
    }

    // =========================
    // Date picker
    // =========================
    private interface DatePicked {
        void onPicked(long millis);
    }

    private void openDatePicker(String title, DatePicked cb) {
        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointBackward.now())
                .build();
        MaterialDatePicker<Long> picker =
                MaterialDatePicker.Builder.datePicker()
                        .setTitleText(title)
                        .setCalendarConstraints(constraints)
                        .build();

        picker.show(getSupportFragmentManager(), "DATE_PICKER_EDIT_PROFILE");
        picker.addOnPositiveButtonClickListener(cb::onPicked);
    }

    // =========================
    // Utils
    // =========================
    private String text(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}