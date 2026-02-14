package com.auction.database;

import com.auction.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
    public static User authenticate(String username, String passwordStr) {
        String sql = "SELECT * FROM Users WHERE username = ? AND password = ?";
        try(Connection connection = DatabaseManager.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, username);
            statement.setString(2, passwordStr);

            ResultSet resultSet = statement.executeQuery();

            if(resultSet.next()) {
                return new User(resultSet.getInt("user_id"), resultSet.getString("username"), resultSet.getString("role"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
