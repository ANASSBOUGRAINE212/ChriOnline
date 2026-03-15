package models;

import java.sql.Timestamp;

public class Order {
    private int id;
    private String orderUid;
    private int userId;
    private double totalPrice;
    private String status;
    private Timestamp createdAt;

    public Order() {}

    public Order(String orderUid, int userId, double totalPrice, String status) {
        this.orderUid = orderUid;
        this.userId = userId;
        this.totalPrice = totalPrice;
        this.status = status;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getOrderUid() { return orderUid; }
    public void setOrderUid(String orderUid) { this.orderUid = orderUid; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
