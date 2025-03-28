package com.example.fitsync.ui;

import com.example.fitsync.model.User;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class DashboardScreen {
    private final User user;

    public DashboardScreen(User user) {
        this.user = user;
    }

    public void start(Stage stage) {
        Label greeting = new Label("Welcome, " + user.getName() + "!");

        // Section buttons
        ToggleGroup sectionGroup = new ToggleGroup();
        RadioButton mealsRadio = new RadioButton("Meals");
        RadioButton workoutsRadio = new RadioButton("Workouts");
        RadioButton progressRadio = new RadioButton("Progress");

        mealsRadio.setToggleGroup(sectionGroup);
        workoutsRadio.setToggleGroup(sectionGroup);
        progressRadio.setToggleGroup(sectionGroup);
        mealsRadio.setSelected(true);

        // Section container
        VBox sectionButtonsBox = new VBox(10);
        sectionButtonsBox.setPadding(new Insets(10));

        // Update section buttons
        Runnable showMeals = () -> {
            sectionButtonsBox.getChildren().setAll(
                    createButton("Log Meal", () -> new LogMealScreen(user).start(stage)),
                    createButton("View Logged Meals", () -> new ViewMealsScreen(user).start(stage))
            );
        };

        Runnable showWorkouts = () -> {
            sectionButtonsBox.getChildren().setAll(
                    createButton("Log Workout", () -> new LogWorkoutScreen(user).start(stage)),
                    createButton("Workout Recommendations", () -> new WorkoutRecommendationsScreen(user).start(stage))
            );
        };

        Runnable showProgress = () -> {
            sectionButtonsBox.getChildren().setAll(
                    createButton("View Daily Summary", () -> new DailySummaryScreen(user).start(stage)),
                    createButton("Today's Summary", () -> new TodayDashboardScreen(user).start(stage)),
                    createButton("Log Weight", () -> new LogWeightScreen(user).start(stage)),
                    createButton("View Weight Chart", () -> new WeightChartScreen(user).start(stage)),
                    createButton("Weekly Progress", () -> new WeeklyProgressScreen(user).start(stage)),
                    createButton("Set Daily Goals", () -> new GoalScreen(user).start(stage)),
                    createButton("View/Edit Profile", () -> new UserProfileScreen(user).start(stage))
            );
        };

        // Initial section load
        showMeals.run();

        // Ensure updates even if same radio clicked again
        mealsRadio.setOnMouseClicked(e -> showMeals.run());
        workoutsRadio.setOnMouseClicked(e -> showWorkouts.run());
        progressRadio.setOnMouseClicked(e -> showProgress.run());

        // Logout button
        Button logoutBtn = new Button("Logout");
        logoutBtn.setOnAction(e -> new LoginScreen().start(stage));

        // Top panel
        VBox topPanel = new VBox(10, greeting,
                new Label("Choose Section:"),
                new HBox(10, mealsRadio, workoutsRadio, progressRadio),
                new Separator()
        );
        topPanel.setPadding(new Insets(10));

        // Layout using BorderPane for cleaner structure
        BorderPane layout = new BorderPane();
        layout.setTop(topPanel);
        layout.setCenter(sectionButtonsBox);
        layout.setBottom(logoutBtn);
        BorderPane.setMargin(logoutBtn, new Insets(10));

        Scene scene = new Scene(layout, 400, 500);
        stage.setTitle("FitSync - Dashboard");
        stage.setScene(scene);
        stage.show();
    }

    private Button createButton(String label, Runnable action) {
        Button btn = new Button(label);
        btn.setOnAction(e -> action.run());
        return btn;
    }
}
