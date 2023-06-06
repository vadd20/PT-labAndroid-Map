package com.example.city_sight.sight;

import com.yandex.mapkit.geometry.Point;

public class Sight {
    private String title;
    private String discovery;
    private Point coordinates;


    public Sight(String title, String discovery, Point coordinates) {
        this.title = title;
        this.discovery = discovery;
        this.coordinates = coordinates;
    }

    public Point getCoordinates() {
        return coordinates;
    }

    public Sight(String title, String discovery, Point coordinates, String workHours) {
        this.title = title;
        this.discovery = discovery;
        this.coordinates = coordinates;
    }


    public String getDiscovery() {
        return discovery;
    }

    public String getTitle() {
        return title;
    }
}
