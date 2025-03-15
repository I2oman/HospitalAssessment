package com.example.hospitalassessment.controllers;

import com.example.hospitalassessment.database.*;
import com.example.hospitalassessment.models.*;
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
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Controller class for managing Prescription operations in the UI.
 * Implements database interactions, table view handling, and user actions.
 */
public class PrescriptionController implements TableController {
    @FXML // Table to display prescription records.
    private TableView<Prescription> prescriptionTable;

    @FXML // Columns representing Prescription properties.
    private TableColumn<Prescription, String> colPrescriptionID, colDatePrescribed, colDosage, colDuration, colComment, colDrug, colDoctor, colPatient;

    @FXML // Field for searching prescription records.
    private TextField searchField;


    private DatabaseManager databaseManager; // Manages database connections and transactions.
    private PrescriptionDAO prescriptionDAO; // Data Access Object for prescription-related operations.
    private DrugDAO drugDAO; // Data Access Object for drug-related operations.
    private DoctorDAO doctorDAO; // Data Access Object for doctor-related operations.
    private PatientDAO patientDAO; // Data Access Object for patient-related operations.


    /**
     * Sets the DatabaseManager for the controller and initializes DAO objects.
     *
     * @param dbManager the DatabaseManager instance to connect to the database
     */
    @Override
    public void setDatabaseManager(DatabaseManager dbManager) {
        this.databaseManager = dbManager;
        this.prescriptionDAO = new PrescriptionDAO(databaseManager);
        this.drugDAO = new DrugDAO(databaseManager);
        this.doctorDAO = new DoctorDAO(databaseManager);
        this.patientDAO = new PatientDAO(databaseManager);
        loadPrescriptions();
    }

