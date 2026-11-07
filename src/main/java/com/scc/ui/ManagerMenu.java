package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.Scanner;

public class ManagerMenu {

    private final Scanner sc;
    private final UserDao userdao;

    public ManagerMenu(UserDao userdao, Scanner sc) {
        this.sc = sc;
        this.userdao = userdao;
    }

    // ================= MAIN MANAGER MENU =================

    public void display(User manager) {
        boolean running = true;

        while (running) {
            System.out.println("\n===== MANAGER MENU =====");
            System.out.println("1. View Food Products");
            System.out.println("2. View Food Waste Records");
            System.out.println("3. Expiration Monitoring");
            System.out.println("4. Generate Reports");
            System.out.println("5. Logout");
            System.out.print("Enter choice: ");

            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> viewFoodProducts();
                case "2" -> viewFoodWasteRecords();
                case "3" -> expirationMonitoring();
                case "4" -> generateReports();
                case "5" -> {
                    System.out.println(" -> You have been logged out. Goodbye, " + manager.name() + "!");
                    running = false;
                }
                default -> System.out.println(" -> [ERROR] Invalid choice! 1 to 5 only.");
            }
        }
    }

    // ================= MANAGER FUNCTIONS (placeholders) =================

    private void viewFoodProducts() {
        System.out.println("\n=== VIEW FOOD PRODUCTS (soon) ===");
    }

    private void viewFoodWasteRecords() {
        System.out.println("\n=== VIEW FOOD WASTE RECORDS (soon) ===");
    }

    private void expirationMonitoring() {
        System.out.println("\n=== EXPIRATION MONITORING (soon) ===");
    }

    private void generateReports() {
        System.out.println("\n=== GENERATE REPORTS (soon) ===");
    }
}
