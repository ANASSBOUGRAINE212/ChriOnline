package application.controllers;

import application.utils.ConnectionManager;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import protocol.response;

public class CartController {
    
    @FXML private VBox cartItemsContainer;
    @FXML private Label totalLabel;
    
    private ConnectionManager connectionManager;
    
    @FXML
    public void initialize() {
        connectionManager = ConnectionManager.getInstance();
        loadCart();
    }
    
    private void loadCart() {
        cartItemsContainer.getChildren().clear();
        
        response res = connectionManager.getCartItems();
        
        if (res.isSuccess()) {
            String message = res.getMessage();
            
            if (message.contains("empty")) {
                Label emptyLabel = new Label("🛒 Your cart is empty");
                emptyLabel.setFont(new Font(18));
                emptyLabel.setStyle("-fx-text-fill: #7f8c8d;");
                cartItemsContainer.getChildren().add(emptyLabel);
                totalLabel.setText("Total: $0.00");
            } else {
                // Parse cart items
                // Format: "iPhone 14 x1 @ $999.99 = $999.99"
                String[] lines = message.split("\n");
                for (String line : lines) {
                    line = line.trim();
                    // Look for lines with product info (contain 'x' and '@')
                    if (line.contains(" x") && line.contains(" @ ")) {
                        HBox itemBox = createCartItemBox(line);
                        cartItemsContainer.getChildren().add(itemBox);
                    }
                }
                
                // Get total
                response totalRes = connectionManager.getCartTotal();
                if (totalRes.isSuccess()) {
                    // Format: "💰 Cart Total: $999.99"
                    String totalMsg = totalRes.getMessage();
                    totalLabel.setText(totalMsg.replace("💰 Cart Total:", "Total:"));
                } else {
                    totalLabel.setText("Total: $0.00");
                }
            }
        } else {
            showError(res.getMessage());
        }
    }
    
    private HBox createCartItemBox(String itemInfo) {
        HBox box = new HBox(15);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        
        Label itemLabel = new Label(itemInfo);
        itemLabel.setFont(new Font(14));
        HBox.setHgrow(itemLabel, Priority.ALWAYS);
        
        Button removeButton = new Button("Remove");
        removeButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");
        removeButton.setOnAction(e -> {
            // Extract product ID from itemInfo like "[ID: 3] Sony Headset x4 @ $199.99 = $799.96"
            try {
                if (itemInfo.contains("[ID:") && itemInfo.contains("]")) {
                    int idStart = itemInfo.indexOf("[ID:") + 4;
                    int idEnd = itemInfo.indexOf("]", idStart);
                    String productIdStr = itemInfo.substring(idStart, idEnd).trim();
                    int productId = Integer.parseInt(productIdStr);
                    
                    response res = connectionManager.removeFromCart(productId);
                    
                    if (res.isSuccess()) {
                        showInfo("Item removed from cart");
                        loadCart();
                    } else {
                        showError(res.getMessage());
                    }
                } else {
                    showError("Could not extract product ID from cart item");
                }
            } catch (Exception ex) {
                showError("Failed to remove item: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
        
        box.getChildren().addAll(itemLabel, removeButton);
        return box;
    }
    
    @FXML
    private void handleClearCart() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Clear Cart");
        confirm.setHeaderText("Are you sure?");
        confirm.setContentText("This will remove all items from your cart.");
        
        if (confirm.showAndWait().get() == ButtonType.OK) {
            // Get all items and remove them one by one
            response res = connectionManager.getCartItems();
            if (res.isSuccess()) {
                String message = res.getMessage();
                
                if (message.contains("empty")) {
                    showInfo("Cart is already empty");
                    return;
                }
                
                String[] lines = message.split("\n");
                boolean anyRemoved = false;
                
                for (String line : lines) {
                    line = line.trim();
                    // Extract product ID from lines like "[ID: 3] Sony Headset x4 @ $199.99 = $799.96"
                    if (line.contains("[ID:") && line.contains("]")) {
                        try {
                            int idStart = line.indexOf("[ID:") + 4;
                            int idEnd = line.indexOf("]", idStart);
                            String productIdStr = line.substring(idStart, idEnd).trim();
                            int productId = Integer.parseInt(productIdStr);
                            connectionManager.removeFromCart(productId);
                            anyRemoved = true;
                        } catch (Exception e) {
                            System.err.println("Error parsing cart item: " + line);
                        }
                    }
                }
                
                if (anyRemoved) {
                    showInfo("Cart cleared successfully");
                    loadCart();
                } else {
                    showError("Failed to clear cart");
                }
            }
        }
    }
    
    @FXML
    private void handleCheckout() {
        System.out.println("🛒 Checkout button clicked");
        
        response res = connectionManager.createOrder();
        
        System.out.println("📦 Create order response: Success=" + res.isSuccess() + ", Message=" + res.getMessage());
        
        if (res.isSuccess()) {
            String message = res.getMessage();
            
            // Extract order ID and total from message
            // Expected format: "✅ Order created successfully!|orderId|total"
            String[] parts = message.split("\\|");
            
            System.out.println("📋 Message parts: " + parts.length);
            for (int i = 0; i < parts.length; i++) {
                System.out.println("   Part " + i + ": " + parts[i]);
            }
            
            if (parts.length >= 3) {
                String orderId = parts[1].trim();
                String total = parts[2].trim();
                
                System.out.println("💳 Opening payment dialog for order: " + orderId + ", total: " + total);
                
                // Show payment dialog
                try {
                    java.net.URL paymentUrl = getClass().getResource("/fxml/payment.fxml");
                    if (paymentUrl == null) {
                        System.err.println("❌ Could not find /fxml/payment.fxml");
                        showError("Payment dialog not found. Please contact support.");
                        return;
                    }
                    
                    System.out.println("✅ Payment FXML found: " + paymentUrl);
                    
                    javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(paymentUrl);
                    javafx.scene.Parent root = loader.load();
                    
                    PaymentController paymentController = loader.getController();
                    paymentController.setOrderInfo(orderId, total);
                    
                    javafx.stage.Stage stage = new javafx.stage.Stage();
                    stage.setTitle("Process Payment");
                    stage.setScene(new javafx.scene.Scene(root));
                    stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
                    stage.showAndWait();
                    
                    loadCart(); // Refresh cart after payment
                } catch (Exception e) {
                    System.err.println("❌ Error opening payment dialog:");
                    e.printStackTrace();
                    showError("Failed to open payment dialog: " + e.getMessage());
                }
            } else {
                // Fallback if format is different
                System.out.println("⚠️ Unexpected message format, showing simple dialog");
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Order Created");
                alert.setHeaderText("Success!");
                alert.setContentText(message);
                alert.showAndWait();
                
                loadCart();
            }
        } else {
            System.err.println("❌ Create order failed: " + res.getMessage());
            showError(res.getMessage());
        }
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
