package application.controllers;

import application.utils.ConnectionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import protocol.response;

public class PaymentController {
    
    @FXML private Label orderInfoLabel;
    @FXML private Label totalLabel;
    @FXML private ComboBox<String> paymentMethodCombo;
    
    private ConnectionManager connectionManager;
    private String orderId;
    private String orderTotal;
    
    @FXML
    public void initialize() {
        connectionManager = ConnectionManager.getInstance();
        
        // Populate payment methods
        paymentMethodCombo.getItems().addAll(
            "CREDIT_CARD",
            "DEBIT_CARD", 
            "PAYPAL",
            "BANK_TRANSFER",
            "CASH"
        );
        paymentMethodCombo.setValue("CREDIT_CARD");
    }
    
    public void setOrderInfo(String orderId, String total) {
        this.orderId = orderId;
        this.orderTotal = total;
        orderInfoLabel.setText("Order ID: " + orderId);
        totalLabel.setText("Total: " + total);
    }
    
    @FXML
    private void handlePayment() {
        String method = paymentMethodCombo.getValue();
        
        if (method == null || method.isEmpty()) {
            showError("Please select a payment method");
            return;
        }
        
        response res = connectionManager.processPayment(orderId, method);
        
        if (res.isSuccess()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Payment Successful");
            alert.setHeaderText("Payment Processed!");
            alert.setContentText(res.getMessage());
            alert.showAndWait();
            
            // Close the payment window
            paymentMethodCombo.getScene().getWindow().hide();
        } else {
            showError(res.getMessage());
        }
    }
    
    @FXML
    private void handleCancel() {
        paymentMethodCombo.getScene().getWindow().hide();
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
