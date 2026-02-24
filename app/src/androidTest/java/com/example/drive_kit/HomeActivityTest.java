package com.example.drive_kit;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

import android.Manifest;
import android.os.Build;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import com.example.drive_kit.View.HomeActivity;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static org.hamcrest.Matchers.not;



@RunWith(AndroidJUnit4.class)
public class HomeActivityTest {

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
    public void home_showsHelloUserText_withFullName() throws Exception {
        // 1) Sign in
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signOut();
        Tasks.await(auth.signInWithEmailAndPassword(EMAIL, PASS), 20, TimeUnit.SECONDS);

        String uid = auth.getCurrentUser().getUid();

        // 2) Read user's full name from Firestore (drivers/{uid})
        var doc = Tasks.await(
                FirebaseFirestore.getInstance()
                        .collection("drivers")
                        .document(uid)
                        .get(),
                20, TimeUnit.SECONDS
        );

        String firstName = safe(doc.getString("firstName"));
        String lastName  = safe(doc.getString("lastName"));
        String fullName  = (firstName + " " + lastName).trim();

        // 3) Launch HomeActivity
        try (ActivityScenario<HomeActivity> scenario = ActivityScenario.launch(HomeActivity.class)) {

            // 4) Assert helloUserText contains full name
            onView(withId(R.id.helloUserText))
                    .check(matches(withText(containsString(fullName))));
        }
    }
    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }
}