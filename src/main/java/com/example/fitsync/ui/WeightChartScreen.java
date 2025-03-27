package com.example.fitsync.ui;

import com.example.fitsync.database.DatabaseConnection;
import com.example.fitsync.model.User;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;

public class WeightChartScreen {
    private final User user;

    public WeightChartScreen(User user) {
        this.user = user;
    }

    public void start(Stage stage) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Date");
        yAxis.setLabel("Weight (kg)");

        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Weight Progress Over Time");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Weight");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT date, weight FROM User_Weights WHERE user_id = ? ORDER BY date"
             )) {
            stmt.setInt(1, user.getId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String date = rs.getDate("date").toString();
                double weight = rs.getDouble("weight");
                series.getData().add(new XYChart.Data<>(date, weight));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        chart.getData().add(series);

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> new DashboardScreen(user).start(stage));

        VBox layout = new VBox(10, chart, backButton);
        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout, 500, 400);
        stage.setTitle("Weight Chart");
        stage.setScene(scene);
        stage.show();
    }
}
