package com.smartstock;

import com.smartstock.model.User;
import com.smartstock.service.AuthService;

import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        AuthService auth = new AuthService();
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n=== SmartStock ===");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choose: ");
            String choice = sc.nextLine();

            try {
                if (choice.equals("1")) {
                    System.out.print("Username: ");
                    String u = sc.nextLine();
                    System.out.print("Password (min 6 chars): ");
                    String p = sc.nextLine();
                    boolean ok = auth.register(u, p, "STAFF");
                    System.out.println(ok ? "Registered successfully!" : "Registration failed (username taken or password too short).");
                } else if (choice.equals("2")) {
                    System.out.print("Username: ");
                    String u = sc.nextLine();
                    System.out.print("Password: ");
                    String p = sc.nextLine();
                    User user = auth.login(u, p);
                    if (user != null) {
                        System.out.println("Welcome, " + user.getUsername() + " (" + user.getRole() + ")");
                        new ProductMenu(sc).show(user);
                    } else {
                        System.out.println("Invalid username or password.");
                    }
                } else if (choice.equals("3")) {
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        sc.close();
    }
}
