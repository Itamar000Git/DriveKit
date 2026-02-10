package com.example.drive_kit.ViewModel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.drive_kit.Data.Repository.InsuranceCompaniesRepository;


/**
 * InsuranceHomeViewModel
 *
 * ViewModel for InsuranceHomeActivity.
 *
 * Responsibilities:
 * - Load company name from repository
 * - Expose the welcome text via LiveData
 *
 * UI is not handled here:
 * - No setText on views
 * - No navigation
 */
public class InsuranceHomeViewModel extends ViewModel {

    private final InsuranceCompaniesRepository repo = new InsuranceCompaniesRepository();

    private final MutableLiveData<String> welcomeText = new MutableLiveData<>(" חברה");

    public LiveData<String> getWelcomeText() {
        return welcomeText;
    }

    /**
     * Initializes the screen data.
     * Behavior matches your original Activity:
     * - If companyId is missing -> "שלום, חברה"
     * - If company exists and has name -> "שלום, <name>"
     * - Else -> "שלום, <companyId>"
     */
    public void loadWelcomeText(@NonNull String companyId) {
        String id = safe(companyId);
        if (id.isEmpty()) {
            welcomeText.setValue(" חברה");
            return;
        }

        repo.getCompanyName(id, new InsuranceCompaniesRepository.CompanyNameCallback() {
            @Override
            public void onSuccess(String companyName) {
                if (companyName == null || companyName.trim().isEmpty()) {
                    welcomeText.setValue(" " + id);
                } else {
                    welcomeText.setValue(" " + companyName.trim());
                }
            }

            @Override
            public void onError(@NonNull Exception e) {
                // Same fallback as original code
                welcomeText.setValue(" " + id);
            }
        });
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
