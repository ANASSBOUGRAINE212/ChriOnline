package model;

import database.databaseConnection;
import java.sql.*;

public class product {
    private String id;
    private String name;
    private String description;
    private double price;
    private int stock;
    private String category;
    private String createdBy;

    // ── Visual separators ────────────────────────────────────────
    private static final String LINE =
        "╔══════════════════════════════════════════════╗";
    private static final String LINE_END =
        "╚══════════════════════════════════════════════╝";
    private static final String MID =
        "╠══════════════════════════════════════════════╣";

    // ── Constructors ─────────────────────────────────────────────
    public product() {}

    public product(String id, String name, String description,
                   double price, int stock, String category, String createdBy) {
        this.id = id;
        setName(name);
        setDescription(description);
        setPrice(price);
        setStock(stock);
        setCategory(category);
        this.createdBy = createdBy;
    }

    // ── Getters / Setters ─────────────────────────────────────────
    public String getId()             { return id; }
    public void setId(String id)      { this.id = id; }

    public String getName()        { return name; }
    public void setName(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Name is required");
        this.name = name.trim();
    }

    public String getDescription()            { return description; }
    public void setDescription(String desc)   {
        this.description = (desc != null) ? desc.trim() : "";
    }

    public double getPrice()              { return price; }
    public void setPrice(double price) {
        if (price < 0)
            throw new IllegalArgumentException("Price cannot be negative");
        this.price = price;
    }

    public int getStock()             { return stock; }
    public void setStock(int stock) {
        if (stock < 0)
            throw new IllegalArgumentException("Stock cannot be negative");
        this.stock = stock;
    }

    public String getCategory()               { return category; }
    public void setCategory(String category)  { this.category = category; }

    public String getCreatedBy()              { return createdBy; }
    public void setCreatedBy(String createdBy){ this.createdBy = createdBy; }

    public boolean isAvailable() { return this.stock > 0; }

