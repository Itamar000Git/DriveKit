//package com.example.drive_kit.View;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.util.Log;
//import android.widget.ArrayAdapter;
//import android.widget.Button;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.lifecycle.ViewModelProvider;
//
//import com.example.drive_kit.Model.CarModel;
//import com.example.drive_kit.R;
//import com.example.drive_kit.ViewModel.VideosViewModel;
//import com.google.android.material.textfield.MaterialAutoCompleteTextView;
//import com.google.android.material.textfield.TextInputLayout;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * DIYFilterActivity lets the user filter DIY videos by:
// * 1) Manufacturer (CarModel)
// * 2) Model (enum depends on manufacturer)
// * 3) Year range (LOADED FROM FIRESTORE via VideosViewModel)
// *
// * Notes:
// * - No typing: user can only pick from dropdown lists.
// * - Year ranges are filled ONLY after selecting a model.
// * - Activity does NOT talk to repository directly (MVVM).
// */
//public class DIYFilterActivity extends AppCompatActivity {
//
//    // UI
//    private TextInputLayout manufacturerLayout;
//    private TextInputLayout modelLayout;
//    private TextInputLayout yearLayout;
//
//    private MaterialAutoCompleteTextView manufacturerDropdown;
//    private MaterialAutoCompleteTextView modelDropdown;
//    private MaterialAutoCompleteTextView yearDropdown;
//    private Button searchButton;
//
//    // ViewModel (MVVM)
//    private VideosViewModel vm;
//
//    // Selected values
//    private CarModel selectedManufacturer = CarModel.UNKNOWN;
//    private String selectedModelName = null;        // enum name (e.g., "COROLLA")
//    private String selectedYearRangeLabel = null;   // from Firestore (e.g., "2016-2020")
//    private static final boolean DEV_SEED =false; //change to true only when needed insert json to video collection in firebase
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.diy_filter);
//
//
//        // --- Find views ---
//        manufacturerLayout = findViewById(R.id.manufacturerLayout);
//        modelLayout = findViewById(R.id.modelLayout);
//        yearLayout = findViewById(R.id.yearLayout);
//
//        manufacturerDropdown = findViewById(R.id.manufacturerDropdown);
//        modelDropdown = findViewById(R.id.modelDropdown);
//        yearDropdown = findViewById(R.id.yearDropdown);
//        searchButton = findViewById(R.id.searchButton);
//
//        // --- ViewModel ---
//        vm = new ViewModelProvider(this).get(VideosViewModel.class);
//
//        // --- Disable typing (dropdown only) ---
//        disableTyping(manufacturerDropdown);
//        disableTyping(modelDropdown);
//        disableTyping(yearDropdown);
//
//        // Clicking anywhere on the row should open the list
//        manufacturerDropdown.setOnClickListener(v -> manufacturerDropdown.showDropDown());
//        modelDropdown.setOnClickListener(v -> modelDropdown.showDropDown());
//        yearDropdown.setOnClickListener(v -> yearDropdown.showDropDown());
//
//        // --- Fill manufacturer list ---
//        ArrayAdapter<String> manufacturerAdapter =
//                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getManufacturersForDropdown());
//        manufacturerDropdown.setAdapter(manufacturerAdapter);
//
//        // Start with model & year disabled until a manufacturer is picked
//        setModelEnabled(false);
//        setYearEnabled(false);
//
//        // --- Observe year ranges from Firestore ---
//        vm.getYearRanges().observe(this, ranges -> {
//            // ranges can be empty if there is no data for this filter
//            ArrayAdapter<String> yearAdapter =
//                    new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
//                            ranges == null ? new ArrayList<>() : ranges);
//
//            yearDropdown.setAdapter(yearAdapter);
//
//            boolean enabled = (ranges != null && !ranges.isEmpty());
//            setYearEnabled(enabled);
//
//            // Reset year selection whenever list changes
//            selectedYearRangeLabel = null;
//            yearDropdown.setText("", false);
//
//            if (!enabled) {
//                Log.w("DIY_FILTER", "No year ranges found in Firestore for this manufacturer+model.");
//            }
//        });
//
//        // --- Observe errors ---
//        vm.getError().observe(this, err -> {
//            if (err != null && !err.trim().isEmpty()) {
//                Log.e("DIY_FILTER", "VM error: " + err);
//            }
//        });
//        if (DEV_SEED) vm.seedDatabaseFromAssets(this);
//
//
//        // --- Optional: pre-fill manufacturer from Intent ---
//        prefillFromIntent(getIntent());
//
//        // --- Manufacturer chosen -> update models ---
//        manufacturerDropdown.setOnItemClickListener((parent, view, position, id) -> {
//            String chosen = (String) parent.getItemAtPosition(position);
//
//            // Save selected manufacturer
//            try {
//                selectedManufacturer = CarModel.valueOf(chosen);
//            } catch (Exception e) {
//                selectedManufacturer = CarModel.UNKNOWN;
//            }
//
//            // Reset dependent fields
//            resetModel();
//            resetYear();
//
//            // Fill model dropdown based on manufacturer (local enums)
//            fillModelsForManufacturer(selectedManufacturer);
//            setModelEnabled(selectedManufacturer != CarModel.UNKNOWN);
//
//            // Year stays disabled until a model is picked AND Firestore returns ranges
//            setYearEnabled(false);
//        });
//
//        // --- Model chosen -> load year ranges from Firestore ---
//        modelDropdown.setOnItemClickListener((parent, view, position, id) -> {
//            selectedModelName = (String) parent.getItemAtPosition(position);
//
//            // Reset year every time model changes
//            resetYear();
//            setYearEnabled(false);
//
//            if (selectedManufacturer == null || selectedManufacturer == CarModel.UNKNOWN) return;
//            if (selectedModelName == null || selectedModelName.trim().isEmpty()) return;
//
//            // Load from Firestore via MVVM
//            vm.loadYearRanges(selectedManufacturer.name(), selectedModelName);
//        });
//
//        // --- Year range chosen ---
//        yearDropdown.setOnItemClickListener((parent, view, position, id) -> {
//            selectedYearRangeLabel = (String) parent.getItemAtPosition(position);
//        });
//
//        // --- Search button ---
//        searchButton.setOnClickListener(v -> {
//            // Simple safety defaults
//            if (selectedManufacturer == null) selectedManufacturer = CarModel.UNKNOWN;
//            if (selectedModelName == null) selectedModelName = "";
//            if (selectedYearRangeLabel == null) selectedYearRangeLabel = "";
//
//
//            // Log all collected values
//            Log.d("DIY_FILTER",
//                    "Search clicked -> manufacturer=" + selectedManufacturer +
//                            ", model=" + selectedModelName +
//                            ", yearRange=" + selectedYearRangeLabel);
//
//            Intent next = new Intent(DIYFilterActivity.this, DIYIssuesActivity.class);
//            next.putExtra("manufacturer", selectedManufacturer.name());
//            next.putExtra("model", selectedModelName);
//            next.putExtra("yearRange", selectedYearRangeLabel);
//            startActivity(next);
//
//        });
//    }
//
//    /**
//     * Prefill manufacturer if it was passed from previous screen.
//     * We only prefill manufacturer here, because year ranges depend on model selection + Firestore.
//     */
//    private void prefillFromIntent(Intent intent) {
//        if (intent == null) return;
//
//        CarModel carModelExtra = (CarModel) intent.getSerializableExtra("carModel");
//        if (carModelExtra != null && carModelExtra != CarModel.UNKNOWN) {
//            selectedManufacturer = carModelExtra;
//            manufacturerDropdown.setText(selectedManufacturer.name(), false);
//
//            // Fill models immediately
//            fillModelsForManufacturer(selectedManufacturer);
//            setModelEnabled(true);
//            setYearEnabled(false);
//        } else {
//            selectedManufacturer = CarModel.UNKNOWN;
//            setModelEnabled(false);
//            setYearEnabled(false);
//        }
//    }
//
//    /**
//     * Returns manufacturer names for the dropdown (enum names).
//     */
//    private List<String> getManufacturersForDropdown() {
//        List<String> list = new ArrayList<>();
//        for (CarModel m : CarModel.values()) {
//            if (m == CarModel.UNKNOWN) continue;
//            list.add(m.name());
//        }
//        return list;
//    }
//
//    /**
//     * Fill the model dropdown based on manufacturer.
//     * Uses CarModel.getModelsFor(manufacturer) (your enum logic).
//     */
//    private void fillModelsForManufacturer(CarModel manufacturer) {
//        Enum<?>[] models = CarModel.getModelsFor(manufacturer);
//
//        List<String> modelNames = new ArrayList<>();
//        for (Enum<?> e : models) {
//            if (e == null) continue;
//            if ("UNKNOWN".equalsIgnoreCase(e.name())) continue;
//            modelNames.add(e.name()); // keep enum name (COROLLA, RAV4...)
//        }
//
//        ArrayAdapter<String> modelAdapter =
//                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, modelNames);
//        modelDropdown.setAdapter(modelAdapter);
//    }
//
//    /**
//     * Reset model selection in UI + variables.
//     */
//    private void resetModel() {
//        selectedModelName = null;
//        modelDropdown.setText("", false);
//        modelDropdown.setAdapter(null);
//    }
//
//    /**
//     * Reset year selection in UI + variables.
//     */
//    private void resetYear() {
//        selectedYearRangeLabel = null;
//        yearDropdown.setText("", false);
//        yearDropdown.setAdapter(null);
//    }
//
//    /**
//     * Disable keyboard typing for an AutoCompleteTextView.
//     * User can still click and choose.
//     */
//    private void disableTyping(MaterialAutoCompleteTextView v) {
//        v.setInputType(android.text.InputType.TYPE_NULL);
//        v.setCursorVisible(false);
//        v.setKeyListener(null);
//    }
//
//    /**
//     * Enable/disable model dropdown visually and functionally.
//     */
//    private void setModelEnabled(boolean enabled) {
//        if (modelLayout != null) modelLayout.setEnabled(enabled);
//        modelDropdown.setEnabled(enabled);
//        modelDropdown.setClickable(enabled);
//    }
//
//    /**
//     * Enable/disable year dropdown visually and functionally.
//     */
//    private void setYearEnabled(boolean enabled) {
//        if (yearLayout != null) yearLayout.setEnabled(enabled);
//        yearDropdown.setEnabled(enabled);
//        yearDropdown.setClickable(enabled);
//    }
//}


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
 * DIYFilterActivity lets the user filter DIY videos by:
 * 1) Manufacturer (CarModel)
 * 2) Model (enum depends on manufacturer)
 * 3) Year range (LOADED FROM FIRESTORE via VideosViewModel)
 *
 * Notes:
 * - No typing: user can only pick from dropdown lists.
 * - Year ranges are filled ONLY after selecting a model.
 * - Activity does NOT talk to repository directly (MVVM).
 */
