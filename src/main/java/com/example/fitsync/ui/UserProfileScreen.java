package com.example.fitsync.ui;

import com.example.fitsync.database.DatabaseConnection;
import com.example.fitsync.model.User;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class UserProfileScreen {
    private final User user;

    public UserProfileScreen(User user) {
        this.user = user;
    }

    public void start(Stage stage) {
        Label title = new Label("Your Profile");

        TextField nameField = new TextField(user.getName());
        TextField emailField = new TextField(user.getEmail());
        emailField.setDisable(true); // email shouldn't be editable

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("New Password (leave blank to keep current)");

        TextField ageField = new TextField(String.valueOf(user.getAge()));
        ComboBox<String> genderBox = new ComboBox<>();
        genderBox.getItems().addAll("M", "F");
        genderBox.setValue(user.getGender());

        TextField weightField = new TextField(String.valueOf(user.getWeight()));
        TextField heightField = new TextField(String.valueOf(user.getHeight()));

        Button saveButton = new Button("Save Changes");
        Button backButton = new Button("Back");
        Label messageLabel = new Label();

        saveButton.setOnAction(e -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String newPassword = passwordField.getText().trim();
                String query;

                if (newPassword.isEmpty()) {
                    // No password update
                    query = "UPDATE Users SET name = ?, age = ?, gender = ?, weight = ?, height = ? WHERE id = ?";
                } else {
                    // With password update
                    query = "UPDATE Users SET name = ?, password = ?, age = ?, gender = ?, weight = ?, height = ? WHERE id = ?";
                }

                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, nameField.getText().trim());

                int index = 2;
                if (!newPassword.isEmpty()) {
                    stmt.setString(2, newPassword);
                    index++;
                }

                stmt.setInt(index++, Integer.parseInt(ageField.getText().trim()));
                stmt.setString(index++, genderBox.getValue());
                stmt.setDouble(index++, Double.parseDouble(weightField.getText().trim()));
                stmt.setDouble(index++, Double.parseDouble(heightField.getText().trim()));
                stmt.setInt(index, user.getId());

                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    messageLabel.setText("✅ Profile updated successfully!");
                } else {
                    messageLabel.setText("⚠️ Failed to update profile.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                messageLabel.setText("❌ Invalid input.");
            }
        });

        backButton.setOnAction(e -> {
            new DashboardScreen(user).start(stage);
        });

        VBox layout = new VBox(10,
                title, nameField, emailField, passwordField,
                ageField, genderBox, weightField, heightField,
                saveButton, backButton, messageLabel
        );
        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout, 350, 450);
        stage.setTitle("User Profile");
        stage.setScene(scene);
        stage.show();
    }
}
