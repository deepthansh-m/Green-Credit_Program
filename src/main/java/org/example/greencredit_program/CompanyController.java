package org.example.greencredit_program;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.util.List;

public class CompanyController {
    @FXML
    private TextField searchField;
    @FXML
    private ListView<String> searchResultsListView;
    @FXML
    private TextField creditAmountField;

    private String companyName;

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
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
                int amount = Integer.parseInt(creditAmountField.getText());
                boolean success = Database.requestCredits(companyName, selectedUser, amount);
                if (success) {
                    showAlert("Request Sent", "Credit request sent to " + selectedUser);
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

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
