package model;

import java.util.Date;
import java.util.List;

enum OrderStatus {
    PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED
}

public class order {

    private String          orderId;
    private String          userId;
    private List<orderItem> items;
    private double          totalAmount;
    private OrderStatus     status;
    private Date            createdAt;
    private payment         payment;

    // ── Constructors ──────────────────────

    public order() {}

    public order(String orderId, String userId, List<orderItem> items,
                 double totalAmount, OrderStatus status, Date createdAt, payment payment) {
        this.orderId     = orderId;
        this.userId      = userId;
        this.items       = items;
        this.totalAmount = totalAmount;
        this.status      = status;
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

    // ── Methods ───────────────────────────

    public boolean     validate()                  { return false; }
    public String      generateId()                { return ""; }
    public void        updateStatus(OrderStatus s) { this.status = s; }
    public List<order> getHistory()                { return null; }
    public void        cancel()                    {}
    public String      getSummary()                { return ""; }
    public boolean     hasErrors()                 { return false; }
    public String      generateReceipt()           { return ""; }
}