package com.RouteMappingProject.RouteMapping;

public class Node {
    private double lon;
    private double lat;
    private String ref;

    public Node(double longitude, double latitude, String ref) {
        this.lon = longitude;
        this.lat = latitude;
        this.ref = ref;
    }

    public Node(double longitude, double latitude) {
        this.lon = longitude;
        this.lat = latitude;
    }

    public Node(Node node) {
        this.lon = node.getLon();
        this.lat = node.getLat();
        this.ref = node.getRef();
    }

    public double getLon() {
        return lon;
    }
    public double getLat() {
        return lat;
    }
    public String getRef() { return ref; }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    @Override
    public String toString() {
        return "(" + lat + ", " + lon + ")";
    }
}
