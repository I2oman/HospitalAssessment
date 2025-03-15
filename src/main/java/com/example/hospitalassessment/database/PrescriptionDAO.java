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

public class PrescriptionDAO {
    private final Connection connection;
    private final DoctorDAO doctorDAO;
    private final PatientDAO patientDAO;
    private final DrugDAO drugDAO;

    public PrescriptionDAO(DatabaseManager dbManager) {
        this.connection = dbManager.getConnection();
        this.doctorDAO = new DoctorDAO(dbManager);
        this.patientDAO = new PatientDAO(dbManager);
        this.drugDAO = new DrugDAO(dbManager);
    }

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
