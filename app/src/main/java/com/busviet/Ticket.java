package com.busviet;

public class Ticket {
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

    public Ticket() {} // cần constructor rỗng cho Firebase
}
