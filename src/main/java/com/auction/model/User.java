package com.auction.model;

import com.auction.util.Enums;

import java.util.Locale;

public class User {
    private final int user_Id;
    private String username;
    private Enums.UserRole userRole;

    public User(int id, String username, String role) {
        this.user_Id = id;
        this.username = username;
        this.userRole = Enums.UserRole.getEnumByValue(role);
    }

    public String getRole() {
        return userRole.getValue();
    }

    public String getUsername() {
        return username;
    }

    public int getUser_Id() {
        return user_Id;
    }

}
