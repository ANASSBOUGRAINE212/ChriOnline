package server.handlers;

import database.dao.cartDAO;
import database.dao.orderDAO;
import java.util.List;
import java.util.UUID;
import model.*;
import protocol.request;
import protocol.response;
import server.sessionManager;

public class orderHandler {
    private final orderDAO orderDAO = new orderDAO();
    private final cartDAO cartDAO = new cartDAO();
    private final sessionManager sessionMgr = sessionManager.getInstance();

    public static response handle(request request) {
        orderHandler handler = new orderHandler();
        return handler.handleRequest(request);
    }

    private response handleRequest(request request) {
        String type = request.getType();
        System.out.println("📦 Order handler processing: " + type);

        if (type.equals(request.CREATE_ORDER))      return handleCreateOrder(request);
        if (type.equals(request.GET_ORDER))         return handleGetOrder(request);
        if (type.equals(request.LIST_ORDERS))       return handleListOrders(request);
        if (type.equals(request.CANCEL_ORDER))      return handleCancelOrder(request);
        if (type.equals(request.UPDATE_ORDER_STATUS)) return handleUpdateOrderStatus(request);

        return new response(false, "Unknown order command: " + type);
    }

    private response handleCreateOrder(request request) {
        String userId = sessionMgr.getUserIdFromToken(request.getToken());
        if (userId == null) {
            return new response(false, "Not authenticated");
        }

        try {
            // Get cart items
            cart userCart = cartDAO.getOrCreateCart(userId);
            if (userCart == null) {
                return new response(false, "Cart not found");
            }

            String cartId = userCart.getCartId();
            List<cartItem> cartItems = cartDAO.getItems(cartId);

            if (cartItems.isEmpty()) {
                return new response(false, "Cart is empty. Add items before creating order.");
            }

            // Calculate total
            double total = cartDAO.getSubtotal(cartId);

            // Create order
            order newOrder = new order();
            newOrder.generateId();
            newOrder.setUserId(userId);
            newOrder.setTotalAmount(total);
            newOrder.setStatus(OrderStatus.PENDING);
            newOrder.setCreatedAt(new java.util.Date());

            // Convert cart items to order items
            List<orderItem> orderItems = new java.util.ArrayList<>();
            for (cartItem ci : cartItems) {
                orderItem oi = new orderItem();
                oi.setItemId("OI-" + UUID.randomUUID().toString());
                oi.setProduct(ci.getProduct());
                oi.setQuantity(ci.getQuantity());
                oi.setUnitPrice(ci.getUnitPrice());
                orderItems.add(oi);
            }
            newOrder.setItems(orderItems);

            // Validate order
            if (!newOrder.validate()) {
                return new response(false, "Order validation failed");
            }

            // Save order to database
            if (orderDAO.createOrder(newOrder)) {
                // Save order items
                for (orderItem oi : orderItems) {
                    orderDAO.addOrderItem(oi, newOrder.getOrderId());
                }

                // Clear cart after successful order
                cartDAO.clearCart(cartId);

                return new response(true, "Order created successfully|" + newOrder.getOrderId());
            } else {
                return new response(false, "Failed to create order");
            }

        } catch (Exception e) {
            System.err.println("❌ Create order error: " + e.getMessage());
            return new response(false, "Server error while creating order");
        }
    }

    private response handleGetOrder(request request) {
        String userId = sessionMgr.getUserIdFromToken(request.getToken());
        if (userId == null) {
            return new response(false, "Not authenticated");
        }

        String orderId = request.getParam("orderId");
        if (orderId == null || orderId.trim().isEmpty()) {
            return new response(false, "Order ID is required");
        }

        try {
            order ord = orderDAO.getOrderById(orderId);
            if (ord == null) {
                return new response(false, "Order not found");
            }

            // Verify user owns this order
            if (!ord.getUserId().equals(userId)) {
                return new response(false, "Access denied");
            }

            return new response(true, ord.getSummary());
        } catch (Exception e) {
            System.err.println("❌ Get order error: " + e.getMessage());
            return new response(false, "Server error while retrieving order");
        }
    }

    private response handleListOrders(request request) {
        String userId = sessionMgr.getUserIdFromToken(request.getToken());
        if (userId == null) {
            return new response(false, "Not authenticated");
        }

        try {
            List<order> orders = orderDAO.getOrdersByUserId(userId);
            
            if (orders.isEmpty()) {
                return new response(true, "No orders found");
            }

            StringBuilder sb = new StringBuilder();
            sb.append("📦 Your Orders:\n");
            sb.append("─────────────────────────────────────────────────\n");
            for (order ord : orders) {
                sb.append(String.format("Order: %s | Status: %s | Total: $%.2f | Date: %s\n",
                    ord.getOrderId().substring(0, Math.min(15, ord.getOrderId().length())),
                    ord.getStatus(),
                    ord.getTotalAmount(),
                    ord.getCreatedAt()));
            }
            sb.append("─────────────────────────────────────────────────");

            return new response(true, sb.toString());
        } catch (Exception e) {
            System.err.println("❌ List orders error: " + e.getMessage());
            return new response(false, "Server error while listing orders");
        }
    }

    private response handleCancelOrder(request request) {
        String userId = sessionMgr.getUserIdFromToken(request.getToken());
        if (userId == null) {
            return new response(false, "Not authenticated");
        }

        String orderId = request.getParam("orderId");
        if (orderId == null || orderId.trim().isEmpty()) {
            return new response(false, "Order ID is required");
        }

        try {
            order ord = orderDAO.getOrderById(orderId);
            if (ord == null) {
                return new response(false, "Order not found");
            }

            // Verify user owns this order
            if (!ord.getUserId().equals(userId)) {
                return new response(false, "Access denied");
            }

            // Check if order can be cancelled
            if (ord.getStatus() == OrderStatus.DELIVERED || ord.getStatus() == OrderStatus.CANCELLED) {
                return new response(false, "Cannot cancel order with status: " + ord.getStatus());
            }

            if (orderDAO.cancelOrder(orderId)) {
                return new response(true, "Order cancelled successfully");
            } else {
                return new response(false, "Failed to cancel order");
            }
        } catch (Exception e) {
            System.err.println("❌ Cancel order error: " + e.getMessage());
            return new response(false, "Server error while cancelling order");
        }
    }

    private response handleUpdateOrderStatus(request request) {
        String userId = sessionMgr.getUserIdFromToken(request.getToken());
        if (userId == null) {
            return new response(false, "Not authenticated");
        }

        String orderId = request.getParam("orderId");
        String statusStr = request.getParam("status");

        if (orderId == null || statusStr == null) {
            return new response(false, "Order ID and status are required");
        }

        try {
            OrderStatus status = OrderStatus.valueOf(statusStr.toUpperCase());
            
            if (orderDAO.updateOrderStatus(orderId, status)) {
                return new response(true, "Order status updated to: " + status);
            } else {
                return new response(false, "Failed to update order status");
            }
        } catch (IllegalArgumentException e) {
            return new response(false, "Invalid status value");
        } catch (Exception e) {
            System.err.println("❌ Update order status error: " + e.getMessage());
            return new response(false, "Server error while updating order status");
        }
    }
}
