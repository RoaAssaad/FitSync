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

public class TodayDashboardScreen {
    private final User user;

    public TodayDashboardScreen(User user) {
        this.user = user;
    }

    public void start(Stage stage) {
        LocalDate today = LocalDate.now();

        Label title = new Label("Today at a Glance - " + today);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#2C3E50"));

        Label caloriesInLabel = new Label("Calories Consumed: -");
        Label caloriesOutLabel = new Label("Calories Burned: -");
        Label netCaloriesLabel = new Label("Net Calories: -");

        Label intakeGoalLabel = new Label("Intake Goal: -");
        Label burnGoalLabel = new Label("Burn Goal: -");
        Label intakeStatus = new Label();
        Label burnStatus = new Label();

        Label mealsTitle = new Label("Meals Logged:");
        ListView<String> mealsList = new ListView<>();

        Label workoutsTitle = new Label("Workouts Logged:");
        ListView<String> workoutsList = new ListView<>();

        Button backButton = new Button("Back");
        backButton.setPrefWidth(140);
        backButton.setPrefHeight(35);
        backButton.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");

        // Style labels
        for (Label label : new Label[]{caloriesInLabel, caloriesOutLabel, netCaloriesLabel,
                intakeGoalLabel, burnGoalLabel, intakeStatus, burnStatus,
                mealsTitle, workoutsTitle}) {
            label.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
            label.setTextFill(Color.web("#34495E"));
        }

        caloriesInLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        caloriesOutLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        netCaloriesLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        intakeStatus.setTextFill(Color.web("#27AE60"));
        burnStatus.setTextFill(Color.web("#27AE60"));

        double caloriesIn = 0.0;
        double caloriesOut = 0.0;
        double intakeGoal = 0.0;
        double burnGoal = 0.0;

        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement summaryStmt = conn.prepareStatement(
                    "SELECT calories_consumed, calories_burned FROM Daily_Summary WHERE user_id = ? AND date = ?"
            );
            summaryStmt.setInt(1, user.getId());
            summaryStmt.setDate(2, Date.valueOf(today));
            ResultSet summaryRs = summaryStmt.executeQuery();

            if (summaryRs.next()) {
                caloriesIn = summaryRs.getDouble("calories_consumed");
                caloriesOut = summaryRs.getDouble("calories_burned");
            }

            caloriesInLabel.setText("Calories Consumed: " + caloriesIn);
            caloriesOutLabel.setText("Calories Burned: " + caloriesOut);
            netCaloriesLabel.setText("Net Calories: " + (caloriesIn - caloriesOut));

            PreparedStatement goalStmt = conn.prepareStatement(
                    "SELECT calories_in_goal, calories_burn_goal FROM Goals WHERE user_id = ?"
            );
            goalStmt.setInt(1, user.getId());
            ResultSet goalRs = goalStmt.executeQuery();

            if (goalRs.next()) {
                intakeGoal = goalRs.getDouble("calories_in_goal");
                burnGoal = goalRs.getDouble("calories_burn_goal");

                intakeGoalLabel.setText("Intake Goal: " + intakeGoal);
                burnGoalLabel.setText("Burn Goal: " + burnGoal);

                if (caloriesIn >= intakeGoal) {
                    intakeStatus.setText("You reached your intake goal!");
                } else {
                    intakeStatus.setText((intakeGoal - caloriesIn) + " cal left to reach intake goal.");
                }

                if (caloriesOut >= burnGoal) {
                    burnStatus.setText("You hit your burn goal!");
                } else {
                    burnStatus.setText((burnGoal - caloriesOut) + " cal left to burn.");
                }
            } else {
                intakeGoalLabel.setText("No intake goal set.");
                burnGoalLabel.setText("No burn goal set.");
            }

            ObservableList<String> meals = FXCollections.observableArrayList();
            PreparedStatement mealStmt = conn.prepareStatement(
                    "SELECT m.food_name, m.meal_type, m.calories FROM User_Meals um " +
                            "JOIN Meals m ON um.meal_id = m.id " +
                            "WHERE um.user_id = ? AND um.meal_date = ?"
            );
            mealStmt.setInt(1, user.getId());
            mealStmt.setDate(2, Date.valueOf(today));
            ResultSet mealRs = mealStmt.executeQuery();
            while (mealRs.next()) {
                meals.add(String.format("%s (%s) - %.0f cal",
                        mealRs.getString("food_name"),
                        mealRs.getString("meal_type"),
                        mealRs.getDouble("calories")));
            }
            mealsList.setItems(meals);
            mealsList.setPrefHeight(100);

            ObservableList<String> workouts = FXCollections.observableArrayList();
            PreparedStatement workoutStmt = conn.prepareStatement(
                    "SELECT w.name FROM User_Workouts uw " +
                            "JOIN Workouts w ON uw.workout_id = w.id " +
                            "WHERE uw.user_id = ? AND uw.completion_date = ?"
            );
            workoutStmt.setInt(1, user.getId());
            workoutStmt.setDate(2, Date.valueOf(today));
            ResultSet workoutRs = workoutStmt.executeQuery();
            while (workoutRs.next()) {
                workouts.add(workoutRs.getString("name"));
            }
            workoutsList.setItems(workouts);
            workoutsList.setPrefHeight(100);

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        VBox layout = new VBox(12,
                title,
                caloriesInLabel, caloriesOutLabel, netCaloriesLabel,
                intakeGoalLabel, intakeStatus,
                burnGoalLabel, burnStatus,
                new Separator(),
                mealsTitle, mealsList,
                workoutsTitle, workoutsList,
                backButton
        );
        layout.setPadding(new Insets(25));
        layout.setAlignment(Pos.TOP_CENTER);
        layout.setStyle("-fx-background-color: #FDFEFE;");

        Scene scene = new Scene(layout, 520, 630);
        stage.setTitle("Today at a Glance");
        stage.setScene(scene);
        stage.show();
    }
}
