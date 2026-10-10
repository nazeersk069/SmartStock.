package com.smartstock.dao;

import com.smartstock.model.User;
import com.smartstock.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    public boolean addUser(String username, String passwordHash, String role) throws SQLException {
        String sql = "INSERT INTO Users (username, password_hash, role) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.setString(3, role);
            return ps.executeUpdate() > 0;
        }
    }

    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT user_id, username, password_hash, role FROM Users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("role"));
                }
            }
        }
        return null;
    }
}
