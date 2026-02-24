package com.example.drive_kit;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.intent.Intents.init;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.release;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasData;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.allOf;

import static java.util.regex.Pattern.matches;

import android.content.Intent;
import android.net.Uri;
import android.os.SystemClock;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.drive_kit.View.CarDetailsActivity;
import com.example.drive_kit.View.HomeActivity;
import com.example.drive_kit.View.PdfViewerActivity;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class CarDetailsTest {

    private static final String EMAIL = "test@gmail.com";
    private static final String PASS  = "Aa123456!";
    private static final String EXPECTED_URL =
            "https://www.yad2.co.il/vehicles/cars?manufacturer=21&model=10272&year=2014-2014";

    @Test
    public void openYad2_opensExpectedUrl() throws Exception {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signOut();
        Tasks.await(auth.signInWithEmailAndPassword(EMAIL, PASS), 20, TimeUnit.SECONDS);

        init();
        try (ActivityScenario<HomeActivity> scenario = ActivityScenario.launch(HomeActivity.class)) {

            onView(withId(R.id.myCarIcon)).perform(click());
            onView(withId(R.id.btnOpenYad2)).perform(click());

            intended(allOf(
                    hasAction(Intent.ACTION_VIEW),
                    hasData(Uri.parse(EXPECTED_URL))
            ));
        } finally {
            release();
        }
    }

    @Test
    public void downloadManual_click_showsToast_downloadStarted() throws Exception {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signOut();
        Tasks.await(auth.signInWithEmailAndPassword(EMAIL, PASS), 20, TimeUnit.SECONDS);

        try (ActivityScenario<CarDetailsActivity> scenario =
                     ActivityScenario.launch(CarDetailsActivity.class)) {
            SystemClock.sleep(5000);
            TestToastUtils.assertToastShownOnClick(
                    () -> onView(withId(R.id.btnDownloadManual)).perform(click()),
                    "ההורדה החלה",
                    6000
            );
        }
    }



    @Test
    public void carBook_search_manual_answerContainsManual() throws Exception {
        // 1) sign in
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signOut();
        Tasks.await(auth.signInWithEmailAndPassword(EMAIL, PASS), 20, TimeUnit.SECONDS);

        // 2) open Home
        try (ActivityScenario<HomeActivity> scenario = ActivityScenario.launch(HomeActivity.class)) {

            onView(withId(R.id.myCarIcon)).perform(click());
            SystemClock.sleep(3000);
            onView(withId(R.id.btnDownloadCarBook)).perform(click());

            // 5) wait for loading
            SystemClock.sleep(15000);

            // 6) type query
            onView(withId(R.id.etQuestion))
                    .perform(replaceText("manual"), closeSoftKeyboard());
//            SystemClock.sleep(10000);
            // 7) click ask/search
            onView(withId(R.id.btnAsk)).perform(click());

            // 8) wait a bit for answer (async)
            SystemClock.sleep(2000);

            // 9) assert answer contains "manual"
            onView(withId(R.id.tvAnswer))
                    .check(matches(withText(containsString("manual"))));
        }
    }
}