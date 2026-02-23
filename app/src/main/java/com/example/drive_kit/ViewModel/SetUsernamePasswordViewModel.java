package com.example.drive_kit.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.drive_kit.Data.Repository.SetUsernamePasswordRepository;
import com.example.drive_kit.Model.Driver;

/**
 * SetUsernamePasswordViewModel
 *
 * Responsibilities:
 * 1) Validate inputs before repository calls.
 * 2) Trigger registration / profile completion flows via repository.
 * 3) Expose state to UI through LiveData:
 *    - signUpSuccess: true when operation completed successfully.
 *    - signUpError: user-facing error message when operation failed.
 *
 * Supported flows:
 * A) New user signup (creates FirebaseAuth user + Firestore data)
 *    - signUp(...)
 *    - signUpInsurance(...)
 *
 * B) Existing FirebaseAuth user missing profile doc (NO new auth user creation)
 *    - completeDriverProfileForExistingAuthUser(...)
 *    - completeInsuranceProfileForExistingAuthUser(...)
 */
public class SetUsernamePasswordViewModel extends ViewModel {

    private final SetUsernamePasswordRepository repo = new SetUsernamePasswordRepository();

    private final MutableLiveData<Boolean> signUpSuccess = new MutableLiveData<>(false);
    private final MutableLiveData<String> signUpError = new MutableLiveData<>(null);

    public LiveData<Boolean> getSignUpSuccess() {
        return signUpSuccess;
    }

    public LiveData<String> getSignUpError() {
        return signUpError;
    }

    // =========================================================
    // Flow A: New DRIVER signup (creates Auth + Firestore)
    // =========================================================
    public void signUp(String email,
                       String password,
                       String confirmPassword,
                       String role,
                       Driver driver) {

        String safeRole = (role == null || role.trim().isEmpty())
                ? "driver"
                : role.trim().toLowerCase();

        // Email validation
        if (isBlank(email)) {
            signUpError.setValue("נא להזין אימייל");
            return;
        }

        // Password validations
        String passValidation = validatePasswords(password, confirmPassword);
        if (passValidation != null) {
            signUpError.setValue(passValidation);
            return;
        }

        if ("driver".equals(safeRole) && driver == null) {
            signUpError.setValue("שגיאה בנתוני הנהג");
            return;
        }

        signUpError.setValue(null);

        repo.registerDriver(email.trim(), password.trim(), driver, new SetUsernamePasswordRepository.SignUpCallback() {
            @Override
            public void onSuccess() {
                signUpSuccess.postValue(true);
            }

            @Override
            public void onError(Exception e) {
                String msg = extractMessageOrFallback(e, "שגיאה בהרשמה. נסה שוב");
                signUpError.postValue("שגיאה בהרשמה: " + msg);
            }
        });
    }

    // =========================================================
    // Flow A: New INSURANCE signup (creates Auth + Firestore)
    // =========================================================
    public void signUpInsurance(String email,
                                String password,
                                String confirmPassword,
                                String firstName,
                                String lastName,
                                String phone,
                                String companyId,
                                String insuranceLogoUriLocal) {

        if (isBlank(email)) {
            signUpError.setValue("נא להזין אימייל");
            return;
        }

        String passValidation = validatePasswords(password, confirmPassword);
        if (passValidation != null) {
            signUpError.setValue(passValidation);
            return;
        }

        if (isBlank(companyId)) {
            signUpError.setValue("נא לבחור חברת ביטוח");
            return;
        }

        Driver insuranceContact = new Driver();
        insuranceContact.setFirstName(safe(firstName));
        insuranceContact.setLastName(safe(lastName));
        insuranceContact.setEmail(email.trim());
        insuranceContact.setPhone(safe(phone));

        signUpError.setValue(null);

        repo.registerInsurance(
                email.trim(),
                password.trim(),
                insuranceContact,
                companyId.trim(),
                insuranceLogoUriLocal,
                new SetUsernamePasswordRepository.SignUpCallback() {
                    @Override
                    public void onSuccess() {
                        signUpSuccess.postValue(true);
                    }

                    @Override
                    public void onError(Exception e) {
                        String raw = (e != null && e.getMessage() != null) ? e.getMessage().trim() : "";

                        // Business marker from repository
                        if ("EMAIL_ALREADY_EXISTS".equals(raw)) {
                            signUpError.postValue("החברה כבר רשומה במערכת. נא להתחבר עם אימייל וסיסמה.");
                            return;
                        }

                        String msg = !raw.isEmpty() ? raw : "שגיאה בהרשמה. נסה שוב";
                        signUpError.postValue("שגיאה בהרשמה: " + msg);
                    }
                }
        );
    }

