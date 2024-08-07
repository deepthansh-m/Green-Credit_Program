package org.example.greencredit_program;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;

import java.io.IOException;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void handleUserLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        if (Database.validateLogin(username, password, false)) {
            saveLogin(username, false);
            loadMainView(false);
        } else {
            System.out.println("Invalid user credentials.");
        }
    }

    @FXML
    private void handleCompanyLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        if (Database.validateLogin(username, password, true)) {
            saveLogin(username, true);
            loadMainView(true);
        } else {
            System.out.println("Invalid company credentials.");
        }
    }

    private void saveLogin(String username, boolean isCompany) {
        try (FileWriter writer = new FileWriter("login.txt")) {
            writer.write(username + "," + isCompany);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String[] loadLogin() {
        try {
            return Files.readAllLines(Paths.get("login.txt")).get(0).split(",");
        } catch (IOException e) {
            return null;
        }
    }

    private void loadMainView(boolean isCompany) {
        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 1350, 1250);
            GreenController controller = fxmlLoader.getController();
            controller.setIsCompany(isCompany);
            controller.setUsername(usernameField.getText());
            controller.initialize();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showCreateUserView() {
        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("create-user-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 400, 400);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void handleSuccessfulCompanyLogin(String companyName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("company-view.fxml"));
            Parent root = loader.load();

            CompanyController companyController = loader.getController();
            companyController.setCompanyName(companyName);

            int companyId = Database.getUserId(companyName, true); // true for isCompany
            companyController.setCompanyId(companyId);

            // ... set up and show the new scene ...
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
