package com.example.hospitalassessment.database;

import com.example.hospitalassessment.models.Insurance;
import com.example.hospitalassessment.models.Patient;
import javafx.scene.control.Alert;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PatientDAO {
    private final Connection connection;
    private final InsuranceDAO insuranceDAO;

    public PatientDAO(DatabaseManager dbManager) {
        this.connection = dbManager.getConnection();
        this.insuranceDAO = new InsuranceDAO(dbManager);
    }

    public List<Patient> getAllPatients() {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT * FROM patient";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                patients.add(extractPatientFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return patients;
    }

    public Patient getPatientById(String patientId) {
        String sql = "SELECT * FROM patient WHERE patientid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, patientId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractPatientFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Patient getPatientByEmail(String email) {
        String sql = "SELECT * FROM patient WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractPatientFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Patient getPatientByFullName(String fullName) {
        String sql = "SELECT * FROM patient WHERE CONCAT(firstname, ' ', surname) = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, fullName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractPatientFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    private Patient extractPatientFromResultSet(ResultSet rs) throws SQLException {
        Insurance insurance = insuranceDAO.getInsuranceById(rs.getString("insuranceid"));

        return new Patient(
                rs.getString("patientid"),
                rs.getString("firstname"),
                rs.getString("surname"),
                rs.getString("address"),
                rs.getString("postcode"),
                rs.getString("phone"),
                rs.getString("email"),
                insurance
        );
    }

    public Map.Entry<String, Alert.AlertType> addPatient(Patient patient) {
        if (getPatientById(patient.getId()) != null) {
            return Map.entry("Error: A patient with this ID already exists.", Alert.AlertType.ERROR);
        }

        if (getPatientByEmail(patient.getEmail()) != null) {
            return Map.entry("Error: A patient with this email already exists.", Alert.AlertType.ERROR);
        }

        String sql = "INSERT INTO patient (patientid, firstname, surname, postcode, address, phone, email, insuranceid) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, patient.getId());
            stmt.setString(2, patient.getFirstName());
            stmt.setString(3, patient.getSurname());
            stmt.setString(4, patient.getPostcode());
            stmt.setString(5, patient.getAddress());
            stmt.setString(6, patient.getPhone());
            stmt.setString(7, patient.getEmail());
            stmt.setString(8, patient.getInsurance() == null ? "NHS" : patient.getInsurance().getId());

            return stmt.executeUpdate() > 0
                    ? Map.entry("Patient added successfully!", Alert.AlertType.INFORMATION)
                    : Map.entry("Error: Patient could not be added.", Alert.AlertType.ERROR);
        } catch (SQLException e) {
            e.printStackTrace();
            return Map.entry("Database error occurred. Please try again.", Alert.AlertType.ERROR);
        }
    }

    /**
     * Updates the details of an existing patient in the database.
     *
     * @param patient The patient object containing updated fields, such as first name,
     *                surname, postcode, address, phone, email, and insurance details.
     * @return A map entry where the key represents a message indicating the result of the operation,
     * and the value represents the type of alert (success or error).
     */
    public Map.Entry<String, Alert.AlertType> updatePatient(Patient patient) {
        if (getPatientById(patient.getId()) == null) {
            return Map.entry("Error: Patient with this ID does not exist.", Alert.AlertType.ERROR);
        }

        if (getPatientByEmail(patient.getEmail()) != null && !getPatientByEmail(patient.getEmail()).getId().equals(patient.getId())) {
            return Map.entry("Error: A patient with this email already exists.", Alert.AlertType.ERROR);
        }

        String sql = "UPDATE patient SET firstname = ?, surname = ?, postcode = ?, address = ?, phone = ?, email = ?, insuranceid = ? WHERE patientid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, patient.getFirstName());
            stmt.setString(2, patient.getSurname());
            stmt.setString(3, patient.getPostcode());
            stmt.setString(4, patient.getAddress());
            stmt.setString(5, patient.getPhone());
            stmt.setString(6, patient.getEmail());
            stmt.setString(7, patient.getInsurance() == null ? "NHS" : patient.getInsurance().getId());
            stmt.setString(8, patient.getId());

            return stmt.executeUpdate() > 0
                    ? Map.entry("Patient updated successfully!", Alert.AlertType.INFORMATION)
                    : Map.entry("Error: No patient was updated.", Alert.AlertType.ERROR);
        } catch (SQLException e) {
            e.printStackTrace();
            return Map.entry("Database error occurred. Please try again.", Alert.AlertType.ERROR);
        }
    }

    public String deletePatient(String patientId) {
        if (getPatientById(patientId) == null) {
            return "Error: Patient with this ID does not exist.";
        }

        String sql = "DELETE FROM patient WHERE patientid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, patientId);
            return stmt.executeUpdate() > 0 ? "Patient deleted successfully!" : "Error: No patient was deleted.";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error occurred. Please try again.";
        }
    }
}
