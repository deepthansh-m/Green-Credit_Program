package org.example.greencredit_program;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class GreenController {

    @FXML
    private TextField searchField;

    @FXML
    private TextField searchField2;

    @FXML
    private ListView<String> searchResultsListView;

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
    @FXML
    private TextField creditAmountField;

    @FXML
    private void showTransactionsView() {
        Node transactionsView = loadTransactionsView();
        mainContent.getChildren().setAll(transactionsView);
    }

    private Node loadTransactionsView() {
        VBox transactionsView = new VBox(10);
        transactionsView.setPadding(new Insets(20));

        Label titleLabel = new Label("Transactions");
        titleLabel.setStyle("-fx-font-size: 24; -fx-font-weight: bold;");

        TextField amountField = new TextField();
        amountField.setPromptText("Enter amount");

        TextField recipientField = new TextField();
        recipientField.setPromptText("Enter recipient username");

        Button transactButton = new Button("Transact");
        transactButton.setOnAction(e -> performTransaction(amountField.getText(), recipientField.getText()));

        ListView<String> transactionHistoryList = new ListView<>();
        transactionHistoryList.setPrefHeight(200);

        transactionsView.getChildren().addAll(titleLabel, amountField, recipientField, transactButton, new Label("Transaction History:"), transactionHistoryList);

        // Load transaction history
        loadTransactionHistory(transactionHistoryList);

        return transactionsView;
    }

    private void performTransaction(String amountStr, String recipient) {
        try {
            double amount = Double.parseDouble(amountStr);
            boolean success = Database.performTransaction(username, recipient, amount, isCompany);
            if (success) {
                System.out.println("Transaction successful");
                // Refresh transaction history
                loadTransactionHistory((ListView<String>) ((VBox) mainContent.getChildren().get(0)).getChildren().get(5));
            } else {
                System.out.println("Transaction failed");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount entered");
        }
    }

    private void loadTransactionHistory(ListView<String> listView) {
        List<String> history = Database.getTransactionHistory(username, isCompany);
        listView.getItems().clear();
        listView.getItems().addAll(history);
    }

    private boolean isCompany;
    private String username;
    private File selectedFile;

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
    private Label creditBalanceLabel;

    @FXML
    private ListView<CreditRequest> creditRequestsListView;

    @FXML
    private TextField addMoneyField;
    @FXML
    private Label accountBalanceLabel;
    @FXML
    private Button addMoneyButton;

    private double accountBalance = 0.0;

    @FXML
    private Label moneyAmountLabel;

    private static final double DOLLARS_PER_CREDIT = 20.0;

    @FXML
    void initialize() {
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
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });

        // Load user posts
        loadUserPosts();
        updateCreditBalance();
        if (!isCompany) {
            loadCreditRequests();
        }
        updateAccountBalanceDisplay();
        addMoneyButton.setOnAction(event -> addMoneyToAccount());
        accountBalance = Database.getAccountBalance(username);
        updateAccountBalanceDisplay();
        addMoneyButton.setOnAction(event -> addMoneyToAccount());
    }

    private void updateAccountBalanceDisplay() {
        accountBalanceLabel.setText(String.format("Account Balance: $%.2f", accountBalance));
    }

    @FXML
    private void addMoneyToAccount() {
        String amountStr = addMoneyField.getText().trim();
        if (amountStr.isEmpty()) {
            showAlert("Invalid Amount", "Please enter an amount to add.");
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                showAlert("Invalid Amount", "Please enter a positive amount.");
                return;
            }

            boolean success = Database.addMoneyToAccount(username, amount);
            if (success) {
                accountBalance += amount;
                updateAccountBalanceDisplay();
                addMoneyField.clear();
                showAlert("Money Added", String.format("$%.2f has been added to your account.", amount));
            } else {
                showAlert("Transaction Failed", "There was an error adding money to your account. Please try again.");
            }
        } catch (NumberFormatException e) {
            showAlert("Invalid Amount", "Please enter a valid number.");
        }
    }

    private void updateCreditBalance() {
        int credits = Database.getUserCredits(username, isCompany);
        creditBalanceLabel.setText("Credits: " + credits);
    }

    private void handleFileUpload(File file) {
        try {
            byte[] fileData = Files.readAllBytes(file.toPath());
            boolean success = Database.saveFileInfoAndAwardCredits(username, file.getName(), fileData, isCompany);
            if (success) {
                System.out.println("File info saved to database and credits awarded successfully.");
                loadUserPosts();
                updateCreditBalance();
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

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
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
            leftMenu.setPrefWidth(350);
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
                new Label("User Name:"),
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
            Scene scene = new Scene(fxmlLoader.load(), 400, 400);
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
    private void loadCreditRequests() {
        List<CreditRequest> requests = Database.getPendingCreditRequests(username);
        creditRequestsListView.getItems().clear();
        creditRequestsListView.getItems().addAll(requests);
        creditRequestsListView.setCellFactory(param -> new ListCell<CreditRequest>() {
            @Override
            protected void updateItem(CreditRequest item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getCompanyName() + " requests " + item.getAmount() + " credits");
                }
            }
        });
    }
    private void updateCreditBalanceDisplay() {
        int credits = Database.getUserCredits(username, isCompany);
        creditBalanceLabel.setText("Credits: " + credits);
    }

    @FXML
    public void searchUsers() {
        String searchTerm = searchField2.getText().trim();

        if (searchTerm.isEmpty()) {
            // Clear the list view if the search term is empty
            searchResultsListView.getItems().clear();
            return;
        }

        // Call the database method to search for users
        List<String> users = Database.searchUsers(searchTerm);

        // Update the ListView with the search results
        searchResultsListView.getItems().clear();
        searchResultsListView.getItems().addAll(users);

        if (users.isEmpty()) {
            // If no users found, add a message to the ListView
            searchResultsListView.getItems().add("No users found");
        }
    }

    private String companyName; // This should be set when the company logs in

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
    private int userId;

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setIsCompany(boolean isCompany) {
        this.isCompany = isCompany;
    }

    @FXML
    public void requestCredits() {
        String selectedUser = searchResultsListView.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert("No User Selected", "Please select a user to request credits from.");
            return;
        }

        String amountStr = creditAmountField.getText().trim();
        if (amountStr.isEmpty()) {
            showAlert("Invalid Amount", "Please enter a credit amount.");
            return;
        }

        try {
            int creditAmount = Integer.parseInt(amountStr);
            if (creditAmount <= 0) {
                showAlert("Invalid Amount", "Please enter a positive credit amount.");
                return;
            }

            double moneyAmount = calculateMoneyAmount(creditAmount);

            boolean success = Database.requestCredits(userId, username, selectedUser, creditAmount, moneyAmount);
            if (success) {
                showAlert("Request Sent", String.format("Credit request sent to %s for %d credits ($%.2f)", selectedUser, creditAmount, moneyAmount));
                creditAmountField.clear();
                moneyAmountLabel.setText("$0.00");
            } else {
                showAlert("Request Failed", "There was an error sending the request. Please try again.");
            }
        } catch (NumberFormatException e) {
            showAlert("Invalid Amount", "Please enter a valid number for the credit amount.");
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
            moneyAmountLabel.setText(String.format("$%.2f", moneyAmount));
        } catch (NumberFormatException e) {
            moneyAmountLabel.setText("$0.00");
        }
    }

    @FXML
    private void uploadFile() {
        if (selectedFile != null) {
            handleFileUpload(selectedFile);
            updateCreditBalanceDisplay();
        } else {
            System.out.println("No file selected for upload.");
        }
    }

    @FXML
    private void approveCreditRequest() {
        CreditRequest selectedRequest = creditRequestsListView.getSelectionModel().getSelectedItem();
        if (selectedRequest != null) {
            boolean success = Database.approveCreditRequest(selectedRequest.getId(), username);
            if (success) {
                showAlert("Request Approved", "You've transferred " + selectedRequest.getAmount() + " credits to " + selectedRequest.getCompanyName());
                updateCreditBalanceDisplay();
                loadCreditRequests();
            } else {
                showAlert("Approval Failed", "There was an error approving the request. Please try again.");
            }
        }
    }
}
