package com.example.hospitalassessment.controllers;

import com.example.hospitalassessment.database.DatabaseManager;
import com.example.hospitalassessment.utils.AlertHelper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainController {
    @FXML
    private ComboBox<String> tableSelector;
    @FXML
    private AnchorPane tableContainer;

    private DatabaseManager databaseManager;
    private final Map<String, String> tableViews = new HashMap<>();

    public void setDatabaseManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @FXML
    public void initialize() {
        tableViews.put("Doctors", "/com/example/hospitalassessment/doctor.fxml");
        tableViews.put("Patients", "/com/example/hospitalassessment/patient.fxml");
        tableViews.put("Drugs", "/com/example/hospitalassessment/drug.fxml");
        tableViews.put("Insurance", "/com/example/hospitalassessment/insurance.fxml");
        tableViews.put("Prescriptions", "/com/example/hospitalassessment/prescription.fxml");
        tableViews.put("Visits", "/com/example/hospitalassessment/visit.fxml");

        tableSelector.getItems().addAll(tableViews.keySet());
    }

    @FXML
    private void loadSelectedTable() {
        String selectedTable = tableSelector.getValue();
        if (selectedTable == null || !tableViews.containsKey(selectedTable)) {
            System.out.println("No table selected.");
            AlertHelper.showAlert("Selection Error", "No table selected.", Alert.AlertType.ERROR);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(tableViews.get(selectedTable)));
            Parent tableView = loader.load();

            Object controller = loader.getController();
            if (controller instanceof TableController) {
                ((TableController) controller).setDatabaseManager(databaseManager);
            }

            tableContainer.getChildren().clear();
            tableContainer.getChildren().add(tableView);
            AnchorPane.setTopAnchor(tableView, 0.0);
            AnchorPane.setBottomAnchor(tableView, 0.0);
            AnchorPane.setLeftAnchor(tableView, 0.0);
            AnchorPane.setRightAnchor(tableView, 0.0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
