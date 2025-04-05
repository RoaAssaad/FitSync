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

public class ViewMealsScreen {
    private final User user;

    public ViewMealsScreen(User user) {
        this.user = user;
    }

    public void start(Stage stage) {
        boolean wasFullScreen = stage.isFullScreen(); // save fullscreen state

        Label title = new Label("View Logged Meals");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#2C3E50"));

        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setPrefHeight(40);
        datePicker.setStyle("-fx-background-color: #ECF0F1; -fx-border-color: #BDC3C7; -fx-border-radius: 5; -fx-background-radius: 5;");

        Button viewButton = new Button("View Meals");
        viewButton.setPrefWidth(140);
        viewButton.setPrefHeight(35);
        viewButton.setStyle("-fx-background-color: #2ECC71; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");

        Button backButton = new Button("Back");
        backButton.setPrefWidth(140);
        backButton.setPrefHeight(35);
        backButton.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");

        ListView<String> mealList = new ListView<>();
        mealList.setPrefHeight(150);

        Label messageLabel = new Label();
        messageLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        messageLabel.setTextFill(Color.web("#34495E"));

        viewButton.setOnAction(e -> {
            LocalDate selectedDate = datePicker.getValue();
            ObservableList<String> meals = FXCollections.observableArrayList();

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT m.food_name, m.calories, m.meal_type " +
                                 "FROM User_Meals um " +
                                 "JOIN Meals m ON um.meal_id = m.id " +
                                 "WHERE um.user_id = ? AND um.meal_date = ?"
                 )) {
                stmt.setInt(1, user.getId());
                stmt.setDate(2, Date.valueOf(selectedDate));
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    String item = String.format("%s (%s): %.0f cal",
                            rs.getString("food_name"),
                            rs.getString("meal_type"),
                            rs.getDouble("calories"));
                    meals.add(item);
                }

                if (meals.isEmpty()) {
                    messageLabel.setTextFill(Color.web("#E74C3C"));
                    messageLabel.setText("No meals found for this date.");
                } else {
                    messageLabel.setTextFill(Color.web("#27AE60"));
                    messageLabel.setText("Meals logged:");
                }

                mealList.setItems(meals);

            } catch (SQLException ex) {
                ex.printStackTrace();
                messageLabel.setTextFill(Color.web("#E74C3C"));
                messageLabel.setText("Database error.");
            }
        });

        backButton.setOnAction(e -> {
            boolean stillFullScreen = stage.isFullScreen();
            new DashboardScreen(user).start(stage);
            stage.setFullScreen(stillFullScreen);
        });

        VBox layout = new VBox(12, title, datePicker, viewButton, messageLabel, mealList, backButton);
        layout.setPadding(new Insets(25));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #FDFEFE;");

        // make sure layout auto-resizes with stage
        layout.prefWidthProperty().bind(stage.widthProperty());
        layout.prefHeightProperty().bind(stage.heightProperty());

        Scene scene = new Scene(layout);
        stage.setScene(scene);
        stage.setFullScreen(wasFullScreen); // force fullscreen after scene set
        stage.setTitle("View Meals");
        stage.show();
    }
}
