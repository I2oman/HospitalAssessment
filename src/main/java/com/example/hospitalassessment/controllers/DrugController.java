package com.example.hospitalassessment.controllers;

import com.example.hospitalassessment.database.DrugDAO;
import com.example.hospitalassessment.models.Drug;
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
 * A controller for managing drug-related operations in a table view.
 * Provides functionality to load, add, modify, and delete drugs using a database manager.
 */
public class DrugController implements TableController {
    @FXML // Table to display drug records.
    private TableView<Drug> drugTable;

    @FXML // Columns representing Drug properties.
    private TableColumn<Drug, String> colDrugID, colDrugName, colSideEffects, colBenefits;

    @FXML // Field for searching drugs.
    private TextField searchField;


    private DatabaseManager databaseManager; // Manages database connections and transactions.
    private DrugDAO drugDAO; // Data Access Object for drug-related operations.

    /**
     * Sets the database manager for this controller, initializes the DrugDAO, and loads drugs into the table.
     *
     * @param dbManager the DatabaseManager instance to be used for database operations
     */
    @Override
    public void setDatabaseManager(DatabaseManager dbManager) {
        this.databaseManager = dbManager;
        this.drugDAO = new DrugDAO(databaseManager);
        loadDrugs();
    }

    /**
     * Initializes the table columns for displaying drug data by setting up their
     * value factories to map to corresponding properties.
     */
    @FXML
    public void initialize() {
        colDrugID.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDrugName.setCellValueFactory(new PropertyValueFactory<>("drugName"));
        colSideEffects.setCellValueFactory(new PropertyValueFactory<>("sideEffects"));
        colBenefits.setCellValueFactory(new PropertyValueFactory<>("benefits"));
    }

    /**
     * Loads drug data from the database into an observable and sortable table structure.
     * Applies search functionality for filtering drugs by ID, name, side effects, or benefits.
     */
    private void loadDrugs() {
        ObservableList<Drug> drugList = FXCollections.observableArrayList(drugDAO.getAllDrugs());
        FilteredList<Drug> filteredData = new FilteredList<>(drugList, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(drug -> {
                if (newValue == null || newValue.isEmpty()) return true;

                String lowerCaseFilter = newValue.toLowerCase();
                return drug.getId().toLowerCase().contains(lowerCaseFilter)
                        || drug.getDrugName().toLowerCase().contains(lowerCaseFilter)
                        || drug.getSideEffects().toLowerCase().contains(lowerCaseFilter)
                        || drug.getBenefits().toLowerCase().contains(lowerCaseFilter);
            });
        });

        SortedList<Drug> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(drugTable.comparatorProperty());

        drugTable.setItems(sortedData);
    }

    /**
     * Opens a form for adding a new drug.
     * Configures the form with empty fields.
     */
    @FXML
    private void handleAddDrug() {
        openEntryForm("Add Drug", null, Set.of());
    }

    /**
     * Handles the modification of a selected drug.
     * Opens a form pre-filled with the selected drug's data for editing.
     * Shows an alert if no drug is selected.
     */
    @FXML
    private void handleModifyDrug() {
        Drug selectedDrug = drugTable.getSelectionModel().getSelectedItem();
        if (selectedDrug != null) {
            Map<String, String> drugData = new HashMap<>();
            drugData.put("Drug ID", selectedDrug.getId());
            drugData.put("Drug Name", selectedDrug.getDrugName());
            drugData.put("Side Effects", selectedDrug.getSideEffects());
            drugData.put("Benefits", selectedDrug.getBenefits());

            openEntryForm("Modify Drug", drugData, Set.of("Drug ID"));
        } else {
            AlertHelper.showAlert("Selection Error", "Please select a drug to modify.", Alert.AlertType.WARNING);
        }
    }

    /**
     * Handles the deletion of a selected drug from the drug table.
     * Prompts user confirmation before deletion.
     * Displays success or error messages based on the operation result.
     * Reloads drugs data after successful deletion.
     */
    @FXML
    private void handleDeleteDrug() {
        Drug selectedDrug = drugTable.getSelectionModel().getSelectedItem();
        if (selectedDrug != null) {
            boolean confirmed = AlertHelper.showConfirmation("Delete Drug",
                    "Are you sure you want to delete Drug " + selectedDrug.getDrugName() + "?");

            if (confirmed) {
                String resultMessage = drugDAO.deleteDrug(selectedDrug.getId());
                AlertHelper.showAlert("Drug Deletion", resultMessage, Alert.AlertType.INFORMATION);
                loadDrugs();
            }
        } else {
            AlertHelper.showAlert("Selection Error", "Please select a drug to delete.", Alert.AlertType.WARNING);
        }
    }

    /**
     * Opens an entry form for drug data, pre-filling fields if data is provided and excluding specified fields from display.
     *
     * @param title               the title of the form window
     * @param existingData        a map of field names to their current values, or null for empty fields
     * @param undisplayableFields a set of field names that should be hidden in the form
     */
    private void openEntryForm(String title, Map<String, String> existingData, Set<String> undisplayableFields) {
        try {
            // Load the FXML file for the entry form
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/hospitalassessment/entry_form.fxml"));
            Parent root = loader.load();
            EntryFormController controller = loader.getController();
            controller.setFormTitle(title);

            // Define fields for a new drug (empty values)
            if (existingData == null) {
                existingData = new HashMap<>();
                existingData.put("Drug ID", "");
                existingData.put("Drug Name", "");
                existingData.put("Side Effects", "");
                existingData.put("Benefits", "");
            }

            // Set form fields and hide fields that shouldn't be displayed
            controller.setFields(existingData, undisplayableFields);

            // Create and configure a new window (Stage) for the form
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL); // Make it a modal window
            stage.initOwner(drugTable.getScene().getWindow()); // Attach it to the main window

            // Preserve the original existing data for reference
            Map<String, String> finalExistingData = existingData;

            // Define the action to be performed when the Save button is clicked
            controller.setOnSaveCallback(() -> {
                Map<String, String> updatedValues = controller.getFieldValues();

                // Ensure Drug ID is preserved if it's not in updatedValues
                String drugId = updatedValues.get("Drug ID") == null ? finalExistingData.get("Drug ID") : updatedValues.get("Drug ID");

                // Create a Drug object with updated values
                Drug drug = new Drug(
                        drugId,
                        updatedValues.get("Drug Name"),
                        updatedValues.get("Side Effects"),
                        updatedValues.get("Benefits")
                );

                // Determine whether to update an existing drug or add a new one
                Map.Entry<String, Alert.AlertType> resultMessage = undisplayableFields.contains("Drug ID")
                        ? drugDAO.updateDrug(drug)    // Update if "Drug ID" is not editable
                        : drugDAO.addDrug(drug);      // Otherwise, add a new drug

                // Show an alert message with the result of the operation
                AlertHelper.showAlert(title, resultMessage.getKey(), resultMessage.getValue());

                // If the operation was successful, close the form and refresh the drug table
                if (resultMessage.getValue() != Alert.AlertType.ERROR) {
                    stage.close(); // Close window only on success
                    loadDrugs(); // Refresh table
                }
            });

            // Display the form and wait for user interaction
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace(); // Print error details if an exception occurs
        }
    }
}
