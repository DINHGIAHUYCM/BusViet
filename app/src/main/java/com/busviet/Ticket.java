package com.busviet;

import java.io.Serializable;

public class Ticket implements Serializable {
    public boolean isValid;
    public long purchaseDate;
    public String purchaseMonth;
    public String routeCode;
    public int ticketCount;
    public String ticketType;
    public int totalPrice;
    public String username;
    public boolean validInCurrentMonth;
    public int remaining;
    public String expireDate; // ISO-8601 format: yyyy-MM-dd

    public Ticket() {
    }

    public Ticket(int remaining, String expireDate) {
        this.remaining = remaining;
        this.expireDate = expireDate;
    }
}
