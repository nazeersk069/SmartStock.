package com.smartstock;

import com.smartstock.dao.SaleDAO;
import com.smartstock.model.Sale;
import com.smartstock.model.User;

import java.util.List;
import java.util.Scanner;

public class SalesMenu {

    private final Scanner sc;
    private final SaleDAO dao = new SaleDAO();

    public SalesMenu(Scanner sc) {
        this.sc = sc;
    }

    public void show(User user) {
        while (true) {
            System.out.println("\n--- Sales ---");
            System.out.println("1. Sell product");
            System.out.println("2. View sales history");
            System.out.println("3. Back");
            System.out.print("Choose: ");
            String choice = sc.nextLine();

            try {
                switch (choice) {
                    case "1" -> sell(user);
                    case "2" -> viewSales();
                    case "3" -> { return; }
                    default -> System.out.println("Invalid choice.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void sell(User user) throws Exception {
        System.out.print("Product ID: ");
        int id = Integer.parseInt(sc.nextLine());
        System.out.print("Quantity to sell: ");
        int qty = Integer.parseInt(sc.nextLine());
        System.out.println(dao.sellProduct(id, user.getUserId(), qty));
    }

    private void viewSales() throws Exception {
        List<Sale> list = dao.getAllSales();
        if (list.isEmpty()) {
            System.out.println("No sales yet.");
            return;
        }
        System.out.printf("%-6s %-20s %-12s %-5s %-10s %s%n",
                "ID", "Product", "Sold by", "Qty", "Total", "Date");
        for (Sale s : list) {
            System.out.printf("%-6d %-20s %-12s %-5d %-10.2f %s%n",
                    s.getSaleId(), s.getProductName(), s.getUsername(),
                    s.getQuantity(), s.getTotalAmount(), s.getSaleDate());
        }
    }
}
