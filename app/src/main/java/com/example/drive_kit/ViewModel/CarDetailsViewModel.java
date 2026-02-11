//package com.example.drive_kit.ViewModel;
//
//import android.util.Log;
//
//import androidx.lifecycle.LiveData;
//import androidx.lifecycle.MutableLiveData;
//import androidx.lifecycle.ViewModel;
//
//import com.example.drive_kit.Data.Repository.ManualsRepository;
//import com.example.drive_kit.Model.Car;
//import com.example.drive_kit.Model.CarModel;
//import com.example.drive_kit.Model.Driver;
//import com.example.drive_kit.Model.Yad2LinkBuilder;
//import com.google.firebase.firestore.FirebaseFirestore;
//
//import java.util.Locale;
//
//public class CarDetailsViewModel extends ViewModel {
//
//    private static final String TAG = "CAR_DETAILS_VM";
//
//    // UI data
//    private final MutableLiveData<String> infoText = new MutableLiveData<>("");
//    private final MutableLiveData<String> screenError = new MutableLiveData<>("");
//
//    // Manual flow (like DIY)
//    private final MutableLiveData<Boolean> manualLoading = new MutableLiveData<>(false);
//    private final MutableLiveData<String> manualPdfUrl = new MutableLiveData<>("");
//    private final MutableLiveData<String> manualError = new MutableLiveData<>("");
//
//    // Yad2 URL (empty => button disabled)
//    private final MutableLiveData<String> yad2Url = new MutableLiveData<>("");
//
//    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
//    private final ManualsRepository manualsRepo = new ManualsRepository();
//
//    public LiveData<String> getInfoText() { return infoText; }
//    public LiveData<String> getScreenError() { return screenError; }
//
//    public LiveData<Boolean> getManualLoading() { return manualLoading; }
//    public LiveData<String> getManualPdfUrl() { return manualPdfUrl; }
//    public LiveData<String> getManualError() { return manualError; }
//
//    public LiveData<String> getYad2Url() { return yad2Url; }
//
//    /**
//     * Load driver -> show details -> load manual -> build Yad2 url.
//     */
//    public void loadDriverAndManual(String uid) {
//        if (isBlank(uid)) {
//            screenError.setValue("לא מחובר/ת");
//            return;
//        }
//
//        db.collection("drivers")
//                .document(uid)
//                .get()
//                .addOnSuccessListener(doc -> {
//                    Driver d = doc.toObject(Driver.class);
//                    if (d == null || d.getCar() == null) {
//                        infoText.setValue("לא נמצאו נתונים");
//                        yad2Url.setValue(""); // keep disabled
//                        return;
//                    }
//
//                    Car car = d.getCar();
//
//                    // Show existing info (keep same behavior)
//                    String carNumber = safe(car.getCarNum());
//                    String text =
//                            "מספר רכב: " + carNumber + "\n" +
//                                    "ביטוח: " + safe(d.getFormattedInsuranceDate()) + "\n" +
//                                    "טסט: " + safe(d.getFormattedTestDate()) + "\n" +
//                                    "טיפול 10K: " + safe(d.getFormattedTreatDate());
//                    infoText.setValue(text);
//
//                    // Manual + Yad2 are based on manufacturer/model/year
//                    loadManualForCar(car);
//                    buildYad2UrlForCar(car);
//                })
//                .addOnFailureListener(e -> {
//                    screenError.setValue("שגיאה בטעינת נתונים");
//                    yad2Url.setValue("");
//                });
//    }
//
//    /**
//     * Load manual PDF URL from Firebase Storage.
//     * Uses ManualsRepository and your existing year-range logic.
//     */
//    private void loadManualForCar(Car car) {
//        manualPdfUrl.setValue("");
//        manualError.setValue("");
//
//        if (car == null) {
//            manualError.setValue("חסר רכב");
//            return;
//        }
//
//        CarModel manufacturerEnum = car.getCarModel();
//        String manufacturerDocId = (manufacturerEnum == null) ? "" : manufacturerEnum.name().trim();
//
//        // Model must match enum constant name (I10/TUCSON/MAZDA3...)
//        String modelRaw = safe(car.getCarSpecificModel());
//        String modelEnumName = normalizeEnumKey(modelRaw);
//
//        int year = car.getYear();
//
//        Log.d(TAG, "Manual filter: manufacturer=" + manufacturerDocId
//                + ", modelRaw=" + modelRaw
//                + ", modelEnumName=" + modelEnumName
//                + ", year=" + year);
//
//        if (manufacturerEnum == null || isBlank(manufacturerDocId) || isBlank(modelEnumName) || year <= 0) {
//            manualError.setValue("חסרים נתונים לטעינת ספר רכב (יצרן/דגם/שנה)");
//            return;
//        }
//
//        // Pick {from,to} without exposing YearRange
//        int[] range = CarModel.pickRangeForYear(manufacturerEnum, modelEnumName, year);
//        int fromYear = (range == null || range.length < 2) ? -1 : range[0];
//        int toYear   = (range == null || range.length < 2) ? -1 : range[1];
//
//        if (fromYear <= 0 || toYear <= 0) {
//            manualError.setValue("לא נמצא טווח שנים לדגם הזה עבור השנה " + year);
//            return;
//        }
//
//        manualLoading.setValue(true);
//
//        manualsRepo.getManualDownloadUrl(
//                manufacturerDocId,   // manuals_seed/{MANUFACTURER}
//                modelEnumName,       // models[].model
//                fromYear,
//                toYear,
//                new ManualsRepository.ResultCallback<String>() {
//                    @Override
//                    public void onSuccess(String url) {
//                        manualLoading.postValue(false);
//
//                        if (isBlank(url)) {
//                            manualPdfUrl.postValue("");
//                            manualError.postValue("לא נמצא ספר רכב");
//                            return;
//                        }
//
//                        manualPdfUrl.postValue(url);
//                    }
//
//                    @Override
//                    public void onError(Exception e) {
//                        manualLoading.postValue(false);
//                        manualPdfUrl.postValue("");
//                        manualError.postValue(
//                                (e != null && e.getMessage() != null) ? e.getMessage() : "שגיאה בטעינת ספר רכב"
//                        );
//                    }
//                }
//        );
//    }
//
//    /**
//     * Build the Yad2 URL for this car.
//     * If we don't have a mapping -> we keep it empty and the button stays disabled.
//     */
//    private void buildYad2UrlForCar(Car car) {
//        if (car == null) {
//            yad2Url.setValue("");
//            return;
//        }
//
//        CarModel manufacturerEnum = car.getCarModel();
//        String modelRaw = safe(car.getCarSpecificModel());
//        String modelEnumName = normalizeEnumKey(modelRaw);
//        int year = car.getYear();
//
//        String url = Yad2LinkBuilder.build(manufacturerEnum, modelEnumName, year);
//        yad2Url.setValue(url == null ? "" : url);
//
//        Log.d(TAG, "Yad2 filter: manufacturer=" + (manufacturerEnum == null ? "null" : manufacturerEnum.name())
//                + ", modelEnumName=" + modelEnumName
//                + ", year=" + year
//                + ", url=" + url);
//    }
//
//    // Normalize to match enum keys (I10, MAZDA3, CX5...)
//    private String normalizeEnumKey(String s) {
//        if (s == null) return "";
//        return s.trim()
//                .toUpperCase(Locale.ROOT)
//                .replaceAll("[^0-9A-Zא-ת]+", "");
//    }
//
//    private String safe(String s) {
//        return (s == null) ? "" : s.trim();
//    }
//
//    private boolean isBlank(String s) {
//        return s == null || s.trim().isEmpty();
//    }
//}


