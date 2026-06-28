package server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Sends UDP notifications to clients for real-time updates.
 * Used for payment confirmations, order updates, etc.
 */
public class UDPNotificationSender {
    
    private static final int UDP_PORT = 5001;
    
    /**
     * Sends a UDP notification to a specific client.
     * 
     * @param clientAddress The client's IP address
     * @param message The notification message
     */
    public static void sendNotification(InetAddress clientAddress, String message) {
        try (DatagramSocket socket = new DatagramSocket()) {
            byte[] buffer = message.getBytes("UTF-8");
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, clientAddress, UDP_PORT);
            socket.send(packet);
            System.out.println("📢 UDP notification sent to " + clientAddress.getHostAddress() + ": " + message);
        } catch (Exception e) {
            System.err.println("❌ Failed to send UDP notification: " + e.getMessage());
        }
    }
    
    /**
     * Sends a payment confirmation notification.
     */
    public static void sendPaymentNotification(InetAddress clientAddress, String paymentId, double amount, String status) {
        String message = String.format("PAYMENT|%s|%.2f|%s", paymentId, amount, status);
        sendNotification(clientAddress, message);
    }
    
    /**
     * Sends an order update notification.
     */
    public static void sendOrderNotification(InetAddress clientAddress, String orderId, String status) {
        String message = String.format("ORDER|%s|%s", orderId, status);
        sendNotification(clientAddress, message);
    }
}
