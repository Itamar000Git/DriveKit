package com.example.drive_kit;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static com.example.drive_kit.InsuranceCallTest.waitUntilViewDisplayed;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.drive_kit.View.Insurance_user.InsuranceHomeActivity;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class PartnerHomeCompanyIdTest {

    private static final String PARTNER_EMAIL = "ccc@gmail.com";
    private static final String PARTNER_PASS  = "Gg2002##";
    private static final String EXPECTED_COMPANY_ID_VALUE = "520042169"; // מה שצריך להופיע במסך

    @Test
    public void partnerLogin_openInsuranceHomeWithExtra_showsCorrectCompanyId() throws Exception {

        // 1) Partner sign-in
        signIn(PARTNER_EMAIL, PARTNER_PASS);

        // 2) Find the insurance_companies doc id for this partner (helper already exists אצלך)
        String companyDocId = findCompanyDocIdForCurrentPartner();

        // 3) Launch InsuranceHomeActivity with putExtra("insuranceCompanyId", companyDocId)
        Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Intent intent = new Intent(ctx, InsuranceHomeActivity.class);
        intent.putExtra("insuranceCompanyId", companyDocId);

        try (ActivityScenario<InsuranceHomeActivity> scenario = ActivityScenario.launch(intent)) {

            // 4) wait until screen loaded
            waitUntilViewDisplayed(R.id.companyIdValue, 8000);

            // 5) assert the shown company identifier is correct
            onView(withId(R.id.companyIdValue))
                    .check(matches(withText(EXPECTED_COMPANY_ID_VALUE)));
        }
    }

    private static void signIn(String email, String pass) throws Exception {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signOut();
        Tasks.await(auth.signInWithEmailAndPassword(email, pass), 20, TimeUnit.SECONDS);
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