    // =========================================================
    // Flow B: Existing AUTH user -> COMPLETE DRIVER PROFILE ONLY
    // (No createUserWithEmailAndPassword)
    // =========================================================
    public void completeDriverProfileForExistingAuthUser(String uid, Driver driver) {
        if (isBlank(uid)) {
            signUpError.setValue("שגיאה: uid חסר");
            return;
        }

        if (driver == null) {
            signUpError.setValue("שגיאה בנתוני הנהג");
            return;
        }

        if (isBlank(driver.getEmail())) {
            signUpError.setValue("שגיאה: אימייל חסר");
            return;
        }

        signUpError.setValue(null);

        repo.completeDriverProfileForExistingAuthUser(uid.trim(), driver,
                new SetUsernamePasswordRepository.SignUpCallback() {
                    @Override
                    public void onSuccess() {
                        signUpSuccess.postValue(true);
                    }

                    @Override
                    public void onError(Exception e) {
                        String msg = extractMessageOrFallback(e, "שגיאה בהשלמת פרטי נהג");
                        signUpError.postValue("שגיאה בהשלמת פרטי נהג: " + msg);
                    }
                });
    }

    // =========================================================
    // Flow B: Existing AUTH user -> COMPLETE INSURANCE PROFILE ONLY
    // (No createUserWithEmailAndPassword)
    // =========================================================
    public void completeInsuranceProfileForExistingAuthUser(String uid,
                                                            String email,
                                                            String firstName,
                                                            String lastName,
                                                            String phone,
                                                            String companyId,
                                                            String insuranceLogoUriLocal) {
        if (isBlank(uid)) {
            signUpError.setValue("שגיאה: uid חסר");
            return;
        }

        if (isBlank(email)) {
            signUpError.setValue("נא להזין אימייל");
            return;
        }

        if (isBlank(companyId)) {
            signUpError.setValue("נא לבחור חברת ביטוח");
            return;
        }

        Driver insuranceContact = new Driver();
        insuranceContact.setFirstName(safe(firstName));
        insuranceContact.setLastName(safe(lastName));
        insuranceContact.setEmail(email.trim());
        insuranceContact.setPhone(safe(phone));

        signUpError.setValue(null);

        repo.completeInsuranceProfileForExistingAuthUser(
                uid.trim(),
                insuranceContact,
                companyId.trim(),
                insuranceLogoUriLocal,
                new SetUsernamePasswordRepository.SignUpCallback() {
                    @Override
                    public void onSuccess() {
                        signUpSuccess.postValue(true);
                    }

                    @Override
                    public void onError(Exception e) {
                        String msg = extractMessageOrFallback(e, "שגיאה בהשלמת פרטי חברת ביטוח");
                        signUpError.postValue("שגיאה בהשלמת פרטי חברת ביטוח: " + msg);
                    }
                }
        );
    }

    // =========================================================
    // Public utility
    // =========================================================
    public void resetState() {
        signUpSuccess.setValue(false);
        signUpError.setValue(null);
    }

    // =========================================================
    // Private helpers
    // =========================================================
    private String validatePasswords(String password, String confirmPassword) {
        if (isBlank(password) || isBlank(confirmPassword)) {
            return "נא להזין סיסמה ולאשר אותה";
        }

        String p = password.trim();
        String cp = confirmPassword.trim();

        if (p.length() < 6) {
            return "הסיסמה חייבת להכיל לפחות 6 תווים";
        }

        if (!p.equals(cp)) {
            return "הסיסמאות אינן תואמות";
        }

        return null;
    }

    private String extractMessageOrFallback(Exception e, String fallback) {
        if (e != null && e.getMessage() != null && !e.getMessage().trim().isEmpty()) {
            return e.getMessage().trim();
        }
        return fallback;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
