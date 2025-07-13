package com.busviet;

import com.google.android.gms.maps.model.LatLng;
import java.util.List;

public class BusRoute {
    private String busNumber;
    private String departureStop;
    private String arrivalStop;
    private int numStops;
    private List<LatLng> polyline;

    public BusRoute(String busNumber, String departureStop, String arrivalStop, int numStops, List<LatLng> polyline) {
        this.busNumber = busNumber;
        this.departureStop = departureStop;
        this.arrivalStop = arrivalStop;
        this.numStops = numStops;
        this.polyline = polyline;
    }

    // Getters
    public String getBusNumber() { return busNumber; }
    public String getDepartureStop() { return departureStop; }
    public String getArrivalStop() { return arrivalStop; }
    public int getNumStops() { return numStops; }
    public List<LatLng> getPolyline() { return polyline; }
}
