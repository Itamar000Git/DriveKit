//package com.example.drive_kit.ViewModel;
//
//import androidx.lifecycle.LiveData;
//import androidx.lifecycle.MutableLiveData;
//import androidx.lifecycle.ViewModel;
//
//import com.example.drive_kit.Data.Repository.HomeRepository;
//import com.example.drive_kit.Model.Driver;
//
///**
// * ViewModel for the HomeActivity.
// * It uses the HomeRepository to access the database.
// * Loading welcome text for the current user.
// */
//public class HomeViewModel extends ViewModel {
//
//    private final HomeRepository repo = new HomeRepository(); // object for access to the database
//
//    private final MutableLiveData<String> welcomeText = new MutableLiveData<>("שלום!"); // default welcome text
//
//    // NEW: keep current driver data so other screens/fragments (e.g. bottom sheet) can use it
//    private final MutableLiveData<Driver> driverLiveData = new MutableLiveData<>(null);
//
//    public LiveData<String> getWelcomeText() {
//        return welcomeText;
//    }
//
//    // NEW: expose current driver
//    public LiveData<Driver> getDriver() {
//        return driverLiveData;
//    }
//
//    /**
//     * Loading welcome text for the current user.
//     * @param uid user id
//     */
//    public void loadWelcomeText(String uid) {
//        if (uid == null || uid.isEmpty()) {
//            welcomeText.postValue("שלום אורח");
//            driverLiveData.postValue(null); // NEW
//            return;
//        }
//
//        // Use the DriverCallback interface to handle the result of the database query
//        repo.getDriver(uid, new HomeRepository.DriverCallback() {
//            // if the user successfully logged in, it loads the welcome text
//            @Override
//            public void onSuccess(Driver driver) {
//                // NEW: publish full driver object
//                driverLiveData.postValue(driver);
//
//                if (driver != null && driver.getFirstName() != null && !driver.getFirstName().trim().isEmpty()) {
//                    welcomeText.postValue("שלום, " + driver.getFirstName() + "!");
//                } else {
//                    welcomeText.postValue("שלום!");
//                }
//            }
//
//            // if the user failed to logged in, it shows an error message
//            @Override
//            public void onError(Exception e) {
//                welcomeText.postValue("שגיאה בטעינת הנתונים");
//                driverLiveData.postValue(null); // NEW
//            }
//        });
//    }
//}


package com.example.drive_kit.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.drive_kit.Data.Repository.HomeRepository;
import com.example.drive_kit.Model.Driver;

/**
 * ViewModel for the HomeActivity.
 * It uses the HomeRepository to access the database.
 * Loading welcome text for the current user.
 */
public class HomeViewModel extends ViewModel {

    private final HomeRepository repo = new HomeRepository();

    private final MutableLiveData<String> welcomeText = new MutableLiveData<>("שלום!");
    private final MutableLiveData<Driver> driverLiveData = new MutableLiveData<>(null);

    public LiveData<String> getWelcomeText() {
        return welcomeText;
    }

    public LiveData<Driver> getDriver() {
        return driverLiveData;
    }

    public void loadWelcomeText(String uid) {
        if (uid == null || uid.isEmpty()) {
            welcomeText.postValue("שלום אורח");
            driverLiveData.postValue(null);
            return;
        }

        repo.getDriver(uid, new HomeRepository.DriverCallback() {
            @Override
            public void onSuccess(Driver driver) {
                driverLiveData.postValue(driver);

                if (driver != null && driver.getFirstName() != null && !driver.getFirstName().trim().isEmpty()) {
                    welcomeText.postValue("שלום, " + driver.getFirstName() + "!");
                } else {
                    welcomeText.postValue("שלום!");
                }
            }

            @Override
            public void onError(Exception e) {
                welcomeText.postValue("שגיאה בטעינת הנתונים");
                driverLiveData.postValue(null);
            }
        });
    }
}