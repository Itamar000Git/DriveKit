package com.example.drive_kit.ViewModel.Insurance_user_ViewModel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.drive_kit.Data.Repository.InsuranceInquiryRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * InsuranceInquiriesViewModel
 *
 * ViewModel for the insurance inquiries list screen.
 *
 * Responsibilities:
 * - Load inquiries for a given companyId + status (via InsuranceInquiryRepository)
 * - Mark an inquiry as contacted (via InsuranceInquiryRepository)
 * - Expose UI state via LiveData: list + toast/error message
 */
public class InsuranceInquiriesViewModel extends ViewModel {

    private final InsuranceInquiryRepository repo = new InsuranceInquiryRepository();

    private String currentStatus = "new";
    private String currentCompanyId = "";

    private final MutableLiveData<List<Map<String, Object>>> inquiries =
            new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<String> toastMessage = new MutableLiveData<>(null);

    public LiveData<List<Map<String, Object>>> getInquiries() {
        return inquiries;
    }

    public LiveData<String> getToastMessage() {
        return toastMessage;
    }

    /**
     * Loads inquiries for company + status.
     */
    public void load(@NonNull String companyId, @NonNull String status) {
        currentCompanyId = safe(companyId);
        currentStatus = safe(status).isEmpty() ? "new" : safe(status).toLowerCase();

        if (currentCompanyId.isEmpty()) {
            toastMessage.setValue("חסר insuranceCompanyId");
            inquiries.setValue(new ArrayList<>());
            return;
        }

        repo.loadInquiriesForCompanyByStatus(currentCompanyId, currentStatus, new InsuranceInquiryRepository.LoadInquiriesCallback() {
            @Override
            public void onSuccess(List<Map<String, Object>> result) {
                inquiries.setValue(result == null ? new ArrayList<>() : result);
            }

            @Override
            public void onError(Exception e) {
                String m = (e != null && e.getMessage() != null) ? e.getMessage() : "שגיאה בטעינת פניות";
                toastMessage.setValue(m);
            }
        });
    }

    /**
     * Marks inquiry as contacted, then refreshes current list.
     * - If we are on "new" list: item disappears after refresh.
     * - If we are on "contacted" list: usually button disabled there anyway.
     */
    public void markContacted(@NonNull String docId) {
        String id = safe(docId);
        if (id.isEmpty()) {
            toastMessage.setValue("שגיאה בעדכון");
            return;
        }
        if (!currentCompanyId.isEmpty()) load(currentCompanyId, "new");

        repo.markAsContacted(id, new InsuranceInquiryRepository.InquiryCallback() {
            @Override
            public void onSuccess() {
                toastMessage.setValue("עודכן ל- contacted");


                if (!currentCompanyId.isEmpty()) {
                    load(currentCompanyId, "new");
                }
            }

            @Override
            public void onError(Exception e) {
                toastMessage.setValue("שגיאה בעדכון");
            }
        });
    }


    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String status) {
        this.currentStatus = safe(status).isEmpty() ? "new" : safe(status).toLowerCase();
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
