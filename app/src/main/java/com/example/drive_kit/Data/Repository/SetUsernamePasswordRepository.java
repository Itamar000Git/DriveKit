package com.example.drive_kit.Data.Repository;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.drive_kit.Model.Driver;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

/**
 * SetUsernamePasswordRepository
 *
 * Responsibilities:
 * 1) NEW DRIVER signup:
 *    - create FirebaseAuth user
 *    - optional upload driver car image
 *    - write Firestore drivers/{uid}
 *
 * 2) NEW INSURANCE signup:
 *    - create FirebaseAuth user
 *    - update existing insurance_companies/{companyId} as partner
 *    - optional logo upload -> logoUrl
 *
 * 3) EXISTING AUTH user completion (NO createUser):
 *    - completeDriverProfileForExistingAuthUser(...)
 *    - completeInsuranceProfileForExistingAuthUser(...)
 */
public class SetUsernamePasswordRepository {

    // =========================================================
    // Callbacks
    // =========================================================
    public interface SignUpCallback {
        void onSuccess();
        void onError(Exception e);
    }

    private interface UrlSuccess {
        void onSuccess(String downloadUrl);
    }

    private interface ErrorCb {
        void onError(Exception e);
    }

    // =========================================================
    // A) NEW DRIVER SIGNUP (creates auth)
    // =========================================================
    public void registerDriver(String email,
                               String password,
                               Driver driver,
                               SignUpCallback cb) {

        String safeEmail = safe(email);
        String safePass = safe(password);

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

                    // Optional local car photo upload
                    String carPhotoUriStr = extractDriverCarImageUri(driver);

                    if (isBlank(carPhotoUriStr) || isRemoteUrl(carPhotoUriStr)) {
                        saveDriverToFirestore(uid, driver, cb, user);
                        return;
                    }

                    Uri localUri = Uri.parse(carPhotoUriStr);
                    uploadCarPhotoToStorage(uid, localUri,
                            downloadUrl -> {
                                if (driver != null && driver.getCar() != null) {
                                    driver.getCar().setCarImageUri(downloadUrl);
                                }
                                saveDriverToFirestore(uid, driver, cb, user);
                            },
                            e -> rollbackAuthUser(user, () -> cb.onError(e)));
                })
                .addOnFailureListener(cb::onError);
    }

    // =========================================================
    // B) NEW INSURANCE SIGNUP (creates auth)
    // =========================================================
    public void registerInsurance(String email,
                                  String password,
                                  Driver insuranceContact,
                                  String companyId,
                                  String insuranceLogoUriLocal,
                                  SignUpCallback cb) {

        String safeEmail = safe(email);
        String safePass = safe(password);
        String safeCompanyId = safe(companyId);


        if (safeEmail.isEmpty() || safePass.isEmpty() || safeCompanyId.isEmpty()) {
            cb.onError(new IllegalArgumentException("Missing fields"));
            return;
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.createUserWithEmailAndPassword(safeEmail, safePass)
                .addOnSuccessListener(res -> {
                    FirebaseUser user = res.getUser();
                    if (user == null) {
                        cb.onError(new IllegalStateException("User null after createUser"));
                        return;
                    }

                    upsertInsuranceCompany(
                            user,
                            insuranceContact,
                            safeCompanyId,
                            insuranceLogoUriLocal,
                            true,   // allow rollback on error (new signup flow)
                            cb
                    );
                })
                .addOnFailureListener(createErr -> {
                    String m = createErr.getMessage() == null ? "" : createErr.getMessage().toLowerCase();

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

    // =========================================================
    // C) EXISTING AUTH USER - COMPLETE DRIVER PROFILE ONLY
    // (NO createUserWithEmailAndPassword)
    // =========================================================
    public void completeDriverProfileForExistingAuthUser(String uid,
                                                         Driver driver,
                                                         SignUpCallback cb) {
        String safeUid = safe(uid);
        if (safeUid.isEmpty()) {
            cb.onError(new IllegalArgumentException("uid is required"));
            return;
        }

        if (driver == null) {
            cb.onError(new IllegalArgumentException("Driver object is null"));
            return;
        }

        String carPhotoUriStr = extractDriverCarImageUri(driver);

        // No local upload needed
        if (isBlank(carPhotoUriStr) || isRemoteUrl(carPhotoUriStr)) {
            upsertDriverProfileOnly(safeUid, driver, cb);
            return;
        }

        // Local image -> upload then save
        Uri localUri = Uri.parse(carPhotoUriStr);
        uploadCarPhotoToStorage(safeUid, localUri,
                downloadUrl -> {
                    if (driver.getCar() != null) {
                        driver.getCar().setCarImageUri(downloadUrl);
                    }
                    upsertDriverProfileOnly(safeUid, driver, cb);
                },
                cb::onError);
    }

    // =========================================================
    // D) EXISTING AUTH USER - COMPLETE INSURANCE PROFILE ONLY
    // (NO createUserWithEmailAndPassword)
    // =========================================================
    public void completeInsuranceProfileForExistingAuthUser(String uid,
                                                            Driver insuranceContact,
                                                            String companyId,
                                                            String insuranceLogoUriLocal,
                                                            SignUpCallback cb) {
        String safeUid = safe(uid);
        String safeCompanyId = safe(companyId);

        if (safeUid.isEmpty()) {
            cb.onError(new IllegalArgumentException("uid is required"));
            return;
        }

        if (safeCompanyId.isEmpty()) {
            cb.onError(new IllegalArgumentException("companyId is required"));
            return;
        }

        FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
        if (current == null || current.getUid() == null || !safeUid.equals(current.getUid())) {
            cb.onError(new IllegalStateException("Current auth user mismatch"));
            return;
        }

        upsertInsuranceCompany(
                current,
                insuranceContact,
                safeCompanyId,
                insuranceLogoUriLocal,
                false,  // DO NOT rollback auth user in completion flow
                cb
        );
    }

    // =========================================================
    // Internal: Driver Firestore write
    // =========================================================
    private void saveDriverToFirestore(String uid,
                                       Driver driver,
                                       SignUpCallback cb,
                                       FirebaseUser rollbackUser) {
        if (driver == null) {
            rollbackAuthUser(rollbackUser, () ->
                    cb.onError(new IllegalArgumentException("Driver object is null")));
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("drivers")
                .document(uid)
                .set(driver) // full object write for NEW signup
                .addOnSuccessListener(aVoid -> cb.onSuccess())
                .addOnFailureListener(e -> rollbackAuthUser(rollbackUser, () -> cb.onError(e)));
    }

    /**
     * Profile completion write (existing auth user):
     * - merge to avoid accidental overwrite of unrelated fields
     */
    private void upsertDriverProfileOnly(String uid, Driver driver, SignUpCallback cb) {
        FirebaseFirestore.getInstance()
                .collection("drivers")
                .document(uid)
                .set(driver, SetOptions.merge())
                .addOnSuccessListener(v -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }

    // =========================================================
    // Internal: Insurance company upsert
    // =========================================================
    private void upsertInsuranceCompany(@NonNull FirebaseUser user,
                                        Driver insuranceContact,
                                        @NonNull String companyId,
                                        String insuranceLogoUriLocal,
                                        boolean allowRollbackAuthOnFailure,
                                        SignUpCallback cb) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference ref = db.collection("insurance_companies").document(companyId);

        ref.get()
                .addOnSuccessListener(snap -> {
                    if (!snap.exists()) {
                        cb.onError(new IllegalStateException("Company not found: " + companyId));
                        return;
                    }

                    String email = "";
                    if (insuranceContact != null && !isBlank(insuranceContact.getEmail())) {
                        email = insuranceContact.getEmail().trim();
                    }
                    if (email.isEmpty() && user.getEmail() != null) {
                        email = user.getEmail().trim();
                    }

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("isPartner", true);
                    updates.put("partnerUid", user.getUid());
                    updates.put("email", email);
                    updates.put("updatedAt", Timestamp.now());

                    if (insuranceContact != null) {
                        updates.put("phone", safe(insuranceContact.getPhone()));
                        updates.put("contactFirstName", safe(insuranceContact.getFirstName()));
                        updates.put("contactLastName", safe(insuranceContact.getLastName()));
                    }

                    ref.set(updates, SetOptions.merge())
                            .addOnSuccessListener(v -> {
                                maybeUploadInsuranceLogo(
                                        companyId,
                                        insuranceLogoUriLocal,
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                cb.onSuccess();
                                            }
                                        },
                                        e -> {
                                            if (allowRollbackAuthOnFailure) {
                                                rollbackAuthUser(user, () -> cb.onError(e));
                                            } else {
                                                cb.onError(e);
                                            }
                                        }
                                );
                            })
                            .addOnFailureListener(e -> {
                                if (allowRollbackAuthOnFailure) {
                                    rollbackAuthUser(user, () -> cb.onError(e));
                                } else {
                                    cb.onError(e);
                                }
                            });
                })
                .addOnFailureListener(cb::onError);
    }

    // =========================================================
    // Storage helpers
    // =========================================================
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
     * Upload insurance logo (optional).
     * If logoUriLocal is:
     * - empty/null: just continue success
     * - http/https: save directly as logoUrl
     * - local Uri: upload then save logoUrl
     */
    private void maybeUploadInsuranceLogo(String companyId,
                                          String logoUriLocal,
                                          Runnable onDone,
                                          ErrorCb onError) {
        if (isBlank(companyId)) {
            onDone.run();
            return;
        }

        if (isBlank(logoUriLocal)) {
            onDone.run();
            return;
        }

        String s = logoUriLocal.trim();
        DocumentReference companyRef = FirebaseFirestore.getInstance()
                .collection("insurance_companies")
                .document(companyId);

        if (isRemoteUrl(s)) {
            companyRef.update("logoUrl", s)
                    .addOnSuccessListener(v -> onDone.run())
                    .addOnFailureListener(onError::onError);
            return;
        }

        Uri uri = Uri.parse(s);
        StorageReference ref = FirebaseStorage.getInstance()
                .getReference()
                .child("insurance_logos")
                .child(companyId)
                .child("logo.jpg");

        ref.putFile(uri)
                .addOnSuccessListener(taskSnapshot ->
                        ref.getDownloadUrl()
                                .addOnSuccessListener(downloadUri ->
                                        companyRef.update("logoUrl", downloadUri.toString())
                                                .addOnSuccessListener(v -> onDone.run())
                                                .addOnFailureListener(onError::onError)
                                )
                                .addOnFailureListener(onError::onError)
                )
                .addOnFailureListener(onError::onError);
    }

    // =========================================================
    // Rollback
    // =========================================================
    private void rollbackAuthUser(FirebaseUser user, Runnable after) {
        if (user == null) {
            after.run();
            return;
        }
        user.delete()
                .addOnCompleteListener(t -> after.run());
    }

    // =========================================================
    // Utils
    // =========================================================
    private String extractDriverCarImageUri(Driver driver) {
        try {
            if (driver != null && driver.getCar() != null) {
                return driver.getCar().getCarImageUri();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private boolean isRemoteUrl(String s) {
        if (s == null) return false;
        String t = s.trim().toLowerCase();
        return t.startsWith("http://") || t.startsWith("https://");
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
