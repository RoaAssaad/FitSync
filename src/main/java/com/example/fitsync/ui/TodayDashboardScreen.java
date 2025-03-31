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

// all existing imports stay the same
import java.util.HashMap;
import java.util.Map;

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
        Label workoutsTitle = new Label("Workouts Logged:");
        Label messageLabel = new Label();

        for (Label label : new Label[]{caloriesInLabel, caloriesOutLabel, netCaloriesLabel, intakeGoalLabel, burnGoalLabel, intakeStatus, burnStatus, mealsTitle, workoutsTitle}) {
            label.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
            label.setTextFill(Color.web("#34495E"));
        }

        caloriesInLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        caloriesOutLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        netCaloriesLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        intakeStatus.setTextFill(Color.web("#27AE60"));
        burnStatus.setTextFill(Color.web("#27AE60"));
        messageLabel.setTextFill(Color.web("#E74C3C"));

        ListView<String> mealsList = new ListView<>();
        ListView<String> workoutsList = new ListView<>();
        mealsList.setPrefHeight(100);
        workoutsList.setPrefHeight(100);

        ComboBox<String> updateMealBox = new ComboBox<>();
        ComboBox<String> updateWorkoutBox = new ComboBox<>();

        Button updateMealBtn = new Button("Update Meal");
        Button deleteMealBtn = new Button("Delete Meal");
        Button updateWorkoutBtn = new Button("Update Workout");
        Button deleteWorkoutBtn = new Button("Delete Workout");

        Button backButton = new Button("Back");
        backButton.setPrefWidth(140);
        backButton.setPrefHeight(35);
        backButton.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");

        updateMealBtn.setStyle("-fx-background-color: #F1C40F; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
        deleteMealBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
        updateWorkoutBtn.setStyle("-fx-background-color: #F1C40F; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
        deleteWorkoutBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");

        Map<String, Integer> mealNameToId = new HashMap<>();
        Map<String, Integer> workoutNameToId = new HashMap<>();
        Map<String, Integer> loggedMealToId = new HashMap<>();
        Map<String, Integer> loggedWorkoutToId = new HashMap<>();
        String[] selectedMeal = {null};
        String[] selectedWorkout = {null};

        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement summaryStmt = conn.prepareStatement("SELECT calories_consumed, calories_burned FROM Daily_Summary WHERE user_id = ? AND date = ?");
            summaryStmt.setInt(1, user.getId());
            summaryStmt.setDate(2, Date.valueOf(today));
            ResultSet summaryRs = summaryStmt.executeQuery();
            if (summaryRs.next()) {
                double in = summaryRs.getDouble("calories_consumed");
                double out = summaryRs.getDouble("calories_burned");
                caloriesInLabel.setText("Calories Consumed: " + in);
                caloriesOutLabel.setText("Calories Burned: " + out);
                netCaloriesLabel.setText("Net Calories: " + (in - out));
            }

            PreparedStatement goalStmt = conn.prepareStatement("SELECT calories_in_goal, calories_burn_goal FROM Goals WHERE user_id = ?");
            goalStmt.setInt(1, user.getId());
            ResultSet goalRs = goalStmt.executeQuery();
            if (goalRs.next()) {
                double inGoal = goalRs.getDouble("calories_in_goal");
                double outGoal = goalRs.getDouble("calories_burn_goal");
                double caloriesIn = summaryRs.getDouble("calories_consumed");
                double caloriesOut = summaryRs.getDouble("calories_burned");
                intakeGoalLabel.setText("Intake Goal: " + inGoal);
                burnGoalLabel.setText("Burn Goal: " + outGoal);
                intakeStatus.setText(caloriesIn >= inGoal ? "You reached your intake goal!" : (inGoal - caloriesIn) + " cal left to reach intake goal.");
                burnStatus.setText(caloriesOut >= outGoal ? "You hit your burn goal!" : (outGoal - caloriesOut) + " cal left to burn.");

            }

            ResultSet allMeals = conn.createStatement().executeQuery("SELECT id, food_name FROM Meals");
            while (allMeals.next()) {
                String name = allMeals.getString("food_name");
                mealNameToId.put(name, allMeals.getInt("id"));
                updateMealBox.getItems().add(name);
            }

            ResultSet allWorkouts = conn.createStatement().executeQuery("SELECT id, name FROM Workouts");
            while (allWorkouts.next()) {
                String name = allWorkouts.getString("name");
                workoutNameToId.put(name, allWorkouts.getInt("id"));
                updateWorkoutBox.getItems().add(name);
            }

            ObservableList<String> meals = FXCollections.observableArrayList();
            PreparedStatement mealStmt = conn.prepareStatement("SELECT m.id, m.food_name, m.meal_type, m.calories FROM User_Meals um JOIN Meals m ON um.meal_id = m.id WHERE um.user_id = ? AND um.meal_date = ?");
            mealStmt.setInt(1, user.getId());
            mealStmt.setDate(2, Date.valueOf(today));
            ResultSet mealRs = mealStmt.executeQuery();
            while (mealRs.next()) {
                String label = String.format("%s (%s) - %.0f cal", mealRs.getString("food_name"), mealRs.getString("meal_type"), mealRs.getDouble("calories"));
                meals.add(label);
                loggedMealToId.put(label, mealRs.getInt("id"));
            }
            mealsList.setItems(meals);

            ObservableList<String> workouts = FXCollections.observableArrayList();
            PreparedStatement workoutStmt = conn.prepareStatement("SELECT w.id, w.name FROM User_Workouts uw JOIN Workouts w ON uw.workout_id = w.id WHERE uw.user_id = ? AND uw.completion_date = ?");
            workoutStmt.setInt(1, user.getId());
            workoutStmt.setDate(2, Date.valueOf(today));
            ResultSet workoutRs = workoutStmt.executeQuery();
            while (workoutRs.next()) {
                String name = workoutRs.getString("name");
                workouts.add(name);
                loggedWorkoutToId.put(name, workoutRs.getInt("id"));
            }
            workoutsList.setItems(workouts);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        mealsList.setOnMouseClicked(e -> selectedMeal[0] = mealsList.getSelectionModel().getSelectedItem());
        workoutsList.setOnMouseClicked(e -> selectedWorkout[0] = workoutsList.getSelectionModel().getSelectedItem());

        updateMealBtn.setOnAction(e -> {
            if (selectedMeal[0] != null && updateMealBox.getValue() != null) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    PreparedStatement stmt = conn.prepareStatement("UPDATE User_Meals SET meal_id = ? WHERE user_id = ? AND meal_id = ? AND meal_date = ?");
                    stmt.setInt(1, mealNameToId.get(updateMealBox.getValue()));
                    stmt.setInt(2, user.getId());
                    stmt.setInt(3, loggedMealToId.get(selectedMeal[0]));
                    stmt.setDate(4, Date.valueOf(today));
                    stmt.executeUpdate();
                    messageLabel.setTextFill(Color.web("#27AE60"));
                    messageLabel.setText("Meal updated!");
                    new TodayDashboardScreen(user).start(stage);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        deleteMealBtn.setOnAction(e -> {
            if (selectedMeal[0] != null) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    PreparedStatement stmt = conn.prepareStatement("DELETE FROM User_Meals WHERE user_id = ? AND meal_id = ? AND meal_date = ?");
                    stmt.setInt(1, user.getId());
                    stmt.setInt(2, loggedMealToId.get(selectedMeal[0]));
                    stmt.setDate(3, Date.valueOf(today));
                    stmt.executeUpdate();
                    messageLabel.setTextFill(Color.web("#27AE60"));
                    messageLabel.setText("Meal deleted!");
                    new TodayDashboardScreen(user).start(stage);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        updateWorkoutBtn.setOnAction(e -> {
            if (selectedWorkout[0] != null && updateWorkoutBox.getValue() != null) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    PreparedStatement stmt = conn.prepareStatement("UPDATE User_Workouts SET workout_id = ? WHERE user_id = ? AND workout_id = ? AND completion_date = ?");
                    stmt.setInt(1, workoutNameToId.get(updateWorkoutBox.getValue()));
                    stmt.setInt(2, user.getId());
                    stmt.setInt(3, loggedWorkoutToId.get(selectedWorkout[0]));
                    stmt.setDate(4, Date.valueOf(today));
                    stmt.executeUpdate();
                    messageLabel.setTextFill(Color.web("#27AE60"));
                    messageLabel.setText("Workout updated!");
                    new TodayDashboardScreen(user).start(stage);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        deleteWorkoutBtn.setOnAction(e -> {
            if (selectedWorkout[0] != null) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    PreparedStatement stmt = conn.prepareStatement("DELETE FROM User_Workouts WHERE user_id = ? AND workout_id = ? AND completion_date = ?");
                    stmt.setInt(1, user.getId());
                    stmt.setInt(2, loggedWorkoutToId.get(selectedWorkout[0]));
                    stmt.setDate(3, Date.valueOf(today));
                    stmt.executeUpdate();
                    messageLabel.setTextFill(Color.web("#27AE60"));
                    messageLabel.setText("Workout deleted!");
                    new TodayDashboardScreen(user).start(stage);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        backButton.setOnAction(e -> new DashboardScreen(user).start(stage));

        VBox layout = new VBox(12,
                title,
                caloriesInLabel, caloriesOutLabel, netCaloriesLabel,
                intakeGoalLabel, intakeStatus,
                burnGoalLabel, burnStatus,
                new Separator(),
                mealsTitle, mealsList, updateMealBox, updateMealBtn, deleteMealBtn,
                workoutsTitle, workoutsList, updateWorkoutBox, updateWorkoutBtn, deleteWorkoutBtn,
                messageLabel, backButton
        );
        layout.setPadding(new Insets(25));
        layout.setAlignment(Pos.TOP_CENTER);
        layout.setStyle("-fx-background-color: #FDFEFE;");

        Scene scene = new Scene(layout, 530, 730);
        stage.setTitle("Today at a Glance");
        stage.setScene(scene);
        stage.show();
    }
}
