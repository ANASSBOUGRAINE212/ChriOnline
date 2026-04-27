package database.dao;

import database.databaseConnection;
import java.sql.*;
import model.*;

public class paymentDAO {

    public boolean createPayment(payment pay) {
        String sql = "INSERT INTO payments (paymentId, orderId, amount, method, status, timestamp) VALUES (?, ?, ?, ?, ?, NOW())";
        
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, pay.getPaymentId());
            ps.setString(2, pay.getOrderId());
            ps.setDouble(3, pay.getAmount());
            ps.setString(4, pay.getMethod().toString());
            ps.setString(5, pay.getStatus().toString());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error creating payment: " + e.getMessage());
        }
        return false;
    }

    public payment getPaymentById(String paymentId) {
        String sql = "SELECT * FROM payments WHERE paymentId = ?";
        
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, paymentId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                payment pay = new payment();
                pay.setPaymentId(rs.getString("paymentId"));
                pay.setOrderId(rs.getString("orderId"));
                pay.setAmount(rs.getDouble("amount"));
                pay.setMethod(PaymentMethod.valueOf(rs.getString("method")));
                pay.setStatus(PaymentStatus.valueOf(rs.getString("status")));
                pay.setTimestamp(rs.getTimestamp("timestamp"));
                return pay;
            }
        } catch (SQLException e) {
            System.err.println("Error getting payment: " + e.getMessage());
        }
        return null;
    }

    public payment getPaymentByOrderId(String orderId) {
        String sql = "SELECT * FROM payments WHERE orderId = ?";
        
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, orderId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                payment pay = new payment();
                pay.setPaymentId(rs.getString("paymentId"));
                pay.setOrderId(rs.getString("orderId"));
                pay.setAmount(rs.getDouble("amount"));
                pay.setMethod(PaymentMethod.valueOf(rs.getString("method")));
                pay.setStatus(PaymentStatus.valueOf(rs.getString("status")));
                pay.setTimestamp(rs.getTimestamp("timestamp"));
                return pay;
            }
        } catch (SQLException e) {
            System.err.println("Error getting payment by order: " + e.getMessage());
        }
        return null;
    }

    public boolean updatePaymentStatus(String paymentId, PaymentStatus status) {
        String sql = "UPDATE payments SET status = ? WHERE paymentId = ?";
        
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, status.toString());
            ps.setString(2, paymentId);
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating payment status: " + e.getMessage());
        }
        return false;
    }

    public boolean refundPayment(String paymentId) {
        return updatePaymentStatus(paymentId, PaymentStatus.REFUNDED);
    }
}
