package org.example.greencredit_program;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import java.util.List;

public class CompanyController {
    @FXML
    private TextField searchField;
    @FXML
    private ListView<String> searchResultsListView;
    @FXML
    private TextField creditAmountField;
    @FXML
    private Label moneyAmountLabel;

    private String companyName;
    private int companyId;
    private static final double DOLLARS_PER_CREDIT = 20.0;

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    @FXML
    private void searchUsers() {
        String searchTerm = searchField.getText();
        List<String> users = Database.searchUsers(searchTerm);
        searchResultsListView.getItems().clear();
        searchResultsListView.getItems().addAll(users);
    }

    @FXML
    private void requestCredits() {
        String selectedUser = searchResultsListView.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            try {
                int creditAmount = Integer.parseInt(creditAmountField.getText());
                double moneyAmount = calculateMoneyAmount(creditAmount);

                boolean success = Database.requestCredits(companyId, companyName, selectedUser, creditAmount, moneyAmount);
                if (success) {
                    showAlert("Request Sent", String.format("Credit request sent to %s for %d credits ($%.2f)", selectedUser, creditAmount, moneyAmount));
                    creditAmountField.clear();
                    moneyAmountLabel.setText("€0.00");
                } else {
                    showAlert("Request Failed", "There was an error sending the request. Please try again.");
                }
            } catch (NumberFormatException e) {
                showAlert("Invalid Amount", "Please enter a valid number for the credit amount.");
            }
        } else {
            showAlert("No Selection", "Please select a user to request credits from.");
        }
    }

    private double calculateMoneyAmount(int creditAmount) {
        return creditAmount * DOLLARS_PER_CREDIT;
    }

    @FXML
    public void updateMoneyAmount() {
        try {
            int creditAmount = Integer.parseInt(creditAmountField.getText().trim());
            double moneyAmount = calculateMoneyAmount(creditAmount);
            moneyAmountLabel.setText(String.format("€%.2f", moneyAmount));
        } catch (NumberFormatException e) {
            moneyAmountLabel.setText("€0.00");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}