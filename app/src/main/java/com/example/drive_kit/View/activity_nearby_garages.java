package com.example.drive_kit.View;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.drive_kit.R;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.net.SearchByTextRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class activity_nearby_garages extends BaseLoggedInActivity implements OnMapReadyCallback {

    // UI
    private TextView tvRadiusValue, tvEmptyState;
    private ProgressBar progressLoading;
    private Slider sliderRadiusKm;
    private RecyclerView rvGarages;

    // Map + location
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedClient;
    private LatLng userLatLng;

    // Places
    private PlacesClient placesClient;

    // Data
    private int radiusKm = 5;
    private final List<GarageItem> allGarages = new ArrayList<>();
    private final List<GarageItem> visibleGarages = new ArrayList<>();

    // Map -> data
    private final Map<Marker, GarageItem> markerToGarage = new HashMap<>();

    private GaragesAdapter adapter;
    private ActivityResultLauncher<String[]> locationPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_nearby_garages);
        getContentLayoutId();


        tvRadiusValue = findViewById(R.id.tvRadiusValue);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        progressLoading = findViewById(R.id.progressLoading);
        sliderRadiusKm = findViewById(R.id.sliderRadiusKm);
        rvGarages = findViewById(R.id.rvGarages);

        adapter = new GaragesAdapter(visibleGarages, new GaragesAdapter.OnGarageAction() {
            @Override public void onNavigate(GarageItem item) { openNav(item); }

            @Override public void onItemClick(GarageItem item) {
                if (googleMap != null) {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(item.latLng, 16f));
                }
            }

            @Override public void onCall(GarageItem item) {
                if (item.phone == null || item.phone.trim().isEmpty()) return;
                Intent i = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + item.phone));
                startActivity(i);
            }

            @Override public void onWebsite(GarageItem item) {
                if (item.website == null || item.website.trim().isEmpty()) return;
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(item.website));
                startActivity(i);
            }
        });

        rvGarages.setLayoutManager(new LinearLayoutManager(this));
        rvGarages.setAdapter(adapter);

        fusedClient = LocationServices.getFusedLocationProviderClient(this);

        if (!Places.isInitialized()) {
            Places.initialize(this, getString(R.string.google_maps_key));
        }
        placesClient = Places.createClient(this);

        locationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    boolean fine = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION));
                    boolean coarse = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_COARSE_LOCATION));
                    if (fine || coarse) fetchUserLocation();
                    else Toast.makeText(this, "צריך הרשאת מיקום כדי להציג מוסכים קרובים", Toast.LENGTH_LONG).show();
                }
        );

        radiusKm = (int) sliderRadiusKm.getValue();
        updateRadiusText(radiusKm);

        sliderRadiusKm.addOnChangeListener((slider, value, fromUser) -> {
            radiusKm = (int) value;
            updateRadiusText(radiusKm);
            applyRadiusFilterAndRefreshUI();
        });

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapContainer);
        if (mapFragment != null) mapFragment.getMapAsync(this);
        else Toast.makeText(this, "שגיאה: mapContainer לא נמצא", Toast.LENGTH_LONG).show();
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_nearby_garages;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        googleMap.setOnMarkerClickListener(marker -> {
            GarageItem item = markerToGarage.get(marker);
            if (item != null) {
                // קליק על נעץ -> זום למיקום + Toast קטן
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(item.latLng, 16f));
                Toast.makeText(this, item.name, Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });

        if (hasLocationPermission()) fetchUserLocation();
        else requestLocationPermission();
    }

    private void updateRadiusText(int km) {
        tvRadiusValue.setText("טווח: " + km + " ק״מ");
    }

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        locationPermissionLauncher.launch(new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    private void fetchUserLocation() {
        if (!hasLocationPermission()) return;

        progressLoading.setVisibility(View.VISIBLE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location == null) {
                        progressLoading.setVisibility(View.GONE);
                        Toast.makeText(this, "לא הצלחתי לקבל מיקום. נסה שוב כש-GPS פעיל.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    onUserLocationReady(location);
                })
                .addOnFailureListener(e -> {
                    progressLoading.setVisibility(View.GONE);
                    Toast.makeText(this, "שגיאה בקבלת מיקום: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void onUserLocationReady(@NonNull Location location) {
        userLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        if (googleMap != null) {
            try {
                if (hasLocationPermission()) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    googleMap.setMyLocationEnabled(true);
                }
            } catch (Exception ignored) {}

            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 13.5f));
        }

        // טוענים עד 20 ק"מ ואז מסננים עם הסליידר
        fetchGaragesByText("מוסך", 20000);
    }

    private void fetchGaragesByText(String query, int fetchRadiusMeters) {
        if (userLatLng == null) return;

        progressLoading.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);

        RectangularBounds bounds = makeBounds(userLatLng, fetchRadiusMeters);

        List<Place.Field> fields = Arrays.asList(
                Place.Field.ID,
                Place.Field.DISPLAY_NAME,
                Place.Field.FORMATTED_ADDRESS,
                Place.Field.LOCATION,
               // Place.Field.PHONE_NUMBER,   // יופיע אם זמין
                Place.Field.WEBSITE_URI     // Uri אם זמין
        );

        SearchByTextRequest req = SearchByTextRequest.builder(query, fields)
                .setMaxResultCount(20)
                .setLocationRestriction(bounds)
                .setRankPreference(SearchByTextRequest.RankPreference.DISTANCE)
                .build();

        placesClient.searchByText(req)
                .addOnSuccessListener(response -> {
                    allGarages.clear();

                    for (Place p : response.getPlaces()) {
                        LatLng loc = p.getLocation();
                        if (loc == null) continue;

                        String placeId = (p.getId() == null) ? "" : p.getId();

                        String name = p.getDisplayName();
                        if (name == null || name.trim().isEmpty()) name = "מוסך";

                        String addr = p.getFormattedAddress();
                        if (addr == null || addr.trim().isEmpty()) addr = "כתובת לא זמינה";

                        String phone = "";
                        if (phone == null) phone = "";

                        Uri webUri = p.getWebsiteUri();
                        String website = (webUri == null) ? "" : webUri.toString();

                        double dist = distanceMeters(userLatLng, loc);

                        allGarages.add(new GarageItem(placeId, name, addr, loc, dist, phone, website));
                    }

                    applyRadiusFilterAndRefreshUI();
                    progressLoading.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    progressLoading.setVisibility(View.GONE);
                    tvEmptyState.setVisibility(View.VISIBLE);
                    tvEmptyState.setText("שגיאה בחיפוש מוסכים. בדוק API Key/Billing/הרשאות.");

                    String msg = e.getMessage();
                    if (e instanceof ApiException) {
                        ApiException ae = (ApiException) e;
                        msg = "statusCode=" + ae.getStatusCode() + " , " + ae.getStatusMessage();
                    }
                    Toast.makeText(this, "Places error: " + msg, Toast.LENGTH_LONG).show();
                });
    }

    private void applyRadiusFilterAndRefreshUI() {
        if (userLatLng == null) return;

        visibleGarages.clear();
        double maxMeters = radiusKm * 1000.0;

        for (GarageItem g : allGarages) {
            if (g.distanceMeters <= maxMeters) visibleGarages.add(g);
        }

        Collections.sort(visibleGarages, Comparator.comparingDouble(o -> o.distanceMeters));
        adapter.notifyDataSetChanged();

        if (googleMap != null) {
            googleMap.clear();
            markerToGarage.clear();

            for (GarageItem g : visibleGarages) {
                Marker m = googleMap.addMarker(new MarkerOptions()
                        .position(g.latLng)
                        .title(g.name)
                        .snippet(g.address));
                if (m != null) markerToGarage.put(m, g);
            }
        }

        tvEmptyState.setVisibility(visibleGarages.isEmpty() ? View.VISIBLE : View.GONE);
        if (visibleGarages.isEmpty()) tvEmptyState.setText("לא נמצאו מוסכים בטווח שנבחר.");
    }

    private static RectangularBounds makeBounds(LatLng center, int radiusMeters) {
        double lat = center.latitude;
        double lon = center.longitude;

        double latDelta = radiusMeters / 111320.0;
        double lonDelta = radiusMeters / (111320.0 * Math.cos(Math.toRadians(lat)));

        LatLng sw = new LatLng(lat - latDelta, lon - lonDelta);
        LatLng ne = new LatLng(lat + latDelta, lon + lonDelta);

        return RectangularBounds.newInstance(sw, ne);
    }

    private static double distanceMeters(LatLng a, LatLng b) {
        float[] res = new float[1];
        Location.distanceBetween(a.latitude, a.longitude, b.latitude, b.longitude, res);
        return res[0];
    }

    static class GarageItem {
        final String placeId;
        final String name;
        final String address;
        final LatLng latLng;
        final double distanceMeters;
        final String phone;
        final String website;

        GarageItem(String placeId, String name, String address, LatLng latLng,
                   double distanceMeters, String phone, String website) {
            this.placeId = placeId;
            this.name = name;
            this.address = address;
            this.latLng = latLng;
            this.distanceMeters = distanceMeters;
            this.phone = phone;
            this.website = website;
        }
    }

    private void openNav(GarageItem item) {
        Uri uri = Uri.parse("google.navigation:q=" + item.latLng.latitude + "," + item.latLng.longitude + "&mode=d");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage("com.google.android.apps.maps");
        if (intent.resolveActivity(getPackageManager()) != null) startActivity(intent);
        else {
            Uri fallback = Uri.parse("geo:" + item.latLng.latitude + "," + item.latLng.longitude);
            startActivity(new Intent(Intent.ACTION_VIEW, fallback));
        }
    }

    static class GaragesAdapter extends RecyclerView.Adapter<GarageViewHolder> {

        interface OnGarageAction {
            void onNavigate(GarageItem item);
            void onItemClick(GarageItem item);
            void onCall(GarageItem item);
            void onWebsite(GarageItem item);
        }

        private final List<GarageItem> items;
        private final OnGarageAction action;

        GaragesAdapter(List<GarageItem> items, OnGarageAction action) {
            this.items = items;
            this.action = action;
        }

        @NonNull
        @Override
        public GarageViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_garage, parent, false);
            return new GarageViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull GarageViewHolder holder, int position) {
            GarageItem item = items.get(position);

            holder.tvName.setText(item.name);
            holder.tvAddress.setText(item.address);

            double km = item.distanceMeters / 1000.0;
            holder.tvDistance.setText(String.format(Locale.getDefault(), "%.1f ק״מ", km));

            holder.itemView.setOnClickListener(v -> action.onItemClick(item));
            holder.btnNavigate.setOnClickListener(v -> action.onNavigate(item));

            // Call
            if (item.phone == null || item.phone.trim().isEmpty()) {
                holder.btnCall.setEnabled(false);
                holder.btnCall.setAlpha(0.5f);
            } else {
                holder.btnCall.setEnabled(true);
                holder.btnCall.setAlpha(1f);
                holder.btnCall.setOnClickListener(v -> action.onCall(item));
            }

            // Website
            if (item.website == null || item.website.trim().isEmpty()) {
                holder.btnWebsite.setEnabled(false);
                holder.btnWebsite.setAlpha(0.5f);
            } else {
                holder.btnWebsite.setEnabled(true);
                holder.btnWebsite.setAlpha(1f);
                holder.btnWebsite.setOnClickListener(v -> action.onWebsite(item));
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    static class GarageViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName, tvAddress, tvDistance;
        final MaterialButton btnNavigate, btnCall, btnWebsite;

        GarageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvGarageName);
            tvAddress = itemView.findViewById(R.id.tvGarageAddress);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            btnNavigate = itemView.findViewById(R.id.btnNavigate);
            btnCall = itemView.findViewById(R.id.btnCall);
            btnWebsite = itemView.findViewById(R.id.btnWebsite);
        }
    }
}
