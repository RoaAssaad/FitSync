package com.example.fitsync.ui;

import com.example.fitsync.database.DatabaseConnection;
import com.example.fitsync.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;

public class LogMealScreen {
    private final User user;

    public LogMealScreen(User user) {
        this.user = user;
    }

    public void start(Stage stage) {
        Label title = new Label("Log a Meal");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#2C3E50"));

        ComboBox<String> mealDropdown = new ComboBox<>();
        mealDropdown.setPromptText("Select Meal");
        mealDropdown.setPrefHeight(40);
        mealDropdown.setStyle("-fx-background-color: #ECF0F1; -fx-border-color: #BDC3C7; -fx-border-radius: 5; -fx-background-radius: 5;");

        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setPrefHeight(40);
        datePicker.setStyle("-fx-background-color: #ECF0F1; -fx-border-color: #BDC3C7; -fx-border-radius: 5; -fx-background-radius: 5;");

        Button logButton = new Button("Log Meal");
        logButton.setPrefWidth(140);
        logButton.setPrefHeight(35);
        logButton.setStyle("-fx-background-color: #2ECC71; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");

        Button backButton = new Button("Back");
        backButton.setPrefWidth(140);
        backButton.setPrefHeight(35);
        backButton.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");

        Label messageLabel = new Label();
        messageLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        messageLabel.setTextFill(Color.web("#E74C3C"));

        // Load meals
        ObservableList<String> mealNames = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT food_name FROM Meals")) {
            while (rs.next()) {
                mealNames.add(rs.getString("food_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        mealDropdown.setItems(mealNames);

        // Log Meal
        logButton.setOnAction(e -> {
            String selectedMeal = mealDropdown.getValue();
            LocalDate selectedDate = datePicker.getValue();

            if (selectedMeal != null && selectedDate != null) {
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(
                             "INSERT INTO User_Meals (user_id, meal_id, meal_date) " +
                                     "VALUES (?, (SELECT id FROM Meals WHERE food_name = ? LIMIT 1), ?)")) {

                    stmt.setInt(1, user.getId());
                    stmt.setString(2, selectedMeal);
                    stmt.setDate(3, Date.valueOf(selectedDate));

                    int rows = stmt.executeUpdate();
                    if (rows > 0) {
                        messageLabel.setTextFill(Color.web("#27AE60")); // green
                        messageLabel.setText("Meal logged successfully!");
                    } else {
                        messageLabel.setTextFill(Color.web("#E74C3C")); // red
                        messageLabel.setText("Failed to log meal.");
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    messageLabel.setText("Database error.");
                }
            } else {
                messageLabel.setText("Please select a meal and date.");
            }
        });

        backButton.setOnAction(e -> {
            new DashboardScreen(user).start(stage);
        });

        VBox layout = new VBox(12, title, mealDropdown, datePicker, logButton, backButton, messageLabel);
        layout.setPadding(new Insets(25));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #FDFEFE;");

        Scene scene = new Scene(layout, 400, 350);
        stage.setTitle("Log Meal");
        stage.setScene(scene);
        stage.show();
    }
}
