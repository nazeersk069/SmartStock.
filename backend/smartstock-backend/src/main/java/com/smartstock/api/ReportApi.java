package com.smartstock.api;

import com.smartstock.dao.ProductDAO;
import com.smartstock.dao.SaleDAO;
import com.smartstock.model.Product;
import com.smartstock.model.Sale;
import com.smartstock.model.User;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ReportApi {

    private static final ProductDAO productDao = new ProductDAO();
    private static final SaleDAO saleDao = new SaleDAO();

    public static void register(HttpServer server) {

        // GET /api/export/products  -> products.csv (admin only)
        server.createContext("/api/export/products", ex -> {
            try {
                if (!isAdmin(ex)) {
                    return;
                }
                StringBuilder sb = new StringBuilder();
                sb.append("ID,Name,Category,Price,Quantity,ReorderLevel,Status\n");
                for (Product p : productDao.getAllProducts()) {
                    String status = p.getQuantity() <= p.getReorderLevel() ? "LOW STOCK" : "OK";
                    sb.append(p.getProductId()).append(',')
                      .append(csv(p.getName())).append(',')
                      .append(csv(p.getCategory())).append(',')
                      .append(p.getPrice()).append(',')
                      .append(p.getQuantity()).append(',')
                      .append(p.getReorderLevel()).append(',')
                      .append(status).append('\n');
                }
                sendCsv(ex, "products.csv", sb.toString());
            } catch (Exception e) {
                ApiUtil.sendJson(ex, 500, Map.of("error", "Server error: " + e.getMessage()));
            }
        });

        // GET /api/export/sales  -> sales.csv (admin only)
        server.createContext("/api/export/sales", ex -> {
            try {
                if (!isAdmin(ex)) {
                    return;
                }
                StringBuilder sb = new StringBuilder();
                sb.append("SaleID,Product,SoldBy,Quantity,Total,Date\n");
                for (Sale s : saleDao.getAllSales()) {
                    String date = String.valueOf(s.getSaleDate());
                    if (date.length() > 19) {
                        date = date.substring(0, 19);
                    }
                    sb.append(s.getSaleId()).append(',')
                      .append(csv(s.getProductName())).append(',')
                      .append(csv(s.getUsername())).append(',')
                      .append(s.getQuantity()).append(',')
                      .append(s.getTotalAmount()).append(',')
                      .append(date).append('\n');
                }
                sendCsv(ex, "sales.csv", sb.toString());
            } catch (Exception e) {
                ApiUtil.sendJson(ex, 500, Map.of("error", "Server error: " + e.getMessage()));
            }
        });
    }

    // Returns true only for a logged-in ADMIN; otherwise sends an error and returns false
    private static boolean isAdmin(HttpExchange ex) throws IOException {
        User user = ApiUtil.getUser(ex);
        if (user == null) {
            ApiUtil.sendJson(ex, 401, Map.of("error", "Please log in first"));
            return false;
        }
        if (!"ADMIN".equals(user.getRole())) {
            ApiUtil.sendJson(ex, 403, Map.of("error", "Reports are for admins only"));
            return false;
        }
        return true;
    }

    private static void sendCsv(HttpExchange ex, String filename, String text) throws IOException {
        // The BOM (\uFEFF) helps Excel read the file correctly
        byte[] body = ("\uFEFF" + text).getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/csv; charset=UTF-8");
        ex.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        ex.sendResponseHeaders(200, body.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(body);
        }
    }

    // Wrap text in quotes so commas inside names don't break the CSV
    private static String csv(String value) {
        if (value == null) {
            return "";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