public class DIYFilterActivity extends BaseLoggedInActivity {

    // UI
    private TextInputLayout manufacturerLayout;
    private TextInputLayout modelLayout;
    private TextInputLayout yearLayout;

    private MaterialAutoCompleteTextView manufacturerDropdown;
    private MaterialAutoCompleteTextView modelDropdown;
    private MaterialAutoCompleteTextView yearDropdown;
    private Button searchButton;

    // =========================
    // ✅ ADDED (Step 2): "My Car" button
    // =========================
    private Button myCarButton;

    // ViewModel (MVVM)
    private VideosViewModel vm;

    // Selected values
    private CarModel selectedManufacturer = CarModel.UNKNOWN;
    private String selectedModelName = null;        // enum name (e.g., "COROLLA")
    private String selectedYearRangeLabel = null;   // from Firestore (e.g., "2016-2020")
    private static final boolean DEV_SEED = false;

    // =========================
    // ✅ ADDED (Step 2): support auto-selecting a yearRange from my car year
    // =========================
    private int pendingMyCarYear = -1;
    private boolean pendingAutoSelectYearRange = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.diy_filter);
        getContentLayoutId();

        TextView credit = findViewById(R.id.creditCarCareKiosk);
        credit.setText(Html.fromHtml(getString(R.string.credit_carcarekiosk_link), Html.FROM_HTML_MODE_LEGACY));
        credit.setMovementMethod(LinkMovementMethod.getInstance());
        credit.setLinksClickable(true);

        // --- Find views ---
        manufacturerLayout = findViewById(R.id.manufacturerLayout);
        modelLayout = findViewById(R.id.modelLayout);
        yearLayout = findViewById(R.id.yearLayout);

        manufacturerDropdown = findViewById(R.id.manufacturerDropdown);
        modelDropdown = findViewById(R.id.modelDropdown);
        yearDropdown = findViewById(R.id.yearDropdown);
        searchButton = findViewById(R.id.searchButton);

        // =========================
        // ✅ ADDED (Step 2): find My Car button
        // (Make sure the id matches your XML)
        // =========================
        myCarButton = findViewById(R.id.myCarButton);

        // --- ViewModel ---
        vm = new ViewModelProvider(this).get(VideosViewModel.class);

        // --- Disable typing (dropdown only) ---
        disableTyping(manufacturerDropdown);
        disableTyping(modelDropdown);
        disableTyping(yearDropdown);

        // Clicking anywhere on the row should open the list
        manufacturerDropdown.setOnClickListener(v -> manufacturerDropdown.showDropDown());
        modelDropdown.setOnClickListener(v -> modelDropdown.showDropDown());
        yearDropdown.setOnClickListener(v -> yearDropdown.showDropDown());

        // --- Fill manufacturer list ---
        ArrayAdapter<String> manufacturerAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getManufacturersForDropdown());
        manufacturerDropdown.setAdapter(manufacturerAdapter);

        // Start with model & year disabled until a manufacturer is picked
        setModelEnabled(false);
        setYearEnabled(false);

        // =========================
        // ✅ CHANGED (Step 2): observe year ranges + support auto-select after "My Car"
        // =========================
        vm.getYearRanges().observe(this, ranges -> {

            ArrayAdapter<String> yearAdapter =
                    new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                            ranges == null ? new ArrayList<>() : ranges);

            yearDropdown.setAdapter(yearAdapter);

            boolean enabled = (ranges != null && !ranges.isEmpty());
            setYearEnabled(enabled);

            // Reset year selection whenever list changes
            selectedYearRangeLabel = null;
            yearDropdown.setText("", false);

            if (!enabled) {
                Log.w("DIY_FILTER", "No year ranges found in Firestore for this manufacturer+model.");
                return;
            }

            // ✅ ADDED (Step 2): if user clicked "My Car", auto-pick the range matching car's year
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

        // --- Observe errors ---
        vm.getError().observe(this, err -> {
            if (err != null && !err.trim().isEmpty()) {
                Log.e("DIY_FILTER", "VM error: " + err);
            }
        });

        if (DEV_SEED) vm.seedDatabaseFromAssets(this);

        // --- Optional: pre-fill manufacturer from Intent ---
        prefillFromIntent(getIntent());

        // --- Manufacturer chosen -> update models ---
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

        // --- Model chosen -> load year ranges from Firestore ---
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

        // --- Year range chosen ---
        yearDropdown.setOnItemClickListener((parent, view, position, id) -> {
            selectedYearRangeLabel = (String) parent.getItemAtPosition(position);
        });

        // =========================
        // ✅ ADDED (Step 2): Hook "My Car" button -> ask ViewModel to load user's car and fill filters
        // =========================
        if (myCarButton != null) {
            myCarButton.setOnClickListener(v -> vm.loadMyCar()); // <- requires VM method (see notes below)
        }

        // =========================
        // ✅ ADDED (Step 2): Observe user's car details and populate dropdowns in correct order
        // =========================
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

        // --- Search button ---
//        searchButton.setOnClickListener(v -> {
//            if (selectedManufacturer == null) selectedManufacturer = CarModel.UNKNOWN;
//            if (selectedModelName == null) selectedModelName = "";
//            if (selectedYearRangeLabel == null) selectedYearRangeLabel = "";
//
//            Log.d("DIY_FILTER",
//                    "Search clicked -> manufacturer=" + selectedManufacturer +
//                            ", model=" + selectedModelName +
//                            ", yearRange=" + selectedYearRangeLabel);
//
//            Intent next = new Intent(DIYFilterActivity.this, DIYIssuesActivity.class);
//            next.putExtra("manufacturer", selectedManufacturer.name());
//            next.putExtra("model", selectedModelName);
//            next.putExtra("yearRange", selectedYearRangeLabel);
//            startActivity(next);
//        });
        searchButton.setOnClickListener(v -> {

            // ✅ enforce all fields are selected
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

    private void resetModel() {
        selectedModelName = null;
        modelDropdown.setText("", false);
        modelDropdown.setAdapter(null);
    }

    private void resetYear() {
        selectedYearRangeLabel = null;
        yearDropdown.setText("", false);
        yearDropdown.setAdapter(null);
    }

    private void disableTyping(MaterialAutoCompleteTextView v) {
        v.setInputType(android.text.InputType.TYPE_NULL);
        v.setCursorVisible(false);
        v.setKeyListener(null);
    }

    private void setModelEnabled(boolean enabled) {
        if (modelLayout != null) modelLayout.setEnabled(enabled);
        modelDropdown.setEnabled(enabled);
        modelDropdown.setClickable(enabled);
    }

    private void setYearEnabled(boolean enabled) {
        if (yearLayout != null) yearLayout.setEnabled(enabled);
        yearDropdown.setEnabled(enabled);
        yearDropdown.setClickable(enabled);
    }

    // =========================
    // ✅ ADDED (Step 2): helper to choose range label from "2014-2018" for a given year
    // =========================
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
