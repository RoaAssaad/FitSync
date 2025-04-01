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

public class LogMealScreen {
    private final User user;
    private ObservableList<String> mealNames = FXCollections.observableArrayList();

    public LogMealScreen(User user) {
        this.user = user;
    }

    public void start(Stage stage) {
        Label title = new Label("Log a Meal");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#2C3E50"));

        ComboBox<String> mealDropdown = new ComboBox<>();
        mealDropdown.setPromptText("Select Meal");
        mealDropdown.setPrefHeight(40);
        mealDropdown.setStyle("-fx-background-color: #ECF0F1; -fx-border-color: #BDC3C7; -fx-border-radius: 5; -fx-background-radius: 5;");

        TextField customMealField = new TextField();
        customMealField.setPromptText("Or enter custom meal name");
        customMealField.setPrefHeight(40);
        customMealField.setStyle("-fx-background-color: #FBFCFC; -fx-border-color: #BDC3C7; -fx-border-radius: 5; -fx-background-radius: 5;");

        TextField caloriesField = new TextField();
        caloriesField.setPromptText("Calories");
        caloriesField.setPrefHeight(40);

        ComboBox<String> mealTypeBox = new ComboBox<>();
        mealTypeBox.setPromptText("Meal Type");
        mealTypeBox.setItems(FXCollections.observableArrayList("Breakfast", "Lunch", "Dinner", "Snack"));
        mealTypeBox.setPrefHeight(40);

        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setPrefHeight(40);
        datePicker.setStyle("-fx-background-color: #ECF0F1; -fx-border-color: #BDC3C7; -fx-border-radius: 5; -fx-background-radius: 5;");

        Button logButton = new Button("Log Meal");
        Button updateButton = new Button("Update Meal");
        Button deleteButton = new Button("Delete Meal");
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

        loadMeals(mealDropdown);

        logButton.setOnAction(e -> {
            String name = customMealField.getText().trim();
            String type = mealTypeBox.getValue();
            String cal = caloriesField.getText().trim();
            LocalDate date = datePicker.getValue();

            if (name.isEmpty() || type == null || cal.isEmpty() || date == null) {
                messageLabel.setText("Please complete all fields.");
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                double calories = Double.parseDouble(cal);

                PreparedStatement insertMeal = conn.prepareStatement(
                        "INSERT IGNORE INTO Meals (food_name, calories, meal_type) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
                insertMeal.setString(1, name);
                insertMeal.setDouble(2, calories);
                insertMeal.setString(3, type);
                insertMeal.executeUpdate();

                int mealId;
                PreparedStatement getId = conn.prepareStatement("SELECT id FROM Meals WHERE food_name = ?");
                getId.setString(1, name);
                ResultSet rs = getId.executeQuery();
                if (rs.next()) {
                    mealId = rs.getInt("id");
                } else {
                    messageLabel.setText("Error retrieving meal ID.");
                    return;
                }

                PreparedStatement log = conn.prepareStatement("INSERT INTO User_Meals (user_id, meal_id, meal_date) VALUES (?, ?, ?)");
                log.setInt(1, user.getId());
                log.setInt(2, mealId);
                log.setDate(3, Date.valueOf(date));
                log.executeUpdate();

                updateDailySummary(conn, user.getId(), date);

                messageLabel.setTextFill(Color.web("#27AE60"));
                messageLabel.setText("Meal logged successfully!");
                refreshDropdown(mealDropdown);
            } catch (Exception ex) {
                ex.printStackTrace();
                messageLabel.setText("Error logging meal.");
            }
        });

        updateButton.setOnAction(e -> {
            String selected = mealDropdown.getValue();
            String newName = customMealField.getText().trim();
            String cal = caloriesField.getText().trim();
            String type = mealTypeBox.getValue();

            if (selected == null || newName.isEmpty() || cal.isEmpty() || type == null) {
                messageLabel.setText("Select meal & fill new data.");
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                double newCal = Double.parseDouble(cal);

                PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE Meals SET food_name = ?, calories = ?, meal_type = ? WHERE food_name = ?");
                stmt.setString(1, newName);
                stmt.setDouble(2, newCal);
                stmt.setString(3, type);
                stmt.setString(4, selected);
                stmt.executeUpdate();

                messageLabel.setTextFill(Color.web("#27AE60"));
                messageLabel.setText("Meal updated.");
                refreshDropdown(mealDropdown);
            } catch (Exception ex) {
                ex.printStackTrace();
                messageLabel.setText("Update failed.");
            }
        });

        deleteButton.setOnAction(e -> {
            String selected = mealDropdown.getValue();
            if (selected == null) {
                messageLabel.setText("Select a meal to delete.");
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                PreparedStatement stmt = conn.prepareStatement("DELETE FROM Meals WHERE food_name = ?");
                stmt.setString(1, selected);
                stmt.executeUpdate();

                messageLabel.setTextFill(Color.web("#27AE60"));
                messageLabel.setText("Meal deleted.");
                refreshDropdown(mealDropdown);
            } catch (Exception ex) {
                ex.printStackTrace();
                messageLabel.setText("Delete failed.");
            }
        });

        mealDropdown.setOnAction(e -> {
            String selected = mealDropdown.getValue();
            if (selected == null) return;
            try (Connection conn = DatabaseConnection.getConnection()) {
                PreparedStatement stmt = conn.prepareStatement("SELECT calories, meal_type FROM Meals WHERE food_name = ?");
                stmt.setString(1, selected);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    caloriesField.setText(String.valueOf(rs.getDouble("calories")));
                    mealTypeBox.setValue(rs.getString("meal_type"));
                    customMealField.setText(selected);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        backButton.setOnAction(e -> new DashboardScreen(user).start(stage));

        VBox layout = new VBox(12,
                title, mealDropdown, customMealField, caloriesField,
                mealTypeBox, datePicker,
                logButton, updateButton, deleteButton,
                backButton, messageLabel);
        layout.setPadding(new Insets(25));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #FDFEFE;");

        Scene scene = new Scene(layout, 440, 560);
        stage.setTitle("Log Meal");
        stage.setScene(scene);
        stage.show();
    }

    private void loadMeals(ComboBox<String> box) {
        mealNames.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT DISTINCT food_name FROM Meals")) {
            while (rs.next()) {
                String name = rs.getString("food_name");
                if (!mealNames.contains(name)) {
                    mealNames.add(name);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        box.setItems(mealNames);
    }

    private void refreshDropdown(ComboBox<String> box) {
        loadMeals(box);
        box.setValue(null);
        box.setPromptText("Select Meal");
    }

    private void updateDailySummary(Connection conn, int userId, LocalDate date) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                "SELECT SUM(m.calories) AS total " +
                        "FROM User_Meals um JOIN Meals m ON um.meal_id = m.id " +
                        "WHERE um.user_id = ? AND um.meal_date = ?");
        stmt.setInt(1, userId);
        stmt.setDate(2, Date.valueOf(date));
        ResultSet rs = stmt.executeQuery();

        double totalCalories = 0.0;
        if (rs.next()) totalCalories = rs.getDouble("total");

        PreparedStatement check = conn.prepareStatement(
                "SELECT id FROM Daily_Summary WHERE user_id = ? AND date = ?");
        check.setInt(1, userId);
        check.setDate(2, Date.valueOf(date));
        ResultSet checkRs = check.executeQuery();

        if (checkRs.next()) {
            PreparedStatement update = conn.prepareStatement(
                    "UPDATE Daily_Summary SET calories_consumed = ? WHERE user_id = ? AND date = ?");
            update.setDouble(1, totalCalories);
            update.setInt(2, userId);
            update.setDate(3, Date.valueOf(date));
            update.executeUpdate();
        } else {
            PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO Daily_Summary (user_id, calories_consumed, calories_burned, date) VALUES (?, ?, 0, ?)");
            insert.setInt(1, userId);
            insert.setDouble(2, totalCalories);
            insert.setDate(3, Date.valueOf(date));
            insert.executeUpdate();
        }
    }

}//
