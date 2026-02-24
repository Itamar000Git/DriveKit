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
import com.example.drive_kit.View.SetUsernamePasswordActivity;
import com.example.drive_kit.View.SignUpActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeoutException;

@RunWith(AndroidJUnit4.class)
public class RegisterTest {


    @Test
    public void Register_with_missingDetails_test() throws TimeoutException {
        try (ActivityScenario<SignUpActivity> scenario =
                     ActivityScenario.launch(SignUpActivity.class)) {

            // Fill only phone (email is missing)
            onView(withId(R.id.phoneEditText))
                    .perform(replaceText("0501234567"), closeSoftKeyboard());

            // Click Next and assert toast via AccessibilityEvent (stable across devices)
            assertToastShownOnClick(
                    () -> onView(withId(R.id.next)).perform(scrollTo(), click()),
                    "נא למלא את כל השדות הבסיסיים",
                    3000
            );
        }
    }

    @Test
    public void wrong_password_format_showsPasswordError() {
        Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();

        long now = System.currentTimeMillis();

        Intent i = new Intent(ctx, SetUsernamePasswordActivity.class);
        i.putExtra("role", "driver");
        i.putExtra("isInsurance", false);

        i.putExtra("firstName", "Itamar");
        i.putExtra("lastName", "Babai");
        i.putExtra("email", "itamar@test.com");
        i.putExtra("phone", "0501234567");

        i.putExtra("insuranceCompanyId", ""); // driver flow
        i.putExtra("insuranceLogoUri", "");   // driver flow

        i.putExtra("carNumber", "1234567");
        i.putExtra("carModel", CarModel.UNKNOWN);
        i.putExtra("year", 2020);
        i.putExtra("carSpecificModel", "Corolla");

        // required for driver flow in SetUsernamePasswordActivity
        i.putExtra("insuranceDateMillis", now + 1000L);
        i.putExtra("testDateMillis", now + 2000L);
        i.putExtra("treatmentDateMillis", now + 3000L);

        i.putExtra("carPhotoUri", ""); // optional in your code

        try (ActivityScenario<SetUsernamePasswordActivity> scenario =
                     ActivityScenario.launch(i)) {

            onView(withId(R.id.passwordEditText))
                    .perform(replaceText("123"), closeSoftKeyboard());

            onView(withId(R.id.confirmPasswordEditText))
                    .perform(replaceText("123"), closeSoftKeyboard());

            // IMPORTANT: no scrollTo() here (layout isn't inside ScrollView)
            onView(withId(R.id.registerButton)).perform(click());

            onView(withId(R.id.passwordEditText))
                    .check(matches(hasErrorText(not(isEmptyOrNullString()))));
        }
    }


    @Test
    public void wrong_Email_format_showsEmailError() {
        Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();

        long now = System.currentTimeMillis();

        Intent i = new Intent(ctx, SetUsernamePasswordActivity.class);
        i.putExtra("role", "driver");
        i.putExtra("isInsurance", false);

        i.putExtra("firstName", "Itamar");
        i.putExtra("lastName", "Babai");
        i.putExtra("email", "itamar@test");/////wrong format
        i.putExtra("phone", "0501234567");

        i.putExtra("insuranceCompanyId", ""); // driver flow
        i.putExtra("insuranceLogoUri", "");   // driver flow

        i.putExtra("carNumber", "1234567");
        i.putExtra("carModel", CarModel.UNKNOWN);
        i.putExtra("year", 2020);
        i.putExtra("carSpecificModel", "Corolla");

        // required for driver flow in SetUsernamePasswordActivity
        i.putExtra("insuranceDateMillis", now + 1000L);
        i.putExtra("testDateMillis", now + 2000L);
        i.putExtra("treatmentDateMillis", now + 3000L);

        i.putExtra("carPhotoUri", ""); // optional in your code

        try (ActivityScenario<SetUsernamePasswordActivity> scenario =
                     ActivityScenario.launch(i)) {

            onView(withId(R.id.passwordEditText))
                    .perform(replaceText("Aa12345!"), closeSoftKeyboard());

            onView(withId(R.id.confirmPasswordEditText))
                    .perform(replaceText("Aa12345!"), closeSoftKeyboard());

            // IMPORTANT: no scrollTo() here (layout isn't inside ScrollView)
            assertToastShownOnClick(
                    () -> onView(withId(R.id.registerButton)).perform(click()),
                    "badly formatted",
                    6000
            );

        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void Register_with_Registered_Email() {
        Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();

        long now = System.currentTimeMillis();

        Intent i = new Intent(ctx, SetUsernamePasswordActivity.class);
        i.putExtra("role", "driver");
        i.putExtra("isInsurance", false);

        i.putExtra("firstName", "Itamar");
        i.putExtra("lastName", "Babai");
        i.putExtra("email", "itamarbabai98@gmail.com");/////wrong format
        i.putExtra("phone", "0501234567");

        i.putExtra("insuranceCompanyId", ""); // driver flow
        i.putExtra("insuranceLogoUri", "");   // driver flow

        i.putExtra("carNumber", "1234567");
        i.putExtra("carModel", CarModel.UNKNOWN);
        i.putExtra("year", 2020);
        i.putExtra("carSpecificModel", "Corolla");

        // required for driver flow in SetUsernamePasswordActivity
        i.putExtra("insuranceDateMillis", now + 1000L);
        i.putExtra("testDateMillis", now + 2000L);
        i.putExtra("treatmentDateMillis", now + 3000L);

        i.putExtra("carPhotoUri", ""); // optional in your code

        try (ActivityScenario<SetUsernamePasswordActivity> scenario =
                     ActivityScenario.launch(i)) {

            onView(withId(R.id.passwordEditText))
                    .perform(replaceText("Aa12345!"), closeSoftKeyboard());

            onView(withId(R.id.confirmPasswordEditText))
                    .perform(replaceText("Aa12345!"), closeSoftKeyboard());

            // IMPORTANT: no scrollTo() here (layout isn't inside ScrollView)
            assertToastShownOnClick(
                    () -> onView(withId(R.id.registerButton)).perform(click()),
                    "שגיאה בהרשמה",
                    6000
            );

        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }
}