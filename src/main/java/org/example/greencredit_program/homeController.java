package org.example.greencredit_program;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class homeController {
    @FXML private TextField postTextField;
    @FXML private VBox dragAndDropArea;
    @FXML private ListView<Image> feedListView;
    @FXML private VBox profileView;

    private String username;
    private boolean isCompany;
    private File selectedFile;

    public void setUsername(String username) {
        this.username = username;
    }

    public void setIsCompany(boolean isCompany) {
        this.isCompany = isCompany;
    }

    @FXML
    public void initialize() {
        // Initialize drag-and-drop functionality
        initializeDragAndDrop();

        // Load user posts
        loadUserPosts();
    }

    private void initializeDragAndDrop() {
        dragAndDropArea.setOnDragOver(event -> {
            if (event.getGestureSource() != dragAndDropArea && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(javafx.scene.input.TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        dragAndDropArea.setOnDragDropped(event -> {
            boolean success = false;
            if (event.getDragboard().hasFiles()) {
                success = true;
                for (File file : event.getDragboard().getFiles()) {
                    selectedFile = file;
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
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
        selectedFile = fileChooser.showOpenDialog(profileView.getScene().getWindow());
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
            boolean success = Database.saveFileInfoAndAwardCredits(username, file.getName(), fileData, isCompany);
            if (success) {
                System.out.println("File info saved to database and credits awarded successfully.");
                loadUserPosts();
                showAlert("Upload Successful", "You've been awarded 10 credits for your upload!");
            } else {
                System.out.println("Failed to save file info to database.");
                showAlert("Upload Failed", "There was an error uploading your file. Please try again.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Upload Error", "An error occurred while reading the file.");
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

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}