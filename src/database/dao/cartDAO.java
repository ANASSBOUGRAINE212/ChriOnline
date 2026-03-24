package database.dao;

import database.databaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import model.cart;
import model.cartItem;
import model.product;

public class cartDAO {

    // ── Visual separators ─────────────────────────────────────────
    private static final String LINE =
        "╔══════════════════════════════════════════════╗";
    private static final String LINE_END =
        "╚══════════════════════════════════════════════╝";
    private static final String MID =
        "╠══════════════════════════════════════════════╣";

    // ════════════════════════════════════════════════════════════════
    // getOrCreateCart(userId: String): Cart
    // ════════════════════════════════════════════════════════════════
    public cart getOrCreateCart(String userId) {
        String selectSql = "SELECT cartId FROM cart WHERE userId = ?";
        String insertSql = "INSERT INTO cart (cartId, userId, createdAt) VALUES (?, ?, NOW())";

        try (Connection conn = databaseConnection.getConnection()) {

            // Try to find existing cart
            PreparedStatement select = conn.prepareStatement(selectSql);
            select.setString(1, userId);
            ResultSet rs = select.executeQuery();

            if (rs.next()) {
                String cartId = rs.getString("cartId");
                System.out.println("\n" + LINE);
                System.out.println("║            GET OR CREATE CART                ║");
                System.out.println(MID);
                System.out.printf("║  🛒 Existing cart found : %-19s║%n", cartId);
                System.out.println(LINE_END);

                cart c = new cart();
                c.setCartId(cartId);
                c.setUserId(userId);
                return c;
            }

            // No cart found → create a new one
            String newCartId = UUID.randomUUID().toString();
            PreparedStatement insert = conn.prepareStatement(insertSql);
            insert.setString(1, newCartId);
            insert.setString(2, userId);
            insert.executeUpdate();

            System.out.println("\n" + LINE);
            System.out.println("║            GET OR CREATE CART                ║");
            System.out.println(MID);
            System.out.printf("║  🛒 New cart created : %-22s║%n", newCartId);
            System.out.println(LINE_END);

            cart c = new cart();
            c.setCartId(newCartId);
            c.setUserId(userId);
            return c;

        } catch (SQLException e) {
            System.err.println("❌ Error getting/creating cart: " + e.getMessage());
        }
        return null;
    }

    // ════════════════════════════════════════════════════════════════
    // getItems(cartId: String): List<CartItem>
    // ════════════════════════════════════════════════════════════════
    public List<cartItem> getItems(String cartId) {
        String sql = """
            SELECT ci.itemId, ci.quantity, ci.unitPrice,
                   p.id, p.name, p.description, p.price, p.stock, p.category
            FROM cart_items ci
            JOIN products p ON ci.productId = p.id
            WHERE ci.cartId = ?
            ORDER BY ci.itemId
            """;

        List<cartItem> items = new ArrayList<>();

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, cartId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                product p = new product();
                p.setId(rs.getString("id"));
                p.setName(rs.getString("name"));
                p.setDescription(rs.getString("description"));
                p.setPrice(rs.getDouble("price"));
                p.setStock(rs.getInt("stock"));
                p.setCategory(rs.getString("category"));

                cartItem item = new cartItem();
                item.setItemId(rs.getString("itemId"));
                item.setProduct(p);
                item.setQuantity(rs.getInt("quantity"));
                item.setUnitPrice(rs.getDouble("unitPrice"));
                items.add(item);
            }

