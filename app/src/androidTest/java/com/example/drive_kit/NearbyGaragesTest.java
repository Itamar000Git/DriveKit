package com.example.drive_kit;

import static org.junit.Assert.assertTrue;

import android.Manifest;
import android.app.Activity;
import android.app.Instrumentation;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import com.example.drive_kit.View.HomeActivity;
import com.example.drive_kit.View.activity_nearby_garages;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.test.core.app.ActivityScenario;
import androidx.test.platform.app.InstrumentationRegistry;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

@RunWith(AndroidJUnit4.class)
public class NearbyGaragesTest {

    private static final String EMAIL = "test@gmail.com";
    private static final String PASS  = "Aa123456!";
    private static final int DEFAULT_RADIUS_KM = 5;

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
    public void nearbyGarages_defaultRadius5_allGaragesWithin5km() throws Exception {
        // 1) sign in
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signOut();
        Tasks.await(auth.signInWithEmailAndPassword(EMAIL, PASS), 20, TimeUnit.SECONDS);

        // 2) set Activity monitor to capture NearbyGaragesActivity launched from HomeActivity
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        Instrumentation.ActivityMonitor monitor =
                instrumentation.addMonitor(activity_nearby_garages.class.getName(), null, false);

        // 3) launch HomeActivity
        try (ActivityScenario<HomeActivity> scenario = ActivityScenario.launch(HomeActivity.class)) {

            // 4) click "מוסך קרוב"
            onView(withId(R.id.circleGarage)).perform(click());

            // 5) wait for NearbyGaragesActivity instance (up to 15s)
            Activity garagesActivity = instrumentation.waitForMonitorWithTimeout(monitor, 15000);
            assertTrue("NearbyGaragesActivity did not open", garagesActivity != null);

            // 6) wait 10 seconds for loading (as you requested)
            SystemClock.sleep(10000);

            // 7) check UI radius text contains "5"
            // (works if tvRadiusValue shows something like "5 ק״מ" / "5 km")
            onView(withId(R.id.tvRadiusValue))
                    .check(matches(withText(containsString(String.valueOf(DEFAULT_RADIUS_KM)))));

            // 8) logic check: all visibleGarages distances <= 5km
            @SuppressWarnings("unchecked")
            List<Object> visible = (List<Object>) getPrivateField(garagesActivity, "visibleGarages");
            assertTrue("visibleGarages is null", visible != null);

            for (Object item : visible) {
                double km = extractDistanceKm(item);
                assertTrue("Garage distance > " + DEFAULT_RADIUS_KM + " km. Got: " + km,
                        km <= (DEFAULT_RADIUS_KM + 1e-9));
            }

            garagesActivity.finish();
        } finally {
            instrumentation.removeMonitor(monitor);
        }
    }

    // ---------- reflection helpers ----------

    private static Object getPrivateField(Object target, String fieldName) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return f.get(target);
        } catch (Exception e) {
            throw new AssertionError("Failed to read field '" + fieldName + "' from "
                    + target.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Tries to extract distance in KM from GarageItem via common fields/methods.
     * Supports:
     * - field: distanceKm (double/float/int)
     * - field: distanceMeters (double/float/int/long) -> converted to km
     * - method: getDistanceKm()
     * - method: getDistanceMeters()
     */
    private static double extractDistanceKm(Object garageItem) {
        // 1) method getDistanceKm()
        Double km = invokeNumberMethodIfExists(garageItem, "getDistanceKm");
        if (km != null) return km;

        // 2) field distanceKm
        km = readNumberFieldIfExists(garageItem, "distanceKm");
        if (km != null) return km;

        // 3) method getDistanceMeters()
        Double meters = invokeNumberMethodIfExists(garageItem, "getDistanceMeters");
        if (meters != null) return meters / 1000.0;

        // 4) field distanceMeters
        meters = readNumberFieldIfExists(garageItem, "distanceMeters");
        if (meters != null) return meters / 1000.0;

        // If none exists, fail with actionable message
        throw new AssertionError(
                "Cannot extract distance from " + garageItem.getClass().getName() +
                        ". Add distanceKm/distanceMeters field or getter, or tell me how to get distance from GarageItem."
        );
    }

    private static Double invokeNumberMethodIfExists(Object obj, String methodName) {
        try {
            Method m = obj.getClass().getMethod(methodName);
            Object val = m.invoke(obj);
            if (val instanceof Number) return ((Number) val).doubleValue();
            return null;
        } catch (NoSuchMethodException e) {
            return null;
        } catch (Exception e) {
            Log.w("TEST", "Failed invoking " + methodName + ": " + e);
            return null;
        }
    }

    private static Double readNumberFieldIfExists(Object obj, String fieldName) {
        try {
            Field f = obj.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            Object val = f.get(obj);
            if (val instanceof Number) return ((Number) val).doubleValue();
            return null;
        } catch (NoSuchFieldException e) {
            return null;
        } catch (Exception e) {
            Log.w("TEST", "Failed reading field " + fieldName + ": " + e);
            return null;
        }
    }


    @Test
    public void nearbyGarages_radius20_hasAtLeastOneGarageOver5km() throws Exception {
        // 1) sign in
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signOut();
        Tasks.await(auth.signInWithEmailAndPassword(EMAIL, PASS), 20, TimeUnit.SECONDS);

        // 2) capture NearbyGaragesActivity
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        Instrumentation.ActivityMonitor monitor =
                instrumentation.addMonitor(activity_nearby_garages.class.getName(), null, false);

        try (ActivityScenario<HomeActivity> scenario = ActivityScenario.launch(HomeActivity.class)) {

            // 3) open nearby garages
            onView(withId(R.id.circleGarage)).perform(click());

            Activity garagesActivity = instrumentation.waitForMonitorWithTimeout(monitor, 15000);
            assertTrue("NearbyGaragesActivity did not open", garagesActivity != null);

            // 4) wait for initial load
            SystemClock.sleep(10000);

            // 5) move slider to 20km
            setSliderValueOnUiThread(garagesActivity, 20);

            // allow VM/UI to recompute list
            SystemClock.sleep(2000);

            // 6) read visibleGarages and assert at least one > 5km
            @SuppressWarnings("unchecked")
            List<Object> visible = (List<Object>) getPrivateField(garagesActivity, "visibleGarages");
            assertTrue("visibleGarages is null", visible != null);
            assertTrue("visibleGarages is empty", visible.size() > 0);

            boolean foundOver5 = false;
            double maxKm = -1;

            for (Object item : visible) {
                double km = extractDistanceKm(item);
                if (km > maxKm) maxKm = km;
                if (km > 5.0) {
                    foundOver5 = true;
                    break;
                }
            }

            assertTrue("Expected at least one garage > 5km when radius=20. maxKm=" + maxKm,
                    foundOver5);

            garagesActivity.finish();
        } finally {
            instrumentation.removeMonitor(monitor);
        }
    }

    /**
     * Sets sliderRadiusKm value on UI thread (Slider is a View).
     * Uses reflection to access the private field: sliderRadiusKm.
     */
    private static void setSliderValueOnUiThread(Activity garagesActivity, int km) {
        garagesActivity.runOnUiThread(() -> {
            try {
                Object slider = getPrivateField(garagesActivity, "sliderRadiusKm");
                // Slider class: com.google.android.material.slider.Slider
                slider.getClass().getMethod("setValue", float.class).invoke(slider, (float) km);
            } catch (Exception e) {
                throw new AssertionError("Failed to set sliderRadiusKm to " + km + ": " + e.getMessage(), e);
            }
        });
    }
}