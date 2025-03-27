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

public class WorkoutRecommendationsScreen {
    private final User user;

    public WorkoutRecommendationsScreen(User user) {
        this.user = user;
    }

    public void start(Stage stage) {
        Label title = new Label("Workout Recommendations");

        ComboBox<String> difficultyBox = new ComboBox<>();
        difficultyBox.getItems().addAll("Easy", "Medium", "Hard");
        difficultyBox.setPromptText("Choose Fitness Level");

        TextField goalField = new TextField();
        goalField.setPromptText("Calories to Burn (optional)");

        Button recommendButton = new Button("Get Recommendations");
        ListView<String> resultList = new ListView<>();
        Button backButton = new Button("Back");

        Label status = new Label();

        recommendButton.setOnAction(e -> {
            String difficulty = difficultyBox.getValue();
            String goalText = goalField.getText().trim();
            Double goalCalories = null;

            if (!goalText.isEmpty()) {
                try {
                    goalCalories = Double.parseDouble(goalText);
                } catch (NumberFormatException ex) {
                    status.setText("⚠️ Invalid calorie input.");
                    return;
                }
            }

            ObservableList<String> recommendations = FXCollections.observableArrayList();

            try (Connection conn = DatabaseConnection.getConnection()) {
                StringBuilder query = new StringBuilder(
                        "SELECT name, category, calories_per_minute, difficulty_level FROM Exercises WHERE 1=1");

                if (difficulty != null) {
                    query.append(" AND difficulty_level = ?");
                }
                if (goalCalories != null) {
                    query.append(" AND calories_per_minute * 30 >= ?");
                }

                PreparedStatement stmt = conn.prepareStatement(query.toString());

                int index = 1;
                if (difficulty != null) {
                    stmt.setString(index++, difficulty);
                }
                if (goalCalories != null) {
                    stmt.setDouble(index++, goalCalories);
                }

                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    recommendations.add(String.format(
                            "%s (%s) - %.1f cal/min [%s]",
                            rs.getString("name"),
                            rs.getString("category"),
                            rs.getDouble("calories_per_minute"),
                            rs.getString("difficulty_level")
                    ));
                }

                if (recommendations.isEmpty()) {
                    status.setText("No matching workouts found.");
                } else {
                    status.setText("Recommended workouts:");
                }

                resultList.setItems(recommendations);

            } catch (SQLException ex) {
                ex.printStackTrace();
                status.setText("❌ Database error.");
            }
        });

        backButton.setOnAction(e -> new DashboardScreen(user).start(stage));

        VBox layout = new VBox(10,
                title, difficultyBox, goalField,
                recommendButton, status, resultList, backButton);
        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout, 450, 500);
        stage.setTitle("Workout Recommendations");
        stage.setScene(scene);
        stage.show();
    }
}
