package com.example.drive_kit.ViewModel.Insurance_user_ViewModel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.drive_kit.Data.Repository.InsuranceCompanyProfileRepository;
import com.example.drive_kit.View.InsuranceCompany;

/**
 * InsuranceCompanyProfileViewModel
 *
 * ViewModel for the insurance company profile screen.
 * Responsibilities:
 * - Load company data from repository
 * - Expose UI state via LiveData (company, loading, error)
 */
public class InsuranceCompanyProfileViewModel extends ViewModel {

    private final InsuranceCompanyProfileRepository repo = new InsuranceCompanyProfileRepository();

    private final MutableLiveData<InsuranceCompany> company = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);

    public LiveData<InsuranceCompany> getCompany() {
        return company;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Loads the company profile by companyId.
     * Activity should call this once (onCreate / onResume).
     */
    public void loadCompany(@NonNull String companyId) {
        loading.setValue(true);
        errorMessage.setValue(null);

        repo.loadCompany(companyId, new InsuranceCompanyProfileRepository.CompanyCallback() {
            @Override
            public void onSuccess(InsuranceCompany result) {
                loading.setValue(false);
                company.setValue(result);
            }

            @Override
            public void onError(Exception e) {
                loading.setValue(false);
                errorMessage.setValue(e != null && e.getMessage() != null ? e.getMessage() : "Unknown error");
            }
        });
    }
}
