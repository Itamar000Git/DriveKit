package com.example.drive_kit.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.drive_kit.Data.Repository.ProfileRepository;
import com.example.drive_kit.Model.Driver;
public class ProfileViewModel extends ViewModel {

    private final ProfileRepository repo = new ProfileRepository();

    private final MutableLiveData<Driver> driver = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LiveData<Driver> getDriver() { return driver; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    /**
     * loads the profile for the given user and updates the UI accordingly
     * if the user is not logged in, it shows an error message
     * @param uid
     */
    public void loadProfile(String uid) {
        repo.getDriver(uid, new ProfileRepository.DriverCallback() {
            @Override
            public void onSuccess(Driver d) {
                driver.postValue(d);
            }

            @Override
            public void onError(Exception e) {
                errorMessage.postValue("שגיאה בטעינת הנתונים");
            }
        });
    }
}
