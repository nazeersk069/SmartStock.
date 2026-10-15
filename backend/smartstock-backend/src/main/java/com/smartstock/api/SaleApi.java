
package com.smartstock.api;

import com.google.gson.JsonObject;
import com.smartstock.dao.SaleDAO;
import com.smartstock.model.Sale;
import com.smartstock.model.User;
import com.sun.net.httpserver.HttpServer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SaleApi {

    private static final SaleDAO dao = new SaleDAO();

    public static void register(HttpServer server) {

        // POST /api/sell  { "productId": 3, "quantity": 2 }
        server.createContext("/api/sell", ex -> {
            try {
                User user = ApiUtil.getUser(ex);
                if (user == null) {
                    ApiUtil.sendJson(ex, 401, Map.of("error", "Please log in first"));
                    return;
                }
                if (!ex.getRequestMethod().equals("POST")) {
                    ApiUtil.sendJson(ex, 405, Map.of("error", "Use POST"));
                    return;
                }

                JsonObject json = ApiUtil.GSON.fromJson(ApiUtil.readBody(ex), JsonObject.class);
                int productId = json.get("productId").getAsInt();
                int quantity = json.get("quantity").getAsInt();

                String result = dao.sellProduct(productId, user.getUserId(), quantity);

                if (result.startsWith("Sale recorded")) {
                    ApiUtil.sendJson(ex, 200, Map.of("message", result));
                } else {
                    ApiUtil.sendJson(ex, 400, Map.of("error", result));
                }

            } catch (RuntimeException e) {
                ApiUtil.sendJson(ex, 400, Map.of("error", "Enter a valid whole number for quantity"));
            } catch (Exception e) {
                ApiUtil.sendJson(ex, 500, Map.of("error", "Server error: " + e.getMessage()));
            }
        });

        // GET /api/sales
        server.createContext("/api/sales", ex -> {
            try {
                User user = ApiUtil.getUser(ex);
                if (user == null) {
                    ApiUtil.sendJson(ex, 401, Map.of("error", "Please log in first"));
                    return;
                }

                List<Map<String, Object>> list = new ArrayList<>();
                for (Sale s : dao.getAllSales()) {
                    String date = String.valueOf(s.getSaleDate());
                    if (date.length() > 19) {
                        date = date.substring(0, 19);
                    }
                    list.add(Map.of(
                            "id", s.getSaleId(),
                            "product", s.getProductName(),
                            "soldBy", s.getUsername(),
                            "quantity", s.getQuantity(),
                            "total", s.getTotalAmount(),
                            "date", date));
                }
                ApiUtil.sendJson(ex, 200, list);

            } catch (Exception e) {
                ApiUtil.sendJson(ex, 500, Map.of("error", "Server error: " + e.getMessage()));
            }
        });
    }
}
