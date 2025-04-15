package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataSourceBK {
    private static DataSourceBK instance;
    private Connection cnx;
    
    private final String URL = "jdbc:mysql://localhost:3306/loe2";
    private final String USERNAME = "root";
    private final String PASSWORD = "";
    
    private DataSourceBK() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            cnx = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Connected to Database!");
        } catch (ClassNotFoundException ex) {
            System.err.println("MySQL Driver not found: " + ex.getMessage());
        } catch (SQLException ex) {
            System.err.println("Database connection error: " + ex.getMessage());
        }
    }
    
    public static DataSourceBK getInstance() {
        if(instance == null) {
            instance = new DataSourceBK();
        }
        return instance;
    }
    
    public Connection getCnx() {
        try {
            if (cnx == null || cnx.isClosed()) {
                instance = new DataSourceBK();
            }
        } catch (SQLException ex) {
            System.err.println("Error checking connection: " + ex.getMessage());
        }
        return cnx;
    }
    
    public static void closeConnection() {
        if (instance != null && instance.cnx != null) {
            try {
                instance.cnx.close();
                System.out.println("Database connection closed");
            } catch (SQLException ex) {
                System.err.println("Error closing connection: " + ex.getMessage());
            }
        }
    }
} 