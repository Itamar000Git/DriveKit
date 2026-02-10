package com.example.drive_kit.Data.Repository;

import android.content.Context;
import android.location.Location;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.example.drive_kit.R;
import com.example.drive_kit.View.activity_nearby_garages;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.net.SearchByTextRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Repository for Nearby Garages feature.
 *
 * This class handles all external services:
 * - FusedLocationProviderClient (last known location)
 * - Google Places (search by text, fetch phone)
 *
 * The ViewModel uses this repository and exposes clean UI state to the Activity.
 */
public class NearbyGaragesRepository {

    public interface LocationCallback {
        void onSuccess(Location location);
        void onFailure(String message);
    }

    public interface GaragesCallback {
        void onSuccess(List<activity_nearby_garages.GarageItem> garages);
        void onFailure(String message);
    }

    public interface PhoneCallback {
        void onSuccess(String phone);
        void onFailure(String message);
    }

    private final Context appContext;
    private final FusedLocationProviderClient fusedClient;
    private final PlacesClient placesClient;

    /**
     * Creates repository with application context.
     * We initialize Places client only once for the app context.
     */
    public NearbyGaragesRepository(@NonNull Context context) {
        this.appContext = context.getApplicationContext();
        this.fusedClient = LocationServices.getFusedLocationProviderClient(appContext);

        if (!Places.isInitialized()) {
            Places.initialize(appContext, appContext.getString(R.string.google_maps_key));
        }
        this.placesClient = Places.createClient(appContext);
    }

    /**
     * Returns the last known location (may be null).
     * Permission checking is done in the Activity (UI responsibility).
     */
    public void getLastLocation(@NonNull LocationCallback cb) {
        fusedClient.getLastLocation()
                .addOnSuccessListener(cb::onSuccess)
                .addOnFailureListener(e -> cb.onFailure(e.getMessage() == null ? "unknown" : e.getMessage()));
    }

    /**
     * Searches garages using Places searchByText.
     *
     * Behavior is kept identical to your working implementation:
     * - setMaxResultCount(maxResults)
     * - rank by distance
     * - restrict results by a rectangle bounds around the user
     * - phone is NOT fetched here (phone starts empty)
     */
    public void searchGaragesByText(@NonNull LatLng userLatLng,
                                    @NonNull String query,
                                    int fetchRadiusMeters,
                                    int maxResults,
                                    @NonNull GaragesCallback cb) {

        RectangularBounds bounds = makeBounds(userLatLng, fetchRadiusMeters);

        List<Place.Field> fields = Arrays.asList(
                Place.Field.ID,
                Place.Field.DISPLAY_NAME,
                Place.Field.FORMATTED_ADDRESS,
                Place.Field.LOCATION,
                Place.Field.WEBSITE_URI
        );

        SearchByTextRequest req = SearchByTextRequest.builder(query, fields)
                .setMaxResultCount(maxResults)
                .setLocationRestriction(bounds)
                .setRankPreference(SearchByTextRequest.RankPreference.DISTANCE)
                .build();

        placesClient.searchByText(req)
                .addOnSuccessListener(response -> {
                    List<activity_nearby_garages.GarageItem> out = new ArrayList<>();

                    for (Place p : response.getPlaces()) {
                        LatLng loc = p.getLocation();
                        if (loc == null) continue;

                        String placeId = (p.getId() == null) ? "" : p.getId();

                        String name = p.getDisplayName();
                        if (name == null || name.trim().isEmpty()) name = "מוסך";

                        String addr = p.getFormattedAddress();
                        if (addr == null || addr.trim().isEmpty()) addr = "כתובת לא זמינה";

                        Uri webUri = p.getWebsiteUri();
                        String website = (webUri == null) ? "" : webUri.toString();

                        double dist = distanceMeters(userLatLng, loc);

                        // Phone starts empty and will be fetched only on "Call"
                        out.add(new activity_nearby_garages.GarageItem(placeId, name, addr, loc, dist, "", website));
                    }

                    cb.onSuccess(out);
                })
                .addOnFailureListener(e -> cb.onFailure(formatPlacesError(e)));
    }

    /**
     * Fetches the international phone number for a given placeId using fetchPlace.
     * This is called only when the user clicks "Call".
     */
    public void fetchPhone(@NonNull String placeId, @NonNull PhoneCallback cb) {
        List<Place.Field> fields = Arrays.asList(
                Place.Field.ID,
                Place.Field.DISPLAY_NAME,
                Place.Field.INTERNATIONAL_PHONE_NUMBER
        );

        FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, fields);

        placesClient.fetchPlace(request)
                .addOnSuccessListener(response -> {
                    Place place = response.getPlace();
                    String phone = place.getInternationalPhoneNumber();
                    cb.onSuccess(phone == null ? null : phone.trim());
                })
                .addOnFailureListener(e -> cb.onFailure(e.getMessage() == null ? "unknown" : e.getMessage()));
    }

    // -------- Helpers --------

    /**
     * Creates a rectangular bounds around the center point.
     * Used for Places location restriction.
     */
    private static RectangularBounds makeBounds(LatLng center, int radiusMeters) {
        double lat = center.latitude;
        double lon = center.longitude;

        double latDelta = radiusMeters / 111320.0;
        double lonDelta = radiusMeters / (111320.0 * Math.cos(Math.toRadians(lat)));

        LatLng sw = new LatLng(lat - latDelta, lon - lonDelta);
        LatLng ne = new LatLng(lat + latDelta, lon + lonDelta);

        return RectangularBounds.newInstance(sw, ne);
    }

    /**
     * Calculates distance between two LatLng points in meters.
     * This matches the behavior of Location.distanceBetween in your original code.
     */
    private static double distanceMeters(LatLng a, LatLng b) {
        float[] res = new float[1];
        Location.distanceBetween(a.latitude, a.longitude, b.latitude, b.longitude, res);
        return res[0];
    }

    /**
     * Builds a more helpful error message for Places failures,
     * including status code when available.
     */
    private static String formatPlacesError(Exception e) {
        String msg = (e.getMessage() == null) ? "unknown" : e.getMessage();
        if (e instanceof ApiException) {
            ApiException ae = (ApiException) e;
            msg = "statusCode=" + ae.getStatusCode() + " , " + ae.getStatusMessage();
        }
        return msg;
    }
}
