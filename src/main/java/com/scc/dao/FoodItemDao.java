package com.scc.dao;

import com.scc.model.FoodItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FoodItemDao {

    private final ActivityLogDao activityLogDao = new ActivityLogDao();

    // #3 Core Business Process: stock intake
    public boolean addItem(int userId, String name, int quantity, String expiryDate) {
        String sql = "INSERT INTO food_items(name, quantity, expiry_date, status, added_by) "
                + "VALUES (?, ?, ?, 'FRESH', ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setInt(2, quantity);
            ps.setString(3, expiryDate);
            ps.setInt(4, userId);

            boolean ok = ps.executeUpdate() > 0;
            if (ok) {
                activityLogDao.logAction(userId, "ITEM_ADDED",
                        "Added " + quantity + " " + name + " (Expiry: " + expiryDate + ")");
            }
            return ok;
        } catch (SQLException e) {
            System.err.println("Error adding item: " + e.getMessage());
            return false;
        }
    }

    // #4 Lifecycle / State Change: status update
    public boolean updateStatus(int userId, int itemId, String newStatus) {
        String selectSql = "SELECT name, status FROM food_items WHERE item_id = ?";
        String updateSql = "UPDATE food_items SET status = ? WHERE item_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {

            String name;
            String oldStatus;
            try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                ps.setInt(1, itemId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println(" -> Item ID not found.");
                        return false;
                    }
                    name = rs.getString("name");
                    oldStatus = rs.getString("status");
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setString(1, newStatus.toUpperCase());
                ps.setInt(2, itemId);
                boolean ok = ps.executeUpdate() > 0;
                if (ok) {
                    // action type kept short so it fits the 18-character audit column
                    activityLogDao.logAction(userId, "ITEM_UPDATED",
                            "Updated Item #" + itemId + " " + name
                                    + " status from " + oldStatus + " to " + newStatus.toUpperCase());
                }
                return ok;
            }
        } catch (SQLException e) {
            System.err.println("Error updating status: " + e.getMessage());
            return false;
        }
    }

    public List<FoodItem> getAllItems() {
        List<FoodItem> list = new ArrayList<>();
        String sql = "SELECT item_id, name, quantity, expiry_date, status FROM food_items ORDER BY expiry_date";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new FoodItem(
                        rs.getInt("item_id"),
                        rs.getString("name"),
                        rs.getInt("quantity"),
                        rs.getString("expiry_date"),
                        rs.getString("status")));
            }
        } catch (SQLException e) {
            System.err.println("Error loading items: " + e.getMessage());
        }
        return list;
    }

    // Same data as getAllItems(), but as plain text rows for printing in a table.
    // Each row: {item_id, name, quantity, expiry_date, status}
    public List<String[]> getItemRows() {
        List<String[]> rows = new ArrayList<>();
        String sql = "SELECT item_id, name, quantity, expiry_date, status FROM food_items ORDER BY expiry_date";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new String[]{
                        String.valueOf(rs.getInt("item_id")),
                        rs.getString("name"),
                        String.valueOf(rs.getInt("quantity")),
                        rs.getString("expiry_date"),
                        rs.getString("status")});
            }
        } catch (SQLException e) {
            System.err.println("Error loading items: " + e.getMessage());
        }
        return rows;
    }
}