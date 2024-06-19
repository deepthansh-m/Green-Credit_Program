package org.example.greencredit_program;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;

import java.io.IOException;

public class CreateUserController {

    @FXML
    private TextField newUsernameField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private CheckBox isCompanyCheckbox;

    @FXML
    private void handleCreateUser() {
        String username = newUsernameField.getText();
        String password = newPasswordField.getText();
        boolean isCompany = isCompanyCheckbox.isSelected();

        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("Username and password cannot be empty.");
            return;
        }

        if (Database.registerUser(username, password, isCompany)) {
            System.out.println("User created successfully.");

            // Navigate back to login view
            handleBackToLogin();
        } else {
            System.out.println("Error creating user.");
        }
    }

    @FXML
    private void handleBackToLogin() {
        try {
            Stage stage = (Stage) newUsernameField.getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 400, 400);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
