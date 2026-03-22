package models;

import database.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Cart {

    // ── Attributs ─────────────────────────────────────────────────
    private String         cartId;
    private String         userId;
    private List<CartItem> items;

    // ── Séparateurs visuels ───────────────────────────────────────
    private static final String LINE =
        "╔══════════════════════════════════════════════╗";
    private static final String LINE_END =
        "╚══════════════════════════════════════════════╝";
    private static final String MID =
        "╠══════════════════════════════════════════════╣";

    // ── Constructeur ──────────────────────────────────────────────
    public Cart(String userId) {
        this.userId = userId;
        this.items  = new ArrayList<>();
    }

    // ── Getters ───────────────────────────────────────────────────
    public String         getCartId() { return cartId; }
    public String         getUserId() { return userId; }
    public List<CartItem> getItems()  { return items;  }

    // ════════════════════════════════════════════════════════════════
    // ── addItem(p: Product, qty: int)
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

        if (p.getStock() < qty) {
            System.out.printf("║  ❌ Not enough stock                          ║%n");
            System.out.printf("║  Available : %-31d║%n", p.getStock());
            System.out.printf("║  Requested : %-31d║%n", qty);
            System.out.println(LINE_END);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {

            // Cherche ou crée le cart pour cet userId
            String cartId = getOrCreateCart(conn, this.userId);
            if (cartId == null) {
                System.out.println("║  ❌ Failed to get or create cart             ║");
                System.out.println(LINE_END);
                return;
            }
            this.cartId = cartId;

            // Vérifie si le produit est déjà dans cart_items
            String checkSql = "SELECT itemId, quantity FROM cart_items WHERE cartId = ? AND productId = ?";
            PreparedStatement checkPs = conn.prepareStatement(checkSql);
            checkPs.setString(1, cartId);
            checkPs.setString(2, p.getProductId());
            ResultSet rs = checkPs.executeQuery();

            if (rs.next()) {
                // Produit déjà là → UPDATE quantité
                int    oldQty = rs.getInt("quantity");
                int    newQty = oldQty + qty;
                String itemId = rs.getString("itemId");

                String updateSql = "UPDATE cart_items SET quantity = ? WHERE itemId = ?";
                PreparedStatement updatePs = conn.prepareStatement(updateSql);
                updatePs.setInt(1, newQty);
                updatePs.setString(2, itemId);
                updatePs.executeUpdate();

                System.out.printf("║  ✅ Quantity updated                          ║%n");
                System.out.printf("║  Product   : %-31s║%n", p.getName());
                System.out.printf("║  Before    : %-31d║%n", oldQty);
                System.out.printf("║  After     : %-31d║%n", newQty);
                System.out.printf("║  UnitPrice : %-28.2f MAD║%n", p.getPrice());

            } else {
                // Produit pas encore là → INSERT
                String itemId    = java.util.UUID.randomUUID().toString();
                String insertSql = "INSERT INTO cart_items (itemId, cartId, productId, quantity, unitPrice) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement insertPs = conn.prepareStatement(insertSql);
                insertPs.setString(1, itemId);
                insertPs.setString(2, cartId);
                insertPs.setString(3, p.getProductId());
                insertPs.setInt(4, qty);
                insertPs.setDouble(5, p.getPrice());
                insertPs.executeUpdate();

                // Ajoute aussi en mémoire
                CartItem newItem = new CartItem(Integer.parseInt(userId), Integer.parseInt(p.getProductId().replaceAll("[^0-9]", "")), qty); 

                System.out.printf("║  ✅ Product added to cart                     ║%n");
                System.out.printf("║  Product   : %-31s║%n", p.getName());
                System.out.printf("║  ProductId : %-31d║%n", p.getProductId());
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
    // ── getOrCreateCart
    // ════════════════════════════════════════════════════════════════
    private String getOrCreateCart(Connection conn, String userId) throws SQLException {

        String selectSql = "SELECT cartId FROM cart WHERE userId = ?";
        PreparedStatement selectPs = conn.prepareStatement(selectSql);
        selectPs.setString(1, userId);
        ResultSet rs = selectPs.executeQuery();

        if (rs.next()) {
            return rs.getString("cartId");
        }

        // Pas de cart → créer un nouveau
        String newCartId = java.util.UUID.randomUUID().toString();
        String insertSql = "INSERT INTO cart (cartId, userId, createdAt) VALUES (?, ?, NOW())";
        PreparedStatement insertPs = conn.prepareStatement(insertSql);
        insertPs.setString(1, newCartId);
        insertPs.setString(2, userId);
        insertPs.executeUpdate();

        System.out.printf("║  🛒 New cart created                          ║%n");
        return newCartId;
    }
 // ════════════════════════════════════════════════════════════════
 // ── getItemCount — nombre total d'articles depuis la BD
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

     String sql = "SELECT SUM(quantity) AS total FROM cart_items WHERE cartId = ?";

     try (Connection conn = DatabaseConnection.getConnection();
          PreparedStatement ps = conn.prepareStatement(sql)) {

         ps.setString(1, this.cartId);
         ResultSet rs = ps.executeQuery();

         if (rs.next()) {
             int total = rs.getInt("total");
             System.out.printf("║  🛒 CartId  : %-30s║%n", cartId);
             System.out.printf("║  📦 Total   : %-30d║%n", total);
             System.out.println(LINE_END);
             return total;
         }

     } catch (SQLException e) {
         System.out.println("║  ❌ SQL Error: " + e.getMessage());
         System.out.println(LINE_END);
     }

     return 0;
 }
 


}
