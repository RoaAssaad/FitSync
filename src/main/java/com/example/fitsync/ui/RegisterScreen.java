package com.example.fitsync.ui;

import com.example.fitsync.model.User;
import com.example.fitsync.service.UserService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class RegisterScreen {

    public void start(Stage stage) {
        boolean wasFullScreen = stage.isFullScreen(); //  save fullscreen state

        Label title = new Label("FitSync - Register");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#2C3E50"));

        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        nameField.setPrefHeight(40);
        nameField.setStyle("-fx-background-color: #ECF0F1; -fx-border-color: #BDC3C7; -fx-border-radius: 5; -fx-background-radius: 5;");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setPrefHeight(40);
        emailField.setStyle("-fx-background-color: #ECF0F1; -fx-border-color: #BDC3C7; -fx-border-radius: 5; -fx-background-radius: 5;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefHeight(40);
        passwordField.setStyle("-fx-background-color: #ECF0F1; -fx-border-color: #BDC3C7; -fx-border-radius: 5; -fx-background-radius: 5;");

        TextField ageField = new TextField();
        ageField.setPromptText("Age");
        ageField.setPrefHeight(40);
        ageField.setStyle("-fx-background-color: #ECF0F1; -fx-border-color: #BDC3C7; -fx-border-radius: 5; -fx-background-radius: 5;");

        ComboBox<String> genderBox = new ComboBox<>();
        genderBox.getItems().addAll("M", "F");
        genderBox.setPromptText("Gender");
        genderBox.setPrefHeight(40);
        genderBox.setStyle("-fx-background-color: #ECF0F1; -fx-border-color: #BDC3C7; -fx-border-radius: 5; -fx-background-radius: 5;");

        TextField weightField = new TextField();
        weightField.setPromptText("Weight (kg)");
        weightField.setPrefHeight(40);
        weightField.setStyle("-fx-background-color: #ECF0F1; -fx-border-color: #BDC3C7; -fx-border-radius: 5; -fx-background-radius: 5;");

        TextField heightField = new TextField();
        heightField.setPromptText("Height (cm)");
        heightField.setPrefHeight(40);
        heightField.setStyle("-fx-background-color: #ECF0F1; -fx-border-color: #BDC3C7; -fx-border-radius: 5; -fx-background-radius: 5;");

        Button registerButton = new Button("Register");
        registerButton.setPrefWidth(140);
        registerButton.setPrefHeight(35);
        registerButton.setStyle("-fx-background-color: #2ECC71; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");

        Button backButton = new Button("Back to Login");
        backButton.setPrefWidth(140);
        backButton.setPrefHeight(35);
        backButton.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");

        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: red;");

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
                    messageLabel.setStyle("-fx-text-fill: green;");
                } else {
                    messageLabel.setText("Error: Email might already be in use.");
                }

            } catch (Exception ex) {
                messageLabel.setText("Please enter valid values.");
            }
        });

        backButton.setOnAction(e -> {
            new LoginScreen().start(stage);
            stage.setFullScreen(wasFullScreen); //  restore fullscreen state when going back
        });

        VBox layout = new VBox(12,
                title, nameField, emailField, passwordField,
                ageField, genderBox, weightField, heightField,
                registerButton, backButton, messageLabel);
        layout.setPadding(new Insets(25));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #FDFEFE;");

        //  stretch to fit fullscreen window if active
        layout.prefWidthProperty().bind(stage.widthProperty());
        layout.prefHeightProperty().bind(stage.heightProperty());

        Scene scene = new Scene(layout);
        stage.setTitle("FitSync - Register");
        stage.setScene(scene);

        stage.setFullScreen(wasFullScreen); //  re-apply fullscreen for consistency
        stage.show();
    }
}
