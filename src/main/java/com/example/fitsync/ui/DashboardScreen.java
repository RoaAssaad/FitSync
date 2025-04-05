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
        boolean wasFullScreen = stage.isFullScreen();

        //  Menu Bar
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");
        MenuItem logoutItem = new MenuItem("Logout");
        logoutItem.setOnAction(e -> {
            new LoginScreen().start(stage);
            stage.setFullScreen(false);
        });
        fileMenu.getItems().add(logoutItem);

        Menu aboutMenu = new Menu("About");
        MenuItem aboutItem = new MenuItem("About FitSync");
        aboutItem.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("About");
            alert.setHeaderText("FitSync Application");
            alert.setContentText(
                    "FitSync helps users track meals, workouts, and monitor fitness progress.\n\n" +
                            "Developed by:\n" +
                            "• Roa Al Assaad\n" +
                            "• Michel Mitri\n\n" +
                            "Supervised by:\n" +
                            "Dr. Imad Zakhem\n\n"

            );

            alert.showAndWait();
        });
        aboutMenu.getItems().add(aboutItem);

        menuBar.getMenus().addAll(fileMenu, aboutMenu);

        //  Greeting
        Label greeting = new Label("Welcome, " + user.getName() + "!");
        greeting.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        greeting.setTextFill(Color.web("#2C3E50"));

        // TabPane
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setPrefHeight(35); // skinnier tabs
        tabPane.setMinWidth(800); // make it stretch horizontally
        tabPane.setMaxWidth(Double.MAX_VALUE);
        tabPane.setTabMinWidth(150);

        // Tabs
        Tab mealsTab = new Tab("Meals");
        Tab workoutsTab = new Tab("Workouts");
        Tab progressTab = new Tab("Progress");

        tabPane.getTabs().addAll(mealsTab, workoutsTab, progressTab);

        //  Buttons per section
        VBox mealsBox = new VBox(12,
                createButton("Log Meal", () -> new LogMealScreen(user).start(stage)),
                createButton("View Logged Meals", () -> new ViewMealsScreen(user).start(stage))
        );

        VBox workoutsBox = new VBox(12,
                createButton("Log Workout", () -> new LogWorkoutScreen(user).start(stage)),
                createButton("Workout Recommendations", () -> new WorkoutRecommendationsScreen(user).start(stage))
        );

        VBox progressBox = new VBox(12,
                createButton("View Daily Summary", () -> new DailySummaryScreen(user).start(stage)),
                createButton("Today's Summary", () -> new TodayDashboardScreen(user).start(stage)),
                createButton("Log Weight", () -> new LogWeightScreen(user).start(stage)),
                createButton("View Weight Chart", () -> new WeightChartScreen(user).start(stage)),
                createButton("Weekly Progress", () -> new WeeklyProgressScreen(user).start(stage)),
                createButton("Set Daily Goals", () -> new GoalScreen(user).start(stage)),
                createButton("View/Edit Profile", () -> new UserProfileScreen(user).start(stage))
        );

        for (VBox box : new VBox[]{mealsBox, workoutsBox, progressBox}) {
            box.setAlignment(Pos.CENTER);
            box.setPadding(new Insets(30));
        }

        // Assign dummy content (required by JavaFX)
        mealsTab.setContent(new Pane());
        workoutsTab.setContent(new Pane());
        progressTab.setContent(new Pane());

        // Main layout
        VBox centerButtons = new VBox(); // where we show current tab's buttons
        centerButtons.setAlignment(Pos.CENTER);
        centerButtons.setPadding(new Insets(10));

        // Tab content switcher
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == mealsTab) {
                centerButtons.getChildren().setAll(mealsBox);
            } else if (newTab == workoutsTab) {
                centerButtons.getChildren().setAll(workoutsBox);
            } else if (newTab == progressTab) {
                centerButtons.getChildren().setAll(progressBox);
            }
        });

        // Trigger default tab programmatically
        tabPane.getSelectionModel().select(mealsTab);
        centerButtons.getChildren().setAll(mealsBox);

        // Header
        VBox header = new VBox(10, greeting, tabPane);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(20, 0, 10, 0));

        // Main container
        BorderPane layout = new BorderPane();
        layout.setTop(new VBox(menuBar, header));
        layout.setCenter(centerButtons);
        layout.setStyle("-fx-background-color: #FDFEFE;");
        layout.prefWidthProperty().bind(stage.widthProperty());
        layout.prefHeightProperty().bind(stage.heightProperty());

        // Show stage
        Scene scene = new Scene(layout);
        stage.setScene(scene);
        stage.setTitle("FitSync - Dashboard");
        stage.setFullScreen(wasFullScreen);
        stage.show();
    }

    private Button createButton(String label, Runnable action) {
        Button btn = new Button(label);
        btn.setPrefWidth(220);
        btn.setPrefHeight(40);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        btn.setStyle("-fx-background-color: #2ECC71; -fx-text-fill: white; -fx-background-radius: 10;");
        btn.setOnAction(e -> action.run());
        return btn;
    }
}
