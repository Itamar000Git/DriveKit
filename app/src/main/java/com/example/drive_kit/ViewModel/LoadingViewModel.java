//package com.example.drive_kit.ViewModel;
//
//import androidx.lifecycle.LiveData;
//import androidx.lifecycle.MutableLiveData;
//import androidx.lifecycle.ViewModel;
//
//import android.content.Context;
//
//import com.example.drive_kit.Data.Repository.NotificationSchedulerRepository;
//import com.example.drive_kit.Data.Repository.LoadingRepository;
//
///**
// * LoadingViewModel belongs to LoadingActivity (the "loading" screen during login).
// *
// * This ViewModel is responsible for:
// * 1) Running the login process using LoadingRepository (Firebase / DB logic).
// * 2) Exposing the login result to the UI using LiveData:
// *    - loginSuccess (true/false)
// *    - loginError (error message)
// * 3) Starting the daily background notification scheduler after a successful login.
// *
// * IMPORTANT:
// * This ViewModel does NOT navigate between screens.
// * It only publishes results, and the Activity decides what to do with them.
// *
// * NEW (Google):
// * - Added loginWithGoogle(idToken) which logs in using FirebaseAuth Google credential.
// */
//public class LoadingViewModel extends ViewModel {
//
//    // Repository that performs the actual sign-in request (for example via FirebaseAuth).
//    // The ViewModel delegates database/auth work to this repository.
//    private final LoadingRepository repo = new LoadingRepository();
//
//    // LiveData that tells the UI if login succeeded.
//    // The Activity observes this to move to HomeActivity when it becomes true.
//    private final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();
//
//    // LiveData that holds an error message when login fails.
//    // The Activity observes this to show a Toast and return to the login screen.
//    private final MutableLiveData<String> loginError = new MutableLiveData<>(); //for login error
//
//    /**
//     * Exposes login success LiveData to the Activity.
//     * @return LiveData<Boolean> that becomes true when login succeeds
//     */
//    public LiveData<Boolean> getLoginSuccess() {
//        return loginSuccess;
//    }
//
//    /**
//     * Exposes login error LiveData to the Activity.
//     * @return LiveData<String> with a user-friendly error message (or null if no error)
//     */
//    public LiveData<String> getLoginError() {
//        return loginError;
//    }
//
//    /**
//     * Starts the login process with the given email and password.
//     *
//     * Flow:
//     * 1) Call repo.signIn(...) which performs the real authentication.
//     * 2) repo returns the result through a callback:
//     *    - onSuccess() -> publish loginSuccess = true
//     *    - onError(e)  -> publish loginError message
//     *
//     * IMPORTANT:
//     * This method does NOT start Activities.
//     * It only updates LiveData so the Activity can react.
//     *
//     * @param email user email input
//     * @param password user password input
//     */
//    public void login(String email, String password) {
//
//        // Basic defensive checks (optional but safer)
//        if (email == null || password == null) {
//            loginError.postValue("חסרים פרטי התחברות");
//            return;
//        }
//
//        // Ask the repository to sign in (asynchronous call).
//        // When it finishes, one of the callback methods will be triggered.
//        repo.signIn(email, password, new LoadingRepository.LoadingCallback() {
//
//            // Publish "true" to observers (LoadingActivity will navigate to HomeActivity).
//            // postValue is used because callbacks may run on a background thread.
//            @Override
//            public void onSuccess() {
//                loginSuccess.postValue(true);
//            }
//
//            // Publish an error message to observers (LoadingActivity will show a Toast).
//            // We ignore the specific exception and show a simple message to the user.
//            @Override
//            public void onError(Exception e) {
//                loginError.postValue("פרטי ההתחברות שגויים");
//            }
//        });
//    }
//
//    /**
//     * NEW: Starts the login process with Google idToken.
//     *
//     * Flow:
//     * 1) Call repo.signInWithGoogle(idToken, ...)
//     * 2) onSuccess -> publish loginSuccess = true
//     * 3) onError   -> publish loginError
//     *
//     * IMPORTANT:
//     * This method does NOT start Activities.
//     * It only updates LiveData so the Activity can react.
//     *
//     * @param idToken Google idToken from GoogleSignInAccount
//     */
//    public void loginWithGoogle(String idToken) {
//
//        // Basic validation
//        if (idToken == null || idToken.trim().isEmpty()) {
//            loginError.postValue("חסר idToken (בדוק default_web_client_id)");
//            return;
//        }
//
//        repo.signInWithGoogle(idToken, new LoadingRepository.LoadingCallback() {
//            @Override
//            public void onSuccess() {
//                loginSuccess.postValue(true);
//            }
//
//            @Override
//            public void onError(Exception e) {
//                loginError.postValue("שגיאה בהתחברות עם Google");
//            }
//        });
//    }
//
//    ///////////////
//    // Repository responsible for scheduling notifications via WorkManager.
//    // This is separated from login logic and used after login success.
//    private final NotificationSchedulerRepository schedulerRepo = new NotificationSchedulerRepository();
//
//    /**
//     * Starts daily notifications scheduling.
//     *
//     * This should be called after a successful login.
//     * We use Application Context because WorkManager should not rely on Activity context.
//     *
//     * @param appContext application context
//     */
//    public void startNotifications(Context appContext) {
//        schedulerRepo.scheduleDaily(appContext);
//    }
//}


