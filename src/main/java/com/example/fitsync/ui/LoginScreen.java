package com.example.fitsync.ui;

import com.example.fitsync.model.User;
import com.example.fitsync.service.UserService;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginScreen {

    public void start(Stage stage) {
        Label title = new Label("FitSync - Login");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Button loginButton = new Button("Login");
        Button registerButton = new Button("Register"); //
        Label messageLabel = new Label();

        loginButton.setOnAction(e -> {
            String email = emailField.getText().trim();
            String password = passwordField.getText().trim();

            UserService userService = new UserService();
            User user = userService.validateLogin(email, password);

            if (user != null) {
                new DashboardScreen(user).start(stage); //  this line switches the screen
            } else {
                messageLabel.setText("Invalid email or password.");
            }
        });



        // Handle register button click
        registerButton.setOnAction(e -> {
            new RegisterScreen().start(stage);
        });

        // Add both buttons to the layout
        VBox layout = new VBox(10,
                title, emailField, passwordField,
                loginButton, registerButton,
                messageLabel
        );
        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout, 300, 250);
        stage.setTitle("FitSync - Login");
        stage.setScene(scene);
        stage.show();
    }
}
