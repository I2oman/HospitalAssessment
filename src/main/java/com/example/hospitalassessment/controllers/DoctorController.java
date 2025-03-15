package com.example.hospitalassessment.controllers;

import com.example.hospitalassessment.database.DoctorDAO;
import com.example.hospitalassessment.models.Doctor;
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

import java.util.*;

public class DoctorController implements TableController {
    @FXML
    private TableView<Doctor> doctorTable;
    @FXML
    private TableColumn<Doctor, String> colDoctorID, colFirstName, colSurname, colAddress, colEmail, colSpecialization, colHospital;
    @FXML
    private TextField searchField;

    private DatabaseManager databaseManager;
    private DoctorDAO doctorDAO;

    @Override
    public void setDatabaseManager(DatabaseManager dbManager) {
        this.databaseManager = dbManager;
        this.doctorDAO = new DoctorDAO(databaseManager);
        loadDoctors();
    }

    @FXML
    public void initialize() {
        colDoctorID.setCellValueFactory(new PropertyValueFactory<>("id"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colSurname.setCellValueFactory(new PropertyValueFactory<>("surname"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colSpecialization.setCellValueFactory(new PropertyValueFactory<>("specialization"));
        colHospital.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getHospital() != null ? cellData.getValue().getHospital() : "N/A")
        );
    }

    private void loadDoctors() {
        ObservableList<Doctor> doctorList = FXCollections.observableArrayList(doctorDAO.getAllDoctors());
        FilteredList<Doctor> filteredData = new FilteredList<>(doctorList, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(doctor -> {
                if (newValue == null || newValue.isEmpty()) return true;

                String lowerCaseFilter = newValue.toLowerCase();
                return doctor.getId().toLowerCase().contains(lowerCaseFilter)
                        || doctor.getFirstName().toLowerCase().contains(lowerCaseFilter)
                        || doctor.getSurname().toLowerCase().contains(lowerCaseFilter)
                        || doctor.getAddress().toLowerCase().contains(lowerCaseFilter)
                        || doctor.getEmail().toLowerCase().contains(lowerCaseFilter)
                        || doctor.getSpecialization().toLowerCase().contains(lowerCaseFilter)
                        || (doctor.getHospital() != null && doctor.getHospital().toLowerCase().contains(lowerCaseFilter));
            });
        });

        SortedList<Doctor> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(doctorTable.comparatorProperty());

        doctorTable.setItems(sortedData);
    }

    @FXML
    private void handleAddDoctor() {
        openEntryForm("Add Doctor", null, Set.of());
    }

    @FXML
    private void handleModifyDoctor() {
        Doctor selectedDoctor = doctorTable.getSelectionModel().getSelectedItem();
        if (selectedDoctor != null) {
            Map<String, String> doctorData = new HashMap<>();
            doctorData.put("Doctor ID", selectedDoctor.getId());
            doctorData.put("First Name", selectedDoctor.getFirstName());
            doctorData.put("Surname", selectedDoctor.getSurname());
            doctorData.put("Address", selectedDoctor.getAddress());
            doctorData.put("Email", selectedDoctor.getEmail());
            doctorData.put("Specialization", selectedDoctor.getSpecialization());
            doctorData.put("Hospital", selectedDoctor.getHospital() != null ? selectedDoctor.getHospital() : "");

            openEntryForm("Modify Doctor", doctorData, Set.of("Doctor ID"));
        } else {
            AlertHelper.showAlert("Selection Error", "Please select a doctor to modify.", Alert.AlertType.ERROR);
        }
    }

    private void openEntryForm(String title, Map<String, String> existingData, Set<String> undisplayableFields) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/hospitalassessment/entry_form.fxml"));
            Parent root = loader.load();
            EntryFormController controller = loader.getController();
            controller.setFormTitle(title);

            if (existingData == null) { // Define fields for a new doctor (empty values)
                existingData = new HashMap<>();
                existingData.put("Doctor ID", "");
                existingData.put("First Name", "");
                existingData.put("Surname", "");
                existingData.put("Address", "");
                existingData.put("Email", "");
                existingData.put("Specialization", "");
                existingData.put("Hospital", "");
            }
            controller.setFields(existingData, undisplayableFields);

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));

            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(doctorTable.getScene().getWindow());

            Map<String, String> finalExistingData = existingData;
            controller.setOnSaveCallback(() -> {
                Map<String, String> updatedValues = controller.getFieldValues();

                String doctorId = updatedValues.get("Doctor ID") == null ? finalExistingData.get("Doctor ID") : updatedValues.get("Doctor ID");

                Doctor doctor = new Doctor(
                        doctorId,
                        updatedValues.get("First Name"),
                        updatedValues.get("Surname"),
                        updatedValues.get("Address"),
                        updatedValues.get("Email"),
                        updatedValues.get("Specialization"),
                        updatedValues.get("Hospital")
                );

                Map.Entry<String, Alert.AlertType> resultMessage = undisplayableFields.contains("Doctor ID")
                        ? doctorDAO.updateDoctor(doctor)
                        : doctorDAO.addDoctor(doctor);

                AlertHelper.showAlert(title, resultMessage.getKey(), resultMessage.getValue());

                if (resultMessage.getValue() != Alert.AlertType.ERROR) {
                    stage.close(); // Close window only on success
                    loadDoctors(); // Refresh table
                }
            });

            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteDoctor() {
        Doctor selectedDoctor = doctorTable.getSelectionModel().getSelectedItem();
        if (selectedDoctor != null) {
            boolean confirmed = AlertHelper.showConfirmation("Delete Doctor",
                    "Are you sure you want to delete Doctor: " + selectedDoctor.getFirstName() + " " + selectedDoctor.getSurname() + "?");

            if (confirmed) {
                String resultMessage = doctorDAO.deleteDoctor(selectedDoctor.getId());
                AlertHelper.showAlert("Doctor Deletion", resultMessage, Alert.AlertType.INFORMATION);
                loadDoctors(); // Refresh table
            }

        } else {
            AlertHelper.showAlert("Selection Error", "Please select a doctor to delete.", Alert.AlertType.ERROR);
        }
    }
}
