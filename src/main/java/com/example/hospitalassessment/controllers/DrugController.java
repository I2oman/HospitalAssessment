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

public class DrugController implements TableController {
    @FXML
    private TableView<Drug> drugTable;
    @FXML
    private TableColumn<Drug, String> colDrugID, colDrugName, colSideEffects, colBenefits;
    @FXML
    private TextField searchField;

    private DatabaseManager databaseManager;
    private DrugDAO drugDAO;

    @Override
    public void setDatabaseManager(DatabaseManager dbManager) {
        this.databaseManager = dbManager;
        this.drugDAO = new DrugDAO(databaseManager);
        loadDrugs();
    }

    @FXML
    public void initialize() {
        colDrugID.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDrugName.setCellValueFactory(new PropertyValueFactory<>("drugName"));
        colSideEffects.setCellValueFactory(new PropertyValueFactory<>("sideEffects"));
        colBenefits.setCellValueFactory(new PropertyValueFactory<>("benefits"));
    }

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

    @FXML
    private void handleAddDrug() {
        openEntryForm("Add Drug", null, Set.of());
    }

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

    private void openEntryForm(String title, Map<String, String> existingData, Set<String> undisplayableFields) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/hospitalassessment/entry_form.fxml"));
            Parent root = loader.load();
            EntryFormController controller = loader.getController();
            controller.setFormTitle(title);

            if (existingData == null) {
                existingData = new HashMap<>();
                existingData.put("Drug ID", "");
                existingData.put("Drug Name", "");
                existingData.put("Side Effects", "");
                existingData.put("Benefits", "");
            }
            controller.setFields(existingData, undisplayableFields);

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));

            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(drugTable.getScene().getWindow());

            Map<String, String> finalExistingData = existingData;
            controller.setOnSaveCallback(() -> {
                Map<String, String> updatedValues = controller.getFieldValues();

                String drugId = updatedValues.get("Drug ID") == null ? finalExistingData.get("Drug ID") : updatedValues.get("Drug ID");

                Drug drug = new Drug(
                        drugId,
                        updatedValues.get("Drug Name"),
                        updatedValues.get("Side Effects"),
                        updatedValues.get("Benefits")
                );

                Map.Entry<String, Alert.AlertType> resultMessage = undisplayableFields.contains("Drug ID")
                        ? drugDAO.updateDrug(drug)
                        : drugDAO.addDrug(drug);

                AlertHelper.showAlert(title, resultMessage.getKey(), resultMessage.getValue());

                if (resultMessage.getValue() != Alert.AlertType.ERROR) {
                    stage.close(); // Close window only on success
                    loadDrugs(); // Refresh table
                }
            });

            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
