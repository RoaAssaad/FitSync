package com.example.fitsync.service;

import com.example.fitsync.database.DatabaseConnection;
import com.example.fitsync.model.User;

import java.sql.*;

public class UserService {

    // Add a new user
    public boolean addUser(User user) {
        String sql = "INSERT INTO Users (name, email, password, age, gender, weight, height) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPassword());
            stmt.setInt(4, user.getAge());
            stmt.setString(5, user.getGender());
            stmt.setDouble(6, user.getWeight());
            stmt.setDouble(7, user.getHeight());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Login validation
    public User validateLogin(String email, String password) {
        String sql = "SELECT * FROM Users WHERE email = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getInt("age"),
                        rs.getString("gender"),
                        rs.getDouble("weight"),
                        rs.getDouble("height")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Get user by ID (example method)
    public User getUserById(int id) {
        String sql = "SELECT * FROM Users WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getInt("age"),
                        rs.getString("gender"),
                        rs.getDouble("weight"),
                        rs.getDouble("height")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
