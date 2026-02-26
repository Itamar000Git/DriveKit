//
//package com.example.drive_kit.ViewModel;
//
//import androidx.lifecycle.LiveData;
//import androidx.lifecycle.MutableLiveData;
//import androidx.lifecycle.ViewModel;
//
//import com.example.drive_kit.Data.Repository.ProfileRepository;
//import com.example.drive_kit.Model.CarModel;
//import com.example.drive_kit.Model.Driver;
//
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.Locale;
//import java.util.TimeZone;
//
///**
// * EditProfileViewModel handles loading + saving the user's profile.
// *
// * IMPORTANT:
// * - UI stays in Activity
// * - This ViewModel validates input and delegates to ProfileRepository
// * - Image can be: null/"" (no change), http(s) url, or local uri (content/file) to upload
// */
//public class EditProfileViewModel extends ViewModel {
//
//    private final ProfileRepository repo = new ProfileRepository();
//
//    private final MutableLiveData<Driver> driver = new MutableLiveData<>();
//    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();
//    private final MutableLiveData<Boolean> finishScreen = new MutableLiveData<>(false);
//
//    private long selectedInsuranceDateMillis = -1;
//    private long selectedTestDateMillis = -1;
//    private long selectedTreatDateMillis = -1;
//
//    public LiveData<Driver> getDriver() { return driver; }
//    public LiveData<String> getToastMessage() { return toastMessage; }
//    public LiveData<Boolean> getFinishScreen() { return finishScreen; }
//
//    public void loadProfile(String uid) {
//        repo.getDriver(uid, new ProfileRepository.DriverCallback() {
//            @Override
//            public void onSuccess(Driver d) {
//                driver.postValue(d);
//
//                // keep millis locally for validation/save even if user didn't touch dates
//                if (d != null && d.getCar() != null) {
//                    selectedInsuranceDateMillis = d.getCar().getInsuranceDateMillis();
//                    selectedTestDateMillis = d.getCar().getTestDateMillis();
//                    selectedTreatDateMillis = d.getCar().getTreatmentDateMillis();
//                }
//            }
//
//            @Override
//            public void onError(Exception e) {
//                toastMessage.postValue("שגיאה בטעינת הנתונים");
//            }
//        });
//    }
//
//    /**
//     * Save with image support:
//     * - imageUriOrUrl can be:
//     *   null/"" -> do not change image
//     *   https://... -> store directly
//     *   content://... or file://... -> upload to Storage and store downloadUrl
//     */
//    public void saveProfile(
//            String uid,
//            String firstName,
//            String lastName,
//            String phone,
//            String carNumber,
//            CarModel manufacturer,
//            String carSpecificModel,
//            int year,
//            String carImageBase64OrNull // NEW
//    ) {
//        if (isBlank(firstName)  || isBlank(phone) || isBlank(carNumber)) {
//            toastMessage.setValue("נא למלא את כל השדות");
//            return;
//        }
//
//        if (selectedInsuranceDateMillis <= 0 || selectedTestDateMillis <= 0 || selectedTreatDateMillis <= 0) {
//            toastMessage.setValue("נא לבחור תאריכים");
//            return;
//        }
//
//        if (manufacturer == null || manufacturer == CarModel.UNKNOWN) {
//            toastMessage.setValue("נא לבחור יצרן");
//            return;
//        }
//        if (isBlank(carSpecificModel)) {
//            toastMessage.setValue("נא לבחור דגם");
//            return;
//        }
//        if (year <= 0) {
//            toastMessage.setValue("נא לבחור שנה");
//            return;
//        }
//
//        repo.updateProfileFieldsWithBase64(
//                uid,
//                firstName,
//                lastName,
//                phone,
//                carNumber,
//                selectedInsuranceDateMillis,
//                selectedTestDateMillis,
//                selectedTreatDateMillis,
//                manufacturer.name(),
//                carSpecificModel,
//                year,
//                carImageBase64OrNull,
//                new ProfileRepository.SimpleCallback() {
//                    @Override public void onSuccess() {
//                        toastMessage.postValue("עודכן בהצלחה");
//                        finishScreen.postValue(true);
//                    }
//                    @Override public void onError(Exception e) {
//                        toastMessage.postValue("שגיאה בעדכון הנתונים");
//                    }
//                }
//        );
//    }
//
//
//    public void setSelectedInsuranceDateMillis(long millis) { selectedInsuranceDateMillis = millis; }
//    public void setSelectedTestDateMillis(long millis) { selectedTestDateMillis = millis; }
//    public void setSelectedTreatDateMillis(long millis) { selectedTreatDateMillis = millis; }
//
//    public String formatDate(long millis) {
//        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
//        sdf.setTimeZone(TimeZone.getDefault());
//        return sdf.format(new Date(millis));
//    }
//
//    private boolean isBlank(String s) {
//        return s == null || s.trim().isEmpty();
//    }
//}

