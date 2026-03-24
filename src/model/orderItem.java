package model;

public class orderItem {

    private String itemId;
    private Object product;   
    private int quantity;
    private double unitPrice;

    // ── Constructors ──────────────────────

    public orderItem() {}

    public orderItem(String itemId, Object product, int quantity, double unitPrice) {
        this.itemId    = itemId;
        this.product   = product;
        this.quantity  = quantity;
        this.unitPrice = unitPrice;
    }

    // ── Getters ───────────────────────────

    public String getItemId()    { return itemId; }
    public Object getProduct()   { return product; }
    public int    getQuantity()  { return quantity; }
    public double getUnitPrice() { return unitPrice; }

    // ── Setters ───────────────────────────

    public void setItemId(String itemId)       { this.itemId    = itemId; }
    public void setProduct(Object product)     { this.product   = product; }
    public void setQuantity(int quantity)      { this.quantity  = quantity; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    // ── Methods ───────────────────────────

    public double getSubtotal()           { return quantity * unitPrice; }
    public void   updateQuantity(int qty) { this.quantity = qty; }
}