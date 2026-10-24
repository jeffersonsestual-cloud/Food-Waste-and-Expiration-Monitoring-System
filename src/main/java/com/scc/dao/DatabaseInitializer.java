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

        String createActivityLog = "CREATE TABLE IF NOT EXISTS activity_log ("
                + "log_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "user_id INTEGER NOT NULL, "
                + "action TEXT NOT NULL, "
                + "details TEXT, "
                + "created_at TEXT DEFAULT CURRENT_TIMESTAMP, "
                + "FOREIGN KEY (user_id) REFERENCES users(user_id))";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createUsers);
            stmt.execute(createActivityLog);
        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
        }

        new UserDao();
        System.out.println(" -> Database initializer verified.");
    }
}