package com.example.drive_kit.ViewModel;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.drive_kit.Data.Repository.NotificationSchedulerRepository;

/**
 * ViewModel for the MainActivity.
 * This class holds UI-related logic and data.
 * It does NOT know about Views, Buttons, or Activities directly.
 */
public class MainViewModel extends ViewModel {
    // LiveData that holds an error message for the UI.
    // The Activity observes this value and shows a Toast when it changes.
    // Initial value is null, which means "no error".
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);

    // Repository responsible for scheduling notification background work.
    // This ViewModel delegates notification scheduling to this repository.
    /////////////////////
    private final NotificationSchedulerRepository schedulerRepo =
            new NotificationSchedulerRepository();
    /**
     * Starts daily background notifications.
     *
     * This method is usually called after a successful login.
     * It uses the application context (not Activity context!)
     * to register a WorkManager task that runs once per day.
     *
     * @param appContext Application context used by WorkManager
     */
    public void startNotifications(Context appContext) {
        schedulerRepo.scheduleDaily(appContext);
    }
    /**
     * Exposes the error message LiveData to the UI.
     *
     * The Activity observes this LiveData.
     * Whenever the value changes, the observer in the Activity is triggered.
     *
     * @return LiveData containing the current error message (or null if no error)
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    /**
     * Validates the login input fields.
     *
     * This method checks that both email and password:
     * - Are not null
     * - Are not empty after trimming spaces
     *
     * If validation fails:
     * - An error message is published via LiveData
     * - The method returns false
     *
     * If validation succeeds:
     * - The error message is cleared (set to null)
     * - The method returns true
     *
     * @param email    User input email
     * @param password User input password
     * @return true if inputs are valid, false otherwise
     */
    public boolean validateLoginInputs(String email, String password) {
        if (email == null || email.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            errorMessage.setValue("נא להזין אימייל וסיסמה");
            return false;
        }

        errorMessage.setValue(null);
        return true;
    }

    public void postError(String msg) {
        errorMessage.setValue(msg);
    }

    public void clearError() {
        errorMessage.setValue(null);
    }
    /** Called when user cancels Google sign-in */
    public void onGoogleSignInCanceled() {
        errorMessage.setValue("התחברות עם Google בוטלה");
    }

    /** Called when Google sign-in fails */
    public void onGoogleSignInFailed(String msg) {
        if (msg == null || msg.trim().isEmpty()) {
            errorMessage.setValue("שגיאה בהתחברות עם Google");
        } else {
            errorMessage.setValue(msg);
        }
    }

}
