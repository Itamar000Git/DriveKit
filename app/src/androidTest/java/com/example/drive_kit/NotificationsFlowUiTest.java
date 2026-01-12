package com.example.drive_kit;

import android.Manifest;
import android.os.Build;
import android.os.SystemClock;
import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import com.example.drive_kit.View.HomeActivity;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;

@RunWith(AndroidJUnit4.class)
public class NotificationsFlowUiTest {

    private static final String PASS = "123456";
    private static final String EMAIL_AAA = "aaa@gmail.com";
    private static final String EMAIL_BBB = "bbb@gmail.com";

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
    public void existingDriver_testDueSoon_showsTestNotification_forAAA() throws Exception {
        signIn(EMAIL_AAA, PASS);

        long now = System.currentTimeMillis();
        int daysLeft = 7;
        long testDateMillis = now - TimeUnit.DAYS.toMillis(365 - daysLeft);

        Map<String, Object> updates = new HashMap<>();
        updates.put("testDateMillis", testDateMillis);
        updates.put("dismissedTestStage", null);

        mergeDriverUpdatesForCurrentUser(updates);

        ActivityScenario.launch(HomeActivity.class);

        onView(withId(R.id.noty_icon)).perform(click());
        onView(withId(R.id.notificationsContainer)).check(matches(isDisplayed()));

        waitUntilTextDisplayed("תוקף הטסט", 30_000);
        onView(withText(containsString("תוקף הטסט"))).check(matches(isDisplayed()));
    }

    @Test
    public void existingDriver_insuranceDueSoon_defer_hidesInsuranceNotification_forBBB() throws Exception {
        signIn(EMAIL_BBB, PASS);

        long now = System.currentTimeMillis();
        int daysLeft = 7;
        long insuranceDateMillis = now - TimeUnit.DAYS.toMillis(365 - daysLeft);

        Map<String, Object> updates = new HashMap<>();
        updates.put("insuranceDateMillis", insuranceDateMillis);
        updates.put("dismissedInsuranceStage", null);

        mergeDriverUpdatesForCurrentUser(updates);

        ActivityScenario.launch(HomeActivity.class);

        onView(withId(R.id.noty_icon)).perform(click());
        onView(withId(R.id.notificationsContainer)).check(matches(isDisplayed()));

        waitUntilTextDisplayed("תוקף הביטוח", 30_000);

        Matcher<View> insuranceCard = allOf(
                hasDescendant(withId(R.id.notificationText)),
                hasDescendant(withText(containsString("תוקף הביטוח")))
        );

        onView(allOf(withId(R.id.btnDefer), isDescendantOfA(insuranceCard)))
                .perform(click());

        waitUntilTextGone("תוקף הביטוח", 30_000);
        onView(withText(containsString("תוקף הביטוח"))).check(doesNotExist());
    }

    private static void signIn(String email, String pass) throws Exception {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signOut();
        Tasks.await(auth.signInWithEmailAndPassword(email, pass), 15, TimeUnit.SECONDS);
    }

    private static void mergeDriverUpdatesForCurrentUser(Map<String, Object> updates) throws Exception {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String uid = auth.getCurrentUser().getUid();

        Tasks.await(
                FirebaseFirestore.getInstance()
                        .collection("drivers")
                        .document(uid)
                        .set(updates, SetOptions.merge()),
                15,
                TimeUnit.SECONDS
        );
    }

    private static void waitUntilTextDisplayed(String partialText, long timeoutMs) {
        long end = SystemClock.elapsedRealtime() + timeoutMs;

        while (SystemClock.elapsedRealtime() < end) {
            try {
                onView(withText(containsString(partialText))).check(matches(isDisplayed()));
                return;
            } catch (Throwable ignored) {
                SystemClock.sleep(200);
            }
        }

        onView(withText(containsString(partialText))).check(matches(isDisplayed()));
    }

    private static void waitUntilTextGone(String partialText, long timeoutMs) {
        long end = SystemClock.elapsedRealtime() + timeoutMs;

        while (SystemClock.elapsedRealtime() < end) {
            try {
                onView(withText(containsString(partialText))).check(doesNotExist());
                return;
            } catch (Throwable ignored) {
                SystemClock.sleep(200);
            }
        }

        onView(withText(containsString(partialText))).check(doesNotExist());
    }
}