package com.example.drive_kit.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.drive_kit.Data.Repository.ProfileRepository;
import com.example.drive_kit.Model.CarModel;
import com.example.drive_kit.Model.Driver;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * EditProfileViewModel handles loading + saving the user's profile.
 *
 * IMPORTANT:
 * - UI stays in Activity
 * - This ViewModel validates input and delegates to ProfileRepository
 * - Image can be: null/"" (no change), http(s) url, or local uri (content/file) to upload
 */
public class EditProfileViewModel extends ViewModel {

    private final ProfileRepository repo = new ProfileRepository();

    private final MutableLiveData<Driver> driver = new MutableLiveData<>();
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> finishScreen = new MutableLiveData<>(false);

    private long selectedInsuranceDateMillis = -1;
    private long selectedTestDateMillis = -1;
    private long selectedTreatDateMillis = -1;

    public LiveData<Driver> getDriver() { return driver; }
    public LiveData<String> getToastMessage() { return toastMessage; }
    public LiveData<Boolean> getFinishScreen() { return finishScreen; }

    public void loadProfile(String uid) {
        repo.getDriver(uid, new ProfileRepository.DriverCallback() {
            @Override
            public void onSuccess(Driver d) {
                driver.postValue(d);

                // keep millis locally for validation/save even if user didn't touch dates
                if (d != null && d.getCar() != null) {
                    selectedInsuranceDateMillis = d.getCar().getInsuranceDateMillis();
                    selectedTestDateMillis = d.getCar().getTestDateMillis();
                    selectedTreatDateMillis = d.getCar().getTreatmentDateMillis();
                }
            }

            @Override
            public void onError(Exception e) {
                toastMessage.postValue("שגיאה בטעינת הנתונים");
            }
        });
    }

    /**
     * Save with image support:
     * - imageUriOrUrl can be:
     *   null/"" -> do not change image
     *   https://... -> store directly
     *   content://... or file://... -> upload to Storage and store downloadUrl
     */
    public void saveProfile(
            String uid,
            String firstName,
            String lastName,
            String phone,
            String carNumber,
            CarModel manufacturer,
            String carSpecificModel,
            int year,
            String carImageBase64OrNull // NEW
    ) {
        if (isBlank(firstName)  || isBlank(phone) || isBlank(carNumber)) {
            toastMessage.setValue("נא למלא את כל השדות");
            return;
        }

        if (selectedInsuranceDateMillis <= 0 || selectedTestDateMillis <= 0 || selectedTreatDateMillis <= 0) {
            toastMessage.setValue("נא לבחור תאריכים");
            return;
        }

        if (manufacturer == null || manufacturer == CarModel.UNKNOWN) {
            toastMessage.setValue("נא לבחור יצרן");
            return;
        }
        if (isBlank(carSpecificModel)) {
            toastMessage.setValue("נא לבחור דגם");
            return;
        }
        if (year <= 0) {
            toastMessage.setValue("נא לבחור שנה");
            return;
        }

        repo.updateProfileFieldsWithBase64(
                uid,
                firstName,
                lastName,
                phone,
                carNumber,
                selectedInsuranceDateMillis,
                selectedTestDateMillis,
                selectedTreatDateMillis,
                manufacturer.name(),
                carSpecificModel,
                year,
                carImageBase64OrNull,
                new ProfileRepository.SimpleCallback() {
                    @Override public void onSuccess() {
                        toastMessage.postValue("עודכן בהצלחה");
                        finishScreen.postValue(true);
                    }
                    @Override public void onError(Exception e) {
                        toastMessage.postValue("שגיאה בעדכון הנתונים");
                    }
                }
        );
    }

    public void setSelectedInsuranceDateMillis(long millis) { selectedInsuranceDateMillis = millis; }
    public void setSelectedTestDateMillis(long millis) { selectedTestDateMillis = millis; }
    public void setSelectedTreatDateMillis(long millis) { selectedTreatDateMillis = millis; }

    public String formatDate(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(new Date(millis));
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}