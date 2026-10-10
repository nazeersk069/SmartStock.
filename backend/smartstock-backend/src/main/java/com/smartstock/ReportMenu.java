package com.smartstock;

import com.smartstock.model.Product;
import com.smartstock.service.ReportService;

import java.util.List;
import java.util.Scanner;

public class ReportMenu {

    private final Scanner sc;
    private final ReportService reports = new ReportService();

    public ReportMenu(Scanner sc) {
        this.sc = sc;
    }

    public void show() {
        while (true) {
            System.out.println("\n--- Reports ---");
            System.out.println("1. Low stock report");
            System.out.println("2. Export products to CSV");
            System.out.println("3. Export sales to CSV");
            System.out.println("4. Back");
            System.out.print("Choose: ");
            String choice = sc.nextLine();

            try {
                switch (choice) {
                    case "1" -> lowStock();
                    case "2" -> System.out.println("Saved: " + reports.exportProducts());
                    case "3" -> System.out.println("Saved: " + reports.exportSales());
                    case "4" -> { return; }
                    default -> System.out.println("Invalid choice.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void lowStock() throws Exception {
        List<Product> low = reports.getLowStockProducts();
        if (low.isEmpty()) {
            System.out.println("All products are well stocked.");
            return;
        }
        System.out.println("\n!! LOW STOCK ALERT !!");
        System.out.printf("%-5s %-20s %-6s %s%n", "ID", "Name", "Qty", "Reorder at");
        for (Product p : low) {
            System.out.printf("%-5d %-20s %-6d %d%n",
                    p.getProductId(), p.getName(), p.getQuantity(), p.getReorderLevel());
        }
    }
}
