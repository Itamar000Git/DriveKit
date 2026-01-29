//package com.example.drive_kit.View;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.util.Log;
//import android.widget.ArrayAdapter;
//import android.widget.Button;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.example.drive_kit.Model.CarModel;
//import com.example.drive_kit.R;
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
// * 3) Year range (2 ranges per model, from the enum logic)
// *
// * Notes:
// * - No typing: user can only pick from dropdown lists.
// * - Year ranges are filled ONLY after selecting a model.
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
//    // Selected values
//    private CarModel selectedManufacturer = CarModel.UNKNOWN;
//    private String selectedModelName = null;     // we keep enum name (e.g., "COROLLA")
//    private String selectedYearRangeLabel = null; // what user picked, e.g. "2016-2020"
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.diy_filter);
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
//        // --- Optional: pre-fill from Intent (if you send from Home) ---
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
//            // Fill model dropdown based on manufacturer
//            fillModelsForManufacturer(selectedManufacturer);
//            setModelEnabled(selectedManufacturer != CarModel.UNKNOWN);
//
//            // Year stays disabled until a model is picked
//            setYearEnabled(false);
//        });
//
//        // --- Model chosen -> update year ranges (2 options from enum) ---
//        modelDropdown.setOnItemClickListener((parent, view, position, id) -> {
//            selectedModelName = (String) parent.getItemAtPosition(position);
//
//            // Reset year every time model changes
//            resetYear();
//
//            // Fill year ranges based on manufacturer + model
//            fillYearRangesFor(selectedManufacturer, selectedModelName);
//
//            // Enable year now
//            setYearEnabled(true);
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
//            // Log all collected values
//            Log.d("DIY_FILTER",
//                    "Search clicked -> manufacturer=" + selectedManufacturer +
//                            ", model=" + selectedModelName +
//                            ", yearRange=" + selectedYearRangeLabel);
//
//            // TODO later: open results screen
//            // Intent next = new Intent(DIYFilterActivity.this, DIYResultsActivity.class);
//            // next.putExtra("carModel", selectedManufacturer);
//            // next.putExtra("model", selectedModelName);
//            // next.putExtra("yearRange", selectedYearRangeLabel);
//            // startActivity(next);
//        });
//    }
//
//    /**
//     * Prefill manufacturer/year if they were passed from previous screen.
//     * We only prefill manufacturer here, because year ranges depend on model selection.
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
//            // No manufacturer -> keep disabled
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
//     * Uses CarModel.getModelsFor(manufacturer).
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
//     * Fill year ranges (2 items) based on manufacturer + model.
//     * Uses CarModel.getYearRangesFor(manufacturer, modelName).
//     *
//     * We show the labels (e.g. "2016-2020") in the dropdown.
//     */
//    private void fillYearRangesFor(CarModel manufacturer, String modelName) {
//        // Your enum method returns YearRange[]
//        // We convert to displayed strings here (label).
//        Object[] ranges = CarModel.getYearRangesFor(manufacturer, modelName);
//
//        List<String> labels = new ArrayList<>();
//        if (ranges != null) {
//            for (Object r : ranges) {
//                if (r == null) continue;
//                String label = r.toString(); // YearRange.toString() returns label
//                if (label == null) continue;
//                if (label.contains("לא ידוע") || label.equalsIgnoreCase("UNKNOWN")) continue;
//                labels.add(label);
//            }
//        }
//
//        // If for some reason it's empty, keep the list empty (no crash)
//        ArrayAdapter<String> yearAdapter =
//                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, labels);
//        yearDropdown.setAdapter(yearAdapter);
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.drive_kit.Model.CarModel;
import com.example.drive_kit.R;
import com.example.drive_kit.ViewModel.VideosViewModel;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

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
public class DIYFilterActivity extends AppCompatActivity {

    // UI
    private TextInputLayout manufacturerLayout;
    private TextInputLayout modelLayout;
    private TextInputLayout yearLayout;

    private MaterialAutoCompleteTextView manufacturerDropdown;
    private MaterialAutoCompleteTextView modelDropdown;
    private MaterialAutoCompleteTextView yearDropdown;
    private Button searchButton;

