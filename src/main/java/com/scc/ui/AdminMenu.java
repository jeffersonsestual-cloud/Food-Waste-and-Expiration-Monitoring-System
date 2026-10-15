package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.Scanner;

public class AdminMenu {
    private final UserDao userDao;
    private final Scanner scanner;

    public AdminMenu(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
    }

    public void display(User admin) {
        boolean running = true;

        while (running) {
            System.out.println();
            System.out.println("========== ADMIN MENU ==========");
            System.out.println("[1] Manage Users");
            System.out.println("[2] Manage Food Products");
            System.out.println("[3] Record Food Waste");
            System.out.println("[4] View Near Expiration / Expired Products");
            System.out.println("[5] View Food Waste Records");
            System.out.println("[6] Update Product Status");
            System.out.println("[7] Deactivate Product / Record");
            System.out.println("[8] Reports (Food Inventory and Expiration Report)");
            System.out.println("[9] View User Activity Log");
            System.out.println("[0] Logout");
            System.out.print("Enter choice: ");

            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                case "2":
                case "3":
                case "4":
                case "5":
                case "6":
                case "7":
                case "8":
                case "9":
                    comingSoon();
                    break;
                case "0":
                    System.out.println("Logging out... Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a number from 0 to 9.");
            }
        }
    }

    private void comingSoon() {
        System.out.println(">> Coming soon.");
    }
}