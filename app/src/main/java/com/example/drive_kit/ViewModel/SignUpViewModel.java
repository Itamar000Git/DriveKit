//package com.example.drive_kit.ViewModel;
//
//import android.net.Uri;
//
//import androidx.lifecycle.LiveData;
//import androidx.lifecycle.MutableLiveData;
//import androidx.lifecycle.ViewModel;
//
///**
// * SignUpViewModel is responsible for holding and validating
// * the data related to the signup screen.
// *
// * This ViewModel:
// * - Stores the selected dates (insurance, test, 10K treatment)
// * - Validates that all required dates were selected
// * - Exposes error messages using LiveData
// *
// * IMPORTANT:
// * This class does NOT know anything about UI elements (EditText, Buttons, etc.)
// */
//public class  SignUpViewModel extends ViewModel {
//
//    // Stores the selected insurance date in milliseconds
//    // -1 means: no date was selected yet
//    private long selectedInsuranceDateMillis = -1;
//
//    // Stores the selected test date in milliseconds
//    private long selectedTestDateMillis = -1;
//
//    // Stores the selected 10K treatment date in milliseconds
//    private long selectedTreatDateMillis = -1;
//
//
//
//
//    // LiveData that holds an error message for the insurance date field
//    // null means: no error
//    private final MutableLiveData<String> insuranceDateError =
//            new MutableLiveData<>(null);
//
//    // LiveData that holds an error message for the test date field
//    private final MutableLiveData<String> testDateError =
//            new MutableLiveData<>(null);
//
//    // LiveData that holds an error message for the treatment date field
//    private final MutableLiveData<String> treatmentDateError =
//            new MutableLiveData<>(null);
//
//
//
//
//    // =========================
//    // NEW: Car photo state (MVVM)
//    // =========================
//
//    // Stores the selected car photo Uri (from gallery or camera)
//    // null means: no photo selected yet
//    private final MutableLiveData<Uri> carPhotoUri =
//            new MutableLiveData<>(null);
//
//    // LiveData that holds an error message for the car photo field (optional)
//    // null means: no error
//    private final MutableLiveData<String> carPhotoError =
//            new MutableLiveData<>(null);
//
//    /**
//     * Sets the selected car photo Uri.
//     * Clears the car photo error when a valid Uri is selected.
//     *
//     * @param uri selected image uri (gallery/camera)
//     */
//    public void setCarPhotoUri(Uri uri) {
//        carPhotoUri.setValue(uri);
//        carPhotoError.setValue(null);
//    }
//
//    /**
//     * @return LiveData containing the selected car photo uri
//     */
//    public LiveData<Uri> getCarPhotoUri() {
//        return carPhotoUri;
//    }
//
//    /**
//     * @return LiveData that emits car photo validation errors (optional)
//     */
//    public LiveData<String> getCarPhotoError() {
//        return carPhotoError;
//    }
//
//
//
//
//    /**
//     * Sets the selected insurance date (in milliseconds).
//     * Clears the insurance date error when a valid date is selected.
//     *
//     * @param millis selected date in milliseconds
//     */
//    public void setSelectedInsuranceDateMillis(long millis) {
//        selectedInsuranceDateMillis = millis;
//        insuranceDateError.setValue(null);
//    }
//
//    /**
//     * Sets the selected test date (in milliseconds).
//     * Clears the test date error when a valid date is selected.
//     *
//     * @param millis selected date in milliseconds
//     */
//    public void setSelectedTestDateMillis(long millis) {
//        selectedTestDateMillis = millis;
//        testDateError.setValue(null);
//    }
//
//    /**
//     * Sets the selected 10K treatment date (in milliseconds).
//     * Clears the treatment date error when a valid date is selected.
//     *
//     * @param millis selected date in milliseconds
//     */
//    public void setSelectedTreatDateMillis(long millis) {
//        selectedTreatDateMillis = millis;
//        treatmentDateError.setValue(null);
//    }
//
//
//    /**
//     * @return selected insurance date in milliseconds, or -1 if not selected
//     */
//    public long getSelectedInsuranceDateMillis() {
//        return selectedInsuranceDateMillis;
//    }
//
//
//    /**
//     * @return selected test date in milliseconds, or -1 if not selected
//     */
//    public long getSelectedTestDateMillis() {
//        return selectedTestDateMillis;
//    }
//
//    /**
//     * @return selected 10K treatment date in milliseconds, or -1 if not selected
//     */
//    public long getSelectedTreatDateMillis() {
//        return selectedTreatDateMillis;
//    }
//
//
//
//
//    /**
//     * @return LiveData that emits insurance date validation errors
//     */
//    public LiveData<String> getInsuranceDateError() {
//        return insuranceDateError;
//    }
//
//    /**
//     * @return LiveData that emits test date validation errors
//     */
//    public LiveData<String> getTestDateError() { return testDateError; }
//
//    /**
//     * @return LiveData that emits treatment date validation errors
//     */
//    public LiveData<String> getTreatDateError() { return treatmentDateError; }
//
//
//
//
//    /**
//     * Validates that all required dates were selected.
//     *
//     * If a date is missing:
//     * - Sets an appropriate error message
//     * - Marks the validation as failed
//     *
//     * @return true if all dates are valid, false otherwise
//     */
//    public boolean validateDates() {
//        boolean ok = true;
//
//        if (selectedInsuranceDateMillis == -1) {
//            insuranceDateError.setValue("בחר תאריך ביטוח");
//            ok = false;
//        }
//
//        if (selectedTestDateMillis == -1) {
//            testDateError.setValue("בחר תאריך טסט");
//            ok = false;
//        }
//        if (selectedTreatDateMillis== -1){
//            treatmentDateError.setValue("בחר תאריך טיפול 10K");
//            ok = false;
//        }
//
//        return ok;
//    }
//
//    /**
//     * OPTIONAL:
//     * If you decide car photo must be required, call this validation from Activity
//     * and set error here to be observed.
//     */
//    public boolean validateCarPhotoRequired() {
//        Uri uri = carPhotoUri.getValue();
//        if (uri == null) {
//            carPhotoError.setValue("נא לבחור / לצלם תמונת רכב");
//            return false;
//        }
//        carPhotoError.setValue(null);
//        return true;
//    }
//}


