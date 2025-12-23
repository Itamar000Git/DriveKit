package com.example.drive_kit.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.drive_kit.Data.Repository.HomeRepository;
import com.example.drive_kit.Model.Driver;

public class HomeViewModel extends ViewModel {

    private final HomeRepository repo = new HomeRepository();

    private final MutableLiveData<String> welcomeText = new MutableLiveData<>("שלום!");

    public LiveData<String> getWelcomeText() {
        return welcomeText;
    }

    public void loadWelcomeText(String uid) {
        if (uid == null || uid.isEmpty()) {
            welcomeText.postValue("שלום אורח");
            return;
        }

        repo.getDriver(uid, new HomeRepository.DriverCallback(){
            @Override
            public void onSuccess(Driver driver) {
                if (driver != null && driver.getFirstName() != null && !driver.getFirstName().trim().isEmpty()) {
                    welcomeText.postValue("שלום, " + driver.getFirstName() + "!");
                } else {
                    welcomeText.postValue("שלום!");
                }
            }

            @Override
            public void onError(Exception e) {
                welcomeText.postValue("שגיאה בטעינת הנתונים");
            }
        });
    }
}
