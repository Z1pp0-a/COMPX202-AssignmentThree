package com.example.assignmentthree.model;

import com.google.android.gms.maps.model.LatLng;
import java.io.Serializable; // 方便 Intent 传输，尽管我们只传 LatLng

public class ParkModel implements Serializable {
    private String placeId;
    private String name;
    private LatLng location;

    public ParkModel(String placeId, String name, LatLng location) {
        this.placeId = placeId;
        this.name = name;
        this.location = location;
    }

    public String getPlaceId() { return placeId; }
    public String getName() { return name; }
    public LatLng getLocation() { return location; }

    // 简化：这里不需要实现其他 Detail 字段的 setter/getter，因为 DetailActivity 会直接处理 JSON。
}
