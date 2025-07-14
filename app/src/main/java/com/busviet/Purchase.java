package com.busviet;

import java.io.Serializable;


public class Purchase implements Serializable {
    public String username;
    public String routeCode;
    public String ticketType; // "single", "daily", "monthly", "quarterly", "yearly"
    public int ticketCount;
    public long purchaseDate; // timestamp
    public long expireDate; // timestamp
    public int totalPrice;
    public boolean isValid;

    public Purchase() {
    }

    public Purchase(String username, String routeCode, String ticketType, int ticketCount, 
                   long purchaseDate, long expireDate, int totalPrice, boolean isValid) {
        this.username = username;
        this.routeCode = routeCode;
        this.ticketType = ticketType;
        this.ticketCount = ticketCount;
        this.purchaseDate = purchaseDate;
        this.expireDate = expireDate;
        this.totalPrice = totalPrice;
        this.isValid = isValid;
    }
    
    // Helper method để get tháng mua vé (format: YYYY-MM)
    public String getPurchaseMonth() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM");
        return sdf.format(new java.util.Date(purchaseDate));
    }
    
    // Helper method để check vé còn hiệu lực
    public boolean isValidInCurrentMonth() {
        long currentTime = System.currentTimeMillis();
        return isValid && currentTime >= purchaseDate && currentTime <= expireDate;
    }
}
