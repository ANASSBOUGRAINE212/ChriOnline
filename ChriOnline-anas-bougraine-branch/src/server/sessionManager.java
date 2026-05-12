package server;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class sessionManager {

    private static sessionManager instance;
    private final Map<String, String> tokenToUserId = new HashMap<>();

    private sessionManager() {
        // TEMP: hardcoded test sessions — remove once login is implemented
        tokenToUserId.put("test-token-alice", "u-001");
        tokenToUserId.put("test-token-admin", "u-003");
    }

    public static sessionManager getInstance() {
        if (instance == null) instance = new sessionManager();
        return instance;
    }

    public String getUserIdFromToken(String token) {
        return tokenToUserId.get(token);
    }

    // Your classmate will fill these in later
    public String createSession(String userId) {
        String token = UUID.randomUUID().toString();
        tokenToUserId.put(token, userId);
        return token;
    }

    public void removeSession(String token) {
        tokenToUserId.remove(token);
    }
}