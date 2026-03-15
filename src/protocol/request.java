
package protocol;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class request implements Serializable {
    public static final String GET_USER_INFO    = "GET_USER_INFO";
    public static final String UPDATE_PROFILE   = "UPDATE_PROFILE";
    public static final String CHANGE_PASSWORD  = "CHANGE_PASSWORD";
    
    private String type;
    private String token;
    private Map<String, String> params = new HashMap<>();
    
    public request(String type) {
        this.type = type;
    }
    
    public String getType() {
        return type;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setParam(String key, String value) {
        params.put(key, value);
    }
    
    public String getParam(String key) {
        return params.get(key);
    }
}
