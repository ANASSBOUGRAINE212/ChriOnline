package model;

public class orderItem {

    private String itemId;
    private product product;   
    private int quantity;
    private double unitPrice;

    // ── Constructors ──────────────────────

    public orderItem() {}

    public orderItem(String itemId, product product, int quantity, double unitPrice) {
        this.itemId    = itemId;
        this.product   = product;
        this.quantity  = quantity;
        this.unitPrice = unitPrice;
    }

    // ── Getters ───────────────────────────

    public String getItemId()    { return itemId; }
    public product getProduct()   { return product; }
    public int    getQuantity()  { return quantity; }
    public double getUnitPrice() { return unitPrice; }

    // ── Setters ───────────────────────────

    public void setItemId(String itemId)       { this.itemId    = itemId; }
    public void setProduct(product product)     { this.product   = product; }
    public void setQuantity(int quantity)      { this.quantity  = quantity; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    // ── Methods from UML ───────────────────────────

    public double getSubtotal()           { 
        return quantity * unitPrice; 
    }
    
    public void updateQuantity(int qty) { 
        if (qty > 0) {
            this.quantity = qty; 
        }
    }
}