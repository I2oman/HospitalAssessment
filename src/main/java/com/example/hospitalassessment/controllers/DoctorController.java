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
    @FXML // Table for displaying Doctor records.
    private TableView<Doctor> doctorTable;

    @FXML // Columns representing Doctor properties.
    private TableColumn<Doctor, String> colDoctorID, colFirstName, colSurname, colAddress, colEmail, colSpecialization, colHospital;

    @FXML // Field for searching Doctor table.
    private TextField searchField;


    private DatabaseManager databaseManager; // DatabaseManager instance for managing database operations.
    private DoctorDAO doctorDAO; // DAO (Data Access Object) for interacting with Doctor-related database operations.

    /**
     * Sets the DatabaseManager instance for use throughout the class.
     * This method is required to initialize the database connection externally.
     *
     * @param dbManager an instance of DatabaseManager
     */
    @Override
    public void setDatabaseManager(DatabaseManager dbManager) {
        this.databaseManager = dbManager;
        this.doctorDAO = new DoctorDAO(databaseManager);
        loadDoctors();
    }

    /**
     * Initializes the columns of the doctor table and sets cell value factories for each column.
     * Populates the "Hospital" column with values or "N/A" if the hospital field is null.
     */
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

    /**
     * Populates the doctor table with data retrieved from the database.
     * Enables search and filter functionality based on doctor attributes.
     * Also sorts the displayed data dynamically based on user interactions.
     */
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

    /**
     * Handles the action of adding a new doctor by opening the entry form dialog.
     * The form is initialized with blank fields for the doctor details.
     */
    @FXML
    private void handleAddDoctor() {
        openEntryForm("Add Doctor", null, Set.of());
    }

    /**
     * Handles the modification of a selected doctor.
     * Retrieves data for the selected doctor, populates the entry form
     * with the details, and allows editing. Displays an error alert if no
     * doctor is selected.
     */
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

    /**
     * Opens the entry form dialog for adding or editing doctor details.
     *
     * @param title               the title of the entry form window
     * @param existingData        a map containing existing doctor data, where keys are field identifiers and values are field values
     * @param undisplayableFields a set of field identifiers that should not be displayed in the form
     */
    private void openEntryForm(String title, Map<String, String> existingData, Set<String> undisplayableFields) {
        try {
            // Load the FXML file for the entry form
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/hospitalassessment/entry_form.fxml"));
            Parent root = loader.load();
            EntryFormController controller = loader.getController();
            controller.setFormTitle(title);

            // Define fields for a new doctor (empty values)
            if (existingData == null) {
                existingData = new HashMap<>();
                existingData.put("Doctor ID", "");
                existingData.put("First Name", "");
                existingData.put("Surname", "");
                existingData.put("Address", "");
                existingData.put("Email", "");
                existingData.put("Specialization", "");
                existingData.put("Hospital", "");
            }

            // Set form fields and hide fields that shouldn't be displayed
            controller.setFields(existingData, undisplayableFields);

            // Create and configure a new window (Stage) for the form
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL); // Make it a modal window
            stage.initOwner(doctorTable.getScene().getWindow()); // Attach it to the main window

            // Preserve the original existing data for reference
            Map<String, String> finalExistingData = existingData;

            // Define the action to be performed when the Save button is clicked
            controller.setOnSaveCallback(() -> {
                Map<String, String> updatedValues = controller.getFieldValues();

                // Ensure Doctor ID is preserved if it's not in updatedValues
                String doctorId = updatedValues.get("Doctor ID") == null ? finalExistingData.get("Doctor ID") : updatedValues.get("Doctor ID");

                // Create a Doctor object with updated values
                Doctor doctor = new Doctor(
                        doctorId,
                        updatedValues.get("First Name"),
                        updatedValues.get("Surname"),
                        updatedValues.get("Address"),
                        updatedValues.get("Email"),
                        updatedValues.get("Specialization"),
                        updatedValues.get("Hospital")
                );

                // Determine whether to update an existing doctor or add a new one
                Map.Entry<String, Alert.AlertType> resultMessage = undisplayableFields.contains("Doctor ID")
                        ? doctorDAO.updateDoctor(doctor)    // Update if "Doctor ID" is not editable
                        : doctorDAO.addDoctor(doctor);      // Otherwise, add a new doctor

                // Show an alert message with the result of the operation
                AlertHelper.showAlert(title, resultMessage.getKey(), resultMessage.getValue());

                // If the operation was successful, close the form and refresh the doctor table
                if (resultMessage.getValue() != Alert.AlertType.ERROR) {
                    stage.close(); // Close window only on success
                    loadDoctors(); // Refresh table
                }
            });

            // Display the form and wait for user interaction
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace(); // Print error details if an exception occurs
        }
    }

    /**
     * Handles the deletion of a selected doctor from the table.
     * Prompts for confirmation before deletion. If confirmed, deletes the doctor
     * from the database, shows a result message, and refreshes the doctor list.
     * Displays an error alert if no doctor is selected.
     */
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
