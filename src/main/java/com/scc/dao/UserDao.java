package com.scc.dao;

import com.scc.model.User;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UserDao {

    public UserDao(){

        try(Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement()){

            defaultAdminGeneret(conn);

        }catch(SQLException e){
            System.err.println("Error seeding default admin: " + e.getMessage());
        }

    }

    public void defaultAdminGeneret(Connection conn) {
        String checkSql = "SELECT COUNT(*) FROM users WHERE role = 'ADMIN'";
        String insertSql = "INSERT INTO users(name, username, password, role, status) VALUES('Admin','admin@gmail.com', 'admin123', 'ADMIN','APPROVED')";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.executeUpdate(insertSql);
                System.err.println("---Defult Admin Created (Username: admin | Password: admin123)");

            }
        }catch(SQLException e){
            System.err.println("Error Seeding Default: " + e.getMessage());
        }
    }

    public boolean registerUser( String name, String username, String password, String role){

        String sql = "INSERT INTO users(name, username, password, role, status) VALUES (?, ?, ?, ?, 'PENDING')";

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString( 2, username);
            pstmt.setString(3, password);
            pstmt.setString(4, role.toUpperCase());
            pstmt.executeUpdate();
            System.out.println("----Successful Registration, Wait for ------");
            return true;
        } catch (SQLException e) {
            System.err.println("Failed: "+ e.getMessage());
            return false;
        }
    }

    public boolean usernameExists(String username){
        String sql ="SELECT username FROM users WHERE username = ?";

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setString(1, username);
            try(ResultSet rs = pstmt.executeQuery()){
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("\nDatabase error: " + e.getMessage());
            return true;
        }
    }

    public User login(String username, String password){
        String sql = "SELECT user_id, name, username, password, role, status FROM users WHERE username = ? AND password = ?";

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try(ResultSet rs = pstmt.executeQuery()){
                if(rs.next()){
                    String status = rs.getString("status");
                    if("PENDING".equalsIgnoreCase(status)){
                        System.out.println("-> Account is pending, Waiting for Approval");
                        return null;
                    }
                    return new User(
                            rs.getInt("user_id"),
                            rs.getString("name"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("role"),
                            rs.getString("status")
                    );
                }else {
                    System.out.println(" -> Invalid username or password");
                }
            }
        }catch (SQLException e){
            System.err.println("Error during login " + e.getMessage());
        }
        return null;
    }

    public List<User> getAllUsers(){
        List<User> usersList = new ArrayList<>();
        String sql = "SELECT user_id, name, username, password, role, status FROM users ORDER BY user_id";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)){

            while (rs.next()) {
                usersList.add(new User(
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getString("status")
                ));
            }
        }catch (SQLException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
        return usersList;
    }

    public List<User> getPendingUsers(){
        List<User> usersList = new ArrayList<>();
        String sql = "SELECT user_id, name, username, password, role, status FROM users WHERE status = 'PENDING' ORDER BY user_id";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)){

            while (rs.next()) {
                usersList.add(new User(
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getString("status")
                ));
            }
        }catch (SQLException e) {
            System.err.println("Error loading pending users: " + e.getMessage());
        }
        return usersList;
    }

    public boolean approveUser(int id) {
        String sql = "UPDATE users SET status = 'APPROVED' WHERE user_id = ? AND status = 'PENDING'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error approving user: " + e.getMessage());
            return false;
        }
    }

    public void logActivity(int userId, String action, String details) {
        String sql = "INSERT INTO activity_log(user_id, action, details) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, action);
            pstmt.setString(3, details);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving activity log: " + e.getMessage());
        }
    }
    public boolean updateUserAndStatus (int userId, String newRoles , String newStatus ) {

        String sql = "UPDATE users SET role = ?, status = ? WHERE user_id = ?";
        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newRoles.toUpperCase());
            pstmt.setString(2, newStatus.toUpperCase());
            pstmt.setInt(3, userId);

            int rowsUpDated = pstmt.executeUpdate();
            if (rowsUpDated > 0) {
                System.out.println(" -> User ID: " + userId + "updated successfully!");
                return true;

            } else {
                System.out.println(" -> Users ID not found.");
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error Updating user: " + e.getMessage());
        }
        return false;
    }

}