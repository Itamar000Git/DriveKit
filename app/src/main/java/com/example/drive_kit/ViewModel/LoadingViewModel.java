package com.example.drive_kit.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import android.content.Context;
import com.example.drive_kit.Data.Repository.NotificationSchedulerRepository;


import com.example.drive_kit.Data.Repository.LoadingRepository;

public class LoadingViewModel extends ViewModel {

    private final LoadingRepository repo = new LoadingRepository();

    private final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> loginError = new MutableLiveData<>();

    public LiveData<Boolean> getLoginSuccess() {
        return loginSuccess;
    }

    public LiveData<String> getLoginError() {
        return loginError;
    }

    public void login(String email, String password) {
        repo.signIn(email, password, new LoadingRepository.LoadingCallback() {
            @Override
            public void onSuccess() {
                loginSuccess.postValue(true);
            }

            @Override
            public void onError(Exception e) {
                loginError.postValue("פרטי ההתחברות שגויים");
            }
        });
    }
    private final NotificationSchedulerRepository schedulerRepo = new NotificationSchedulerRepository();

    public void startNotifications(Context appContext) {
        schedulerRepo.scheduleDaily(appContext);
    }

}
