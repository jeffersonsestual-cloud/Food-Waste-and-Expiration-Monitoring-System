package com.scc.dao;

import com.scc.model.ActivityLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDao {

    // Saves one audit entry (parameterized PreparedStatement)
    public void logAction(int userId, String actionType, String description) {
        // localtime: SQLite's CURRENT_TIMESTAMP is UTC, so store the computer's local time instead.
        String sql = "INSERT INTO activity_logs (user_id, action_type, description, timestamp) "
                + "VALUES (?, ?, ?, datetime('now', 'localtime'))";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, actionType);
            ps.setString(3, description);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving audit log: " + e.getMessage());
        }
    }

    // All logs, newest first
    public List<ActivityLog> getAllLogs() {
        String sql = "SELECT id, user_id, action_type, description, timestamp "
                + "FROM activity_logs ORDER BY timestamp DESC, id DESC";
        List<ActivityLog> logs = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                logs.add(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error loading audit logs: " + e.getMessage());
        }
        return logs;
    }

    // Logs of one user, newest first
    public List<ActivityLog> getLogsByUserId(int userId) {
        String sql = "SELECT id, user_id, action_type, description, timestamp "
                + "FROM activity_logs WHERE user_id = ? ORDER BY timestamp DESC, id DESC";
        List<ActivityLog> logs = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    logs.add(map(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading audit logs: " + e.getMessage());
        }
        return logs;
    }

    // Login/logout history with who (username + role) and when.
    // Each row: {timestamp, username, role, action_type}
    public List<String[]> getLoginHistory() {
        String sql = "SELECT l.timestamp, u.username, u.role, l.action_type "
                + "FROM activity_logs l LEFT JOIN users u ON u.user_id = l.user_id "
                + "WHERE l.action_type IN ('LOGIN', 'LOGOUT') "
                + "ORDER BY l.timestamp DESC, l.id DESC";
        List<String[]> rows = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new String[]{
                        rs.getString("timestamp"),
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getString("action_type")});
            }
        } catch (SQLException e) {
            System.err.println("Error loading login history: " + e.getMessage());
        }
        return rows;
    }

    private ActivityLog map(ResultSet rs) throws SQLException {
        return new ActivityLog(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getString("action_type"),
                rs.getString("description"),
                rs.getString("timestamp"));
    }
}