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

    private final HomeRepository repo = new HomeRepository(); //object for access to the database

    private final MutableLiveData<String> welcomeText = new MutableLiveData<>("שלום!"); //default welcome text

    public LiveData<String> getWelcomeText() {
        return welcomeText;
    }

    /**
     * Loading welcome text for the current user.
     * @param uid
     */
    public void loadWelcomeText(String uid) {
        if (uid == null || uid.isEmpty()) {
            welcomeText.postValue("שלום אורח");
            return;
        }

        //Use the DriverCallback interface to handle the result of the database query
        repo.getDriver(uid, new HomeRepository.DriverCallback(){
            // if the user successfully logged in, it loads the welcome text
            @Override
            public void onSuccess(Driver driver) {
                if (driver != null && driver.getFirstName() != null && !driver.getFirstName().trim().isEmpty()) {
                    welcomeText.postValue("שלום, " + driver.getFirstName() + "!");
                } else {
                    welcomeText.postValue("שלום!");
                }
            }

            // if the user failed to logged in, it shows an error message
            @Override
            public void onError(Exception e) {
                welcomeText.postValue("שגיאה בטעינת הנתונים");
            }
        });
    }
}
