package com.auction.database;

import com.auction.model.User;

import java.sql.*;

public class DatabaseManager {
    private static final String url = "jdbc:mysql://localhost:3306/AuctionSimulation";
    private static final String user = "root";
    private static final String password = "MySQLTej@111111";

    public static Connection getConnection() throws SQLException{
        return DriverManager.getConnection(url, user, password);
    }

}
