package com.example.drive_kit.View;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast; // ✅ ADDED

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.drive_kit.Model.Car;
import com.example.drive_kit.Model.CarModel;
import com.example.drive_kit.R;
import com.example.drive_kit.ViewModel.VideosViewModel;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import android.text.Html;
import android.text.method.LinkMovementMethod;

/**
 * DIYFilterActivity
 *
 * Screen for filtering DIY car videos based on:
 * - Manufacturer
 * - Model
 * - Year range (loaded from Firestore)
 *
 * Responsibilities:
 * - Manage dropdown selections
 * - Enforce valid selection flow (manufacturer → model → year)
 * - Load data via ViewModel (MVVM)
 * - Navigate to DIYIssuesActivity with selected filters
 *
 * Special Features:
 * - "My Car" button → auto-fill filters from user's saved car
 * - Auto-select year range based on car year
 *
 * Architecture:
 * - Activity → UI + interactions
 * - ViewModel → data loading (Firestore + logic)
 */
public class DIYFilterActivity extends BaseLoggedInActivity {

    /** Layout wrappers for inputs */
    private TextInputLayout manufacturerLayout;
    private TextInputLayout modelLayout;
    private TextInputLayout yearLayout;

    /** Dropdowns */
    private MaterialAutoCompleteTextView manufacturerDropdown;
    private MaterialAutoCompleteTextView modelDropdown;
    private MaterialAutoCompleteTextView yearDropdown;

    /** Search button */
    private Button searchButton;

    /** "My Car" button (auto-fill feature) */
    private Button myCarButton;

    /** ViewModel (MVVM) */
    private VideosViewModel vm;

    // ===== Selected values =====
    private CarModel selectedManufacturer = CarModel.UNKNOWN; // Selected manufacturer enum
    private String selectedModelName = null; // Selected model name (string enum name)
    private String selectedYearRangeLabel = null; // Selected year range label (e.g., "2016-2020")
    private static final boolean DEV_SEED = false; // Development flag for seeding database

    // ===== Auto-select logic for "My Car" =====
    private int pendingMyCarYear = -1; // Holds user's car year temporarily
    private boolean pendingAutoSelectYearRange = false; // Indicates whether we should auto-select year range

