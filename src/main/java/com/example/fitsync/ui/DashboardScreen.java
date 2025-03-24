package com.example.fitsync.ui;

import com.example.fitsync.model.User;
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
            // Navigate to meal screen
        });

        summaryButton.setOnAction(e -> {
            // Navigate to summary screen
        });

        logoutButton.setOnAction(e -> {
            new LoginScreen().start(stage); // Go back to login screen
        });

        VBox layout = new VBox(10, greeting, workoutButton, mealButton, summaryButton, logoutButton);
        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout, 300, 250);
        stage.setTitle("FitSync - Dashboard");
        stage.setScene(scene);
        stage.show();
    }
}
