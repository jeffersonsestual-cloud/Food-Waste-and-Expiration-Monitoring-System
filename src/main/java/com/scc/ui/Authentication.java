package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.Scanner;

public class Authentication {

    private final Scanner sc;
    private final UserDao userDao;
    private final ActivityLogDao activityLogDao = new ActivityLogDao();

    public Authentication(UserDao userDao, Scanner sc){
        this.userDao = userDao;
        this.sc = sc;
    }

    public void start(){
        while (true) {

            System.out.println("\n========================================");
            System.out.println("        Expiration Monitoring System      ");
            System.out.println("\n========================================");
            System.out.println("[1] Login");
            System.out.println("[2] Register");
            System.out.println("[3] Exit");
            System.out.print("Enter Choice: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    handleLogin();
                    break;
                case "2":
                    registerUser();
                    break;
                case "3":
                    return;
                default:
                    System.out.println("Invalid Choice, Try Again.");
            }

        }
    }

    public void handleLogin(){
        System.out.println("\n    ===== LOG IN =====");

        final int MAX_ATTEMPTS = 3;
        User user = null;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {

            System.out.print("Enter Username: ");
            String username = sc.nextLine().trim().toLowerCase();

            if (username.isEmpty()) {
                System.out.println("Invalid! Username cannot be empty. Attempt " + attempt + " of " + MAX_ATTEMPTS + ".");
                continue;
            }
            if (!username.endsWith("@gmail.com") || username.length() <= "@gmail.com".length() || username.contains(" ")) {
                System.out.println("Invalid! Enter your full Gmail (example: admin@gmail.com). Attempt " + attempt + " of " + MAX_ATTEMPTS + ".");
                continue;
            }

            System.out.print("Enter Password: ");
            String password = sc.nextLine().trim();

            if (password.isEmpty()) {
                System.out.println("Invalid! Password cannot be empty. Attempt " + attempt + " of " + MAX_ATTEMPTS + ".");
                continue;
            }

            user = userDao.login(username, password);

            if (user != null) {
                break;
            }
            System.out.println("Attempt " + attempt + " of " + MAX_ATTEMPTS + ".");
        }

        if (user == null) {
            System.out.println(" -> Too many failed attempts. Returning to main menu.");
            return;
        }

        System.out.println("\n -> Welcome " + user.name() + " | ROLE: " + user.role());

        // AUDIT LOG: successful login
        activityLogDao.logAction(user.user_id(), "LOGIN",
                "User " + user.username() + " (ID: " + user.user_id() + ") logged in.");

        boolean menuShown = true;

        switch (user.role()) {
            case "ADMIN" -> new AdminMenu(userDao, sc).display(user);
            case "MANAGER" -> new ManagerMenu(userDao, sc).display(user);
            case "KITCHEN STAFF" -> new KitchenStaffMenu(userDao, sc).display(user);
            default -> {
                menuShown = false;
                System.out.println(" -> [ERROR] Unknown role: " + user.role());
            }
        }

        // AUDIT LOG: display() returns when the user chooses Logout
        if (menuShown) {
            activityLogDao.logAction(user.user_id(), "LOGOUT",
                    "User " + user.username() + " (ID: " + user.user_id() + ") logged out.");
        }
    }


    public void registerUser(){

        System.out.println("\n    ===== REGISTER =====");

        String full_name;
        while (true) {
            System.out.print("Enter your Fullname: ");
            full_name = sc.nextLine().trim();

            if (full_name.isEmpty()) {
                System.out.println("Invalid! Fullname cannot be empty.");
            } else {
                break;
            }
        }

        String user;
        while (true) {
            System.out.print("Enter username(with @gmail.com): ");
            user = sc.nextLine().trim().toLowerCase();

            if (!user.endsWith("@gmail.com") || user.length() <= "@gmail.com".length() || user.contains(" ")) {
                System.out.println("Invalid! Enter a valid Gmail without spaces (example: jeff@gmail.com).");
            } else if (userDao.usernameExists(user)) {
                System.out.println("Invalid! Username already taken.");
            } else {
                break;
            }
        }

        String pass;
        while (true) {
            System.out.print("Enter Password: ");
            pass = sc.nextLine().trim();

            if (pass.length() < 5) {
                System.out.println("Invalid! Password must be at least 5 characters.");
                continue;
            }

            boolean confirmed = false;
            for (int attempt = 1; attempt <= 3; attempt++) {
                System.out.print("Confirm Password: ");
                String confirm = sc.nextLine().trim();

                if (confirm.equals(pass)) {
                    confirmed = true;
                    break;
                }
                System.out.println("\nPasswords do not match! Attempt " + attempt + " of 3.");
            }

            if (confirmed) {
                break;
            }
            System.out.println("Too many wrong attempts. Register Again.");
            return;
        }

        String role;
        label:
        while (true) {
            System.out.println("\n-- Select Role --");
            System.out.println("[1] ADMIN");
            System.out.println("[2] KITCHEN STAFF");
            System.out.println("[3] MANAGER");
            System.out.print("Enter Choice: ");
            String roleChoice = sc.nextLine().trim();

            switch (roleChoice) {
                case "1":
                    role = "ADMIN";
                    break label;
                case "2":
                    role = "KITCHEN STAFF";
                    break label;
                case "3":
                    role = "MANAGER";
                    break label;
                default:
                    System.out.println("Invalid! Please select 1, 2, or 3 only.");
                    break;
            }
        }

        userDao.registerUser(full_name, user, pass, role);
    }

}