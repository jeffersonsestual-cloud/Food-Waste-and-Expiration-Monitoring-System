package com.scc.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    public static void initializer(){
        String createUsers = "CREATE TABLE IF NOT EXISTS users ("
                + "user_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "username TEXT NOT NULL UNIQUE, "
                + "password TEXT NOT NULL, "
                + "role TEXT NOT NULL, "
                + "status TEXT NOT NULL DEFAULT 'PENDING')";

        String createActivityLogs = "CREATE TABLE IF NOT EXISTS activity_logs ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "user_id INTEGER NOT NULL, "
                + "action_type TEXT NOT NULL, "
                + "description TEXT NOT NULL, "
                + "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, "
                + "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE)";

        String createFoodItems = "CREATE TABLE IF NOT EXISTS food_items ("
                + "item_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "quantity INTEGER NOT NULL, "
                + "expiry_date TEXT NOT NULL, "
                + "status TEXT NOT NULL DEFAULT 'FRESH', "
                + "added_by INTEGER NOT NULL, "
                + "FOREIGN KEY (added_by) REFERENCES users(user_id))";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createUsers);
            stmt.execute(createActivityLogs);
            stmt.execute(createFoodItems);
        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
        }

        new UserDao();
        System.out.println(" -> Database initializer verified.");
    }
}