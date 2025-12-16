package com.example.assignmentthree.service;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.assignmentthree.Config;

import org.json.JSONException;
import org.json.JSONObject;

public class ApiService {

    private static ApiService instance;
    private final RequestQueue requestQueue;
    private final Context context;

    private ApiService(Context context) {
        this.context = context.getApplicationContext();
        this.requestQueue = Volley.newRequestQueue(this.context);
    }

    public static synchronized ApiService getInstance(Context context) {
        if (instance == null) {
            instance = new ApiService(context);
        }
        return instance;
    }

    public void getWeatherData(double lat, double lng, WeatherCallback callback) {
        // Use Config class for API key instead of BuildConfig
        String weatherApiKey = Config.WEATHER_API_KEY;

        String url = "http://api.weatherapi.com/v1/current.json" +
                "?key=" + weatherApiKey +
                "&q=" + lat + "," + lng +
                "&aqi=no";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONObject current = response.getJSONObject("current");
                        double tempC = current.getDouble("temp_c");
                        String condition = current.getJSONObject("condition")
                                .getString("text");
                        callback.onSuccess(tempC, condition);
                    } catch (JSONException e) {
                        // Log the error and provide a generic message
                        Log.e("ApiService", "Error parsing weather data", e);
                        callback.onError("Unable to parse weather data");
                    }
                },
                error -> {
                    // Use getMessage() safely
                    String errorMessage = error.getMessage() != null ?
                            error.getMessage() : "Unknown network error";
                    callback.onError("Network error: " + errorMessage);
                });

        requestQueue.add(request);
    }

    public interface WeatherCallback {
        void onSuccess(double temperature, String condition);
        void onError(String error);
    }
}