package com.busviet;

import java.io.Serializable;

public class Bus implements Serializable {
    public String id;
    public String routeCode;
    public String startPoint;
    public String endPoint;
    public int ticketPrice;
    public boolean active;

    public Bus() {}

    public Bus(String id, String routeCode, String startPoint, String endPoint, int ticketPrice, boolean active) {
        this.id = id;
        this.routeCode = routeCode;
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.ticketPrice = ticketPrice;
        this.active = active;
    }
}
