package com.example.hospitalassessment.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.HashMap;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Controller for managing an entry form, its fields, and associated actions.
 */
public class EntryFormController {
    @FXML // Label displaying the title of the form.
    private Label formTitle;

    @FXML // Grid layout for dynamically adding input fields.
    private GridPane formGrid;

    @FXML // Button to save the form data.
    private Button btnSave;


    private Map<String, Control> fieldMap = new HashMap<>(); // Map to store input fields by their labels.
    private Runnable onSaveCallback; // Callback triggered when the form is saved.

    /**
     * Sets the title of the form by updating the text of the formTitle field.
     *
     * @param title the new title to be displayed on the form
     */
    public void setFormTitle(String title) {
        formTitle.setText(title);
    }

    /**
     * Populates the form with the provided fields, excluding those specified as undisplayable.
     *
     * @param fields              a map containing field names as keys and their corresponding values
     * @param undisplayableFields a set of field names that should be excluded from the form
     */
    public void setFields(Map<String, String> fields, Set<String> undisplayableFields) {
        formGrid.getChildren().clear();
        fieldMap.clear();
        int row = 0;

        for (Map.Entry<String, String> entry : fields.entrySet()) {
            if (undisplayableFields.contains(entry.getKey())) {
                continue; // Skip fields that should not be displayed
            }

            Label label = new Label(entry.getKey() + ":");

            TextField textField = new TextField(entry.getValue());
            formGrid.add(label, 0, row);
            formGrid.add(textField, 1, row);
            fieldMap.put(entry.getKey(), textField);

            row++;
        }
    }

    /**
     * Populates the form with fields, excluding undisplayable ones, and handles insurance options.
     *
     * @param fields              a map containing field names as keys and their corresponding values
     * @param undisplayableFields a set of field names to exclude from the form
     * @param insuranceOptions    a list of insurance options to display in the combo box for the "Insurance" field
     */
    public void setFields(Map<String, String> fields, Set<String> undisplayableFields, List<String> insuranceOptions) {
        formGrid.getChildren().clear();
        fieldMap.clear();
        int row = 0;

        for (Map.Entry<String, String> entry : fields.entrySet()) {
            if (undisplayableFields.contains(entry.getKey())) {
                continue; // Skip undisplayable fields
            }

            Label label = new Label(entry.getKey() + ":");
            if (entry.getKey().equals("Insurance")) {
                ComboBox<String> insuranceDropdown = new ComboBox<>();
                insuranceDropdown.getItems().add("NHS");
                insuranceDropdown.getItems().addAll(insuranceOptions);
                insuranceDropdown.setValue(entry.getValue().isEmpty() ? "NHS" : entry.getValue()); // Set initial value

                formGrid.add(label, 0, row);
                formGrid.add(insuranceDropdown, 1, row);
                fieldMap.put(entry.getKey(), insuranceDropdown);
            } else {
                TextField textField = new TextField(entry.getValue());
                formGrid.add(label, 0, row);
                formGrid.add(textField, 1, row);
                fieldMap.put(entry.getKey(), textField);
            }
            row++;
        }
    }

    /**
     * Populates the form with the given fields, excluding undisplayable ones,
     * and configures specific input controls for patients, doctors, and the date of visit.
     *
     * @param fields              a map containing field names as keys and their corresponding values
     * @param undisplayableFields a set of field names to exclude from the form
     * @param patientOptions      a list of options for the patient dropdown
     * @param doctorOptions       a list of options for the doctor dropdown
     * @param datePicker          the DatePicker control to use for date input
     */
    public void setFields(Map<String, String> fields, Set<String> undisplayableFields, List<String> patientOptions, List<String> doctorOptions, DatePicker datePicker) {
        formGrid.getChildren().clear();
        fieldMap.clear();
        int row = 0;

        for (Map.Entry<String, String> entry : fields.entrySet()) {
            if (undisplayableFields.contains(entry.getKey())) {
                continue; // Skip undisplayable fields
            }

            Label label = new Label(entry.getKey() + ":");

            if (entry.getKey().equals("Patient")) {
                ComboBox<String> patientDropdown = new ComboBox<>();
                patientDropdown.getItems().addAll(patientOptions);
                patientDropdown.setValue(entry.getValue()); // Set initial value

                formGrid.add(label, 0, row);
                formGrid.add(patientDropdown, 1, row);
                fieldMap.put(entry.getKey(), patientDropdown);
            } else if (entry.getKey().equals("Doctor")) {
                ComboBox<String> doctorDropdown = new ComboBox<>();
                doctorDropdown.getItems().addAll(doctorOptions);
                doctorDropdown.setValue(entry.getValue()); // Set initial value

                formGrid.add(label, 0, row);
                formGrid.add(doctorDropdown, 1, row);
                fieldMap.put(entry.getKey(), doctorDropdown);
            } else if (entry.getKey().equals("Date of Visit")) {
                datePicker.setValue(entry.getValue().isEmpty() ? null : LocalDate.parse(entry.getValue()));

                formGrid.add(label, 0, row);
                formGrid.add(datePicker, 1, row);
                fieldMap.put(entry.getKey(), datePicker);
            } else {
                TextField textField = new TextField(entry.getValue());
                formGrid.add(label, 0, row);
                formGrid.add(textField, 1, row);
                fieldMap.put(entry.getKey(), textField);
            }
            row++;
        }
    }

