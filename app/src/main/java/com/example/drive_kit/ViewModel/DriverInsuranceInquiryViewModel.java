package com.example.drive_kit.ViewModel;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.drive_kit.Data.Repository.InsuranceInquiryRepository;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DriverInsuranceInquiryViewModel extends ViewModel {

    private static final String TAG = "DRV_INQ_VM";

    private final InsuranceInquiryRepository inquiryRepo = new InsuranceInquiryRepository();

    private final MutableLiveData<List<String>> companyDisplayList =
            new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<String> toastMessage = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> sent = new MutableLiveData<>(false);

    // display -> meta (docId + hp + name)
    private final Map<String, CompanyMeta> displayToMeta = new HashMap<>();

    public LiveData<List<String>> getCompanyDisplayList() { return companyDisplayList; }
    public LiveData<String> getToastMessage() { return toastMessage; }
    public LiveData<Boolean> getSent() { return sent; }

    /**
     * Loads companies for the dropdown.
     * We need BOTH: docId + h_p.
     */
    public void loadCompanies() {
        sent.setValue(false);

        FirebaseFirestore.getInstance()
                .collection("insurance_companies")
                .whereEqualTo("category", "car")
                .get()
                .addOnSuccessListener(qs -> {
                    displayToMeta.clear();
                    List<String> displays = new ArrayList<>();

                    Log.d(TAG, "loadCompanies docs=" + qs.size());

                    for (com.google.firebase.firestore.DocumentSnapshot doc : qs.getDocuments()) {
                        String docId = safe(doc.getId()).toLowerCase(Locale.ROOT); // ✅ companyDocId
                        String name = safe(doc.getString("name"));
                        if (name.isEmpty()) name = docId;

                        String hp = "";
                        Object hpObj = doc.get("h_p");
                        if (hpObj != null) hp = safe(hpObj.toString());

                        // display shown to user
                        String display = name + " (" + docId + ")";

                        displayToMeta.put(display, new CompanyMeta(docId, hp, name));
                        displays.add(display);
                    }

                    companyDisplayList.setValue(displays);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "loadCompanies failed", e);
                    toastMessage.setValue("שגיאה בטעינת חברות ביטוח");
                    companyDisplayList.setValue(new ArrayList<>());
                });
    }

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
        if (display.isEmpty()) {
            toastMessage.setValue("נא לבחור חברת ביטוח");
            return;
        }

        CompanyMeta meta = displayToMeta.get(display);
        if (meta == null) {
            Log.e(TAG, "sendInquiry: display not in map: [" + display + "]");
            toastMessage.setValue("נא לבחור חברת ביטוח מהרשימה");
            return;
        }

        String companyDocId = safe(meta.docId).toLowerCase(Locale.ROOT); // ✅ חובה ל-rules
        String hp = safe(meta.hp);

        if (hp.isEmpty()) hp = companyDocId; // fallback
        String companyName = safe(meta.name);

        Log.d(TAG, "sendInquiry uid=" + uid
                + " companyDocId=" + companyDocId
                + " hp=" + hp
                + " name=" + companyName);

        sent.setValue(false);

        // ✅ CALL THE NEW overload (with companyDocId)
        inquiryRepo.logInquiry(
                uid,
                // companyId (h_p) לתצוגה/מידע
                companyDocId,        // ✅ companyDocId לפי rules
                companyName,
                safe(driverName),
                safe(driverPhone),
                safe(driverEmail),
                safe(carNumber),
                safe(carModel),
                safe(message),
                new InsuranceInquiryRepository.InquiryCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "sendInquiry success");
                        toastMessage.setValue("הפרטים נשלחו בהצלחה");
                        sent.setValue(true);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "sendInquiry failed", e);
                        String err = (e != null && e.getMessage() != null && !e.getMessage().trim().isEmpty())
                                ? e.getMessage().trim()
                                : "שגיאה בשליחת הפנייה";
                        toastMessage.setValue(err);
                        sent.setValue(false);
                    }
                }
        );
    }
    private String extractDocIdFromDisplay(String display) {
        if (display == null) return "";
        int l = display.lastIndexOf('(');
        int r = display.lastIndexOf(')');
        if (l < 0 || r < 0 || r <= l) return "";
        return display.substring(l + 1, r).trim(); // docId
    }


    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private static class CompanyMeta {
        final String docId; // insurance_companies docId
        final String hp;    // h_p
        final String name;

        CompanyMeta(String docId, String hp, String name) {
            this.docId = docId;
            this.hp = hp;
            this.name = name;
        }
    }
}
