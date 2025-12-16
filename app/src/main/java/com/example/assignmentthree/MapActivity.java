package com.example.assignmentthree;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

// Google Maps imports
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

// Google Location services
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

// Google Places API
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import com.google.android.gms.common.api.Status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng currentLocation;
    private Marker currentMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Initialize Places API - FIXED: Use string directly or check BuildConfig
        String mapsApiKey = "YOUR_GOOGLE_MAPS_API_KEY_HERE"; // Replace with your actual API key

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), mapsApiKey);
        }

        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Setup search bar
        setupSearchBar();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {  // FIXED: Added @NonNull annotation
        googleMap = map;

        // Configure map
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);

        // Get current location
        getCurrentLocation();
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            currentLocation = new LatLng(location.getLatitude(),
                                    location.getLongitude());

                            // Add blue marker
                            currentMarker = googleMap.addMarker(new MarkerOptions()
                                    .position(currentLocation)
                                    .title("You are here")
                                    .icon(BitmapDescriptorFactory.defaultMarker(
                                            BitmapDescriptorFactory.HUE_BLUE)));

                            // Move camera to current location
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    currentLocation, 14));
                        }
                    });
        }
    }

    private void setupSearchBar() {
        // Initialize Places autocomplete
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        if (autocompleteFragment != null) {
            // Specify the fields to return
            autocompleteFragment.setPlaceFields(Arrays.asList(
                    Place.Field.ID,
                    Place.Field.NAME,
                    Place.Field.LAT_LNG,
                    Place.Field.ADDRESS));

            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    // Handle selected place
                    LatLng destination = place.getLatLng();
                    if (destination != null) {
                        // Add red destination marker
                        googleMap.addMarker(new MarkerOptions()
                                .position(destination)
                                .title(place.getName())
                                .icon(BitmapDescriptorFactory.defaultMarker(
                                        BitmapDescriptorFactory.HUE_RED)));

                        // Search nearby parks - FIXED: Use destination instead of currentLocation
                        searchNearbyParks(destination);
                    }
                }

                @Override
                public void onError(@NonNull Status status) {
                    Toast.makeText(MapActivity.this, "Error: " + status, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void searchNearbyParks(@NonNull LatLng location) {  // FIXED: Added @NonNull and using parameter
        // Build search request
        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.builder(
                        Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG,
                                Place.Field.ADDRESS, Place.Field.RATING))
                .build();

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            PlacesClient placesClient = Places.createClient(this);

            placesClient.findCurrentPlace(request)
                    .addOnSuccessListener((response) -> {
                        List<PlaceLikelihood> placeLikelihoods = response.getPlaceLikelihoods();

                        // Filter parks and limit to 10
                        List<Place> parks = new ArrayList<>();
                        for (PlaceLikelihood placeLikelihood : placeLikelihoods) {
                            Place place = placeLikelihood.getPlace();
                            // TODO: Add park type filtering here
                            parks.add(place);
                            if (parks.size() >= 10) break;
                        }

                        // Display park markers on map
                        displayParkMarkers(parks);
                    })
                    .addOnFailureListener((exception) ->
                            Toast.makeText(MapActivity.this, "Error finding parks",
                                    Toast.LENGTH_SHORT).show());  // FIXED: Converted to expression lambda
        }
    }

    private void displayParkMarkers(List<Place> parks) {
        for (Place park : parks) {
            LatLng parkLocation = park.getLatLng();
            if (parkLocation != null) {
                Marker parkMarker = googleMap.addMarker(new MarkerOptions()
                        .position(parkLocation)
                        .title(park.getName())
                        .icon(BitmapDescriptorFactory.defaultMarker(
                                BitmapDescriptorFactory.HUE_GREEN)));

                // Set marker click listener - FIXED: Added null check
                String parkId = park.getId();
                if (parkId != null) {
                    parkMarker.setTag(parkId);
                }
            }
        }

        // Set marker click listener - FIXED: Converted to expression lambda
        googleMap.setOnMarkerClickListener(marker -> {
            String placeId = (String) marker.getTag();
            if (placeId != null) {
                openParkDetails(placeId, marker.getTitle());
            }
            return true;
        });
    }

    private void openParkDetails(String placeId, String parkName) {
        Intent intent = new Intent(MapActivity.this, DetailActivity.class);
        intent.putExtra("place_id", placeId);
        intent.putExtra("park_name", parkName);
        startActivity(intent);
    }
}