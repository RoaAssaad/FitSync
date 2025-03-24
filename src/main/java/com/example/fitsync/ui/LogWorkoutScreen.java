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

public class LogWorkoutScreen {
    private final User user;

    public LogWorkoutScreen(User user) {
        this.user = user;
    }

    public void start(Stage stage) {
        Label title = new Label("Log a Workout");

        ComboBox<String> workoutDropdown = new ComboBox<>();
        DatePicker datePicker = new DatePicker(LocalDate.now());
        Button logButton = new Button("Log Workout");
        Button backButton = new Button("Back");
        Label messageLabel = new Label();

        //  Load available workouts from DB
        ObservableList<String> workoutNames = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name FROM Workouts")) {
            while (rs.next()) {
                workoutNames.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        workoutDropdown.setItems(workoutNames);
        workoutDropdown.setPromptText("Select Workout");

        // Log Workout Button Action (with debug + LIMIT 1 in SQL)
        logButton.setOnAction(e -> {
            String selectedWorkout = workoutDropdown.getValue();
            LocalDate selectedDate = datePicker.getValue();

            // Debug prints
            System.out.println("Selected workout: " + selectedWorkout);
            System.out.println("Selected date: " + selectedDate);
            System.out.println("User ID: " + user.getId());

            if (selectedWorkout != null && selectedDate != null) {
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(
                             "INSERT INTO User_Workouts (user_id, workout_id, completion_date) " +
                                     "VALUES (?, (SELECT id FROM Workouts WHERE name = ? LIMIT 1), ?)")) {

                    stmt.setInt(1, user.getId());
                    stmt.setString(2, selectedWorkout);
                    stmt.setDate(3, Date.valueOf(selectedDate));

                    int rows = stmt.executeUpdate();
                    if (rows > 0) {
                        messageLabel.setText(" Workout logged successfully!");
                        System.out.println("Workout logged to DB.");
                    } else {
                        messageLabel.setText("⚠ Failed to log workout.");
                        System.out.println("No rows inserted.");
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    messageLabel.setText(" Database error.");
                }
            } else {
                messageLabel.setText("Please select a workout and date.");
            }
        });

        //  Back to Dashboard
        backButton.setOnAction(e -> {
            new DashboardScreen(user).start(stage);
        });

        VBox layout = new VBox(10, title, workoutDropdown, datePicker, logButton, backButton, messageLabel);
        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout, 350, 300);
        stage.setTitle("Log Workout");
        stage.setScene(scene);
        stage.show();
    }
}
