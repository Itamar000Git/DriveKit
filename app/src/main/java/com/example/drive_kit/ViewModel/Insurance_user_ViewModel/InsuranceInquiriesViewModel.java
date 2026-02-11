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
 * - Load inquiries for a given companyId (via InsuranceInquiryRepository)
 * - Mark an inquiry as contacted (via InsuranceInquiryRepository)
 * - Expose UI state via LiveData: list + toast/error message
 *
 * UI stays in the Activity:
 * - RecyclerView / Adapter
 * - Toast rendering
 * - finish()
 */
public class InsuranceInquiriesViewModel extends ViewModel {

    private final InsuranceInquiryRepository repo = new InsuranceInquiryRepository();

    private final MutableLiveData<List<Map<String, Object>>> inquiries =
            new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<String> toastMessage = new MutableLiveData<>(null);

    // Keep last used companyId so we can refresh after updates
    private String currentCompanyId = "";

    public LiveData<List<Map<String, Object>>> getInquiries() {
        return inquiries;
    }

    public LiveData<String> getToastMessage() {
        return toastMessage;
    }

    /**
     * Loads inquiries for this company.
     * Activity should call this once after reading companyId from Intent.
     */
    public void load(@NonNull String companyId) {
        currentCompanyId = safe(companyId);
        android.util.Log.d("INS_INQ_DEBUG", "VM load() companyId normalized = [" + currentCompanyId + "]");

        if (currentCompanyId.isEmpty()) {
            toastMessage.setValue("חסר insuranceCompanyId");
            inquiries.setValue(new ArrayList<>());
            return;
        }
        repo.loadInquiriesForCompany(currentCompanyId, new InsuranceInquiryRepository.LoadInquiriesCallback() {
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
     * Marks a single inquiry as contacted, then refreshes the list.
     * Keeps the same behavior as your Activity:
     * - on success -> show toast and reload list
     */
    public void markContacted(@NonNull String docId) {
        String id = safe(docId);
        if (id.isEmpty()) {
            toastMessage.setValue("שגיאה בעדכון");
            return;
        }

        repo.markAsContacted(id, new InsuranceInquiryRepository.InquiryCallback() {
            @Override
            public void onSuccess() {
                toastMessage.setValue("עודכן ל- contacted");
                // refresh list after update (same behavior)
                if (!currentCompanyId.isEmpty()) load(currentCompanyId);
            }

            @Override
            public void onError(Exception e) {
                toastMessage.setValue("שגיאה בעדכון");
            }
        });
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
