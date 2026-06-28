package client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import javafx.application.Platform;

/**
 * Listens for UDP notifications from the server.
 * Runs in a background thread to receive real-time updates.
 */
public class udpNotificationListener extends Thread {
    
    private static final int UDP_PORT = 5001;
    private DatagramSocket socket;
    private volatile boolean running = true;
    private NotificationHandler handler;
    
    public interface NotificationHandler {
        void onPaymentNotification(String paymentId, double amount, String status);
        void onOrderNotification(String orderId, String status);
        void onGenericNotification(String message);
    }
    
    public udpNotificationListener(NotificationHandler handler) {
        this.handler = handler;
        setDaemon(true); // Thread will stop when main app stops
        setName("UDP-Notification-Listener");
    }
    
    @Override
    public void run() {
        try {
            socket = new DatagramSocket(UDP_PORT);
            System.out.println("📡 UDP notification listener started on port " + UDP_PORT);
            
            byte[] buffer = new byte[1024];
            
            while (running) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                
                String message = new String(packet.getData(), 0, packet.getLength(), "UTF-8");
                System.out.println("📬 UDP notification received: " + message);
                
                // Process notification on JavaFX thread
                Platform.runLater(() -> processNotification(message));
            }
            
        } catch (Exception e) {
            if (running) {
                System.err.println("❌ UDP listener error: " + e.getMessage());
            }
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }
    
    private void processNotification(String message) {
        try {
            String[] parts = message.split("\\|");
            
            if (parts.length == 0) {
                handler.onGenericNotification(message);
                return;
            }
            
            String type = parts[0];
            
            switch (type) {
                case "PAYMENT":
                    if (parts.length >= 4) {
                        String paymentId = parts[1];
                        double amount = Double.parseDouble(parts[2]);
                        String status = parts[3];
                        handler.onPaymentNotification(paymentId, amount, status);
                    }
                    break;
                    
                case "ORDER":
                    if (parts.length >= 3) {
                        String orderId = parts[1];
                        String status = parts[2];
                        handler.onOrderNotification(orderId, status);
                    }
                    break;
                    
                default:
                    handler.onGenericNotification(message);
                    break;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error processing notification: " + e.getMessage());
            handler.onGenericNotification(message);
        }
    }
    
    public void stopListening() {
        running = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}
