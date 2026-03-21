package models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import database.DatabaseConnection;

public class Product {

    // ── Attributs ─────────────────────────────────────────────────
    private String productId;
    private String name;
    private String description;
    private double price;
    private int    stock;
    private String category;

    // ── Séparateurs visuels ───────────────────────────────────────
    private static final String LINE =
        "╔══════════════════════════════════════════════╗";
    private static final String LINE_END =
        "╚══════════════════════════════════════════════╝";
    private static final String MID =
        "╠══════════════════════════════════════════════╣";

    // ── Constructeurs ─────────────────────────────────────────────
    public Product() {}

    public Product(String name, String description,
                   double price, int stock, String category) {
        setName(name);
        setDescription(description);
        setPrice(price);
        setStock(stock);
        setCategory(category);
    }

    // ── Getters / Setters ─────────────────────────────────────────
    public String getProductId()           { return productId; }
    public void   setProductId(String id)  { this.productId = id; }

    public String getName()                { return name; }
    public void   setName(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Le nom est obligatoire");
        this.name = name.trim();
    }

    public String getDescription()             { return description; }
    public void   setDescription(String desc)  {
        this.description = (desc != null) ? desc.trim() : "";
    }

    public double getPriceField()               { return price; }
    public void   setPrice(double price) {
        if (price < 0)
            throw new IllegalArgumentException("Le prix ne peut pas être négatif");
        this.price = price;
    }

    public int  getStock()               { return stock; }
    public void setStock(int stock) {
        if (stock < 0)
            throw new IllegalArgumentException("Le stock ne peut pas être négatif");
        this.stock = stock;
    }

    public String getCategory()                { return category; }
    public void   setCategory(String category) { this.category = category; }

    public boolean isAvailable() { return this.stock > 0; }