package com.example.drive_kit.ViewModel;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.drive_kit.Data.Repository.SignUpInsuranceCompaniesRepository;

import java.util.ArrayList;
import java.util.List;

public class SignUpViewModel extends ViewModel {

    // =========================
    // Existing fields (unchanged)
    // =========================
    private long selectedInsuranceDateMillis = -1;
    private long selectedTestDateMillis = -1;
    private long selectedTreatDateMillis = -1;

    private final MutableLiveData<String> insuranceDateError = new MutableLiveData<>(null);
    private final MutableLiveData<String> testDateError = new MutableLiveData<>(null);
    private final MutableLiveData<String> treatmentDateError = new MutableLiveData<>(null);

    private final MutableLiveData<Uri> carPhotoUri = new MutableLiveData<>(null);
    private final MutableLiveData<String> carPhotoError = new MutableLiveData<>(null);

    public void setCarPhotoUri(Uri uri) {
        carPhotoUri.setValue(uri);
        carPhotoError.setValue(null);
    }

    public LiveData<Uri> getCarPhotoUri() { return carPhotoUri; }
    public LiveData<String> getCarPhotoError() { return carPhotoError; }

    public void setSelectedInsuranceDateMillis(long millis) {
        selectedInsuranceDateMillis = millis;
        insuranceDateError.setValue(null);
    }

    public void setSelectedTestDateMillis(long millis) {
        selectedTestDateMillis = millis;
        testDateError.setValue(null);
    }

    public void setSelectedTreatDateMillis(long millis) {
        selectedTreatDateMillis = millis;
        treatmentDateError.setValue(null);
    }

    public long getSelectedInsuranceDateMillis() { return selectedInsuranceDateMillis; }
    public long getSelectedTestDateMillis() { return selectedTestDateMillis; }
    public long getSelectedTreatDateMillis() { return selectedTreatDateMillis; }

    public LiveData<String> getInsuranceDateError() { return insuranceDateError; }
    public LiveData<String> getTestDateError() { return testDateError; }
    public LiveData<String> getTreatDateError() { return treatmentDateError; }

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
        if (selectedTreatDateMillis == -1) {
            treatmentDateError.setValue("בחר תאריך טיפול 10K");
            ok = false;
        }
        return ok;
    }

    public boolean validateCarPhotoRequired() {
        Uri uri = carPhotoUri.getValue();
        if (uri == null) {
            carPhotoError.setValue("נא לבחור / לצלם תמונת רכב");
            return false;
        }
        carPhotoError.setValue(null);
        return true;
    }

    // =========================
    // ✅ NEW: Insurance companies (Firebase moved out of Activity)
    // =========================
    private final SignUpInsuranceCompaniesRepository insuranceRepo = new SignUpInsuranceCompaniesRepository();

    private final MutableLiveData<List<SignUpInsuranceCompaniesRepository.CompanyItem>> insuranceCompanies =
            new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<SignUpInsuranceCompaniesRepository.CompanyDetails> selectedCompanyDetails =
            new MutableLiveData<>(null);

    private final MutableLiveData<String> insuranceCompaniesError =
            new MutableLiveData<>(null);

    public LiveData<List<SignUpInsuranceCompaniesRepository.CompanyItem>> getInsuranceCompanies() {
        return insuranceCompanies;
    }

    public LiveData<SignUpInsuranceCompaniesRepository.CompanyDetails> getSelectedCompanyDetails() {
        return selectedCompanyDetails;
    }

    public LiveData<String> getInsuranceCompaniesError() {
        return insuranceCompaniesError;
    }

    public void loadInsuranceCompanies() {
        insuranceRepo.loadCompanies(new SignUpInsuranceCompaniesRepository.CompaniesCallback() {
            @Override
            public void onSuccess(@NonNull List<SignUpInsuranceCompaniesRepository.CompanyItem> items) {
                insuranceCompanies.postValue(items);
                insuranceCompaniesError.postValue(null);
            }

            @Override
            public void onError(@NonNull Exception e) {
                insuranceCompanies.postValue(new ArrayList<>());
                insuranceCompaniesError.postValue("שגיאה בטעינת חברות הביטוח");
            }
        });
    }

    public void loadInsuranceCompanyDetails(@NonNull String companyDocId) {
        String id = companyDocId == null ? "" : companyDocId.trim();
        if (id.isEmpty()) {
            selectedCompanyDetails.postValue(null);
            return;
        }

        insuranceRepo.loadCompanyDetails(id, new SignUpInsuranceCompaniesRepository.CompanyDetailsCallback() {
            @Override
            public void onSuccess(@NonNull SignUpInsuranceCompaniesRepository.CompanyDetails details) {
                selectedCompanyDetails.postValue(details);
                insuranceCompaniesError.postValue(null);
            }

            @Override
            public void onError(@NonNull Exception e) {
                selectedCompanyDetails.postValue(null);
                insuranceCompaniesError.postValue("שגיאה בטעינת פרטי החברה");
            }
        });
    }
}