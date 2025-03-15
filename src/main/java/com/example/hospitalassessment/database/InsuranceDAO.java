package com.example.hospitalassessment.database;

import com.example.hospitalassessment.models.Insurance;
import javafx.scene.control.Alert;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object (DAO) for managing Insurance entities in the database.
 */
public class InsuranceDAO {
    private final Connection connection; // Represents the database connection.

    /**
     * Constructs an InsuranceDAO instance with a database connection.
     *
     * @param dbManager the DatabaseManager providing the database connection
     */
    public InsuranceDAO(DatabaseManager dbManager) {
        this.connection = dbManager.getConnection();
    }

    /**
     * Retrieves a list of all insurance records from the database.
     *
     * @return a list of Insurance objects representing all insurances in the database.
     */
    public List<Insurance> getAllInsurance() {
        List<Insurance> insurances = new ArrayList<>();
        String sql = "SELECT * FROM insurance";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                insurances.add(extractInsuranceFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return insurances;
    }

    /**
     * Retrieves an Insurance object based on the specified insurance ID.
     *
     * @param insuranceId the ID of the insurance to retrieve
     * @return the Insurance object if found, otherwise null
     */
    public Insurance getInsuranceById(String insuranceId) {
        String sql = "SELECT * FROM insurance WHERE insuranceid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, insuranceId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractInsuranceFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves an Insurance object based on the specified company name.
     *
     * @param companyName the name of the company to search for
     * @return the Insurance object if found, otherwise null
     */
    public Insurance getInsuranceByCompany(String companyName) {
        String sql = "SELECT * FROM insurance WHERE company = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, companyName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractInsuranceFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Extracts an Insurance object from the given ResultSet.
     *
     * @param rs the ResultSet containing insurance data
     * @return an Insurance object populated with data from the ResultSet
     * @throws SQLException if a database access error occurs
     */
    private Insurance extractInsuranceFromResultSet(ResultSet rs) throws SQLException {
        return new Insurance(
                rs.getString("insuranceid"),
                rs.getString("company"),
                rs.getString("address"),
                rs.getString("phone")
        );
    }

    /**
     * Adds a new insurance record to the database.
     *
     * @param insurance the Insurance object to be added
     * @return a map entry containing a message and alert type, indicating success or failure
     */
    public Map.Entry<String, Alert.AlertType> addInsurance(Insurance insurance) {
        if (getInsuranceById(insurance.getId()) != null) {
            return Map.entry("Error: An insurance with this ID already exists.", Alert.AlertType.ERROR);
        }

        String sql = "INSERT INTO insurance (insuranceid, company, address, phone) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, insurance.getId());
            stmt.setString(2, insurance.getCompany());
            stmt.setString(3, insurance.getAddress());
            stmt.setString(4, insurance.getPhone());

            return stmt.executeUpdate() > 0
                    ? Map.entry("Insurance added successfully!", Alert.AlertType.INFORMATION)
                    : Map.entry("Error: Insurance could not be added.", Alert.AlertType.ERROR);
        } catch (SQLException e) {
            e.printStackTrace();
            return Map.entry("Database error occurred. Please try again.", Alert.AlertType.ERROR);
        }
    }

    /**
     * Updates the details of an existing insurance record in the database.
     *
     * @param insurance the Insurance object containing updated data
     * @return a map entry with a message and alert type indicating success or failure
     */
    public Map.Entry<String, Alert.AlertType> updateInsurance(Insurance insurance) {
        if (getInsuranceById(insurance.getId()) == null) {
            return Map.entry("Error: Insurance with this ID does not exist.", Alert.AlertType.ERROR);
        }

        String sql = "UPDATE insurance SET company = ?, address = ?, phone = ? WHERE insuranceid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, insurance.getCompany());
            stmt.setString(2, insurance.getAddress());
            stmt.setString(3, insurance.getPhone());
            stmt.setString(4, insurance.getId());

            return stmt.executeUpdate() > 0
                    ? Map.entry("Insurance updated successfully!", Alert.AlertType.INFORMATION)
                    : Map.entry("Error: No insurance was updated.", Alert.AlertType.ERROR);
        } catch (SQLException e) {
            e.printStackTrace();
            return Map.entry("Database error occurred. Please try again.", Alert.AlertType.ERROR);
        }
    }

    /**
     * Deletes an insurance record from the database based on the provided insurance ID.
     *
     * @param insuranceId the ID of the insurance to delete
     * @return a message indicating the outcome of the operation
     */
    public String deleteInsurance(String insuranceId) {
        if (getInsuranceById(insuranceId) == null) {
            return "Error: Insurance with this ID does not exist.";
        }

        String sql = "DELETE FROM insurance WHERE insuranceid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, insuranceId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0 ? "Insurance deleted successfully!" : "Error: No insurance was deleted.";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error occurred. Please try again.";
        }
    }
}
