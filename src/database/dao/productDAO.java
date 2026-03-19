package database.dao;

import database.databaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import model.product;

public class productDAO {
    
    public product getProductById(String productId) {
        String sql = "SELECT * FROM products WHERE productId = ?";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new product(
                    rs.getString("productId"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDouble("price"),
                    rs.getInt("stock"),
                    rs.getString("category")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error getting product by ID: " + e.getMessage());
        }
        return null;
    }
    
    public boolean updateProduct(String productId, String name, String description, Double price, Integer stock, String category) {
        // Build dynamic SQL based on provided parameters
        StringBuilder sql = new StringBuilder("UPDATE products SET ");
        boolean hasUpdates = false;
        
        if (name != null && !name.trim().isEmpty()) {
            sql.append("name = ?, ");
            hasUpdates = true;
        }
        if (description != null) {
            sql.append("description = ?, ");
            hasUpdates = true;
        }
        if (price != null) {
            sql.append("price = ?, ");
            hasUpdates = true;
        }
        if (stock != null) {
            sql.append("stock = ?, ");
            hasUpdates = true;
        }
        if (category != null && !category.trim().isEmpty()) {
            sql.append("category = ?, ");
            hasUpdates = true;
        }
        
        if (!hasUpdates) {
            return false; // Nothing to update
        }
        
        // Remove last comma and space, add WHERE clause
        sql.setLength(sql.length() - 2);
        sql.append(" WHERE productId = ?");
        
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            int paramIndex = 1;
            
            if (name != null && !name.trim().isEmpty()) {
                ps.setString(paramIndex++, name.trim());
            }
            if (description != null) {
                ps.setString(paramIndex++, description.trim());
            }
            if (price != null) {
                ps.setDouble(paramIndex++, price);
            }
            if (stock != null) {
                ps.setInt(paramIndex++, stock);
            }
            if (category != null && !category.trim().isEmpty()) {
                ps.setString(paramIndex++, category.trim());
            }
            
            ps.setString(paramIndex, productId);
            
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating product: " + e.getMessage());
        }
        return false;
    }
}
