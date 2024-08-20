package org.example.greencredit_program;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class TransactionController {
    @FXML private TextField amountField;
    @FXML private TextField recipientField;
    @FXML private ListView<String> transactionHistoryList;

    private String username;
    private boolean isCompany;

    public void setUsername(String username) {
        this.username = username;
    }

    public void setIsCompany(boolean isCompany) {
        this.isCompany = isCompany;
    }

    public void initialize() {
        loadTransactionHistory();
    }

    @FXML
    private void performTransaction() {
        try {
            double amount = Double.parseDouble(amountField.getText());
            String recipient = recipientField.getText();
            boolean success = Database.performTransaction(username, recipient, amount, isCompany);
            if (success) {
                System.out.println("Transaction successful");
                loadTransactionHistory();
            } else {
                System.out.println("Transaction failed");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount entered");
        }
    }

    private void loadTransactionHistory() {
        transactionHistoryList.getItems().clear();
        transactionHistoryList.getItems().addAll(Database.getTransactionHistory(username, isCompany));
    }
}