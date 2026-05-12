package server.handlers;

import database.dao.orderDAO;
import database.dao.paymentDAO;
import database.dao.userDao;
import model.*;
import protocol.request;
import protocol.response;
import security.storage.AuditLogger;
import security.storage.ReplayProtector;
import security.storage.SecureDataStore;
import server.sessionManager;

public class paymentHandler {
    private final paymentDAO paymentDAO = new paymentDAO();
    private final orderDAO orderDAO = new orderDAO();
    private final userDao userDAO = new userDao();
    private final sessionManager sessionMgr = sessionManager.getInstance();
    private final AuditLogger auditLogger = new AuditLogger();
    private final SecureDataStore secureStore = new SecureDataStore();
    private final ReplayProtector replayProtector = new ReplayProtector();

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
            auditLogger.logAction("anonymous", "PAYMENT_FAILED", "Authentication required");
            return new response(false, "Not authenticated");
        }

        String orderId = request.getParam("orderId");
        String methodStr = request.getParam("method");
        String txId = request.getParam("txId"); // Transaction ID for replay protection

        if (orderId == null || methodStr == null) {
            auditLogger.logAction(userId, "PAYMENT_FAILED", "Missing orderId or payment method");
            return new response(false, "Order ID and payment method are required");
        }

        // 🔒 Replay Protection: Check if this transaction was already processed
        if (txId != null && !txId.isEmpty()) {
            if (replayProtector.isReplay(txId)) {
                auditLogger.logAction(userId, "PAYMENT_REPLAY_BLOCKED", "Duplicate transaction: " + txId);
                return new response(false, "Duplicate transaction detected. Payment already processed.");
            }
            replayProtector.registerTransaction(txId);
        }

        try {
            // Verify order exists and belongs to user
            order ord = orderDAO.getOrderById(orderId);
            if (ord == null) {
                auditLogger.logAction(userId, "PAYMENT_FAILED", "Order not found: " + orderId);
                return new response(false, "Order not found");
            }

            if (!ord.getUserId().equals(userId)) {
                auditLogger.logAction(userId, "PAYMENT_ACCESS_DENIED", "Attempted to pay for order: " + orderId);
                return new response(false, "Access denied");
            }

            // Parse payment method
            PaymentMethod method = PaymentMethod.valueOf(methodStr.toUpperCase());

            // Get username for audit log
            user userObj = userDAO.getUserById(userId);
            String username = userObj != null ? userObj.getUsername() : userId;

            // Create and process payment
            payment pay = new payment();
            boolean success = pay.processPayment(orderId, ord.getTotalAmount(), method);

            if (success) {
                // Save payment to database
                paymentDAO.createPayment(pay);

                // Update order status to CONFIRMED
                orderDAO.updateOrderStatus(orderId, OrderStatus.CONFIRMED);

                // 🔒 Store encrypted payment data
                String paymentKey = "payment:" + pay.getPaymentId();
                String sensitiveData = String.format("PaymentID=%s|OrderID=%s|Amount=%.2f|Method=%s|Status=%s",
                    pay.getPaymentId(), orderId, ord.getTotalAmount(), method, pay.getStatus());
                secureStore.saveEncrypted(paymentKey, sensitiveData);

                // 🔒 Audit log successful payment
                auditLogger.logAction(username, "PAYMENT_SUCCESS", 
                    String.format("Payment %s processed: $%.2f via %s for order %s", 
                        pay.getPaymentId(), ord.getTotalAmount(), method, orderId));

                System.out.println("✅ Payment processed and logged: " + pay.getPaymentId());
                return new response(true, "Payment processed successfully|" + pay.getPaymentId());
            } else {
                // Save failed payment
                paymentDAO.createPayment(pay);
                
                // 🔒 Audit log failed payment
                auditLogger.logAction(username, "PAYMENT_FAILED", 
                    String.format("Payment failed for order %s: $%.2f via %s", 
                        orderId, ord.getTotalAmount(), method));
                
                return new response(false, "Payment failed. Please try again.");
            }

        } catch (IllegalArgumentException e) {
            auditLogger.logAction(userId, "PAYMENT_FAILED", "Invalid payment method: " + methodStr);
            return new response(false, "Invalid payment method. Use: CREDIT_CARD, DEBIT_CARD, PAYPAL, BANK_TRANSFER, or CASH");
        } catch (Exception e) {
            System.err.println("❌ Process payment error: " + e.getMessage());
            auditLogger.logAction(userId, "PAYMENT_ERROR", "Exception: " + e.getMessage());
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
                auditLogger.logAction(userId, "PAYMENT_ACCESS_DENIED", "Attempted to view payment: " + paymentId);
                return new response(false, "Access denied");
            }

            // 🔒 Audit log payment view
            user userObj = userDAO.getUserById(userId);
            String username = userObj != null ? userObj.getUsername() : userId;
            auditLogger.logAction(username, "PAYMENT_VIEWED", "Viewed payment: " + paymentId);

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
                auditLogger.logAction(userId, "REFUND_ACCESS_DENIED", "Attempted to refund payment: " + paymentId);
                return new response(false, "Access denied");
            }

            // Get username for audit log
            user userObj = userDAO.getUserById(userId);
            String username = userObj != null ? userObj.getUsername() : userId;

            // Process refund
            if (pay.refund(paymentId)) {
                paymentDAO.updatePaymentStatus(paymentId, PaymentStatus.REFUNDED);
                orderDAO.updateOrderStatus(pay.getOrderId(), OrderStatus.CANCELLED);
                
                // 🔒 Update encrypted payment data with refund status
                String paymentKey = "payment:" + paymentId;
                String refundData = String.format("PaymentID=%s|OrderID=%s|Amount=%.2f|Status=REFUNDED",
                    paymentId, pay.getOrderId(), pay.getAmount());
                secureStore.saveEncrypted(paymentKey, refundData);

                // 🔒 Audit log refund
                auditLogger.logAction(username, "REFUND_SUCCESS", 
                    String.format("Refunded payment %s: $%.2f", paymentId, pay.getAmount()));
                
                return new response(true, "Payment refunded successfully");
            } else {
                auditLogger.logAction(username, "REFUND_FAILED", "Refund failed for payment: " + paymentId);
                return new response(false, "Refund failed. Payment may not be eligible for refund.");
            }

        } catch (Exception e) {
            System.err.println("❌ Refund payment error: " + e.getMessage());
            auditLogger.logAction(userId, "REFUND_ERROR", "Exception: " + e.getMessage());
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
                auditLogger.logAction(userId, "RECEIPT_ACCESS_DENIED", "Attempted to get receipt: " + paymentId);
                return new response(false, "Access denied");
            }

            // 🔒 Audit log receipt access
            user userObj = userDAO.getUserById(userId);
            String username = userObj != null ? userObj.getUsername() : userId;
            auditLogger.logAction(username, "RECEIPT_ACCESSED", "Accessed receipt for payment: " + paymentId);

            return new response(true, pay.getReceipt());
        } catch (Exception e) {
            System.err.println("❌ Get receipt error: " + e.getMessage());
            return new response(false, "Server error while generating receipt");
        }
    }
}
