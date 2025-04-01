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
import java.util.HashMap;
import java.util.Map;

public class LogWorkoutScreen {
    private final User user;
    private final ObservableList<String> workoutNames = FXCollections.observableArrayList();
    private final Map<String, Integer> workoutNameToId = new HashMap<>();

    public LogWorkoutScreen(User user) {
        this.user = user;
    }

    public void start(Stage stage) {
        Label title = new Label("Log a Workout");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#2C3E50"));

        ComboBox<String> workoutDropdown = new ComboBox<>();
        workoutDropdown.setPromptText("Select Workout");
        workoutDropdown.setPrefHeight(40);
        workoutDropdown.setStyle("-fx-background-color: #ECF0F1; -fx-border-color: #BDC3C7; -fx-border-radius: 5; -fx-background-radius: 5;");

        TextField workoutNameField = new TextField();
        workoutNameField.setPromptText("Workout name");
        workoutNameField.setPrefHeight(40);
        workoutNameField.setStyle("-fx-background-color: #FBFCFC; -fx-border-color: #BDC3C7; -fx-border-radius: 5; -fx-background-radius: 5;");

        TextField caloriesField = new TextField();
        caloriesField.setPromptText("Calories Burned");
        caloriesField.setPrefHeight(40);

        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setPrefHeight(40);
        datePicker.setStyle("-fx-background-color: #ECF0F1; -fx-border-color: #BDC3C7; -fx-border-radius: 5; -fx-background-radius: 5;");

        Button logButton = new Button("Log Workout");
        Button updateButton = new Button("Update Workout");
        Button deleteButton = new Button("Delete Workout");
        Button backButton = new Button("Back");

        for (Button b : new Button[]{logButton, updateButton, deleteButton, backButton}) {
            b.setPrefHeight(35);
            b.setPrefWidth(140);
        }

        logButton.setStyle("-fx-background-color: #2ECC71; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
        updateButton.setStyle("-fx-background-color: #F1C40F; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
        deleteButton.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
        backButton.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");

        Label messageLabel = new Label();
        messageLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        messageLabel.setTextFill(Color.web("#E74C3C"));

        loadWorkouts(workoutDropdown);

        logButton.setOnAction(e -> {
            String name = workoutNameField.getText().trim();
            String caloriesText = caloriesField.getText().trim();
            LocalDate date = datePicker.getValue();

            if (name.isEmpty() || caloriesText.isEmpty() || date == null) {
                messageLabel.setText("Fill in all fields.");
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                double calories = Double.parseDouble(caloriesText);

                PreparedStatement insertWorkout = conn.prepareStatement(
                        "INSERT INTO Workouts (name, duration) VALUES (?, ?) ON DUPLICATE KEY UPDATE duration = VALUES(duration)", Statement.RETURN_GENERATED_KEYS);
                insertWorkout.setString(1, name);
                insertWorkout.setInt(2, (int) calories);
                insertWorkout.executeUpdate();

                int workoutId;
                PreparedStatement getId = conn.prepareStatement("SELECT id FROM Workouts WHERE name = ?");
                getId.setString(1, name);
                ResultSet rs = getId.executeQuery();
                if (rs.next()) {
                    workoutId = rs.getInt("id");
                } else {
                    messageLabel.setText("Error retrieving workout ID.");
                    return;
                }

                PreparedStatement log = conn.prepareStatement("INSERT INTO User_Workouts (user_id, workout_id, completion_date) VALUES (?, ?, ?)");
                log.setInt(1, user.getId());
                log.setInt(2, workoutId);
                log.setDate(3, Date.valueOf(date));
                log.executeUpdate();

                updateSummaryCaloriesBurned(conn, date);

                messageLabel.setTextFill(Color.web("#27AE60"));
                messageLabel.setText("Workout logged!");
                refreshDropdown(workoutDropdown);
            } catch (Exception ex) {
                ex.printStackTrace();
                messageLabel.setText("Invalid input.");
            }
        });

        updateButton.setOnAction(e -> {
            String selected = workoutDropdown.getValue();
            String newName = workoutNameField.getText().trim();
            String newCal = caloriesField.getText().trim();

            if (selected == null || newName.isEmpty() || newCal.isEmpty()) {
                messageLabel.setText("Select workout and enter new values.");
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                int workoutId = workoutNameToId.get(selected);
                PreparedStatement stmt = conn.prepareStatement("UPDATE Workouts SET name = ?, duration = ? WHERE id = ?");
                stmt.setString(1, newName);
                stmt.setInt(2, Integer.parseInt(newCal));
                stmt.setInt(3, workoutId);
                stmt.executeUpdate();

                messageLabel.setTextFill(Color.web("#27AE60"));
                messageLabel.setText("Workout updated.");
                refreshDropdown(workoutDropdown);
            } catch (Exception ex) {
                ex.printStackTrace();
                messageLabel.setText("Update failed.");
            }
        });

        deleteButton.setOnAction(e -> {
            String selected = workoutDropdown.getValue();
            if (selected == null) {
                messageLabel.setText("Select a workout to delete.");
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                int workoutId = workoutNameToId.get(selected);
                PreparedStatement stmt = conn.prepareStatement("DELETE FROM Workouts WHERE id = ?");
                stmt.setInt(1, workoutId);
                stmt.executeUpdate();

                messageLabel.setTextFill(Color.web("#27AE60"));
                messageLabel.setText("Workout deleted.");
                refreshDropdown(workoutDropdown);
            } catch (Exception ex) {
                ex.printStackTrace();
                messageLabel.setText("Delete failed.");
            }
        });

        workoutDropdown.setOnAction(e -> {
            String selected = workoutDropdown.getValue();
            if (selected == null) return;
            try (Connection conn = DatabaseConnection.getConnection()) {
                PreparedStatement stmt = conn.prepareStatement("SELECT duration FROM Workouts WHERE name = ?");
                stmt.setString(1, selected);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    workoutNameField.setText(selected);
                    caloriesField.setText(String.valueOf(rs.getInt("duration")));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        backButton.setOnAction(e -> new DashboardScreen(user).start(stage));

        VBox layout = new VBox(12,
                title, workoutDropdown, workoutNameField, caloriesField, datePicker,
                logButton, updateButton, deleteButton, backButton, messageLabel
        );
        layout.setPadding(new Insets(25));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #FDFEFE;");

        Scene scene = new Scene(layout, 440, 560);
        stage.setTitle("Log Workout");
        stage.setScene(scene);
        stage.show();
    }

    private void loadWorkouts(ComboBox<String> dropdown) {
        workoutNames.clear();
        workoutNameToId.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name FROM Workouts")) {
            while (rs.next()) {
                String name = rs.getString("name");
                int id = rs.getInt("id");
                if (!workoutNames.contains(name)) {
                    workoutNames.add(name);
                    workoutNameToId.put(name, id);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        dropdown.setItems(workoutNames);
    }

    private void refreshDropdown(ComboBox<String> dropdown) {
        loadWorkouts(dropdown);
        dropdown.setValue(null);
        dropdown.setPromptText("Select Workout");
    }

    private void updateSummaryCaloriesBurned(Connection conn, LocalDate date) throws SQLException {
        PreparedStatement sumStmt = conn.prepareStatement(
                "SELECT SUM(duration) FROM User_Workouts uw JOIN Workouts w ON uw.workout_id = w.id WHERE uw.user_id = ? AND uw.completion_date = ?");
        sumStmt.setInt(1, user.getId());
        sumStmt.setDate(2, Date.valueOf(date));
        ResultSet rs = sumStmt.executeQuery();
        double total = 0.0;
        if (rs.next()) total = rs.getDouble(1);

        PreparedStatement check = conn.prepareStatement("SELECT id FROM Daily_Summary WHERE user_id = ? AND date = ?");
        check.setInt(1, user.getId());
        check.setDate(2, Date.valueOf(date));
        ResultSet checkRs = check.executeQuery();

        if (checkRs.next()) {
            PreparedStatement update = conn.prepareStatement("UPDATE Daily_Summary SET calories_burned = ? WHERE user_id = ? AND date = ?");
            update.setDouble(1, total);
            update.setInt(2, user.getId());
            update.setDate(3, Date.valueOf(date));
            update.executeUpdate();
        } else {
            PreparedStatement insert = conn.prepareStatement("INSERT INTO Daily_Summary (user_id, calories_consumed, calories_burned, date) VALUES (?, 0, ?, ?)");
            insert.setInt(1, user.getId());
            insert.setDouble(2, total);
            insert.setDate(3, Date.valueOf(date));
            insert.executeUpdate();
        }
    }
}
//