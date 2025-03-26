package com.example.fitsync.ui;

import com.example.fitsync.model.User;
import com.example.fitsync.ui.LogMealScreen;
import com.example.fitsync.ui.DailySummaryScreen;
import com.example.fitsync.ui.ViewMealsScreen;
import com.example.fitsync.ui.UserProfileScreen;
import com.example.fitsync.ui.WeeklyProgressScreen;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DashboardScreen {
    private final User user;

    public DashboardScreen(User user) {
        this.user = user;
    }

    public void start(Stage stage) {
        Label greeting = new Label("Welcome, " + user.getName() + "!");

        Button workoutButton = new Button("Log Workout");
        Button mealButton = new Button("Log Meal");
        Button summaryButton = new Button("View Daily Summary");
        Button logoutButton = new Button("Logout");

        // TODO: Add actions to each button
        workoutButton.setOnAction(e -> {
            new LogWorkoutScreen(user).start(stage);  //  this triggers the screen change
        });

        mealButton.setOnAction(e -> {
            new LogMealScreen(user).start(stage);
        });

        summaryButton.setOnAction(e -> {
            new DailySummaryScreen(user).start(stage);
        });

        Button progressButton = new Button("Weekly Progress");
        progressButton.setOnAction(e -> {
            new WeeklyProgressScreen(user).start(stage);
        });


        Button profileButton = new Button("View / Edit Profile");
        profileButton.setOnAction(e -> {
            new UserProfileScreen(user).start(stage);
        });

        Button viewMealsButton = new Button("View Logged Meals");
        viewMealsButton.setOnAction(e -> {
            new ViewMealsScreen(user).start(stage);
        });


        logoutButton.setOnAction(e -> {
            new LoginScreen().start(stage); // Go back to login screen
        });

        VBox layout = new VBox(10, greeting,
                workoutButton, mealButton, summaryButton,
                viewMealsButton, profileButton, progressButton,
                logoutButton);

        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout, 300, 250);
        stage.setTitle("FitSync - Dashboard");
        stage.setScene(scene);
        stage.show();
    }
}
