//
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
//public class InsuranceHomeViewModel extends ViewModel {
//
//    private final InsuranceCompaniesRepository repo = new InsuranceCompaniesRepository();
//
//    private final MutableLiveData<String> welcomeText = new MutableLiveData<>("שלום, חברה");
//    private final MutableLiveData<String> internalCompanyId = new MutableLiveData<>("");
//    private final MutableLiveData<String> companyLogoUrl = new MutableLiveData<>("");
//    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);
//
//    public LiveData<String> getWelcomeText() {
//        return welcomeText;
//    }
//
//    public LiveData<String> getInternalCompanyId() {
//        return internalCompanyId;
//    }
//
//    // ✅ NEW
//    public LiveData<String> getCompanyLogoUrl() {
//        return companyLogoUrl;
//    }
//
//    public LiveData<String> getErrorMessage() {
//        return errorMessage;
//    }
//
//    public void loadCompanyForHome(String companyDocId) {
//        String docId = safe(companyDocId);
//        if (docId.isEmpty()) {
//            welcomeText.setValue("שלום, חברה");
//            internalCompanyId.setValue("");
//            companyLogoUrl.setValue("");
//            return;
//        }
//
//        repo.getCompanyById(docId, new InsuranceCompaniesRepository.CompanyCallback() {
//            @Override
//            public void onSuccess(@NonNull InsuranceCompany company) {
//                String name = company.getName() == null ? "" : company.getName().trim();
//                String id = company.getId() == null ? "" : company.getId().trim();
//
//                welcomeText.setValue(name.isEmpty() ? ("שלום, " + docId) : ("שלום, " + name));
//                internalCompanyId.setValue(id);
//
//                // ✅ NEW: assumes InsuranceCompany has getLogoUrl()
//                String logo = "";
//                if (company.getLogoUrl() != null) logo = company.getLogoUrl().trim();
//                companyLogoUrl.setValue(logo);
//            }
//
//            @Override
//            public void onError(Exception e) {
//                welcomeText.setValue("שלום, " + docId);
//                internalCompanyId.setValue(docId);
//                companyLogoUrl.setValue("");
//                errorMessage.setValue(e == null ? "Unknown error" : e.getMessage());
//            }
//        });
//    }
//
//    private String safe(String s) {
//        return s == null ? "" : s.trim();
//    }
//}


package com.example.drive_kit.ViewModel.Insurance_user_ViewModel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.drive_kit.Data.Repository.InsuranceCompaniesRepository;
import com.example.drive_kit.Model.InsuranceCompany;
import com.google.firebase.firestore.ListenerRegistration;

public class InsuranceHomeViewModel extends ViewModel {

    private final InsuranceCompaniesRepository repo = new InsuranceCompaniesRepository();

    private final MutableLiveData<String> welcomeText = new MutableLiveData<>("שלום, חברה");
    private final MutableLiveData<String> internalCompanyId = new MutableLiveData<>("");
    private final MutableLiveData<String> companyLogoUrl = new MutableLiveData<>("");
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);

    // ✅ NEW: badge count state
    private final MutableLiveData<Integer> newInquiriesCount = new MutableLiveData<>(0);

    // ✅ NEW: listener holder (VM controls lifecycle)
    private ListenerRegistration inquiriesReg;

    public LiveData<String> getWelcomeText() { return welcomeText; }
    public LiveData<String> getInternalCompanyId() { return internalCompanyId; }
    public LiveData<String> getCompanyLogoUrl() { return companyLogoUrl; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    // ✅ NEW
    public LiveData<Integer> getNewInquiriesCount() { return newInquiriesCount; }

    public void loadCompanyForHome(String companyDocId) {
        String docId = safe(companyDocId);
        if (docId.isEmpty()) {
            welcomeText.setValue("שלום, חברה");
            internalCompanyId.setValue("");
            companyLogoUrl.setValue("");
            return;
        }

        repo.getCompanyById(docId, new InsuranceCompaniesRepository.CompanyCallback() {
            @Override
            public void onSuccess(@NonNull InsuranceCompany company) {
                String name = company.getName() == null ? "" : company.getName().trim();
                String id = company.getId() == null ? "" : company.getId().trim();

                welcomeText.setValue(name.isEmpty() ? ("שלום, " + docId) : ("שלום, " + name));
                internalCompanyId.setValue(id);

                String logo = "";
                if (company.getLogoUrl() != null) logo = company.getLogoUrl().trim();
                companyLogoUrl.setValue(logo);
            }

            @Override
            public void onError(@NonNull Exception e) {
                welcomeText.setValue("שלום, " + docId);
                internalCompanyId.setValue(docId);
                companyLogoUrl.setValue("");
                errorMessage.setValue(e.getMessage());
            }
        });
    }

    // =========================================================
    // ✅ NEW: badge listener (only "new")
    // =========================================================

    public void startNewInquiriesListener(String companyDocId) {
        stopNewInquiriesListener();

        String cid = safe(companyDocId);
        if (cid.isEmpty()) {
            newInquiriesCount.setValue(0);
            return;
        }

        inquiriesReg = repo.listenNewInquiriesCount(cid, new InsuranceCompaniesRepository.CountCallback() {
            @Override
            public void onCount(int count) {
                newInquiriesCount.postValue(count);
            }

            @Override
            public void onError(@NonNull Exception e) {
                newInquiriesCount.postValue(0);
            }
        });
    }

    public void stopNewInquiriesListener() {
        if (inquiriesReg != null) {
            inquiriesReg.remove();
            inquiriesReg = null;
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        stopNewInquiriesListener();
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}