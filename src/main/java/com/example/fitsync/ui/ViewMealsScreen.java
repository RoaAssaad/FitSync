package com.example.fitsync.ui;

import com.example.fitsync.database.DatabaseConnection;
import com.example.fitsync.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;

public class ViewMealsScreen {
    private final User user;

    public ViewMealsScreen(User user) {
        this.user = user;
    }

    public void start(Stage stage) {
        Label title = new Label("View Logged Meals");

        DatePicker datePicker = new DatePicker(LocalDate.now());
        Button viewButton = new Button("View Meals");
        Button backButton = new Button("Back");

        ListView<String> mealList = new ListView<>();
        Label messageLabel = new Label();

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
                    messageLabel.setText("No meals found for this date.");
                } else {
                    messageLabel.setText("Meals logged:");
                }

                mealList.setItems(meals);

            } catch (SQLException ex) {
                ex.printStackTrace();
                messageLabel.setText("Database error.");
            }
        });

        backButton.setOnAction(e -> {
            new DashboardScreen(user).start(stage);
        });


        VBox layout = new VBox(10, title, datePicker, viewButton, messageLabel, mealList, backButton);
        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout, 400, 350);
        stage.setTitle("View Meals");
        stage.setScene(scene);
        stage.show();
    }
}
