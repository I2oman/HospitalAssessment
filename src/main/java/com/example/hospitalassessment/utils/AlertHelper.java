package com.example.hospitalassessment.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/**
 * Utility class for displaying alerts and confirmation dialogs in JavaFX applications.
 */
public class AlertHelper {

    /**
     * Displays an alert dialog with a specified title, message, and alert type.
     *
     * @param title     the title of the alert dialog
     * @param message   the message content of the alert dialog
     * @param alertType the type of the alert (e.g., INFORMATION, WARNING, ERROR)
     */
    public static void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Displays a confirmation dialog with the given title and message.
     *
     * @param title   the title of the confirmation dialog
     * @param message the message to be displayed in the dialog
     * @return true if the user confirms (clicks OK), otherwise false
     */
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}
