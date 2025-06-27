package com.busviet;

public class User {
    public String userName;
    public String password;
    public String email;
    public String phone;

    public User() {
    }

    public User(String password, String email, String phone, String userName) {
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.userName = userName;
    }
}