    // ── Barre de stock visuelle ───────────────────────────────────
    private String stockBar(int stock, int max) {
        int filled = (max == 0) ? 0 : Math.min(10, (int)((stock * 10.0) / max));
        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < 10; i++)
            bar.append(i < filled ? "█" : "░");
        bar.append("]");
        return bar.toString();
    }

    // ── toString() ────────────────────────────────────────────────
    @Override
    public String toString() {
        return String.format(
            "  %-8s | %-20s | %8.2f MAD | Stock: %3d | %s",
            productId, name, price, stock,
            isAvailable() ? "✅ Dispo" : "❌ Rupture"
        );
    }

    // ════════════════════════════════════════════════════════════════
    // ── toDTO() : convertit l'objet Product courant en ProductDTO
    // ════════════════════════════════════════════════════════════════
    public ProductDTO toDTO() {
        return new ProductDTO(
            this.productId,
            this.name,
            this.description,
            this.price,
            this.stock,
            this.category
        );
    }

    // ════════════════════════════════════════════════════════════════
    // ── fromDTO() : reconstruit un Product à partir d'un ProductDTO
    // ════════════════════════════════════════════════════════════════
    public static Product fromDTO(ProductDTO dto) {
        Product p = new Product();
        p.setProductId(dto.getProductId());
        p.setName(dto.getName());
        p.setDescription(dto.getDescription());
        p.setPrice(dto.getPrice());
        p.setStock(dto.getStock());
        p.setCategory(dto.getCategory());
        return p;
    }

    // ════════════════════════════════════════════════════════════════
    // ── getProductDetails() — refaite avec toDTO() / fromDTO()
    //
    //  Étapes :
    //    1. Requête SQL → données brutes dans un ProductDTO
    //    2. fromDTO()   → reconstruit le Product (met à jour this)
    //    3. toDTO()     → lit le DTO final pour construire l'affichage
    // ════════════════════════════════════════════════════════════════
    public String getProductDetails() {

        String sql = "SELECT * FROM Products WHERE productId = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, this.productId);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                return "❌ Produit introuvable en base (productId=" + productId + ")";
            }

            // ── Étape 1 : construire le DTO depuis le ResultSet ───
            ProductDTO dto = new ProductDTO(
                rs.getString("productId"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getDouble("price"),
                rs.getInt("stock"),
                rs.getString("category")
            );

            // ── Étape 2 : reconstruire le Product depuis le DTO ───
            //    (synchronise les champs de this avec la base)
            Product updated = Product.fromDTO(dto);
            this.name        = updated.name;
            this.description = updated.description;
            this.price       = updated.price;
            this.stock       = updated.stock;
            this.category    = updated.category;

            // ── Étape 3 : lire via toDTO() pour l'affichage ───────
            ProductDTO view = this.toDTO();

            String descDisplay = view.getDescription() != null
                && view.getDescription().length() > 27
                    ? view.getDescription().substring(0, 24) + "..."
                    : view.getDescription();

            return String.format(
                "%n%s%n" +
                "║           DÉTAILS DU PRODUIT                 ║%n" +
                "%s%n" +
                "║  🔑 ID          : %-27s║%n" +
                "║  📦 Nom         : %-27s║%n" +
                "║  🏷️  Catégorie   : %-27s║%n" +
                "║  📝 Description : %-27s║%n" +
                "%s%n" +
                "║  💰 Prix        : %-24.2f MAD║%n" +
                "║  📊 Stock       : %-27d║%n" +
                "║  📉 Jauge       : %-27s║%n" +
                "║  🚦 Statut      : %-27s║%n" +
                "%s",
                LINE, MID,
                view.getProductId(),
                view.getName(),
                view.getCategory() != null ? view.getCategory() : "Non définie",
                descDisplay,
                MID,
                view.getPrice(),
                view.getStock(),
                stockBar(view.getStock(), 30),
                view.getStock() > 0 ? "✅ En stock" : "❌ Rupture de stock",
                LINE_END
            );

        } catch (SQLException e) {
            return "❌ Erreur SQL : " + e.getMessage();
        }
    }

    // ════════════════════════════════════════════════════════════════
    // ── updateStock(qty: int): void
    // ════════════════════════════════════════════════════════════════
    public void updateStock(int qty) {

        System.out.println("\n" + LINE);
        System.out.println("║           MISE À JOUR DU STOCK               ║");
        System.out.println(MID);

        if (qty == 0) {
            System.out.println("║  ⚠️  Quantité 0 — aucune modification          ║");
            System.out.println(LINE_END);
            return;
        }

        if (this.stock + qty < 0) {
            System.out.printf("║  ❌ Stock insuffisant                         ║%n");
            System.out.printf("║     Stock actuel  : %-25d║%n", this.stock);
            System.out.printf("║     Retrait voulu : %-25d║%n", Math.abs(qty));
            System.out.printf("║     Manque        : %-25d║%n", Math.abs(this.stock + qty));
            System.out.println(LINE_END);
            throw new IllegalArgumentException(
                "Stock insuffisant : " + this.stock + " disponible, " +
                Math.abs(qty) + " demandé.");
        }

        String sql = "UPDATE Products SET stock = stock + ? WHERE productId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, qty);
            ps.setString(2, this.productId);
            int rows = ps.executeUpdate();

            if (rows > 0) {
                int ancienStock = this.stock;
                this.stock += qty;
                String sens = qty > 0 ? "📦 Réapprovisionnement" : "🛒 Vente / Retrait";
                System.out.printf("║  %-44s║%n", sens);
                System.out.printf("║  Produit    : %-31s║%n", this.name);
                System.out.printf("║  Avant      : %-31d║%n", ancienStock);
                System.out.printf("║  Variation  : %-31s║%n", (qty > 0 ? "+" : "") + qty);
                System.out.printf("║  Après      : %-31d║%n", this.stock);
                System.out.printf("║  Statut     : %-31s║%n",
                    isAvailable() ? "✅ En stock" : "❌ Rupture de stock");
                System.out.println(LINE_END);
            } else {
                System.out.println("║  ❌ Produit introuvable en base (productId=" + productId + ")    ║");
                System.out.println(LINE_END);
            }

        } catch (SQLException e) {
            System.out.println("║  ❌ Erreur SQL : " + e.getMessage());
            System.out.println(LINE_END);
        }
    }

    // ════════════════════════════════════════════════════════════════
    // ── checkStock(): boolean
    // ════════════════════════════════════════════════════════════════
    public boolean checkStock() {

        System.out.println("\n" + LINE);
        System.out.println("║           VÉRIFICATION DU STOCK              ║");
        System.out.println(MID);

        String sql = "SELECT stock FROM Products WHERE productId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, this.productId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int stockDB = rs.getInt("stock");
                this.stock  = stockDB;
                boolean dispo = stockDB > 0;

                System.out.printf("║  Produit    : %-31s║%n",
                    this.name != null ? this.name : "productId=" + productId);
                System.out.printf("║  Stock DB   : %-31d║%n", stockDB);
                System.out.printf("║  Jauge      : %-31s║%n", stockBar(stockDB, 30));
                System.out.printf("║  Résultat   : %-31s║%n",
                    dispo ? "✅ Disponible" : "❌ Rupture de stock");
                System.out.println(LINE_END);
                return dispo;
            } else {
                System.out.println("║  ❌ Produit introuvable (productId=" + productId + ")           ║");
                System.out.println(LINE_END);
            }

        } catch (SQLException e) {
            System.out.println("║  ❌ Erreur SQL : " + e.getMessage());
            System.out.println(LINE_END);
        }
        return false;
    }

    // ════════════════════════════════════════════════════════════════
    // ── getPrice(): double
    // ════════════════════════════════════════════════════════════════
    public double getPrice() {

        System.out.println("\n" + LINE);
        System.out.println("║           CONSULTATION DU PRIX               ║");
        System.out.println(MID);

        String sql = "SELECT name, price FROM Products WHERE productId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, this.productId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                this.name  = rs.getString("name");
                this.price = rs.getDouble("price");

                System.out.printf("║  Produit : %-34s║%n", this.name);
                System.out.printf("║  Prix    : %-31.2f MAD║%n", this.price);
                System.out.println(LINE_END);
                return this.price;
            } else {
                System.out.println("║  ❌ Produit introuvable (productId=" + productId + ")           ║");
                System.out.println(LINE_END);
            }

        } catch (SQLException e) {
            System.out.println("║  ❌ Erreur SQL : " + e.getMessage());
            System.out.println(LINE_END);
        }
        return -1.0;
    }
}