    // ViewModel (MVVM)
    private VideosViewModel vm;

    // Selected values
    private CarModel selectedManufacturer = CarModel.UNKNOWN;
    private String selectedModelName = null;        // enum name (e.g., "COROLLA")
    private String selectedYearRangeLabel = null;   // from Firestore (e.g., "2016-2020")

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.diy_filter);

        // --- Find views ---
        manufacturerLayout = findViewById(R.id.manufacturerLayout);
        modelLayout = findViewById(R.id.modelLayout);
        yearLayout = findViewById(R.id.yearLayout);

        manufacturerDropdown = findViewById(R.id.manufacturerDropdown);
        modelDropdown = findViewById(R.id.modelDropdown);
        yearDropdown = findViewById(R.id.yearDropdown);
        searchButton = findViewById(R.id.searchButton);

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

        // --- Observe year ranges from Firestore ---
        vm.getYearRanges().observe(this, ranges -> {
            // ranges can be empty if there is no data for this filter
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
            }
        });

        // --- Observe errors ---
        vm.getError().observe(this, err -> {
            if (err != null && !err.trim().isEmpty()) {
                Log.e("DIY_FILTER", "VM error: " + err);
            }
        });

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

        // --- Search button ---
        searchButton.setOnClickListener(v -> {
            // Simple safety defaults
            if (selectedManufacturer == null) selectedManufacturer = CarModel.UNKNOWN;
            if (selectedModelName == null) selectedModelName = "";
            if (selectedYearRangeLabel == null) selectedYearRangeLabel = "";

            // Log all collected values
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
     * Prefill manufacturer if it was passed from previous screen.
     * We only prefill manufacturer here, because year ranges depend on model selection + Firestore.
     */
    private void prefillFromIntent(Intent intent) {
        if (intent == null) return;

        CarModel carModelExtra = (CarModel) intent.getSerializableExtra("carModel");
        if (carModelExtra != null && carModelExtra != CarModel.UNKNOWN) {
            selectedManufacturer = carModelExtra;
            manufacturerDropdown.setText(selectedManufacturer.name(), false);

            // Fill models immediately
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
     * Uses CarModel.getModelsFor(manufacturer) (your enum logic).
     */
    private void fillModelsForManufacturer(CarModel manufacturer) {
        Enum<?>[] models = CarModel.getModelsFor(manufacturer);

        List<String> modelNames = new ArrayList<>();
        for (Enum<?> e : models) {
            if (e == null) continue;
            if ("UNKNOWN".equalsIgnoreCase(e.name())) continue;
            modelNames.add(e.name()); // keep enum name (COROLLA, RAV4...)
        }

        ArrayAdapter<String> modelAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, modelNames);
        modelDropdown.setAdapter(modelAdapter);
    }

    /**
     * Reset model selection in UI + variables.
     */
    private void resetModel() {
        selectedModelName = null;
        modelDropdown.setText("", false);
        modelDropdown.setAdapter(null);
    }

    /**
     * Reset year selection in UI + variables.
     */
    private void resetYear() {
        selectedYearRangeLabel = null;
        yearDropdown.setText("", false);
        yearDropdown.setAdapter(null);
    }

    /**
     * Disable keyboard typing for an AutoCompleteTextView.
     * User can still click and choose.
     */
    private void disableTyping(MaterialAutoCompleteTextView v) {
        v.setInputType(android.text.InputType.TYPE_NULL);
        v.setCursorVisible(false);
        v.setKeyListener(null);
    }

    /**
     * Enable/disable model dropdown visually and functionally.
     */
    private void setModelEnabled(boolean enabled) {
        if (modelLayout != null) modelLayout.setEnabled(enabled);
        modelDropdown.setEnabled(enabled);
        modelDropdown.setClickable(enabled);
    }

    /**
     * Enable/disable year dropdown visually and functionally.
     */
    private void setYearEnabled(boolean enabled) {
        if (yearLayout != null) yearLayout.setEnabled(enabled);
        yearDropdown.setEnabled(enabled);
        yearDropdown.setClickable(enabled);
    }
}
