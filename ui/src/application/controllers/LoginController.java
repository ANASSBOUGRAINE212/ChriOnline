package application.controllers;

import application.Main;
import application.utils.ConnectionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import protocol.response;

public class LoginController {
    
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    
    private ConnectionManager connectionManager;
    
    @FXML
    public void initialize() {
        connectionManager = ConnectionManager.getInstance();
    }
    
    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        
        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter both email and password");
            return;
        }
        
        loginButton.setDisable(true);
        messageLabel.setText("Logging in...");
        
        response res = connectionManager.login(email, password);
        
        if (res.isSuccess()) {
            String[] parts = res.getMessage().split("\\|");
            if (parts.length > 1) {
                connectionManager.setSessionToken(parts[1]);
            }
            if (parts.length > 2) {
                connectionManager.setUserRole(parts[2]);
            }
            
            // Navigate to dashboard
            Main.showDashboard();
        } else {
            showError(res.getMessage());
            loginButton.setDisable(false);
        }
    }
    
    @FXML
    private void handleRegister() {
        // TODO: Open registration dialog or screen
        Main.showRegisterScreen();
    }
    
    private void showError(String message) {
        messageLabel.setStyle("-fx-text-fill: red;");
        messageLabel.setText(message);
    }
    
    private void showInfo(String message) {
        messageLabel.setStyle("-fx-text-fill: blue;");
        messageLabel.setText(message);
    }
}
