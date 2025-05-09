package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DataSource {

    private String url = "jdbc:mysql://localhost:3306/";
    private String dbName = "loe";
    private String username = "root";
    private String password = "";
    private Connection connection;
    public static DataSource instance;

    private DataSource() {
        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Set connection properties
            Properties properties = new Properties();
            properties.setProperty("user", username);
            properties.setProperty("password", password);
            properties.setProperty("useSSL", "false");
            properties.setProperty("autoReconnect", "true");
            properties.setProperty("allowPublicKeyRetrieval", "true");
            properties.setProperty("connectTimeout", "3000");
            
            System.out.println("Attempting to connect to MySQL server at " + url);
            // First connect without specifying a database
            connection = DriverManager.getConnection(url, properties);
            System.out.println("Database server connection established successfully!");
            
            // Check if the loe database exists, create it if it doesn't
            ensureDatabaseExists();
            
            // Connect to the actual loe database
            connection.close();
            connection = DriverManager.getConnection(url + dbName, properties);
            System.out.println("Successfully connected to 'loe' database!");
            
            // Ensure the user table exists
            ensureUserTableExists();
            
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
            throw new RuntimeException("MySQL JDBC Driver not found", e);
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            System.err.println("Please make sure MySQL is running and accessible.");
            throw new RuntimeException("Database connection failed", e);
        }
    }
    
    private void ensureDatabaseExists() throws SQLException {
        Statement stmt = connection.createStatement();
        System.out.println("Checking if 'loe' database exists...");
        
        // Try to create the database if it doesn't exist
        stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName);
        System.out.println("Database 'loe' is ready!");
        stmt.close();
    }
    
    private void ensureUserTableExists() throws SQLException {
        Statement stmt = connection.createStatement();
        System.out.println("Checking if 'user' table exists...");
        
        // Create the user table if it doesn't exist
        String createTableSQL = "CREATE TABLE IF NOT EXISTS user (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL, " +
                "email VARCHAR(255) NOT NULL UNIQUE, " +
                "date_of_birth DATE, " +
                "password VARCHAR(255) NOT NULL, " +
                "img VARCHAR(255), " +
                "type VARCHAR(50), " +
                "phone INT DEFAULT 0, " +
                "rate DOUBLE DEFAULT 0.0, " +
                "score INT DEFAULT 0, " +
                "bio TEXT DEFAULT 'Vous n''avez pas encore de bio', " +
                "verified VARCHAR(1) DEFAULT '0', " +
                "google_id VARCHAR(255) DEFAULT NULL" +
                ")";
        
        stmt.executeUpdate(createTableSQL);
        System.out.println("User table is ready!");
        stmt.close();
    }
    
    public static DataSource getInstance() {
        if (instance == null) {
            instance = new DataSource();
        }
        return instance;
    }

    public Connection getConnection() {
        // Check if connection is still valid
        try {
            if (connection == null || connection.isClosed()) {
                System.out.println("Connection is closed or null. Attempting to reconnect...");
                // Recreate the connection
                Properties properties = new Properties();
                properties.setProperty("user", username);
                properties.setProperty("password", password);
                properties.setProperty("useSSL", "false");
                properties.setProperty("autoReconnect", "true");
                properties.setProperty("allowPublicKeyRetrieval", "true");
                
                connection = DriverManager.getConnection(url + dbName, properties);
                System.out.println("Database reconnection successful!");
            }
        } catch (SQLException e) {
            System.err.println("Error checking database connection: " + e.getMessage());
            throw new RuntimeException("Database connection validation failed", e);
        }
        return connection;
    }
}