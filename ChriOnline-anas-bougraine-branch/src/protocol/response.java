
package protocol;

import java.io.Serializable;

public class response implements Serializable {
    private boolean success;
    private String message;
    
    public response(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getMessage() {
        return message;
    }
}
