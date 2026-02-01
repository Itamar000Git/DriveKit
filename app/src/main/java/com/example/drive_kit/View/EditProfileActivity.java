//package com.example.drive_kit.View;
//
//import android.net.Uri;
//import android.os.Bundle;
//import android.text.InputType;
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
//    // NEW: avatar + change image
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
//    // NEW: holds chosen image (content://...) until save
//    private String selectedCarImageUriString = null;
//
//    // NEW: image picker launcher
//    private ActivityResultLauncher<String> pickImageLauncher;
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
//        // NEW:
//        editProfileAvatar = findViewById(R.id.editProfileAvatar);
//        changeImageButton = findViewById(R.id.changeImageButton);
//
//        // Email should be display-only (as in XML)
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
//        // ---- Image picker (NEW) ----
//        pickImageLauncher = registerForActivityResult(
//                new ActivityResultContracts.GetContent(),
//                uri -> {
//                    if (uri == null) return;
//
//                    selectedCarImageUriString = uri.toString();
//
//                    // Preview immediately
//                    Glide.with(EditProfileActivity.this)
//                            .load(uri)
//                            .placeholder(R.drawable.ic_profile_placeholder)
//                            .error(R.drawable.ic_profile_placeholder)
//                            .centerCrop()
//                            .into(editProfileAvatar);
//                }
//        );
//
//        changeImageButton.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
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
//
//            setupModelDropdownFor(selectedManufacturer);
//
//            selectedModelName = null;
//            modelDropdown.setText("", false);
//        });
//
//        modelDropdown.setOnItemClickListener((parent, view, position, id) -> {
//            selectedModelName = (String) parent.getItemAtPosition(position);
//        });
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
//            carNumberEditText.setText(nullToEmpty(car.getCarNum()));
//
//            // Dates (formatted)
//            insuranceDateEditText.setText(nullToEmpty(d.getFormattedInsuranceDate()));
//            testDateEditText.setText(nullToEmpty(d.getFormattedTestDate()));
//            treatmentDateEditText.setText(nullToEmpty(d.getFormattedTreatDate()));
//
//            // Keep millis in ViewModel
//            viewModel.setSelectedInsuranceDateMillis(car.getInsuranceDateMillis());
//            viewModel.setSelectedTestDateMillis(car.getTestDateMillis());
//            viewModel.setSelectedTreatDateMillis(car.getTreatmentDateMillis());
//
//            // Prefill manufacturer/model/year
//            CarModel cm = car.getCarModel() == null ? CarModel.UNKNOWN : car.getCarModel();
//            selectedManufacturer = cm;
//            manufacturerDropdown.setText(selectedManufacturer.name(), false);
//
//            setupModelDropdownFor(selectedManufacturer);
//
//            selectedModelName = isBlank(car.getCarSpecificModel()) ? null : car.getCarSpecificModel();
//            modelDropdown.setText(selectedModelName == null ? "" : selectedModelName, false);
//
//            selectedYear = car.getYear();
//            yearDropdown.setText(selectedYear > 0 ? String.valueOf(selectedYear) : "", false);
//
//            // NEW: load existing image into avatar if no new selection yet
//            // >>> אם אצלך השם של getter שונה - עדכני רק את השורה הבאה:
//            String existing = nullToEmpty(car.getCarImageUri()); // <-- adjust if needed
//
//            if (isBlank(selectedCarImageUriString)) {
//                if (!isBlank(existing)) {
//                    Glide.with(EditProfileActivity.this)
//                            .load(existing)
//                            .placeholder(R.drawable.ic_profile_placeholder)
//                            .error(R.drawable.ic_profile_placeholder)
//                            .centerCrop()
//                            .into(editProfileAvatar);
//                } else {
//                    editProfileAvatar.setImageResource(R.drawable.ic_profile_placeholder);
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
//            // NEW: pass selectedCarImageUriString (can be null/empty if not changed)
//            viewModel.saveProfile(
//                    uid,
//                    firstName,
//                    lastName,
//                    phone,
//                    carNumber,
//                    selectedManufacturer,
//                    selectedModelName,
//                    selectedYear,
//                    selectedCarImageUriString
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
//    // Dropdown setup
//    // =========================
//
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
//
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
//
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
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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

    // NEW: Base64 selected image (only if user changed it)
    private String selectedCarImageBase64 = null;

    // Launchers
    private ActivityResultLauncher<String> pickImageLauncher;         // gallery -> Uri
    private ActivityResultLauncher<Void> takePicturePreviewLauncher;  // camera -> Bitmap

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
            String chosen = (String) parent.getItemAtPosition(position);
            try {
                selectedManufacturer = CarModel.valueOf(chosen);
            } catch (Exception e) {
                selectedManufacturer = CarModel.UNKNOWN;
            }
            setupModelDropdownFor(selectedManufacturer);

            selectedModelName = null;
            modelDropdown.setText("", false);
        });

        modelDropdown.setOnItemClickListener((parent, view, position, id) ->
                selectedModelName = (String) parent.getItemAtPosition(position)
        );

        yearDropdown.setOnItemClickListener((parent, view, position, id) -> {
            String chosen = (String) parent.getItemAtPosition(position);
            try {
                selectedYear = Integer.parseInt(chosen);
            } catch (Exception ignored) {
                selectedYear = 0;
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
        MaterialDatePicker<Long> picker =
                MaterialDatePicker.Builder.datePicker()
                        .setTitleText(title)
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