package com.example.drive_kit.ViewModel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.drive_kit.Data.Repository.ManualsRepository;
import com.example.drive_kit.Model.Car;
import com.example.drive_kit.Model.CarModel;
import com.example.drive_kit.Model.Driver;
import com.example.drive_kit.Model.Yad2LinkBuilder;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;

public class CarDetailsViewModel extends ViewModel {

    private static final String TAG = "CAR_DETAILS_VM";

    // Legacy info text (kept for compatibility)
    private final MutableLiveData<String> infoText = new MutableLiveData<>("");

    // Screen error (toast / fallback)
    private final MutableLiveData<String> screenError = new MutableLiveData<>("");

    // Profile-like fields for the new UI
    private final MutableLiveData<String> carSubtitle = new MutableLiveData<>("");
    private final MutableLiveData<String> carNumber = new MutableLiveData<>("");
    private final MutableLiveData<String> manufacturer = new MutableLiveData<>("");
    private final MutableLiveData<String> model = new MutableLiveData<>("");
    private final MutableLiveData<String> year = new MutableLiveData<>("");
    private final MutableLiveData<String> color = new MutableLiveData<>("");
    private final MutableLiveData<String> nickname = new MutableLiveData<>("");

    private final MutableLiveData<String> insuranceCompanyName = new MutableLiveData<>("");
    private final MutableLiveData<String> insuranceCompanyId = new MutableLiveData<>("");

    // Car image (same priority logic as profile, but Activity renders it)
    private final MutableLiveData<String> carImageBase64 = new MutableLiveData<>("");
    private final MutableLiveData<String> carImageUri = new MutableLiveData<>("");

