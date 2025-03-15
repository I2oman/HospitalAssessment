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

/**
 * Controller for managing patient records in a table view. Provides functionality
 * to add, modify, delete, and search patients. Interacts with database through DAOs.
 */
public class PatientController implements TableController {
    @FXML // Table to display patient records.
    private TableView<Patient> patientTable;

    @FXML // Columns representing Patient properties.
    private TableColumn<Patient, String> colPatientID, colFirstName, colSurname, colPostcode, colAddress, colPhone, colEmail, colInsurance;

    @FXML // Field for searching patient records.
    private TextField searchField;


    private DatabaseManager databaseManager; // Manages database connections and transactions.
    private PatientDAO patientDAO; // Data Access Object for patient-related operations.
    private InsuranceDAO insuranceDAO; // Data Access Object for insurance-related operations.

    /**
     * Sets the provided DatabaseManager instance and initializes related DAOs.
     *
     * @param dbManager the DatabaseManager instance to be set and used for database operations.
     */
    @Override
    public void setDatabaseManager(DatabaseManager dbManager) {
        this.databaseManager = dbManager;
        this.patientDAO = new PatientDAO(databaseManager);
        this.insuranceDAO = new InsuranceDAO(databaseManager);
        loadPatients();
    }

    /**
     * Initializes the patient table columns by configuring their cell value factories
     * to map to corresponding patient properties, including handling null insurance cases.
     */
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

    /**
     * Loads patient data into the table with filtering and sorting functionality.
     * Filters allow searching by patient ID, name, postcode, address, phone, email, or insurance company.
     * Binds sorted data to match the table's comparator.
     */
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

    /**
     * Handles the action of adding a new patient by opening a patient entry form.
     * The form allows input of patient details and saves the data upon submission.
     */
    @FXML
    private void handleAddPatient() {
        openEntryForm("Add Patient", null, Set.of());
    }

    /**
     * Handles the modification of a selected patient.
     * Opens a form pre-filled with the selected patient's details
     * for editing. Displays a warning if no patient is selected.
     */
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

    /**
     * Handles the deletion of a selected patient from the table.
     * Displays a confirmation dialog before deletion.
     * Shows an alert with the result or a warning if no patient is selected.
     */
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

    /**
     * Opens an entry form for adding or modifying patient details.
     *
     * @param title               the title of the form to be displayed on the window.
     * @param existingData        a map containing pre-filled data for the form fields, or null to initialize default fields.
     * @param undisplayableFields a set of field names that should not be displayed in the form.
     */
    private void openEntryForm(String title, Map<String, String> existingData, Set<String> undisplayableFields) {
        try {
            // Load the FXML file for the entry form
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/hospitalassessment/entry_form.fxml"));
            Parent root = loader.load();
            EntryFormController controller = loader.getController();
            controller.setFormTitle(title);

            // Define fields for a new patient (empty values)
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

            // Set form fields and hide fields that shouldn't be displayed
            // and provide a list of available insurance companies for selection.
            controller.setFields(existingData, undisplayableFields, insuranceDAO.getAllInsurance().stream().map(Insurance::getCompany).toList());

            // Create and configure a new window (Stage) for the form
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL); // Make it a modal window
            stage.initOwner(patientTable.getScene().getWindow()); // Attach it to the main window

            // Preserve the original existing data for reference
            Map<String, String> finalExistingData = existingData;

            // Define the action to be performed when the Save button is clicked
            controller.setOnSaveCallback(() -> {
                Map<String, String> updatedValues = controller.getFieldValues();

                // Ensure Patient ID is preserved if it's not in updatedValues
                String patientId = updatedValues.get("Patient ID") == null ? finalExistingData.get("Patient ID") : updatedValues.get("Patient ID");

                // Create a Patient object with updated values
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

                // Determine whether to update an existing patient or add a new one
                Map.Entry<String, Alert.AlertType> resultMessage = undisplayableFields.contains("Patient ID")
                        ? patientDAO.updatePatient(patient)    // Update if "Patient ID" is not editable
                        : patientDAO.addPatient(patient);      // Otherwise, add a new patient

                // Show an alert message with the result of the operation
                AlertHelper.showAlert(title, resultMessage.getKey(), resultMessage.getValue());

                // If the operation was successful, close the form and refresh the patient table
                if (resultMessage.getValue() != Alert.AlertType.ERROR) {
                    stage.close(); // Close window only on success
                    loadPatients(); // Refresh table
                }
            });

            // Display the form and wait for user interaction
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace(); // Print error details if an exception occurs
        }
    }
}
