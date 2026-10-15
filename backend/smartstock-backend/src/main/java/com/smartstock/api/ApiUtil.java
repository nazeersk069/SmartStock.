package com.smartstock.api;

import com.google.gson.Gson;
import com.smartstock.model.User;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ApiUtil {

    public static final Gson GSON = new Gson();
    private static final Map<String, User> SESSIONS = new ConcurrentHashMap<>();

    public static void sendJson(HttpExchange ex, int status, Object data) throws IOException {
        byte[] body = GSON.toJson(data).getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        ex.sendResponseHeaders(status, body.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(body);
        }
    }

    public static String readBody(HttpExchange ex) throws IOException {
        return new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    public static String createSession(User user) {
        String token = UUID.randomUUID().toString();
        SESSIONS.put(token, user);
        return token;
    }

    public static User getUser(HttpExchange ex) {
        String token = ex.getRequestHeaders().getFirst("X-Auth-Token");
        return token == null ? null : SESSIONS.get(token);
    }

    public static void removeSession(HttpExchange ex) {
        String token = ex.getRequestHeaders().getFirst("X-Auth-Token");
        if (token != null) {
            SESSIONS.remove(token);
        }
    }
}
