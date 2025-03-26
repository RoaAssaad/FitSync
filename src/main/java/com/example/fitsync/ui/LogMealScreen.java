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

public class LogMealScreen {
    private final User user;

    public LogMealScreen(User user) {
        this.user = user;
    }

    public void start(Stage stage) {
        Label title = new Label("Log a Meal");

        ComboBox<String> mealDropdown = new ComboBox<>();
        DatePicker datePicker = new DatePicker(LocalDate.now());
        Button logButton = new Button("Log Meal");
        Button backButton = new Button("Back");
        Label messageLabel = new Label();

        //  Load available meals from DB
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
        mealDropdown.setPromptText("Select Meal");

        //  Log Meal Action
        logButton.setOnAction(e -> {
            String selectedMeal = mealDropdown.getValue();
            LocalDate selectedDate = datePicker.getValue();

            System.out.println("Selected meal: " + selectedMeal);
            System.out.println("Selected date: " + selectedDate);
            System.out.println("User ID: " + user.getId());

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
                        messageLabel.setText("Meal logged successfully!");
                    } else {
                        messageLabel.setText("Failed to log meal.");
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    messageLabel.setText(" Database error.");
                }
            } else {
                messageLabel.setText("Please select a meal and date.");
            }
        });

        //  Back to Dashboard
        backButton.setOnAction(e -> {
            new DashboardScreen(user).start(stage);
        });

        VBox layout = new VBox(10, title, mealDropdown, datePicker, logButton, backButton, messageLabel);
        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout, 350, 300);
        stage.setTitle("Log Meal");
        stage.setScene(scene);
        stage.show();
    }
}
