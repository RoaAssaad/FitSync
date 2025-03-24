package com.example.fitsync.ui;

import com.example.fitsync.model.User;
import com.example.fitsync.service.UserService;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class RegisterScreen {

    public void start(Stage stage) {
        Label title = new Label("FitSync - Register");

        TextField nameField = new TextField();
        nameField.setPromptText("Name");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        TextField ageField = new TextField();
        ageField.setPromptText("Age");

        ComboBox<String> genderBox = new ComboBox<>();
        genderBox.getItems().addAll("M", "F");
        genderBox.setPromptText("Gender");

        TextField weightField = new TextField();
        weightField.setPromptText("Weight (kg)");

        TextField heightField = new TextField();
        heightField.setPromptText("Height (cm)");

        Button registerButton = new Button("Register");
        Button backButton = new Button("Back to Login");
        Label messageLabel = new Label();

        registerButton.setOnAction(e -> {
            try {
                String name = nameField.getText().trim();
                String email = emailField.getText().trim();
                String password = passwordField.getText().trim();
                int age = Integer.parseInt(ageField.getText().trim());
                String gender = genderBox.getValue();
                double weight = Double.parseDouble(weightField.getText().trim());
                double height = Double.parseDouble(heightField.getText().trim());

                User user = new User(name, email, password, age, gender, weight, height);
                UserService userService = new UserService();

                boolean success = userService.addUser(user);
                if (success) {
                    messageLabel.setText("Registration successful! You can now log in.");
                } else {
                    messageLabel.setText("Error: Email might already be in use.");
                }

            } catch (Exception ex) {
                messageLabel.setText("Please enter valid values.");
            }
        });


        backButton.setOnAction(e -> {
            new LoginScreen().start(stage);
        });

        VBox layout = new VBox(10,
                title, nameField, emailField, passwordField,
                ageField, genderBox, weightField, heightField,
                registerButton, backButton, messageLabel);
        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout, 350, 450);
        stage.setTitle("FitSync - Register");
        stage.setScene(scene);
        stage.show();
    }
}
