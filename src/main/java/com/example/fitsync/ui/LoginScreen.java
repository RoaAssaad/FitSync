package com.example.fitsync.ui;

import com.example.fitsync.model.User;
import com.example.fitsync.service.UserService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class LoginScreen {

    public void start(Stage stage) {
        boolean wasFullScreen = stage.isFullScreen(); // Save fullscreen state

        Label title = new Label("FitSync - Login");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#2C3E50"));

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setPrefHeight(40);
        emailField.setMaxWidth(300);
        emailField.setStyle("-fx-background-color: #ECF0F1; -fx-border-color: #BDC3C7; -fx-border-radius: 5; -fx-background-radius: 5;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefHeight(40);
        passwordField.setMaxWidth(300);
        passwordField.setStyle("-fx-background-color: #ECF0F1; -fx-border-color: #BDC3C7; -fx-border-radius: 5; -fx-background-radius: 5;");

        TextField visiblePasswordField = new TextField();
        visiblePasswordField.setPromptText("Password");
        visiblePasswordField.setManaged(false);
        visiblePasswordField.setVisible(false);
        visiblePasswordField.setPrefHeight(40);
        visiblePasswordField.setMaxWidth(300);
        visiblePasswordField.setStyle("-fx-background-color: #ECF0F1; -fx-border-color: #BDC3C7; -fx-border-radius: 5; -fx-background-radius: 5;");

        CheckBox showPasswordCheckBox = new CheckBox("Show Password");
        showPasswordCheckBox.setTextFill(Color.web("#34495E"));

        showPasswordCheckBox.setOnAction(e -> {
            if (showPasswordCheckBox.isSelected()) {
                visiblePasswordField.setText(passwordField.getText());
                visiblePasswordField.setManaged(true);
                visiblePasswordField.setVisible(true);
                passwordField.setManaged(false);
                passwordField.setVisible(false);
            } else {
                passwordField.setText(visiblePasswordField.getText());
                passwordField.setManaged(true);
                passwordField.setVisible(true);
                visiblePasswordField.setManaged(false);
                visiblePasswordField.setVisible(false);
            }
        });

        Button loginButton = new Button("Login");
        loginButton.setPrefHeight(35);
        loginButton.setMaxWidth(200);
        loginButton.setStyle("-fx-background-color: #2ECC71; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");

        Button registerButton = new Button("Register");
        registerButton.setPrefHeight(35);
        registerButton.setMaxWidth(200);
        registerButton.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");

        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: red;");

        loginButton.setOnAction(e -> {
            String email = emailField.getText().trim();
            String password = showPasswordCheckBox.isSelected()
                    ? visiblePasswordField.getText().trim()
                    : passwordField.getText().trim();

            UserService userService = new UserService();
            User user = userService.validateLogin(email, password);

            if (user != null) {
                new DashboardScreen(user).start(stage);
                stage.setFullScreen(wasFullScreen); // Restore fullscreen
            } else {
                messageLabel.setText("Invalid email or password.");
            }
        });

        registerButton.setOnAction(e -> {
            new RegisterScreen().start(stage);
            stage.setFullScreen(wasFullScreen);
        });

        VBox form = new VBox(12,
                title,
                emailField,
                passwordField,
                visiblePasswordField,
                showPasswordCheckBox,
                loginButton,
                registerButton,
                messageLabel
        );
        form.setPadding(new Insets(25));
        form.setAlignment(Pos.CENTER);
        form.setMaxWidth(350);

        VBox layout = new VBox(form);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #FDFEFE;");
        layout.prefWidthProperty().bind(stage.widthProperty());
        layout.prefHeightProperty().bind(stage.heightProperty());

        Scene scene = new Scene(layout);
        stage.setTitle("FitSync - Login");
        stage.setScene(scene);
        stage.setFullScreen(wasFullScreen); // Apply fullscreen for first load too
        stage.show();
    }
}
