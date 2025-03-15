package com.example.hospitalassessment.database;

import com.example.hospitalassessment.models.Drug;
import javafx.scene.control.Alert;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Handles operations related to the Drug entity in the database.
 */
public class DrugDAO {
    private final Connection connection; // Represents the database connection.

    /**
     * Constructs a DrugDAO instance with a database connection.
     *
     * @param dbManager the DatabaseManager providing the database connection
     */
    public DrugDAO(DatabaseManager dbManager) {
        this.connection = dbManager.getConnection();
    }

    /**
     * Retrieves a list of all drugs from the database.
     *
     * @return a list of Drug objects representing all drugs in the database.
     */
    public List<Drug> getAllDrugs() {
        List<Drug> drugs = new ArrayList<>();
        String sql = "SELECT * FROM drug";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                drugs.add(extractDrugFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return drugs;
    }

    /**
     * Retrieves a Drug object based on the specified drug ID.
     *
     * @param drugId the ID of the drug to retrieve
     * @return the Drug object if found, otherwise null
     */
    public Drug getDrugById(String drugId) {
        String sql = "SELECT * FROM drug WHERE drugid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, drugId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractDrugFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Extracts a Drug object from the given ResultSet.
     *
     * @param rs the ResultSet containing drug data
     * @return a Drug object populated with data from the ResultSet
     * @throws SQLException if a database access error occurs
     */
    private Drug extractDrugFromResultSet(ResultSet rs) throws SQLException {
        return new Drug(
                rs.getString("drugid"),
                rs.getString("drugname"),
                rs.getString("sideeffects"),
                rs.getString("benefits")
        );
    }

    /**
     * Adds a drug to the database.
     *
     * @param drug the drug to be added
     * @return a Map.Entry containing a status message and an alert type
     */
    public Map.Entry<String, Alert.AlertType> addDrug(Drug drug) {
        if (getDrugById(drug.getId()) != null) {
            return Map.entry("Error: A drug with this ID already exists.", Alert.AlertType.ERROR);
        }

        String sql = "INSERT INTO drug (drugid, drugname, sideeffects, benefits) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, drug.getId());
            stmt.setString(2, drug.getDrugName());
            stmt.setString(3, drug.getSideEffects());
            stmt.setString(4, drug.getBenefits());

            return stmt.executeUpdate() > 0
                    ? Map.entry("Drug added successfully!", Alert.AlertType.INFORMATION)
                    : Map.entry("Error: Drug could not be added.", Alert.AlertType.ERROR);
        } catch (SQLException e) {
            e.printStackTrace();
            return Map.entry("Database error occurred. Please try again.", Alert.AlertType.ERROR);
        }
    }

    /**
     * Updates the details of an existing drug in the database.
     *
     * @param drug the Drug object containing updated information
     * @return a Map.Entry with a status message and corresponding Alert.AlertType
     */
    public Map.Entry<String, Alert.AlertType> updateDrug(Drug drug) {
        if (getDrugById(drug.getId()) == null) {
            return Map.entry("Error: Drug with this ID does not exist.", Alert.AlertType.ERROR);
        }

        String sql = "UPDATE drug SET drugname = ?, sideeffects = ?, benefits = ? WHERE drugid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, drug.getDrugName());
            stmt.setString(2, drug.getSideEffects());
            stmt.setString(3, drug.getBenefits());
            stmt.setString(4, drug.getId());

            return stmt.executeUpdate() > 0
                    ? Map.entry("Drug updated successfully!", Alert.AlertType.INFORMATION)
                    : Map.entry("Error: No drug was updated.", Alert.AlertType.ERROR);
        } catch (SQLException e) {
            e.printStackTrace();
            return Map.entry("Database error occurred. Please try again.", Alert.AlertType.ERROR);
        }
    }

    /**
     * Deletes a drug from the database based on the provided drug ID.
     *
     * @param drugId the ID of the drug to delete
     * @return a status message indicating success or an error
     */
    public String deleteDrug(String drugId) {
        if (getDrugById(drugId) == null) {
            return "Error: Drug with this ID does not exist.";
        }

        String sql = "DELETE FROM drug WHERE drugid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, drugId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0 ? "Drug deleted successfully!" : "Error: No drug was deleted.";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error occurred. Please try again.";
        }
    }
}
