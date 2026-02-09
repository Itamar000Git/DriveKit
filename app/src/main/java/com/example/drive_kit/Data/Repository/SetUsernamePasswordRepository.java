package com.example.drive_kit.Data.Repository;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.drive_kit.Model.Driver;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

/**
 * SetUsernamePasswordRepository
 *
 * Data-layer class responsible for authentication and persistence operations
 * used by the final signup/login screens.
 *
 * Main responsibilities:
 * 1) Driver registration:
 *    - Create Firebase Auth user
 *    - Optionally upload local car photo
 *    - Save Driver document in Firestore
 * 2) Insurance registration:
 *    - Create Firebase Auth user
 *    - Mark existing insurance company doc as partner (isPartner=true)
 * 3) Insurance login:
 *    - Sign in with email/password
 *    - Resolve linked insurance company by partnerUid
 *
 * Notes:
 * - Asynchronous callbacks are exposed via SignUpCallback / LoginCallback.
 * - Some flows include rollback (delete auth user) on partial failure.
 */
public class SetUsernamePasswordRepository {

    /**
     * Callback for signup flows (driver/insurance).
     */
    public interface SignUpCallback {
        void onSuccess();
        void onError(Exception e);
    }

    /**
     * Callback for insurance login flow.
     * onSuccess returns the resolved insurance company document ID.
     */
    public interface LoginCallback {
        void onSuccess(String companyId);
        void onError(Exception e);
    }

    // =========================================================
    // DRIVER FLOW
    // =========================================================
    /**
     * Registers a driver account.
     *
     * Flow:
     * 1) Validate email/password.
     * 2) Create Firebase Auth user.
     * 3) If car image URI is local -> upload to Storage and replace URI with download URL.
     * 4) Save Driver object into Firestore collection "drivers/{uid}".
     *
     * Rollback:
     * - If photo upload or Firestore write fails after auth creation,
     *   auth user is deleted to avoid partial/inconsistent state.
     */
    public void registerDriver(String email,
                               String password,
                               Driver driver,
                               SignUpCallback cb) {

        // Normalize credentials
        String safeEmail = email == null ? "" : email.trim();
        String safePass  = password == null ? "" : password.trim();

        // Basic required validation
        if (safeEmail.isEmpty() || safePass.isEmpty()) {
            cb.onError(new IllegalArgumentException("חסרים פרטים (email/password)"));
            return;
        }

        FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(safeEmail, safePass)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user == null) {
                        cb.onError(new IllegalStateException("User is null after signup"));
                        return;
                    }

                    String uid = user.getUid();

                    // Try to read driver's car image URI (if exists)
                    String carPhotoUriStr = null;
                    if (driver != null && driver.getCar() != null) {
                        carPhotoUriStr = driver.getCar().getCarImageUri();
                    }

                    // If no local image to upload (empty or already remote URL) -> save directly
                    if (carPhotoUriStr == null || carPhotoUriStr.trim().isEmpty()
                            || carPhotoUriStr.startsWith("http://")
                            || carPhotoUriStr.startsWith("https://")) {
                        saveDriverToFirestore(uid, driver, cb, user);
                        return;
                    }

