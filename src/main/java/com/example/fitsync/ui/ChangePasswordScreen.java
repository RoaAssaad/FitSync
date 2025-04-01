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

public class ChangePasswordScreen {
    private final User user;

    public ChangePasswordScreen(User user) {
        this.user = user;
    }

    public void start(Stage stage) {
        Label title = new Label("Change Password");

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("New Password");

        Button saveButton = new Button("Update Password");
        Button backButton = new Button("Back");
        Label messageLabel = new Label();

        saveButton.setOnAction(e -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String newPass = newPasswordField.getText().trim();
                String query = "UPDATE Users SET password = ? WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, newPass);
                stmt.setInt(2, user.getId());
                int updated = stmt.executeUpdate();
                if (updated > 0) {
                    messageLabel.setText("Password updated!");
                } else {
                    messageLabel.setText(" Failed to update password.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                messageLabel.setText(" Error updating password.");
            }
        });

        backButton.setOnAction(e -> new DashboardScreen(user).start(stage));

        VBox layout = new VBox(10, title, newPasswordField, saveButton, backButton, messageLabel);
        layout.setPadding(new Insets(20));
        Scene scene = new Scene(layout, 300, 250);
        stage.setTitle("Change Password");
        stage.setScene(scene);
        stage.show();
    }
}
