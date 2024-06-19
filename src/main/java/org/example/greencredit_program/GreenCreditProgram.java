package org.example.greencredit_program;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class GreenCreditProgram extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        String[] loginState = LoginController.loadLogin();
        if (loginState != null) {
            FXMLLoader fxmlLoader = new FXMLLoader(org.example.greencredit_program.GreenCreditProgram.class.getResource("main-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 1320, 1240);
            GreenController controller = fxmlLoader.getController();
            controller.setIsCompany(Boolean.parseBoolean(loginState[1]));
            stage.setScene(scene);
        } else {
            FXMLLoader fxmlLoader = new FXMLLoader(org.example.greencredit_program.GreenCreditProgram.class.getResource("login-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 400, 400);
            stage.setScene(scene);
        }
        stage.setTitle("Green Credit Program");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
