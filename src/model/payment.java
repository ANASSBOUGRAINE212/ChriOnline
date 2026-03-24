package model;

import java.util.Date;

enum PaymentMethod {
    CREDIT_CARD, DEBIT_CARD, PAYPAL, BANK_TRANSFER, CASH
}

enum PaymentStatus {
    PENDING, COMPLETED, FAILED, REFUNDED, CANCELLED
}

public class payment {

    private String        paymentId;
    private String        orderId;
    private double        amount;
    private PaymentMethod method;
    private PaymentStatus status;
    private Date          timestamp;

    // ── Constructors ──────────────────────

    public payment() {}

    public payment(String paymentId, String orderId, double amount,
                   PaymentMethod method, PaymentStatus status, Date timestamp) {
        this.paymentId = paymentId;
        this.orderId   = orderId;
        this.amount    = amount;
        this.method    = method;
        this.status    = status;
        this.timestamp = timestamp;
    }

    // ── Getters ───────────────────────────

    public String        getPaymentId() { return paymentId; }
    public String        getOrderId()   { return orderId; }
    public double        getAmount()    { return amount; }
    public PaymentMethod getMethod()    { return method; }
    public PaymentStatus getStatus()    { return status; }
    public Date          getTimestamp() { return timestamp; }

    // ── Setters ───────────────────────────

    public void setPaymentId(String paymentId)       { this.paymentId = paymentId; }
    public void setOrderId(String orderId)           { this.orderId   = orderId; }
    public void setAmount(double amount)             { this.amount    = amount; }
    public void setMethod(PaymentMethod method)      { this.method    = method; }
    public void setStatus(PaymentStatus status)      { this.status    = status; }
    public void setTimestamp(Date timestamp)         { this.timestamp = timestamp; }

    // ── Methods ───────────────────────────

    public boolean       processPayment(String orderId, double amount, PaymentMethod method) { return false; }
    public PaymentStatus simulate()                                                           { return PaymentStatus.PENDING; }
    public boolean       refund(String paymentId)                                             { return false; }
    public String        getReceipt()                                                         { return ""; }
    public boolean       validateCard()                                                       { return false; }
}