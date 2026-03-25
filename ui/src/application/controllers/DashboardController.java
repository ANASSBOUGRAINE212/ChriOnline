package application.controllers;

import application.utils.ConnectionManager;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class DashboardController {
    
    @FXML private Label welcomeLabel;
    @FXML private VBox contentArea;
    @FXML private Button productsButton;
    @FXML private Button cartButton;
    @FXML private Button ordersButton;
    @FXML private Button logoutButton;
    
    private ConnectionManager connectionManager;
    
    @FXML
    public void initialize() {
        connectionManager = ConnectionManager.getInstance();
        
        String role = connectionManager.getUserRole();
        welcomeLabel.setText("Welcome to ChriOnline! Role: " + role);
    }
    
    @FXML
    private void showProducts() {
        try {
            Parent productsView = javafx.fxml.FXMLLoader.load(getClass().getResource("/fxml/products.fxml"));
            contentArea.getChildren().clear();
            contentArea.getChildren().add(productsView);
            VBox.setVgrow(productsView, javafx.scene.layout.Priority.ALWAYS);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load products view");
        }
    }
    
    @FXML
    private void showCart() {
        try {
            java.net.URL cartUrl = getClass().getResource("/fxml/cart.fxml");
            if (cartUrl == null) {
                System.err.println("❌ Could not find /fxml/cart.fxml");
                System.err.println("   Classpath resources:");
                System.err.println("   - Check that ui/resources folder is in Eclipse build path");
                System.err.println("   - Try: Project → Clean → Rebuild");
                showError("Could not load cart view. FXML file not found in classpath.");
                return;
            }
            
            Parent cartView = javafx.fxml.FXMLLoader.load(cartUrl);
            contentArea.getChildren().clear();
            contentArea.getChildren().add(cartView);
            VBox.setVgrow(cartView, javafx.scene.layout.Priority.ALWAYS);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load cart view: " + e.getMessage());
        }
    }
    
    @FXML
    private void showOrders() {
        try {
            java.net.URL ordersUrl = getClass().getResource("/fxml/orders.fxml");
            if (ordersUrl == null) {
                System.err.println("❌ Could not find /fxml/orders.fxml");
                showError("Could not load orders view. FXML file not found in classpath.");
                return;
            }
            
            Parent ordersView = javafx.fxml.FXMLLoader.load(ordersUrl);
            contentArea.getChildren().clear();
            contentArea.getChildren().add(ordersView);
            VBox.setVgrow(ordersView, javafx.scene.layout.Priority.ALWAYS);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load orders view: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleLogout() {
        connectionManager.logout();
        application.Main.showLoginScreen();
    }
    
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