    // Manual flow (like DIY)
    private final MutableLiveData<Boolean> manualLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> manualPdfUrl = new MutableLiveData<>("");
    private final MutableLiveData<String> manualError = new MutableLiveData<>("");

    // Yad2 URL (empty => button disabled)
    private final MutableLiveData<String> yad2Url = new MutableLiveData<>("");

    // Data sources
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final ManualsRepository manualsRepo = new ManualsRepository();

    // -------------------- Getters --------------------

    public LiveData<String> getInfoText() { return infoText; }
    public LiveData<String> getScreenError() { return screenError; }

    public LiveData<String> getCarSubtitle() { return carSubtitle; }
    public LiveData<String> getCarNumber() { return carNumber; }
    public LiveData<String> getManufacturer() { return manufacturer; }
    public LiveData<String> getModel() { return model; }
    public LiveData<String> getYear() { return year; }
    public LiveData<String> getColor() { return color; }
    public LiveData<String> getNickname() { return nickname; }

    public LiveData<String> getInsuranceCompanyName() { return insuranceCompanyName; }
    public LiveData<String> getInsuranceCompanyId() { return insuranceCompanyId; }

    public LiveData<String> getCarImageBase64() { return carImageBase64; }
    public LiveData<String> getCarImageUri() { return carImageUri; }

    public LiveData<Boolean> getManualLoading() { return manualLoading; }
    public LiveData<String> getManualPdfUrl() { return manualPdfUrl; }
    public LiveData<String> getManualError() { return manualError; }

    public LiveData<String> getYad2Url() { return yad2Url; }

    // -------------------- Main load --------------------

