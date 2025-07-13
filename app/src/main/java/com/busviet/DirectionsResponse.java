package com.busviet;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class DirectionsResponse {
    private List<Route> routes;

    public List<Route> getRoutes() { 
        return routes != null ? routes : new ArrayList<>(); 
    }

    public static class Route {
        private List<Leg> legs;
        public List<Leg> getLegs() { 
            return legs != null ? legs : new ArrayList<>(); 
        }
    }

    public static class Leg {
        private List<Step> steps;
        public List<Step> getSteps() { 
            return steps != null ? steps : new ArrayList<>(); 
        }
    }

    public static class Step {
        @SerializedName("transit_details")
        private TransitDetails transitDetails;
        private Polyline polyline;
        
        public TransitDetails getTransitDetails() { return transitDetails; }
        public Polyline getPolyline() { return polyline; }
    }

    public static class TransitDetails {
        @SerializedName("departure_stop")
        private Stop departureStop;
        @SerializedName("arrival_stop")
        private Stop arrivalStop;
        @SerializedName("num_stops")
        private int numStops;
        private Line line;

        public Stop getDepartureStop() { return departureStop; }
        public Stop getArrivalStop() { return arrivalStop; }
        public int getNumStops() { return numStops; }
        public Line getLine() { return line; }
    }

    public static class Stop {
        private String name;
        public String getName() { return name != null ? name : ""; }
    }

    public static class Line {
        @SerializedName("short_name")
        private String shortName;
        public String getShortName() { return shortName != null ? shortName : ""; }
    }

    public static class Polyline {
        private String points;
        public String getPoints() { return points != null ? points : ""; }
    }
}
