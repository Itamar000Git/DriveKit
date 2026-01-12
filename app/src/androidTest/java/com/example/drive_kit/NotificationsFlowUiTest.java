package com.example.drive_kit;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.containsString;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.drive_kit.View.HomeActivity;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import android.view.View;
import android.Manifest;

import androidx.test.rule.GrantPermissionRule;
import org.junit.Rule;


@RunWith(AndroidJUnit4.class)
public class NotificationsFlowUiTest {

    private static final String EMAIL = "aaa@gmail.com";
    private static final String PASS  = "123456";

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    );

    @Before
    public void seedDriverWithTestDueSoon() throws Exception {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // התחברות
        Tasks.await(auth.signInWithEmailAndPassword(EMAIL, PASS));
        String uid = auth.getCurrentUser().getUid();

        long now = System.currentTimeMillis();

        long testDateMillis = now - TimeUnit.DAYS.toMillis(351);

        Map<String, Object> driver = new HashMap<>();
        driver.put("firstName", "testuser");
        driver.put("lastName", "ui");
        driver.put("insuranceDateMillis", 0L);
        driver.put("testDateMillis", testDateMillis);
        driver.put("treatmentDateMillis", 0L);
        driver.put("dismissedInsuranceStage", null);

        Tasks.await(
                FirebaseFirestore.getInstance()
                        .collection("drivers")
                        .document(uid)
                        .set(driver)
        );
    }

    @Test
    public void newDriver_withSpecificTestDate_showsTestNotification() {
        ActivityScenario.launch(HomeActivity.class);

        onView(isRoot()).perform(waitFor(1500));

        onView(withId(R.id.noty_icon)).perform(click());

        onView(withId(R.id.notificationsContainer))
                .check(matches(isDisplayed()));

        onView(isRoot()).perform(waitFor(1500));

        onView(withText(containsString("בעוד פחות מ 14 ימים יפוג תוקף הטסט שלך")))
                .check(matches(isDisplayed()));
    }

    private static ViewAction waitFor(long millis) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "Wait for " + millis + " milliseconds.";
            }

            @Override
            public void perform(UiController uiController, View view) {
                uiController.loopMainThreadForAtLeast(millis);
            }
        };
    }
}
