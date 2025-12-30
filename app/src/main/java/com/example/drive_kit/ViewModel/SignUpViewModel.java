package com.example.drive_kit.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * SignUpViewModel is responsible for holding and validating
 * the data related to the signup screen.
 *
 * This ViewModel:
 * - Stores the selected dates (insurance, test, 10K treatment)
 * - Validates that all required dates were selected
 * - Exposes error messages using LiveData
 *
 * IMPORTANT:
 * This class does NOT know anything about UI elements (EditText, Buttons, etc.)
 */
public class SignUpViewModel extends ViewModel {

    // Stores the selected insurance date in milliseconds
    // -1 means: no date was selected yet
    private long selectedInsuranceDateMillis = -1;

    // Stores the selected test date in milliseconds
    private long selectedTestDateMillis = -1;

    // Stores the selected 10K treatment date in milliseconds
    private long selectedTreatDateMillis = -1;




    // LiveData that holds an error message for the insurance date field
    // null means: no error
    private final MutableLiveData<String> insuranceDateError =
            new MutableLiveData<>(null);

    // LiveData that holds an error message for the test date field
    private final MutableLiveData<String> testDateError =
            new MutableLiveData<>(null);

    // LiveData that holds an error message for the treatment date field
    private final MutableLiveData<String> treatmentDateError =
            new MutableLiveData<>(null);



    /**
     * Sets the selected insurance date (in milliseconds).
     * Clears the insurance date error when a valid date is selected.
     *
     * @param millis selected date in milliseconds
     */
    public void setSelectedInsuranceDateMillis(long millis) {
        selectedInsuranceDateMillis = millis;
        insuranceDateError.setValue(null);
    }

    /**
     * Sets the selected test date (in milliseconds).
     * Clears the test date error when a valid date is selected.
     *
     * @param millis selected date in milliseconds
     */
    public void setSelectedTestDateMillis(long millis) {
        selectedTestDateMillis = millis;
        testDateError.setValue(null);
    }

    /**
     * Sets the selected 10K treatment date (in milliseconds).
     * Clears the treatment date error when a valid date is selected.
     *
     * @param millis selected date in milliseconds
     */
    public void setSelectedTreatDateMillis(long millis) {
        selectedTreatDateMillis = millis;
        treatmentDateError.setValue(null);
    }


    /**
     * @return selected insurance date in milliseconds, or -1 if not selected
     */
    public long getSelectedInsuranceDateMillis() {
        return selectedInsuranceDateMillis;
    }


    /**
     * @return selected test date in milliseconds, or -1 if not selected
     */
    public long getSelectedTestDateMillis() {
        return selectedTestDateMillis;
    }

    /**
     * @return selected 10K treatment date in milliseconds, or -1 if not selected
     */
    public long getSelectedTreatDateMillis() {
        return selectedTreatDateMillis;
    }




    /**
     * @return LiveData that emits insurance date validation errors
     */
    public LiveData<String> getInsuranceDateError() {
        return insuranceDateError;
    }

    /**
     * @return LiveData that emits test date validation errors
     */
    public LiveData<String> getTestDateError() { return testDateError; }

    /**
     * @return LiveData that emits treatment date validation errors
     */
    public LiveData<String> getTreatDateError() { return treatmentDateError; }




    /**
     * Validates that all required dates were selected.
     *
     * If a date is missing:
     * - Sets an appropriate error message
     * - Marks the validation as failed
     *
     * @return true if all dates are valid, false otherwise
     */
    public boolean validateDates() {
        boolean ok = true;

        if (selectedInsuranceDateMillis == -1) {
            insuranceDateError.setValue("בחר תאריך ביטוח");
            ok = false;
        }

        if (selectedTestDateMillis == -1) {
            testDateError.setValue("בחר תאריך טסט");
            ok = false;
        }
        if (selectedTreatDateMillis== -1){
            treatmentDateError.setValue("בחר תאריך טיפול 10K");
            ok = false;
        }

        return ok;
    }
}
