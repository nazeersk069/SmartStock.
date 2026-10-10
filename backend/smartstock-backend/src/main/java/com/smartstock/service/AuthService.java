
package com.smartstock.service;

import com.smartstock.dao.UserDAO;
import com.smartstock.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

public class AuthService {

    private final UserDAO userDAO = new UserDAO();

    public boolean register(String username, String password, String role) throws SQLException {
        if (username.isBlank() || password.length() < 6) {
            return false;
        }
        if (userDAO.findByUsername(username) != null) {
            return false; // username already taken
        }
        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        return userDAO.addUser(username, hash, role);
    }

    public User login(String username, String password) throws SQLException {
        User user = userDAO.findByUsername(username);
        if (user != null && BCrypt.checkpw(password, user.getPasswordHash())) {
            return user;
        }
        return null;
    }
}
