package database.dao;

import database.databaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.*;

public class orderDAO {

    public boolean createOrder(order ord) {
        String sql = "INSERT INTO orders (orderId, userId, totalAmount, status, createdAt) VALUES (?, ?, ?, ?, NOW())";
        
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, ord.getOrderId());
            ps.setString(2, ord.getUserId());
            ps.setDouble(3, ord.getTotalAmount());
            
            // Safety check for null status
            OrderStatus status = ord.getStatus();
            if (status == null) {
                status = OrderStatus.PENDING;
                ord.setStatus(status);
            }
            ps.setString(4, status.toString());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error creating order: " + e.getMessage());
        }
        return false;
    }

    public order getOrderById(String orderId) {
        String sql = "SELECT * FROM orders WHERE orderId = ?";
        
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, orderId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                order ord = new order();
                ord.setOrderId(rs.getString("orderId"));
                ord.setUserId(rs.getString("userId"));
                ord.setTotalAmount(rs.getDouble("totalAmount"));
                ord.setStatus(OrderStatus.valueOf(rs.getString("status")));
                ord.setCreatedAt(rs.getTimestamp("createdAt"));
                
                // Load order items
                ord.setItems(getOrderItems(orderId));
                
                return ord;
            }
        } catch (SQLException e) {
            System.err.println("Error getting order: " + e.getMessage());
        }
        return null;
    }

    public List<order> getOrdersByUserId(String userId) {
        String sql = "SELECT * FROM orders WHERE userId = ? ORDER BY createdAt DESC";
        List<order> orders = new ArrayList<>();
        
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                order ord = new order();
                ord.setOrderId(rs.getString("orderId"));
                ord.setUserId(rs.getString("userId"));
                ord.setTotalAmount(rs.getDouble("totalAmount"));
                ord.setStatus(OrderStatus.valueOf(rs.getString("status")));
                ord.setCreatedAt(rs.getTimestamp("createdAt"));
                ord.setItems(getOrderItems(ord.getOrderId()));
                orders.add(ord);
            }
        } catch (SQLException e) {
            System.err.println("Error getting user orders: " + e.getMessage());
        }
        return orders;
    }

    public boolean updateOrderStatus(String orderId, OrderStatus status) {
        String sql = "UPDATE orders SET status = ? WHERE orderId = ?";
        
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, status.toString());
            ps.setString(2, orderId);
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating order status: " + e.getMessage());
        }
        return false;
    }

    public boolean addOrderItem(orderItem item, String orderId) {
        String sql = "INSERT INTO order_items (itemId, orderId, productId, quantity, unitPrice) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, item.getItemId());
            ps.setString(2, orderId);
            ps.setString(3, item.getProduct().getId());
            ps.setInt(4, item.getQuantity());
            ps.setDouble(5, item.getUnitPrice());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding order item: " + e.getMessage());
        }
        return false;
    }

    public List<orderItem> getOrderItems(String orderId) {
        String sql = """
            SELECT oi.*, p.id, p.name, p.description, p.price, p.stock, p.category
            FROM order_items oi
            JOIN products p ON oi.productId = p.id
            WHERE oi.orderId = ?
            """;
        List<orderItem> items = new ArrayList<>();
        
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, orderId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                product p = new product();
                p.setId(String.valueOf(rs.getInt("id")));
                p.setName(rs.getString("name"));
                p.setDescription(rs.getString("description"));
                p.setPrice(rs.getDouble("price"));
                p.setStock(rs.getInt("stock"));
                p.setCategory(rs.getString("category"));
                
                orderItem item = new orderItem();
                item.setItemId(rs.getString("itemId"));
                item.setProduct(p);
                item.setQuantity(rs.getInt("quantity"));
                item.setUnitPrice(rs.getDouble("unitPrice"));
                
                items.add(item);
            }
        } catch (SQLException e) {
            System.err.println("Error getting order items: " + e.getMessage());
        }
        return items;
    }

    public boolean cancelOrder(String orderId) {
        return updateOrderStatus(orderId, OrderStatus.CANCELLED);
    }
}
