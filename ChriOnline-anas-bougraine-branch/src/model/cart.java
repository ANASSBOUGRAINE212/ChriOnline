package model;

import java.util.ArrayList;
import java.util.List;

public class cart {
    private String cartId;
    private String userId;
    private List<cartItem> items;
    private String createdAt;

    public cart() {
        this.items = new ArrayList<>();
    }

    public cart(String cartId, String userId, String createdAt) {
        this.cartId = cartId;
        this.userId = userId;
        this.createdAt = createdAt;
        this.items = new ArrayList<>();
    }

    public String getCartId() { return cartId; }
    public void setCartId(String cartId) { this.cartId = cartId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public List<cartItem> getItems() { return items; }
    public void setItems(List<cartItem> items) { this.items = items; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
