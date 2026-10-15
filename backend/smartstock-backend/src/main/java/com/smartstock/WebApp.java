package com.smartstock;

import com.google.gson.JsonObject;
import com.smartstock.api.ApiUtil;
import com.smartstock.api.ProductApi;
import com.smartstock.api.SaleApi;
import com.smartstock.api.ReportApi;
import com.smartstock.model.User;
import com.smartstock.service.AuthService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Map;

public class WebApp {

    private static final AuthService auth = new AuthService();

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Web pages (everything that is not /api/...)
        server.createContext("/", WebApp::serveFile);

        // POST /api/login  { "username": "...", "password": "..." }
        server.createContext("/api/login", ex -> {
            try {
                if (!ex.getRequestMethod().equals("POST")) {
                    ApiUtil.sendJson(ex, 405, Map.of("error", "Use POST"));
                    return;
                }
                JsonObject json = ApiUtil.GSON.fromJson(ApiUtil.readBody(ex), JsonObject.class);
                String username = json.get("username").getAsString();
                String password = json.get("password").getAsString();

                User user = auth.login(username, password);
                if (user == null) {
                    ApiUtil.sendJson(ex, 401, Map.of("error", "Invalid username or password"));
                    return;
                }
                String token = ApiUtil.createSession(user);
                ApiUtil.sendJson(ex, 200, Map.of(
                        "token", token,
                        "username", user.getUsername(),
                        "role", user.getRole()));
            } catch (Exception e) {
                ApiUtil.sendJson(ex, 500, Map.of("error", "Server error: " + e.getMessage()));
            }
        });

        // GET /api/me  (is this token still logged in?)
        server.createContext("/api/me", ex -> {
            User user = ApiUtil.getUser(ex);
            if (user == null) {
                ApiUtil.sendJson(ex, 401, Map.of("error", "Not logged in"));
                return;
            }
            ApiUtil.sendJson(ex, 200, Map.of(
                    "username", user.getUsername(),
                    "role", user.getRole()));
        });

        // POST /api/logout
        server.createContext("/api/logout", ex -> {
            ApiUtil.removeSession(ex);
            ApiUtil.sendJson(ex, 200, Map.of("message", "Logged out"));
        });
        ProductApi.register(server);
        SaleApi.register(server);
        ReportApi.register(server);
        server.start();
        System.out.println("SmartStock is running at http://localhost:8080");

        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(new URI("http://localhost:8080"));
        }
    }

    // Serves files from src/main/resources/web
    private static void serveFile(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        if (path.equals("/")) {
            path = "/index.html";
        }
        if (path.contains("..")) {
            ex.sendResponseHeaders(400, -1);
            return;
        }
        try (InputStream in = WebApp.class.getResourceAsStream("/web" + path)) {
            if (in == null) {
                byte[] msg = "Not found".getBytes();
                ex.sendResponseHeaders(404, msg.length);
                try (OutputStream os = ex.getResponseBody()) {
                    os.write(msg);
                }
                return;
            }
            byte[] body = in.readAllBytes();
            ex.getResponseHeaders().set("Content-Type", contentType(path));
            ex.sendResponseHeaders(200, body.length);
            try (OutputStream os = ex.getResponseBody()) {
                os.write(body);
            }
        }
    }

    private static String contentType(String path) {
        if (path.endsWith(".html")) return "text/html; charset=UTF-8";
        if (path.endsWith(".css")) return "text/css; charset=UTF-8";
        if (path.endsWith(".js")) return "application/javascript; charset=UTF-8";
        return "application/octet-stream";
    }
}
