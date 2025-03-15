package com.example.hospitalassessment.controllers;

import com.example.hospitalassessment.database.DatabaseManager;

/**
 * Defines a contract for controllers handling table views and database interactions.
 */
public interface TableController {
    /**
     * Sets the DatabaseManager for the TableController.
     *
     * @param dbManager the DatabaseManager used for managing database operations
     */
    void setDatabaseManager(DatabaseManager dbManager);
}
