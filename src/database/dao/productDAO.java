package database.dao;

import database.databaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.product;

public class productDAO {

    public boolean addProduct(product p) {
        String sql = "INSERT INTO products (name, description, price, stock, category, created_by) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getDescription());
            ps.setDouble(3, p.getPrice());
            ps.setInt(4, p.getStock());
            ps.setString(5, p.getCategory());
            ps.setString(6, p.getCreatedBy());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding product: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteProduct(int productId) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting product: " + e.getMessage());
        }
        return false;
    }

    public product getProductById(int productId) {
        String sql = """
            SELECT p.*, u.username as admin_username
            FROM products p
            LEFT JOIN users u ON p.created_by = u.userId
            WHERE p.id = ?
            """;
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                product p = new product();
                p.setId(String.valueOf(rs.getInt("id")));
                p.setName(rs.getString("name"));
                p.setDescription(rs.getString("description"));
                p.setPrice(rs.getDouble("price"));
                p.setStock(rs.getInt("stock"));
                p.setCategory(rs.getString("category"));
                p.setCreatedBy(rs.getString("admin_username"));
                return p;
            }
        } catch (SQLException e) {
            System.err.println("Error getting product: " + e.getMessage());
        }
        return null;
    }

    // ✅ From person 3 — update product dynamically
    public boolean updateProduct(int productId, String name, String description,
                                  Double price, Integer stock, String category) {
        StringBuilder sql = new StringBuilder("UPDATE products SET ");
        boolean hasUpdates = false;

        if (name != null && !name.trim().isEmpty())        { sql.append("name = ?, ");        hasUpdates = true; }
        if (description != null)                           { sql.append("description = ?, "); hasUpdates = true; }
        if (price != null)                                 { sql.append("price = ?, ");       hasUpdates = true; }
        if (stock != null)                                 { sql.append("stock = ?, ");       hasUpdates = true; }
        if (category != null && !category.trim().isEmpty()){ sql.append("category = ?, ");   hasUpdates = true; }

        if (!hasUpdates) return false;

        sql.setLength(sql.length() - 2);
        sql.append(" WHERE id = ?");

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int i = 1;
            if (name != null && !name.trim().isEmpty())        ps.setString(i++, name.trim());
            if (description != null)                           ps.setString(i++, description.trim());
            if (price != null)                                 ps.setDouble(i++, price);
            if (stock != null)                                 ps.setInt(i++, stock);
            if (category != null && !category.trim().isEmpty())ps.setString(i++, category.trim());
            ps.setInt(i, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating product: " + e.getMessage());
        }
        return false;
    }

    // ✅ From person 3 — list all products
    public List<product> getAllProducts() {
        String sql = "SELECT * FROM products ORDER BY id";
        List<product> products = new ArrayList<>();
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                product p = new product();
                p.setId(String.valueOf(rs.getInt("id")));
                p.setName(rs.getString("name"));
                p.setDescription(rs.getString("description"));
                p.setPrice(rs.getDouble("price"));
                p.setStock(rs.getInt("stock"));
                p.setCategory(rs.getString("category"));
                products.add(p);
            }
        } catch (SQLException e) {
            System.err.println("Error listing products: " + e.getMessage());
        }
        return products;
    }
}