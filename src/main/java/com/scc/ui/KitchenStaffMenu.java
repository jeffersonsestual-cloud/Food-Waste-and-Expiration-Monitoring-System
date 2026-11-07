package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.Scanner;

public class KitchenStaffMenu {

    private final Scanner sc;

    public KitchenStaffMenu(UserDao userdao, Scanner sc) {
        this.sc = sc;
    }

    // ================= MAIN KITCHEN STAFF MENU =================

    public void display(User staff) {
        boolean running = true;

        while (running) {
            System.out.println("\n===== KITCHEN STAFF MENU =====");
            System.out.println("1. View Food Products");
            System.out.println("2. Record Food Waste");
            System.out.println("3. Expiration Alerts");
            System.out.println("4. Logout");
            System.out.print("Enter choice: ");

            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> viewFoodProducts();
                case "2" -> recordFoodWaste();
                case "3" -> expirationAlerts();
                case "4" -> {
                    System.out.println(" -> You have been logged out. Goodbye, " + staff.name() + "!");
                    running = false;
                }
                default -> System.out.println(" -> [ERROR] Invalid choice! 1 to 4 only.");
            }
        }
    }

    // ================= STAFF FUNCTIONS (placeholders) =================

    private void viewFoodProducts() {
        System.out.println("\n=== VIEW FOOD PRODUCTS (soon) ===");
    }

    private void recordFoodWaste() {
        System.out.println("\n=== RECORD FOOD WASTE (soon) ===");
    }

    private void expirationAlerts() {
        System.out.println("\n=== EXPIRATION ALERTS (soon) ===");
    }
}