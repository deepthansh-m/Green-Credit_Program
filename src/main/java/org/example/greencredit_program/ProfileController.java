package org.example.greencredit_program;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class ProfileController {
    @FXML private Label usernameLabel;
    @FXML private Label emailLabel;
    @FXML private Label creditBalanceLabel;
    @FXML private Label accountBalanceLabel;
    @FXML private Label isCompanyLabel;
    @FXML private VBox profileView;
    @FXML private TextField emailField;

    private String username;
    private boolean isCompany;

    public void setUsername(String username) {
        this.username = username;
    }

    public void setIsCompany(boolean isCompany) {
        this.isCompany = isCompany;
    }

    public void initialize() {
        usernameLabel.setText("Username: " + username);

        String email = Database.getUserEmail(username, isCompany);
        if (email != null && !email.isEmpty()) {
            emailLabel.setText("Email: " + email);
        } else {
            emailLabel.setText("Email: Not set");
        }

        int credits = Database.getUserCredits(username, isCompany);
        creditBalanceLabel.setText("Credit Balance: " + credits);

        double accountBalance = Database.getAccountBalance(username);
        accountBalanceLabel.setText(String.format("Account Balance: $%.2f", accountBalance));

        isCompanyLabel.setText("Account Type: " + (isCompany ? "Company" : "Individual"));

        emailField = new TextField();
        emailField.setPromptText("Enter new email");
        emailField.setVisible(false);
        profileView.getChildren().add(emailField);
    }

    @FXML
    private void editEmail() {
        if (emailField.isVisible()) {
            // Save the new email
            String newEmail = emailField.getText();
            if (!newEmail.isEmpty()) {
                boolean success = Database.updateUserEmail(username, newEmail, isCompany);
                if (success) {
                    emailLabel.setText("Email: " + newEmail);
                    emailField.setVisible(false);
                } else {
                    // Show error message
                    System.out.println("Failed to update email");
                }
            }
        } else {
            // Show the email field for editing
            emailField.setVisible(true);
        }
    }
}