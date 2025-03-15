package com.example.hospitalassessment.controllers;

import com.example.hospitalassessment.database.InsuranceDAO;
import com.example.hospitalassessment.models.Insurance;
import com.example.hospitalassessment.database.DatabaseManager;
import com.example.hospitalassessment.utils.AlertHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class InsuranceController implements TableController {
    @FXML
    private TableView<Insurance> insuranceTable;
    @FXML
    private TableColumn<Insurance, String> colInsuranceID, colCompany, colAddress, colPhone;
    @FXML
    private TextField searchField;

    private DatabaseManager databaseManager;
    private InsuranceDAO insuranceDAO;

    @Override
    public void setDatabaseManager(DatabaseManager dbManager) {
        this.databaseManager = dbManager;
        this.insuranceDAO = new InsuranceDAO(databaseManager);
        loadInsurances();
    }

    @FXML
    public void initialize() {
        colInsuranceID.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCompany.setCellValueFactory(new PropertyValueFactory<>("company"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
    }

    private void loadInsurances() {
        ObservableList<Insurance> insuranceList = FXCollections.observableArrayList(insuranceDAO.getAllInsurance());
        FilteredList<Insurance> filteredData = new FilteredList<>(insuranceList, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(insurance -> {
                if (newValue == null || newValue.isEmpty()) return true;

                String lowerCaseFilter = newValue.toLowerCase();
                return insurance.getId().toLowerCase().contains(lowerCaseFilter)
                        || insurance.getCompany().toLowerCase().contains(lowerCaseFilter)
                        || insurance.getAddress().toLowerCase().contains(lowerCaseFilter)
                        || insurance.getPhone().toLowerCase().contains(lowerCaseFilter);
            });
        });

        SortedList<Insurance> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(insuranceTable.comparatorProperty());

        insuranceTable.setItems(sortedData);
    }

    @FXML
    private void handleAddInsurance() {
        openEntryForm("Add Insurance", null, Set.of());
    }

    @FXML
    private void handleModifyInsurance() {
        Insurance selectedInsurance = insuranceTable.getSelectionModel().getSelectedItem();
        if (selectedInsurance != null) {
            Map<String, String> insuranceData = new HashMap<>();
            insuranceData.put("Insurance ID", selectedInsurance.getId());
            insuranceData.put("Company", selectedInsurance.getCompany());
            insuranceData.put("Address", selectedInsurance.getAddress());
            insuranceData.put("Phone", selectedInsurance.getPhone());

            openEntryForm("Modify Insurance", insuranceData, Set.of("Insurance ID"));
        } else {
            AlertHelper.showAlert("Selection Error", "Please select an insurance entry to modify.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void handleDeleteInsurance() {
        Insurance selectedInsurance = insuranceTable.getSelectionModel().getSelectedItem();
        if (selectedInsurance != null) {
            boolean confirmed = AlertHelper.showConfirmation("Delete Insurance",
                    "Are you sure you want to delete the insurance company: " + selectedInsurance.getCompany() + "?");

            if (confirmed) {
                String resultMessage = insuranceDAO.deleteInsurance(selectedInsurance.getId());
                AlertHelper.showAlert("Insurance Deletion", resultMessage, Alert.AlertType.INFORMATION);
                loadInsurances();
            }
        } else {
            AlertHelper.showAlert("Selection Error", "Please select an insurance entry to delete.", Alert.AlertType.WARNING);
        }
    }

    private void openEntryForm(String title, Map<String, String> existingData, Set<String> undisplayableFields) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/hospitalassessment/entry_form.fxml"));
            Parent root = loader.load();
            EntryFormController controller = loader.getController();
            controller.setFormTitle(title);

            if (existingData == null) {
                existingData = new HashMap<>();
                existingData.put("Insurance ID", "");
                existingData.put("Company", "");
                existingData.put("Address", "");
                existingData.put("Phone", "");
            }
            controller.setFields(existingData, undisplayableFields);

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));

            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(insuranceTable.getScene().getWindow());

            Map<String, String> finalExistingData = existingData;
            controller.setOnSaveCallback(() -> {
                Map<String, String> updatedValues = controller.getFieldValues();

                String insuranceId = updatedValues.get("Insurance ID") == null ? finalExistingData.get("Insurance ID") : updatedValues.get("Insurance ID");

                Insurance insurance = new Insurance(
                        insuranceId,
                        updatedValues.get("Company"),
                        updatedValues.get("Address"),
                        updatedValues.get("Phone")
                );

                Map.Entry<String, Alert.AlertType> resultMessage = undisplayableFields.contains("Insurance ID")
                        ? insuranceDAO.updateInsurance(insurance)
                        : insuranceDAO.addInsurance(insurance);

                AlertHelper.showAlert(title, resultMessage.getKey(), resultMessage.getValue());

                if (resultMessage.getValue() != Alert.AlertType.ERROR) {
                    stage.close(); // Close window only on success
                    loadInsurances(); // Refresh table
                }
            });

            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
