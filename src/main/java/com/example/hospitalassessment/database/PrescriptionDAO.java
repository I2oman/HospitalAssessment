package com.example.hospitalassessment.database;

import com.example.hospitalassessment.models.Doctor;
import com.example.hospitalassessment.models.Drug;
import com.example.hospitalassessment.models.Patient;
import com.example.hospitalassessment.models.Prescription;
import javafx.scene.control.Alert;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Manages CRUD operations for prescription-related data in the database.
 * Interacts with PatientDAO, DoctorDAO, and DrugDAO.
 * Utilizes a database connection to execute SQL queries.
 */
public class PrescriptionDAO {
    private final Connection connection; // Represents the database connection.
    private final DoctorDAO doctorDAO; // Manages doctor-related data operations.
    private final PatientDAO patientDAO; // Manages patient-related data operations.
    private final DrugDAO drugDAO; // Manages drug-related data operations.

    /**
     * Initializes a PrescriptionDAO instance with the provided DatabaseManager.
     *
     * @param dbManager the DatabaseManager instance for managing the database connection
     */
    public PrescriptionDAO(DatabaseManager dbManager) {
        this.connection = dbManager.getConnection();
        this.doctorDAO = new DoctorDAO(dbManager);
        this.patientDAO = new PatientDAO(dbManager);
        this.drugDAO = new DrugDAO(dbManager);
    }

    /**
     * Retrieves all prescriptions from the database.
     *
     * @return a list of Prescription objects representing all prescriptions in the database.
     */
    public List<Prescription> getAllPrescriptions() {
        List<Prescription> prescriptions = new ArrayList<>();
        String sql = "SELECT * FROM prescription";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                prescriptions.add(extractPrescriptionFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return prescriptions;
    }

    /**
     * Retrieves a prescription by its unique identifier from the database.
     *
     * @param prescriptionId the unique identifier of the prescription to retrieve
     * @return the Prescription corresponding to the specified ID, or null if not found
     */
    public Prescription getPrescriptionById(String prescriptionId) {
        String sql = "SELECT * FROM prescription WHERE prescriptionid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, prescriptionId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractPrescriptionFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Extracts a Prescription object from the given ResultSet.
     *
     * @param rs the ResultSet containing prescription data.
     * @return a Prescription object populated with data from the ResultSet.
     * @throws SQLException if a database access error occurs.
     */
    private Prescription extractPrescriptionFromResultSet(ResultSet rs) throws SQLException {
        Doctor doctor = doctorDAO.getDoctorById(rs.getString("doctorid"));
        Patient patient = patientDAO.getPatientById(rs.getString("patientid"));
        Drug drug = drugDAO.getDrugById(rs.getString("drugid"));

        return new Prescription(
                rs.getString("prescriptionid"),
                rs.getDate("dateprescribed"),
                rs.getInt("dosage"),
                rs.getInt("duration"),
                rs.getString("comment"),
                drug,
                doctor,
                patient
        );
    }

    /**
     * Adds a new prescription to the database.
     *
     * @param prescription the Prescription object to be added
     * @return a Map.Entry containing a message as key and an alert type as value
     */
    public Map.Entry<String, Alert.AlertType> addPrescription(Prescription prescription) {
        if (getPrescriptionById(prescription.getId()) != null) {
            return Map.entry("Error: A prescription with this ID already exists.", Alert.AlertType.ERROR);
        }

        String sql = "INSERT INTO prescription (prescriptionid, dateprescribed, dosage, duration, comment, drugid, doctorid, patientid) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, prescription.getId());
            stmt.setDate(2, prescription.getDatePrescribed());
            stmt.setInt(3, prescription.getDosage());
            stmt.setInt(4, prescription.getDuration());
            stmt.setString(5, prescription.getComment());
            stmt.setString(6, prescription.getDrug().getId());
            stmt.setString(7, prescription.getDoctor().getId());
            stmt.setString(8, prescription.getPatient().getId());

            return stmt.executeUpdate() > 0
                    ? Map.entry("Prescription added successfully!", Alert.AlertType.INFORMATION)
                    : Map.entry("Error: Prescription could not be added.", Alert.AlertType.ERROR);
        } catch (SQLException e) {
            e.printStackTrace();
            return Map.entry("Database error occurred. Please try again.", Alert.AlertType.ERROR);
        }
    }

    /**
     * Updates an existing prescription in the database.
     *
     * @param prescription the Prescription object containing updated data
     * @return a Map.Entry containing a message and an AlertType indicating success or error
     */
    public Map.Entry<String, Alert.AlertType> updatePrescription(Prescription prescription) {
        if (getPrescriptionById(prescription.getId()) == null) {
            return Map.entry("Error: Prescription with this ID does not exist.", Alert.AlertType.ERROR);
        }

        String sql = "UPDATE prescription SET dateprescribed = ?, dosage = ?, duration = ?, comment = ?, drugid = ?, doctorid = ?, patientid = ? WHERE prescriptionid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDate(1, prescription.getDatePrescribed());
            stmt.setInt(2, prescription.getDosage());
            stmt.setInt(3, prescription.getDuration());
            stmt.setString(4, prescription.getComment());
            stmt.setString(5, prescription.getDrug().getId());
            stmt.setString(6, prescription.getDoctor().getId());
            stmt.setString(7, prescription.getPatient().getId());
            stmt.setString(8, prescription.getId());

            return stmt.executeUpdate() > 0
                    ? Map.entry("Prescription updated successfully!", Alert.AlertType.INFORMATION)
                    : Map.entry("Error: No prescription was updated.", Alert.AlertType.ERROR);
        } catch (SQLException e) {
            e.printStackTrace();
            return Map.entry("Database error occurred. Please try again.", Alert.AlertType.ERROR);
        }
    }

    /**
     * Deletes a prescription from the database based on the provided prescription ID.
     *
     * @param prescriptionId the unique identifier of the prescription to delete
     * @return a message indicating whether the deletion was successful or an error occurred
     */
    public String deletePrescription(String prescriptionId) {
        if (getPrescriptionById(prescriptionId) == null) {
            return "Error: Prescription with this ID does not exist.";
        }

        String sql = "DELETE FROM prescription WHERE prescriptionid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, prescriptionId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0 ? "Prescription deleted successfully!" : "Error: No prescription was deleted.";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error occurred. Please try again.";
        }
    }
}
