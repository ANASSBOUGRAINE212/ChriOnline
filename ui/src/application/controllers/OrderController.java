package application.controllers;

import application.utils.ConnectionManager;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import protocol.response;

public class OrderController {
    
    @FXML private VBox ordersContainer;
    
    private ConnectionManager connectionManager;
    
    @FXML
    public void initialize() {
        connectionManager = ConnectionManager.getInstance();
        loadOrders();
    }
    
    @FXML
    public void loadOrders() {
        ordersContainer.getChildren().clear();
        
        response res = connectionManager.listOrders();
        
        if (res.isSuccess()) {
            String message = res.getMessage();
            
            if (message.contains("No orders")) {
                Label emptyLabel = new Label("📦 No orders yet");
                emptyLabel.setFont(new Font(18));
                emptyLabel.setStyle("-fx-text-fill: #7f8c8d;");
                ordersContainer.getChildren().add(emptyLabel);
            } else {
                // Parse orders
                // Format: "Order: 123e4567-e89b-1 | Status: PENDING | Total: $999.99 | Date: 2024-01-01"
                String[] lines = message.split("\n");
                for (String line : lines) {
                    line = line.trim();
                    if (line.startsWith("Order:")) {
                        VBox orderBox = createOrderBox(line);
                        ordersContainer.getChildren().add(orderBox);
                    }
                }
            }
        } else {
            showError(res.getMessage());
        }
    }
    
    private VBox createOrderBox(String orderInfo) {
        VBox box = new VBox(10);
        box.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        
        // Parse order info: "Order: 123e4567-e89b-1 | Status: PENDING | Total: $999.99 | Date: 2024-01-01"
        String orderId = extractOrderId(orderInfo);
        String status = extractStatus(orderInfo);
        String total = extractTotal(orderInfo);
        String date = extractDate(orderInfo);
        
        // Order header with status badge
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label orderIdLabel = new Label("Order #" + (orderId != null ? orderId.substring(0, Math.min(8, orderId.length())) : "Unknown"));
        orderIdLabel.setFont(new Font("System Bold", 16));
        
        Label statusBadge = new Label(status != null ? status : "UNKNOWN");
        statusBadge.setFont(new Font("System Bold", 12));
        statusBadge.setPadding(new Insets(5, 10, 5, 10));
        statusBadge.setStyle(getStatusStyle(status));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label totalLabel = new Label(total != null ? total : "$0.00");
        totalLabel.setFont(new Font("System Bold", 18));
        totalLabel.setStyle("-fx-text-fill: #27ae60;");
        
        header.getChildren().addAll(orderIdLabel, statusBadge, spacer, totalLabel);
        
        // Order date
        Label dateLabel = new Label("📅 " + (date != null ? date : "Unknown date"));
        dateLabel.setStyle("-fx-text-fill: #7f8c8d;");
        
        // Action buttons
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        
        Button viewButton = new Button("View Details");
        viewButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
        viewButton.setOnAction(e -> {
            if (orderId != null) {
                System.out.println("📋 Viewing order details for: " + orderId);
                response res = connectionManager.getOrder(orderId);
                System.out.println("📦 Get order response: Success=" + res.isSuccess() + ", Message=" + res.getMessage());
                
                if (res.isSuccess()) {
                    showOrderDetails(res.getMessage());
                } else {
                    showError(res.getMessage());
                }
            } else {
                System.err.println("❌ Order ID is null!");
                showError("Order ID not found");
            }
        });
        
        Button cancelButton = new Button("Cancel Order");
        cancelButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");
        
        // Only show cancel button if order is PENDING
        if (status != null && status.equals("PENDING")) {
            cancelButton.setOnAction(e -> {
                if (orderId != null) {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Cancel Order");
                    confirm.setHeaderText("Are you sure?");
                    confirm.setContentText("Do you want to cancel this order?");
                    
                    if (confirm.showAndWait().get() == ButtonType.OK) {
                        response res = connectionManager.cancelOrder(orderId);
                        if (res.isSuccess()) {
                            showInfo("Order cancelled successfully");
                            loadOrders();
                        } else {
                            showError(res.getMessage());
                        }
                    }
                }
            });
            actions.getChildren().addAll(viewButton, cancelButton);
        } else {
            actions.getChildren().add(viewButton);
        }
        
        box.getChildren().addAll(header, dateLabel, actions);
        return box;
    }
    
    private String getStatusStyle(String status) {
        if (status == null) return "-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-background-radius: 5;";
        
        switch (status) {
            case "PENDING":
                return "-fx-background-color: #f39c12; -fx-text-fill: white; -fx-background-radius: 5;";
            case "CONFIRMED":
                return "-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 5;";
            case "SHIPPED":
                return "-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-background-radius: 5;";
            case "DELIVERED":
                return "-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 5;";
            case "CANCELLED":
                return "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 5;";
            default:
                return "-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-background-radius: 5;";
        }
    }
    
    private String extractStatus(String orderInfo) {
        try {
            int start = orderInfo.indexOf("Status:") + 7;
            int end = orderInfo.indexOf("|", start);
            if (start > 6 && end > start) {
                return orderInfo.substring(start, end).trim();
            }
        } catch (Exception e) {
            System.err.println("Error extracting status: " + e.getMessage());
        }
        return null;
    }
    
    private String extractTotal(String orderInfo) {
        try {
            int start = orderInfo.indexOf("Total:") + 6;
            int end = orderInfo.indexOf("|", start);
            if (start > 5 && end > start) {
                return orderInfo.substring(start, end).trim();
            }
        } catch (Exception e) {
            System.err.println("Error extracting total: " + e.getMessage());
        }
        return null;
    }
    
    private String extractDate(String orderInfo) {
        try {
            int start = orderInfo.indexOf("Date:") + 5;
            if (start > 4) {
                return orderInfo.substring(start).trim();
            }
        } catch (Exception e) {
            System.err.println("Error extracting date: " + e.getMessage());
        }
        return null;
    }
    
    private void showOrderDetails(String details) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Order Details");
        alert.setHeaderText("Complete Order Information");
        
        // Create a text area for better display
        TextArea textArea = new TextArea(details);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(15);
        
        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }
    
    private String extractOrderId(String orderInfo) {
        try {
            // Extract from "Order: 123e4567-e89b-1 | Status: PENDING | ..."
            // The order ID is between "Order: " and " |"
            int start = orderInfo.indexOf("Order:") + 6;
            int end = orderInfo.indexOf("|", start);
            if (start > 5 && end > start) {
                return orderInfo.substring(start, end).trim();
            }
        } catch (Exception e) {
            System.err.println("Error extracting order ID: " + e.getMessage());
        }
        return null;
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
