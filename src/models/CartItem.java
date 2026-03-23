package models;

public class CartItem {

    // ── Attributs (exactement selon le diagramme UML) ─────────────
    private String  itemId;      // private
    private Product product;     // private  — référence à Product (UML)
    private int     quantity;    // private
    private double  unitPrice;   // private

    // ── Constructeurs ─────────────────────────────────────────────

    public CartItem() {}

    // Constructeur complet selon UML
    public CartItem(String itemId, Product product, int quantity, double unitPrice) {
        this.itemId    = itemId;
        this.product   = product;
        this.quantity  = quantity;
        this.unitPrice = unitPrice;
    }

    // ── Getters / Setters ─────────────────────────────────────────

    public String getItemId()                  { return itemId; }
    public void setItemId(String itemId)       { this.itemId = itemId; }

    public Product getProduct()                { return product; }
    public void setProduct(Product product)    { this.product = product; }

    public int getQuantity()                   { return quantity; }
    public void setQuantity(int quantity)      { this.quantity = quantity; }

    public double getUnitPrice()               { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    // ════════════════════════════════════════════════════════════════
    // + getSubtotal(): double                        [UML]
    // ════════════════════════════════════════════════════════════════
    public double getSubtotal() {
        return this.unitPrice * this.quantity;
    }

    // ════════════════════════════════════════════════════════════════
    // + updateQuantity(qty: int): void               [UML]
    // ════════════════════════════════════════════════════════════════
    public void updateQuantity(int qty) {
        if (qty <= 0) {
            System.out.println("❌ Quantity must be greater than 0.");
            return;
        }
        this.quantity = qty;
        System.out.println("✅ Quantity updated to " + qty);
    }

    // ════════════════════════════════════════════════════════════════
    // + getItemDetails(): String                     [UML]
    // ════════════════════════════════════════════════════════════════
    public String getItemDetails() {
        String productName = (product != null) ? product.getName() : "Unknown";
        String productId   = (product != null) ? product.getProductId() : "N/A";
        return String.format(
            "🛒 Product   : %s\n" +
            "🔑 ProductId : %s\n" +
            "🔢 Quantity  : %d\n" +
            "💰 Unit Price: %.2f MAD\n" +
            "💵 Subtotal  : %.2f MAD",
            productName,
            productId,
            quantity,
            unitPrice,
            getSubtotal()
        );
    }
}