            System.out.println("📦 Retrieved " + items.size() + " items from cart: " + cartId);

        } catch (SQLException e) {
            System.err.println("❌ Error getting cart items: " + e.getMessage());
        }
        return items;
    }

    // ════════════════════════════════════════════════════════════════
    // addItem(cartId: String, productId: int, quantity: int, unitPrice: double): boolean
    // ════════════════════════════════════════════════════════════════
    public boolean addItem(String cartId, int productId, int quantity, double unitPrice) {
        String checkSql = "SELECT quantity FROM cart_items WHERE cartId = ? AND productId = ?";
        String updateSql = "UPDATE cart_items SET quantity = quantity + ? WHERE cartId = ? AND productId = ?";
        String insertSql = "INSERT INTO cart_items (itemId, cartId, productId, quantity, unitPrice) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = databaseConnection.getConnection()) {
            
            // Check if item already exists in cart
            PreparedStatement checkPs = conn.prepareStatement(checkSql);
            checkPs.setString(1, cartId);
            checkPs.setInt(2, productId);
            ResultSet rs = checkPs.executeQuery();

            if (rs.next()) {
                // Item exists, update quantity
                PreparedStatement updatePs = conn.prepareStatement(updateSql);
                updatePs.setInt(1, quantity);
                updatePs.setString(2, cartId);
                updatePs.setInt(3, productId);
                int rows = updatePs.executeUpdate();
                
                if (rows > 0) {
                    System.out.println("✅ Updated quantity for product " + productId + " in cart");
                    return true;
                }
            } else {
                // Item doesn't exist, insert new
                String itemId = UUID.randomUUID().toString();
                PreparedStatement insertPs = conn.prepareStatement(insertSql);
                insertPs.setString(1, itemId);
                insertPs.setString(2, cartId);
                insertPs.setInt(3, productId);
                insertPs.setInt(4, quantity);
                insertPs.setDouble(5, unitPrice);
                int rows = insertPs.executeUpdate();
                
                if (rows > 0) {
                    System.out.println("✅ Added product " + productId + " to cart");
                    return true;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error adding item to cart: " + e.getMessage());
        }
        return false;
    }

    // ════════════════════════════════════════════════════════════════
    // removeItem(cartId: String, productId: int): boolean
    // ════════════════════════════════════════════════════════════════
    public boolean removeItem(String cartId, int productId) {
        String sql = "DELETE FROM cart_items WHERE cartId = ? AND productId = ?";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, cartId);
            ps.setInt(2, productId);
            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("🗑️  Removed product " + productId + " from cart");
                return true;
            } else {
                System.out.println("⚠️  Product " + productId + " not found in cart");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error removing item from cart: " + e.getMessage());
        }
        return false;
    }

    // ════════════════════════════════════════════════════════════════
    // getSubtotal(cartId: String): double
    // ════════════════════════════════════════════════════════════════
    public double getSubtotal(String cartId) {
        String sql = "SELECT SUM(quantity * unitPrice) AS total FROM cart_items WHERE cartId = ?";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, cartId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                double total = rs.getDouble("total");
                System.out.printf("💰 Cart subtotal: %.2f MAD%n", total);
                return total;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error calculating cart subtotal: " + e.getMessage());
        }
        return 0.0;
    }

    // ════════════════════════════════════════════════════════════════
    // getItemCount(cartId: String): int
    // ════════════════════════════════════════════════════════════════
    public int getItemCount(String cartId) {
        System.out.println("\n" + LINE);
        System.out.println("║            GET ITEM COUNT                    ║");
        System.out.println(MID);

        if (cartId == null) {
            System.out.println("║  ❌ No cart found for this user               ║");
            System.out.println(LINE_END);
            return 0;
        }

        String sql = "SELECT COUNT(*) AS count, SUM(quantity) AS total FROM cart_items WHERE cartId = ?";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, cartId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int count = rs.getInt("count");
                int total = rs.getInt("total");
                System.out.printf("║  🛒 CartId        : %-24s║%n", cartId);
                System.out.printf("║  📦 Distinct items: %-24d║%n", count);
                System.out.printf("║  📦 Total articles: %-24d║%n", total);
                System.out.println(LINE_END);
                return total;
            }

        } catch (SQLException e) {
            System.out.println("║  ❌ SQL Error: " + e.getMessage());
            System.out.println(LINE_END);
        }
        return 0;
    }

    // ════════════════════════════════════════════════════════════════
    // clearCart(cartId: String): boolean
    // ════════════════════════════════════════════════════════════════
    public boolean clearCart(String cartId) {
        String sql = "DELETE FROM cart_items WHERE cartId = ?";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, cartId);
            ps.executeUpdate();
            System.out.println("🗑️  Cart cleared successfully");
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Error clearing cart: " + e.getMessage());
        }
        return false;
    }
}