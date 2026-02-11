//package com.example.drive_kit.ViewModel.Insurance_user_ViewModel;
//
//import androidx.annotation.NonNull;
//import androidx.lifecycle.LiveData;
//import androidx.lifecycle.MutableLiveData;
//import androidx.lifecycle.ViewModel;
//
//import com.example.drive_kit.Data.Repository.InsuranceCompaniesRepository;
//import com.example.drive_kit.Model.InsuranceCompany;
//
///**
// * InsuranceCompanyEditProfileViewModel
// *
// * ViewModel for editing an insurance company profile.
// *
// * Responsibilities:
// * - Load current company data (for prefilling inputs)
// * - Save updated company fields to Firestore
// *
// * UI stays in Activity:
// * - reading EditTexts
// * - showing Toasts
// * - finish()/navigation
// */
//public class InsuranceCompanyEditProfileViewModel extends ViewModel {
//
//    private final InsuranceCompaniesRepository repo = new InsuranceCompaniesRepository();
//
//    private final MutableLiveData<InsuranceCompany> company = new MutableLiveData<>(null);
//    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
//    private final MutableLiveData<Boolean> saving = new MutableLiveData<>(false);
//    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);
//    private final MutableLiveData<Boolean> saveSuccess = new MutableLiveData<>(false);
//
//    public LiveData<InsuranceCompany> getCompany() {
//        return company;
//    }
//
//    public LiveData<Boolean> getLoading() {
//        return loading;
//    }
//
//    public LiveData<Boolean> getSaving() {
//        return saving;
//    }
//
//    public LiveData<String> getErrorMessage() {
//        return errorMessage;
//    }
//
//    public LiveData<Boolean> getSaveSuccess() {
//        return saveSuccess;
//    }
//
//    /**
//     * Loads company data for showing current values in the edit screen.
//     */
//    public void loadCompany(@NonNull String companyId) {
//        String id = safe(companyId);
//        if (id.isEmpty()) {
//            errorMessage.setValue("Missing insuranceCompanyId");
//            return;
//        }
//
//        loading.setValue(true);
//        errorMessage.setValue(null);
//
//        repo.getCompanyById(id, new InsuranceCompaniesRepository.CompanyCallback() {
//            @Override
//            public void onSuccess(@NonNull InsuranceCompany c) {
//                loading.setValue(false);
//                company.setValue(c);
//            }
//
//            @Override
//            public void onError(@NonNull Exception e) {
//                loading.setValue(false);
//                errorMessage.setValue(msg(e, "Failed to load company"));
//            }
//        });
//    }
//
//    /**
//     * Saves the updated editable fields to Firestore.
//     * This does NOT change category/isPartner (same data behavior).
//     */
//    public void saveCompany(
//            @NonNull String companyId,
//            @NonNull String name,
//            @NonNull String phone,
//            @NonNull String email,
//            @NonNull String website
//    ) {
//        String id = safe(companyId);
//        if (id.isEmpty()) {
//            errorMessage.setValue("Missing insuranceCompanyId");
//            return;
//        }
//
//        saving.setValue(true);
//        saveSuccess.setValue(false);
//        errorMessage.setValue(null);
//
//        repo.updateCompanyProfile(id, name, phone, email, website, new InsuranceCompaniesRepository.SimpleCallback() {
//            @Override
//            public void onSuccess() {
//                saving.setValue(false);
//                saveSuccess.setValue(true);
//            }
//
//            @Override
//            public void onError(@NonNull Exception e) {
//                saving.setValue(false);
//                errorMessage.setValue(msg(e, "Failed to save"));
//            }
//        });
//    }
//
//    private String safe(String s) {
//        return s == null ? "" : s.trim();
//    }
//
//    private String msg(Exception e, String fallback) {
//        if (e == null || e.getMessage() == null || e.getMessage().trim().isEmpty()) return fallback;
//        return e.getMessage().trim();
//    }
//}

// InsuranceCompanyEditProfileViewModel.java
package com.example.drive_kit.ViewModel.Insurance_user_ViewModel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import android.net.Uri;
import android.util.Log;

import com.example.drive_kit.Data.Repository.InsuranceCompaniesRepository;
import com.example.drive_kit.Model.InsuranceCompany;

public class InsuranceCompanyEditProfileViewModel extends ViewModel {

    private static final String TAG = "LOGO_FLOW_VM";

    private final InsuranceCompaniesRepository repo = new InsuranceCompaniesRepository();

    private final MutableLiveData<InsuranceCompany> company = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> saving = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> saveSuccess = new MutableLiveData<>(false);

    public LiveData<InsuranceCompany> getCompany() { return company; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<Boolean> getSaving() { return saving; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getSaveSuccess() { return saveSuccess; }

    public void loadCompany(@NonNull String companyId) {
        String id = safe(companyId);
        if (id.isEmpty()) {
            errorMessage.setValue("Missing insuranceCompanyId");
            return;
        }

        Log.d(TAG, "loadCompany id=" + id);

        loading.setValue(true);
        errorMessage.setValue(null);

        repo.getCompanyById(id, new InsuranceCompaniesRepository.CompanyCallback() {
            @Override
            public void onSuccess(@NonNull InsuranceCompany c) {
                Log.d(TAG, "loadCompany success. logoUrl=" + safe(c.getLogoUrl()));
                loading.setValue(false);
                company.setValue(c);
            }

            @Override
            public void onError(@NonNull Exception e) {
                Log.e(TAG, "loadCompany error", e);
                loading.setValue(false);
                errorMessage.setValue(msg(e, "Failed to load company"));
            }
        });
    }

    public void saveCompany(
            @NonNull String companyId,
            @NonNull String name,
            @NonNull String phone,
            @NonNull String email,
            @NonNull String website
    ) {
        String id = safe(companyId);
        if (id.isEmpty()) {
            errorMessage.setValue("Missing insuranceCompanyId");
            return;
        }

        Log.d(TAG, "saveCompany id=" + id);

        saving.setValue(true);
        saveSuccess.setValue(false);
        errorMessage.setValue(null);

        repo.updateCompanyProfile(id, name, phone, email, website, new InsuranceCompaniesRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "saveCompany success");
                saving.setValue(false);
                saveSuccess.setValue(true);
            }

            @Override
            public void onError(@NonNull Exception e) {
                Log.e(TAG, "saveCompany error", e);
                saving.setValue(false);
                errorMessage.setValue(msg(e, "Failed to save"));
            }
        });
    }

    public void uploadLogo(@NonNull String companyId, @NonNull Uri localUri) {
        String id = safe(companyId);
        if (id.isEmpty()) {
            errorMessage.setValue("Missing insuranceCompanyId");
            return;
        }
        if (localUri == null) {
            errorMessage.setValue("localUri is null");
            return;
        }

        Log.d(TAG, "uploadLogo start id=" + id + " uri=" + localUri);

        saving.setValue(true);
        errorMessage.setValue(null);

        repo.uploadCompanyLogoAndSave(id, localUri, new InsuranceCompaniesRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "uploadLogo success -> reload company");
                saving.setValue(false);
                loadCompany(id);
            }

            @Override
            public void onError(@NonNull Exception e) {
                Log.e(TAG, "uploadLogo error", e);
                saving.setValue(false);
                errorMessage.setValue(msg(e, "Failed to upload logo"));
            }
        });
    }

    private String safe(String s) { return s == null ? "" : s.trim(); }

    private String msg(Exception e, String fallback) {
        if (e == null || e.getMessage() == null || e.getMessage().trim().isEmpty()) return fallback;
        return e.getMessage().trim();
    }
}
