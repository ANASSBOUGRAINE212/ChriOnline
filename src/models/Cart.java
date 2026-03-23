package models;

import database.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Cart {

    // ── Attributs (exactement selon le diagramme UML) ─────────────
    private String         cartId;      // private
    private String         userId;      // private
    private List<CartItem> items;       // private
    private Date           createdAt;   // private

    // ── Séparateurs visuels (Membre 3) ────────────────────────────
    private static final String LINE =
        "╔══════════════════════════════════════════════╗";
    private static final String LINE_END =
        "╚══════════════════════════════════════════════╝";
    private static final String MID =
        "╠══════════════════════════════════════════════╣";

    // ── Constructeurs ─────────────────────────────────────────────

    public Cart() {
        this.items     = new ArrayList<>();
        this.createdAt = new Date();
    }

    public Cart(String userId) {
        this.userId    = userId;
        this.items     = new ArrayList<>();
        this.createdAt = new Date();
    }

    public Cart(String cartId, String userId, Date createdAt) {
        this.cartId    = cartId;
        this.userId    = userId;
        this.createdAt = createdAt;
        this.items     = new ArrayList<>();
    }

    // ── Getters / Setters ─────────────────────────────────────────
    public String getCartId()                      { return cartId; }
    public void setCartId(String cartId)           { this.cartId = cartId; }
    public String getUserId()                      { return userId; }
    public void setUserId(String userId)           { this.userId = userId; }
    public Date getCreatedAt()                     { return createdAt; }
    public void setCreatedAt(Date createdAt)       { this.createdAt = createdAt; }
  
    public void setItems(List<CartItem> items)     { this.items = items; }

    // ════════════════════════════════════════════════════════════════
    // + addItem(p: Product, qty: int): void          [UML]
    // ════════════════════════════════════════════════════════════════
    public void addItem(Product p, int qty) {

        System.out.println("\n" + LINE);
        System.out.println("║              ADD ITEM TO CART                ║");
        System.out.println(MID);

        if (p == null) {
            System.out.println("║  ❌ Product is null                          ║");
            System.out.println(LINE_END);
            return;
        }
        if (qty <= 0) {
            System.out.println("║  ❌ Quantity must be greater than 0          ║");
            System.out.println(LINE_END);
            return;
        }
        if (!p.checkStock()) {
            System.out.printf("║  ❌ Not enough stock                         ║%n");
            System.out.printf("║  Available : %-31d║%n", p.getStock());
            System.out.printf("║  Requested : %-31d║%n", qty);
            System.out.println(LINE_END);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {

            // Récupère ou crée le cart pour cet userId
            String resolvedCartId = getOrCreateCart(conn, this.userId);
            if (resolvedCartId == null) {
                System.out.println("║  ❌ Failed to get or create cart             ║");
                System.out.println(LINE_END);
                return;
            }
            this.cartId = resolvedCartId;

            // Vérifie si le produit est déjà dans cart_items
            String checkSql = "SELECT itemId, quantity FROM cart_items WHERE cartId = ? AND productId = ?";
            PreparedStatement checkPs = conn.prepareStatement(checkSql);
            checkPs.setString(1, this.cartId);
            checkPs.setString(2, p.getProductId());
            ResultSet rs = checkPs.executeQuery();

            if (rs.next()) {
                // Produit déjà présent → UPDATE quantité
                int    oldQty = rs.getInt("quantity");
                int    newQty = oldQty + qty;
                String itemId = rs.getString("itemId");

                String updateSql = "UPDATE cart_items SET quantity = ? WHERE itemId = ?";
                PreparedStatement updatePs = conn.prepareStatement(updateSql);
                updatePs.setInt(1, newQty);
                updatePs.setString(2, itemId);
                updatePs.executeUpdate();

                System.out.printf("║  ✅ Quantity updated                         ║%n");
                System.out.printf("║  Product   : %-31s║%n", p.getName());
                System.out.printf("║  Before    : %-31d║%n", oldQty);
                System.out.printf("║  After     : %-31d║%n", newQty);
                System.out.printf("║  UnitPrice : %-28.2f MAD║%n", p.getPrice());

            } else {
                // Produit pas encore là → INSERT
                String itemId    = UUID.randomUUID().toString();
                String insertSql = "INSERT INTO cart_items (itemId, cartId, productId, quantity, unitPrice) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement insertPs = conn.prepareStatement(insertSql);
                insertPs.setString(1, itemId);
                insertPs.setString(2, this.cartId);
                insertPs.setString(3, p.getProductId());
                insertPs.setInt(4, qty);
                insertPs.setDouble(5, p.getPrice());
                insertPs.executeUpdate();

                // Ajoute aussi en mémoire
                CartItem newItem = new CartItem(itemId, p, qty, p.getPrice());
                this.items.add(newItem);

                System.out.printf("║  ✅ Product added to cart                    ║%n");
                System.out.printf("║  Product   : %-31s║%n", p.getName());
                System.out.printf("║  Quantity  : %-31d║%n", qty);
                System.out.printf("║  UnitPrice : %-28.2f MAD║%n", p.getPrice());
            }
            System.out.println(LINE_END);

        } catch (SQLException e) {
            System.out.println("║  ❌ SQL Error: " + e.getMessage());
            System.out.println(LINE_END);
        }
    }

    // ════════════════════════════════════════════════════════════════
    // + removeItem(productId: String): void          [UML]
    // ════════════════════════════════════════════════════════════════
    public void removeItem(String productId) {
        String sql = "DELETE FROM cart_items WHERE cartId = ? AND productId = ?";
        try (Connection conn =DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, this.cartId);
            ps.setString(2, productId);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                // Supprimer aussi de la liste en mémoire
                items.removeIf(item -> item.getProduct().getProductId().equals(productId));
                System.out.println("🗑️  Product " + productId + " removed from cart");
            } else {
                System.out.println("⚠️  Product " + productId + " not found in cart");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error removing item: " + e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════════
    // + calculateTotal(): double                     [UML]
    // ════════════════════════════════════════════════════════════════
    public double calculateTotal() {
        String sql = "SELECT SUM(unitPrice * quantity) as total FROM cart_items WHERE cartId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, this.cartId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                double total = rs.getDouble("total");
                System.out.printf("💰 Cart total: %.2f MAD%n", total);
                return total;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error calculating total: " + e.getMessage());
        }
        return 0.0;
    }

    // ════════════════════════════════════════════════════════════════
    // + clear(): void                                [UML]
    // ════════════════════════════════════════════════════════════════
    public void clear() {
        String sql = "DELETE FROM cart_items WHERE cartId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, this.cartId);
            ps.executeUpdate();
            this.items.clear();
            System.out.println("🗑️  Cart cleared successfully");
        } catch (SQLException e) {
            System.err.println("❌ Error clearing cart: " + e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════════
    // + getItems(): List<CartItem>                   [UML]
    // ════════════════════════════════════════════════════════════════
    public List<CartItem> getItems() {
        String sql = """
            SELECT ci.itemId, ci.quantity, ci.unitPrice,
                   p.id, p.name, p.description, p.price, p.stock, p.category
            FROM cart_items ci
            JOIN products p ON ci.productId = p.id
            WHERE ci.cartId = ?
            ORDER BY ci.itemId
            """;
        List<CartItem> itemList = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, this.cartId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Product p = new Product();
                p.setProductId(rs.getString("id"));
                p.setName(rs.getString("name"));
                p.setDescription(rs.getString("description"));
                p.setPrice(rs.getDouble("price"));
                p.setStock(rs.getInt("stock"));
                p.setCategory(rs.getString("category"));

                CartItem item = new CartItem();
                item.setItemId(rs.getString("itemId"));
                item.setProduct(p);
                item.setQuantity(rs.getInt("quantity"));
                item.setUnitPrice(rs.getDouble("unitPrice"));
                itemList.add(item);
            }
            this.items = itemList;
            System.out.println("📦 Retrieved " + itemList.size() + " items from cart: " + cartId);
        } catch (SQLException e) {
            System.err.println("❌ Error getting cart items: " + e.getMessage());
        }
        return itemList;
    }

    // ════════════════════════════════════════════════════════════════
    // + getItemCount(): int                          [UML]
    // ════════════════════════════════════════════════════════════════
    public int getItemCount() {

        System.out.println("\n" + LINE);
        System.out.println("║            GET ITEM COUNT                    ║");
        System.out.println(MID);

        if (cartId == null) {
            System.out.println("║  ❌ No cart found for this user               ║");
            System.out.println(LINE_END);
            return 0;
        }

        String sql = "SELECT COUNT(*) as count, SUM(quantity) as total FROM cart_items WHERE cartId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, this.cartId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int count = rs.getInt("count");
                int total = rs.getInt("total");
                System.out.printf("║  🛒 CartId        : %-24s║%n", cartId);
                System.out.printf("║  📦 Produits diff : %-24d║%n", count);
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
    // Méthode privée utilitaire — getOrCreateCart (Membre 3)
    // ════════════════════════════════════════════════════════════════
    private String getOrCreateCart(Connection conn, String userId) throws SQLException {
        String selectSql = "SELECT cartId FROM cart WHERE userId = ?";
        PreparedStatement selectPs = conn.prepareStatement(selectSql);
        selectPs.setString(1, userId);
        ResultSet rs = selectPs.executeQuery();

        if (rs.next()) return rs.getString("cartId");

        // Pas de cart → créer un nouveau
        String newCartId = UUID.randomUUID().toString();
        String insertSql = "INSERT INTO cart (cartId, userId, createdAt) VALUES (?, ?, NOW())";
        PreparedStatement insertPs = conn.prepareStatement(insertSql);
        insertPs.setString(1, newCartId);
        insertPs.setString(2, userId);
        insertPs.executeUpdate();

        System.out.printf("║  🛒 New cart created : %-22s║%n", newCartId);
        return newCartId;
    }
}
