package model;

public class cartItem {
    private String itemId;
    private product product;
    private int quantity;
    private double unitPrice;

    public cartItem() {}

    public cartItem(String itemId, product product, int quantity, double unitPrice) {
        this.itemId = itemId;
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public product getProduct() { return product; }
    public void setProduct(product product) { this.product = product; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public double getSubtotal() {
        return quantity * unitPrice;
    }

    @Override
    public String toString() {
        String productName = product != null ? product.getName() : "Unknown";
        return String.format("%s x%d @ $%.2f = $%.2f", 
            productName, quantity, unitPrice, getSubtotal());
    }
}
