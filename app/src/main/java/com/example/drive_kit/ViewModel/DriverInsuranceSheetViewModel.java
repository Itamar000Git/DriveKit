package com.example.drive_kit.ViewModel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.drive_kit.Data.Repository.DriverRepository;
import com.example.drive_kit.Data.Repository.InsuranceInquiryRepository;
import com.example.drive_kit.Model.Driver;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

/**
 * DriverInsuranceSheetViewModel
 *
 * ViewModel for the driver's insurance company BottomSheet.
 *
 * Responsibilities:
 * - Validate inputs (userId/companyId)
 * - Fetch latest Driver from Firestore (via DriverRepository)
 * - Log inquiry with full driver details (via InsuranceInquiryRepository)
 *
 * UI stays in the BottomSheet:
 * - Intents (dial/mail/web)
 * - Toast rendering
 * - dismiss()
 */
public class DriverInsuranceSheetViewModel extends ViewModel {

    private final DriverRepository driverRepo = new DriverRepository();
    private final InsuranceInquiryRepository inquiryRepo = new InsuranceInquiryRepository();

    private final MutableLiveData<String> toastMessage = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> sendSuccess = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> sending = new MutableLiveData<>(false);

    public LiveData<String> getToastMessage() {
        return toastMessage;
    }

    public LiveData<Boolean> getSendSuccess() {
        return sendSuccess;
    }

    public LiveData<Boolean> getSending() {
        return sending;
    }

    /**
     * Called when the user clicks "Send my details".
     * This keeps the exact same behavior as your original BottomSheet:
     * - Reads FirebaseAuth uid
     * - Loads driver from Firestore
     * - Builds driver fields (name/phone/email/carNumber/carModel)
     * - Calls inquiryRepo.logInquiry(...) with callback
     */
    public void sendMyDetails(@NonNull String companyId, @NonNull String companyName) {

        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null || userId.trim().isEmpty()) {
            toastMessage.setValue("לא נמצא משתמש מחובר");
            return;
        }

        String cid = safe(companyId);
        if (cid.isEmpty()) {
            toastMessage.setValue("לא נמצא מזהה חברה");
            return;
        }

        sending.setValue(true);
        sendSuccess.setValue(false);

        driverRepo.getDriverById(userId, new DriverRepository.DriverCallback() {
            @Override
            public void onSuccess(Driver d) {
                // Build the same fields as before (no logic change)
                String driverName = "";
                String driverPhone = "";
                String driverEmail = "";
                String carNumber = "";
                String carModel = "";

                if (d != null) {
                    String first = d.getFirstName() == null ? "" : d.getFirstName().trim();
                    String last = d.getLastName() == null ? "" : d.getLastName().trim();
                    driverName = (first + " " + last).trim();

                    driverPhone = d.getPhone() == null ? "" : d.getPhone().trim();
                    driverEmail = d.getEmail() == null ? "" : d.getEmail().trim();

                    carNumber = (d.getCar() != null && d.getCar().getCarNumber() != null)
                            ? d.getCar().getCarNumber().trim() : "";

                    carModel = (d.getCar() != null && d.getCar().getCarModel() != null)
                            ? d.getCar().getCarModel().name() : "";
                }

                inquiryRepo.logInquiry(
                        userId,
                        cid,
                        companyName,
                        driverName,
                        driverPhone,
                        driverEmail,
                        carNumber,
                        carModel,
                        "הנהג ביקש שיחזרו אליו דרך DriveKit",
                        new InsuranceInquiryRepository.InquiryCallback() {
                            @Override
                            public void onSuccess() {
                                sending.setValue(false);
                                sendSuccess.setValue(true);
                            }

                            @Override
                            public void onError(Exception e) {
                                sending.setValue(false);
                                String msg = (e != null && e.getMessage() != null) ? e.getMessage() : "";
                                toastMessage.setValue("שליחה נכשלה: " + msg);
                            }
                        }
                );
            }

            @Override
            public void onError(@NonNull Exception e) {
                sending.setValue(false);
                String msg = (e.getMessage() != null) ? e.getMessage() : "";
                toastMessage.setValue("שגיאה בטעינת פרטי נהג: " + msg);
            }
        });
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
