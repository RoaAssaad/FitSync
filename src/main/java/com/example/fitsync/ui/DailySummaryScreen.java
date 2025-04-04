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

import java.sql.*;
import java.time.LocalDate;

public class DailySummaryScreen {
    private final User user;

    public DailySummaryScreen(User user) {
        this.user = user;
    }

    public void start(Stage stage) {
        boolean wasFullScreen = stage.isFullScreen(); // 🔒 remember fullscreen

        Label title = new Label("Your Daily Summary");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#2C3E50"));

        DatePicker datePicker = new DatePicker(LocalDate.now());
        styleInput(datePicker);

        Button viewButton = new Button("View Summary");
        styleButton(viewButton, "#2ECC71");

        Button backButton = new Button("Back");
        styleButton(backButton, "#3498DB");

        Label caloriesInLabel = new Label("Calories Consumed: -");
        Label caloriesOutLabel = new Label("Calories Burned: -");
        for (Label label : new Label[]{caloriesInLabel, caloriesOutLabel}) {
            label.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
            label.setTextFill(Color.web("#34495E"));
        }

        viewButton.setOnAction(e -> {
            LocalDate selectedDate = datePicker.getValue();
            double totalIn = 0.0;
            double totalOut = 0.0;

            try (Connection conn = DatabaseConnection.getConnection()) {
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

                PreparedStatement checkStmt = conn.prepareStatement(
                        "SELECT id FROM Daily_Summary WHERE user_id = ? AND date = ?"
                );
                checkStmt.setInt(1, user.getId());
                checkStmt.setDate(2, Date.valueOf(selectedDate));
                ResultSet checkRs = checkStmt.executeQuery();

                if (checkRs.next()) {
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

                caloriesInLabel.setText("Calories Consumed: " + totalIn);
                caloriesOutLabel.setText("Calories Burned: " + totalOut);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        backButton.setOnAction(e -> {
            new DashboardScreen(user).start(stage);
            stage.setFullScreen(wasFullScreen);
        });

        VBox layout = new VBox(14, title, datePicker, viewButton, caloriesInLabel, caloriesOutLabel, backButton);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #FDFEFE;");
        layout.setPadding(new Insets(25));
        layout.prefWidthProperty().bind(stage.widthProperty());
        layout.prefHeightProperty().bind(stage.heightProperty());

        Scene scene = new Scene(layout);
        stage.setTitle("Daily Summary");
        stage.setScene(scene);
        stage.setFullScreen(wasFullScreen);
        stage.show();
    }

    private void styleInput(Control control) {
        control.setPrefHeight(40);
        control.setMaxWidth(300);
        control.setStyle("-fx-background-color: #ECF0F1; -fx-border-color: #BDC3C7; -fx-border-radius: 5; -fx-background-radius: 5;");
    }

    private void styleButton(Button button, String color) {
        button.setPrefWidth(160);
        button.setPrefHeight(35);
        button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
    }
}
