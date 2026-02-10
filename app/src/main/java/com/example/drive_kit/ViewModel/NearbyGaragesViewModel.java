package com.example.drive_kit.ViewModel;

import android.app.Application;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.drive_kit.Data.Repository.NearbyGaragesRepository;
import com.example.drive_kit.View.activity_nearby_garages;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * ViewModel for Nearby Garages screen.
 *
 * Responsibilities:
 * - Holds screen state (radius, lists, user location, loading/empty/error)
 * - Starts data loading when BOTH conditions are true:
 *   1) Map is ready
 *   2) Location permission is granted
 * - Filters and sorts the list based on radius (closest first)
 * - Fetches phone number on demand (when user clicks "Call")
 *
 * Note:
 * - This ViewModel does not request permissions and does not start activities.
 *   Those are UI responsibilities kept in the Activity.
 */
public class NearbyGaragesViewModel extends AndroidViewModel {

    /**
     * Simple callback for returning a phone number to the Activity.
     */
    public interface PhoneCallback {
        void onResult(String phone);
    }

    private final NearbyGaragesRepository repo;

    // UI state
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> emptyText = new MutableLiveData<>(null);
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>(null);

    // Screen settings/state
    private final MutableLiveData<Integer> radiusKm = new MutableLiveData<>(5);
    private final MutableLiveData<LatLng> userLatLng = new MutableLiveData<>(null);

    // Data
    private final MutableLiveData<List<activity_nearby_garages.GarageItem>> allGarages =
            new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<List<activity_nearby_garages.GarageItem>> visibleGarages =
            new MutableLiveData<>(new ArrayList<>());

    // Start conditions
    private boolean mapReady = false;
    private boolean permissionGranted = false;
    private boolean started = false;

    public NearbyGaragesViewModel(@NonNull Application application) {
        super(application);
        repo = new NearbyGaragesRepository(application);
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getEmptyText() {
        return emptyText;
    }

    public LiveData<String> getToastMessage() {
        return toastMessage;
    }

    public LiveData<Integer> getRadiusKm() {
        return radiusKm;
    }

    public LiveData<LatLng> getUserLatLng() {
        return userLatLng;
    }

    public LiveData<List<activity_nearby_garages.GarageItem>> getVisibleGarages() {
        return visibleGarages;
    }

    /**
     * Called by Activity when GoogleMap is ready.
     * We do not start loading until permission is also granted.
     */
    public void setMapReady(boolean ready) {
        mapReady = ready;
        tryStart();
    }

    /**
     * Called by Activity after the user granted location permission.
     * We do not start loading until map is also ready.
     */
    public void onLocationPermissionGranted() {
        permissionGranted = true;
        tryStart();
    }

    /**
     * Called when the user changes the slider.
     * We recompute visible list immediately.
     */
    public void setRadiusKm(int km) {
        radiusKm.setValue(km);
        recomputeVisible();
    }

    /**
     * Start loading data only once, and only after map+permission are ready.
     * This avoids duplicate calls when Activity recreates.
     */
    private void tryStart() {
        if (started) return;
        if (!mapReady) return;
        if (!permissionGranted) return;

        started = true;
        loadUserLocationAndGarages();
    }

    /**
     * Load last known location, then search garages around it.
     * Same behavior as your working version:
     * - Fetch up to 20km and then filter by slider radius.
     */
    private void loadUserLocationAndGarages() {
        loading.setValue(true);
        emptyText.setValue(null);

        repo.getLastLocation(new NearbyGaragesRepository.LocationCallback() {
            @Override
            public void onSuccess(Location location) {
                if (location == null) {
                    loading.setValue(false);
                    toastMessage.setValue("לא הצלחתי לקבל מיקום. נסה שוב כש-GPS פעיל.");
                    emptyText.setValue("לא הצלחתי לקבל מיקום.");
                    return;
                }

                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                userLatLng.setValue(ll);

                // Same query + radius as before
                fetchGaragesByText(ll, "מוסך", 20000);
            }

            @Override
            public void onFailure(String message) {
                loading.setValue(false);
                toastMessage.setValue("שגיאה בקבלת מיקום: " + message);
                emptyText.setValue("שגיאה בקבלת מיקום.");
            }
        });
    }

    /**
     * Search garages using Places "searchByText".
     * We request 20 results, ranked by distance (same behavior).
     */
    private void fetchGaragesByText(@NonNull LatLng user, @NonNull String query, int fetchRadiusMeters) {
        loading.setValue(true);
        emptyText.setValue(null);

        repo.searchGaragesByText(user, query, fetchRadiusMeters, 20, new NearbyGaragesRepository.GaragesCallback() {
            @Override
            public void onSuccess(List<activity_nearby_garages.GarageItem> garages) {
                allGarages.setValue(garages != null ? garages : new ArrayList<>());

                recomputeVisible();
                loading.setValue(false);

                List<activity_nearby_garages.GarageItem> v = visibleGarages.getValue();
                if (v == null || v.isEmpty()) {
                    emptyText.setValue("לא נמצאו מוסכים בטווח שנבחר.");
                } else {
                    emptyText.setValue(null);
                }
            }

            @Override
            public void onFailure(String message) {
                loading.setValue(false);
                emptyText.setValue("שגיאה בחיפוש מוסכים. בדוק API Key/Billing/הרשאות.");
                toastMessage.setValue("Places error: " + message);
            }
        });
    }

    /**
     * Filter the full list by current radius, then sort by distance (closest first).
     * This is the main "business logic" of the screen.
     */
    private void recomputeVisible() {
        List<activity_nearby_garages.GarageItem> all = allGarages.getValue();
        Integer km = radiusKm.getValue();

        if (all == null) all = new ArrayList<>();
        if (km == null) km = 5;

        double maxMeters = km * 1000.0;

        List<activity_nearby_garages.GarageItem> filtered = new ArrayList<>();
        for (activity_nearby_garages.GarageItem g : all) {
            if (g != null && g.distanceMeters <= maxMeters) filtered.add(g);
        }

        Collections.sort(filtered, Comparator.comparingDouble(o -> o.distanceMeters));
        visibleGarages.setValue(filtered);
    }

    /**
     * Fetch phone number for a place (only when user clicks "Call").
     * Returns the phone via callback to the Activity.
     */
    public void fetchPhone(@NonNull String placeId, @NonNull PhoneCallback cb) {
        repo.fetchPhone(placeId, new NearbyGaragesRepository.PhoneCallback() {
            @Override
            public void onSuccess(String phone) {
                // Update cached phone in our stored list
                updateCachedPhone(placeId, phone);
                cb.onResult(phone);
            }

            @Override
            public void onFailure(String message) {
                cb.onResult(null);
            }
        });
    }

    /**
     * Cache the fetched phone in the existing objects so next click is immediate.
     * This keeps behavior identical to your working Activity version.
     */
    private void updateCachedPhone(@NonNull String placeId, String phone) {
        List<activity_nearby_garages.GarageItem> all = allGarages.getValue();
        if (all == null || all.isEmpty()) return;

        boolean changed = false;
        for (activity_nearby_garages.GarageItem g : all) {
            if (g != null && placeId.equals(g.placeId)) {
                g.phone = (phone == null) ? "" : phone.trim();
                changed = true;
            }
        }

        if (changed) {
            // Trigger recompute so UI can refresh if needed
            allGarages.setValue(all);
            recomputeVisible();
        }
    }
}
