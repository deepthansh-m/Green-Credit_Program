package org.example.greencredit_program;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Database {
    private static final String URL = "jdbc:mysql://localhost:3306/green_credit_db";
    private static final String USER = "root"; // Change to your database user
    private static final String PASSWORD = "Dimpu@2004"; // Change to your database password

    static {
        // Set trust store properties
        System.setProperty("javax.net.ssl.trustStore", "/path/to/truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "Dimpu@2004");
    }

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static boolean validateLogin(String username, String password, boolean isCompany) {
        String table = isCompany ? "companies" : "users";
        String query = "SELECT * FROM " + table + " WHERE username = ? AND password = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean registerUser(String username, String password, boolean isCompany) {
        String table = isCompany ? "companies" : "users";
        String query = "INSERT INTO " + table + " (username, password) VALUES (?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
