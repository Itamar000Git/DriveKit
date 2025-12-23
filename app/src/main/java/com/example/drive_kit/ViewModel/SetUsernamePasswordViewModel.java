package com.example.drive_kit.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.drive_kit.Data.Repository.SetUsernamePasswordRepository;
import com.example.drive_kit.Model.Driver;

public class SetUsernamePasswordViewModel extends ViewModel {

    private final SetUsernamePasswordRepository repo = new SetUsernamePasswordRepository();

    private final MutableLiveData<Boolean> signUpSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> signUpError = new MutableLiveData<>();

    public LiveData<Boolean> getSignUpSuccess() {
        return signUpSuccess;
    }

    public LiveData<String> getSignUpError() {
        return signUpError;
    }

    public void signUp(
            String email,
            String password,
            String confirmPassword,
            Driver driver
    ) {
        if (password.isEmpty() || confirmPassword.isEmpty()) {
            signUpError.postValue("נא למלא את כל השדות");
            return;
        }

        if (!password.equals(confirmPassword)) {
            signUpError.postValue("הסיסמאות אינן תואמות");
            return;
        }

        repo.register(email, password, driver, new SetUsernamePasswordRepository.SignUpCallback() {
            @Override
            public void onSuccess() {
                signUpSuccess.postValue(true);
            }

            @Override
            public void onError(Exception e) {
                signUpError.postValue("שגיאה בהרשמה: " + e.getMessage());
            }
        });
    }
}
