package server.handlers;

import database.dao.paymentDAO;
import database.dao.orderDAO;
import model.*;
import protocol.request;
import protocol.response;
import server.sessionManager;

public class paymentHandler {
    private final paymentDAO paymentDAO = new paymentDAO();
    private final orderDAO orderDAO = new orderDAO();
    private final sessionManager sessionMgr = sessionManager.getInstance();

    public static response handle(request request) {
        paymentHandler handler = new paymentHandler();
        return handler.handleRequest(request);
    }

    private response handleRequest(request request) {
        String type = request.getType();
        System.out.println("💳 Payment handler processing: " + type);

        if (type.equals(request.PROCESS_PAYMENT))   return handleProcessPayment(request);
        if (type.equals(request.GET_PAYMENT))       return handleGetPayment(request);
        if (type.equals(request.REFUND_PAYMENT))    return handleRefundPayment(request);
        if (type.equals(request.GET_RECEIPT))       return handleGetReceipt(request);

        return new response(false, "Unknown payment command: " + type);
    }

    private response handleProcessPayment(request request) {
        String userId = sessionMgr.getUserIdFromToken(request.getToken());
        if (userId == null) {
            return new response(false, "Not authenticated");
        }

        String orderId = request.getParam("orderId");
        String methodStr = request.getParam("method");

        if (orderId == null || methodStr == null) {
            return new response(false, "Order ID and payment method are required");
        }

        try {
            // Verify order exists and belongs to user
            order ord = orderDAO.getOrderById(orderId);
            if (ord == null) {
                return new response(false, "Order not found");
            }

            if (!ord.getUserId().equals(userId)) {
                return new response(false, "Access denied");
            }

            // Parse payment method
            PaymentMethod method = PaymentMethod.valueOf(methodStr.toUpperCase());

            // Create and process payment
            payment pay = new payment();
            boolean success = pay.processPayment(orderId, ord.getTotalAmount(), method);

            if (success) {
                // Save payment to database
                paymentDAO.createPayment(pay);

                // Update order status to CONFIRMED
                orderDAO.updateOrderStatus(orderId, OrderStatus.CONFIRMED);

                return new response(true, "Payment processed successfully|" + pay.getPaymentId());
            } else {
                // Save failed payment
                paymentDAO.createPayment(pay);
                
                return new response(false, "Payment failed. Please try again.");
            }

        } catch (IllegalArgumentException e) {
            return new response(false, "Invalid payment method. Use: CREDIT_CARD, DEBIT_CARD, PAYPAL, BANK_TRANSFER, or CASH");
        } catch (Exception e) {
            System.err.println("❌ Process payment error: " + e.getMessage());
            return new response(false, "Server error while processing payment");
        }
    }

    private response handleGetPayment(request request) {
        String userId = sessionMgr.getUserIdFromToken(request.getToken());
        if (userId == null) {
            return new response(false, "Not authenticated");
        }

        String paymentId = request.getParam("paymentId");
        if (paymentId == null || paymentId.trim().isEmpty()) {
            return new response(false, "Payment ID is required");
        }

        try {
            payment pay = paymentDAO.getPaymentById(paymentId);
            if (pay == null) {
                return new response(false, "Payment not found");
            }

            // Verify user owns the order associated with this payment
            order ord = orderDAO.getOrderById(pay.getOrderId());
            if (ord == null || !ord.getUserId().equals(userId)) {
                return new response(false, "Access denied");
            }

            StringBuilder sb = new StringBuilder();
            sb.append("Payment ID: ").append(pay.getPaymentId()).append("\n");
            sb.append("Order ID: ").append(pay.getOrderId()).append("\n");
            sb.append("Amount: $").append(String.format("%.2f", pay.getAmount())).append("\n");
            sb.append("Method: ").append(pay.getMethod()).append("\n");
            sb.append("Status: ").append(pay.getStatus()).append("\n");
            sb.append("Date: ").append(pay.getTimestamp());

            return new response(true, sb.toString());
        } catch (Exception e) {
            System.err.println("❌ Get payment error: " + e.getMessage());
            return new response(false, "Server error while retrieving payment");
        }
    }

    private response handleRefundPayment(request request) {
        String userId = sessionMgr.getUserIdFromToken(request.getToken());
        if (userId == null) {
            return new response(false, "Not authenticated");
        }

        String paymentId = request.getParam("paymentId");
        if (paymentId == null || paymentId.trim().isEmpty()) {
            return new response(false, "Payment ID is required");
        }

        try {
            payment pay = paymentDAO.getPaymentById(paymentId);
            if (pay == null) {
                return new response(false, "Payment not found");
            }

            // Verify user owns the order
            order ord = orderDAO.getOrderById(pay.getOrderId());
            if (ord == null || !ord.getUserId().equals(userId)) {
                return new response(false, "Access denied");
            }

            // Process refund
            if (pay.refund(paymentId)) {
                paymentDAO.updatePaymentStatus(paymentId, PaymentStatus.REFUNDED);
                orderDAO.updateOrderStatus(pay.getOrderId(), OrderStatus.CANCELLED);
                
                return new response(true, "Payment refunded successfully");
            } else {
                return new response(false, "Refund failed. Payment may not be eligible for refund.");
            }

        } catch (Exception e) {
            System.err.println("❌ Refund payment error: " + e.getMessage());
            return new response(false, "Server error while processing refund");
        }
    }

    private response handleGetReceipt(request request) {
        String userId = sessionMgr.getUserIdFromToken(request.getToken());
        if (userId == null) {
            return new response(false, "Not authenticated");
        }

        String paymentId = request.getParam("paymentId");
        if (paymentId == null || paymentId.trim().isEmpty()) {
            return new response(false, "Payment ID is required");
        }

        try {
            payment pay = paymentDAO.getPaymentById(paymentId);
            if (pay == null) {
                return new response(false, "Payment not found");
            }

            // Verify user owns the order
            order ord = orderDAO.getOrderById(pay.getOrderId());
            if (ord == null || !ord.getUserId().equals(userId)) {
                return new response(false, "Access denied");
            }

            return new response(true, pay.getReceipt());
        } catch (Exception e) {
            System.err.println("❌ Get receipt error: " + e.getMessage());
            return new response(false, "Server error while generating receipt");
        }
    }
}
