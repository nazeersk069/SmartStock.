package com.smartstock.api;

import com.google.gson.JsonObject;
import com.smartstock.dao.ProductDAO;
import com.smartstock.model.Product;
import com.smartstock.model.User;
import com.sun.net.httpserver.HttpServer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProductApi {

    private static final ProductDAO dao = new ProductDAO();

    public static void register(HttpServer server) {
        server.createContext("/api/products", ex -> {
            try {
                User user = ApiUtil.getUser(ex);
                if (user == null) {
                    ApiUtil.sendJson(ex, 401, Map.of("error", "Please log in first"));
                    return;
                }

                String method = ex.getRequestMethod();

                if (method.equals("GET")) {
                    List<Map<String, Object>> list = new ArrayList<>();
                    for (Product p : dao.getAllProducts()) {
                        list.add(Map.of(
                                "id", p.getProductId(),
                                "name", p.getName(),
                                "category", p.getCategory() == null ? "" : p.getCategory(),
                                "price", p.getPrice(),
                                "quantity", p.getQuantity(),
                                "reorderLevel", p.getReorderLevel(),
                                "lowStock", p.getQuantity() <= p.getReorderLevel()));
                    }
                    ApiUtil.sendJson(ex, 200, list);

                } else if (method.equals("POST")) {
                    JsonObject json = ApiUtil.GSON.fromJson(ApiUtil.readBody(ex), JsonObject.class);
                    String name = json.get("name").getAsString().trim();
                    String category = json.get("category").getAsString().trim();
                    double price = json.get("price").getAsDouble();
                    int quantity = json.get("quantity").getAsInt();
                    int reorder = json.get("reorderLevel").getAsInt();

                    if (name.isEmpty() || price < 0 || quantity < 0 || reorder < 0) {
                        ApiUtil.sendJson(ex, 400, Map.of("error",
                                "Enter a name and numbers that are not negative"));
                        return;
                    }

                    boolean ok = dao.addProduct(new Product(0, name, category, price, quantity, reorder));
                    if (ok) {
                        ApiUtil.sendJson(ex, 200, Map.of("message", "Product added"));
                    } else {
                        ApiUtil.sendJson(ex, 500, Map.of("error", "Could not add product"));
                    }

                } else {
                    ApiUtil.sendJson(ex, 405, Map.of("error", "Method not allowed"));
                }

            } catch (RuntimeException e) {
                ApiUtil.sendJson(ex, 400, Map.of("error", "Please fill in every field correctly"));
            } catch (Exception e) {
                ApiUtil.sendJson(ex, 500, Map.of("error", "Server error: " + e.getMessage()));
            }
        });
    }
}
