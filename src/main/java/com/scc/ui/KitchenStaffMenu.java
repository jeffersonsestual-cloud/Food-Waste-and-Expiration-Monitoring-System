package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.Scanner;

public class KitchenStaffMenu {
    private final UserDao userDao;
    private final Scanner scanner;

    public KitchenStaffMenu(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
    }

    public void display(User staff) {
        boolean running = true;

        while (running) {
            System.out.println();
            System.out.println("======== KITCHEN STAFF MENU ========");
            System.out.println("[1] Add New Food Product");
            System.out.println("[2] View Food Products");
            System.out.println("[3] Update Product Quantity / Information");
            System.out.println("[4] View Near Expiration / Expired Products");
            System.out.println("[5] Record Food Waste");
            System.out.println("[0] Logout");
            System.out.print("Enter choice: ");

            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                case "2":
                case "3":
                case "4":
                case "5":
                    comingSoon();
                    break;
                case "0":
                    System.out.println("Logging out... Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a number from 0 to 5.");
            }
        }
    }

    private void comingSoon() {
        System.out.println(">> Coming soon.");
    }
}