    /**
     * Initializes UI, ViewModel, dropdown logic and observers.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable clickable HTML link in credit text
        getContentLayoutId();

        TextView credit = findViewById(R.id.creditCarCareKiosk);
        credit.setText(Html.fromHtml(getString(R.string.credit_carcarekiosk_link), Html.FROM_HTML_MODE_LEGACY));
        credit.setMovementMethod(LinkMovementMethod.getInstance());
        credit.setLinksClickable(true);

        // ===== Bind views =====
        manufacturerLayout = findViewById(R.id.manufacturerLayout);
        modelLayout = findViewById(R.id.modelLayout);
        yearLayout = findViewById(R.id.yearLayout);

        manufacturerDropdown = findViewById(R.id.manufacturerDropdown);
        modelDropdown = findViewById(R.id.modelDropdown);
        yearDropdown = findViewById(R.id.yearDropdown);
        searchButton = findViewById(R.id.searchButton);

        myCarButton = findViewById(R.id.myCarButton);

        // ===== ViewModel =====
        vm = new ViewModelProvider(this).get(VideosViewModel.class);

        // Disable typing → dropdown only
        disableTyping(manufacturerDropdown);
        disableTyping(modelDropdown);
        disableTyping(yearDropdown);

        // Clicking anywhere opens dropdown
        manufacturerDropdown.setOnClickListener(v -> manufacturerDropdown.showDropDown());
        modelDropdown.setOnClickListener(v -> modelDropdown.showDropDown());
        yearDropdown.setOnClickListener(v -> yearDropdown.showDropDown());

        // ===== Manufacturer list =====
        ArrayAdapter<String> manufacturerAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getManufacturersForDropdown());
        manufacturerDropdown.setAdapter(manufacturerAdapter);

        // Disable dependent fields initially
        setModelEnabled(false);
        setYearEnabled(false);

        /**
         * Observe year ranges from Firestore.
         * Triggered after selecting manufacturer + model.
         */
        vm.getYearRanges().observe(this, ranges -> {

            ArrayAdapter<String> yearAdapter =
                    new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                            ranges == null ? new ArrayList<>() : ranges);

            yearDropdown.setAdapter(yearAdapter);

            boolean enabled = (ranges != null && !ranges.isEmpty());
            setYearEnabled(enabled);

            // Reset selection
            selectedYearRangeLabel = null;
            yearDropdown.setText("", false);

            if (!enabled) {
                Log.w("DIY_FILTER", "No year ranges found in Firestore for this manufacturer+model.");
                return;
            }

            /**
             * Auto-select year range if "My Car" was used.
             */
            if (pendingAutoSelectYearRange && pendingMyCarYear > 0) {
                String match = pickRangeLabelForYear(ranges, pendingMyCarYear);
                if (match != null && !match.trim().isEmpty()) {
                    selectedYearRangeLabel = match;
                    yearDropdown.setText(match, false);
                }
                pendingAutoSelectYearRange = false;
                pendingMyCarYear = -1;
            }
        });

        // Observe errors
        vm.getError().observe(this, err -> {
            if (err != null && !err.trim().isEmpty()) {
                Log.e("DIY_FILTER", "VM error: " + err);
            }
        });

        if (DEV_SEED) vm.seedDatabaseFromAssets(this);

        // Prefill manufacturer if exists
        prefillFromIntent(getIntent());

        /**
         * Manufacturer selection → updates model dropdown
         */
        manufacturerDropdown.setOnItemClickListener((parent, view, position, id) -> {
            String chosen = (String) parent.getItemAtPosition(position);

            // Save selected manufacturer
            try {
                selectedManufacturer = CarModel.valueOf(chosen);
            } catch (Exception e) {
                selectedManufacturer = CarModel.UNKNOWN;
            }

            // Reset dependent fields
            resetModel();
            resetYear();

            // Fill model dropdown based on manufacturer (local enums)
            fillModelsForManufacturer(selectedManufacturer);
            setModelEnabled(selectedManufacturer != CarModel.UNKNOWN);

            // Year stays disabled until a model is picked AND Firestore returns ranges
            setYearEnabled(false);
        });

        /**
         * Model selection → load year ranges from Firestore
         */
        modelDropdown.setOnItemClickListener((parent, view, position, id) -> {
            selectedModelName = (String) parent.getItemAtPosition(position);

            // Reset year every time model changes
            resetYear();
            setYearEnabled(false);

            if (selectedManufacturer == null || selectedManufacturer == CarModel.UNKNOWN) return;
            if (selectedModelName == null || selectedModelName.trim().isEmpty()) return;

            // Load from Firestore via MVVM
            vm.loadYearRanges(selectedManufacturer.name(), selectedModelName);
        });

        /**
         * Year selection
         */
        yearDropdown.setOnItemClickListener((parent, view, position, id) -> {
            selectedYearRangeLabel = (String) parent.getItemAtPosition(position);
        });

        /**
         * "My Car" button → load user's car from ViewModel
         */
        if (myCarButton != null) {
            myCarButton.setOnClickListener(v -> vm.loadMyCar()); // <- requires VM method (see notes below)
        }

        /**
         * Observe user's car → auto-fill all fields
         */
        vm.getMyCar().observe(this, car -> {  // <- requires VM LiveData<Car>
            if (car == null) {
                Toast.makeText(this, "לא נמצאו פרטי רכב", Toast.LENGTH_SHORT).show();
                return;
            }

            CarModel m = car.getCarModel();
            if (m == null) m = CarModel.UNKNOWN;

            String specificModel = car.getCarSpecificModel();
            int carYear = car.getYear();

            // 1) Manufacturer
            selectedManufacturer = m;
            manufacturerDropdown.setText(selectedManufacturer.name(), false);

            // 2) Fill models
            resetModel();
            resetYear();
            fillModelsForManufacturer(selectedManufacturer);
            setModelEnabled(selectedManufacturer != CarModel.UNKNOWN);

            // 3) Select specific model if exists
            if (specificModel != null && !specificModel.trim().isEmpty()) {
                selectedModelName = specificModel;
                modelDropdown.setText(selectedModelName, false);

                // 4) Load year ranges for manufacturer+model (from Firestore)
                setYearEnabled(false);

                // prepare auto selection of year range by year
                pendingMyCarYear = carYear;
                pendingAutoSelectYearRange = true;

                vm.loadYearRanges(selectedManufacturer.name(), selectedModelName);
            } else {
                selectedModelName = null;
                modelDropdown.setText("", false);
                setYearEnabled(false);
                Toast.makeText(this, "חסר דגם לרכב שלך", Toast.LENGTH_SHORT).show();
            }
        });

        vm.getMyCarError().observe(this, err -> { // <- requires VM LiveData<String>
            if (err != null && !err.trim().isEmpty()) {
                Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
            }
        });

        /**
         * Search button → validate inputs and navigate
         */
        searchButton.setOnClickListener(v -> {

            boolean missing = false;

            if (selectedManufacturer == null || selectedManufacturer == CarModel.UNKNOWN) {
                manufacturerDropdown.setError("בחר יצרן");
                missing = true;
            } else {
                manufacturerDropdown.setError(null);
            }

            if (selectedModelName == null || selectedModelName.trim().isEmpty()) {
                modelDropdown.setError("בחר דגם");
                missing = true;
            } else {
                modelDropdown.setError(null);
            }

            if (selectedYearRangeLabel == null || selectedYearRangeLabel.trim().isEmpty()) {
                yearDropdown.setError("בחר טווח שנים");
                missing = true;
            } else {
                yearDropdown.setError(null);
            }

            if (missing) {
                android.widget.Toast
                        .makeText(this, "יש למלא את כל השדות לפני חיפוש", android.widget.Toast.LENGTH_SHORT)
                        .show();
                return;
            }

            Log.d("DIY_FILTER",
                    "Search clicked -> manufacturer=" + selectedManufacturer +
                            ", model=" + selectedModelName +
                            ", yearRange=" + selectedYearRangeLabel);

            Intent next = new Intent(DIYFilterActivity.this, DIYIssuesActivity.class);
            next.putExtra("manufacturer", selectedManufacturer.name());
            next.putExtra("model", selectedModelName);
            next.putExtra("yearRange", selectedYearRangeLabel);
            startActivity(next);
        });

    }

    /**
     * Provides layout resource for BaseLoggedInActivity.
     *
     * @return layout resource ID for this screen
     */
    @Override
    protected int getContentLayoutId() {
        return R.layout.diy_filter ;
    }

    /**
     * Prefill manufacturer if it was passed from previous screen.
     * We only prefill manufacturer here, because year ranges depend on model selection + Firestore.
     */
    private void prefillFromIntent(Intent intent) {
        if (intent == null) return;

        CarModel carModelExtra = (CarModel) intent.getSerializableExtra("carModel");
        if (carModelExtra != null && carModelExtra != CarModel.UNKNOWN) {
            selectedManufacturer = carModelExtra;
            manufacturerDropdown.setText(selectedManufacturer.name(), false);

            fillModelsForManufacturer(selectedManufacturer);
            setModelEnabled(true);
            setYearEnabled(false);
        } else {
            selectedManufacturer = CarModel.UNKNOWN;
            setModelEnabled(false);
            setYearEnabled(false);
        }
    }

    /**
     * Returns manufacturer names for the dropdown (enum names).
     */
    private List<String> getManufacturersForDropdown() {
        List<String> list = new ArrayList<>();
        for (CarModel m : CarModel.values()) {
            if (m == CarModel.UNKNOWN) continue;
            list.add(m.name());
        }
        return list;
    }

    /**
     * Fill the model dropdown based on manufacturer.
     * Uses CarModel.getModelsFor(manufacturer).
     */
    private void fillModelsForManufacturer(CarModel manufacturer) {
        Enum<?>[] models = CarModel.getModelsFor(manufacturer);

        List<String> modelNames = new ArrayList<>();
        for (Enum<?> e : models) {
            if (e == null) continue;
            if ("UNKNOWN".equalsIgnoreCase(e.name())) continue;
            modelNames.add(e.name());
        }

        ArrayAdapter<String> modelAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, modelNames);
        modelDropdown.setAdapter(modelAdapter);
    }

    /**
     * Resets model selection and UI.
     *
     * Clears:
     * - selected model value
     * - dropdown text
     * - adapter data
     */
    private void resetModel() {
        selectedModelName = null;
        modelDropdown.setText("", false);
        modelDropdown.setAdapter(null);
    }

    /**
     * Resets year selection and UI.
     *
     * Clears:
     * - selected year range
     * - dropdown text
     * - adapter data
     */
    private void resetYear() {
        selectedYearRangeLabel = null;
        yearDropdown.setText("", false);
        yearDropdown.setAdapter(null);
    }

    /**
     * Disables manual typing in dropdown.
     *
     * Purpose:
     * - Forces user to select from list only
     * - Prevents invalid input
     *
     * @param v dropdown view
     */
    private void disableTyping(MaterialAutoCompleteTextView v) {
        v.setInputType(android.text.InputType.TYPE_NULL);
        v.setCursorVisible(false);
        v.setKeyListener(null);
    }

    /**
     * Enables/disables model input.
     *
     * Affects:
     * - TextInputLayout (visual state)
     * - Dropdown interaction
     *
     * @param enabled true to enable, false to disable
     */
    private void setModelEnabled(boolean enabled) {
        if (modelLayout != null) modelLayout.setEnabled(enabled);
        modelDropdown.setEnabled(enabled);
        modelDropdown.setClickable(enabled);
    }

    /**
     * Enables/disables year input.
     *
     * Affects:
     * - TextInputLayout (visual state)
     * - Dropdown interaction
     *
     * @param enabled true to enable, false to disable
     */
    private void setYearEnabled(boolean enabled) {
        if (yearLayout != null) yearLayout.setEnabled(enabled);
        yearDropdown.setEnabled(enabled);
        yearDropdown.setClickable(enabled);
    }


    /**
     * Finds matching year range label for a given year.
     *
     * Input format example:
     * - ranges: ["2014-2018", "2019-2022"]
     * - year: 2017
     *
     * Output:
     * - "2014-2018"
     *
     * Logic:
     * - Parses each range
     * - Checks if year is within [from, to]
     *
     * @param ranges list of range labels
     * @param year specific year
     * @return matching range label or null if none found
     */
    private String pickRangeLabelForYear(List<String> ranges, int year) {
        if (ranges == null || ranges.isEmpty() || year <= 0) return null;

        for (String label : ranges) {
            if (label == null) continue;
            String s = label.trim();
            // expected format: "2014-2018"
            String[] parts = s.split("-");
            if (parts.length != 2) continue;

            try {
                int from = Integer.parseInt(parts[0].trim());
                int to = Integer.parseInt(parts[1].trim());
                if (from <= year && year <= to) return s;
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}
