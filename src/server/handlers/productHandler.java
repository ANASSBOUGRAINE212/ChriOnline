package server.handlers;

import database.dao.productDAO;
import database.dao.userDao;
import model.user;
import protocol.request;
import protocol.response;
import server.sessionManager;

public class productHandler {
    private final productDAO productDAO = new productDAO();
    private final userDao userDAO = new userDao();
    private final sessionManager sessionMgr = sessionManager.getInstance();

    public static response handle(request request) {
        productHandler handler = new productHandler();
        return handler.handleRequest(request);
    }
    
    private response handleRequest(request request) {
        String type = request.getType();
        
        if (type.equals(request.UPDATE_PRODUCT)) {
            return handleUpdateProduct(request);
        } else if (type.equals(request.LIST_PRODUCTS)) {
            return handleListProducts(request);
        } else {
            return new response(false, "Unknown product command: " + type);
        }
    }
    
    private response handleUpdateProduct(request request) {
        String token = request.getToken();
        String userId = sessionMgr.getUserIdFromToken(token);
        if (userId == null) {
            return new response(false, "Not authenticated");
        }

        try {
            // Check if user is ADMIN
            user userObj = userDAO.getUserById(userId);
            if (userObj == null) {
                return new response(false, "User not found");
            }
            
            if (userObj.getRole() != user.Role.ADMIN) {
                return new response(false, "Access denied. Admin privileges required to update products.");
            }
            
            // Get product parameters
            String productId = request.getParam("productId");
            String productName = request.getParam("productName");
            String description = request.getParam("description");
            String priceStr = request.getParam("price");
            String stockStr = request.getParam("stock");
            String category = request.getParam("category");
            
            // Validate required fields
            if (productId == null || productId.trim().isEmpty()) {
                return new response(false, "Product ID is required");
            }
            
            if (productName == null || productName.trim().isEmpty()) {
                return new response(false, "Product name is required");
            }
            
            // Parse and validate price
            Double price = null;
            if (priceStr != null && !priceStr.trim().isEmpty()) {
                try {
                    price = Double.parseDouble(priceStr);
                    if (price < 0) {
                        return new response(false, "Price cannot be negative");
                    }
                } catch (NumberFormatException e) {
                    return new response(false, "Invalid price format");
                }
            }
            
            // Parse and validate stock
            Integer stock = null;
            if (stockStr != null && !stockStr.trim().isEmpty()) {
                try {
                    stock = Integer.parseInt(stockStr);
                    if (stock < 0) {
                        return new response(false, "Stock cannot be negative");
                    }
                } catch (NumberFormatException e) {
                    return new response(false, "Invalid stock format");
                }
            }
            
            // Update product in database
            boolean success = productDAO.updateProduct(productId, productName, description, price, stock, category);
            
            if (success) {
                String updateInfo = String.format(
                    "Product updated successfully!\n" +
                    "Product ID: %s\n" +
                    "Name: %s\n" +
                    "Description: %s\n" +
                    "Price: %s\n" +
                    "Stock: %s\n" +
                    "Category: %s\n" +
                    "Updated by: %s (ADMIN)",
                    productId,
                    productName,
                    description != null ? description : "Not specified",
                    price != null ? String.format("%.2f", price) : "Not specified",
                    stock != null ? stock.toString() : "Not specified",
                    category != null ? category : "Not specified",
                    userObj.getUsername()
                );
                
                return new response(true, updateInfo);
            } else {
                return new response(false, "Failed to update product. Product may not exist.");
            }
            
        } catch (Exception e) {
            System.err.println("Update product error: " + e.getMessage());
            return new response(false, "Error updating product");
        }
    }
    
    private response handleListProducts(request request) {
        // Mock product data for now
        String productList = 
            "📦 Available Products:\n" +
            "─────────────────────────────────────\n" +
            "🆔 P001 | 💻 Laptop Dell XPS 13 | 💰 $999.99\n" +
            "🆔 P002 | 📱 iPhone 15 Pro | 💰 $1,199.00\n" +
            "🆔 P003 | 🎧 Sony WH-1000XM5 | 💰 $349.99\n" +
            "🆔 P004 | ⌨️  Mechanical Keyboard | 💰 $129.99\n" +
            "🆔 P005 | 🖱️  Wireless Mouse | 💰 $49.99\n" +
            "─────────────────────────────────────";
        
        return new response(true, productList);
    }
}
