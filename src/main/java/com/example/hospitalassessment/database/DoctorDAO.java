package com.example.hospitalassessment.database;

import com.example.hospitalassessment.models.Doctor;
import javafx.scene.control.Alert;

import java.sql.*;
import java.util.*;

/**
 * DoctorDAO is responsible for handling CRUD operations for the Doctor entity in the database.
 * It utilizes a database connection provided by DatabaseManager for executing queries.
 */
public class DoctorDAO {
    private final Connection connection; // Represents the database connection.

    /**
     * Initializes DoctorDAO with a database connection.
     *
     * @param dbManager the DatabaseManager providing the database connection
     */
    public DoctorDAO(DatabaseManager dbManager) {
        this.connection = dbManager.getConnection();
    }

    /**
     * Retrieves all doctors from the database.
     *
     * @return a list of Doctor objects representing all doctors in the database.
     */
    public List<Doctor> getAllDoctors() {
        List<Doctor> doctors = new ArrayList<>();
        String sql = "SELECT * FROM doctor";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                doctors.add(extractDoctorFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return doctors;
    }

    /**
     * Retrieves a Doctor object from the database based on the provided doctor ID.
     *
     * @param doctorId the unique identifier of the doctor.
     * @return a Doctor object if found, otherwise null.
     */
    public Doctor getDoctorById(String doctorId) {
        String sql = "SELECT * FROM doctor WHERE doctorid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, doctorId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractDoctorFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves a Doctor object from the database based on the provided email.
     *
     * @param email the email of the doctor to retrieve
     * @return the Doctor object if found, or null if no matching doctor exists
     */
    public Doctor getDoctorByEmail(String email) {
        String sql = "SELECT * FROM doctor WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractDoctorFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves a doctor from the database based on the full name.
     *
     * @param fullName the full name of the doctor to retrieve
     * @return a Doctor object if found, otherwise null
     */
    public Doctor getDoctorByFullName(String fullName) {
        String sql = "SELECT * FROM doctor WHERE CONCAT(firstname, ' ', surname) = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, fullName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractDoctorFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Extracts a Doctor object from the given ResultSet.
     *
     * @param rs the ResultSet containing doctor data
     * @return a Doctor object constructed from the ResultSet
     * @throws SQLException if a database access error occurs
     */
    private Doctor extractDoctorFromResultSet(ResultSet rs) throws SQLException {
        return new Doctor(
                rs.getString("doctorid"),
                rs.getString("firstname"),
                rs.getString("surname"),
                rs.getString("address"),
                rs.getString("email"),
                rs.getString("specialization"),
                rs.getString("hospital")
        );
    }

    /**
     * Adds a new doctor to the database.
     *
     * @param doctor the Doctor object containing the doctor's details to add
     * @return a Map.Entry containing a message and an Alert.AlertType indicating success or failure
     */
    public Map.Entry<String, Alert.AlertType> addDoctor(Doctor doctor) {
        if (getDoctorById(doctor.getId()) != null) {
            return Map.entry("Error: Doctor with this ID already exists.", Alert.AlertType.ERROR);
        }
        if (getDoctorByEmail(doctor.getEmail()) != null) {
            return Map.entry("Error: A doctor with this email already exists.", Alert.AlertType.ERROR);
        }
        String sql = "INSERT INTO doctor (doctorid, firstname, surname, address, email, specialization, hospital) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, doctor.getId());
            stmt.setString(2, doctor.getFirstName());
            stmt.setString(3, doctor.getSurname());
            stmt.setString(4, doctor.getAddress());
            stmt.setString(5, doctor.getEmail());
            stmt.setString(6, doctor.getSpecialization());
            stmt.setString(7, doctor.getHospital() != null ? doctor.getHospital() : null);

            return stmt.executeUpdate() > 0
                    ? Map.entry("Doctor added successfully!", Alert.AlertType.INFORMATION)
                    : Map.entry("Error: Doctor could not be added.", Alert.AlertType.ERROR);
        } catch (SQLException e) {
            e.printStackTrace();
            return Map.entry("Database error occurred. Please try again.", Alert.AlertType.ERROR);
        }
    }

    /**
     * Updates an existing doctor's details in the database.
     *
     * @param doctor the Doctor object containing updated details of the doctor
     * @return a Map.Entry containing a status message and an Alert.AlertType indicating success or error
     */
    public Map.Entry<String, Alert.AlertType> updateDoctor(Doctor doctor) {
        if (getDoctorById(doctor.getId()) == null) {
            return Map.entry("Error: Doctor with this ID does not exist.", Alert.AlertType.ERROR);
        }
        if (getDoctorByEmail(doctor.getEmail()) != null && !getDoctorByEmail(doctor.getEmail()).getId().equals(doctor.getId())) {
            return Map.entry("Error: A doctor with this email already exists.", Alert.AlertType.ERROR);
        }
        String sql = "UPDATE doctor SET firstname = ?, surname = ?, address = ?, email = ?, specialization = ?, hospital = ? WHERE doctorid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, doctor.getFirstName());
            stmt.setString(2, doctor.getSurname());
            stmt.setString(3, doctor.getAddress());
            stmt.setString(4, doctor.getEmail());
            stmt.setString(5, doctor.getSpecialization());
            stmt.setString(6, doctor.getHospital() != null ? doctor.getHospital() : null);
            stmt.setString(7, doctor.getId());

            return stmt.executeUpdate() > 0
                    ? Map.entry("Doctor updated successfully!", Alert.AlertType.INFORMATION)
                    : Map.entry("Error: No doctor was updated.", Alert.AlertType.ERROR);
        } catch (SQLException e) {
            e.printStackTrace();
            return Map.entry("Database error occurred. Please try again.", Alert.AlertType.ERROR);
        }
    }

    /**
     * Deletes a doctor from the database based on the provided doctor ID.
     *
     * @param doctorId the unique identifier of the doctor to be deleted.
     * @return a message indicating the result of the deletion operation.
     */
    public String deleteDoctor(String doctorId) {
        if (getDoctorById(doctorId) == null) {
            return "Error: Doctor with this ID does not exist.";
        }

        String sql = "DELETE FROM doctor WHERE doctorid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, doctorId);
            return stmt.executeUpdate() > 0 ? "Doctor deleted successfully!" : "Error: No doctor was deleted.";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error occurred. Please try again.";
        }
    }
}
