package com.example.drive_kit.ViewModel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.drive_kit.Data.Repository.InsuranceCompaniesRepository;
import com.example.drive_kit.Data.Repository.InsuranceInquiryRepository;
import com.example.drive_kit.View.InsuranceCompany;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DriverInsuranceInquiryViewModel
 *
 * ViewModel for the driver inquiry screen.
 *
 * Responsibilities:
 * - Load insurance companies list for the dropdown (via InsuranceCompaniesRepository)
 * - Keep mapping: display -> companyId
 * - Send an inquiry using InsuranceInquiryRepository
 *
 * UI is not handled here:
 * - No Toast
 * - No finish()
 */
public class DriverInsuranceInquiryViewModel extends ViewModel {

    private final InsuranceCompaniesRepository companiesRepo = new InsuranceCompaniesRepository();
    private final InsuranceInquiryRepository inquiryRepo = new InsuranceInquiryRepository();

    private final MutableLiveData<List<String>> companyDisplayList = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> sent = new MutableLiveData<>(false);

    // display -> companyId
    private final Map<String, String> displayToId = new HashMap<>();

    public LiveData<List<String>> getCompanyDisplayList() {
        return companyDisplayList;
    }

    public LiveData<String> getToastMessage() {
        return toastMessage;
    }

    public LiveData<Boolean> getSent() {
        return sent;
    }

    /**
     * Loads companies for the dropdown using the existing InsuranceCompaniesRepository.
     * Keeps the same display format: "name (docId)".
     */
    public void loadCompanies() {
        companiesRepo.loadCarCompanies(new InsuranceCompaniesRepository.Callback() {
            @Override
            public void onResult(List<InsuranceCompany> companies) {
                displayToId.clear();

                List<String> displays = new ArrayList<>();
                if (companies != null) {
                    for (InsuranceCompany c : companies) {
                        String id = safe(c.getId());
                        String name = safe(c.getName());
                        if (name.isEmpty()) name = id;

                        String display = name + " (" + id + ")";
                        displays.add(display);

                        // map display -> id
                        if (!display.isEmpty() && !id.isEmpty()) {
                            displayToId.put(display, id);
                        }
                    }
                }

                companyDisplayList.setValue(displays);
            }

            @Override
            public void onError(Exception e) {
                toastMessage.setValue("שגיאה בטעינת חברות ביטוח");
            }
        });
    }

    /**
     * Sends the inquiry using the existing InsuranceInquiryRepository.
     * Same behavior as the Activity:
     * - Company is required
     * - Message is optional
     * - Driver details are optional
     */
    public void sendInquiry(@NonNull String userId,
                            @NonNull String selectedDisplay,
                            @NonNull String driverName,
                            @NonNull String driverPhone,
                            @NonNull String driverEmail,
                            @NonNull String carNumber,
                            @NonNull String carModel,
                            @NonNull String message) {

        String uid = safe(userId);
        if (uid.isEmpty()) {
            toastMessage.setValue("משתמש לא מחובר");
            return;
        }

        String display = safe(selectedDisplay);
        String companyId = displayToId.get(display);
        if (companyId == null || companyId.trim().isEmpty()) {
            toastMessage.setValue("נא לבחור חברת ביטוח");
            return;
        }

        sent.setValue(false);

        inquiryRepo.logInquiry(
                uid,
                companyId,
                display, // companyName as shown to user
                safe(driverName),
                safe(driverPhone),
                safe(driverEmail),
                safe(carNumber),
                safe(carModel),
                safe(message),
                new InsuranceInquiryRepository.InquiryCallback() {
                    @Override
                    public void onSuccess() {
                        toastMessage.setValue("הפרטים נשלחו בהצלחה");
                        sent.setValue(true);
                    }

                    @Override
                    public void onError(Exception e) {
                        String err = (e != null && e.getMessage() != null && !e.getMessage().trim().isEmpty())
                                ? e.getMessage()
                                : "שגיאה בשליחת הפנייה";
                        toastMessage.setValue(err);
                    }
                }
        );
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
