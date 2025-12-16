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

    // ... (代码承接自步骤 8.2)

    // --- DETAIL ACTIVITY METHODS ---

    /**
     * 获取 Places Details (名称, 地址, 评分, 营业时间, 评论)
     */
    public void getPlaceDetails(String placeId, ApiCallback<JSONObject> callback) {
        // 请求 fields 必须在 URL 中明确指定
        String url = String.format(
                "https://maps.googleapis.com/maps/api/place/details/json?place_id=%s&fields=name,formatted_address,rating,opening_hours,review&key=%s",
                placeId, GOOGLE_API_KEY
        );

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        // 成功后，将整个 JSON 响应对象返回给 Activity 处理
                        callback.onSuccess(response.getJSONObject("result"));
                    } catch (Exception e) {
                        callback.onError("Details Parse Error: " + e.getMessage());
                    }
                },
                error -> callback.onError("Details API Error: " + (error.getMessage() != null ? error.getMessage() : "Unknown Error"))
        );
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    /**
     * 获取当前天气数据
     */
    public void getCurrentWeather(LatLng location, ApiCallback<String> callback) {
        // 使用 Weather API
        String url = String.format(
                "http://api.weatherapi.com/v1/current.json?key=%s&q=%f,%f",
                WEATHER_API_KEY, location.latitude, location.longitude
        );

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONObject current = response.getJSONObject("current");
                        double tempC = current.getDouble("temp_c");
                        String condition = current.getJSONObject("condition").getString("text");
                        callback.onSuccess(String.format("%.1f°C, %s", tempC, condition));
                    } catch (Exception e) {
                        callback.onError("Weather Parsing Error: " + e.getMessage());
                    }
                },
                error -> callback.onError("Weather API Error: Failed to fetch data.")
        );
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }
}

