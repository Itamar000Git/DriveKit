package com.example.drive_kit.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.drive_kit.Data.Repository.SetUsernamePasswordRepository;
import com.example.drive_kit.Model.Driver;

/**
 * ViewModel for the SetUsernamePasswordActivity.
 * It uses the SetUsernamePasswordRepository to access the database.
 * It observes the LiveData in the ViewModel and updates the UI accordingly.
 * If the registration is successful, it starts the HomeActivity.
 */
public class SetUsernamePasswordViewModel extends ViewModel {

    private final SetUsernamePasswordRepository repo = new SetUsernamePasswordRepository(); //object for access to the database

    private final MutableLiveData<Boolean> signUpSuccess = new MutableLiveData<>(); //for registration success
    private final MutableLiveData<String> signUpError = new MutableLiveData<>(); //for registration error


    public LiveData<Boolean> getSignUpSuccess() {
        return signUpSuccess;
    }

    public LiveData<String> getSignUpError() {
        return signUpError;
    }

    /**
     * registers the user with the given email and password and driver object
     * if the registration is successful, it starts the HomeActivity
     * if the registration fails, it shows an error message
     * @param email
     * @param password
     * @param confirmPassword
     * @param driver
     */
    public void signUp(String email, String password, String confirmPassword, Driver driver){
        //validate the input
        if (password.isEmpty() || confirmPassword.isEmpty()) {
            signUpError.postValue("נא למלא את כל השדות");
            return;
        }

        if (!password.equals(confirmPassword)) {
            signUpError.postValue("הסיסמאות אינן תואמות");
            return;
        }



        // using the signUp method from the SetUsernamePasswordRepository to register the user
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
