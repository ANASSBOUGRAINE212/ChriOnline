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
    private String userRole;

    public clientConnection(String host, int port) {
        System.out.println("🔌 Connecting to " + host + ":" + port);
        try {
            this.socket = new Socket(host, port);
            this.out    = new ObjectOutputStream(socket.getOutputStream());
            this.in     = new ObjectInputStream(socket.getInputStream());
            this.sessionToken = null;
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

    // ── Auth ─────────────────────────────────────────────────────
    public response login(String email, String password) {
        request req = new request(request.LOGIN);
        req.setParam("email", email);
        req.setParam("password", password);
        return sendRequest(req);
    }

    public response register(String username, String email, String password, String address, String phone) {
        request req = new request(request.REGISTER);
        req.setParam("username", username);
        req.setParam("email", email);
        req.setParam("password", password);
        req.setParam("address", address != null ? address : "");
        req.setParam("phone",   phone   != null ? phone   : "");
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

    public void setUserRole(String role)  { this.userRole = role; }
    public String getUserRole()           { return this.userRole; }
    public boolean isAdmin()              { return "ADMIN".equals(this.userRole); }

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
        req.setParam("phone",   phone);
        return sendRequest(req);
    }

    public response changePassword(String oldPassword, String newPassword) {
        request req = new request(request.CHANGE_PASSWORD);
        req.setToken(sessionToken);
        req.setParam("oldPassword", oldPassword);
        req.setParam("newPassword", newPassword);
        return sendRequest(req);
    }

    // ── Products ─────────────────────────────────────────────────
    public response addProduct(String name, String description, double price, int stock, String category) {
        request req = new request(request.ADD_PRODUCT);
        req.setToken(sessionToken);
        req.setParam("name",        name);
        req.setParam("description", description);
        req.setParam("price",       String.valueOf(price));
        req.setParam("stock",       String.valueOf(stock));
        req.setParam("category",    category);
        return sendRequest(req);
    }

    public response deleteProduct(int productId) {
        request req = new request(request.DELETE_PRODUCT);
        req.setToken(sessionToken);
        req.setParam("productId", String.valueOf(productId));
        return sendRequest(req);
    }

    public response getProduct(int productId) {
        request req = new request(request.GET_PRODUCT);
        req.setToken(sessionToken);
        req.setParam("productId", String.valueOf(productId));
        return sendRequest(req);
    }

    public response listProducts() {
        request req = new request(request.LIST_PRODUCTS);
        if (sessionToken != null) req.setToken(sessionToken);
        return sendRequest(req);
    }

    public response updateProduct(int productId, String name, String description,
                                   String price, String stock, String category) {
        request req = new request(request.UPDATE_PRODUCT);
        req.setToken(sessionToken);
        req.setParam("productId",   String.valueOf(productId));
        req.setParam("productName", name);
        req.setParam("description", description);
        req.setParam("price",       price);
        req.setParam("stock",       stock);
        req.setParam("category",    category);
        return sendRequest(req);
    }

    // ── Cart ─────────────────────────────────────────────────────
    public response addToCart(int productId, int quantity, double price) {
        request req = new request(request.ADD_TO_CART);
        req.setToken(sessionToken);
        req.setParam("productId", String.valueOf(productId));
        req.setParam("quantity", String.valueOf(quantity));
        req.setParam("price", String.valueOf(price));
        return sendRequest(req);
    }

    public response getCart() {
        request req = new request(request.GET_CART_ITEMS);
        req.setToken(sessionToken);
        return sendRequest(req);
    }

    public response getCartItems() {
        return getCart();
    }

    public response removeFromCart(int productId) {
        request req = new request(request.REMOVE_FROM_CART);
        req.setToken(sessionToken);
        req.setParam("productId", String.valueOf(productId));
        return sendRequest(req);
    }

    public response getCartTotal() {
        request req = new request(request.GET_CART_TOTAL);
        req.setToken(sessionToken);
        return sendRequest(req);
    }

    public response getCartItemCount() {
        // For now, we'll get cart items and count them client-side
        // You can add a server endpoint later if needed
        response res = getCart();
        if (res.isSuccess()) {
            String message = res.getMessage();
            if (message.contains("empty")) {
                return new response(true, "📦 Cart is empty (0 items)");
            }
            // Count lines that represent items (simple heuristic)
            int count = message.split("\n").length - 3; // Subtract header/footer lines
            return new response(true, "📦 Cart contains " + Math.max(0, count) + " item(s)");
        }
        return res;
    }

    public response getItemDetails(String productId) {
        try {
            int id = Integer.parseInt(productId);
            return getProduct(id);
        } catch (NumberFormatException e) {
            return new response(false, "Invalid product ID format");
        }
    }

    public response clearCart() {
        // This would need a server endpoint - for now return not implemented
        return new response(false, "Clear cart feature not yet implemented on server");
    }

    public void close() {
        try {
            if (out    != null) out.close();
            if (in     != null) in.close();
            if (socket != null) socket.close();
            System.out.println("🔌 Connection closed");
        } catch (IOException e) {
            System.out.println("⚠️ Error closing connection: " + e.getMessage());
        }
    }
}