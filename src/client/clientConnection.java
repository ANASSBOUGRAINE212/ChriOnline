package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import protocol.request;
import protocol.response;

public class clientConnection {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String sessionToken;
    private String userRole; // Store user role after login

    public clientConnection(String host, int port) {
        System.out.println("🔌 Connecting to " + host + ":" + port);
        try {
            this.socket = new Socket(host, port);
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());
            this.sessionToken = null; // No token initially
            System.out.println("✅ Connected successfully!");
        } catch (IOException e) {
            System.out.println("❌ Connection failed: " + e.getMessage());
            throw new RuntimeException("Could not connect to server", e);
        }
    }
    
    private response sendRequest(request req) {
        try {
            System.out.println("📤 Sending request: " + req.getType());
            out.writeObject(req);
            out.flush();
            
            response serverResponse = (response) in.readObject();
            System.out.println("📥 Received response: " + (serverResponse.isSuccess() ? "SUCCESS" : "ERROR"));
            return serverResponse;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("❌ Communication error: " + e.getMessage());
            return new response(false, "Connection error: " + e.getMessage());
        }
    }
    
    public response login(String username, String password) {
        request req = new request(request.LOGIN);
        req.setParam("username", username);
        req.setParam("password", password);
        return sendRequest(req);
    }
    
    // Main register method with all parameters
    public response register(String username, String email, String password, String address, String phone) {
        request req = new request(request.REGISTER);
        req.setParam("username", username);
        req.setParam("email", email);
        req.setParam("password", password);
        req.setParam("address", address != null ? address : "");
        req.setParam("phone", phone != null ? phone : "");
        return sendRequest(req);
    }
    
    public response logout() {
        request req = new request(request.LOGOUT);
        req.setToken(sessionToken);
        return sendRequest(req);
    }
    
    public void setSessionToken(String token) {
        this.sessionToken = token;
        System.out.println("🎫 Session token updated");
    }
    
    public void setUserRole(String role) {
        this.userRole = role;
    }
    
    public String getUserRole() {
        return this.userRole;
    }
    
    public boolean isAdmin() {
        return "ADMIN".equals(this.userRole);
    }
    
    public response getUserInfo() {
        request req = new request(request.GET_USER_INFO);
        req.setToken(sessionToken);
        return sendRequest(req);
    }
    
    public response getProfile() {
        request req = new request(request.GET_PROFILE);
        req.setToken(sessionToken);
        return sendRequest(req);
    }

    public response updateProfile(String address, String phone) {
        request req = new request(request.UPDATE_PROFILE);
        req.setToken(sessionToken);
        req.setParam("address", address);
        req.setParam("phone", phone);
        return sendRequest(req);
    }

    public response changePassword(String oldPassword, String newPassword) {
        request req = new request(request.CHANGE_PASSWORD);
        req.setToken(sessionToken);
        req.setParam("oldPassword", oldPassword);
        req.setParam("newPassword", newPassword);
        return sendRequest(req);
    }
    
    // Product functionality
    public response listProducts() {
        request req = new request(request.LIST_PRODUCTS);
        if (sessionToken != null) {
            req.setToken(sessionToken);
        }
        return sendRequest(req);
    }
    
    // Admin-only product update functionality
    public response updateProduct(String productId, String productName, String description, String price, String stock, String category) {
        request req = new request(request.UPDATE_PRODUCT);
        req.setToken(sessionToken);
        req.setParam("productId", productId);
        req.setParam("productName", productName);
        req.setParam("description", description);
        req.setParam("price", price);
        req.setParam("stock", stock);
        req.setParam("category", category);
        return sendRequest(req);
    }
    
    public void close() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
            System.out.println("🔌 Connection closed");
        } catch (IOException e) {
            System.out.println("⚠️ Error closing connection: " + e.getMessage());
        }
    }
}