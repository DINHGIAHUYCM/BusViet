package com.busviet;

public class Ticket {
    public int remaining;
    public String expireDate; // ISO-8601 format: yyyy-MM-dd

    public Ticket() {
    }

    public Ticket(int remaining, String expireDate) {
        this.remaining = remaining;
        this.expireDate = expireDate;
    }
}
