package com.example.drive_kit.ViewModel;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.drive_kit.Data.Repository.NotificationSchedulerRepository;

/**
 * ViewModel for the MainActivity.
 * It observes the LiveData in the ViewModel and updates the UI accordingly.
 */
public class MainViewModel extends ViewModel {

    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);
    ///
    private final NotificationSchedulerRepository schedulerRepo =
            new NotificationSchedulerRepository();

    public void startNotifications(Context appContext) {
        schedulerRepo.scheduleDaily(appContext);
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public boolean validateLoginInputs(String email, String password) {
        if (email == null || email.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            errorMessage.setValue("נא להזין אימייל וסיסמה");
            return false;
        }

        errorMessage.setValue(null);
        return true;
    }
}
