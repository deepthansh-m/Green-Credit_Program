package org.example.greencredit_program;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class GreenController {

    @FXML
    private TextField searchField;

    @FXML
    private ListView<Image> feedListView;

    @FXML
    private Label dynamicLabel;

    @FXML
    private VBox leftMenu;

    @FXML
    private StackPane mainContent;

    @FXML
    private VBox dragAndDropArea;

    private boolean isCompany;
    private String username;
    private File selectedFile;

    public void setIsCompany(boolean isCompany) {
        this.isCompany = isCompany;
        // Load different data or functionalities based on the user type
        if (isCompany) {
            // Company-specific initializations
        } else {
            // User-specific initializations
        }
    }

    public void setUsername(String username) {
        this.username = username;
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
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(2), event -> updateDynamicLabel())
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        // Initialize drag-and-drop
        dragAndDropArea.setOnDragOver(event -> {
            if (event.getGestureSource() != dragAndDropArea && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        dragAndDropArea.setOnDragDropped(event -> {
            boolean success = false;
            if (event.getDragboard().hasFiles()) {
                success = true;
                for (File file : event.getDragboard().getFiles()) {
                    selectedFile = file;
                    System.out.println("Selected file: " + file.getPath());
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });

        // Load user posts
        loadUserPosts();
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

    @FXML
    private void showDragAndDropArea() {
        dragAndDropArea.setVisible(true);
    }

    @FXML
    private void selectFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("Video Files", "*.mp4", "*.avi", "*.mov")
        );
        selectedFile = fileChooser.showOpenDialog(mainContent.getScene().getWindow());
        if (selectedFile != null) {
            System.out.println("Selected file: " + selectedFile.getPath());
        }
    }

    @FXML
    private void uploadFile() {
        if (selectedFile != null) {
            handleFileUpload(selectedFile);
        } else {
            System.out.println("No file selected for upload.");
        }
    }

    private void handleFileUpload(File file) {
        try {
            byte[] fileData = Files.readAllBytes(file.toPath());
            boolean success = Database.saveFileInfo(username, file.getName(), fileData, isCompany);
            if (success) {
                System.out.println("File info saved to database successfully.");
                loadUserPosts();
            } else {
                System.out.println("Failed to save file info to database.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadUserPosts() {
        List<Image> posts = Database.getUserPosts(username, isCompany);
        feedListView.getItems().clear();
        feedListView.getItems().addAll(posts);

        feedListView.setCellFactory(param -> new ListCell<Image>() {
            private final ImageView imageView = new ImageView();

            @Override
            protected void updateItem(Image image, boolean empty) {
                super.updateItem(image, empty);
                if (empty || image == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    imageView.setImage(image);
                    imageView.setFitWidth(100);
                    imageView.setPreserveRatio(true);
                    setGraphic(imageView);
                }
            }
        });
    }
}
