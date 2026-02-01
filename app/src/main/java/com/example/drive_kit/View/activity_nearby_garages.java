package com.example.drive_kit.View;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
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
import java.util.List;
import java.util.Locale;

public class activity_nearby_garages extends AppCompatActivity implements OnMapReadyCallback {

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
    private GaragesAdapter adapter;

    private ActivityResultLauncher<String[]> locationPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_garages);


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
        });
        rvGarages.setLayoutManager(new LinearLayoutManager(this));
        rvGarages.setAdapter(adapter);

        fusedClient = LocationServices.getFusedLocationProviderClient(this);

        // Places init (New)
        // אם אצלך קיימת המתודה initializeWithNewPlacesApiEnabled() – תעדיף אותה לפי הדוק׳.
        // אחרת initialize() עדיין יעבוד בהתאם לגרסה.
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
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);

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
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    googleMap.setMyLocationEnabled(true);
                }
            } catch (Exception ignored) {}

            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 13.5f));
        }

        // נטען עד 20 ק"מ ואז מסננים לפי הסליידר (כמו שעשית ב-OSM)
        fetchGaragesByText("מוסך", 20000);

        progressLoading.setVisibility(View.GONE);
    }

    /**
     * Text Search (New): מחפש לפי טקסט ("מוסך") בתוך אזור מוגבל סביב המשתמש.
     * Text Search מאפשר מחרוזת חופשית. :contentReference[oaicite:10]{index=10}
     */
    private void fetchGaragesByText(String query, int fetchRadiusMeters) {
        if (userLatLng == null) return;

        progressLoading.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);

        RectangularBounds bounds = makeBounds(userLatLng, fetchRadiusMeters);

        List<Place.Field> fields = Arrays.asList(
                Place.Field.ID,
                Place.Field.DISPLAY_NAME,
                Place.Field.FORMATTED_ADDRESS,
                Place.Field.LOCATION
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

                        String name = p.getDisplayName();
                        if (name == null || name.trim().isEmpty()) name = "מוסך";

                        String addr = p.getFormattedAddress();
                        if (addr == null || addr.trim().isEmpty()) addr = "כתובת לא זמינה";

                        double dist = distanceMeters(userLatLng, loc);
                        allGarages.add(new GarageItem(name, addr, loc, dist));
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
        if (userLatLng == null || googleMap == null) return;

        visibleGarages.clear();
        double maxMeters = radiusKm * 1000.0;

        for (GarageItem g : allGarages) {
            if (g.distanceMeters <= maxMeters) visibleGarages.add(g);
        }

        Collections.sort(visibleGarages, Comparator.comparingDouble(o -> o.distanceMeters));
        adapter.notifyDataSetChanged();

        googleMap.clear();

        for (GarageItem g : visibleGarages) {
            googleMap.addMarker(new MarkerOptions()
                    .position(g.latLng)
                    .title(g.name)
                    .snippet(g.address));
        }

        tvEmptyState.setVisibility(visibleGarages.isEmpty() ? View.VISIBLE : View.GONE);
        if (visibleGarages.isEmpty()) tvEmptyState.setText("לא נמצאו מוסכים בטווח שנבחר.");
    }

    private static RectangularBounds makeBounds(LatLng center, int radiusMeters) {
        // קירוב פשוט: 1 deg lat ~= 111,320m
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

    // --- Data + Adapter ---

    static class GarageItem {
        final String name;
        final String address;
        final LatLng latLng;
        final double distanceMeters;

        GarageItem(String name, String address, LatLng latLng, double distanceMeters) {
            this.name = name;
            this.address = address;
            this.latLng = latLng;
            this.distanceMeters = distanceMeters;
        }
    }

    private void openNav(GarageItem item) {
        // השארתך בכוונה כמו קודם – אם תרצה, נחבר גם ל-Intent הרשמי של Google Maps.
        android.net.Uri uri = android.net.Uri.parse("google.navigation:q=" + item.latLng.latitude + "," + item.latLng.longitude + "&mode=d");
        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW, uri);
        intent.setPackage("com.google.android.apps.maps");
        if (intent.resolveActivity(getPackageManager()) != null) startActivity(intent);
        else {
            android.net.Uri fallback = android.net.Uri.parse("geo:" + item.latLng.latitude + "," + item.latLng.longitude);
            startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW, fallback));
        }
    }

    static class GaragesAdapter extends RecyclerView.Adapter<GarageViewHolder> {

        interface OnGarageAction {
            void onNavigate(GarageItem item);
            void onItemClick(GarageItem item);
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
            android.view.View v = android.view.LayoutInflater.from(parent.getContext())
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
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    static class GarageViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName, tvAddress, tvDistance;
        final MaterialButton btnNavigate;

        GarageViewHolder(@NonNull android.view.View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvGarageName);
            tvAddress = itemView.findViewById(R.id.tvGarageAddress);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            btnNavigate = itemView.findViewById(R.id.btnNavigate);
        }
    }
}
