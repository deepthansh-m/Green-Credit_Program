package org.example.greencredit_program;

import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/green_credit_db";
    private static final String USER = "root"; // Change to your database user
    private static final String PASSWORD = "Dimpu@2004"; // Change to your database password

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
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
        if (!isValid(username) || !isValid(password)) {
            return false;
        }

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

    private static boolean isValid(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        char firstChar = input.charAt(0);
        return Character.isLetterOrDigit(firstChar);
    }

    public static boolean saveFileInfo(String username, String fileName, byte[] fileData, boolean isCompany) {
        String table = isCompany ? "company_posts" : "user_posts";
        String query = "INSERT INTO " + table + " (username, file_name, file_data) VALUES (?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, fileName);
            pstmt.setBytes(3, fileData);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static List<Image> getUserPosts(String username, boolean isCompany) {
        String table = isCompany ? "company_posts" : "user_posts";
        String query = "SELECT file_data FROM " + table + " WHERE username = ?";
        List<Image> posts = new ArrayList<>();
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                byte[] fileData = rs.getBytes("file_data");
                if (fileData != null) {
                    InputStream inputStream = new ByteArrayInputStream(fileData);
                    Image image = new Image(inputStream);
                    posts.add(image);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }
}
