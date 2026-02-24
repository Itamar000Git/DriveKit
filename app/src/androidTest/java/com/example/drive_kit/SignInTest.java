package com.example.drive_kit;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.example.drive_kit.TestToastUtils.assertToastShownOnClick;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.drive_kit.Model.CarModel;
import com.example.drive_kit.View.MainActivity;
import com.example.drive_kit.View.SetUsernamePasswordActivity;
import com.example.drive_kit.View.SignUpActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeoutException;

@RunWith(AndroidJUnit4.class)
public class SignInTest {



    @Test
    public void SignIn_with_wrong_Password_test() throws TimeoutException {
        try (ActivityScenario<MainActivity> scenario =
                     ActivityScenario.launch(MainActivity.class)) {

            // Fill only phone (email is missing)
            onView(withId(R.id.emailEditText))
                    .perform(replaceText("aaa@gmail.com"), closeSoftKeyboard());

            // Fill only phone (email is missing)
            onView(withId(R.id.passwordEditText))
                    .perform(replaceText("11111111"), closeSoftKeyboard());
            // Click Next and assert toast via AccessibilityEvent (stable across devices)
            assertToastShownOnClick(
                    () -> onView(withId(R.id.loginButton)).perform(click()),
                    "פרטי ההתחברות שגויים",
                    3000
            );
        }
    }

    @Test
    public void SignIn_with_wrong_Email_test() throws TimeoutException {
        try (ActivityScenario<MainActivity> scenario =
                     ActivityScenario.launch(MainActivity.class)) {

            // Fill only phone (email is missing)
            onView(withId(R.id.emailEditText))
                    .perform(replaceText("aabb@gmail.com"), closeSoftKeyboard());

            // Fill only phone (email is missing)
            onView(withId(R.id.passwordEditText))
                    .perform(replaceText("11111111"), closeSoftKeyboard());
            // Click Next and assert toast via AccessibilityEvent (stable across devices)
            assertToastShownOnClick(
                    () -> onView(withId(R.id.loginButton)).perform(click()),
                    "פרטי ההתחברות שגויים",
                    3000
            );
        }
    }
}
