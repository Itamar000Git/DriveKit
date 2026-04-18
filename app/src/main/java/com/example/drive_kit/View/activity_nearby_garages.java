package com.example.drive_kit.View;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.drive_kit.R;
import com.example.drive_kit.ViewModel.NearbyGaragesViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Activity for displaying nearby garages.
 *
 * This is the UI layer:
 * - Shows the map and the list (RecyclerView)
 * - Requests location permissions (Android UI responsibility)
 * - Starts Intents (dial, navigation, open website)
 *
 * The ViewModel holds the screen state and business logic:
 * - Loads location + garages via repository
 * - Filters/sorts the list based on the slider radius
 * - Fetches phone number only when user clicks "Call"
 */
public class activity_nearby_garages extends BaseLoggedInActivity implements OnMapReadyCallback {

    // UI
    private TextView tvRadiusValue, tvEmptyState;
    private ProgressBar progressLoading;
    private Slider sliderRadiusKm;
    private RecyclerView rvGarages;

    // Map
    private GoogleMap googleMap;

    // Recycler data (kept here because adapter uses it directly)
    private final List<GarageItem> visibleGarages = new ArrayList<>();

    // Map -> item mapping (for marker clicks)
    private final Map<Marker, GarageItem> markerToGarage = new HashMap<>();

    private GaragesAdapter adapter;
    private ActivityResultLauncher<String[]> locationPermissionLauncher;

    private NearbyGaragesViewModel vm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        vm = new ViewModelProvider(this).get(NearbyGaragesViewModel.class);

        // Find views
        tvRadiusValue = findViewById(R.id.tvRadiusValue);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        progressLoading = findViewById(R.id.progressLoading);
        sliderRadiusKm = findViewById(R.id.sliderRadiusKm);
        rvGarages = findViewById(R.id.rvGarages);

