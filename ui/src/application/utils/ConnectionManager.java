package application.utils;

import client.clientConnection;
import protocol.response;

/**
 * Singleton wrapper for client connection
 */
public class ConnectionManager {
    
    private static ConnectionManager instance;
    private clientConnection connection;
    private String sessionToken;
    private String userRole;
    
    private ConnectionManager() {
        try {
            System.out.println("🔌 Initializing ConnectionManager...");
            // Connect to server
            connection = new clientConnection("localhost", 5000);
            System.out.println("✅ Connected to server successfully!");
        } catch (Exception e) {
            System.err.println("❌ Failed to connect to server: " + e.getMessage());
            System.err.println("⚠️  Make sure the server is running on localhost:5000");
            e.printStackTrace();
        }
    }
    
    public static ConnectionManager getInstance() {
        if (instance == null) {
            instance = new ConnectionManager();
        }
        return instance;
    }
    
    // Auth methods
    public response login(String email, String password) {
        System.out.println("🔐 Attempting login for: " + email);
        response res = connection.login(email, password);
        if (res.isSuccess()) {
            System.out.println("✅ Login successful!");
            String[] parts = res.getMessage().split("\\|");
            if (parts.length > 1) {
                setSessionToken(parts[1]);
                System.out.println("🎫 Session token set");
            }
            if (parts.length > 2) {
                setUserRole(parts[2]);
                System.out.println("👤 User role: " + parts[2]);
            }
        } else {
            System.err.println("❌ Login failed: " + res.getMessage());
        }
        return res;
    }
    
    public response register(String username, String email, String password, String address, String phone) {
        return connection.register(username, email, password, address, phone);
    }
    
    public void logout() {
        if (connection != null) {
            connection.logout();
        }
        sessionToken = null;
        userRole = null;
    }
    
    // Product methods
    public response listProducts() {
        System.out.println("📦 Fetching product list...");
        response res = connection.listProducts();
        if (res.isSuccess()) {
            System.out.println("✅ Products loaded successfully");
        } else {
            System.err.println("❌ Failed to load products: " + res.getMessage());
        }
        return res;
    }
    
    public response getProduct(int productId) {
        return connection.getProduct(productId);
    }
    
    // Cart methods
    public response addToCart(int productId, int quantity, double price) {
        return connection.addToCart(productId, quantity, price);
    }
    
    public response getCartItems() {
        return connection.getCartItems();
    }
    
    public response removeFromCart(int productId) {
        return connection.removeFromCart(productId);
    }
    
    public response getCartTotal() {
        return connection.getCartTotal();
    }
    
    // Order methods
    public response createOrder() {
        return connection.createOrder();
    }
    
    public response listOrders() {
        return connection.listOrders();
    }
    
    public response getOrder(String orderId) {
        return connection.getOrder(orderId);
    }
    
    public response cancelOrder(String orderId) {
        return connection.cancelOrder(orderId);
    }
    
    // Payment methods
    public response processPayment(String orderId, String method) {
        return connection.processPayment(orderId, method);
    }
    
    public response getReceipt(String paymentId) {
        return connection.getReceipt(paymentId);
    }
    
    // Getters/Setters
    public void setSessionToken(String token) {
        this.sessionToken = token;
        if (connection != null) {
            connection.setSessionToken(token);
        }
    }
    
    public void setUserRole(String role) {
        this.userRole = role;
        if (connection != null) {
            connection.setUserRole(role);
        }
    }
    
    public String getUserRole() {
        return userRole;
    }
    
    public String getSessionToken() {
        return sessionToken;
    }
    
    public boolean isConnected() {
        return connection != null;
    }
}
