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

public class EntryFormController {
    @FXML
    private Label formTitle;
    @FXML
    private GridPane formGrid;
    @FXML
    private Button btnSave;

    private Map<String, Control> fieldMap = new HashMap<>();
    private Runnable onSaveCallback;

    public void setFormTitle(String title) {
        formTitle.setText(title);
    }

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

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    @FXML
    private void handleSave() {
        if (onSaveCallback != null) {
            onSaveCallback.run();
        }
    }

    @FXML
    private void handleCancel() {
        closePopup();
    }

    private void closePopup() {
        Stage stage = (Stage) btnSave.getScene().getWindow();
        stage.close();
    }
}
