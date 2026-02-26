package com.example.drive_kit;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.init;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.release;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasDataString;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import com.example.drive_kit.View.HomeActivity;
import com.example.drive_kit.View.Insurance_user.InsuranceHomeActivity;
import com.example.drive_kit.View.Insurance_user.InsuranceInquiriesActivity;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import android.content.Context;
import android.content.Intent;

import com.google.firebase.firestore.FirebaseFirestore;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class InsuranceCallTest {

    // driver user
    private static final String EMAIL = "test@gmail.com";
    private static final String PASS  = "Aa123456!";

    // partner (Ayalon)
    private static final String PARTNER_EMAIL = "ccc@gmail.com";
    private static final String PARTNER_PASS  = "Gg2002##";

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
    public void insurance_AIG_clickCall_opensDialer() throws Exception {
        signIn(EMAIL, PASS);

        init();
        try (ActivityScenario<HomeActivity> scenario = ActivityScenario.launch(HomeActivity.class)) {

            onView(withId(R.id.circleInsurance)).perform(click());
            SystemClock.sleep(5000);

            onView(withText(containsString("AIG"))).perform(scrollTo(), click());

            waitUntilViewDisplayed(R.id.bsCallBtn, 5000);

            onView(withId(R.id.bsCallBtn)).perform(click());

            intended(allOf(
                    hasAction(Intent.ACTION_DIAL),
                    hasDataString(startsWith("tel:"))
            ));

        } finally {
            release();
        }
    }

    @Test
    public void insurance_AIG_clickWebsite_opensBrowser() throws Exception {
        signIn(EMAIL, PASS);

        init();
        try (ActivityScenario<HomeActivity> scenario = ActivityScenario.launch(HomeActivity.class)) {

            onView(withId(R.id.circleInsurance)).perform(click());
            SystemClock.sleep(5000);

            onView(withText(containsString("AIG"))).perform(scrollTo(), click());

            waitUntilViewDisplayed(R.id.bsWebBtn, 5000);

            onView(withId(R.id.bsWebBtn)).perform(click());

            intended(allOf(
                    hasAction(Intent.ACTION_VIEW),
                    hasDataString(anyOf(startsWith("http://"), startsWith("https://")))
            ));

        } finally {
            release();
        }
    }


    @Test
    public void insurance_leaveDetails_AIG()  throws Exception {
        // ===== Part 1: Driver flow =====
        signIn(EMAIL, PASS);

        try (ActivityScenario<HomeActivity> homeScenario = ActivityScenario.launch(HomeActivity.class)) {

            onView(withId(R.id.circleInsurance)).perform(click());
            SystemClock.sleep(5000);

            // AIG: button hidden
            onView(withText(containsString("AIG"))).perform(scrollTo(), click());
            SystemClock.sleep(500);
            onView(withId(R.id.bsLeaveDetailsBtn)).check(matches(not(isDisplayed())));

        }
    }

    @Test
    public void hidden_Ayalon_send_thenPartnerSeesInquiry() throws Exception {

        // ===== Part 1: Driver sends details to Ayalon =====
        signIn(EMAIL, PASS);

        try (ActivityScenario<HomeActivity> homeScenario = ActivityScenario.launch(HomeActivity.class)) {

            // MUST open insurance screen first
            onView(withId(R.id.circleInsurance)).perform(click());
            SystemClock.sleep(5000);

            // Open Ayalon bottom sheet
            onView(withText(containsString("איילון"))).perform(scrollTo(), click());
            SystemClock.sleep(500);

            // Ensure button visible
            onView(withId(R.id.bsLeaveDetailsBtn)).check(matches(isDisplayed()));

            // Click and assert toast
            TestToastUtils.assertToastShownOnClick(
                    () -> onView(withId(R.id.bsLeaveDetailsBtn)).perform(click()),
                    "נשלח",
                    8000
            );
        }

        // ===== Part 2: Partner checks inquiries =====
        FirebaseAuth.getInstance().signOut();
        signIn(PARTNER_EMAIL, PARTNER_PASS);

        String companyDocId = findCompanyDocIdForCurrentPartner();

        Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Intent partnerIntent = new Intent(ctx, InsuranceHomeActivity.class);
        partnerIntent.putExtra("insuranceCompanyId", companyDocId);

        try (ActivityScenario<InsuranceHomeActivity> partnerScenario =
                     ActivityScenario.launch(partnerIntent)) {

            onView(withId(R.id.openInquiriesButton)).perform(scrollTo(), click());
            SystemClock.sleep(4000);

            onView(withId(R.id.inquiriesRecycler))
                    .perform(androidx.test.espresso.contrib.RecyclerViewActions.scrollTo(
                            hasDescendant(withText(containsString("בדיקה")))
                    ));

            onView(withText(containsString("בדיקה"))).check(matches(isDisplayed()));
        }
    }

    private static void signIn(String email, String pass) throws Exception {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signOut();
        Tasks.await(auth.signInWithEmailAndPassword(email, pass), 20, TimeUnit.SECONDS);
    }

    static void waitUntilViewDisplayed(int viewId, long timeoutMs) {
        long end = SystemClock.elapsedRealtime() + timeoutMs;

        while (SystemClock.elapsedRealtime() < end) {
            try {
                onView(withId(viewId)).check(matches(isDisplayed()));
                return;
            } catch (Throwable ignored) {
                SystemClock.sleep(200);
            }
        }

        onView(withId(viewId)).check(matches(isDisplayed()));
    }

    private static String findCompanyDocIdForCurrentPartner() throws Exception {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String uid = auth.getCurrentUser().getUid();

        var query = FirebaseFirestore.getInstance()
                .collection("insurance_companies")
                .whereEqualTo("partnerUid", uid)
                .limit(1)
                .get();

        var snap = Tasks.await(query, 15, TimeUnit.SECONDS);
        if (snap.isEmpty()) throw new AssertionError("No insurance_companies doc for partnerUid=" + uid);

        return snap.getDocuments().get(0).getId();
    }
}