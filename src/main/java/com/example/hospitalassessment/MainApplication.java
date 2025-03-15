package com.example.hospitalassessment;

import com.example.hospitalassessment.controllers.MainController;
import io.github.cdimascio.dotenv.Dotenv;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.example.hospitalassessment.database.DatabaseManager;

public class MainApplication extends Application {
    private static DatabaseManager databaseManager;

    @Override
    public void start(Stage stage) throws Exception {
        Dotenv dotenv = Dotenv.load();

        databaseManager = new DatabaseManager(dotenv.get("DB_URL"), dotenv.get("DB_USER"), dotenv.get("DB_PASSWORD"));

        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("main.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), 1437, 692);
        stage.setTitle("Hospital Database Management");
        stage.resizableProperty().setValue(Boolean.FALSE);
        stage.setScene(scene);

        MainController mainController = fxmlLoader.getController();
        mainController.setDatabaseManager(databaseManager);

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void stop() {
        System.out.println("Closing database connection...");
        if (databaseManager != null) {
            databaseManager.closeConnection();
        }
    }
}
