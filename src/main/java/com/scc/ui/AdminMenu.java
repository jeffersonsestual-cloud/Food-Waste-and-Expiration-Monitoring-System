package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private final Scanner sc;
    private final UserDao userdao;

    public AdminMenu(UserDao userdao, Scanner sc) {
        this.sc = sc;
        this.userdao = userdao;
    }

    // ================= MAIN ADMIN MENU =================

    public void display(User admin) {
        boolean running = true;

        while (running) {
            System.out.println("\n===== ADMIN MENU =====");
            System.out.println("1. Manage Users");
            System.out.println("2. Manage Food Products");
            System.out.println("3. Manage Food Waste Records");
            System.out.println("4. Expiration Monitoring");
            System.out.println("5. Generate Reports");
            System.out.println("6. View Activity Logs");
            System.out.println("7. Logout");
            System.out.print("Enter choice: ");

            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> manageUsers(admin);
                case "2" -> manageFoodProducts();
                case "3" -> manageFoodWasteRecords();
                case "4" -> expirationMonitoring();
                case "5" -> generateReports();
                case "6" -> viewActivityLogs();
                case "7" -> {
                    System.out.println(" -> You have been logged out. Goodbye, " + admin.name() + "!");
                    running = false;
                }
                default -> System.out.println(" -> [ERROR] Invalid choice! 1 to 7 only.");
            }
        }
    }

    // ================= MANAGE USERS =================

    private void manageUsers(User admin) {
        while (true) {
            System.out.println("\n===============================");
            System.out.println("         MANAGE USERS");
            System.out.println("===============================");
            System.out.println("[1] Add User\n[2] View All Users\n[3] View Pending User\n[4] Approved User" +
                    "\n[5] Update User(ROLE/STATUS) \n[0] Back");
            System.out.print("Enter Choice: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> System.out.println("Add User (soon)");
                case "2" -> viewAllUser();
                case "3" -> viewPendingUser();
                case "4" -> approvedUser(admin);
                case "5" -> updateUser(admin);
                case "0" -> {
                    return;
                }
                default -> System.out.println("Invalid choice! 0 to 5 only.");
            }
        }
    }

    private void displayUserTable(List<User> users, String title) {
        System.out.println("\n====================================== " + title + " ======================================");

        if (users == null || users.isEmpty()) {
            System.out.println("\n-> No Records.");
            return;
        }

        String border = "+------+--------------------+----------------------+------------------+------------+";
        String format = "| %-4s | %-18s | %-20s | %-16s | %-10s |\n";

        System.out.println();
        System.out.println(border);
        System.out.printf(format, "ID", "Name", "Username", "Role", "Status");
        System.out.println(border);

        for (User u : users) {
            System.out.printf(format, u.user_id(), u.name(), u.username(), u.role(), u.status());
        }

        System.out.println(border);
    }

    private void viewAllUser() {
        displayUserTable(userdao.getAllUsers(), " ALL REGISTERED USER");
    }

    private void viewPendingUser() {
        displayUserTable(userdao.getPendingUsers(), " PENDING APPROVALS ");
    }

    private void approvedUser(User admin) {
        List<User> pendingUsers = userdao.getPendingUsers();
        displayUserTable(pendingUsers, " PENDING APPROVALS ");

        if (pendingUsers.isEmpty()) {
            return;
        }

        int userId = readInput("Enter User ID to approved (0 to cancel): ");

        if (userId == 0) {
            System.out.println("-> Cancelled.");
            return;
        }

        User target = findInList(pendingUsers, userId);

        if (target == null) {
            System.out.println(" -> [ERROR] User ID not found");
            return;
        }

        if (userdao.approveUser(userId)) {
            userdao.logActivity(admin.user_id(), "STATUS_CHANGE",
                    admin.username() + " approved " + target.username());
            System.out.println(" -> Success! " + target.username() + " (" + target.role() + ") has been approved.");
        } else {
            System.out.println(" -> [ERROR] Approval failed. Please try again.");
        }
    }

    private User findInList(List<User> users, int userId) {
        for (User u : users) {
            if (u.user_id() == userId) return u;
        }
        return null;
    }

    private void updateUser(User currentAdmin) {
        List<User> users = userdao.getAllUsers();
        displayUserTable(users, "SELECT USER TO UPDATE");

        if (users.isEmpty()) {
            return;
        }

        int userId = readInput("Enter user ID to update(0 to cancel): ");

        if (userId == 0) {
            System.out.println(" -> Operation Cancelled");
            return;
        }

        User target = findInList(users, userId);

        if (target == null) {
            System.out.println(" -> [ERROR] User ID not found");
            return;
        }

        if (userId == currentAdmin.user_id()) {
            System.out.println(" -> [WARNING] Modifying current active Admin is Restricted.");
            return;
        }

        String newRole = readvalidRole();
        if (newRole == null) {
            System.out.println(" -> Operation Cancelled.");
            return;
        }

        String newStatus = readValidStatus();
        if (newStatus == null) {
            System.out.println(" -> Operation Cancelled.");
            return;
        }

        userdao.updateUserAndStatus(userId, newRole, newStatus);
        userdao.logActivity(currentAdmin.user_id(), "STATUS_CHANGE",
                currentAdmin.username() + " updated " + target.username()
                        + " to " + newRole + " / " + newStatus);
        System.out.println(" -> Success! " + target.username() + " is now " + newRole + " (" + newStatus + ").");
    }

    private String readvalidRole() {
        while (true) {
            System.out.print("Enter Role (ADMIN/MANAGER/KITCHEN STAFF [0 to cancel]): ");
            String role = sc.nextLine().trim().toUpperCase();

            if (role.equals("0") || role.equals("CANCEL")) {
                return null;
            }

            if (role.equals("ADMIN") || role.equals("MANAGER") || role.equals("KITCHEN STAFF")) {
                return role;
            }

            System.out.println(" -> [ERROR] Invalid Role");
        }
    }

    private String readValidStatus() {
        while (true) {
            System.out.print("Enter Status (APPROVED/PENDING [0 to cancel]): ");
            String status = sc.nextLine().trim().toUpperCase();

            if (status.equals("0") || status.equals("CANCEL")) {
                return null;
            }

            if (status.equals("APPROVED") || status.equals("PENDING")) {
                return status;
            }

            System.out.println(" -> [ERROR] Invalid Status. Must be APPROVED or PENDING.");
        }
    }

    private int readInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println(" -> [ERROR] Please enter a valid numeric ID.");
            }
        }
    }

    // ================= OTHER ADMIN FUNCTIONS (placeholders) =================

    private void manageFoodProducts() {
        System.out.println("\n=== MANAGE FOOD PRODUCTS (soon) ===");
    }

    private void manageFoodWasteRecords() {
        System.out.println("\n=== MANAGE FOOD WASTE RECORDS (soon) ===");
    }

    private void expirationMonitoring() {
        System.out.println("\n=== EXPIRATION MONITORING (soon) ===");
    }

    private void generateReports() {
        System.out.println("\n=== GENERATE REPORTS (soon) ===");
    }

    private void viewActivityLogs() {
        System.out.println("\n=== ACTIVITY LOGS (soon) ===");
    }
}