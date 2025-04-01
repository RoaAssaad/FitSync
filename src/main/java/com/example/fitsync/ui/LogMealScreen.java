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
        logButton.setPrefWidth(140);
        logButton.setPrefHeight(35);
        logButton.setStyle("-fx-background-color: #2ECC71; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");

        Button updateButton = new Button("Update Meal");
        updateButton.setPrefWidth(140);
        updateButton.setPrefHeight(35);
        updateButton.setStyle("-fx-background-color: #F1C40F; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");

        Button deleteButton = new Button("Delete Meal");
        deleteButton.setPrefWidth(140);
        deleteButton.setPrefHeight(35);
        deleteButton.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");

        Button backButton = new Button("Back");
        backButton.setPrefWidth(140);
        backButton.setPrefHeight(35);
        backButton.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");

        Label messageLabel = new Label();
        messageLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        messageLabel.setTextFill(Color.web("#E74C3C"));

        // Load meals
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
                        "INSERT INTO Meals (food_name, calories, meal_type) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
                insertMeal.setString(1, name);
                insertMeal.setDouble(2, calories);
                insertMeal.setString(3, type);
                insertMeal.executeUpdate();

                ResultSet rs = insertMeal.getGeneratedKeys();
                int mealId = -1;
                if (rs.next()) mealId = rs.getInt(1);

                PreparedStatement log = conn.prepareStatement(
                        "INSERT INTO User_Meals (user_id, meal_id, meal_date) VALUES (?, ?, ?)");
                log.setInt(1, user.getId());
                log.setInt(2, mealId);
                log.setDate(3, Date.valueOf(date));
                log.executeUpdate();

                messageLabel.setTextFill(Color.web("#27AE60"));
                messageLabel.setText("Meal logged successfully!");
                refreshDropdown(mealDropdown);
            } catch (Exception ex) {
                ex.printStackTrace();
                messageLabel.setText("Error logging meal.");
            }
        });

        updateButton.setOnAction(e -> {
            String selectedMeal = mealDropdown.getValue();
            String newName = customMealField.getText().trim();
            String cal = caloriesField.getText().trim();
            String type = mealTypeBox.getValue();

            if (selectedMeal == null || newName.isEmpty() || cal.isEmpty() || type == null) {
                messageLabel.setText("Select meal & fill new data.");
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                double newCal = Double.parseDouble(cal);

                PreparedStatement update = conn.prepareStatement(
                        "UPDATE Meals SET food_name = ?, calories = ?, meal_type = ? WHERE food_name = ?");
                update.setString(1, newName);
                update.setDouble(2, newCal);
                update.setString(3, type);
                update.setString(4, selectedMeal);
                int rows = update.executeUpdate();

                if (rows > 0) {
                    messageLabel.setTextFill(Color.web("#27AE60"));
                    messageLabel.setText("Meal updated.");
                    refreshDropdown(mealDropdown);
                } else {
                    messageLabel.setText("Update failed.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                messageLabel.setText("Invalid input.");
            }
        });

        deleteButton.setOnAction(e -> {
            String selectedMeal = mealDropdown.getValue();
            if (selectedMeal == null) {
                messageLabel.setText("Select a meal to delete.");
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                PreparedStatement delete = conn.prepareStatement("DELETE FROM Meals WHERE food_name = ?");
                delete.setString(1, selectedMeal);
                int rows = delete.executeUpdate();
                if (rows > 0) {
                    messageLabel.setTextFill(Color.web("#27AE60"));
                    messageLabel.setText("Meal deleted.");
                    refreshDropdown(mealDropdown);
                } else {
                    messageLabel.setText("Delete failed.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                messageLabel.setText("Error deleting meal.");
            }
        });

        backButton.setOnAction(e -> new DashboardScreen(user).start(stage));

        mealDropdown.setOnAction(e -> {
            String selected = mealDropdown.getValue();
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
             ResultSet rs = stmt.executeQuery("SELECT food_name FROM Meals")) {
            while (rs.next()) {
                mealNames.add(rs.getString("food_name"));
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
}
