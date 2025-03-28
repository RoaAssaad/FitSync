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

        TextField visiblePasswordField = new TextField();
        visiblePasswordField.setPromptText("Password");
        visiblePasswordField.setManaged(false);
        visiblePasswordField.setVisible(false);

        CheckBox showPasswordCheckBox = new CheckBox("Show Password");

        // Toggle password visibility
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
        Button registerButton = new Button("Register");
        Label messageLabel = new Label();

        loginButton.setOnAction(e -> {
            String email = emailField.getText().trim();
            String password = showPasswordCheckBox.isSelected()
                    ? visiblePasswordField.getText().trim()
                    : passwordField.getText().trim();

            UserService userService = new UserService();
            User user = userService.validateLogin(email, password);

            if (user != null) {
                new DashboardScreen(user).start(stage);
            } else {
                messageLabel.setText("Invalid email or password.");
            }
        });

        registerButton.setOnAction(e -> {
            new RegisterScreen().start(stage);
        });

        VBox layout = new VBox(10,
                title,
                emailField,
                passwordField,
                visiblePasswordField,
                showPasswordCheckBox,
                loginButton,
                registerButton,
                messageLabel
        );
        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout, 300, 280);
        stage.setTitle("FitSync - Login");
        stage.setScene(scene);
        stage.show();
    }
}
