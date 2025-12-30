package com.example.drive_kit.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import android.content.Context;
import com.example.drive_kit.Data.Repository.NotificationSchedulerRepository;


import com.example.drive_kit.Data.Repository.LoadingRepository;

/**
 * LoadingViewModel belongs to LoadingActivity (the "loading" screen during login).
 *
 * This ViewModel is responsible for:
 * 1) Running the login process using LoadingRepository (Firebase / DB logic).
 * 2) Exposing the login result to the UI using LiveData:
 *    - loginSuccess (true/false)
 *    - loginError (error message)
 * 3) Starting the daily background notification scheduler after a successful login.
 *
 * IMPORTANT:
 * This ViewModel does NOT navigate between screens.
 * It only publishes results, and the Activity decides what to do with them.
 */
public class LoadingViewModel extends ViewModel {

    // Repository that performs the actual sign-in request (for example via FirebaseAuth).
    // The ViewModel delegates database/auth work to this repository.
    private final LoadingRepository repo = new LoadingRepository();

    // LiveData that tells the UI if login succeeded.
    // The Activity observes this to move to HomeActivity when it becomes true.
    private final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();

    // LiveData that holds an error message when login fails.
    // The Activity observes this to show a Toast and return to the login screen.
    private final MutableLiveData<String> loginError = new MutableLiveData<>(); //for login error

    /**
     * Exposes login success LiveData to the Activity.
     * @return LiveData<Boolean> that becomes true when login succeeds
     */
    public LiveData<Boolean> getLoginSuccess() {
        return loginSuccess;
    }

    /**
     * Exposes login error LiveData to the Activity.
     * @return LiveData<String> with a user-friendly error message (or null if no error)
     */
    public LiveData<String> getLoginError() {
        return loginError;
    }

    /**
     * Starts the login process with the given email and password.
     *
     * Flow:
     * 1) Call repo.signIn(...) which performs the real authentication.
     * 2) repo returns the result through a callback:
     *    - onSuccess() -> publish loginSuccess = true
     *    - onError(e)  -> publish loginError message
     *
     * IMPORTANT:
     * This method does NOT start Activities.
     * It only updates LiveData so the Activity can react.
     *
     * @param email user email input
     * @param password user password input
     */
    public void login(String email, String password) {
        // Ask the repository to sign in (asynchronous call).
        // When it finishes, one of the callback methods will be triggered.
        repo.signIn(email, password, new LoadingRepository.LoadingCallback() {

            // Publish "true" to observers (LoadingActivity will navigate to HomeActivity).
            // postValue is used because callbacks may run on a background thread.
            @Override
            public void onSuccess() {
                loginSuccess.postValue(true);
            }
            // Publish an error message to observers (LoadingActivity will show a Toast).
            // We ignore the specific exception and show a simple message to the user.
            @Override
            public void onError(Exception e) {
                loginError.postValue("פרטי ההתחברות שגויים");
            }
        });
    }
    ///////////////
    // Repository responsible for scheduling notifications via WorkManager.
    // This is separated from login logic and used after login success.
    private final NotificationSchedulerRepository schedulerRepo = new NotificationSchedulerRepository();
    /**
     * Starts daily notifications scheduling.
     *
     * This should be called after a successful login.
     * We use Application Context because WorkManager should not rely on Activity context.
     *
     * @param appContext application context
     */
    public void startNotifications(Context appContext) {
        schedulerRepo.scheduleDaily(appContext);
    }

}
