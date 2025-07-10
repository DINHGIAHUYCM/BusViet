package com.busviet;

import java.io.Serializable;

public class Ticket implements Serializable {
    public long expireDate;
    public boolean isValid;
    public long purchaseDate;
    public String purchaseMonth;
    public String routeCode;
    public int ticketCount;
    public String ticketType;
    public int totalPrice;
    public String username;
    public boolean validInCurrentMonth;

    public Ticket() {
    }
}