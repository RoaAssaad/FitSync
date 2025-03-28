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

public class LogWorkoutScreen {
    private final User user;

    public LogWorkoutScreen(User user) {
        this.user = user;
    }

    public void start(Stage stage) {
        Label title = new Label("Log a Workout");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#2C3E50"));

        ComboBox<String> workoutDropdown = new ComboBox<>();
        workoutDropdown.setPromptText("Select Workout");
        workoutDropdown.setPrefHeight(40);
        workoutDropdown.setStyle("-fx-background-color: #ECF0F1; -fx-border-color: #BDC3C7; -fx-border-radius: 5; -fx-background-radius: 5;");

        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setPrefHeight(40);
        datePicker.setStyle("-fx-background-color: #ECF0F1; -fx-border-color: #BDC3C7; -fx-border-radius: 5; -fx-background-radius: 5;");

        Button logButton = new Button("Log Workout");
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

        // Load workouts
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

        logButton.setOnAction(e -> {
            String selectedWorkout = workoutDropdown.getValue();
            LocalDate selectedDate = datePicker.getValue();

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
                        messageLabel.setTextFill(Color.web("#27AE60")); // success green
                        messageLabel.setText("Workout logged successfully!");
                    } else {
                        messageLabel.setText("⚠ Failed to log workout.");
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    messageLabel.setText("Database error.");
                }
            } else {
                messageLabel.setText("Please select a workout and date.");
            }
        });

        backButton.setOnAction(e -> new DashboardScreen(user).start(stage));

        VBox layout = new VBox(12, title, workoutDropdown, datePicker, logButton, backButton, messageLabel);
        layout.setPadding(new Insets(25));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #FDFEFE;");

        Scene scene = new Scene(layout, 400, 330);
        stage.setTitle("Log Workout");
        stage.setScene(scene);
        stage.show();
    }
}
