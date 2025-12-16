package com.example.assignmentthree;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = "DetailActivity";
    private TextView parkNameTextView;
    private TextView addressTextView;
    private TextView hoursTextView;
    private TextView weatherTextView;
    private RatingBar ratingBar;
    private ImageView streetViewImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Initialize UI components
        parkNameTextView = findViewById(R.id.parkName);
        addressTextView = findViewById(R.id.address);
        hoursTextView = findViewById(R.id.hours);
        weatherTextView = findViewById(R.id.weatherInfo);
        ratingBar = findViewById(R.id.ratingBar);
        streetViewImage = findViewById(R.id.streetViewImage);

        // Get passed data
        String placeId = getIntent().getStringExtra("place_id");
        String parkName = getIntent().getStringExtra("park_name");

        // Display park name
        if (parkName != null) {
            parkNameTextView.setText(parkName);
        }

        // Load park details
        if (placeId != null) {
            loadParkDetails(placeId);
        } else {
            Toast.makeText(this, "No place ID provided", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadParkDetails(String placeId) {
        // Define the fields to request
        List<Place.Field> placeFields = Arrays.asList(
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.OPENING_HOURS,
                Place.Field.RATING,
                Place.Field.USER_RATINGS_TOTAL,
                Place.Field.LAT_LNG
        );

        // Build the request
        FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields).build();

        // Get the Places client and make the request
        PlacesClient placesClient = Places.createClient(this);

        placesClient.fetchPlace(request)
                .addOnSuccessListener(response -> {
                    Place place = response.getPlace();

                    // Update UI with place details
                    if (place.getAddress() != null) {
                        addressTextView.setText(place.getAddress());
                    }

                    if (place.getOpeningHours() != null) {
                        List<String> weekdays = place.getOpeningHours().getWeekdayText();
                        if (weekdays != null && !weekdays.isEmpty()) {
                            StringBuilder hoursBuilder = new StringBuilder();
                            for (String day : weekdays) {
                                hoursBuilder.append(day).append("\n");
                            }
                            hoursTextView.setText(hoursBuilder.toString().trim());
                        } else {
                            hoursTextView.setText("Operating hours not available");
                        }
                    } else {
                        hoursTextView.setText("Operating hours not available");
                    }

                    if (place.getRating() != null) {
                        // Convert Double to float for RatingBar
                        ratingBar.setRating(place.getRating().floatValue());
                    }

                    // Load weather and StreetView if we have coordinates
                    if (place.getLatLng() != null) {
                        loadWeatherData(place.getLatLng());
                        loadStreetViewImage(place.getLatLng());
                    }
                })
                .addOnFailureListener(exception ->
                        Toast.makeText(this, "Failed to load park details: " + exception.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    private void loadWeatherData(LatLng location) {
        // Use your actual Weather API key - replace this
        String weatherApiKey = "YOUR_WEATHER_API_KEY_HERE";

        String url = "http://api.weatherapi.com/v1/current.json" +
                "?key=" + weatherApiKey +
                "&q=" + location.latitude + "," + location.longitude +
                "&aqi=no";

        JsonObjectRequest weatherRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                this::handleWeatherResponse,
                error -> weatherTextView.setText("Weather data not available"));

        // Add to request queue
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(weatherRequest);
    }

    private void handleWeatherResponse(JSONObject response) {
        try {
            JSONObject current = response.getJSONObject("current");
            double tempC = current.getDouble("temp_c");
            String condition = current.getJSONObject("condition").getString("text");

            String weatherText = String.format("Temperature: %.1f°C, %s", tempC, condition);
            weatherTextView.setText(weatherText);
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing weather data", e);
            weatherTextView.setText("Weather data not available");
        }
    }

    private void loadStreetViewImage(LatLng location) {
        // Use your actual Google Maps API key - replace this
        String mapsApiKey = "YOUR_GOOGLE_MAPS_API_KEY_HERE";

        // Build StreetView URL
        String streetViewUrl = "https://maps.googleapis.com/maps/api/streetview" +
                "?size=600x300" +
                "&location=" + location.latitude + "," + location.longitude +
                "&key=" + mapsApiKey +
                "&fov=90" +
                "&heading=235" +
                "&pitch=10";

        // Use Glide to load the image
        Glide.with(this)
                .load(streetViewUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.stat_notify_error)
                .into(streetViewImage);
    }
}