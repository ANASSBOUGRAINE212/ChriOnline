package models;

import java.sql.Timestamp;

public class Payment {
    private int id;
    private int orderId;
    private String method;
    private String status;
    private Timestamp paidAt;

    public Payment() {}

    public Payment(int orderId, String method, String status) {
        this.orderId = orderId;
        this.method = method;
        this.status = status;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Timestamp getPaidAt() { return paidAt; }
    public void setPaidAt(Timestamp paidAt) { this.paidAt = paidAt; }
}
