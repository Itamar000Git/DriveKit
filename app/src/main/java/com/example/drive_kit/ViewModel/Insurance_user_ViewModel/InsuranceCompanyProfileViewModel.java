//package com.example.drive_kit.ViewModel.Insurance_user_ViewModel;
//
//import androidx.annotation.NonNull;
//import androidx.lifecycle.LiveData;
//import androidx.lifecycle.MutableLiveData;
//import androidx.lifecycle.ViewModel;
//
//import com.example.drive_kit.Data.Repository.InsuranceCompanyProfileRepository;
//import com.example.drive_kit.Model.InsuranceCompany;
//
///**
// * InsuranceCompanyProfileViewModel
// *
// * ViewModel for the insurance company profile screen.
// * Responsibilities:
// * - Load company data from repository
// * - Expose UI state via LiveData (company, loading, error)
// */
//public class InsuranceCompanyProfileViewModel extends ViewModel {
//
//    private final InsuranceCompanyProfileRepository repo = new InsuranceCompanyProfileRepository();
//
//    private final MutableLiveData<InsuranceCompany> company = new MutableLiveData<>(null);
//    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
//    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);
//
//    public LiveData<InsuranceCompany> getCompany() {
//        return company;
//    }
//
//    public LiveData<Boolean> getLoading() {
//        return loading;
//    }
//
//    public LiveData<String> getErrorMessage() {
//        return errorMessage;
//    }
//
//    /**
//     * Loads the company profile by companyId.
//     * Activity should call this once (onCreate / onResume).
//     */
//    public void loadCompany(@NonNull String companyId) {
//        loading.setValue(true);
//        errorMessage.setValue(null);
//
//        repo.loadCompany(companyId, new InsuranceCompanyProfileRepository.CompanyCallback() {
//            @Override
//            public void onSuccess(InsuranceCompany result) {
//                loading.setValue(false);
//                company.setValue(result);
//            }
//
//            @Override
//            public void onError(Exception e) {
//                loading.setValue(false);
//                errorMessage.setValue(e != null && e.getMessage() != null ? e.getMessage() : "Unknown error");
//            }
//        });
//    }
//}

package com.example.drive_kit.ViewModel.Insurance_user_ViewModel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.drive_kit.Data.Repository.InsuranceCompaniesRepository;
import com.example.drive_kit.Model.InsuranceCompany;

public class InsuranceCompanyProfileViewModel extends ViewModel {

    private final InsuranceCompaniesRepository repo = new InsuranceCompaniesRepository();

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);
    private final MutableLiveData<InsuranceCompany> company = new MutableLiveData<>(null);

    // ✅ NEW: logo url
    private final MutableLiveData<String> companyLogoUrl = new MutableLiveData<>("");

    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<InsuranceCompany> getCompany() { return company; }

    // ✅ NEW
    public LiveData<String> getCompanyLogoUrl() { return companyLogoUrl; }

    public void loadCompany(String companyDocId) {
        String cid = safe(companyDocId);
        if (cid.isEmpty()) {
            company.setValue(null);
            companyLogoUrl.setValue("");
            return;
        }

        loading.setValue(true);
        errorMessage.setValue(null);

        repo.getCompanyById(cid, new InsuranceCompaniesRepository.CompanyCallback() {
            @Override
            public void onSuccess(@NonNull InsuranceCompany c) {
                loading.postValue(false);
                company.postValue(c);

                String logo = (c.getLogoUrl() == null) ? "" : c.getLogoUrl().trim();
                companyLogoUrl.postValue(logo);
            }

            @Override
            public void onError(@NonNull Exception e) {
                loading.postValue(false);
                company.postValue(null);
                companyLogoUrl.postValue("");
                errorMessage.postValue(e.getMessage());
            }
        });
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}

