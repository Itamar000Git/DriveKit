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
 * 1) Validate password-related inputs before making repository calls.
 * 2) Trigger registration flows via SetUsernamePasswordRepository.
 * 3) Expose registration state to the UI through LiveData:
 *    - signUpSuccess: true when operation completed successfully.
 *    - signUpError: user-facing error text when operation failed.
 *
 * Notes:
 * - This ViewModel handles both driver and insurance registration paths.
 * - Input validation is done here to keep Activities/UI thinner.
 */
public class SetUsernamePasswordViewModel extends ViewModel {

    /** Repository layer that performs Firebase/Auth/Firestore operations. */
    private final SetUsernamePasswordRepository repo = new SetUsernamePasswordRepository();

    /** Emits true on successful registration flow completion. */
    private final MutableLiveData<Boolean> signUpSuccess = new MutableLiveData<>(false);

    /** Emits user-facing error text when validation/repository fails. */
    private final MutableLiveData<String> signUpError = new MutableLiveData<>(null);

    /** Read-only success state exposed to UI (Activity/Fragment). */
    public LiveData<Boolean> getSignUpSuccess() {
        return signUpSuccess;
    }

    /** Read-only error state exposed to UI (Activity/Fragment). */
    public LiveData<String> getSignUpError() {
        return signUpError;
    }

    /**
     * Driver registration flow (called from final password screen).
     *
     * Parameters:
     * - email/password/confirmPassword: credentials entered by user.
     * - role: "driver" / "insurance" (normalized here, default "driver").
     * - driver: required when role is "driver".
     */
    public void signUp(String email,
                       String password,
                       String confirmPassword,
                       String role,
                       Driver driver) {

        // Role normalization:
        // If role is missing/blank, default to "driver".
        String safeRole = (role == null || role.trim().isEmpty()) ? "driver" : role.trim().toLowerCase();

        // Email validation
        if (email == null || email.trim().isEmpty()) {
            signUpError.setValue("נא להזין אימייל");
            return;
        }

        // Password fields existence validation
        if (password == null || password.trim().isEmpty()
                || confirmPassword == null || confirmPassword.trim().isEmpty()) {
            signUpError.setValue("נא להזין סיסמה ולאשר אותה");
            return;
        }

        // Normalize password inputs
        String p = password.trim();
        String cp = confirmPassword.trim();

        // Password length rule
        if (p.length() < 6) {
            signUpError.setValue("הסיסמה חייבת להכיל לפחות 6 תווים");
            return;
        }

        // Password confirmation match rule
        if (!p.equals(cp)) {
            signUpError.setValue("הסיסמאות אינן תואמות");
            return;
        }

        // Driver path requires non-null Driver payload
        if ("driver".equals(safeRole) && driver == null) {
            signUpError.setValue("שגיאה בנתוני הנהג");
            return;
        }

        // Clear previous UI error before async call
        signUpError.setValue(null);

        // Trigger repository driver registration
        repo.registerDriver(email.trim(), p, driver, new SetUsernamePasswordRepository.SignUpCallback() {
            @Override
            public void onSuccess() {
                // Post value because callback may run off main thread
                signUpSuccess.postValue(true);
            }

            @Override
            public void onError(Exception e) {
                // Defensive extraction of backend error message
                String msg = (e != null && e.getMessage() != null && !e.getMessage().trim().isEmpty())
                        ? e.getMessage()
                        : "שגיאה בהרשמה. נסה שוב";
                signUpError.postValue("שגיאה בהרשמה: " + msg);
            }
        });

    }

    /**
     * Resets transient UI state.
     * Useful when re-entering screen or after navigation events.
     */
    public void resetState() {
        signUpSuccess.setValue(false);
        signUpError.setValue(null);
    }

    /**
     * Insurance registration flow.
     *
     * Validates:
     * - email presence
     * - password presence/length/match
     * - companyId presence
     *
     * Then builds a minimal Driver object used as insurance contact payload
     * and sends request to repository registerInsurance(...).
     */
    public void signUpInsurance(String email,
                                String password,
                                String confirmPassword,
                                String firstName,
                                String lastName,
                                String phone,
                                String companyId) {

        // Email validation
        if (email == null || email.trim().isEmpty()) {
            signUpError.setValue("נא להזין אימייל");
            return;
        }

        // Password fields existence validation
        if (password == null || password.trim().isEmpty()
                || confirmPassword == null || confirmPassword.trim().isEmpty()) {
            signUpError.setValue("נא להזין סיסמה ולאשר אותה");
            return;
        }

        // Normalize password inputs
        String p = password.trim();
        String cp = confirmPassword.trim();

        // Password length rule
        if (p.length() < 6) {
            signUpError.setValue("הסיסמה חייבת להכיל לפחות 6 תווים");
            return;
        }

        // Password confirmation match rule
        if (!p.equals(cp)) {
            signUpError.setValue("הסיסמאות אינן תואמות");
            return;
        }


        // Insurance company selection is mandatory
        if (companyId == null || companyId.trim().isEmpty()) {
            signUpError.setValue("נא לבחור חברת ביטוח");
            return;
        }

        // Reuse Driver model as a contact DTO for insurance side
        Driver insuranceContact = new Driver();
        insuranceContact.setFirstName(firstName == null ? "" : firstName.trim());
        insuranceContact.setLastName(lastName == null ? "" : lastName.trim());
        insuranceContact.setEmail(email.trim());
        insuranceContact.setPhone(phone == null ? "" : phone.trim());

        // Clear previous UI error before async call
        signUpError.setValue(null);

        // Trigger repository insurance registration
        repo.registerInsurance(email.trim(), p, insuranceContact, companyId.trim(),
                new SetUsernamePasswordRepository.SignUpCallback() {
                    @Override
                    public void onSuccess() {
                        // Post value because callback may run off main thread
                        signUpSuccess.postValue(true);
                    }

                    @Override
                    public void onError(Exception e) {
                        // Read raw backend marker message if available
                        String raw = (e != null && e.getMessage() != null) ? e.getMessage().trim() : "";

                        // Special business case:
                        // Email already exists -> instruct user to login instead of re-register.
                        if ("EMAIL_ALREADY_EXISTS".equals(raw)) {
                            signUpError.postValue("החברה כבר רשומה במערכת. נא להתחבר עם אימייל וסיסמה.");
                            return;
                        }

                        // Generic fallback error
                        String msg = !raw.isEmpty() ? raw : "שגיאה בהרשמה. נסה שוב";
                        signUpError.postValue("שגיאה בהרשמה: " + msg);
                    }


                });

    }

}
