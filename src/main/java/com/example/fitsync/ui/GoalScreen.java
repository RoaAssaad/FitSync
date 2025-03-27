package com.example.fitsync.ui;

import com.example.fitsync.database.DatabaseConnection;
import com.example.fitsync.model.User;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;

public class GoalScreen {
    private final User user;

    public GoalScreen(User user) {
        this.user = user;
    }

    public void start(Stage stage) {
        Label title = new Label("Set Daily Calorie Goals");

        TextField intakeGoalField = new TextField();
        intakeGoalField.setPromptText("Calories to Consume (e.g. 2000)");

        TextField burnGoalField = new TextField();
        burnGoalField.setPromptText("Calories to Burn (e.g. 500)");

        Button saveButton = new Button("Save Goals");
        Button backButton = new Button("Back");
        Label status = new Label();

        // Load existing goals
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT calories_in_goal, calories_burn_goal FROM Goals WHERE user_id = ?")) {
            stmt.setInt(1, user.getId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                intakeGoalField.setText(rs.getString("calories_in_goal"));
                burnGoalField.setText(rs.getString("calories_burn_goal"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        saveButton.setOnAction(e -> {
            try {
                double intakeGoal = Double.parseDouble(intakeGoalField.getText().trim());
                double burnGoal = Double.parseDouble(burnGoalField.getText().trim());

                try (Connection conn = DatabaseConnection.getConnection()) {
                    // UPSERT
                    PreparedStatement stmt = conn.prepareStatement(
                            "REPLACE INTO Goals (user_id, calories_in_goal, calories_burn_goal) VALUES (?, ?, ?)"
                    );
                    stmt.setInt(1, user.getId());
                    stmt.setDouble(2, intakeGoal);
                    stmt.setDouble(3, burnGoal);

                    int rows = stmt.executeUpdate();
                    status.setText(rows > 0 ? " Goals saved!" : " Failed to save goals.");
                }
            } catch (NumberFormatException ex) {
                status.setText(" Please enter valid numbers.");
            } catch (SQLException ex) {
                ex.printStackTrace();
                status.setText("Database error.");
            }
        });

        backButton.setOnAction(e -> new DashboardScreen(user).start(stage));

        VBox layout = new VBox(10, title, intakeGoalField, burnGoalField, saveButton, backButton, status);
        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout, 350, 300);
        stage.setTitle("Set Daily Goals");
        stage.setScene(scene);
        stage.show();
    }
}
