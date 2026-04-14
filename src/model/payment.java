package model;

import java.util.Date;
import java.util.Random;
import java.util.UUID;
import model.PaymentMethod;
import model.PaymentStatus;

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

    // ── Methods from UML ───────────────────────────

    public boolean processPayment(String orderId, double amount, PaymentMethod method) {
        this.orderId = orderId;
        this.amount = amount;
        this.method = method;
        this.paymentId = "PAY-" + UUID.randomUUID().toString();
        this.timestamp = new Date();
        
        // Simulate payment processing
        this.status = simulate();
        
        return this.status == PaymentStatus.COMPLETED;
    }

    public PaymentStatus simulate() {
        // Simulate payment processing with 90% success rate
        Random random = new Random();
        int result = random.nextInt(100);
        
        if (result < 90) {
            return PaymentStatus.COMPLETED;
        } else {
            return PaymentStatus.FAILED;
        }
    }

    public boolean refund(String paymentId) {
        if (this.paymentId != null && this.paymentId.equals(paymentId)) {
            if (this.status == PaymentStatus.COMPLETED) {
                this.status = PaymentStatus.REFUNDED;
                return true;
            }
        }
        return false;
    }

    public String getReceipt() {
        StringBuilder receipt = new StringBuilder();
        receipt.append("═══════════════════════════════════════\n");
        receipt.append("         PAYMENT RECEIPT\n");
        receipt.append("═══════════════════════════════════════\n");
        receipt.append("Payment ID: ").append(paymentId).append("\n");
        receipt.append("Order ID: ").append(orderId).append("\n");
        receipt.append("Amount: $").append(String.format("%.2f", amount)).append("\n");
        receipt.append("Method: ").append(method).append("\n");
        receipt.append("Status: ").append(status).append("\n");
        receipt.append("Date: ").append(timestamp).append("\n");
        receipt.append("═══════════════════════════════════════\n");
        return receipt.toString();
    }

    public boolean validateCard() {
        // Simple validation for credit/debit cards
        if (method == PaymentMethod.CREDIT_CARD || method == PaymentMethod.DEBIT_CARD) {
            // In real implementation, would validate card number, CVV, expiry
            return true;
        }
        return true; // Other payment methods don't need card validation
    }
}