    /**
     * Populates the form with the provided fields and configures appropriate input controls,
     * omitting fields specified as undisplayable.
     *
     * @param fields              a map containing field names as keys and their corresponding values
     * @param undisplayableFields a set of field names to exclude from the form
     * @param drugOptions         a list of options for the drug dropdown
     * @param doctorOptions       a list of options for the doctor dropdown
     * @param patientOptions      a list of options for the patient dropdown
     * @param datePicker          the DatePicker control to use for date input
     */
    public void setFields(Map<String, String> fields, Set<String> undisplayableFields, List<String> drugOptions, List<String> doctorOptions, List<String> patientOptions, DatePicker datePicker) {
        formGrid.getChildren().clear();
        fieldMap.clear();
        int row = 0;

        for (Map.Entry<String, String> entry : fields.entrySet()) {
            if (undisplayableFields.contains(entry.getKey())) {
                continue; // Skip undisplayable fields
            }

            Label label = new Label(entry.getKey() + ":");

            if (entry.getKey().equals("Drug")) {
                ComboBox<String> drugDropdown = new ComboBox<>();
                drugDropdown.getItems().addAll(drugOptions);
                drugDropdown.setValue(entry.getValue());

                formGrid.add(label, 0, row);
                formGrid.add(drugDropdown, 1, row);
                fieldMap.put(entry.getKey(), drugDropdown);
            } else if (entry.getKey().equals("Doctor")) {
                ComboBox<String> doctorDropdown = new ComboBox<>();
                doctorDropdown.getItems().addAll(doctorOptions);
                doctorDropdown.setValue(entry.getValue());

                formGrid.add(label, 0, row);
                formGrid.add(doctorDropdown, 1, row);
                fieldMap.put(entry.getKey(), doctorDropdown);
            } else if (entry.getKey().equals("Patient")) {
                ComboBox<String> patientDropdown = new ComboBox<>();
                patientDropdown.getItems().addAll(patientOptions);
                patientDropdown.setValue(entry.getValue());

                formGrid.add(label, 0, row);
                formGrid.add(patientDropdown, 1, row);
                fieldMap.put(entry.getKey(), patientDropdown);
            } else if (entry.getKey().equals("Date Prescribed")) {
                datePicker.setValue(entry.getValue().isEmpty() ? null : LocalDate.parse(entry.getValue()));

                formGrid.add(label, 0, row);
                formGrid.add(datePicker, 1, row);
                fieldMap.put(entry.getKey(), datePicker);
            } else {
                TextField textField = new TextField(entry.getValue());
                formGrid.add(label, 0, row);
                formGrid.add(textField, 1, row);
                fieldMap.put(entry.getKey(), textField);
            }
            row++;
        }
    }


    /**
     * Retrieves the current values of form fields.
     *
     * @return a map where keys are field names and values are their current inputs
     */
    public Map<String, String> getFieldValues() {
        Map<String, String> values = new HashMap<>();
        for (Map.Entry<String, Control> entry : fieldMap.entrySet()) {
            if (entry.getValue() instanceof TextField) {
                values.put(entry.getKey(), ((TextField) entry.getValue()).getText());
            } else if (entry.getValue() instanceof ComboBox) {
                values.put(entry.getKey(), ((ComboBox<?>) entry.getValue()).getValue().toString());
            } else if (entry.getValue() instanceof DatePicker) {
                DatePicker datePicker = (DatePicker) entry.getValue();
                LocalDate date = datePicker.getValue();
                if (date != null) {
                    values.put(entry.getKey(), date.toString()); // Format LocalDate as 'yyyy-MM-dd'
                } else {
                    values.put(entry.getKey(), ""); // Handle null case
                }
            }

        }
        return values;
    }

    /**
     * Sets a callback to be executed when the save action is triggered.
     *
     * @param callback a Runnable representing the callback to be called on save
     */
    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    /**
     * Handles the save action by executing the onSaveCallback if it is set.
     */
    @FXML
    private void handleSave() {
        if (onSaveCallback != null) {
            onSaveCallback.run();
        }
    }

    /**
     * Handles the cancel action by closing the current popup window.
     */
    @FXML
    private void handleCancel() {
        closePopup();
    }

    /**
     * Closes the current popup window by obtaining its stage and calling the close method.
     */
    private void closePopup() {
        Stage stage = (Stage) btnSave.getScene().getWindow();
        stage.close();
    }
}
