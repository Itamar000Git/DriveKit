package com.example.drive_kit.ViewModel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.drive_kit.Data.Repository.InsuranceCompaniesRepository;
import com.example.drive_kit.Data.Repository.InsuranceInquiryRepository;
import com.example.drive_kit.Model.InsuranceCompany;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * DriverInsuranceListViewModel
 *
 * ViewModel for the driver insurance screen that shows a list of insurance companies.
 *
 * Responsibilities:
 * - Load the insurance companies list from InsuranceCompaniesRepository
 * - Expose UI state via LiveData (companies, loading, error)
 * - Handle business logic when a company is clicked:
 *   If the company is a partner -> log an inquiry using InsuranceInquiryRepository
 *
 * Note:
 * - UI actions (showing BottomSheet, navigation, toasts) stay in the Activity.
 */
public class DriverInsuranceListViewModel extends ViewModel {

    private final InsuranceCompaniesRepository companiesRepo = new InsuranceCompaniesRepository();
    private final InsuranceInquiryRepository inquiryRepo = new InsuranceInquiryRepository();

    private final MutableLiveData<List<InsuranceCompany>> companies = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);

    public LiveData<List<InsuranceCompany>> getCompanies() {
        return companies;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Loads the insurance companies for the driver screen.
     * Activity should call this once (e.g., in onCreate).
     */
    public void loadCompanies() {
        loading.setValue(true);
        errorMessage.setValue(null);

        companiesRepo.loadCarCompanies(new InsuranceCompaniesRepository.Callback() {
            @Override
            public void onResult(List<InsuranceCompany> result) {
                loading.setValue(false);
                companies.setValue(result == null ? Collections.emptyList() : result);
            }

            @Override
            public void onError(Exception e) {
                loading.setValue(false);
                errorMessage.setValue(e == null ? "Unknown error" : e.getMessage());
            }
        });
    }

//    /**
//     * Called when the user clicks a company.
//     * Business logic only (no UI here):
//     * - If the company is a partner -> log inquiry.
//     *
//     * @param uid     Firebase user uid (can be null; then we do nothing)
//     * @param company clicked company (must not be null)
//     */
//    public void onCompanyClicked(String uid, @NonNull InsuranceCompany company) {
//        if (!company.isPartner()) return;
//        if (uid == null || uid.trim().isEmpty()) return;
//
//        String companyDocId = safe(company.getDocId()).toLowerCase(Locale.ROOT); // ✅
//        String hp = safe(company.getId()).toLowerCase(Locale.ROOT);             // זה ה-h_p/תצוגה
//
//        inquiryRepo.logInquiry(
//                uid,
//                hp,
//                companyDocId,
//                company.getName(),
//                "", "", "", "", "", "הנהג ביקש שיחזרו אליו דרך DriveKit",
//                new InsuranceInquiryRepository.InquiryCallback() {
//                    @Override public void onSuccess() { /* toast */ }
//                    @Override public void onError(Exception e) { /* toast */ }
//                }
//        );
//    }

    private String safe(String s){ return s==null? "" : s.trim(); }

}
