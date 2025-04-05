package com.example.fitsync.ui;

import com.example.fitsync.model.User;
import com.example.fitsync.service.UserService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class LoginScreen {

    public void start(Stage stage) {
        boolean wasFullScreen = stage.isFullScreen(); // Preserve fullscreen state

        // 🔥 Add logo image
        ImageView logo = new ImageView(new Image(getClass().getResourceAsStream("/images/fitsyncLogo.png")));
        logo.setFitWidth(150);
        logo.setPreserveRatio(true);

        Label title = new Label("FitSync - Login");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#2C3E50"));

        TextField emailField = new TextField();
        styleInputField(emailField);
        emailField.setPromptText("Email");

        PasswordField passwordField = new PasswordField();
        styleInputField(passwordField);
        passwordField.setPromptText("Password");

        TextField visiblePasswordField = new TextField();
        styleInputField(visiblePasswordField);
        visiblePasswordField.setPromptText("Password");
        visiblePasswordField.setManaged(false);
        visiblePasswordField.setVisible(false);

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
                stage.setFullScreen(wasFullScreen);
            } else {
                messageLabel.setText("Invalid email or password.");
            }
        });

        registerButton.setOnAction(e -> {
            new RegisterScreen().start(stage);
            stage.setFullScreen(wasFullScreen);
        });

        VBox form = new VBox(12,
                logo,                        // 👈 logo goes here
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

        Scene scene = new Scene(layout, 800, 600); // big enough to avoid startup squish
        stage.setTitle("FitSync - Login");
        stage.setScene(scene);
        stage.setFullScreen(wasFullScreen);
        stage.show();
    }

    private void styleInputField(TextField field) {
        field.setPrefHeight(40);
        field.setMaxWidth(300);
        field.setStyle("-fx-background-color: #ECF0F1; " +
                "-fx-border-color: #BDC3C7; " +
                "-fx-border-radius: 5; " +
                "-fx-background-radius: 5;");
    }
}
