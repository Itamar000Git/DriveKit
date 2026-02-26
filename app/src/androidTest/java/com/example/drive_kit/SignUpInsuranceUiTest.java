package com.example.drive_kit;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.action.ViewActions.*;

import static org.hamcrest.core.IsNot.not;

import androidx.test.core.app.ActivityScenario;

import com.example.drive_kit.View.SignUpActivity;

import org.junit.Test;

public class SignUpInsuranceUiTest {

    @Test
    public void signUp_chooseInsurance_showsInsuranceFields_hidesDriverFields() {
        try (ActivityScenario<SignUpActivity> scenario =
                     ActivityScenario.launch(SignUpActivity.class)) {

            // 1) choose role = Insurance Company
            onView(withId(R.id.radioInsurance)).perform(scrollTo(), click());

            // 2) insurance fields should be visible
            onView(withId(R.id.insuranceCompanyLayout)).check(matches(isDisplayed()));
            onView(withId(R.id.insuranceCompanyDropdown)).check(matches(isDisplayed()));

            onView(withId(R.id.insuranceCompanyIdLayout)).check(matches(isDisplayed()));
            onView(withId(R.id.insuranceCompanyIdEditText)).check(matches(isDisplayed()));

            onView(withId(R.id.insuranceLogoLayout)).check(matches(isDisplayed()));
            onView(withId(R.id.insuranceLogoEditText)).check(matches(isDisplayed()));

            // 3) driver-only fields should NOT be visible
            onView(withId(R.id.carNumberLayout)).check(matches(not(isDisplayed())));
            onView(withId(R.id.manufacturerLayout)).check(matches(not(isDisplayed())));
            onView(withId(R.id.modelLayout)).check(matches(not(isDisplayed())));
            onView(withId(R.id.yearLayout)).check(matches(not(isDisplayed())));
            onView(withId(R.id.carPhotoLayout)).check(matches(not(isDisplayed())));
            onView(withId(R.id.insuranceDateLayout)).check(matches(not(isDisplayed())));
            onView(withId(R.id.testDateLayout)).check(matches(not(isDisplayed())));
            onView(withId(R.id.service10kDateLayout)).check(matches(not(isDisplayed())));
            onView(withId(R.id.tvService10kDontRemember)).check(matches(not(isDisplayed())));
        }
    }
}