package com.smartstock.dao;

import com.smartstock.model.Sale;
import com.smartstock.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SaleDAO {

    // Returns a message describing the result
    public String sellProduct(int productId, int userId, int qty) throws SQLException {
        if (qty <= 0) {
            return "Quantity must be greater than 0.";
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // start transaction
            try {
                // 1. Read current stock and price (lock the row)
                String select = "SELECT quantity, price FROM Products WITH (UPDLOCK) WHERE product_id = ?";
                int stock;
                double price;
                try (PreparedStatement ps = conn.prepareStatement(select)) {
                    ps.setInt(1, productId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return "Product not found.";
                        }
                        stock = rs.getInt("quantity");
                        price = rs.getDouble("price");
                    }
                }

                // 2. Check stock
                if (stock < qty) {
                    conn.rollback();
                    return "Not enough stock. Available: " + stock;
                }

                // 3. Reduce stock
                String update = "UPDATE Products SET quantity = quantity - ? WHERE product_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(update)) {
                    ps.setInt(1, qty);
                    ps.setInt(2, productId);
                    ps.executeUpdate();
                }

                // 4. Record the sale
                String insert = "INSERT INTO Sales (product_id, user_id, quantity, unit_price, total_amount) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insert)) {
                    ps.setInt(1, productId);
                    ps.setInt(2, userId);
                    ps.setInt(3, qty);
                    ps.setDouble(4, price);
                    ps.setDouble(5, price * qty);
                    ps.executeUpdate();
                }

                conn.commit();
                return "Sale recorded! Total: " + String.format("%.2f", price * qty);
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public List<Sale> getAllSales() throws SQLException {
        List<Sale> list = new ArrayList<>();
        String sql = "SELECT s.sale_id, p.name AS product_name, u.username, s.quantity, s.total_amount, s.sale_date "
                   + "FROM Sales s "
                   + "JOIN Products p ON s.product_id = p.product_id "
                   + "JOIN Users u ON s.user_id = u.user_id "
                   + "ORDER BY s.sale_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Sale(
                    rs.getInt("sale_id"),
                    rs.getString("product_name"),
                    rs.getString("username"),
                    rs.getInt("quantity"),
                    rs.getDouble("total_amount"),
                    rs.getTimestamp("sale_date")));
            }
        }
        return list;
    }
}
