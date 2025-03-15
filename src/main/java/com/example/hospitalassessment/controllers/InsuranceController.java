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

/**
 * Controller for managing insurance records, including adding, modifying, deleting,
 * and displaying insurance details in a table view. Integrates with the database
 * and allows interactive filtering and sorting of records.
 */
public class InsuranceController implements TableController {
    @FXML // Table to display insurance records.
    private TableView<Insurance> insuranceTable;

    @FXML // Columns representing Insurance properties.
    private TableColumn<Insurance, String> colInsuranceID, colCompany, colAddress, colPhone;

    @FXML // Field for searching insurance records.
    private TextField searchField;


    private DatabaseManager databaseManager; // Manages database connections and transactions.
    private InsuranceDAO insuranceDAO; // Data Access Object for insurance-related operations.

    /**
     * Sets the DatabaseManager instance for this controller and initializes the InsuranceDAO.
     * This method also triggers the loading of insurance data into the application.
     *
     * @param dbManager the DatabaseManager instance used for database operations
     */
    @Override
    public void setDatabaseManager(DatabaseManager dbManager) {
        this.databaseManager = dbManager;
        this.insuranceDAO = new InsuranceDAO(databaseManager);
        loadInsurances();
    }

    /**
     * Initializes the table columns for displaying insurance data
     * by setting up their value factories to map to corresponding properties.
     */
    @FXML
    public void initialize() {
        colInsuranceID.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCompany.setCellValueFactory(new PropertyValueFactory<>("company"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
    }

    /**
     * Loads insurance data from the database into the table.
     * Applies filtering based on the search field input
     * and sorts data according to table settings.
     */
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

    /**
     * Opens a form to add a new insurance entry. The form is pre-populated with empty fields.
     */
    @FXML
    private void handleAddInsurance() {
        openEntryForm("Add Insurance", null, Set.of());
    }

    /**
     * Handles the modification of an existing insurance entry selected from the table.
     * Opens a form pre-populated with the selected insurance's data for editing.
     * If no insurance is selected, displays a warning alert to the user.
     */
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

    /**
     * Handles the deletion of an insurance entry from the table.
     * Prompts the user for confirmation before proceeding with deletion.
     * If confirmed, deletes the selected insurance entry using the database and refreshes the table.
     * Shows an informational or warning alert based on the result.
     */
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

    /**
     * Opens an entry form for creating or editing insurance data.
     *
     * @param title               the title of the entry form window
     * @param existingData        the map of pre-filled field data; uses defaults if null
     * @param undisplayableFields the set of field names that should not be displayed in the form
     */
    private void openEntryForm(String title, Map<String, String> existingData, Set<String> undisplayableFields) {
        try {
            // Load the FXML file for the entry form
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/hospitalassessment/entry_form.fxml"));
            Parent root = loader.load();
            EntryFormController controller = loader.getController();
            controller.setFormTitle(title);

            // Define fields for a new insurance (empty values)
            if (existingData == null) {
                existingData = new HashMap<>();
                existingData.put("Insurance ID", "");
                existingData.put("Company", "");
                existingData.put("Address", "");
                existingData.put("Phone", "");
            }

            // Set form fields and hide fields that shouldn't be displayed
            controller.setFields(existingData, undisplayableFields);

            // Create and configure a new window (Stage) for the form
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL); // Make it a modal window
            stage.initOwner(insuranceTable.getScene().getWindow()); // Attach it to the main window

            // Preserve the original existing data for reference
            Map<String, String> finalExistingData = existingData;

            // Define the action to be performed when the Save button is clicked
            controller.setOnSaveCallback(() -> {
                Map<String, String> updatedValues = controller.getFieldValues();

                // Ensure Insurance ID is preserved if it's not in updatedValues
                String insuranceId = updatedValues.get("Insurance ID") == null ? finalExistingData.get("Insurance ID") : updatedValues.get("Insurance ID");

                // Create an Insurance object with updated values
                Insurance insurance = new Insurance(
                        insuranceId,
                        updatedValues.get("Company"),
                        updatedValues.get("Address"),
                        updatedValues.get("Phone")
                );

                // Determine whether to update an existing insurance or add a new one
                Map.Entry<String, Alert.AlertType> resultMessage = undisplayableFields.contains("Insurance ID")
                        ? insuranceDAO.updateInsurance(insurance)    // Update if "Insurance ID" is not editable
                        : insuranceDAO.addInsurance(insurance);      // Otherwise, add a new insurance

                // Show an alert message with the result of the operation
                AlertHelper.showAlert(title, resultMessage.getKey(), resultMessage.getValue());

                // If the operation was successful, close the form and refresh the insurance table
                if (resultMessage.getValue() != Alert.AlertType.ERROR) {
                    stage.close(); // Close window only on success
                    loadInsurances(); // Refresh table
                }
            });

            // Display the form and wait for user interaction
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace(); // Print error details if an exception occurs
        }
    }
}
