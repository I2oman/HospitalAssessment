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

public class DrugDAO {
    private final Connection connection;

    public DrugDAO(DatabaseManager dbManager) {
        this.connection = dbManager.getConnection();
    }

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

    private Drug extractDrugFromResultSet(ResultSet rs) throws SQLException {
        return new Drug(
                rs.getString("drugid"),
                rs.getString("drugname"),
                rs.getString("sideeffects"),
                rs.getString("benefits")
        );
    }

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
