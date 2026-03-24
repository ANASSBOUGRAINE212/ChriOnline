package protocol;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class request implements Serializable {
    public static final String REGISTER        = "REGISTER";
    public static final String LOGIN           = "LOGIN";
    public static final String LOGOUT          = "LOGOUT";
    public static final String GET_USER_INFO   = "GET_USER_INFO";
    public static final String GET_PROFILE     = "GET_PROFILE";
    public static final String UPDATE_PROFILE  = "UPDATE_PROFILE";
    public static final String CHANGE_PASSWORD = "CHANGE_PASSWORD";
    public static final String ADD_PRODUCT     = "ADD_PRODUCT";
    public static final String DELETE_PRODUCT  = "DELETE_PRODUCT";
    public static final String GET_PRODUCT     = "GET_PRODUCT";
    public static final String UPDATE_PRODUCT  = "UPDATE_PRODUCT";
    public static final String LIST_PRODUCTS   = "LIST_PRODUCTS";
    public static final String ADD_TO_CART     = "ADD_TO_CART";
    public static final String REMOVE_FROM_CART = "REMOVE_FROM_CART";
    public static final String GET_CART_ITEMS  = "GET_CART_ITEMS";
    public static final String GET_CART_TOTAL  = "GET_CART_TOTAL";

    private String type;
    private String token;
    private Map<String, String> params = new HashMap<>();

    public request(String type) { this.type = type; }

    public String getType()               { return type; }
    public void setToken(String token)    { this.token = token; }
    public String getToken()              { return token; }
    public void setParam(String key, String value) { params.put(key, value); }
    public String getParam(String key)    { return params.get(key); }
    public Map<String, String> getParams(){ return params; }
    public void setParams(Map<String, String> params) { this.params = params; }
}