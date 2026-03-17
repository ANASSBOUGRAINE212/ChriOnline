
package protocol;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class request implements Serializable {
	public static final String REGISTER         = "REGISTER";
    public static final String LOGIN            = "LOGIN";
    public static final String LOGOUT           = "LOGOUT";
    public static final String GET_USER_INFO    = "GET_USER_INFO";
    public static final String GET_PROFILE      = "GET_PROFILE";
    public static final String UPDATE_PROFILE   = "UPDATE_PROFILE";
    public static final String CHANGE_PASSWORD  = "CHANGE_PASSWORD";
    
    public static final String SUCCESS          = "SUCCESS";
    public static final String ERROR            = "ERROR";
    public static final String ERR_USER_EXISTS  = "USERNAME_DEJA_PRIS";
    public static final String ERR_EMAIL_EXISTS = "EMAIL_DEJA_UTILISE";
    public static final String ERR_INVALID_DATA = "DONNEES_INVALIDES";
    public static final String ERR_SERVER       = "ERREUR_SERVEUR";
    public static final String SEP = "|";
    
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
    
    public Map<String, String> getParams() {
        return params;
    }
    
    public void setParams(Map<String, String> params) {
        this.params = params;
    }
}
