package com.example.hospitalassessment.controllers;

import com.example.hospitalassessment.database.DoctorDAO;
import com.example.hospitalassessment.database.PatientDAO;
import com.example.hospitalassessment.database.VisitDAO;
import com.example.hospitalassessment.models.Doctor;
import com.example.hospitalassessment.models.Patient;
import com.example.hospitalassessment.models.Visit;
import com.example.hospitalassessment.database.DatabaseManager;
import com.example.hospitalassessment.utils.AlertHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class VisitController implements TableController {
    @FXML
    private TableView<Visit> visitTable;
    @FXML
    private TableColumn<Visit, String> colPatient, colDoctor, colDateOfVisit, colSymptoms, colDiagnosis;
    @FXML
    private TextField searchField;

    private DatabaseManager databaseManager;
    private VisitDAO visitDAO;
    private DoctorDAO doctorDAO;
    private PatientDAO patientDAO;

    @Override
    public void setDatabaseManager(DatabaseManager dbManager) {
        this.databaseManager = dbManager;
        this.visitDAO = new VisitDAO(databaseManager);
        this.doctorDAO = new DoctorDAO(databaseManager);
        this.patientDAO = new PatientDAO(databaseManager);
        loadVisits();
    }

    @FXML
    public void initialize() {
        colDateOfVisit.setCellValueFactory(new PropertyValueFactory<>("dateOfVisit"));
        colSymptoms.setCellValueFactory(new PropertyValueFactory<>("symptoms"));
        colDiagnosis.setCellValueFactory(new PropertyValueFactory<>("diagnosis"));

        colPatient.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPatient().getFirstName() + " " + cellData.getValue().getPatient().getSurname())
        );
        colDoctor.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDoctor().getFirstName() + " " + cellData.getValue().getDoctor().getSurname())
        );
    }

    private void loadVisits() {
        ObservableList<Visit> visitList = FXCollections.observableArrayList(visitDAO.getAllVisits());
        FilteredList<Visit> filteredData = new FilteredList<>(visitList, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(visit -> {
                if (newValue == null || newValue.isEmpty()) return true;

                String lowerCaseFilter = newValue.toLowerCase();
                return visit.getDateOfVisit().toString().contains(lowerCaseFilter)
                        || visit.getSymptoms().toLowerCase().contains(lowerCaseFilter)
                        || visit.getDiagnosis().toLowerCase().contains(lowerCaseFilter)
                        || (visit.getDoctor().getFirstName() + " " + visit.getDoctor().getSurname()).toLowerCase().contains(lowerCaseFilter)
                        || (visit.getPatient().getFirstName() + " " + visit.getPatient().getSurname()).toLowerCase().contains(lowerCaseFilter);
            });
        });

        SortedList<Visit> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(visitTable.comparatorProperty());

        visitTable.setItems(sortedData);
    }

    @FXML
    private void handleAddVisit() {
        openEntryForm("Add Visit", null, Set.of());
    }

    @FXML
    private void handleModifyVisit() {
        Visit selectedVisit = visitTable.getSelectionModel().getSelectedItem();
        if (selectedVisit != null) {
            Map<String, String> visitData = new HashMap<>();
            visitData.put("Patient", selectedVisit.getPatient().getFirstName() + " " + selectedVisit.getPatient().getSurname());
            visitData.put("Doctor", selectedVisit.getDoctor().getFirstName() + " " + selectedVisit.getDoctor().getSurname());
            visitData.put("Date of Visit", selectedVisit.getDateOfVisit().toString());
            visitData.put("Symptoms", selectedVisit.getSymptoms());
            visitData.put("Diagnosis", selectedVisit.getDiagnosis());

            openEntryForm("Modify Visit", visitData, Set.of("Patient", "Doctor", "Date of Visit"));
        } else {
            AlertHelper.showAlert("Selection Error", "Please select a visit to modify.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void handleDeleteVisit() {
        Visit selectedVisit = visitTable.getSelectionModel().getSelectedItem();
        if (selectedVisit != null) {
            boolean confirmed = AlertHelper.showConfirmation("Delete Visit",
                    "Are you sure you want to delete this visit?");

            if (confirmed) {
                String resultMessage = visitDAO.deleteVisit(selectedVisit.getPatient().getId(), selectedVisit.getDoctor().getId(), selectedVisit.getDateOfVisit());
                AlertHelper.showAlert("Visit Deletion", resultMessage, Alert.AlertType.INFORMATION);
                loadVisits();
            }
        } else {
            AlertHelper.showAlert("Selection Error", "Please select a visit to delete.", Alert.AlertType.WARNING);
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
                existingData.put("Patient", "");
                existingData.put("Doctor", "");
                existingData.put("Date of Visit", "");
                existingData.put("Symptoms", "");
                existingData.put("Diagnosis", "");
            }
            controller.setFields(existingData, undisplayableFields,
                    patientDAO.getAllPatients().stream().map(patient -> patient.getFirstName() + " " + patient.getSurname()).toList(),
                    doctorDAO.getAllDoctors().stream().map(doctor -> doctor.getFirstName() + " " + doctor.getSurname()).toList(),
                    new DatePicker()
            );

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));

            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(visitTable.getScene().getWindow());

            Map<String, String> finalExistingData = existingData;
            controller.setOnSaveCallback(() -> {
                Map<String, String> updatedValues = controller.getFieldValues();

                Doctor doctor = doctorDAO.getDoctorByFullName(updatedValues.get("Doctor") == null ? finalExistingData.get("Doctor") : updatedValues.get("Doctor"));
                Patient patient = patientDAO.getPatientByFullName(updatedValues.get("Patient") == null ? finalExistingData.get("Patient") : updatedValues.get("Patient"));

                if (doctor == null || patient == null) {
                    AlertHelper.showAlert("Error", "Invalid doctor or patient selection.", Alert.AlertType.ERROR);
                    return;
                }

                java.sql.Date sqlDate = null;
                try {
                    String dateInput = updatedValues.get("Date of Visit") == null ? finalExistingData.get("Date of Visit") : updatedValues.get("Date of Visit");
                    if (dateInput == null || dateInput.isBlank()) {
                        throw new IllegalArgumentException("Date of Visit cannot be empty.");
                    }
                    sqlDate = java.sql.Date.valueOf(dateInput);
                } catch (IllegalArgumentException e) {
                    AlertHelper.showAlert("Input Error", "Invalid date format. Please use 'yyyy-mm-dd'.", Alert.AlertType.ERROR);
                    return;
                }

                Visit visit = new Visit(
                        patient,
                        doctor,
                        sqlDate,
                        updatedValues.get("Symptoms"),
                        updatedValues.get("Diagnosis")
                );

                Map.Entry<String, Alert.AlertType> resultMessage = undisplayableFields.contains("Date of Visit")
                        ? visitDAO.updateVisit(visit)
                        : visitDAO.addVisit(visit);

                AlertHelper.showAlert(title, resultMessage.getKey(), resultMessage.getValue());

                if (resultMessage.getValue() != Alert.AlertType.ERROR) {
                    stage.close(); // Close window only on success
                    loadVisits(); // Refresh table
                }
            });

            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
