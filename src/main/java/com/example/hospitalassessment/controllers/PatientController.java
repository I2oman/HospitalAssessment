package com.example.hospitalassessment.controllers;

import com.example.hospitalassessment.database.InsuranceDAO;
import com.example.hospitalassessment.database.PatientDAO;
import com.example.hospitalassessment.models.Insurance;
import com.example.hospitalassessment.models.Patient;
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
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PatientController implements TableController {
    @FXML
    private TableView<Patient> patientTable;
    @FXML
    private TableColumn<Patient, String> colPatientID, colFirstName, colSurname, colPostcode, colAddress, colPhone, colEmail, colInsurance;
    @FXML
    private TextField searchField;

    private DatabaseManager databaseManager;
    private PatientDAO patientDAO;
    private InsuranceDAO insuranceDAO;

    @Override
    public void setDatabaseManager(DatabaseManager dbManager) {
        this.databaseManager = dbManager;
        this.patientDAO = new PatientDAO(databaseManager);
        this.insuranceDAO = new InsuranceDAO(databaseManager);
        loadPatients();
    }

    @FXML
    public void initialize() {
        colPatientID.setCellValueFactory(new PropertyValueFactory<>("id"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colSurname.setCellValueFactory(new PropertyValueFactory<>("surname"));
        colPostcode.setCellValueFactory(new PropertyValueFactory<>("postcode"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colInsurance.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getInsurance() != null ? cellData.getValue().getInsurance().getCompany() : "NHS")
        );
    }

    private void loadPatients() {
        ObservableList<Patient> patientList = FXCollections.observableArrayList(patientDAO.getAllPatients());
        FilteredList<Patient> filteredData = new FilteredList<>(patientList, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(patient -> {
                if (newValue == null || newValue.isEmpty()) return true;

                String lowerCaseFilter = newValue.toLowerCase();
                return patient.getId().toLowerCase().contains(lowerCaseFilter)
                        || patient.getFirstName().toLowerCase().contains(lowerCaseFilter)
                        || patient.getSurname().toLowerCase().contains(lowerCaseFilter)
                        || patient.getPostcode().toLowerCase().contains(lowerCaseFilter)
                        || patient.getAddress().toLowerCase().contains(lowerCaseFilter)
                        || patient.getPhone().toLowerCase().contains(lowerCaseFilter)
                        || (patient.getEmail() != null && patient.getEmail().toLowerCase().contains(lowerCaseFilter))
                        || (patient.getInsurance() != null && patient.getInsurance().getCompany().toLowerCase().contains(lowerCaseFilter));
            });
        });

        SortedList<Patient> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(patientTable.comparatorProperty());

        patientTable.setItems(sortedData);
    }

    @FXML
    private void handleAddPatient() {
        openEntryForm("Add Patient", null, Set.of());
    }

    @FXML
    private void handleModifyPatient() {
        Patient selectedPatient = patientTable.getSelectionModel().getSelectedItem();
        if (selectedPatient != null) {
            Map<String, String> patientData = new HashMap<>();
            patientData.put("Patient ID", selectedPatient.getId());
            patientData.put("First Name", selectedPatient.getFirstName());
            patientData.put("Surname", selectedPatient.getSurname());
            patientData.put("Address", selectedPatient.getAddress());
            patientData.put("Postcode", selectedPatient.getPostcode());
            patientData.put("Phone", selectedPatient.getPhone());
            patientData.put("Email", selectedPatient.getEmail());
            patientData.put("Insurance", selectedPatient.getInsurance() != null ? selectedPatient.getInsurance().getCompany() : "NHS");

            openEntryForm("Modify Patient", patientData, Set.of("Patient ID"));
        } else {
            AlertHelper.showAlert("Selection Error", "Please select a patient to modify.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void handleDeletePatient() {
        Patient selectedPatient = patientTable.getSelectionModel().getSelectedItem();
        if (selectedPatient != null) {
            boolean confirmed = AlertHelper.showConfirmation("Delete Patient",
                    "Are you sure you want to delete Patient " + selectedPatient.getFirstName() + " " + selectedPatient.getSurname() + "?");

            if (confirmed) {
                String resultMessage = patientDAO.deletePatient(selectedPatient.getId());
                AlertHelper.showAlert("Patient Deletion", resultMessage, Alert.AlertType.INFORMATION);
                loadPatients();
            }
        } else {
            AlertHelper.showAlert("Selection Error", "Please select a patient to delete.", Alert.AlertType.WARNING);
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
                existingData.put("Patient ID", "");
                existingData.put("First Name", "");
                existingData.put("Surname", "");
                existingData.put("Address", "");
                existingData.put("Postcode", "");
                existingData.put("Phone", "");
                existingData.put("Email", "");
                existingData.put("Insurance", "");
            }
            controller.setFields(existingData, undisplayableFields, insuranceDAO.getAllInsurance().stream().map(Insurance::getCompany).toList());

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));

            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(patientTable.getScene().getWindow());

            Map<String, String> finalExistingData = existingData;
            controller.setOnSaveCallback(() -> {
                Map<String, String> updatedValues = controller.getFieldValues();

                String patientId = updatedValues.get("Patient ID") == null ? finalExistingData.get("Patient ID") : updatedValues.get("Patient ID");

                Patient patient = new Patient(
                        patientId,
                        updatedValues.get("First Name"),
                        updatedValues.get("Surname"),
                        updatedValues.get("Address"),
                        updatedValues.get("Postcode"),
                        updatedValues.get("Phone"),
                        updatedValues.get("Email"),
                        insuranceDAO.getInsuranceByCompany(updatedValues.get("Insurance"))
                );

                Map.Entry<String, Alert.AlertType> resultMessage = undisplayableFields.contains("Patient ID")
                        ? patientDAO.updatePatient(patient)
                        : patientDAO.addPatient(patient);

                AlertHelper.showAlert(title, resultMessage.getKey(), resultMessage.getValue());

                if (resultMessage.getValue() != Alert.AlertType.ERROR) {
                    stage.close(); // Close window only on success
                    loadPatients(); // Refresh table
                }
            });

            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
