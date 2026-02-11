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
//                        return;
//                    }
//
//                    Car car = d.getCar();
//
//                    // Show existing info (unchanged)
//                    String carNumber = safe(car.getCarNum());
//                    String text =
//                            "מספר רכב: " + carNumber + "\n" +
//                                    "ביטוח: " + safe(d.getFormattedInsuranceDate()) + "\n" +
//                                    "טסט: " + safe(d.getFormattedTestDate()) + "\n" +
//                                    "טיפול 10K: " + safe(d.getFormattedTreatDate());
//                    infoText.setValue(text);
//
//                    // Load manual
//                    loadManualForCar(car);
//                })
//                .addOnFailureListener(e -> screenError.setValue("שגיאה בטעינת נתונים"));
//    }
//
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
//        // model name should match the enum constant name in HyundaiModel/ToyotaModel etc.
//        String modelRaw = safe(car.getCarSpecificModel());
//        String modelEnumName = normalizeEnumKey(modelRaw); // "i10" -> "I10"
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

    // UI data
    private final MutableLiveData<String> infoText = new MutableLiveData<>("");
    private final MutableLiveData<String> screenError = new MutableLiveData<>("");

    // Manual flow (like DIY)
    private final MutableLiveData<Boolean> manualLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> manualPdfUrl = new MutableLiveData<>("");
    private final MutableLiveData<String> manualError = new MutableLiveData<>("");

    // Yad2 URL (empty => button disabled)
    private final MutableLiveData<String> yad2Url = new MutableLiveData<>("");

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final ManualsRepository manualsRepo = new ManualsRepository();

    public LiveData<String> getInfoText() { return infoText; }
    public LiveData<String> getScreenError() { return screenError; }

    public LiveData<Boolean> getManualLoading() { return manualLoading; }
    public LiveData<String> getManualPdfUrl() { return manualPdfUrl; }
    public LiveData<String> getManualError() { return manualError; }

    public LiveData<String> getYad2Url() { return yad2Url; }

    /**
     * Load driver -> show details -> load manual -> build Yad2 url.
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
                        infoText.setValue("לא נמצאו נתונים");
                        yad2Url.setValue(""); // keep disabled
                        return;
                    }

                    Car car = d.getCar();

                    // Show existing info (keep same behavior)
                    String carNumber = safe(car.getCarNum());
                    String text =
                            "מספר רכב: " + carNumber + "\n" +
                                    "ביטוח: " + safe(d.getFormattedInsuranceDate()) + "\n" +
                                    "טסט: " + safe(d.getFormattedTestDate()) + "\n" +
                                    "טיפול 10K: " + safe(d.getFormattedTreatDate());
                    infoText.setValue(text);

                    // Manual + Yad2 are based on manufacturer/model/year
                    loadManualForCar(car);
                    buildYad2UrlForCar(car);
                })
                .addOnFailureListener(e -> {
                    screenError.setValue("שגיאה בטעינת נתונים");
                    yad2Url.setValue("");
                });
    }

    /**
     * Load manual PDF URL from Firebase Storage.
     * Uses ManualsRepository and your existing year-range logic.
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

        // Model must match enum constant name (I10/TUCSON/MAZDA3...)
        String modelRaw = safe(car.getCarSpecificModel());
        String modelEnumName = normalizeEnumKey(modelRaw);

        int year = car.getYear();

        Log.d(TAG, "Manual filter: manufacturer=" + manufacturerDocId
                + ", modelRaw=" + modelRaw
                + ", modelEnumName=" + modelEnumName
                + ", year=" + year);

        if (manufacturerEnum == null || isBlank(manufacturerDocId) || isBlank(modelEnumName) || year <= 0) {
            manualError.setValue("חסרים נתונים לטעינת ספר רכב (יצרן/דגם/שנה)");
            return;
        }

        // Pick {from,to} without exposing YearRange
        int[] range = CarModel.pickRangeForYear(manufacturerEnum, modelEnumName, year);
        int fromYear = (range == null || range.length < 2) ? -1 : range[0];
        int toYear   = (range == null || range.length < 2) ? -1 : range[1];

        if (fromYear <= 0 || toYear <= 0) {
            manualError.setValue("לא נמצא טווח שנים לדגם הזה עבור השנה " + year);
            return;
        }

        manualLoading.setValue(true);

        manualsRepo.getManualDownloadUrl(
                manufacturerDocId,   // manuals_seed/{MANUFACTURER}
                modelEnumName,       // models[].model
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

    /**
     * Build the Yad2 URL for this car.
     * If we don't have a mapping -> we keep it empty and the button stays disabled.
     */
    private void buildYad2UrlForCar(Car car) {
        if (car == null) {
            yad2Url.setValue("");
            return;
        }

        CarModel manufacturerEnum = car.getCarModel();
        String modelRaw = safe(car.getCarSpecificModel());
        String modelEnumName = normalizeEnumKey(modelRaw);
        int year = car.getYear();

        String url = Yad2LinkBuilder.build(manufacturerEnum, modelEnumName, year);
        yad2Url.setValue(url == null ? "" : url);

        Log.d(TAG, "Yad2 filter: manufacturer=" + (manufacturerEnum == null ? "null" : manufacturerEnum.name())
                + ", modelEnumName=" + modelEnumName
                + ", year=" + year
                + ", url=" + url);
    }

    // Normalize to match enum keys (I10, MAZDA3, CX5...)
    private String normalizeEnumKey(String s) {
        if (s == null) return "";
        return s.trim()
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^0-9A-Zא-ת]+", "");
    }

    private String safe(String s) {
        return (s == null) ? "" : s.trim();
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
