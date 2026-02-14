package com.auction.database;

import com.auction.model.AuctionItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

//Class for accessing item data from database
public class ItemDAO {
    public static List<AuctionItem> getItemsByCategory(String category) {
        List<AuctionItem> items = new ArrayList<>();
        String sql = category.equals("All") ? "SELECT * FROM Items" : "SELECT * FROM Items WHERE category = ?";

        try(Connection connection = DatabaseManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {
            if(!category.equals("All")) {
                statement.setString(1, category);
            }
            ResultSet resultSet = statement.executeQuery();

            while(resultSet.next()) {
                items.add(new AuctionItem(resultSet.getString("name"), resultSet.getDouble("current_price"), resultSet.getInt("time_left"), resultSet.getString("category")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public static void updatePriceInDB(String itemName, double newPrice) {
        String updateQuery = "UPDATE Items SET current_price = ? WHERE name = ?";
        new Thread(() -> {
            try(Connection connection = DatabaseManager.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {

                preparedStatement.setDouble(1, newPrice);
                preparedStatement.setString(2, itemName);

                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).start();

    }
}
