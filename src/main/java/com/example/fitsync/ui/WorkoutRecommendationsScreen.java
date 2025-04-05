package com.example.fitsync.ui;

import com.example.fitsync.database.DatabaseConnection;
import com.example.fitsync.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.*;

public class WorkoutRecommendationsScreen {
    private final User user;

    public WorkoutRecommendationsScreen(User user) {
        this.user = user;
    }

    public void start(Stage stage) {
        boolean wasFullScreen = stage.isFullScreen();

        Label title = new Label("Workout Recommendations");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#2C3E50"));

        //  Radio Buttons for Fitness Level
        Label levelLabel = new Label("Choose Fitness Level:");
        levelLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        levelLabel.setTextFill(Color.web("#34495E"));

        RadioButton lowBtn = new RadioButton("Low");
        RadioButton modBtn = new RadioButton("Moderate");
        RadioButton highBtn = new RadioButton("High");

        ToggleGroup levelGroup = new ToggleGroup();
        lowBtn.setToggleGroup(levelGroup);
        modBtn.setToggleGroup(levelGroup);
        highBtn.setToggleGroup(levelGroup);

        HBox radioRow = new HBox(20, lowBtn, modBtn, highBtn);
        radioRow.setAlignment(Pos.CENTER);

        VBox levelBox = new VBox(5, levelLabel, radioRow);
        levelBox.setAlignment(Pos.CENTER);

        //  Goal Calories Field
        TextField goalField = new TextField();
        goalField.setPromptText("Calories to Burn (optional)");
        goalField.setPrefHeight(40);
        goalField.setMaxWidth(300);
        goalField.setStyle("-fx-background-color: #ECF0F1; -fx-border-color: #BDC3C7; -fx-border-radius: 5; -fx-background-radius: 5;");

        //  Buttons
        Button recommendButton = new Button("Get Recommendations");
        recommendButton.setPrefWidth(200);
        recommendButton.setPrefHeight(35);
        recommendButton.setStyle("-fx-background-color: #2ECC71; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");

        Button backButton = new Button("Back");
        backButton.setPrefWidth(140);
        backButton.setPrefHeight(35);
        backButton.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");

        Label status = new Label();
        status.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        status.setTextFill(Color.web("#34495E"));

        ListView<String> resultList = new ListView<>();
        resultList.setPrefHeight(200);

        recommendButton.setOnAction(e -> {
            String difficulty = null;
            if (lowBtn.isSelected()) difficulty = "Easy";
            else if (modBtn.isSelected()) difficulty = "Medium";
            else if (highBtn.isSelected()) difficulty = "Hard";

            String goalText = goalField.getText().trim();
            Double goalCalories = null;

            if (!goalText.isEmpty()) {
                try {
                    goalCalories = Double.parseDouble(goalText);
                } catch (NumberFormatException ex) {
                    status.setTextFill(Color.web("#E74C3C"));
                    status.setText(" Invalid calorie input.");
                    return;
                }
            }

            ObservableList<String> recommendations = FXCollections.observableArrayList();

            try (Connection conn = DatabaseConnection.getConnection()) {
                StringBuilder query = new StringBuilder(
                        "SELECT name, category, calories_per_minute, difficulty_level FROM Exercises WHERE 1=1"
                );

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
                    status.setTextFill(Color.web("#E74C3C"));
                    status.setText("No matching workouts found.");
                } else {
                    status.setTextFill(Color.web("#27AE60"));
                    status.setText("Recommended workouts:");
                }

                resultList.setItems(recommendations);
            } catch (SQLException ex) {
                ex.printStackTrace();
                status.setTextFill(Color.web("#E74C3C"));
                status.setText(" Database error.");
            }
        });

        backButton.setOnAction(e -> {
            new DashboardScreen(user).start(stage);
            stage.setFullScreen(wasFullScreen);
        });

        VBox layout = new VBox(12,
                title,
                levelBox,
                goalField,
                recommendButton,
                status,
                resultList,
                backButton
        );
        layout.setPadding(new Insets(25));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #FDFEFE;");
        layout.prefWidthProperty().bind(stage.widthProperty());
        layout.prefHeightProperty().bind(stage.heightProperty());

        Scene scene = new Scene(layout);
        stage.setScene(scene);
        stage.setTitle("Workout Recommendations");
        stage.setFullScreen(wasFullScreen);
        stage.show();
    }
}
