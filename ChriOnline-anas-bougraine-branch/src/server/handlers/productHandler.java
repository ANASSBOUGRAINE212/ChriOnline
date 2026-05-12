package server.handlers;

import database.dao.productDAO;
import database.dao.userDao;
import java.util.List;
import model.product;
import model.user;
import protocol.request;
import protocol.response;
import server.sessionManager;

public class productHandler {
    private final productDAO productDAO = new productDAO();
    private final userDao userDAO       = new userDao();
    private final sessionManager sessionMgr = sessionManager.getInstance();

    public static response handle(request request) {
        productHandler handler = new productHandler();
        return handler.handleRequest(request);
    }

    private response handleRequest(request request) {
        String type = request.getType();
        if (type.equals("ADD_PRODUCT"))     return handleAddProduct(request);
        if (type.equals("DELETE_PRODUCT"))  return handleDeleteProduct(request);
        if (type.equals("GET_PRODUCT"))     return handleGetProduct(request);
        if (type.equals("UPDATE_PRODUCT"))  return handleUpdateProduct(request);
        if (type.equals("LIST_PRODUCTS"))   return handleListProducts(request);
        return new response(false, "Unknown product command: " + type);
    }

    // ✅ ADMIN only
    private response handleAddProduct(request request) {
        if (!isAdmin(request)) return new response(false, "Access denied: ADMIN role required");

        String name        = request.getParam("name");
        String description = request.getParam("description");
        String priceStr    = request.getParam("price");
        String stockStr    = request.getParam("stock");
        String category    = request.getParam("category");
        String userId      = sessionMgr.getUserIdFromToken(request.getToken());

        if (name == null || priceStr == null || name.trim().isEmpty())
            return new response(false, "Name and price are required");

        try {
            double price = Double.parseDouble(priceStr);
            int stock    = stockStr != null ? Integer.parseInt(stockStr) : 0;

            product p = new product();
            p.setName(name.trim());
            p.setDescription(description != null ? description.trim() : "");
            p.setPrice(price);
            p.setStock(stock);
            p.setCategory(category != null ? category.trim() : "");
            p.setCreatedBy(userId);

            if (productDAO.addProduct(p))
                return new response(true, "Product added successfully");
            else
                return new response(false, "Failed to add product");
        } catch (NumberFormatException e) {
            return new response(false, "Invalid price or stock value");
        } catch (Exception e) {
            System.err.println("Add product error: " + e.getMessage());
            return new response(false, "Server error while adding product");
        }
    }

    // ✅ ADMIN only
    private response handleDeleteProduct(request request) {
        if (!isAdmin(request)) return new response(false, "Access denied: ADMIN role required");

        String productIdStr = request.getParam("productId");
        if (productIdStr == null || productIdStr.trim().isEmpty())
            return new response(false, "Product ID is required");

        try {
            int productId = Integer.parseInt(productIdStr);
            if (productDAO.deleteProduct(productId))
                return new response(true, "Product deleted successfully");
            else
                return new response(false, "Product not found");
        } catch (NumberFormatException e) {
            return new response(false, "Invalid product ID");
        } catch (Exception e) {
            System.err.println("Delete product error: " + e.getMessage());
            return new response(false, "Server error while deleting product");
        }
    }

    // ✅ Everyone
    private response handleGetProduct(request request) {
        String userId = sessionMgr.getUserIdFromToken(request.getToken());
        if (userId == null) return new response(false, "Not authenticated");

        String productIdStr = request.getParam("productId");
        if (productIdStr == null || productIdStr.trim().isEmpty())
            return new response(false, "Product ID is required");

        try {
            int productId  = Integer.parseInt(productIdStr);
            product p      = productDAO.getProductById(productId);
            if (p == null) return new response(false, "Product not found");
            return new response(true, p.getProductInfo());
        } catch (NumberFormatException e) {
            return new response(false, "Invalid product ID");
        } catch (Exception e) {
            System.err.println("Get product error: " + e.getMessage());
            return new response(false, "Server error while retrieving product");
        }
    }

    // ✅ ADMIN only — from person 3
    private response handleUpdateProduct(request request) {
        if (!isAdmin(request)) return new response(false, "Access denied: ADMIN role required");

        String productIdStr = request.getParam("productId");
        String name         = request.getParam("productName");
        String description  = request.getParam("description");
        String priceStr     = request.getParam("price");
        String stockStr     = request.getParam("stock");
        String category     = request.getParam("category");

        if (productIdStr == null || productIdStr.trim().isEmpty())
            return new response(false, "Product ID is required");
        if (name == null || name.trim().isEmpty())
            return new response(false, "Product name is required");

        try {
            int productId = Integer.parseInt(productIdStr);

            Double price = null;
            if (priceStr != null && !priceStr.trim().isEmpty()) {
                price = Double.parseDouble(priceStr);
                if (price < 0) return new response(false, "Price cannot be negative");
            }

            Integer stock = null;
            if (stockStr != null && !stockStr.trim().isEmpty()) {
                stock = Integer.parseInt(stockStr);
                if (stock < 0) return new response(false, "Stock cannot be negative");
            }

            boolean success = productDAO.updateProduct(productId, name, description, price, stock, category);

            if (success) {
                String userId   = sessionMgr.getUserIdFromToken(request.getToken());
                user userObj    = userDAO.getUserById(userId);
                String updatedBy = userObj != null ? userObj.getUsername() : "ADMIN";

                return new response(true, String.format(
                    "✅ Product updated successfully!\n" +
                    "📦 Name: %s\n" +
                    "📝 Description: %s\n" +
                    "💰 Price: %s\n" +
                    "🏷️  Stock: %s\n" +
                    "🗂️  Category: %s\n" +
                    "👤 Updated by: %s",
                    name,
                    description  != null ? description  : "Not specified",
                    price        != null ? String.format("%.2f", price) : "Not specified",
                    stock        != null ? stock.toString() : "Not specified",
                    category     != null ? category     : "Not specified",
                    updatedBy
                ));
            } else {
                return new response(false, "Failed to update product. Product may not exist.");
            }
        } catch (NumberFormatException e) {
            return new response(false, "Invalid product ID, price or stock format");
        } catch (Exception e) {
            System.err.println("Update product error: " + e.getMessage());
            return new response(false, "Server error while updating product");
        }
    }

    // ✅ Everyone — from person 3
    private response handleListProducts(request request) {
        try {
            List<product> products = productDAO.getAllProducts();
            if (products.isEmpty())
                return new response(true, "No products available.");

            StringBuilder sb = new StringBuilder();
            sb.append("📦 Available Products:\n");
            sb.append("─────────────────────────────────────────────────\n");
            for (product p : products) {
                sb.append(String.format(
                    "🆔 %-4s | %-20s | 💰 $%8.2f | 🏷️  %-15s | Stock: %d%n",
                    p.getId(), p.getName(), p.getPrice(),
                    p.getCategory() != null ? p.getCategory() : "N/A",
                    p.getStock()
                ));
            }
            sb.append("─────────────────────────────────────────────────");
            return new response(true, sb.toString());
        } catch (Exception e) {
            System.err.println("List products error: " + e.getMessage());
            return new response(false, "Server error while listing products");
        }
    }

    private boolean isAdmin(request request) {
        String token  = request.getToken();
        String userId = sessionMgr.getUserIdFromToken(token);
        if (userId == null) return false;
        user u = userDAO.getUserById(userId);
        return u != null && u.getRole() == user.Role.ADMIN;
    }
}