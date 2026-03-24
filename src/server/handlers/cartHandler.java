package server.handlers;

import database.dao.cartDAO;
import database.dao.productDAO;
import java.util.List;
import model.cartItem;
import model.product;
import protocol.request;
import protocol.response;
import server.sessionManager;

public class cartHandler {
    private final cartDAO cartDAO = new cartDAO();
    private final productDAO productDAO = new productDAO();
    private final sessionManager sessionMgr = sessionManager.getInstance();

    public static response handle(request request) {
        cartHandler handler = new cartHandler();
        return handler.handleRequest(request);
    }

    private response handleRequest(request request) {
        String type = request.getType();
        System.out.println("🛒 Cart handler processing: " + type);

        if (type.equals(request.ADD_TO_CART))      return handleAddToCart(request);
        if (type.equals(request.GET_CART_ITEMS))   return handleGetCartItems(request);
        if (type.equals(request.REMOVE_FROM_CART)) return handleRemoveFromCart(request);
        if (type.equals(request.GET_CART_TOTAL))   return handleGetCartTotal(request);

        return new response(false, "Unknown cart command: " + type);
    }

    private response handleAddToCart(request request) {
        String userId = sessionMgr.getUserIdFromToken(request.getToken());
        if (userId == null) {
            System.out.println("❌ Authentication failed - no valid session");
            return new response(false, "Not authenticated");
        }

        String productIdStr = request.getParam("productId");
        String quantityStr = request.getParam("quantity");
        String priceStr = request.getParam("price");

        if (productIdStr == null || productIdStr.trim().isEmpty()) {
            return new response(false, "Product ID is required");
        }

        try {
            int productId = Integer.parseInt(productIdStr);
            int quantity = quantityStr != null ? Integer.parseInt(quantityStr) : 1;
            double price = priceStr != null ? Double.parseDouble(priceStr) : 0.0;

            if (quantity <= 0) {
                return new response(false, "Quantity must be greater than 0");
            }

            // Verify product exists
            product p = productDAO.getProductById(productId);
            if (p == null) {
                return new response(false, "Product not found");
            }

            // Use product price if not provided
            if (price == 0.0) {
                price = p.getPrice();
            }

            System.out.println("🛒 Adding product " + productId + " (qty: " + quantity + ") to cart");

            String cartId = "cart-" + userId;

            if (cartDAO.addItem(cartId, productId, quantity, price)) {
                return new response(true, "✅ Product added to cart successfully");
            } else {
                return new response(false, "Failed to add product to cart");
            }
        } catch (NumberFormatException e) {
            return new response(false, "Invalid product ID, quantity, or price format");
        } catch (Exception e) {
            System.err.println("❌ Add to cart error: " + e.getMessage());
            return new response(false, "Server error while adding to cart");
        }
    }

    private response handleGetCartItems(request request) {
        String userId = sessionMgr.getUserIdFromToken(request.getToken());
        if (userId == null) {
            System.out.println("❌ Authentication failed - no valid session");
            return new response(false, "Not authenticated");
        }

        System.out.println("👤 User ID from session: " + userId);
        String cartId = "cart-" + userId;

        List<cartItem> items = cartDAO.getItems(cartId);
        if (items.isEmpty()) {
            return new response(true, "🛒 Your cart is empty");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("🛒 Your Cart:\n");
        sb.append("─────────────────────────────────────────────────\n");
        for (cartItem item : items) {
            sb.append("  ").append(item.toString()).append("\n");
        }
        sb.append("─────────────────────────────────────────────────");

        return new response(true, sb.toString());
    }

    private response handleRemoveFromCart(request request) {
        String userId = sessionMgr.getUserIdFromToken(request.getToken());
        if (userId == null) {
            System.out.println("❌ Authentication failed - no valid session");
            return new response(false, "Not authenticated");
        }

        String productIdStr = request.getParam("productId");
        if (productIdStr == null || productIdStr.trim().isEmpty()) {
            return new response(false, "Product ID is required");
        }

        try {
            int productId = Integer.parseInt(productIdStr);
            System.out.println("🗑️  Removing product " + productId + " from cart");

            String cartId = "cart-" + userId;

            if (cartDAO.removeItem(cartId, productId)) {
                return new response(true, "✅ Item removed from cart");
            } else {
                return new response(false, "Item not found in cart");
            }
        } catch (NumberFormatException e) {
            return new response(false, "Invalid product ID");
        } catch (Exception e) {
            System.err.println("❌ Remove from cart error: " + e.getMessage());
            return new response(false, "Server error while removing item");
        }
    }

    private response handleGetCartTotal(request request) {
        String userId = sessionMgr.getUserIdFromToken(request.getToken());
        if (userId == null) {
            System.out.println("❌ Authentication failed - no valid session");
            return new response(false, "Not authenticated");
        }

        System.out.println("💰 Calculating cart total for user: " + userId);
        String cartId = "cart-" + userId;

        double total = cartDAO.getSubtotal(cartId);
        return new response(true, String.format("💰 Cart Total: $%.2f", total));
    }
}