                    // Local URI -> upload to Firebase Storage first
                    Uri carPhotoUri = Uri.parse(carPhotoUriStr);
                    uploadCarPhotoToStorage(uid, carPhotoUri,
                            downloadUrl -> {
                                // Replace local URI with hosted download URL before Firestore save
                                driver.getCar().setCarImageUri(downloadUrl);
                                saveDriverToFirestore(uid, driver, cb, user);
                            },
                            e -> rollbackAuthUser(user, () -> cb.onError(e)));
                })
                .addOnFailureListener(cb::onError);
    }

    // =========================================================
    // INSURANCE SIGNUP FLOW
    // =========================================================
    /**
     * Registers an insurance user and links it to an existing insurance company doc.
     *
     * Business rule here:
     * - companyId must already exist in "insurance_companies".
     * - On success, that company doc is updated with:
     *   isPartner=true, partnerUid=<auth uid>, contact/email fields, updatedAt.
     *
     * Behavior on existing email:
     * - Detects "already in use" patterns from Firebase error message
     * - Returns "EMAIL_ALREADY_EXISTS" marker via callback error.
     */
    public void registerInsurance(String email,
                                  String password,
                                  Driver insuranceContact,
                                  String companyId,
                                  SignUpCallback cb) {

        // Normalize inputs
        String safeEmail = email == null ? "" : email.trim();
        String safePass = password == null ? "" : password.trim();
        String safeCompanyId = companyId == null ? "" : companyId.trim().toLowerCase();

        // Required validation
        if (safeEmail.isEmpty() || safePass.isEmpty() || safeCompanyId.isEmpty()) {
            cb.onError(new IllegalArgumentException("Missing fields"));
            return;
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();

        // קודם ננסה create
        auth.createUserWithEmailAndPassword(safeEmail, safePass)
                .addOnSuccessListener(res -> {
                    FirebaseUser user = res.getUser();
                    if (user == null) {
                        cb.onError(new IllegalStateException("User null after createUser"));
                        return;
                    }
                    // Update/claim existing insurance company doc as active partner
                    upsertInsuranceCompany(user, insuranceContact, safeCompanyId, cb);
                })
                .addOnFailureListener(createErr -> {
                    String m = createErr.getMessage() == null ? "" : createErr.getMessage().toLowerCase();

                    // Heuristic check for duplicate-email condition
                    boolean alreadyExists =
                            m.contains("already in use")
                                    || m.contains("email address is already in use")
                                    || m.contains("email already in use");

                    if (alreadyExists) {
                        cb.onError(new IllegalStateException("EMAIL_ALREADY_EXISTS"));
                        return;
                    }

                    cb.onError(createErr);
                });
    }

//    // =========================================================
//    // INSURANCE LOGIN FLOW
//    // =========================================================
//    /**
//     * Logs in an insurance user and resolves the linked insurance company.
//     *
//     * Flow:
//     * 1) Sign in with email/password.
//     * 2) Query insurance_companies where partnerUid == auth uid.
//     * 3) Require isPartner == true.
//     * 4) Return company doc ID via callback.
//     */
//    public void loginInsurance(String email, String password, LoginCallback cb) {
//        // Normalize credentials
//        String safeEmail = email == null ? "" : email.trim();
//        String safePass = password == null ? "" : password.trim();
//
//        // Required validation
//        if (safeEmail.isEmpty() || safePass.isEmpty()) {
//            cb.onError(new IllegalArgumentException("Missing email/password"));
//            return;
//        }
//
//        FirebaseAuth auth = FirebaseAuth.getInstance();
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//
//        auth.signInWithEmailAndPassword(safeEmail, safePass)
//                .addOnSuccessListener(res -> {
//                    FirebaseUser user = res.getUser();
//                    if (user == null || user.getUid() == null) {
//                        cb.onError(new IllegalStateException("User null after login"));
//                        return;
//                    }
//
//                    String uid = user.getUid();
//                    db.collection("insurance_companies")
//                            .whereEqualTo("partnerUid", uid)
//                            .limit(1)
//                            .get()
//                            .addOnSuccessListener(qs -> {
//                                if (qs.isEmpty()) {
//                                    cb.onError(new IllegalStateException("No insurance company linked to this user"));
//                                    return;
//                                }
//
//                                DocumentSnapshot doc = qs.getDocuments().get(0);
//                                Boolean isPartner = doc.getBoolean("isPartner");
//                                if (isPartner == null || !isPartner) {
//                                    cb.onError(new IllegalStateException("Company is not active partner"));
//                                    return;
//                                }
//
//                                // Return resolved insurance company ID
//                                cb.onSuccess(doc.getId());
//                            })
//                            .addOnFailureListener(cb::onError);
//                })
//                .addOnFailureListener(cb::onError);
//    }

    // =========================================================
    // Firestore - Driver
    // =========================================================
    /**
     * Saves Driver object under "drivers/{uid}".
     *
     * If driver is null or write fails, performs auth rollback (delete created user)
     * and forwards error through callback.
     */
    private void saveDriverToFirestore(String uid, Driver driver, SignUpCallback cb, FirebaseUser rollbackUser) {
        if (driver == null) {
            rollbackAuthUser(rollbackUser, () ->
                    cb.onError(new IllegalArgumentException("Driver object is null")));
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("drivers")
                .document(uid)
                .set(driver)
                .addOnSuccessListener(aVoid -> cb.onSuccess())
                .addOnFailureListener(e -> rollbackAuthUser(rollbackUser, () -> cb.onError(e)));
    }

    // =========================================================
    // Firestore - Insurance update existing company doc
    // =========================================================
    /**
     * Updates an existing insurance company document with partner linkage.
     *
     * Preconditions:
     * - Document insurance_companies/{companyId} must exist.
     *
     * Updates (merge):
     * - isPartner = true
     * - email
     * - partnerUid
     * - updatedAt
     * - optional contact fields: phone/contactFirstName/contactLastName
     */
    private void upsertInsuranceCompany(@NonNull FirebaseUser user,
                                        Driver insuranceContact,
                                        @NonNull String companyId,
                                        SignUpCallback cb) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference ref = db.collection("insurance_companies").document(companyId);

        ref.get()
                .addOnSuccessListener(snap -> {
                    // Business rule: company must pre-exist in DB
                    if (!snap.exists()) {
                        cb.onError(new IllegalStateException("Company not found: " + companyId));
                        return;
                    }

                    // Prefer contact email from payload, fallback to auth email
                    String email = "";
                    if (insuranceContact != null && insuranceContact.getEmail() != null) {
                        email = insuranceContact.getEmail().trim();
                    }
                    if (email.isEmpty() && user.getEmail() != null) {
                        email = user.getEmail().trim();
                    }

                    // Fields to merge into insurance company document
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("isPartner", true);
                    updates.put("email", email);
                    updates.put("partnerUid", user.getUid());
                    updates.put("updatedAt", Timestamp.now());

                    // שדות קשר אופציונליים
                    if (insuranceContact != null) {
                        updates.put("phone", safe(insuranceContact.getPhone()));
                        updates.put("contactFirstName", safe(insuranceContact.getFirstName()));
                        updates.put("contactLastName", safe(insuranceContact.getLastName()));
                    }

                    ref.set(updates, SetOptions.merge())
                            .addOnSuccessListener(v -> cb.onSuccess())
                            .addOnFailureListener(cb::onError);
                })
                .addOnFailureListener(cb::onError);
    }

    /**
     * Null-safe trim helper for strings.
     */
    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    // =========================================================
    // Storage upload (driver car photo)
    // =========================================================
    /**
     * Internal callback for successful Storage upload URL retrieval.
     */
    private interface UrlSuccess {
        void onSuccess(String downloadUrl);
    }

    /**
     * Internal callback for upload failure.
     */
    private interface ErrorCb {
        void onError(Exception e);
    }

    /**
     * Uploads car photo to:
     *   car_photos/{uid}/car.jpg
     * then returns public download URL through UrlSuccess.
     */
    private void uploadCarPhotoToStorage(String uid, Uri uri, UrlSuccess ok, ErrorCb fail) {
        StorageReference ref = FirebaseStorage.getInstance()
                .getReference()
                .child("car_photos")
                .child(uid)
                .child("car.jpg");

        ref.putFile(uri)
                .addOnSuccessListener(taskSnapshot ->
                        ref.getDownloadUrl()
                                .addOnSuccessListener(downloadUri -> ok.onSuccess(downloadUri.toString()))
                                .addOnFailureListener(fail::onError)
                )
                .addOnFailureListener(fail::onError);
    }

    /**
     * Best-effort rollback:
     * Deletes the auth user (if exists), then runs follow-up action.
     * Used to avoid partial signup state after downstream failures.
     */
    private void rollbackAuthUser(FirebaseUser user, Runnable after) {
        if (user == null) {
            after.run();
            return;
        }
        user.delete().addOnCompleteListener(t -> after.run());
    }
}
