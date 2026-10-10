package com.smartstock.model;

import java.sql.Timestamp;

public class Sale {
    private int saleId;
    private String productName;
    private String username;
    private int quantity;
    private double totalAmount;
    private Timestamp saleDate;

    public Sale(int saleId, String productName, String username,
                int quantity, double totalAmount, Timestamp saleDate) {
        this.saleId = saleId;
        this.productName = productName;
        this.username = username;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
        this.saleDate = saleDate;
    }

    public int getSaleId() { return saleId; }
    public String getProductName() { return productName; }
    public String getUsername() { return username; }
    public int getQuantity() { return quantity; }
    public double getTotalAmount() { return totalAmount; }
    public Timestamp getSaleDate() { return saleDate; }
}
