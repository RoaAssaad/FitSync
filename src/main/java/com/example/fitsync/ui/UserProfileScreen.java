package com.example.fitsync.ui;

import com.example.fitsync.database.DatabaseConnection;
import com.example.fitsync.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
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
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#2C3E50"));

        TextField nameField = new TextField(user.getName());
        nameField.setPrefHeight(40);
        nameField.setStyle("-fx-background-color: #ECF0F1; -fx-border-color: #BDC3C7; -fx-border-radius: 5; -fx-background-radius: 5;");

        TextField emailField = new TextField(user.getEmail());
        emailField.setDisable(true);
        emailField.setPrefHeight(40);
        emailField.setStyle("-fx-background-color: #E0E0E0; -fx-border-radius: 5; -fx-background-radius: 5;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("New Password (leave blank to keep current)");
        passwordField.setPrefHeight(40);
        passwordField.setStyle("-fx-background-color: #ECF0F1; -fx-border-color: #BDC3C7; -fx-border-radius: 5; -fx-background-radius: 5;");

        TextField ageField = new TextField(String.valueOf(user.getAge()));
        ageField.setPrefHeight(40);
        ageField.setStyle("-fx-background-color: #ECF0F1; -fx-border-color: #BDC3C7; -fx-border-radius: 5; -fx-background-radius: 5;");

        ComboBox<String> genderBox = new ComboBox<>();
        genderBox.getItems().addAll("M", "F");
        genderBox.setValue(user.getGender());
        genderBox.setPrefHeight(40);
        genderBox.setStyle("-fx-background-color: #ECF0F1; -fx-border-color: #BDC3C7; -fx-border-radius: 5; -fx-background-radius: 5;");

        TextField weightField = new TextField(String.valueOf(user.getWeight()));
        weightField.setPrefHeight(40);
        weightField.setStyle("-fx-background-color: #ECF0F1; -fx-border-color: #BDC3C7; -fx-border-radius: 5; -fx-background-radius: 5;");

        TextField heightField = new TextField(String.valueOf(user.getHeight()));
        heightField.setPrefHeight(40);
        heightField.setStyle("-fx-background-color: #ECF0F1; -fx-border-color: #BDC3C7; -fx-border-radius: 5; -fx-background-radius: 5;");

        Button saveButton = new Button("Save Changes");
        saveButton.setPrefWidth(160);
        saveButton.setPrefHeight(35);
        saveButton.setStyle("-fx-background-color: #2ECC71; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");

        Button backButton = new Button("Back");
        backButton.setPrefWidth(160);
        backButton.setPrefHeight(35);
        backButton.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");

        Label messageLabel = new Label();
        messageLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        messageLabel.setTextFill(Color.web("#E74C3C"));

        saveButton.setOnAction(e -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String newPassword = passwordField.getText().trim();
                String query;

                if (newPassword.isEmpty()) {
                    query = "UPDATE Users SET name = ?, age = ?, gender = ?, weight = ?, height = ? WHERE id = ?";
                } else {
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
                    messageLabel.setTextFill(Color.web("#27AE60"));
                    messageLabel.setText("✅ Profile updated successfully!");
                } else {
                    messageLabel.setTextFill(Color.web("#E74C3C"));
                    messageLabel.setText("⚠️ Failed to update profile.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                messageLabel.setTextFill(Color.web("#E74C3C"));
                messageLabel.setText("❌ Invalid input.");
            }
        });

        backButton.setOnAction(e -> {
            new DashboardScreen(user).start(stage);
        });

        VBox layout = new VBox(12,
                title, nameField, emailField, passwordField,
                ageField, genderBox, weightField, heightField,
                saveButton, backButton, messageLabel
        );
        layout.setPadding(new Insets(25));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #FDFEFE;");

        Scene scene = new Scene(layout, 400, 520);
        stage.setTitle("User Profile");
        stage.setScene(scene);
        stage.show();
    }
}
