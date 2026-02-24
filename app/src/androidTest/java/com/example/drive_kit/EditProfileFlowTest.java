package com.example.drive_kit;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.isEmptyOrNullString;

import android.Manifest;
import android.os.Build;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import com.example.drive_kit.View.EditProfileActivity;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class EditProfileFlowTest {

    private static final String EMAIL = "test@gmail.com";
    private static final String PASS  = "Aa123456!";

    @Rule
    public GrantPermissionRule locationRule = GrantPermissionRule.grant(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    );

    @Rule
    public GrantPermissionRule notificationRule =
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    ? GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)
                    : GrantPermissionRule.grant();

    @Test
    public void editProfile_emailIsNotEditable() throws Exception {
        signIn(EMAIL, PASS);

        try (ActivityScenario<EditProfileActivity> scenario =
                     ActivityScenario.launch(EditProfileActivity.class)) {

            // emailEditText has android:enabled="false" in XML
            onView(withId(R.id.emailEditText))
                    .check(matches(not(isEnabled())));
        }
    }

    @Test
    public void editProfile_editName_savesToFirestore_thenRestores() throws Exception {
        signIn(EMAIL, PASS);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        String uid = auth.getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Read original (so we can restore later)
        DocumentSnapshot doc = Tasks.await(
                db.collection("drivers").document(uid).get(),
                15, TimeUnit.SECONDS
        );

        String originalFirst = safe(doc.getString("firstName"));

        String newFirst = "TestFirst";

        try (ActivityScenario<EditProfileActivity> scenario =
                     ActivityScenario.launch(EditProfileActivity.class)) {

            // Edit fields (IDs from edit_profile_activity.xml)
            onView(withId(R.id.firstNameEditText))
                    .perform(scrollTo(), replaceText(newFirst), closeSoftKeyboard());


            // Save (ID from XML)
            onView(withId(R.id.saveButton))
                    .perform(scrollTo(), click());

            // Verify Firestore updated (polling)
            waitUntilFirestoreNameEquals(uid, newFirst, 15000);

        } finally {
            // Restore original names (cleanup)
            Map<String, Object> restore = new HashMap<>();
            restore.put("firstName", originalFirst);

            Tasks.await(
                    db.collection("drivers")
                            .document(uid)
                            .set(restore, SetOptions.merge()),
                    15, TimeUnit.SECONDS
            );
        }
    }

    // ===== helpers =====

    private static void signIn(String email, String pass) throws Exception {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signOut();
        Tasks.await(auth.signInWithEmailAndPassword(email, pass), 20, TimeUnit.SECONDS);
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private static void waitUntilFirestoreNameEquals(String uid, String first, long timeoutMs) throws Exception {
        long end = System.currentTimeMillis() + timeoutMs;
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        while (System.currentTimeMillis() < end) {
            DocumentSnapshot doc = Tasks.await(
                    db.collection("drivers").document(uid).get(),
                    5, TimeUnit.SECONDS
            );

            String f = safe(doc.getString("firstName"));


            if (first.equals(f)) return;

            Thread.sleep(250);
        }

        // final failure with a clear message
        DocumentSnapshot doc = Tasks.await(
                db.collection("drivers").document(uid).get(),
                5, TimeUnit.SECONDS
        );
        String f = safe(doc.getString("firstName"));
        throw new AssertionError("Firestore name not updated. Expected: " + first + " Got: " + f + ".");
    }
}