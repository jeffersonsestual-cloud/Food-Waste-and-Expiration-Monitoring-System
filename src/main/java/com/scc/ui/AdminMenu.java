package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.FoodItemDao;
import com.scc.dao.UserDao;
import com.scc.model.ActivityLog;
import com.scc.model.User;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private final Scanner sc;
    private final UserDao userdao;
    private final ActivityLogDao activityLogDao = new ActivityLogDao();
    private final FoodItemDao foodItemDao = new FoodItemDao();

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
            System.out.println("6. Logout");
            System.out.print("Enter choice: ");

            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> manageUsers(admin);
                case "2" -> manageFoodProducts(admin);
                case "3" -> manageFoodWasteRecords();
                case "4" -> expirationMonitoring();
                case "5" -> generateReports();
                case "6" -> {
                    System.out.println(" -> You have been logged out. Goodbye, " + admin.name() + "!");
                    running = false;
                }
                default -> System.out.println(" -> [ERROR] Invalid choice! 1 to 6 only.");
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
                    "\n[5] Update User(ROLE/STATUS)\n[6] View System Audit Logs\n[0] Back");
            System.out.print("Enter Choice: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> addUser(admin);
                case "2" -> viewAllUser();
                case "3" -> viewPendingUser();
                case "4" -> approvedUser(admin);
                case "5" -> updateUser(admin);
                case "6" -> viewSystemAuditLogs();
                case "0" -> {
                    return;
                }
                default -> System.out.println("Invalid choice! 0 to 6 only.");
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

    // ---------- [1] ADD USER ----------

    private void addUser(User admin) {
        System.out.println("\n=== ADD NEW USER (0 to cancel) ===");

        String name = readNonEmpty("Enter Name: ");
        if (name == null) {
            System.out.println(" -> Operation Cancelled.");
            return;
        }

        String username;
        while (true) {
            username = readNonEmpty("Enter Username (with @gmail.com): ");
            if (username == null) {
                System.out.println(" -> Operation Cancelled.");
                return;
            }
            username = username.toLowerCase();

            // same rule as login/register, so the new user is able to log in
            if (!username.endsWith("@gmail.com") || username.length() <= "@gmail.com".length() || username.contains(" ")) {
                System.out.println(" -> [ERROR] Enter a valid Gmail without spaces (example: lito@gmail.com).");
            } else if (userdao.usernameExists(username)) {
                System.out.println(" -> [ERROR] Username already exists. Try another.");
            } else {
                break;
            }
        }

        String password;
        while (true) {
            password = readNonEmpty("Enter Password (min 5 characters): ");
            if (password == null) {
                System.out.println(" -> Operation Cancelled.");
                return;
            }
            if (password.length() < 5) {
                System.out.println(" -> [ERROR] Password must be at least 5 characters.");
            } else {
                break;
            }
        }

        String role = readvalidRole();
        if (role == null) {
            System.out.println(" -> Operation Cancelled.");
            return;
        }

        String status = readValidStatus();
        if (status == null) {
            System.out.println(" -> Operation Cancelled.");
            return;
        }

        if (userdao.addUser(admin.user_id(), name, username, password, role, status)) {
            System.out.println(" -> Success! " + username + " (" + role + ") has been added as " + status + ".");
        } else {
            System.out.println(" -> [ERROR] Failed to add user.");
        }
    }

    // ---------- [2] VIEW ALL USERS ----------

    private void viewAllUser() {
        displayUserTable(userdao.getAllUsers(), " ALL REGISTERED USER");
    }

    // ---------- [3] VIEW PENDING USERS ----------

    private void viewPendingUser() {
        displayUserTable(userdao.getPendingUsers(), " PENDING APPROVALS ");
    }

    // ---------- [4] APPROVE USER ----------

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

        if (userdao.approveUser(admin.user_id(), target.user_id())) {
            System.out.println(" -> Success! " + target.username() + " (" + target.role() + ") has been approved.");
        } else {
            System.out.println(" -> [ERROR] Approval failed. Please try again.");
        }
    }

    // ---------- [5] UPDATE USER (ROLE/STATUS) ----------

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

        if (userdao.updateUserAndStatus(currentAdmin.user_id(), userId, newRole, newStatus)) {
            System.out.println(" -> Success! " + target.username() + " is now " + newRole + " (" + newStatus + ").");
        }
    }

    // ---------- [6] VIEW SYSTEM AUDIT LOGS ----------

    private void viewSystemAuditLogs() {
        while (true) {
            System.out.println("\n===============================");
            System.out.println("      SYSTEM AUDIT LOGS");
            System.out.println("===============================");
            System.out.println("[1] View All System Logs\n[2] Filter Logs by User ID\n[0] Back");
            System.out.print("Enter Choice: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> displayLogTable(activityLogDao.getAllLogs(), " ALL SYSTEM LOGS ");
                case "2" -> filterLogsByUser();
                case "0" -> {
                    return;
                }
                default -> System.out.println("Invalid choice! 0 to 2 only.");
            }
        }
    }

    private void filterLogsByUser() {
        List<User> users = userdao.getAllUsers();
        displayUserTable(users, " SELECT USER ");

        if (users.isEmpty()) {
            return;
        }

        int userId = readInput("Enter User ID to filter logs (0 to cancel): ");

        if (userId == 0) {
            System.out.println(" -> Cancelled.");
            return;
        }

        if (findInList(users, userId) == null) {
            System.out.println(" -> [ERROR] User ID not found");
            return;
        }

        displayLogTable(activityLogDao.getLogsByUserId(userId), " LOGS OF USER ID " + userId + " ");
    }

    // Required console format: %-5s | %-8s | %-18s | %-35s | %-20s
    // Long descriptions continue on the next line instead of being cut off.
    private void displayLogTable(List<ActivityLog> logs, String title) {
        System.out.println("\n=========================================== " + title + " ===========================================");

        if (logs == null || logs.isEmpty()) {
            System.out.println("\n-> No Records.");
            return;
        }

        String format = "%-5s | %-8s | %-18s | %-35s | %-20s%n";
        String line = "-".repeat(98);

        System.out.println();
        System.out.printf(format, "ID", "User ID", "Action", "Description", "Timestamp");
        System.out.println(line);

        for (ActivityLog log : logs) {
            List<String> descLines = wrap(log.description(), 35);

            for (int i = 0; i < descLines.size(); i++) {
                if (i == 0) {
                    System.out.printf(format,
                            log.id(),
                            log.userId(),
                            cut(log.actionType(), 18),
                            descLines.get(i),
                            cut(log.timestamp(), 20));
                } else {
                    System.out.printf(format, "", "", "", descLines.get(i), "");
                }
            }
        }

        System.out.println(line);
        System.out.println(" Total: " + logs.size() + " record(s)");
    }

    // ================= MANAGE FOOD PRODUCTS =================

    private void manageFoodProducts(User admin) {
        while (true) {
            System.out.println("\n===============================");
            System.out.println("      MANAGE FOOD PRODUCTS");
            System.out.println("===============================");
            System.out.println("[1] Add Food Item (Stock Intake)\n[2] View All Food Items\n[3] Update Item Status\n[0] Back");
            System.out.print("Enter Choice: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> addFoodItem(admin);
                case "2" -> displayFoodTable(foodItemDao.getItemRows(), " ALL FOOD ITEMS ");
                case "3" -> updateFoodStatus(admin);
                case "0" -> {
                    return;
                }
                default -> System.out.println("Invalid choice! 0 to 3 only.");
            }
        }
    }

    private void addFoodItem(User admin) {
        System.out.println("\n=== ADD FOOD ITEM (0 to cancel) ===");

        String name = readNonEmpty("Enter Item Name: ");
        if (name == null) {
            System.out.println(" -> Operation Cancelled.");
            return;
        }

        int quantity;
        while (true) {
            quantity = readInput("Enter Quantity (0 to cancel): ");
            if (quantity < 0) {
                System.out.println(" -> [ERROR] Quantity cannot be negative.");
            } else {
                break;
            }
        }
        if (quantity == 0) {
            System.out.println(" -> Operation Cancelled.");
            return;
        }

        String expiry = readExpiryDate();
        if (expiry == null) {
            System.out.println(" -> Operation Cancelled.");
            return;
        }

        if (foodItemDao.addItem(admin.user_id(), name, quantity, expiry)) {
            System.out.println(" -> Success! " + quantity + " " + name + " added (Expiry: " + expiry + ").");
        } else {
            System.out.println(" -> [ERROR] Failed to add food item.");
        }
    }

    private void updateFoodStatus(User admin) {
        List<String[]> rows = foodItemDao.getItemRows();
        displayFoodTable(rows, " SELECT ITEM TO UPDATE ");

        if (rows.isEmpty()) {
            return;
        }

        int itemId = readInput("Enter Item ID to update (0 to cancel): ");

        if (itemId == 0) {
            System.out.println(" -> Operation Cancelled.");
            return;
        }

        String[] target = null;
        for (String[] row : rows) {
            if (row[0].equals(String.valueOf(itemId))) {
                target = row;
                break;
            }
        }

        if (target == null) {
            System.out.println(" -> [ERROR] Item ID not found");
            return;
        }

        String newStatus = readItemStatus();
        if (newStatus == null) {
            System.out.println(" -> Operation Cancelled.");
            return;
        }

        if (newStatus.equals(target[4])) {
            System.out.println(" -> No change. " + target[1] + " is already " + newStatus + ".");
            return;
        }

        if (foodItemDao.updateStatus(admin.user_id(), itemId, newStatus)) {
            System.out.println(" -> Success! " + target[1] + " status changed from " + target[4] + " to " + newStatus + ".");
        } else {
            System.out.println(" -> [ERROR] Failed to update item status.");
        }
    }

    private void displayFoodTable(List<String[]> rows, String title) {
        System.out.println("\n====================================== " + title + " ======================================");

        if (rows == null || rows.isEmpty()) {
            System.out.println("\n-> No Records.");
            return;
        }

        String border = "+------+--------------------------+----------+--------------+--------------+";
        String format = "| %-4s | %-24s | %-8s | %-12s | %-12s |\n";

        System.out.println();
        System.out.println(border);
        System.out.printf(format, "ID", "Name", "Qty", "Expiry Date", "Status");
        System.out.println(border);

        for (String[] r : rows) {
            System.out.printf(format, r[0], cut(r[1], 24), r[2], r[3], r[4]);
        }

        System.out.println(border);
    }

    // ================= HELPERS =================

    private User findInList(List<User> users, int userId) {
        for (User u : users) {
            if (u.user_id() == userId) return u;
        }
        return null;
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

    // Shows a numbered list so the admin only types 1-4 (0 to cancel)
    private String readItemStatus() {
        while (true) {
            System.out.println("\n-- Select New Status --");
            System.out.println("[1] FRESH");
            System.out.println("[2] NEAR_EXPIRY");
            System.out.println("[3] EXPIRED");
            System.out.println("[4] DISPOSED");
            System.out.println("[0] Cancel");
            System.out.print("Enter Choice: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> {
                    return "FRESH";
                }
                case "2" -> {
                    return "NEAR_EXPIRY";
                }
                case "3" -> {
                    return "EXPIRED";
                }
                case "4" -> {
                    return "DISPOSED";
                }
                case "0" -> {
                    return null;
                }
                default -> System.out.println(" -> [ERROR] Invalid choice! 0 to 4 only.");
            }
        }
    }

    private String readExpiryDate() {
        while (true) {
            System.out.print("Enter Expiry Date (YYYY-MM-DD, 0 to cancel): ");
            String input = sc.nextLine().trim();

            if (input.equals("0")) {
                return null;
            }

            try {
                return LocalDate.parse(input).toString();
            } catch (DateTimeParseException e) {
                System.out.println(" -> [ERROR] Invalid date. Use YYYY-MM-DD (example: 2026-12-31).");
            }
        }
    }

    private int readInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println(" -> [ERROR] Please enter a valid number.");
            }
        }
    }

    // Returns null if the admin types 0, and keeps asking while the input is empty
    private String readNonEmpty(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();

            if (input.equals("0")) {
                return null;
            }
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println(" -> [ERROR] This field cannot be empty.");
        }
    }

    // Cuts long text so the table columns stay aligned
    private String cut(String text, int max) {
        if (text == null) return "";
        return text.length() <= max ? text : text.substring(0, max - 3) + "...";
    }

    // Splits text into lines of at most `width` characters, breaking at spaces
    private List<String> wrap(String text, int width) {
        List<String> lines = new ArrayList<>();

        if (text == null || text.trim().isEmpty()) {
            lines.add("");
            return lines;
        }

        StringBuilder current = new StringBuilder();

        for (String word : text.trim().split("\\s+")) {
            while (word.length() > width) {
                if (current.length() > 0) {
                    lines.add(current.toString());
                    current.setLength(0);
                }
                lines.add(word.substring(0, width));
                word = word.substring(width);
            }

            if (current.length() == 0) {
                current.append(word);
            } else if (current.length() + 1 + word.length() <= width) {
                current.append(' ').append(word);
            } else {
                lines.add(current.toString());
                current.setLength(0);
                current.append(word);
            }
        }

        if (current.length() > 0) {
            lines.add(current.toString());
        }
        return lines;
    }

    // ================= OTHER ADMIN FUNCTIONS (placeholders) =================

    private void manageFoodWasteRecords() {
        System.out.println("\n=== MANAGE FOOD WASTE RECORDS (soon) ===");
    }

    private void expirationMonitoring() {
        System.out.println("\n=== EXPIRATION MONITORING (soon) ===");
    }

    private void generateReports() {
        System.out.println("\n=== GENERATE REPORTS (soon) ===");
    }
}