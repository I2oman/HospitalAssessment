package com.example.hospitalassessment;

import com.example.hospitalassessment.controllers.MainController;
import io.github.cdimascio.dotenv.Dotenv;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.example.hospitalassessment.database.DatabaseManager;

/**
 * MainApplication serves as the entry point for the hospital database management system.
 * It initializes the primary application window and manages the application lifecycle.
 */
public class MainApplication extends Application {
    private static DatabaseManager databaseManager; // Manages the application's database connection.

    /**
     * Initializes the application's main stage and sets up the primary scene.
     * Loads the database configuration and establishes a connection.
     *
     * @param stage the main window of the JavaFX application
     * @throws Exception if any error occurs during initialization or loading resources
     */
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

    /**
     * The entry point of the application that launches the JavaFX runtime.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        launch();
    }

    /**
     * Stops the application lifecycle by ensuring the database connection is properly closed.
     * Logs a message during the process.
     */
    @Override
    public void stop() {
        System.out.println("Closing database connection...");
        if (databaseManager != null) {
            databaseManager.closeConnection();
        }
    }
}
