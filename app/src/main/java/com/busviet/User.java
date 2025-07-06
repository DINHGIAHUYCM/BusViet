package com.busviet;

public class User {
    public String password;
    public String contact;
    public String phone;

    public String role;

    public User() {
    }

    public User(String password, String contact, String phone, String role) {
        this.password = password;
        this.contact = contact;
        this.phone = phone;
        this.role = role;
    }
}