    /**
     * Initializes the Prescription table view by setting cell value factories
     * and customizing the appearance of cells for specific columns.
     */
    @FXML
    public void initialize() {
        colPrescriptionID.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDatePrescribed.setCellValueFactory(new PropertyValueFactory<>("datePrescribed"));
        colDosage.setCellValueFactory(new PropertyValueFactory<>("dosage"));
        colDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        colComment.setCellValueFactory(new PropertyValueFactory<>("comment"));
        colComment.setCellFactory(tc -> {
            TableCell<Prescription, String> cell = new TableCell<>() {
                private final Text text = new Text();

                {
                    text.wrappingWidthProperty().bind(tc.widthProperty().subtract(10));
                    text.setStyle("-fx-text-alignment: left;");
                    setGraphic(text);
                    setPrefHeight(Control.USE_COMPUTED_SIZE);
                }

                @Override
                protected void updateItem(String comment, boolean empty) {
                    super.updateItem(comment, empty);
                    if (empty || comment == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        text.setText(comment);
                        setGraphic(text);
                    }
                }
            };
            return cell;
        });

        colDrug.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDrug().getDrugName())
        );
        colDoctor.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDoctor().getFirstName() + " " + cellData.getValue().getDoctor().getSurname())
        );
        colPatient.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPatient().getFirstName() + " " + cellData.getValue().getPatient().getSurname())
        );
    }

    /**
     * Loads prescriptions from the database and populates the table view.
     * Applies filtering and sorting based on user input in the search field.
     */
    private void loadPrescriptions() {
        ObservableList<Prescription> prescriptionList = FXCollections.observableArrayList(prescriptionDAO.getAllPrescriptions());
        FilteredList<Prescription> filteredData = new FilteredList<>(prescriptionList, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(prescription -> {
                if (newValue == null || newValue.isEmpty()) return true;

                String lowerCaseFilter = newValue.toLowerCase();
                return prescription.getId().toLowerCase().contains(lowerCaseFilter)
                        || prescription.getDatePrescribed().toString().contains(lowerCaseFilter)
                        || String.valueOf(prescription.getDosage()).contains(lowerCaseFilter)
                        || String.valueOf(prescription.getDuration()).contains(lowerCaseFilter)
                        || (prescription.getComment() != null && prescription.getComment().toLowerCase().contains(lowerCaseFilter))
                        || prescription.getDrug().getDrugName().toLowerCase().contains(lowerCaseFilter)
                        || (prescription.getDoctor().getFirstName() + " " + prescription.getDoctor().getSurname()).toLowerCase().contains(lowerCaseFilter)
                        || (prescription.getPatient().getFirstName() + " " + prescription.getPatient().getSurname()).toLowerCase().contains(lowerCaseFilter);
            });
        });

        SortedList<Prescription> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(prescriptionTable.comparatorProperty());

        prescriptionTable.setItems(sortedData);
    }

    /**
     * Handles the Add Prescription action.
     * Opens a form for creating a new prescription record.
     */
    @FXML
    private void handleAddPrescription() {
        openEntryForm("Add Prescription", null, Set.of());
    }

    /**
     * Handles the modification of a selected prescription.
     * Prompts the user with a form pre-filled with the prescription's data for editing.
     * Displays a warning alert if no prescription is selected.
     */
    @FXML
    private void handleModifyPrescription() {
        Prescription selectedPrescription = prescriptionTable.getSelectionModel().getSelectedItem();
        if (selectedPrescription != null) {
            Map<String, String> prescriptionData = new HashMap<>();
            prescriptionData.put("Prescription ID", selectedPrescription.getId());
            prescriptionData.put("Drug", selectedPrescription.getDrug().getId() + " - " + selectedPrescription.getDrug().getDrugName());
            prescriptionData.put("Doctor", selectedPrescription.getDoctor().getFirstName() + " " + selectedPrescription.getDoctor().getSurname());
            prescriptionData.put("Patient", selectedPrescription.getPatient().getFirstName() + " " + selectedPrescription.getPatient().getSurname());
            prescriptionData.put("Date Prescribed", selectedPrescription.getDatePrescribed().toString());
            prescriptionData.put("Dosage", String.valueOf(selectedPrescription.getDosage()));
            prescriptionData.put("Duration", String.valueOf(selectedPrescription.getDuration()));
            prescriptionData.put("Comment", selectedPrescription.getComment());

            openEntryForm("Modify Prescription", prescriptionData, Set.of("Prescription ID"));
        } else {
            AlertHelper.showAlert("Selection Error", "Please select a prescription to modify.", Alert.AlertType.WARNING);
        }
    }

    /**
     * Handles the deletion of a selected prescription.
     * Prompts the user for confirmation, deletes the prescription if confirmed,
     * and updates the table view. Displays a warning if no prescription is selected.
     */
    @FXML
    private void handleDeletePrescription() {
        Prescription selectedPrescription = prescriptionTable.getSelectionModel().getSelectedItem();
        if (selectedPrescription != null) {
            boolean confirmed = AlertHelper.showConfirmation("Delete Prescription",
                    "Are you sure you want to delete this prescription?");

            if (confirmed) {
                String resultMessage = prescriptionDAO.deletePrescription(selectedPrescription.getId());
                AlertHelper.showAlert("Prescription Deletion", resultMessage, Alert.AlertType.INFORMATION);
                loadPrescriptions();
            }
        } else {
            AlertHelper.showAlert("Selection Error", "Please select a prescription to delete.", Alert.AlertType.WARNING);
        }
    }

    /**
     * Opens an entry form for creating or modifying a prescription.
     *
     * @param title               the title of the form window
     * @param existingData        a map containing pre-populated field values, or empty if creating a new prescription
     * @param undisplayableFields a set of field names that should not be displayed on the form
     */
    private void openEntryForm(String title, Map<String, String> existingData, Set<String> undisplayableFields) {
        try {
            // Load the FXML file for the entry form
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/hospitalassessment/entry_form.fxml"));
            Parent root = loader.load();
            EntryFormController controller = loader.getController();
            controller.setFormTitle(title);

            // Define fields for a new prescription (empty values)
            if (existingData == null) {
                existingData = new HashMap<>();
                existingData.put("Prescription ID", "");
                existingData.put("Drug", "");
                existingData.put("Doctor", "");
                existingData.put("Patient", "");
                existingData.put("Date Prescribed", "");
                existingData.put("Dosage", "");
                existingData.put("Duration", "");
                existingData.put("Comment", "");
            }

            // Set form fields and hide fields that shouldn't be displayed
            // and provide a list of available drugs, doctors and patients for selection.
            controller.setFields(
                    existingData,
                    undisplayableFields,
                    drugDAO.getAllDrugs().stream().map(drug -> drug.getId() + " - " + drug.getDrugName()).toList(),
                    doctorDAO.getAllDoctors().stream().map(doctor -> doctor.getFirstName() + " " + doctor.getSurname()).toList(),
                    patientDAO.getAllPatients().stream().map(patient -> patient.getFirstName() + " " + patient.getSurname()).toList(),
                    new DatePicker()
            );

            // Create and configure a new window (Stage) for the form
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL); // Make it a modal window
            stage.initOwner(prescriptionTable.getScene().getWindow()); // Attach it to the main window

            // Preserve the original existing data for reference
            Map<String, String> finalExistingData = existingData;

            // Define the action to be performed when the Save button is clicked
            controller.setOnSaveCallback(() -> {
                Map<String, String> updatedValues = controller.getFieldValues();

                // Get Drug, Doctor, and Patient objects based on selection
                Drug selectedDrug = drugDAO.getDrugById(updatedValues.get("Drug").split(" - ")[0] == null ? finalExistingData.get("Drug").split(" - ")[0] : updatedValues.get("Drug").split(" - ")[0]);
                Doctor selectedDoctor = doctorDAO.getDoctorByFullName(updatedValues.get("Doctor") == null ? finalExistingData.get("Doctor") : updatedValues.get("Doctor"));
                Patient selectedPatient = patientDAO.getPatientByFullName(updatedValues.get("Patient") == null ? finalExistingData.get("Patient") : updatedValues.get("Patient"));

                // Ensure that a valid drug, doctor, and patient are selected before proceeding
                if (selectedDrug == null || selectedDoctor == null || selectedPatient == null) {
                    AlertHelper.showAlert("Error", "Invalid drug, doctor, or patient selection.", Alert.AlertType.ERROR);
                    return;
                }

                java.sql.Date sqlDate = null;
                try {
                    // Retrieve the date input, either from the updated values or the existing data
                    String dateInput = updatedValues.get("Date Prescribed") == null ? finalExistingData.get("Date Prescribed") : updatedValues.get("Date Prescribed");

                    // Ensure the date is not empty
                    if (dateInput == null || dateInput.isBlank()) {
                        throw new IllegalArgumentException("Date Prescribed cannot be empty.");
                    }

                    // Convert the input string to a SQL Date format
                    sqlDate = java.sql.Date.valueOf(dateInput);
                } catch (IllegalArgumentException e) {
                    // Show an error alert if the date format is incorrect
                    AlertHelper.showAlert("Input Error", "Invalid date format. Please use 'yyyy-mm-dd'.", Alert.AlertType.ERROR);
                    return;
                }

                try {
                    // Create a Prescription object with updated values
                    Prescription prescription = new Prescription(
                            finalExistingData.get("Prescription ID").isEmpty() ? updatedValues.get("Prescription ID") : finalExistingData.get("Prescription ID"),
                            sqlDate,
                            Integer.parseInt(updatedValues.get("Dosage")),
                            Integer.parseInt(updatedValues.get("Duration")),
                            updatedValues.get("Comment"),
                            selectedDrug,
                            selectedDoctor,
                            selectedPatient
                    );

                    // Determine whether to update an existing prescription or add a new one
                    Map.Entry<String, Alert.AlertType> resultMessage = undisplayableFields.contains("Prescription ID")
                            ? prescriptionDAO.updatePrescription(prescription)    // Update if "Prescription ID" is not editable
                            : prescriptionDAO.addPrescription(prescription);      // Otherwise, add a new prescription

                    // Show an alert message with the result of the operation
                    AlertHelper.showAlert(title, resultMessage.getKey(), resultMessage.getValue());

                    // If the operation was successful, close the form and refresh the prescription table
                    if (resultMessage.getValue() != Alert.AlertType.ERROR) {
                        stage.close(); // Close window only on success
                        loadPrescriptions(); // Refresh table
                    }
                } catch (NumberFormatException e) {
                    AlertHelper.showAlert("Input Error", "Dosage and Duration must be valid numbers.", Alert.AlertType.ERROR);
                }
            });

            // Display the form and wait for user interaction
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace(); // Print error details if an exception occurs
        }
    }
}
