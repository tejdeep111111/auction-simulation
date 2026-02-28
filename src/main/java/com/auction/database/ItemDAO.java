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

        try(ConnectedStatement cs = prepareStatement(sql)) {
            if(!category.equals("All")) {
                cs.statement.setString(1, category);
            }
            ResultSet resultSet = cs.statement.executeQuery();

            while(resultSet.next()) {
                items.add(mapResultSetToAuctionItem(resultSet));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    //a method that does both updating value of price in items table and also place a log into bidlogs table
    public static void placeBidWithLog(int itemId, int userId, String itemName, double newPrice) {
        new Thread(() -> {
            try(Connection connection = DatabaseManager.getConnection()) {
                //Start a transaction
                connection.setAutoCommit(false);

                String updateQuery = "UPDATE Items SET current_price = ? WHERE name = ?";
                try (PreparedStatement preparedStatement1 = connection.prepareStatement(updateQuery)) {
                    preparedStatement1.setDouble(1, newPrice);
                    preparedStatement1.setString(2, itemName);
                    preparedStatement1.executeUpdate();
                }

                String logBid = "INSERT INTO BidLogs (item_id, user_id, bid_amount) VALUES (?, ?, ?)";
                try (PreparedStatement preparedStatement2 = connection.prepareStatement(logBid)) {
                    preparedStatement2.setInt(1, itemId);
                    preparedStatement2.setInt(2, userId);
                    preparedStatement2.setDouble(3, newPrice);
                    preparedStatement2.executeUpdate();
                }

                //End transaction by commiting both changes at once
                connection.commit();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).start();

    }

    public static boolean addItem(String name, double price, int duration, String category) {
        String sql = "INSERT INTO Items (name, current_price, time_left, category) VALUES (?, ?, ?, ?)";
        try (ConnectedStatement cs = prepareStatement(sql)) {
            cs.statement.setString(1, name);
            cs.statement.setDouble(2, price);
            cs.statement.setInt(3, duration);
            cs.statement.setString(4, category);

            return cs.statement.executeUpdate() > 0;
        } catch (SQLException  e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<String> getBidLogs() {
        List<String> logs = new ArrayList<>();

        String sql = "SELECT u.username, i.name, b.bid_amount, b.bid_time " +
                "FROM BidLogs b " +
                "JOIN Users u ON b.user_id = u.user_id " +
                "JOIN Items i ON b.item_id = i.item_id " +
                "ORDER BY b.bid_time DESC";

        try(Connection connection = DatabaseManager.getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                String logEntry = String.format("[%s] %s bid $%.2f on %s",
                        resultSet.getTimestamp("bid_time"),
                        resultSet.getString("username"),
                        resultSet.getDouble("bid_amount"),
                        resultSet.getString("name"));

                logs.add(logEntry);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }

    public static void toggleFavorite(int userId, int itemId) {
        if(isFavorite(userId, itemId)) {
            String sql = "DELETE FROM Favorites WHERE user_id = ? AND item_id = ?";
            executeUpdate(sql, userId, itemId);
        } else {
            String sql = "INSERT INTO Favorites (user_id, item_id) VALUES (?, ?)";
            executeUpdate(sql, userId, itemId);
        }
    }

    //helper for toggle fn
    private static void executeUpdate(String sql, int userId, int itemId) {
        try (ConnectedStatement cs = prepareStatement(sql)) {
            cs.statement.setInt(1, userId);
            cs.statement.setInt(2, itemId);
            cs.statement.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static boolean isFavorite(int userId, int itemId) {
        String sql = "SELECT COUNT(*) FROM Favorites WHERE user_id = ? AND item_id = ?";
        try(ConnectedStatement cs = prepareStatement(sql)) {
            cs.statement.setInt(1, userId);
            cs.statement.setInt(2, itemId);
            ResultSet resultSet = cs.statement.executeQuery();

            //as the query returns count - 1 if favorite and 0 if not
            return resultSet.next() && resultSet.getInt(1) > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static List<AuctionItem> getFavoriteItems(int userId) {
        List<AuctionItem> items = new ArrayList<>();
        // Join items and favorites tables to get the full item details
        String sql = "SELECT i.* FROM Items i JOIN Favorites f ON i.item_id = f.item_id WHERE f.user_id = ?";

        try (ConnectedStatement cs = prepareStatement(sql)) {
            cs.statement.setInt(1, userId);
            ResultSet rs = cs.statement.executeQuery();
            while (rs.next()) {
                items.add(mapResultSetToAuctionItem(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    private static AuctionItem mapResultSetToAuctionItem(ResultSet rs) throws SQLException {
        return new AuctionItem(
                rs.getInt("item_id"),
                rs.getString("name"),
                rs.getDouble("current_price"),
                rs.getInt("time_left"),
                rs.getString("category")
        );
    }

    private static ConnectedStatement prepareStatement(String sql) throws SQLException {
        return new ConnectedStatement(sql);
    }

    private static class ConnectedStatement implements AutoCloseable {
        private final Connection connection;
        private final PreparedStatement statement;

        ConnectedStatement(String sql) throws SQLException {
            this.connection = DatabaseManager.getConnection();
            try {
                this.statement = connection.prepareStatement(sql);
            } catch (SQLException e) {
                connection.close();
                throw e;
            }
        }

        @Override
        public void close() throws SQLException {
            try {
                statement.close();
            } finally {
                connection.close();
            }
        }
    }
}