package com.example.drive_kit.ViewModel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.drive_kit.Data.Repository.LoadingRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * LoadingViewModel
 *
 * Responsibilities:
 * - Perform login (email/pass or Google) via LoadingRepository.
 * - After successful login: determine route (insurance partner / driver has profile / driver no profile).
 *
 * Activity does NOT query Firestore.
 * Activity only navigates according to routeLiveData.
 */
public class LoadingViewModel extends ViewModel {

    public static class RouteDecision {
        public enum Target {
            INSURANCE_HOME,
            HOME,
            SIGN_UP,
            BACK_TO_LOGIN
        }

        public final Target target;
        public final String insuranceCompanyId; // docId for insurance home
        public final String prefillEmail;        // for sign up

        private RouteDecision(Target target, String insuranceCompanyId, String prefillEmail) {
            this.target = target;
            this.insuranceCompanyId = insuranceCompanyId;
            this.prefillEmail = prefillEmail;
        }

        public static RouteDecision insurance(@NonNull String companyDocId) {
            return new RouteDecision(Target.INSURANCE_HOME, companyDocId, null);
        }

        public static RouteDecision home() {
            return new RouteDecision(Target.HOME, null, null);
        }

        public static RouteDecision signup(String prefillEmail) {
            return new RouteDecision(Target.SIGN_UP, null, prefillEmail);
        }

        public static RouteDecision backToLogin() {
            return new RouteDecision(Target.BACK_TO_LOGIN, null, null);
        }
    }

    private final LoadingRepository repo = new LoadingRepository();

    private final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> loginError = new MutableLiveData<>();

    // ✅ NEW: route decision after login
    private final MutableLiveData<RouteDecision> routeDecision = new MutableLiveData<>(null);

    public LiveData<Boolean> getLoginSuccess() { return loginSuccess; }
    public LiveData<String> getLoginError() { return loginError; }
    public LiveData<RouteDecision> getRouteDecision() { return routeDecision; }

    public void login(String email, String password) {
        if (email == null || password == null) {
            loginError.postValue("חסרים פרטי התחברות");
            return;
        }

        repo.signIn(email, password, new LoadingRepository.LoadingCallback() {
            @Override public void onSuccess() {
                loginSuccess.postValue(true);
                computeRouteAfterLogin();
            }
            @Override public void onError(Exception e) {
                loginError.postValue("פרטי ההתחברות שגויים");
            }
        });
    }

    public void loginWithGoogle(String idToken) {
        if (idToken == null || idToken.trim().isEmpty()) {
            loginError.postValue("חסר idToken (בדוק default_web_client_id)");
            return;
        }

        repo.signInWithGoogle(idToken, new LoadingRepository.LoadingCallback() {
            @Override public void onSuccess() {
                loginSuccess.postValue(true);
                computeRouteAfterLogin();
            }
            @Override public void onError(Exception e) {
                loginError.postValue("שגיאה בהתחברות עם Google");
            }
        });
    }

    /**
     * ✅ NEW: decide where to route after successful login.
     * Same logic as your Activity had, just moved into VM/Repo.
     */
    private void computeRouteAfterLogin() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            routeDecision.postValue(RouteDecision.backToLogin());
            return;
        }

        String uid = user.getUid();

        // 1) Insurance partner?
        repo.checkInsurancePartner(uid, new LoadingRepository.InsurancePartnerCallback() {
            @Override
            public void onResult(boolean isPartner, String companyDocIdOrNull) {
                if (isPartner && companyDocIdOrNull != null && !companyDocIdOrNull.trim().isEmpty()) {
                    routeDecision.postValue(RouteDecision.insurance(companyDocIdOrNull.trim()));
                    return;
                }

                // 2) Not insurance -> check drivers/{uid}
                repo.checkDriverDoc(uid, new LoadingRepository.DriverDocCallback() {
                    @Override
                    public void onResult(boolean exists) {
                        if (exists) {
                            routeDecision.postValue(RouteDecision.home());
                        } else {
                            routeDecision.postValue(RouteDecision.signup(user.getEmail()));
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        // fallback safe: back to login
                        routeDecision.postValue(RouteDecision.backToLogin());
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                // אם נכשלת בדיקת insurance, עדיין מנסים מסלול driver
                repo.checkDriverDoc(uid, new LoadingRepository.DriverDocCallback() {
                    @Override
                    public void onResult(boolean exists) {
                        if (exists) routeDecision.postValue(RouteDecision.home());
                        else routeDecision.postValue(RouteDecision.signup(user.getEmail()));
                    }

                    @Override
                    public void onError(Exception ex) {
                        routeDecision.postValue(RouteDecision.backToLogin());
                    }
                });
            }
        });
    }
}