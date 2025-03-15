package com.example.hospitalassessment.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages the database connection lifecycle for the application.
 */
public class DatabaseManager {
    private Connection connection; // Represents the database connection.

    /**
     * Establishes a connection to the database using the provided credentials and URL.
     *
     * @param URL      the database URL
     * @param USER     the username for the database
     * @param PASSWORD the password for the database
     */
    public DatabaseManager(String URL, String USER, String PASSWORD) {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Database connected successfully!");
        } catch (SQLException e) {
            throw new RuntimeException("Error connecting to the database", e);
        }
    }

    /**
     * Provides the active database connection.
     *
     * @return the current database connection
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Closes the database connection if it is open.
     * Logs confirmation or prints the stack trace in case of an error.
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
