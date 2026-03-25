package application.controllers;

import application.Main;
import application.utils.ConnectionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import protocol.response;

public class RegisterController {
    
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField addressField;
    @FXML private TextField phoneField;
    @FXML private Label messageLabel;
    @FXML private Button registerButton;
    
    private ConnectionManager connectionManager;
    
    @FXML
    public void initialize() {
        connectionManager = ConnectionManager.getInstance();
    }
    
    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String address = addressField.getText().trim();
        String phone = phoneField.getText().trim();
        
        // Validation
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("Username, email, and password are required");
            return;
        }
        
        if (password.length() < 6) {
            showError("Password must be at least 6 characters");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }
        
        if (!email.contains("@")) {
            showError("Invalid email format");
            return;
        }
        
        registerButton.setDisable(true);
        messageLabel.setText("Creating account...");
        
        // Call actual register method
        response res = connectionManager.register(username, email, password, address, phone);
        
        if (res.isSuccess()) {
            showSuccess("Account created! Please login.");
            
            // Wait 2 seconds then go to login
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    javafx.application.Platform.runLater(() -> Main.showLoginScreen());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            showError(res.getMessage());
            registerButton.setDisable(false);
        }
    }
    
    @FXML
    private void handleBack() {
        Main.showLoginScreen();
    }
    
    private void showError(String message) {
        messageLabel.setStyle("-fx-text-fill: red;");
        messageLabel.setText(message);
        registerButton.setDisable(false);
    }
    
    private void showSuccess(String message) {
        messageLabel.setStyle("-fx-text-fill: green;");
        messageLabel.setText(message);
    }
}
