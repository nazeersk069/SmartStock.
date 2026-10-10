package com.smartstock.service;

import com.smartstock.dao.ProductDAO;
import com.smartstock.dao.SaleDAO;
import com.smartstock.model.Product;
import com.smartstock.model.Sale;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class ReportService {

    private static final String REPORT_DIR = "reports";
    private final ProductDAO productDAO = new ProductDAO();
    private final SaleDAO saleDAO = new SaleDAO();

    // Products at or below their reorder level
    public List<Product> getLowStockProducts() throws SQLException {
        return productDAO.getAllProducts().stream()
                .filter(p -> p.getQuantity() <= p.getReorderLevel())
                .collect(Collectors.toList());
    }

    public String exportProducts() throws SQLException, IOException {
        List<Product> products = productDAO.getAllProducts();
        File file = newReportFile("products");
        try (PrintWriter out = new PrintWriter(file, StandardCharsets.UTF_8)) {
            out.println("ID,Name,Category,Price,Quantity,ReorderLevel,Status");
            for (Product p : products) {
                String status = p.getQuantity() <= p.getReorderLevel() ? "LOW STOCK" : "OK";
                out.println(p.getProductId() + "," + csv(p.getName()) + "," + csv(p.getCategory())
                        + "," + p.getPrice() + "," + p.getQuantity() + "," + p.getReorderLevel()
                        + "," + status);
            }
        }
        return file.getAbsolutePath();
    }

    public String exportSales() throws SQLException, IOException {
        List<Sale> sales = saleDAO.getAllSales();
        File file = newReportFile("sales");
        try (PrintWriter out = new PrintWriter(file, StandardCharsets.UTF_8)) {
            out.println("SaleID,Product,SoldBy,Quantity,Total,Date");
            for (Sale s : sales) {
                out.println(s.getSaleId() + "," + csv(s.getProductName()) + "," + csv(s.getUsername())
                        + "," + s.getQuantity() + "," + s.getTotalAmount() + "," + s.getSaleDate());
            }
        }
        return file.getAbsolutePath();
    }

    // Creates reports/<name>_<timestamp>.csv
    private File newReportFile(String name) throws IOException {
        File dir = new File(REPORT_DIR);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Could not create folder: " + dir.getAbsolutePath());
        }
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return new File(dir, name + "_" + time + ".csv");
    }

    // Wrap text in quotes so commas inside names don't break the CSV
    private String csv(String value) {
        if (value == null) return "";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
