package com.example.assignmentthree.service;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.assignmentthree.model.ParkModel;
import com.google.android.gms.maps.model.LatLng;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class ApiService {

    private final Context context;
    // !!! 替换为你自己的 API KEY !!!
    private final String GOOGLE_API_KEY = "YOUR_GOOGLE_API_KEY_HERE";
    private final String WEATHER_API_KEY = "YOUR_WEATHER_API_KEY_HERE";

    public ApiService(Context context) {
        this.context = context;
    }

    // --- MAP ACTIVITY METHODS (Nearby Search) ---
    public void findNearbyParks(LatLng center, ApiCallback<List<ParkModel>> callback) {
        // 构建 Places Nearby Search URL
        String url = String.format(
                "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=%f,%f&radius=3000&type=park&key=%s",
                center.latitude, center.longitude, GOOGLE_API_KEY
        );

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        List<ParkModel> parks = new ArrayList<>();
                        JSONArray results = response.getJSONArray("results");

                        // 只处理前 10 个公园
                        for (int i = 0; i < Math.min(10, results.length()); i++) {
                            JSONObject place = results.getJSONObject(i);
                            String placeId = place.getString("place_id");
                            String name = place.getString("name");

                            JSONObject locationObj = place.getJSONObject("geometry").getJSONObject("location");
                            LatLng location = new LatLng(locationObj.getDouble("lat"), locationObj.getDouble("lng"));

                            parks.add(new ParkModel(placeId, name, location));
                        }
                        callback.onSuccess(parks);
                    } catch (Exception e) {
                        callback.onError("JSON Parsing Error during Nearby Search: " + e.getMessage());
                    }
                },
                error -> callback.onError("Network Error finding parks: " + (error.getMessage() != null ? error.getMessage() : "Unknown Error"))
        );
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    // --- DETAIL ACTIVITY METHODS (Details, Weather) ---
    // (我们将在步骤 10 中补充这些方法)
    public void getPlaceDetails(String placeId, ApiCallback<JSONObject> callback) {
        // 留空，将在步骤 10 补充
    }
    public void getCurrentWeather(LatLng location, ApiCallback<String> callback) {
        // 留空，将在步骤 10 补充
    }
}
