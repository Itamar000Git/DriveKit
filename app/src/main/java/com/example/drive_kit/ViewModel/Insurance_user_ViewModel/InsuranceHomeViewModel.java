package com.example.drive_kit.ViewModel.Insurance_user_ViewModel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.drive_kit.Data.Repository.InsuranceCompaniesRepository;
import com.example.drive_kit.View.InsuranceCompany;

/**
 * InsuranceHomeViewModel
 *
 * Loads insurance company info for the home screen.
 * - welcomeText: "שלום, <name>"
 * - internalCompanyId: value of field "id" in Firestore (fallback to docId)
 */
public class InsuranceHomeViewModel extends ViewModel {

    private final InsuranceCompaniesRepository repo = new InsuranceCompaniesRepository();

    private final MutableLiveData<String> welcomeText = new MutableLiveData<>("שלום, חברה");
    private final MutableLiveData<String> internalCompanyId = new MutableLiveData<>("");
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);

    public LiveData<String> getWelcomeText() {
        return welcomeText;
    }

    public LiveData<String> getInternalCompanyId() {
        return internalCompanyId;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Loads company and updates both welcome text and internal id.
     * @param companyDocId Firestore document id (the one you pass in Intent)
     */
    public void loadCompanyForHome(String companyDocId) {
        String docId = safe(companyDocId);
        if (docId.isEmpty()) {
            welcomeText.setValue("שלום, חברה");
            internalCompanyId.setValue("");
            return;
        }

        repo.getCompanyById(docId, new InsuranceCompaniesRepository.CompanyCallback() {
            @Override
            public void onSuccess(@NonNull InsuranceCompany company) {
                // Company.getId() in your repo is the INTERNAL id (field "id") or docId fallback
                String name = company.getName() == null ? "" : company.getName().trim();
                String id = company.getId() == null ? "" : company.getId().trim();

                welcomeText.setValue(name.isEmpty() ? ("שלום, " + docId) : ("שלום, " + name));
                internalCompanyId.setValue(id);
            }

            @Override
            public void onError(Exception e) {
                // Keep fallback UI stable
                welcomeText.setValue("שלום, " + docId);
                internalCompanyId.setValue(docId); // fallback: show doc id if load failed
                errorMessage.setValue(e == null ? "Unknown error" : e.getMessage());
            }
        });
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
