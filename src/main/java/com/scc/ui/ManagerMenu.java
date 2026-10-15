package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.Scanner;

public class ManagerMenu {
    private final UserDao userDao;
    private final Scanner scanner;

    public ManagerMenu(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
    }

    public void display(User manager) {
        boolean running = true;

        while (running) {
            System.out.println();
            System.out.println("========== MANAGER MENU ==========");
            System.out.println("[1] View Food Products");
            System.out.println("[2] View Near Expiration / Expired Products");
            System.out.println("[3] View Food Waste Records");
            System.out.println("[4] Update Product Status");
            System.out.println("[5] Food Inventory and Expiration Report");
            System.out.println("[6] View User Activity Log");
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
                    comingSoon();
                    break;
                case "0":
                    System.out.println("Logging out... Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a number from 0 to 6.");
            }
        }
    }

    private void comingSoon() {
        System.out.println(">> Coming soon.");
    }
}


