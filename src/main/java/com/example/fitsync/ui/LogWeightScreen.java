package com.example.fitsync.ui;

import com.example.fitsync.database.DatabaseConnection;
import com.example.fitsync.model.User;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
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
        DatePicker datePicker = new DatePicker(LocalDate.now());
        TextField weightField = new TextField();
        weightField.setPromptText("Enter your weight (kg)");

        Button saveButton = new Button("Save");
        Button backButton = new Button("Back");
        Label status = new Label();

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
                    status.setText(" Weight logged for " + date);
                }
            } catch (Exception ex) {
                status.setText(" Please enter a valid weight.");
            }
        });

        backButton.setOnAction(e -> new DashboardScreen(user).start(stage));

        VBox layout = new VBox(10, title, datePicker, weightField, saveButton, backButton, status);
        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout, 350, 300);
        stage.setTitle("Log Weight");
        stage.setScene(scene);
        stage.show();
    }
}
