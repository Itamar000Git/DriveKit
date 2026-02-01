//package com.example.drive_kit.ViewModel;
//
//import androidx.lifecycle.LiveData;
//import androidx.lifecycle.MutableLiveData;
//import androidx.lifecycle.ViewModel;
//
//import com.example.drive_kit.Data.Repository.SetUsernamePasswordRepository;
//import com.example.drive_kit.Model.Driver;
//import android.util.Log;
//
///**
// * ViewModel for the SetUsernamePasswordActivity.
// * It uses the SetUsernamePasswordRepository to access the database.
// * It observes the LiveData in the ViewModel and updates the UI accordingly.
// * If the registration is successful, it starts the HomeActivity.
// */
//public class SetUsernamePasswordViewModel extends ViewModel {
//
//    private final SetUsernamePasswordRepository repo = new SetUsernamePasswordRepository(); //object for access to the database
//
//    private final MutableLiveData<Boolean> signUpSuccess = new MutableLiveData<>(); //for registration success
//    private final MutableLiveData<String> signUpError = new MutableLiveData<>(); //for registration error
//
//
//    public LiveData<Boolean> getSignUpSuccess() {
//        return signUpSuccess;
//    }
//
//    public LiveData<String> getSignUpError() {
//        return signUpError;
//    }
//
//    /**
//     * registers the user with the given email and password and driver object
//     * if the registration is successful, it starts the HomeActivity
//     * if the registration fails, it shows an error message
//     * @param email
//     * @param password
//     * @param confirmPassword
//     * @param driver
//     */
//    public void signUp(String email, String password, String confirmPassword, Driver driver){
//        //validate the input
//        if (password.isEmpty() || confirmPassword.isEmpty()) {
//            signUpError.postValue("נא למלא את כל השדות");
//            return;
//        }
//
//        if (!password.equals(confirmPassword)) {
//            signUpError.postValue("הסיסמאות אינן תואמות");
//            return;
//        }
//        System.out.println("drivercheck"+driver.toString());
//        // using the signUp method from the SetUsernamePasswordRepository to register the user
//        repo.register(email, password, driver, new SetUsernamePasswordRepository.SignUpCallback() {
//            @Override
//            public void onSuccess() {
//                signUpSuccess.postValue(true);
//            }
//
//            @Override
//            public void onError(Exception e) {
//                signUpError.postValue("שגיאה בהרשמה: " + e.getMessage());
//            }
//        });
//    }
//}


package com.example.drive_kit.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.drive_kit.Data.Repository.SetUsernamePasswordRepository;
import com.example.drive_kit.Model.Driver;

/**
 * SetUsernamePasswordViewModel is responsible for:
 * 1) Validating password inputs
 * 2) Triggering sign-up via repository
 * 3) Exposing results to Activity via LiveData
 *
 * IMPORTANT:
 * - This class does NOT know anything about Views, Buttons, or Activities directly.
 */
public class SetUsernamePasswordViewModel extends ViewModel {

    // Repository that performs Firebase Auth + Storage upload + Firestore save
    private final SetUsernamePasswordRepository repo = new SetUsernamePasswordRepository();

    // LiveData for signup success state
    private final MutableLiveData<Boolean> signUpSuccess = new MutableLiveData<>(false);

    // LiveData for error messages
    private final MutableLiveData<String> signUpError = new MutableLiveData<>(null);

    /**
     * Exposes signup success LiveData to the UI.
     *
     * @return LiveData<Boolean> true when signup completed successfully
     */
    public LiveData<Boolean> getSignUpSuccess() {
        return signUpSuccess;
    }

    /**
     * Exposes signup error LiveData to the UI.
     *
     * @return LiveData<String> error message or null
     */
    public LiveData<String> getSignUpError() {
        return signUpError;
    }

    /**
     * Starts the signup process:
     * - Validates password fields
     * - Calls repository to create Firebase Auth user
     * - Uploads car photo to Firebase Storage if provided
     * - Saves final Driver (with photo downloadUrl) in Firestore
     *
     * @param email user email
     * @param password chosen password
     * @param confirmPassword confirm password
     * @param driver full driver object collected from previous steps
     */
    public void signUp(String email, String password, String confirmPassword, Driver driver) {

        // Basic validations
        if (email == null || email.trim().isEmpty()) {
            signUpError.setValue("נא להזין אימייל");
            return;
        }

        if (password == null || password.trim().isEmpty()
                || confirmPassword == null || confirmPassword.trim().isEmpty()) {
            signUpError.setValue("נא להזין סיסמה ולאשר אותה");
            return;
        }

        // Firebase requires at least 6 chars for password
        if (password.trim().length() < 6) {
            signUpError.setValue("הסיסמה חייבת להכיל לפחות 6 תווים");
            return;
        }

        if (!password.trim().equals(confirmPassword.trim())) {
            signUpError.setValue("הסיסמאות אינן תואמות");
            return;
        }

        // Clear previous error
        signUpError.setValue(null);

        // Delegate real work to repository (async)
        repo.register(email.trim(), password.trim(), driver, new SetUsernamePasswordRepository.SignUpCallback() {
            @Override
            public void onSuccess() {
                signUpSuccess.postValue(true);
            }

            @Override
            public void onError(Exception e) {
                // Show friendly message (you can also log e.getMessage() if needed)
                signUpError.postValue("שגיאה בהרשמה. נסה שוב");
            }
        });
    }
}
