package com.example.fitsync.ui;

import com.example.fitsync.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class DashboardScreen {
    private final User user;

    public DashboardScreen(User user) {
        this.user = user;
    }

    public void start(Stage stage) {
        Label greeting = new Label("Welcome, " + user.getName() + "!");
        greeting.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        greeting.setTextFill(Color.web("#2C3E50"));

        ToggleGroup sectionGroup = new ToggleGroup();
        RadioButton mealsRadio = new RadioButton("Meals");
        RadioButton workoutsRadio = new RadioButton("Workouts");
        RadioButton progressRadio = new RadioButton("Progress");

        mealsRadio.setToggleGroup(sectionGroup);
        workoutsRadio.setToggleGroup(sectionGroup);
        progressRadio.setToggleGroup(sectionGroup);
        mealsRadio.setSelected(true);

        // Style radio buttons
        for (RadioButton rb : new RadioButton[]{mealsRadio, workoutsRadio, progressRadio}) {
            rb.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
            rb.setTextFill(Color.web("#34495E"));
        }

        VBox sectionButtonsBox = new VBox(10);
        sectionButtonsBox.setPadding(new Insets(10));
        sectionButtonsBox.setAlignment(Pos.CENTER);

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

        showMeals.run();

        mealsRadio.setOnMouseClicked(e -> showMeals.run());
        workoutsRadio.setOnMouseClicked(e -> showWorkouts.run());
        progressRadio.setOnMouseClicked(e -> showProgress.run());

        Button logoutBtn = new Button("Logout");
        logoutBtn.setPrefWidth(140);
        logoutBtn.setPrefHeight(35);
        logoutBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
        logoutBtn.setOnAction(e -> new LoginScreen().start(stage));

        VBox topPanel = new VBox(10,
                greeting,
                new Label("Choose Section:"),
                new HBox(10, mealsRadio, workoutsRadio, progressRadio),
                new Separator()
        );
        topPanel.setPadding(new Insets(15));
        topPanel.setAlignment(Pos.CENTER);

        BorderPane layout = new BorderPane();
        layout.setTop(topPanel);
        layout.setCenter(sectionButtonsBox);
        layout.setBottom(logoutBtn);
        BorderPane.setAlignment(logoutBtn, Pos.CENTER);
        BorderPane.setMargin(logoutBtn, new Insets(15));
        layout.setStyle("-fx-background-color: #FDFEFE;");

        Scene scene = new Scene(layout, 420, 520);
        stage.setTitle("FitSync - Dashboard");
        stage.setScene(scene);
        stage.show();
    }

    private Button createButton(String label, Runnable action) {
        Button btn = new Button(label);
        btn.setPrefWidth(200);
        btn.setPrefHeight(40);
        btn.setStyle("-fx-background-color: #2ECC71; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
        btn.setOnAction(e -> action.run());
        return btn;
    }
}
