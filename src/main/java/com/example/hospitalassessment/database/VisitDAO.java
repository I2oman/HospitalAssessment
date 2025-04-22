package com.example.hospitalassessment.database;

import com.example.hospitalassessment.models.Doctor;
import com.example.hospitalassessment.models.Patient;
import com.example.hospitalassessment.models.Visit;
import javafx.scene.control.Alert;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * VisitDAO handles database operations for the "visit" entity.
 * It interacts with the database to perform CRUD operations.
 */
public class VisitDAO {
    private final Connection connection; // Represents the database connection.
    private final DoctorDAO doctorDAO; // Manages doctor-related data operations.
    private final PatientDAO patientDAO; // Manages patient-related data operations.

    /**
     * Constructs a VisitDAO object for managing visits in the database.
     *
     * @param dbManager the DatabaseManager object used to manage the database connection
     */
    public VisitDAO(DatabaseManager dbManager) {
        this.connection = dbManager.getConnection();
        this.doctorDAO = new DoctorDAO(dbManager);
        this.patientDAO = new PatientDAO(dbManager);
    }

    /**
     * Retrieves a list of all visits from the database.
     *
     * @return a list of Visit objects representing all visits.
     */
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

    /**
     * Retrieves a Visit object based on the provided patient ID, doctor ID, and date of visit.
     *
     * @param patientId   the ID of the patient
     * @param doctorId    the ID of the doctor
     * @param dateOfVisit the date of the visit
     * @return the Visit object if found, otherwise null
     */
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

    public Doctor getMainDoctorForPatient(String patientId) {
        String sql = "SELECT doctorid, COUNT(*) AS visit_count FROM visit WHERE patientid = ? GROUP BY doctorid ORDER BY visit_count DESC LIMIT 1";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, patientId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String doctorId = rs.getString("doctorid");
                return doctorDAO.getDoctorById(doctorId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Extracts a Visit object from the given ResultSet.
     *
     * @param rs the ResultSet containing visit-related data.
     * @return a Visit object populated with the data from the ResultSet.
     * @throws SQLException if an error occurs while accessing the ResultSet.
     */
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

    /**
     * Adds a new visit to the database if it does not already exist.
     *
     * @param visit the Visit object containing patient, doctor, and visit details to add
     * @return a Map.Entry containing a status message and an Alert.AlertType indicating the result
     */
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

    /**
     * Updates an existing visit record in the database.
     *
     * @param visit the Visit object containing updated patient, doctor, date, symptoms, and diagnosis details
     * @return a Map.Entry containing a status message and an alert type indicating the result of the update operation
     */
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

    /**
     * Deletes a visit from the database based on patient ID, doctor ID, and visit date.
     *
     * @param patientId   the ID of the patient
     * @param doctorId    the ID of the doctor
     * @param dateOfVisit the date of the visit
     * @return a message indicating the result of the deletion
     */
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
