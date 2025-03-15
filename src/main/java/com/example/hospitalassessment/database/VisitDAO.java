package com.example.hospitalassessment.database;

import com.example.hospitalassessment.models.Doctor;
import com.example.hospitalassessment.models.Patient;
import com.example.hospitalassessment.models.Visit;
import javafx.scene.control.Alert;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VisitDAO {
    private final Connection connection;
    private final DoctorDAO doctorDAO;
    private final PatientDAO patientDAO;

    public VisitDAO(DatabaseManager dbManager) {
        this.connection = dbManager.getConnection();
        this.doctorDAO = new DoctorDAO(dbManager);
        this.patientDAO = new PatientDAO(dbManager);
    }

    public List<Visit> getAllVisits() {
        List<Visit> visits = new ArrayList<>();
        String sql = "SELECT * FROM visit";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                visits.add(extractVisitFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return visits;
    }

    public Visit getVisitByPrimaryKey(String patientId, String doctorId, Date dateOfVisit) {
        String sql = "SELECT * FROM visit WHERE patientid = ? AND doctorid = ? AND dateofvisit = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, patientId);
            stmt.setString(2, doctorId);
            stmt.setDate(3, dateOfVisit);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractVisitFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Visit extractVisitFromResultSet(ResultSet rs) throws SQLException {
        Doctor doctor = doctorDAO.getDoctorById(rs.getString("doctorid"));
        Patient patient = patientDAO.getPatientById(rs.getString("patientid"));

        return new Visit(
                patient,
                doctor,
                rs.getDate("dateofvisit"),
                rs.getString("symptoms"),
                rs.getString("diagnosis")
        );
    }

    public Map.Entry<String, Alert.AlertType> addVisit(Visit visit) {
        if (getVisitByPrimaryKey(visit.getPatient().getId(), visit.getDoctor().getId(), visit.getDateOfVisit()) != null) {
            return Map.entry("Error: A visit with this patient, doctor, and date already exists.", Alert.AlertType.ERROR);
        }

        String sql = "INSERT INTO visit (patientid, doctorid, dateofvisit, symptoms, diagnosis) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, visit.getPatient().getId());
            stmt.setString(2, visit.getDoctor().getId());
            stmt.setDate(3, visit.getDateOfVisit());
            stmt.setString(4, visit.getSymptoms());
            stmt.setString(5, visit.getDiagnosis());

            return stmt.executeUpdate() > 0
                    ? Map.entry("Visit added successfully!", Alert.AlertType.INFORMATION)
                    : Map.entry("Error: Visit could not be added.", Alert.AlertType.ERROR);
        } catch (SQLException e) {
            e.printStackTrace();
            return Map.entry("Database error occurred. Please try again.", Alert.AlertType.ERROR);
        }
    }

    public Map.Entry<String, Alert.AlertType> updateVisit(Visit visit) {
        if (getVisitByPrimaryKey(visit.getPatient().getId(), visit.getDoctor().getId(), visit.getDateOfVisit()) == null) {
            return Map.entry("Error: Visit with this patient, doctor, and date does not exist.", Alert.AlertType.ERROR);
        }

        String sql = "UPDATE visit SET symptoms = ?, diagnosis = ? WHERE patientid = ? AND doctorid = ? AND dateofvisit = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, visit.getSymptoms());
            stmt.setString(2, visit.getDiagnosis());
            stmt.setString(3, visit.getPatient().getId());
            stmt.setString(4, visit.getDoctor().getId());
            stmt.setDate(5, visit.getDateOfVisit());

            return stmt.executeUpdate() > 0
                    ? Map.entry("Visit updated successfully!", Alert.AlertType.INFORMATION)
                    : Map.entry("Error: No visit was updated.", Alert.AlertType.ERROR);
        } catch (SQLException e) {
            e.printStackTrace();
            return Map.entry("Database error occurred. Please try again.", Alert.AlertType.ERROR);
        }
    }

    public String deleteVisit(String patientId, String doctorId, Date dateOfVisit) {
        if (getVisitByPrimaryKey(patientId, doctorId, dateOfVisit) == null) {
            return "Error: Visit with this patient, doctor, and date does not exist.";
        }

        String sql = "DELETE FROM visit WHERE patientid = ? AND doctorid = ? AND dateofvisit = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, patientId);
            stmt.setString(2, doctorId);
            stmt.setDate(3, dateOfVisit);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0 ? "Visit deleted successfully!" : "Error: No visit was deleted.";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error occurred. Please try again.";
        }
    }
}
