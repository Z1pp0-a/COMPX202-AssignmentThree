package com.example.assignmentthree;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.assignmentthree.R;
import com.example.assignmentthree.model.ParkModel;
import com.example.assignmentthree.service.ApiCallback;
import com.example.assignmentthree.service.ApiService;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.Arrays;

public class MapActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng userLatLng;
    private Marker destinationMarker;
    private ApiService apiService;

    // 自定义 Marker 颜色
    private static final float HUE_PARK = BitmapDescriptorFactory.HUE_GREEN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        apiService = new ApiService(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // 确保 Places SDK 已初始化
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiService.GOOGLE_API_KEY);
        }

        // 步骤 1: 加载地图
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // 步骤 2: 设置搜索栏
        setupAutocomplete();
    }

    // --- MAP INIT AND PERMISSION HANDLING ---
    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.setOnMarkerClickListener(this);
        checkLocationPermission();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLocation();
        } else {
            Toast.makeText(this, "Location permission denied. Cannot show current location.", Toast.LENGTH_LONG).show();
        }
    }

    // --- LOCATION RETRIEVAL (Figure 2) ---
    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        googleMap.setMyLocationEnabled(true);
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                userLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                // 蓝色 Marker: 用户当前位置 (Assignment requirement)
                googleMap.addMarker(new MarkerOptions()
                        .position(userLatLng)
                        .title("My Location")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                        .zIndex(1.0f) // 确保在底部显示
                );
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f));
            }
        });
    }

    // --- PLACES SEARCH (Figure 3 & 4) ---
    private void setupAutocomplete() {
        AutocompleteSupportFragment autocompleteFragment =
                (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // 我们只需要 Place ID, 经纬度 和 名称
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                LatLng destinationLatLng = place.getLatLng();
                if (destinationLatLng == null) return;

                // 清除地图，但保留定位蓝点
                googleMap.clear();
                if(userLatLng != null) {
                    // 重新添加用户位置 Marker
                    googleMap.addMarker(new MarkerOptions()
                            .position(userLatLng)
                            .title("My Location")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                            .zIndex(1.0f)
                    );
                }

                // 红色 Marker: 目的地
                destinationMarker = googleMap.addMarker(new MarkerOptions()
                        .position(destinationLatLng)
                        .title(place.getName())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        .tag("DESTINATION") // 用于区分
                );
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destinationLatLng, 14f));

                // 触发 Nearby Search
                searchNearbyParks(destinationLatLng);
            }

            @Override
            public void onError(@NonNull com.google.android.libraries.places.api.net.Status status) {
                Toast.makeText(MapActivity.this, "Search error: " + status.getStatusMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // --- NEARBY PARKS (Figure 4) ---
    private void searchNearbyParks(LatLng center) {
        apiService.findNearbyParks(center, new ApiCallback<List<ParkModel>>() {
            @Override
            public void onSuccess(List<ParkModel> parks) {
                for (ParkModel park : parks) {
                    // 绿色 Marker: 公园
                    Marker parkMarker = googleMap.addMarker(new MarkerOptions()
                            .position(park.getLocation())
                            .title(park.getName())
                            .icon(BitmapDescriptorFactory.defaultMarker(HUE_PARK))
                            .zIndex(2.0f) // 确保公园 Marker 在用户 Marker 上方
                    );
                    // 核心：将 placeId 存储在 Marker tag 中
                    parkMarker.setTag(park.getPlaceId());
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(MapActivity.this, "Failed to load parks: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    // --- MARKER CLICK (跳转到 DetailActivity) ---
    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        // 确保点击的是公园 Marker (即 tag 是 placeId, 且不是 DESTINATION)
        if (marker.getTag() != null && marker.getIcon().equals(BitmapDescriptorFactory.defaultMarker(HUE_PARK))) {

            String placeId = (String) marker.getTag();
            LatLng location = marker.getPosition();

            Intent intent = new Intent(MapActivity.this, DetailActivity.class);
            intent.putExtra("PLACE_ID", placeId);
            intent.putExtra("LATITUDE", location.latitude);
            intent.putExtra("LONGITUDE", location.longitude);
            startActivity(intent);
            return true; // 消费点击事件
        }
        return false; // 允许默认的信息窗口行为
    }
}