    // ── Stock bar ─────────────────────────────────────────────────
    private String stockBar(int stock, int max) {
        int filled = (max == 0) ? 0 : Math.min(10, (int)((stock * 10.0) / max));
        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < 10; i++)
            bar.append(i < filled ? "█" : "░");
        bar.append("]");
        return bar.toString();
    }

    // ── toString ──────────────────────────────────────────────────
    @Override
    public String toString() {
        return String.format(
            "  %-8d | %-20s | %8.2f | Stock: %3d | %s",
            id, name, price, stock,
            isAvailable() ? "✅ Available" : "❌ Out of stock"
        );
    }

    // ── getProductInfo ────────────────────────────────────────────
    public String getProductInfo() {
        return String.format(
            "📦 Name: %s\n" +
            "📝 Description: %s\n" +
            "💰 Price: $%.2f\n" +
            "🏷️  Stock: %d\n" +
            "🗂️  Category: %s\n" +
            "👤 Added by: %s",
            name        != null ? name        : "Not set",
            description != null ? description : "Not set",
            price,
            stock,
            category    != null ? category    : "Not set",
            createdBy   != null ? createdBy   : "Not set"
        );
    }

    // ── toDTO / fromDTO ───────────────────────────────────────────
    public String toDTO() {
        return String.format("%d|%s|%s|%.2f|%d|%s|%s",
            id,
            name        != null ? name        : "",
            description != null ? description : "",
            price,
            stock,
            category    != null ? category    : "",
            createdBy   != null ? createdBy   : ""
        );
    }

    public static product fromDTO(String dtoString) {
        if (dtoString == null || dtoString.trim().isEmpty()) return null;
        String[] parts = dtoString.split("\\|");
        if (parts.length < 7) return null;
        try {
            product p = new product();
            p.setId(parts[0].isEmpty() ? "0" : parts[0]);
            p.setName(parts[1].isEmpty() ? "unknown" : parts[1]);
            p.setDescription(parts[2].isEmpty() ? "" : parts[2]);
            p.setPrice(parts[3].isEmpty() ? 0.0 : Double.parseDouble(parts[3]));
            p.setStock(parts[4].isEmpty() ? 0 : Integer.parseInt(parts[4]));
            p.setCategory(parts[5].isEmpty() ? null : parts[5]);
            p.setCreatedBy(parts[6].isEmpty() ? null : parts[6]);
            return p;
        } catch (Exception e) {
            System.err.println("Error parsing product DTO: " + e.getMessage());
            return null;
        }
    }

    // ════════════════════════════════════════════════════════════════
    // ── updateStock(qty) — ADMIN updates stock
    // ════════════════════════════════════════════════════════════════
    public void updateStock(int qty) {
        System.out.println("\n" + LINE);
        System.out.println("║           STOCK UPDATE                       ║");
        System.out.println(MID);

        if (qty == 0) {
            System.out.println("║  ⚠️  Quantity 0 — no changes                  ║");
            System.out.println(LINE_END);
            return;
        }

        if (this.stock + qty < 0) {
            System.out.printf("║  ❌ Insufficient stock                        ║%n");
            System.out.printf("║     Current stock : %-25d║%n", this.stock);
            System.out.printf("║     Requested     : %-25d║%n", Math.abs(qty));
            System.out.printf("║     Missing       : %-25d║%n", Math.abs(this.stock + qty));
            System.out.println(LINE_END);
            throw new IllegalArgumentException(
                "Insufficient stock: " + this.stock + " available, " +
                Math.abs(qty) + " requested.");
        }

        String sql = "UPDATE products SET stock = stock + ? WHERE id = ?";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, qty);
            ps.setString(2, this.id);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                int oldStock = this.stock;
                this.stock += qty;
                String type = qty > 0 ? "📦 Restock" : "🛒 Sale / Removal";
                System.out.printf("║  %-44s║%n", type);
                System.out.printf("║  Product  : %-31s║%n", this.name);
                System.out.printf("║  Before   : %-31d║%n", oldStock);
                System.out.printf("║  Change   : %-31s║%n", (qty > 0 ? "+" : "") + qty);
                System.out.printf("║  After    : %-31d║%n", this.stock);
                System.out.printf("║  Status   : %-31s║%n",
                    isAvailable() ? "✅ In stock" : "❌ Out of stock");
                System.out.println(LINE_END);
            } else {
                System.out.println("║  ❌ Product not found (id=" + id + ")");
                System.out.println(LINE_END);
            }
        } catch (SQLException e) {
            System.out.println("║  ❌ SQL Error: " + e.getMessage());
            System.out.println(LINE_END);
        }
    }

    // ════════════════════════════════════════════════════════════════
    // ── checkStock() — check if product is available
    // ════════════════════════════════════════════════════════════════
    public boolean checkStock() {
        System.out.println("\n" + LINE);
        System.out.println("║           STOCK CHECK                        ║");
        System.out.println(MID);

        String sql = "SELECT stock FROM products WHERE id = ?";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, this.id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int stockDB = rs.getInt("stock");
                this.stock = stockDB;
                boolean available = stockDB > 0;
                System.out.printf("║  Product  : %-31s║%n",
                    this.name != null ? this.name : "id=" + id);
                System.out.printf("║  Stock DB : %-31d║%n", stockDB);
                System.out.printf("║  Bar      : %-31s║%n", stockBar(stockDB, 30));
                System.out.printf("║  Status   : %-31s║%n",
                    available ? "✅ Available" : "❌ Out of stock");
                System.out.println(LINE_END);
                return available;
            } else {
                System.out.println("║  ❌ Product not found (id=" + id + ")");
                System.out.println(LINE_END);
            }
        } catch (SQLException e) {
            System.out.println("║  ❌ SQL Error: " + e.getMessage());
            System.out.println(LINE_END);
        }
        return false;
    }

    // ════════════════════════════════════════════════════════════════
    // ── fetchPrice() — fetch price from DB
    // ════════════════════════════════════════════════════════════════
    public double fetchPrice() {
        System.out.println("\n" + LINE);
        System.out.println("║           PRICE CONSULTATION                 ║");
        System.out.println(MID);

        String sql = "SELECT name, price FROM products WHERE id = ?";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, this.id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                this.name  = rs.getString("name");
                this.price = rs.getDouble("price");
                System.out.printf("║  Product : %-34s║%n", this.name);
                System.out.printf("║  Price   : %-31.2f MAD║%n", this.price);
                System.out.println(LINE_END);
                return this.price;
            } else {
                System.out.println("║  ❌ Product not found (id=" + id + ")");
                System.out.println(LINE_END);
            }
        } catch (SQLException e) {
            System.out.println("║  ❌ SQL Error: " + e.getMessage());
            System.out.println(LINE_END);
        }
        return -1.0;
    }
}