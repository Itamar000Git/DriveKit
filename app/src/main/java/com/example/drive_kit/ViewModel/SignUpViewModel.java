package com.example.drive_kit.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SignUpViewModel extends ViewModel {

    private long selectedInsuranceDateMillis = -1;
    private long selectedTestDateMillis = -1;


    //will tell us if there an error
    private final MutableLiveData<String> insuranceDateError = new MutableLiveData<>(null);
    private final MutableLiveData<String> testDateError = new MutableLiveData<>(null);

    /**
     * sets the selected insurance date in millis and clears the error message
     * @param millis
     */
    public void setSelectedInsuranceDateMillis(long millis) {
        selectedInsuranceDateMillis = millis;
        insuranceDateError.setValue(null);
    }
    public void setSelectedTestDateMillis(long millis) {
        selectedTestDateMillis = millis;
        testDateError.setValue(null);
    }

    public long getSelectedInsuranceDateMillis() {
        return selectedInsuranceDateMillis;
    }

    public long getSelectedTestDateMillis() {
        return selectedTestDateMillis;
    }

    public LiveData<String> getInsuranceDateError() {
        return insuranceDateError;
    }

    public LiveData<String> getTestDateError() {
        return testDateError;
    }


    /**
     * checks if the selected dates are valid and sets the error messages accordingly
     * and returns true if they are valid
     * @return
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

        return ok;
    }
}
