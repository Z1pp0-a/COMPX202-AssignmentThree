package com.example.assignmentthree;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.assignmentthree.R;
import com.example.assignmentthree.service.ApiCallback;
import com.example.assignmentthree.service.ApiService;
import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONObject;

public class DetailActivity extends AppCompatActivity {

    private ApiService apiService;

    private TextView nameTv, addressTv, hoursTv, weatherTv, reviewsTv;
    private RatingBar ratingBar;
    private ImageView streetViewIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        apiService = new ApiService(this);
        initViews();

        // 接收 Intent 数据
        String placeId = getIntent().getStringExtra("PLACE_ID");
        double lat = getIntent().getDoubleExtra("LATITUDE", 0);
        double lon = getIntent().getDoubleExtra("LONGITUDE", 0);
        LatLng location = new LatLng(lat, lon);

        // 触发所有 API 请求
        loadPlaceDetails(placeId);
        loadWeather(location);
        loadStreetView(location);
    }

    private void initViews() {
        nameTv = findViewById(R.id.tv_park_name);
        addressTv = findViewById(R.id.tv_address);
        hoursTv = findViewById(R.id.tv_hours);
        weatherTv = findViewById(R.id.tv_weather);
        reviewsTv = findViewById(R.id.tv_reviews);
        ratingBar = findViewById(R.id.rating_bar);
        streetViewIv = findViewById(R.id.iv_street_view);
    }

    // --- API CALL 1: PLACES DETAILS ---
    private void loadPlaceDetails(String placeId) {
        apiService.getPlaceDetails(placeId, new ApiCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    nameTv.setText(result.optString("name", "N/A"));
                    addressTv.setText(result.optString("formatted_address", "Address not available"));

                    // Rating
                    ratingBar.setRating((float) result.optDouble("rating", 0.0));

                    // Hours
                    JSONObject hours = result.optJSONObject("opening_hours");
                    if (hours != null && hours.optBoolean("open_now", false)) {
                        // 简单显示当前状态或第一个文本行
                        JSONArray weekdayText = hours.optJSONArray("weekday_text");
                        hoursTv.setText(weekdayText != null ? weekdayText.getString(0) : "Open now.");
                    } else {
                        hoursTv.setText("Opening hours not available.");
                    }

                    // Reviews (Max 5)
                    JSONArray reviews = result.optJSONArray("reviews");
                    StringBuilder reviewText = new StringBuilder();
                    if (reviews != null) {
                        for (int i = 0; i < Math.min(5, reviews.length()); i++) {
                            JSONObject review = reviews.getJSONObject(i);
                            reviewText.append(String.format("(%.1f stars) %s\n",
                                    review.getDouble("rating"), review.getString("text")));
                        }
                    } else {
                        reviewText.append("No user reviews found.");
                    }
                    reviewsTv.setText(reviewText.toString());

                } catch (Exception e) {
                    Toast.makeText(DetailActivity.this, "Details Parsing Error", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(DetailActivity.this, "Failed to load details: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    // --- API CALL 2: WEATHER ---
    private void loadWeather(LatLng location) {
        apiService.getCurrentWeather(location, new ApiCallback<String>() {
            @Override
            public void onSuccess(String weatherResult) {
                weatherTv.setText(weatherResult);
            }

            @Override
            public void onError(String error) {
                weatherTv.setText("Weather: Failed to retrieve data.");
            }
        });
    }

    // --- API CALL 3: STREET VIEW ---
    private void loadStreetView(LatLng location) {
        // 构建 Street View Static API URL (直接 URL，无需 Volley)
        String streetViewUrl = String.format(
                "https://maps.googleapis.com/maps/api/streetview?size=600x400&location=%f,%f&key=%s",
                location.latitude, location.longitude, apiService.GOOGLE_API_KEY
        );

        // 使用 Picasso 加载图片
        Picasso.get()
                .load(streetViewUrl)
                // 你应该创建自己的占位符图标
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_foreground)
                .into(streetViewIv);
    }
}
