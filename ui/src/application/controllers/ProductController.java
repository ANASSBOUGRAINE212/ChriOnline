package application.controllers;

import application.utils.ConnectionManager;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

public class ProductController {
    
    @FXML private GridPane productsGrid;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private Label statusLabel;
    
    private ConnectionManager connectionManager;
    
    @FXML
    public void initialize() {
        connectionManager = ConnectionManager.getInstance();
        
        // Setup category filter
        categoryFilter.getItems().addAll("All", "Smartphones", "Laptops", "Audio", "Accessories");
        categoryFilter.setValue("All");
        
        loadProducts();
    }
    
    @FXML
    public void loadProducts() {
        productsGrid.getChildren().clear();
        statusLabel.setText("Loading products...");
        
        protocol.response res = connectionManager.listProducts();
        
        if (res.isSuccess()) {
            String message = res.getMessage();
            System.out.println("Products response: " + message); // Debug
            
            // The response format from server is:
            // 🆔 1    | iPhone 14            | 💰 $  999.99 | 🏷️  Smartphones     | Stock: 10
            String[] lines = message.split("\\n");
            
            int col = 0;
            int row = 0;
            int count = 0;
            
            for (String line : lines) {
                line = line.trim();
                System.out.println("Processing line: " + line); // Debug
                
                // Look for product lines that start with 🆔
                if (line.startsWith("🆔")) {
                    try {
                        // Split by | to get parts
                        String[] parts = line.split("\\|");
                        if (parts.length >= 5) {
                            // Extract ID (remove 🆔 emoji)
                            String id = parts[0].replace("🆔", "").trim();
                            
                            // Extract name
                            String name = parts[1].trim();
                            
                            // Extract price (remove 💰 $ and spaces)
                            String priceStr = parts[2].replace("💰", "").replace("$", "").trim();
                            
                            // Replace comma with period for European format
                            String price = priceStr.replace(",", ".");
                            
                            // Extract stock (from "Stock: 10")
                            String stockPart = parts[4].trim();
                            String stock = stockPart.replace("Stock:", "").trim();
                            
                            System.out.println("Parsed: ID=" + id + ", Name=" + name + ", Price (original)=" + priceStr + ", Price (cleaned)=" + price + ", Stock=" + stock);
                            
                            VBox productCard = createProductCard(id, name, price, stock);
                            productsGrid.add(productCard, col, row);
                            
                            col++;
                            if (col == 3) {
                                col = 0;
                                row++;
                            }
                            count++;
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing product line: " + line);
                        e.printStackTrace();
                    }
                }
            }
            
            statusLabel.setText("Showing " + count + " products");
            
            if (count == 0) {
                Label noProducts = new Label("No products available");
                noProducts.setFont(new Font(16));
                productsGrid.add(noProducts, 0, 0);
            }
        } else {
            statusLabel.setText("Failed to load products");
            showError("Failed to load products: " + res.getMessage());
        }
    }
    
    private VBox createProductCard(String id, String name, String price, String stock) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        card.setPadding(new Insets(15));
        card.setPrefWidth(200);
        card.setPrefHeight(220);
        
        // Product icon (placeholder)
        Label icon = new Label("📦");
        icon.setFont(new Font(48));
        
        // Product name
        Label nameLabel = new Label(name);
        nameLabel.setFont(new Font("System Bold", 16));
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(180);
        
        // Price - clean it for display
        String cleanPrice = price.trim();
        Label priceLabel = new Label("$" + cleanPrice);
        priceLabel.setFont(new Font(18));
        priceLabel.setStyle("-fx-text-fill: #27ae60;");
        
        // Stock
        Label stockLabel = new Label("Stock: " + stock);
        stockLabel.setStyle("-fx-text-fill: #7f8c8d;");
        
        // Add to cart button
        Button addButton = new Button("Add to Cart");
        addButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
        addButton.setMaxWidth(Double.MAX_VALUE);
        addButton.setOnAction(e -> handleAddToCart(id, name, cleanPrice));
        
        card.getChildren().addAll(icon, nameLabel, priceLabel, stockLabel, addButton);
        
        return card;
    }
    
    private void handleAddToCart(String productId, String name, String price) {
        try {
            int id = Integer.parseInt(productId);
            
            // Clean the price string:
            // 1. Replace comma with period (European format: 2499,99 -> 2499.99)
            // 2. Remove any whitespace
            // 3. Remove any other non-numeric characters except decimal point
            String cleanPrice = price.trim()
                                     .replace(",", ".")  // Handle European format
                                     .replaceAll("[^0-9.]", "");
            double priceValue = Double.parseDouble(cleanPrice);
            
            System.out.println("Adding to cart: ID=" + id + ", Original Price=" + price + ", Cleaned Price=" + cleanPrice + ", Parsed=" + priceValue);
            
            protocol.response res = connectionManager.addToCart(id, 1, priceValue);
            
            if (res.isSuccess()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText(name + " added to cart!");
                alert.showAndWait();
            } else {
                showError("Failed to add to cart: " + res.getMessage());
            }
        } catch (NumberFormatException e) {
            showError("Error parsing price: " + price + " - " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim().toLowerCase();
        
        if (query.isEmpty()) {
            loadProducts();
            return;
        }
        
        protocol.response res = connectionManager.listProducts();
        
        if (res.isSuccess()) {
            productsGrid.getChildren().clear();
            String message = res.getMessage();
            String[] lines = message.split("\\n");
            
            int col = 0;
            int row = 0;
            int count = 0;
            
            for (String line : lines) {
                line = line.trim();
                
                // Use same parsing logic as loadProducts()
                if (line.startsWith("🆔")) {
                    try {
                        String[] parts = line.split("\\|");
                        if (parts.length >= 5) {
                            String id = parts[0].replace("🆔", "").trim();
                            String name = parts[1].trim();
                            String priceStr = parts[2].replace("💰", "").replace("$", "").trim();
                            String price = priceStr.replace(",", ".");  // Handle European format
                            String stockPart = parts[4].trim();
                            String stock = stockPart.replace("Stock:", "").trim();
                            
                            // Filter by search query
                            if (name.toLowerCase().contains(query)) {
                                VBox productCard = createProductCard(id, name, price, stock);
                                productsGrid.add(productCard, col, row);
                                
                                col++;
                                if (col == 3) {
                                    col = 0;
                                    row++;
                                }
                                count++;
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing product line: " + line);
                        e.printStackTrace();
                    }
                }
            }
            
            statusLabel.setText("Found " + count + " products matching '" + query + "'");
            
            if (count == 0) {
                Label noResults = new Label("No products found matching '" + query + "'");
                noResults.setFont(new Font(16));
                productsGrid.add(noResults, 0, 0);
            }
        }
    }
    
    @FXML
    private void handleFilter() {
        String category = categoryFilter.getValue();
        
        if (category == null || category.equals("All")) {
            loadProducts();
            return;
        }
        
        statusLabel.setText("Filtering by: " + category + " (filter by category not yet implemented on server)");
        // TODO: Implement category filter when server supports it
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
