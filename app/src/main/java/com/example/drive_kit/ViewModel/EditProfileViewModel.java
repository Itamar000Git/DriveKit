//package com.example.drive_kit.ViewModel;
//
//import android.content.Context;
//
//import androidx.lifecycle.LiveData;
//import androidx.lifecycle.MutableLiveData;
//import androidx.lifecycle.ViewModel;
//
//import com.example.drive_kit.Data.Repository.ProfileRepository;
//import com.example.drive_kit.Model.Driver;
//
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.Locale;
//import java.util.TimeZone;
//
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
//            }
//
//            @Override
//            public void onError(Exception e) {
//                toastMessage.postValue("שגיאה בטעינת הנתונים");
//            }
//        });
//    }
//
//    public void saveProfile(
//            String uid,
//            String firstName,
//            String lastName,
//            String phone,
//            String carNumber
//    ) {
//        // basic validation
//        if (firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty() || carNumber.isEmpty()) {
//            toastMessage.setValue("נא למלא את כל השדות");
//            return;
//        }
//        if (selectedInsuranceDateMillis <= 0 || selectedTestDateMillis <= 0 || selectedTreatDateMillis <= 0) {
//            toastMessage.setValue("נא לבחור תאריכים");
//            return;
//        }
//
//        repo.updateProfileFields(
//                uid,
//                firstName,
//                lastName,
//                phone,
//                carNumber,
//                selectedInsuranceDateMillis,
//                selectedTestDateMillis,
//                selectedTreatDateMillis,
//                new ProfileRepository.SimpleCallback() {
//                    @Override
//                    public void onSuccess() {
//                        toastMessage.postValue("עודכן בהצלחה");
//                        finishScreen.postValue(true);
//                    }
//
//                    @Override
//                    public void onError(Exception e) {
//                        toastMessage.postValue("שגיאה בעדכון הנתונים");
//                    }
//                }
//        );
//    }
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
//}


package com.example.drive_kit.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.drive_kit.Data.Repository.ProfileRepository;
import com.example.drive_kit.Model.CarModel;          // NEW
import com.example.drive_kit.Model.Driver;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class EditProfileViewModel extends ViewModel {

    private final ProfileRepository repo = new ProfileRepository();

    private final MutableLiveData<Driver> driver = new MutableLiveData<>();
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> finishScreen = new MutableLiveData<>(false);

    private long selectedInsuranceDateMillis = -1;
    private long selectedTestDateMillis = -1;
    private long selectedTreatDateMillis = -1;

    // NEW: car extra fields
    private CarModel selectedManufacturer = CarModel.UNKNOWN; // NEW
    private String selectedCarSpecificModel = null;           // NEW
    private int selectedYear = 0;                             // NEW

    public LiveData<Driver> getDriver() { return driver; }
    public LiveData<String> getToastMessage() { return toastMessage; }
    public LiveData<Boolean> getFinishScreen() { return finishScreen; }

    public void loadProfile(String uid) {
        repo.getDriver(uid, new ProfileRepository.DriverCallback() {
            @Override
            public void onSuccess(Driver d) {
                driver.postValue(d);

                // NEW: keep current car fields in VM too (so save works even if user doesn't touch dropdowns)
                if (d != null && d.getCar() != null) {
                    selectedManufacturer = d.getCar().getCarModel() == null ? CarModel.UNKNOWN : d.getCar().getCarModel();
                    selectedCarSpecificModel = d.getCar().getCarSpecificModel();
                    selectedYear = d.getCar().getYear();
                }
            }

            @Override
            public void onError(Exception e) {
                toastMessage.postValue("שגיאה בטעינת הנתונים");
            }
        });
    }

    // CHANGED: added manufacturer/model/year
    public void saveProfile(
            String uid,
            String firstName,
            String lastName,
            String phone,
            String carNumber,
            CarModel manufacturer,          // NEW
            String carSpecificModel,        // NEW
            int year                        // NEW
    ) {
        // basic validation
        if (isBlank(firstName) || isBlank(lastName) || isBlank(phone) || isBlank(carNumber)) {
            toastMessage.setValue("נא למלא את כל השדות");
            return;
        }

        if (selectedInsuranceDateMillis <= 0 || selectedTestDateMillis <= 0 || selectedTreatDateMillis <= 0) {
            toastMessage.setValue("נא לבחור תאריכים");
            return;
        }

        // NEW: validate car fields
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

        // NEW: store into VM fields (optional, but keeps state consistent)
        selectedManufacturer = manufacturer;
        selectedCarSpecificModel = carSpecificModel;
        selectedYear = year;

        // CHANGED: update repository call to include the new fields
        // NOTE: assume ProfileRepository has a matching function.
        repo.updateProfileFields(
                uid,
                firstName,
                lastName,
                phone,
                carNumber,
                selectedInsuranceDateMillis,
                selectedTestDateMillis,
                selectedTreatDateMillis,
                selectedManufacturer.name(),     // NEW (store as string)
                selectedCarSpecificModel,        // NEW
                selectedYear,                    // NEW
                new ProfileRepository.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        toastMessage.postValue("עודכן בהצלחה");
                        finishScreen.postValue(true);
                    }

                    @Override
                    public void onError(Exception e) {
                        toastMessage.postValue("שגיאה בעדכון הנתונים");
                    }
                }
        );
    }

    public void setSelectedInsuranceDateMillis(long millis) { selectedInsuranceDateMillis = millis; }
    public void setSelectedTestDateMillis(long millis) { selectedTestDateMillis = millis; }
    public void setSelectedTreatDateMillis(long millis) { selectedTreatDateMillis = millis; }

    // NEW: optional setters if you want Activity to set them as user picks
    public void setSelectedManufacturer(CarModel m) { selectedManufacturer = (m == null ? CarModel.UNKNOWN : m); } // NEW
    public void setSelectedCarSpecificModel(String s) { selectedCarSpecificModel = s; } // NEW
    public void setSelectedYear(int y) { selectedYear = y; } // NEW

    public String formatDate(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(new Date(millis));
    }

    private boolean isBlank(String s) { // NEW
        return s == null || s.trim().isEmpty();
    }
}
