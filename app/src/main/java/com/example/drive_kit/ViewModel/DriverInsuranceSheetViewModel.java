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

public class DriverInsuranceSheetViewModel extends ViewModel {

    private final DriverRepository driverRepo = new DriverRepository();
    private final InsuranceInquiryRepository inquiryRepo = new InsuranceInquiryRepository();

    private final MutableLiveData<String> toastMessage = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> sendSuccess = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> sending = new MutableLiveData<>(false);

    public LiveData<String> getToastMessage() { return toastMessage; }
    public LiveData<Boolean> getSendSuccess() { return sendSuccess; }
    public LiveData<Boolean> getSending() { return sending; }

    /**
     * ✅ UPDATED:
     * companyIdHp  = h_p / מספר (לשדה companyId במסמך inquiry)
     * companyDocId = docId של המסמך ב-insurance_companies (aig/clal/...)
     */
    public void sendMyDetails(
            @NonNull String companyIdHp,
            @NonNull String companyDocId,
            @NonNull String companyName
    ) {

        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null || userId.trim().isEmpty()) {
            toastMessage.setValue("לא נמצא משתמש מחובר");
            return;
        }

        String hp = safe(companyIdHp).toLowerCase(Locale.ROOT);
        String docId = safe(companyDocId).toLowerCase(Locale.ROOT);
        String cname = safe(companyName);

        if (docId.isEmpty()) {
            toastMessage.setValue("חסר companyDocId (docId של החברה)");
            return;
        }
        // hp הוא אופציונלי (אבל נוח). אם אין h_p ניפול חזרה ל-docId
        if (hp.isEmpty()) hp = docId;

        sending.setValue(true);
        sendSuccess.setValue(false);

        String finalHp = hp;
        driverRepo.getDriverById(userId, new DriverRepository.DriverCallback() {
            @Override
            public void onSuccess(Driver d) {
                String driverName = "";
                String driverPhone = "";
                String driverEmail = "";
                String carNumber = "";
                String carModel = "";

                if (d != null) {
                    String first = d.getFirstName() == null ? "" : d.getFirstName().trim();
                    String last  = d.getLastName() == null ? "" : d.getLastName().trim();
                    driverName = (first + " " + last).trim();

                    driverPhone = d.getPhone() == null ? "" : d.getPhone().trim();
                    driverEmail = d.getEmail() == null ? "" : d.getEmail().trim();

                    carNumber = (d.getCar() != null && d.getCar().getCarNumber() != null)
                            ? d.getCar().getCarNumber().trim() : "";

                    carModel = (d.getCar() != null && d.getCar().getCarModel() != null)
                            ? d.getCar().getCarModel().name() : "";
                }

                // ✅ IMPORTANT: call the NEW overload that includes companyDocId
                inquiryRepo.logInquiry(
                        userId,
                        finalHp,                 // companyId (h_p) - מידע/תצוגה
                        docId,              // ✅ companyDocId (docId אמיתי) - חובה ל-rules
                        cname,
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
