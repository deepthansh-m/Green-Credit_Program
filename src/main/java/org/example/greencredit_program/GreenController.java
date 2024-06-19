package org.example.greencredit_program;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class GreenController {

    @FXML
    private TextField searchField;

    @FXML
    private ListView<String> connectionsListView;

    @FXML
    private ListView<String> feedListView;

    @FXML
    private Label dynamicLabel;

    @FXML
    private VBox leftMenu;

    @FXML
    private StackPane mainContent;

    private boolean isCompany;

    public void setIsCompany(boolean isCompany) {
        this.isCompany = isCompany;
        // Load different data or functionalities based on the user type
        if (isCompany) {
            // Company-specific initializations
        } else {
            // User-specific initializations
        }
    }

    private final List<String> dynamicMessages = Arrays.asList(
            "You have 5 new transactions with the company.",
            "Your current green credits balance is 1200.",
            "3 new green credits earned today.",
            "Latest transaction: $500 on project A.",
            "Next transaction due in 3 days."
    );

    private final AtomicInteger currentIndex = new AtomicInteger(0);

    @FXML
    private void initialize() {
        connectionsListView.getItems().addAll("Connection 1", "Connection 2", "Connection 3");

        feedListView.getItems().addAll("Post 1", "Post 2", "Post 3");

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(2), event -> updateDynamicLabel())
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void updateDynamicLabel() {
        int index = currentIndex.getAndUpdate(i -> (i + 1) % dynamicMessages.size());
        dynamicLabel.setText(dynamicMessages.get(index));
    }

    @FXML
    private void onSearch() {
        String searchText = searchField.getText();
        System.out.println("Searching for: " + searchText);
    }

    @FXML
    private void toggleLeftMenu() {
        boolean isVisible = leftMenu.isVisible();
        leftMenu.setVisible(!isVisible);
        if (isVisible) {
            leftMenu.setPrefWidth(0);
        } else {
            leftMenu.setPrefWidth(200);
        }
    }

    @FXML
    private void showProfileView() {
        Node profileView = loadProfileView();
        mainContent.getChildren().setAll(profileView);
    }

    private Node loadProfileView() {
        VBox profileView = new VBox();
        profileView.setSpacing(10);
        profileView.getChildren().addAll(
                new Label("Profile"),
                new Label("User Name: John Doe"),
                new Label("Email: john.doe@example.com")
        );
        return profileView;
    }

    @FXML
    private void handleLogout() {
        try {
            Files.deleteIfExists(Paths.get("login.txt"));

            Stage stage = (Stage) mainContent.getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 600, 400);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
