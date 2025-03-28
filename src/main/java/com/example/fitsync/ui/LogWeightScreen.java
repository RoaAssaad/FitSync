package com.example.fitsync.ui;

import com.example.fitsync.database.DatabaseConnection;
import com.example.fitsync.model.User;
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

public class LogWeightScreen {
    private final User user;

    public LogWeightScreen(User user) {
        this.user = user;
    }

    public void start(Stage stage) {
        Label title = new Label("Log Today’s Weight");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#2C3E50"));

        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setPrefHeight(40);
        datePicker.setStyle("-fx-background-color: #ECF0F1; -fx-border-color: #BDC3C7; -fx-border-radius: 5; -fx-background-radius: 5;");

        TextField weightField = new TextField();
        weightField.setPromptText("Enter your weight (kg)");
        weightField.setPrefHeight(40);
        weightField.setStyle("-fx-background-color: #ECF0F1; -fx-border-color: #BDC3C7; -fx-border-radius: 5; -fx-background-radius: 5;");

        Button saveButton = new Button("Save");
        saveButton.setPrefWidth(140);
        saveButton.setPrefHeight(35);
        saveButton.setStyle("-fx-background-color: #2ECC71; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");

        Button backButton = new Button("Back");
        backButton.setPrefWidth(140);
        backButton.setPrefHeight(35);
        backButton.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");

        Label status = new Label();
        status.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        status.setTextFill(Color.web("#E74C3C"));

        saveButton.setOnAction(e -> {
            try {
                double weight = Double.parseDouble(weightField.getText().trim());
                LocalDate date = datePicker.getValue();

                try (Connection conn = DatabaseConnection.getConnection()) {
                    PreparedStatement stmt = conn.prepareStatement(
                            "REPLACE INTO User_Weights (user_id, date, weight) VALUES (?, ?, ?)"
                    );
                    stmt.setInt(1, user.getId());
                    stmt.setDate(2, Date.valueOf(date));
                    stmt.setDouble(3, weight);

                    stmt.executeUpdate();
                    status.setTextFill(Color.web("#27AE60")); // success green
                    status.setText("Weight logged for " + date);
                }
            } catch (Exception ex) {
                status.setTextFill(Color.web("#E74C3C")); // error red
                status.setText("Please enter a valid weight.");
            }
        });

        backButton.setOnAction(e -> new DashboardScreen(user).start(stage));

        VBox layout = new VBox(12, title, datePicker, weightField, saveButton, backButton, status);
        layout.setPadding(new Insets(25));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #FDFEFE;");

        Scene scene = new Scene(layout, 400, 330);
        stage.setTitle("Log Weight");
        stage.setScene(scene);
        stage.show();
    }
}
