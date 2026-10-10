package com.smartstock;

import com.smartstock.dao.ProductDAO;
import com.smartstock.model.Product;
import com.smartstock.model.User;

import java.util.List;
import java.util.Scanner;

public class ProductMenu {

    private final Scanner sc;
    private final ProductDAO dao = new ProductDAO();

    public ProductMenu(Scanner sc) {
        this.sc = sc;
    }

    public void show(User user) {
        while (true) {
            System.out.println("\n--- Products (logged in as " + user.getUsername() + ") ---");
            System.out.println("1. Add product");
            System.out.println("2. View all products");
            System.out.println("3. Update product");
            System.out.println("4. Delete product");
            System.out.println("5. Sales");
            System.out.println("6. Reports");
            System.out.println("7. Logout");
            System.out.print("Choose: ");
            String choice = sc.nextLine();

            try {
                switch (choice) {
                    case "1" -> addProduct();
                    case "2" -> viewProducts();
                    case "3" -> { if (requireAdmin(user)) updateProduct(); }
                     case "4" -> { if (requireAdmin(user)) deleteProduct(); }
                    case "5" -> new SalesMenu(sc).show(user);
                    case "6" -> { if(requireAdmin(user)) new ReportMenu(sc).show(); }
                    case "7" -> { return; }
                    default -> System.out.println("Invalid choice.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
    private boolean requireAdmin(User user) {
    if ("ADMIN".equals(user.getRole())) {
        return true;
    }
    System.out.println("Access denied: this option is for admins only.");
    return false;
    }

    private Product readProduct(int id) {
        System.out.print("Name: ");
        String name = sc.nextLine();
        System.out.print("Category: ");
        String category = sc.nextLine();
        System.out.print("Price: ");
        double price = Double.parseDouble(sc.nextLine());
        System.out.print("Quantity: ");
        int qty = Integer.parseInt(sc.nextLine());
        System.out.print("Reorder level: ");
        int reorder = Integer.parseInt(sc.nextLine());
        return new Product(id, name, category, price, qty, reorder);
    }

    private void addProduct() throws Exception {
        Product p = readProduct(0);
        System.out.println(dao.addProduct(p) ? "Product added!" : "Could not add product.");
    }

    private void viewProducts() throws Exception {
        List<Product> list = dao.getAllProducts();
        if (list.isEmpty()) {
            System.out.println("No products yet.");
            return;
        }
        System.out.printf("%-5s %-20s %-15s %-10s %-6s %s%n",
                "ID", "Name", "Category", "Price", "Qty", "Status");
        for (Product p : list) {
            String status = p.getQuantity() <= p.getReorderLevel() ? "LOW STOCK" : "OK";
            System.out.printf("%-5d %-20s %-15s %-10.2f %-6d %s%n",
                    p.getProductId(), p.getName(), p.getCategory(),
                    p.getPrice(), p.getQuantity(), status);
        }
    }

    private void updateProduct() throws Exception {
        System.out.print("Product ID to update: ");
        int id = Integer.parseInt(sc.nextLine());
        Product p = readProduct(id);
        System.out.println(dao.updateProduct(p) ? "Product updated!" : "Product not found.");
    }

    private void deleteProduct() throws Exception {
        System.out.print("Product ID to delete: ");
        int id = Integer.parseInt(sc.nextLine());
        System.out.println(dao.deleteProduct(id) ? "Product deleted!" : "Product not found.");
    }
}



