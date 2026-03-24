package model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class order {

    private String          orderId;
    private String          userId;
    private List<orderItem> items;
    private double          totalAmount;
    private OrderStatus     status;
    private Date            createdAt;
    private payment         payment;

    // ── Constructors ──────────────────────

    public order() {
        this.status = OrderStatus.PENDING; // Default status
        this.items = new ArrayList<>();
    }

    public order(String orderId, String userId, List<orderItem> items,
                 double totalAmount, OrderStatus status, Date createdAt, payment payment) {
        this.orderId     = orderId;
        this.userId      = userId;
        this.items       = items != null ? items : new ArrayList<>();
        this.totalAmount = totalAmount;
        this.status      = status != null ? status : OrderStatus.PENDING;
        this.createdAt   = createdAt;
        this.payment     = payment;
    }

    // ── Getters ───────────────────────────

    public String          getOrderId()     { return orderId; }
    public String          getUserId()      { return userId; }
    public List<orderItem> getItems()       { return items; }
    public double          getTotalAmount() { return totalAmount; }
    public OrderStatus     getStatus()      { return status; }
    public Date            getCreatedAt()   { return createdAt; }
    public payment         getPayment()     { return payment; }

    // ── Setters ───────────────────────────

    public void setOrderId(String orderId)         { this.orderId     = orderId; }
    public void setUserId(String userId)           { this.userId      = userId; }
    public void setItems(List<orderItem> items)    { this.items       = items; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public void setStatus(OrderStatus status)      { this.status      = status; }
    public void setCreatedAt(Date createdAt)       { this.createdAt   = createdAt; }
    public void setPayment(payment payment)        { this.payment     = payment; }

    // ── Methods from UML ───────────────────────────

    public boolean validate() {
        if (orderId == null || orderId.trim().isEmpty()) return false;
        if (userId == null || userId.trim().isEmpty()) return false;
        if (items == null || items.isEmpty()) return false;
        if (totalAmount <= 0) return false;
        return true;
    }

    public String generateId() {
        this.orderId = "ORD-" + UUID.randomUUID().toString();
        return this.orderId;
    }

    public void updateStatus(OrderStatus s) {
        this.status = s;
    }

    public List<order> getHistory() {
        // This would typically be implemented in DAO
        return new ArrayList<>();
    }

    public void cancel() {
        this.status = OrderStatus.CANCELLED;
    }

    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Order ID: ").append(orderId).append("\n");
        sb.append("User ID: ").append(userId).append("\n");
        sb.append("Status: ").append(status).append("\n");
        sb.append("Total Amount: $").append(String.format("%.2f", totalAmount)).append("\n");
        sb.append("Items: ").append(items != null ? items.size() : 0).append("\n");
        sb.append("Created: ").append(createdAt).append("\n");
        return sb.toString();
    }

    public boolean hasErrors() {
        return !validate();
    }

    public String generateReceipt() {
        StringBuilder receipt = new StringBuilder();
        receipt.append("═══════════════════════════════════════\n");
        receipt.append("           ORDER RECEIPT\n");
        receipt.append("═══════════════════════════════════════\n");
        receipt.append("Order ID: ").append(orderId).append("\n");
        receipt.append("Date: ").append(createdAt).append("\n");
        receipt.append("Status: ").append(status).append("\n");
        receipt.append("───────────────────────────────────────\n");
        receipt.append("ITEMS:\n");
        if (items != null) {
            for (orderItem item : items) {
                receipt.append(String.format("  %s x%d @ $%.2f = $%.2f\n",
                    item.getProduct() != null ? item.getProduct().getName() : "Unknown",
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getSubtotal()));
            }
        }
        receipt.append("───────────────────────────────────────\n");
        receipt.append(String.format("TOTAL: $%.2f\n", totalAmount));
        receipt.append("═══════════════════════════════════════\n");
        return receipt.toString();
    }
}