        // Setup RecyclerView
        adapter = new GaragesAdapter(visibleGarages, new GaragesAdapter.OnGarageAction() {
            @Override
            public void onNavigate(GarageItem item) {
                openNav(item);
            }

            @Override
            public void onItemClick(GarageItem item) {
                if (googleMap != null) {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(item.latLng, 16f));
                }
            }

            @Override
            public void onCall(GarageItem item, MaterialButton callButton) {
                if (item == null) return;
                onCallClicked(item, callButton);
            }

            @Override
            public void onWebsite(GarageItem item) {
                if (item.website == null || item.website.trim().isEmpty()) return;
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(item.website)));
            }
        });

        rvGarages.setLayoutManager(new LinearLayoutManager(this));
        rvGarages.setAdapter(adapter);

        // Permission launcher (UI responsibility)
        locationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    boolean fine = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION));
                    boolean coarse = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_COARSE_LOCATION));
                    if (fine || coarse) {
                        vm.onLocationPermissionGranted();
                    } else {
                        Toast.makeText(this, "צריך הרשאת מיקום כדי להציג מוסכים קרובים", Toast.LENGTH_LONG).show();
                    }
                }
        );

        // Init radius from slider and update VM
        int radiusKm = (int) sliderRadiusKm.getValue();
        updateRadiusText(radiusKm);
        vm.setRadiusKm(radiusKm);

        // When slider changes, we only update VM state (VM recomputes list)
        sliderRadiusKm.addOnChangeListener((slider, value, fromUser) -> {
            int km = (int) value;
            updateRadiusText(km);
            vm.setRadiusKm(km);
        });

        // Observe VM state and update UI
        bindViewModel();

        // Setup map
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapContainer);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Toast.makeText(this, "שגיאה: mapContainer לא נמצא", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_nearby_garages;
    }

    /**
     * Connect UI to the ViewModel LiveData.
     * This keeps the Activity simple: it only draws UI based on state.
     */
    private void bindViewModel() {
        vm.getLoading().observe(this, isLoading -> {
            progressLoading.setVisibility(Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE);
        });

        vm.getEmptyText().observe(this, text -> {
            if (text == null || text.trim().isEmpty()) {
                tvEmptyState.setVisibility(View.GONE);
            } else {
                tvEmptyState.setVisibility(View.VISIBLE);
                tvEmptyState.setText(text);
            }
        });

        vm.getToastMessage().observe(this, msg -> {
            // This is a simple one-way message (not persisted)
            if (msg != null && !msg.trim().isEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        });

        vm.getUserLatLng().observe(this, ll -> {
            if (ll == null) return;
            if (googleMap == null) return;

            // UI concern: enable blue dot if permission exists
            try {
                if (hasLocationPermission()) {
                    googleMap.setMyLocationEnabled(true);
                }
            } catch (Exception ignored) {
            }

            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ll, 13.5f));
        });

        vm.getVisibleGarages().observe(this, list -> {
            visibleGarages.clear();
            if (list != null) visibleGarages.addAll(list);

            adapter.notifyDataSetChanged();
            redrawMarkers();
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        // Marker click -> focus + small toast (same behavior)
        googleMap.setOnMarkerClickListener(marker -> {
            GarageItem item = markerToGarage.get(marker);
            if (item != null) {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(item.latLng, 16f));
                Toast.makeText(this, item.name, Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });

        // Tell VM that map exists (VM starts loading only when permission is also granted)
        vm.setMapReady(true);

        if (hasLocationPermission()) {
            vm.onLocationPermissionGranted();
        } else {
            requestLocationPermission();
        }
    }

    /**
     * Redraw markers based on the currently visible (filtered) list.
     */
    private void redrawMarkers() {
        if (googleMap == null) return;

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

    /**
     * Called when user clicks "Call".
     * - If phone is already cached -> dial immediately.
     * - Otherwise -> ask VM to fetch phone (repository does fetchPlace).
     */
    private void onCallClicked(@NonNull GarageItem item, MaterialButton callButton) {
        Log.d("GARAGES_PHONE", "click call: placeId=" + item.placeId + ", cachedPhone=" + item.phone);

        // Cached phone -> dial immediately
        if (item.phone != null && !item.phone.trim().isEmpty()) {
            startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + item.phone.trim())));
            return;
        }

        // No placeId -> cannot fetch phone
        if (item.placeId == null || item.placeId.trim().isEmpty()) {
            Toast.makeText(this, "אין מספר טלפון זמין לעסק הזה", Toast.LENGTH_SHORT).show();
            return;
        }

        setCallButtonLoading(callButton, true);

        vm.fetchPhone(item.placeId, phone -> {
            setCallButtonLoading(callButton, false);

            if (phone == null || phone.trim().isEmpty()) {
                Toast.makeText(this, "לא נמצא מספר טלפון לעסק הזה", Toast.LENGTH_SHORT).show();
                return;
            }

            // Cache the phone on the UI item (so next click is immediate)
            item.phone = phone.trim();

            startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + item.phone.trim())));
        });
    }

    /**
     * Small UI helper to show loading state on the call button.
     * This avoids adding a full progress view inside the list item.
     */
    private void setCallButtonLoading(MaterialButton button, boolean isLoading) {
        if (button == null) return;

        if (isLoading) {
            button.setTag(button.getText());
            button.setEnabled(false);
            button.setAlpha(0.8f);
            button.setText("טוען...");
        } else {
            Object prev = button.getTag();
            CharSequence original = (prev instanceof CharSequence) ? (CharSequence) prev : "התקשר";
            button.setText(original);
            button.setEnabled(true);
            button.setAlpha(1f);
        }
    }

    private void updateRadiusText(int km) {
        tvRadiusValue.setText("טווח: " + km + " ק״מ");
    }

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        locationPermissionLauncher.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    /**
     * Opens Google Maps navigation to the selected garage.
     * This is UI/Intent logic, so it stays in the Activity.
     */
    private void openNav(GarageItem item) {
        Uri uri = Uri.parse("google.navigation:q=" + item.latLng.latitude + "," + item.latLng.longitude + "&mode=d");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage("com.google.android.apps.maps");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Uri fallback = Uri.parse("geo:" + item.latLng.latitude + "," + item.latLng.longitude);
            startActivity(new Intent(Intent.ACTION_VIEW, fallback));
        }
    }

    // --------------------------------------------------
    // UI model used by adapter + map
    // --------------------------------------------------

    public static class GarageItem {
        public final String placeId;
        public final String name;
        public final String address;
        public final LatLng latLng;
        public final double distanceMeters;
        public String phone;
        public final String website;

        public GarageItem(String placeId, String name, String address, LatLng latLng,
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

    // --------------------------------------------------
    // RecyclerView
    // --------------------------------------------------

    static class GaragesAdapter extends RecyclerView.Adapter<GarageViewHolder> {

        interface OnGarageAction {
            void onNavigate(GarageItem item);
            void onItemClick(GarageItem item);
            void onCall(GarageItem item, MaterialButton callButton);
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

            // Call: always enabled, fetch happens on click
            holder.btnCall.setEnabled(true);
            holder.btnCall.setAlpha(1f);
            holder.btnCall.setOnClickListener(v -> action.onCall(item, holder.btnCall));

            // Website: enabled only if we have a URL
            if (item.website == null || item.website.trim().isEmpty()) {
                holder.btnWebsite.setEnabled(false);
                holder.btnWebsite.setAlpha(0.5f);
                holder.btnWebsite.setOnClickListener(null);
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
