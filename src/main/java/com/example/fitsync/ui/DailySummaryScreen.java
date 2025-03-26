package com.example.fitsync.ui;

import com.example.fitsync.database.DatabaseConnection;
import com.example.fitsync.model.User;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;

public class DailySummaryScreen {
    private final User user;

    public DailySummaryScreen(User user) {
        this.user = user;
    }

    public void start(Stage stage) {
        Label title = new Label("Your Daily Summary");

        DatePicker datePicker = new DatePicker(LocalDate.now());
        Button viewButton = new Button("View Summary");
        Button backButton = new Button("Back");

        Label caloriesInLabel = new Label("Calories Consumed: -");
        Label caloriesOutLabel = new Label("Calories Burned: -");

        viewButton.setOnAction(e -> {
            LocalDate selectedDate = datePicker.getValue();
            double totalIn = 0.0;
            double totalOut = 0.0;

            try (Connection conn = DatabaseConnection.getConnection()) {
                //  Total Calories Consumed
                PreparedStatement inStmt = conn.prepareStatement(
                        "SELECT SUM(m.calories) AS total_in " +
                                "FROM User_Meals um " +
                                "JOIN Meals m ON um.meal_id = m.id " +
                                "WHERE um.user_id = ? AND um.meal_date = ?"
                );
                inStmt.setInt(1, user.getId());
                inStmt.setDate(2, Date.valueOf(selectedDate));
                ResultSet inRs = inStmt.executeQuery();
                if (inRs.next()) totalIn = inRs.getDouble("total_in");

                // Total Calories Burned
                PreparedStatement outStmt = conn.prepareStatement(
                        "SELECT SUM(e.calories_per_minute * we.repetitions) AS total_out " +
                                "FROM User_Workouts uw " +
                                "JOIN Workout_Exercises we ON uw.workout_id = we.workout_id " +
                                "JOIN Exercises e ON we.exercise_id = e.id " +
                                "WHERE uw.user_id = ? AND uw.completion_date = ?"
                );
                outStmt.setInt(1, user.getId());
                outStmt.setDate(2, Date.valueOf(selectedDate));
                ResultSet outRs = outStmt.executeQuery();
                if (outRs.next()) totalOut = outRs.getDouble("total_out");

                // Upsert into Daily_Summary
                PreparedStatement checkStmt = conn.prepareStatement(
                        "SELECT id FROM Daily_Summary WHERE user_id = ? AND date = ?"
                );
                checkStmt.setInt(1, user.getId());
                checkStmt.setDate(2, Date.valueOf(selectedDate));
                ResultSet checkRs = checkStmt.executeQuery();

                if (checkRs.next()) {
                    // Update existing
                    PreparedStatement updateStmt = conn.prepareStatement(
                            "UPDATE Daily_Summary SET calories_consumed = ?, calories_burned = ? " +
                                    "WHERE user_id = ? AND date = ?"
                    );
                    updateStmt.setDouble(1, totalIn);
                    updateStmt.setDouble(2, totalOut);
                    updateStmt.setInt(3, user.getId());
                    updateStmt.setDate(4, Date.valueOf(selectedDate));
                    updateStmt.executeUpdate();
                } else {
                    // Insert new
                    PreparedStatement insertStmt = conn.prepareStatement(
                            "INSERT INTO Daily_Summary (user_id, calories_consumed, calories_burned, date) " +
                                    "VALUES (?, ?, ?, ?)"
                    );
                    insertStmt.setInt(1, user.getId());
                    insertStmt.setDouble(2, totalIn);
                    insertStmt.setDouble(3, totalOut);
                    insertStmt.setDate(4, Date.valueOf(selectedDate));
                    insertStmt.executeUpdate();
                }

                // Update labels
                caloriesInLabel.setText("Calories Consumed: " + totalIn);
                caloriesOutLabel.setText("Calories Burned: " + totalOut);

            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        backButton.setOnAction(e -> {
            new DashboardScreen(user).start(stage);
        });

        VBox layout = new VBox(10, title, datePicker, viewButton, caloriesInLabel, caloriesOutLabel, backButton);
        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout, 350, 300);
        stage.setTitle("Daily Summary");
        stage.setScene(scene);
        stage.show();
    }
}
