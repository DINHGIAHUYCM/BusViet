package com.busviet;

public class BusRouteItem {
    private String busName;
    private String departureStop;
    private String arrivalStop;
    private int numStops;
    private String price;

    public BusRouteItem(String busName, String departureStop, String arrivalStop, int numStops, String price) {
        this.busName = busName;
        this.departureStop = departureStop;
        this.arrivalStop = arrivalStop;
        this.numStops = numStops;
        this.price = price;
    }

    // Getters
    public String getBusName() { return busName; }
    public String getDepartureStop() { return departureStop; }
    public String getArrivalStop() { return arrivalStop; }
    public int getNumStops() { return numStops; }
    public String getPrice() { return price; }
}
