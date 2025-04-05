package com.example.fitsync.ui;

import com.example.fitsync.database.DatabaseConnection;
import com.example.fitsync.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;

public class WeeklyProgressScreen {
    private final User user;

    public WeeklyProgressScreen(User user) {
        this.user = user;
    }

    public void start(Stage stage) {
        boolean wasFullScreen = stage.isFullScreen(); //  save fullscreen state

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Date");
        yAxis.setLabel("Calories");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Weekly Calorie Summary");
        barChart.setLegendVisible(true);
        barChart.setCategoryGap(10);
        barChart.setBarGap(4);
        barChart.setStyle("-fx-background-color: #FDFEFE;");
        barChart.setMaxWidth(600);

        XYChart.Series<String, Number> consumedSeries = new XYChart.Series<>();
        consumedSeries.setName("Calories Consumed");

        XYChart.Series<String, Number> burnedSeries = new XYChart.Series<>();
        burnedSeries.setName("Calories Burned");

        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(6); // Last 7 days

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT date, calories_consumed, calories_burned FROM Daily_Summary " +
                             "WHERE user_id = ? AND date BETWEEN ? AND ? ORDER BY date"
             )) {

            stmt.setInt(1, user.getId());
            stmt.setDate(2, Date.valueOf(weekAgo));
            stmt.setDate(3, Date.valueOf(today));

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String dateStr = rs.getDate("date").toString();
                consumedSeries.getData().add(new XYChart.Data<>(dateStr, rs.getDouble("calories_consumed")));
                burnedSeries.getData().add(new XYChart.Data<>(dateStr, rs.getDouble("calories_burned")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        barChart.getData().addAll(consumedSeries, burnedSeries);

        Button backButton = new Button("Back");
        backButton.setPrefWidth(160);
        backButton.setPrefHeight(35);
        backButton.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
        backButton.setOnAction(e -> {
            new DashboardScreen(user).start(stage);
            stage.setFullScreen(wasFullScreen); //  restore fullscreen after switching back
        });

        VBox layout = new VBox(20, barChart, backButton);
        layout.setPadding(new Insets(25));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #FDFEFE;");
        layout.prefWidthProperty().bind(stage.widthProperty());
        layout.prefHeightProperty().bind(stage.heightProperty());

        Scene scene = new Scene(layout);
        stage.setTitle("Weekly Progress");
        stage.setScene(scene);
        stage.setFullScreen(wasFullScreen);
        stage.show();
    }
}
