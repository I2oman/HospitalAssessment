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

/**
 * Controller for managing the Visit table view in a medical application.
 * Handles database interactions and table operations for visits.
 * Implements search, filter, add, modify, and delete functionalities.
 */
public class VisitController implements TableController {
    @FXML // Table to display visit records.
    private TableView<Visit> visitTable;

    @FXML // Columns representing Visit properties.
    private TableColumn<Visit, String> colPatient, colDoctor, colDateOfVisit, colSymptoms, colDiagnosis;

    @FXML // Field for searching visit records.
    private TextField searchField;


    private DatabaseManager databaseManager; // Manages database connections and transactions.
    private VisitDAO visitDAO; // Data Access Object for visit-related operations.
    private DoctorDAO doctorDAO; // Data Access Object for doctor-related operations.
    private PatientDAO patientDAO; // Data Access Object for patient-related operations.


    /**
     * Sets the DatabaseManager instance and initializes related DAOs and data.
     *
     * @param dbManager the DatabaseManager instance to be set.
     */
    @Override
    public void setDatabaseManager(DatabaseManager dbManager) {
        this.databaseManager = dbManager;
        this.visitDAO = new VisitDAO(databaseManager);
        this.doctorDAO = new DoctorDAO(databaseManager);
        this.patientDAO = new PatientDAO(databaseManager);
        loadVisits();
    }

    /**
     * Initializes the VisitController by configuring table columns with data from
     * the Visit model. Sets up value factories for patient and doctor names, date
     * of visit, symptoms, and diagnosis.
     */
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

    /**
     * Loads and initializes visits data into the visit table with filtering and sorting functionality.
     * The visits can be filtered based on entered text in the search field by matching
     * date of visit, symptoms, diagnosis, or patient and doctor details.
     */
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

    /**
     * Opens a form to add a new visit entry with empty fields for patient, doctor,
     * date of visit, symptoms, and diagnosis.
     */
    @FXML
    private void handleAddVisit() {
        openEntryForm("Add Visit", null, Set.of());
    }

    /**
     * Handles the modification of a selected visit from the visit table.
     * Opens an entry form pre-filled with visit details for editing.
     * If no visit is selected, displays a warning alert.
     */
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

    /**
     * Handles the deletion of a selected visit.
     * Displays a confirmation dialog and, if confirmed, deletes the visit.
     * Shows appropriate alerts for completion or selection errors.
     */
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

    /**
     * Opens the entry form to add or modify visit data.
     *
     * @param title               the title of the form window.
     * @param existingData        a map containing pre-filled data for the form fields, or null for empty fields.
     * @param undisplayableFields a set of field names to be hidden in the form.
     */
    private void openEntryForm(String title, Map<String, String> existingData, Set<String> undisplayableFields) {
        try {
            // Load the FXML file for the entry form
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/hospitalassessment/entry_form.fxml"));
            Parent root = loader.load();
            EntryFormController controller = loader.getController();
            controller.setFormTitle(title);

            // Define fields for a new visit (empty values)
            if (existingData == null) {
                existingData = new HashMap<>();
                existingData.put("Patient", "");
                existingData.put("Doctor", "");
                existingData.put("Date of Visit", "");
                existingData.put("Symptoms", "");
                existingData.put("Diagnosis", "");
            }

            // Set form fields and hide fields that shouldn't be displayed
            // and provide a list of available doctors and patients for selection.
            controller.setFields(existingData, undisplayableFields,
                    patientDAO.getAllPatients().stream().map(patient -> patient.getFirstName() + " " + patient.getSurname()).toList(),
                    doctorDAO.getAllDoctors().stream().map(doctor -> doctor.getFirstName() + " " + doctor.getSurname()).toList(),
                    new DatePicker()
            );

            // Create and configure a new window (Stage) for the form
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL); // Make it a modal window
            stage.initOwner(visitTable.getScene().getWindow()); // Attach it to the main window

            // Preserve the original existing data for reference
            Map<String, String> finalExistingData = existingData;

            // Define the action to be performed when the Save button is clicked
            controller.setOnSaveCallback(() -> {
                Map<String, String> updatedValues = controller.getFieldValues();

                // Get Doctor and Patient objects based on selection
                Doctor doctor = doctorDAO.getDoctorByFullName(updatedValues.get("Doctor") == null ? finalExistingData.get("Doctor") : updatedValues.get("Doctor"));
                Patient patient = patientDAO.getPatientByFullName(updatedValues.get("Patient") == null ? finalExistingData.get("Patient") : updatedValues.get("Patient"));

                // Ensure that a valid doctor and patient are selected before proceeding
                if (doctor == null || patient == null) {
                    AlertHelper.showAlert("Error", "Invalid doctor or patient selection.", Alert.AlertType.ERROR);
                    return;
                }

                java.sql.Date sqlDate = null;
                try {
                    // Retrieve the date input, either from the updated values or the existing data
                    String dateInput = updatedValues.get("Date of Visit") == null ? finalExistingData.get("Date of Visit") : updatedValues.get("Date of Visit");

                    // Ensure the date is not empty
                    if (dateInput == null || dateInput.isBlank()) {
                        throw new IllegalArgumentException("Date of Visit cannot be empty.");
                    }

                    // Convert the input string to a SQL Date format
                    sqlDate = java.sql.Date.valueOf(dateInput);
                } catch (IllegalArgumentException e) {
                    // Show an error alert if the date format is incorrect
                    AlertHelper.showAlert("Input Error", "Invalid date format. Please use 'yyyy-mm-dd'.", Alert.AlertType.ERROR);
                    return;
                }

                // Create a Visit object with updated values
                Visit visit = new Visit(
                        patient,
                        doctor,
                        sqlDate,
                        updatedValues.get("Symptoms"),
                        updatedValues.get("Diagnosis")
                );

                // Determine whether to update an existing visit or add a new one
                Map.Entry<String, Alert.AlertType> resultMessage = undisplayableFields.contains("Date of Visit")
                        ? visitDAO.updateVisit(visit)    // Update if "Visit ID" is not editable
                        : visitDAO.addVisit(visit);      // Otherwise, add a new visit

                // Show an alert message with the result of the operation
                AlertHelper.showAlert(title, resultMessage.getKey(), resultMessage.getValue());

                // If the operation was successful, close the form and refresh the visit table
                if (resultMessage.getValue() != Alert.AlertType.ERROR) {
                    stage.close(); // Close window only on success
                    loadVisits(); // Refresh table
                }
            });

            // Display the form and wait for user interaction
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace(); // Print error details if an exception occurs
        }
    }
}
