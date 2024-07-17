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

    public static boolean requestCredits(int requesterId, String requesterName, String recipientUsername, int amount) {
        String sql = "INSERT INTO credit_requests (requester_id, requester_name, recipient_username, amount) VALUES (?, ?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, requesterId);
            pstmt.setString(2, requesterName);
            pstmt.setString(3, recipientUsername);
            pstmt.setInt(4, amount);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<CreditRequest> getPendingCreditRequests(String username) {
        List<CreditRequest> requests = new ArrayList<>();
        String sql = "SELECT * FROM credit_requests WHERE recipient_username = ? AND status = 'PENDING'";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                requests.add(new CreditRequest(
                        rs.getInt("id"),
                        rs.getString("requester_name"),
                        rs.getInt("amount")
                ));
            }
            // Add this print statement for debugging
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }

    public static boolean approveCreditRequest(int requestId, String username) {
        Connection conn = null;
        try {
            conn = connect();
            conn.setAutoCommit(false);

            // Get request details
            String getRequestQuery = "SELECT requester_name, amount FROM credit_requests WHERE id = ? AND recipient_username = ? AND status = 'PENDING'";
            int amount;
            String requesterName;
            try (PreparedStatement pstmt = conn.prepareStatement(getRequestQuery)) {
                pstmt.setInt(1, requestId);
                pstmt.setString(2, username);
                ResultSet rs = pstmt.executeQuery();
                if (!rs.next()) {
                    throw new SQLException("Invalid request");
                }
                requesterName = rs.getString("requester_name");
                amount = rs.getInt("amount");
            }

            // Update recipient's credits
            String updateRecipientQuery = "UPDATE users SET credits = credits - ? WHERE username = ? AND credits >= ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateRecipientQuery)) {
                pstmt.setInt(1, amount);
                pstmt.setString(2, username);
                pstmt.setInt(3, amount);
                int updatedRows = pstmt.executeUpdate();
                if (updatedRows == 0) {
                    throw new SQLException("Insufficient credits");
                }
            }

            // Update requester's credits
            String updateRequesterQuery = "UPDATE users SET credits = credits + ? WHERE username = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateRequesterQuery)) {
                pstmt.setInt(1, amount);
                pstmt.setString(2, requesterName);
                pstmt.executeUpdate();
            }

            // Update request status
            String updateRequestQuery = "UPDATE credit_requests SET status = 'APPROVED' WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateRequestQuery)) {
                pstmt.setInt(1, requestId);
                pstmt.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static int getUserId(String username, boolean isCompany) {
        String table = isCompany ? "companies" : "users";
        String sql = "SELECT id FROM " + table + " WHERE username = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    private String getCompanyNameForUser(int userId) throws SQLException {
        String sql = "SELECT company_name FROM users WHERE user_id = ?";
        try (PreparedStatement pstmt = connect().prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("company_name");
                }
            }
        }
        return null;
    }

    public static List<String> searchUsers(String searchTerm) {
        List<String> users = new ArrayList<>();
        String query = "SELECT username FROM users WHERE username LIKE ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, "%" + searchTerm + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                users.add(rs.getString("username"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
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
    public static boolean performTransaction(String sender, String recipient, double amount, boolean isCompanySender) {
        String senderTable = isCompanySender ? "companies" : "users";
        String recipientTable = isCompanySender ? "users" : "companies";

        Connection conn = null;
        try {
            conn = connect();
            conn.setAutoCommit(false);

            // Check sender's balance
            String checkBalanceQuery = "SELECT credits FROM " + senderTable + " WHERE username = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(checkBalanceQuery)) {
                pstmt.setString(1, sender);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    double balance = rs.getDouble("credits");
                    if (balance < amount) {
                        throw new SQLException("Insufficient credits");
                    }
                } else {
                    throw new SQLException("Sender not found");
                }
            }

            // Update sender's balance
            String updateSenderQuery = "UPDATE " + senderTable + " SET credits = credits - ? WHERE username = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateSenderQuery)) {
                pstmt.setDouble(1, amount);
                pstmt.setString(2, sender);
                pstmt.executeUpdate();
            }

            // Update recipient's balance
            String updateRecipientQuery = "UPDATE " + recipientTable + " SET credits = credits + ? WHERE username = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateRecipientQuery)) {
                pstmt.setDouble(1, amount);
                pstmt.setString(2, recipient);
                pstmt.executeUpdate();
            }

            // Record the transaction
            String recordTransactionQuery = "INSERT INTO transactions (sender, recipient, amount, transaction_date) VALUES (?, ?, ?, NOW())";
            try (PreparedStatement pstmt = conn.prepareStatement(recordTransactionQuery)) {
                pstmt.setString(1, sender);
                pstmt.setString(2, recipient);
                pstmt.setDouble(3, amount);
                pstmt.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static List<String> getTransactionHistory(String username, boolean isCompany) {
        List<String> history = new ArrayList<>();
        String query = "SELECT * FROM transactions WHERE sender = ? OR recipient = ? ORDER BY transaction_date DESC LIMIT 10";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, username);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String sender = rs.getString("sender");
                String recipient = rs.getString("recipient");
                double amount = rs.getDouble("amount");
                Timestamp date = rs.getTimestamp("transaction_date");
                String transactionStr = String.format("%s: %s %s %.2f credits to %s",
                        date.toString(),
                        sender.equals(username) ? "Sent" : "Received",
                        sender.equals(username) ? "-" : "+",
                        amount,
                        sender.equals(username) ? recipient : sender);
                history.add(transactionStr);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }
    public static boolean saveFileInfoAndAwardCredits(String username, String fileName, byte[] fileData, boolean isCompany) {
        String table = isCompany ? "company_posts" : "user_posts";
        String query = "INSERT INTO " + table + " (username, file_name, file_data) VALUES (?, ?, ?)";
        String updateCreditsQuery = "UPDATE " + (isCompany ? "companies" : "users") + " SET credits = credits + ? WHERE username = ?";

        Connection conn = null;
        try {
            conn = connect();
            conn.setAutoCommit(false);

            // Save file info
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, username);
                pstmt.setString(2, fileName);
                pstmt.setBytes(3, fileData);
                pstmt.executeUpdate();
            }

            // Award credits
            int creditsToAward = 10; // You can adjust this value
            try (PreparedStatement pstmt = conn.prepareStatement(updateCreditsQuery)) {
                pstmt.setInt(1, creditsToAward);
                pstmt.setString(2, username);
                pstmt.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static int getUserCredits(String username, boolean isCompany) {
        String table = isCompany ? "companies" : "users";
        String query = "SELECT credits FROM " + table + " WHERE username = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("credits");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