    /**
     * Loads driver + car from Firestore.
     * Then:
     * 1) Fill UI fields (profile-like)
     * 2) Load manual PDF url
     * 3) Build Yad2 url
     */
    public void loadDriverAndManual(String uid) {
        if (isBlank(uid)) {
            screenError.setValue("לא מחובר/ת");
            return;
        }

        db.collection("drivers")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    Driver d = doc.toObject(Driver.class);

                    if (d == null || d.getCar() == null) {
                        clearAllUi();
                        infoText.setValue("לא נמצאו נתונים");
                        yad2Url.setValue("");
                        return;
                    }

                    Car car = d.getCar();

                    // Fill UI fields from Car
                    fillCarFields(car);

                    // Keep old multi-line text (backward compatibility)
                    String legacy =
                            "מספר רכב: " + safe(car.getCarNum()) + "\n" +
                                    "ביטוח: " + safe(d.getFormattedInsuranceDate()) + "\n" +
                                    "טסט: " + safe(d.getFormattedTestDate()) + "\n" +
                                    "טיפול 10K: " + safe(d.getFormattedTreatDate());
                    infoText.setValue(legacy);

                    // Manual + Yad2
                    loadManualForCar(car);
                    buildYad2UrlForCar(car);
                })
                .addOnFailureListener(e -> {
                    screenError.setValue("שגיאה בטעינת נתונים");
                    yad2Url.setValue("");
                });
    }

    // -------------------- UI fill --------------------

    /**
     * Fill profile-like fields from the Car object.
     * Only uses fields that exist in Car to avoid crashes.
     */
    private void fillCarFields(Car car) {
        if (car == null) {
            clearAllUi();
            return;
        }

        // Basic
        String num = safe(car.getCarNum());
        carNumber.setValue(num);

        CarModel manEnum = car.getCarModel();
        String manName = (manEnum == null) ? "" : safe(manEnum.name());
        manufacturer.setValue(manName);

        String specificModel = safe(car.getCarSpecificModel());
        model.setValue(specificModel);

        int y = car.getYear();
        year.setValue(y > 0 ? String.valueOf(y) : "");

        color.setValue(safe(car.getCarColor()));
        nickname.setValue(safe(car.getNickname()));

        insuranceCompanyName.setValue(safe(car.getInsuranceCompanyName()));
        insuranceCompanyId.setValue(safe(car.getInsuranceCompanyId()));

        // Image: base64 (new) + uri (old)
        carImageUri.setValue(safe(car.getCarImageUri()));
        String b64 = "";
        try {
            b64 = safe(car.getCarImageBase64()); // must exist (like profile)
        } catch (Exception ignored) {}
        carImageBase64.setValue(b64);

        // Subtitle: nickname if exists, else manufacturer | model | year
        String sub;
        if (!isBlank(car.getNickname())) {
            sub = safe(car.getNickname());
        } else {
            String yStr = (y > 0) ? String.valueOf(y) : "";
            sub = joinNotEmpty(manName, specificModel, yStr);
        }
        carSubtitle.setValue(sub);

        Log.d(TAG, "Car fields filled: num=" + num + ", man=" + manName + ", model=" + specificModel + ", year=" + y);
    }

    private void clearAllUi() {
        carSubtitle.setValue("");
        carNumber.setValue("");
        manufacturer.setValue("");
        model.setValue("");
        year.setValue("");
        color.setValue("");
        nickname.setValue("");
        insuranceCompanyName.setValue("");
        insuranceCompanyId.setValue("");
        carImageBase64.setValue("");
        carImageUri.setValue("");
        manualPdfUrl.setValue("");
        manualError.setValue("");
        manualLoading.setValue(false);
        yad2Url.setValue("");
    }

    // -------------------- Manual --------------------

    /**
     * Loads manual PDF URL using:
     * manufacturer enum name (doc id) + normalized model enum name + year range.
     */
    private void loadManualForCar(Car car) {
        manualPdfUrl.setValue("");
        manualError.setValue("");

        if (car == null) {
            manualError.setValue("חסר רכב");
            return;
        }

        CarModel manufacturerEnum = car.getCarModel();
        String manufacturerDocId = (manufacturerEnum == null) ? "" : manufacturerEnum.name().trim();

        // Model must match enum keys (I10/TUCSON/MAZDA3...)
        String modelRaw = safe(car.getCarSpecificModel());
        String modelEnumName = normalizeEnumKey(modelRaw);

        int yearInt = car.getYear();

        Log.d(TAG, "Manual filter: manufacturer=" + manufacturerDocId
                + ", modelRaw=" + modelRaw
                + ", modelEnumName=" + modelEnumName
                + ", year=" + yearInt);

        if (manufacturerEnum == null || isBlank(manufacturerDocId) || isBlank(modelEnumName) || yearInt <= 0) {
            manualError.setValue("חסרים נתונים לטעינת ספר רכב (יצרן/דגם/שנה)");
            return;
        }

        int[] range = CarModel.pickRangeForYear(manufacturerEnum, modelEnumName, yearInt);
        int fromYear = (range == null || range.length < 2) ? -1 : range[0];
        int toYear   = (range == null || range.length < 2) ? -1 : range[1];

        if (fromYear <= 0 || toYear <= 0) {
            manualError.setValue("לא נמצא טווח שנים לדגם הזה עבור השנה " + yearInt);
            return;
        }

        manualLoading.setValue(true);

        manualsRepo.getManualDownloadUrl(
                manufacturerDocId,
                modelEnumName,
                fromYear,
                toYear,
                new ManualsRepository.ResultCallback<String>() {
                    @Override
                    public void onSuccess(String url) {
                        manualLoading.postValue(false);

                        if (isBlank(url)) {
                            manualPdfUrl.postValue("");
                            manualError.postValue("לא נמצא ספר רכב");
                            return;
                        }

                        manualPdfUrl.postValue(url);
                    }

                    @Override
                    public void onError(Exception e) {
                        manualLoading.postValue(false);
                        manualPdfUrl.postValue("");
                        manualError.postValue(
                                (e != null && e.getMessage() != null) ? e.getMessage() : "שגיאה בטעינת ספר רכב"
                        );
                    }
                }
        );
    }

    // -------------------- Yad2 --------------------

    /**
     * Builds Yad2 URL. If mapping is missing -> url becomes empty and button stays disabled.
     */
    private void buildYad2UrlForCar(Car car) {
        if (car == null) {
            yad2Url.setValue("");
            return;
        }

        CarModel manufacturerEnum = car.getCarModel();
        String modelEnumName = normalizeEnumKey(safe(car.getCarSpecificModel()));
        int yearInt = car.getYear();

        String url = Yad2LinkBuilder.build(manufacturerEnum, modelEnumName, yearInt);
        yad2Url.setValue(url == null ? "" : url);

        Log.d(TAG, "Yad2: manufacturer=" + (manufacturerEnum == null ? "null" : manufacturerEnum.name())
                + ", modelEnumName=" + modelEnumName
                + ", year=" + yearInt
                + ", url=" + url);
    }

    // -------------------- Helpers --------------------

    private String normalizeEnumKey(String s) {
        if (s == null) return "";
        return s.trim()
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^0-9A-Zא-ת]+", "");
    }

    private String joinNotEmpty(String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p == null) continue;
            String t = p.trim();
            if (t.isEmpty()) continue;
            if (sb.length() > 0) sb.append("  |  ");
            sb.append(t);
        }
        return sb.toString();
    }

    private String safe(String s) {
        return (s == null) ? "" : s.trim();
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
