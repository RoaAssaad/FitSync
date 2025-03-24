package com.example.fitsync;

import com.example.fitsync.ui.LoginScreen;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        new LoginScreen().start(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
